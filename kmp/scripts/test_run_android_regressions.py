"""Host-only tests for Android orchestration; no ADB, SDK, or Gradle needed."""

from __future__ import annotations

from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

import run_android_regressions as runner


class FakeAdb:
    def __init__(self) -> None:
        self.original = {"wifi_on": "1", "mobile_data": "0", "window_animation_scale": "1.0",
                         "transition_animation_scale": "null", "animator_duration_scale": "0.5"}
        self.calls = []
        self.fail_once = None

    def shell(self, *args: str) -> str:
        self.calls.append(args)
        if args == self.fail_once:
            self.fail_once = None
            raise runner.RegressionError("intentional ADB failure")
        if args[:3] == ("settings", "get", "global"):
            return self.original[args[3]]
        if args == ("dumpsys", "connectivity"):
            return "Network requests:\nActive default network: none\n"
        return ""


class RunnerTest(unittest.TestCase):
    def test_clean_positive_counts(self) -> None:
        self.assertEqual(1, runner.instrumentation_count("OK (1 test)\nINSTRUMENTATION_CODE: -1\n"))
        self.assertEqual(48, runner.instrumentation_count("Time: 4.1\n\nOK (48 tests)\n", 48))

    def test_failures_crashes_zero_and_missing_result_are_rejected(self) -> None:
        samples = (
            "FAILURES!!!\nTests run: 4, Failures: 1",
            "INSTRUMENTATION_FAILED: runner crashed\nOK (1 test)",
            "INSTRUMENTATION_RESULT: shortMsg=Process crashed.\nOK (1 test)",
            "INSTRUMENTATION_STATUS_CODE: -2\nOK (1 test)",
            "INSTRUMENTATION_STATUS_CODE: -1\nOK (1 test)",
            "INSTRUMENTATION_ABORTED\nOK (1 test)",
            "OK (0 tests)", "INSTRUMENTATION_CODE: -1", "OK (1 test)\nOK (1 test)",
        )
        for sample in samples:
            with self.subTest(sample=sample), self.assertRaises(runner.RegressionError):
                runner.instrumentation_count(sample)

    def test_incomplete_visual_count_rejected(self) -> None:
        with self.assertRaisesRegex(runner.RegressionError, "Expected 48"):
            runner.instrumentation_count("OK (47 tests)", 48)

    def test_device_selection_requires_ready_unambiguous_emulator(self) -> None:
        self.assertEqual("emulator-5556", runner.select_emulator(
            "List of devices attached\nemulator-5556 device product:sdk\n", None))
        self.assertEqual("emulator-5556", runner.select_emulator(
            "phone device\nemulator-5556 device\n", "emulator-5556"))
        for listing, serial in (("", None), ("emulator-5556 offline", None),
                                ("emulator-5554 device\nemulator-5556 device", None),
                                ("phone device", None), ("phone device", "phone"),
                                ("emulator-5556 offline", "emulator-5556")):
            with self.subTest(listing=listing), self.assertRaises(runner.RegressionError):
                runner.select_emulator(listing, serial)

    def test_api_and_qemu_property_are_verified(self) -> None:
        class Properties:
            def __init__(self, qemu: str, api: str) -> None:
                self.qemu, self.api = qemu, api

            def shell(self, _getprop: str, key: str) -> str:
                return self.qemu if key == "ro.kernel.qemu" else self.api

        runner.validate_emulator(Properties("1", "35"))
        for props in (Properties("0", "35"), Properties("1", "34"), Properties("1", "36")):
            with self.assertRaises(runner.RegressionError):
                runner.validate_emulator(props)

    def test_capture_reset_only_clears_fixed_build_directory(self) -> None:
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp).resolve()
            output = root / "androidApp/build/outputs/visual-regression"
            output.mkdir(parents=True)
            (output / "stale.png").write_bytes(b"stale")
            runner.reset_visual_output(root, output)
            self.assertEqual([], list(output.iterdir()))
            baseline = root / "androidApp/src/androidTest/screenshots"
            baseline.mkdir(parents=True)
            marker = baseline / "keep.png"
            marker.write_bytes(b"baseline")
            with self.assertRaises(runner.RegressionError):
                runner.reset_visual_output(root, baseline)
            self.assertEqual(b"baseline", marker.read_bytes())

    def test_symlinked_capture_parent_is_not_followed(self) -> None:
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp).resolve()
            (root / "androidApp/build").mkdir(parents=True)
            outside = root / "outside"
            outside.mkdir()
            try:
                (root / "androidApp/build/outputs").symlink_to(outside, target_is_directory=True)
            except OSError:
                self.skipTest("Host does not permit symlink creation")
            with self.assertRaises(runner.RegressionError):
                runner.reset_visual_output(root, root / "androidApp/build/outputs/visual-regression")
            self.assertEqual([], list(outside.iterdir()))

    def assert_original_restored(self, adb: FakeAdb) -> None:
        for key, value in adb.original.items():
            operation = ("settings", "delete", "global", key) if value == "null" else (
                "settings", "put", "global", key, value)
            self.assertIn(operation, adb.calls)
        self.assertIn(("svc", "wifi", "enable"), adb.calls)
        self.assertIn(("svc", "data", "disable"), adb.calls)

    def test_radio_and_animation_settings_restore_after_success(self) -> None:
        adb = FakeAdb()
        with runner.offline_emulator_settings(adb):
            self.assertIn(("svc", "wifi", "disable"), adb.calls)
            for key in runner.ANIMATION_KEYS:
                self.assertIn(("settings", "put", "global", key, "0"), adb.calls)
        self.assert_original_restored(adb)

    def test_settings_restore_after_instrumentation_failure(self) -> None:
        adb = FakeAdb()
        with self.assertRaisesRegex(runner.RegressionError, "suite failure"):
            with runner.offline_emulator_settings(adb):
                raise runner.RegressionError("suite failure")
        self.assert_original_restored(adb)

    def test_settings_restore_after_partial_setup_failure(self) -> None:
        adb = FakeAdb()
        adb.fail_once = ("settings", "put", "global", "window_animation_scale", "0")
        with self.assertRaisesRegex(runner.RegressionError, "intentional"):
            with runner.offline_emulator_settings(adb):
                self.fail("Setup failure must prevent test execution")
        self.assert_original_restored(adb)

    def test_one_restore_failure_does_not_skip_other_settings(self) -> None:
        adb = FakeAdb()
        with self.assertRaisesRegex(runner.RegressionError, "Could not restore"):
            with runner.offline_emulator_settings(adb):
                adb.fail_once = ("svc", "wifi", "enable")
        self.assert_original_restored(adb)

    def test_adb_timeout_saves_partial_instrumentation_log(self) -> None:
        with tempfile.TemporaryDirectory() as temp:
            log = Path(temp) / "visual.txt"
            failure = subprocess.TimeoutExpired(["adb"], 10, output=b"partial test output")
            with patch.object(runner.subprocess, "run", side_effect=failure):
                with self.assertRaisesRegex(runner.RegressionError, "timed out"):
                    runner.Adb("adb", "emulator-5556").run("shell", timeout=10, log=log)
            self.assertIn("partial test output", log.read_text())
            self.assertIn("Host timeout after 10 seconds", log.read_text())


if __name__ == "__main__":
    unittest.main()
