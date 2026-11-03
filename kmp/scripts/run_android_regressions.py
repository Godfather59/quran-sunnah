#!/usr/bin/env python3
"""Run prebuilt Android UI regressions on one disposable API 35 emulator."""

from __future__ import annotations

import argparse
from contextlib import contextmanager
import json
from pathlib import Path
import re
import shutil
import subprocess
import sys
import time
from typing import Iterator


APP_ID = "com.godfather59.quransunnah"
VISUAL_CLASS = f"{APP_ID}.VisualRegressionTest"
RUNNER = f"{APP_ID}.test/androidx.test.runner.AndroidJUnitRunner"
ANIMATION_KEYS = ("window_animation_scale", "transition_animation_scale", "animator_duration_scale")
RADIO_KEYS = ("wifi_on", "mobile_data")
COMMAND_TIMEOUT_SECONDS = 900


class RegressionError(RuntimeError):
    """The emulator or one regression suite did not satisfy the contract."""


def as_text(value: str | bytes | None) -> str:
    if isinstance(value, bytes):
        return value.decode("utf-8", errors="replace")
    return value or ""


class Adb:
    def __init__(self, executable: str, serial: str | None = None) -> None:
        self.executable = executable
        self.serial = serial

    def run(self, *args: str, timeout: int = 60, log: Path | None = None) -> str:
        command = [self.executable]
        if self.serial:
            command += ["-s", self.serial]
        command += list(args)
        try:
            result = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                    encoding="utf-8", errors="replace", timeout=timeout, check=False)
            output = result.stdout
        except subprocess.TimeoutExpired as error:
            output = as_text(error.stdout) + f"\nHost timeout after {timeout} seconds.\n"
            if log:
                log.write_text(output, encoding="utf-8")
            raise RegressionError(f"ADB command timed out; see {log or 'command output'}") from error
        except OSError as error:
            raise RegressionError(f"Cannot execute ADB: {error}") from error
        if log:
            log.write_text(output, encoding="utf-8")
        if result.returncode:
            raise RegressionError(f"ADB failed ({result.returncode}): {' '.join(args)}\n{output[-4000:]}")
        return output

    def shell(self, *args: str) -> str:
        return self.run("shell", *args).strip()


def select_emulator(devices_output: str, requested_serial: str | None) -> str:
    connected = []
    for line in devices_output.splitlines():
        parts = line.split()
        if len(parts) >= 2 and parts[1] == "device":
            connected.append(parts[0])
    if requested_serial:
        if requested_serial not in connected:
            raise RegressionError(f"Requested emulator {requested_serial!r} is not connected and ready")
        serial = requested_serial
    else:
        if len(connected) != 1:
            raise RegressionError("Connect exactly one emulator, or select one with --serial")
        serial = connected[0]
    if not re.fullmatch(r"emulator-\d+", serial):
        raise RegressionError(f"Refusing non-emulator device {serial!r}; use a disposable AVD")
    return serial


def validate_emulator(adb: Adb) -> None:
    if adb.shell("getprop", "ro.kernel.qemu") != "1":
        raise RegressionError("Refusing a device that is not an Android emulator")
    api_level = adb.shell("getprop", "ro.build.version.sdk")
    if api_level != "35":
        raise RegressionError(f"API 35 is required; selected emulator reports API {api_level}")


def instrumentation_count(output: str, expected_count: int | None = None) -> int:
    failure = re.search(
        r"FAILURES!!!|INSTRUMENTATION_FAILED|INSTRUMENTATION_ABORTED|Process crashed|"
        r"INSTRUMENTATION_RESULT:\s*shortMsg\s*=|INSTRUMENTATION_STATUS_CODE:\s*-[12]\b",
        output, re.IGNORECASE,
    )
    matches = re.findall(r"^\s*OK\s*\(\s*(\d+)\s+tests?\s*\)\s*$", output, re.MULTILINE)
    if failure or len(matches) != 1 or int(matches[0]) <= 0:
        raise RegressionError("Instrumentation did not report a clean, positive OK test count")
    count = int(matches[0])
    if expected_count is not None and count != expected_count:
        raise RegressionError(f"Expected {expected_count} tests, instrumentation reported {count}")
    return count


def reset_visual_output(kmp_root: Path, output: Path) -> None:
    """Only the known build capture directory can be recursively replaced."""
    root = kmp_root.resolve(strict=True)
    expected = root / "androidApp" / "build" / "outputs" / "visual-regression"
    if output.absolute() != expected or output.resolve() != expected:
        raise RegressionError("Refusing to clear a path other than the fixed build capture directory")
    if output.exists():
        if not output.is_dir() or output.is_symlink():
            raise RegressionError("Capture output must be an ordinary directory")
        shutil.rmtree(output)
    output.mkdir(parents=True)


def restore_setting(adb: Adb, key: str, value: str) -> None:
    if value == "null":
        adb.shell("settings", "delete", "global", key)
    else:
        adb.shell("settings", "put", "global", key, value)


def await_disconnected(adb: Adb, timeout_seconds: float = 30) -> None:
    deadline = time.monotonic() + timeout_seconds
    while True:
        state = adb.shell("dumpsys", "connectivity")
        if re.search(r"Active default network:\s*(?:none|null)\b", state, re.IGNORECASE):
            return
        if time.monotonic() >= deadline:
            raise RegressionError("Emulator still has an active default network after disabling Wi-Fi/mobile data")
        time.sleep(0.25)


@contextmanager
def offline_emulator_settings(adb: Adb) -> Iterator[None]:
    # Read the complete snapshot before the first mutation. Preserve absent
    # settings, too, rather than silently replacing them with guessed defaults.
    original = {key: adb.shell("settings", "get", "global", key)
                for key in (*RADIO_KEYS, *ANIMATION_KEYS)}
    try:
        adb.shell("svc", "wifi", "disable")
        adb.shell("svc", "data", "disable")
        for key in ANIMATION_KEYS:
            adb.shell("settings", "put", "global", key, "0")
        await_disconnected(adb)
        yield
    finally:
        unwinding_failure = sys.exc_info()[0] is not None
        errors = []
        # A failed restoration must not stop restoration of the other settings.
        for radio, key in (("wifi", "wifi_on"), ("data", "mobile_data")):
            try:
                value = original[key]
                adb.shell("svc", radio, "enable" if value not in ("0", "null") else "disable")
            except RegressionError as error:
                errors.append(str(error))
        for key, value in original.items():
            try:
                restore_setting(adb, key, value)
            except RegressionError as error:
                errors.append(str(error))
        if errors:
            message = "Could not restore every emulator setting: " + "; ".join(errors)
            if unwinding_failure:
                print(message, file=sys.stderr, flush=True)
            else:
                raise RegressionError(message)


def run_suite(adb: Adb, suite: str, log_dir: Path) -> int:
    selector = "notClass" if suite == "behavior" else "class"
    print(f"Running {suite} instrumentation…", flush=True)
    log = log_dir / f"{suite}.txt"
    output = adb.run("shell", "am", "instrument", "-w", "-e", selector, VISUAL_CLASS, RUNNER,
                     timeout=COMMAND_TIMEOUT_SECONDS, log=log)
    count = instrumentation_count(output, 48 if suite == "visual" else None)
    print(f"{suite}: {count} tests passed ({log})", flush=True)
    return count


def run(args: argparse.Namespace, kmp_root: Path) -> dict:
    adb = Adb(args.adb)
    adb.serial = select_emulator(adb.run("devices", "-l"), args.serial)
    validate_emulator(adb)
    app_apk = kmp_root / "androidApp/build/outputs/apk/debug/androidApp-debug.apk"
    test_apk = kmp_root / "androidApp/build/outputs/apk/androidTest/debug/androidApp-debug-androidTest.apk"
    for apk in (app_apk, test_apk):
        if not apk.is_file():
            raise RegressionError(f"Build the app and test APKs first; missing {apk}")
    log_dir = kmp_root / "androidApp/build/outputs/regression-results"
    log_dir.mkdir(parents=True, exist_ok=True)
    summary = {"serial": adb.serial, "apiLevel": 35, "suite": args.suite, "status": "running", "tests": {}}
    summary_path = log_dir / "summary.json"
    summary_path.write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
    try:
        print(f"Installing prebuilt APKs on {adb.serial}…", flush=True)
        adb.run("install", "-r", "-t", str(app_apk), timeout=180, log=log_dir / "install-app.txt")
        adb.run("install", "-r", "-t", str(test_apk), timeout=180, log=log_dir / "install-test.txt")
        with offline_emulator_settings(adb):
            if args.suite in ("all", "behavior"):
                summary["tests"]["behavior"] = run_suite(adb, "behavior", log_dir)
            if args.suite in ("all", "visual"):
                output = kmp_root / "androidApp/build/outputs/visual-regression"
                reset_visual_output(kmp_root, output)
                try:
                    summary["tests"]["visual"] = run_suite(adb, "visual", log_dir)
                finally:
                    # Keep partial evidence on test failure. The comparator's
                    # complete-set check prevents a partial run from passing.
                    unwinding_failure = sys.exc_info()[0] is not None
                    try:
                        adb.run("pull", f"/sdcard/Android/data/{APP_ID}/files/visual-regression/.",
                                str(output), timeout=180, log=log_dir / "pull-visual.txt")
                    except RegressionError as error:
                        if not unwinding_failure:
                            raise
                        print(f"Could not pull partial visual evidence: {error}", file=sys.stderr, flush=True)
        summary["status"] = "passed"
    except (RegressionError, OSError, KeyboardInterrupt) as error:
        summary["status"] = "failed"
        summary["error"] = str(error) or type(error).__name__
        raise
    finally:
        summary_path.write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
    return summary


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--adb", default="adb", help="ADB executable (default: adb on PATH)")
    parser.add_argument("--serial", help="Explicit emulator serial when multiple devices are connected")
    parser.add_argument("--suite", choices=("behavior", "visual", "all"), default="all")
    args = parser.parse_args(argv)
    try:
        summary = run(args, Path(__file__).resolve().parent.parent)
    except (RegressionError, OSError) as error:
        print(f"Android regressions failed: {error}", file=sys.stderr)
        return 1
    except KeyboardInterrupt:
        print("Android regressions interrupted; emulator settings restoration attempted.", file=sys.stderr)
        return 130
    print(f"Android regressions passed: {summary['tests']}", flush=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
