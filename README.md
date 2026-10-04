# Quran & Sunnah — Flutter (iOS + Android)

Premium, calm, offline-first Quran + authentic Sunnah app with bundled Arabic fonts. Material 3 on
Android, native-feeling navigation/gestures/safe-areas on iOS.

> **Data integrity (highest priority):** this repo ships verified, source-backed
> Quran and Hadith datasets and never fabricates sacred text. Every sacred-text
> widget renders either exact rows from a verified bundled source or the string
> **"Content unavailable for this source."** Never wire an LLM or generated text
> into `Ayah.text` / `Hadith.matnAr`.

## Structure

```
lib/
  main.dart / app.dart
  core/theme, core/l10n (ar/en/fr), core/utils (Arabic search normalization),
       core/navigation (adaptive bottom bar ↔ rail), core/widgets
  data/models (QuranEdition/Ayah, HadithCollection/Hadith, library)
  data/seed (114 surah metadata, 14-riwaya catalog, 11 collections — metadata only)
  data/repositories (Quran/Hadith interfaces + stubs — swap in verified loaders)
  data/database (Drift/SQLite personal library + FTS5 search index)
  data/services (verified asset loaders, integrity-aware indexed search, just_audio)
  state (Riverpod: QuranPrefs, AppPrefs, database-backed library, downloads)
  features/ splash, onboarding(5 pages), home, quran (surah list, reader,
     mushaf, riwaya selector, script selector, font sheet, ayah sheet,
     compare, audio, tafsir), sunnah (home, sources, filters, reader card,
     book browser, narrator chain), search, library, downloads, settings
```

## Key architecture decisions

- **Riwaya ≠ script ≠ font ≠ Tajweed**. A Quran edition is backed by an
  explicit verified Riwaya + script asset. Tajweed is an optional annotation
  overlay only for Hafs/Uthmani; it never creates or alters Quran wording.
- **Adding a Riwaya** = append to `kRiwayaCatalog` + ship its verified file.
  No UI change needed.
- **Search** uses an on-device SQLite FTS5 index. Arabic normalization is index-only; display text is never mutated. Index fingerprints are derived from the integrity manifest and rebuild automatically when verified asset bytes change.
- **Audio** reciters are bound to the Riwaya they actually recite (`kReciters`); never mislabel a Hafs recording as Warsh/Qalun.
- **Personal library** (bookmarks, notes, highlights, collections and recent items) is stored in Drift/SQLite. Existing SharedPreferences data is migrated transactionally once.
- **Notes** render in `UserNoteCard`, visually distinct from sacred text.
- **Translations** use a muted latin-first style, never Quran-styled.

## Connecting verified datasets

1. **Quran per-Riwaya:** ✅ Hafs ‘an Asim **bundled** — Uthmani + Imla’i
   (Tanzil Project v1.1, Feb 2021, verbatim, `assets/quran/hafs-an-asim/`).
   ✅ Warsh + Qalun **bundled** (QuranComplex v8 via quran-api, 6236
   each) — Compare Riwayat shows real differences.
   To add a Riwaya, export one `surah|ayah|text` file per
   `RiwayaId.storageKey` (+ script), add its row to `kVerifiedQuranAssets`,
   and extend the dataset test to assert its verse counts.
   Scripts: Uthmani + Imla’i + **IndoPak** bundled for Hafs
   (`hafs-an-asim__indopak`, QuranComplex Unicode, rendered with the
   Extended-B-capable bundled Amiri Quran face).
   Structural metadata ✅ bundled: 30 Juz / 240 quarters (60 Hizb) /
   604 Medina pages / 15 sajdas (Tanzil quran-data.xml v1.0,
   `assets/quran/metadata/`) — Hafs/Medina mapping only.
   Tajweed ✅ bundled: 59,126 Hafs/Uthmani annotations over 6173 verses
   (decision-tree generated against our exact text, bounds-verified;
   `assets/quran/tajweed/hafs/`).
   Word morphology ✅ bundled: 82,260 tokens aligned to our verses
   (Quranic Arabic Corpus v0.4, 98.6% lemma/root/POS; `assets/quran/
   words/hafs/`) — powers Word Meanings with source credit.
2. **Translations/tafsir:** ✅ Quran EN (Saheeh International) + FR
   (Hamidullah) **bundled** (`assets/quran/translations/`, via Tanzil,
   6236 each, translator always credited, muted styling). ✅ Tafsir
   al-Jalalayn + Al-Siraj (Arabic) **bundled** (`assets/quran/tafsir/`,
   per-surah, sourced); Ibn Kathir/Tabari/Sa‘di/Qurtubi plug in via
   the same layout + one catalog row.
3. **Hadith:** ✅ 10 Arabic collections **bundled** — Bukhari (7589),
   Muslim (7563), Abu Dawud (5274), Tirmidhi (3998), Nasa’i (5765),
   Ibn Majah (4343), Muwatta Malik (1858), Nawawi 40, Qudsi 40,
   Dehlawi 40 — ~36.5k narrations (`assets/hadith/<id>/`, provenance
   in each PROVENANCE.txt; upstream `grades` are empty so the app
   shows grade as unavailable rather than inventing one). Ahmad,
   Riyad, Adab, Bulugh have no verified open Arabic edition in the
   current upstream — they plug into the same per-section layout
   once sourced. Never merge parallels.
4. **Audio:** 8 verified Hafs reciters (Alafasy, Husary, Muaiqly,
   Ajamy, Shaatree, Hudhaify, Ayyoub, Jibreel — streams individually
   reachability-checked) via Islamic Network CDN, per-ayah files;
   background playback with lock-screen metadata; per-surah offline
   download with size shown BEFORE downloading + local-first playback.
   Other Riwayat honestly report no recitation — never mislabeled.
5. **Search:** global search over bundled Quran/Hadith/Tafsir through SQLite FTS5 + Surah metadata, diacritic-insensitive Arabic, verse shortcuts (`2:255`), narrator/topic honestly empty pending verified structured datasets.
6. **Offline:** Download Manager reflects the 14 pre-bundled datasets
   as installed and protects them from deletion.

## Run

```sh
flutter pub get
flutter run                    # phone
flutter run -d "iPad"          # rail layout ≥840dp
flutter test
```

Requires Flutter 3.44+. iOS: Xcode 15+, `pod install` under `ios/`.
Android: AGP 8+, dynamic color via `dynamic_color`.

## Screens covered (§32)

Splash · Onboarding(5) · Home · Surah list (Surah/Juz/Hizb/Rub/Page + fast
selector) · Quran reader · Mushaf reader · Riwaya selector · Script selector
+ font sheet · Ayah bottom sheet · Compare Riwayat · Audio player · Tafsir ·
Sunnah home · Collection selector (incl. Sahihayn/Kutub al-Sittah presets) ·
Book/chapter browser · Hadith reader card · Hadith filters · Narrator/Sanad
view · Global search · Library (bookmarks/notes/collections/recent/downloads)
· Downloads manager · Settings.

## Accessibility & performance

- Dynamic text, screen-reader labels, ≥48dp targets, RTL-first layout.
- Hadith browsing streams bundled sections; global Quran/Hadith/Tafsir search uses a persisted FTS5 index instead of rescanning the corpus on every query.
- 60/120Hz scrolling: reader uses `ListView.separated`, no heavy shadows.


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

## Phase 3 product/content tooling

- **Verified content gate:** `tool/content_pipeline.dart` validates incoming Quran/translation and Hadith packages before registration. Quran-style pipe datasets must contain exactly 6236 unique references with the expected 114-surah structure; structured Hadith grades are rejected without a grading authority.
- **QuranEnc acquisition:** `tool/quranenc_fetch.dart` retains raw API responses plus version/publisher/terms metadata and SHA-256 fingerprints before producing normalized text. Candidate English Rowwad, French Rachid Maach and Arabic al-Sa‘di sources are recorded but remain disabled until their raw package and transcript/footnote metadata have been reviewed.
- **Riwayat expansion:** al-Bazzi is recorded as a source candidate only. It is not exposed as a bundled edition while direct redistribution rights for the underlying edition remain unresolved.
- **Mushaf polish:** verified Medina pages now persist reading position and show Juz/Hizb/Rubʿ and sajda metadata. Medina page layout is never borrowed for another Riwaya.
- **Audio downloads:** per-surah queue with pause/resume/cancel/retry, partial-file safety, local-first resume and storage accounting.
- **Library:** searchable bookmarks/notes/collections/highlights plus versioned JSON backup/restore of personal data only.
- **Sunnah structured fields:** narrator/sanad/grade/topic filters are capability-driven and automatically remain locked until a registered verified source actually supplies those fields.
- **Device QA:** `integration_test/device_smoke_test.dart` measures startup, verified Quran asset load and first/repeat FTS search on a real device. See `docs/PHASE3_CONTENT_AND_DEVICE_QA.md`.

