# Phase 3 content and device validation

## Verified content pipeline

No new sacred-text dataset is registered directly in the UI.

### Current native validation

```sh
cd kmp
./gradlew :shared:jvmTest :androidApp:testDebugUnitTest
```

The shared dataset tests validate the bundled editions and metadata. New
Quran editions must have 6236 unique canonical `surah|ayah|text` rows and
the verified per-surah counts, with explicit provenance and content terms.
The old Dart importers were removed at the KMP cutover; their historical
implementation is available at the `flutter-final` Git tag, not in the
current toolchain. Do not treat a passing bundled-data test as approval
for a newly acquired source.

### Hadith structured package validation

Keep a source-backed structured package and add loader/integrity regression
coverage before registering it. A grade must have a grading authority. Narrator/topic capability
is detected from the source package rather than inferred.

### QuranEnc acquisition

Any replacement acquisition tool must retain all 114 raw API responses
and metadata before creating a normalized pipe file. There is currently
no maintained QuranEnc fetch command in this checkout.

Source versions checked 2026-10-04:
- English Rowwad: V1.0.19, dated 2026-03-12.
- French Rachid Maach: V1.0.3, dated 2026-06-21.
- Arabic Tafsir al-Sa'di: V1.0.0, dated 2026-07-27.

QuranEnc terms require unmodified content, source/publisher attribution,
version retention, transcript preservation, notification of corrections and
updating to newer source versions. Therefore normalized output must not be
bundled until raw response metadata/footnotes are reviewed and represented
correctly.

## Riwayat

The current upstream quran-api tree exposes an additional al-Bazzi edition,
but the underlying QuranComplex-derived redistribution rights remain
unresolved in this project. Structural validation alone is insufficient; it must not be
registered as bundled until direct content terms/permission are recorded.

## Real-device QA

Automated Compose tests complement a physical-device pass.

Run:

```sh
cd kmp
./gradlew :androidApp:connectedDebugAndroidTest
./gradlew :androidApp:assembleDebug
```

Use `gradlew.bat` on Windows. The connected task requires a running Android
emulator/device. For a focused large-text reader pass, select
`com.godfather59.quransunnah.ReaderLayoutTest` with the Android instrumentation
runner's `class` argument. Follow `docs/BETA_QA.md` for the device/locale matrix
and `docs/TAB_SWIPE_BENCHMARK.md` for repeatable swipe measurements.

Capture Android Studio system traces/profiler output for:
- main-thread/frame stalls while scrolling Quran and Hadith lists;
- memory after first FTS index build;
- memory after repeated searches;
- background/resume behavior during audio playback;
- download queue pause/resume/cancel/retry;
- airplane-mode access to bundled Quran/Hadith/Tafsir/fonts;
- upgrade migration from the previous SharedPreferences library.

Also verify source selection across Warsh/Qalun and Mushaf, system Back,
long verses at 200% text, search result context, and reminder renewal across
midnight and selected-city timezone changes. Validate the SwiftUI app with
the Xcode scheme on macOS, including Dynamic Type and VoiceOver.

Do not create arbitrary pass/fail millisecond thresholds in CI: device class,
thermal state and first-run SQLite work materially change these numbers. Track
regressions against the same device instead.
