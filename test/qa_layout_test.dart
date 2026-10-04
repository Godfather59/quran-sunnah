import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/app.dart';
import 'package:quran_sunnah_app/core/navigation/adaptive_scaffold.dart';
import 'package:quran_sunnah_app/data/database/app_database.dart';
import 'package:quran_sunnah_app/state/database_provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

Future<AppDatabase> _pumpConfiguredApp(
  WidgetTester tester, {
  required Size size,
  required String locale,
  required int themeIndex,
}) async {
  tester.view.devicePixelRatio = 1;
  tester.view.physicalSize = size;
  addTearDown(tester.view.resetPhysicalSize);
  addTearDown(tester.view.resetDevicePixelRatio);

  SharedPreferences.setMockInitialValues({
    'app.onboarded': true,
    'app.locale': locale,
    'app.theme': themeIndex,
  });

  final db = AppDatabase.memory();
  await tester.pumpWidget(
    ProviderScope(
      overrides: [
        appDatabaseProvider.overrideWithValue(Future.value(db)),
      ],
      child: const QuranSunnahApp(),
    ),
  );
  await tester.pump();
  await tester.pump(const Duration(milliseconds: 50));
  return db;
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('phone layout is RTL and dark in Arabic without overflow',
      (tester) async {
    final db = await _pumpConfiguredApp(
      tester,
      size: const Size(360, 640),
      locale: 'ar',
      themeIndex: 2,
    );
    addTearDown(db.close);

    expect(find.byType(AdaptiveScaffold), findsOneWidget);
    expect(find.byType(NavigationBar), findsOneWidget);
    expect(find.byType(NavigationRail), findsNothing);

    final context = tester.element(find.byType(AdaptiveScaffold));
    expect(Directionality.of(context), TextDirection.rtl);
    expect(Theme.of(context).brightness, Brightness.dark);
    expect(tester.takeException(), isNull);
  });

  testWidgets('large layout switches to navigation rail in LTR',
      (tester) async {
    final db = await _pumpConfiguredApp(
      tester,
      size: const Size(1200, 900),
      locale: 'en',
      themeIndex: 1,
    );
    addTearDown(db.close);

    expect(find.byType(AdaptiveScaffold), findsOneWidget);
    expect(find.byType(NavigationRail), findsOneWidget);
    expect(find.byType(NavigationBar), findsNothing);

    final context = tester.element(find.byType(AdaptiveScaffold));
    expect(Directionality.of(context), TextDirection.ltr);
    expect(Theme.of(context).brightness, Brightness.light);
    expect(tester.takeException(), isNull);
  });
}
