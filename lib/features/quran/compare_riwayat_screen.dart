import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../data/models/quran.dart';
import '../../data/repositories/quran_repository.dart';
import '../../data/repositories/verified_asset_quran_repository.dart';
import '../../data/seed/riwayat_catalog.dart';

/// Compare Riwayat (§11): same ayah from each edition's own verified
/// file. Only documented differences shown — never AI-generated.
class CompareRiwayatScreen extends ConsumerStatefulWidget {
  const CompareRiwayatScreen(
      {super.key, required this.surah, required this.ayah});

  final int surah;
  final int ayah;

  @override
  ConsumerState<CompareRiwayatScreen> createState() =>
      _CompareRiwayatScreenState();
}

class _CompareRiwayatScreenState
    extends ConsumerState<CompareRiwayatScreen> {
  final _selected = {
    'hafs-an-asim__uthmani',
    'warsh-an-nafi__uthmani',
    'qalun-an-nafi__uthmani',
  };

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(s.t('compareRiwayat'))),
      body: FutureBuilder(
        future: ref
            .read(quranRepositoryProvider)
            .compareAyah(
                widget.surah, widget.ayah, _selected.toList()),
        builder: (context, snap) {
          final rows = snap.data ?? [];
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              Text(
                  'Ayah ${widget.surah}:${widget.ayah} — differences below come only from verified Qira’at datasets.',
                  style: Theme.of(context).textTheme.bodySmall),
              const SizedBox(height: 12),
              Wrap(
                spacing: 8,
                children: kRiwayaCatalog.where((r) =>
                    kVerifiedQuranAssets.containsKey(
                        '${r.id.storageKey}__uthmani')).map((r) {
                  final id = '${r.id.storageKey}__uthmani';
                  return FilterChip(
                    label: Text(r.riwayaEn),
                    selected: _selected.contains(id),
                    onSelected: (v) => setState(() {
                      v ? _selected.add(id) : _selected.remove(id);
                    }),
                  );
                }).toList(),
              ),
              const SizedBox(height: 12),
              if (rows.isEmpty ||
                  rows.every((a) => a.isPlaceholder))
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(18),
                    child: Column(
                      crossAxisAlignment:
                          CrossAxisAlignment.stretch,
                      children: [
                        Text(s.t('contentUnavailable')),
                        const SizedBox(height: 8),
                        const Text(
                          'Example layout once datasets ship:\n\n'
                          'Ḥafṣ ʿan ʿĀṣim\n[verified text]\n\n'
                          'Warsh ʿan Nāfiʿ\n[verified text]\n\n'
                          'Qālūn ʿan Nāfiʿ\n[verified text]',
                        ),
                      ],
                    ),
                  ),
                )
              else
                ...rows.map((a) => Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment:
                              CrossAxisAlignment.stretch,
                          children: [
                            Text('${a.editionId} · ${a.surah}:${a.displayAyahNumber}',
                                style: Theme.of(context)
                                    .textTheme
                                    .labelSmall),
                            Text(a.text,
                                textDirection:
                                    TextDirection.rtl,
                                style: const TextStyle(
                                    fontSize: 22, height: 2)),
                          ],
                        ),
                      ),
                    )),
            ],
          );
        },
      ),
    );
  }
}
