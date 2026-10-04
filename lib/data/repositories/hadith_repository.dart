import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/hadith.dart';
import '../seed/hadith_collections.dart';
import '../seed/placeholders.dart';
import 'verified_asset_hadith_repository.dart';

class HadithCapabilities {
  const HadithCapabilities({
    this.narrator = false,
    this.sanad = false,
    this.grade = false,
    this.topics = false,
  });

  final bool narrator;
  final bool sanad;
  final bool grade;
  final bool topics;

  bool get anyStructured => narrator || sanad || grade || topics;

  HadithCapabilities merge(HadithCapabilities other) => HadithCapabilities(
        narrator: narrator || other.narrator,
        sanad: sanad || other.sanad,
        grade: grade || other.grade,
        topics: topics || other.topics,
      );
}

class HadithFilter {
  const HadithFilter({
    this.collectionIds = const {},
    this.book,
    this.narrator,
    this.grade,
    this.topic,
    this.query,
    this.number,
  });

  final Set<String> collectionIds;
  final String? book;
  final String? narrator;
  final String? grade;
  final String? topic;
  final String? query;
  final String? number;

  HadithFilter copyWith({
    Set<String>? collectionIds,
    String? book,
    String? narrator,
    String? grade,
    String? topic,
    String? query,
    String? number,
  }) =>
      HadithFilter(
        collectionIds: collectionIds ?? this.collectionIds,
        book: book ?? this.book,
        narrator: narrator ?? this.narrator,
        grade: grade ?? this.grade,
        topic: topic ?? this.topic,
        query: query ?? this.query,
        number: number ?? this.number,
      );
}

abstract class HadithRepository {
  Future<List<HadithCollection>> collections();
  Future<List<Hadith>> query(HadithFilter filter,
      {int limit = 30, int offset = 0});

  /// Linear full-collection read for one-time local indexing.
  /// Implementations must return only source-backed rows.
  Future<List<Hadith>> allForIndex(String collectionId);

  Future<HadithCapabilities> capabilities(Set<String> collectionIds);

  Future<List<Hadith>> related(String hadithId);
}

/// Stub preserving reference structure. Real loader reads verified
/// per-collection JSON/SQLite (never invents matn/sanad/grades).
class StubHadithRepository implements HadithRepository {
  @override
  Future<List<HadithCollection>> collections() async =>
      kHadithCollections;

  @override
  Future<List<Hadith>> query(HadithFilter filter,
      {int limit = 30, int offset = 0}) async {
    await Future<void>.delayed(const Duration(milliseconds: 150));
    // Until datasets ship: one placeholder row demonstrating the card.
    if (offset > 0) return [];
    return [placeholderHadith];
  }

  @override
  Future<List<Hadith>> allForIndex(String collectionId) async => [];

  @override
  Future<HadithCapabilities> capabilities(Set<String> collectionIds) async =>
      const HadithCapabilities();

  @override
  Future<List<Hadith>> related(String hadithId) async => [];
}

final hadithRepositoryProvider =
    Provider<HadithRepository>(
        (ref) => VerifiedAssetHadithRepository());

final hadithFilterProvider =
    StateProvider<HadithFilter>((ref) => const HadithFilter(
          collectionIds: {'bukhari', 'muslim'},
        ));
