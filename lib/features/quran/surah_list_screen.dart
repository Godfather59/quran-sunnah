import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../data/repositories/quran_metadata.dart';
import '../../data/seed/surah_metadata.dart';
import '../../state/providers.dart';
import 'mushaf_reader_screen.dart';
import 'quran_reader_screen.dart';

/// Surah / Juz / Hizb / Rub / Page browsing + fast selector.
class SurahListScreen extends ConsumerStatefulWidget {
  const SurahListScreen({super.key});

  @override
  ConsumerState<SurahListScreen> createState() => _SurahListScreenState();
}

class _SurahListScreenState extends ConsumerState<SurahListScreen>
    with SingleTickerProviderStateMixin {
  late final TabController _tabs;
  String _query = '';

  @override
  void initState() {
    super.initState();
    _tabs = TabController(length: 5, vsync: this);
  }

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    return Scaffold(
      appBar: AppBar(
        title: Text(s.t('quran')),
        bottom: TabBar(
          controller: _tabs,
          isScrollable: true,
          tabs: const [
            Tab(text: 'Surah'),
            Tab(text: 'Juz'),
            Tab(text: 'Hizb'),
            Tab(text: 'Rubʿ'),
            Tab(text: 'Page'),
          ],
        ),
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(12),
            child: SearchBar(
              hintText: s.t('searchHint'),
              leading: const Icon(Icons.search),
              onChanged: (v) => setState(() => _query = v),
              onTap: () => _openFastSelector(context),
            ),
          ),
          Expanded(
            child: TabBarView(
              controller: _tabs,
              children: [
                _surahList(),
                _metaGrid(
                    label: 'Juz',
                    count: 30,
                    startOf: (meta, i) => meta.juzStarts[i],
                    openReading: true),
                _metaGrid(
                    label: 'Hizb',
                    count: 60,
                    startOf: (meta, i) =>
                        meta.quarterStarts[i * 4],
                    openReading: true),
                _metaGrid(
                    label: 'Rubʿ',
                    count: 240,
                    startOf: (meta, i) =>
                        meta.quarterStarts[i],
                    openReading: true),
                _metaGrid(
                    label: 'Page',
                    count: 604,
                    startOf: (meta, i) =>
                        meta.pageStarts[i],
                    openReading: false),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _surahList() {
    final q = _query.trim().toLowerCase();
    final items = kSurahMetadata.where((m) {
      if (q.isEmpty) return true;
      return m.nameAr.contains(_query) ||
          m.nameEn.toLowerCase().contains(q) ||
          m.nameFr.toLowerCase().contains(q) ||
          m.number.toString() == q;
    }).toList();
    return ListView.separated(
      itemCount: items.length,
      separatorBuilder: (_, _) => const Divider(height: 1),
      itemBuilder: (context, i) {
        final m = items[i];
        final loc = AppStrings.of(context);
        final sub = loc.isArabic
            ? '${m.makki ? loc.t('makki') : loc.t('madani')} · ${m.ayahCount} ${loc.t('ayat')}'
            : '${m.nameEn} · ${m.nameFr}\n${m.makki ? 'Makki' : 'Madani'} · ${m.ayahCount} ayat';
        return ListTile(
          leading: CircleAvatar(child: Text('${m.number}')),
          title: Text(m.nameAr,
              textDirection: TextDirection.rtl,
              style: const TextStyle(fontSize: 20)),
          subtitle: Text(sub),
          isThreeLine: true,
          trailing: const Icon(Icons.chevron_right),
          onTap: () => Navigator.of(context).push(MaterialPageRoute(
              builder: (_) => QuranReaderScreen(surah: m.number))),
        );
      },
    );
  }

  /// Metadata-driven grid: taps open the reader at the verified
  /// start ayah (reading mode) or the exact Mushaf page.
  Widget _metaGrid({
    required String label,
    required int count,
    required AyahRef Function(QuranMetadata, int) startOf,
    required bool openReading,
  }) {
    final metaAsync = ref.watch(quranMetadataProvider);
    return metaAsync.when(
      loading: () =>
          const Center(child: CircularProgressIndicator()),
      error: (e, _) => Center(child: Text('$e')),
      data: (meta) => GridView.builder(
        padding: const EdgeInsets.all(12),
        gridDelegate:
            const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 3,
                mainAxisSpacing: 8,
                crossAxisSpacing: 8,
                childAspectRatio: 1.6),
        itemCount: count,
        itemBuilder: (context, i) {
          final ref_ = startOf(meta, i);
          return Card(
            child: InkWell(
              borderRadius: BorderRadius.circular(20),
              onTap: () => Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => openReading
                      ? QuranReaderScreen(
                          surah: ref_.surah,
                          initialAyah: ref_.ayah)
                      : MushafReaderScreen(page: i + 1),
                ),
              ),
              child: Center(
                  child: Text('$label ${i + 1}\n${ref_.surah}:${ref_.ayah}',
                      textAlign: TextAlign.center)),
            ),
          );
        },
      ),
    );
  }

  void _openFastSelector(BuildContext context) {
    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      builder: (ctx) => DraggableScrollableSheet(
        expand: false,
        builder: (_, ctrl) => ListView.builder(
          controller: ctrl,
          itemCount: kSurahMetadata.length,
          itemBuilder: (_, i) {
            final m = kSurahMetadata[i];
            return ListTile(
              title: Text('${m.number}. ${m.nameAr} — ${m.nameEn}'),
              onTap: () {
                Navigator.pop(ctx);
                Navigator.of(context).push(MaterialPageRoute(
                    builder: (_) => QuranReaderScreen(surah: m.number)));
              },
            );
          },
        ),
      ),
    );
  }
}
