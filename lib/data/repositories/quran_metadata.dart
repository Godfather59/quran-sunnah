// Verified Quran structural metadata (Tanzil quran-data.xml v1.0).
//
// Covers: 30 Juz starts, 240 Hizb-quarter starts, 604 Medina Mushaf
// page starts, 15 sajdas. Applies to the Hafs/Medina text ONLY —
// other Riwayat get different page mappings from their own sources.

import 'package:flutter/services.dart';
import 'package:xml/xml.dart';

class AyahRef {
  const AyahRef(this.surah, this.ayah);

  final int surah;
  final int ayah;

  bool get isValid => surah >= 1 && surah <= 114 && ayah >= 1;

  /// Lexicographic mushaf order comparison.
  int compareTo(AyahRef other) {
    if (surah != other.surah) return surah.compareTo(other.surah);
    return ayah.compareTo(other.ayah);
  }
}

class QuranMetadata {
  QuranMetadata._({
    required this.juzStarts,
    required this.quarterStarts,
    required this.pageStarts,
    required this.sajdas,
  });

  final List<AyahRef> juzStarts; // 30
  final List<AyahRef> quarterStarts; // 240
  final List<AyahRef> pageStarts; // 604
  final List<AyahRef> sajdas; // 15

  static QuranMetadata? _cached;

  static Future<QuranMetadata> load([AssetBundle? bundle]) async {
    final hit = _cached;
    if (hit != null) return hit;
    final raw = await (bundle ?? rootBundle)
        .loadString('assets/quran/metadata/quran-data.xml');
    final doc = XmlDocument.parse(raw);

    List<AyahRef> read(String tag) => doc
        .findAllElements(tag)
        .map((e) => AyahRef(
            int.parse(e.getAttribute('sura') ?? e.getAttribute('index') ?? '1'),
            int.parse(e.getAttribute('aya') ?? '1')))
        .toList();

    // juz/page/quarter elements carry sura+aya; sajda too.
    final meta = QuranMetadata._(
      juzStarts: read('juz'),
      quarterStarts: read('quarter'),
      pageStarts: read('page'),
      sajdas: read('sajda'),
    );
    _cached = meta;
    return meta;
  }

  static void debugResetCache() => _cached = null;

  /// 1-based index of the last start marker <= [ref].
  int _containing(List<AyahRef> starts, AyahRef ref) {
    var idx = 1;
    for (var i = 0; i < starts.length; i++) {
      if (starts[i].compareTo(ref) <= 0) {
        idx = i + 1;
      } else {
        break;
      }
    }
    return idx;
  }

  int juzOf(int surah, int ayah) =>
      _containing(juzStarts, AyahRef(surah, ayah));

  int quarterOf(int surah, int ayah) =>
      _containing(quarterStarts, AyahRef(surah, ayah));

  /// Hizb number 1..60 derived from quarter.
  int hizbOf(int surah, int ayah) =>
      ((quarterOf(surah, ayah) - 1) ~/ 4) + 1;

  /// Rubʿ within hizb, 1..4.
  int rubOf(int surah, int ayah) =>
      ((quarterOf(surah, ayah) - 1) % 4) + 1;

  int pageOf(int surah, int ayah) =>
      _containing(pageStarts, AyahRef(surah, ayah));

  bool isSajda(int surah, int ayah) => sajdas.any(
      (s) => s.surah == surah && s.ayah == ayah);
}
