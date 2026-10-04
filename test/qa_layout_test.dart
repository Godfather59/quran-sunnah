import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/core/l10n/app_strings.dart';
import 'package:quran_sunnah_app/core/navigation/adaptive_scaffold.dart';
import 'package:quran_sunnah_app/core/theme/app_theme.dart';
import 'package:quran_sunnah_app/data/database/app_database.dart';
import 'package:quran_sunnah_app/state/database_provider.dart';

Future<AppDatabase> _pumpAdaptive(
  WidgetTester tester, {
  required Size size,
  required Locale locale,
  required ThemeMode themeMode,
}) async {
  tester.view.devicePixelRatio = 1;
  tester.view.physicalSize = size;
  addTearDown(tester.view.resetPhysicalSize);
  addTearDown(tester.view.resetDevicePixelRatio);

  final db = AppDatabase.memory();
  addTearDown(db.close);
  await tester.pumpWidget(
    ProviderScope(
      overrides: [
        appDatabaseProvider.overrideWithValue(Future.value(db)),
      ],
      child: MaterialApp(
        theme: AppTheme.light(null),
        darkTheme: AppTheme.dark(null),
        themeMode: themeMode,
        locale: locale,
        supportedLocales: AppStrings.supported,
        localizationsDelegates: const [
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
          GlobalCupertinoLocalizations.delegate,
        ],
        home: const AdaptiveScaffold(),
      ),
    ),
  );
  await tester.pump();
  await tester.pump(const Duration(milliseconds: 100));
  return db;
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('phone layout is RTL and dark in Arabic without overflow',
      (tester) async {
    await _pumpAdaptive(
      tester,
      size: const Size(360, 640),
      locale: const Locale('ar'),
      themeMode: ThemeMode.dark,
    );
    expect(find.byType(NavigationBar), findsOneWidget);
    expect(find.byType(NavigationRail), findsNothing);

    final context = tester.element(find.byType(AdaptiveScaffold));
    expect(Directionality.of(context), TextDirection.rtl);
    expect(Theme.of(context).brightness, Brightness.dark);
    expect(tester.takeException(), isNull);
  });

  testWidgets('large layout switches to navigation rail in LTR',
      (tester) async {
    final db = await _pumpAdaptive(
      tester,
      size: const Size(1200, 900),
      locale: const Locale('en'),
      themeMode: ThemeMode.light,
    );
    expect(find.byType(NavigationRail), findsOneWidget);
    expect(find.byType(NavigationBar), findsNothing);

    final context = tester.element(find.byType(AdaptiveScaffold));
    expect(Directionality.of(context), TextDirection.ltr);
    expect(Theme.of(context).brightness, Brightness.light);
    expect(tester.takeException(), isNull);
  });
}
