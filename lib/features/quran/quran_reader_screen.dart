import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../core/theme/app_theme.dart';
import '../../core/utils/text_utils.dart';
import '../../core/widgets/common.dart';
import '../../data/models/quran.dart';
import '../../data/repositories/translation_repository.dart';
import '../../data/seed/riwayat_catalog.dart';
import '../../data/seed/surah_metadata.dart';
import '../../data/repositories/quran_repository.dart';
import '../../state/library_state.dart';
import '../../state/providers.dart';
import 'mushaf_reader_screen.dart';
import 'riwaya_selector_screen.dart';
import 'compare_riwayat_screen.dart';
import 'tafsir_screen.dart';
import 'widgets/ayah_action_sheet.dart';
import 'widgets/tajweed_text.dart';

/// Reading Mode: vertical ayahs. Mushaf Mode via app-bar toggle.
/// Peaceful hierarchy: Quran text → number → translation → actions.
class QuranReaderScreen extends ConsumerStatefulWidget {
  const QuranReaderScreen(
      {super.key, required this.surah, this.initialAyah = 1});

  final int surah;

  /// Jump target (Juz/Hizb entry). Scrolled to after first frame.
  final int initialAyah;

  @override
  ConsumerState<QuranReaderScreen> createState() =>
      _QuranReaderScreenState();
}

class _QuranReaderScreenState extends ConsumerState<QuranReaderScreen> {
  bool _fullscreen = false;
  bool _scrolledToInitial = false;
  final Map<int, GlobalKey> _keys = {};

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    final q = ref.watch(quranPrefsProvider);
    final riwaya =
        kRiwayaCatalog.firstWhere((r) => r.id == q.riwaya);
    final meta =
        kSurahMetadata.firstWhere((m) => m.number == widget.surah);
    final ayahsAsync = ref.watch(
        _surahAyahsProvider((widget.surah, q.editionId)));

    return Scaffold(
      appBar: _fullscreen
          ? null
          : AppBar(
              title: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(meta.nameAr, textDirection: TextDirection.rtl),
                  GestureDetector(
                    onTap: () => Navigator.of(context).push(
                        MaterialPageRoute(
                            builder: (_) =>
                                const RiwayaSelectorScreen())),
                    child: Text(
                      '${s.t('riwaya')} · ${riwaya.riwayaAr} ▾',
                      style:
                          Theme.of(context).textTheme.labelSmall,
                    ),
                  ),
                ],
              ),
              actions: [
                if (q.script == QuranScript.tajweed)
                  IconButton(
                    tooltip: 'Tajweed legend',
                    icon: const Icon(Icons.palette_outlined),
                    onPressed: () => showModalBottomSheet(
                        context: context,
                        showDragHandle: true,
                        builder: (_) =>
                            const TajweedLegendSheet()),
                  ),
                IconButton(
                  tooltip: 'Mushaf / Reading',
                  icon: Icon(q.readingMode.name == 'reading'
                      ? Icons.auto_stories
                      : Icons.view_agenda),
                  onPressed: () => Navigator.of(context).pushReplacement(
                      MaterialPageRoute(
                          builder: (_) =>
                              MushafReaderScreen(surah: widget.surah))),
                ),
                IconButton(
                  tooltip: 'Fullscreen',
                  icon: const Icon(Icons.fullscreen),
                  onPressed: () =>
                      setState(() => _fullscreen = true),
                ),
              ],
            ),
      body: GestureDetector(
        onTap: () {
          if (_fullscreen) setState(() => _fullscreen = false);
        },
        child: ayahsAsync.when(
          loading: () =>
              const Center(child: CircularProgressIndicator()),
          error: (e, _) => Center(child: Text('$e')),
          data: (ayahs) {
            if (ayahs.isEmpty ||
                ayahs.every((a) => a.isPlaceholder)) {              return ListView(
                padding: EdgeInsets.all(q.margins + 8),
                children: [
                  UnavailableBanner(
                      message: s.t('contentUnavailable')),
                  const SizedBox(height: 8),
                  Text(
                    'Edition: ${q.editionId}\nSource: ${riwaya.source} v${riwaya.datasetVersion}\n'
                    'Connect a verified dataset to render Surah ${meta.nameEn}.',
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                  const SizedBox(height: 16),
                  Wrap(
                    spacing: 8,
                    children: [
                      FilledButton.tonal(
                        onPressed: () => Navigator.of(context).push(
                            MaterialPageRoute(
                                builder: (_) =>
                                    CompareRiwayatScreen(
                                        surah: widget.surah,
                                        ayah: 1))),
                        child: Text(s.t('compareRiwayat')),
                      ),
                      FilledButton.tonal(
                        onPressed: () => Navigator.of(context).push(
                            MaterialPageRoute(
                                builder: (_) => TafsirScreen(
                                    surah: widget.surah,
                                    ayah: 1))),
                        child: const Text('Tafsir'),
                      ),
                    ],
                  ),
                ],
              );
            }
            if (!_scrolledToInitial && widget.initialAyah > 1) {
              _scrolledToInitial = true;
              WidgetsBinding.instance.addPostFrameCallback((_) {
                final key = _keys[widget.initialAyah];
                final ctx = key?.currentContext;
                if (ctx != null) {
                  Scrollable.ensureVisible(ctx,
                      duration:
                          const Duration(milliseconds: 400));
                }
              });
            }
            return ListView.separated(
              padding: EdgeInsets.fromLTRB(
                  q.margins + 8, 12, q.margins + 8, 48),
              itemCount: ayahs.length,
              separatorBuilder: (_, __) =>
                  SizedBox(height: q.ayahSpacing),
              itemBuilder: (context, i) {
                final a = ayahs[i];
                final num = switch (q.ayahNumberStyle) {
                  AyahNumberStyle.arabicIndic =>
                    toArabicIndic(a.ayah),
                  _ => '${a.ayah}',
                };
                final bookmarked = ref
                    .watch(libraryProvider)
                    .any((b) => b.refKey == a.key);
                final highlight = ref
                    .watch(highlightsProvider)
                    .where((h) => h.refKey == a.key)
                    .firstOrNull;
                final hasNote = ref
                    .watch(notesProvider)
                    .any((n) => n.refKey == a.key);
                return Container(
                  decoration: highlight == null
                      ? null
                      : BoxDecoration(
                          color: Color(highlight.colorValue)
                              .withValues(alpha: 0.28),
                          borderRadius:
                              BorderRadius.circular(12),
                        ),
                  padding: highlight == null
                      ? null
                      : const EdgeInsets.all(8),
                  child: InkWell(
                  key: _keys.putIfAbsent(
                      a.ayah, () => GlobalKey()),
                  borderRadius: BorderRadius.circular(12),
                  onTap: () =>
                      showAyahActionSheet(context, a),
                  child: Column(
                    crossAxisAlignment:
                        CrossAxisAlignment.stretch,
                    children: [
                      TajweedText(
                          ayah: a, fontSize: q.fontSize),
                      const SizedBox(height: 4),
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(
                                horizontal: 10, vertical: 4),
                            decoration: BoxDecoration(
                              border: Border.all(
                                  color: Theme.of(context)
                                      .colorScheme
                                      .outlineVariant),
                              borderRadius:
                                  BorderRadius.circular(20),
                            ),
                            child: Text('﴿$num﴾',
                                textDirection:
                                    TextDirection.rtl),
                          ),
                          const Spacer(),
                          if (hasNote)
                            const Icon(Icons.edit_note,
                                size: 18),
                          if (bookmarked)
                            const Icon(Icons.bookmark,
                                size: 18),
                        ],
                      ),
                      if (q.showTranslation &&
                          q.translations.isNotEmpty) ...[
                        const SizedBox(height: 6),
                        _AyahTranslations(
                            ayahKey: a.key,
                            translationIds: q.translations),
                      ],
                    ],
                  ),
                  ),
                );
              },
            );
          },
        ),
      ),
    );
  }
}

final _surahAyahsProvider = FutureProvider.family(
    (ref, (int, String) args) => ref
        .watch(quranRepositoryProvider)
        .ayahsOfSurah(args.$1, args.$2));

/// Translations for one ayah: translator credited, muted style —
/// visually distinct from Quran text per spec §13.
class _AyahTranslations extends ConsumerWidget {
  const _AyahTranslations(
      {required this.ayahKey, required this.translationIds});

  final String ayahKey;
  final List<String> translationIds;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: translationIds.map((id) {
        final meta = kTranslationCatalog
            .where((t) => t.id == id)
            .firstOrNull;
        final texts = ref.watch(translationTextsProvider(id));
        return texts.when(
          loading: () => const SizedBox.shrink(),
          error: (_, __) => const SizedBox.shrink(),
          data: (map) {
            final text = map[ayahKey];
            if (text == null) {
              return const SizedBox.shrink();
            }
            return Padding(
              padding: const EdgeInsets.only(top: 6),
              child: Column(
                crossAxisAlignment:
                    CrossAxisAlignment.stretch,
                children: [
                  Text(
                    '[${s.t('translationLabel')} · ${meta?.translator ?? id}]',
                    style:
                        Theme.of(context).textTheme.labelSmall,
                  ),
                  Text(text,
                      style: AppTheme.translation(context)),
                ],
              ),
            );
          },
        );
      }).toList(),
    );
  }
}
