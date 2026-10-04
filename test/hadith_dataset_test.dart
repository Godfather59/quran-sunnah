// Integrity tests for core Bukhari/Muslim plus optional package sources.
// Structural only: counts preserved losslessly, references intact,
// no empty matn, ids unique, Arabic search normalization works.

import 'dart:convert';
import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/core/utils/text_utils.dart';
import 'package:quran_sunnah_app/data/content/content_packages.dart';
import 'package:quran_sunnah_app/data/repositories/hadith_repository.dart';
import 'package:quran_sunnah_app/data/repositories/verified_asset_hadith_repository.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late Directory packageRoot;

  setUpAll(() async {
    packageRoot =
        await Directory.systemTemp.createTemp('content-packages-hadith-');
    ContentPackageStore.instance.resetForTesting();
    ContentPackageStore.instance.setRootDirectoryForTesting(packageRoot);
  });

  tearDownAll(() async {
    ContentPackageStore.instance.resetForTesting();
    if (await packageRoot.exists()) {
      await packageRoot.delete(recursive: true);
    }
  });

  test('bukhari index: 98 sections, 7589 entries preserved', () async {
    final repo = VerifiedAssetHadithRepository();
    final secs = await repo.sections();
    expect(secs.length, 98);
    final counted =
        secs.fold<int>(0, (a, s) => a + s.count);
    expect(counted, 7589);
  });

  test('all hadiths: references intact, unique ids, honest nulls',
      () async {
    final repo = VerifiedAssetHadithRepository();
    final all = await repo.allBukhari();
    expect(all.length, 7589);
    final ids = <String>{};
    var placeholders = 0;
    for (final h in all) {
      expect(ids.add(h.id), isTrue,
          reason: 'duplicate id ${h.id}');
      expect(h.collectionId, 'bukhari');
      expect(h.book.isNotEmpty, isTrue);
      expect(h.hadithNumber.isNotEmpty, isTrue);
      if (h.isPlaceholder) {
        placeholders++;
        continue;
      }
      expect(h.matnAr.isNotEmpty, isTrue);
      // Source provides no grades/sanad breakdown: must stay null.
      expect(h.grade, isNull);
      expect(h.gradingAuthority, isNull);
    }
    // 9 upstream numbers ship without matn: references preserved.
    expect(placeholders, 9);
  });

  test('query paginates and searches Arabic diacritic-insensitive',
      () async {
    final repo = VerifiedAssetHadithRepository();
    const f = HadithFilter(collectionIds: {'bukhari'});
    final p1 = await repo.query(f, limit: 20, offset: 0);
    expect(p1.length, 20);
    expect(p1.first.hadithNumber, '1');
    final p2 = await repo.query(f, limit: 20, offset: 20);
    expect(p2.length, 20);
    expect(p2.first.id, isNot(p1.first.id));

    // Search with and without diacritics hits the same matn.
    final withTashkeel = await repo.query(
        const HadithFilter(
            collectionIds: {'bukhari'}, query: 'الأعمال بالنيات'),
        limit: 5);
    final plain = await repo.query(
        const HadithFilter(
            collectionIds: {'bukhari'}, query: 'الاعمال بالنيات'),
        limit: 5);
    expect(withTashkeel.isNotEmpty, isTrue);
    expect(plain.isNotEmpty, isTrue);
    expect(plain.first.id, withTashkeel.first.id);
    expect(normalizeArabic('الأعْمَالُ'), normalizeArabic('الاعمال'));
  });

  test('unbundled collection yields empty (honest)', () async {
    final repo = VerifiedAssetHadithRepository();
    final res = await repo.query(
        const HadithFilter(collectionIds: {'ahmad'}),
        limit: 10);
    expect(res, isEmpty);
  });

  group('sahih muslim (bundled)', () {
    test('index: 57 sections incl. introduction, 7563 preserved',
        () async {
      final repo = VerifiedAssetHadithRepository();
      final secs = await repo.sections('muslim');
      expect(secs.length, 57);
      expect(secs.first.section, 0);
      final counted =
          secs.fold<int>(0, (a, s) => a + s.count);
      expect(counted, 7563);
    });

    test('references intact, unique ids, honest nulls', () async {
      final repo = VerifiedAssetHadithRepository();
      final all = await repo.allIn('muslim');
      expect(all.length, 7563);
      final ids = <String>{};
      for (final h in all) {
        expect(ids.add(h.id), isTrue,
            reason: 'duplicate id ${h.id}');
        expect(h.collectionId, 'muslim');
        expect(h.grade, isNull);
        expect(h.gradingAuthority, isNull);
      }
    });

    test('combined query spans bundled collections', () async {
      final repo = VerifiedAssetHadithRepository();
      const all = HadithFilter(collectionIds: {});
      final page =
          await repo.query(all, limit: 50, offset: 0);
      expect(page.length, 50);
      expect(page.first.collectionId, 'bukhari');
    });

    test('optional collections stay uninstalled but source counts remain exact',
        () async {
      final repo = VerifiedAssetHadithRepository();
      final collections = await repo.collections();
      const expected = {
        'abudawud': (44, 5274),
        'tirmidhi': (50, 3998),
        'nasai': (52, 5765),
        'ibnmajah': (38, 4343),
        'malik': (62, 1858),
        'nawawi': (2, 42),
        'qudsi': (2, 40),
        'dehlawi': (2, 40),
      };

      for (final entry in expected.entries) {
        final meta =
            collections.firstWhere((c) => c.id == entry.key);
        expect(meta.isDownloaded, isFalse, reason: entry.key);

        final index = jsonDecode(
          await File('assets/hadith/${entry.key}/index.json')
              .readAsString(),
        ) as Map<String, dynamic>;
        final sections = index['sections'] as List;
        expect(sections.length, entry.value.$1,
            reason: '${entry.key} sections');
        final counted = sections.fold<int>(
          0,
          (sum, item) =>
              sum +
              ((item as Map<String, dynamic>)['count'] as num)
                  .toInt(),
        );
        expect(counted, entry.value.$2,
            reason: '${entry.key} entries');

        final page = await repo.query(
          HadithFilter(collectionIds: {entry.key}),
          limit: 5,
        );
        expect(page, isEmpty,
            reason: '${entry.key} must require package install');
      }
    });
  });
}
