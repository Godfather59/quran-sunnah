import 'dart:convert';

import 'package:crypto/crypto.dart';
import 'package:flutter/services.dart';

import '../../core/utils/text_utils.dart';
import '../database/app_database.dart';
import '../content/content_packages.dart';
import '../repositories/hadith_repository.dart';
import '../repositories/quran_repository.dart';
import '../repositories/tafsir_repository.dart';
import '../repositories/verified_asset_quran_repository.dart';

class SearchIndexService {
  SearchIndexService({
    required this.database,
    required this.quran,
    required this.hadith,
    AssetBundle? bundle,
  }) : _bundle = bundle ?? rootBundle;

  final AppDatabase database;
  final QuranRepository quran;
  final HadithRepository hadith;
  final AssetBundle _bundle;

  Map<String, dynamic>? _integrityFiles;

  Future<void> ensureIndexed({
    required String editionId,
    required String tafsirId,
  }) async {
    await _ensureQuran(editionId);
    await _ensureHadith();
    await _ensureTafsir(tafsirId);
  }

  Future<void> _ensureQuran(String editionId) async {
    final path = kVerifiedQuranAssets[editionId];
    if (path == null) return;
    final fingerprint = await _fingerprintExact(path);
    final metaKey = 'search.quran.$editionId';
    if (await database.getMeta(metaKey) == fingerprint) return;

    final ayahs = await quran.allAyahs(editionId);
    final docs = ayahs
        .where((a) => !a.isPlaceholder && a.text.isNotEmpty)
        .map((a) => SearchIndexDocument(
              id: 'quran:$editionId:${a.canonicalVerseId}',
              kind: 'quran',
              refKey: a.canonicalVerseId,
              title: 'Surah ${a.surah} · Ayah ${a.displayAyahNumber}',
              subtitle: 'Quran · $editionId',
              body: a.text,
              normalizedBody: normalizeForIndex(a.text),
              normalizedTitle: normalizeForIndex(
                'Surah ${a.surah} Ayah ${a.displayAyahNumber}',
              ),
              surah: a.canonicalSurahNumber,
              ayah: a.canonicalAyahNumber,
              editionId: editionId,
            ))
        .toList(growable: false);

    await database.transaction(() async {
      await database.clearSearchDocuments(
        kind: 'quran',
        editionId: editionId,
      );
      await database.insertSearchDocuments(docs);
      await database.setMeta(metaKey, fingerprint);
    });
  }

  Future<void> _ensureHadith() async {
    final installed = await ContentPackageStore.instance.installedIds();
    final installedHadith = installed
        .where((id) => id.startsWith('hadith:'))
        .toList()
      ..sort();
    final fingerprint =
        '${await _fingerprintPrefix('assets/hadith/')}|${installedHadith.join(',')}';
    const metaKey = 'search.hadith.all';
    if (await database.getMeta(metaKey) == fingerprint) return;

    final collections = await hadith.collections();
    await database.transaction(() async {
      await database.clearSearchDocuments(kind: 'hadith');
      for (final collection in collections.where((c) => c.isDownloaded)) {
        final rows = await hadith.allForIndex(collection.id);
        await database.insertSearchDocuments(
          rows
              .where((h) => !h.isPlaceholder && h.matnAr.isNotEmpty)
              .map((h) => SearchIndexDocument(
                    id: 'hadith:${h.id}',
                    kind: 'hadith',
                    refKey: h.id,
                    title:
                        '${collection.nameEn} · Hadith ${h.hadithNumber}',
                    subtitle: h.book,
                    body: h.matnAr,
                    normalizedBody: normalizeForIndex(h.matnAr),
                    normalizedTitle: normalizeForIndex(
                      '${collection.nameEn} ${h.book} ${h.hadithNumber}',
                    ),
                    collectionId: h.collectionId,
                    hadithNumber: h.hadithNumber,
                    book: h.book,
                  )),
        );
      }
      await database.setMeta(metaKey, fingerprint);
    });
  }

  Future<void> _ensureTafsir(String tafsirId) async {
    final info =
        kTafsirCatalog.where((t) => t.id == tafsirId).firstOrNull;
    if (info == null || !info.bundled) return;
    final prefix = 'assets/quran/tafsir/$tafsirId/';
    final packageId = 'quran:tafsir-$tafsirId';
    final installed =
        await ContentPackageStore.instance.isInstalled(packageId);
    final fingerprint =
        '${await _fingerprintPrefix(prefix)}|${installed ? 'installed' : 'absent'}';
    final metaKey = 'search.tafsir.$tafsirId';
    if (await database.getMeta(metaKey) == fingerprint) return;

    if (!installed) {
      await database.transaction(() async {
        await database.clearSearchDocuments(
          kind: 'tafsir',
          tafsirId: tafsirId,
        );
        await database.setMeta(metaKey, fingerprint);
      });
      return;
    }

    final docs = <SearchIndexDocument>[];
    for (var surah = 1; surah <= 114; surah++) {
      try {
        final raw = await ContentPackageStore.instance.loadString(
          '$prefix$surah.json',
          bundle: _bundle,
        );
        final json = jsonDecode(raw) as Map<String, dynamic>;
        for (final item in json['entries'] as List) {
          final map = item as Map<String, dynamic>;
          final ayah = (map['ayah'] as num).toInt();
          final text = (map['text'] as String?) ?? '';
          if (text.isEmpty) continue;
          docs.add(SearchIndexDocument(
            id: 'tafsir:$tafsirId:$surah:$ayah',
            kind: 'tafsir',
            refKey: '$tafsirId:$surah:$ayah',
            title: '${info.titleEn} · $surah:$ayah',
            subtitle: info.source,
            body: text,
            normalizedBody: normalizeForIndex(text),
            normalizedTitle:
                normalizeForIndex('${info.titleEn} $surah $ayah'),
            surah: surah,
            ayah: ayah,
            tafsirId: tafsirId,
          ));
        }
      } catch (_) {
        // Missing source files stay absent; never synthesize tafsir.
      }
    }

    await database.transaction(() async {
      await database.clearSearchDocuments(
        kind: 'tafsir',
        tafsirId: tafsirId,
      );
      await database.insertSearchDocuments(docs);
      await database.setMeta(metaKey, fingerprint);
    });
  }

  Future<Map<String, dynamic>> _manifestFiles() async {
    final cached = _integrityFiles;
    if (cached != null) return cached;
    final raw =
        await _bundle.loadString('assets/integrity_manifest.json');
    final decoded = jsonDecode(raw) as Map<String, dynamic>;
    final files = Map<String, dynamic>.from(
      decoded['files'] as Map<dynamic, dynamic>,
    );
    _integrityFiles = files;
    return files;
  }

  Future<String> _fingerprintExact(String path) async {
    final files = await _manifestFiles();
    return files[path]?.toString() ?? 'missing:$path';
  }

  Future<String> _fingerprintPrefix(String prefix) async {
    final files = await _manifestFiles();
    final entries = files.entries
        .where((e) => e.key.startsWith(prefix))
        .map((e) => '${e.key}=${e.value}')
        .toList()
      ..sort();
    return sha256.convert(utf8.encode(entries.join('\n'))).toString();
  }

  static String normalizeForIndex(String value) =>
      normalizeArabic(value).toLowerCase();

  static String ftsQuery(String value) {
    final normalized = normalizeForIndex(value);
    final safe = normalized.replaceAll(
      RegExp(r'[^\u0600-\u06FF\u0750-\u077F\u08A0-\u08FFa-z0-9]+'),
      ' ',
    );
    final tokens = safe
        .split(RegExp(r'\s+'))
        .where((t) => t.isNotEmpty)
        .toList(growable: false);
    if (tokens.isEmpty) return '';
    return tokens.map((t) => '$t*').join(' AND ');
  }
}
