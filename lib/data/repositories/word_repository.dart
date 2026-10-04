// Verified word morphology (Quranic Arabic Corpus, Hafs).
//
// Per-token lemma/root/POS aligned to our exact bundled verse text.
// Tokens without a corpus entry render with the word only —
// meanings are never guessed.

import 'dart:convert';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../content/content_packages.dart';

const String kWordSource =
    'Quranic Arabic Corpus v0.4 (corpus.quran.com)';

class WordInfo {
  const WordInfo({
    required this.word,
    required this.lemma,
    required this.root,
    required this.pos,
  });

  final String word;
  final String lemma;
  final String root;
  final String pos;

  bool get isMark => pos == 'MARK';
  bool get hasGloss => lemma.isNotEmpty || root.isNotEmpty;
}

/// ayah → words for one surah. Only Hafs/Uthmani word data ships.
final wordSurahProvider =
    FutureProvider.family<Map<int, List<WordInfo>>, int>(
        (ref, surah) async {
  ref.watch(contentRevisionProvider);
  try {
    if (!await ContentPackageStore.instance
        .isInstalled('quran:words-hafs')) {
      return const {};
    }
    final raw = await ContentPackageStore.instance.loadString(
      'assets/quran/words/hafs/$surah.json',
    );
    final json = jsonDecode(raw) as Map<String, dynamic>;
    final out = <int, List<WordInfo>>{};
    for (final e in ((json['entries'] as List?) ?? [])) {
      final m = e as Map<String, dynamic>;
      out[(m['ayah'] as num).toInt()] = [
        for (final w in ((m['words'] as List?) ?? []))
          WordInfo(
            word: (w as Map<String, dynamic>)['w'] as String,
            lemma: (w['lemma'] as String?) ?? '',
            root: (w['root'] as String?) ?? '',
            pos: (w['pos'] as String?) ?? '',
          ),
      ];
    }
    return out;
  } catch (_) {
    return const {};
  }
});
