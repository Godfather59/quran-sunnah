# Android tab-swipe benchmark

Run `kmp/scripts/benchmark-tab-swipes.ps1` against an already-installed and open Android app. The script does not install or launch the app, so start on **Home** first. It warms up the pager, then measures repeated Home ↔ Quran ↔ Sunnah swipes and saves per-swipe ADB latency, frame timing counts, raw `gfxinfo` output, device fingerprint, resolution, and refresh rate.

```powershell
cd "C:\path\to\quran-sunnah"
.\kmp\scripts\benchmark-tab-swipes.ps1 -Iterations 8
```

If more than one device is connected, pass its serial from `adb devices`:

```powershell
.\kmp\scripts\benchmark-tab-swipes.ps1 -Serial emulator-5554 -Iterations 8
```

On devices that deny `adb shell input swipe` (as this Xiaomi does), use the manual capture instead. It resets the app's graphics counters, waits while you perform physical swipes, then records the bounded recent frame history and aggregate jank counters. It does not install or launch the app:

```powershell
.\kmp\scripts\capture-manual-tab-swipes.ps1 -Serial <device-serial>
```

Results are written under `artifacts/tab-swipe-benchmarks/<timestamp>/` by default. `summary.json` gives aggregate p50/p90/max frame times and counts frames over 32, 50, and 100 ms; `swipes.csv` has each transition. A swipe with any frame over 100 ms is marked as a detected stall. Compare runs on the same device, refresh rate, build, and thermal state. ADB injection latency includes command round-trip overhead, so use it as a repeatable comparison signal rather than exact touch-to-display latency. Some Xiaomi builds reject `adb shell input swipe`; in that case the script reports the ADB error and no gesture is counted. The user can still run the benchmark on an emulator or another device that accepts injected input.

This is a profiling aid, not a pass/fail test. Collect a baseline and a post-change run before making further swipe optimizations. Preserve the raw data so unexpected frame parsing or device-specific behavior can be inspected.
