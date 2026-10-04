import 'dart:convert';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/data/database/app_database.dart';
import 'package:quran_sunnah_app/state/database_provider.dart';
import 'package:quran_sunnah_app/state/library_state.dart';
import 'package:shared_preferences/shared_preferences.dart';

ProviderContainer _container(AppDatabase db) => ProviderContainer(
      overrides: [
        appDatabaseProvider.overrideWithValue(Future.value(db)),
      ],
    );

Future<void> _hydrate(ProviderContainer container) async {
  await Future.wait([
    container.read(libraryProvider.notifier).ready,
    container.read(notesProvider.notifier).ready,
    container.read(collectionsProvider.notifier).ready,
    container.read(highlightsProvider.notifier).ready,
    container.read(recentProvider.notifier).ready,
  ]);
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() => SharedPreferences.setMockInitialValues({}));

  test('notes upsert trims, edits and empty text deletes', () async {
    final db = AppDatabase.memory();
    addTearDown(db.close);
    final container = _container(db);
    addTearDown(container.dispose);
    await _hydrate(container);

    final notes = container.read(notesProvider.notifier);
    await notes.upsert('2:255', '  my note  ');
    expect(container.read(notesProvider).single.text, 'my note');

    await notes.upsert('2:255', 'edited');
    expect(container.read(notesProvider).single.text, 'edited');

    await notes.upsert('2:255', '   ');
    expect(container.read(notesProvider), isEmpty);
  });

  test('highlights toggle and recolor in SQLite', () async {
    final db = AppDatabase.memory();
    addTearDown(db.close);
    final container = _container(db);
    addTearDown(container.dispose);
    await _hydrate(container);

    final highlights = container.read(highlightsProvider.notifier);
    await highlights.toggle('2:255', 0xFFFFD54F);
    expect(container.read(highlightsProvider), hasLength(1));

    await highlights.toggle('2:255', 0xFFFFD54F);
    expect(container.read(highlightsProvider), isEmpty);

    await highlights.toggle('2:255', 0xFFFFD54F);
    await highlights.toggle('2:255', 0xFF90CAF9);
    final all = container.read(highlightsProvider);
    expect(all, hasLength(1));
    expect(all.single.colorValue, 0xFF90CAF9);
  });

  test('collections survive and deleted collection detaches bookmark',
      () async {
    final db = AppDatabase.memory();
    addTearDown(db.close);
    final container = _container(db);
    addTearDown(container.dispose);
    await _hydrate(container);

    final collections = container.read(collectionsProvider.notifier);
    final library = container.read(libraryProvider.notifier);

    await collections.add('Study');
    final added = container
        .read(collectionsProvider)
        .firstWhere((c) => c.name == 'Study');

    await collections.rename(added.id, 'Deep Study');
    expect(
      container
          .read(collectionsProvider)
          .firstWhere((c) => c.id == added.id)
          .name,
      'Deep Study',
    );

    await library.setAyahCollection(2, 255, added.id);
    expect(
      container
          .read(libraryProvider)
          .firstWhere((b) => b.refKey == '2:255')
          .collectionId,
      added.id,
    );

    await collections.remove(added.id);
    await library.ready;
    // Reload the bookmark view because removing a collection is performed by
    // the collections notifier while the database atomically detaches items.
    final restarted = _container(db);
    addTearDown(restarted.dispose);
    await _hydrate(restarted);
    expect(
      restarted
          .read(libraryProvider)
          .firstWhere((b) => b.refKey == '2:255')
          .collectionId,
      isNull,
    );
  });

  test('bookmarks survive provider recreation in the same database',
      () async {
    final db = AppDatabase.memory();
    addTearDown(db.close);

    final first = _container(db);
    await _hydrate(first);
    await first.read(libraryProvider.notifier).toggleAyah(2, 255);
    first.dispose();

    final second = _container(db);
    addTearDown(second.dispose);
    await _hydrate(second);
    expect(
      second.read(libraryProvider).any((b) => b.refKey == '2:255'),
      isTrue,
    );
  });

  test('legacy SharedPreferences library migrates once into SQLite',
      () async {
    final legacy = jsonEncode([
      {
        'id': 'legacy-1',
        'kind': 0,
        'refKey': '1:1',
        'title': 'Legacy ayah',
        'subtitle': 'Quran bookmark',
        'collectionId': null,
        'createdAt': '2026-01-01T00:00:00.000Z',
      }
    ]);
    SharedPreferences.setMockInitialValues({
      'library.bookmarks.v1': legacy,
    });

    final db = AppDatabase.memory();
    addTearDown(db.close);
    final container = _container(db);
    addTearDown(container.dispose);
    await _hydrate(container);

    expect(
      container.read(libraryProvider).map((b) => b.refKey),
      contains('1:1'),
    );

    final prefs = await SharedPreferences.getInstance();
    expect(prefs.containsKey('library.bookmarks.v1'), isFalse);

    // A second initialization must not duplicate the migrated bookmark.
    final second = _container(db);
    addTearDown(second.dispose);
    await _hydrate(second);
    expect(
      second.read(libraryProvider).where((b) => b.refKey == '1:1'),
      hasLength(1),
    );
  });
}
