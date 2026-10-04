// Verified Quran translation loader (bundled `surah|ayah|text`).
//
// Translations are human interpretation: always rendered with the
// translator's name in a muted style (see AppTheme.translation) and
// NEVER styled like Quran text.

import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/quran.dart';

const Map<String, String> kTranslationAssets = {
  'en-sahih': 'assets/quran/translations/en-sahih.txt',
  'fr-hamidullah': 'assets/quran/translations/fr-hamidullah.txt',
};

const List<QuranTranslation> kTranslationCatalog = [
  QuranTranslation(
    id: 'en-sahih',
    language: 'en',
    translator: 'Saheeh International',
    source: 'Tanzil — https://tanzil.net/trans/en.sahih',
  ),
  QuranTranslation(
    id: 'fr-hamidullah',
    language: 'fr',
    translator: 'Muhammad Hamidullah',
    source: 'Tanzil — https://tanzil.net/trans/fr.hamidullah',
  ),
];

/// refKey "surah:ayah" → translation text. Cached per translation id.
final translationTextsProvider =
    FutureProvider.family<Map<String, String>, String>(
        (ref, id) async {
  final path = kTranslationAssets[id];
  if (path == null) {
    return const {};
  }
  final raw =
      await rootBundle.loadString(path, cache: false);
  final out = <String, String>{};
  for (final line in raw.split('\n')) {
    final first = line.indexOf('|');
    if (first < 0) {
      continue;
    }
    final second = line.indexOf('|', first + 1);
    if (second < 0) {
      continue;
    }
    final surah = int.tryParse(line.substring(0, first));
    final ayah =
        int.tryParse(line.substring(first + 1, second));
    if (surah == null || ayah == null) {
      continue;
    }
    var text = line.substring(second + 1);
    if (text.endsWith('\r')) {
      text = text.substring(0, text.length - 1);
    }
    if (text.isEmpty) {
      continue;
    }
    out['$surah:$ayah'] = text;
  }
  return out;
});
