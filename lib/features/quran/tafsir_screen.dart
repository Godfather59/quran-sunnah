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
  bool _compare = false;

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
          SwitchListTile(
            title: Text(s.isArabic
                ? 'مقارنة: الجلالين + السراج'
                : s.locale.languageCode == 'fr'
                    ? 'Comparer : Jalalayn + Siraj'
                    : 'Compare: Jalalayn + Siraj'),
            value: _compare,
            onChanged: (v) => setState(() => _compare = v),
            secondary: const Icon(Icons.compare_arrows),
          ),
          if (_compare)
            _CompareTafsir(
                surah: widget.surah, ayah: widget.ayah)
          else
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

/// Side-by-side Jalalayn + Siraj for the same ayah (bundled, offline).
class _CompareTafsir extends ConsumerWidget {
  const _CompareTafsir({required this.surah, required this.ayah});

  final int surah;
  final int ayah;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final jal = ref.watch(tafsirSurahProvider(('jalalayn', surah)));
    final sir = ref.watch(tafsirSurahProvider(('siraj', surah)));
    Widget card(String title, AsyncValue<Map<int, String>> async) {
      return Card(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(title,
                  style: Theme.of(context)
                      .textTheme
                      .titleSmall
                      ?.copyWith(fontWeight: FontWeight.w700)),
              const SizedBox(height: 8),
              async.when(
                loading: () => const Center(
                    child: CircularProgressIndicator()),
                error: (e, _) => Text('$e'),
                data: (map) {
                  final t = map[ayah];
                  if (t == null) return Text(s.t('contentUnavailable'));
                  return SelectableText(t,
                      textDirection: TextDirection.rtl,
                      style:
                          const TextStyle(fontSize: 17, height: 2.0));
                },
              ),
            ],
          ),
        ),
      );
    }

    final isWide = MediaQuery.sizeOf(context).width >= 700;
    if (isWide) {
      return Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(child: card('الجلالين · Jalalayn', jal)),
          const SizedBox(width: 8),
          Expanded(child: card('السراج · Siraj', sir)),
        ],
      );
    }
    return Column(
      children: [
        card('الجلالين · Jalalayn', jal),
        const SizedBox(height: 8),
        card('السراج · Siraj', sir),
      ],
    );
  }
}
