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

class QuranPrefsNotifier extends StateNotifier<QuranPrefs> {
  QuranPrefsNotifier() : super(const QuranPrefs()) {
    final initial = state;
    _load(initial);
  }

  T _enumByName<T extends Enum>(List<T> values, String? name, T fallback) {
    if (name == null) return fallback;
    for (final v in values) {
      if (v.name == name) return v;
    }
    return fallback;
  }

  RiwayaId _loadRiwaya(SharedPreferences p) {
    final s = p.getString('q.riwayaName');
    if (s != null) {
      return _enumByName(RiwayaId.values, s, RiwayaId.hafsAsim);
    }
    final idx = p.getInt('q.riwaya') ?? 0;
    if (idx >= 0 && idx < RiwayaId.values.length) return RiwayaId.values[idx];
    return RiwayaId.hafsAsim;
  }

  Future<void> _load(QuranPrefs initial) async {
    final p = await SharedPreferences.getInstance();
    if (!identical(state, initial)) return;
    final riwaya = _loadRiwaya(p);
    var script = _enumByName(
        QuranScript.values, p.getString('q.scriptName'), QuranScript.uthmani);
    // Migrate legacy int keys once.
    if (p.getString('q.scriptName') == null && p.containsKey('q.script')) {
      final si = p.getInt('q.script') ?? 0;
      if (si >= 0 && si < QuranScript.values.length) {
        script = QuranScript.values[si];
      }
    }
    var font = _enumByName(
        QuranFont.values, p.getString('q.fontName'), QuranFont.uthmani);
    if (p.getString('q.fontName') == null && p.containsKey('q.font')) {
      final fi = p.getInt('q.font') ?? 0;
      if (fi >= 0 && fi < QuranFont.values.length) font = QuranFont.values[fi];
    }
    var mode = _enumByName(
        ReadingMode.values, p.getString('q.modeName'), ReadingMode.reading);
    if (p.getString('q.modeName') == null && p.containsKey('q.mode')) {
      final mi = p.getInt('q.mode') ?? 0;
      if (mi >= 0 && mi < ReadingMode.values.length) {
        mode = ReadingMode.values[mi];
      }
    }
    var numberStyle = _enumByName(AyahNumberStyle.values,
        p.getString('q.ayahNumberStyleName'), AyahNumberStyle.arabicIndic);
    if (p.getString('q.ayahNumberStyleName') == null &&
        p.containsKey('q.ayahNumberStyle')) {
      final ni = p.getInt('q.ayahNumberStyle') ?? 0;
      if (ni >= 0 && ni < AyahNumberStyle.values.length) {
        numberStyle = AyahNumberStyle.values[ni];
      }
    }
    final legacyTajweed = p.getString('q.scriptName') == null &&
        (p.getInt('q.script') != null &&
            () {
              final si = p.getInt('q.script') ?? 0;
              return si >= 0 &&
                  si < QuranScript.values.length &&
                  QuranScript.values[si] == QuranScript.tajweed;
            }());
    var loaded = state.copyWith(
      riwaya: riwaya,
      script: script.datasetScript,
      font: font,
      readingMode: mode,
      ayahNumberStyle: numberStyle,
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
    await p.setString('q.riwayaName', normalized.riwaya.name);
    await p.setString('q.scriptName', normalized.script.name);
    await p.setString('q.fontName', normalized.font.name);
    await p.setString('q.modeName', normalized.readingMode.name);
    await p.setString(
        'q.ayahNumberStyleName', normalized.ayahNumberStyle.name);
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

class AppPrefsNotifier extends StateNotifier<AppPrefs> {
  AppPrefsNotifier() : super(const AppPrefs()) {
    final initial = state;
    _load(initial);
  }

  Future<void> _load(AppPrefs initial) async {
    final p = await SharedPreferences.getInstance();
    if (!identical(state, initial)) return;
    final themeName = p.getString('app.themeName');
    AppThemeMode mode = AppThemeMode.system;
    if (themeName != null) {
      for (final v in AppThemeMode.values) {
        if (v.name == themeName) {
          mode = v;
          break;
        }
      }
    } else {
      final themeIndex = p.getInt('app.theme') ?? 0;
      if (themeIndex >= 0 && themeIndex < AppThemeMode.values.length) {
        // Legacy order was system(0?) — map safely, default system.
        // Old enum order: system, light, dark (current). Keep index compat.
        mode = AppThemeMode.values[themeIndex];
      }
    }
    state = state.copyWith(
      locale: p.getString('app.locale') ?? 'ar',
      themeMode: mode,
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
    await p.setString('app.themeName', next.themeMode.name);
    await p.setBool('app.dynamicColor', next.useDynamicColor);
    await p.setString('app.qari', next.qari);
    await p.setDouble('app.playbackSpeed', next.playbackSpeed);
    await p.setBool('app.onboarded', next.onboarded);
    await p.setBool('app.sanad', next.displaySanad);
    await p.setBool('app.grade', next.displayGrade);
  }
}

final quranPrefsProvider =
    StateNotifierProvider<QuranPrefsNotifier, QuranPrefs>(
        (ref) => QuranPrefsNotifier());

final appPrefsProvider =
    StateNotifierProvider<AppPrefsNotifier, AppPrefs>(
        (ref) => AppPrefsNotifier());
