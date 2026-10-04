import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Download state per dataset id ("quran:hafs-an-asim__uthmani",
/// "hadith:bukhari", "audio:mishary-hafs", ...).
class DownloadState {
  const DownloadState({
    this.installed = const {},
    this.progress = const {},
  });

  final Set<String> installed;
  final Map<String, double> progress;

  DownloadState copyWith({Set<String>? installed, Map<String, double>? p}) =>
      DownloadState(
          installed: installed ?? this.installed,
          progress: p ?? progress);
}

/// Datasets shipped inside the app bundle — always "installed".
/// Mirrors pubspec assets + repository bundle tables.
const kBundledDatasetIds = {
  'quran:hafs-an-asim__uthmani',
  'quran:hafs-an-asim__imlai',
  'quran:hafs-an-asim__indopak',
  'quran:warsh-an-nafi__uthmani',
  'quran:qalun-an-nafi__uthmani',
  'quran:metadata',
  'quran:en-sahih',
  'quran:fr-hamidullah',
  'quran:tafsir-jalalayn',
  'quran:tafsir-siraj',
  'hadith:bukhari',
  'hadith:muslim',
  'hadith:abudawud',
  'hadith:tirmidhi',
  'hadith:nasai',
  'hadith:ibnmajah',
  'hadith:malik',
  'hadith:nawawi',
  'hadith:qudsi',
  'hadith:dehlawi',
};

class DownloadNotifier extends StateNotifier<DownloadState> {
  DownloadNotifier()
      : super(const DownloadState(installed: kBundledDatasetIds));

  bool isInstalled(String id) => state.installed.contains(id);

  /// Bundled ids cannot be removed (they ship with the app).
  bool isProtected(String id) => kBundledDatasetIds.contains(id);

  /// General remote dataset installation is intentionally unavailable
  /// until a verified downloader with checksums/provenance is wired.
  /// Never simulate a successful religious-dataset download.
  Future<void> install(String id) async {
    throw UnsupportedError(
      'Remote dataset installation is not available for $id. '
      'Only verified bundled datasets are exposed as installed.',
    );
  }

  void remove(String id) {
    if (kBundledDatasetIds.contains(id)) {
      return; // ships with the app — cannot be removed.
    }
    state = DownloadState(
      installed: {...state.installed}..remove(id),
      progress: state.progress,
    );
  }
}

final downloadProvider =
    StateNotifierProvider<DownloadNotifier, DownloadState>(
        (ref) => DownloadNotifier());

