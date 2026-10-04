// Integrity tests for bundled Tafsir datasets.
// Structural only: full 6236 coverage per tafsir, non-empty,
// keys matching the verified Arabic verse keys.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/data/repositories/tafsir_repository.dart';
import 'package:quran_sunnah_app/data/repositories/verified_asset_quran_repository.dart';
import 'package:quran_sunnah_app/data/seed/surah_metadata.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  for (final t in kTafsirCatalog.where((t) => t.bundled)) {
    test('tafsir ${t.id} covers all 6236 verses', () async {
      final container = ProviderContainer();
      addTearDown(container.dispose);
      final quran = VerifiedAssetQuranRepository();
      var total = 0;
      for (final m in kSurahMetadata) {
        final map = await container
            .read(tafsirSurahProvider((t.id, m.number)).future);
        expect(map.length, m.ayahCount,
            reason: '${t.id} surah ${m.number}');
        for (final a in await quran.ayahsOfSurah(
            m.number, 'hafs-an-asim__uthmani')) {
          expect(map.containsKey(a.ayah), isTrue);
          expect(map[a.ayah]!.isNotEmpty, isTrue);
        }
        total += map.length;
      }
      expect(total, 6236);
    });
  }

  test('unbundled tafsir yields empty (honest)', () async {
    final container = ProviderContainer();
    addTearDown(container.dispose);
    final map = await container
        .read(tafsirSurahProvider(('ibn-kathir', 112)).future);
    expect(map, isEmpty);
  });
}
