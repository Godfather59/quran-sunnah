import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/l10n/app_strings.dart';
import '../../data/models/quran.dart';
import '../../data/seed/surah_metadata.dart';
import '../../data/services/audio_service.dart';
import '../../state/providers.dart';

class AudioPlayerScreen extends ConsumerStatefulWidget {
  const AudioPlayerScreen({super.key});

  @override
  ConsumerState<AudioPlayerScreen> createState() => _AudioPlayerScreenState();
}

class _AudioPlayerScreenState extends ConsumerState<AudioPlayerScreen> {
  int _surah = 112;
  int _from = 1;
  int? _to;
  bool _repeat = false;
  int? _sleepMin;

  Reciter get _reciter {
    final audio = ref.read(audioServiceProvider);
    return reciterById(audio.reciterId) ?? kReciters.first;
  }

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    final q = ref.watch(quranPrefsProvider);
    final audio = ref.watch(audioServiceProvider);
    final svc = ref.read(audioServiceProvider.notifier);
    final riwayaKey = q.riwaya.storageKey;
    final reciters = svc.recitersFor(riwayaKey);
    final canStream = reciters.isNotEmpty;
    final meta = kSurahMetadata.firstWhere((m) => m.number == _surah);
    final reciter = _reciter;
    final task = audio.downloads['${reciter.identifier}:$_surah'];
    final offline = audio.offlineSurahs.contains(_surah);

    return Scaffold(
      appBar: AppBar(title: Text(s.t('quranAudio'))),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                children: [
                  const Icon(Icons.graphic_eq, size: 48),
                  const SizedBox(height: 8),
                  Text(
                    canStream
                        ? (s.isArabic
                            ? '${reciter.nameAr} · ${s.t('quranAudio')}'
                            : '${reciter.nameEn} · Hafs')
                        : s.t('noVerifiedRecitation'),
                    style: Theme.of(context).textTheme.titleLarge,
                    textAlign: TextAlign.center,
                  ),
                  if (canStream) ...[
                    const SizedBox(height: 8),
                    DropdownButton<String>(
                      value: audio.reciterId,
                      items: reciters
                          .map(
                            (r) => DropdownMenuItem(
                              value: r.identifier,
                              child: Text(s.isArabic ? r.nameAr : r.nameEn),
                            ),
                          )
                          .toList(growable: false),
                      onChanged: (value) {
                        if (value != null) svc.selectReciter(value);
                      },
                    ),
                  ],
                  if (audio.refKey != null)
                    Text(
                      '${s.t('playingFrom')} ${audio.refKey}',
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                  if (audio.error != null) ...[
                    const SizedBox(height: 8),
                    Text(
                      audio.error!,
                      style: TextStyle(
                        color: Theme.of(context).colorScheme.error,
                      ),
                    ),
                  ],
                  const SizedBox(height: 16),
                  if (audio.loading)
                    const CircularProgressIndicator()
                  else
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        IconButton(
                          icon: const Icon(Icons.stop),
                          onPressed: svc.stop,
                        ),
                        const SizedBox(width: 8),
                        FilledButton.icon(
                          onPressed: !canStream
                              ? null
                              : audio.playing
                                  ? svc.pause
                                  : () => svc.playRange(
                                        riwayaKey: riwayaKey,
                                        surah: _surah,
                                        fromAyah: _from,
                                        toAyah: _to,
                                        repeatAyah: _repeat,
                                      ),
                          icon: Icon(
                            audio.playing ? Icons.pause : Icons.play_arrow,
                          ),
                          label: Text(
                            audio.playing ? s.t('pause') : s.t('play'),
                          ),
                        ),
                      ],
                    ),
                  const SizedBox(height: 8),
                  Wrap(
                    alignment: WrapAlignment.center,
                    spacing: 8,
                    children: [0.75, 1.0, 1.25, 1.5]
                        .map(
                          (speed) => ChoiceChip(
                            label: Text('${speed}x'),
                            selected: audio.speed == speed,
                            onSelected: (_) => svc.setSpeed(speed),
                          ),
                        )
                        .toList(growable: false),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    s.t('range'),
                    style: const TextStyle(fontWeight: FontWeight.w700),
                  ),
                  DropdownButtonFormField<int>(
                    key: ValueKey('surah-$_surah'),
                    initialValue: _surah,
                    decoration: InputDecoration(labelText: s.t('surah')),
                    items: kSurahMetadata
                        .map(
                          (m) => DropdownMenuItem(
                            value: m.number,
                            child: Text(
                              s.isArabic
                                  ? '${m.number}. ${m.nameAr}'
                                  : '${m.number}. ${m.nameEn}',
                            ),
                          ),
                        )
                        .toList(growable: false),
                    onChanged: (value) => setState(() {
                      _surah = value ?? 112;
                      _from = 1;
                      _to = null;
                    }),
                  ),
                  Row(
                    children: [
                      Expanded(
                        child: DropdownButtonFormField<int>(
                          key: ValueKey('from-$_surah-$_from'),
                          initialValue: _from,
                          decoration: InputDecoration(
                            labelText: s.t('fromAyah'),
                          ),
                          items: List.generate(meta.ayahCount, (i) => i + 1)
                              .map(
                                (ayah) => DropdownMenuItem(
                                  value: ayah,
                                  child: Text('$ayah'),
                                ),
                              )
                              .toList(growable: false),
                          onChanged: (value) => setState(() {
                            _from = value ?? 1;
                            if (_to != null && _to! < _from) _to = null;
                          }),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: DropdownButtonFormField<int?>(
                          key: ValueKey('to-$_surah-$_from-$_to'),
                          initialValue: _to,
                          decoration: InputDecoration(
                            labelText: s.t('toOptional'),
                          ),
                          items: [
                            DropdownMenuItem<int?>(
                              value: null,
                              child: Text(s.t('end')),
                            ),
                            ...List.generate(meta.ayahCount, (i) => i + 1)
                                .where((ayah) => ayah >= _from)
                                .map(
                                  (ayah) => DropdownMenuItem<int?>(
                                    value: ayah,
                                    child: Text('$ayah'),
                                  ),
                                ),
                          ],
                          onChanged: (value) => setState(() => _to = value),
                        ),
                      ),
                    ],
                  ),
                  SwitchListTile(
                    title: Text(s.t('repeat')),
                    value: _repeat,
                    onChanged: (value) {
                      setState(() => _repeat = value);
                      svc.setRepeatAyah(value);
                    },
                  ),
                  _OfflineRow(
                    surah: _surah,
                    offline: offline,
                    task: task,
                    localBytes: audio.surahStorageBytes[_surah] ?? 0,
                    onDownload: canStream
                        ? () => _confirmAndQueue(context, reciter, _surah)
                        : null,
                    onDelete: () => svc.deleteSurah(reciter, _surah),
                    onPause: task == null
                        ? null
                        : () => svc.pauseDownload(task.key),
                    onResume: task == null
                        ? null
                        : () => svc.resumeDownload(task.key),
                    onCancel: task == null
                        ? null
                        : () => svc.cancelDownload(task.key),
                    onRetry: task == null
                        ? null
                        : () => svc.retryDownload(task.key),
                  ),
                  ListTile(
                    leading: const Icon(Icons.bedtime_outlined),
                    title: Text(s.t('sleepTimer')),
                    trailing: DropdownButton<int?>(
                      value: _sleepMin,
                      hint: Text(s.t('off')),
                      items: const [
                        DropdownMenuItem(value: 15, child: Text('15 min')),
                        DropdownMenuItem(value: 30, child: Text('30 min')),
                        DropdownMenuItem(value: 60, child: Text('60 min')),
                      ],
                      onChanged: (value) {
                        setState(() => _sleepMin = value);
                        svc.sleepTimer(
                          value == null ? null : Duration(minutes: value),
                        );
                      },
                    ),
                  ),
                  ListTile(
                    leading: const Icon(Icons.storage_outlined),
                    title: Text(s.t('storageUsed')),
                    subtitle: Text(
                      '${s.t('reciterStorage')}: ${_formatBytes(audio.reciterStorageBytes)}',
                    ),
                    trailing: Text(_formatBytes(audio.storageBytes)),
                  ),
                ],
              ),
            ),
          ),
          if (audio.downloads.isNotEmpty) ...[
            const SizedBox(height: 12),
            Text(
              s.t('downloadQueue'),
              style: Theme.of(context).textTheme.titleMedium,
            ),
            ...audio.downloads.values.map(
              (download) => _DownloadTaskTile(task: download),
            ),
          ],
          const SizedBox(height: 12),
          Text(
            s.t('streamingOfflineHint'),
            style: Theme.of(context).textTheme.bodySmall,
          ),
        ],
      ),
    );
  }

  Future<void> _confirmAndQueue(
    BuildContext context,
    Reciter reciter,
    int surah,
  ) async {
    final svc = ref.read(audioServiceProvider.notifier);
    final s = AppStrings.of(context);
    final messenger = ScaffoldMessenger.of(context);

    final total = await svc.estimateSurahBytes(reciter, surah);
    if (!context.mounted) return;
    final size = _formatBytes(total);
    final accepted = await showDialog<bool>(
          context: context,
          builder: (ctx) => AlertDialog(
            title: Text(s.t('downloads')),
            content: Text(
              s.isArabic
                  ? 'تنزيل السورة $surah بصوت ${reciter.nameAr}؟ الحجم التقريبي: $size'
                  : s.locale.languageCode == 'fr'
                      ? 'Télécharger la sourate $surah avec ${reciter.nameEn} ? Taille estimée : $size'
                      : 'Download surah $surah by ${reciter.nameEn}? Estimated size: $size',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(ctx, false),
                child: Text(s.t('cancel')),
              ),
              FilledButton(
                onPressed: () => Navigator.pop(ctx, true),
                child: Text(s.t('downloads')),
              ),
            ],
          ),
        ) ??
        false;

    if (!accepted) return;
    await svc.queueSurahDownload(
      reciter: reciter,
      surah: surah,
      totalBytes: total,
    );
    messenger.showSnackBar(
      SnackBar(content: Text('${s.t('queued')} · ${s.t('surah')} $surah')),
    );
  }

  static String _formatBytes(int bytes) {
    if (bytes <= 0) return '0 MB';
    return '${(bytes / 1048576).toStringAsFixed(1)} MB';
  }
}

class _OfflineRow extends StatelessWidget {
  const _OfflineRow({
    required this.surah,
    required this.offline,
    required this.task,
    required this.localBytes,
    required this.onDownload,
    required this.onDelete,
    required this.onPause,
    required this.onResume,
    required this.onCancel,
    required this.onRetry,
  });

  final int surah;
  final bool offline;
  final AudioDownloadTask? task;
  final int localBytes;
  final VoidCallback? onDownload;
  final VoidCallback onDelete;
  final VoidCallback? onPause;
  final VoidCallback? onResume;
  final VoidCallback? onCancel;
  final VoidCallback? onRetry;

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    final current = task;

    if (offline) {
      return ListTile(
        leading: const Icon(Icons.offline_pin_outlined),
        title: Text('${s.t('surah')} $surah · ${s.t('downloaded')}'),
        subtitle: Text('${s.t('surahStorage')}: ${_AudioPlayerScreenState._formatBytes(localBytes)}'),
        trailing: IconButton(
          tooltip: s.t('remove'),
          icon: const Icon(Icons.delete_outline),
          onPressed: onDelete,
        ),
      );
    }

    if (current != null) {
      switch (current.status) {
        case AudioDownloadStatus.queued:
        case AudioDownloadStatus.measuring:
        case AudioDownloadStatus.downloading:
        case AudioDownloadStatus.paused:
          return ListTile(
            leading: SizedBox(
              width: 24,
              height: 24,
              child: CircularProgressIndicator(
                value: current.status == AudioDownloadStatus.queued
                    ? null
                    : current.progress,
              ),
            ),
            title: Text(
              '${s.t('surah')} $surah · '
              '${current.status == AudioDownloadStatus.paused ? s.t('pause') : s.t('downloading')}',
            ),
            subtitle: LinearProgressIndicator(value: current.progress),
            trailing: Wrap(
              spacing: 2,
              children: [
                if (current.status == AudioDownloadStatus.paused)
                  IconButton(
                    icon: const Icon(Icons.play_arrow),
                    onPressed: onResume,
                  )
                else
                  IconButton(
                    icon: const Icon(Icons.pause),
                    onPressed: onPause,
                  ),
                IconButton(
                  icon: const Icon(Icons.close),
                  onPressed: onCancel,
                ),
              ],
            ),
          );
        case AudioDownloadStatus.failed:
          return ListTile(
            leading: const Icon(Icons.error_outline),
            title: Text('${s.t('surah')} $surah'),
            subtitle: Text(current.error ?? ''),
            trailing: IconButton(
              tooltip: s.t('retry'),
              icon: const Icon(Icons.refresh),
              onPressed: onRetry,
            ),
          );
        case AudioDownloadStatus.canceled:
        case AudioDownloadStatus.completed:
          break;
      }
    }

    return ListTile(
      leading: const Icon(Icons.download_outlined),
      title: Text('${s.t('downloads')} · ${s.t('surah')} $surah'),
      trailing: const Icon(Icons.chevron_right),
      onTap: onDownload,
    );
  }
}

class _DownloadTaskTile extends ConsumerWidget {
  const _DownloadTaskTile({required this.task});
  final AudioDownloadTask task;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final svc = ref.read(audioServiceProvider.notifier);
    final reciter = reciterById(task.reciterId);
    final name = reciter == null
        ? task.reciterId
        : s.isArabic
            ? reciter.nameAr
            : reciter.nameEn;
    final status = switch (task.status) {
      AudioDownloadStatus.queued => s.t('queued'),
      AudioDownloadStatus.measuring => s.t('downloading'),
      AudioDownloadStatus.downloading => s.t('downloading'),
      AudioDownloadStatus.paused => s.t('pause'),
      AudioDownloadStatus.completed => s.t('downloaded'),
      AudioDownloadStatus.failed => s.t('retry'),
      AudioDownloadStatus.canceled => s.t('cancel'),
    };

    return Card(
      child: ListTile(
        title: Text('$name · ${s.t('surah')} ${task.surah}'),
        subtitle: Text(
          '$status · ${(task.progress * 100).toStringAsFixed(0)}%',
        ),
        trailing: task.status == AudioDownloadStatus.failed
            ? IconButton(
                icon: const Icon(Icons.refresh),
                tooltip: s.t('retry'),
                onPressed: () => svc.retryDownload(task.key),
              )
            : task.status == AudioDownloadStatus.completed ||
                    task.status == AudioDownloadStatus.canceled
                ? IconButton(
                    icon: const Icon(Icons.close),
                    tooltip: s.t('remove'),
                    onPressed: () => svc.dismissDownload(task.key),
                  )
                : null,
      ),
    );
  }
}
