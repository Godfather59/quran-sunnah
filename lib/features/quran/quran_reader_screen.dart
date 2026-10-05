import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
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
import '../../state/memorization_provider.dart';
import '../../state/khatma_provider.dart';
import '../../state/providers.dart';
import 'mushaf_reader_screen.dart';
import 'riwaya_selector_screen.dart';
import 'compare_riwayat_screen.dart';
import 'tafsir_screen.dart';
import 'widgets/ayah_action_sheet.dart';
import 'widgets/tajweed_text.dart';
import 'widgets/word_tap_text.dart';

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
  bool _hideMode = false;
  final Set<String> _revealed = {};
  final Map<int, GlobalKey> _keys = {};

  @override
  void initState() {
    super.initState();
    // Streak + last-read persistence (fire-and-forget).
    Future.microtask(() async {
      try {
        ref.read(streakProvider.notifier).touchToday();
        final q = ref.read(quranPrefsProvider);
        // Update last position if navigating to a new surah explicitly.
        if (q.lastSurah != widget.surah) {
          await ref.read(quranPrefsProvider.notifier).update(
                q.copyWith(
                    lastSurah: widget.surah,
                    lastAyah: widget.initialAyah.clamp(1, 300)),
              );
        }
      } catch (_) {}
    });
  }

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
    // Hoisted: watch once per build, not per ayah row (perf).
    final bookmarkKeys = {
      for (final b in ref.watch(libraryProvider)) b.refKey
    };
    final highlightsByKey = {
      for (final h in ref.watch(highlightsProvider)) h.refKey: h
    };
    final noteKeys = {
      for (final n in ref.watch(notesProvider)) n.refKey
    };
    final memorized = ref.watch(memorizationProvider);

    return Scaffold(
      appBar: _fullscreen
          ? null
          : AppBar(
              title: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(meta.nameAr, textDirection: TextDirection.rtl),
                  GestureDetector(
                    onTap: () => _showEditionSheet(context, s),
                    child: Text(
                      '${riwaya.riwayaAr} · ${_scriptLabel(s, q.datasetScript)} ▾',
                      style:
                          Theme.of(context).textTheme.labelSmall,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
              actions: [
                if (q.showTajweed && q.tajweedAvailable)
                  IconButton(
                    tooltip: s.t('tajweedLegend'),
                    icon: const Icon(Icons.palette_outlined),
                    onPressed: () => showModalBottomSheet(
                        context: context,
                        showDragHandle: true,
                        builder: (_) =>
                            TajweedLegendSheet()),
                  ),
                IconButton(
                  tooltip: s.t('mushafReading'),
                  icon: Icon(q.readingMode.name == 'reading'
                      ? Icons.auto_stories
                      : Icons.view_agenda),
                  onPressed: () => Navigator.of(context).pushReplacement(
                      MaterialPageRoute(
                          builder: (_) =>
                              MushafReaderScreen(surah: widget.surah))),
                ),
                IconButton(
                  tooltip: _hideMode ? '✓' : '◉',
                  icon: Icon(_hideMode
                      ? Icons.visibility_off
                      : Icons.visibility_outlined),
                  onPressed: () => setState(() {
                    _hideMode = !_hideMode;
                    if (!_hideMode) _revealed.clear();
                  }),
                ),
                IconButton(
                  tooltip: s.t('fullscreen'),
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
                ayahs.every((a) => a.isPlaceholder)) {
              return ListView(
                padding: EdgeInsets.all(q.margins + 8),
                children: [
                  UnavailableBanner(
                      message: s.t('contentUnavailable')),
                  const SizedBox(height: 8),
                  Text(
                    'Edition: ${q.editionId}\nSource: ${riwaya.source} v${riwaya.datasetVersion}\n'
                    '${s.t('verifiedDatasetRequired')}',
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
                        child: Text(s.t('tafsir')),
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
                  AyahNumberStyle.arabicIndic ||
                  AyahNumberStyle.easternArabic =>
                    toArabicIndic(a.displayAyahNumber),
                  AyahNumberStyle.latin => '${a.displayAyahNumber}',
                };
                final bookmarked = bookmarkKeys.contains(a.key);
                final highlight = highlightsByKey[a.key];
                final hasNote = noteKeys.contains(a.key);
                final isMemorized = memorized.contains(
                    '${a.canonicalSurahNumber}:${a.canonicalAyahNumber}');
                final hidden =
                    _hideMode && !_revealed.contains(a.key);
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
                  child: Column(
                    crossAxisAlignment:
                        CrossAxisAlignment.stretch,
                    children: [
                      // Tap-only-Arabic opens sheet (translation selectable).
                      // Hide mode: tap reveals, long-press opens sheet.
                      InkWell(
                        key: _keys.putIfAbsent(
                            a.canonicalAyahNumber, () => GlobalKey()),
                        borderRadius: BorderRadius.circular(12),
                        onTap: () {
                          if (hidden) {
                            setState(() => _revealed.add(a.key));
                          } else {
                            showAyahActionSheet(context, a);
                          }
                        },
                        onLongPress: () {
                          HapticFeedback.lightImpact();
                          showAyahActionSheet(context, a);
                        },
                        child: Padding(
                          padding: const EdgeInsets.symmetric(
                              vertical: 8),
                          child: Column(
                            crossAxisAlignment:
                                CrossAxisAlignment.stretch,
                            children: [
                              if (hidden)
                                GestureDetector(
                                  onTap: () => setState(
                                      () => _revealed.add(a.key)),
                                  child: ImageFiltered(
                                    imageFilter: ImageFilter.blur(
                                        sigmaX: 8, sigmaY: 8),
                                    child: WordTapAyahText(
                                        ayah: a,
                                        fontSize: q.fontSize),
                                  ),
                                )
                              else
                                WordTapAyahText(
                                    ayah: a, fontSize: q.fontSize),
                              const SizedBox(height: 4),
                              Row(
                                children: [
                                  Container(
                                    constraints: const BoxConstraints(
                                        minHeight: 48),
                                    alignment: Alignment.center,
                                    padding:
                                        const EdgeInsets.symmetric(
                                            horizontal: 10,
                                            vertical: 4),
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
                                  if (a.isSajda)
                                    Semantics(
                                      label: s.t('sajda'),
                                      child: Padding(
                                        padding:
                                            const EdgeInsets.all(12),
                                        child: Text(
                                          '۩',
                                          style: TextStyle(
                                            color: Theme.of(context)
                                                .colorScheme
                                                .tertiary,
                                            fontSize: 22,
                                          ),
                                        ),
                                      ),
                                    ),
                                  if (hasNote)
                                    const Padding(
                                      padding: EdgeInsets.all(12),
                                      child: Icon(Icons.edit_note,
                                          size: 22),
                                    ),
                                  if (bookmarked)
                                    const Padding(
                                      padding: EdgeInsets.all(12),
                                      child: Icon(Icons.bookmark,
                                          size: 22),
                                    ),
                                  InkWell(
                                    borderRadius:
                                        BorderRadius.circular(24),
                                    onTap: () => ref
                                        .read(memorizationProvider
                                            .notifier)
                                        .toggle(
                                            a.canonicalSurahNumber,
                                            a.canonicalAyahNumber),
                                    child: Padding(
                                      padding:
                                          const EdgeInsets.all(12),
                                      child: Icon(
                                        isMemorized
                                            ? Icons.check_circle
                                            : Icons
                                                .check_circle_outline,
                                        size: 22,
                                        color: isMemorized
                                            ? Theme.of(context)
                                                .colorScheme
                                                .primary
                                            : null,
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ),
                      ),
                      if (q.showTranslation &&
                          q.translations.isNotEmpty) ...[
                        const Divider(height: 16),
                        _AyahTranslations(
                            ayahKey: a.key,
                            translationIds: q.translations),
                      ],
                    ],
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
                  SelectableText(text,
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

String _scriptLabel(AppStrings s, QuranScript script) => switch (script) {
      QuranScript.uthmani => s.t('uthmani'),
      QuranScript.imlai => s.t('imlai'),
      QuranScript.indopak => s.t('indopak'),
      QuranScript.tajweed => s.t('uthmani'),
    };

void _showEditionSheet(BuildContext context, AppStrings s) {
  showModalBottomSheet(
    context: context,
    showDragHandle: true,
    builder: (ctx) => SafeArea(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          ListTile(
            leading: const Icon(Icons.menu_book_outlined),
            title: Text(s.t('riwaya')),
            trailing: const Icon(Icons.chevron_right),
            onTap: () {
              Navigator.pop(ctx);
              Navigator.of(context).push(MaterialPageRoute(
                  builder: (_) => const RiwayaSelectorScreen()));
            },
          ),
          ListTile(
            leading: const Icon(Icons.font_download_outlined),
            title: Text(s.t('quranScript')),
            trailing: const Icon(Icons.chevron_right),
            onTap: () {
              Navigator.pop(ctx);
              Navigator.of(context).push(MaterialPageRoute(
                  builder: (_) => const ScriptSelectorScreen()));
            },
          ),
          const SizedBox(height: 8),
        ],
      ),
    ),
  );
}
