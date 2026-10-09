# Quran & Sunnah — Kotlin Multiplatform (Android + iOS)

Premium, calm, offline-first Quran + authentic Sunnah app with bundled Arabic fonts. Jetpack Compose (Material 3) on
Android, SwiftUI with native navigation/gestures/safe-areas on iOS, over one shared Kotlin logic module.

> **Data integrity (highest priority):** this repo ships verified, source-backed
> Quran and Hadith datasets and never fabricates sacred text. Every sacred-text
> surface renders either exact rows from a verified bundled source or the string
> **"Content unavailable for this source."** Never wire an LLM or generated text
> into `Ayah.text` / `Hadith.matnAr`.

## Structure

```
kmp/
  shared/      # commonMain (domain, seed, loaders, integrity, search, prayer,
               #            library store, prefs, audio/download logic, notifications)
               # + androidMain (Media3, HttpURLConnection, GPS, compass, share,
               #   FileProvider, backup picker) + jvmMain (tests) + iosMain
               #   (NSDate clock, NSBundle reader, native driver, fail-soft device)
  androidApp/  # Compose UI: home, surah list, reader, mushaf, tafsir/compare/
               # study sheets, sunnah browser, search, library, downloads,
               # prayer & qibla, dhikr, memorization, settings, onboarding
  iosApp/      # SwiftUI shell (framework compiles on any host; .xcodeproj
               # link + bundle resources need macOS — see kmp/README.md)
  gradle/libs.versions.toml  # Kotlin 2.4.0, AGP 9.1.0 (pinned), SQLDelight,
                             # coroutines, multiplatform-settings, Media3
assets/        # single copy of all verified data (63 MB): Quran editions,
               # translations, tafsir, hadith, fonts, licenses, manifests.
               # Referenced by Gradle, never copied into kmp/.
docs/          # DATA_LICENSES.md, BETA_QA.md, PHASE3_CONTENT_AND_DEVICE_QA.md
```

History: the Flutter app (`lib/`, `test/`, `tool/`, Flutter `android/`/`ios/` glue) was removed at cutover.
The last Flutter commit is tagged **`flutter-final`** for archaeology.

## Key architecture decisions

- **Riwaya ≠ script ≠ font ≠ Tajweed**. A Quran edition is backed by an
  explicit verified Riwaya + script asset. Tajweed is an optional annotation
  overlay only for Hafs/Uthmani; it never creates or alters Quran wording.
- **Adding a Riwaya** = append to the shared `RiwayatCatalog` + ship its verified file.
  No UI change needed.
- **Search** uses a shared in-memory `SearchEngine` (normalized AND-prefix tokens,
  exact=3/prefix=1/title=+2 scoring, per-category limits) over the same normalization as
  the persisted SQLite FTS5 index. Arabic normalization is index-only; display text is never mutated.
- **Audio** reciters are bound to the Riwaya they actually recite (`reciters`); never mislabel a Hafs recording as Warsh/Qalun.
- **Personal library** (bookmarks, notes, highlights, collections and recent items) is stored in SQLDelight/SQLite with the same table/column names and pref keys as the Flutter app, so data round-trips. Backup/restore is versioned JSON of personal data only.
- **Notes** render visually distinct from sacred text.
- **Translations** use a muted latin-first style, never Quran-styled.
- **Main-thread discipline:** verified-asset parses, FTS/index builds, prayer math and file scans run on `Dispatchers.IO`; Compose shows skeletons until ready. Shared mutable state (`SearchEngine`, `DownloadQueue`) is guarded by a portable `CommonLock` (JVM `synchronized` / iOS `NSRecursiveLock`).

## Verified datasets (bundled under `assets/`)

1. **Quran per-Riwaya:** ✅ Hafs ‘an Asim **bundled** — Uthmani + Imla’i
   (Tanzil Project v1.1, Feb 2021, verbatim, `assets/quran/hafs-an-asim/`).
   ✅ Warsh + Qalun **bundled** (QuranComplex v8 via quran-api, 6236
   each) — Compare Riwayat shows real differences.
   To add a Riwaya, export one `surah|ayah|text` file per
   `RiwayaId.storageKey` (+ script), add its row to `verifiedQuranAssets`,
   and extend the dataset test to assert its verse counts.
   Scripts: Uthmani + Imla’i + **IndoPak** bundled for Hafs
   (`hafs-an-asim__indopak`, QuranComplex Unicode, rendered with the
   Extended-B-capable bundled Amiri Quran face).
   Structural metadata ✅ bundled: 30 Juz / 240 quarters (60 Hizb) /
   604 Medina pages / 15 sajdas (Tanzil quran-data.xml v1.0,
   `assets/quran/metadata/`) — Hafs/Medina mapping only.
   Tajweed ✅ bundled: Hafs/Uthmani annotations
   (`assets/quran/tajweed/hafs/`).
   Word morphology ✅ bundled: tokens aligned to our verses
   (Quranic Arabic Corpus, `assets/quran/words/hafs/`) — powers Word Meanings with source credit.
2. **Translations/tafsir:** ✅ Quran EN (Saheeh International) + FR
   (Hamidullah) **bundled** (`assets/quran/translations/`, via Tanzil,
   6236 each, translator always credited, muted styling). ✅ Tafsir
   al-Jalalayn + Al-Siraj (Arabic) **bundled** (`assets/quran/tafsir/`,
   per-surah, sourced).
3. **Hadith:** ✅ 10 Arabic collections **bundled** — Bukhari (7589),
   Muslim (7563), Abu Dawud (5274), Tirmidhi (3998), Nasa’i (5765),
   Ibn Majah (4343), Muwatta Malik (1858), Nawawi 40, Qudsi 40,
   Dehlawi 40 — ~36.5k narrations (`assets/hadith/<id>/`, provenance
   in each PROVENANCE.txt; upstream `grades` are empty so the app
   shows grade as unavailable rather than inventing one). Never merge parallels.
4. **Audio:** 8 verified Hafs reciters via Islamic Network CDN, per-ayah files;
   background playback (Media3 `MediaSessionService`) with lock-screen metadata; per-surah offline
   download with size shown BEFORE downloading + local-first playback.
   Other Riwayat honestly report no recitation — never mislabeled.
5. **Search:** global search over bundled Quran/Hadith/Tafsir through the shared engine + SQLite FTS5 + Surah metadata, diacritic-insensitive Arabic, verse shortcuts (`2:255`).
6. **Offline:** Download Manager reflects the pre-bundled datasets
   as installed and protects them from deletion.

## Run

```sh
cd kmp
./gradlew :shared:jvmTest                    # shared logic tests (Windows-safe, 115 green)
./gradlew :shared:compileKotlinIosX64 \
  :shared:compileKotlinIosArm64 \
  :shared:compileKotlinIosSimulatorArm64     # iOS framework compiles on any host
./gradlew :androidApp:assembleDebug          # APK: androidApp/build/outputs/apk/debug/
```

Requires JDK 17+ and the Android SDK with `sdk.dir` in `kmp/local.properties`
(git-ignored, never commit it). Gradle wrapper (`gradlew`, `gradlew.bat`, 9.3.1) is committed.
iOS `.xcodeproj` link + bundle resources need macOS (see `kmp/README.md`).

## Screens covered

Splash · Onboarding(5) · Home · Surah list (Surah/Juz/Hizb/Rub/Page + fast
selector) · Quran reader · Mushaf reader · Riwaya selector · Script selector
+ font sheet · Ayah bottom sheet · Compare Riwayat · Word study · Audio player · Tafsir ·
Sunnah home · Collection selector · Book/chapter browser · Hadith reader card · Hadith filters ·
Global search · Library (bookmarks/notes/collections/recent/downloads)
· Downloads manager · Prayer & Qibla · Dhikr · Memorization · Settings.

## Accessibility & performance

- Dynamic text, screen-reader labels, ≥48dp targets, RTL-first layout.
- Hadith browsing streams bundled sections; global Quran/Hadith/Tafsir search builds once per process on IO with a loading indicator instead of rescanning on every query.
- 60/120Hz scrolling: reader uses `LazyColumn`, no heavy shadows; sheet/parses/downloads never block Main.

## Release safety

- Android release builds are never signed with the debug key. Put your private
  upload-keystore settings in `android/key.properties` (ignored by Git).
- The current Android application id is `com.godfather59.quransunnah`.
- Do not publish a dataset until its provenance and redistribution terms have
  been checked. Upstream API repositories may be permissively licensed while
  individual source editions/translations can have separate terms.

## Canonical verse identity

`Ayah.canonicalVerseId` is separate from `displayAyahNumber`. Current bundled
editions map to the same normalized 6236-row coordinates, so both values are
identical today. The separation is deliberate: a future verified mushaf
tradition may use edition-specific display numbering while bookmarks,
cross-edition comparison and internal references remain attached to the same
canonical verse identity.

## Data licensing

See `docs/DATA_LICENSES.md` for the dataset-by-dataset audit and
`assets/licenses/DATA_NOTICES.txt` for the notices bundled with the app.
Unresolved underlying-text redistribution rights are explicitly marked
unresolved rather than inferred from an aggregator repository license.
