import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:drift_flutter/drift_flutter.dart';

part 'app_database.g.dart';

@DataClassName('MetaRow')
class AppMeta extends Table {
  TextColumn get key => text()();
  TextColumn get value => text()();

  @override
  Set<Column<Object>> get primaryKey => {key};
}

@DataClassName('BookmarkDbRow')
class Bookmarks extends Table {
  TextColumn get id => text()();
  IntColumn get kind => integer()();
  TextColumn get refKey => text()();
  TextColumn get title => text()();
  TextColumn get subtitle => text()();
  TextColumn get collectionId => text().nullable()();
  DateTimeColumn get createdAt => dateTime()();

  @override
  Set<Column<Object>> get primaryKey => {id};
}

@DataClassName('NoteDbRow')
class Notes extends Table {
  TextColumn get id => text()();
  TextColumn get refKey => text()();
  TextColumn get textValue => text()();
  DateTimeColumn get createdAt => dateTime()();

  @override
  Set<Column<Object>> get primaryKey => {id};
}

@DataClassName('HighlightDbRow')
class Highlights extends Table {
  TextColumn get id => text()();
  TextColumn get refKey => text()();
  IntColumn get colorValue => integer()();

  @override
  Set<Column<Object>> get primaryKey => {id};
}

@DataClassName('CollectionDbRow')
class Collections extends Table {
  TextColumn get id => text()();
  TextColumn get name => text()();

  @override
  Set<Column<Object>> get primaryKey => {id};
}

@DataClassName('RecentDbRow')
class RecentItems extends Table {
  TextColumn get refKey => text()();
  TextColumn get title => text()();
  TextColumn get subtitle => text()();
  IntColumn get kind => integer()();
  DateTimeColumn get touchedAt => dateTime()();

  @override
  Set<Column<Object>> get primaryKey => {refKey};
}

@DataClassName('SearchDocumentDbRow')
class SearchDocuments extends Table {
  TextColumn get id => text()();
  TextColumn get kind => text()();
  TextColumn get refKey => text()();
  TextColumn get title => text()();
  TextColumn get subtitle => text()();
  TextColumn get body => text()();
  TextColumn get normalizedBody => text()();
  TextColumn get normalizedTitle => text()();
  IntColumn get surah => integer().nullable()();
  IntColumn get ayah => integer().nullable()();
  TextColumn get editionId => text().nullable()();
  TextColumn get tafsirId => text().nullable()();
  TextColumn get collectionId => text().nullable()();
  TextColumn get hadithNumber => text().nullable()();
  TextColumn get book => text().nullable()();

  @override
  Set<Column<Object>> get primaryKey => {id};
}

@DriftDatabase(tables: [
  AppMeta,
  Bookmarks,
  Notes,
  Highlights,
  Collections,
  RecentItems,
  SearchDocuments,
])
class AppDatabase extends _$AppDatabase {
  AppDatabase([QueryExecutor? executor])
      : super(
          executor ??
              driftDatabase(
                name: 'quran_sunnah',
                native: DriftNativeOptions(
                  shareAcrossIsolates: true,
                ),
              ),
        );

  AppDatabase.memory() : this(NativeDatabase.memory());

  @override
  int get schemaVersion => 1;

  @override
  MigrationStrategy get migration => MigrationStrategy(
        onCreate: (m) async {
          await m.createAll();
          await _createIndexesAndFts();
        },
        beforeOpen: (_) async {
          await customStatement('PRAGMA foreign_keys = ON');
          await _createIndexesAndFts();
        },
      );

  Future<void> _createIndexesAndFts() async {
    await customStatement(
      'CREATE UNIQUE INDEX IF NOT EXISTS idx_bookmarks_ref '
      'ON bookmarks(ref_key)',
    );
    await customStatement(
      'CREATE UNIQUE INDEX IF NOT EXISTS idx_notes_ref '
      'ON notes(ref_key)',
    );
    await customStatement(
      'CREATE UNIQUE INDEX IF NOT EXISTS idx_highlights_ref '
      'ON highlights(ref_key)',
    );
    await customStatement(
      'CREATE INDEX IF NOT EXISTS idx_search_kind_edition '
      'ON search_documents(kind, edition_id)',
    );
    await customStatement(
      'CREATE INDEX IF NOT EXISTS idx_search_kind_tafsir '
      'ON search_documents(kind, tafsir_id)',
    );
    await customStatement(
      'CREATE INDEX IF NOT EXISTS idx_search_kind_collection '
      'ON search_documents(kind, collection_id)',
    );
    await customStatement(
      'CREATE VIRTUAL TABLE IF NOT EXISTS search_fts USING fts5('
      'normalized_body, normalized_title, '
      "content='search_documents', content_rowid='rowid', "
      "tokenize='unicode61')",
    );
    await customStatement(
      'CREATE TRIGGER IF NOT EXISTS search_documents_ai '
      'AFTER INSERT ON search_documents BEGIN '
      'INSERT INTO search_fts(rowid, normalized_body, normalized_title) '
      'VALUES (new.rowid, new.normalized_body, new.normalized_title); '
      'END',
    );
    await customStatement(
      'CREATE TRIGGER IF NOT EXISTS search_documents_ad '
      'AFTER DELETE ON search_documents BEGIN '
      'INSERT INTO search_fts(search_fts, rowid, normalized_body, normalized_title) '
      "VALUES ('delete', old.rowid, old.normalized_body, old.normalized_title); "
      'END',
    );
    await customStatement(
      'CREATE TRIGGER IF NOT EXISTS search_documents_au '
      'AFTER UPDATE ON search_documents BEGIN '
      'INSERT INTO search_fts(search_fts, rowid, normalized_body, normalized_title) '
      "VALUES ('delete', old.rowid, old.normalized_body, old.normalized_title); "
      'INSERT INTO search_fts(rowid, normalized_body, normalized_title) '
      'VALUES (new.rowid, new.normalized_body, new.normalized_title); '
      'END',
    );
  }

  Future<String?> getMeta(String key) async {
    final rows = await customSelect(
      'SELECT value FROM app_meta WHERE key = ? LIMIT 1',
      variables: [Variable<String>(key)],
    ).get();
    return rows.isEmpty ? null : rows.first.read<String>('value');
  }

  Future<void> setMeta(String key, String value) =>
      customStatement(
        'INSERT INTO app_meta(key, value) VALUES (?, ?) '
        'ON CONFLICT(key) DO UPDATE SET value = excluded.value',
        [key, value],
      );

  Future<void> clearSearchDocuments({
    required String kind,
    String? editionId,
    String? tafsirId,
  }) async {
    final where = <String>['kind = ?'];
    final args = <Object?>[kind];
    if (editionId != null) {
      where.add('edition_id = ?');
      args.add(editionId);
    }
    if (tafsirId != null) {
      where.add('tafsir_id = ?');
      args.add(tafsirId);
    }
    await customStatement(
      'DELETE FROM search_documents WHERE ${where.join(' AND ')}',
      args,
    );
  }

  Future<void> insertSearchDocuments(
      Iterable<SearchIndexDocument> documents) async {
    const sql =
        'INSERT INTO search_documents('
        'id, kind, ref_key, title, subtitle, body, '
        'normalized_body, normalized_title, surah, ayah, '
        'edition_id, tafsir_id, collection_id, hadith_number, book'
        ') VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)';

    await batch((b) {
      for (final d in documents) {
        b.customStatement(sql, [
          d.id,
          d.kind,
          d.refKey,
          d.title,
          d.subtitle,
          d.body,
          d.normalizedBody,
          d.normalizedTitle,
          d.surah,
          d.ayah,
          d.editionId,
          d.tafsirId,
          d.collectionId,
          d.hadithNumber,
          d.book,
        ]);
      }
    });
  }

  Future<List<IndexedSearchRow>> searchIndex({
    required String query,
    required String kind,
    String? editionId,
    String? tafsirId,
    Set<String> collectionIds = const {},
    String? book,
    String? hadithNumber,
    int? surah,
    int limit = 50,
  }) async {
    final where = <String>[
      'search_fts MATCH ?',
      'd.kind = ?',
    ];
    final variables = <Variable<Object>>[
      Variable<String>(query),
      Variable<String>(kind),
    ];
    if (editionId != null) {
      where.add('d.edition_id = ?');
      variables.add(Variable<String>(editionId));
    }
    if (tafsirId != null) {
      where.add('d.tafsir_id = ?');
      variables.add(Variable<String>(tafsirId));
    }
    if (collectionIds.isNotEmpty) {
      where.add(
        'd.collection_id IN (${List.filled(collectionIds.length, '?').join(',')})',
      );
      variables.addAll(collectionIds.map(Variable<String>.new));
    }
    if (book != null && book.isNotEmpty) {
      where.add('d.book = ?');
      variables.add(Variable<String>(book));
    }
    if (hadithNumber != null && hadithNumber.isNotEmpty) {
      where.add('d.hadith_number = ?');
      variables.add(Variable<String>(hadithNumber));
    }
    if (surah != null) {
      where.add('d.surah = ?');
      variables.add(Variable<int>(surah));
    }
    variables.add(Variable<int>(limit));

    final rows = await customSelect(
      'SELECT d.*, bm25(search_fts) AS score '
      'FROM search_fts '
      'JOIN search_documents d ON d.rowid = search_fts.rowid '
      'WHERE ${where.join(' AND ')} '
      'ORDER BY score, d.rowid '
      'LIMIT ?',
      variables: variables,
    ).get();

    return rows
        .map((row) => IndexedSearchRow.fromMap(row.data))
        .toList(growable: false);
  }

  Future<int> countSearchDocuments({String? kind}) async {
    final rows = await customSelect(
      kind == null
          ? 'SELECT COUNT(*) AS c FROM search_documents'
          : 'SELECT COUNT(*) AS c FROM search_documents WHERE kind = ?',
      variables:
          kind == null ? const [] : [Variable<String>(kind)],
    ).get();
    return rows.first.read<int>('c');
  }
}

class SearchIndexDocument {
  const SearchIndexDocument({
    required this.id,
    required this.kind,
    required this.refKey,
    required this.title,
    required this.subtitle,
    required this.body,
    required this.normalizedBody,
    required this.normalizedTitle,
    this.surah,
    this.ayah,
    this.editionId,
    this.tafsirId,
    this.collectionId,
    this.hadithNumber,
    this.book,
  });

  final String id;
  final String kind;
  final String refKey;
  final String title;
  final String subtitle;
  final String body;
  final String normalizedBody;
  final String normalizedTitle;
  final int? surah;
  final int? ayah;
  final String? editionId;
  final String? tafsirId;
  final String? collectionId;
  final String? hadithNumber;
  final String? book;
}

class IndexedSearchRow {
  const IndexedSearchRow({
    required this.id,
    required this.kind,
    required this.refKey,
    required this.title,
    required this.subtitle,
    required this.body,
    this.surah,
    this.ayah,
    this.editionId,
    this.tafsirId,
    this.collectionId,
    this.hadithNumber,
    this.book,
    this.score,
  });

  factory IndexedSearchRow.fromMap(Map<String, Object?> map) =>
      IndexedSearchRow(
        id: map['id'] as String,
        kind: map['kind'] as String,
        refKey: map['ref_key'] as String,
        title: map['title'] as String,
        subtitle: map['subtitle'] as String,
        body: map['body'] as String,
        surah: map['surah'] as int?,
        ayah: map['ayah'] as int?,
        editionId: map['edition_id'] as String?,
        tafsirId: map['tafsir_id'] as String?,
        collectionId: map['collection_id'] as String?,
        hadithNumber: map['hadith_number'] as String?,
        book: map['book'] as String?,
        score: (map['score'] as num?)?.toDouble(),
      );

  final String id;
  final String kind;
  final String refKey;
  final String title;
  final String subtitle;
  final String body;
  final int? surah;
  final int? ayah;
  final String? editionId;
  final String? tafsirId;
  final String? collectionId;
  final String? hadithNumber;
  final String? book;
  final double? score;
}
