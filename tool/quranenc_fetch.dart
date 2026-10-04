import 'dart:convert';
import 'dart:io';

import 'package:crypto/crypto.dart';

Future<void> main(List<String> args) async {
  if (args.length < 5) {
    stderr.writeln(
      'Usage: dart run tool/quranenc_fetch.dart '
      '<translation_key> <version> <publisher> <kind:translation|tafsir> <output_dir>',
    );
    exitCode = 64;
    return;
  }

  final key = args[0];
  final version = args[1];
  final publisher = args[2];
  final kind = args[3];
  final out = Directory(args[4]);

  if (kind != 'translation' && kind != 'tafsir') {
    stderr.writeln('kind must be translation or tafsir');
    exitCode = 64;
    return;
  }

  await out.create(recursive: true);
  final rawDir = Directory('${out.path}/raw');
  await rawDir.create(recursive: true);

  final client = HttpClient();
  final normalized = StringBuffer();
  final fileHashes = <String, String>{};
  var count = 0;

  try {
    for (var surah = 1; surah <= 114; surah++) {
      final uri = Uri.parse(
        'https://quranenc.com/api/v1/translation/sura/$key/$surah',
      );
      final request = await client.getUrl(uri);
      request.headers.set(HttpHeaders.userAgentHeader, 'quran-sunnah-content-import/1');
      final response = await request.close();
      if (response.statusCode != 200) {
        throw HttpException('QuranEnc HTTP ${response.statusCode} for surah $surah');
      }
      final bytes = await response.fold<List<int>>(<int>[], (a, b) => a..addAll(b));
      final rawFile = File('${rawDir.path}/$surah.json');
      await rawFile.writeAsBytes(bytes, flush: true);
      fileHashes['raw/$surah.json'] = sha256.convert(bytes).toString();

      final decoded = jsonDecode(utf8.decode(bytes));
      final rows = decoded is Map<String, dynamic>
          ? (decoded['result'] ?? decoded['translations'] ?? decoded['data'])
          : decoded;
      if (rows is! List) {
        throw FormatException('Unexpected QuranEnc payload for surah $surah');
      }

      for (final item in rows) {
        if (item is! Map) continue;
        final m = Map<String, dynamic>.from(item);
        final s = int.tryParse((m['sura'] ?? m['surah'] ?? surah).toString());
        final a = int.tryParse((m['aya'] ?? m['ayah']).toString());
        final text = (m['translation'] ?? '').toString();
        if (s == null || a == null || text.isEmpty) {
          throw FormatException('Missing sura/aya/translation in $key surah $surah');
        }
        normalized.writeln('$s|$a|$text');
        count++;
      }

      stdout.writeln('Fetched $key surah $surah/114');
      await Future<void>.delayed(const Duration(milliseconds: 120));
    }
  } finally {
    client.close(force: true);
  }

  if (count != 6236) {
    throw StateError('Expected 6236 rows, received $count');
  }

  final normalizedFile = File('${out.path}/normalized.txt');
  final normalizedBytes = utf8.encode(normalized.toString());
  await normalizedFile.writeAsBytes(normalizedBytes, flush: true);

  final manifest = {
    'source': 'QuranEnc.com',
    'api': 'https://quranenc.com/api/v1/translation/sura/{translation_key}/{sura_number}',
    'translationKey': key,
    'kind': kind,
    'version': version,
    'publisher': publisher,
    'retrievedAt': DateTime.now().toUtc().toIso8601String(),
    'rows': count,
    'normalizedSha256': sha256.convert(normalizedBytes).toString(),
    'rawSha256': fileHashes,
    'terms': {
      'noModification': true,
      'sourceAttributionRequired': true,
      'versionRequired': true,
      'keepTranscriptInformation': true,
      'updateToLatestVersion': true,
      'termsUrl': 'https://quranenc.com/en/home/api',
    },
    'note':
        'Raw API responses are retained intentionally. Review transcript and '
        'footnote fields before wiring normalized text into the app.',
  };

  await File('${out.path}/IMPORT_MANIFEST.json').writeAsString(
    const JsonEncoder.withIndent('  ').convert(manifest),
    flush: true,
  );

  stdout.writeln('VALID QuranEnc package $key rows=$count');
}
