import 'dart:convert';

class ContentValidationIssue {
  const ContentValidationIssue(this.message);
  final String message;
  @override
  String toString() => message;
}

class QuranTextValidationResult {
  const QuranTextValidationResult({
    required this.rows,
    required this.issues,
  });
  final int rows;
  final List<ContentValidationIssue> issues;
  bool get isValid => issues.isEmpty;
}

const List<int> kExpectedAyahCounts = [
  7, 286, 200, 176, 120, 165, 206, 75, 129, 109, 123, 111, 43, 52, 99,
  128, 111, 110, 98, 135, 112, 78, 118, 64, 77, 227, 93, 88, 69, 60,
  34, 30, 73, 54, 45, 83, 182, 88, 75, 85, 54, 53, 89, 59, 37, 35, 38,
  29, 18, 45, 60, 49, 62, 55, 78, 96, 29, 22, 24, 13, 14, 11, 11, 18,
  12, 12, 30, 52, 52, 44, 28, 28, 20, 56, 40, 31, 50, 40, 46, 42, 29,
  19, 36, 25, 22, 17, 19, 26, 30, 20, 15, 21, 11, 8, 8, 19, 5, 8, 8,
  11, 11, 8, 3, 9, 5, 4, 7, 3, 6, 3, 5, 4, 5, 6,
];

QuranTextValidationResult validatePipeQuranText(String raw) {
  final issues = <ContentValidationIssue>[];
  final seen = <String>{};
  final counts = List<int>.filled(114, 0);
  var rows = 0;

  for (final original in const LineSplitter().convert(raw)) {
    if (original.trim().isEmpty || original.startsWith('#')) continue;
    final first = original.indexOf('|');
    final second = first < 0 ? -1 : original.indexOf('|', first + 1);
    if (first <= 0 || second <= first + 1) {
      issues.add(ContentValidationIssue('Malformed row: $original'));
      continue;
    }
    final surah = int.tryParse(original.substring(0, first));
    final ayah = int.tryParse(original.substring(first + 1, second));
    var text = original.substring(second + 1);
    if (text.endsWith('\r')) text = text.substring(0, text.length - 1);
    if (surah == null || ayah == null || surah < 1 || surah > 114) {
      issues.add(ContentValidationIssue('Invalid reference: $original'));
      continue;
    }
    final expected = kExpectedAyahCounts[surah - 1];
    if (ayah < 1 || ayah > expected) {
      issues.add(ContentValidationIssue('Out-of-range ayah $surah:$ayah'));
      continue;
    }
    if (text.isEmpty) {
      issues.add(ContentValidationIssue('Empty text at $surah:$ayah'));
    }
    final key = '$surah:$ayah';
    if (!seen.add(key)) {
      issues.add(ContentValidationIssue('Duplicate reference $key'));
    }
    counts[surah - 1]++;
    rows++;
  }

  if (rows != 6236) {
    issues.add(ContentValidationIssue('Expected 6236 rows, found $rows'));
  }
  for (var i = 0; i < counts.length; i++) {
    if (counts[i] != kExpectedAyahCounts[i]) {
      issues.add(ContentValidationIssue(
        'Surah ${i + 1}: expected ${kExpectedAyahCounts[i]}, found ${counts[i]}',
      ));
    }
  }
  return QuranTextValidationResult(rows: rows, issues: issues);
}

class HadithPackageValidationResult {
  const HadithPackageValidationResult({
    required this.rows,
    required this.issues,
    required this.hasStructuredNarrators,
    required this.hasStructuredGrades,
    required this.hasStructuredTopics,
  });

  final int rows;
  final List<ContentValidationIssue> issues;
  final bool hasStructuredNarrators;
  final bool hasStructuredGrades;
  final bool hasStructuredTopics;
  bool get isValid => issues.isEmpty;
}

HadithPackageValidationResult validateHadithRows(
  Iterable<Map<String, dynamic>> rows,
) {
  final issues = <ContentValidationIssue>[];
  final ids = <String>{};
  var count = 0;
  var narrators = false;
  var grades = false;
  var topics = false;

  for (final row in rows) {
    count++;
    final id = (row['id'] ?? '').toString().trim();
    final number = (row['hadithNumber'] ?? row['hadithnumber'] ?? '').toString().trim();
    final matn = (row['matnAr'] ?? row['arabic'] ?? row['text'] ?? '').toString().trim();
    if (id.isEmpty && number.isEmpty) {
      issues.add(ContentValidationIssue('Hadith row $count has no stable id/number'));
    }
    final stable = id.isEmpty ? number : id;
    if (!ids.add(stable)) {
      issues.add(ContentValidationIssue('Duplicate Hadith id/number $stable'));
    }
    if (matn.isEmpty) {
      issues.add(ContentValidationIssue('Hadith $stable has empty Arabic text'));
    }

    final narrator = row['narrator'];
    if (narrator is String && narrator.trim().isNotEmpty) narrators = true;

    final grade = row['grade'];
    if (grade is String && grade.trim().isNotEmpty) {
      grades = true;
      final authority = row['gradingAuthority'];
      if (authority is! String || authority.trim().isEmpty) {
        issues.add(ContentValidationIssue(
          'Hadith $stable has a grade without gradingAuthority',
        ));
      }
    }

    final topicValue = row['topics'];
    if (topicValue is List && topicValue.any((e) => e.toString().trim().isNotEmpty)) {
      topics = true;
    }
  }

  return HadithPackageValidationResult(
    rows: count,
    issues: issues,
    hasStructuredNarrators: narrators,
    hasStructuredGrades: grades,
    hasStructuredTopics: topics,
  );
}

List<ContentValidationIssue> validateProvenanceText(String raw) {
  final issues = <ContentValidationIssue>[];
  final lower = raw.toLowerCase();
  for (final requirement in ['source', 'version', 'license']) {
    if (!lower.contains(requirement)) {
      issues.add(ContentValidationIssue(
        'Provenance must explicitly record $requirement.',
      ));
    }
  }
  if (lower.contains('verify upstream terms before redistribution')) {
    issues.add(const ContentValidationIssue(
      'Redistribution terms remain unresolved.',
    ));
  }
  return issues;
}
