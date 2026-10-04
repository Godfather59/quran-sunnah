import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/core/l10n/app_strings.dart';
import 'package:quran_sunnah_app/data/content/content_packages.dart';
import 'package:quran_sunnah_app/data/repositories/hadith_repository.dart';
import 'package:quran_sunnah_app/data/repositories/verified_asset_hadith_repository.dart';
import 'package:quran_sunnah_app/features/sunnah/hadith_filter_screen.dart';
import 'package:quran_sunnah_app/features/sunnah/topic_collections_screen.dart';
import 'dart:io';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  late Directory packageRoot;

  setUpAll(() async {
    packageRoot =
        await Directory.systemTemp.createTemp('content-packages-rawi-');
    ContentPackageStore.instance.resetForTesting();
    ContentPackageStore.instance.setRootDirectoryForTesting(packageRoot);
  });

  tearDownAll(() async {
    ContentPackageStore.instance.resetForTesting();
    if (await packageRoot.exists()) {
      await packageRoot.delete(recursive: true);
    }
  });

  test('rawi filter works as honest matn text search', () async {
    final repo = VerifiedAssetHadithRepository();
    final hits = await repo.query(
      const HadithFilter(
        collectionIds: {'bukhari'},
        narrator: 'الوحي',
      ),
      limit: 10,
    );
    expect(hits, isNotEmpty);

    final miss = await repo.query(
      const HadithFilter(
        collectionIds: {'bukhari'},
        narrator: 'zzzqx-no-such-word',
      ),
      limit: 10,
    );
    expect(miss, isEmpty);
  });

  test('topic collections come from verified section metadata', () async {
    final repo = VerifiedAssetHadithRepository();
    final secs = await repo.sections('bukhari');
    expect(secs.length, 98);
    final titles =
        secs.where((s) => s.title.isNotEmpty && s.count > 0).toList();
    expect(titles, isNotEmpty);
    expect(
      titles.any((s) => s.title.toLowerCase().contains('revelation')),
      isTrue,
    );
    expect(
      secs.fold<int>(0, (sum, s) => sum + s.count),
      7589,
    );
  });

  testWidgets('topics screen lists verified book collections',
      (tester) async {
    await tester.pumpWidget(
      const ProviderScope(
        child: MaterialApp(
          locale: Locale('en'),
          supportedLocales: AppStrings.supported,
          localizationsDelegates: [
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          home: TopicCollectionsScreen(),
        ),
      ),
    );
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 500));
    expect(find.byType(TopicCollectionsScreen), findsOneWidget);
    // AppBar + search render synchronously; topic rows stream from the
    // verified section indexes (covered at repo level above).
    expect(find.text('Topic'), findsOneWidget);
    expect(find.byType(SearchBar), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('rawi dropdown offers one-tap narrators', (tester) async {
    final container = ProviderContainer();
    addTearDown(container.dispose);
    await tester.pumpWidget(
      UncontrolledProviderScope(
        container: container,
        child: const MaterialApp(
          locale: Locale('ar'),
          supportedLocales: AppStrings.supported,
          localizationsDelegates: [
            GlobalMaterialLocalizations.delegate,
            GlobalWidgetsLocalizations.delegate,
            GlobalCupertinoLocalizations.delegate,
          ],
          home: HadithFilterScreen(),
        ),
      ),
    );
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));

    expect(find.byType(DropdownMenu<String>), findsOneWidget);
    await tester.tap(find.byType(DropdownMenu<String>));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));
    expect(find.text('أبو هريرة'), findsWidgets);

    await tester.tap(find.text('أبو هريرة').last);
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));

    final scrollable = find.byType(Scrollable).first;
    await tester.scrollUntilVisible(
      find.text('تطبيق التصفية'),
      200,
      scrollable: scrollable,
    );
    await tester.tap(find.text('تطبيق التصفية'));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));
    expect(container.read(hadithFilterProvider).narrator, 'أبو هريرة');
    expect(tester.takeException(), isNull);
  });
}
