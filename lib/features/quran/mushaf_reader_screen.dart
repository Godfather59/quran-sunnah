import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../core/theme/app_theme.dart';
import '../../core/utils/text_utils.dart';
import '../../core/widgets/common.dart';
import '../../data/models/quran.dart';
import '../../data/repositories/quran_repository.dart';
import '../../state/providers.dart';
import 'widgets/ayah_action_sheet.dart';

/// Mushaf Mode: one Medina page per swipe, boundaries from verified
/// metadata. Hafs editions only — other Riwaya show honest fallback.
class MushafReaderScreen extends ConsumerStatefulWidget {
  const MushafReaderScreen({super.key, this.surah = 1, this.page = 1});

  final int surah;
  final int page;

  @override
  ConsumerState<MushafReaderScreen> createState() =>
      _MushafReaderScreenState();
}

class _MushafReaderScreenState
    extends ConsumerState<MushafReaderScreen> {
  late final PageController _ctrl;
  late int _page;

  @override
  void initState() {
    super.initState();
    _page = widget.page;
    _ctrl = PageController(initialPage: _page - 1);
  }

  @override
  Widget build(BuildContext context) {
    final q = ref.watch(quranPrefsProvider);
    final metaAsync = ref.watch(quranMetadataProvider);

    return Scaffold(
      appBar: AppBar(
        title: Text('Mushaf · ص ${_page.toString()}'),
        actions: [
          IconButton(
            icon: const Icon(Icons.find_in_page),
            tooltip: 'Go to page',
            onPressed: () => _jumpToPage(context),
          ),
        ],
      ),
      body: metaAsync.when(
        loading: () =>
            const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('$e')),
        data: (meta) => Column(
          children: [
            Expanded(
              child: PageView.builder(
                controller: _ctrl,
                onPageChanged: (i) =>
                    setState(() => _page = i + 1),
                itemCount: meta.pageStarts.length,
                itemBuilder: (context, i) =>
                    _MushafPage(
                        page: i + 1, editionId: q.editionId),
              ),
            ),
            SafeArea(
              child: Padding(
                padding: const EdgeInsets.all(8),
                child: Row(
                  mainAxisAlignment:
                      MainAxisAlignment.center,
                  children: [
                    IconButton(
                        onPressed: () => _ctrl.previousPage(
                            duration: const Duration(
                                milliseconds: 250),
                            curve: Curves.easeOut),
                        icon:
                            const Icon(Icons.chevron_left)),
                    Text(
                        'Page $_page / ${meta.pageStarts.length}'),
                    IconButton(
                        onPressed: () => _ctrl.nextPage(
                            duration: const Duration(
                                milliseconds: 250),
                            curve: Curves.easeOut),
                        icon: const Icon(
                            Icons.chevron_right)),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _jumpToPage(BuildContext context) async {
    final ctrl =
        TextEditingController(text: '$_page');
    final v = await showDialog<int>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Go to page (1–604)'),
        content: TextField(
          controller: ctrl,
          keyboardType: TextInputType.number,
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text('Cancel')),
          FilledButton(
              onPressed: () => Navigator.pop(
                  ctx, int.tryParse(ctrl.text)),
              child: const Text('Go')),
        ],
      ),
    );
    if (v != null && v >= 1 && v <= 604) {
      _ctrl.jumpToPage(v - 1);
    }
  }
}

class _MushafPage extends ConsumerWidget {
  const _MushafPage(
      {required this.page, required this.editionId});

  final int page;
  final String editionId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final q = ref.watch(quranPrefsProvider);
    final ayahsAsync =
        ref.watch(_pageAyahsProvider((page, editionId)));

    return Padding(
      padding: const EdgeInsets.all(12),
      child: Card(
        child: Padding(
          padding: EdgeInsets.all(q.margins + 8),
          child: ayahsAsync.when(
            loading: () => const Center(
                child: CircularProgressIndicator()),
            error: (e, _) => Center(child: Text('$e')),
            data: (ayahs) {
              if (ayahs.isEmpty) {
                return Center(
                    child: UnavailableBanner(
                        message:
                            s.t('contentUnavailable')));
              }
              final juz = ayahs.first.juz;
              return Column(
                children: [
                  Text('Juz $juz · Page $page',
                      style: Theme.of(context)
                          .textTheme
                          .labelSmall),
                  const Divider(),
                  Expanded(
                    child: SingleChildScrollView(
                      child: Wrap(
                        children: ayahs
                            .map((a) => _PageAyah(
                                ayah: a, prefs: q))
                            .toList(),
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

class _PageAyah extends StatelessWidget {
  const _PageAyah({required this.ayah, required this.prefs});

  final Ayah ayah;
  final QuranPrefs prefs;

  @override
  Widget build(BuildContext context) {
    final num = switch (prefs.ayahNumberStyle) {
      AyahNumberStyle.arabicIndic =>
        toArabicIndic(ayah.displayAyahNumber),
      _ => '${ayah.displayAyahNumber}',
    };
    return InkWell(
      onTap: () => showAyahActionSheet(context, ayah),
      child: RichText(
        textDirection: TextDirection.rtl,
        textAlign: TextAlign.right,
        text: TextSpan(
          style: AppTheme.quranArabic(context,
              size: prefs.fontSize, height: prefs.lineHeight),
          children: [
            TextSpan(text: '${ayah.text} '),
            TextSpan(
              text: '﴿$num﴾ ',
              style: TextStyle(
                color: Theme.of(context)
                    .colorScheme
                    .primary,
                fontSize: prefs.fontSize * 0.8,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

final _pageAyahsProvider = FutureProvider.family(
    (ref, (int, String) args) => ref
        .watch(quranRepositoryProvider)
        .ayahsOfPage(args.$1, args.$2));
