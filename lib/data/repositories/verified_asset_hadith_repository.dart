// Verified-asset Hadith loader (bundled per-section JSON).
//
// Text and references are preserved VERBATIM from the upstream edition.
// Fields the source does not provide (grade, sanad breakdown, Arabic
// book titles) stay null so the UI renders them as unavailable —
// they are NEVER synthesized.
//
// Adding a collection = bundle `assets/hadith/<id>/index.json` +
// `sections/<n>.json` in the same shape and register it below.

import 'dart:convert';

import 'package:flutter/services.dart';
import '../../core/utils/text_utils.dart';
import '../models/hadith.dart';
import '../seed/hadith_collections.dart';
import 'hadith_repository.dart';

class _BundledCollection {
  const _BundledCollection({
    required this.id,
    required this.source,
    required this.version,
    required this.totalHadith,
    required this.sizeMb,
  });

  final String id;
  final String source;
  final String version;
  final int totalHadith;
  final double sizeMb;
}

const _bundled = [
  _BundledCollection(
    id: 'bukhari',
    source:
        'fawazahmed0/hadith-api@1 ara-bukhari (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 7589,
    sizeMb: 9.0,
  ),
  _BundledCollection(
    id: 'muslim',
    source:
        'fawazahmed0/hadith-api@1 ara-muslim (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 7563,
    sizeMb: 8.0,
  ),
  _BundledCollection(
    id: 'abudawud',
    source:
        'fawazahmed0/hadith-api@1 ara-abudawud (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 5274,
    sizeMb: 6.5,
  ),
  _BundledCollection(
    id: 'tirmidhi',
    source:
        'fawazahmed0/hadith-api@1 ara-tirmidhi (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 3998,
    sizeMb: 5.0,
  ),
  _BundledCollection(
    id: 'nasai',
    source:
        'fawazahmed0/hadith-api@1 ara-nasai (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 5765,
    sizeMb: 7.0,
  ),
  _BundledCollection(
    id: 'ibnmajah',
    source:
        'fawazahmed0/hadith-api@1 ara-ibnmajah (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 4343,
    sizeMb: 5.5,
  ),
  _BundledCollection(
    id: 'malik',
    source:
        'fawazahmed0/hadith-api@1 ara-malik (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 1858,
    sizeMb: 2.5,
  ),
  _BundledCollection(
    id: 'nawawi',
    source:
        'fawazahmed0/hadith-api@1 ara-nawawi (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 42,
    sizeMb: 0.3,
  ),
  _BundledCollection(
    id: 'qudsi',
    source:
        'fawazahmed0/hadith-api@1 ara-qudsi (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 40,
    sizeMb: 0.3,
  ),
  _BundledCollection(
    id: 'dehlawi',
    source:
        'fawazahmed0/hadith-api@1 ara-dehlawi (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 40,
    sizeMb: 0.3,
  ),
];

/// Collection list with bundled state; the rest load on future
/// download integration.
List<HadithCollection> get kBundledHadithCollections {
  final byId = {for (final b in _bundled) b.id: b};
  return kHadithCollections.map((c) {
    final b = byId[c.id];
    if (b == null) {
      return c;
    }
    return HadithCollection(
      id: c.id,
      nameAr: c.nameAr,
      nameEn: c.nameEn,
      nameFr: c.nameFr,
      compiler: c.compiler,
      source: b.source,
      version: b.version,
      totalHadith: b.totalHadith,
      isDownloaded: true,
      downloadSizeMb: b.sizeMb,
    );
  }).toList();
}

class BundledSection {
  const BundledSection({
    required this.section,
    required this.title,
    required this.first,
    required this.last,
    required this.count,
  });

  final int section;
  final String title;
  final int first;
  final int last;
  final int count;
}

/// Backwards-compatible alias (Bukhari was the first bundled collection).
typedef BukhariSection = BundledSection;

class VerifiedAssetHadithRepository implements HadithRepository {
  VerifiedAssetHadithRepository({AssetBundle? bundle})
      : _bundle = bundle ?? rootBundle;

  final AssetBundle _bundle;
  final Map<String, List<BundledSection>> _indices = {};
  final Map<String, List<Hadith>> _sections = {};

  bool get isBundled => true;

  bool isCollectionBundled(String id) =>
      _bundled.any((b) => b.id == id);

  Future<List<BundledSection>> _loadIndex(String collectionId) async {
    final hit = _indices[collectionId];
    if (hit != null) {
      return hit;
    }
    final raw = await _bundle.loadString(
        'assets/hadith/$collectionId/index.json',
        cache: false);
    final json = jsonDecode(raw) as Map<String, dynamic>;
    final out = <BundledSection>[];
    for (final e in (json['sections'] as List)) {
      final m = e as Map<String, dynamic>;
      out.add(BundledSection(
        section: (m['section'] as num).toInt(),
        title: m['title'] as String,
        first: (m['first'] as num).toInt(),
        last: (m['last'] as num).toInt(),
        count: (m['count'] as num).toInt(),
      ));
    }
    _indices[collectionId] = out;
    return out;
  }

  Hadith _mapEntry(
      String collectionId, BundledSection meta, Map<String, dynamic> m,
      {required int sameCount}) {
    final number = (m['hadithnumber'] as num).toInt();
    final text = (m['text'] as String?) ?? '';
    final id = sameCount == 0
        ? '$collectionId:$number'
        : '$collectionId:$number#${sameCount + 1}';
    return Hadith(
      id: id,
      collectionId: collectionId,
      book: meta.title,
      bookAr: '',
      chapter: meta.title,
      chapterAr: '',
      hadithNumber: '$number',
      matnAr: text,
      narrator: null,
      sanadAr: null,
      grade: null,
      gradingAuthority: null,
      // Upstream numbers without matn keep their reference honestly.
      isPlaceholder: text.isEmpty,
    );
  }

  Future<List<Hadith>> _loadSection(
      String collectionId, int section) async {
    final key = '$collectionId:$section';
    final hit = _sections[key];
    if (hit != null) {
      return hit;
    }
    final meta = (await _loadIndex(collectionId))
        .firstWhere((e) => e.section == section);
    final raw = await _bundle.loadString(
        'assets/hadith/$collectionId/sections/$section.json',
        cache: false);
    final json = jsonDecode(raw) as Map<String, dynamic>;
    final out = <Hadith>[];
    for (final e in (json['hadiths'] as List)) {
      final m = e as Map<String, dynamic>;
      final number = (m['hadithnumber'] as num).toInt();
      final same =
          out.where((h) => h.hadithNumber == '$number').length;
      out.add(_mapEntry(collectionId, meta, m, sameCount: same));
    }
    _sections[key] = out;
    return out;
  }

  /// Public section catalog (book/chapter browser).
  Future<List<BundledSection>> sections(
          [String collectionId = 'bukhari']) =>
      _loadIndex(collectionId);

  /// Public per-section hadiths (book/chapter browser).
  Future<List<Hadith>> sectionHadiths(int section,
          [String collectionId = 'bukhari']) =>
      _loadSection(collectionId, section);

  /// All bundled hadiths of one collection, section order.
  Future<List<Hadith>> allIn(String collectionId) async {
    final index = await _loadIndex(collectionId);
    final out = <Hadith>[];
    for (final s in index) {
      out.addAll(await _loadSection(collectionId, s.section));
    }
    return out;
  }

  /// Backwards-compatible Bukhari accessor.
  Future<List<Hadith>> allBukhari() => allIn('bukhari');

  @override
  Future<List<HadithCollection>> collections() async =>
      kBundledHadithCollections;

  @override
  Future<List<Hadith>> query(HadithFilter filter,
      {int limit = 30, int offset = 0}) async {
    final ids = _bundled
        .map((b) => b.id)
        .where((id) =>
            filter.collectionIds.isEmpty ||
            filter.collectionIds.contains(id))
        .toList();
    if (ids.isEmpty) {
      return [];
    }

    final q = filter.query?.trim() ?? '';
    final normQ = q.isEmpty ? '' : normalizeArabic(q);
    final out = <Hadith>[];

    bool matches(Hadith h) {
      if (h.isPlaceholder) {
        return false;
      }
      if (filter.book != null &&
          filter.book!.isNotEmpty &&
          h.book != filter.book) {
        return false;
      }
      if (q.isNotEmpty &&
          !normalizeArabic(h.matnAr).contains(normQ) &&
          !h.hadithNumber.contains(q)) {
        return false;
      }
      return true;
    }

    for (final id in ids) {
      for (final h in await allIn(id)) {
        if (matches(h)) {
          out.add(h);
          if (out.length >= offset + limit) {
            break;
          }
        }
      }
      if (out.length >= offset + limit) {
        break;
      }
    }
    if (offset >= out.length) {
      return [];
    }
    return out.sublist(offset);
  }

  @override
  Future<List<Hadith>> related(String hadithId) async {
    // Parallel-narration graph requires a verified cross-reference
    // dataset; until then, honestly empty.
    return [];
  }
}
