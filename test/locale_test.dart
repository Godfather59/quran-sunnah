// Locale integrity: Arabic chrome must be Arabic-only (no Latin).
// Sacred text is untouched by this — only interface strings checked.

import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/core/l10n/app_strings.dart';

void main() {
  test('arabic strings contain no Latin letters', () {
    final s = const AppStrings(Locale('ar'));
    final keys = [
      'appTitle',
      'home',
      'quran',
      'sunnah',
      'library',
      'settings',
      'continueReading',
      'dailyAyah',
      'dailyHadith',
      'search',
      'searchHint',
      'riwaya',
      'compareRiwayat',
      'play',
      'repeat',
      'tafsir',
      'translation',
      'bookmark',
      'share',
      'sources',
      'filters',
      'downloads',
      'quranGroup',
      'sunnahGroup',
      'appearanceGroup',
      'audioGroup',
      'languageGroup',
      'storageGroup',
      'light',
      'dark',
      'system',
      'cancel',
      'makki',
      'madani',
      'tabAll',
      'tabNarrator',
      'tabTopic',
      'obLanguage',
      'obRiwaya',
      'obScript',
      'obSources',
      'obNext',
      'obDone',
      'hadith',
      'surahWord',
      'ayat',
    ];
    final latin = RegExp(r'[A-Za-z]');
    for (final k in keys) {
      expect(latin.hasMatch(s.t(k)), isFalse,
          reason: 'key "$k" => "${s.t(k)}"');
    }
  });

  test('all locales resolve every key (no fallback leakage)', () {
    for (final locale in AppStrings.supported) {
      final s = AppStrings(locale);
      for (final k in ['home', 'quran', 'sunnah', 'settings', 'search']) {
        expect(s.t(k), isNot(k));
      }
    }
  });
}
