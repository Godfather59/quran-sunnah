import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/state/library_state.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  setUp(() => SharedPreferences.setMockInitialValues({}));
  test('notes upsert trims, empties delete', () async {
    final container = ProviderContainer();
    addTearDown(container.dispose);
    final notes = container.read(notesProvider.notifier);

    notes.upsert('2:255', '  my note  ');
    expect(container.read(notesProvider).length, 1);
    expect(container.read(notesProvider).first.text, 'my note');
    await Future<void>.delayed(Duration.zero);

    notes.upsert('2:255', 'edited');
    expect(container.read(notesProvider).length, 1);
    expect(container.read(notesProvider).first.text, 'edited');

    notes.upsert('2:255', '   ');
    expect(container.read(notesProvider), isEmpty);
  });

  test('highlights toggle and recolor', () async {
    final container = ProviderContainer();
    addTearDown(container.dispose);
    final hl = container.read(highlightsProvider.notifier);

    hl.toggle('2:255', 0xFFFFD54F);
    expect(container.read(highlightsProvider).length, 1);

    // Same color again removes.
    hl.toggle('2:255', 0xFFFFD54F);
    expect(container.read(highlightsProvider), isEmpty);

    // Different color replaces.
    hl.toggle('2:255', 0xFFFFD54F);
    hl.toggle('2:255', 0xFF90CAF9);
    final all = container.read(highlightsProvider);
    expect(all.length, 1);
    expect(all.first.colorValue, 0xFF90CAF9);
  });

  test('collections add/rename/remove; bookmark assignment', () async {
    final container = ProviderContainer();
    addTearDown(container.dispose);
    final cols = container.read(collectionsProvider.notifier);
    final lib = container.read(libraryProvider.notifier);

    cols.add('Study');
    final added = container
        .read(collectionsProvider)
        .firstWhere((c) => c.name == 'Study');
    cols.rename(added.id, 'Deep Study');
    expect(
        container
            .read(collectionsProvider)
            .firstWhere((c) => c.id == added.id)
            .name,
        'Deep Study');

    lib.setAyahCollection(2, 255, added.id);
    expect(
        container
            .read(libraryProvider)
            .firstWhere((b) => b.refKey == '2:255')
            .collectionId,
        added.id);

    cols.remove(added.id);
    expect(
        container
            .read(collectionsProvider)
            .any((c) => c.id == added.id),
        isFalse);
  });

  test('bookmarks survive provider recreation', () async {
    final first = ProviderContainer();
    await first.read(libraryProvider.notifier).toggleAyah(2, 255);
    first.dispose();

    final second = ProviderContainer();
    await Future<void>.delayed(const Duration(milliseconds: 20));
    expect(
      second.read(libraryProvider).any((b) => b.refKey == '2:255'),
      isTrue,
    );
    second.dispose();
  });
}
