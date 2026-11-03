#!/usr/bin/env python3
"""Compare a complete, deterministic Android screenshot capture against reviewed PNGs.

Requires Pillow. Verification never writes to either input directory. Baseline
recording is a separate, explicit local command and is refused in CI.
"""

from __future__ import annotations

import argparse
from dataclasses import asdict, dataclass
from datetime import datetime, timezone
import hashlib
import html
import json
import math
import os
from pathlib import Path, PurePosixPath
import shutil
import sys
import tempfile
from typing import Any

from PIL import Image, ImageChops, ImageEnhance, ImageDraw


class CaptureError(ValueError):
    """An incomplete, unsafe, or incompatible screenshot capture."""


@dataclass(frozen=True)
class Thresholds:
    color_tolerance: int = 8
    max_changed_fraction: float = 0.0001
    max_changed_pixels: int = 64
    max_component_pixels: int = 8
    material_delta: int = 32

    def validate(self) -> None:
        if not 0 <= self.color_tolerance < self.material_delta <= 255:
            raise CaptureError("Require 0 <= color tolerance < material delta <= 255.")
        if not math.isfinite(self.max_changed_fraction) or not 0 <= self.max_changed_fraction <= 1:
            raise CaptureError("Changed fraction must be between 0 and 1.")
        if self.max_changed_pixels < 0 or self.max_component_pixels < 0:
            raise CaptureError("Pixel and connected-component limits must be nonnegative.")


def _positive_number(value: Any) -> bool:
    return isinstance(value, (int, float)) and not isinstance(value, bool) and math.isfinite(value) and value > 0


def _capture_path(value: Any) -> str:
    if not isinstance(value, str) or not value or "\\" in value or ":" in value:
        raise CaptureError(f"Invalid root-relative PNG path: {value!r}")
    path = PurePosixPath(value)
    if path.is_absolute() or any(part in ("", ".", "..") for part in value.split("/")):
        raise CaptureError(f"PNG path must stay inside its capture directory: {value!r}")
    if path.suffix.lower() != ".png":
        raise CaptureError(f"Capture must be a PNG: {value!r}")
    return value


def load_manifest(root: Path) -> dict[str, Any]:
    manifest_path = root / "manifest.json"
    if manifest_path.is_symlink():
        raise CaptureError("manifest.json must not be a symbolic link.")
    try:
        manifest = json.loads(manifest_path.read_text(encoding="utf-8-sig"))
    except (OSError, UnicodeError, json.JSONDecodeError) as error:
        raise CaptureError(f"Cannot read manifest.json: {error}") from error
    if not isinstance(manifest, dict) or type(manifest.get("schemaVersion")) is not int or manifest["schemaVersion"] != 1:
        raise CaptureError("manifest.json must declare schemaVersion: 1.")
    profile = manifest.get("profile")
    required_profile = ("apiLevel", "viewportWidthDp", "viewportHeightDp", "density")
    if not isinstance(profile, dict) or not all(_positive_number(profile.get(key)) for key in required_profile):
        raise CaptureError("profile requires positive apiLevel, viewportWidthDp, viewportHeightDp, and density.")
    captures = manifest.get("captures")
    if not isinstance(captures, list) or not captures:
        raise CaptureError("captures must be a nonempty list.")
    seen: set[str] = set()
    for capture in captures:
        if not isinstance(capture, dict):
            raise CaptureError("Every capture must be an object.")
        file = _capture_path(capture.get("file"))
        if file.casefold() in seen:
            raise CaptureError(f"Duplicate/case-conflicting capture path: {file}")
        seen.add(file.casefold())
        if not isinstance(capture.get("screen"), str) or not capture["screen"].strip():
            raise CaptureError(f"{file}: screen is required.")
        if capture.get("locale") not in ("ar", "en", "fr"):
            raise CaptureError(f"{file}: locale must be ar, en, or fr.")
        if capture.get("theme") not in ("light", "dark"):
            raise CaptureError(f"{file}: theme must be light or dark.")
        if not _positive_number(capture.get("fontScale")):
            raise CaptureError(f"{file}: fontScale must be positive.")
    return manifest


def png_inventory(root: Path) -> set[str]:
    if not root.is_dir() or root.is_symlink():
        raise CaptureError(f"Capture directory is missing or is a symbolic link: {root}")
    files: set[str] = set()
    folded: set[str] = set()
    for path in root.rglob("*"):
        if path.is_symlink():
            raise CaptureError(f"Capture directory contains a symbolic link: {path.relative_to(root)}")
        if path.is_file() and path.suffix.lower() == ".png":
            relative = path.relative_to(root).as_posix()
            if relative.casefold() in folded:
                raise CaptureError(f"Case-conflicting PNG files: {relative}")
            folded.add(relative.casefold())
            files.add(relative)
    return files


def _read_png(path: Path) -> Image.Image:
    try:
        with Image.open(path) as image:
            if image.format != "PNG":
                raise CaptureError("File is not a PNG.")
            image.verify()
        with Image.open(path) as image:
            image.load()
            return image.convert("RGBA")
    except (OSError, ValueError, SyntaxError, Image.DecompressionBombError) as error:
        raise CaptureError(f"Cannot decode PNG: {error}") from error


def _profile_size(manifest: dict[str, Any]) -> tuple[int, int]:
    profile = manifest["profile"]
    return (
        round(profile["viewportWidthDp"] * profile["density"]),
        round(profile["viewportHeightDp"] * profile["density"]),
    )


def _limit_exceeding_component(mask: Image.Image, limit: int) -> int:
    """Return the largest component, stopping at limit+1 (enough to reject)."""
    width, height = mask.size
    pixels = bytearray(mask.tobytes())
    largest = 0
    for start, value in enumerate(pixels):
        if not value:
            continue
        pixels[start] = 0
        queue = [start]
        size = 0
        while queue:
            position = queue.pop()
            size += 1
            if size > limit:
                return size
            x = position % width
            neighbors = []
            if x:
                neighbors.append(position - 1)
            if x + 1 < width:
                neighbors.append(position + 1)
            if position >= width:
                neighbors.append(position - width)
            if position + width < width * height:
                neighbors.append(position + width)
            for neighbor in neighbors:
                if pixels[neighbor]:
                    pixels[neighbor] = 0
                    queue.append(neighbor)
        largest = max(largest, size)
    return largest


def compare_images(expected: Image.Image, actual: Image.Image, thresholds: Thresholds) -> tuple[dict[str, Any], Image.Image]:
    """RGBA max-channel deltas; meaningful color/alpha changes always reject."""
    thresholds.validate()
    if expected.size != actual.size:
        raise CaptureError(f"Dimensions differ: expected {expected.size}, actual {actual.size}.")
    delta = ImageChops.difference(expected.convert("RGBA"), actual.convert("RGBA"))
    channels = delta.split()
    maximum = channels[0]
    for channel in channels[1:]:
        maximum = ImageChops.lighter(maximum, channel)
    histogram = maximum.histogram()
    changed = sum(histogram[thresholds.color_tolerance + 1:])
    material = sum(histogram[thresholds.material_delta + 1:])
    total = expected.width * expected.height
    allowed = min(thresholds.max_changed_pixels, math.floor(total * thresholds.max_changed_fraction))
    mask = maximum.point(lambda value: 255 if value > thresholds.color_tolerance else 0)
    component = _limit_exceeding_component(mask, thresholds.max_component_pixels) if changed else 0
    errors = []
    if material:
        errors.append(f"{material} pixel(s) exceed material color delta {thresholds.material_delta}; even one rejects.")
    if changed > allowed:
        errors.append(f"Changed pixels {changed} exceed allowed {allowed} ({thresholds.max_changed_fraction:.6%}, capped at {thresholds.max_changed_pixels}).")
    if component > thresholds.max_component_pixels:
        errors.append(f"A connected changed area has at least {component} pixels; limit is {thresholds.max_component_pixels}.")
    metrics = {
        "width": expected.width, "height": expected.height, "totalPixels": total,
        "changedPixels": changed, "changedFraction": changed / total,
        "allowedChangedPixels": allowed, "materialPixels": material,
        "largestComponentAtLeast": component,
        "maximumChannelDelta": next((index for index in range(255, -1, -1) if histogram[index]), 0),
        "errors": errors,
    }
    context = ImageEnhance.Brightness(expected.convert("RGB")).enhance(0.35)
    diff = Image.composite(Image.new("RGB", expected.size, "#ff006e"), context, mask)
    return metrics, diff


def _placeholder(size: tuple[int, int], label: str) -> Image.Image:
    image = Image.new("RGB", size, "#351926")
    draw = ImageDraw.Draw(image)
    draw.multiline_text((12, 12), label, fill="white", spacing=6)
    return image


def _assert_separate_paths(expected: Path, actual: Path, report: Path | None = None) -> None:
    paths = [expected.resolve(), actual.resolve()]
    if report is not None:
        paths.append(report.resolve())
    for index, left in enumerate(paths):
        for right in paths[index + 1:]:
            if left == right or left in right.parents or right in left.parents:
                raise CaptureError(f"Expected, actual, and report directories must be separate, non-nested paths: {left} / {right}")


def _manifest_file_map(manifest: dict[str, Any] | None) -> dict[str, dict[str, Any]]:
    return {capture["file"]: capture for capture in manifest["captures"]} if manifest else {}


def verify(expected_root: Path, actual_root: Path, report_root: Path, thresholds: Thresholds | None = None) -> dict[str, Any]:
    thresholds = thresholds or Thresholds()
    thresholds.validate()
    for root in (expected_root, actual_root, report_root):
        if root.is_symlink():
            raise CaptureError(f"Input/report root must not be a symbolic link: {root}")
    if report_root.is_dir() and any(path.is_symlink() for path in report_root.rglob("*")):
        raise CaptureError("Report directory must not contain symbolic links.")
    expected_root, actual_root, report_root = (path.resolve() for path in (expected_root, actual_root, report_root))
    _assert_separate_paths(expected_root, actual_root, report_root)
    report: dict[str, Any] = {
        "schemaVersion": 1, "generatedAt": datetime.now(timezone.utc).isoformat(),
        "expectedRoot": str(expected_root), "actualRoot": str(actual_root),
        "thresholds": asdict(thresholds), "globalErrors": [], "captures": [],
    }
    manifests: dict[str, dict[str, Any] | None] = {}
    inventories: dict[str, set[str]] = {}
    for label, root in (("expected", expected_root), ("actual", actual_root)):
        try:
            manifests[label] = load_manifest(root)
        except CaptureError as error:
            manifests[label] = None
            report["globalErrors"].append(f"{label}: {error}")
        try:
            inventories[label] = png_inventory(root)
        except CaptureError as error:
            inventories[label] = set()
            report["globalErrors"].append(f"{label}: {error}")
        listed = set(_manifest_file_map(manifests[label]))
        if manifests[label] is not None:
            for file in sorted(listed - inventories[label]):
                report["globalErrors"].append(f"{label}: missing manifested PNG {file}")
            for file in sorted(inventories[label] - listed):
                report["globalErrors"].append(f"{label}: unexpected/unmanifested PNG {file}")
    expected_manifest, actual_manifest = manifests["expected"], manifests["actual"]
    expected_files, actual_files = _manifest_file_map(expected_manifest), _manifest_file_map(actual_manifest)
    if expected_manifest and actual_manifest:
        if expected_manifest["profile"] != actual_manifest["profile"]:
            report["globalErrors"].append("Capture profiles differ; use the reviewed emulator/API/density/viewport.")
        for file in sorted(set(expected_files) - set(actual_files)):
            report["globalErrors"].append(f"Actual manifest is missing expected capture {file}")
        for file in sorted(set(actual_files) - set(expected_files)):
            report["globalErrors"].append(f"Actual manifest contains unexpected capture {file}")
    report_root.mkdir(parents=True, exist_ok=True)
    for file in sorted(set(expected_files) | set(actual_files) | inventories["expected"] | inventories["actual"]):
        capture: dict[str, Any] = {"file": file, "errors": [], "images": {}, "metrics": None}
        if expected_files.get(file) != actual_files.get(file):
            capture["errors"].append("Capture metadata differs or is absent (screen/locale/theme/fontScale/file).")
        asset_dir = report_root / "assets" / hashlib.sha256(file.encode("utf-8")).hexdigest()[:16]
        asset_dir.mkdir(parents=True, exist_ok=True)
        images: dict[str, Image.Image] = {}
        for label, root, manifest in (("expected", expected_root, expected_manifest), ("actual", actual_root, actual_manifest)):
            if file not in inventories[label]:
                capture["errors"].append(f"{label} PNG is missing.")
                continue
            try:
                image = _read_png(root / file)
                images[label] = image
                shutil.copyfile(root / file, asset_dir / f"{label}.png")
                capture["images"][label] = (asset_dir / f"{label}.png").relative_to(report_root).as_posix()
                if manifest and image.size != _profile_size(manifest):
                    capture["errors"].append(f"{label} dimensions {image.size} disagree with declared profile {_profile_size(manifest)}.")
            except CaptureError as error:
                capture["errors"].append(f"{label}: {error}")
        if "expected" in images and "actual" in images and images["expected"].size == images["actual"].size:
            metrics, diff = compare_images(images["expected"], images["actual"], thresholds)
            capture["metrics"] = metrics
            capture["errors"].extend(metrics["errors"])
        else:
            sizes = [image.size for image in images.values()]
            size = (max((size[0] for size in sizes), default=720), max((size[1] for size in sizes), default=1480))
            if len(images) == 2:
                capture["errors"].append(f"Dimensions differ: expected {images['expected'].size}, actual {images['actual'].size}.")
            diff = _placeholder(size, "IMAGE COMPARISON FAILED\nMissing, invalid, or differently sized PNG.\nSee report for details.")
            for label in ("expected", "actual"):
                if label not in capture["images"]:
                    _placeholder(size, f"{label.upper()} PNG MISSING OR INVALID").save(asset_dir / f"{label}.png")
                    capture["images"][label] = (asset_dir / f"{label}.png").relative_to(report_root).as_posix()
        diff.save(asset_dir / "diff.png")
        capture["images"]["diff"] = (asset_dir / "diff.png").relative_to(report_root).as_posix()
        capture["status"] = "failed" if capture["errors"] else "passed"
        report["captures"].append(capture)
    failed = sum(capture["status"] == "failed" for capture in report["captures"])
    report["summary"] = {"total": len(report["captures"]), "passed": len(report["captures"]) - failed, "failed": failed}
    report["status"] = "failed" if failed or report["globalErrors"] else "passed"
    (report_root / "report.json").write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    (report_root / "index.html").write_text(_render_html(report), encoding="utf-8")
    return report


def _render_html(report: dict[str, Any]) -> str:
    escape = html.escape
    errors = "".join(f"<li>{escape(error)}</li>" for error in report["globalErrors"])
    cards = []
    # Failures appear first. A successful capture still has all three artifacts.
    for capture in sorted(report["captures"], key=lambda item: (item["status"] != "failed", item["file"])):
        figures = "".join(
            f'<figure><figcaption>{label.title()}</figcaption><a href="{escape(capture["images"][label], quote=True)}">'
            f'<img loading="lazy" src="{escape(capture["images"][label], quote=True)}" alt="{label} screenshot"></a></figure>'
            for label in ("expected", "actual", "diff")
        )
        details = "".join(f"<li>{escape(error)}</li>" for error in capture["errors"])
        metrics = capture["metrics"]
        stats = (f'{metrics["width"]} × {metrics["height"]}; changed {metrics["changedPixels"]}/{metrics["allowedChangedPixels"]} allowed '
                 f'({metrics["changedFraction"]:.6%}); material {metrics["materialPixels"]}; '
                 f'connected area ≥{metrics["largestComponentAtLeast"]} px') if metrics else "Pixel comparison unavailable."
        cards.append(f'<section class="{capture["status"]}"><h2>{escape(capture["file"])} · {capture["status"].upper()}</h2>'
                     f'<p>{escape(stats)}</p><ul>{details}</ul><div class="images">{figures}</div></section>')
    return f'''<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Screenshot regression report</title><style>
body{{font:15px/1.5 system-ui,sans-serif;background:#f5f7f9;color:#17202a;margin:0;padding:24px}}
main{{max-width:1500px;margin:auto}}h1{{margin:0}}h2{{font-size:18px;overflow-wrap:anywhere}}section{{background:white;border:1px solid #cbd3da;border-radius:10px;padding:16px;margin:20px 0}}
section.failed{{border-left:6px solid #b32448}}section.passed{{border-left:6px solid #19734b}}.images{{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px}}
figure{{margin:0}}figcaption{{font-weight:600;margin-bottom:8px}}img{{width:100%;height:auto;border:1px solid #d0d7de}}code{{overflow-wrap:anywhere}}li{{margin:4px 0}}
@media(max-width:700px){{body{{padding:12px}}.images{{grid-template-columns:1fr}}}}
</style><main><h1>Screenshot regression: {report["status"].upper()}</h1>
<p>{report["summary"]["passed"]} passed · {report["summary"]["failed"]} failed · {len(report["globalErrors"])} capture-set/profile error(s).</p>
<p>Expected: <code>{escape(report["expectedRoot"])}</code><br>Actual: <code>{escape(report["actualRoot"])}</code></p>
<p>Magenta highlights pixels exceeding color tolerance. The dimmed expected image provides context. Click an image for its original resolution.</p>
<p>Limits: {escape(json.dumps(report["thresholds"]))}. Material color changes reject even a single pixel; area and connected-region limits also apply.</p>
<ul>{errors}</ul>{"".join(cards)}</main></html>'''


def record_baseline(actual_root: Path, expected_root: Path, replace: bool = False) -> int:
    """Explicit local baseline creation/update; never called by verification."""
    if any(os.environ.get(key, "").lower() not in ("", "false", "0", "no") for key in ("CI", "GITHUB_ACTIONS")):
        raise CaptureError("Baseline recording is disabled in CI. Record locally, inspect repeat verification, and commit reviewed PNGs.")
    if actual_root.is_symlink() or expected_root.is_symlink():
        raise CaptureError("Capture/baseline roots must not be symbolic links.")
    actual_root, expected_root = actual_root.resolve(), expected_root.resolve()
    _assert_separate_paths(expected_root, actual_root)
    manifest = load_manifest(actual_root)
    actual_files = png_inventory(actual_root)
    listed = set(_manifest_file_map(manifest))
    if actual_files != listed:
        raise CaptureError(f"Actual PNG set differs from manifest. Missing: {sorted(listed - actual_files)}; unexpected: {sorted(actual_files - listed)}")
    for file in sorted(listed):
        image = _read_png(actual_root / file)
        if image.size != _profile_size(manifest):
            raise CaptureError(f"{file}: dimensions {image.size} disagree with profile {_profile_size(manifest)}.")
    if expected_root.exists():
        old_files = png_inventory(expected_root)
        if (old_files or (expected_root / "manifest.json").exists()) and not replace:
            raise CaptureError("Baseline already exists. Use record --replace only after reviewing the actual capture.")
    else:
        old_files = set()
    expected_root.mkdir(parents=True, exist_ok=True)
    # Stage every file before replacing anything. Only PNGs and manifest.json are
    # managed; README files and other review notes remain untouched.
    with tempfile.TemporaryDirectory(prefix=".screenshot-record-", dir=expected_root.parent) as staging:
        stage_root = Path(staging).resolve()
        if stage_root.parent != expected_root.parent:
            raise CaptureError("Unexpected staging directory.")
        for file in sorted(listed):
            staged = stage_root / file
            staged.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(actual_root / file, staged)
        (stage_root / "manifest.json").write_text(json.dumps(manifest, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
        for file in sorted(listed):
            target = expected_root / file
            if expected_root not in target.resolve().parents:
                raise CaptureError(f"Unsafe baseline target: {file}")
            target.parent.mkdir(parents=True, exist_ok=True)
            os.replace(stage_root / file, target)
        for file in sorted(old_files - listed):
            target = expected_root / file
            if expected_root not in target.resolve().parents:
                raise CaptureError(f"Unsafe stale baseline target: {file}")
            target.unlink()
        os.replace(stage_root / "manifest.json", expected_root / "manifest.json")
    return len(listed)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    verify_parser = commands.add_parser("verify", help="Compare PNGs; never change baselines.")
    verify_parser.add_argument("--expected", type=Path, required=True)
    verify_parser.add_argument("--actual", type=Path, required=True)
    verify_parser.add_argument("--report", type=Path, required=True)
    verify_parser.add_argument("--color-tolerance", type=int, default=8)
    verify_parser.add_argument("--max-changed-fraction", type=float, default=0.0001)
    verify_parser.add_argument("--max-changed-pixels", type=int, default=64)
    verify_parser.add_argument("--max-component-pixels", type=int, default=8)
    verify_parser.add_argument("--material-delta", type=int, default=32)
    record_parser = commands.add_parser("record", help="Explicit local baseline recording; prohibited in CI.")
    record_parser.add_argument("--actual", type=Path, required=True)
    record_parser.add_argument("--expected", type=Path, required=True)
    record_parser.add_argument("--replace", action="store_true", help="Replace existing reviewed PNGs; inspect and commit the diff.")
    args = parser.parse_args(argv)
    try:
        if args.command == "record":
            count = record_baseline(args.actual, args.expected, args.replace)
            print(f"Recorded {count} baseline PNGs at {args.expected.resolve()}. Review these files and verify an independent repeat capture before committing.")
            return 0
        thresholds = Thresholds(args.color_tolerance, args.max_changed_fraction, args.max_changed_pixels, args.max_component_pixels, args.material_delta)
        report = verify(args.expected, args.actual, args.report, thresholds)
        print(f"Screenshot verification {report['status'].upper()}: {report['summary']['passed']} passed, {report['summary']['failed']} failed, {len(report['globalErrors'])} set/profile error(s).")
        for error in report["globalErrors"]:
            print(f"  {error}")
        for capture in report["captures"]:
            if capture["status"] == "failed":
                print(f"  {capture['file']}: {'; '.join(capture['errors'])}")
        print(f"Report: {(args.report / 'index.html').resolve()}")
        return 0 if report["status"] == "passed" else 1
    except (CaptureError, OSError) as error:
        print(f"Screenshot tool error: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
