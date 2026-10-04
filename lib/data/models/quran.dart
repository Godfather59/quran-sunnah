// ─────────────────────────────────────────────────────────────
// Quran domain models.
//
// DATA INTEGRITY: Ayah.text must ONLY be populated from a verified
// Quran dataset (per-edition files). Never synthesize verses.
// ─────────────────────────────────────────────────────────────

/// Canonical Riwaya identifier. Each entry MUST map to its own
/// verified dataset file — never derive one riwaya from another.
enum RiwayaId {
  hafsAsim,
  shubahAsim,
  warshNafi,
  qalunNafi,
  bazziIbnKathir,
  qunbulIbnKathir,
  duriAbiAmr,
  susiAbiAmr,
  hishamIbnAmir,
  ibnDhakwanIbnAmir,
  khalafHamzah,
  khalladHamzah,
  abulHarithKisai,
  duriKisai,
}

extension RiwayaIdX on RiwayaId {
  String get storageKey {
    switch (this) {
      case RiwayaId.hafsAsim:
        return 'hafs-an-asim';
      case RiwayaId.shubahAsim:
        return 'shubah-an-asim';
      case RiwayaId.warshNafi:
        return 'warsh-an-nafi';
      case RiwayaId.qalunNafi:
        return 'qalun-an-nafi';
      case RiwayaId.bazziIbnKathir:
        return 'albazzi-an-ibn-kathir';
      case RiwayaId.qunbulIbnKathir:
        return 'qunbul-an-ibn-kathir';
      case RiwayaId.duriAbiAmr:
        return 'alduri-an-abi-amr';
      case RiwayaId.susiAbiAmr:
        return 'alsusi-an-abi-amr';
      case RiwayaId.hishamIbnAmir:
        return 'hisham-an-ibn-amir';
      case RiwayaId.ibnDhakwanIbnAmir:
        return 'ibn-dhakwan-an-ibn-amir';
      case RiwayaId.khalafHamzah:
        return 'khalaf-an-hamzah';
      case RiwayaId.khalladHamzah:
        return 'khallad-an-hamzah';
      case RiwayaId.abulHarithKisai:
        return 'abul-harith-an-kisai';
      case RiwayaId.duriKisai:
        return 'alduri-an-kisai';
    }
  }
}

class RiwayaInfo {
  const RiwayaInfo({
    required this.id,
    required this.qiraaAr,
    required this.qiraaEn,
    required this.riwayaAr,
    required this.riwayaEn,
    required this.datasetVersion,
    required this.source,
    this.isAvailableOfflineSeed = false,
    String? qiraaFr,
    String? riwayaFr,
  })  : qiraaFr = qiraaFr ?? qiraaEn,
        riwayaFr = riwayaFr ?? riwayaEn;

  final RiwayaId id;
  final String qiraaAr;
  final String qiraaEn;
  final String riwayaAr;
  final String riwayaEn;
  final String qiraaFr;
  final String riwayaFr;
  final String datasetVersion;
  final String source;
  final bool isAvailableOfflineSeed;
}

/// Writing style (rasm) — INDEPENDENT from [RiwayaId].
///
/// [tajweed] is retained only as a legacy persisted value from pre-1.0
/// builds. Tajweed is a presentation overlay on Hafs/Uthmani, not a
/// separate Quran edition. New UI must never select this enum value.
enum QuranScript { uthmani, imlai, indopak, tajweed }

extension QuranScriptX on QuranScript {
  /// Dataset-backed script. Legacy Tajweed resolves to Uthmani.
  QuranScript get datasetScript =>
      this == QuranScript.tajweed ? QuranScript.uthmani : this;
}

/// Reading layout.
enum ReadingMode { reading, mushaf }

/// Ayah number rendering.
enum AyahNumberStyle { arabicIndic, easternArabic, latin }

/// Font choice — independent from script & riwaya.
enum QuranFont { uthmani, naskh, notoNaskh, indopak }

/// One verified Quran edition = one riwaya + one script + source metadata.
class QuranEdition {
  const QuranEdition({
    required this.id,
    required this.riwaya,
    required this.script,
    required this.language,
    required this.source,
    required this.version,
    this.isDownloaded = false,
    this.downloadSizeMb,
  });

  final String id; // e.g. "hafs-an-asim__uthmani"
  final RiwayaId riwaya;
  final QuranScript script;
  final String language; // 'ar'
  final String source;
  final String version;
  final bool isDownloaded;
  final double? downloadSizeMb;
}

class SurahMeta {
  const SurahMeta({
    required this.number,
    required this.nameAr,
    required this.nameEn,
    required this.nameFr,
    required this.ayahCount,
    required this.makki,
    this.juzStart = 1,
  });

  final int number;
  final String nameAr;
  final String nameEn;
  final String nameFr;
  final int ayahCount;
  final bool makki;
  final int juzStart;
}

class Ayah {
  const Ayah({
    required this.surah,
    required this.ayah,
    required this.text,
    required this.editionId,
    this.juz,
    this.hizb,
    this.rub,
    this.page,
    this.canonicalSurah,
    this.canonicalAyah,
    this.displayAyah,
    this.isSajda = false,
    this.isPlaceholder = false,
  }) : assert(
          (canonicalSurah == null) == (canonicalAyah == null),
          'canonicalSurah and canonicalAyah must be set together',
        );

  final int surah;
  final int ayah;
  final String text;
  final String editionId;
  final int? juz;
  final int? hizb;
  final int? rub;
  final int? page;
  final bool isSajda;

  /// Optional mapping to the app-wide canonical verse identity.
  ///
  /// The bundled datasets are currently normalized to the same 6236-row
  /// coordinate system, so these are null today and [surah]/[ayah] are used.
  /// Future mushaf traditions may display a different ayah number while still
  /// pointing to the same canonical verse identity.
  final int? canonicalSurah;
  final int? canonicalAyah;

  /// Edition/mushaf-facing ayah number. Defaults to [ayah].
  final int? displayAyah;

  int get canonicalSurahNumber => canonicalSurah ?? surah;
  int get canonicalAyahNumber => canonicalAyah ?? ayah;
  int get displayAyahNumber => displayAyah ?? ayah;

  String get canonicalVerseId =>
      '$canonicalSurahNumber:$canonicalAyahNumber';

  /// True when no verified dataset row exists yet. UI must show
  /// "Content unavailable for this source." and never invent text.
  final bool isPlaceholder;

  String get key => canonicalVerseId;
}

class QuranTranslation {
  const QuranTranslation({
    required this.id,
    required this.language,
    required this.translator,
    required this.source,
  });

  final String id;
  final String language;
  final String translator;
  final String source;
}

class TafsirEntry {
  const TafsirEntry({
    required this.surah,
    required this.ayah,
    required this.tafsirId,
    required this.source,
    required this.text,
  });

  final int surah;
  final int ayah;
  final String tafsirId;
  final String source;
  final String text;
}
