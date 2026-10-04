import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/core/l10n/app_strings.dart';
import 'package:quran_sunnah_app/data/content/content_packages.dart';
import 'package:quran_sunnah_app/features/onboarding/onboarding_screen.dart';

Future<void> _pumpStable(WidgetTester tester) async {
  await tester.pump();
  await tester.pump(const Duration(milliseconds: 350));
  await tester.pump(const Duration(milliseconds: 350));
}

Widget _app() => const ProviderScope(
      child: MaterialApp(
        locale: Locale('en'),
        supportedLocales: AppStrings.supported,
        localizationsDelegates: [
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
          GlobalCupertinoLocalizations.delegate,
        ],
        home: OnboardingScreen(),
      ),
    );

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late Directory packageRoot;

  setUp(() async {
    packageRoot =
        await Directory.systemTemp.createTemp('content-packages-onboarding-');
    ContentPackageStore.instance.resetForTesting();
    ContentPackageStore.instance.setRootDirectoryForTesting(packageRoot);
    await ContentPackageStore.instance.manifest();
  });

  tearDown(() async {
    ContentPackageStore.instance.resetForTesting();
    if (await packageRoot.exists()) {
      await packageRoot.delete(recursive: true);
    }
  });

  testWidgets('onboarding applies the selected language immediately',
      (tester) async {
    await tester.pumpWidget(_app());
    await _pumpStable(tester);

    // Onboarding defaults to Arabic even if the surrounding app locale is EN.
    expect(find.text('اختر اللغة'), findsOneWidget);
    expect(find.text('التالي'), findsOneWidget);

    await tester.tap(find.text('English'));
    await _pumpStable(tester);
    expect(find.text('Choose language'), findsOneWidget);
    expect(find.text('Next'), findsOneWidget);

    await tester.tap(find.text('العربية'));
    await _pumpStable(tester);
    expect(find.text('اختر اللغة'), findsOneWidget);
    expect(find.text('التالي'), findsOneWidget);

    final context = tester.element(find.byType(Scaffold));
    expect(Directionality.of(context), TextDirection.rtl);
  });

  testWidgets('additional content step shows real offline choices in Arabic',
      (tester) async {
    await tester.pumpWidget(_app());
    await _pumpStable(tester);

    // Language -> Riwaya -> Script -> Hadith sources -> Additional content.
    for (var i = 0; i < 4; i++) {
      await tester.tap(find.text('التالي'));
      await _pumpStable(tester);
    }

    expect(find.text('محتوى إضافي'), findsOneWidget);
    expect(find.text('ابدأ'), findsOneWidget);

    // Preloaded manifest makes the package choices deterministic.
    expect(
      find.text('الترجمة الإنجليزية — صحيح إنترناشونال'),
      findsOneWidget,
    );

    // Lower controls are intentionally below the small test viewport.
    // Their package behavior is covered by content_packages_test; here we
    // keep the onboarding test focused on localized chrome and real choices.
    final ar = AppStrings(const Locale('ar'));
    expect(ar.t('jalalayn'), 'تفسير الجلالين');
    expect(ar.t('tajweedColors'), 'ألوان التجويد');
    expect(ar.t('selectedDownloadSize'), 'حجم التنزيل المحدد');
  });
}
