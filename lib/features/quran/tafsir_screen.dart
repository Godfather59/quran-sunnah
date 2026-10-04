import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../data/repositories/tafsir_repository.dart';
import '../../data/content/content_packages.dart';
import '../../state/download_state.dart';
import '../../state/providers.dart';

/// Tafsir per ayah (§14). Source always shown; bundled works offline,
/// unbundled ids honestly report unavailability.
class TafsirScreen extends ConsumerStatefulWidget {
  const TafsirScreen({super.key, required this.surah, required this.ayah});

  final int surah;
  final int ayah;

  @override
  ConsumerState<TafsirScreen> createState() => _TafsirScreenState();
}

class _TafsirScreenState extends ConsumerState<TafsirScreen> {
  late String _tafsir;

  @override
  void initState() {
    super.initState();
    _tafsir = ref.read(quranPrefsProvider).tafsirId;
    if (!kTafsirCatalog.any((t) => t.id == _tafsir && t.bundled)) {
      _tafsir = 'jalalayn';
    }
  }

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    final info =
        kTafsirCatalog.firstWhere((t) => t.id == _tafsir);
    final downloads = ref.watch(downloadProvider);
    final packageId = 'quran:tafsir-$_tafsir';
    final installed = downloads.installed.contains(packageId);
    final entriesAsync = ref
        .watch(tafsirSurahProvider((_tafsir, widget.surah)));

    return Scaffold(
      appBar: AppBar(
          title: Text('${s.t('tafsir')} · ${widget.surah}:${widget.ayah}')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Wrap(
            spacing: 8,
            children: kTafsirCatalog
                .map((t) => ChoiceChip(
                      label: Text(s.isArabic ? t.titleAr : t.titleEn),
                      selected: _tafsir == t.id,
                      onSelected: !t.bundled
                          ? null
                          : (_) async {
                              final id = 'quran:tafsir-${t.id}';
                              if (!ref
                                  .read(downloadProvider)
                                  .installed
                                  .contains(id)) {
                                try {
                                  await ref
                                      .read(downloadProvider.notifier)
                                      .install(id);
                                  ref
                                      .read(contentRevisionProvider.notifier)
                                      .state++;
                                } catch (_) {
                                  return;
                                }
                              }
                              if (!mounted) return;
                              setState(() => _tafsir = t.id);
                              final q = ref.read(quranPrefsProvider);
                              await ref
                                  .read(quranPrefsProvider.notifier)
                                  .update(q.copyWith(tafsirId: t.id));
                            },
                    ))
                .toList(),
          ),
          const SizedBox(height: 12),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                      '${s.t('source')}: ${info.titleAr} · ${info.titleEn}\n${info.source}',
                      textDirection: TextDirection.ltr,
                      style:
                          Theme.of(context).textTheme.labelSmall),
                  const SizedBox(height: 8),
                  if (!info.bundled) ...[
                    Text(s.t('contentUnavailable')),
                    const SizedBox(height: 8),
                    Text(s.t('tafsirDatasetRequired')),
                  ] else if (!installed) ...[
                    Text(s.t('downloadBeforeUse')),
                    const SizedBox(height: 8),
                    FilledButton.icon(
                      onPressed: () async {
                        try {
                          await ref
                              .read(downloadProvider.notifier)
                              .install(packageId);
                          ref
                              .read(contentRevisionProvider.notifier)
                              .state++;
                        } catch (_) {}
                      },
                      icon: const Icon(Icons.download),
                      label: Text(s.t('download')),
                    ),
                  ] else
                    entriesAsync.when(
                      loading: () => const Center(
                          child: CircularProgressIndicator()),
                      error: (e, _) => Text('$e'),
                      data: (map) {
                        final text = map[widget.ayah];
                        if (text == null) {
                          return Text(
                              s.t('contentUnavailable'));
                        }
                        return Text(text,
                            textDirection:
                                TextDirection.rtl,
                            style: const TextStyle(
                                fontSize: 18,
                                height: 2.0));
                      },
                    ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
