import 'dart:convert';
import 'package:crypto/crypto.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

String _gitBlobSha(Uint8List bytes) {
  final header = utf8.encode('blob ${bytes.length}\u0000');
  final digest = sha1.convert([...header, ...bytes]);
  return digest.toString();
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('bundled religious datasets match pinned exact bytes', () async {
    final raw = await rootBundle.loadString('assets/integrity_manifest.json');
    final manifest = jsonDecode(raw) as Map<String, dynamic>;
    expect(manifest['algorithm'], 'git-blob-sha1');

    final files =
        Map<String, dynamic>.from(manifest['files'] as Map<dynamic, dynamic>);
    expect(files.length, greaterThan(800));

    for (final entry in files.entries) {
      final data = await rootBundle.load(entry.key);
      final bytes = data.buffer.asUint8List(
        data.offsetInBytes,
        data.lengthInBytes,
      );
      expect(
        _gitBlobSha(bytes),
        entry.value,
        reason: 'asset bytes changed: ${entry.key}',
      );
    }
  });
}
