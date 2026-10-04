// Integrity tests for the bundled verified Quran text.
// Structural only: verse/surah counts per edition, no empty verses,
// per-surah counts matching metadata. (No verse wording is asserted
// here beyond existence — the bytes ship verbatim from Tanzil.)

import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/data/repositories/quran_metadata.dart';
import 'package:quran_sunnah_app/data/repositories/verified_asset_quran_repository.dart';
import 'package:quran_sunnah_app/data/seed/surah_metadata.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  for (final editionId in kVerifiedQuranAssets.keys) {
    test('edition $editionId loads 6236 non-empty verses', () async {
      final repo = VerifiedAssetQuranRepository();
      final all = <String, int>{};
      var total = 0;
      for (final meta in kSurahMetadata) {
        final ayahs =
            await repo.ayahsOfSurah(meta.number, editionId);
        expect(ayahs.length, meta.ayahCount,
            reason: 'surah ${meta.number}');
        for (final a in ayahs) {
          expect(a.text.isNotEmpty, isTrue);
          expect(a.isPlaceholder, isFalse);
        }
        total += ayahs.length;
        all['${meta.number}'] = ayahs.length;
      }
      expect(total, 6236);
    });
  }

  test('unknown edition yields honest placeholders', () async {
    final repo = VerifiedAssetQuranRepository();
    final ayahs =
        await repo.ayahsOfSurah(112, 'shubah-an-asim__uthmani');
    expect(ayahs.length, 4);
    expect(ayahs.every((a) => a.isPlaceholder), isTrue);
  });

  group('verified metadata (Tanzil quran-data.xml)', () {
    test('counts: 30 juz, 240 quarters, 604 pages, 15 sajdas',
        () async {
      final meta = await QuranMetadata.load();
      expect(meta.juzStarts.length, 30);
      expect(meta.quarterStarts.length, 240);
      expect(meta.pageStarts.length, 604);
      expect(meta.sajdas.length, 15);
      expect(meta.juzStarts.first.surah, 1);
      expect(meta.juzStarts.first.ayah, 1);
    });

    test('every verse maps to juz 1..30 and page 1..604', () async {
      final repo = VerifiedAssetQuranRepository();
      final meta = await QuranMetadata.load();
      var total = 0;
      final pages = <int>{};
      for (final m in kSurahMetadata) {
        final ayahs = await repo.ayahsOfSurah(
            m.number, 'hafs-an-asim__uthmani');
        for (final a in ayahs) {
          expect(a.juz, inInclusiveRange(1, 30));
          expect(a.hizb, inInclusiveRange(1, 60));
          expect(a.rub, inInclusiveRange(1, 4));
          expect(a.page, inInclusiveRange(1, 604));
          expect(a.juz, meta.juzOf(a.surah, a.ayah));
          expect(a.page, meta.pageOf(a.surah, a.ayah));
          pages.add(a.page!);
          total++;
        }
      }
      expect(total, 6236);
      expect(pages.length, 604);
    });

    test('ayahsOfJuz(1) starts at 1:1 and ayahsOfPage(1) is Fatiha',
        () async {
      final repo = VerifiedAssetQuranRepository();
      const ed = 'hafs-an-asim__uthmani';
      final juz1 = await repo.ayahsOfJuz(1, ed);
      expect(juz1.first.surah, 1);
      expect(juz1.first.ayah, 1);
      final page1 = await repo.ayahsOfPage(1, ed);
      expect(page1.length, 7);
      expect(
          page1.every((a) => a.surah == 1), isTrue);
    });
  });
}
