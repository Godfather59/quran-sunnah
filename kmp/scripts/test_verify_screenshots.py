"""Synthetic regression tests for the host PNG gate (no Android/Gradle needed)."""

from __future__ import annotations

import contextlib
import hashlib
import io
import json
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

from PIL import Image, ImageDraw

import verify_screenshots as gate


class ScreenshotGateTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temp = tempfile.TemporaryDirectory(prefix="screenshot-gate-test-")
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.expected = self.root / "expected"
        self.actual = self.root / "actual"
        self.report = self.root / "report"
        self.file = "reader-ar-light-100.png"
        self.manifest = {
            "schemaVersion": 1,
            "profile": {"apiLevel": 35, "viewportWidthDp": 64, "viewportHeightDp": 64, "density": 2.0},
            "captures": [{"file": self.file, "screen": "reader", "locale": "ar", "theme": "light", "fontScale": 1.0}],
        }
        for folder in (self.expected, self.actual):
            folder.mkdir()
            self.write_manifest(folder)
            image = Image.new("RGB", (128, 128), "white")
            # A thin high-contrast shape represents text, rather than merely
            # changing all pixels by a constant color offset.
            draw = ImageDraw.Draw(image)
            draw.rectangle((30, 30, 32, 64), fill="#202020")
            draw.rectangle((32, 61, 52, 64), fill="#202020")
            image.save(folder / self.file)

    def write_manifest(self, folder: Path, manifest: dict | None = None) -> None:
        (folder / "manifest.json").write_text(json.dumps(manifest or self.manifest), encoding="utf-8")

    def change_pixels(self, points: list[tuple[int, int]], color: tuple[int, int, int]) -> None:
        with Image.open(self.actual / self.file) as original:
            image = original.convert("RGB")
        for point in points:
            image.putpixel(point, color)
        image.save(self.actual / self.file)

    def verify(self) -> dict:
        return gate.verify(self.expected, self.actual, self.report)

    def test_independent_identical_capture_passes_and_emits_all_artifacts(self) -> None:
        report = self.verify()
        self.assertEqual("passed", report["status"])
        self.assertEqual({"total": 1, "passed": 1, "failed": 0}, report["summary"])
        capture = report["captures"][0]
        for label in ("expected", "actual", "diff"):
            self.assertTrue((self.report / capture["images"][label]).is_file())
        self.assertEqual("passed", json.loads((self.report / "report.json").read_text())["status"])
        self.assertIn("Screenshot regression: PASSED", (self.report / "index.html").read_text())

    def test_small_antialias_color_changes_are_tolerated(self) -> None:
        # The contour is still present; small per-channel variations do not
        # become noisy failures even when many edge pixels vary slightly.
        self.change_pixels([(30, y) for y in range(30, 65)], (39, 37, 34))
        report = self.verify()
        self.assertEqual("passed", report["status"])
        self.assertEqual(0, report["captures"][0]["metrics"]["changedPixels"])

    def test_one_isolated_small_noise_pixel_can_pass_within_all_limits(self) -> None:
        self.change_pixels([(100, 100)], (243, 243, 243))
        report = self.verify()
        self.assertEqual("passed", report["status"])
        self.assertEqual(1, report["captures"][0]["metrics"]["changedPixels"])

    def test_even_one_material_pixel_change_rejects(self) -> None:
        self.change_pixels([(31, 40)], (255, 255, 255))
        report = self.verify()
        self.assertEqual("failed", report["status"])
        self.assertEqual(1, report["captures"][0]["metrics"]["materialPixels"])

    def test_clipped_text_stroke_rejects_even_below_whole_image_area_budget(self) -> None:
        self.change_pixels([(31, y) for y in range(30, 37)], (255, 255, 255))
        thresholds = gate.Thresholds(max_changed_fraction=0.01)
        report = gate.verify(self.expected, self.actual, self.report, thresholds)
        self.assertEqual("failed", report["status"])
        self.assertEqual(7, report["captures"][0]["metrics"]["materialPixels"])

    def test_connected_low_contrast_shape_change_rejects(self) -> None:
        self.change_pixels([(100, y) for y in range(50, 62)], (235, 235, 235))
        thresholds = gate.Thresholds(max_changed_fraction=0.01)
        report = gate.verify(self.expected, self.actual, self.report, thresholds)
        metrics = report["captures"][0]["metrics"]
        self.assertEqual(0, metrics["materialPixels"])
        self.assertGreater(metrics["largestComponentAtLeast"], thresholds.max_component_pixels)
        self.assertEqual("failed", report["status"])

    def test_distributed_changes_reject_by_area(self) -> None:
        self.change_pixels([(x, 100) for x in (80, 85, 90, 95)], (240, 240, 240))
        report = self.verify()
        metrics = report["captures"][0]["metrics"]
        self.assertEqual(1, metrics["allowedChangedPixels"])
        self.assertEqual(1, metrics["largestComponentAtLeast"])
        self.assertEqual("failed", report["status"])

    def test_dimension_change_rejects_and_report_still_has_comparison_images(self) -> None:
        Image.new("RGB", (128, 127), "white").save(self.actual / self.file)
        report = self.verify()
        self.assertEqual("failed", report["status"])
        self.assertTrue(any("Dimensions differ" in error for error in report["captures"][0]["errors"]))
        for path in report["captures"][0]["images"].values():
            self.assertTrue((self.report / path).exists())

    def test_both_images_at_wrong_profile_dimensions_reject(self) -> None:
        for folder in (self.expected, self.actual):
            Image.new("RGB", (64, 64), "white").save(folder / self.file)
        report = self.verify()
        self.assertEqual("failed", report["status"])
        self.assertTrue(any("declared profile" in error for error in report["captures"][0]["errors"]))

    def test_missing_png_rejects_with_readable_report(self) -> None:
        (self.actual / self.file).unlink()
        report = self.verify()
        self.assertEqual("failed", report["status"])
        self.assertTrue(any("missing manifested PNG" in error for error in report["globalErrors"]))
        self.assertTrue((self.report / "index.html").exists())

    def test_unexpected_png_rejects_even_when_expected_image_matches(self) -> None:
        Image.new("RGB", (128, 128), "white").save(self.actual / "unlisted.png")
        report = self.verify()
        self.assertEqual("failed", report["status"])
        self.assertTrue(any("unexpected/unmanifested PNG unlisted.png" in error for error in report["globalErrors"]))

    def test_missing_manifest_rejects_instead_of_recording_baseline(self) -> None:
        (self.expected / "manifest.json").unlink()
        report = self.verify()
        self.assertEqual("failed", report["status"])
        self.assertFalse((self.expected / "manifest.json").exists())

    def test_mismatched_manifest_capture_set_rejects(self) -> None:
        manifest = json.loads(json.dumps(self.manifest))
        manifest["captures"][0]["file"] = "reader-fr-light-100.png"
        (self.actual / self.file).rename(self.actual / manifest["captures"][0]["file"])
        self.write_manifest(self.actual, manifest)
        report = self.verify()
        self.assertEqual("failed", report["status"])
        self.assertTrue(any("missing expected capture" in error for error in report["globalErrors"]))

    def test_matching_pixels_with_wrong_locale_metadata_reject(self) -> None:
        manifest = json.loads(json.dumps(self.manifest))
        manifest["captures"][0]["locale"] = "fr"
        self.write_manifest(self.actual, manifest)
        self.assertEqual("failed", self.verify()["status"])

    def test_matching_pixels_from_different_api_profile_reject(self) -> None:
        manifest = json.loads(json.dumps(self.manifest))
        manifest["profile"]["apiLevel"] = 34
        self.write_manifest(self.actual, manifest)
        report = self.verify()
        self.assertEqual("failed", report["status"])
        self.assertTrue(any("profiles differ" in error for error in report["globalErrors"]))

    def test_additional_fingerprint_and_fixture_profile_keys_are_compared_exactly(self) -> None:
        manifest = json.loads(json.dumps(self.manifest))
        manifest["profile"].update(imageFingerprint="aosp/api35/reviewed", fixtureMillis=1768478400000)
        for folder in (self.expected, self.actual):
            self.write_manifest(folder, manifest)
        self.assertEqual("passed", self.verify()["status"])
        manifest["profile"]["imageFingerprint"] = "aosp/api35/different"
        self.write_manifest(self.actual, manifest)
        self.assertEqual("failed", self.verify()["status"])

    def test_corrupted_png_rejects_and_generates_placeholders(self) -> None:
        (self.actual / self.file).write_bytes(b"not a PNG")
        report = self.verify()
        self.assertEqual("failed", report["status"])
        self.assertTrue(any("Cannot decode PNG" in error for error in report["captures"][0]["errors"]))
        self.assertEqual(3, len(report["captures"][0]["images"]))

    def test_alpha_change_is_not_ignored(self) -> None:
        with Image.open(self.actual / self.file) as original:
            image = original.convert("RGBA")
        image.putpixel((100, 100), (255, 255, 255, 0))
        image.save(self.actual / self.file)
        self.assertEqual("failed", self.verify()["status"])

    def test_verification_never_changes_baseline_bytes(self) -> None:
        before = {path.name: hashlib.sha256(path.read_bytes()).hexdigest() for path in self.expected.iterdir()}
        self.change_pixels([(31, 40)], (255, 255, 255))
        self.assertEqual("failed", self.verify()["status"])
        after = {path.name: hashlib.sha256(path.read_bytes()).hexdigest() for path in self.expected.iterdir()}
        self.assertEqual(before, after)

    def test_traversal_and_duplicate_manifest_paths_reject(self) -> None:
        manifest = json.loads(json.dumps(self.manifest))
        manifest["captures"][0]["file"] = "../outside.png"
        self.write_manifest(self.actual, manifest)
        with self.assertRaisesRegex(gate.CaptureError, "inside"):
            gate.load_manifest(self.actual)
        manifest["captures"][0]["file"] = self.file
        manifest["captures"].append(dict(manifest["captures"][0]))
        self.write_manifest(self.actual, manifest)
        with self.assertRaisesRegex(gate.CaptureError, "Duplicate"):
            gate.load_manifest(self.actual)

    def test_record_is_explicit_and_replace_preserves_review_notes(self) -> None:
        with patch.dict(os.environ, {"CI": "false", "GITHUB_ACTIONS": "false"}):
            fresh = self.root / "new-baseline"
            self.assertEqual(1, gate.record_baseline(self.actual, fresh))
            self.assertEqual((self.actual / self.file).read_bytes(), (fresh / self.file).read_bytes())
            self.assertEqual("passed", gate.verify(fresh, self.actual, self.report)["status"])
            with self.assertRaisesRegex(gate.CaptureError, "--replace"):
                gate.record_baseline(self.actual, fresh)
            (fresh / "README.md").write_text("Review notes", encoding="utf-8")
            Image.new("RGB", (128, 128), "white").save(fresh / "stale.png")
            self.assertEqual(1, gate.record_baseline(self.actual, fresh, replace=True))
            self.assertFalse((fresh / "stale.png").exists())
            self.assertEqual("Review notes", (fresh / "README.md").read_text())

    def test_record_is_prohibited_in_ci_even_with_replace(self) -> None:
        with patch.dict(os.environ, {"CI": "true"}):
            with self.assertRaisesRegex(gate.CaptureError, "disabled in CI"):
                gate.record_baseline(self.actual, self.expected, replace=True)

    def test_cli_returns_nonzero_for_changed_and_missing_images(self) -> None:
        arguments = ["verify", "--expected", str(self.expected), "--actual", str(self.actual), "--report", str(self.report)]
        with contextlib.redirect_stdout(io.StringIO()):
            self.assertEqual(0, gate.main(arguments))
            self.change_pixels([(31, 40)], (255, 255, 255))
            self.assertEqual(1, gate.main(arguments))
            (self.actual / self.file).unlink()
            self.assertEqual(1, gate.main(arguments))

    def test_report_directory_cannot_pollute_capture_inventory(self) -> None:
        with self.assertRaisesRegex(gate.CaptureError, "non-nested"):
            gate.verify(self.expected, self.actual, self.actual / "report")


if __name__ == "__main__":
    unittest.main(verbosity=2)
