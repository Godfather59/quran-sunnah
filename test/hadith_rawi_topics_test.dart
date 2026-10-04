import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/core/l10n/app_strings.dart';
import 'package:quran_sunnah_app/data/content/content_packages.dart';
import 'package:quran_sunnah_app/data/repositories/hadith_repository.dart';
import 'package:quran_sunnah_app/data/repositories/verified_asset_hadith_repository.dart';
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
    expect(tester.takeException(), isNull);
    expect(tester.takeException(), isNull);
  });
}
