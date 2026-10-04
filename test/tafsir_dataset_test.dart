import 'dart:convert';
import 'dart:io';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/data/content/content_packages.dart';
import 'package:quran_sunnah_app/data/repositories/tafsir_repository.dart';
import 'package:quran_sunnah_app/data/seed/surah_metadata.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  for (final tafsir in kTafsirCatalog.where((t) => t.bundled)) {
    test('optional tafsir ${tafsir.id} source covers 6236 verses', () async {
      var total = 0;
      for (final surah in kSurahMetadata) {
        final raw = await File(
          'assets/quran/tafsir/${tafsir.id}/${surah.number}.json',
        ).readAsString();
        final json = jsonDecode(raw) as Map<String, dynamic>;
        final entries = json['entries'] as List;
        expect(entries.length, surah.ayahCount,
            reason: '${tafsir.id} surah ${surah.number}');
        for (final item in entries) {
          final map = item as Map<String, dynamic>;
          expect((map['text'] as String).isNotEmpty, isTrue);
        }
        total += entries.length;
      }
      expect(total, 6236);
    });
  }

  test('clean install returns no optional tafsir until package install',
      () async {
    final temp = await Directory.systemTemp.createTemp('content-packages-');
    addTearDown(() async {
      ContentPackageStore.instance.resetForTesting();
      if (await temp.exists()) await temp.delete(recursive: true);
    });
    ContentPackageStore.instance.setRootDirectoryForTesting(temp);

    final container = ProviderContainer();
    addTearDown(container.dispose);
    final map = await container
        .read(tafsirSurahProvider(('jalalayn', 1)).future);
    expect(map, isEmpty);
  });

  test('unverified tafsir candidate remains unavailable', () async {
    final container = ProviderContainer();
    addTearDown(container.dispose);
    final map = await container
        .read(tafsirSurahProvider(('ibn-kathir', 112)).future);
    expect(map, isEmpty);
  });
}
