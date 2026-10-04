import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/data/validation/content_validator.dart';

void main() {
  test('Quran validator rejects malformed/incomplete data', () {
    final result = validatePipeQuranText('1|1|بسم الله\n1|1|duplicate');
    expect(result.isValid, isFalse);
    expect(result.issues.any((i) => i.message.contains('Duplicate')), isTrue);
    expect(result.issues.any((i) => i.message.contains('6236')), isTrue);
  });

  test('Hadith validator requires grading authority when grade exists', () {
    final result = validateHadithRows([
      {
        'id': 'x:1',
        'hadithNumber': '1',
        'matnAr': 'نص',
        'grade': 'Sahih',
      }
    ]);
    expect(result.isValid, isFalse);
    expect(result.hasStructuredGrades, isTrue);
    expect(
      result.issues.any((i) => i.message.contains('gradingAuthority')),
      isTrue,
    );
  });

  test('Hadith structured capabilities are detected without invention', () {
    final result = validateHadithRows([
      {
        'id': 'x:1',
        'hadithNumber': '1',
        'matnAr': 'نص موثق',
        'narrator': 'راوٍ',
        'grade': 'Sahih',
        'gradingAuthority': 'Verified source',
        'topics': ['prayer'],
      }
    ]);
    expect(result.isValid, isTrue);
    expect(result.hasStructuredNarrators, isTrue);
    expect(result.hasStructuredGrades, isTrue);
    expect(result.hasStructuredTopics, isTrue);
  });

  test('Provenance gate blocks unresolved redistribution notes', () {
    final issues = validateProvenanceText(
      'Source: upstream\nVersion: 1\nLicense: verify upstream terms before redistribution',
    );
    expect(issues, isNotEmpty);
  });
}
