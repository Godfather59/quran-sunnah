import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_theme.dart';
import '../../../data/models/quran.dart';
import '../../../data/repositories/tajweed_repository.dart';
import '../../../state/providers.dart';

/// Verse text with Tajweed coloring when the user selected the
/// Tajweed script AND verified metadata covers this edition.
/// Otherwise falls back to plain verse text (never blank).
class TajweedText extends ConsumerWidget {
  const TajweedText({
    super.key,
    required this.ayah,
    required this.fontSize,
    this.textAlign = TextAlign.right,
  });

  final Ayah ayah;
  final double fontSize;
  final TextAlign textAlign;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final q = ref.watch(quranPrefsProvider);
    final indopak = ayah.editionId.endsWith('__indopak');
    TextStyle style() => AppTheme.quranArabic(context,
        size: fontSize,
        height: q.lineHeight,
        font: q.font,
        indopak: indopak);
    final tajweedActive =
        q.script == QuranScript.tajweed && ayah.editionId == 'hafs-an-asim__uthmani';
    if (!tajweedActive) {
      return Text(
        ayah.text,
        textDirection: TextDirection.rtl,
        textAlign: textAlign,
        style: style(),
      );
    }
    final spansAsync = ref.watch(tajweedSurahProvider(ayah.surah));
    return spansAsync.when(
      loading: () => Text(
        ayah.text,
        textDirection: TextDirection.rtl,
        textAlign: textAlign,
        style: style(),
      ),
      error: (_, __) => Text(
        ayah.text,
        textDirection: TextDirection.rtl,
        textAlign: textAlign,
        style: style(),
      ),
      data: (map) {
        final spans = map[ayah.ayah] ?? const <TajweedSpan>[];
        if (spans.isEmpty) {
          return Text(
            ayah.text,
            textDirection: TextDirection.rtl,
            textAlign: textAlign,
            style: style(),
          );
        }
        final base = style();
        final segments =
            buildTajweedSegments(ayah.text, spans);
        return RichText(
          textDirection: TextDirection.rtl,
          textAlign: textAlign,
          text: TextSpan(
            style: base,
            children: segments
                .map((seg) => TextSpan(
                      text: ayah.text
                          .substring(seg.start, seg.end),
                      style: seg.rule == null
                          ? null
                          : TextStyle(
                              color: Color(ruleInfo(
                                      seg.rule!)
                                  .colorValue),
                            ),
                    ))
                .toList(),
          ),
        );
      },
    );
  }
}

/// Color legend for Tajweed rules (verbs as data, not rulings).
class TajweedLegendSheet extends StatelessWidget {
  const TajweedLegendSheet({super.key});

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Tajweed · التجويد',
                style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 4),
            Text(
              'Hafs/Uthmani annotations (decision-tree verified). Colors guide study — learn rulings from a teacher.',
              style: Theme.of(context).textTheme.bodySmall,
            ),
            const SizedBox(height: 12),
            Flexible(
              child: ListView(
                shrinkWrap: true,
                children: kTajweedRules
                    .map((r) => ListTile(
                          dense: true,
                          leading: Container(
                              width: 20,
                              height: 20,
                              decoration: BoxDecoration(
                                color:
                                    Color(r.colorValue),
                                shape: BoxShape.circle,
                              )),
                          title: Text(
                              '${r.nameAr} · ${r.nameEn}'),
                        ))
                    .toList(),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
