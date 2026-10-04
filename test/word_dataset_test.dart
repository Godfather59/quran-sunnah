// Integrity tests for bundled word morphology.
// Structural only: 114 surahs, token counts match the verse text,
// glosses come only from the dataset (never invented).

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/data/repositories/verified_asset_quran_repository.dart';
import 'package:quran_sunnah_app/data/repositories/word_repository.dart';
import 'package:quran_sunnah_app/data/seed/surah_metadata.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('word data covers all surahs and mirrors verse tokens',
      () async {
    final container = ProviderContainer();
    addTearDown(container.dispose);
    final quran = VerifiedAssetQuranRepository();
    var glossed = 0;
    var total = 0;
    for (final m in kSurahMetadata) {
      final map = await container
          .read(wordSurahProvider(m.number).future);
      final ayahs = await quran.ayahsOfSurah(
          m.number, 'hafs-an-asim__uthmani');
      expect(map.length, ayahs.length,
          reason: 'surah ${m.number} coverage');
      for (final a in ayahs) {
        final words = map[a.ayah] ?? [];
        // Same token stream (marks included) as the verse text.
        expect(
            words.map((w) => w.word).join(' '), a.text,
            reason: a.key);
        for (final w in words) {
          total++;
          if (w.hasGloss || w.isMark) {
            glossed++;
          }
        }
      }
    }
    // ~98.6% tokens carry a gloss or mark tag; the rest render
    // as plain words (never guessed).
    expect(glossed / total, greaterThan(0.98));
  });

  test('unknown surah yields empty (honest)', () async {
    final container = ProviderContainer();
    addTearDown(container.dispose);
    final map =
        await container.read(wordSurahProvider(999).future);
    expect(map, isEmpty);
  });
}
