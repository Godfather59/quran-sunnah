import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/l10n/app_strings.dart';
import '../../data/models/quran.dart';
import '../../data/content/content_packages.dart';
import '../../data/repositories/verified_asset_quran_repository.dart';
import '../../data/seed/riwayat_catalog.dart';
import '../../state/download_state.dart';
import '../../state/providers.dart';

class RiwayaSelectorScreen extends ConsumerWidget {
  const RiwayaSelectorScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final q = ref.watch(quranPrefsProvider);
    final dl = ref.watch(downloadProvider);

    return Scaffold(
      appBar: AppBar(title: Text(s.t('riwaya'))),
      body: ListView(
        padding: const EdgeInsets.all(12),
        children: [
          Padding(
            padding: const EdgeInsets.all(8),
            child: Text(
              s.t('riwayaExplanation'),
              style: const TextStyle(fontSize: 13),
            ),
          ),
          ...kRiwayaCatalog.map((r) {
            final preferredEdition =
                '${r.id.storageKey}__${q.datasetScript.name}';
            final fallbackEdition = '${r.id.storageKey}__uthmani';
            final available =
                kVerifiedQuranAssets.containsKey(preferredEdition) ||
                    kVerifiedQuranAssets.containsKey(fallbackEdition);
            final editionId =
                kVerifiedQuranAssets.containsKey(preferredEdition)
                    ? preferredEdition
                    : fallbackEdition;
            final selected = q.riwaya == r.id;
            final installed =
                available && dl.installed.contains('quran:$editionId');

            final subtitle = s.isArabic
                ? '${s.t('qiraaLabel')}: ${r.qiraaAr} · '
                    '${installed ? s.t('downloaded') : s.t('notDownloaded')}'
                : '${_riwayaName(s, r)}\n'
                    '${s.t('qiraaLabel')} ${_qiraaName(s, r)} · '
                    '${s.t('versionLabel')} ${r.datasetVersion} · '
                    '${installed ? s.t('downloaded') : s.t('notDownloaded')}';

            return Card(
              child: ListTile(
                selected: selected,
                leading: selected
                    ? const Icon(Icons.check_circle)
                    : const Icon(Icons.circle_outlined),
                title: Text(
                  r.riwayaAr,
                  textDirection: TextDirection.rtl,
                ),
                subtitle: Text(
                  available ? subtitle : s.t('datasetUnavailable'),
                ),
                isThreeLine: !s.isArabic,
                enabled: available,
                trailing: available
                    ? (installed
                        ? const Icon(Icons.download_done)
                        : const Icon(Icons.download))
                    : const Icon(Icons.lock_outline),
                onTap: !available
                    ? null
                    : () async {
                        final nextScript =
                            kVerifiedQuranAssets.containsKey(preferredEdition)
                                ? q.datasetScript
                                : QuranScript.uthmani;
                        await ref
                            .read(quranPrefsProvider.notifier)
                            .update(
                              q.copyWith(
                                riwaya: r.id,
                                script: nextScript,
                                showTajweed:
                                    r.id == RiwayaId.hafsAsim &&
                                        nextScript == QuranScript.uthmani &&
                                        q.showTajweed,
                              ),
                            );
                        if (context.mounted) Navigator.pop(context);
                      },
              ),
            );
          }),
        ],
      ),
    );
  }

  String _riwayaName(AppStrings s, RiwayaInfo info) =>
      s.locale.languageCode == 'fr' ? info.riwayaFr : info.riwayaEn;

  String _qiraaName(AppStrings s, RiwayaInfo info) =>
      s.locale.languageCode == 'fr' ? info.qiraaFr : info.qiraaEn;
}

class ScriptSelectorScreen extends ConsumerWidget {
  const ScriptSelectorScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final q = ref.watch(quranPrefsProvider);
    final dl = ref.watch(downloadProvider);
    final tajweedInstalled = dl.installed.contains('quran:tajweed-hafs');
    final items = [
      (QuranScript.uthmani, s.t('uthmani'), s.t('uthmaniHint')),
      (QuranScript.imlai, s.t('imlai'), s.t('imlaiHint')),
      (QuranScript.indopak, s.t('indopak'), s.t('indopakHint')),
    ];

    return Scaffold(
      appBar: AppBar(title: Text(s.t('scriptStyle'))),
      body: ListView(
        padding: const EdgeInsets.all(12),
        children: [
          Padding(
            padding: const EdgeInsets.all(8),
            child: Text(s.t('scriptExplanation')),
          ),
          ...items.map((e) {
            final editionId = '${q.riwaya.storageKey}__${e.$1.name}';
            final available = kVerifiedQuranAssets.containsKey(editionId);
            return Card(
              child: RadioGroup<QuranScript>(
                groupValue: q.datasetScript,
                onChanged: (v) {
                  if (!available || v == null) return;
                  ref.read(quranPrefsProvider.notifier).update(
                        q.copyWith(
                          script: v,
                          showTajweed:
                              v == QuranScript.uthmani && q.showTajweed,
                        ),
                      );
                },
                child: RadioListTile<QuranScript>(
                  value: e.$1,
                  enabled: available,
                  title: Text(e.$2),
                  subtitle: Text(
                    available
                        ? e.$3
                        : '${e.$3} · ${s.t('datasetUnavailableForRiwaya')}',
                  ),
                ),
              ),
            );
          }),
          Card(
            child: SwitchListTile(
              secondary: const Icon(Icons.palette_outlined),
              title: Text(s.t('tajweedColors')),
              subtitle: Text(
                !q.tajweedAvailable
                    ? s.t('tajweedHafsOnly')
                    : tajweedInstalled
                        ? s.t('tajweedVerifiedHint')
                        : s.t('downloadBeforeUse'),
              ),
              value: q.showTajweed &&
                  q.tajweedAvailable &&
                  tajweedInstalled,
              onChanged: q.tajweedAvailable
                  ? (v) async {
                      if (v && !tajweedInstalled) {
                        try {
                          await ref
                              .read(downloadProvider.notifier)
                              .install('quran:tajweed-hafs');
                          ref
                              .read(contentRevisionProvider.notifier)
                              .bump();
                        } catch (_) {
                          return;
                        }
                      }
                      await ref
                          .read(quranPrefsProvider.notifier)
                          .update(q.copyWith(showTajweed: v));
                    }
                  : null,
            ),
          ),
        ],
      ),
    );
  }
}

class FontSettingsSheet extends ConsumerWidget {
  const FontSettingsSheet({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final q = ref.watch(quranPrefsProvider);

    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              s.t('quranTypography'),
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w700,
              ),
            ),
            Wrap(
              spacing: 8,
              children: QuranFont.values
                  .map(
                    (font) => ChoiceChip(
                      label: Text(_fontLabel(s, font)),
                      selected: q.font == font,
                      onSelected: (_) => ref
                          .read(quranPrefsProvider.notifier)
                          .update(q.copyWith(font: font)),
                    ),
                  )
                  .toList(),
            ),
            _slider(
              s.t('fontSize'),
              q.fontSize,
              16,
              40,
              (v) => ref
                  .read(quranPrefsProvider.notifier)
                  .update(q.copyWith(fontSize: v)),
            ),
            _slider(
              s.t('lineHeight'),
              q.lineHeight,
              1.4,
              2.6,
              (v) => ref
                  .read(quranPrefsProvider.notifier)
                  .update(q.copyWith(lineHeight: v)),
            ),
            _slider(
              s.t('ayahSpacing'),
              q.ayahSpacing,
              4,
              32,
              (v) => ref
                  .read(quranPrefsProvider.notifier)
                  .update(q.copyWith(ayahSpacing: v)),
            ),
            const SizedBox(height: 12),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                border: Border.all(
                    color:
                        Theme.of(context).colorScheme.outlineVariant),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                'بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ ﴿١﴾',
                textDirection: TextDirection.rtl,
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontFamily: switch (q.font) {
                    QuranFont.uthmani => 'Amiri Quran',
                    QuranFont.indopak => 'Amiri Quran',
                    _ => 'Noto Naskh Arabic',
                  },
                  fontSize: q.fontSize.clamp(16, 40),
                  height: q.lineHeight.clamp(1.4, 2.6),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _fontLabel(AppStrings s, QuranFont font) => switch (font) {
        QuranFont.uthmani => s.t('uthmani'),
        QuranFont.naskh => s.isArabic ? 'نسخ' : 'Naskh',
        QuranFont.notoNaskh => s.isArabic ? 'نوتو نسخ عربي' : 'Noto Naskh Arabic',
        QuranFont.indopak => s.t('indopak'),
      };

  Widget _slider(
    String label,
    double value,
    double min,
    double max,
    ValueChanged<double> onChanged,
  ) {
    return Row(
      children: [
        SizedBox(width: 110, child: Text(label)),
        Expanded(
          child: Slider(
            value: value,
            min: min,
            max: max,
            onChanged: onChanged,
          ),
        ),
        SizedBox(
          width: 44,
          child: Text(
            value.toStringAsFixed(1),
            textAlign: TextAlign.end,
          ),
        ),
      ],
    );
  }
}
