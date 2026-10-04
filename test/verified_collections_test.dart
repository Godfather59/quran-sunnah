import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/data/content/content_packages.dart';
import 'package:quran_sunnah_app/data/repositories/verified_asset_hadith_repository.dart';
import 'package:quran_sunnah_app/data/seed/hadith_collections.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('verified hadith ids match bundled manifest and seed', () async {
    const expected = {
      'bukhari',
      'muslim',
      'abudawud',
      'tirmidhi',
      'nasai',
      'ibnmajah',
      'malik',
      'nawawi',
      'qudsi',
      'dehlawi',
    };
    expect(kVerifiedHadithCollectionIds, expected);

    // Unverified collections must have no verified package and no asset dir.
    const unverified = {'ahmad', 'riyad', 'adab', 'bulugh'};
    final manifest = await ContentPackageStore.instance.manifest();
    final optional = manifest.packages.map((p) => p.id).toSet();
    for (final id in unverified) {
      expect(optional.contains('hadith:$id'), isFalse, reason: id);
      expect(
        await Directory('assets/hadith/$id').exists(),
        isFalse,
        reason: id,
      );
    }

    // Every seed collection is either verified or explicitly unverified.
    final seedIds = kHadithCollections.map((c) => c.id).toSet();
    expect(
      seedIds.difference(kVerifiedHadithCollectionIds),
      unverified,
    );
  });

  test('every optional package declares redistribution status', () async {
    final manifest = await ContentPackageStore.instance.manifest();
    for (final pkg in manifest.packages) {
      expect(pkg.licenseStatus.trim(), isNotEmpty, reason: pkg.id);
    }
    // Unresolved underlying-text rights must stay explicit, never silent.
    final byId = {for (final p in manifest.packages) p.id: p};
    for (final id in kVerifiedHadithCollectionIds) {
      if (id == 'bukhari' || id == 'muslim') continue;
      expect(
        byId['hadith:$id']!.licenseStatus,
        'underlying-text-rights-unresolved',
        reason: id,
      );
    }
  });

  test('line-ending policy preserves exact verified bytes', () async {
    final attrs = File('.gitattributes');
    expect(await attrs.exists(), isTrue);
    final text = await attrs.readAsString();
    expect(text, contains('assets/** text eol=lf'));
  });
}
