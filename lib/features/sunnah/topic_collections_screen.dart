import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/l10n/app_strings.dart';
import '../../data/models/hadith.dart';
import '../../data/repositories/hadith_repository.dart';
import '../../data/repositories/verified_asset_hadith_repository.dart';
import 'hadith_reader_screen.dart';

/// Topic collections (§15): every book/section of the bundled Hadith
/// editions is browsable as its own topic collection. Titles and counts
/// come VERBATIM from each collection's verified `index.json` — topics
/// are never invented or merged across collections.
class TopicCollectionsScreen extends ConsumerStatefulWidget {
  const TopicCollectionsScreen({super.key});

  @override
  ConsumerState<TopicCollectionsScreen> createState() =>
      _TopicCollectionsScreenState();
}

class _TopicCollectionsScreenState
    extends ConsumerState<TopicCollectionsScreen> {
  final _ctrl = TextEditingController();
  String _query = '';
  late final Future<List<HadithCollection>> _collectionsFuture;

  @override
  void initState() {
    super.initState();
    final repo = ref.read(hadithRepositoryProvider);
    final verified =
        repo is VerifiedAssetHadithRepository ? repo : null;
    _collectionsFuture =
        verified == null ? Future.value(const []) : verified.collections();
  }

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(s.t('tabTopic'))),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(12),
            child: SearchBar(
              controller: _ctrl,
              hintText: s.t('searchHint'),
              leading: const Icon(Icons.search),
              trailing: [
                if (_query.isNotEmpty)
                  IconButton(
                    icon: const Icon(Icons.clear),
                    onPressed: () => setState(() {
                      _ctrl.clear();
                      _query = '';
                    }),
                  ),
              ],
              onChanged: (v) =>
                  setState(() => _query = v.trim().toLowerCase()),
            ),
          ),
          Expanded(
            child: FutureBuilder<List<HadithCollection>>(
              future: _collectionsFuture,
              builder: (context, snap) {
                final all = (snap.data ?? [])
                    .where((c) => c.isDownloaded)
                    .toList();
                if (snap.connectionState == ConnectionState.waiting) {
                  return const Center(
                      child: CircularProgressIndicator());
                }
                if (all.isEmpty) {
                  return Center(
                      child: Text(s.t('contentUnavailable')));
                }
                return _TopicList(query: _query, collections: all);
              },
            ),
          ),
        ],
      ),
    );
  }
}

class _TopicList extends ConsumerWidget {
  const _TopicList({required this.query, required this.collections});

  final String query;
  final List<HadithCollection> collections;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final repo = ref.watch(hadithRepositoryProvider);
    final verified =
        repo is VerifiedAssetHadithRepository ? repo : null;
    if (verified == null) {
      return Center(
          child: Text(AppStrings.of(context).t('contentUnavailable')));
    }
    return ListView(
      padding: const EdgeInsets.fromLTRB(12, 0, 12, 24),
      children: [
        for (final c in collections)
          _CollectionTopics(
            key: ValueKey(c.id),
            verified: verified,
            collection: c,
            query: query,
          ),
      ],
    );
  }
}

class _CollectionTopics extends StatefulWidget {
  const _CollectionTopics({
    super.key,
    required this.verified,
    required this.collection,
    required this.query,
  });

  final VerifiedAssetHadithRepository verified;
  final HadithCollection collection;
  final String query;

  @override
  State<_CollectionTopics> createState() => _CollectionTopicsState();
}

class _CollectionTopicsState extends State<_CollectionTopics> {
  late final Future<List<BundledSection>> _secsFuture;

  @override
  void initState() {
    super.initState();
    _secsFuture =
        widget.verified.sections(widget.collection.id);
  }

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    return FutureBuilder<List<BundledSection>>(
      future: _secsFuture,
      builder: (context, snap) {
        final secs = (snap.data ?? [])
            .where((sec) =>
                sec.count > 0 &&
                (widget.query.isEmpty ||
                    sec.title
                        .toLowerCase()
                        .contains(widget.query)))
            .toList();
        if (snap.connectionState == ConnectionState.waiting) {
          return const SizedBox.shrink();
        }
        if (secs.isEmpty) return const SizedBox.shrink();
        final c = widget.collection;
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(4, 16, 4, 4),
              child: Text(
                s.isArabic ? c.nameAr : c.nameEn,
                style: Theme.of(context)
                    .textTheme
                    .titleSmall
                    ?.copyWith(fontWeight: FontWeight.w700),
              ),
            ),
            for (final sec in secs)
              Card(
                child: ListTile(
                  leading: const Icon(Icons.topic_outlined),
                  title: Text(sec.title.isEmpty
                      ? '${s.t('book')} ${sec.section}'
                      : sec.title),
                  subtitle: Text(
                    '${sec.count} ${s.t('hadithCountUnit')} · '
                    '${s.t('numbersLabel')} ${sec.first}–${sec.last}',
                  ),
                  trailing: const Icon(Icons.chevron_right),
                  onTap: () => Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => _TopicHadithsScreen(
                        collectionId: c.id,
                        section: sec.section,
                        title: sec.title.isEmpty
                            ? '${s.t('book')} ${sec.section}'
                            : sec.title,
                      ),
                    ),
                  ),
                ),
              ),
          ],
        );
      },
    );
  }
}

class _TopicHadithsScreen extends StatelessWidget {
  const _TopicHadithsScreen({
    required this.collectionId,
    required this.section,
    required this.title,
  });

  final String collectionId;
  final int section;
  final String title;

  @override
  Widget build(BuildContext context) {
    return _TopicHadithsBody(
      collectionId: collectionId,
      section: section,
      title: title,
    );
  }
}

class _TopicHadithsBody extends ConsumerWidget {
  const _TopicHadithsBody({
    required this.collectionId,
    required this.section,
    required this.title,
  });

  final String collectionId;
  final int section;
  final String title;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final repo = ref.watch(hadithRepositoryProvider);
    final verified =
        repo is VerifiedAssetHadithRepository ? repo : null;
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: verified == null
          ? Center(child: Text(s.t('contentUnavailable')))
          : FutureBuilder(
              future:
                  verified.sectionHadiths(section, collectionId),
              builder: (context, snap) {
                final items = snap.data ?? [];
                if (snap.connectionState ==
                    ConnectionState.waiting) {
                  return const Center(
                      child: CircularProgressIndicator());
                }
                if (items.isEmpty) {
                  return Center(
                      child: Text(s.t('contentUnavailable')));
                }
                return ListView.builder(
                  padding: const EdgeInsets.all(12),
                  itemCount: items.length,
                  itemBuilder: (context, i) => Padding(
                    padding: const EdgeInsets.only(bottom: 8),
                    child: HadithCard(
                        hadith: items[i],
                        missingMessage: null),
                  ),
                );
              },
            ),
    );
  }
}
