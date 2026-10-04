// Streaming Quran audio (Hafs only until matching recitations ship).
//
// Per-ayah files by GLOBAL ayah number (1..6236):
//   https://cdn.islamic.network/quran/audio/128/<identifier>/<n>.mp3
// (source: Islamic Network / EveryAyah; reachability verified 2026-10).
// A reciter is ONLY offered for the Riwaya they actually recite.

import 'dart:async';
import 'dart:io';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:just_audio/just_audio.dart';
import 'package:just_audio_background/just_audio_background.dart';
import 'package:path_provider/path_provider.dart';
import '../seed/surah_metadata.dart';
import '../../state/providers.dart';

class Reciter {
  const Reciter({
    required this.identifier,
    required this.nameEn,
    required this.nameAr,
    required this.riwayaKey,
  });

  final String identifier; // CDN edition id
  final String nameEn;
  final String nameAr;
  final String riwayaKey; // must match RiwayaId.storageKey

  String fileUrl(int globalAyah) =>
      'https://cdn.islamic.network/quran/audio/128/$identifier/$globalAyah.mp3';
}

/// All reciters below are documented Hafs ‘an ‘Asim reciters with
/// individually verified reachable streams. Never add a reciter here
/// without verifying BOTH their riwaya and stream reachability.
const List<Reciter> kReciters = [
  Reciter(
      identifier: 'ar.alafasy',
      nameEn: 'Mishary Alafasy',
      nameAr: 'مشاري العفاسي',
      riwayaKey: 'hafs-an-asim'),
  Reciter(
      identifier: 'ar.husary',
      nameEn: 'Mahmoud Al-Husary',
      nameAr: 'محمود خليل الحصري',
      riwayaKey: 'hafs-an-asim'),
  Reciter(
      identifier: 'ar.mahermuaiqly',
      nameEn: 'Maher Al-Muaiqly',
      nameAr: 'ماهر المعيقلي',
      riwayaKey: 'hafs-an-asim'),
  Reciter(
      identifier: 'ar.ahmedajamy',
      nameEn: 'Ahmed Ibn Ali Al-Ajamy',
      nameAr: 'أحمد بن علي العجمي',
      riwayaKey: 'hafs-an-asim'),
  Reciter(
      identifier: 'ar.shaatree',
      nameEn: 'Abu Bakr Ash-Shatree',
      nameAr: 'أبو بكر الشاطري',
      riwayaKey: 'hafs-an-asim'),
  Reciter(
      identifier: 'ar.hudhaify',
      nameEn: 'Ali Al-Hudhaify',
      nameAr: 'علي الحذيفي',
      riwayaKey: 'hafs-an-asim'),
  Reciter(
      identifier: 'ar.muhammadayyoub',
      nameEn: 'Muhammad Ayyoub',
      nameAr: 'محمد أيوب',
      riwayaKey: 'hafs-an-asim'),
  Reciter(
      identifier: 'ar.muhammadjibreel',
      nameEn: 'Muhammad Jibreel',
      nameAr: 'محمد جبريل',
      riwayaKey: 'hafs-an-asim'),
];

Reciter? reciterById(String identifier) =>
    kReciters.where((r) => r.identifier == identifier).firstOrNull;

List<Reciter> recitersForRiwaya(String riwayaKey) =>
    kReciters.where((r) => r.riwayaKey == riwayaKey).toList();

enum AudioDownloadStatus {
  queued,
  measuring,
  downloading,
  paused,
  completed,
  failed,
  canceled,
}

class AudioDownloadTask {
  const AudioDownloadTask({
    required this.reciterId,
    required this.surah,
    required this.status,
    this.progress = 0,
    this.totalBytes = 0,
    this.error,
  });

  final String reciterId;
  final int surah;
  final AudioDownloadStatus status;
  final double progress;
  final int totalBytes;
  final String? error;

  String get key => '$reciterId:$surah';

  AudioDownloadTask copyWith({
    AudioDownloadStatus? status,
    double? progress,
    int? totalBytes,
    String? error,
  }) =>
      AudioDownloadTask(
        reciterId: reciterId,
        surah: surah,
        status: status ?? this.status,
        progress: progress ?? this.progress,
        totalBytes: totalBytes ?? this.totalBytes,
        error: error,
      );
}

class AudioState {
  const AudioState({
    this.playing = false,
    this.loading = false,
    this.refKey,
    this.speed = 1.0,
    this.error,
    this.reciterId = 'ar.alafasy',
    this.offlineSurahs = const {},
    this.downloads = const {},
    this.storageBytes = 0,
    this.reciterStorageBytes = 0,
    this.surahStorageBytes = const {},
  });

  final bool playing;
  final bool loading;
  final String? refKey; // "surah:ayah" currently (starting) ayah
  final double speed;
  final String? error;
  final String reciterId;
  final Set<int> offlineSurahs;
  final Map<String, AudioDownloadTask> downloads;
  final int storageBytes;
  final int reciterStorageBytes;
  final Map<int, int> surahStorageBytes;

  AudioState copyWith({
    bool? playing,
    bool? loading,
    String? refKey,
    double? speed,
    String? error,
    String? reciterId,
    Set<int>? offlineSurahs,
    Map<String, AudioDownloadTask>? downloads,
    int? storageBytes,
    int? reciterStorageBytes,
    Map<int, int>? surahStorageBytes,
  }) =>
      AudioState(
        playing: playing ?? this.playing,
        loading: loading ?? this.loading,
        refKey: refKey ?? this.refKey,
        speed: speed ?? this.speed,
        error: error,
        reciterId: reciterId ?? this.reciterId,
        offlineSurahs: offlineSurahs ?? this.offlineSurahs,
        downloads: downloads ?? this.downloads,
        storageBytes: storageBytes ?? this.storageBytes,
        reciterStorageBytes:
            reciterStorageBytes ?? this.reciterStorageBytes,
        surahStorageBytes:
            surahStorageBytes ?? this.surahStorageBytes,
      );
}

/// Global ayah number 1..6236 (CDN file id).
int globalAyahNumber(int surah, int ayah) {
  var n = 0;
  for (final m in kSurahMetadata) {
    if (m.number < surah) {
      n += m.ayahCount;
    } else if (m.number == surah) {
      return n + ayah;
    } else {
      break;
    }
  }
  throw ArgumentError('invalid $surah:$ayah');
}

class AudioService extends StateNotifier<AudioState> {
  AudioService(this._ref, {AudioCache? cache})
      : _cache = cache ?? AudioCache(),
        super(AudioState(
          reciterId: _initialReciter(_ref),
          speed: _ref.read(appPrefsProvider).playbackSpeed,
        )) {
    _player.playbackEventStream.listen((_) {
      final playing = _player.playing;
      if (playing != state.playing) {
        state = state.copyWith(playing: playing);
      }
    });
    _player.currentIndexStream.listen((i) {
      final seq = _player.sequence;
      if (i != null && seq != null && i < seq.length) {
        final tag = seq[i].tag;
        if (tag is MediaItem) {
          state = state.copyWith(refKey: tag.id);
        }
      }
    });
    refreshOffline();
  }

  final _player = AudioPlayer();
  final AudioCache _cache;
  final Ref _ref;
  Timer? _sleepTimer;
  final List<(Reciter, int, int)> _downloadQueue = [];
  final Set<String> _pausedDownloads = {};
  final Set<String> _cancelledDownloads = {};
  bool _drainingDownloads = false;

  static String _initialReciter(Ref ref) {
    final saved = ref.read(appPrefsProvider).qari;
    if (reciterById(saved) != null) {
      return saved;
    }
    return kReciters.first.identifier;
  }

  /// Reciters verified for [riwayaKey]. Empty = honestly no recitation.
  List<Reciter> recitersFor(String riwayaKey) =>
      recitersForRiwaya(riwayaKey);

  /// Backwards-compatible single-prefix lookup.
  String? streamPrefixFor(String riwayaKey) {
    final list = recitersForRiwaya(riwayaKey);
    if (list.isEmpty) {
      return null;
    }
    return 'https://cdn.islamic.network/quran/audio/128/${list.first.identifier}';
  }

  void selectReciter(String identifier) {
    if (reciterById(identifier) != null) {
      state = state.copyWith(reciterId: identifier);
      final prefs = _ref.read(appPrefsProvider);
      _ref
          .read(appPrefsProvider.notifier)
          .update(prefs.copyWith(qari: identifier));
      refreshOffline();
    }
  }

  Future<void> refreshOffline() async {
    final surahs = await _cache.downloadedSurahs(state.reciterId);
    final perSurah = await _cache.surahStorageBytes(state.reciterId);
    final reciterStorage =
        perSurah.values.fold<int>(0, (sum, bytes) => sum + bytes);
    final storage = await _cache.storageBytes();
    if (mounted) {
      state = state.copyWith(
        offlineSurahs: surahs,
        storageBytes: storage,
        reciterStorageBytes: reciterStorage,
        surahStorageBytes: perSurah,
      );
    }
  }

  Future<void> playRange({
    required String riwayaKey,
    required int surah,
    required int fromAyah,
    int? toAyah,
    bool repeatAyah = false,
    String? reciterId,
  }) async {
    final reciter = reciterById(reciterId ?? state.reciterId);
    if (reciter == null || reciter.riwayaKey != riwayaKey) {
      state = state.copyWith(
          error:
              'Selected reciter does not recite this Riwaya — choose a matching reciter.',
          playing: false,
          loading: false);
      return;
    }
    state = state.copyWith(
        loading: true, error: null, refKey: '$surah:$fromAyah');
    try {
      final meta =
          kSurahMetadata.firstWhere((m) => m.number == surah);
      final end = (toAyah ?? meta.ayahCount)
          .clamp(fromAyah, meta.ayahCount);
      final sources = <AudioSource>[];
      for (var a = fromAyah; a <= end; a++) {
        final uri =
            await _cache.resolve(reciter, surah, a);
        sources.add(AudioSource.uri(uri,
            tag: MediaItem(
              id: '$surah:$a',
              title: 'Ayah $a · ${meta.nameEn}',
              artist: reciter.nameEn,
              album: 'Surah ${meta.nameEn}',
            )));
      }
      await _player.setAudioSource(
          ConcatenatingAudioSource(children: sources));
      await _player.setSpeed(state.speed);
      await _player.setLoopMode(
          repeatAyah ? LoopMode.one : LoopMode.off);
      await _player.play();
      state = state.copyWith(loading: false);
    } catch (e) {
      state = state.copyWith(
          loading: false, playing: false, error: '$e');
    }
  }

  Future<void> pause() => _player.pause();
  Future<void> resume() => _player.play();
  Future<void> stop() => _player.stop();

  Future<void> setSpeed(double speed) async {
    state = state.copyWith(speed: speed);
    final prefs = _ref.read(appPrefsProvider);
    await _ref
        .read(appPrefsProvider.notifier)
        .update(prefs.copyWith(playbackSpeed: speed));
    await _player.setSpeed(speed);
  }

  Future<void> setRepeatAyah(bool repeat) =>
      _player.setLoopMode(repeat ? LoopMode.one : LoopMode.off);

  void sleepTimer(Duration? duration) {
    _sleepTimer?.cancel();
    _sleepTimer = null;
    if (duration != null) {
      _sleepTimer = Timer(duration, () => _player.pause());
    }
  }

  String _downloadKey(Reciter reciter, int surah) =>
      '${reciter.identifier}:$surah';

  Future<int> estimateSurahBytes(Reciter reciter, int surah) async {
    final meta = kSurahMetadata.firstWhere((m) => m.number == surah);
    final client = HttpClient();
    try {
      var total = 0;
      for (var ayah = 1; ayah <= meta.ayahCount; ayah++) {
        total += await _cache.remoteSize(client, reciter, surah, ayah);
      }
      return total;
    } finally {
      client.close();
    }
  }

  Future<void> queueSurahDownload({
    required Reciter reciter,
    required int surah,
    required int totalBytes,
  }) async {
    final key = _downloadKey(reciter, surah);
    final existing = state.downloads[key];
    if (existing != null &&
        (existing.status == AudioDownloadStatus.queued ||
            existing.status == AudioDownloadStatus.downloading ||
            existing.status == AudioDownloadStatus.measuring ||
            existing.status == AudioDownloadStatus.paused)) {
      return;
    }
    _cancelledDownloads.remove(key);
    _pausedDownloads.remove(key);
    _downloadQueue.add((reciter, surah, totalBytes));
    _setDownloadTask(AudioDownloadTask(
      reciterId: reciter.identifier,
      surah: surah,
      status: AudioDownloadStatus.queued,
      totalBytes: totalBytes,
    ));
    unawaited(_drainDownloadQueue());
  }

  void pauseDownload(String key) {
    final task = state.downloads[key];
    if (task == null ||
        task.status != AudioDownloadStatus.downloading) {
      return;
    }
    _pausedDownloads.add(key);
    _setDownloadTask(task.copyWith(status: AudioDownloadStatus.paused));
  }

  void resumeDownload(String key) {
    final task = state.downloads[key];
    if (task == null || task.status != AudioDownloadStatus.paused) {
      return;
    }
    _pausedDownloads.remove(key);
    _setDownloadTask(task.copyWith(status: AudioDownloadStatus.downloading));
  }

  void cancelDownload(String key) {
    _cancelledDownloads.add(key);
    _pausedDownloads.remove(key);
    final task = state.downloads[key];
    if (task != null) {
      _setDownloadTask(task.copyWith(status: AudioDownloadStatus.canceled));
    }
  }

  Future<void> retryDownload(String key) async {
    final task = state.downloads[key];
    if (task == null) return;
    final reciter = reciterById(task.reciterId);
    if (reciter == null) return;
    await queueSurahDownload(
      reciter: reciter,
      surah: task.surah,
      totalBytes: task.totalBytes,
    );
  }

  void dismissDownload(String key) {
    final next = Map<String, AudioDownloadTask>.from(state.downloads)
      ..remove(key);
    state = state.copyWith(downloads: next);
  }

  void _setDownloadTask(AudioDownloadTask task) {
    final next = Map<String, AudioDownloadTask>.from(state.downloads)
      ..[task.key] = task;
    state = state.copyWith(downloads: next);
  }

  Future<void> _drainDownloadQueue() async {
    if (_drainingDownloads) return;
    _drainingDownloads = true;
    try {
      while (_downloadQueue.isNotEmpty) {
        final (reciter, surah, totalBytes) = _downloadQueue.removeAt(0);
        final key = _downloadKey(reciter, surah);
        if (_cancelledDownloads.contains(key)) continue;
        await _runDownload(reciter, surah, totalBytes);
      }
    } finally {
      _drainingDownloads = false;
    }
  }

  Future<void> _runDownload(
    Reciter reciter,
    int surah,
    int totalBytes,
  ) async {
    final key = _downloadKey(reciter, surah);
    final meta = kSurahMetadata.firstWhere((m) => m.number == surah);
    final client = HttpClient();
    try {
      var completedBytes = 0;
      _setDownloadTask(AudioDownloadTask(
        reciterId: reciter.identifier,
        surah: surah,
        status: AudioDownloadStatus.downloading,
        totalBytes: totalBytes,
      ));
      for (var ayah = 1; ayah <= meta.ayahCount; ayah++) {
        while (_pausedDownloads.contains(key) &&
            !_cancelledDownloads.contains(key)) {
          await Future<void>.delayed(const Duration(milliseconds: 200));
        }
        if (_cancelledDownloads.contains(key)) {
          _setDownloadTask((state.downloads[key]!).copyWith(
            status: AudioDownloadStatus.canceled,
          ));
          return;
        }

        final expected =
            await _cache.remoteSize(client, reciter, surah, ayah);
        await _cache.fetch(client, reciter, surah, ayah);
        completedBytes += expected;
        final progress = totalBytes <= 0
            ? ayah / meta.ayahCount
            : (completedBytes / totalBytes).clamp(0.0, 1.0);
        final current = state.downloads[key];
        if (current != null) {
          _setDownloadTask(current.copyWith(
            status: AudioDownloadStatus.downloading,
            progress: progress,
          ));
        }
      }

      final current = state.downloads[key];
      if (current != null) {
        _setDownloadTask(current.copyWith(
          status: AudioDownloadStatus.completed,
          progress: 1,
        ));
      }
      await refreshOffline();
    } catch (e) {
      final current = state.downloads[key];
      if (current != null) {
        _setDownloadTask(current.copyWith(
          status: AudioDownloadStatus.failed,
          error: '$e',
        ));
      }
    } finally {
      client.close();
      _cancelledDownloads.remove(key);
      _pausedDownloads.remove(key);
    }
  }

  /// Download a whole surah for offline use. Size is computed with
  /// HEAD requests BEFORE downloading; [onProgress] gets 0..1.
  /// Returns total bytes downloaded.
  Future<int> downloadSurah({
    required Reciter reciter,
    required int surah,
    void Function(double progress)? onProgress,
    Future<bool> Function(int bytes)? confirm,
  }) async {
    final meta =
        kSurahMetadata.firstWhere((m) => m.number == surah);
    final client = HttpClient();
    try {
      // 1. Measure first (spec: show size before downloading).
      var total = 0;
      final sizes = <int>[];
      for (var a = 1; a <= meta.ayahCount; a++) {
        final size = await _cache.remoteSize(
            client, reciter, surah, a);
        sizes.add(size);
        total += size;
      }
      if (confirm != null && !await confirm(total)) {
        return 0;
      }
      // 2. Download missing files.
      var done = 0;
      for (var a = 1; a <= meta.ayahCount; a++) {
        await _cache.fetch(client, reciter, surah, a);
        done += sizes[a - 1];
        onProgress?.call(total == 0 ? 1 : done / total);
      }
      await refreshOffline();
      return total;
    } finally {
      client.close();
    }
  }

  Future<void> deleteSurah(Reciter reciter, int surah) async {
    await _cache.deleteSurah(reciter, surah);
    dismissDownload(_downloadKey(reciter, surah));
    await refreshOffline();
  }

  @override
  void dispose() {
    _sleepTimer?.cancel();
    _player.dispose();
    super.dispose();
  }
}

/// Local-first ayah audio cache:
/// `<docs>/audio/<reciter>/<surah>/<ayah>.mp3`.
class AudioCache {
  String dirFor(String reciterId, int surah) =>
      'audio/$reciterId/$surah';

  String fileName(int ayah) => '$ayah.mp3';

  Future<Directory> _dir(String reciterId, int surah) async {
    final base = await getApplicationDocumentsDirectory();
    final dir = Directory('${base.path}/${dirFor(reciterId, surah)}');
    if (!await dir.exists()) {
      await dir.create(recursive: true);
    }
    return dir;
  }

  Future<File> _file(
      String reciterId, int surah, int ayah) async {
    final dir = await _dir(reciterId, surah);
    return File('${dir.path}/${fileName(ayah)}');
  }

  /// Local file if present, else the streaming URL.
  Future<Uri> resolve(Reciter reciter, int surah, int ayah) async {
    final file =
        await _file(reciter.identifier, surah, ayah);
    if (await file.exists() && await file.length() > 0) {
      return file.uri;
    }
    return Uri.parse(
        reciter.fileUrl(globalAyahNumber(surah, ayah)));
  }

  Future<int> remoteSize(HttpClient client, Reciter reciter,
      int surah, int ayah) async {
    try {
      final req = await client.headUrl(Uri.parse(
          reciter.fileUrl(globalAyahNumber(surah, ayah))));
      final res = await req.close();
      await res.drain();
      return res.contentLength < 0 ? 0 : res.contentLength;
    } catch (_) {
      return 0;
    }
  }

  Future<void> fetch(HttpClient client, Reciter reciter,
      int surah, int ayah) async {
    final file =
        await _file(reciter.identifier, surah, ayah);
    if (await file.exists() && await file.length() > 0) {
      return;
    }
    final req = await client.getUrl(Uri.parse(
        reciter.fileUrl(globalAyahNumber(surah, ayah))));
    final res = await req.close();
    if (res.statusCode != 200) {
      throw HttpException(
          'audio ${res.statusCode} for $surah:$ayah');
    }
    final part = File('${file.path}.part');
    if (await part.exists()) {
      await part.delete();
    }
    try {
      final sink = part.openWrite();
      await res.pipe(sink);
      final actual = await part.length();
      final expected = res.contentLength;
      if (actual <= 0 || (expected > 0 && actual != expected)) {
        throw HttpException(
          'incomplete audio download for $surah:$ayah '
          '(expected $expected bytes, got $actual)',
        );
      }
      if (await file.exists()) {
        await file.delete();
      }
      await part.rename(file.path);
    } catch (_) {
      if (await part.exists()) {
        await part.delete();
      }
      rethrow;
    }
  }

  Future<Map<int, int>> surahStorageBytes(String reciterId) async {
    try {
      final base = await getApplicationDocumentsDirectory();
      final root = Directory('${base.path}/audio/$reciterId');
      if (!await root.exists()) return const {};
      final out = <int, int>{};
      await for (final entity in root.list()) {
        if (entity is! Directory) continue;
        final surah =
            int.tryParse(entity.path.split(Platform.pathSeparator).last);
        if (surah == null) continue;
        var bytes = 0;
        await for (final file in entity.list()) {
          if (file is File && file.path.endsWith('.mp3')) {
            bytes += await file.length();
          }
        }
        if (bytes > 0) out[surah] = bytes;
      }
      return out;
    } catch (_) {
      return const {};
    }
  }

  Future<int> storageBytes() async {
    try {
      final base = await getApplicationDocumentsDirectory();
      final root = Directory('${base.path}/audio');
      if (!await root.exists()) return 0;
      var total = 0;
      await for (final entity in root.list(recursive: true)) {
        if (entity is File && entity.path.endsWith('.mp3')) {
          total += await entity.length();
        }
      }
      return total;
    } catch (_) {
      return 0;
    }
  }

  Future<Set<int>> downloadedSurahs(String reciterId) async {
    try {
      final base = await getApplicationDocumentsDirectory();
      final root = Directory('${base.path}/audio/$reciterId');
      if (!await root.exists()) {
        return {};
      }
      final out = <int>{};
      await for (final e in root.list()) {
        if (e is Directory) {
          final surah = int.tryParse(
              e.path.split(Platform.pathSeparator).last);
          if (surah == null) {
            continue;
          }
          final meta = kSurahMetadata
              .where((m) => m.number == surah)
              .firstOrNull;
          if (meta == null) {
            continue;
          }
          var count = 0;
          await for (final f in e.list()) {
            if (f is File &&
                f.path.endsWith('.mp3') &&
                await f.length() > 0) {
              count++;
            }
          }
          if (count >= meta.ayahCount) {
            out.add(surah);
          }
        }
      }
      return out;
    } catch (_) {
      return {};
    }
  }

  Future<void> deleteSurah(Reciter reciter, int surah) async {
    try {
      final base = await getApplicationDocumentsDirectory();
      final dir = Directory(
          '${base.path}/${dirFor(reciter.identifier, surah)}');
      if (await dir.exists()) {
        await dir.delete(recursive: true);
      }
    } catch (_) {}
  }
}

final audioServiceProvider =
    StateNotifierProvider<AudioService, AudioState>(
        (ref) => AudioService(ref));
