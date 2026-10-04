import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/l10n/app_strings.dart';
import '../../data/content/content_packages.dart';
import '../../state/download_state.dart';
import '../quran/audio_player_screen.dart';

class DownloadsScreen extends ConsumerWidget {
  const DownloadsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final dl = ref.watch(downloadProvider);
    final manifest = ref.watch(contentPackageManifestProvider);

    return Scaffold(
      appBar: AppBar(title: Text(s.t('downloads'))),
      body: manifest.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, _) => Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Text('${s.t('contentUnavailable')}\n$error'),
          ),
        ),
        data: (catalog) => ListView(
          padding: const EdgeInsets.all(16),
          children: [
            Text(
              s.t('coreContent'),
              style: const TextStyle(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 8),
            ..._coreRows(s),
            const SizedBox(height: 16),
            Text(
              s.t('optionalContent'),
              style: const TextStyle(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 8),
            ...catalog.packages.map(
              (pkg) => _packageRow(context, ref, dl, pkg),
            ),
            const SizedBox(height: 16),
            Card(
              child: ListTile(
                leading: const Icon(Icons.storage_outlined),
                title: Text(s.t('storageUsed')),
                subtitle: Text(_formatBytes(dl.storageUsedBytes)),
                trailing: Text(
                  '${dl.installed.length} ${s.t('verifiedDatasetsInstalled')}',
                ),
              ),
            ),
            Card(
              child: ListTile(
                leading: const Icon(Icons.headphones_outlined),
                title: Text(s.t('quranAudioDownloads')),
                subtitle: Text(s.t('audioDownloadsHint')),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => Navigator.of(context).push(
                  MaterialPageRoute(
                    builder: (_) => const AudioPlayerScreen(),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  List<Widget> _coreRows(AppStrings s) {
    final rows = <(String, String)>[
      ('حفص عن عاصم · ${s.t('uthmani')}', 'quran:hafs-an-asim__uthmani'),
      ('حفص عن عاصم · ${s.t('imlai')}', 'quran:hafs-an-asim__imlai'),
      ('حفص عن عاصم · ${s.t('indopak')}', 'quran:hafs-an-asim__indopak'),
      ('ورش عن نافع · ${s.t('uthmani')}', 'quran:warsh-an-nafi__uthmani'),
      ('قالون عن نافع · ${s.t('uthmani')}', 'quran:qalun-an-nafi__uthmani'),
      (s.isArabic ? 'صحيح البخاري' : 'Sahih al-Bukhari', 'hadith:bukhari'),
      (s.isArabic ? 'صحيح مسلم' : 'Sahih Muslim', 'hadith:muslim'),
    ];
    return rows
        .map(
          (row) => Card(
            child: ListTile(
              leading: const Icon(Icons.check_circle_outline),
              title: Text(row.$1),
              subtitle: Text(
                '${s.t('installed')} · ${s.t('shipsWithApp')}',
              ),
            ),
          ),
        )
        .toList(growable: false);
  }

  Widget _packageRow(
    BuildContext context,
    WidgetRef ref,
    DownloadState dl,
    ContentPackage pkg,
  ) {
    final s = AppStrings.of(context);
    final notifier = ref.read(downloadProvider.notifier);
    final status = notifier.statusOf(pkg.id);
    final installed = dl.installed.contains(pkg.id);
    final progress = dl.progress[pkg.id] ?? 0;
    final title = s.isArabic ? pkg.titleAr : pkg.titleEn;
    final stateText = switch (status) {
      PackageTransferStatus.downloading =>
        '${s.t('downloading')} ${(progress * 100).toStringAsFixed(0)}%',
      PackageTransferStatus.paused => s.t('pause'),
      PackageTransferStatus.failed => s.t('downloadFailed'),
      PackageTransferStatus.installed => s.t('installed'),
      PackageTransferStatus.idle => s.t('notDownloaded'),
    };

    return Card(
      child: ListTile(
        title: Text(title),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('$stateText · ${_formatBytes(pkg.sizeBytes)}'),
            Text(
              '${s.t('packageSource')}: ${pkg.source}',
              style: Theme.of(context).textTheme.bodySmall,
            ),
            Text(
              '${s.t('licenseStatus')}: '
              '${_licenseLabel(s, pkg.licenseStatus)}',
              style: Theme.of(context).textTheme.bodySmall,
            ),
            if (status == PackageTransferStatus.downloading ||
                status == PackageTransferStatus.paused)
              Padding(
                padding: const EdgeInsets.only(top: 8),
                child: LinearProgressIndicator(value: progress),
              ),
            if (dl.errors[pkg.id] case final error?)
              Padding(
                padding: const EdgeInsets.only(top: 4),
                child: Text(
                  error,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(
                    color: Theme.of(context).colorScheme.error,
                    fontSize: 11,
                  ),
                ),
              ),
          ],
        ),
        isThreeLine: true,
        trailing: _actions(
          context,
          ref,
          pkg,
          status,
          installed,
        ),
      ),
    );
  }

  Widget _actions(
    BuildContext context,
    WidgetRef ref,
    ContentPackage pkg,
    PackageTransferStatus status,
    bool installed,
  ) {
    final s = AppStrings.of(context);
    final notifier = ref.read(downloadProvider.notifier);

    Future<void> changed(Future<void> Function() action) async {
      try {
        await action();
        ref.read(contentRevisionProvider.notifier).state++;
      } catch (_) {
        // Error text is already kept in DownloadState.
      }
    }

    if (installed) {
      return IconButton(
        tooltip: s.t('remove'),
        icon: const Icon(Icons.delete_outline),
        onPressed: () => changed(() => notifier.remove(pkg.id)),
      );
    }

    return switch (status) {
      PackageTransferStatus.downloading => Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            IconButton(
              tooltip: s.t('pause'),
              icon: const Icon(Icons.pause),
              onPressed: () => notifier.pause(pkg.id),
            ),
            IconButton(
              tooltip: s.t('cancelDownload'),
              icon: const Icon(Icons.close),
              onPressed: () => changed(() => notifier.cancel(pkg.id)),
            ),
          ],
        ),
      PackageTransferStatus.paused => Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            IconButton(
              tooltip: s.t('resume'),
              icon: const Icon(Icons.play_arrow),
              onPressed: () => changed(() => notifier.resume(pkg.id)),
            ),
            IconButton(
              tooltip: s.t('cancelDownload'),
              icon: const Icon(Icons.close),
              onPressed: () => changed(() => notifier.cancel(pkg.id)),
            ),
          ],
        ),
      PackageTransferStatus.failed => IconButton(
          tooltip: s.t('retry'),
          icon: const Icon(Icons.refresh),
          onPressed: () => changed(() => notifier.retry(pkg.id)),
        ),
      _ => IconButton(
          tooltip: s.t('download'),
          icon: const Icon(Icons.download),
          onPressed: () => changed(() => notifier.install(pkg.id)),
        ),
    };
  }

  String _formatBytes(int bytes) {
    if (bytes < 1024) return '$bytes B';
    final kb = bytes / 1024;
    if (kb < 1024) return '${kb.toStringAsFixed(1)} KB';
    final mb = kb / 1024;
    return '${mb.toStringAsFixed(1)} MB';
  }

  String _licenseLabel(AppStrings s, String value) {
    if (!s.isArabic) return value;
    return switch (value) {
      'restricted-noncommercial' => 'مقيد للاستخدام غير التجاري',
      'restricted-verify-direct-rights' =>
        'مقيد — يلزم التحقق المباشر من الحقوق',
      'conditional-version-incomplete' =>
        'مشروط — بيانات الإصدار غير مكتملة',
      'cc-by-4.0' => 'CC BY 4.0 — مع الإسناد',
      'gpl-with-attribution' => 'GPL — مع الإسناد',
      'underlying-text-rights-unresolved' =>
        'حقوق النص الأصلي غير محسومة',
      _ => value,
    };
  }
}
