import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../core/theme/app_theme.dart';
import '../../data/repositories/hadith_repository.dart';
import '../../data/repositories/quran_repository.dart';
import '../../data/seed/surah_metadata.dart';
import '../../data/services/search_service.dart';
import '../../state/providers.dart';
import '../../state/database_provider.dart';
import '../quran/quran_reader_screen.dart';
import '../quran/tafsir_screen.dart';
import '../sunnah/hadith_reader_screen.dart';

final searchServiceProvider = FutureProvider<SearchService>((ref) async {
  final database = await ref.watch(appDatabaseProvider);
  return SearchService(
    quran: ref.watch(quranRepositoryProvider),
    hadith: ref.watch(hadithRepositoryProvider),
    database: database,
  );
});

/// Global search (§21): Quran / Hadith / Tafsir / Surah over bundled
/// datasets. Arabic diacritic-insensitive (index only — display text
/// untouched). Narrator/topic tabs honestly report no dataset yet.
class GlobalSearchScreen extends ConsumerStatefulWidget {
  const GlobalSearchScreen({super.key});

  @override
  ConsumerState<GlobalSearchScreen> createState() =>
      _GlobalSearchScreenState();
}

class _GlobalSearchScreenState
    extends ConsumerState<GlobalSearchScreen> {
  final _ctrl = TextEditingController();
  Timer? _debounce;
  int _tab = 0; // 0 All · 1 Quran · 2 Hadith · 3 Tafsir · 4 Surah · 5 Narrator · 6 Topic
  Future<SearchResults>? _future;

  List<String> _tabLabels(AppStrings s) => [
        s.t('tabAll'),
        s.t('quran'),
        s.t('hadith'),
        s.t('tafsir'),
        s.t('surahs'),
        s.t('tabNarrator'),
        s.t('tabTopic'),
      ];

  @override
  void dispose() {
    _debounce?.cancel();
    _ctrl.dispose();
    super.dispose();
  }

  void _onChanged(String v) {
    _debounce?.cancel();
    if (v.trim().isEmpty) {
      setState(() => _future = null);
      return;
    }
    _debounce = Timer(const Duration(milliseconds: 400), () async {
      final q = ref.read(quranPrefsProvider);
      final svc = await ref.read(searchServiceProvider.future);
      if (!mounted) return;
      setState(() => _future = svc.search(
            query: v,
            editionId: q.editionId,
            tafsirId: q.tafsirId,
          ));
    });
  }

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(s.t('search'))),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(12),
            child: SearchBar(
              controller: _ctrl,
              hintText: s.t('searchHint'),
              leading: const Icon(Icons.search),
              autoFocus: true,
              trailing: [
                if (_ctrl.text.isNotEmpty)
                  IconButton(
                      icon: const Icon(Icons.clear),
                      onPressed: () {
                        _ctrl.clear();
                        _onChanged('');
                        setState(() {});
                      }),
              ],
              onChanged: (v) {
                _onChanged(v);
                setState(() {});
              },
            ),
          ),
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            padding:
                const EdgeInsets.symmetric(horizontal: 12),
            child: Row(
              children: _tabLabels(s)
                  .asMap()
                  .entries
                  .map((e) => Padding(
                        padding: const EdgeInsets.only(
                            right: 8),
                        child: ChoiceChip(
                          label: Text(e.value),
                          selected: _tab == e.key,
                          onSelected: (_) => setState(
                              () => _tab = e.key),
                        ),
                      ))
                  .toList(),
            ),
          ),
          Expanded(child: _results(context)),
        ],
      ),
    );
  }

  Widget _results(BuildContext context) {
    final s = AppStrings.of(context);
    if (_ctrl.text.trim().isEmpty) {
      return Center(child: Text(s.t('searchHint')));
    }
    if (_tab == 5 || _tab == 6) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Text(
            s.isArabic
                ? 'لا توجد بيانات منظمة للرواة أو المواضيع بعد — تبقى فارغة حتى تتوفر مصادر موثوقة.'
                : 'No structured narrator/topic dataset is bundled yet — '
                    'this stays empty until a verified source ships.',
            textAlign: TextAlign.center,
          ),
        ),
      );
    }
    final future = _future;
    if (future == null) {
      return const Center(child: CircularProgressIndicator());
    }
    return FutureBuilder(
      future: future,
      builder: (context, snap) {
        if (snap.connectionState == ConnectionState.waiting) {
          return const Center(
              child: CircularProgressIndicator());
        }
        final r = snap.data;
        if (r == null || r.total == 0) {
          return Center(child: Text(s.t('contentUnavailable')));
        }
        final showAll = _tab == 0;
        return ListView(
          padding: const EdgeInsets.all(12),
          children: [
            Text(
                '${r.total} · ${r.truncated ? '(50)' : ''}',
                style: Theme.of(context).textTheme.bodySmall),
            if (showAll || _tab == 4)
              ...r.surahs.map(_surahTile),
            if (showAll || _tab == 1) ...[
              _header(context, s.t('quran'), r.quran.length),
              ...r.quran.map(_quranTile),
            ],
            if (showAll || _tab == 2) ...[
              _header(context, s.t('hadith'), r.hadith.length),
              ...r.hadith.map(_hadithTile),
            ],
            if (showAll || _tab == 3) ...[
              _header(context, s.t('tafsir'), r.tafsir.length),
              ...r.tafsir.map(_tafsirTile),
            ],
            const SizedBox(height: 24),
          ],
        );
      },
    );
  }

  Widget _header(BuildContext context, String title, int n) =>
      Padding(
        padding: const EdgeInsets.fromLTRB(4, 16, 4, 4),
        child: Text('$title · $n',
            style: Theme.of(context)
                .textTheme
                .titleSmall
                ?.copyWith(fontWeight: FontWeight.w700)),
      );

  Widget _surahTile(SearchHit h) {
    final s = AppStrings.of(context);
    final m = kSurahMetadata
        .where((m) => m.number == h.surah)
        .firstOrNull;
    final title = (m == null || !s.isArabic)
        ? h.title
        : '${m.number}. ${m.nameAr}';
    return Card(
      child: ListTile(
        leading: CircleAvatar(child: Text('${h.surah}')),
        title: Text(title),
        subtitle: Text(h.subtitle),
        onTap: () => Navigator.of(context).push(
            MaterialPageRoute(
                builder: (_) =>
                    QuranReaderScreen(surah: h.surah!))),
      ),
    );
  }

  Widget _quranTile(SearchHit h) {
    final s = AppStrings.of(context);
    final title = s.isArabic
        ? '${s.t('surahWord')} ${h.surah} · ${s.t('ayat')} ${h.ayah}'
        : h.title;
    return Card(
      child: ListTile(
        title: Text(title,
            style:
                Theme.of(context).textTheme.labelLarge),
          subtitle: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: 4),
              Text(h.snippet,
                  textDirection: TextDirection.rtl,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: AppTheme.quranArabic(context,
                      size: 19)),
            ],
          ),
          onTap: () => Navigator.of(context).push(
              MaterialPageRoute(
                  builder: (_) => QuranReaderScreen(
                      surah: h.surah!,
                      initialAyah: h.ayah!))),
        ),
      );
  }

  Widget _hadithTile(SearchHit h) => Card(
        child: ListTile(
          title: Text(h.title,
              style: Theme.of(context).textTheme.labelLarge),
          subtitle: Text('${h.subtitle}\n${h.snippet}',
              maxLines: 2, overflow: TextOverflow.ellipsis),
          isThreeLine: true,
          onTap: () => showModalBottomSheet(
            context: context,
            showDragHandle: true,
            isScrollControlled: true,
            builder: (_) => DraggableScrollableSheet(
              expand: false,
              initialChildSize: 0.85,
              builder: (_, ctrl) => SingleChildScrollView(
                controller: ctrl,
                padding: const EdgeInsets.all(12),
                child: HadithCard(
                    hadith: h.hadith,
                    missingMessage: null),
              ),
            ),
          ),
        ),
      );

  Widget _tafsirTile(SearchHit h) => Card(
        child: ListTile(
          title: Text(h.title,
              style: Theme.of(context).textTheme.labelLarge),
          subtitle: Text(h.snippet,
              maxLines: 2, overflow: TextOverflow.ellipsis),
          isThreeLine: true,
          onTap: () => Navigator.of(context).push(
              MaterialPageRoute(
                  builder: (_) => TafsirScreen(
                      surah: h.surah!, ayah: h.ayah!))),
        ),
      );
}
