import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/database/library_store.dart';
import '../data/models/library.dart';
import 'database_provider.dart';

class LibraryNotifier extends StateNotifier<List<Bookmark>> {
  LibraryNotifier(this._storeFuture) : super(const []) {
    ready = _load();
  }

  final Future<LibraryStore> _storeFuture;
  late final Future<void> ready;

  Future<void> _load() async {
    final store = await _storeFuture;
    state = await store.bookmarks();
  }

  Future<void> toggleAyah(int surah, int ayah) async {
    await ready;
    final store = await _storeFuture;
    await store.toggleAyah(surah, ayah);
    state = await store.bookmarks();
  }

  Future<void> setAyahCollection(
      int surah, int ayah, String? collectionId) async {
    await ready;
    final store = await _storeFuture;
    await store.setAyahCollection(surah, ayah, collectionId);
    state = await store.bookmarks();
  }

  Future<void> toggleHadith(String id, String title) async {
    await ready;
    final store = await _storeFuture;
    await store.toggleHadith(id, title);
    state = await store.bookmarks();
  }

  bool isBookmarked(String key) => state.any((b) => b.refKey == key);

  Future<void> remove(String id) async {
    await ready;
    final store = await _storeFuture;
    await store.removeBookmark(id);
    state = await store.bookmarks();
  }
}

final libraryProvider =
    StateNotifierProvider<LibraryNotifier, List<Bookmark>>(
  (ref) => LibraryNotifier(ref.watch(libraryStoreProvider)),
);

class NotesNotifier extends StateNotifier<List<UserNote>> {
  NotesNotifier(this._storeFuture) : super(const []) {
    ready = _load();
  }

  final Future<LibraryStore> _storeFuture;
  late final Future<void> ready;

  Future<void> _load() async {
    final store = await _storeFuture;
    state = await store.notes();
  }

  UserNote? forRef(String refKey) =>
      state.where((n) => n.refKey == refKey).firstOrNull;

  Future<void> upsert(String refKey, String text) async {
    await ready;
    final store = await _storeFuture;
    await store.upsertNote(refKey, text);
    state = await store.notes();
  }

  Future<void> remove(String id) async {
    await ready;
    final store = await _storeFuture;
    await store.removeNote(id);
    state = await store.notes();
  }
}

final notesProvider =
    StateNotifierProvider<NotesNotifier, List<UserNote>>(
  (ref) => NotesNotifier(ref.watch(libraryStoreProvider)),
);

class CollectionsNotifier
    extends StateNotifier<List<CustomCollection>> {
  CollectionsNotifier(this._storeFuture) : super(defaultCollections) {
    ready = _load();
  }

  final Future<LibraryStore> _storeFuture;
  late final Future<void> ready;

  Future<void> _load() async {
    final store = await _storeFuture;
    state = await store.collections();
  }

  Future<void> add(String name) async {
    await ready;
    final store = await _storeFuture;
    await store.addCollection(name);
    state = await store.collections();
  }

  Future<void> rename(String id, String name) async {
    await ready;
    final store = await _storeFuture;
    await store.renameCollection(id, name);
    state = await store.collections();
  }

  Future<void> remove(String id) async {
    await ready;
    final store = await _storeFuture;
    await store.removeCollection(id);
    state = await store.collections();
  }
}

final collectionsProvider =
    StateNotifierProvider<CollectionsNotifier, List<CustomCollection>>(
  (ref) => CollectionsNotifier(ref.watch(libraryStoreProvider)),
);

class HighlightsNotifier extends StateNotifier<List<Highlight>> {
  HighlightsNotifier(this._storeFuture) : super(const []) {
    ready = _load();
  }

  final Future<LibraryStore> _storeFuture;
  late final Future<void> ready;

  Future<void> _load() async {
    final store = await _storeFuture;
    state = await store.highlights();
  }

  Highlight? forRef(String refKey) =>
      state.where((h) => h.refKey == refKey).firstOrNull;

  Future<void> toggle(String refKey, int colorValue) async {
    await ready;
    final store = await _storeFuture;
    await store.toggleHighlight(refKey, colorValue);
    state = await store.highlights();
  }

  Future<void> remove(String id) async {
    await ready;
    final store = await _storeFuture;
    await store.removeHighlight(id);
    state = await store.highlights();
  }
}

final highlightsProvider =
    StateNotifierProvider<HighlightsNotifier, List<Highlight>>(
  (ref) => HighlightsNotifier(ref.watch(libraryStoreProvider)),
);

class RecentNotifier extends StateNotifier<List<RecentItem>> {
  RecentNotifier(this._storeFuture) : super(const []) {
    ready = _load();
  }

  final Future<LibraryStore> _storeFuture;
  late final Future<void> ready;

  Future<void> _load() async {
    final store = await _storeFuture;
    state = await store.recent();
  }

  Future<void> touch(RecentItem item) async {
    await ready;
    final store = await _storeFuture;
    await store.touchRecent(item);
    state = await store.recent();
  }
}

final recentProvider =
    StateNotifierProvider<RecentNotifier, List<RecentItem>>(
  (ref) => RecentNotifier(ref.watch(libraryStoreProvider)),
);

const List<Color> kHighlightColors = [
  Color(0xFFFFD54F),
  Color(0xFFA5D6A7),
  Color(0xFF90CAF9),
  Color(0xFFF48FB1),
];
