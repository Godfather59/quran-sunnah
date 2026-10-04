import 'dart:convert';
import 'dart:io';

import 'package:crypto/crypto.dart';
import 'package:quran_sunnah_app/data/validation/content_validator.dart';

Never _usage() {
  stderr.writeln('''
Verified content pipeline

Quran/translation:
  dart run tool/content_pipeline.dart quran <file> <provenance-file> <edition-id>

Hadith JSON array:
  dart run tool/content_pipeline.dart hadith <file> <provenance-file> <collection-id>

The command never modifies bundled religious assets. It validates an incoming
source package and prints a fingerprint + registry information. Commit/import
only after provenance/licensing review.
''');
  exitCode = 64;
  throw StateError('usage');
}

Future<void> main(List<String> args) async {
  if (args.length != 4) _usage();
  final kind = args[0];
  final file = File(args[1]);
  final provenance = File(args[2]);
  final id = args[3];

  if (!await file.exists() || !await provenance.exists()) {
    stderr.writeln('Input or provenance file does not exist.');
    exitCode = 66;
    return;
  }

  final provenanceText = await provenance.readAsString();
  final provenanceIssues = validateProvenanceText(provenanceText);
  if (provenanceIssues.isNotEmpty) {
    stderr.writeln('PROVENANCE FAILED');
    for (final issue in provenanceIssues) {
      stderr.writeln(' - $issue');
    }
    exitCode = 2;
    return;
  }

  final bytes = await file.readAsBytes();
  final sha = sha256.convert(bytes).toString();

  if (kind == 'quran') {
    final raw = utf8.decode(bytes);
    final result = validatePipeQuranText(raw);
    if (!result.isValid) {
      stderr.writeln('QURAN VALIDATION FAILED');
      for (final issue in result.issues.take(100)) {
        stderr.writeln(' - $issue');
      }
      exitCode = 3;
      return;
    }
    stdout.writeln('VALID quran edition=$id rows=${result.rows}');
    stdout.writeln('sha256=$sha');
    stdout.writeln("registry: '$id': '<asset-path>',");
    return;
  }

  if (kind == 'hadith') {
    final parsed = jsonDecode(utf8.decode(bytes));
    if (parsed is! List) {
      stderr.writeln('Hadith input must be a JSON array.');
      exitCode = 4;
      return;
    }
    final rows = parsed
        .whereType<Map>()
        .map((e) => Map<String, dynamic>.from(e));
    final result = validateHadithRows(rows);
    if (!result.isValid) {
      stderr.writeln('HADITH VALIDATION FAILED');
      for (final issue in result.issues.take(100)) {
        stderr.writeln(' - $issue');
      }
      exitCode = 5;
      return;
    }
    stdout.writeln('VALID hadith collection=$id rows=${result.rows}');
    stdout.writeln('structured_narrators=${result.hasStructuredNarrators}');
    stdout.writeln('structured_grades=${result.hasStructuredGrades}');
    stdout.writeln('structured_topics=${result.hasStructuredTopics}');
    stdout.writeln('sha256=$sha');
    return;
  }

  _usage();
}
