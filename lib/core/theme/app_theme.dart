import 'package:dynamic_color/dynamic_color.dart';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../data/models/quran.dart';

/// Own calm identity — no default Material purple anywhere.
/// Warm paper + deep ink + muted teal + bronze. Subtle, premium,
/// respectful. Dynamic Color is opt-in (Settings → Appearance).
class AppTheme {
  // Brand primitives.
  static const _ink = Color(0xFF1A2E2A); // deep green-ink
  static const _teal = Color(0xFF0F6A5F); // muted deep teal
  static const _bronze = Color(0xFF9A7B4F); // warm bronze accent
  static const _paper = Color(0xFFF7F3EA); // warm paper background
  static const _sand = Color(0xFFEDE6D6); // sand container
  static const _night = Color(0xFF0E1513); // night background
  static const _nightSurface = Color(0xFF182220);
  static const _tealLight = Color(0xFF8FD0C2); // teal for dark mode
  static const _bronzeLight = Color(0xFFD3B98C);

  static const ColorScheme lightScheme = ColorScheme(
    brightness: Brightness.light,
    primary: _teal,
    onPrimary: Colors.white,
    primaryContainer: Color(0xFFD5EAE4),
    onPrimaryContainer: _ink,
    secondary: _bronze,
    onSecondary: Colors.white,
    secondaryContainer: _sand,
    onSecondaryContainer: Color(0xFF4A3F2C),
    tertiary: Color(0xFF5B7A6E),
    onTertiary: Colors.white,
    tertiaryContainer: Color(0xFFE3EDE7),
    onTertiaryContainer: _ink,
    error: Color(0xFFA63A2E),
    onError: Colors.white,
    errorContainer: Color(0xFFF3DAD4),
    onErrorContainer: Color(0xFF4A1D17),
    surface: Color(0xFFFFFDF7),
    onSurface: _ink,
    surfaceContainerHighest: _sand,
    surfaceContainerHigh: Color(0xFFF3EDDF),
    surfaceContainer: Color(0xFFFAF6EC),
    surfaceContainerLow: Color(0xFFFFFDF7),
    surfaceContainerLowest: Colors.white,
    onSurfaceVariant: Color(0xFF4E5D58),
    outline: Color(0xFF7E8D87),
    outlineVariant: Color(0xFFD8D2C0),
    scrim: Colors.black,
    inverseSurface: _ink,
    onInverseSurface: _paper,
    inversePrimary: _tealLight,
    shadow: Colors.black,
    surfaceTint: _teal,
  );

  static const ColorScheme darkScheme = ColorScheme(
    brightness: Brightness.dark,
    primary: _tealLight,
    onPrimary: Color(0xFF06231F),
    primaryContainer: Color(0xFF0B3B34),
    onPrimaryContainer: Color(0xFFD5EAE4),
    secondary: _bronzeLight,
    onSecondary: Color(0xFF2E2515),
    secondaryContainer: Color(0xFF3A3423),
    onSecondaryContainer: Color(0xFFEDE6D6),
    tertiary: Color(0xFFA9C6BB),
    onTertiary: Color(0xFF0B2420),
    tertiaryContainer: Color(0xFF24423B),
    onTertiaryContainer: Color(0xFFE3EDE7),
    error: Color(0xFFE5A396),
    onError: Color(0xFF4A1D17),
    errorContainer: Color(0xFF5E231B),
    onErrorContainer: Color(0xFFF3DAD4),
    surface: _nightSurface,
    onSurface: Color(0xFFE9E7DC),
    surfaceContainerHighest: Color(0xFF243330),
    surfaceContainerHigh: Color(0xFF1E2C29),
    surfaceContainer: Color(0xFF182220),
    surfaceContainerLow: Color(0xFF131D1A),
    surfaceContainerLowest: _night,
    onSurfaceVariant: Color(0xFFB9C6C0),
    outline: Color(0xFF84948D),
    outlineVariant: Color(0xFF33433F),
    scrim: Colors.black,
    inverseSurface: _paper,
    onInverseSurface: _ink,
    inversePrimary: _teal,
    shadow: Colors.black,
    surfaceTint: _tealLight,
  );

  static ThemeData light(ColorScheme? dynamic) =>
      _base(dynamic ?? lightScheme);

  static ThemeData dark(ColorScheme? dynamic) =>
      _base(dynamic ?? darkScheme);

  static ThemeData _base(ColorScheme scheme) {
    var theme = ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      scaffoldBackgroundColor: scheme.surface,
      visualDensity: VisualDensity.standard,
    );
    return theme.copyWith(
      textTheme: GoogleFonts.notoNaskhArabicTextTheme(theme.textTheme),
      appBarTheme: theme.appBarTheme.copyWith(
        centerTitle: false,
        backgroundColor: scheme.surface,
        foregroundColor: scheme.onSurface,
      ),
      cardTheme: theme.cardTheme.copyWith(
        elevation: 0,
        color: scheme.surfaceContainer,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
          side: BorderSide(
              color: scheme.outlineVariant.withValues(alpha: 0.7)),
        ),
      ),
      navigationBarTheme: theme.navigationBarTheme.copyWith(
        backgroundColor: scheme.surfaceContainer,
        indicatorColor: scheme.primaryContainer,
      ),
      navigationRailTheme: theme.navigationRailTheme.copyWith(
        backgroundColor: scheme.surfaceContainer,
        indicatorColor: scheme.primaryContainer,
      ),
      floatingActionButtonTheme:
          theme.floatingActionButtonTheme.copyWith(
        backgroundColor: scheme.primary,
        foregroundColor: scheme.onPrimary,
      ),
      chipTheme: theme.chipTheme.copyWith(
        selectedColor: scheme.primaryContainer,
      ),
    );
  }

  /// Arabic body style with priority for Arabic glyphs.
  /// [font] is the user's font preference; [indopak] forces the
  /// Extended-B-capable Amiri Quran face (fetched once, cached offline).
  static TextStyle quranArabic(
    BuildContext context, {
    double size = 24,
    double height = 1.9,
    QuranFont? font,
    bool indopak = false,
  }) {
    final base = Theme.of(context).textTheme.bodyLarge!;
    final family = indopak
        ? 'Amiri Quran'
        : switch (font) {
            null || QuranFont.uthmani => null,
            QuranFont.naskh || QuranFont.notoNaskh =>
              'Noto Naskh Arabic',
            QuranFont.indopak => 'Amiri Quran',
          };
    if (family == null) {
      return GoogleFonts.amiri(
        fontSize: size,
        height: height,
        color: base.color,
      );
    }
    return GoogleFonts.getFont(
      family,
      fontSize: size,
      height: height,
      color: base.color,
    );
  }

  /// Translation must NEVER look like Quran: smaller, muted, latin-first.
  static TextStyle translation(BuildContext context) {
    final t = Theme.of(context).textTheme;
    return (t.bodyMedium ?? const TextStyle()).copyWith(
      color: t.bodyMedium?.color?.withValues(alpha: 0.72),
      height: 1.6,
    );
  }
}

class DynamicSchemes extends StatelessWidget {
  const DynamicSchemes({
    super.key,
    required this.enabled,
    required this.lightFallback,
    required this.darkFallback,
    required this.builder,
  });

  /// When false, the brand schemes are used untouched.
  final bool enabled;
  final ThemeData lightFallback;
  final ThemeData darkFallback;
  final Widget Function(BuildContext, ThemeData light, ThemeData dark) builder;

  @override
  Widget build(BuildContext context) {
    if (!enabled) {
      return builder(context, lightFallback, darkFallback);
    }
    return DynamicColorBuilder(
      builder: (lightDynamic, darkDynamic) {
        return builder(
          context,
          AppTheme.light(lightDynamic),
          AppTheme.dark(darkDynamic),
        );
      },
    );
  }
}
