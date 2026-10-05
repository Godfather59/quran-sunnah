import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_theme.dart';
import '../../../data/models/quran.dart';
import '../../../data/repositories/word_repository.dart';
import '../../../state/download_state.dart';
import '../../../state/providers.dart';
import 'tajweed_text.dart';

/// Tappable word-by-word ayah text.
/// Each token looks up verified morphology (QAC v0.4); tap shows lemma/root/POS.
/// Falls back to [TajweedText] when morphology is not installed or not Hafs.
class WordTapAyahText extends ConsumerWidget {
  const WordTapAyahText({
    super.key,
    required this.ayah,
    required this.fontSize,
  });

  final Ayah ayah;
  final double fontSize;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final q = ref.watch(quranPrefsProvider);
    final isHafs = ayah.editionId.startsWith('hafs-an-asim__');
    final installed = ref
        .watch(downloadProvider)
        .installed
        .contains('quran:words-hafs');
    if (!isHafs || !installed) {
      return TajweedText(ayah: ayah, fontSize: fontSize);
    }
    final wordsAsync = ref.watch(wordSurahProvider(ayah.surah));
    return wordsAsync.when(
      loading: () => TajweedText(ayah: ayah, fontSize: fontSize),
      error: (_, __) => TajweedText(ayah: ayah, fontSize: fontSize),
      data: (map) {
        final infos = map[ayah.canonicalAyahNumber];
        if (infos == null || infos.isEmpty) {
          return TajweedText(ayah: ayah, fontSize: fontSize);
        }
        final byWord = <String, WordInfo>{};
        for (final w in infos) {
          byWord.putIfAbsent(w.word, () => w);
        }
        final tokens = ayah.text.split(' ');
        final style = AppTheme.quranArabic(
          context,
          size: fontSize,
          height: q.lineHeight,
          font: q.font,
        );
        return RichText(
          textDirection: TextDirection.rtl,
          textAlign: TextAlign.right,
          text: TextSpan(
            style: style,
            children: [
              for (var i = 0; i < tokens.length; i++) ...[
                TextSpan(
                  text: tokens[i],
                  style: byWord.containsKey(tokens[i])
                      ? style.copyWith(
                          decoration: TextDecoration.underline,
                          decorationStyle: TextDecorationStyle.dotted,
                        )
                      : null,
                  recognizer: TapGestureRecognizer()
                    ..onTap = () => _showWord(
                        context, tokens[i], byWord[tokens[i]]),
                ),
                if (i < tokens.length - 1)
                  const TextSpan(text: ' '),
              ],
            ],
          ),
        );
      },
    );
  }

  void _showWord(BuildContext context, String token, WordInfo? info) {
    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      builder: (_) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(token,
                  textDirection: TextDirection.rtl,
                  textAlign: TextAlign.right,
                  style: const TextStyle(fontSize: 26, height: 1.9)),
              const SizedBox(height: 8),
              if (info == null || !info.hasGloss)
                Text(
                  'No verified morphology for this token — never guessed.',
                  style: Theme.of(context).textTheme.bodySmall,
                )
              else
                Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text('Lemma: ${info.lemma}',
                        textDirection: TextDirection.rtl),
                    if (info.root.isNotEmpty)
                      Text('Root: √${info.root}',
                          textDirection: TextDirection.rtl),
                    if (info.pos.isNotEmpty) Text('POS: ${info.pos}'),
                  ],
                ),
              const SizedBox(height: 8),
              Text(kWordSource,
                  style: Theme.of(context).textTheme.labelSmall),
            ],
          ),
        ),
      ),
    );
  }
}
