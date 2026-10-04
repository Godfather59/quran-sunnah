// ─────────────────────────────────────────────────────────────
// Hadith domain models. Every field preserves source provenance.
// Never merge parallel narrations into one fabricated text.
// ─────────────────────────────────────────────────────────────

class HadithCollection {
  const HadithCollection({
    required this.id,
    required this.nameAr,
    required this.nameEn,
    required this.nameFr,
    required this.compiler,
    required this.source,
    required this.version,
    this.totalHadith,
    this.isDownloaded = false,
    this.downloadSizeMb,
  });

  final String id; // e.g. "bukhari"
  final String nameAr;
  final String nameEn;
  final String nameFr;
  final String compiler;
  final String source;
  final String version;
  final int? totalHadith;
  final bool isDownloaded;
  final double? downloadSizeMb;
}

class Hadith {
  const Hadith({
    required this.id,
    required this.collectionId,
    required this.book,
    required this.bookAr,
    required this.chapter,
    required this.chapterAr,
    required this.hadithNumber,
    required this.matnAr,
    this.matnTranslation,
    this.sanadAr,
    this.narrator,
    this.grade,
    this.gradingAuthority,
    this.topics = const [],
    this.parallelIds = const [],
    this.isPlaceholder = false,
  });

  final String id; // "<collection>:<number>"
  final String collectionId;
  final String book;
  final String bookAr;
  final String chapter;
  final String chapterAr;
  final String hadithNumber;
  final String matnAr;
  final String? matnTranslation;
  final String? sanadAr;
  final String? narrator;
  final String? grade; // e.g. "Sahih", "Hasan", "Da'if"
  final String? gradingAuthority; // scholar/source responsible
  final List<String> topics;
  final List<String> parallelIds;
  final bool isPlaceholder;
}

class NarratorProfile {
  const NarratorProfile({
    required this.nameAr,
    required this.nameEn,
    this.bio = '',
  });

  final String nameAr;
  final String nameEn;
  final String bio;
}
