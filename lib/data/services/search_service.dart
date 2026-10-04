// Global search across verified bundled datasets.
//
// Search text is indexed in SQLite FTS5 using a normalized copy only.
// Displayed Quran, Hadith and Tafsir strings always come from the exact
// source text stored in the verified assets.

import '../database/app_database.dart';
import '../models/hadith.dart';
import '../repositories/hadith_repository.dart';
import '../repositories/quran_repository.dart';
import '../seed/surah_metadata.dart';
import 'search_index_service.dart';

enum SearchKind { quran, hadith, tafsir, surah }

class SearchHit {
  const SearchHit({
    required this.kind,
    required this.title,
    required this.subtitle,
    required this.snippet,
    required this.refKey,
    this.surah,
    this.ayah,
    this.hadith,
  });

  final SearchKind kind;
  final String title;
  final String subtitle;
  final String snippet;
  final String refKey;
  final int? surah;
  final int? ayah;
  final Hadith? hadith;
}

class SearchResults {
  const SearchResults({
    this.quran = const [],
    this.hadith = const [],
    this.tafsir = const [],
    this.surahs = const [],
    this.truncated = false,
  });

  final List<SearchHit> quran;
  final List<SearchHit> hadith;
  final List<SearchHit> tafsir;
  final List<SearchHit> surahs;
  final bool truncated;

  int get total =>
      quran.length + hadith.length + tafsir.length + surahs.length;
}

class SearchService {
  SearchService({
    required this.quran,
    required this.hadith,
    required this.database,
  }) : _index = SearchIndexService(
          database: database,
          quran: quran,
          hadith: hadith,
        );

  final QuranRepository quran;
  final HadithRepository hadith;
  final AppDatabase database;
  final SearchIndexService _index;

  List<SearchHit> searchSurahs(String query) {
    final q = query.trim().toLowerCase();
    if (q.isEmpty) return const [];
    return kSurahMetadata
        .where((m) =>
            m.nameAr.contains(query.trim()) ||
            m.nameEn.toLowerCase().contains(q) ||
            m.nameFr.toLowerCase().contains(q) ||
            m.number.toString() == q)
        .map((m) => SearchHit(
              kind: SearchKind.surah,
              title: '${m.number}. ${m.nameAr} — ${m.nameEn}',
              subtitle:
                  '${m.makki ? 'Makki' : 'Madani'} · ${m.ayahCount} ayat',
              snippet: m.nameFr,
              refKey: 'surah:${m.number}',
              surah: m.number,
            ))
        .toList();
  }

  Future<SearchResults> search({
    required String query,
    required String editionId,
    required String tafsirId,
    HadithFilter hadithFilter = const HadithFilter(),
    int? surahScope,
    int limitPerCategory = 50,
  }) async {
    final raw = query.trim();
    if (raw.isEmpty) return const SearchResults();

    await _index.ensureIndexed(
      editionId: editionId,
      tafsirId: tafsirId,
    );

    final fts = SearchIndexService.ftsQuery(raw);
    var truncated = false;

    SearchHit? directVerse;
    final refMatch =
        RegExp(r'^(\d{1,3})\s*:\s*(\d{1,3})$').firstMatch(raw);
    if (refMatch != null) {
      final surah = int.parse(refMatch.group(1)!);
      final displayAyah = int.parse(refMatch.group(2)!);
      if (surah >= 1 && surah <= 114) {
        final ayahs = await quran.ayahsOfSurah(surah, editionId);
        final match = ayahs
            .where((a) =>
                a.displayAyahNumber == displayAyah && !a.isPlaceholder)
            .firstOrNull;
        if (match != null) {
          directVerse = SearchHit(
            kind: SearchKind.quran,
            title:
                'Surah $surah · Ayah ${match.displayAyahNumber}',
            subtitle: 'Direct verse reference',
            snippet: match.text,
            refKey: match.canonicalVerseId,
            surah: match.surah,
            ayah: match.displayAyahNumber,
          );
        }
      }
    }

    final quranHits = <SearchHit>[];
    if (directVerse != null) quranHits.add(directVerse);

    if (fts.isNotEmpty) {
      final rows = await database.searchIndex(
        query: fts,
        kind: 'quran',
        editionId: editionId,
        surah: surahScope,
        limit: limitPerCategory + 1,
      );
      if (rows.length > limitPerCategory) truncated = true;
      for (final row in rows.take(limitPerCategory)) {
        if (directVerse != null && row.refKey == directVerse.refKey) {
          continue;
        }
        quranHits.add(SearchHit(
          kind: SearchKind.quran,
          title: row.title,
          subtitle: row.subtitle,
          snippet: row.body,
          refKey: row.refKey,
          surah: row.surah,
          ayah: row.ayah,
        ));
      }
    }

    final hadithHits = <SearchHit>[];
    if (fts.isNotEmpty) {
      final rows = await database.searchIndex(
        query: fts,
        kind: 'hadith',
        collectionIds: hadithFilter.collectionIds,
        book: hadithFilter.book,
        hadithNumber: hadithFilter.number,
        limit: limitPerCategory + 1,
      );
      if (rows.length > limitPerCategory) truncated = true;
      for (final row in rows.take(limitPerCategory)) {
        final collection = row.collectionId ?? '';
        final number = row.hadithNumber ?? '';
        final h = Hadith(
          id: row.refKey,
          collectionId: collection,
          book: row.book ?? row.subtitle,
          bookAr: '',
          chapter: row.book ?? row.subtitle,
          chapterAr: '',
          hadithNumber: number,
          matnAr: row.body,
          narrator: null,
          sanadAr: null,
          grade: null,
          gradingAuthority: null,
        );
        hadithHits.add(SearchHit(
          kind: SearchKind.hadith,
          title: row.title,
          subtitle: row.subtitle,
          snippet: _snippet(row.body),
          refKey: row.refKey,
          hadith: h,
        ));
      }
    }

    final tafsirHits = <SearchHit>[];
    if (fts.isNotEmpty) {
      final rows = await database.searchIndex(
        query: fts,
        kind: 'tafsir',
        tafsirId: tafsirId,
        surah: surahScope,
        limit: limitPerCategory + 1,
      );
      if (rows.length > limitPerCategory) truncated = true;
      for (final row in rows.take(limitPerCategory)) {
        tafsirHits.add(SearchHit(
          kind: SearchKind.tafsir,
          title: row.title,
          subtitle: row.subtitle,
          snippet: _snippet(row.body),
          refKey: row.refKey,
          surah: row.surah,
          ayah: row.ayah,
        ));
      }
    }

    return SearchResults(
      quran: quranHits.take(limitPerCategory).toList(growable: false),
      hadith: hadithHits,
      tafsir: tafsirHits,
      surahs: searchSurahs(raw),
      truncated: truncated,
    );
  }

  static String _snippet(String value) =>
      value.length > 220 ? '${value.substring(0, 220)}…' : value;
}
