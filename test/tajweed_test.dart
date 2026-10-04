import 'dart:convert';
import 'dart:io';

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
      segs.map((s) => '${s.start}-${s.end}:${s.rule ?? '-'}').toList(),
      ['0-2:-', '2-5:ikhfa', '5-8:-'],
    );
  });

  test('overlapping spans clamp and reproduce source text', () {
    const text = 'abcdef';
    final segs = buildTajweedSegments(text, const [
      TajweedSpan(rule: 'a', start: 1, end: 4),
      TajweedSpan(rule: 'b', start: 3, end: 6),
      TajweedSpan(rule: 'c', start: -5, end: 99),
    ]);
    expect(segs.first.start, 0);
    expect(segs.last.end, 6);
    expect(
      segs.map((s) => text.substring(s.start, s.end)).join(),
      text,
    );
  });

  test('unknown rules fall back to grey', () {
    expect(ruleInfo('some_future_rule').colorValue, 0xFF757575);
    expect(ruleInfo('qalqalah').nameAr, 'قلقلة');
  });

  test('optional Hafs Tajweed source covers all verses and valid bounds',
      () async {
    final quran = VerifiedAssetQuranRepository();
    var totalAnnotations = 0;
    final seenRules = <String>{};
    for (var surah = 1; surah <= 114; surah++) {
      final raw = await File(
        'assets/quran/tajweed/hafs/$surah.json',
      ).readAsString();
      final json = jsonDecode(raw) as Map<String, dynamic>;
      final entries = json['entries'] as List;
      final ayahs = await quran.ayahsOfSurah(
        surah,
        'hafs-an-asim__uthmani',
      );
      expect(entries.length, ayahs.length, reason: 'surah $surah');

      for (final item in entries) {
        final map = item as Map<String, dynamic>;
        final ayahNumber = (map['ayah'] as num).toInt();
        final ayah = ayahs.firstWhere((a) => a.ayah == ayahNumber);
        final spans = <TajweedSpan>[
          for (final rawSpan in map['annotations'] as List)
            TajweedSpan(
              rule: (rawSpan as Map<String, dynamic>)['rule'] as String,
              start: (rawSpan['start'] as num).toInt(),
              end: (rawSpan['end'] as num).toInt(),
            ),
        ];
        for (final span in spans) {
          expect(span.start >= 0 && span.end <= ayah.text.length, isTrue,
              reason: ayah.key);
          seenRules.add(span.rule);
          totalAnnotations++;
        }
        expect(
          buildTajweedSegments(ayah.text, spans)
              .map((segment) =>
                  ayah.text.substring(segment.start, segment.end))
              .join(),
          ayah.text,
          reason: ayah.key,
        );
      }
    }
    expect(totalAnnotations, greaterThan(50000));
    final known = kTajweedRules.map((r) => r.rule).toSet();
    expect(seenRules.difference(known), isEmpty);
  });
}
