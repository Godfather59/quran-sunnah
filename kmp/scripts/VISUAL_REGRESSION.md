# Android UI regressions and screenshot comparison

`verify_screenshots.py` compares deterministic Android content captures against
reviewed PNGs. Python 3.10+ and Pillow are required. Development and CI should
use the same Pillow version; the current verified runtime uses Pillow 12.3.0.

```powershell
python -m pip install -r kmp/scripts/requirements-visual-regression.txt
python -m unittest discover -s kmp/scripts -p test_verify_screenshots.py -v
```

## Capture and flow runner

Start a disposable **AOSP API 35 x86_64** emulator using the Pixel 5 profile.
The CI emulator uses SwiftShader, Vulkan disabled, 2 GB RAM and two cores.
Use the same system-image fingerprint as the reviewed manifest; updating an
image requires reviewing the new captures. The runner refuses physical devices
and other API levels. It installs the debug app and test APKs onto the emulator,
so use an AVD reserved for testing.

Reviewed image: `system-images;android-35;default;x86_64`, package revision **2**,
fingerprint `Android/sdk_phone64_x86_64/emu64x:15/AE3A.240806.019/12368160:userdebug/test-keys`.
The action selects API 35, whose repository image can receive updates. If its
fingerprint changes, comparison deliberately fails: confirm the package revision,
review all new images, explicitly replace the baselines, and independently
recapture and verify before accepting that update. Never remove the fingerprint
check or record from CI to make an image update pass.

Build first, then run from the repository root:

```powershell
cd kmp
.\gradlew.bat :androidApp:assembleDebug :androidApp:assembleDebugAndroidTest
cd ..
python kmp/scripts/run_android_regressions.py --suite all
```

On Linux/macOS use `./gradlew` in place of `.\gradlew.bat`. Set `--adb` to the
ADB executable if it is not on PATH. With multiple connected devices, explicitly
select the emulator, for example `--serial emulator-5556`. Without `--serial`,
exactly one device must be connected and ready.

`--suite behavior` runs all instrumentation except `VisualRegressionTest`.
`--suite visual` runs the 48-case screenshot matrix. `--suite all` runs both in
that order. The runner uses prebuilt APKs and never invokes Gradle itself.
Wi-Fi/mobile data are disabled, and the runner waits for the active default
network to disappear. Window, transition and animator scales are set to zero.
Original radio and animation settings are restored in `finally`, including on
test failure or keyboard interruption. Tests also assert offline connectivity.

Instrumentation must report a clean, positive `OK (N tests)` result; an ADB
exit code of zero alone cannot pass the gate. The visual suite must report
exactly 48 tests. Each instrumentation command has a 15-minute host timeout.
Logs and `summary.json` are written to
`kmp/androidApp/build/outputs/regression-results/`.

Before a visual run, the runner replaces only the fixed build output capture
directory. It pulls fresh screenshots there, including partial evidence after
a test failure. Path checks refuse redirected/symlinked deletion targets. The
runner never records baselines or performs image comparison; run the explicit
verification command below after a successful capture. CI uses the same runner,
then verifies the reviewed baselines and uploads captures, diffs, logs and lint
reports even when a previous step fails.

## Capture contract

- Reviewed baselines: `kmp/androidApp/src/androidTest/screenshots/`
- Fresh, complete capture: `kmp/androidApp/build/outputs/visual-regression/`
- Report: `kmp/androidApp/build/reports/visual-regression/`
- Each input directory contains `manifest.json` and exactly the PNGs it lists.
  Remove previous actual captures before pulling a fresh run, so stale PNGs
  cannot conceal a missing capture. The test must list every planned capture,
  including one whose capture fails.
- File names are root-relative POSIX paths. Absolute paths, traversal,
  duplicate/case-conflicting paths, and symbolic links are rejected.
- The suite captures 48 cases: four screens × Arabic/English/French × light/dark ×
  font scales 1.0/2.0. A typical file is `reader-ar-light-100.png`.
- Capture content is 360×740 dp at density 2.0 (720×1480 pixels), without OS
  chrome. Both image dimensions and manifest metadata are checked.
- `profile` is compared in full, so an API/image fingerprint, density,
  viewport, fixture clock, or any other profile change requires explicit
  review. Additional profile and capture keys are accepted and compared.

Example manifest (abbreviated to one capture):

```json
{
  "schemaVersion": 1,
  "profile": {
    "apiLevel": 35,
    "viewportWidthDp": 360,
    "viewportHeightDp": 740,
    "density": 2.0,
    "imageFingerprint": "the exact Build.FINGERPRINT from the capture device",
    "fixtureMillis": 1768478400000
  },
  "captures": [
    {
      "file": "reader-ar-light-100.png",
      "screen": "reader",
      "locale": "ar",
      "theme": "light",
      "fontScale": 1.0
    }
  ]
}
```

## Verification

```powershell
python kmp/scripts/verify_screenshots.py verify `
  --expected kmp/androidApp/src/androidTest/screenshots `
  --actual kmp/androidApp/build/outputs/visual-regression `
  --report kmp/androidApp/build/reports/visual-regression
```

Exit codes: **0** passed; **1** regression, incomplete/mismatched capture set,
unreadable capture, or profile mismatch; **2** invalid invocation or tool/file
operation failure. Verification never writes to either input directory. Report
and input directories must be separate and cannot contain one another.

The gate compares RGBA pixels; alpha changes are included. Defaults:

| Check | Limit |
| --- | --- |
| Maximum per-channel delta ignored as small raster/antialias noise | 8 of 255 |
| Pixels above that tolerance | ≤0.01% of image, with an absolute cap of 64 |
| Four-connected region above that tolerance | ≤8 pixels |
| Pixels with any channel/alpha delta above 32 | **Zero allowed** |

All checks must pass. The absolute area cap and connected-region limit prevent
a large screenshot from hiding a changed small label. The material-delta check
rejects even a single missing high-contrast stroke pixel, independently of the
area allowance. Small color variations preserving the contour can pass. Do not
loosen thresholds to absorb text movement, missing content, clipping, changed
fonts, or a different emulator; inspect the rendered images instead.

The report contains `index.html`, `report.json`, and a capture-specific expected,
actual, and diff PNG for every capture. Missing/invalid images get explicit
placeholder artifacts. Magenta marks pixels above the color tolerance; a dimmed
expected image supplies context. Failures appear first in the HTML. Open image
links to inspect original resolution. The machine-readable report includes
capture-set errors, metadata mismatches, image sizes, changed/material pixel
counts, and a lower bound on the largest changed region (component scanning
stops once it exceeds the rejection limit).

## Explicit local baseline recording

Create the initial baseline only after reviewing all 48 actual images:

```powershell
python kmp/scripts/verify_screenshots.py record `
  --actual kmp/androidApp/build/outputs/visual-regression `
  --expected kmp/androidApp/src/androidTest/screenshots
```

Then independently capture the same 48 cases again and run **verify** against
that new run. The repeat must pass before committing the baseline. Recording
and comparing the same files does not establish capture stability.

For an intentional design change, inspect its actual images and use the same
record command with `--replace`. Review the resulting PNG/manifest changes in
Git, independently recapture, and verify again. Replacement manages only PNGs
and `manifest.json`, preserving README/review notes. Existing baselines are
refused without `--replace`. Recording is always refused when `CI` or
`GITHUB_ACTIONS` is truthy. CI should only run **verify**, never **record**.

Run all host tooling checks with:

```powershell
python -m unittest discover -s kmp/scripts -p 'test_*.py' -v
```

The synthetic comparator tests exercise accepted antialias/noise variations, missing and
unexpected captures, profile/locale mismatches, damaged/incorrectly sized PNGs,
alpha changes, clipped strokes, low-contrast connected changes, distributed
pixel regressions, CLI failure codes, explicit baseline replacement, and the
CI recording prohibition.

Orchestration tests cover clean/failed/crashed/zero-count instrumentation,
incomplete visual counts, physical-device and API checks, safe output cleanup,
partial logs on timeout, and restoration after both setup and test failures.
