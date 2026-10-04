# Phase 3 content and device validation

## Verified content pipeline

No new sacred-text dataset is registered directly in the UI.

### Quran / translation validation

```sh
dart run tool/content_pipeline.dart quran INPUT.txt PROVENANCE.txt edition-id
```

The gate requires exactly 6236 unique `surah|ayah|text` rows and the exact
per-surah verse counts.

### Hadith structured package validation

```sh
dart run tool/content_pipeline.dart hadith INPUT.json PROVENANCE.txt collection-id
```

A grade is rejected if it has no grading authority. Narrator/topic capability
is detected from the source package rather than inferred.

### QuranEnc acquisition

The fetcher retains all 114 raw API responses before creating a normalized
pipe file:

```sh
dart run tool/quranenc_fetch.dart \
  english_rwwad 1.0.19 "Rowwad Translation Center" translation work/english-rwwad

dart run tool/quranenc_fetch.dart \
  french_rashid 1.0.3 "Rachid Maach / QuranEnc" translation work/french-rashid

dart run tool/quranenc_fetch.dart \
  arabic_saadi 1.0.0 "Abd al-Rahman al-Sa'di / QuranEnc" tafsir work/saadi
```

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
unresolved in this project. The importer can validate it, but it must not be
registered as bundled until direct content terms/permission are recorded.

## Real-device QA

Automated widget tests are not a substitute for a physical-device pass.

Run:

```sh
flutter test integration_test/device_smoke_test.dart -d <device-id>
flutter run --profile -d <device-id>
```

Record the printed:
- `startup_ui_ms`
- `hafs_asset_load_ms`
- `first_fts_search_ms`
- `repeat_fts_search_ms`

Then inspect Flutter DevTools for:
- frame build/raster spikes while scrolling Quran and Hadith lists;
- memory after first FTS index build;
- memory after repeated searches;
- background/resume behavior during audio playback;
- download queue pause/resume/cancel/retry;
- airplane-mode access to bundled Quran/Hadith/Tafsir/fonts;
- upgrade migration from the previous SharedPreferences library.

Do not create arbitrary pass/fail millisecond thresholds in CI: device class,
thermal state and first-run SQLite work materially change these numbers. Track
regressions against the same device instead.
