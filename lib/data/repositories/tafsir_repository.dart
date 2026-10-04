// Verified Tafsir loader (bundled per-surah JSON).
//
// Tafsir text is scholarly interpretation: always rendered with its
// source, never merged with Quran text. Unbundled ids yield null so
// the UI states unavailability honestly.

import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class TafsirInfo {
  const TafsirInfo({
    required this.id,
    required this.titleAr,
    required this.titleEn,
    required this.language,
    required this.source,
    required this.bundled,
  });

  final String id;
  final String titleAr;
  final String titleEn;
  final String language;
  final String source;
  final bool bundled;
}

const List<TafsirInfo> kTafsirCatalog = [
  TafsirInfo(
    id: 'jalalayn',
    titleAr: 'تفسير الجلالين',
    titleEn: 'Tafsir al-Jalalayn',
    language: 'ar',
    source: 'quran-api@1 ara-jalaladdinalmah (tanzil.net)',
    bundled: true,
  ),
  TafsirInfo(
    id: 'siraj',
    titleAr: 'السراج في تفسير القرآن',
    titleEn: 'Al-Siraj Tafsir',
    language: 'ar',
    source: 'quran-api@1 ara-sirajtafseer (quranenc.com)',
    bundled: true,
  ),
  TafsirInfo(
    id: 'ibn-kathir',
    titleAr: 'تفسير ابن كثير',
    titleEn: 'Tafsir Ibn Kathir',
    language: 'ar',
    source: 'Verified licensed dataset required',
    bundled: false,
  ),
  TafsirInfo(
    id: 'tabari',
    titleAr: 'تفسير الطبري',
    titleEn: 'Tafsir al-Tabari',
    language: 'ar',
    source: 'Verified licensed dataset required',
    bundled: false,
  ),
  TafsirInfo(
    id: 'saadi',
    titleAr: 'تفسير السعدي',
    titleEn: 'Tafsir al-Sa‘di',
    language: 'ar',
    source: 'Verified licensed dataset required',
    bundled: false,
  ),
  TafsirInfo(
    id: 'qurtubi',
    titleAr: 'تفسير القرطبي',
    titleEn: 'Tafsir al-Qurtubi',
    language: 'ar',
    source: 'Verified licensed dataset required',
    bundled: false,
  ),
];

/// ayah → tafsir text for one (tafsir, surah). Cached per surah file.
final tafsirSurahProvider =
    FutureProvider.family<Map<int, String>, (String, int)>(
        (ref, args) async {
  final (tafsirId, surah) = args;
  final info = kTafsirCatalog.where((t) => t.id == tafsirId).firstOrNull;
  if (info == null || !info.bundled) {
    return const {};
  }
  final raw = await rootBundle.loadString(
      'assets/quran/tafsir/$tafsirId/$surah.json',
      cache: false);
  final json = jsonDecode(raw) as Map<String, dynamic>;
  final out = <int, String>{};
  for (final e in (json['entries'] as List)) {
    final m = e as Map<String, dynamic>;
    final text = (m['text'] as String?) ?? '';
    if (text.isEmpty) {
      continue;
    }
    out[(m['ayah'] as num).toInt()] = text;
  }
  return out;
});
