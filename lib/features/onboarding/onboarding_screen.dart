import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/l10n/app_strings.dart';
import '../../core/navigation/adaptive_scaffold.dart';
import '../../data/content/content_packages.dart';
import '../../data/models/quran.dart';
import '../../data/repositories/hadith_repository.dart';
import '../../data/repositories/verified_asset_hadith_repository.dart';
import '../../data/repositories/tafsir_repository.dart';
import '../../data/repositories/translation_repository.dart';
import '../../data/repositories/verified_asset_quran_repository.dart';
import '../../data/seed/hadith_collections.dart';
import '../../state/download_state.dart';
import '../../state/providers.dart';

class OnboardingScreen extends ConsumerStatefulWidget {
  const OnboardingScreen({super.key});

  @override
  ConsumerState<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends ConsumerState<OnboardingScreen> {
  final _ctrl = PageController();

  int _page = 0;
  String _locale = 'ar';
  RiwayaId _riwaya = RiwayaId.hafsAsim;
  QuranScript _script = QuranScript.uthmani;
  Set<String> _collections = {'bukhari', 'muslim'};
  Set<String> _translations = {};
  String _tafsirId = 'jalalayn';
  bool _includeTafsir = false;
  bool _showTajweed = false;
  bool _downloadWords = false;
  bool _finishing = false;
  String? _finishError;

  AppStrings get _strings => AppStrings(Locale(_locale));

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final s = _strings;
    final direction =
        _locale == 'ar' ? TextDirection.rtl : TextDirection.ltr;

    return Directionality(
      textDirection: direction,
      child: Scaffold(
        body: SafeArea(
          child: Column(
            children: [
              Expanded(
                child: PageView(
                  controller: _ctrl,
                  physics: _finishing
                      ? const NeverScrollableScrollPhysics()
                      : null,
                  onPageChanged: (i) => setState(() => _page = i),
                  children: [
                    _languagePage(),
                    _riwayaPage(),
                    _scriptPage(),
                    _collectionsPage(),
                    _additionalContentPage(),
                  ],
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(20),
                child: Row(
                  children: [
                    Text('${_page + 1} / 5'),
                    const Spacer(),
                    if (_page > 0 && !_finishing)
                      TextButton(
                        onPressed: () => _ctrl.previousPage(
                          duration: const Duration(milliseconds: 300),
                          curve: Curves.easeOut,
                        ),
                        child: const Text('‹'),
                      ),
                    const SizedBox(width: 8),
                    FilledButton(
                      onPressed: _finishing
                          ? null
                          : _page == 4
                              ? _finish
                              : () => _ctrl.nextPage(
                                    duration:
                                        const Duration(milliseconds: 300),
                                    curve: Curves.easeOut,
                                  ),
                      child: Text(
                        _finishing
                            ? s.t('downloading')
                            : _page == 4
                                ? s.t('obDone')
                                : s.t('obNext'),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _languagePage() {
    final s = _strings;
    return _wrap(s.t('obLanguage'), [
      _option('العربية', _locale == 'ar', () => _setLocale('ar')),
      _option('English', _locale == 'en', () => _setLocale('en')),
      _option('Français', _locale == 'fr', () => _setLocale('fr')),
    ]);
  }

  void _setLocale(String locale) {
    setState(() {
      _locale = locale;
      // Optional packages are never forced by language. A clean install
      // can always finish setup fully offline with the core datasets.
      _translations = <String>{};
    });
  }

  Widget _riwayaPage() {
    final s = _strings;
    final ar = _locale == 'ar';
    return _wrap(s.t('obRiwaya'), [
      _option(
        ar ? 'حفص عن عاصم' : 'حفص عن عاصم — Ḥafṣ ʿan ʿĀṣim',
        _riwaya == RiwayaId.hafsAsim,
        () => setState(() => _riwaya = RiwayaId.hafsAsim),
      ),
      _option(
        ar ? 'ورش عن نافع' : 'ورش عن نافع — Warsh ʿan Nāfiʿ',
        _riwaya == RiwayaId.warshNafi,
        () => setState(() {
          _riwaya = RiwayaId.warshNafi;
          _script = QuranScript.uthmani;
          _showTajweed = false;
        }),
      ),
      _option(
        ar ? 'قالون عن نافع' : 'قالون عن نافع — Qālūn ʿan Nāfiʿ',
        _riwaya == RiwayaId.qalunNafi,
        () => setState(() {
          _riwaya = RiwayaId.qalunNafi;
          _script = QuranScript.uthmani;
          _showTajweed = false;
        }),
      ),
    ]);
  }

  Widget _scriptPage() {
    final s = _strings;
    final ar = _locale == 'ar';
    return _wrap(s.t('obScript'), [
      _option(
        ar ? s.t('uthmani') : '${s.t('uthmani')} — عثماني',
        _script == QuranScript.uthmani,
        () => setState(() => _script = QuranScript.uthmani),
      ),
      if (kVerifiedQuranAssets.containsKey(
        '${_riwaya.storageKey}__imlai',
      ))
        _option(
          ar ? s.t('imlai') : '${s.t('imlai')} — إملائي',
          _script == QuranScript.imlai,
          () => setState(() {
            _script = QuranScript.imlai;
            _showTajweed = false;
          }),
        ),
      if (kVerifiedQuranAssets.containsKey(
        '${_riwaya.storageKey}__indopak',
      ))
        _option(
          s.t('indopak'),
          _script == QuranScript.indopak,
          () => setState(() {
            _script = QuranScript.indopak;
            _showTajweed = false;
          }),
        ),
    ]);
  }

  Widget _collectionsPage() {
    final s = _strings;
    final ar = _locale == 'ar';
    return _wrap(s.t('obSources'), [
      _preset(
        ar ? 'الصحيحان' : 'Sahihayn (Bukhari + Muslim)',
        {'bukhari', 'muslim'},
      ),
      _preset(s.t('kutubSittah'), kKutubSittah),
      _preset(
        s.t('allCollections'),
        kHadithCollections
            .map((c) => c.id)
            .where(kVerifiedHadithCollectionIds.contains)
            .toSet(),
      ),
      const Divider(),
      ...kHadithCollections.map(
        (c) {
          final verified =
              kVerifiedHadithCollectionIds.contains(c.id);
          if (!verified) {
            return CheckboxListTile(
              value: false,
              onChanged: null,
              title: Text(ar ? c.nameAr : '${c.nameAr} · ${c.nameEn}'),
              subtitle: Text(s.t('verifiedDatasetRequired')),
            );
          }
          return CheckboxListTile(
            value: _collections.contains(c.id),
            onChanged: (_) => setState(() {
              _collections.contains(c.id)
                  ? _collections.remove(c.id)
                  : _collections.add(c.id);
            }),
            title: Text(ar ? c.nameAr : '${c.nameAr} · ${c.nameEn}'),
            subtitle: Text(
              kCoreDatasetIds.contains('hadith:${c.id}')
                  ? s.t('shipsWithApp')
                  : s.t('downloadBeforeUse'),
            ),
          );
        },
      ),
    ]);
  }

  Widget _additionalContentPage() {
    final s = _strings;
    final manifestAsync = ref.watch(contentPackageManifestProvider);
    final downloadState = ref.watch(downloadProvider);
    final tajweedAvailable = _riwaya == RiwayaId.hafsAsim &&
        _script == QuranScript.uthmani;

    return manifestAsync.when(
      loading: () => _wrap(s.t('obDownloads'), const [
        Center(child: CircularProgressIndicator()),
      ]),
      error: (error, _) => _wrap(s.t('obDownloads'), [
        Text('${s.t('contentUnavailable')}\n$error'),
      ]),
      data: (manifest) {
        final ids = _selectedPackageIds();
        final selected = manifest.packages
            .where((p) => ids.contains(p.id))
            .toList(growable: false);
        final total = selected.fold<int>(
          0,
          (sum, p) => sum + p.sizeBytes,
        );
        final downloaded = selected.fold<double>(
          0,
          (sum, p) {
            if (downloadState.installed.contains(p.id)) {
              return sum + p.sizeBytes;
            }
            return sum +
                p.sizeBytes * (downloadState.progress[p.id] ?? 0);
          },
        );

        return _wrap(s.t('obDownloads'), [
          Text(
            s.t('obDownloadsHint'),
            style: Theme.of(context).textTheme.bodyMedium,
          ),
          const SizedBox(height: 12),
          if (_finishing) ...[
            LinearProgressIndicator(
              value: total == 0 ? 1 : downloaded / total,
            ),
            const SizedBox(height: 8),
            Text(s.t('downloadingContent')),
            const SizedBox(height: 12),
          ],
          if (_finishError != null) ...[
            Text(
              s.t('downloadFailed'),
              style: TextStyle(
                color: Theme.of(context).colorScheme.error,
              ),
            ),
            const SizedBox(height: 8),
          ],
          Text(
            s.t('translations'),
            style: Theme.of(context).textTheme.titleMedium,
          ),
          ...kTranslationCatalog
              .where((item) =>
                  item.id == 'en-sahih' ||
                  item.id == 'fr-hamidullah')
              .map(
                (item) => CheckboxListTile(
                  value: _translations.contains(item.id),
                  title: Text(
                    item.id == 'en-sahih'
                        ? s.t('saheehInternational')
                        : s.t('hamidullahFrench'),
                  ),
                  onChanged: _finishing
                      ? null
                      : (enabled) => setState(() {
                            enabled == true
                                ? _translations.add(item.id)
                                : _translations.remove(item.id);
                          }),
                ),
              ),
          const Divider(),
          SwitchListTile(
            value: _includeTafsir,
            onChanged: _finishing
                ? null
                : (value) => setState(() => _includeTafsir = value),
            title: Text(s.t('includeTafsir')),
          ),
          RadioGroup<String>(
            groupValue: _tafsirId,
            onChanged: !_includeTafsir || _finishing
                ? (_) {}
                : (value) {
                    if (value != null) {
                      setState(() => _tafsirId = value);
                    }
                  },
            child: Column(
              children: [
                for (final item
                    in kTafsirCatalog.where((item) => item.bundled))
                  RadioListTile<String>(
                    value: item.id,
                    enabled: _includeTafsir && !_finishing,
                    title: Text(
                      _locale == 'ar' ? item.titleAr : item.titleEn,
                    ),
                  ),
              ],
            ),
          ),
          const Divider(),
          SwitchListTile(
            value: tajweedAvailable && _showTajweed,
            onChanged: tajweedAvailable && !_finishing
                ? (value) => setState(() => _showTajweed = value)
                : null,
            title: Text(s.t('tajweedColors')),
            subtitle: Text(
              tajweedAvailable
                  ? s.t('tajweedVerifiedHint')
                  : s.t('tajweedHafsOnly'),
            ),
          ),
          SwitchListTile(
            value: _downloadWords,
            onChanged: _finishing
                ? null
                : (value) => setState(() => _downloadWords = value),
            title: Text(s.t('wordMorphology')),
            subtitle: Text(s.t('wordMorphologyHint')),
          ),
          const Divider(),
          ListTile(
            leading: const Icon(Icons.download_outlined),
            title: Text(s.t('selectedDownloadSize')),
            subtitle: Text(
              '${selected.length} ${s.t('packageCount')} · '
              '${_formatBytes(total)}',
            ),
          ),
          if (_collections
              .where((id) => !kCoreDatasetIds.contains('hadith:$id'))
              .isNotEmpty)
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Text(
                s.isArabic
                    ? 'تتضمن اختياراتك كتب حديث إضافية وسيتم تنزيلها الآن.'
                    : 'Your selection includes additional Hadith collections that will be downloaded now.',
                style: Theme.of(context).textTheme.bodySmall,
              ),
            ),
        ]);
      },
    );
  }

  Set<String> _selectedPackageIds() {
    final ids = <String>{
      for (final id in _translations) 'quran:$id',
      for (final id in _collections)
        if (!kCoreDatasetIds.contains('hadith:$id')) 'hadith:$id',
    };
    if (_includeTafsir) ids.add('quran:tafsir-$_tafsirId');
    if (_showTajweed) ids.add('quran:tajweed-hafs');
    if (_downloadWords) ids.add('quran:words-hafs');
    return ids;
  }

  Widget _wrap(String title, List<Widget> children) => ListView(
        padding: const EdgeInsets.all(24),
        children: [
          const SizedBox(height: 24),
          Text(
            title,
            style: const TextStyle(
              fontSize: 22,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 16),
          ...children,
        ],
      );

  Widget _option(
    String label,
    bool selected,
    VoidCallback onTap,
  ) =>
      Card(
        child: ListTile(
          title: Text(label),
          trailing: selected ? const Icon(Icons.check_circle) : null,
          onTap: _finishing ? null : onTap,
        ),
      );

  Widget _preset(String label, Set<String> ids) => ListTile(
        title: Text(label),
        trailing: _collections.containsAll(ids)
            ? const Icon(Icons.check_circle)
            : const Icon(Icons.circle_outlined),
        onTap: _finishing
            ? null
            : () => setState(() => _collections = {...ids}),
      );

  String _formatBytes(int bytes) {
    if (bytes < 1024) return '$bytes B';
    final kb = bytes / 1024;
    if (kb < 1024) return '${kb.toStringAsFixed(1)} KB';
    return '${(kb / 1024).toStringAsFixed(1)} MB';
  }

  Future<void> _finish() async {
    setState(() {
      _finishing = true;
      _finishError = null;
    });

    try {
      final manifest =
          await ref.read(contentPackageManifestProvider.future);
      final known = manifest.packages.map((p) => p.id).toSet();
      final selected =
          _selectedPackageIds().where(known.contains).toSet();

      await ref.read(downloadProvider.notifier).installMany(selected);
      ref.read(contentRevisionProvider.notifier).state++;

      await ref.read(appPrefsProvider.notifier).update(
            ref.read(appPrefsProvider).copyWith(
                  locale: _locale,
                  onboarded: true,
                ),
          );

      final current = ref.read(quranPrefsProvider);
      await ref.read(quranPrefsProvider.notifier).update(
            current.copyWith(
              riwaya: _riwaya,
              script: _script,
              translations: _translations.toList(growable: false),
              tafsirId: _tafsirId,
              showTranslation: _translations.isNotEmpty,
              showTajweed: _showTajweed,
            ),
          );

      ref.read(hadithFilterProvider.notifier).state = HadithFilter(
        collectionIds: _collections
            .where(kVerifiedHadithCollectionIds.contains)
            .toSet(),
      );

      if (mounted) {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const AdaptiveScaffold()),
        );
      }
    } catch (error) {
      if (mounted) {
        setState(() {
          _finishing = false;
          _finishError = '$error';
        });
      }
    }
  }
}
