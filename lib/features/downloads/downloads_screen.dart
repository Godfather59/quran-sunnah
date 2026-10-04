import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../data/models/quran.dart';
import '../../data/seed/hadith_collections.dart';
import '../../data/seed/riwayat_catalog.dart';
import '../../state/download_state.dart';
import '../../state/providers.dart';

/// Download Manager (§24): per-dataset size, installed/update,
/// storage used, remove. Nothing forced.
class DownloadsScreen extends ConsumerWidget {
  const DownloadsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final dl = ref.watch(downloadProvider);
    final q = ref.watch(quranPrefsProvider);

    String quranId(RiwayaId r) =>
        'quran:${r.storageKey}__${q.script.name}';

    return Scaffold(
      appBar: AppBar(title: Text(s.t('downloads'))),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          const Text('Quran Riwayat',
              style:
                  TextStyle(fontWeight: FontWeight.w700)),
          ...kRiwayaCatalog.take(4).map((r) =>
              _row(context, ref, dl, quranId(r.id),
                  '${r.riwayaAr} · ${r.riwayaEn}', 120)),
          const SizedBox(height: 12),
          const Text('Hadith collections',
              style:
                  TextStyle(fontWeight: FontWeight.w700)),
          ...kHadithCollections.map((c) => _row(
              context,
              ref,
              dl,
              'hadith:${c.id}',
              '${c.nameAr} · ${c.nameEn}',
              c.downloadSizeMb ?? 10)),
          const SizedBox(height: 12),
          const Text('Audio',
              style:
                  TextStyle(fontWeight: FontWeight.w700)),
          ...kQariCatalog.map((a) => _row(
              context,
              ref,
              dl,
              'audio:${a.name}',
              '${a.name} (${a.riwayaKey})',
              a.sizeMb)),
          const SizedBox(height: 16),
          Text(
              'Storage used: ${(dl.installed.length * 120).toStringAsFixed(0)} MB (estimate)'),
        ],
      ),
    );
  }

  Widget _row(BuildContext context, WidgetRef ref,
      DownloadState dl, String id, String label, double sizeMb) {
    final installed = dl.installed.contains(id);
    final prog = dl.progress[id];
    final protected =
        ref.read(downloadProvider.notifier).isProtected(id);
    return Card(
      child: ListTile(
        title: Text(label),
        subtitle: Text(installed
            ? 'Installed${protected ? ' · ships with app' : ''} · ${sizeMb.toStringAsFixed(1)} MB'
            : prog != null
                ? 'Downloading ${(prog * 100).toStringAsFixed(0)}%'
                : '${sizeMb.toStringAsFixed(1)} MB'),
        trailing: installed
            ? (protected
                ? const Icon(Icons.check_circle_outline)
                : IconButton(
                    icon:
                        const Icon(Icons.delete_outline),
                    onPressed: () => ref
                        .read(downloadProvider.notifier)
                        .remove(id),
                  ))
            : prog != null
                ? SizedBox(
                    width: 48,
                    height: 48,
                    child: Padding(
                      padding: const EdgeInsets.all(12),
                      child: CircularProgressIndicator(
                          value: prog),
                    ),
                  )
                : IconButton(
                    icon: const Icon(Icons.download),
                    onPressed: () => ref
                        .read(downloadProvider.notifier)
                        .install(id),
                  ),
      ),
    );
  }
}
