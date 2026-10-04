import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/core/l10n/app_strings.dart';
import 'package:quran_sunnah_app/core/navigation/adaptive_scaffold.dart';
import 'package:quran_sunnah_app/data/content/content_packages.dart';
import 'package:quran_sunnah_app/data/database/app_database.dart';
import 'package:quran_sunnah_app/features/onboarding/onboarding_screen.dart';
import 'package:quran_sunnah_app/state/database_provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

Future<void> _pumpStable(WidgetTester tester) async {
  await tester.pump();
  await tester.pump(const Duration(milliseconds: 350));
  await tester.pump(const Duration(milliseconds: 350));
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late Directory packageRoot;

  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    packageRoot =
        await Directory.systemTemp.createTemp('content-packages-skip-');
    ContentPackageStore.instance.resetForTesting();
    ContentPackageStore.instance.setRootDirectoryForTesting(packageRoot);
  });

  tearDown(() async {
    ContentPackageStore.instance.resetForTesting();
    if (await packageRoot.exists()) {
      await packageRoot.delete(recursive: true);
    }
  });

  testWidgets('failed downloads surface skip and finish offline',
      (tester) async {
    final db = AppDatabase.memory();
    addTearDown(db.close);
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          appDatabaseProvider.overrideWithValue(Future.value(db)),
          // Deterministic offline: the manifest never resolves, so every
          // optional download fails fast instead of hanging on network.
          contentPackageManifestProvider.overrideWith(
            (ref) async {
              await Future<void>.delayed(
                  const Duration(milliseconds: 100));
              throw const SocketException('offline');
            },
          ),
        ],
        child: const MaterialApp(
          locale: Locale('en'),
          supportedLocales: AppStrings.supported,
          localizationsDelegates: [
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          home: OnboardingScreen(),
        ),
      ),
    );
    await _pumpStable(tester);

    // Onboarding runs in Arabic by default: language -> riwaya ->
    // script -> sources (pick a downloadable extra) -> content.
    for (var i = 0; i < 3; i++) {
      await tester.tap(find.text('التالي'));
      await _pumpStable(tester);
    }

    // Select Abu Dawud so the finish attempts a real optional download.
    await tester.tap(find.text('سنن أبي داود'));
    await _pumpStable(tester);
    await tester.tap(find.text('التالي'));
    await _pumpStable(tester);
    await tester.pump(const Duration(seconds: 1));

    await tester.tap(find.text('ابدأ'));
    await tester.pump();
    await tester.pump(const Duration(seconds: 1));
    await tester.pump(const Duration(seconds: 1));

    // Offline failure is reported with a way out instead of a dead end.
    expect(find.text('تخطي'), findsWidgets);

    await tester.tap(find.text('تخطي'));
    await tester.pump();
    await tester.pump(const Duration(seconds: 1));
    await tester.pump(const Duration(seconds: 1));
    expect(find.byType(AdaptiveScaffold), findsOneWidget);
    expect(tester.takeException(), isNull);
  });
}
