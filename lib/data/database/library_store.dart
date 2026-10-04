import 'dart:convert';

import 'package:drift/drift.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../models/library.dart';
import 'app_database.dart';

const _legacyBookmarksKey = 'library.bookmarks.v1';
const _legacyNotesKey = 'library.notes.v1';
const _legacyCollectionsKey = 'library.collections.v1';
const _legacyHighlightsKey = 'library.highlights.v1';
const _legacyRecentKey = 'library.recent.v1';
const _migrationMetaKey = 'legacy_library_migrated_v1';

const defaultCollections = [
  CustomCollection(id: 'fav-ayat', name: 'Favorite Ayat'),
  CustomCollection(id: 'prayer', name: 'Prayer Hadith'),
  CustomCollection(id: 'ramadan', name: 'Ramadan'),
];

class LibraryStore {
  LibraryStore(this.db);

  final AppDatabase db;

  Future<void> initialize() async {
    await _migrateLegacySharedPreferences();
    await _ensureDefaultCollections();
  }

  Future<List<Bookmark>> bookmarks() async {
    final rows = await db.customSelect(
      'SELECT id, kind, ref_key, title, subtitle, collection_id, created_at '
      'FROM bookmarks ORDER BY created_at DESC, rowid DESC',
    ).get();
    return rows.map((r) => Bookmark(
      id: r.read<String>('id'),
      kind: BookmarkKind.values[r.read<int>('kind')],
      refKey: r.read<String>('ref_key'),
      title: r.read<String>('title'),
      subtitle: r.read<String>('subtitle'),
      collectionId: r.readNullable<String>('collection_id'),
      createdAt: _date(r.read<int>('created_at')),
    )).toList(growable: false);
  }

  Future<void> toggleAyah(int surah, int ayah) async {
    final key = '$surah:$ayah';
    if (await _exists('bookmarks', 'ref_key', key)) {
      await db.customStatement('DELETE FROM bookmarks WHERE ref_key = ?', [key]);
      return;
    }
    final now = DateTime.now();
    await db.customStatement(
      'INSERT INTO bookmarks(id, kind, ref_key, title, subtitle, collection_id, created_at) '
      'VALUES (?, ?, ?, ?, ?, ?, ?)',
      [
        'bm-$key-${now.microsecondsSinceEpoch}',
        BookmarkKind.ayah.index,
        key,
        'Surah $surah · Ayah $ayah',
        'Quran bookmark',
        null,
        _seconds(now),
      ],
    );
  }

  Future<void> setAyahCollection(int surah, int ayah, String? collectionId) async {
    final key = '$surah:$ayah';
    if (!await _exists('bookmarks', 'ref_key', key)) {
      final now = DateTime.now();
      await db.customStatement(
        'INSERT INTO bookmarks(id, kind, ref_key, title, subtitle, collection_id, created_at) '
        'VALUES (?, ?, ?, ?, ?, ?, ?)',
        [
          'bm-$key-${now.microsecondsSinceEpoch}',
          BookmarkKind.ayah.index,
          key,
          'Surah $surah · Ayah $ayah',
          'Quran bookmark',
          collectionId,
          _seconds(now),
        ],
      );
      return;
    }
    await db.customStatement(
      'UPDATE bookmarks SET collection_id = ? WHERE ref_key = ?',
      [collectionId, key],
    );
  }

  Future<void> toggleHadith(String id, String title) async {
    if (await _exists('bookmarks', 'ref_key', id)) {
      await db.customStatement('DELETE FROM bookmarks WHERE ref_key = ?', [id]);
      return;
    }
    final now = DateTime.now();
    await db.customStatement(
      'INSERT INTO bookmarks(id, kind, ref_key, title, subtitle, collection_id, created_at) '
      'VALUES (?, ?, ?, ?, ?, ?, ?)',
      [
        'bm-$id-${now.microsecondsSinceEpoch}',
        BookmarkKind.hadith.index,
        id,
        title,
        'Hadith bookmark',
        null,
        _seconds(now),
      ],
    );
  }

  Future<void> removeBookmark(String id) =>
      db.customStatement('DELETE FROM bookmarks WHERE id = ?', [id]);

  Future<List<UserNote>> notes() async {
    final rows = await db.customSelect(
      'SELECT id, ref_key, text_value, created_at FROM notes '
      'ORDER BY created_at DESC, rowid DESC',
    ).get();
    return rows.map((r) => UserNote(
      id: r.read<String>('id'),
      refKey: r.read<String>('ref_key'),
      text: r.read<String>('text_value'),
      createdAt: _date(r.read<int>('created_at')),
    )).toList(growable: false);
  }

  Future<void> upsertNote(String refKey, String text) async {
    final trimmed = text.trim();
    if (trimmed.isEmpty) {
      await db.customStatement('DELETE FROM notes WHERE ref_key = ?', [refKey]);
      return;
    }
    final existing = await db.customSelect(
      'SELECT id FROM notes WHERE ref_key = ? LIMIT 1',
      variables: [Variable<String>(refKey)],
    ).get();
    if (existing.isEmpty) {
      final now = DateTime.now();
      await db.customStatement(
        'INSERT INTO notes(id, ref_key, text_value, created_at) VALUES (?, ?, ?, ?)',
        [
          'note-$refKey-${now.microsecondsSinceEpoch}',
          refKey,
          trimmed,
          _seconds(now),
        ],
      );
    } else {
      await db.customStatement(
        'UPDATE notes SET text_value = ? WHERE ref_key = ?',
        [trimmed, refKey],
      );
    }
  }

  Future<void> removeNote(String id) =>
      db.customStatement('DELETE FROM notes WHERE id = ?', [id]);

  Future<List<Highlight>> highlights() async {
    final rows = await db.customSelect(
      'SELECT id, ref_key, color_value FROM highlights ORDER BY rowid DESC',
    ).get();
    return rows.map((r) => Highlight(
      id: r.read<String>('id'),
      refKey: r.read<String>('ref_key'),
      colorValue: r.read<int>('color_value'),
    )).toList(growable: false);
  }

  Future<void> toggleHighlight(String refKey, int colorValue) async {
    final rows = await db.customSelect(
      'SELECT color_value FROM highlights WHERE ref_key = ? LIMIT 1',
      variables: [Variable<String>(refKey)],
    ).get();
    if (rows.isNotEmpty && rows.first.read<int>('color_value') == colorValue) {
      await db.customStatement('DELETE FROM highlights WHERE ref_key = ?', [refKey]);
      return;
    }
    await db.customStatement(
      'INSERT INTO highlights(id, ref_key, color_value) VALUES (?, ?, ?) '
      'ON CONFLICT(ref_key) DO UPDATE SET color_value = excluded.color_value',
      ['hl-$refKey', refKey, colorValue],
    );
  }

  Future<void> removeHighlight(String id) =>
      db.customStatement('DELETE FROM highlights WHERE id = ?', [id]);

  Future<List<CustomCollection>> collections() async {
    final rows = await db.customSelect(
      'SELECT id, name FROM collections ORDER BY rowid',
    ).get();
    return rows.map((r) => CustomCollection(
      id: r.read<String>('id'),
      name: r.read<String>('name'),
    )).toList(growable: false);
  }

  Future<void> addCollection(String name) async {
    final trimmed = name.trim();
    if (trimmed.isEmpty) return;
    await db.customStatement(
      'INSERT INTO collections(id, name) VALUES (?, ?)',
      ['c-${DateTime.now().microsecondsSinceEpoch}', trimmed],
    );
  }

  Future<void> renameCollection(String id, String name) async {
    final trimmed = name.trim();
    if (trimmed.isEmpty) return;
    await db.customStatement(
      'UPDATE collections SET name = ? WHERE id = ?',
      [trimmed, id],
    );
  }

  Future<void> removeCollection(String id) async {
    await db.transaction(() async {
      await db.customStatement(
        'UPDATE bookmarks SET collection_id = NULL WHERE collection_id = ?',
        [id],
      );
      await db.customStatement('DELETE FROM collections WHERE id = ?', [id]);
    });
  }

  Future<List<RecentItem>> recent() async {
    final rows = await db.customSelect(
      'SELECT ref_key, title, subtitle, kind FROM recent_items '
      'ORDER BY touched_at DESC LIMIT 50',
    ).get();
    return rows.map((r) => RecentItem(
      refKey: r.read<String>('ref_key'),
      title: r.read<String>('title'),
      subtitle: r.read<String>('subtitle'),
      kind: BookmarkKind.values[r.read<int>('kind')],
    )).toList(growable: false);
  }

  Future<void> touchRecent(RecentItem item) async {
    await db.customStatement(
      'INSERT INTO recent_items(ref_key, title, subtitle, kind, touched_at) '
      'VALUES (?, ?, ?, ?, ?) '
      'ON CONFLICT(ref_key) DO UPDATE SET '
      'title = excluded.title, subtitle = excluded.subtitle, '
      'kind = excluded.kind, touched_at = excluded.touched_at',
      [
        item.refKey,
        item.title,
        item.subtitle,
        item.kind.index,
        _seconds(DateTime.now()),
      ],
    );
    await db.customStatement(
      'DELETE FROM recent_items WHERE ref_key NOT IN ('
      'SELECT ref_key FROM recent_items ORDER BY touched_at DESC LIMIT 50)',
    );
  }

  Future<void> _ensureDefaultCollections() async {
    final rows = await db.customSelect('SELECT COUNT(*) AS c FROM collections').get();
    if (rows.first.read<int>('c') != 0) return;
    await db.transaction(() async {
      for (final collection in defaultCollections) {
        await db.customStatement(
          'INSERT OR IGNORE INTO collections(id, name) VALUES (?, ?)',
          [collection.id, collection.name],
        );
      }
    });
  }

  Future<void> _migrateLegacySharedPreferences() async {
    if (await db.getMeta(_migrationMetaKey) == '1') return;
    final prefs = await SharedPreferences.getInstance();

    await db.transaction(() async {
      for (final item in _decodeList(prefs.getString(_legacyCollectionsKey))) {
        if (item is! Map) continue;
        final m = Map<String, dynamic>.from(item);
        final id = m['id'] as String? ?? '';
        final name = m['name'] as String? ?? '';
        if (id.isEmpty || name.isEmpty) continue;
        await db.customStatement(
          'INSERT OR IGNORE INTO collections(id, name) VALUES (?, ?)',
          [id, name],
        );
      }

      for (final item in _decodeList(prefs.getString(_legacyBookmarksKey))) {
        if (item is! Map) continue;
        final m = Map<String, dynamic>.from(item);
        final id = m['id'] as String? ?? '';
        final refKey = m['refKey'] as String? ?? '';
        final kind = m['kind'] as int? ?? -1;
        if (id.isEmpty || refKey.isEmpty || kind < 0 || kind >= BookmarkKind.values.length) continue;
        await db.customStatement(
          'INSERT OR REPLACE INTO bookmarks('
          'id, kind, ref_key, title, subtitle, collection_id, created_at'
          ') VALUES (?, ?, ?, ?, ?, ?, ?)',
          [
            id,
            kind,
            refKey,
            m['title'] as String? ?? '',
            m['subtitle'] as String? ?? '',
            m['collectionId'] as String?,
            _seconds(DateTime.tryParse(m['createdAt'] as String? ?? '') ?? DateTime.now()),
          ],
        );
      }

      for (final item in _decodeList(prefs.getString(_legacyNotesKey))) {
        if (item is! Map) continue;
        final m = Map<String, dynamic>.from(item);
        final id = m['id'] as String? ?? '';
        final refKey = m['refKey'] as String? ?? '';
        final text = m['text'] as String? ?? '';
        if (id.isEmpty || refKey.isEmpty || text.isEmpty) continue;
        await db.customStatement(
          'INSERT OR REPLACE INTO notes(id, ref_key, text_value, created_at) '
          'VALUES (?, ?, ?, ?)',
          [
            id,
            refKey,
            text,
            _seconds(DateTime.tryParse(m['createdAt'] as String? ?? '') ?? DateTime.now()),
          ],
        );
      }

      for (final item in _decodeList(prefs.getString(_legacyHighlightsKey))) {
        if (item is! Map) continue;
        final m = Map<String, dynamic>.from(item);
        final id = m['id'] as String? ?? '';
        final refKey = m['refKey'] as String? ?? '';
        final color = m['colorValue'] as int?;
        if (id.isEmpty || refKey.isEmpty || color == null) continue;
        await db.customStatement(
          'INSERT OR REPLACE INTO highlights(id, ref_key, color_value) VALUES (?, ?, ?)',
          [id, refKey, color],
        );
      }

      var offset = 0;
      for (final item in _decodeList(prefs.getString(_legacyRecentKey))) {
        if (item is! Map) continue;
        final m = Map<String, dynamic>.from(item);
        final refKey = m['refKey'] as String? ?? '';
        final kind = m['kind'] as int? ?? -1;
        if (refKey.isEmpty || kind < 0 || kind >= BookmarkKind.values.length) continue;
        await db.customStatement(
          'INSERT OR REPLACE INTO recent_items('
          'ref_key, title, subtitle, kind, touched_at) VALUES (?, ?, ?, ?, ?)',
          [
            refKey,
            m['title'] as String? ?? '',
            m['subtitle'] as String? ?? '',
            kind,
            _seconds(DateTime.now().subtract(Duration(seconds: offset++))),
          ],
        );
      }

      await db.setMeta(_migrationMetaKey, '1');
    });

    for (final key in [
      _legacyBookmarksKey,
      _legacyNotesKey,
      _legacyCollectionsKey,
      _legacyHighlightsKey,
      _legacyRecentKey,
    ]) {
      await prefs.remove(key);
    }
  }

  Future<bool> _exists(String table, String column, String value) async {
    final rows = await db.customSelect(
      'SELECT 1 AS found FROM $table WHERE $column = ? LIMIT 1',
      variables: [Variable<String>(value)],
    ).get();
    return rows.isNotEmpty;
  }

  static int _seconds(DateTime date) => date.millisecondsSinceEpoch ~/ 1000;
  static DateTime _date(int seconds) =>
      DateTime.fromMillisecondsSinceEpoch(seconds * 1000);

  static List<dynamic> _decodeList(String? raw) {
    if (raw == null || raw.isEmpty) return const [];
    try {
      final value = jsonDecode(raw);
      return value is List ? value : const [];
    } catch (_) {
      return const [];
    }
  }
}
