import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../data/models/quran.dart';
import '../../data/seed/hadith_collections.dart';
import '../../data/repositories/verified_asset_quran_repository.dart';
import '../../state/providers.dart';
import '../../core/navigation/adaptive_scaffold.dart';

/// 5-page onboarding per spec §26. No account required.
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

  @override
  Widget build(BuildContext context) {
    final s = AppStrings.of(context);
    return Scaffold(
      body: SafeArea(
        child: Column(
          children: [
            Expanded(
              child: PageView(
                controller: _ctrl,
                onPageChanged: (i) => setState(() => _page = i),
                children: [
                  _languagePage(),
                  _riwayaPage(),
                  _scriptPage(),
                  _collectionsPage(),
                  _downloadsPage(),
                ],
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(20),
              child: Row(
                children: [
                  Text('${_page + 1} / 5'),
                  const Spacer(),
                  if (_page > 0)
                    TextButton(
                        onPressed: () => _ctrl.previousPage(
                            duration: const Duration(milliseconds: 300),
                            curve: Curves.easeOut),
                        child: const Text('‹')),
                  const SizedBox(width: 8),
                  FilledButton(
                    onPressed: _page == 4 ? _finish : () => _ctrl.nextPage(
                        duration: const Duration(milliseconds: 300),
                        curve: Curves.easeOut),
                    child: Text(
                        _page == 4 ? s.t('obDone') : s.t('obNext')),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _languagePage() {
    final s = AppStrings.of(context);
    return _wrap(s.t('obLanguage'), [
        _option('العربية', _locale == 'ar', () => setState(() => _locale = 'ar')),
        _option('English', _locale == 'en', () => setState(() => _locale = 'en')),
        _option('Français', _locale == 'fr', () => setState(() => _locale = 'fr')),
      ]);
  }

  Widget _riwayaPage() {
    final s = AppStrings.of(context);
    final ar = _locale == 'ar';
    return _wrap(s.t('obRiwaya'), [
      _option(ar ? 'حفص عن عاصم' : 'حفص عن عاصم — Ḥafṣ ʿan ʿĀṣim',
          _riwaya == RiwayaId.hafsAsim,
          () => setState(() => _riwaya = RiwayaId.hafsAsim)),
      _option(ar ? 'ورش عن نافع' : 'ورش عن نافع — Warsh ʿan Nāfiʿ',
          _riwaya == RiwayaId.warshNafi,
          () => setState(() {
            _riwaya = RiwayaId.warshNafi;
            _script = QuranScript.uthmani;
          })),
      _option(ar ? 'قالون عن نافع' : 'قالون عن نافع — Qālūn ʿan Nāfiʿ',
          _riwaya == RiwayaId.qalunNafi,
          () => setState(() {
            _riwaya = RiwayaId.qalunNafi;
            _script = QuranScript.uthmani;
          })),
    ]);
  }

  Widget _scriptPage() {
    final s = AppStrings.of(context);
    final ar = _locale == 'ar';
    return _wrap(s.t('obScript'), [
      _option(ar ? 'عثماني' : 'Uthmani — عثماني',
          _script == QuranScript.uthmani,
          () => setState(() => _script = QuranScript.uthmani)),
      if (kVerifiedQuranAssets.containsKey(
          '${_riwaya.storageKey}__imlai'))
        _option(ar ? 'إملائي' : 'Simple / Imla’i — إملائي',
            _script == QuranScript.imlai,
            () => setState(() => _script = QuranScript.imlai)),
      if (kVerifiedQuranAssets.containsKey(
          '${_riwaya.storageKey}__indopak'))
        _option(ar ? 'هندي باكستاني' : 'IndoPak',
            _script == QuranScript.indopak,
            () => setState(() => _script = QuranScript.indopak)),
    ]);
  }

  Widget _collectionsPage() {
    final s = AppStrings.of(context);
    final ar = _locale == 'ar';
    return _wrap(s.t('obSources'), [
      _preset(
          ar ? 'الصحيحان' : 'Sahihayn (Bukhari + Muslim)',
          {'bukhari', 'muslim'}),
      _preset(ar ? 'الكتب الستة' : 'Kutub al-Sittah',
          kKutubSittah),
      _preset(ar ? 'كل المجموعات' : 'All collections',
          kHadithCollections.map((c) => c.id).toSet()),
      const Divider(),
      ...kHadithCollections.take(6).map((c) => CheckboxListTile(
            value: _collections.contains(c.id),
            onChanged: (_) => setState(() {
              _collections.contains(c.id)
                  ? _collections.remove(c.id)
                  : _collections.add(c.id);
            }),
            title: Text(ar ? c.nameAr : '${c.nameAr} · ${c.nameEn}'),
          )),
    ]);
  }

  Widget _downloadsPage() {
    final s = AppStrings.of(context);
    return _wrap(s.t('obDownloads'), [
      Text(s.t('obDownloadsHint')),
    ]);
  }

  Widget _wrap(String title, List<Widget> children) => ListView(
        padding: const EdgeInsets.all(24),
        children: [
          const SizedBox(height: 24),
          Text(title, style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w700)),
          const SizedBox(height: 16),
          ...children,
        ],
      );

  Widget _option(String label, bool selected, VoidCallback onTap) => Card(
        child: ListTile(
          title: Text(label),
          trailing: selected ? const Icon(Icons.check_circle) : null,
          onTap: onTap,
        ),
      );

  Widget _preset(String label, Set<String> ids) => ListTile(
        title: Text(label),
        trailing: _collections.containsAll(ids)
            ? const Icon(Icons.check_circle)
            : const Icon(Icons.circle_outlined),
        onTap: () => setState(() => _collections = ids),
      );

  Future<void> _finish() async {
    final app = ref.read(appPrefsProvider.notifier);
    await app.update(ref.read(appPrefsProvider).copyWith(
        locale: _locale, onboarded: true));
    final q = ref.read(quranPrefsProvider.notifier);
    await q.update(ref
        .read(quranPrefsProvider)
        .copyWith(riwaya: _riwaya, script: _script));
    if (mounted) {
      Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const AdaptiveScaffold()));
    }
  }
}
