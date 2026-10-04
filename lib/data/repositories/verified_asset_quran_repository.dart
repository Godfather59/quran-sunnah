// Verified-asset Quran loader.
//
// Reads per-edition `surah|ayah|text` files (Tanzil Project, verbatim)
// from bundled assets. Text is preserved EXACTLY — no normalization,
// no trimming of inner content, no reconstruction. Lines that do not
// parse are ignored (never guessed); missing editions yield
// placeholders so the UI shows "Content unavailable for this source."

import 'package:flutter/services.dart';
import '../models/quran.dart';
import '../seed/placeholders.dart';
import '../seed/surah_metadata.dart';
import 'quran_metadata.dart';
import 'quran_repository.dart';

/// Asset table: editionId → asset path. One file per (riwaya, script).
/// Add a row + ship the file to support a new edition. No UI change.
const Map<String, String> kVerifiedQuranAssets = {
  'hafs-an-asim__uthmani': 'assets/quran/hafs-an-asim/uthmani.txt',
  'hafs-an-asim__imlai': 'assets/quran/hafs-an-asim/imlai.txt',
  'hafs-an-asim__indopak': 'assets/quran/hafs-an-asim/indopak.txt',
  'warsh-an-nafi__uthmani': 'assets/quran/warsh-an-nafi/uthmani.txt',
  'qalun-an-nafi__uthmani': 'assets/quran/qalun-an-nafi/uthmani.txt',
};

const String kHafsSource =
    'Tanzil Project v1.1 (Feb 2021), Hafs ‘an ‘Asim — https://tanzil.net';

class VerifiedAssetQuranRepository implements QuranRepository {
  VerifiedAssetQuranRepository({AssetBundle? bundle})
      : _bundle = bundle ?? rootBundle;

  final AssetBundle _bundle;
  final Map<String, List<Ayah>> _cache = {};

  Future<List<Ayah>> _loadEdition(String editionId) async {
    final cached = _cache[editionId];
    if (cached != null) return cached;

    final path = kVerifiedQuranAssets[editionId];
    if (path == null) return const [];

    final raw = await _bundle.loadString(path, cache: false);
    // Juz/Hizb/Rub are verse-based: valid for every Hafs script.
    // Page mapping is Medina-Mushaf layout specific (Uthmani only).
    final isHafs = editionId.startsWith('hafs-an-asim__');
    final meta = isHafs ? await QuranMetadata.load(_bundle) : null;
    final pageMapped = editionId == 'hafs-an-asim__uthmani';
    final ayahs = <Ayah>[];
    for (final line in raw.split('\n')) {
      // Lines look like: 2|255|<exact verse text>
      final first = line.indexOf('|');
      if (first < 0) continue; // comment/header lines (#...)
      final second = line.indexOf('|', first + 1);
      if (second < 0) continue;
      final surah = int.tryParse(line.substring(0, first));
      final ayah = int.tryParse(line.substring(first + 1, second));
      if (surah == null || ayah == null) continue;
      var text = line.substring(second + 1);
      if (text.endsWith('\r')) {
        text = text.substring(0, text.length - 1);
      }
      if (text.isEmpty) continue; // never ship empty verses
      ayahs.add(Ayah(
        surah: surah,
        ayah: ayah,
        text: text,
        editionId: editionId,
        juz: meta?.juzOf(surah, ayah),
        hizb: meta?.hizbOf(surah, ayah),
        rub: meta?.rubOf(surah, ayah),
        page: pageMapped ? meta?.pageOf(surah, ayah) : null,
        isSajda: meta?.isSajda(surah, ayah) ?? false,
      ));
    }
    _cache[editionId] = ayahs;
    return ayahs;
  }

  @override
  Future<List<Ayah>> ayahsOfSurah(int surah, String editionId) async {
    final all = await _loadEdition(editionId);
    if (all.isEmpty) {
      final meta =
          kSurahMetadata.firstWhere((s) => s.number == surah);
      return placeholderAyahs(
          surah: surah, count: meta.ayahCount, editionId: editionId);
    }
    return all.where((a) => a.surah == surah).toList();
  }

  @override
  Future<List<Ayah>> ayahsOfJuz(int juz, String editionId) async {
    final all = await _loadEdition(editionId);
    return all.where((a) => a.juz == juz).toList();
  }

  @override
  Future<List<Ayah>> ayahsOfPage(int page, String editionId) async {
    final all = await _loadEdition(editionId);
    return all.where((a) => a.page == page).toList();
  }

  @override
  Future<List<Ayah>> allAyahs(String editionId) =>
      _loadEdition(editionId);

  @override
  Future<List<Ayah>> compareAyah(
      int surah, int ayah, List<String> editionIds) async {
    final out = <Ayah>[];
    for (final e in editionIds) {
      final all = await _loadEdition(e);
      final match = all.where(
          (a) =>
              a.canonicalSurahNumber == surah &&
              a.canonicalAyahNumber == ayah);
      if (match.isEmpty) {
        out.add(Ayah(
            surah: surah,
            ayah: ayah,
            text: '',
            editionId: e,
            isPlaceholder: true));
      } else {
        out.add(match.first);
      }
    }
    return out;
  }
}
