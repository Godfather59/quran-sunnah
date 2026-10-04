import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/l10n/app_strings.dart';
import '../../data/models/quran.dart';
import '../../data/repositories/tafsir_repository.dart';
import '../../data/repositories/translation_repository.dart';
import '../downloads/downloads_screen.dart';
import '../quran/audio_player_screen.dart';
import '../quran/riwaya_selector_screen.dart';
import '../../state/providers.dart';
import 'data_sources_screen.dart';

/// Organized settings (§25): grouped cards with icons, fully
/// localized — Arabic locale shows Arabic-only chrome (§33 terms kept
/// exact: Riwaya / Rasm / Font / Collection / Narrator).
class SettingsScreen extends ConsumerWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final s = AppStrings.of(context);
    final q = ref.watch(quranPrefsProvider);
    final app = ref.watch(appPrefsProvider);

    return Scaffold(
      appBar: AppBar(title: Text(s.t('settings'))),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
        children: [
          _Group(
            icon: Icons.menu_book_outlined,
            title: s.t('quranGroup'),
            children: [
              _Nav(
                icon: Icons.record_voice_over_outlined,
                title: s.t('defaultRiwaya'),
                subtitle: s.isArabic
                    ? _riwayaAr(q.riwaya)
                    : q.riwaya.name,
                onTap: () => Navigator.of(context).push(
                    MaterialPageRoute(
                        builder: (_) =>
                            const RiwayaSelectorScreen())),
              ),
              _Nav(
                icon: Icons.text_fields_outlined,
                title: s.t('quranScript'),
                subtitle: q.script.name,
                onTap: () => Navigator.of(context).push(
                    MaterialPageRoute(
                        builder: (_) =>
                            const ScriptSelectorScreen())),
              ),
              _Nav(
                icon: Icons.font_download_outlined,
                title: s.t('quranFont'),
                subtitle:
                    '${q.font.name} · ${q.fontSize.toStringAsFixed(0)}',
                onTap: () => showModalBottomSheet(
                    context: context,
                    showDragHandle: true,
                    builder: (_) =>
                        const FontSettingsSheet()),
              ),
              _Nav(
                icon: Icons.translate_outlined,
                title: s.t('translation'),
                subtitle: q.translations.isEmpty
                    ? '—'
                    : q.translations.join(' · '),
                onTap: () =>
                    _translationsSheet(context, ref, q),
              ),
              SwitchListTile(
                secondary: const Icon(
                    Icons.visibility_outlined),
                title: Text(s.t('showTranslation')),
                value: q.showTranslation,
                onChanged: (v) => ref
                    .read(quranPrefsProvider.notifier)
                    .update(
                        q.copyWith(showTranslation: v)),
              ),
              _Nav(
                icon: Icons.auto_stories_outlined,
                title: s.t('preferredTafsir'),
                subtitle: _tafsirLabel(q.tafsirId, s),
                onTap: () =>
                    _tafsirSheet(context, ref, q),
              ),
              _Nav(
                icon: Icons.format_list_numbered_outlined,
                title: s.t('ayahNumberStyle'),
                subtitle: q.ayahNumberStyle.name,
                onTap: () =>
                    _ayahNumberSheet(context, ref, q),
              ),
            ],
          ),
          _Group(
            icon: Icons.auto_stories,
            title: s.t('sunnahGroup'),
            children: [
              SwitchListTile(
                secondary:
                    const Icon(Icons.account_tree_outlined),
                title: Text(s.t('displaySanad')),
                value: app.displaySanad,
                onChanged: (v) => ref
                    .read(appPrefsProvider.notifier)
                    .update(
                        app.copyWith(displaySanad: v)),
              ),
              SwitchListTile(
                secondary:
                    const Icon(Icons.verified_outlined),
                title: Text(s.t('displayGrade')),
                value: app.displayGrade,
                onChanged: (v) => ref
                    .read(appPrefsProvider.notifier)
                    .update(
                        app.copyWith(displayGrade: v)),
              ),
            ],
          ),
          _Group(
            icon: Icons.palette_outlined,
            title: s.t('appearanceGroup'),
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(
                    horizontal: 16, vertical: 8),
                child: SegmentedButton<AppThemeMode>(
                  segments: [
                    ButtonSegment(
                        value: AppThemeMode.light,
                        label: Text(s.t('light'))),
                    ButtonSegment(
                        value: AppThemeMode.dark,
                        label: Text(s.t('dark'))),
                    ButtonSegment(
                        value: AppThemeMode.system,
                        label: Text(s.t('system'))),
                  ],
                  selected: {app.themeMode},
                  onSelectionChanged: (v) => ref
                      .read(appPrefsProvider.notifier)
                      .update(app.copyWith(
                          themeMode: v.first)),
                ),
              ),
              SwitchListTile(
                secondary:
                    const Icon(Icons.color_lens_outlined),
                title: Text(s.t('dynamicColor')),
                subtitle: Text(s.t('dynamicColorHint')),
                value: app.useDynamicColor,
                onChanged: (v) => ref
                    .read(appPrefsProvider.notifier)
                    .update(app.copyWith(
                        useDynamicColor: v)),
              ),
            ],
          ),
          _Group(
            icon: Icons.headphones_outlined,
            title: s.t('audioGroup'),
            children: [
              _Nav(
                icon: Icons.play_circle_outline,
                title: s.t('quranAudio'),
                onTap: () => Navigator.of(context).push(
                    MaterialPageRoute(
                        builder: (_) =>
                            const AudioPlayerScreen())),
              ),
            ],
          ),
          _Group(
            icon: Icons.language_outlined,
            title: s.t('languageGroup'),
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(
                    horizontal: 16, vertical: 8),
                child: SegmentedButton<String>(
                  segments: const [
                    ButtonSegment(
                        value: 'ar',
                        label: Text('العربية')),
                    ButtonSegment(
                        value: 'en',
                        label: Text('English')),
                    ButtonSegment(
                        value: 'fr',
                        label: Text('Français')),
                  ],
                  selected: {app.locale},
                  onSelectionChanged: (v) => ref
                      .read(appPrefsProvider.notifier)
                      .update(
                          app.copyWith(locale: v.first)),
                ),
              ),
            ],
          ),
          _Group(
            icon: Icons.storage_outlined,
            title: s.t('storageGroup'),
            children: [
              _Nav(
                icon: Icons.download_outlined,
                title: s.t('downloadManager'),
                onTap: () => Navigator.of(context).push(
                    MaterialPageRoute(
                        builder: (_) =>
                            const DownloadsScreen())),
              ),
              _Nav(
                icon: Icons.source_outlined,
                title: s.t('dataSourcesLicenses'),
                subtitle: s.t('contentProvenance'),
                onTap: () => Navigator.of(context).push(
                    MaterialPageRoute(
                        builder: (_) =>
                            const DataSourcesScreen())),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(s.t('privacyNote'),
              style: Theme.of(context).textTheme.bodySmall),
        ],
      ),
    );
  }

  String _riwayaAr(RiwayaId id) => switch (id) {
        RiwayaId.hafsAsim => 'حفص عن عاصم',
        RiwayaId.shubahAsim => 'شعبة عن عاصم',
        RiwayaId.warshNafi => 'ورش عن نافع',
        RiwayaId.qalunNafi => 'قالون عن نافع',
        RiwayaId.bazziIbnKathir => 'البزي عن ابن كثير',
        RiwayaId.qunbulIbnKathir => 'قنبل عن ابن كثير',
        RiwayaId.duriAbiAmr => 'الدوري عن أبي عمرو',
        RiwayaId.susiAbiAmr => 'السوسي عن أبي عمرو',
        RiwayaId.hishamIbnAmir => 'هشام عن ابن عامر',
        RiwayaId.ibnDhakwanIbnAmir => 'ابن ذكوان عن ابن عامر',
        RiwayaId.khalafHamzah => 'خلف عن حمزة',
        RiwayaId.khalladHamzah => 'خلاد عن حمزة',
        RiwayaId.abulHarithKisai => 'أبو الحارث عن الكسائي',
        RiwayaId.duriKisai => 'الدوري عن الكسائي',
      };

  String _tafsirLabel(String id, AppStrings s) {
    final info =
        kTafsirCatalog.where((t) => t.id == id).firstOrNull;
    if (info == null) {
      return id;
    }
    return s.isArabic ? info.titleAr : info.titleEn;
  }

  void _translationsSheet(
      BuildContext context, WidgetRef ref, QuranPrefs q) {
    final s = AppStrings.of(context);
    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      builder: (ctx) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment:
                CrossAxisAlignment.start,
            children: [
              Text(s.t('translation'),
                  style: const TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.w700)),
              ...kTranslationCatalog.map((t) => CheckboxListTile(
                    value: q.translations.contains(t.id),
                    onChanged: !t.bundled
                        ? null
                        : (v) {
                            final next = [...q.translations];
                            v == true
                                ? next.add(t.id)
                                : next.remove(t.id);
                            ref
                                .read(quranPrefsProvider.notifier)
                                .update(q.copyWith(translations: next));
                          },
                    title: Text(s.isArabic
                        ? t.language == 'ar'
                            ? t.translator
                            : '${t.translator} (${t.language})'
                        : '${t.translator} · ${t.language}'),
                    subtitle: Text(
                      '${t.source}${t.version == null ? '' : ' · ${t.version}'}${t.bundled ? '' : ' · ${s.t('notDownloaded')}'}',
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                  )),
            ],
          ),
        ),
      ),
    );
  }

  void _tafsirSheet(
      BuildContext context, WidgetRef ref, QuranPrefs q) {
    final s = AppStrings.of(context);
    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      builder: (ctx) => SafeArea(
        child: RadioGroup<String>(
          groupValue: q.tafsirId,
          onChanged: (v) async {
            if (v == null) {
              return;
            }
            await ref
                .read(quranPrefsProvider.notifier)
                .update(q.copyWith(tafsirId: v));
            if (ctx.mounted) {
              Navigator.pop(ctx);
            }
          },
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: kTafsirCatalog
                .map((t) => RadioListTile<String>(
                      value: t.id,
                      enabled: t.bundled,
                      title: Text(s.isArabic
                          ? t.titleAr
                          : t.titleEn),
                      subtitle: Text(t.bundled
                          ? t.source
                          : '${t.source} · ${s.t('notDownloaded')}'),
                    ))
                .toList(),
          ),
        ),
      ),
    );
  }

  void _ayahNumberSheet(
      BuildContext context, WidgetRef ref, QuranPrefs q) {
    showModalBottomSheet(
      context: context,
      showDragHandle: true,
      builder: (ctx) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: AyahNumberStyle.values
              .map((v) => RadioGroup<AyahNumberStyle>(
                    groupValue: q.ayahNumberStyle,
                    onChanged: (nv) {
                      if (nv == null) {
                        return;
                      }
                      ref
                          .read(quranPrefsProvider.notifier)
                          .update(q.copyWith(
                              ayahNumberStyle: nv));
                      Navigator.pop(ctx);
                    },
                    child: RadioListTile<AyahNumberStyle>(
                      value: v,
                      title: Text(v.name),
                    ),
                  ))
              .toList(),
        ),
      ),
    );
  }
}

class _Group extends StatelessWidget {
  const _Group(
      {required this.icon,
      required this.title,
      required this.children});

  final IconData icon;
  final String title;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Padding(
      padding: const EdgeInsets.only(bottom: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding:
                const EdgeInsets.fromLTRB(4, 8, 4, 8),
            child: Row(
              children: [
                Icon(icon,
                    size: 20, color: scheme.primary),
                const SizedBox(width: 8),
                Text(title,
                    style: Theme.of(context)
                        .textTheme
                        .titleMedium
                        ?.copyWith(
                            fontWeight: FontWeight.w700,
                            color: scheme.primary)),
              ],
            ),
          ),
          Card(
            margin: EdgeInsets.zero,
            child: Column(children: children),
          ),
        ],
      ),
    );
  }
}

class _Nav extends StatelessWidget {
  const _Nav(
      {required this.icon,
      required this.title,
      this.subtitle,
      this.onTap});

  final IconData icon;
  final String title;
  final String? subtitle;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return ListTile(
      leading: Icon(icon),
      title: Text(title),
      subtitle:
          subtitle == null ? null : Text(subtitle!),
      trailing: const Icon(Icons.chevron_right),
      onTap: onTap,
    );
  }
}
