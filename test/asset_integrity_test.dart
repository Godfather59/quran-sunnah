import 'dart:convert';
import 'dart:io';

import 'package:crypto/crypto.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

String _gitBlobSha(Uint8List bytes) {
  final header = utf8.encode('blob ${bytes.length}\u0000');
  return sha1.convert([...header, ...bytes]).toString();
}

Future<Uint8List> _sourceBytes(String path) async {
  final file = File(path);
  if (await file.exists()) {
    return file.readAsBytes();
  }
  final data = await rootBundle.load(path);
  return data.buffer.asUint8List(
    data.offsetInBytes,
    data.lengthInBytes,
  );
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('all religious source files match pinned exact bytes', () async {
    final raw = await rootBundle.loadString('assets/integrity_manifest.json');
    final manifest = jsonDecode(raw) as Map<String, dynamic>;
    expect(manifest['algorithm'], 'git-blob-sha1');

    final files =
        Map<String, dynamic>.from(manifest['files'] as Map<dynamic, dynamic>);
    expect(files.length, greaterThan(800));

    for (final entry in files.entries) {
      final bytes = await _sourceBytes(entry.key);
      expect(
        _gitBlobSha(bytes),
        entry.value,
        reason: 'source bytes changed: ${entry.key}',
      );
    }
  });
}
