import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../data/models/quran.dart';
import '../../data/seed/hadith_collections.dart';
import '../../data/seed/riwayat_catalog.dart';
import '../../state/download_state.dart';
import '../../state/providers.dart';
import '../quran/audio_player_screen.dart';
import '../../data/repositories/verified_asset_quran_repository.dart';
import '../../data/repositories/verified_asset_hadith_repository.dart';

/// Download Manager (§24): per-dataset size, installed/update,
/// storage used, remove. Nothing forced.
class DownloadsScreen extends ConsumerWidget {
  const DownloadsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final dl = ref.watch(downloadProvider);
    final q = ref.watch(quranPrefsProvider);

    String quranId(RiwayaId r) {
      final preferred = '${r.storageKey}__${q.datasetScript.name}';
      if (kVerifiedQuranAssets.containsKey(preferred)) {
        return 'quran:$preferred';
      }
      return 'quran:${r.storageKey}__uthmani';
    }

    return Scaffold(
      appBar: AppBar(title: Text(s.t('downloads'))),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Text(s.t('quranRiwayat'),
              style:
                  TextStyle(fontWeight: FontWeight.w700)),
          ...kRiwayaCatalog.take(4).map((r) {
            final id = quranId(r.id);
            final available = kBundledDatasetIds.contains(id);
            return _row(
              context,
              ref,
              dl,
              id,
              '${r.riwayaAr} · ${r.riwayaEn}',
              120,
              available: available,
            );
          }),
          const SizedBox(height: 12),
          Text(s.t('hadithCollections'),
              style:
                  TextStyle(fontWeight: FontWeight.w700)),
          ...kHadithCollections.map((c) {
            final id = 'hadith:${c.id}';
            final bundled = kBundledHadithCollections
                .any((x) => x.id == c.id && x.isDownloaded);
            return _row(
              context,
              ref,
              dl,
              id,
              '${c.nameAr} · ${c.nameEn}',
              c.downloadSizeMb ?? 10,
              available: bundled,
            );
          }),
          const SizedBox(height: 12),
          Card(
            child: ListTile(
              leading: const Icon(Icons.headphones_outlined),
              title: Text(s.t('quranAudioDownloads')),
              subtitle: Text(s.t('audioDownloadsHint')),
              trailing: const Icon(Icons.chevron_right),
              onTap: () => Navigator.of(context).push(
                MaterialPageRoute(builder: (_) => const AudioPlayerScreen()),
              ),
            ),
          ),
          const SizedBox(height: 16),
          Text('${dl.installed.length} ${s.t('verifiedDatasetsInstalled')}'),
        ],
      ),
    );
  }

  Widget _row(BuildContext context, WidgetRef ref,
      DownloadState dl, String id, String label, double sizeMb,
      {required bool available}) {
    final installed = dl.installed.contains(id);
    final protected =
        ref.read(downloadProvider.notifier).isProtected(id);
    return Card(
      child: ListTile(
        title: Text(label),
        subtitle: Text(installed
            ? '${AppStrings.of(context).t('installed')}${protected ? ' · ${AppStrings.of(context).t('shipsWithApp')}' : ''} · ${sizeMb.toStringAsFixed(1)} MB'
            : available
                ? '${sizeMb.toStringAsFixed(1)} MB'
                : AppStrings.of(context).t('datasetUnavailable')),
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
            : const Icon(Icons.lock_outline),
      ),
    );
  }
}
