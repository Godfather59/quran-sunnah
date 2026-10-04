import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/database/library_store.dart';
import '../data/models/library.dart';
import 'database_provider.dart';

class LibraryNotifier extends Notifier<List<Bookmark>> {
  late final Future<LibraryStore> _storeFuture;
  late final Future<void> ready;

  @override
  List<Bookmark> build() {
    _storeFuture = ref.watch(libraryStoreProvider);
    ready = _load();
    return const [];
  }

  Future<void> _load() async {
    final store = await _storeFuture;
    if (!ref.mounted) return;
    final data = await store.bookmarks();
    if (!ref.mounted) return;
    state = data;
  }

  Future<void> reload() async {
    await ready;
    final store = await _storeFuture;
    if (!ref.mounted) return;
    final data = await store.bookmarks();
    if (!ref.mounted) return;
    state = data;
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
    NotifierProvider<LibraryNotifier, List<Bookmark>>(
  LibraryNotifier.new,
);

class NotesNotifier extends Notifier<List<UserNote>> {
  late final Future<LibraryStore> _storeFuture;
  late final Future<void> ready;

  @override
  List<UserNote> build() {
    _storeFuture = ref.watch(libraryStoreProvider);
    ready = _load();
    return const [];
  }

  Future<void> _load() async {
    final store = await _storeFuture;
    if (!ref.mounted) return;
    final data = await store.notes();
    if (!ref.mounted) return;
    state = data;
  }

  Future<void> reload() async {
    await ready;
    final store = await _storeFuture;
    if (!ref.mounted) return;
    final data = await store.notes();
    if (!ref.mounted) return;
    state = data;
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
    NotifierProvider<NotesNotifier, List<UserNote>>(
  NotesNotifier.new,
);

class CollectionsNotifier extends Notifier<List<CustomCollection>> {
  late final Future<LibraryStore> _storeFuture;
  late final Future<void> ready;

  @override
  List<CustomCollection> build() {
    _storeFuture = ref.watch(libraryStoreProvider);
    ready = _load();
    return defaultCollections;
  }

  Future<void> _load() async {
    final store = await _storeFuture;
    if (!ref.mounted) return;
    final data = await store.collections();
    if (!ref.mounted) return;
    state = data;
  }

  Future<void> reload() async {
    await ready;
    final store = await _storeFuture;
    if (!ref.mounted) return;
    final data = await store.collections();
    if (!ref.mounted) return;
    state = data;
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
    await ref.read(libraryProvider.notifier).reload();
  }
}

final collectionsProvider =
    NotifierProvider<CollectionsNotifier, List<CustomCollection>>(
  CollectionsNotifier.new,
);

class HighlightsNotifier extends Notifier<List<Highlight>> {
  late final Future<LibraryStore> _storeFuture;
  late final Future<void> ready;

  @override
  List<Highlight> build() {
    _storeFuture = ref.watch(libraryStoreProvider);
    ready = _load();
    return const [];
  }

  Future<void> _load() async {
    final store = await _storeFuture;
    if (!ref.mounted) return;
    final data = await store.highlights();
    if (!ref.mounted) return;
    state = data;
  }

  Future<void> reload() async {
    await ready;
    final store = await _storeFuture;
    if (!ref.mounted) return;
    final data = await store.highlights();
    if (!ref.mounted) return;
    state = data;
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
    NotifierProvider<HighlightsNotifier, List<Highlight>>(
  HighlightsNotifier.new,
);

class RecentNotifier extends Notifier<List<RecentItem>> {
  late final Future<LibraryStore> _storeFuture;
  late final Future<void> ready;

  @override
  List<RecentItem> build() {
    _storeFuture = ref.watch(libraryStoreProvider);
    ready = _load();
    return const [];
  }

  Future<void> _load() async {
    final store = await _storeFuture;
    if (!ref.mounted) return;
    final data = await store.recent();
    if (!ref.mounted) return;
    state = data;
  }

  Future<void> reload() async {
    await ready;
    final store = await _storeFuture;
    if (!ref.mounted) return;
    final data = await store.recent();
    if (!ref.mounted) return;
    state = data;
  }

  Future<void> touch(RecentItem item) async {
    await ready;
    final store = await _storeFuture;
    await store.touchRecent(item);
    state = await store.recent();
  }
}

final recentProvider =
    NotifierProvider<RecentNotifier, List<RecentItem>>(
  RecentNotifier.new,
);

const List<Color> kHighlightColors = [
  Color(0xFFFFD54F),
  Color(0xFFA5D6A7),
  Color(0xFF90CAF9),
  Color(0xFFF48FB1),
];
