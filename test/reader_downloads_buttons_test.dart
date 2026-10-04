import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/core/l10n/app_strings.dart';
import 'package:quran_sunnah_app/data/database/app_database.dart';
import 'package:quran_sunnah_app/data/models/hadith.dart';
import 'package:quran_sunnah_app/features/quran/quran_reader_screen.dart';
import 'package:quran_sunnah_app/features/quran/riwaya_selector_screen.dart';
import 'package:quran_sunnah_app/features/sunnah/hadith_reader_screen.dart';
import 'package:quran_sunnah_app/features/sunnah/narrator_view_screen.dart';
import 'package:quran_sunnah_app/state/database_provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

const _hadith = Hadith(
  id: 'bukhari:1',
  collectionId: 'bukhari',
  book: 'Book of Revelation',
  bookAr: '',
  chapter: 'Book of Revelation',
  chapterAr: '',
  hadithNumber: '1',
  matnAr: 'إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ',
);

Widget _wrap(Widget home) => ProviderScope(
      child: MaterialApp(
        locale: const Locale('en'),
        supportedLocales: AppStrings.supported,
        localizationsDelegates: const [
          GlobalMaterialLocalizations.delegate,
          GlobalWidgetsLocalizations.delegate,
          GlobalCupertinoLocalizations.delegate,
        ],
        home: home,
      ),
    );

Future<void> _pumpHadith(
  WidgetTester tester, {
  Hadith? hadith,
  String? missingMessage,
}) async {
  SharedPreferences.setMockInitialValues({});
  final db = AppDatabase.memory();
  addTearDown(db.close);
  await tester.pumpWidget(
    ProviderScope(
      overrides: [
        appDatabaseProvider.overrideWithValue(Future.value(db)),
      ],
      child: _wrap(
        Scaffold(
          body: HadithCard(hadith: hadith, missingMessage: missingMessage),
        ),
      ),
    ),
  );
  await tester.pump();
  await tester.pump(const Duration(milliseconds: 100));
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('hadith card exposes copy, share and meaning', (tester) async {
    await _pumpHadith(tester, hadith: _hadith);
    expect(find.textContaining('إِنَّمَا'), findsOneWidget);
    // Copy + share actions exist and are enabled.
    expect(find.text('Copy'), findsOneWidget);
    expect(find.text('Share'), findsOneWidget);
    // No verified translation bundled -> honest unavailable, never invented.
    expect(find.textContaining('Translation'), findsWidgets);
    expect(find.textContaining('unavailable'), findsWidgets);
    expect(tester.takeException(), isNull);
  });

  testWidgets('hadith card shows verified translation when present',
      (tester) async {
    const withMeaning = Hadith(
      id: 'muslim:1',
      collectionId: 'muslim',
      book: 'Book',
      bookAr: '',
      chapter: 'Book',
      chapterAr: '',
      hadithNumber: '1',
      matnAr: 'متن الحديث',
      matnTranslation: 'Actions are by intentions.',
    );
    await _pumpHadith(tester, hadith: withMeaning);
    expect(find.text('Actions are by intentions.'), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('placeholder chain and related buttons work', (tester) async {
    await _pumpHadith(tester, hadith: null, missingMessage: null);
    expect(find.text('Content unavailable for this source.'), findsOneWidget);

    await tester.tap(find.text('Chain of Narration'));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));
    expect(find.byType(NarratorViewScreen), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('quran reader header exposes kitaba without settings',
      (tester) async {
    SharedPreferences.setMockInitialValues({
      'app.onboarded': true,
      'app.locale': 'en',
    });
    final db = AppDatabase.memory();
    addTearDown(db.close);
    tester.view.devicePixelRatio = 1;
    tester.view.physicalSize = const Size(360, 640);
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          appDatabaseProvider.overrideWithValue(Future.value(db)),
        ],
        child: _wrap(const QuranReaderScreen(surah: 112)),
      ),
    );
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 500));

    // Riwaya + kitaba lines both present in reader AppBar.
    expect(find.textContaining('Riwaya'), findsOneWidget);
    expect(find.textContaining('Quran script'), findsOneWidget);

    await tester.tap(find.textContaining('Quran script'));
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 300));
    expect(find.byType(ScriptSelectorScreen), findsOneWidget);
    expect(tester.takeException(), isNull);
  });

  testWidgets('reader header tolerates 200 percent text scaling',
      (tester) async {
    tester.platformDispatcher.textScaleFactorTestValue = 2.0;
    addTearDown(tester.platformDispatcher.clearTextScaleFactorTestValue);
    SharedPreferences.setMockInitialValues({'app.onboarded': true});
    final db = AppDatabase.memory();
    addTearDown(db.close);
    tester.view.devicePixelRatio = 1;
    tester.view.physicalSize = const Size(390, 844);
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          appDatabaseProvider.overrideWithValue(Future.value(db)),
        ],
        child: _wrap(const QuranReaderScreen(surah: 112)),
      ),
    );
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 500));
    expect(find.byType(QuranReaderScreen), findsOneWidget);
    expect(tester.takeException(), isNull);
  });
}
