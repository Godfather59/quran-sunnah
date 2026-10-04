import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../data/models/hadith.dart';
import '../../data/repositories/hadith_repository.dart';
import '../../data/repositories/verified_asset_hadith_repository.dart';
import '../../data/seed/hadith_collections.dart';
import '../../state/library_state.dart';
import '../../state/providers.dart';
import 'narrator_view_screen.dart';

/// Clean hadith card (§18): collection → book → chapter → sanad →
/// Arabic matn → translation → reference → grade. Reference always
/// visible. Placeholder used until verified dataset ships.
class HadithCardRef extends ConsumerWidget {
  const HadithCardRef({super.key, required this.id});

  final String id;

  @override
  Widget build(BuildContext context, WidgetRef ref) =>
      Text('${AppStrings.of(context).t('hadith')} $id');
}

class HadithPlaceholderCard extends StatelessWidget {
  const HadithPlaceholderCard({super.key});

  @override
  Widget build(BuildContext context) {
    return const HadithCard(hadith: null, missingMessage: null);
  }
}

class HadithCard extends ConsumerWidget {
  const HadithCard(
      {super.key, required this.hadith, required this.missingMessage});

  final Hadith? hadith;
  final String? missingMessage;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final app = ref.watch(appPrefsProvider);
    final h = hadith;
    final bookmarked = h != null &&
        ref
            .watch(libraryProvider)
            .any((b) => b.refKey == h.id);

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    h == null
                        ? (s.isArabic
                            ? 'صحيح البخاري'
                            : 'Sahih al-Bukhari · صحيح البخاري')
                        : (s.isArabic
                            ? '${_collectionAr(h.collectionId)} · ${s.t('hadith')} ${h.hadithNumber}'
                            : '${_collectionName(h.collectionId)} · Hadith ${h.hadithNumber}'),
                    style:
                        Theme.of(context).textTheme.labelLarge,
                  ),
                ),
                IconButton(
                  icon: Icon(bookmarked
                      ? Icons.bookmark
                      : Icons.bookmark_outline),
                  onPressed: h == null
                      ? null
                      : () => ref
                          .read(libraryProvider.notifier)
                          .toggleHadith(
                            h.id,
                            '${s.t('hadith')} ${h.hadithNumber}',
                          ),
                ),
              ],
            ),
            if (h != null) ...[
              if (h.bookAr.isNotEmpty)
                Text(h.bookAr,
                    textDirection: TextDirection.rtl,
                    style:
                        Theme.of(context).textTheme.bodySmall),
              if (!s.isArabic || h.bookAr.isEmpty)
                Text(
                  h.book,
                  style: Theme.of(context).textTheme.bodySmall,
                ),
              if (h.chapterAr.isNotEmpty &&
                  h.chapterAr != h.chapter)
                Text(h.chapterAr,
                    textDirection: TextDirection.rtl,
                    style:
                        Theme.of(context).textTheme.bodySmall),
              if ((!s.isArabic || h.chapterAr.isEmpty) &&
                  h.chapter.isNotEmpty &&
                  h.chapter != h.book)
                Text(
                  h.chapter,
                  style: Theme.of(context).textTheme.bodySmall,
                ),
            ] else ...[
              Text(
                s.isArabic
                    ? 'كتاب بدء الوحي'
                    : 'Book of Revelation · كتاب بدء الوحي',
                style: Theme.of(context).textTheme.bodySmall,
              ),
            ],
            const Divider(height: 24),
            if (h == null || h.isPlaceholder) ...[
              Text(s.t('contentUnavailable')),
              const SizedBox(height: 8),
              Text(
                s.t('arabicMatnPlaceholderHint'),
                style: const TextStyle(height: 1.6),
              ),
              const SizedBox(height: 12),
              Wrap(
                spacing: 8,
                children: [
                  OutlinedButton(
                    onPressed: () {},
                    child: Text(s.t('chainOfNarration')),
                  ),
                  OutlinedButton(
                    onPressed: () {},
                    child: Text(s.t('relatedNarrations')),
                  ),
                ],
              ),
            ] else ...[
              if (h.sanadAr != null && app.displaySanad)
                Text(h.sanadAr!,
                    textDirection: TextDirection.rtl),
              const SizedBox(height: 8),
              Text(h.matnAr,
                  textDirection: TextDirection.rtl,
                  style: const TextStyle(
                      fontSize: 20, height: 2.0)),
              if (h.matnTranslation != null) ...[
                const SizedBox(height: 8),
                Text(h.matnTranslation!),
              ],
              const SizedBox(height: 12),
              if (app.displayGrade) _GradeRow(hadith: h),
              if (h.narrator != null &&
                  h.narrator!.isNotEmpty)
                TextButton(
                  onPressed: () => Navigator.of(context)
                      .push(MaterialPageRoute(
                          builder: (_) =>
                              NarratorViewScreen(
                                  name: h.narrator!))),
                  child: Text(
                    '${s.t('narratorLabel')}: ${h.narrator} → '
                    '${s.t('profileLabel')}',
                  ),
                ),
            ],
          ],
        ),
      ),
    );
  }

  String _collectionName(String id) => switch (id) {
        'bukhari' => 'Sahih al-Bukhari',
        'muslim' => 'Sahih Muslim',
        _ => id,
      };

  String _collectionAr(String id) {
    for (final c in kHadithCollections) {
      if (c.id == id) {
        return c.nameAr;
      }
    }
    return id;
  }
}

class _GradeRow extends StatelessWidget {
  const _GradeRow({required this.hadith});

  final Hadith hadith;

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    return Container(
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(
        color: Theme.of(context)
            .colorScheme
            .surfaceContainerHighest,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        hadith.grade == null
            ? s.t('gradeUnavailable')
            : '${s.t('gradeLabel')}: ${hadith.grade}'
                '${hadith.gradingAuthority != null ? ' (${hadith.gradingAuthority})' : ''}',
        style: Theme.of(context).textTheme.bodySmall,
      ),
    );
  }
}

/// Book / chapter browser (§15) with real bundled sections.
class BookChapterBrowserScreen extends ConsumerStatefulWidget {
  const BookChapterBrowserScreen(
      {super.key, required this.collectionId});

  final String collectionId;

  @override
  ConsumerState<BookChapterBrowserScreen> createState() =>
      _BookChapterBrowserScreenState();
}

class _BookChapterBrowserScreenState
    extends ConsumerState<BookChapterBrowserScreen> {
  late String _collectionId;

  @override
  void initState() {
    super.initState();
    _collectionId = widget.collectionId;
  }

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    final repo = ref.watch(hadithRepositoryProvider);
    final verified =
        repo is VerifiedAssetHadithRepository ? repo : null;

    return Scaffold(
      appBar: AppBar(
        title: Text('${s.t('booksChapters')} · ${_collectionLabel(s, _collectionId)}'),
      ),
      body: verified == null
          ? Center(child: Text(s.t('contentUnavailable')))
          : Column(
              children: [
                Padding(
                  padding: const EdgeInsets.all(12),
                  child: SingleChildScrollView(
                    scrollDirection: Axis.horizontal,
                    child: SegmentedButton<String>(
                      segments: [
                        for (final c in kBundledHadithCollections
                            .where((c) => c.isDownloaded))
                          ButtonSegment(
                            value: c.id,
                            label: Text(s.isArabic ? c.nameAr : c.nameEn),
                          ),
                      ],
                      selected: {_collectionId},
                      onSelectionChanged: (v) => setState(
                          () => _collectionId = v.first),
                    ),
                  ),
                ),
                Expanded(
                  child: FutureBuilder(
                    future:
                        verified.sections(_collectionId),
                    builder: (context, snap) {
                      final secs = snap.data ?? [];
                      if (secs.isEmpty) {
                        return const Center(
                            child:
                                CircularProgressIndicator());
                      }
                      return ListView.builder(
                        padding: const EdgeInsets.all(12),
                        itemCount: secs.length,
                        itemBuilder: (context, i) {
                          final sec = secs[i];
                          return Card(
                            child: ExpansionTile(
                              title: Text(
                                s.isArabic
                                    ? '${s.t('book')} ${sec.section}'
                                    : '${sec.section}. ${sec.title}',
                              ),
                              subtitle: Text(
                                '${sec.count} ${s.t('hadithCountUnit')} · '
                                '${s.t('numbersLabel')} ${sec.first}–${sec.last}',
                              ),
                              children: [
                                _SectionHadiths(
                                    repo: verified,
                                    collectionId:
                                        _collectionId,
                                    section: sec.section),
                              ],
                            ),
                          );
                        },
                      );
                    },
                  ),
                ),
              ],
            ),
    );
  }

  String _collectionLabel(AppStrings s, String id) {
    final info = kBundledHadithCollections.where((c) => c.id == id).firstOrNull;
    if (info == null) return id;
    return s.isArabic ? info.nameAr : info.nameEn;
  }
}

class _SectionHadiths extends StatelessWidget {
  const _SectionHadiths(
      {required this.repo,
      required this.collectionId,
      required this.section});

  final VerifiedAssetHadithRepository repo;
  final String collectionId;
  final int section;

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: repo.sectionHadiths(section, collectionId),
      builder: (context, snap) {
        final items = snap.data ?? [];
        if (items.isEmpty) {
          return const Padding(
            padding: EdgeInsets.all(16),
            child: CircularProgressIndicator(),
          );
        }
        return Column(
          children: items
              .map((h) => Padding(
                    padding: const EdgeInsets.all(8),
                    child: HadithCard(
                        hadith: h,
                        missingMessage: null),
                  ))
              .toList(),
        );
      },
    );
  }
}
