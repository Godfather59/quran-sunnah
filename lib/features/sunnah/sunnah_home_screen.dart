import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../data/models/hadith.dart';
import '../../data/repositories/hadith_repository.dart';
import '../../data/repositories/verified_asset_hadith_repository.dart';
import '../../data/seed/hadith_collections.dart';
import 'hadith_reader_screen.dart';
import 'topic_collections_screen.dart';

/// Sunnah home (§15–16): everything inline, no AppBar buttons.
/// Collections = one-tap chips, search/book/number/narrator type-to-filter,
/// Books & Topics = visible one-tap rows (no hidden toolbar icons).
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
  late final ScrollController _scrollCtrl;

  final _searchCtrl = TextEditingController();
  final _bookCtrl = TextEditingController();
  final _numberCtrl = TextEditingController();
  final _narratorCtrl = TextEditingController();
  Timer? _debounce;

  static const _famousNarrators = [
    'أبو هريرة',
    'عائشة',
    'عبد الله بن عباس',
    'أنس بن مالك',
    'عمر بن الخطاب',
  ];

  @override
  void initState() {
    super.initState();
    _scrollCtrl = ScrollController();
    _scrollCtrl.addListener(_onScroll);
    final f = ref.read(hadithFilterProvider);
    _searchCtrl.text = f.query ?? '';
    _bookCtrl.text = f.book ?? '';
    _numberCtrl.text = f.number ?? '';
    _narratorCtrl.text = f.narrator ?? '';
    WidgetsBinding.instance
        .addPostFrameCallback((_) => _reset());
  }

  @override
  void dispose() {
    _scrollCtrl.removeListener(_onScroll);
    _scrollCtrl.dispose();
    _searchCtrl.dispose();
    _bookCtrl.dispose();
    _numberCtrl.dispose();
    _narratorCtrl.dispose();
    _debounce?.cancel();
    super.dispose();
  }

  void _onScroll() {
    if (_scrollCtrl.position.pixels >=
        _scrollCtrl.position.maxScrollExtent - 600) {
      _more();
    }
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
    try {
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
    } catch (_) {
      if (!mounted || epoch != _epoch) return;
      setState(() => _loading = false);
    }
  }

  void _onFilterChanged() {
    if (_scrollCtrl.hasClients) {
      _scrollCtrl.jumpTo(0);
    }
    _reset();
  }

  void _update(HadithFilter next) {
    ref.read(hadithFilterProvider.notifier).state = next;
  }

  void _debouncedQuery(String v) {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 400), () {
      final f = ref.read(hadithFilterProvider);
      final t = v.trim();
      _update(f.copyWith(query: t.isEmpty ? null : t));
    });
  }

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    final filter = ref.watch(hadithFilterProvider);
    ref.listen(hadithFilterProvider, (_, __) => _onFilterChanged());

    return Scaffold(
      appBar: AppBar(title: Text(s.t('sunnah'))),
      body: Column(
        children: [
          _InlineFilters(
            filter: filter,
            searchCtrl: _searchCtrl,
            bookCtrl: _bookCtrl,
            numberCtrl: _numberCtrl,
            narratorCtrl: _narratorCtrl,
            onQuery: _debouncedQuery,
            onUpdate: _update,
          ),
          Expanded(
            child: _items.isEmpty && !_loading
                ? Center(
                    child: Text(s.t('noHadithMatches')))
                : ListView.builder(
                    controller: _scrollCtrl,
                    padding: const EdgeInsets.all(12),
                    itemCount:
                        _items.length + (_done ? 0 : 1),
                    itemBuilder: (context, i) {
                      if (i >= _items.length) {
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
                            missingMessage: null,
                            highlight: filter.query ??
                                filter.narrator),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}

class _InlineFilters extends ConsumerWidget {
  const _InlineFilters({
    required this.filter,
    required this.searchCtrl,
    required this.bookCtrl,
    required this.numberCtrl,
    required this.narratorCtrl,
    required this.onQuery,
    required this.onUpdate,
  });

  final HadithFilter filter;
  final TextEditingController searchCtrl;
  final TextEditingController bookCtrl;
  final TextEditingController numberCtrl;
  final TextEditingController narratorCtrl;
  final ValueChanged<String> onQuery;
  final ValueChanged<HadithFilter> onUpdate;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final verified = kHadithCollections
        .where((c) => kVerifiedHadithCollectionIds.contains(c.id))
        .toList();

    void toggleCollection(String id) {
      final next = {...filter.collectionIds};
      if (next.contains(id)) {
        next.remove(id);
      } else {
        next.add(id);
      }
      onUpdate(filter.copyWith(collectionIds: next));
    }

    return SingleChildScrollView(
      padding: const EdgeInsets.fromLTRB(12, 8, 12, 4),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 1. Search — type to filter, no Apply button.
          SearchBar(
            controller: searchCtrl,
            hintText: s.t('searchHint'),
            leading: const Icon(Icons.search),
            trailing: [
              if (searchCtrl.text.isNotEmpty)
                IconButton(
                  icon: const Icon(Icons.clear),
                  onPressed: () {
                    searchCtrl.clear();
                    onQuery('');
                  },
                ),
            ],
            onChanged: (v) {
              onQuery(v);
            },
          ),
          const SizedBox(height: 8),
          // 2. Collections — one-tap FilterChips.
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: [
                for (final c in verified)
                  Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: FilterChip(
                      label: Text(s.isArabic
                          ? c.nameAr
                          : c.nameEn),
                      selected:
                          filter.collectionIds.contains(c.id),
                      onSelected: (_) => toggleCollection(c.id),
                    ),
                  ),
              ],
            ),
          ),
          const SizedBox(height: 4),
          // 3. Presets — one tap.
          Wrap(
            spacing: 8,
            children: [
              ActionChip(
                label: Text(s.t('onlySahihayn')),
                onPressed: () => onUpdate(
                    filter.copyWith(collectionIds: kSahihayn)),
              ),
              ActionChip(
                label: Text(s.t('kutubSittah')),
                onPressed: () => onUpdate(filter.copyWith(
                    collectionIds: kKutubSittah)),
              ),
              ActionChip(
                label: Text(s.t('deselectAll')),
                onPressed: () =>
                    onUpdate(filter.copyWith(collectionIds: {})),
              ),
            ],
          ),
          const SizedBox(height: 8),
          // 4. Book + number — always visible, type to filter.
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: bookCtrl,
                  decoration: InputDecoration(
                    labelText: s.t('book'),
                    isDense: true,
                    border:
                        const OutlineInputBorder(),
                    suffixIcon:
                        bookCtrl.text.isNotEmpty
                            ? IconButton(
                                icon:
                                    const Icon(Icons.clear,
                                        size: 18),
                                onPressed: () {
                                  bookCtrl.clear();
                                  final t = bookCtrl
                                      .text
                                      .trim();
                                  onUpdate(filter.copyWith(
                                      book: t.isEmpty
                                          ? null
                                          : t));
                                },
                              )
                            : null,
                  ),
                  onChanged: (v) {
                    final t = v.trim();
                    onUpdate(filter.copyWith(
                        book:
                            t.isEmpty ? null : t));
                  },
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: TextField(
                  controller: numberCtrl,
                  keyboardType: TextInputType.number,
                  decoration: InputDecoration(
                    labelText: s.t('hadithNumber'),
                    isDense: true,
                    border:
                        const OutlineInputBorder(),
                    suffixIcon:
                        numberCtrl.text.isNotEmpty
                            ? IconButton(
                                icon:
                                    const Icon(Icons.clear,
                                        size: 18),
                                onPressed: () {
                                  numberCtrl.clear();
                                  onUpdate(filter.copyWith(
                                      number: null));
                                },
                              )
                            : null,
                  ),
                  onChanged: (v) {
                    final t = v.trim();
                    onUpdate(filter.copyWith(
                        number:
                            t.isEmpty ? null : t));
                  },
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          // 5. Narrator — quick picks + free text, no separate screen.
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: [
                for (final n
                    in _SunnahHomeScreenState
                        ._famousNarrators)
                  Padding(
                    padding:
                        const EdgeInsets.only(right: 8),
                    child: ChoiceChip(
                      label: Text(n),
                      selected:
                          filter.narrator == n,
                      onSelected: (_) {
                        if (filter.narrator == n) {
                          narratorCtrl.clear();
                          onUpdate(filter.copyWith(
                              narrator: null));
                        } else {
                          narratorCtrl.text = n;
                          onUpdate(filter.copyWith(
                              narrator: n));
                        }
                      },
                    ),
                  ),
              ],
            ),
          ),
          const SizedBox(height: 4),
          TextField(
            controller: narratorCtrl,
            decoration: InputDecoration(
              labelText: s.t('tabNarrator'),
              isDense: true,
              border: const OutlineInputBorder(),
              suffixIcon: narratorCtrl
                      .text.isNotEmpty
                  ? IconButton(
                      icon: const Icon(Icons.clear,
                          size: 18),
                      onPressed: () {
                        narratorCtrl.clear();
                        onUpdate(filter.copyWith(
                            narrator: null));
                      },
                    )
                  : null,
            ),
            onChanged: (v) {
              final t = v.trim();
              onUpdate(filter.copyWith(
                  narrator: t.isEmpty ? null : t));
            },
          ),
          // 6. Books & Topics — visible one-tap rows, no AppBar hunting.
          const SizedBox(height: 4),
          Row(
            children: [
              Expanded(
                child: FilledButton.tonalIcon(
                  onPressed: () {
                    final first = filter
                            .collectionIds.isNotEmpty
                        ? filter.collectionIds.first
                        : 'bukhari';
                    Navigator.of(context).push(
                        MaterialPageRoute(
                            builder: (_) =>
                                BookChapterBrowserScreen(
                                    collectionId:
                                        first)));
                  },
                  icon: const Icon(
                      Icons.library_books,
                      size: 18),
                  label: Text(s.t('booksChapters')),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: FilledButton.tonalIcon(
                  onPressed: () =>
                      Navigator.of(context).push(
                          MaterialPageRoute(
                              builder: (_) =>
                                  const TopicCollectionsScreen())),
                  icon: const Icon(
                      Icons.topic_outlined,
                      size: 18),
                  label: Text(s.t('tabTopic')),
                ),
              ),
            ],
          ),
          // 7. Active text filters with one-tap clear.
          if ((filter.query ?? '').isNotEmpty ||
              (filter.book ?? '').isNotEmpty ||
              (filter.number ?? '').isNotEmpty ||
              (filter.narrator ?? '').isNotEmpty)
            Padding(
              padding:
                  const EdgeInsets.only(top: 4),
              child: Wrap(
                spacing: 6,
                children: [
                  if ((filter.query ?? '')
                      .isNotEmpty)
                    Chip(
                      label: Text(
                          '${s.t('search')}: ${filter.query}'),
                      visualDensity:
                          VisualDensity.compact,
                      onDeleted: () {
                        searchCtrl.clear();
                        onUpdate(filter.copyWith(
                            query: null));
                      },
                    ),
                  if ((filter.book ?? '')
                      .isNotEmpty)
                    Chip(
                      label: Text(
                          '${s.t('book')}: ${filter.book}'),
                      visualDensity:
                          VisualDensity.compact,
                      onDeleted: () {
                        bookCtrl.clear();
                        onUpdate(filter.copyWith(
                            book: null));
                      },
                    ),
                  if ((filter.number ?? '')
                      .isNotEmpty)
                    Chip(
                      label: Text(
                          '${s.t('hadithNumber')}: ${filter.number}'),
                      visualDensity:
                          VisualDensity.compact,
                      onDeleted: () {
                        numberCtrl.clear();
                        onUpdate(filter.copyWith(
                            number: null));
                      },
                    ),
                  if ((filter.narrator ?? '')
                      .isNotEmpty)
                    Chip(
                      label: Text(
                        '${s.t('tabNarrator')}: ${filter.narrator}',
                        textDirection:
                            TextDirection.rtl,
                      ),
                      visualDensity:
                          VisualDensity.compact,
                      onDeleted: () {
                        narratorCtrl.clear();
                        onUpdate(filter.copyWith(
                            narrator: null));
                      },
                    ),
                ],
              ),
            ),
        ],
      ),
    );
  }
}
