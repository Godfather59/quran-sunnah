import 'dart:convert';
import 'dart:io';

import 'package:crypto/crypto.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/data/content/content_packages.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('optional package manifest is complete and SHA-256 exact', () async {
    final raw = await rootBundle.loadString('assets/content_packages.json');
    final manifest = ContentPackageManifest.fromJson(
      Map<String, dynamic>.from(jsonDecode(raw) as Map),
    );

    expect(manifest.schemaVersion, 1);
    expect(manifest.sourceRevision, hasLength(40));
    expect(manifest.packages.length, 14);

    const expected = {
      'quran:en-sahih',
      'quran:fr-hamidullah',
      'quran:tafsir-jalalayn',
      'quran:tafsir-siraj',
      'quran:tajweed-hafs',
      'quran:words-hafs',
      'hadith:abudawud',
      'hadith:tirmidhi',
      'hadith:nasai',
      'hadith:ibnmajah',
      'hadith:malik',
      'hadith:nawawi',
      'hadith:qudsi',
      'hadith:dehlawi',
    };
    expect(manifest.packages.map((p) => p.id).toSet(), expected);

    for (final pkg in manifest.packages) {
      var total = 0;
      final aggregate = StringBuffer();
      expect(pkg.files, isNotEmpty, reason: pkg.id);
      for (final spec in pkg.files) {
        final file = File(spec.path);
        expect(await file.exists(), isTrue, reason: spec.path);
        final bytes = await file.readAsBytes();
        expect(bytes.length, spec.sizeBytes, reason: spec.path);
        expect(
          sha256.convert(bytes).toString(),
          spec.sha256,
          reason: spec.path,
        );
        total += spec.sizeBytes;
        aggregate
          ..write(spec.path)
          ..write('\u0000')
          ..write(spec.sizeBytes)
          ..write('\u0000')
          ..write(spec.sha256)
          ..write('\n');
      }
      expect(total, pkg.sizeBytes, reason: pkg.id);
      expect(
        sha256.convert(utf8.encode(aggregate.toString())).toString(),
        pkg.sha256,
        reason: pkg.id,
      );
    }
  });

  test('core datasets are not duplicated as optional packages', () async {
    final manifest = await ContentPackageStore.instance.manifest();
    final optional = manifest.packages.map((p) => p.id).toSet();
    expect(optional.intersection(kCoreDatasetIds), isEmpty);
  });
}
