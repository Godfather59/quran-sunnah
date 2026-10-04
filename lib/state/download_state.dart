import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/content/content_packages.dart';

enum PackageTransferStatus {
  idle,
  downloading,
  paused,
  failed,
  installed,
}

class DownloadState {
  const DownloadState({
    this.installed = kCoreDatasetIds,
    this.progress = const {},
    this.status = const {},
    this.errors = const {},
    this.storageUsedBytes = 0,
  });

  final Set<String> installed;
  final Map<String, double> progress;
  final Map<String, PackageTransferStatus> status;
  final Map<String, String> errors;
  final int storageUsedBytes;

  DownloadState copyWith({
    Set<String>? installed,
    Map<String, double>? progress,
    Map<String, PackageTransferStatus>? status,
    Map<String, String>? errors,
    int? storageUsedBytes,
  }) =>
      DownloadState(
        installed: installed ?? this.installed,
        progress: progress ?? this.progress,
        status: status ?? this.status,
        errors: errors ?? this.errors,
        storageUsedBytes: storageUsedBytes ?? this.storageUsedBytes,
      );
}

const kBundledDatasetIds = kCoreDatasetIds;

class DownloadNotifier extends Notifier<DownloadState> {
  late final ContentPackageStore _store;
  late final ContentPackageDownloader _downloader;
  late final Future<void> ready;

  @override
  DownloadState build() {
    _store = ContentPackageStore.instance;
    _downloader = ContentPackageDownloader();
    ready = _hydrate();
    return const DownloadState();
  }

  Future<void> _hydrate() async {
    final installed = await _store.installedIds();
    if (!ref.mounted) return;
    final used = await _store.installedStorageBytes();
    if (!ref.mounted) return;
    final statuses = <String, PackageTransferStatus>{
      for (final id in installed)
        id: PackageTransferStatus.installed,
    };
    state = state.copyWith(
      installed: installed,
      status: statuses,
      storageUsedBytes: used,
    );
  }

  bool isInstalled(String id) => state.installed.contains(id);

  bool isProtected(String id) => kCoreDatasetIds.contains(id);

  PackageTransferStatus statusOf(String id) =>
      state.status[id] ??
      (state.installed.contains(id)
          ? PackageTransferStatus.installed
          : PackageTransferStatus.idle);

  Future<void> install(String id) async {
    await ready;
    if (isProtected(id) || state.installed.contains(id)) return;

    final status = {...state.status, id: PackageTransferStatus.downloading};
    final errors = {...state.errors}..remove(id);
    state = state.copyWith(status: status, errors: errors);

    try {
      await _downloader.install(
        id,
        onProgress: (downloaded, total) {
          final value =
              total <= 0 ? 0.0 : (downloaded / total).clamp(0.0, 1.0);
          state = state.copyWith(
            progress: {...state.progress, id: value},
            status: {
              ...state.status,
              id: PackageTransferStatus.downloading,
            },
          );
        },
      );
      await _hydrate();
      state = state.copyWith(
        progress: {...state.progress, id: 1.0},
        status: {
          ...state.status,
          id: PackageTransferStatus.installed,
        },
      );
    } on PackageDownloadPaused {
      state = state.copyWith(
        status: {...state.status, id: PackageTransferStatus.paused},
      );
    } on PackageDownloadCancelled {
      await _downloader.clearStaging(id);
      final p = {...state.progress}..remove(id);
      final s = {...state.status}..remove(id);
      state = state.copyWith(progress: p, status: s);
    } catch (error) {
      state = state.copyWith(
        status: {...state.status, id: PackageTransferStatus.failed},
        errors: {...state.errors, id: '$error'},
      );
      rethrow;
    }
  }

  Future<void> installMany(Iterable<String> ids) async {
    for (final id in ids.toSet()) {
      if (isProtected(id) || state.installed.contains(id)) continue;
      await install(id);
    }
  }

  void pause(String id) {
    if (statusOf(id) != PackageTransferStatus.downloading) return;
    _downloader.pause(id);
  }

  Future<void> resume(String id) => install(id);

  Future<void> retry(String id) => install(id);

  Future<void> cancel(String id) async {
    final wasDownloading =
        statusOf(id) == PackageTransferStatus.downloading;
    _downloader.cancel(id);
    if (!wasDownloading) {
      // Paused/failed transfers have no active stream left to perform
      // cancellation cleanup, so clear their resumable staging now.
      await _downloader.clearStaging(id);
    }
    final p = {...state.progress}..remove(id);
    final s = {...state.status, id: PackageTransferStatus.idle};
    final e = {...state.errors}..remove(id);
    state = state.copyWith(progress: p, status: s, errors: e);
    // Active downloads observe the cancel flag, close their stream, then
    // clear staging in install()'s cancellation handler.
  }

  Future<void> remove(String id) async {
    if (isProtected(id)) return;
    await _store.remove(id);
    final installed = {...state.installed}..remove(id);
    final p = {...state.progress}..remove(id);
    final s = {...state.status}..remove(id);
    final e = {...state.errors}..remove(id);
    final used = await _store.installedStorageBytes();
    state = state.copyWith(
      installed: installed,
      progress: p,
      status: s,
      errors: e,
      storageUsedBytes: used,
    );
  }
}

final downloadProvider =
    NotifierProvider<DownloadNotifier, DownloadState>(
  DownloadNotifier.new,
);
