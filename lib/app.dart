import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'core/l10n/app_strings.dart';
import 'core/theme/app_theme.dart';
import 'state/providers.dart';
import 'core/navigation/adaptive_scaffold.dart';
import 'features/splash/splash_screen.dart';
import 'features/onboarding/onboarding_screen.dart';
import 'features/quran/quran_reader_screen.dart';
import 'features/dhikr/dhikr_screen.dart';
import 'features/memorization/memorization_screen.dart';
import 'features/prayer/prayer_screen.dart';
import 'features/search/global_search_screen.dart';
import 'features/settings/settings_screen.dart';
import 'features/library/library_screen.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

/// Parses "2:255", "/quran/2/255", "quran://2/255" -> (surah, ayah).
(int, int)? parseQuranRef(String raw) {
  final s = raw.trim();
  final m1 = RegExp(r'^(\d{1,3})\s*:\s*(\d{1,3})$').firstMatch(s);
  if (m1 != null) {
    return (int.parse(m1.group(1)!), int.parse(m1.group(2)!));
  }
  final m2 = RegExp(r'quran[:/]+(\d{1,3})/(\d{1,3})').firstMatch(s);
  if (m2 != null) {
    return (int.parse(m2.group(1)!), int.parse(m2.group(2)!));
  }
  return null;
}

/// Pushes a Quran reference like "2:255" via named-route handling.
/// Returns true if handled.
bool pushQuranRef(BuildContext context, String raw) {
  final parsed = parseQuranRef(raw);
  if (parsed == null) return false;
  final (su, ay) = parsed;
  if (su < 1 || su > 114 || ay < 1) return false;
  Navigator.of(context).push(
    MaterialPageRoute(
      builder: (_) => QuranReaderScreen(surah: su, initialAyah: ay),
      settings: RouteSettings(name: '$su:$ay'),
    ),
  );
  return true;
}

class QuranSunnahApp extends ConsumerWidget {
  const QuranSunnahApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final prefs = ref.watch(appPrefsProvider);

    return DynamicSchemes(
      enabled: prefs.useDynamicColor,
      lightFallback: AppTheme.light(null),
      darkFallback: AppTheme.dark(null),
      builder: (ctx, light, dark) {
        return MaterialApp(
          debugShowCheckedModeBanner: false,
          title: 'Quran & Sunnah',
          theme: light,
          darkTheme: dark,
          themeMode: switch (prefs.themeMode) {
            AppThemeMode.light => ThemeMode.light,
            AppThemeMode.dark => ThemeMode.dark,
            AppThemeMode.system => ThemeMode.system,
          },
          locale: Locale(prefs.locale),
          supportedLocales: AppStrings.supported,
          localizationsDelegates: const [
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          home: prefs.onboarded
              ? const AdaptiveScaffold()
              : const _LaunchDecider(),
          onGenerateRoute: (settings) {
            final name = (settings.name ?? '').trim();
            // Static routes.
            switch (name) {
              case '/search':
                return MaterialPageRoute(
                  builder: (_) => const GlobalSearchScreen(),
                  settings: settings,
                );
              case '/prayer':
                return MaterialPageRoute(
                  builder: (_) => const PrayerScreen(),
                  settings: settings,
                );
              case '/memorization':
                return MaterialPageRoute(
                  builder: (_) =>
                      const MemorizationScreen(),
                  settings: settings,
                );
              case '/dhikr':
                return MaterialPageRoute(
                  builder: (_) => const DhikrScreen(),
                  settings: settings,
                );
              case '/settings':
                return MaterialPageRoute(
                  builder: (_) => const SettingsScreen(),
                  settings: settings,
                );
              case '/library':
                return MaterialPageRoute(
                  builder: (_) => const LibraryScreen(),
                  settings: settings,
                );
            }
            final refKeys = parseQuranRef(name);
            if (refKeys != null) {
              final (su, ay) = refKeys;
              if (su >= 1 && su <= 114 && ay >= 1) {
                return MaterialPageRoute(
                  builder: (_) =>
                      QuranReaderScreen(surah: su, initialAyah: ay),
                  settings: settings,
                );
              }
            }
            return null;
          },
        );
      },
    );
  }
}

class _LaunchDecider extends ConsumerStatefulWidget {
  const _LaunchDecider();

  @override
  ConsumerState<_LaunchDecider> createState() => _LaunchDeciderState();
}

class _LaunchDeciderState extends ConsumerState<_LaunchDecider> {
  bool _ready = false;

  @override
  void initState() {
    super.initState();
    Future.delayed(const Duration(milliseconds: 1400), () {
      if (mounted) setState(() => _ready = true);
    });
  }

  @override
  Widget build(BuildContext context) {
    if (!_ready) return const SplashScreen();
    return const OnboardingScreen();
  }
}
