import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../data/models/library.dart';

const _bookmarksKey = 'library.bookmarks.v1';
const _notesKey = 'library.notes.v1';
const _collectionsKey = 'library.collections.v1';
const _highlightsKey = 'library.highlights.v1';
const _recentKey = 'library.recent.v1';

List<dynamic> _decodeList(String? raw) {
  if (raw == null || raw.isEmpty) return const [];
  try {
    final value = jsonDecode(raw);
    return value is List ? value : const [];
  } catch (_) {
    return const [];
  }
}

class LibraryNotifier extends StateNotifier<List<Bookmark>> {
  LibraryNotifier() : super(const []) {
    final initial = state;
    unawaited(_load(initial));
  }

  Future<void> _load(List<Bookmark> initial) async {
    final p = await SharedPreferences.getInstance();
    if (!identical(state, initial)) return;
    final out = <Bookmark>[];
    for (final item in _decodeList(p.getString(_bookmarksKey))) {
      if (item is! Map) continue;
      final m = Map<String, dynamic>.from(item);
      final kindIndex = m['kind'] as int? ?? -1;
      if (kindIndex < 0 || kindIndex >= BookmarkKind.values.length) continue;
      out.add(Bookmark(
        id: m['id'] as String? ?? '',
        kind: BookmarkKind.values[kindIndex],
        refKey: m['refKey'] as String? ?? '',
        title: m['title'] as String? ?? '',
        subtitle: m['subtitle'] as String? ?? '',
        collectionId: m['collectionId'] as String?,
        createdAt: DateTime.tryParse(m['createdAt'] as String? ?? ''),
      ));
    }
    state = out.where((b) => b.id.isNotEmpty && b.refKey.isNotEmpty).toList();
  }

  Future<void> _save() async {
    final p = await SharedPreferences.getInstance();
    await p.setString(
      _bookmarksKey,
      jsonEncode([
        for (final b in state)
          {
            'id': b.id,
            'kind': b.kind.index,
            'refKey': b.refKey,
            'title': b.title,
            'subtitle': b.subtitle,
            'collectionId': b.collectionId,
            'createdAt': b.createdAt?.toIso8601String(),
          },
      ]),
    );
  }

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
    unawaited(_save());
  }

  void setAyahCollection(int surah, int ayah, String? collectionId) {
    final key = '$surah:$ayah';
    final existing = state.where((b) => b.refKey == key).firstOrNull;
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
    unawaited(_save());
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
    unawaited(_save());
  }

  bool isBookmarked(String key) => state.any((b) => b.refKey == key);

  void remove(String id) {
    state = state.where((b) => b.id != id).toList();
    unawaited(_save());
  }
}

final libraryProvider =
    StateNotifierProvider<LibraryNotifier, List<Bookmark>>(
        (ref) => LibraryNotifier());

class NotesNotifier extends StateNotifier<List<UserNote>> {
  NotesNotifier() : super(const []) {
    final initial = state;
    unawaited(_load(initial));
  }

  Future<void> _load(List<UserNote> initial) async {
    final p = await SharedPreferences.getInstance();
    if (!identical(state, initial)) return;
    final out = <UserNote>[];
    for (final item in _decodeList(p.getString(_notesKey))) {
      if (item is! Map) continue;
      final m = Map<String, dynamic>.from(item);
      final id = m['id'] as String? ?? '';
      final refKey = m['refKey'] as String? ?? '';
      final text = m['text'] as String? ?? '';
      if (id.isEmpty || refKey.isEmpty || text.isEmpty) continue;
      out.add(UserNote(
        id: id,
        refKey: refKey,
        text: text,
        createdAt: DateTime.tryParse(m['createdAt'] as String? ?? ''),
      ));
    }
    state = out;
  }

  Future<void> _save() async {
    final p = await SharedPreferences.getInstance();
    await p.setString(
      _notesKey,
      jsonEncode([
        for (final n in state)
          {
            'id': n.id,
            'refKey': n.refKey,
            'text': n.text,
            'createdAt': n.createdAt?.toIso8601String(),
          },
      ]),
    );
  }

  UserNote? forRef(String refKey) =>
      state.where((n) => n.refKey == refKey).firstOrNull;

  void upsert(String refKey, String text) {
    final trimmed = text.trim();
    if (trimmed.isEmpty) {
      state = state.where((n) => n.refKey != refKey).toList();
      unawaited(_save());
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
                  createdAt: n.createdAt,
                )
              : n)
          .toList();
    }
    unawaited(_save());
  }

  void remove(String id) {
    state = state.where((n) => n.id != id).toList();
    unawaited(_save());
  }
}

final notesProvider =
    StateNotifierProvider<NotesNotifier, List<UserNote>>(
        (ref) => NotesNotifier());

const _defaultCollections = [
  CustomCollection(id: 'fav-ayat', name: 'Favorite Ayat'),
  CustomCollection(id: 'prayer', name: 'Prayer Hadith'),
  CustomCollection(id: 'ramadan', name: 'Ramadan'),
];

class CollectionsNotifier extends StateNotifier<List<CustomCollection>> {
  CollectionsNotifier() : super(_defaultCollections) {
    final initial = state;
    unawaited(_load(initial));
  }

  Future<void> _load(List<CustomCollection> initial) async {
    final p = await SharedPreferences.getInstance();
    if (!identical(state, initial)) return;
    final raw = p.getString(_collectionsKey);
    if (raw == null) return;
    final out = <CustomCollection>[];
    for (final item in _decodeList(raw)) {
      if (item is! Map) continue;
      final m = Map<String, dynamic>.from(item);
      final id = m['id'] as String? ?? '';
      final name = m['name'] as String? ?? '';
      if (id.isNotEmpty && name.isNotEmpty) {
        out.add(CustomCollection(id: id, name: name));
      }
    }
    state = out;
  }

  Future<void> _save() async {
    final p = await SharedPreferences.getInstance();
    await p.setString(
      _collectionsKey,
      jsonEncode([
        for (final c in state) {'id': c.id, 'name': c.name},
      ]),
    );
  }

  void add(String name) {
    final trimmed = name.trim();
    if (trimmed.isEmpty) return;
    state = [
      ...state,
      CustomCollection(
        id: 'c-${DateTime.now().microsecondsSinceEpoch}',
        name: trimmed,
      ),
    ];
    unawaited(_save());
  }

  void rename(String id, String name) {
    final trimmed = name.trim();
    if (trimmed.isEmpty) return;
    state = state
        .map((c) => c.id == id
            ? CustomCollection(id: c.id, name: trimmed)
            : c)
        .toList();
    unawaited(_save());
  }

  void remove(String id) {
    state = state.where((c) => c.id != id).toList();
    unawaited(_save());
  }
}

final collectionsProvider =
    StateNotifierProvider<CollectionsNotifier, List<CustomCollection>>(
        (ref) => CollectionsNotifier());

class HighlightsNotifier extends StateNotifier<List<Highlight>> {
  HighlightsNotifier() : super(const []) {
    final initial = state;
    unawaited(_load(initial));
  }

  Future<void> _load(List<Highlight> initial) async {
    final p = await SharedPreferences.getInstance();
    if (!identical(state, initial)) return;
    final out = <Highlight>[];
    for (final item in _decodeList(p.getString(_highlightsKey))) {
      if (item is! Map) continue;
      final m = Map<String, dynamic>.from(item);
      final id = m['id'] as String? ?? '';
      final refKey = m['refKey'] as String? ?? '';
      final color = m['colorValue'] as int?;
      if (id.isNotEmpty && refKey.isNotEmpty && color != null) {
        out.add(Highlight(id: id, refKey: refKey, colorValue: color));
      }
    }
    state = out;
  }

  Future<void> _save() async {
    final p = await SharedPreferences.getInstance();
    await p.setString(
      _highlightsKey,
      jsonEncode([
        for (final h in state)
          {
            'id': h.id,
            'refKey': h.refKey,
            'colorValue': h.colorValue,
          },
      ]),
    );
  }

  Highlight? forRef(String refKey) =>
      state.where((h) => h.refKey == refKey).firstOrNull;

  void toggle(String refKey, int colorValue) {
    final existing = forRef(refKey);
    if (existing != null && existing.colorValue == colorValue) {
      state = state.where((h) => h.refKey != refKey).toList();
    } else {
      state = [
        ...state.where((h) => h.refKey != refKey),
        Highlight(id: 'hl-$refKey', refKey: refKey, colorValue: colorValue),
      ];
    }
    unawaited(_save());
  }

  void remove(String id) {
    state = state.where((h) => h.id != id).toList();
    unawaited(_save());
  }
}

final highlightsProvider =
    StateNotifierProvider<HighlightsNotifier, List<Highlight>>(
        (ref) => HighlightsNotifier());

class RecentNotifier extends StateNotifier<List<RecentItem>> {
  RecentNotifier() : super(const []) {
    final initial = state;
    unawaited(_load(initial));
  }

  Future<void> _load(List<RecentItem> initial) async {
    final p = await SharedPreferences.getInstance();
    if (!identical(state, initial)) return;
    final out = <RecentItem>[];
    for (final item in _decodeList(p.getString(_recentKey))) {
      if (item is! Map) continue;
      final m = Map<String, dynamic>.from(item);
      final kindIndex = m['kind'] as int? ?? -1;
      if (kindIndex < 0 || kindIndex >= BookmarkKind.values.length) continue;
      final refKey = m['refKey'] as String? ?? '';
      if (refKey.isEmpty) continue;
      out.add(RecentItem(
        refKey: refKey,
        title: m['title'] as String? ?? '',
        subtitle: m['subtitle'] as String? ?? '',
        kind: BookmarkKind.values[kindIndex],
      ));
    }
    state = out;
  }

  Future<void> _save() async {
    final p = await SharedPreferences.getInstance();
    await p.setString(
      _recentKey,
      jsonEncode([
        for (final r in state)
          {
            'refKey': r.refKey,
            'title': r.title,
            'subtitle': r.subtitle,
            'kind': r.kind.index,
          },
      ]),
    );
  }

  void touch(RecentItem item) {
    state = [
      item,
      ...state.where((r) => r.refKey != item.refKey),
    ].take(50).toList();
    unawaited(_save());
  }
}

final recentProvider =
    StateNotifierProvider<RecentNotifier, List<RecentItem>>(
        (ref) => RecentNotifier());

/// Highlight palette (opaque dots, translucent wash on text).
const List<Color> kHighlightColors = [
  Color(0xFFFFD54F),
  Color(0xFFA5D6A7),
  Color(0xFF90CAF9),
  Color(0xFFF48FB1),
];
