// Integrity tests for bundled Quran translations.
// Structural only: full 6236-key coverage per translation, non-empty,
// keys matching the verified Arabic verse keys.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/data/repositories/translation_repository.dart';
import 'package:quran_sunnah_app/data/repositories/verified_asset_quran_repository.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  for (final t in kTranslationCatalog.where((t) => t.bundled)) {
    test('translation ${t.id} covers all 6236 verses', () async {
      final container = ProviderContainer();
      addTearDown(container.dispose);
      final map =
          await container.read(translationTextsProvider(t.id).future);
      expect(map.length, 6236);
      expect(map['1:1'], isNotNull);
      expect(map['1:1']!.isNotEmpty, isTrue);
      expect(map['114:6'], isNotNull);

      // Keys match the verified Arabic edition exactly.
      final quran = VerifiedAssetQuranRepository();
      final arabicKeys = <String>{};
      for (var surah = 1; surah <= 114; surah++) {
        for (final a in await quran.ayahsOfSurah(
            surah, 'hafs-an-asim__uthmani')) {
          arabicKeys.add(a.key);
        }
      }
      expect(map.keys.toSet(), arabicKeys);
    });
  }

  test('unbundled verified candidates stay empty until imported', () async {
    final container = ProviderContainer();
    addTearDown(container.dispose);
    for (final t in kTranslationCatalog.where((t) => !t.bundled)) {
      final map =
          await container.read(translationTextsProvider(t.id).future);
      expect(map, isEmpty, reason: t.id);
      expect(t.version, isNotNull);
    }
  });

  test('unknown translation id yields empty (honest)', () async {
    final container = ProviderContainer();
    addTearDown(container.dispose);
    final map = await container
        .read(translationTextsProvider('xx-none').future);
    expect(map, isEmpty);
  });
}
