import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/l10n/app_strings.dart';
import '../../core/theme/app_theme.dart';
import '../../core/utils/text_utils.dart';
import '../../core/widgets/common.dart';
import '../../data/models/quran.dart';
import '../../data/repositories/quran_metadata.dart';
import '../../data/repositories/quran_repository.dart';
import '../../state/providers.dart';
import 'widgets/ayah_action_sheet.dart';

/// Medina-page Mushaf mode backed by verified Hafs/Uthmani page metadata.
///
/// Page layout is intentionally unavailable for editions that do not ship
/// their own verified page mapping. We never reuse Hafs page boundaries for
/// another Riwaya and label them as if they were authoritative.
class MushafReaderScreen extends ConsumerStatefulWidget {
  const MushafReaderScreen({super.key, this.surah = 1, this.page = 1});

  final int surah;
  final int page;

  @override
  ConsumerState<MushafReaderScreen> createState() =>
      _MushafReaderScreenState();
}

class _MushafReaderScreenState extends ConsumerState<MushafReaderScreen> {
  late final PageController _ctrl;
  late int _page;
  Timer? _rememberDebounce;
  bool _surahJumpDone = false;

  @override
  void initState() {
    super.initState();
    // Explicit page navigation (Page tab) wins; otherwise resolve surah
    // to its first Medina page async once metadata loads.
    _page = widget.page.clamp(1, 604);
    _ctrl = PageController(initialPage: _page - 1);
    if (widget.page == 1 && widget.surah > 1) {
      WidgetsBinding.instance.addPostFrameCallback((_) => _jumpToSurah());
    } else {
      _surahJumpDone = true;
    }
  }

  Future<void> _jumpToSurah() async {
    if (_surahJumpDone || !mounted) return;
    try {
      final meta = await ref.read(quranMetadataProvider.future);
      if (!mounted || _surahJumpDone) return;
      final p = meta.pageOf(widget.surah, 1).clamp(1, 604);
      _surahJumpDone = true;
      if (p != _page && _ctrl.hasClients) {
        await _ctrl.jumpToPage(p - 1);
      }
      setState(() => _page = p);
    } catch (_) {
      _surahJumpDone = true;
    }
  }

  @override
  void dispose() {
    _rememberDebounce?.cancel();
    _ctrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    final q = ref.watch(quranPrefsProvider);
    final metaAsync = ref.watch(quranMetadataProvider);
    final hasVerifiedPageMap = q.editionId == 'hafs-an-asim__uthmani';
    final headerAyahsAsync = hasVerifiedPageMap
        ? ref.watch(_pageAyahsProvider((_page, q.editionId)))
        : null;

    return Scaffold(
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('${s.t('mushaf')} · ${s.t('page')} $_page'),
            if (headerAyahsAsync != null)
              headerAyahsAsync.when(
                loading: () => const SizedBox.shrink(),
                error: (_, __) => const SizedBox.shrink(),
                data: (ayahs) {
                  if (ayahs.isEmpty) return const SizedBox.shrink();
                  final first = ayahs.first;
                  final hasSajda = ayahs.any((a) => a.isSajda);
                  final parts = [
                    if (first.juz != null) '${s.t('juz')} ${first.juz}',
                    if (first.hizb != null) '${s.t('hizb')} ${first.hizb}',
                    if (hasSajda) '۩ ${s.t('sajda')}',
                  ];
                  if (parts.isEmpty) return const SizedBox.shrink();
                  return Text(
                    parts.join(' · '),
                    style: Theme.of(context).textTheme.labelSmall,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  );
                },
              ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.find_in_page_outlined),
            tooltip: s.t('goToPage'),
            onPressed:
                hasVerifiedPageMap ? () => _jumpToPage(context) : null,
          ),
        ],
      ),
      body: !hasVerifiedPageMap
          ? Center(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: UnavailableBanner(
                  message: s.t('mushafPageMapUnavailable'),
                ),
              ),
            )
          : metaAsync.when(
              loading: () =>
                  const Center(child: CircularProgressIndicator()),
              error: (e, _) => Center(child: Text('$e')),
              data: (meta) => Column(
                children: [
                  Expanded(
                    child: PageView.builder(
                      controller: _ctrl,
                      reverse: true,
                      onPageChanged: (i) {
                        final page = i + 1;
                        setState(() => _page = page);
                        _rememberPage(meta, page);
                      },
                      itemCount: meta.pageStarts.length,
                      itemBuilder: (context, i) => _MushafPage(
                        page: i + 1,
                        editionId: q.editionId,
                      ),
                    ),
                  ),
                  SafeArea(
                    top: false,
                    child: Padding(
                      padding: const EdgeInsets.fromLTRB(12, 4, 12, 8),
                      child: Row(
                        children: [
                          IconButton(
                            tooltip: s.t('previousPage'),
                            onPressed: _page <= 1
                                ? null
                                : () => _ctrl.previousPage(
                                      duration:
                                          const Duration(milliseconds: 220),
                                      curve: Curves.easeOut,
                                    ),
                            icon: const Icon(Icons.chevron_right),
                          ),
                          Expanded(
                            child: Semantics(
                              label:
                                  '${s.t('page')} $_page ${s.t('of')} ${meta.pageStarts.length}',
                              child: Text(
                                '${s.t('page')} $_page / ${meta.pageStarts.length}',
                                textAlign: TextAlign.center,
                                style: Theme.of(context).textTheme.labelLarge,
                              ),
                            ),
                          ),
                          IconButton(
                            tooltip: s.t('nextPage'),
                            onPressed: _page >= meta.pageStarts.length
                                ? null
                                : () => _ctrl.nextPage(
                                      duration:
                                          const Duration(milliseconds: 220),
                                      curve: Curves.easeOut,
                                    ),
                            icon: const Icon(Icons.chevron_left),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
    );
  }

  Future<void> _rememberPage(QuranMetadata meta, int page) async {
    if (page < 1 || page > meta.pageStarts.length) return;
    _rememberDebounce?.cancel();
    _rememberDebounce = Timer(const Duration(milliseconds: 500), () async {
      if (!mounted) return;
      final start = meta.pageStarts[page - 1];
      final prefs = ref.read(quranPrefsProvider);
      // Skip if already at same position to avoid redundant writes.
      if (prefs.lastSurah == start.surah && prefs.lastAyah == start.ayah) {
        return;
      }
      await ref.read(quranPrefsProvider.notifier).update(
            prefs.copyWith(
              lastSurah: start.surah,
              lastAyah: start.ayah,
              readingMode: ReadingMode.mushaf,
            ),
          );
    });
  }

  Future<void> _jumpToPage(BuildContext context) async {
    final s = AppStrings.of(context);
    final ctrl = TextEditingController(text: '$_page');
    final v = await showDialog<int>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(s.t('goToPage')),
        content: TextField(
          controller: ctrl,
          keyboardType: TextInputType.number,
          decoration: InputDecoration(
            hintText: '1–604',
            labelText: s.t('page'),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: Text(s.t('cancel')),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(ctx, int.tryParse(ctrl.text)),
            child: Text(s.t('go')),
          ),
        ],
      ),
    );
    ctrl.dispose();
    if (v != null && v >= 1 && v <= 604) {
      await _ctrl.animateToPage(
        v - 1,
        duration: const Duration(milliseconds: 250),
        curve: Curves.easeOut,
      );
    }
  }
}

class _MushafPage extends ConsumerWidget {
  const _MushafPage({
    required this.page,
    required this.editionId,
  });

  final int page;
  final String editionId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final q = ref.watch(quranPrefsProvider);
    final ayahsAsync = ref.watch(_pageAyahsProvider((page, editionId)));

    return Padding(
      padding: const EdgeInsets.all(10),
      child: Card(
        clipBehavior: Clip.antiAlias,
        child: Padding(
          padding: EdgeInsets.all(q.margins + 6),
          child: ayahsAsync.when(
            loading: () =>
                const Center(child: CircularProgressIndicator()),
            error: (e, _) => Center(child: Text('$e')),
            data: (ayahs) {
              if (ayahs.isEmpty) {
                return Center(
                  child: UnavailableBanner(
                    message: s.t('contentUnavailable'),
                  ),
                );
              }
              final first = ayahs.first;
              final hasSajda = ayahs.any((a) => a.isSajda);
              return Column(
                children: [
                  Wrap(
                    alignment: WrapAlignment.center,
                    spacing: 8,
                    runSpacing: 4,
                    children: [
                      _MetaChip(
                        label: s.t('juz'),
                        value: first.juz?.toString() ?? '—',
                      ),
                      _MetaChip(
                        label: s.t('hizb'),
                        value: first.hizb?.toString() ?? '—',
                      ),
                      _MetaChip(
                        label: s.t('rub'),
                        value: first.rub?.toString() ?? '—',
                      ),
                      if (hasSajda)
                        Chip(
                          avatar: const Text('۩'),
                          label: Text(s.t('sajda')),
                          visualDensity: VisualDensity.compact,
                        ),
                    ],
                  ),
                  const Divider(),
                  Expanded(
                    child: SingleChildScrollView(
                      child: Wrap(
                        textDirection: TextDirection.rtl,
                        children: ayahs
                            .map((a) => _PageAyah(
                                  ayah: a,
                                  prefs: q,
                                ))
                            .toList(growable: false),
                      ),
                    ),
                  ),
                ],
              );
            },
          ),
        ),
      ),
    );
  }
}

class _MetaChip extends StatelessWidget {
  const _MetaChip({required this.label, required this.value});
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) => Chip(
        label: Text('$label $value'),
        visualDensity: VisualDensity.compact,
      );
}

class _PageAyah extends StatelessWidget {
  const _PageAyah({
    required this.ayah,
    required this.prefs,
  });

  final Ayah ayah;
  final QuranPrefs prefs;

  @override
  Widget build(BuildContext context) {
    final num = switch (prefs.ayahNumberStyle) {
      AyahNumberStyle.arabicIndic ||
      AyahNumberStyle.easternArabic =>
        toArabicIndic(ayah.displayAyahNumber),
      AyahNumberStyle.latin => '${ayah.displayAyahNumber}',
    };
    return InkWell(
      onTap: () => showAyahActionSheet(context, ayah),
      onLongPress: () {
        HapticFeedback.lightImpact();
        showAyahActionSheet(context, ayah);
      },
      child: Semantics(
        button: true,
        label: 'Ayah ${ayah.surah}:${ayah.displayAyahNumber}',
        child: RichText(
          textDirection: TextDirection.rtl,
          textAlign: TextAlign.right,
          text: TextSpan(
            style: AppTheme.quranArabic(
              context,
              size: prefs.fontSize,
              height: prefs.lineHeight,
            ),
            children: [
              TextSpan(text: '${ayah.text} '),
              TextSpan(
                text: '﴿$num﴾',
                style: TextStyle(
                  color: Theme.of(context).colorScheme.primary,
                  fontSize: prefs.fontSize * 0.8,
                ),
              ),
              if (ayah.isSajda)
                TextSpan(
                  text: ' ۩ ',
                  style: TextStyle(
                    color: Theme.of(context).colorScheme.tertiary,
                    fontSize: prefs.fontSize * 0.85,
                  ),
                )
              else
                const TextSpan(text: ' '),
            ],
          ),
        ),
      ),
    );
  }
}

final _pageAyahsProvider = FutureProvider.family(
  (ref, (int, String) args) => ref
      .watch(quranRepositoryProvider)
      .ayahsOfPage(args.$1, args.$2),
);
