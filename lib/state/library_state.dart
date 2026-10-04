import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/models/library.dart';

/// In-memory library; swap with Drift box without changing UI.
/// Notes are always rendered in a visually distinct container.
class LibraryNotifier extends StateNotifier<List<Bookmark>> {
  LibraryNotifier() : super(const []);

  void toggleAyah(int surah, int ayah) {
    final key = '$surah:$ayah';
    if (state.any((b) => b.refKey == key)) {
      state = state.where((b) => b.refKey != key).toList();
    } else {
      state = [
        ...state,
        Bookmark(
          id: 'bm-$key-${DateTime.now().microsecondsSinceEpoch}',
          kind: BookmarkKind.ayah,
          refKey: key,
          title: 'Surah $surah · Ayah $ayah',
          subtitle: 'Quran bookmark',
          createdAt: DateTime.now(),
        ),
      ];
    }
  }

  /// Assign an ayah bookmark to a custom collection (creates it).
  void setAyahCollection(
      int surah, int ayah, String? collectionId) {
    final key = '$surah:$ayah';
    final existing =
        state.where((b) => b.refKey == key).firstOrNull;
    if (existing == null) {
      state = [
        ...state,
        Bookmark(
          id: 'bm-$key-${DateTime.now().microsecondsSinceEpoch}',
          kind: BookmarkKind.ayah,
          refKey: key,
          title: 'Surah $surah · Ayah $ayah',
          subtitle: 'Quran bookmark',
          collectionId: collectionId,
          createdAt: DateTime.now(),
        ),
      ];
    } else {
      state = state
          .map((b) => b.refKey == key
              ? Bookmark(
                  id: b.id,
                  kind: b.kind,
                  refKey: b.refKey,
                  title: b.title,
                  subtitle: b.subtitle,
                  collectionId: collectionId,
                  createdAt: b.createdAt,
                )
              : b)
          .toList();
    }
  }

  void toggleHadith(String id, String title) {
    if (state.any((b) => b.refKey == id)) {
      state = state.where((b) => b.refKey != id).toList();
    } else {
      state = [
        ...state,
        Bookmark(
          id: 'bm-$id',
          kind: BookmarkKind.hadith,
          refKey: id,
          title: title,
          subtitle: 'Hadith bookmark',
          createdAt: DateTime.now(),
        ),
      ];
    }
  }

  bool isBookmarked(String key) => state.any((b) => b.refKey == key);

  void remove(String id) =>
      state = state.where((b) => b.id != id).toList();
}

final libraryProvider =
    StateNotifierProvider<LibraryNotifier, List<Bookmark>>(
        (ref) => LibraryNotifier());

class NotesNotifier extends StateNotifier<List<UserNote>> {
  NotesNotifier() : super(const []);

  UserNote? forRef(String refKey) =>
      state.where((n) => n.refKey == refKey).firstOrNull;

  void upsert(String refKey, String text) {
    final trimmed = text.trim();
    if (trimmed.isEmpty) {
      state = state.where((n) => n.refKey != refKey).toList();
      return;
    }
    final existing = forRef(refKey);
    if (existing == null) {
      state = [
        ...state,
        UserNote(
          id: 'note-$refKey-${DateTime.now().microsecondsSinceEpoch}',
          refKey: refKey,
          text: trimmed,
          createdAt: DateTime.now(),
        ),
      ];
    } else {
      state = state
          .map((n) => n.refKey == refKey
              ? UserNote(
                  id: n.id,
                  refKey: n.refKey,
                  text: trimmed,
                  createdAt: n.createdAt)
              : n)
          .toList();
    }
  }

  void remove(String id) =>
      state = state.where((n) => n.id != id).toList();
}

final notesProvider =
    StateNotifierProvider<NotesNotifier, List<UserNote>>(
        (ref) => NotesNotifier());

class CollectionsNotifier
    extends StateNotifier<List<CustomCollection>> {
  CollectionsNotifier()
      : super(const [
          CustomCollection(id: 'fav-ayat', name: 'Favorite Ayat'),
          CustomCollection(id: 'prayer', name: 'Prayer Hadith'),
          CustomCollection(id: 'ramadan', name: 'Ramadan'),
        ]);

  void add(String name) {
    final trimmed = name.trim();
    if (trimmed.isEmpty) {
      return;
    }
    state = [
      ...state,
      CustomCollection(
          id:
              'c-${DateTime.now().microsecondsSinceEpoch}',
          name: trimmed),
    ];
  }

  void rename(String id, String name) {
    final trimmed = name.trim();
    if (trimmed.isEmpty) {
      return;
    }
    state = state
        .map((c) => c.id == id
            ? CustomCollection(id: c.id, name: trimmed)
            : c)
        .toList();
  }

  void remove(String id) =>
      state = state.where((c) => c.id != id).toList();
}

final collectionsProvider = StateNotifierProvider<
    CollectionsNotifier, List<CustomCollection>>(
    (ref) => CollectionsNotifier());

class HighlightsNotifier extends StateNotifier<List<Highlight>> {
  HighlightsNotifier() : super(const []);

  Highlight? forRef(String refKey) =>
      state.where((h) => h.refKey == refKey).firstOrNull;

  void toggle(String refKey, int colorValue) {
    final existing = forRef(refKey);
    if (existing != null && existing.colorValue == colorValue) {
      state = state.where((h) => h.refKey != refKey).toList();
    } else {
      state = [
        ...state.where((h) => h.refKey != refKey),
        Highlight(
          id: 'hl-$refKey',
          refKey: refKey,
          colorValue: colorValue,
        ),
      ];
    }
  }

  void remove(String id) =>
      state = state.where((h) => h.id != id).toList();
}

final highlightsProvider =
    StateNotifierProvider<HighlightsNotifier, List<Highlight>>(
        (ref) => HighlightsNotifier());

final recentProvider =
    StateProvider<List<RecentItem>>((ref) => const [
          RecentItem(
              refKey: '2:255',
              title: 'Al-Baqarah · 255',
              subtitle: 'Quran',
              kind: BookmarkKind.ayah),
          RecentItem(
              refKey: 'bukhari:1',
              title: 'Bukhari · Hadith 1',
              subtitle: 'Sunnah',
              kind: BookmarkKind.hadith),
        ]);

/// Highlight palette (opaque dots, translucent wash on text).
const List<Color> kHighlightColors = [
  Color(0xFFFFD54F), // amber
  Color(0xFFA5D6A7), // green
  Color(0xFF90CAF9), // blue
  Color(0xFFF48FB1), // pink
];
