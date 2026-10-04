import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:quran_sunnah_app/app.dart';
import 'package:quran_sunnah_app/data/database/app_database.dart';
import 'package:quran_sunnah_app/data/repositories/verified_asset_hadith_repository.dart';
import 'package:quran_sunnah_app/data/repositories/verified_asset_quran_repository.dart';
import 'package:quran_sunnah_app/data/services/search_service.dart';
import 'package:quran_sunnah_app/state/database_provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('device smoke: app starts in Arabic RTL and core assets load',
      (tester) async {
    SharedPreferences.setMockInitialValues({
      'app.onboarded': true,
      'app.locale': 'ar',
      'app.theme': 0,
    });
    final db = AppDatabase.memory();
    addTearDown(db.close);

    final startup = Stopwatch()..start();
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          appDatabaseProvider.overrideWithValue(Future.value(db)),
        ],
        child: const QuranSunnahApp(),
      ),
    );
    await tester.pump(const Duration(seconds: 2));
    startup.stop();

    expect(find.byType(MaterialApp), findsOneWidget);
    final context = tester.element(find.byType(MaterialApp));
    expect(Directionality.of(context), TextDirection.rtl);

    final quran = VerifiedAssetQuranRepository();
    final load = Stopwatch()..start();
    final ayahs = await quran.allAyahs('hafs-an-asim__uthmani');
    load.stop();
    expect(ayahs, hasLength(6236));

    debugPrint('PROFILE startup_ui_ms=${startup.elapsedMilliseconds}');
    debugPrint('PROFILE hafs_asset_load_ms=${load.elapsedMilliseconds}');
  });

  testWidgets('device smoke: first and repeat FTS searches are source-backed',
      (tester) async {
    SharedPreferences.setMockInitialValues({
      'app.onboarded': true,
      'app.locale': 'en',
    });
    final db = AppDatabase.memory();
    addTearDown(db.close);

    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          appDatabaseProvider.overrideWithValue(Future.value(db)),
        ],
        child: const QuranSunnahApp(),
      ),
    );
    await tester.pump(const Duration(milliseconds: 300));

    final search = SearchService(
      quran: VerifiedAssetQuranRepository(),
      hadith: VerifiedAssetHadithRepository(),
      database: db,
    );

    final cold = Stopwatch()..start();
    final first = await search.search(
      query: 'الحمد',
      editionId: 'hafs-an-asim__uthmani',
      tafsirId: 'jalalayn',
    );
    cold.stop();
    expect(first.total, greaterThan(0));

    final warm = Stopwatch()..start();
    final second = await search.search(
      query: 'الرحمن',
      editionId: 'hafs-an-asim__uthmani',
      tafsirId: 'jalalayn',
    );
    warm.stop();
    expect(second.total, greaterThan(0));

    debugPrint('PROFILE first_fts_search_ms=${cold.elapsedMilliseconds}');
    debugPrint('PROFILE repeat_fts_search_ms=${warm.elapsedMilliseconds}');
  });
}
