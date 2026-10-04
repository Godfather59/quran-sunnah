import 'dart:io';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:quran_sunnah_app/data/content/content_packages.dart';
import 'package:quran_sunnah_app/data/repositories/translation_repository.dart';

Map<String, String> _parse(String raw) {
  final out = <String, String>{};
  for (final line in raw.split('\n')) {
    final first = line.indexOf('|');
    final second = first < 0 ? -1 : line.indexOf('|', first + 1);
    if (first < 0 || second < 0) continue;
    final surah = int.tryParse(line.substring(0, first));
    final ayah = int.tryParse(line.substring(first + 1, second));
    if (surah == null || ayah == null) continue;
    final text = line.substring(second + 1).replaceFirst(RegExp(r'\r$'), '');
    if (text.isNotEmpty) out['$surah:$ayah'] = text;
  }
  return out;
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('optional translations retain exact 6236 source rows', () async {
    for (final entry in kTranslationAssets.entries) {
      final map = _parse(await File(entry.value).readAsString());
      expect(map.length, 6236, reason: entry.key);
      expect(map['1:1'], isNotEmpty);
      expect(map['114:6'], isNotEmpty);
    }
  });

  test('clean install does not expose optional translations', () async {
    final temp = await Directory.systemTemp.createTemp('content-packages-');
    addTearDown(() async {
      ContentPackageStore.instance.resetForTesting();
      if (await temp.exists()) await temp.delete(recursive: true);
    });
    ContentPackageStore.instance.setRootDirectoryForTesting(temp);

    final container = ProviderContainer();
    addTearDown(container.dispose);
    for (final id in kTranslationAssets.keys) {
      final map =
          await container.read(translationTextsProvider(id).future);
      expect(map, isEmpty, reason: id);
    }
  });

  test('unimported translation candidates stay unavailable', () async {
    final container = ProviderContainer();
    addTearDown(container.dispose);
    for (final t in kTranslationCatalog.where((t) => !t.bundled)) {
      final map =
          await container.read(translationTextsProvider(t.id).future);
      expect(map, isEmpty, reason: t.id);
      expect(t.version, isNotNull);
    }
  });
}
