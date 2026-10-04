import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../data/models/hadith.dart';
import '../../data/repositories/hadith_repository.dart';
import 'collection_selector_screen.dart';
import 'hadith_filter_screen.dart';
import 'hadith_reader_screen.dart';

/// Sunnah home (§15–16): collection selector + paginated hadith feed.
class SunnahHomeScreen extends ConsumerStatefulWidget {
  const SunnahHomeScreen({super.key});

  @override
  ConsumerState<SunnahHomeScreen> createState() =>
      _SunnahHomeScreenState();
}

class _SunnahHomeScreenState
    extends ConsumerState<SunnahHomeScreen> {
  static const _pageSize = 20;
  final List<Hadith> _items = [];
  bool _loading = false;
  bool _done = false;
  int _epoch = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance
        .addPostFrameCallback((_) => _reset());
  }

  void _reset() {
    _items.clear();
    _done = false;
    _epoch++;
    _more();
  }

  Future<void> _more() async {
    if (_loading || _done) return;
    setState(() => _loading = true);
    final epoch = _epoch;
    final filter = ref.read(hadithFilterProvider);
    final page = await ref
        .read(hadithRepositoryProvider)
        .query(filter,
            limit: _pageSize, offset: _items.length);
    if (!mounted || epoch != _epoch) return;
    setState(() {
      _loading = false;
      if (page.length < _pageSize) _done = true;
      _items.addAll(page);
    });
  }

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    final filter = ref.watch(hadithFilterProvider);
    // Refetch when the filter changes.
    ref.listen(hadithFilterProvider, (_, __) => _reset());

    return Scaffold(
      appBar: AppBar(
        title: Text(s.t('sunnah')),
        actions: [
          IconButton(
            icon: const Icon(Icons.filter_list),
            tooltip: s.t('filters'),
            onPressed: () => Navigator.of(context).push(
                MaterialPageRoute(
                    builder: (_) =>
                        const HadithFilterScreen())),
          ),
          IconButton(
            icon: const Icon(Icons.library_books),
            tooltip: s.t('booksChapters'),
            onPressed: () => Navigator.of(context).push(
                MaterialPageRoute(
                    builder: (_) =>
                        const BookChapterBrowserScreen(
                            collectionId: 'bukhari'))),
          ),
        ],
      ),
      body: Column(
        children: [
          _SourceBar(filter: filter),
          Expanded(
            child: _items.isEmpty && !_loading
                ? Center(
                    child: Text(s.t('noHadithMatches')))
                : ListView.builder(
                    padding: const EdgeInsets.all(12),
                    itemCount:
                        _items.length + (_done ? 0 : 1),
                    itemBuilder: (context, i) {
                      if (i >= _items.length) {
                        _more();
                        return const Padding(
                          padding: EdgeInsets.all(16),
                          child: Center(
                              child:
                                  CircularProgressIndicator()),
                        );
                      }
                      final h = _items[i];
                      if (h.isPlaceholder) {
                        return const HadithPlaceholderCard();
                      }
                      return Padding(
                        padding: const EdgeInsets.only(
                            bottom: 8),
                        child: HadithCard(
                            hadith: h,
                            missingMessage: null),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}

class _SourceBar extends ConsumerWidget {
  const _SourceBar({required this.filter});

  final HadithFilter filter;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    return Container(
      padding:
          const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      child: Row(
        children: [
          Expanded(
            child: Text(
              filter.collectionIds.isEmpty
                  ? s.t('sourcesNone')
                  : '${filter.collectionIds.length} ${s.t('sourcesSelected')}',
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ),
          FilledButton.tonal(
            onPressed: () => Navigator.of(context).push(
                MaterialPageRoute(
                    builder: (_) =>
                        const CollectionSelectorScreen())),
            child: Text(s.t('sources')),
          ),
        ],
      ),
    );
  }
}
