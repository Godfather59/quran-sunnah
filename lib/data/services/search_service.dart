// Global search across bundled datasets (§21).
//
// Arabic matching is diacritic-insensitive via [normalizeArabic] —
// applied to the IN-MEMORY comparison only; displayed text is always
// the verbatim source string. Narrator/topic search honestly returns
// empty: no structured biographical/topical dataset is bundled yet.

import 'dart:convert';

import 'package:flutter/services.dart';
import '../../core/utils/text_utils.dart';
import '../models/hadith.dart';
import '../repositories/hadith_repository.dart';
import '../repositories/quran_repository.dart';
import '../repositories/tafsir_repository.dart';
import '../seed/surah_metadata.dart';

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
    AssetBundle? bundle,
  }) : _bundle = bundle ?? rootBundle;

  final QuranRepository quran;
  final HadithRepository hadith;
  final AssetBundle _bundle;
  final Map<String, Map<int, String>> _tafsirCache = {};

  Future<Map<int, String>> _tafsirSurah(
      String tafsirId, int surah) async {
    final key = '$tafsirId:$surah';
    final hit = _tafsirCache[key];
    if (hit != null) {
      return hit;
    }
    final info =
        kTafsirCatalog.where((t) => t.id == tafsirId).firstOrNull;
    if (info == null || !info.bundled) {
      return const {};
    }
    try {
      final raw = await _bundle.loadString(
          'assets/quran/tafsir/$tafsirId/$surah.json',
          cache: false);
      final json = jsonDecode(raw) as Map<String, dynamic>;
      final out = <int, String>{};
      for (final e in (json['entries'] as List)) {
        final m = e as Map<String, dynamic>;
        out[(m['ayah'] as num).toInt()] =
            (m['text'] as String?) ?? '';
      }
      _tafsirCache[key] = out;
      return out;
    } catch (_) {
      return const {};
    }
  }

  List<SearchHit> searchSurahs(String query) {
    final q = query.trim().toLowerCase();
    if (q.isEmpty) {
      return const [];
    }
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
    if (raw.isEmpty) {
      return const SearchResults();
    }
    final normQ = normalizeArabic(raw);
    var truncated = false;

    // Verse-reference shortcut: "2:255".
    SearchHit? refHit;
    final refMatch = RegExp(r'^(\d{1,3})\s*:\s*(\d{1,3})$').firstMatch(raw);
    if (refMatch != null) {
      final s = int.parse(refMatch.group(1)!);
      final a = int.parse(refMatch.group(2)!);
      final ayahs = await quran.ayahsOfSurah(s, editionId);
      final match = ayahs
          .where((x) => x.ayah == a && !x.isPlaceholder)
          .firstOrNull;
      if (match != null) {
        refHit = SearchHit(
          kind: SearchKind.quran,
          title: 'Surah $s · Ayah $a',
          subtitle: 'Direct verse reference',
          snippet: match.text,
          refKey: match.key,
          surah: s,
          ayah: a,
        );
      }
    }

    // Quran full-text.
    final quranHits = <SearchHit>[];
    if (refHit != null) {
      quranHits.add(refHit);
    }
    final all = await quran.allAyahs(editionId);
    for (final a in all) {
      if (a.isPlaceholder) {
        continue;
      }
      if (surahScope != null && a.surah != surahScope) {
        continue;
      }
      if (!normalizeArabic(a.text).contains(normQ)) {
        continue;
      }
      if (refHit != null && a.key == refHit.refKey) {
        continue;
      }
      quranHits.add(SearchHit(
        kind: SearchKind.quran,
        title: 'Surah ${a.surah} · Ayah ${a.ayah}',
        subtitle: 'Quran · $editionId',
        snippet: a.text,
        refKey: a.key,
        surah: a.surah,
        ayah: a.ayah,
      ));
      if (quranHits.length >= limitPerCategory) {
        truncated = true;
        break;
      }
    }

    // Hadith (bundled collections, diacritic-insensitive + number).
    final hadithHits = <SearchHit>[];
    final hadiths = await hadith.query(
        hadithFilter.copyWith(query: raw),
        limit: limitPerCategory);
    for (final h in hadiths) {
      hadithHits.add(SearchHit(
        kind: SearchKind.hadith,
        title:
            '${_collectionShort(h.collectionId)} · Hadith ${h.hadithNumber}',
        subtitle: h.book,
        snippet: h.matnAr.length > 220
            ? '${h.matnAr.substring(0, 220)}…'
            : h.matnAr,
        refKey: h.id,
        hadith: h,
      ));
    }

    // Tafsir (preferred tafsir only).
    final tafsirHits = <SearchHit>[];
    final tafsirInfo =
        kTafsirCatalog.where((t) => t.id == tafsirId).firstOrNull;
    if (tafsirInfo != null && tafsirInfo.bundled) {
      outer:
      for (var s = 1; s <= 114; s++) {
        if (surahScope != null && s != surahScope) {
          continue;
        }
        final map = await _tafsirSurah(tafsirId, s);
        for (final e in map.entries) {
          if (!normalizeArabic(e.value).contains(normQ)) {
            continue;
          }
          tafsirHits.add(SearchHit(
            kind: SearchKind.tafsir,
            title: '${tafsirInfo.titleEn} · $s:${e.key}',
            subtitle: tafsirInfo.source,
            snippet: e.value.length > 220
                ? '${e.value.substring(0, 220)}…'
                : e.value,
            refKey: '$tafsirId:$s:${e.key}',
            surah: s,
            ayah: e.key,
          ));
          if (tafsirHits.length >= limitPerCategory) {
            truncated = true;
            break outer;
          }
        }
      }
    }

    return SearchResults(
      quran: quranHits,
      hadith: hadithHits,
      tafsir: tafsirHits,
      surahs: searchSurahs(raw),
      truncated: truncated,
    );
  }

  String _collectionShort(String id) => switch (id) {
        'bukhari' => 'Bukhari',
        'muslim' => 'Muslim',
        'abudawud' => 'Abu Dawud',
        'tirmidhi' => 'Tirmidhi',
        'nasai' => 'Nasa’i',
        'ibnmajah' => 'Ibn Majah',
        'malik' => 'Malik',
        _ => id,
      };
}
