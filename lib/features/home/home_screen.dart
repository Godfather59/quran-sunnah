import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../core/theme/app_theme.dart';
import '../../core/widgets/common.dart';
import '../../data/models/library.dart';
import '../../data/repositories/hadith_repository.dart';
import '../../data/repositories/quran_repository.dart';
import '../../data/repositories/verified_asset_hadith_repository.dart';
import '../../data/seed/riwayat_catalog.dart';
import '../../data/seed/surah_metadata.dart';
import '../../state/download_state.dart';
import '../../state/home_prefs.dart';
import '../../state/library_state.dart';
import '../../state/providers.dart';
import '../quran/audio_player_screen.dart';
import '../quran/quran_reader_screen.dart';
import '../quran/surah_list_screen.dart';
import '../search/global_search_screen.dart';
import '../sunnah/sunnah_home_screen.dart';

/// Personalized dashboard (§4): hero continue-reading, quick actions,
/// REAL daily ayah/hadith from bundled datasets, library stats.
/// Sections hideable + reorderable via Customize.
class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final layout = ref.watch(homeLayoutProvider);
    final locale = ref.watch(appPrefsProvider).locale;

    return Scaffold(
      appBar: AppBar(
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(s.t('appTitle')),
            Text(
              MaterialLocalizations.of(context)
                  .formatMediumDate(DateTime.now()),
              style: Theme.of(context).textTheme.labelSmall,
            ),
          ],
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            tooltip: s.t('search'),
            onPressed: () => Navigator.of(context).push(
                MaterialPageRoute(
                    builder: (_) =>
                        const GlobalSearchScreen())),
          ),
          IconButton(
            icon: const Icon(Icons.dashboard_customize_outlined),
            tooltip: s.t('customizeHome'),
            onPressed: () => _customize(context, ref, locale),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
        children: [
          for (final section in layout.visible) ...[
            _section(context, ref, section),
          ],
        ],
      ),
    );
  }

  Widget _section(
      BuildContext context, WidgetRef ref, HomeSection section) {
    return switch (section) {
      HomeSection.continueReading => const _ContinueReadingHero(),
      HomeSection.quickActions => const _QuickActions(),
      HomeSection.dailyAyah => const _DailyAyah(),
      HomeSection.dailyHadith => const _DailyHadith(),
      HomeSection.bookmarks => const _BookmarksPreview(),
      HomeSection.recent => const _RecentPreview(),
      HomeSection.stats => const _LibraryStats(),
    };
  }

  void _customize(
      BuildContext context, WidgetRef ref, String locale) {
    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      isScrollControlled: true,
      builder: (ctx) => const _CustomizeSheet(),
    );
  }
}

/// Hero: last position + progress within surah + riwaya chip.
class _ContinueReadingHero extends ConsumerWidget {
  const _ContinueReadingHero();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final q = ref.watch(quranPrefsProvider);
    final riwaya =
        kRiwayaCatalog.firstWhere((r) => r.id == q.riwaya);
    final meta =
        kSurahMetadata.firstWhere((m) => m.number == q.lastSurah);
    final progress = (q.lastAyah / meta.ayahCount).clamp(0.0, 1.0);
    final scheme = Theme.of(context).colorScheme;

    return Card(
      color: scheme.primaryContainer,
      child: InkWell(
        borderRadius: BorderRadius.circular(20),
        onTap: () => Navigator.of(context).push(MaterialPageRoute(
            builder: (_) => QuranReaderScreen(
                surah: q.lastSurah,
                initialAyah: q.lastAyah))),
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(Icons.menu_book,
                      color: scheme.onPrimaryContainer),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      s.t('continueReading'),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: Theme.of(context)
                          .textTheme
                          .titleMedium
                          ?.copyWith(
                              color: scheme.onPrimaryContainer,
                              fontWeight: FontWeight.w700),
                    ),
                  ),
                  const SizedBox(width: 8),
                  const Icon(Icons.arrow_forward),
                ],
              ),
              const SizedBox(height: 12),
              Text(
                s.isArabic
                    ? meta.nameAr
                    : s.locale.languageCode == 'fr'
                        ? '${meta.nameAr} · ${meta.nameFr}'
                        : '${meta.nameAr} · ${meta.nameEn}',
                textDirection: TextDirection.rtl,
                style: Theme.of(context)
                    .textTheme
                    .headlineSmall
                    ?.copyWith(
                        color: scheme.onPrimaryContainer),
              ),
              const SizedBox(height: 4),
              Text(
                '${s.t('ayahLabel')} ${q.lastAyah} ${s.t('of')} ${meta.ayahCount} · ${(progress * 100).toStringAsFixed(0)}%',
                style: Theme.of(context)
                    .textTheme
                    .bodySmall
                    ?.copyWith(
                        color: scheme.onPrimaryContainer),
              ),
              const SizedBox(height: 10),
              ClipRRect(
                borderRadius: BorderRadius.circular(8),
                child: LinearProgressIndicator(
                    value: progress,
                    minHeight: 6,
                    backgroundColor: scheme.onPrimaryContainer
                        .withValues(alpha: 0.2)),
              ),
              const SizedBox(height: 10),
              Chip(
                label: Text(
                  '${s.t('riwaya')}: ${s.isArabic ? riwaya.riwayaAr : riwaya.riwayaEn}',
                ),
                visualDensity: VisualDensity.compact,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _QuickActions extends ConsumerWidget {
  const _QuickActions();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final actions = [
      (Icons.menu_book_outlined, s.t('quickQuran'),
          const SurahListScreen()),
      (Icons.auto_stories_outlined, s.t('quickSunnah'),
          const SunnahHomeScreen()),
      (Icons.headphones_outlined, s.t('quickAudio'),
          const AudioPlayerScreen()),
      (Icons.search, s.t('search'), const GlobalSearchScreen()),
    ];
    return Padding(
      padding: const EdgeInsets.only(top: 16),
      child: Row(
        children: actions
            .map((a) => Expanded(
                  child: InkWell(
                    borderRadius: BorderRadius.circular(16),
                    onTap: () => Navigator.of(context).push(
                        MaterialPageRoute(builder: (_) => a.$3)),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 4, vertical: 8),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          CircleAvatar(
                            radius: 24,
                            child: Icon(a.$1),
                          ),
                          const SizedBox(height: 6),
                          Text(
                            a.$2,
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                            textAlign: TextAlign.center,
                            style: Theme.of(context)
                                .textTheme
                                .labelMedium,
                          ),
                        ],
                      ),
                    ),
                  ),
                ))
            .toList(),
      ),
    );
  }
}

/// Deterministic daily ayah from the bundled verified text.
class _DailyAyah extends ConsumerWidget {
  const _DailyAyah();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final q = ref.watch(quranPrefsProvider);
    final now = DateTime.now();
    final dayIndex =
        DateTime(now.year, now.month, now.day)
                .difference(DateTime(now.year, 1, 1))
                .inDays %
            6236;
    var acc = 0;
    var surah = 1;
    var ayah = 1;
    for (final m in kSurahMetadata) {
      if (dayIndex < acc + m.ayahCount) {
        surah = m.number;
        ayah = dayIndex - acc + 1;
        break;
      }
      acc += m.ayahCount;
    }

    final ayahsAsync =
        ref.watch(_dailyAyahProvider((surah, ayah, q.editionId)));

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SectionHeader(title: s.t('dailyAyah')),
        Card(
          child: InkWell(
            borderRadius: BorderRadius.circular(20),
            onTap: () => Navigator.of(context).push(
                MaterialPageRoute(
                    builder: (_) => QuranReaderScreen(
                        surah: surah,
                        initialAyah: ayah))),
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: ayahsAsync.when(
                loading: () => const Center(
                    child: CircularProgressIndicator()),
                error: (e, _) => Text('$e'),
                data: (list) {
                  final match = list
                      .where((a) =>
                          a.surah == surah &&
                          a.ayah == ayah &&
                          !a.isPlaceholder)
                      .firstOrNull;
                  if (match == null) {
                    return Text(s.t('contentUnavailable'));
                  }
                  return Column(
                    crossAxisAlignment:
                        CrossAxisAlignment.stretch,
                    children: [
                      Text(match.text,
                          textDirection:
                              TextDirection.rtl,
                          textAlign: TextAlign.right,
                          style: AppTheme.quranArabic(
                              context,
                              size: 22)),
                      const SizedBox(height: 8),
                      Text(
                        '${s.t('surah')} $surah · ${s.t('ayahLabel')} $ayah',
                        style: Theme.of(context).textTheme.labelSmall,
                      ),
                    ],
                  );
                },
              ),
            ),
          ),
        ),
      ],
    );
  }
}

final _dailyAyahProvider = FutureProvider.family(
    (ref, (int, int, String) args) => ref
        .watch(quranRepositoryProvider)
        .ayahsOfSurah(args.$1, args.$3));

/// Deterministic daily hadith from bundled Bukhari.
class _DailyHadith extends ConsumerWidget {
  const _DailyHadith();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final repo = ref.watch(hadithRepositoryProvider);
    final verified =
        repo is VerifiedAssetHadithRepository ? repo : null;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SectionHeader(title: s.t('dailyHadith')),
        Card(
          child: Padding(
            padding: const EdgeInsets.all(18),
            child: verified == null
                ? Text(s.t('contentUnavailable'))
                : FutureBuilder(
                    future: verified.allBukhari(),
                    builder: (context, snap) {
                      final all = (snap.data ?? [])
                          .where((h) => !h.isPlaceholder)
                          .toList();
                      if (all.isEmpty) {
                        return Text(
                            s.t('contentUnavailable'));
                      }
                      final now = DateTime.now();
                      final h = all[DateTime(now.year,
                                  now.month, now.day)
                              .difference(DateTime(
                                  now.year, 1, 1))
                              .inDays %
                          all.length];
                      return Column(
                        crossAxisAlignment:
                            CrossAxisAlignment.start,
                        children: [
                          Text(h.matnAr,
                              textDirection:
                                  TextDirection.rtl,
                              maxLines: 4,
                              overflow:
                                  TextOverflow.ellipsis,
                              style: const TextStyle(
                                  fontSize: 17,
                                  height: 1.9)),
                          const SizedBox(height: 8),
                          Text(
                            s.isArabic
                                ? 'صحيح البخاري · ${s.t('hadith')} ${h.hadithNumber}'
                                : 'Sahih al-Bukhari · ${s.t('hadith')} ${h.hadithNumber}',
                            style: Theme.of(context).textTheme.labelSmall,
                          ),
                        ],
                      );
                    },
                  ),
          ),
        ),
      ],
    );
  }
}

class _BookmarksPreview extends ConsumerWidget {
  const _BookmarksPreview();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final bookmarks = ref.watch(libraryProvider);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SectionHeader(
            title: s.t('statBookmarks'),
            action: '${bookmarks.length}'),
        if (bookmarks.isEmpty)
          Text(s.t('noBookmarksYet'),
              style: Theme.of(context).textTheme.bodySmall)
        else
          ...bookmarks.take(3).map((b) => ListTile(
                contentPadding: EdgeInsets.zero,
                leading: Icon(b.kind == BookmarkKind.ayah
                    ? Icons.bookmark
                    : Icons.auto_stories),
                title: Text(b.title),
                subtitle: Text(b.subtitle),
              )),
      ],
    );
  }
}

class _RecentPreview extends ConsumerWidget {
  const _RecentPreview();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final recent = ref.watch(recentProvider);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SectionHeader(title: s.t('recentlyViewed')),
        ...recent.map((r) => ListTile(
              contentPadding: EdgeInsets.zero,
              leading: const Icon(Icons.history),
              title: Text(r.title),
              subtitle: Text(r.subtitle),
            )),
      ],
    );
  }
}

class _LibraryStats extends ConsumerWidget {
  const _LibraryStats();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final bookmarks = ref.watch(libraryProvider);
    final dl = ref.watch(downloadProvider);
    final collections = ref.watch(collectionsProvider);
    final stats = [
      (Icons.bookmark, '${bookmarks.length}',
          s.t('statBookmarks')),
      (Icons.download_done, '${dl.installed.length}',
          s.t('statDownloaded')),
      (Icons.folder, '${collections.length}',
          s.t('statCollections')),
    ];
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SectionHeader(title: s.t('atAGlance')),
        if (MediaQuery.textScalerOf(context).scale(1) >= 1.5)
          ...stats.map(
            (e) => Card(
              child: ListTile(
                leading: Icon(e.$1),
                title: Text(e.$3),
                trailing: Text(
                  e.$2,
                  style: Theme.of(context).textTheme.titleLarge,
                ),
              ),
            ),
          )
        else
          Row(
            children: stats
                .map((e) => Expanded(
                        child: Card(
                      child: Padding(
                        padding: const EdgeInsets.symmetric(vertical: 16),
                        child: Column(
                          children: [
                            Icon(e.$1),
                            const SizedBox(height: 4),
                            Text(e.$2,
                                style: Theme.of(context)
                                    .textTheme
                                    .titleLarge),
                            Text(
                              e.$3,
                              textAlign: TextAlign.center,
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                              style: Theme.of(context)
                                  .textTheme
                                  .labelSmall,
                            ),
                          ],
                        ),
                      ),
                    )))
                .toList(),
          ),
      ],
    );
  }
}

class _CustomizeSheet extends ConsumerWidget {
  const _CustomizeSheet();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final layout = ref.watch(homeLayoutProvider);
    final locale = ref.watch(appPrefsProvider).locale;
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(s.t('customizeHome'),
                style: Theme.of(context)
                    .textTheme
                    .titleLarge),
            const SizedBox(height: 4),
            Text(s.t('customizeHint')),
            const SizedBox(height: 12),
            Flexible(
              child: ReorderableListView.builder(
                shrinkWrap: true,
                itemCount: layout.order.length,
                onReorderItem: (oldIndex, newIndex) => ref
                    .read(homeLayoutProvider.notifier)
                    .reorder(oldIndex, newIndex),
                itemBuilder: (context, i) {
                  final section = layout.order[i];
                  final hidden =
                      layout.hidden.contains(section);
                  return ListTile(
                    key: ValueKey(section),
                    leading: const Icon(Icons.drag_handle),
                    title: Text(
                        section.label(locale),
                        style: TextStyle(
                            color: hidden
                                ? Theme.of(context)
                                    .disabledColor
                                : null)),
                    trailing: IconButton(
                      icon: Icon(hidden
                          ? Icons.visibility_off
                          : Icons.visibility),
                      onPressed: () => ref
                          .read(homeLayoutProvider
                              .notifier)
                          .toggleHidden(section),
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
