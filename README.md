# Quran & Sunnah — Flutter (iOS + Android)

Premium, calm, offline-first Quran + authentic Sunnah app. Material 3 on
Android, native-feeling navigation/gestures/safe-areas on iOS.

> **Data integrity (highest priority):** this repo ships **no verse wording
> and no hadith matn**. Every sacred-text widget renders either rows from a
> verified dataset file or the string **"Content unavailable for this source."**
> Never wire an LLM or auto-generated text into `Ayah.text` / `Hadith.matnAr`.

## Structure

```
lib/
  main.dart / app.dart
  core/theme, core/l10n (ar/en/fr), core/utils (Arabic search normalization),
       core/navigation (adaptive bottom bar ↔ rail), core/widgets
  data/models (QuranEdition/Ayah, HadithCollection/Hadith, library)
  data/seed (114 surah metadata, 14-riwaya catalog, 11 collections — metadata only)
  data/repositories (Quran/Hadith interfaces + stubs — swap in verified loaders)
  data/services (add Drift/SQLite + just_audio wiring here)
  state (Riverpod: QuranPrefs, AppPrefs, library, downloads)
  features/ splash, onboarding(5 pages), home, quran (surah list, reader,
     mushaf, riwaya selector, script selector, font sheet, ayah sheet,
     compare, audio, tafsir), sunnah (home, sources, filters, reader card,
     book browser, narrator chain), search, library, downloads, settings
```

## Key architecture decisions

- **Riwaya ≠ script ≠ font** — three independent settings (`QuranPrefs`).
  `editionId = "<riwaya-storageKey>__<script>"`, each backed by its own file.
- **Adding a Riwaya** = append to `kRiwayaCatalog` + ship its verified file.
  No UI change needed.
- **Search** normalizes Arabic (tashkeel/tatweel/alef) for the index only
  (`normalizeArabic`); display text is never mutated.
- **Audio** Qaris are bound to the Riwaya they actually recite
  (`kQariCatalog`); never mislabel a Hafs recording as Warsh/Qalun.
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
   Extended-B-capable Amiri Quran face).
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
5. **Search:** global search over all bundled Quran/Hadith/Tafsir +
   Surah metadata, diacritic-insensitive Arabic, verse shortcuts
   (`2:255`), narrator/topic honestly empty pending datasets.
6. **Offline:** Download Manager reflects the 14 pre-bundled datasets
   as installed and protects them from deletion.

## Run

```sh
flutter pub get
flutter run                    # phone
flutter run -d "iPad"          # rail layout ≥840dp
flutter test
```

Requires Flutter 3.22+. iOS: Xcode 15+, `pod install` under `ios/`.
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
- Paginated queries, indexed local DB (FTS5 recommended), no full-DB-in-RAM.
- 60/120Hz scrolling: reader uses `ListView.separated`, no heavy shadows.
