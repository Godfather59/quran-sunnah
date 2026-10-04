import 'dart:convert';
import 'dart:io';

import 'package:crypto/crypto.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path_provider/path_provider.dart';

class ContentPackageFile {
  const ContentPackageFile({
    required this.path,
    required this.sizeBytes,
    required this.sha256,
  });

  factory ContentPackageFile.fromJson(Map<String, dynamic> json) =>
      ContentPackageFile(
        path: json['path'] as String,
        sizeBytes: (json['sizeBytes'] as num).toInt(),
        sha256: json['sha256'] as String,
      );

  final String path;
  final int sizeBytes;
  final String sha256;
}

class ContentPackage {
  const ContentPackage({
    required this.id,
    required this.titleAr,
    required this.titleEn,
    required this.version,
    required this.source,
    required this.licenseStatus,
    required this.sizeBytes,
    required this.sha256,
    required this.files,
  });

  factory ContentPackage.fromJson(Map<String, dynamic> json) =>
      ContentPackage(
        id: json['id'] as String,
        titleAr: json['titleAr'] as String,
        titleEn: json['titleEn'] as String,
        version: json['version'] as String,
        source: json['source'] as String,
        licenseStatus: json['licenseStatus'] as String,
        sizeBytes: (json['sizeBytes'] as num).toInt(),
        sha256: json['sha256'] as String,
        files: (json['files'] as List)
            .map((e) => ContentPackageFile.fromJson(
                  Map<String, dynamic>.from(e as Map),
                ))
            .toList(growable: false),
      );

  final String id;
  final String titleAr;
  final String titleEn;
  final String version;
  final String source;
  final String licenseStatus;
  final int sizeBytes;
  final String sha256;
  final List<ContentPackageFile> files;
}

class ContentPackageManifest {
  const ContentPackageManifest({
    required this.schemaVersion,
    required this.sourceRevision,
    required this.sourceBaseUrl,
    required this.packages,
  });

  factory ContentPackageManifest.fromJson(Map<String, dynamic> json) =>
      ContentPackageManifest(
        schemaVersion: (json['schemaVersion'] as num).toInt(),
        sourceRevision: json['sourceRevision'] as String,
        sourceBaseUrl: json['sourceBaseUrl'] as String,
        packages: (json['packages'] as List)
            .map((e) => ContentPackage.fromJson(
                  Map<String, dynamic>.from(e as Map),
                ))
            .toList(growable: false),
      );

  final int schemaVersion;
  final String sourceRevision;
  final String sourceBaseUrl;
  final List<ContentPackage> packages;

  ContentPackage? byId(String id) =>
      packages.where((p) => p.id == id).firstOrNull;

  ContentPackage? forAsset(String path) => packages
      .where((p) => p.files.any((f) => f.path == path))
      .firstOrNull;
}

const kCoreDatasetIds = <String>{
  'quran:hafs-an-asim__uthmani',
  'quran:hafs-an-asim__imlai',
  'quran:hafs-an-asim__indopak',
  'quran:warsh-an-nafi__uthmani',
  'quran:qalun-an-nafi__uthmani',
  'quran:metadata',
  'hadith:bukhari',
  'hadith:muslim',
};

class ContentPackageStore {
  ContentPackageStore._();

  static final ContentPackageStore instance = ContentPackageStore._();

  ContentPackageManifest? _manifest;
  Directory? _root;

  Future<ContentPackageManifest> manifest({AssetBundle? bundle}) async {
    final cached = _manifest;
    if (cached != null) return cached;
    final raw = await (bundle ?? rootBundle)
        .loadString('assets/content_packages.json', cache: false);
    final parsed = ContentPackageManifest.fromJson(
      Map<String, dynamic>.from(jsonDecode(raw) as Map),
    );
    _manifest = parsed;
    return parsed;
  }

  Future<Directory> rootDirectory() async {
    final cached = _root;
    if (cached != null) return cached;
    final support = await getApplicationSupportDirectory();
    final dir = Directory(
      '${support.path}${Platform.pathSeparator}content_packages',
    );
    await dir.create(recursive: true);
    _root = dir;
    return dir;
  }

  String _safeId(String id) =>
      id.replaceAll(RegExp(r'[^A-Za-z0-9._-]'), '_');

  Future<Directory> packageDirectory(String packageId) async {
    final root = await rootDirectory();
    return Directory(
      '${root.path}${Platform.pathSeparator}${_safeId(packageId)}',
    );
  }

  Future<Directory> stagingDirectory(String packageId) async {
    final root = await rootDirectory();
    return Directory(
      '${root.path}${Platform.pathSeparator}.staging'
      '${Platform.pathSeparator}${_safeId(packageId)}',
    );
  }

  String _nativeRelative(String path) =>
      path.split('/').join(Platform.pathSeparator);

  Future<File> installedFile(
    String packageId,
    String assetPath,
  ) async {
    final dir = await packageDirectory(packageId);
    return File(
      '${dir.path}${Platform.pathSeparator}${_nativeRelative(assetPath)}',
    );
  }

  Future<File> stagingFile(
    String packageId,
    String assetPath,
  ) async {
    final dir = await stagingDirectory(packageId);
    return File(
      '${dir.path}${Platform.pathSeparator}${_nativeRelative(assetPath)}',
    );
  }

  Future<File> markerFile(String packageId) async {
    final dir = await packageDirectory(packageId);
    return File(
      '${dir.path}${Platform.pathSeparator}.installed.json',
    );
  }

  Future<bool> isInstalled(
    String packageId, {
    AssetBundle? bundle,
  }) async {
    if (kCoreDatasetIds.contains(packageId)) return true;
    final pkg = (await manifest(bundle: bundle)).byId(packageId);
    if (pkg == null) return false;
    final marker = await markerFile(packageId);
    if (!await marker.exists()) return false;
    try {
      final json = jsonDecode(await marker.readAsString());
      return json is Map &&
          json['version'] == pkg.version &&
          json['sha256'] == pkg.sha256;
    } catch (_) {
      return false;
    }
  }

  Future<Set<String>> installedIds({AssetBundle? bundle}) async {
    final out = <String>{...kCoreDatasetIds};
    final m = await manifest(bundle: bundle);
    for (final pkg in m.packages) {
      if (await isInstalled(pkg.id, bundle: bundle)) {
        out.add(pkg.id);
      }
    }
    return out;
  }

  Future<String> loadString(
    String assetPath, {
    AssetBundle? bundle,
  }) async {
    final b = bundle ?? rootBundle;
    final m = await manifest(bundle: b);
    final pkg = m.forAsset(assetPath);
    if (pkg == null) {
      return b.loadString(assetPath, cache: false);
    }
    if (await isInstalled(pkg.id, bundle: b)) {
      final file = await installedFile(pkg.id, assetPath);
      if (await file.exists()) {
        return file.readAsString();
      }
    }
    // During tests or for core-migration compatibility, allow an asset
    // fallback only when Flutter actually bundled it.
    return b.loadString(assetPath, cache: false);
  }

  Future<int> installedStorageBytes({AssetBundle? bundle}) async {
    var total = 0;
    final m = await manifest(bundle: bundle);
    for (final pkg in m.packages) {
      if (await isInstalled(pkg.id, bundle: bundle)) {
        total += pkg.sizeBytes;
      }
    }
    return total;
  }

  Future<void> remove(String packageId) async {
    if (kCoreDatasetIds.contains(packageId)) return;
    final dir = await packageDirectory(packageId);
    if (await dir.exists()) await dir.delete(recursive: true);
    final staging = await stagingDirectory(packageId);
    if (await staging.exists()) await staging.delete(recursive: true);
  }
}

class PackageDownloadPaused implements Exception {
  const PackageDownloadPaused();
}

class PackageDownloadCancelled implements Exception {
  const PackageDownloadCancelled();
}

class ContentPackageDownloader {
  ContentPackageDownloader({ContentPackageStore? store})
      : store = store ?? ContentPackageStore.instance;

  final ContentPackageStore store;
  final Set<String> _pauseRequested = {};
  final Set<String> _cancelRequested = {};

  void pause(String id) => _pauseRequested.add(id);

  void cancel(String id) => _cancelRequested.add(id);

  Future<void> install(
    String packageId, {
    required void Function(int downloaded, int total) onProgress,
  }) async {
    _pauseRequested.remove(packageId);
    _cancelRequested.remove(packageId);

    final manifest = await store.manifest();
    final pkg = manifest.byId(packageId);
    if (pkg == null) {
      throw StateError('Unknown content package: $packageId');
    }
    if (await store.isInstalled(packageId)) {
      onProgress(pkg.sizeBytes, pkg.sizeBytes);
      return;
    }

    final staging = await store.stagingDirectory(packageId);
    await staging.create(recursive: true);

    var completedBytes = 0;
    for (final fileSpec in pkg.files) {
      final finalFile = await store.stagingFile(packageId, fileSpec.path);
      await finalFile.parent.create(recursive: true);
      if (await finalFile.exists() &&
          await _verifyFile(finalFile, fileSpec)) {
        completedBytes += fileSpec.sizeBytes;
        onProgress(completedBytes, pkg.sizeBytes);
        continue;
      }

      final part = File('${finalFile.path}.part');
      var existing = await part.exists() ? await part.length() : 0;
      if (existing > fileSpec.sizeBytes) {
        await part.delete();
        existing = 0;
      }

      final url = Uri.parse(
        '${manifest.sourceBaseUrl}${fileSpec.path}',
      );
      final client = HttpClient();
      try {
        var request = await client.getUrl(url);
        if (existing > 0) {
          request.headers.set(
            HttpHeaders.rangeHeader,
            'bytes=$existing-',
          );
        }
        var response = await request.close();

        if (existing > 0 &&
            response.statusCode != HttpStatus.partialContent) {
          await part.delete();
          existing = 0;
          request = await client.getUrl(url);
          response = await request.close();
        }
        if (response.statusCode != HttpStatus.ok &&
            response.statusCode != HttpStatus.partialContent) {
          throw HttpException(
            'HTTP ${response.statusCode} for $url',
          );
        }

        final sink = part.openWrite(
          mode: existing > 0 ? FileMode.append : FileMode.write,
        );
        var written = existing;
        try {
          await for (final chunk in response) {
            if (_cancelRequested.contains(packageId)) {
              throw const PackageDownloadCancelled();
            }
            if (_pauseRequested.contains(packageId)) {
              throw const PackageDownloadPaused();
            }
            sink.add(chunk);
            written += chunk.length;
            onProgress(
              completedBytes + written,
              pkg.sizeBytes,
            );
          }
        } finally {
          await sink.flush();
          await sink.close();
        }

        if (written != fileSpec.sizeBytes) {
          throw StateError(
            'Size mismatch for ${fileSpec.path}: '
            '$written != ${fileSpec.sizeBytes}',
          );
        }
        if (!await _verifyFile(part, fileSpec)) {
          await part.delete();
          throw StateError(
            'SHA-256 mismatch for ${fileSpec.path}',
          );
        }
        if (await finalFile.exists()) await finalFile.delete();
        await part.rename(finalFile.path);
        completedBytes += fileSpec.sizeBytes;
        onProgress(completedBytes, pkg.sizeBytes);
      } finally {
        client.close(force: true);
      }
    }

    final finalDir = await store.packageDirectory(packageId);
    if (await finalDir.exists()) {
      await finalDir.delete(recursive: true);
    }
    await finalDir.parent.create(recursive: true);
    await staging.rename(finalDir.path);
    final marker = await store.markerFile(packageId);
    await marker.writeAsString(
      jsonEncode({
        'id': pkg.id,
        'version': pkg.version,
        'sha256': pkg.sha256,
        'sourceRevision': manifest.sourceRevision,
      }),
      flush: true,
    );
    _pauseRequested.remove(packageId);
    _cancelRequested.remove(packageId);
  }

  Future<void> clearStaging(String packageId) async {
    final staging = await store.stagingDirectory(packageId);
    if (await staging.exists()) await staging.delete(recursive: true);
    _pauseRequested.remove(packageId);
    _cancelRequested.remove(packageId);
  }

  Future<bool> _verifyFile(
    File file,
    ContentPackageFile spec,
  ) async {
    if (!await file.exists()) return false;
    if (await file.length() != spec.sizeBytes) return false;
    final digest = await sha256.bind(file.openRead()).first;
    return digest.toString() == spec.sha256;
  }
}

final contentPackageManifestProvider =
    FutureProvider<ContentPackageManifest>(
  (ref) => ContentPackageStore.instance.manifest(),
);
