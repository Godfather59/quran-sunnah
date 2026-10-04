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
import '../content/content_packages.dart';
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
    required this.capabilities,
  });

  final String id;
  final String source;
  final String version;
  final int totalHadith;
  final double sizeMb;
  final HadithCapabilities capabilities;
}

const _bundled = [
  _BundledCollection(
    id: 'bukhari',
    source:
        'fawazahmed0/hadith-api@1 ara-bukhari (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 7589,
    sizeMb: 9.0,
    capabilities: HadithCapabilities(),
  ),
  _BundledCollection(
    id: 'muslim',
    source:
        'fawazahmed0/hadith-api@1 ara-muslim (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 7563,
    sizeMb: 8.0,
    capabilities: HadithCapabilities(),
  ),
  _BundledCollection(
    id: 'abudawud',
    source:
        'fawazahmed0/hadith-api@1 ara-abudawud (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 5274,
    sizeMb: 6.5,
    capabilities: HadithCapabilities(),
  ),
  _BundledCollection(
    id: 'tirmidhi',
    source:
        'fawazahmed0/hadith-api@1 ara-tirmidhi (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 3998,
    sizeMb: 5.0,
    capabilities: HadithCapabilities(),
  ),
  _BundledCollection(
    id: 'nasai',
    source:
        'fawazahmed0/hadith-api@1 ara-nasai (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 5765,
    sizeMb: 7.0,
    capabilities: HadithCapabilities(),
  ),
  _BundledCollection(
    id: 'ibnmajah',
    source:
        'fawazahmed0/hadith-api@1 ara-ibnmajah (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 4343,
    sizeMb: 5.5,
    capabilities: HadithCapabilities(),
  ),
  _BundledCollection(
    id: 'malik',
    source:
        'fawazahmed0/hadith-api@1 ara-malik (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 1858,
    sizeMb: 2.5,
    capabilities: HadithCapabilities(),
  ),
  _BundledCollection(
    id: 'nawawi',
    source:
        'fawazahmed0/hadith-api@1 ara-nawawi (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 42,
    sizeMb: 0.3,
    capabilities: HadithCapabilities(),
  ),
  _BundledCollection(
    id: 'qudsi',
    source:
        'fawazahmed0/hadith-api@1 ara-qudsi (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 40,
    sizeMb: 0.3,
    capabilities: HadithCapabilities(),
  ),
  _BundledCollection(
    id: 'dehlawi',
    source:
        'fawazahmed0/hadith-api@1 ara-dehlawi (grades unavailable)',
    version: 'hadith-api@1',
    totalHadith: 40,
    sizeMb: 0.3,
    capabilities: HadithCapabilities(),
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

  bool get isBundled => true;

  bool isCollectionKnown(String id) =>
      _bundled.any((b) => b.id == id);

  Future<bool> isCollectionInstalled(String id) =>
      ContentPackageStore.instance.isInstalled('hadith:$id');

  Future<List<BundledSection>> _loadIndex(String collectionId) async {
    final hit = _indices[collectionId];
    if (hit != null) {
      return hit;
    }
    if (!await isCollectionInstalled(collectionId)) {
      return const [];
    }
    final raw = await ContentPackageStore.instance.loadString(
      'assets/hadith/$collectionId/index.json',
      bundle: _bundle,
    );
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
    final narrator = (m['narrator'] as String?)?.trim();
    final sanad = (m['sanadAr'] as String?)?.trim();
    final grade = (m['grade'] as String?)?.trim();
    final authority = (m['gradingAuthority'] as String?)?.trim();
    final topics = (m['topics'] as List?)
            ?.map((e) => e.toString().trim())
            .where((e) => e.isNotEmpty)
            .toList(growable: false) ??
        const <String>[];

    return Hadith(
      id: id,
      collectionId: collectionId,
      book: meta.title,
      bookAr: '',
      chapter: meta.title,
      chapterAr: '',
      hadithNumber: '$number',
      matnAr: text,
      narrator: narrator?.isEmpty == true ? null : narrator,
      sanadAr: sanad?.isEmpty == true ? null : sanad,
      grade: grade?.isEmpty == true ? null : grade,
      gradingAuthority: authority?.isEmpty == true ? null : authority,
      topics: topics,
      // Upstream numbers without matn keep their reference honestly.
      isPlaceholder: text.isEmpty,
    );
  }

  Future<List<Hadith>> _loadSection(
      String collectionId, int section) async {
    final index = await _loadIndex(collectionId);
    if (index.isEmpty) return const [];
    final meta = index.firstWhere((e) => e.section == section);
    final raw = await ContentPackageStore.instance.loadString(
      'assets/hadith/$collectionId/sections/$section.json',
      bundle: _bundle,
    );
    final json = jsonDecode(raw) as Map<String, dynamic>;
    final out = <Hadith>[];
    for (final e in (json['hadiths'] as List)) {
      final m = e as Map<String, dynamic>;
      final number = (m['hadithnumber'] as num).toInt();
      final same =
          out.where((h) => h.hadithNumber == '$number').length;
      out.add(_mapEntry(collectionId, meta, m, sameCount: same));
    }
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
  Future<List<Hadith>> allForIndex(String collectionId) async {
    if (!await isCollectionInstalled(collectionId)) return const [];
    return allIn(collectionId);
  }

  @override
  Future<List<HadithCollection>> collections() async {
    final installed = await ContentPackageStore.instance.installedIds();
    final byId = {for (final b in _bundled) b.id: b};
    return kHadithCollections.map((item) {
      final meta = byId[item.id];
      if (meta == null) return item;
      return HadithCollection(
        id: item.id,
        nameAr: item.nameAr,
        nameEn: item.nameEn,
        nameFr: item.nameFr,
        compiler: item.compiler,
        source: meta.source,
        version: meta.version,
        totalHadith: meta.totalHadith,
        isDownloaded: installed.contains('hadith:${item.id}'),
        downloadSizeMb: meta.sizeMb,
      );
    }).toList(growable: false);
  }

  @override
  Future<List<Hadith>> query(HadithFilter filter,
      {int limit = 30, int offset = 0}) async {
    final installed = await ContentPackageStore.instance.installedIds();
    final ids = _bundled
        .map((b) => b.id)
        .where((id) => installed.contains('hadith:$id'))
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
      if (filter.number != null &&
          filter.number!.trim().isNotEmpty &&
          h.hadithNumber != filter.number!.trim()) {
        return false;
      }
      if (filter.narrator != null &&
          filter.narrator!.trim().isNotEmpty &&
          !(h.narrator ?? '')
              .toLowerCase()
              .contains(filter.narrator!.trim().toLowerCase())) {
        return false;
      }
      if (filter.grade != null &&
          filter.grade!.trim().isNotEmpty &&
          (h.grade ?? '').toLowerCase() !=
              filter.grade!.trim().toLowerCase()) {
        return false;
      }
      if (filter.topic != null &&
          filter.topic!.trim().isNotEmpty &&
          !h.topics.any((t) => t
              .toLowerCase()
              .contains(filter.topic!.trim().toLowerCase()))) {
        return false;
      }
      if (q.isNotEmpty &&
          !normalizeArabic(h.matnAr).contains(normQ) &&
          !h.hadithNumber.contains(q)) {
        return false;
      }
      return true;
    }

    var matched = 0;
    for (final id in ids) {
      final index = await _loadIndex(id);
      for (final section in index) {
        final items = await _loadSection(id, section.section);
        for (final h in items) {
          if (!matches(h)) continue;
          if (matched++ < offset) continue;
          out.add(h);
          if (out.length >= limit) {
            return out;
          }
        }
      }
    }
    return out;
  }

  @override
  Future<HadithCapabilities> capabilities(Set<String> collectionIds) async {
    final ids = collectionIds.isEmpty
        ? _bundled.map((b) => b.id).toSet()
        : collectionIds;
    var result = const HadithCapabilities();
    for (final item in _bundled.where((b) => ids.contains(b.id))) {
      result = result.merge(item.capabilities);
    }
    return result;
  }

  @override
  Future<List<Hadith>> related(String hadithId) async {
    // Parallel-narration graph requires a verified cross-reference
    // dataset; until then, honestly empty.
    return [];
  }
}
