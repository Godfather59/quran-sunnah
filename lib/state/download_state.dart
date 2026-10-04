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

  Future<void> install(String id) async {
    // Real implementation: chunked download w/ size check,
    // checksum verify, then mark installed. Stub simulates.
    for (var i = 1; i <= 10; i++) {
      await Future<void>.delayed(const Duration(milliseconds: 120));
      state = state.copyWith(
          p: {...state.progress, id: i / 10});
    }
    state = DownloadState(
      installed: {...state.installed, id},
      progress: {...state.progress}..remove(id),
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

/// Audio: qari MUST be bound to the riwaya it actually recites.
/// Never label a Hafs recording as Warsh/Qalun.
class QariBinding {
  const QariBinding(this.name, this.riwayaKey, this.sizeMb);
  final String name;
  final String riwayaKey; // must match RiwayaId.storageKey
  final double sizeMb;
}

const kQariCatalog = [
  QariBinding('Mishary Alafasy', 'hafs-an-asim', 1850),
  QariBinding('Abdul Basit (Murattal)', 'hafs-an-asim', 1620),
  QariBinding('Yassin Al-Jazaery (Warsh)', 'warsh-an-nafi', 1900),
];
