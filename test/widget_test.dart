import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/app.dart';

void main() {
  testWidgets('App boots to splash then onboarding', (tester) async {
    await tester.pumpWidget(const ProviderScope(child: QuranSunnahApp()));
    // Splash visible immediately.
    await tester.pump(const Duration(milliseconds: 500));
    expect(find.textContaining('ٱلْقُرْآن').evaluate().isNotEmpty, isTrue);
    // Splash timer (1400ms) fires → onboarding language page.
    await tester.pump(const Duration(milliseconds: 1500));
    expect(
        find.textContaining('Language').evaluate().isNotEmpty ||
            find.textContaining('Quran').evaluate().isNotEmpty ||
            find.textContaining('اللغة').evaluate().isNotEmpty ||
            find.textContaining('الرواية').evaluate().isNotEmpty,
        isTrue);
  });
}
