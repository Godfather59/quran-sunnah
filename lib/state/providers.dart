import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../data/models/quran.dart';
import '../data/repositories/quran_metadata.dart';

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

  String get editionId => '${riwaya.storageKey}__${script.name}';

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
      );
}

class QuranPrefsNotifier extends StateNotifier<QuranPrefs> {
  QuranPrefsNotifier() : super(const QuranPrefs()) {
    _load();
  }

  Future<void> _load() async {
    final p = await SharedPreferences.getInstance();
    state = state.copyWith(
      riwaya: RiwayaId.values[p.getInt('q.riwaya') ?? 0],
      script: QuranScript.values[p.getInt('q.script') ?? 0],
      readingMode: ReadingMode.values[p.getInt('q.mode') ?? 0],
      fontSize: p.getDouble('q.fontSize') ?? 24,
      lastSurah: p.getInt('q.lastSurah') ?? 2,
      lastAyah: p.getInt('q.lastAyah') ?? 255,
      showTranslation: p.getBool('q.showTr') ?? true,
    );
  }

  Future<void> update(QuranPrefs next) async {
    state = next;
    final p = await SharedPreferences.getInstance();
    await p.setInt('q.riwaya', next.riwaya.index);
    await p.setInt('q.script', next.script.index);
    await p.setInt('q.mode', next.readingMode.index);
    await p.setDouble('q.fontSize', next.fontSize);
    await p.setInt('q.lastSurah', next.lastSurah);
    await p.setInt('q.lastAyah', next.lastAyah);
    await p.setBool('q.showTr', next.showTranslation);
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
    _load();
  }

  Future<void> _load() async {
    final p = await SharedPreferences.getInstance();
    state = state.copyWith(
      locale: p.getString('app.locale') ?? 'ar',
      themeMode:
          AppThemeMode.values[p.getInt('app.theme') ?? 0],
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
