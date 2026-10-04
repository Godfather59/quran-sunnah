import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/models/quran.dart';
import '../../data/seed/riwayat_catalog.dart';
import '../../data/repositories/verified_asset_quran_repository.dart';
import '../../state/download_state.dart';
import '../../state/providers.dart';

/// Dedicated Riwaya selector (§6). Terminology: Qira'a / Riwaya.
/// Each row shows source + version + offline state.
class RiwayaSelectorScreen extends ConsumerWidget {
  const RiwayaSelectorScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final q = ref.watch(quranPrefsProvider);
    final dl = ref.watch(downloadProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Riwaya · الرواية')),
      body: ListView(
        padding: const EdgeInsets.all(12),
        children: [
          const Padding(
            padding: EdgeInsets.all(8),
            child: Text(
              'Qirā’a = قراءة · Riwaya = رواية. Each Riwaya uses its own verified dataset — never a text substitution.',
              style: TextStyle(fontSize: 13),
            ),
          ),
          ...kRiwayaCatalog.map((r) {
            final preferredEdition =
                '${r.id.storageKey}__${q.datasetScript.name}';
            final fallbackEdition = '${r.id.storageKey}__uthmani';
            final available = kVerifiedQuranAssets.containsKey(preferredEdition) ||
                kVerifiedQuranAssets.containsKey(fallbackEdition);
            final editionId = kVerifiedQuranAssets.containsKey(preferredEdition)
                ? preferredEdition
                : fallbackEdition;
            final selected = q.riwaya == r.id;
            final installed =
                available && dl.installed.contains('quran:$editionId');
            return Card(
              child: ListTile(
                selected: selected,
                leading: selected
                    ? const Icon(Icons.check_circle)
                    : const Icon(Icons.circle_outlined),
                title: Text(r.riwayaAr,
                    textDirection: TextDirection.rtl),
                subtitle: Text(
                    '${r.riwayaEn}\nQirā’at ${r.qiraaEn} · v${r.datasetVersion} · ${installed ? 'Downloaded' : 'Not downloaded'}'),
                isThreeLine: true,
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
                            .update(q.copyWith(
                              riwaya: r.id,
                              script: nextScript,
                              showTajweed: r.id == RiwayaId.hafsAsim &&
                                  nextScript == QuranScript.uthmani &&
                                  q.showTajweed,
                            ));
                        if (context.mounted) Navigator.pop(context);
                      },
              ),
            );
          }),
        ],
      ),
    );
  }
}

/// Quran Text Style selector (§7) — independent from Riwaya.
class ScriptSelectorScreen extends ConsumerWidget {
  const ScriptSelectorScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final q = ref.watch(quranPrefsProvider);
    const items = [
      (QuranScript.uthmani, 'Uthmani — عثماني',
          'Traditional orthography with Quranic marks.'),
      (QuranScript.imlai, 'Simple / Imla’i — إملائي',
          'Simplified modern reading & search-friendly.'),
      (QuranScript.indopak, 'IndoPak',
          'Bundled for Hafs with Extended Arabic marks.'),
    ];
    return Scaffold(
      appBar: AppBar(
          title: const Text('Quran Text Style · رسم المصحف')),
      body: ListView(
        padding: const EdgeInsets.all(12),
        children: [
          const Padding(
            padding: EdgeInsets.all(8),
            child: Text(
                'Style changes presentation only — never the wording or meaning.'),
          ),
          ...items.map((e) {
            final editionId = '${q.riwaya.storageKey}__${e.$1.name}';
            final available = kVerifiedQuranAssets.containsKey(editionId);
            return Card(
              child: RadioGroup<QuranScript>(
                groupValue: q.datasetScript,
                onChanged: (v) {
                  if (!available || v == null) return;
                  ref
                      .read(quranPrefsProvider.notifier)
                      .update(q.copyWith(
                        script: v,
                        showTajweed:
                            v == QuranScript.uthmani && q.showTajweed,
                      ));
                },
                child: RadioListTile<QuranScript>(
                  value: e.$1,
                  enabled: available,
                  title: Text(e.$2),
                  subtitle: Text(available
                      ? e.$3
                      : '${e.$3} · Dataset unavailable for this Riwaya'),
                ),
              ),
            );
          }),
          Card(
            child: SwitchListTile(
              secondary: const Icon(Icons.palette_outlined),
              title: const Text('Tajweed colors · ألوان التجويد'),
              subtitle: Text(q.tajweedAvailable
                  ? 'Verified Hafs/Uthmani annotations; Quran text stays unchanged.'
                  : 'Available only with Hafs + Uthmani.'),
              value: q.showTajweed && q.tajweedAvailable,
              onChanged: q.tajweedAvailable
                  ? (v) => ref
                      .read(quranPrefsProvider.notifier)
                      .update(q.copyWith(showTajweed: v))
                  : null,
            ),
          ),
        ],
      ),
    );
  }
}

/// Font / typography controls (§8). Licensed fonts only.
class FontSettingsSheet extends ConsumerWidget {
  const FontSettingsSheet({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final q = ref.watch(quranPrefsProvider);
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Quran typography',
                style:
                    TextStyle(fontSize: 18, fontWeight: FontWeight.w700)),
            Wrap(
              spacing: 8,
              children: QuranFont.values
                  .map((f) => ChoiceChip(
                        label: Text(f.name),
                        selected: q.font == f,
                        onSelected: (_) => ref
                            .read(quranPrefsProvider.notifier)
                            .update(q.copyWith(font: f)),
                      ))
                  .toList(),
            ),
            _slider('Font size', q.fontSize, 16, 40, (v) => ref
                .read(quranPrefsProvider.notifier)
                .update(q.copyWith(fontSize: v))),
            _slider('Line height', q.lineHeight, 1.4, 2.6, (v) => ref
                .read(quranPrefsProvider.notifier)
                .update(q.copyWith(lineHeight: v))),
            _slider('Ayah spacing', q.ayahSpacing, 4, 32, (v) => ref
                .read(quranPrefsProvider.notifier)
                .update(q.copyWith(ayahSpacing: v))),
          ],
        ),
      ),
    );
  }

  Widget _slider(String label, double value, double min, double max,
      ValueChanged<double> onChanged) {
    return Row(
      children: [
        SizedBox(width: 110, child: Text(label)),
        Expanded(
            child: Slider(
                value: value, min: min, max: max, onChanged: onChanged)),
        SizedBox(
            width: 44,
            child: Text(value.toStringAsFixed(1),
                textAlign: TextAlign.end)),
      ],
    );
  }
}
