import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/l10n/app_strings.dart';
import '../core/theme/app_theme.dart';
import '../state/providers.dart';
import 'core/navigation/adaptive_scaffold.dart';
import 'features/splash/splash_screen.dart';
import 'features/onboarding/onboarding_screen.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

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
