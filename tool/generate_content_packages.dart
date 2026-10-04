import 'dart:convert';
import 'dart:io';

import 'package:crypto/crypto.dart';

const sourceRevision = '14511839381c742a78d40834d40eec840b1a2ad6';
const sourceBaseUrl =
    'https://raw.githubusercontent.com/Godfather59/quran-sunnah/$sourceRevision/';

class Def {
  const Def({
    required this.id,
    required this.titleAr,
    required this.titleEn,
    required this.version,
    required this.source,
    required this.licenseStatus,
    required this.paths,
  });

  final String id;
  final String titleAr;
  final String titleEn;
  final String version;
  final String source;
  final String licenseStatus;
  final List<String> paths;
}

const defs = <Def>[
  Def(
    id: 'quran:en-sahih',
    titleAr: 'الترجمة الإنجليزية — صحيح إنترناشونال',
    titleEn: 'English — Saheeh International',
    version: 'tanzil-2026-10-04',
    source: 'Tanzil Project',
    licenseStatus: 'restricted-noncommercial',
    paths: ['assets/quran/translations/en-sahih.txt'],
  ),
  Def(
    id: 'quran:fr-hamidullah',
    titleAr: 'الترجمة الفرنسية — محمد حميد الله',
    titleEn: 'French — Muhammad Hamidullah',
    version: 'tanzil-2026-10-04',
    source: 'Tanzil Project',
    licenseStatus: 'restricted-noncommercial',
    paths: ['assets/quran/translations/fr-hamidullah.txt'],
  ),
  Def(
    id: 'quran:tafsir-jalalayn',
    titleAr: 'تفسير الجلالين',
    titleEn: 'Tafsir al-Jalalayn',
    version: 'quran-api-1',
    source: 'quran-api@1 / Tanzil',
    licenseStatus: 'restricted-verify-direct-rights',
    paths: ['assets/quran/tafsir/jalalayn'],
  ),
  Def(
    id: 'quran:tafsir-siraj',
    titleAr: 'السراج في تفسير القرآن',
    titleEn: 'Al-Siraj Tafsir',
    version: 'quran-api-1',
    source: 'quran-api@1 / QuranEnc',
    licenseStatus: 'conditional-version-incomplete',
    paths: ['assets/quran/tafsir/siraj'],
  ),
  Def(
    id: 'quran:tajweed-hafs',
    titleAr: 'بيانات ألوان التجويد — حفص',
    titleEn: 'Tajweed annotations — Hafs',
    version: 'cpfair-quran-tajweed-2026-10-04',
    source: 'cpfair/quran-tajweed',
    licenseStatus: 'cc-by-4.0',
    paths: ['assets/quran/tajweed/hafs'],
  ),
  Def(
    id: 'quran:words-hafs',
    titleAr: 'التحليل الصرفي للكلمات — حفص',
    titleEn: 'Word morphology — Hafs',
    version: 'qac-0.4',
    source: 'Quranic Arabic Corpus v0.4',
    licenseStatus: 'gpl-with-attribution',
    paths: ['assets/quran/words/hafs'],
  ),
  Def(
    id: 'hadith:abudawud',
    titleAr: 'سنن أبي داود',
    titleEn: 'Sunan Abi Dawud',
    version: 'hadith-api-1',
    source: 'fawazahmed0/hadith-api@1 ara-abudawud',
    licenseStatus: 'underlying-text-rights-unresolved',
    paths: ['assets/hadith/abudawud/index.json', 'assets/hadith/abudawud/sections'],
  ),
  Def(
    id: 'hadith:tirmidhi',
    titleAr: 'جامع الترمذي',
    titleEn: 'Jami at-Tirmidhi',
    version: 'hadith-api-1',
    source: 'fawazahmed0/hadith-api@1 ara-tirmidhi',
    licenseStatus: 'underlying-text-rights-unresolved',
    paths: ['assets/hadith/tirmidhi/index.json', 'assets/hadith/tirmidhi/sections'],
  ),
  Def(
    id: 'hadith:nasai',
    titleAr: 'سنن النسائي',
    titleEn: 'Sunan an-Nasa’i',
    version: 'hadith-api-1',
    source: 'fawazahmed0/hadith-api@1 ara-nasai',
    licenseStatus: 'underlying-text-rights-unresolved',
    paths: ['assets/hadith/nasai/index.json', 'assets/hadith/nasai/sections'],
  ),
  Def(
    id: 'hadith:ibnmajah',
    titleAr: 'سنن ابن ماجه',
    titleEn: 'Sunan Ibn Majah',
    version: 'hadith-api-1',
    source: 'fawazahmed0/hadith-api@1 ara-ibnmajah',
    licenseStatus: 'underlying-text-rights-unresolved',
    paths: ['assets/hadith/ibnmajah/index.json', 'assets/hadith/ibnmajah/sections'],
  ),
  Def(
    id: 'hadith:malik',
    titleAr: 'موطأ مالك',
    titleEn: 'Muwatta Malik',
    version: 'hadith-api-1',
    source: 'fawazahmed0/hadith-api@1 ara-malik',
    licenseStatus: 'underlying-text-rights-unresolved',
    paths: ['assets/hadith/malik/index.json', 'assets/hadith/malik/sections'],
  ),
  Def(
    id: 'hadith:nawawi',
    titleAr: 'الأربعون النووية',
    titleEn: 'Forty Hadith of an-Nawawi',
    version: 'hadith-api-1',
    source: 'fawazahmed0/hadith-api@1 ara-nawawi',
    licenseStatus: 'underlying-text-rights-unresolved',
    paths: ['assets/hadith/nawawi/index.json', 'assets/hadith/nawawi/sections'],
  ),
  Def(
    id: 'hadith:qudsi',
    titleAr: 'الأحاديث القدسية',
    titleEn: 'Forty Hadith Qudsi',
    version: 'hadith-api-1',
    source: 'fawazahmed0/hadith-api@1 ara-qudsi',
    licenseStatus: 'underlying-text-rights-unresolved',
    paths: ['assets/hadith/qudsi/index.json', 'assets/hadith/qudsi/sections'],
  ),
  Def(
    id: 'hadith:dehlawi',
    titleAr: 'أربعون شاه ولي الله الدهلوي',
    titleEn: 'Forty Hadith Shah Waliullah Dehlawi',
    version: 'hadith-api-1',
    source: 'fawazahmed0/hadith-api@1 ara-dehlawi',
    licenseStatus: 'underlying-text-rights-unresolved',
    paths: ['assets/hadith/dehlawi/index.json', 'assets/hadith/dehlawi/sections'],
  ),
];

Future<void> main() async {
  final packages = <Map<String, Object?>>[];

  for (final def in defs) {
    final files = <File>[];
    for (final path in def.paths) {
      final type = FileSystemEntity.typeSync(path);
      if (type == FileSystemEntityType.file) {
        files.add(File(path));
      } else if (type == FileSystemEntityType.directory) {
        files.addAll(
          Directory(path)
              .listSync(recursive: true, followLinks: false)
              .whereType<File>(),
        );
      } else {
        stderr.writeln('Missing package source path: $path');
        exitCode = 2;
        return;
      }
    }
    files.sort((a, b) => a.path.compareTo(b.path));

    final entries = <Map<String, Object?>>[];
    var size = 0;
    for (final file in files) {
      final bytes = await file.readAsBytes();
      final path = file.path.replaceAll('\\', '/');
      final digest = sha256.convert(bytes).toString();
      size += bytes.length;
      entries.add({
        'path': path,
        'sizeBytes': bytes.length,
        'sha256': digest,
      });
    }

    final aggregate = StringBuffer();
    for (final entry in entries) {
      aggregate
        ..write(entry['path'])
        ..write('\u0000')
        ..write(entry['sizeBytes'])
        ..write('\u0000')
        ..write(entry['sha256'])
        ..write('\n');
    }

    packages.add({
      'id': def.id,
      'titleAr': def.titleAr,
      'titleEn': def.titleEn,
      'version': def.version,
      'source': def.source,
      'licenseStatus': def.licenseStatus,
      'sizeBytes': size,
      'sha256':
          sha256.convert(utf8.encode(aggregate.toString())).toString(),
      'files': entries,
    });
  }

  final output = {
    'schemaVersion': 1,
    'sourceRevision': sourceRevision,
    'sourceBaseUrl': sourceBaseUrl,
    'packages': packages,
  };

  final encoder = const JsonEncoder.withIndent('  ');
  await File('assets/content_packages.json')
      .writeAsString('${encoder.convert(output)}\n');
}
