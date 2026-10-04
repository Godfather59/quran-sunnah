import 'dart:convert';
import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/data/repositories/verified_asset_quran_repository.dart';
import 'package:quran_sunnah_app/data/seed/surah_metadata.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('optional word morphology source mirrors Hafs verse tokens',
      () async {
    final quran = VerifiedAssetQuranRepository();
    var glossed = 0;
    var total = 0;

    for (final surah in kSurahMetadata) {
      final raw = await File(
        'assets/quran/words/hafs/${surah.number}.json',
      ).readAsString();
      final json = jsonDecode(raw) as Map<String, dynamic>;
      final entries = json['entries'] as List;
      final ayahs = await quran.ayahsOfSurah(
        surah.number,
        'hafs-an-asim__uthmani',
      );
      expect(entries.length, ayahs.length, reason: 'surah ${surah.number}');

      for (final item in entries) {
        final map = item as Map<String, dynamic>;
        final ayahNo = (map['ayah'] as num).toInt();
        final ayah = ayahs.firstWhere((a) => a.ayah == ayahNo);
        final words = (map['words'] as List)
            .map((rawWord) => Map<String, dynamic>.from(rawWord as Map))
            .toList();
        expect(
          words.map((w) => w['w'] as String).join(' '),
          ayah.text,
          reason: ayah.key,
        );
        for (final word in words) {
          total++;
          final lemma = (word['lemma'] as String?) ?? '';
          final root = (word['root'] as String?) ?? '';
          final pos = (word['pos'] as String?) ?? '';
          if (lemma.isNotEmpty || root.isNotEmpty || pos == 'MARK') {
            glossed++;
          }
        }
      }
    }
    expect(glossed / total, greaterThan(0.98));
  });
}
