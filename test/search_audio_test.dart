// Tests for global search + audio numbering.
// Verifies: verse shortcut, diacritic-insensitive Quran/Hadith/Tafsir
// hits, surah lookup, honest narrator/topic emptiness, CDN numbering.

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/data/database/app_database.dart';
import 'package:quran_sunnah_app/data/repositories/hadith_repository.dart';
import 'package:quran_sunnah_app/data/repositories/verified_asset_hadith_repository.dart';
import 'package:quran_sunnah_app/data/repositories/verified_asset_quran_repository.dart';
import 'package:quran_sunnah_app/data/services/audio_service.dart';
import 'package:quran_sunnah_app/data/services/search_service.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late AppDatabase database;
  late SearchService search;

  setUpAll(() {
    database = AppDatabase.memory();
    search = SearchService(
      quran: VerifiedAssetQuranRepository(),
      hadith: VerifiedAssetHadithRepository(),
      database: database,
    );
  });

  tearDownAll(() => database.close());

  test('verse shortcut 2:255 resolves', () async {
    final r = await search.search(
      query: '2:255',
      editionId: 'hafs-an-asim__uthmani',
      tafsirId: 'jalalayn',
    );
    expect(
        r.quran.any((h) => h.surah == 2 && h.ayah == 255), isTrue);
  });

  test('arabic search finds verse with and without diacritics',
      () async {
    final svc = search;
    const ed = 'hafs-an-asim__uthmani';
    final withMarks = await svc.search(
        query: 'الْحَمْدُ لِلَّهِ',
        editionId: ed,
        tafsirId: 'jalalayn');
    final plain = await svc.search(
        query: 'الحمد لله', editionId: ed, tafsirId: 'jalalayn');
    expect(withMarks.quran.isNotEmpty, isTrue);
    expect(plain.quran.isNotEmpty, isTrue);
    expect(plain.quran.first.refKey,
        withMarks.quran.first.refKey);
  });

  test('hadith search hits bundled collections', () async {
    final r = await search.search(
      query: 'الأعمال',
      editionId: 'hafs-an-asim__uthmani',
      tafsirId: 'jalalayn',
      hadithFilter:
          const HadithFilter(collectionIds: {'bukhari', 'muslim'}),
    );
    expect(r.hadith.isNotEmpty, isTrue);
    expect(r.hadith.first.hadith, isNotNull);
  });

  test('tafsir + surah search hit bundled content', () async {
    final svc = search;
    const ed = 'hafs-an-asim__uthmani';
    final t = await svc.search(
        query: 'الصراط', editionId: ed, tafsirId: 'jalalayn');
    expect(t.tafsir.isNotEmpty, isTrue);
    expect(t.tafsir.first.surah, isNotNull);

    final sq = svc.searchSurahs('بقرة');
    expect(sq.any((h) => h.surah == 2), isTrue);
    expect(svc.searchSurahs('').isEmpty, isTrue);
  });

  test('empty query yields empty results', () async {
    final r = await search.search(
      query: '   ',
      editionId: 'hafs-an-asim__uthmani',
      tafsirId: 'jalalayn',
    );
    expect(r.total, 0);
  });

  test('FTS5 index is persisted and repeat search does not duplicate rows',
      () async {
    await search.search(
      query: 'الحمد',
      editionId: 'hafs-an-asim__uthmani',
      tafsirId: 'jalalayn',
    );
    final before = await database.countSearchDocuments();
    expect(before, greaterThan(45000));

    await search.search(
      query: 'الرحمن',
      editionId: 'hafs-an-asim__uthmani',
      tafsirId: 'jalalayn',
    );
    final after = await database.countSearchDocuments();
    expect(after, before);
  });

  test('bundled Hadith collections expose no invented structured fields',
      () async {
    final repo = VerifiedAssetHadithRepository();
    final caps = await repo.capabilities({'bukhari', 'muslim'});
    expect(caps.narrator, isFalse);
    expect(caps.grade, isFalse);
    expect(caps.topics, isFalse);
    expect(caps.sanad, isFalse);
  });

  test('global ayah numbering anchors CDN files', () {
    expect(globalAyahNumber(1, 1), 1);
    expect(globalAyahNumber(1, 7), 7);
    expect(globalAyahNumber(2, 1), 8);
    expect(globalAyahNumber(114, 6), 6236);
  });

  test('reciter binding: hafs streams, others refused', () {
    SharedPreferences.setMockInitialValues({});
    final container = ProviderContainer();
    addTearDown(container.dispose);
    final svc = container.read(audioServiceProvider.notifier);
    expect(
        svc.streamPrefixFor('hafs-an-asim'), isNotNull);
    expect(
        svc.streamPrefixFor('warsh-an-nafi'), isNull);
  });

  test('reciter catalog: verified hafs bindings only', () {
    expect(kReciters.length, 8);
    for (final r in kReciters) {
      expect(r.riwayaKey, 'hafs-an-asim');
      expect(r.identifier.startsWith('ar.'), isTrue);
      expect(
          r.fileUrl(1),
          'https://cdn.islamic.network/quran/audio/128/${r.identifier}/1.mp3');
    }
    expect(recitersForRiwaya('warsh-an-nafi'), isEmpty);
    expect(reciterById('ar.husary')?.nameEn, 'Mahmoud Al-Husary');
  });

  test('audio cache paths are deterministic', () {
    final cache = AudioCache();
    expect(cache.dirFor('ar.alafasy', 112), 'audio/ar.alafasy/112');
    expect(cache.fileName(4), '4.mp3');
  });
}
