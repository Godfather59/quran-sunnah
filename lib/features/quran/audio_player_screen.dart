import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../data/models/quran.dart';
import '../../data/seed/surah_metadata.dart';
import '../../data/services/audio_service.dart';
import '../../state/providers.dart';

/// Audio player (§12) wired to real streaming recitation.
/// Reciter ↔ Riwaya binding enforced: only Hafs streams exist, so the
/// player refuses non-Hafs editions instead of mislabeling audio.
class AudioPlayerScreen extends ConsumerStatefulWidget {
  const AudioPlayerScreen({super.key});

  @override
  ConsumerState<AudioPlayerScreen> createState() =>
      _AudioPlayerScreenState();
}

class _AudioPlayerScreenState
    extends ConsumerState<AudioPlayerScreen> {
  int _surah = 112;
  int _from = 1;
  int? _to;
  bool _repeat = false;
  int? _sleepMin;
  double? _dlProgress;
  int? _dlBytes;

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
    final meta =
        kSurahMetadata.firstWhere((m) => m.number == _surah);
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
                    !canStream
                        ? (s.isArabic
                            ? 'لا توجد تلاوة موثقة لهذه الرواية'
                            : 'No verified recitation for this Riwaya')
                        : s.isArabic
                            ? '${_reciter.nameAr} · ${s.t('quranAudio')}'
                            : '${_reciter.nameEn} · Hafs (streaming)',
                    style:
                        Theme.of(context).textTheme.titleLarge,
                    textAlign: TextAlign.center,
                  ),
                  if (canStream) ...[
                    const SizedBox(height: 8),
                    DropdownButton<String>(
                      value: audio.reciterId,
                      items: reciters
                          .map((r) => DropdownMenuItem(
                              value: r.identifier,
                              child: Text(s.isArabic
                                  ? r.nameAr
                                  : r.nameEn)))
                          .toList(),
                      onChanged: (v) {
                        if (v != null) {
                          svc.selectReciter(v);
                        }
                      },
                    ),
                  ],
                  if (audio.refKey != null)
                    Text('Playing from ${audio.refKey}',
                        style: Theme.of(context)
                            .textTheme
                            .bodySmall),
                  if (audio.error != null) ...[
                    const SizedBox(height: 8),
                    Text(audio.error!,
                        style: TextStyle(
                            color: Theme.of(context)
                                .colorScheme
                                .error)),
                  ],
                  const SizedBox(height: 16),
                  if (audio.loading)
                    const CircularProgressIndicator()
                  else
                    Row(
                      mainAxisAlignment:
                          MainAxisAlignment.center,
                      children: [
                        IconButton(
                            icon: const Icon(Icons.stop),
                            onPressed: () =>
                                svc.stop()),
                        const SizedBox(width: 8),
                        FilledButton.icon(
                          onPressed: !canStream
                              ? null
                              : audio.playing
                                  ? () => svc.pause()
                                  : () => svc.playRange(
                                        riwayaKey:
                                            riwayaKey,
                                        surah: _surah,
                                        fromAyah: _from,
                                        toAyah: _to,
                                        repeatAyah: _repeat,
                                      ),
                          icon: Icon(audio.playing
                              ? Icons.pause
                              : Icons.play_arrow),
                          label: Text(audio.playing
                              ? s.t('pause')
                              : s.t('play')),
                        ),
                      ],
                    ),
                  const SizedBox(height: 8),
                  Row(
                    mainAxisAlignment:
                        MainAxisAlignment.center,
                    children: [0.75, 1.0, 1.25, 1.5]
                        .map((v) => Padding(
                              padding:
                                  const EdgeInsets.symmetric(
                                      horizontal: 4),
                              child: ChoiceChip(
                                label: Text('${v}x'),
                                selected:
                                    audio.speed == v,
                                onSelected: (_) =>
                                    svc.setSpeed(v),
                              ),
                            ))
                        .toList(),
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
                crossAxisAlignment:
                    CrossAxisAlignment.start,
                children: [
                  const Text('Range',
                      style: TextStyle(
                          fontWeight: FontWeight.w700)),
                  DropdownButtonFormField<int>(
                    initialValue: _surah,
                    decoration: const InputDecoration(
                        labelText: 'Surah'),
                    items: kSurahMetadata
                        .map((m) => DropdownMenuItem(
                            value: m.number,
                            child: Text(
                                '${m.number}. ${m.nameEn}')))
                        .toList(),
                    onChanged: (v) => setState(() {
                      _surah = v ?? 112;
                      _from = 1;
                      _to = null;
                    }),
                  ),
                  Row(
                    children: [
                      Expanded(
                        child: DropdownButtonFormField<int>(
                          initialValue: _from,
                          decoration:
                              const InputDecoration(
                                  labelText: 'From ayah'),
                          items: List.generate(
                                  meta.ayahCount, (i) => i + 1)
                              .map((a) => DropdownMenuItem(
                                  value: a,
                                  child: Text('$a')))
                              .toList(),
                          onChanged: (v) => setState(
                              () => _from = v ?? 1),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: DropdownButtonFormField<int?>(
                          initialValue: _to,
                          decoration:
                              const InputDecoration(
                                  labelText: 'To (optional)'),
                          items: [
                            const DropdownMenuItem(
                                value: null,
                                child: Text('End')),
                            ...List.generate(
                                    meta.ayahCount,
                                    (i) => i + 1)
                                .where((a) => a >= _from)
                                .map((a) => DropdownMenuItem(
                                    value: a,
                                    child: Text('$a'))),
                          ],
                          onChanged: (v) =>
                              setState(() => _to = v),
                        ),
                      ),
                    ],
                  ),
                  SwitchListTile(
                    title: Text(s.t('repeat')),
                    value: _repeat,
                    onChanged: (v) {
                      setState(() => _repeat = v);
                      svc.setRepeatAyah(v);
                    },
                  ),
                  _OfflineRow(
                    surah: _surah,
                    offline: offline,
                    progress: _dlProgress,
                    sizeBytes: _dlBytes,
                    onDownload: canStream
                        ? () => _downloadSurah(context)
                        : null,
                    onDelete: () => svc.deleteSurah(
                        _reciter, _surah),
                  ),
                  ListTile(
                    leading:
                        const Icon(Icons.bedtime),
                    title: Text(s.t('sleepTimer')),
                    trailing: DropdownButton<int?>(
                      value: _sleepMin,
                      hint: Text(s.t('off')),
                      items: const [
                        DropdownMenuItem(
                            value: 15,
                            child: Text('15 min')),
                        DropdownMenuItem(
                            value: 30,
                            child: Text('30 min')),
                        DropdownMenuItem(
                            value: 60,
                            child: Text('60 min')),
                      ],
                      onChanged: (v) {
                        setState(() => _sleepMin = v);
                        svc.sleepTimer(v == null
                            ? null
                            : Duration(minutes: v));
                      },
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
          Text(
            s.isArabic
                ? 'البث يحتاج إنترنت. يمكن تنزيل السور للاستماع دون إنترنت (يُعرض الحجم أولًا).'
                : 'Streaming requires internet. Surahs can be downloaded per reciter for offline listening (size shown first).',
            style: Theme.of(context).textTheme.bodySmall,
          ),
        ],
      ),
    );
  }

  Future<void> _downloadSurah(BuildContext context) async {
    final svc = ref.read(audioServiceProvider.notifier);
    final reciter = _reciter;
    setState(() {
      _dlProgress = 0;
      _dlBytes = null;
    });
    final messenger = ScaffoldMessenger.of(context);
    var confirmed = true;
    final bytes = await svc.downloadSurah(
      reciter: reciter,
      surah: _surah,
      onProgress: (p) {
        if (mounted) {
          setState(() => _dlProgress = p);
        }
      },
      confirm: (total) async {
        if (!mounted) {
          return false;
        }
        setState(() => _dlBytes = total);
        final mb = (total / 1048576).toStringAsFixed(1);
        final ok = await showDialog<bool>(
          context: context,
          builder: (ctx) => AlertDialog(
            title: Text(AppStrings.of(ctx).t('downloads')),
            content: Text(AppStrings.of(ctx).isArabic
                ? 'تنزيل السورة $_surah بصوت ${reciter.nameAr}؟ الحجم: $mb م.ب'
                : 'Download surah $_surah by ${reciter.nameEn}? Size: $mb MB'),
            actions: [
              TextButton(
                  onPressed: () =>
                      Navigator.pop(ctx, false),
                  child: Text(
                      AppStrings.of(ctx).t('cancel'))),
              FilledButton(
                  onPressed: () =>
                      Navigator.pop(ctx, true),
                  child: Text(
                      AppStrings.of(ctx).t('downloads'))),
            ],
          ),
        );
        confirmed = ok == true;
        return confirmed;
      },
    );
    if (!mounted) {
      return;
    }
    setState(() {
      _dlProgress = null;
      _dlBytes = null;
    });
    if (confirmed && bytes > 0) {
      messenger.showSnackBar(SnackBar(
          content: Text(
              'Surah $_surah offline · ${(bytes / 1048576).toStringAsFixed(1)} MB')));
    }
  }
}

class _OfflineRow extends StatelessWidget {
  const _OfflineRow({
    required this.surah,
    required this.offline,
    required this.progress,
    required this.sizeBytes,
    required this.onDownload,
    required this.onDelete,
  });

  final int surah;
  final bool offline;
  final double? progress;
  final int? sizeBytes;
  final VoidCallback? onDownload;
  final VoidCallback onDelete;

  @override
  Widget build(BuildContext context) {
    if (progress != null) {
      final mb = sizeBytes == null
          ? ''
          : ' · ${(sizeBytes! / 1048576).toStringAsFixed(1)} MB';
      return ListTile(
        leading: SizedBox(
          width: 24,
          height: 24,
          child: CircularProgressIndicator(value: progress),
        ),
        title: Text(
            'Downloading surah $surah…${(progress! * 100).toStringAsFixed(0)}%$mb'),
      );
    }
    if (offline) {
      return ListTile(
        leading: const Icon(Icons.offline_pin_outlined),
        title: Text('Surah $surah available offline'),
        trailing: IconButton(
          icon: const Icon(Icons.delete_outline),
          onPressed: onDelete,
        ),
      );
    }
    return ListTile(
      leading: const Icon(Icons.download_outlined),
      title: Text('Download surah $surah for offline'),
      trailing: const Icon(Icons.chevron_right),
      onTap: onDownload,
    );
  }
}
