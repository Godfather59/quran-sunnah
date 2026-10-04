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

  test('installed marker exposes local verified package and remove clears it',
      () async {
    final temp = await Directory.systemTemp.createTemp('package-store-');
    addTearDown(() async {
      ContentPackageStore.instance.resetForTesting();
      if (await temp.exists()) await temp.delete(recursive: true);
    });
    ContentPackageStore.instance.resetForTesting();
    ContentPackageStore.instance.setRootDirectoryForTesting(temp);

    final store = ContentPackageStore.instance;
    final manifest = await store.manifest();
    final pkg = manifest.byId('quran:en-sahih')!;
    expect(pkg.files, hasLength(1));

    final spec = pkg.files.single;
    final source = File(spec.path);
    final target = await store.installedFile(pkg.id, spec.path);
    await target.parent.create(recursive: true);
    await source.copy(target.path);

    final marker = await store.markerFile(pkg.id);
    await marker.writeAsString(jsonEncode({
      'id': pkg.id,
      'version': pkg.version,
      'sha256': pkg.sha256,
      'sourceRevision': manifest.sourceRevision,
    }));

    expect(await store.isInstalled(pkg.id), isTrue);
    expect(
      await store.loadString(spec.path),
      await source.readAsString(),
    );
    expect((await store.installedIds()).contains(pkg.id), isTrue);

    await store.remove(pkg.id);
    expect(await store.isInstalled(pkg.id), isFalse);
  });
}
