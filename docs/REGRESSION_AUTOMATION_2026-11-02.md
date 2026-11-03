# Android regression automation — 2 November 2026

The Android UI now has reproducible screenshot coverage and offline flow checks.
The existing working tree was preserved; no commit, push, or deployment was made.

## Coverage

- 48 production-screen captures: Home, Quran reader, full Mushaf reader, and
  Settings × Arabic/English/French × light/dark × 100%/200% font scale.
- Captures use a fixed clock, UTC fixture, verified bundled text, seeded reading
  preferences, and a 360×740 dp content viewport at density 2. They wait for
  source content and stable pixels before saving. The complete planned manifest
  is written before the first capture, so failed cases remain detectable.
- Six new flow checks cover edition confirmation/cancellation, saved-position
  restoration, offline reader and search, failed-index retry with a preserved
  query, and cancellation/reentry while loading. They join 12 existing UI tests.
- The screenshot comparator checks complete capture sets, metadata, image
  dimensions, alpha, changed area, connected changes, and high-contrast strokes.
  It writes an HTML report with expected, actual, and difference images. CI can
  verify baselines but cannot record them.
- A shared local/CI runner installs prebuilt APKs only on an API 35 emulator,
  disconnects its radios, verifies positive instrumentation results, preserves
  evidence, and restores radio/animation settings even on failure.

## UI corrections found during capture review

- At large font scales, Home shortcuts use full-width rows so complete labels fit.
- The Quran reader gives its title and edition a separate full-width heading
  instead of squeezing them between toolbar buttons.
- Settings language and theme choices wrap into visible rows at larger font scales.

## CI and local use

The Android job now runs lint, regression-tool unit tests, offline UI flows,
the screenshot matrix, and comparison against reviewed baselines. Captures,
diffs, logs, and lint reports are retained for failed runs too. Existing iOS
compilation and temporary test-APK jobs remain configured.

See [the regression guide](../kmp/scripts/VISUAL_REGRESSION.md) for commands,
the exact emulator image, comparison thresholds, and explicit baseline updates.

## Validation

- **125 shared JVM tests and 30 Android unit tests passed** against the final source.
- **18 Android behavior tests and all 48 captures passed** using the same
  `--suite all` runner configured in CI, with radios disconnected.
- All 48 baselines were visually reviewed, then compared against a separate
  fresh capture run: **48 passed, 0 failed, 0 capture-set/profile errors**.
- **36 host-tooling tests passed** (24 comparator cases and 12 runner cases).
- Debug app/test APK assembly and Android lint passed; lint reports no issues.
  Workflow YAML parsing and `git diff --check` passed. Existing Gradle plugin
  deprecation warnings remain.

Evidence:

- [Run summary](../artifacts/visual-regression-20261029/summary.json)
- [Behavior test output](../artifacts/visual-regression-20261029/behavior.txt)
- [Visual test output](../artifacts/visual-regression-20261029/visual.txt)
- [Comparison result](../artifacts/visual-regression-20261029/comparison.json)
- [Home review sheet](../artifacts/visual-regression-20261029/home-review.png)
- [Reader review sheet](../artifacts/visual-regression-20261029/reader-review.png)
- [Mushaf review sheet](../artifacts/visual-regression-20261029/mushaf-review.png)
- [Settings review sheet](../artifacts/visual-regression-20261029/settings-review.png)

The interactive HTML comparison report is generated at
`kmp/androidApp/build/reports/visual-regression/index.html` and uploaded by CI.

## Limits

These captures cover the top viewport of each screen. They complement the
long-content scrolling and flow tests; they do not validate physical-device
performance, TalkBack, notification delivery, or iOS rendering. iOS screenshot
coverage and native runtime checks still require macOS/Xcode. Remote CI has not
been run from this working tree.
