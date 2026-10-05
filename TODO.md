# Quran & Sunnah — Improvement Roadmap

Generated from full codebase audit (Oct 2026). Verified-only text principle preserved.

## P0 — Logic bugs / perf (do first)
- [x] `lib/features/quran/quran_reader_screen.dart` — precompute bookmark/highlight/note key sets outside `ListView.builder` (currently `ref.watch` per ayah). Use `select` / memoized sets.
- [x] `lib/core/navigation/adaptive_scaffold.dart` — use `IndexedStack` to preserve tab state / scroll / futures.
- [x] `lib/features/quran/widgets/ayah_action_sheet.dart` — share includes ayah text + translation + credit, not just `surah:ayah`. Copy includes reference.
- [x] `lib/features/search/global_search_screen.dart` — add generation ID to discard stale debounced searches; add recent-search history (SharedPreferences `search.history` max 10).
- [x] `lib/state/providers.dart` — persist enums by `name` string, not `index`; migrate old int keys; surface message on forced Hafs fallback.

## P1 — Logic / UX perf
- [x] `lib/features/quran/mushaf_reader_screen.dart` — debounce `_rememberPage` 500ms, avoid SharedPreferences storm on fast swipe.
- [x] `lib/features/home/home_screen.dart` — avoid `allBukhari()` + full `ayahsOfSurah` for daily cards. Add `ayahByKey` / `dailyHadithProvider` cached.
- [x] `lib/features/sunnah/sunnah_home_screen.dart` — move `ref.listen` out of `build`, add `ScrollController` threshold pagination, preserve list on filter change.
- [x] `lib/data/services/audio_service.dart` — parallelize `estimateSurahBytes` (pool 6), merge `downloadSurah`/`queueSurahDownload`, fix ETA to exclude paused time, reuse single `HttpClient`.
- [x] `lib/features/library/library_screen.dart` — `Dismissible` with Undo SnackBar (keep deleted item 5s).
- [x] Home hero progress — use khatma `globalAyahNumber/6236`, not `lastAyah/ayahCount`.

## P2 — UI
- [x] `lib/core/theme/app_theme.dart` — remove global `fontFamily: Noto Naskh Arabic` for Latin UI. Use `fontFamilyFallback` + locale theme; check bronze/paper contrast ≥4.5.
- [x] Reader — `SelectableText` for translation, tap-only-Arabic opens sheet; bump action icons to 48dp targets; divider between Quran/translation.
- [x] Ayah sheet — group 12 actions (Play/Study/Save/Share sections), fix FR label truncation.
- [x] Add `MiniPlayer` widget in `AdaptiveScaffold` when `audio.playing || loading`.
- [x] Search — highlight matched term, show counts per tab, empty-state illustration.
- [x] Settings — replace `SegmentedButton` truncation with dropdown + live font preview.

## P3 — Features
- [x] Khatma tracker + Juz progress + streak (new `state/khatma_provider.dart`).
- [x] Tafsir side-by-side Jalalayn+Siraj (`_CompareTafsir`, wide Row / narrow Column, SelectableText).
- [x] Audio true A-B repeat (LoopMode.all for range, .one for single) + sleep timer Off/5/15/30/60 + repeat subtitle.
- [x] Library diacritic-insensitive search (`normalizeArabic`) + auto-backup copy to support dir on export.
- [x] Deep-link `parseQuranRef("2:255", "/quran/2/255", "quran://2/255")` + `onGenerateRoute` in `app.dart` (no go_router dependency).

## Verification
- [ ] `flutter analyze`
- [ ] `flutter test`
- [ ] Manual: tab switch preserves scroll, Baqarah scroll 60fps, share contains text, offline airplane mode, RTL/LTR chevrons.
