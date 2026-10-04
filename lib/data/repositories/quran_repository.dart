// Repositories expose verified-data loading behind a stable interface.
// Swap LocalStub* with Drift/SQLite/asset-JSON loaders without UI changes.
//
// Verified source guidance (see README):
//  - Quran per-riwaya text: Tanzil.net / King Fahd Glorious Quran Complex
//  - Translations/tafsir: licensed copies with source attribution
//  - Hadith: Shamela / Dorar / licensed digitizations, preserving numbering

import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/quran.dart';
import '../seed/placeholders.dart';
import '../seed/surah_metadata.dart';
import 'verified_asset_quran_repository.dart';

abstract class QuranRepository {
  Future<List<Ayah>> ayahsOfSurah(int surah, String editionId);
  Future<List<Ayah>> ayahsOfJuz(int juz, String editionId);
  Future<List<Ayah>> ayahsOfPage(int page, String editionId);
  Future<List<Ayah>> compareAyah(int surah, int ayah, List<String> editionIds);

  /// Whole edition in mushaf order (for search/export). May be large;
  /// implementations cache the parse.
  Future<List<Ayah>> allAyahs(String editionId);
}

/// Stub: returns placeholders until verified dataset files are bundled.
/// NEVER generates verse wording.
class StubQuranRepository implements QuranRepository {
  @override
  Future<List<Ayah>> ayahsOfSurah(int surah, String editionId) async {
    final meta = kSurahMetadata.firstWhere((s) => s.number == surah);
    await Future<void>.delayed(const Duration(milliseconds: 120));
    return placeholderAyahs(
        surah: surah, count: meta.ayahCount, editionId: editionId);
  }

  @override
  Future<List<Ayah>> ayahsOfJuz(int juz, String editionId) async {
    await Future<void>.delayed(const Duration(milliseconds: 120));
    return [];
  }

  @override
  Future<List<Ayah>> ayahsOfPage(int page, String editionId) async {
    await Future<void>.delayed(const Duration(milliseconds: 120));
    return [];
  }

  @override
  Future<List<Ayah>> allAyahs(String editionId) async => [];

  @override
  Future<List<Ayah>> compareAyah(
      int surah, int ayah, List<String> editionIds) async {
    // Real implementation: fetch the SAME surah:ayah from each
    // edition's own verified file. Never diff/generate.
    return editionIds
        .map((e) => Ayah(
            surah: surah,
            ayah: ayah,
            text: '',
            editionId: e,
            isPlaceholder: true))
        .toList();
  }
}

final quranRepositoryProvider =
    Provider<QuranRepository>(
        (ref) => VerifiedAssetQuranRepository());
