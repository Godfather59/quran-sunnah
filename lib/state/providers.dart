import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../data/models/quran.dart';
import '../data/repositories/quran_metadata.dart';
import '../data/repositories/verified_asset_quran_repository.dart';

/// Verified structural metadata (Juz/Hizb/Page). Hafs/Medina mapping.
final quranMetadataProvider = FutureProvider<QuranMetadata>(
    (ref) => QuranMetadata.load());

// ── Quran reading preferences ────────────────────────────────

class QuranPrefs {
  const QuranPrefs({
    this.riwaya = RiwayaId.hafsAsim,
    this.script = QuranScript.uthmani,
    this.font = QuranFont.uthmani,
    this.fontSize = 24,
    this.lineHeight = 1.9,
    this.ayahSpacing = 12,
    this.margins = 16,
    this.readingMode = ReadingMode.reading,
    this.ayahNumberStyle = AyahNumberStyle.arabicIndic,
    this.translations = const ['en-sahih'],
    this.tafsirId = 'jalalayn',
    this.lastSurah = 2,
    this.lastAyah = 255,
    this.showTranslation = true,
    this.showTajweed = false,
  });

  final RiwayaId riwaya;
  final QuranScript script;
  final QuranFont font;
  final double fontSize;
  final double lineHeight;
  final double ayahSpacing;
  final double margins;
  final ReadingMode readingMode;
  final AyahNumberStyle ayahNumberStyle;
  final List<String> translations;
  final String tafsirId;
  final int lastSurah;
  final int lastAyah;
  final bool showTranslation;
  final bool showTajweed;

  QuranScript get datasetScript => script.datasetScript;
  String get editionId => '${riwaya.storageKey}__${datasetScript.name}';

  bool get tajweedAvailable =>
      riwaya == RiwayaId.hafsAsim &&
      datasetScript == QuranScript.uthmani;

  QuranPrefs copyWith({
    RiwayaId? riwaya,
    QuranScript? script,
    QuranFont? font,
    double? fontSize,
    double? lineHeight,
    double? ayahSpacing,
    double? margins,
    ReadingMode? readingMode,
    AyahNumberStyle? ayahNumberStyle,
    List<String>? translations,
    String? tafsirId,
    int? lastSurah,
    int? lastAyah,
    bool? showTranslation,
    bool? showTajweed,
  }) =>
      QuranPrefs(
        riwaya: riwaya ?? this.riwaya,
        script: script ?? this.script,
        font: font ?? this.font,
        fontSize: fontSize ?? this.fontSize,
        lineHeight: lineHeight ?? this.lineHeight,
        ayahSpacing: ayahSpacing ?? this.ayahSpacing,
        margins: margins ?? this.margins,
        readingMode: readingMode ?? this.readingMode,
        ayahNumberStyle: ayahNumberStyle ?? this.ayahNumberStyle,
        translations: translations ?? this.translations,
        tafsirId: tafsirId ?? this.tafsirId,
        lastSurah: lastSurah ?? this.lastSurah,
        lastAyah: lastAyah ?? this.lastAyah,
        showTranslation: showTranslation ?? this.showTranslation,
        showTajweed: showTajweed ?? this.showTajweed,
      );
}

class QuranPrefsNotifier extends Notifier<QuranPrefs> {
  @override
  QuranPrefs build() {
    const initial = QuranPrefs();
    Future.microtask(() => _load(initial));
    return initial;
  }

  Future<void> _load(QuranPrefs initial) async {
    final p = await SharedPreferences.getInstance();
    if (!ref.mounted) return;
    if (!identical(state, initial)) return;
    final riwayaIndex = p.getInt('q.riwaya') ?? 0;
    final scriptIndex = p.getInt('q.script') ?? 0;
    final modeIndex = p.getInt('q.mode') ?? 0;
    final fontIndex = p.getInt('q.font') ?? 0;
    final numberStyleIndex = p.getInt('q.ayahNumberStyle') ?? 0;

    final riwaya = riwayaIndex >= 0 && riwayaIndex < RiwayaId.values.length
        ? RiwayaId.values[riwayaIndex]
        : RiwayaId.hafsAsim;
    var script = scriptIndex >= 0 && scriptIndex < QuranScript.values.length
        ? QuranScript.values[scriptIndex]
        : QuranScript.uthmani;
    final legacyTajweed = script == QuranScript.tajweed;
    script = script.datasetScript;

    var loaded = state.copyWith(
      riwaya: riwaya,
      script: script,
      font: fontIndex >= 0 && fontIndex < QuranFont.values.length
          ? QuranFont.values[fontIndex]
          : QuranFont.uthmani,
      readingMode: modeIndex >= 0 && modeIndex < ReadingMode.values.length
          ? ReadingMode.values[modeIndex]
          : ReadingMode.reading,
      ayahNumberStyle: numberStyleIndex >= 0 &&
              numberStyleIndex < AyahNumberStyle.values.length
          ? AyahNumberStyle.values[numberStyleIndex]
          : AyahNumberStyle.arabicIndic,
      fontSize: p.getDouble('q.fontSize') ?? 24,
      lineHeight: p.getDouble('q.lineHeight') ?? 1.9,
      ayahSpacing: p.getDouble('q.ayahSpacing') ?? 12,
      margins: p.getDouble('q.margins') ?? 16,
      translations: p.getStringList('q.translations') ?? const ['en-sahih'],
      tafsirId: p.getString('q.tafsirId') ?? 'jalalayn',
      lastSurah: p.getInt('q.lastSurah') ?? 2,
      lastAyah: p.getInt('q.lastAyah') ?? 255,
      showTranslation: p.getBool('q.showTr') ?? true,
      showTajweed: p.getBool('q.showTajweed') ?? legacyTajweed,
    );
    loaded = _normalize(loaded);
    state = loaded;
  }

  QuranPrefs _normalize(QuranPrefs next) {
    var script = next.datasetScript;
    var editionId = '${next.riwaya.storageKey}__${script.name}';
    if (!kVerifiedQuranAssets.containsKey(editionId)) {
      script = QuranScript.uthmani;
      editionId = '${next.riwaya.storageKey}__${script.name}';
    }
    // Pending riwayat have no verified text yet. Keep the previous valid
    // riwaya rather than persisting a selection that can never render.
    var riwaya = next.riwaya;
    if (!kVerifiedQuranAssets.containsKey(editionId)) {
      riwaya = RiwayaId.hafsAsim;
      script = QuranScript.uthmani;
    }
    final normalized = next.copyWith(riwaya: riwaya, script: script);
    return normalized.copyWith(
      showTajweed: normalized.tajweedAvailable && next.showTajweed,
    );
  }

  Future<void> update(QuranPrefs next) async {
    final normalized = _normalize(next);
    state = normalized;
    final p = await SharedPreferences.getInstance();
    await p.setInt('q.riwaya', normalized.riwaya.index);
    await p.setInt('q.script', normalized.script.index);
    await p.setInt('q.font', normalized.font.index);
    await p.setInt('q.mode', normalized.readingMode.index);
    await p.setInt('q.ayahNumberStyle', normalized.ayahNumberStyle.index);
    await p.setDouble('q.fontSize', normalized.fontSize);
    await p.setDouble('q.lineHeight', normalized.lineHeight);
    await p.setDouble('q.ayahSpacing', normalized.ayahSpacing);
    await p.setDouble('q.margins', normalized.margins);
    await p.setStringList('q.translations', normalized.translations);
    await p.setString('q.tafsirId', normalized.tafsirId);
    await p.setInt('q.lastSurah', normalized.lastSurah);
    await p.setInt('q.lastAyah', normalized.lastAyah);
    await p.setBool('q.showTr', normalized.showTranslation);
    await p.setBool('q.showTajweed', normalized.showTajweed);
  }
}

// ── App-wide prefs (locale / theme / audio) ──────────────────

enum AppThemeMode { system, light, dark }

class AppPrefs {
  const AppPrefs({
    this.locale = 'ar',
    this.themeMode = AppThemeMode.system,
    this.useDynamicColor = false,
    this.qari = '',
    this.playbackSpeed = 1.0,
    this.onboarded = false,
    this.displaySanad = true,
    this.displayGrade = true,
  });

  final String locale;
  final AppThemeMode themeMode;
  final bool useDynamicColor;
  final String qari;
  final double playbackSpeed;
  final bool onboarded;
  final bool displaySanad;
  final bool displayGrade;

  AppPrefs copyWith({
    String? locale,
    AppThemeMode? themeMode,
    bool? useDynamicColor,
    String? qari,
    double? playbackSpeed,
    bool? onboarded,
    bool? displaySanad,
    bool? displayGrade,
  }) =>
      AppPrefs(
        locale: locale ?? this.locale,
        themeMode: themeMode ?? this.themeMode,
        useDynamicColor: useDynamicColor ?? this.useDynamicColor,
        qari: qari ?? this.qari,
        playbackSpeed: playbackSpeed ?? this.playbackSpeed,
        onboarded: onboarded ?? this.onboarded,
        displaySanad: displaySanad ?? this.displaySanad,
        displayGrade: displayGrade ?? this.displayGrade,
      );
}

class AppPrefsNotifier extends Notifier<AppPrefs> {
  @override
  AppPrefs build() {
    const initial = AppPrefs();
    Future.microtask(() => _load(initial));
    return initial;
  }

  Future<void> _load(AppPrefs initial) async {
    final p = await SharedPreferences.getInstance();
    if (!ref.mounted) return;
    if (!identical(state, initial)) return;
    final themeIndex = p.getInt('app.theme') ?? 0;
    state = state.copyWith(
      locale: p.getString('app.locale') ?? 'ar',
      themeMode: themeIndex >= 0 && themeIndex < AppThemeMode.values.length
          ? AppThemeMode.values[themeIndex]
          : AppThemeMode.system,
      useDynamicColor: p.getBool('app.dynamicColor') ?? false,
      qari: p.getString('app.qari') ?? '',
      playbackSpeed: p.getDouble('app.playbackSpeed') ?? 1.0,
      onboarded: p.getBool('app.onboarded') ?? false,
      displaySanad: p.getBool('app.sanad') ?? true,
      displayGrade: p.getBool('app.grade') ?? true,
    );
  }

  Future<void> update(AppPrefs next) async {
    state = next;
    final p = await SharedPreferences.getInstance();
    await p.setString('app.locale', next.locale);
    await p.setInt('app.theme', next.themeMode.index);
    await p.setBool('app.dynamicColor', next.useDynamicColor);
    await p.setString('app.qari', next.qari);
    await p.setDouble('app.playbackSpeed', next.playbackSpeed);
    await p.setBool('app.onboarded', next.onboarded);
    await p.setBool('app.sanad', next.displaySanad);
    await p.setBool('app.grade', next.displayGrade);
  }
}

final quranPrefsProvider =
    NotifierProvider<QuranPrefsNotifier, QuranPrefs>(
        QuranPrefsNotifier.new);

final appPrefsProvider =
    NotifierProvider<AppPrefsNotifier, AppPrefs>(
        AppPrefsNotifier.new);
