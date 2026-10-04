import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/data/repositories/tajweed_repository.dart';
import 'package:quran_sunnah_app/data/repositories/verified_asset_quran_repository.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('segments split text by rule spans', () {
    const text = 'abcdefgh';
    final segs = buildTajweedSegments(text, const [
      TajweedSpan(rule: 'ikhfa', start: 2, end: 5),
    ]);
    expect(
        segs
            .map((s) =>
                '${s.start}-${s.end}:${s.rule ?? '-'}')
            .toList(),
        ['0-2:-', '2-5:ikhfa', '5-8:-']);
  });

  test('overlapping spans resolve first-match, bounds clamp', () {
    const text = 'abcdef';
    final segs = buildTajweedSegments(text, const [
      TajweedSpan(rule: 'a', start: 1, end: 4),
      TajweedSpan(rule: 'b', start: 3, end: 6),
      TajweedSpan(rule: 'c', start: -5, end: 99),
    ]);
    // Full-text span 'c' would cover all — first-match wins per segment,
    // but 'a'/'b' were added first so they take precedence where set.
    expect(segs.first.start, 0);
    expect(segs.last.end, 6);
    final mid = segs.firstWhere((s) => s.start == 1);
    expect(mid.rule, 'a');
    // Concatenated segments reproduce the text exactly.
    final rebuilt =
        segs.map((s) => text.substring(s.start, s.end)).join();
    expect(rebuilt, text);
  });

  test('unknown rules fall back to grey instead of vanishing', () {
    final info = ruleInfo('some_future_rule');
    expect(info.colorValue, 0xFF757575);
    expect(ruleInfo('qalqalah').nameAr, 'قلقلة');
  });

  test('empty spans return single plain segment', () {
    const text = 'abc';
    final segs = buildTajweedSegments(text, const []);
    expect(segs.length, 1);
    expect(segs.first.rule, isNull);
  });

  group('bundled hafs tajweed data', () {
    test('6236 verses covered, bounds valid, rules known', () async {
      final container = ProviderContainer();
      addTearDown(container.dispose);
      final quran = VerifiedAssetQuranRepository();
      var totalAnn = 0;
      final seenRules = <String>{};
      for (var surah = 1; surah <= 114; surah++) {
        final map = await container
            .read(tajweedSurahProvider(surah).future);
        final ayahs = await quran.ayahsOfSurah(
            surah, 'hafs-an-asim__uthmani');
        expect(map.length, ayahs.length,
            reason: 'surah $surah coverage');
        for (final a in ayahs) {
          final spans = map[a.ayah] ?? const <TajweedSpan>[];
          for (final sp in spans) {
            expect(sp.start >= 0 && sp.end <= a.text.length,
                isTrue,
                reason: '${a.key} ${sp.rule}');
            seenRules.add(sp.rule);
            totalAnn++;
          }
          // Segments must reproduce the verse exactly.
          final rebuilt = buildTajweedSegments(a.text, spans)
              .map((sg) =>
                  a.text.substring(sg.start, sg.end))
              .join();
          expect(rebuilt, a.text, reason: a.key);
        }
      }
      expect(totalAnn, greaterThan(50000));
      // Every observed rule has a palette entry (no silent greys).
      final known =
          kTajweedRules.map((r) => r.rule).toSet();
      expect(seenRules.difference(known), isEmpty);
    });
  });
}
