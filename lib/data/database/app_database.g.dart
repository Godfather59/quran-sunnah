// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'app_database.dart';

// ignore_for_file: type=lint
class $AppMetaTable extends AppMeta with TableInfo<$AppMetaTable, MetaRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $AppMetaTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _keyMeta = const VerificationMeta('key');
  @override
  late final GeneratedColumn<String> key = GeneratedColumn<String>(
    'key',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _valueMeta = const VerificationMeta('value');
  @override
  late final GeneratedColumn<String> value = GeneratedColumn<String>(
    'value',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [key, value];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'app_meta';
  @override
  VerificationContext validateIntegrity(
    Insertable<MetaRow> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('key')) {
      context.handle(
        _keyMeta,
        key.isAcceptableOrUnknown(data['key']!, _keyMeta),
      );
    } else if (isInserting) {
      context.missing(_keyMeta);
    }
    if (data.containsKey('value')) {
      context.handle(
        _valueMeta,
        value.isAcceptableOrUnknown(data['value']!, _valueMeta),
      );
    } else if (isInserting) {
      context.missing(_valueMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {key};
  @override
  MetaRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return MetaRow(
      key: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}key'],
      )!,
      value: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}value'],
      )!,
    );
  }

  @override
  $AppMetaTable createAlias(String alias) {
    return $AppMetaTable(attachedDatabase, alias);
  }
}

class MetaRow extends DataClass implements Insertable<MetaRow> {
  final String key;
  final String value;
  const MetaRow({required this.key, required this.value});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['key'] = Variable<String>(key);
    map['value'] = Variable<String>(value);
    return map;
  }

  AppMetaCompanion toCompanion(bool nullToAbsent) {
    return AppMetaCompanion(key: Value(key), value: Value(value));
  }

  factory MetaRow.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return MetaRow(
      key: serializer.fromJson<String>(json['key']),
      value: serializer.fromJson<String>(json['value']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'key': serializer.toJson<String>(key),
      'value': serializer.toJson<String>(value),
    };
  }

  MetaRow copyWith({String? key, String? value}) =>
      MetaRow(key: key ?? this.key, value: value ?? this.value);
  MetaRow copyWithCompanion(AppMetaCompanion data) {
    return MetaRow(
      key: data.key.present ? data.key.value : this.key,
      value: data.value.present ? data.value.value : this.value,
    );
  }

  @override
  String toString() {
    return (StringBuffer('MetaRow(')
          ..write('key: $key, ')
          ..write('value: $value')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(key, value);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is MetaRow && other.key == this.key && other.value == this.value);
}

class AppMetaCompanion extends UpdateCompanion<MetaRow> {
  final Value<String> key;
  final Value<String> value;
  final Value<int> rowid;
  const AppMetaCompanion({
    this.key = const Value.absent(),
    this.value = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  AppMetaCompanion.insert({
    required String key,
    required String value,
    this.rowid = const Value.absent(),
  }) : key = Value(key),
       value = Value(value);
  static Insertable<MetaRow> custom({
    Expression<String>? key,
    Expression<String>? value,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (key != null) 'key': key,
      if (value != null) 'value': value,
      if (rowid != null) 'rowid': rowid,
    });
  }

  AppMetaCompanion copyWith({
    Value<String>? key,
    Value<String>? value,
    Value<int>? rowid,
  }) {
    return AppMetaCompanion(
      key: key ?? this.key,
      value: value ?? this.value,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (key.present) {
      map['key'] = Variable<String>(key.value);
    }
    if (value.present) {
      map['value'] = Variable<String>(value.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('AppMetaCompanion(')
          ..write('key: $key, ')
          ..write('value: $value, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $BookmarksTable extends Bookmarks
    with TableInfo<$BookmarksTable, BookmarkDbRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $BookmarksTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _kindMeta = const VerificationMeta('kind');
  @override
  late final GeneratedColumn<int> kind = GeneratedColumn<int>(
    'kind',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _refKeyMeta = const VerificationMeta('refKey');
  @override
  late final GeneratedColumn<String> refKey = GeneratedColumn<String>(
    'ref_key',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _titleMeta = const VerificationMeta('title');
  @override
  late final GeneratedColumn<String> title = GeneratedColumn<String>(
    'title',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _subtitleMeta = const VerificationMeta(
    'subtitle',
  );
  @override
  late final GeneratedColumn<String> subtitle = GeneratedColumn<String>(
    'subtitle',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _collectionIdMeta = const VerificationMeta(
    'collectionId',
  );
  @override
  late final GeneratedColumn<String> collectionId = GeneratedColumn<String>(
    'collection_id',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _createdAtMeta = const VerificationMeta(
    'createdAt',
  );
  @override
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
    'created_at',
    aliasedName,
    false,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    kind,
    refKey,
    title,
    subtitle,
    collectionId,
    createdAt,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'bookmarks';
  @override
  VerificationContext validateIntegrity(
    Insertable<BookmarkDbRow> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('kind')) {
      context.handle(
        _kindMeta,
        kind.isAcceptableOrUnknown(data['kind']!, _kindMeta),
      );
    } else if (isInserting) {
      context.missing(_kindMeta);
    }
    if (data.containsKey('ref_key')) {
      context.handle(
        _refKeyMeta,
        refKey.isAcceptableOrUnknown(data['ref_key']!, _refKeyMeta),
      );
    } else if (isInserting) {
      context.missing(_refKeyMeta);
    }
    if (data.containsKey('title')) {
      context.handle(
        _titleMeta,
        title.isAcceptableOrUnknown(data['title']!, _titleMeta),
      );
    } else if (isInserting) {
      context.missing(_titleMeta);
    }
    if (data.containsKey('subtitle')) {
      context.handle(
        _subtitleMeta,
        subtitle.isAcceptableOrUnknown(data['subtitle']!, _subtitleMeta),
      );
    } else if (isInserting) {
      context.missing(_subtitleMeta);
    }
    if (data.containsKey('collection_id')) {
      context.handle(
        _collectionIdMeta,
        collectionId.isAcceptableOrUnknown(
          data['collection_id']!,
          _collectionIdMeta,
        ),
      );
    }
    if (data.containsKey('created_at')) {
      context.handle(
        _createdAtMeta,
        createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta),
      );
    } else if (isInserting) {
      context.missing(_createdAtMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  BookmarkDbRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return BookmarkDbRow(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      kind: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}kind'],
      )!,
      refKey: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}ref_key'],
      )!,
      title: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}title'],
      )!,
      subtitle: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}subtitle'],
      )!,
      collectionId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}collection_id'],
      ),
      createdAt: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}created_at'],
      )!,
    );
  }

  @override
  $BookmarksTable createAlias(String alias) {
    return $BookmarksTable(attachedDatabase, alias);
  }
}

class BookmarkDbRow extends DataClass implements Insertable<BookmarkDbRow> {
  final String id;
  final int kind;
  final String refKey;
  final String title;
  final String subtitle;
  final String? collectionId;
  final DateTime createdAt;
  const BookmarkDbRow({
    required this.id,
    required this.kind,
    required this.refKey,
    required this.title,
    required this.subtitle,
    this.collectionId,
    required this.createdAt,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['kind'] = Variable<int>(kind);
    map['ref_key'] = Variable<String>(refKey);
    map['title'] = Variable<String>(title);
    map['subtitle'] = Variable<String>(subtitle);
    if (!nullToAbsent || collectionId != null) {
      map['collection_id'] = Variable<String>(collectionId);
    }
    map['created_at'] = Variable<DateTime>(createdAt);
    return map;
  }

  BookmarksCompanion toCompanion(bool nullToAbsent) {
    return BookmarksCompanion(
      id: Value(id),
      kind: Value(kind),
      refKey: Value(refKey),
      title: Value(title),
      subtitle: Value(subtitle),
      collectionId: collectionId == null && nullToAbsent
          ? const Value.absent()
          : Value(collectionId),
      createdAt: Value(createdAt),
    );
  }

  factory BookmarkDbRow.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return BookmarkDbRow(
      id: serializer.fromJson<String>(json['id']),
      kind: serializer.fromJson<int>(json['kind']),
      refKey: serializer.fromJson<String>(json['refKey']),
      title: serializer.fromJson<String>(json['title']),
      subtitle: serializer.fromJson<String>(json['subtitle']),
      collectionId: serializer.fromJson<String?>(json['collectionId']),
      createdAt: serializer.fromJson<DateTime>(json['createdAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'kind': serializer.toJson<int>(kind),
      'refKey': serializer.toJson<String>(refKey),
      'title': serializer.toJson<String>(title),
      'subtitle': serializer.toJson<String>(subtitle),
      'collectionId': serializer.toJson<String?>(collectionId),
      'createdAt': serializer.toJson<DateTime>(createdAt),
    };
  }

  BookmarkDbRow copyWith({
    String? id,
    int? kind,
    String? refKey,
    String? title,
    String? subtitle,
    Value<String?> collectionId = const Value.absent(),
    DateTime? createdAt,
  }) => BookmarkDbRow(
    id: id ?? this.id,
    kind: kind ?? this.kind,
    refKey: refKey ?? this.refKey,
    title: title ?? this.title,
    subtitle: subtitle ?? this.subtitle,
    collectionId: collectionId.present ? collectionId.value : this.collectionId,
    createdAt: createdAt ?? this.createdAt,
  );
  BookmarkDbRow copyWithCompanion(BookmarksCompanion data) {
    return BookmarkDbRow(
      id: data.id.present ? data.id.value : this.id,
      kind: data.kind.present ? data.kind.value : this.kind,
      refKey: data.refKey.present ? data.refKey.value : this.refKey,
      title: data.title.present ? data.title.value : this.title,
      subtitle: data.subtitle.present ? data.subtitle.value : this.subtitle,
      collectionId: data.collectionId.present
          ? data.collectionId.value
          : this.collectionId,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('BookmarkDbRow(')
          ..write('id: $id, ')
          ..write('kind: $kind, ')
          ..write('refKey: $refKey, ')
          ..write('title: $title, ')
          ..write('subtitle: $subtitle, ')
          ..write('collectionId: $collectionId, ')
          ..write('createdAt: $createdAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode =>
      Object.hash(id, kind, refKey, title, subtitle, collectionId, createdAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is BookmarkDbRow &&
          other.id == this.id &&
          other.kind == this.kind &&
          other.refKey == this.refKey &&
          other.title == this.title &&
          other.subtitle == this.subtitle &&
          other.collectionId == this.collectionId &&
          other.createdAt == this.createdAt);
}

class BookmarksCompanion extends UpdateCompanion<BookmarkDbRow> {
  final Value<String> id;
  final Value<int> kind;
  final Value<String> refKey;
  final Value<String> title;
  final Value<String> subtitle;
  final Value<String?> collectionId;
  final Value<DateTime> createdAt;
  final Value<int> rowid;
  const BookmarksCompanion({
    this.id = const Value.absent(),
    this.kind = const Value.absent(),
    this.refKey = const Value.absent(),
    this.title = const Value.absent(),
    this.subtitle = const Value.absent(),
    this.collectionId = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  BookmarksCompanion.insert({
    required String id,
    required int kind,
    required String refKey,
    required String title,
    required String subtitle,
    this.collectionId = const Value.absent(),
    required DateTime createdAt,
    this.rowid = const Value.absent(),
  }) : id = Value(id),
       kind = Value(kind),
       refKey = Value(refKey),
       title = Value(title),
       subtitle = Value(subtitle),
       createdAt = Value(createdAt);
  static Insertable<BookmarkDbRow> custom({
    Expression<String>? id,
    Expression<int>? kind,
    Expression<String>? refKey,
    Expression<String>? title,
    Expression<String>? subtitle,
    Expression<String>? collectionId,
    Expression<DateTime>? createdAt,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (kind != null) 'kind': kind,
      if (refKey != null) 'ref_key': refKey,
      if (title != null) 'title': title,
      if (subtitle != null) 'subtitle': subtitle,
      if (collectionId != null) 'collection_id': collectionId,
      if (createdAt != null) 'created_at': createdAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  BookmarksCompanion copyWith({
    Value<String>? id,
    Value<int>? kind,
    Value<String>? refKey,
    Value<String>? title,
    Value<String>? subtitle,
    Value<String?>? collectionId,
    Value<DateTime>? createdAt,
    Value<int>? rowid,
  }) {
    return BookmarksCompanion(
      id: id ?? this.id,
      kind: kind ?? this.kind,
      refKey: refKey ?? this.refKey,
      title: title ?? this.title,
      subtitle: subtitle ?? this.subtitle,
      collectionId: collectionId ?? this.collectionId,
      createdAt: createdAt ?? this.createdAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (kind.present) {
      map['kind'] = Variable<int>(kind.value);
    }
    if (refKey.present) {
      map['ref_key'] = Variable<String>(refKey.value);
    }
    if (title.present) {
      map['title'] = Variable<String>(title.value);
    }
    if (subtitle.present) {
      map['subtitle'] = Variable<String>(subtitle.value);
    }
    if (collectionId.present) {
      map['collection_id'] = Variable<String>(collectionId.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<DateTime>(createdAt.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('BookmarksCompanion(')
          ..write('id: $id, ')
          ..write('kind: $kind, ')
          ..write('refKey: $refKey, ')
          ..write('title: $title, ')
          ..write('subtitle: $subtitle, ')
          ..write('collectionId: $collectionId, ')
          ..write('createdAt: $createdAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $NotesTable extends Notes with TableInfo<$NotesTable, NoteDbRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $NotesTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _refKeyMeta = const VerificationMeta('refKey');
  @override
  late final GeneratedColumn<String> refKey = GeneratedColumn<String>(
    'ref_key',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _textValueMeta = const VerificationMeta(
    'textValue',
  );
  @override
  late final GeneratedColumn<String> textValue = GeneratedColumn<String>(
    'text_value',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _createdAtMeta = const VerificationMeta(
    'createdAt',
  );
  @override
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
    'created_at',
    aliasedName,
    false,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [id, refKey, textValue, createdAt];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'notes';
  @override
  VerificationContext validateIntegrity(
    Insertable<NoteDbRow> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('ref_key')) {
      context.handle(
        _refKeyMeta,
        refKey.isAcceptableOrUnknown(data['ref_key']!, _refKeyMeta),
      );
    } else if (isInserting) {
      context.missing(_refKeyMeta);
    }
    if (data.containsKey('text_value')) {
      context.handle(
        _textValueMeta,
        textValue.isAcceptableOrUnknown(data['text_value']!, _textValueMeta),
      );
    } else if (isInserting) {
      context.missing(_textValueMeta);
    }
    if (data.containsKey('created_at')) {
      context.handle(
        _createdAtMeta,
        createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta),
      );
    } else if (isInserting) {
      context.missing(_createdAtMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  NoteDbRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return NoteDbRow(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      refKey: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}ref_key'],
      )!,
      textValue: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}text_value'],
      )!,
      createdAt: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}created_at'],
      )!,
    );
  }

  @override
  $NotesTable createAlias(String alias) {
    return $NotesTable(attachedDatabase, alias);
  }
}

class NoteDbRow extends DataClass implements Insertable<NoteDbRow> {
  final String id;
  final String refKey;
  final String textValue;
  final DateTime createdAt;
  const NoteDbRow({
    required this.id,
    required this.refKey,
    required this.textValue,
    required this.createdAt,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['ref_key'] = Variable<String>(refKey);
    map['text_value'] = Variable<String>(textValue);
    map['created_at'] = Variable<DateTime>(createdAt);
    return map;
  }

  NotesCompanion toCompanion(bool nullToAbsent) {
    return NotesCompanion(
      id: Value(id),
      refKey: Value(refKey),
      textValue: Value(textValue),
      createdAt: Value(createdAt),
    );
  }

  factory NoteDbRow.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return NoteDbRow(
      id: serializer.fromJson<String>(json['id']),
      refKey: serializer.fromJson<String>(json['refKey']),
      textValue: serializer.fromJson<String>(json['textValue']),
      createdAt: serializer.fromJson<DateTime>(json['createdAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'refKey': serializer.toJson<String>(refKey),
      'textValue': serializer.toJson<String>(textValue),
      'createdAt': serializer.toJson<DateTime>(createdAt),
    };
  }

  NoteDbRow copyWith({
    String? id,
    String? refKey,
    String? textValue,
    DateTime? createdAt,
  }) => NoteDbRow(
    id: id ?? this.id,
    refKey: refKey ?? this.refKey,
    textValue: textValue ?? this.textValue,
    createdAt: createdAt ?? this.createdAt,
  );
  NoteDbRow copyWithCompanion(NotesCompanion data) {
    return NoteDbRow(
      id: data.id.present ? data.id.value : this.id,
      refKey: data.refKey.present ? data.refKey.value : this.refKey,
      textValue: data.textValue.present ? data.textValue.value : this.textValue,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('NoteDbRow(')
          ..write('id: $id, ')
          ..write('refKey: $refKey, ')
          ..write('textValue: $textValue, ')
          ..write('createdAt: $createdAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, refKey, textValue, createdAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is NoteDbRow &&
          other.id == this.id &&
          other.refKey == this.refKey &&
          other.textValue == this.textValue &&
          other.createdAt == this.createdAt);
}

class NotesCompanion extends UpdateCompanion<NoteDbRow> {
  final Value<String> id;
  final Value<String> refKey;
  final Value<String> textValue;
  final Value<DateTime> createdAt;
  final Value<int> rowid;
  const NotesCompanion({
    this.id = const Value.absent(),
    this.refKey = const Value.absent(),
    this.textValue = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  NotesCompanion.insert({
    required String id,
    required String refKey,
    required String textValue,
    required DateTime createdAt,
    this.rowid = const Value.absent(),
  }) : id = Value(id),
       refKey = Value(refKey),
       textValue = Value(textValue),
       createdAt = Value(createdAt);
  static Insertable<NoteDbRow> custom({
    Expression<String>? id,
    Expression<String>? refKey,
    Expression<String>? textValue,
    Expression<DateTime>? createdAt,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (refKey != null) 'ref_key': refKey,
      if (textValue != null) 'text_value': textValue,
      if (createdAt != null) 'created_at': createdAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  NotesCompanion copyWith({
    Value<String>? id,
    Value<String>? refKey,
    Value<String>? textValue,
    Value<DateTime>? createdAt,
    Value<int>? rowid,
  }) {
    return NotesCompanion(
      id: id ?? this.id,
      refKey: refKey ?? this.refKey,
      textValue: textValue ?? this.textValue,
      createdAt: createdAt ?? this.createdAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (refKey.present) {
      map['ref_key'] = Variable<String>(refKey.value);
    }
    if (textValue.present) {
      map['text_value'] = Variable<String>(textValue.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<DateTime>(createdAt.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('NotesCompanion(')
          ..write('id: $id, ')
          ..write('refKey: $refKey, ')
          ..write('textValue: $textValue, ')
          ..write('createdAt: $createdAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $HighlightsTable extends Highlights
    with TableInfo<$HighlightsTable, HighlightDbRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $HighlightsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _refKeyMeta = const VerificationMeta('refKey');
  @override
  late final GeneratedColumn<String> refKey = GeneratedColumn<String>(
    'ref_key',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _colorValueMeta = const VerificationMeta(
    'colorValue',
  );
  @override
  late final GeneratedColumn<int> colorValue = GeneratedColumn<int>(
    'color_value',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [id, refKey, colorValue];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'highlights';
  @override
  VerificationContext validateIntegrity(
    Insertable<HighlightDbRow> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('ref_key')) {
      context.handle(
        _refKeyMeta,
        refKey.isAcceptableOrUnknown(data['ref_key']!, _refKeyMeta),
      );
    } else if (isInserting) {
      context.missing(_refKeyMeta);
    }
    if (data.containsKey('color_value')) {
      context.handle(
        _colorValueMeta,
        colorValue.isAcceptableOrUnknown(data['color_value']!, _colorValueMeta),
      );
    } else if (isInserting) {
      context.missing(_colorValueMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  HighlightDbRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return HighlightDbRow(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      refKey: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}ref_key'],
      )!,
      colorValue: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}color_value'],
      )!,
    );
  }

  @override
  $HighlightsTable createAlias(String alias) {
    return $HighlightsTable(attachedDatabase, alias);
  }
}

class HighlightDbRow extends DataClass implements Insertable<HighlightDbRow> {
  final String id;
  final String refKey;
  final int colorValue;
  const HighlightDbRow({
    required this.id,
    required this.refKey,
    required this.colorValue,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['ref_key'] = Variable<String>(refKey);
    map['color_value'] = Variable<int>(colorValue);
    return map;
  }

  HighlightsCompanion toCompanion(bool nullToAbsent) {
    return HighlightsCompanion(
      id: Value(id),
      refKey: Value(refKey),
      colorValue: Value(colorValue),
    );
  }

  factory HighlightDbRow.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return HighlightDbRow(
      id: serializer.fromJson<String>(json['id']),
      refKey: serializer.fromJson<String>(json['refKey']),
      colorValue: serializer.fromJson<int>(json['colorValue']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'refKey': serializer.toJson<String>(refKey),
      'colorValue': serializer.toJson<int>(colorValue),
    };
  }

  HighlightDbRow copyWith({String? id, String? refKey, int? colorValue}) =>
      HighlightDbRow(
        id: id ?? this.id,
        refKey: refKey ?? this.refKey,
        colorValue: colorValue ?? this.colorValue,
      );
  HighlightDbRow copyWithCompanion(HighlightsCompanion data) {
    return HighlightDbRow(
      id: data.id.present ? data.id.value : this.id,
      refKey: data.refKey.present ? data.refKey.value : this.refKey,
      colorValue: data.colorValue.present
          ? data.colorValue.value
          : this.colorValue,
    );
  }

  @override
  String toString() {
    return (StringBuffer('HighlightDbRow(')
          ..write('id: $id, ')
          ..write('refKey: $refKey, ')
          ..write('colorValue: $colorValue')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, refKey, colorValue);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is HighlightDbRow &&
          other.id == this.id &&
          other.refKey == this.refKey &&
          other.colorValue == this.colorValue);
}

class HighlightsCompanion extends UpdateCompanion<HighlightDbRow> {
  final Value<String> id;
  final Value<String> refKey;
  final Value<int> colorValue;
  final Value<int> rowid;
  const HighlightsCompanion({
    this.id = const Value.absent(),
    this.refKey = const Value.absent(),
    this.colorValue = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  HighlightsCompanion.insert({
    required String id,
    required String refKey,
    required int colorValue,
    this.rowid = const Value.absent(),
  }) : id = Value(id),
       refKey = Value(refKey),
       colorValue = Value(colorValue);
  static Insertable<HighlightDbRow> custom({
    Expression<String>? id,
    Expression<String>? refKey,
    Expression<int>? colorValue,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (refKey != null) 'ref_key': refKey,
      if (colorValue != null) 'color_value': colorValue,
      if (rowid != null) 'rowid': rowid,
    });
  }

  HighlightsCompanion copyWith({
    Value<String>? id,
    Value<String>? refKey,
    Value<int>? colorValue,
    Value<int>? rowid,
  }) {
    return HighlightsCompanion(
      id: id ?? this.id,
      refKey: refKey ?? this.refKey,
      colorValue: colorValue ?? this.colorValue,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (refKey.present) {
      map['ref_key'] = Variable<String>(refKey.value);
    }
    if (colorValue.present) {
      map['color_value'] = Variable<int>(colorValue.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('HighlightsCompanion(')
          ..write('id: $id, ')
          ..write('refKey: $refKey, ')
          ..write('colorValue: $colorValue, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $CollectionsTable extends Collections
    with TableInfo<$CollectionsTable, CollectionDbRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $CollectionsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _nameMeta = const VerificationMeta('name');
  @override
  late final GeneratedColumn<String> name = GeneratedColumn<String>(
    'name',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [id, name];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'collections';
  @override
  VerificationContext validateIntegrity(
    Insertable<CollectionDbRow> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('name')) {
      context.handle(
        _nameMeta,
        name.isAcceptableOrUnknown(data['name']!, _nameMeta),
      );
    } else if (isInserting) {
      context.missing(_nameMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  CollectionDbRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return CollectionDbRow(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      name: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}name'],
      )!,
    );
  }

  @override
  $CollectionsTable createAlias(String alias) {
    return $CollectionsTable(attachedDatabase, alias);
  }
}

class CollectionDbRow extends DataClass implements Insertable<CollectionDbRow> {
  final String id;
  final String name;
  const CollectionDbRow({required this.id, required this.name});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['name'] = Variable<String>(name);
    return map;
  }

  CollectionsCompanion toCompanion(bool nullToAbsent) {
    return CollectionsCompanion(id: Value(id), name: Value(name));
  }

  factory CollectionDbRow.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return CollectionDbRow(
      id: serializer.fromJson<String>(json['id']),
      name: serializer.fromJson<String>(json['name']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'name': serializer.toJson<String>(name),
    };
  }

  CollectionDbRow copyWith({String? id, String? name}) =>
      CollectionDbRow(id: id ?? this.id, name: name ?? this.name);
  CollectionDbRow copyWithCompanion(CollectionsCompanion data) {
    return CollectionDbRow(
      id: data.id.present ? data.id.value : this.id,
      name: data.name.present ? data.name.value : this.name,
    );
  }

  @override
  String toString() {
    return (StringBuffer('CollectionDbRow(')
          ..write('id: $id, ')
          ..write('name: $name')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, name);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is CollectionDbRow &&
          other.id == this.id &&
          other.name == this.name);
}

class CollectionsCompanion extends UpdateCompanion<CollectionDbRow> {
  final Value<String> id;
  final Value<String> name;
  final Value<int> rowid;
  const CollectionsCompanion({
    this.id = const Value.absent(),
    this.name = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  CollectionsCompanion.insert({
    required String id,
    required String name,
    this.rowid = const Value.absent(),
  }) : id = Value(id),
       name = Value(name);
  static Insertable<CollectionDbRow> custom({
    Expression<String>? id,
    Expression<String>? name,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (name != null) 'name': name,
      if (rowid != null) 'rowid': rowid,
    });
  }

  CollectionsCompanion copyWith({
    Value<String>? id,
    Value<String>? name,
    Value<int>? rowid,
  }) {
    return CollectionsCompanion(
      id: id ?? this.id,
      name: name ?? this.name,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (name.present) {
      map['name'] = Variable<String>(name.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('CollectionsCompanion(')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $RecentItemsTable extends RecentItems
    with TableInfo<$RecentItemsTable, RecentDbRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $RecentItemsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _refKeyMeta = const VerificationMeta('refKey');
  @override
  late final GeneratedColumn<String> refKey = GeneratedColumn<String>(
    'ref_key',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _titleMeta = const VerificationMeta('title');
  @override
  late final GeneratedColumn<String> title = GeneratedColumn<String>(
    'title',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _subtitleMeta = const VerificationMeta(
    'subtitle',
  );
  @override
  late final GeneratedColumn<String> subtitle = GeneratedColumn<String>(
    'subtitle',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _kindMeta = const VerificationMeta('kind');
  @override
  late final GeneratedColumn<int> kind = GeneratedColumn<int>(
    'kind',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _touchedAtMeta = const VerificationMeta(
    'touchedAt',
  );
  @override
  late final GeneratedColumn<DateTime> touchedAt = GeneratedColumn<DateTime>(
    'touched_at',
    aliasedName,
    false,
    type: DriftSqlType.dateTime,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [
    refKey,
    title,
    subtitle,
    kind,
    touchedAt,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'recent_items';
  @override
  VerificationContext validateIntegrity(
    Insertable<RecentDbRow> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('ref_key')) {
      context.handle(
        _refKeyMeta,
        refKey.isAcceptableOrUnknown(data['ref_key']!, _refKeyMeta),
      );
    } else if (isInserting) {
      context.missing(_refKeyMeta);
    }
    if (data.containsKey('title')) {
      context.handle(
        _titleMeta,
        title.isAcceptableOrUnknown(data['title']!, _titleMeta),
      );
    } else if (isInserting) {
      context.missing(_titleMeta);
    }
    if (data.containsKey('subtitle')) {
      context.handle(
        _subtitleMeta,
        subtitle.isAcceptableOrUnknown(data['subtitle']!, _subtitleMeta),
      );
    } else if (isInserting) {
      context.missing(_subtitleMeta);
    }
    if (data.containsKey('kind')) {
      context.handle(
        _kindMeta,
        kind.isAcceptableOrUnknown(data['kind']!, _kindMeta),
      );
    } else if (isInserting) {
      context.missing(_kindMeta);
    }
    if (data.containsKey('touched_at')) {
      context.handle(
        _touchedAtMeta,
        touchedAt.isAcceptableOrUnknown(data['touched_at']!, _touchedAtMeta),
      );
    } else if (isInserting) {
      context.missing(_touchedAtMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {refKey};
  @override
  RecentDbRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return RecentDbRow(
      refKey: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}ref_key'],
      )!,
      title: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}title'],
      )!,
      subtitle: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}subtitle'],
      )!,
      kind: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}kind'],
      )!,
      touchedAt: attachedDatabase.typeMapping.read(
        DriftSqlType.dateTime,
        data['${effectivePrefix}touched_at'],
      )!,
    );
  }

  @override
  $RecentItemsTable createAlias(String alias) {
    return $RecentItemsTable(attachedDatabase, alias);
  }
}

class RecentDbRow extends DataClass implements Insertable<RecentDbRow> {
  final String refKey;
  final String title;
  final String subtitle;
  final int kind;
  final DateTime touchedAt;
  const RecentDbRow({
    required this.refKey,
    required this.title,
    required this.subtitle,
    required this.kind,
    required this.touchedAt,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['ref_key'] = Variable<String>(refKey);
    map['title'] = Variable<String>(title);
    map['subtitle'] = Variable<String>(subtitle);
    map['kind'] = Variable<int>(kind);
    map['touched_at'] = Variable<DateTime>(touchedAt);
    return map;
  }

  RecentItemsCompanion toCompanion(bool nullToAbsent) {
    return RecentItemsCompanion(
      refKey: Value(refKey),
      title: Value(title),
      subtitle: Value(subtitle),
      kind: Value(kind),
      touchedAt: Value(touchedAt),
    );
  }

  factory RecentDbRow.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return RecentDbRow(
      refKey: serializer.fromJson<String>(json['refKey']),
      title: serializer.fromJson<String>(json['title']),
      subtitle: serializer.fromJson<String>(json['subtitle']),
      kind: serializer.fromJson<int>(json['kind']),
      touchedAt: serializer.fromJson<DateTime>(json['touchedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'refKey': serializer.toJson<String>(refKey),
      'title': serializer.toJson<String>(title),
      'subtitle': serializer.toJson<String>(subtitle),
      'kind': serializer.toJson<int>(kind),
      'touchedAt': serializer.toJson<DateTime>(touchedAt),
    };
  }

  RecentDbRow copyWith({
    String? refKey,
    String? title,
    String? subtitle,
    int? kind,
    DateTime? touchedAt,
  }) => RecentDbRow(
    refKey: refKey ?? this.refKey,
    title: title ?? this.title,
    subtitle: subtitle ?? this.subtitle,
    kind: kind ?? this.kind,
    touchedAt: touchedAt ?? this.touchedAt,
  );
  RecentDbRow copyWithCompanion(RecentItemsCompanion data) {
    return RecentDbRow(
      refKey: data.refKey.present ? data.refKey.value : this.refKey,
      title: data.title.present ? data.title.value : this.title,
      subtitle: data.subtitle.present ? data.subtitle.value : this.subtitle,
      kind: data.kind.present ? data.kind.value : this.kind,
      touchedAt: data.touchedAt.present ? data.touchedAt.value : this.touchedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('RecentDbRow(')
          ..write('refKey: $refKey, ')
          ..write('title: $title, ')
          ..write('subtitle: $subtitle, ')
          ..write('kind: $kind, ')
          ..write('touchedAt: $touchedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(refKey, title, subtitle, kind, touchedAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is RecentDbRow &&
          other.refKey == this.refKey &&
          other.title == this.title &&
          other.subtitle == this.subtitle &&
          other.kind == this.kind &&
          other.touchedAt == this.touchedAt);
}

class RecentItemsCompanion extends UpdateCompanion<RecentDbRow> {
  final Value<String> refKey;
  final Value<String> title;
  final Value<String> subtitle;
  final Value<int> kind;
  final Value<DateTime> touchedAt;
  final Value<int> rowid;
  const RecentItemsCompanion({
    this.refKey = const Value.absent(),
    this.title = const Value.absent(),
    this.subtitle = const Value.absent(),
    this.kind = const Value.absent(),
    this.touchedAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  RecentItemsCompanion.insert({
    required String refKey,
    required String title,
    required String subtitle,
    required int kind,
    required DateTime touchedAt,
    this.rowid = const Value.absent(),
  }) : refKey = Value(refKey),
       title = Value(title),
       subtitle = Value(subtitle),
       kind = Value(kind),
       touchedAt = Value(touchedAt);
  static Insertable<RecentDbRow> custom({
    Expression<String>? refKey,
    Expression<String>? title,
    Expression<String>? subtitle,
    Expression<int>? kind,
    Expression<DateTime>? touchedAt,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (refKey != null) 'ref_key': refKey,
      if (title != null) 'title': title,
      if (subtitle != null) 'subtitle': subtitle,
      if (kind != null) 'kind': kind,
      if (touchedAt != null) 'touched_at': touchedAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  RecentItemsCompanion copyWith({
    Value<String>? refKey,
    Value<String>? title,
    Value<String>? subtitle,
    Value<int>? kind,
    Value<DateTime>? touchedAt,
    Value<int>? rowid,
  }) {
    return RecentItemsCompanion(
      refKey: refKey ?? this.refKey,
      title: title ?? this.title,
      subtitle: subtitle ?? this.subtitle,
      kind: kind ?? this.kind,
      touchedAt: touchedAt ?? this.touchedAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (refKey.present) {
      map['ref_key'] = Variable<String>(refKey.value);
    }
    if (title.present) {
      map['title'] = Variable<String>(title.value);
    }
    if (subtitle.present) {
      map['subtitle'] = Variable<String>(subtitle.value);
    }
    if (kind.present) {
      map['kind'] = Variable<int>(kind.value);
    }
    if (touchedAt.present) {
      map['touched_at'] = Variable<DateTime>(touchedAt.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('RecentItemsCompanion(')
          ..write('refKey: $refKey, ')
          ..write('title: $title, ')
          ..write('subtitle: $subtitle, ')
          ..write('kind: $kind, ')
          ..write('touchedAt: $touchedAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $SearchDocumentsTable extends SearchDocuments
    with TableInfo<$SearchDocumentsTable, SearchDocumentDbRow> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $SearchDocumentsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _kindMeta = const VerificationMeta('kind');
  @override
  late final GeneratedColumn<String> kind = GeneratedColumn<String>(
    'kind',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _refKeyMeta = const VerificationMeta('refKey');
  @override
  late final GeneratedColumn<String> refKey = GeneratedColumn<String>(
    'ref_key',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _titleMeta = const VerificationMeta('title');
  @override
  late final GeneratedColumn<String> title = GeneratedColumn<String>(
    'title',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _subtitleMeta = const VerificationMeta(
    'subtitle',
  );
  @override
  late final GeneratedColumn<String> subtitle = GeneratedColumn<String>(
    'subtitle',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _bodyMeta = const VerificationMeta('body');
  @override
  late final GeneratedColumn<String> body = GeneratedColumn<String>(
    'body',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _normalizedBodyMeta = const VerificationMeta(
    'normalizedBody',
  );
  @override
  late final GeneratedColumn<String> normalizedBody = GeneratedColumn<String>(
    'normalized_body',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _normalizedTitleMeta = const VerificationMeta(
    'normalizedTitle',
  );
  @override
  late final GeneratedColumn<String> normalizedTitle = GeneratedColumn<String>(
    'normalized_title',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _surahMeta = const VerificationMeta('surah');
  @override
  late final GeneratedColumn<int> surah = GeneratedColumn<int>(
    'surah',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _ayahMeta = const VerificationMeta('ayah');
  @override
  late final GeneratedColumn<int> ayah = GeneratedColumn<int>(
    'ayah',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _editionIdMeta = const VerificationMeta(
    'editionId',
  );
  @override
  late final GeneratedColumn<String> editionId = GeneratedColumn<String>(
    'edition_id',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _tafsirIdMeta = const VerificationMeta(
    'tafsirId',
  );
  @override
  late final GeneratedColumn<String> tafsirId = GeneratedColumn<String>(
    'tafsir_id',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _collectionIdMeta = const VerificationMeta(
    'collectionId',
  );
  @override
  late final GeneratedColumn<String> collectionId = GeneratedColumn<String>(
    'collection_id',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _hadithNumberMeta = const VerificationMeta(
    'hadithNumber',
  );
  @override
  late final GeneratedColumn<String> hadithNumber = GeneratedColumn<String>(
    'hadith_number',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _bookMeta = const VerificationMeta('book');
  @override
  late final GeneratedColumn<String> book = GeneratedColumn<String>(
    'book',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    kind,
    refKey,
    title,
    subtitle,
    body,
    normalizedBody,
    normalizedTitle,
    surah,
    ayah,
    editionId,
    tafsirId,
    collectionId,
    hadithNumber,
    book,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'search_documents';
  @override
  VerificationContext validateIntegrity(
    Insertable<SearchDocumentDbRow> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    } else if (isInserting) {
      context.missing(_idMeta);
    }
    if (data.containsKey('kind')) {
      context.handle(
        _kindMeta,
        kind.isAcceptableOrUnknown(data['kind']!, _kindMeta),
      );
    } else if (isInserting) {
      context.missing(_kindMeta);
    }
    if (data.containsKey('ref_key')) {
      context.handle(
        _refKeyMeta,
        refKey.isAcceptableOrUnknown(data['ref_key']!, _refKeyMeta),
      );
    } else if (isInserting) {
      context.missing(_refKeyMeta);
    }
    if (data.containsKey('title')) {
      context.handle(
        _titleMeta,
        title.isAcceptableOrUnknown(data['title']!, _titleMeta),
      );
    } else if (isInserting) {
      context.missing(_titleMeta);
    }
    if (data.containsKey('subtitle')) {
      context.handle(
        _subtitleMeta,
        subtitle.isAcceptableOrUnknown(data['subtitle']!, _subtitleMeta),
      );
    } else if (isInserting) {
      context.missing(_subtitleMeta);
    }
    if (data.containsKey('body')) {
      context.handle(
        _bodyMeta,
        body.isAcceptableOrUnknown(data['body']!, _bodyMeta),
      );
    } else if (isInserting) {
      context.missing(_bodyMeta);
    }
    if (data.containsKey('normalized_body')) {
      context.handle(
        _normalizedBodyMeta,
        normalizedBody.isAcceptableOrUnknown(
          data['normalized_body']!,
          _normalizedBodyMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_normalizedBodyMeta);
    }
    if (data.containsKey('normalized_title')) {
      context.handle(
        _normalizedTitleMeta,
        normalizedTitle.isAcceptableOrUnknown(
          data['normalized_title']!,
          _normalizedTitleMeta,
        ),
      );
    } else if (isInserting) {
      context.missing(_normalizedTitleMeta);
    }
    if (data.containsKey('surah')) {
      context.handle(
        _surahMeta,
        surah.isAcceptableOrUnknown(data['surah']!, _surahMeta),
      );
    }
    if (data.containsKey('ayah')) {
      context.handle(
        _ayahMeta,
        ayah.isAcceptableOrUnknown(data['ayah']!, _ayahMeta),
      );
    }
    if (data.containsKey('edition_id')) {
      context.handle(
        _editionIdMeta,
        editionId.isAcceptableOrUnknown(data['edition_id']!, _editionIdMeta),
      );
    }
    if (data.containsKey('tafsir_id')) {
      context.handle(
        _tafsirIdMeta,
        tafsirId.isAcceptableOrUnknown(data['tafsir_id']!, _tafsirIdMeta),
      );
    }
    if (data.containsKey('collection_id')) {
      context.handle(
        _collectionIdMeta,
        collectionId.isAcceptableOrUnknown(
          data['collection_id']!,
          _collectionIdMeta,
        ),
      );
    }
    if (data.containsKey('hadith_number')) {
      context.handle(
        _hadithNumberMeta,
        hadithNumber.isAcceptableOrUnknown(
          data['hadith_number']!,
          _hadithNumberMeta,
        ),
      );
    }
    if (data.containsKey('book')) {
      context.handle(
        _bookMeta,
        book.isAcceptableOrUnknown(data['book']!, _bookMeta),
      );
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  SearchDocumentDbRow map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return SearchDocumentDbRow(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}id'],
      )!,
      kind: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}kind'],
      )!,
      refKey: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}ref_key'],
      )!,
      title: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}title'],
      )!,
      subtitle: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}subtitle'],
      )!,
      body: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}body'],
      )!,
      normalizedBody: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}normalized_body'],
      )!,
      normalizedTitle: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}normalized_title'],
      )!,
      surah: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}surah'],
      ),
      ayah: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}ayah'],
      ),
      editionId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}edition_id'],
      ),
      tafsirId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}tafsir_id'],
      ),
      collectionId: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}collection_id'],
      ),
      hadithNumber: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}hadith_number'],
      ),
      book: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}book'],
      ),
    );
  }

  @override
  $SearchDocumentsTable createAlias(String alias) {
    return $SearchDocumentsTable(attachedDatabase, alias);
  }
}

class SearchDocumentDbRow extends DataClass
    implements Insertable<SearchDocumentDbRow> {
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
  const SearchDocumentDbRow({
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
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['kind'] = Variable<String>(kind);
    map['ref_key'] = Variable<String>(refKey);
    map['title'] = Variable<String>(title);
    map['subtitle'] = Variable<String>(subtitle);
    map['body'] = Variable<String>(body);
    map['normalized_body'] = Variable<String>(normalizedBody);
    map['normalized_title'] = Variable<String>(normalizedTitle);
    if (!nullToAbsent || surah != null) {
      map['surah'] = Variable<int>(surah);
    }
    if (!nullToAbsent || ayah != null) {
      map['ayah'] = Variable<int>(ayah);
    }
    if (!nullToAbsent || editionId != null) {
      map['edition_id'] = Variable<String>(editionId);
    }
    if (!nullToAbsent || tafsirId != null) {
      map['tafsir_id'] = Variable<String>(tafsirId);
    }
    if (!nullToAbsent || collectionId != null) {
      map['collection_id'] = Variable<String>(collectionId);
    }
    if (!nullToAbsent || hadithNumber != null) {
      map['hadith_number'] = Variable<String>(hadithNumber);
    }
    if (!nullToAbsent || book != null) {
      map['book'] = Variable<String>(book);
    }
    return map;
  }

  SearchDocumentsCompanion toCompanion(bool nullToAbsent) {
    return SearchDocumentsCompanion(
      id: Value(id),
      kind: Value(kind),
      refKey: Value(refKey),
      title: Value(title),
      subtitle: Value(subtitle),
      body: Value(body),
      normalizedBody: Value(normalizedBody),
      normalizedTitle: Value(normalizedTitle),
      surah: surah == null && nullToAbsent
          ? const Value.absent()
          : Value(surah),
      ayah: ayah == null && nullToAbsent ? const Value.absent() : Value(ayah),
      editionId: editionId == null && nullToAbsent
          ? const Value.absent()
          : Value(editionId),
      tafsirId: tafsirId == null && nullToAbsent
          ? const Value.absent()
          : Value(tafsirId),
      collectionId: collectionId == null && nullToAbsent
          ? const Value.absent()
          : Value(collectionId),
      hadithNumber: hadithNumber == null && nullToAbsent
          ? const Value.absent()
          : Value(hadithNumber),
      book: book == null && nullToAbsent ? const Value.absent() : Value(book),
    );
  }

  factory SearchDocumentDbRow.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return SearchDocumentDbRow(
      id: serializer.fromJson<String>(json['id']),
      kind: serializer.fromJson<String>(json['kind']),
      refKey: serializer.fromJson<String>(json['refKey']),
      title: serializer.fromJson<String>(json['title']),
      subtitle: serializer.fromJson<String>(json['subtitle']),
      body: serializer.fromJson<String>(json['body']),
      normalizedBody: serializer.fromJson<String>(json['normalizedBody']),
      normalizedTitle: serializer.fromJson<String>(json['normalizedTitle']),
      surah: serializer.fromJson<int?>(json['surah']),
      ayah: serializer.fromJson<int?>(json['ayah']),
      editionId: serializer.fromJson<String?>(json['editionId']),
      tafsirId: serializer.fromJson<String?>(json['tafsirId']),
      collectionId: serializer.fromJson<String?>(json['collectionId']),
      hadithNumber: serializer.fromJson<String?>(json['hadithNumber']),
      book: serializer.fromJson<String?>(json['book']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'kind': serializer.toJson<String>(kind),
      'refKey': serializer.toJson<String>(refKey),
      'title': serializer.toJson<String>(title),
      'subtitle': serializer.toJson<String>(subtitle),
      'body': serializer.toJson<String>(body),
      'normalizedBody': serializer.toJson<String>(normalizedBody),
      'normalizedTitle': serializer.toJson<String>(normalizedTitle),
      'surah': serializer.toJson<int?>(surah),
      'ayah': serializer.toJson<int?>(ayah),
      'editionId': serializer.toJson<String?>(editionId),
      'tafsirId': serializer.toJson<String?>(tafsirId),
      'collectionId': serializer.toJson<String?>(collectionId),
      'hadithNumber': serializer.toJson<String?>(hadithNumber),
      'book': serializer.toJson<String?>(book),
    };
  }

  SearchDocumentDbRow copyWith({
    String? id,
    String? kind,
    String? refKey,
    String? title,
    String? subtitle,
    String? body,
    String? normalizedBody,
    String? normalizedTitle,
    Value<int?> surah = const Value.absent(),
    Value<int?> ayah = const Value.absent(),
    Value<String?> editionId = const Value.absent(),
    Value<String?> tafsirId = const Value.absent(),
    Value<String?> collectionId = const Value.absent(),
    Value<String?> hadithNumber = const Value.absent(),
    Value<String?> book = const Value.absent(),
  }) => SearchDocumentDbRow(
    id: id ?? this.id,
    kind: kind ?? this.kind,
    refKey: refKey ?? this.refKey,
    title: title ?? this.title,
    subtitle: subtitle ?? this.subtitle,
    body: body ?? this.body,
    normalizedBody: normalizedBody ?? this.normalizedBody,
    normalizedTitle: normalizedTitle ?? this.normalizedTitle,
    surah: surah.present ? surah.value : this.surah,
    ayah: ayah.present ? ayah.value : this.ayah,
    editionId: editionId.present ? editionId.value : this.editionId,
    tafsirId: tafsirId.present ? tafsirId.value : this.tafsirId,
    collectionId: collectionId.present ? collectionId.value : this.collectionId,
    hadithNumber: hadithNumber.present ? hadithNumber.value : this.hadithNumber,
    book: book.present ? book.value : this.book,
  );
  SearchDocumentDbRow copyWithCompanion(SearchDocumentsCompanion data) {
    return SearchDocumentDbRow(
      id: data.id.present ? data.id.value : this.id,
      kind: data.kind.present ? data.kind.value : this.kind,
      refKey: data.refKey.present ? data.refKey.value : this.refKey,
      title: data.title.present ? data.title.value : this.title,
      subtitle: data.subtitle.present ? data.subtitle.value : this.subtitle,
      body: data.body.present ? data.body.value : this.body,
      normalizedBody: data.normalizedBody.present
          ? data.normalizedBody.value
          : this.normalizedBody,
      normalizedTitle: data.normalizedTitle.present
          ? data.normalizedTitle.value
          : this.normalizedTitle,
      surah: data.surah.present ? data.surah.value : this.surah,
      ayah: data.ayah.present ? data.ayah.value : this.ayah,
      editionId: data.editionId.present ? data.editionId.value : this.editionId,
      tafsirId: data.tafsirId.present ? data.tafsirId.value : this.tafsirId,
      collectionId: data.collectionId.present
          ? data.collectionId.value
          : this.collectionId,
      hadithNumber: data.hadithNumber.present
          ? data.hadithNumber.value
          : this.hadithNumber,
      book: data.book.present ? data.book.value : this.book,
    );
  }

  @override
  String toString() {
    return (StringBuffer('SearchDocumentDbRow(')
          ..write('id: $id, ')
          ..write('kind: $kind, ')
          ..write('refKey: $refKey, ')
          ..write('title: $title, ')
          ..write('subtitle: $subtitle, ')
          ..write('body: $body, ')
          ..write('normalizedBody: $normalizedBody, ')
          ..write('normalizedTitle: $normalizedTitle, ')
          ..write('surah: $surah, ')
          ..write('ayah: $ayah, ')
          ..write('editionId: $editionId, ')
          ..write('tafsirId: $tafsirId, ')
          ..write('collectionId: $collectionId, ')
          ..write('hadithNumber: $hadithNumber, ')
          ..write('book: $book')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    kind,
    refKey,
    title,
    subtitle,
    body,
    normalizedBody,
    normalizedTitle,
    surah,
    ayah,
    editionId,
    tafsirId,
    collectionId,
    hadithNumber,
    book,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is SearchDocumentDbRow &&
          other.id == this.id &&
          other.kind == this.kind &&
          other.refKey == this.refKey &&
          other.title == this.title &&
          other.subtitle == this.subtitle &&
          other.body == this.body &&
          other.normalizedBody == this.normalizedBody &&
          other.normalizedTitle == this.normalizedTitle &&
          other.surah == this.surah &&
          other.ayah == this.ayah &&
          other.editionId == this.editionId &&
          other.tafsirId == this.tafsirId &&
          other.collectionId == this.collectionId &&
          other.hadithNumber == this.hadithNumber &&
          other.book == this.book);
}

class SearchDocumentsCompanion extends UpdateCompanion<SearchDocumentDbRow> {
  final Value<String> id;
  final Value<String> kind;
  final Value<String> refKey;
  final Value<String> title;
  final Value<String> subtitle;
  final Value<String> body;
  final Value<String> normalizedBody;
  final Value<String> normalizedTitle;
  final Value<int?> surah;
  final Value<int?> ayah;
  final Value<String?> editionId;
  final Value<String?> tafsirId;
  final Value<String?> collectionId;
  final Value<String?> hadithNumber;
  final Value<String?> book;
  final Value<int> rowid;
  const SearchDocumentsCompanion({
    this.id = const Value.absent(),
    this.kind = const Value.absent(),
    this.refKey = const Value.absent(),
    this.title = const Value.absent(),
    this.subtitle = const Value.absent(),
    this.body = const Value.absent(),
    this.normalizedBody = const Value.absent(),
    this.normalizedTitle = const Value.absent(),
    this.surah = const Value.absent(),
    this.ayah = const Value.absent(),
    this.editionId = const Value.absent(),
    this.tafsirId = const Value.absent(),
    this.collectionId = const Value.absent(),
    this.hadithNumber = const Value.absent(),
    this.book = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  SearchDocumentsCompanion.insert({
    required String id,
    required String kind,
    required String refKey,
    required String title,
    required String subtitle,
    required String body,
    required String normalizedBody,
    required String normalizedTitle,
    this.surah = const Value.absent(),
    this.ayah = const Value.absent(),
    this.editionId = const Value.absent(),
    this.tafsirId = const Value.absent(),
    this.collectionId = const Value.absent(),
    this.hadithNumber = const Value.absent(),
    this.book = const Value.absent(),
    this.rowid = const Value.absent(),
  }) : id = Value(id),
       kind = Value(kind),
       refKey = Value(refKey),
       title = Value(title),
       subtitle = Value(subtitle),
       body = Value(body),
       normalizedBody = Value(normalizedBody),
       normalizedTitle = Value(normalizedTitle);
  static Insertable<SearchDocumentDbRow> custom({
    Expression<String>? id,
    Expression<String>? kind,
    Expression<String>? refKey,
    Expression<String>? title,
    Expression<String>? subtitle,
    Expression<String>? body,
    Expression<String>? normalizedBody,
    Expression<String>? normalizedTitle,
    Expression<int>? surah,
    Expression<int>? ayah,
    Expression<String>? editionId,
    Expression<String>? tafsirId,
    Expression<String>? collectionId,
    Expression<String>? hadithNumber,
    Expression<String>? book,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (kind != null) 'kind': kind,
      if (refKey != null) 'ref_key': refKey,
      if (title != null) 'title': title,
      if (subtitle != null) 'subtitle': subtitle,
      if (body != null) 'body': body,
      if (normalizedBody != null) 'normalized_body': normalizedBody,
      if (normalizedTitle != null) 'normalized_title': normalizedTitle,
      if (surah != null) 'surah': surah,
      if (ayah != null) 'ayah': ayah,
      if (editionId != null) 'edition_id': editionId,
      if (tafsirId != null) 'tafsir_id': tafsirId,
      if (collectionId != null) 'collection_id': collectionId,
      if (hadithNumber != null) 'hadith_number': hadithNumber,
      if (book != null) 'book': book,
      if (rowid != null) 'rowid': rowid,
    });
  }

  SearchDocumentsCompanion copyWith({
    Value<String>? id,
    Value<String>? kind,
    Value<String>? refKey,
    Value<String>? title,
    Value<String>? subtitle,
    Value<String>? body,
    Value<String>? normalizedBody,
    Value<String>? normalizedTitle,
    Value<int?>? surah,
    Value<int?>? ayah,
    Value<String?>? editionId,
    Value<String?>? tafsirId,
    Value<String?>? collectionId,
    Value<String?>? hadithNumber,
    Value<String?>? book,
    Value<int>? rowid,
  }) {
    return SearchDocumentsCompanion(
      id: id ?? this.id,
      kind: kind ?? this.kind,
      refKey: refKey ?? this.refKey,
      title: title ?? this.title,
      subtitle: subtitle ?? this.subtitle,
      body: body ?? this.body,
      normalizedBody: normalizedBody ?? this.normalizedBody,
      normalizedTitle: normalizedTitle ?? this.normalizedTitle,
      surah: surah ?? this.surah,
      ayah: ayah ?? this.ayah,
      editionId: editionId ?? this.editionId,
      tafsirId: tafsirId ?? this.tafsirId,
      collectionId: collectionId ?? this.collectionId,
      hadithNumber: hadithNumber ?? this.hadithNumber,
      book: book ?? this.book,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (kind.present) {
      map['kind'] = Variable<String>(kind.value);
    }
    if (refKey.present) {
      map['ref_key'] = Variable<String>(refKey.value);
    }
    if (title.present) {
      map['title'] = Variable<String>(title.value);
    }
    if (subtitle.present) {
      map['subtitle'] = Variable<String>(subtitle.value);
    }
    if (body.present) {
      map['body'] = Variable<String>(body.value);
    }
    if (normalizedBody.present) {
      map['normalized_body'] = Variable<String>(normalizedBody.value);
    }
    if (normalizedTitle.present) {
      map['normalized_title'] = Variable<String>(normalizedTitle.value);
    }
    if (surah.present) {
      map['surah'] = Variable<int>(surah.value);
    }
    if (ayah.present) {
      map['ayah'] = Variable<int>(ayah.value);
    }
    if (editionId.present) {
      map['edition_id'] = Variable<String>(editionId.value);
    }
    if (tafsirId.present) {
      map['tafsir_id'] = Variable<String>(tafsirId.value);
    }
    if (collectionId.present) {
      map['collection_id'] = Variable<String>(collectionId.value);
    }
    if (hadithNumber.present) {
      map['hadith_number'] = Variable<String>(hadithNumber.value);
    }
    if (book.present) {
      map['book'] = Variable<String>(book.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('SearchDocumentsCompanion(')
          ..write('id: $id, ')
          ..write('kind: $kind, ')
          ..write('refKey: $refKey, ')
          ..write('title: $title, ')
          ..write('subtitle: $subtitle, ')
          ..write('body: $body, ')
          ..write('normalizedBody: $normalizedBody, ')
          ..write('normalizedTitle: $normalizedTitle, ')
          ..write('surah: $surah, ')
          ..write('ayah: $ayah, ')
          ..write('editionId: $editionId, ')
          ..write('tafsirId: $tafsirId, ')
          ..write('collectionId: $collectionId, ')
          ..write('hadithNumber: $hadithNumber, ')
          ..write('book: $book, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

abstract class _$AppDatabase extends GeneratedDatabase {
  _$AppDatabase(QueryExecutor e) : super(e);
  $AppDatabaseManager get managers => $AppDatabaseManager(this);
  late final $AppMetaTable appMeta = $AppMetaTable(this);
  late final $BookmarksTable bookmarks = $BookmarksTable(this);
  late final $NotesTable notes = $NotesTable(this);
  late final $HighlightsTable highlights = $HighlightsTable(this);
  late final $CollectionsTable collections = $CollectionsTable(this);
  late final $RecentItemsTable recentItems = $RecentItemsTable(this);
  late final $SearchDocumentsTable searchDocuments = $SearchDocumentsTable(
    this,
  );
  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [
    appMeta,
    bookmarks,
    notes,
    highlights,
    collections,
    recentItems,
    searchDocuments,
  ];
}

typedef $$AppMetaTableCreateCompanionBuilder =
    AppMetaCompanion Function({
      required String key,
      required String value,
      Value<int> rowid,
    });
typedef $$AppMetaTableUpdateCompanionBuilder =
    AppMetaCompanion Function({
      Value<String> key,
      Value<String> value,
      Value<int> rowid,
    });

class $$AppMetaTableFilterComposer
    extends Composer<_$AppDatabase, $AppMetaTable> {
  $$AppMetaTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get key => $composableBuilder(
    column: $table.key,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get value => $composableBuilder(
    column: $table.value,
    builder: (column) => ColumnFilters(column),
  );
}

class $$AppMetaTableOrderingComposer
    extends Composer<_$AppDatabase, $AppMetaTable> {
  $$AppMetaTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get key => $composableBuilder(
    column: $table.key,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get value => $composableBuilder(
    column: $table.value,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$AppMetaTableAnnotationComposer
    extends Composer<_$AppDatabase, $AppMetaTable> {
  $$AppMetaTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get key =>
      $composableBuilder(column: $table.key, builder: (column) => column);

  GeneratedColumn<String> get value =>
      $composableBuilder(column: $table.value, builder: (column) => column);
}

class $$AppMetaTableTableManager
    extends
        RootTableManager<
          _$AppDatabase,
          $AppMetaTable,
          MetaRow,
          $$AppMetaTableFilterComposer,
          $$AppMetaTableOrderingComposer,
          $$AppMetaTableAnnotationComposer,
          $$AppMetaTableCreateCompanionBuilder,
          $$AppMetaTableUpdateCompanionBuilder,
          (MetaRow, BaseReferences<_$AppDatabase, $AppMetaTable, MetaRow>),
          MetaRow,
          PrefetchHooks Function()
        > {
  $$AppMetaTableTableManager(_$AppDatabase db, $AppMetaTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$AppMetaTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$AppMetaTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$AppMetaTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<String> key = const Value.absent(),
                Value<String> value = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => AppMetaCompanion(key: key, value: value, rowid: rowid),
          createCompanionCallback:
              ({
                required String key,
                required String value,
                Value<int> rowid = const Value.absent(),
              }) =>
                  AppMetaCompanion.insert(key: key, value: value, rowid: rowid),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable<$AppMetaTable, MetaRow>(table),
                  BaseReferences<_$AppDatabase, $AppMetaTable, MetaRow>(
                    db,
                    table,
                    e,
                  ),
                ),
              )
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$AppMetaTableProcessedTableManager =
    ProcessedTableManager<
      _$AppDatabase,
      $AppMetaTable,
      MetaRow,
      $$AppMetaTableFilterComposer,
      $$AppMetaTableOrderingComposer,
      $$AppMetaTableAnnotationComposer,
      $$AppMetaTableCreateCompanionBuilder,
      $$AppMetaTableUpdateCompanionBuilder,
      (MetaRow, BaseReferences<_$AppDatabase, $AppMetaTable, MetaRow>),
      MetaRow,
      PrefetchHooks Function()
    >;
typedef $$BookmarksTableCreateCompanionBuilder =
    BookmarksCompanion Function({
      required String id,
      required int kind,
      required String refKey,
      required String title,
      required String subtitle,
      Value<String?> collectionId,
      required DateTime createdAt,
      Value<int> rowid,
    });
typedef $$BookmarksTableUpdateCompanionBuilder =
    BookmarksCompanion Function({
      Value<String> id,
      Value<int> kind,
      Value<String> refKey,
      Value<String> title,
      Value<String> subtitle,
      Value<String?> collectionId,
      Value<DateTime> createdAt,
      Value<int> rowid,
    });

class $$BookmarksTableFilterComposer
    extends Composer<_$AppDatabase, $BookmarksTable> {
  $$BookmarksTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get kind => $composableBuilder(
    column: $table.kind,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get refKey => $composableBuilder(
    column: $table.refKey,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get subtitle => $composableBuilder(
    column: $table.subtitle,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get collectionId => $composableBuilder(
    column: $table.collectionId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnFilters(column),
  );
}

class $$BookmarksTableOrderingComposer
    extends Composer<_$AppDatabase, $BookmarksTable> {
  $$BookmarksTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get kind => $composableBuilder(
    column: $table.kind,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get refKey => $composableBuilder(
    column: $table.refKey,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get subtitle => $composableBuilder(
    column: $table.subtitle,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get collectionId => $composableBuilder(
    column: $table.collectionId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$BookmarksTableAnnotationComposer
    extends Composer<_$AppDatabase, $BookmarksTable> {
  $$BookmarksTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<int> get kind =>
      $composableBuilder(column: $table.kind, builder: (column) => column);

  GeneratedColumn<String> get refKey =>
      $composableBuilder(column: $table.refKey, builder: (column) => column);

  GeneratedColumn<String> get title =>
      $composableBuilder(column: $table.title, builder: (column) => column);

  GeneratedColumn<String> get subtitle =>
      $composableBuilder(column: $table.subtitle, builder: (column) => column);

  GeneratedColumn<String> get collectionId => $composableBuilder(
    column: $table.collectionId,
    builder: (column) => column,
  );

  GeneratedColumn<DateTime> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);
}

class $$BookmarksTableTableManager
    extends
        RootTableManager<
          _$AppDatabase,
          $BookmarksTable,
          BookmarkDbRow,
          $$BookmarksTableFilterComposer,
          $$BookmarksTableOrderingComposer,
          $$BookmarksTableAnnotationComposer,
          $$BookmarksTableCreateCompanionBuilder,
          $$BookmarksTableUpdateCompanionBuilder,
          (
            BookmarkDbRow,
            BaseReferences<_$AppDatabase, $BookmarksTable, BookmarkDbRow>,
          ),
          BookmarkDbRow,
          PrefetchHooks Function()
        > {
  $$BookmarksTableTableManager(_$AppDatabase db, $BookmarksTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$BookmarksTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$BookmarksTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$BookmarksTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<String> id = const Value.absent(),
                Value<int> kind = const Value.absent(),
                Value<String> refKey = const Value.absent(),
                Value<String> title = const Value.absent(),
                Value<String> subtitle = const Value.absent(),
                Value<String?> collectionId = const Value.absent(),
                Value<DateTime> createdAt = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => BookmarksCompanion(
                id: id,
                kind: kind,
                refKey: refKey,
                title: title,
                subtitle: subtitle,
                collectionId: collectionId,
                createdAt: createdAt,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String id,
                required int kind,
                required String refKey,
                required String title,
                required String subtitle,
                Value<String?> collectionId = const Value.absent(),
                required DateTime createdAt,
                Value<int> rowid = const Value.absent(),
              }) => BookmarksCompanion.insert(
                id: id,
                kind: kind,
                refKey: refKey,
                title: title,
                subtitle: subtitle,
                collectionId: collectionId,
                createdAt: createdAt,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable<$BookmarksTable, BookmarkDbRow>(table),
                  BaseReferences<_$AppDatabase, $BookmarksTable, BookmarkDbRow>(
                    db,
                    table,
                    e,
                  ),
                ),
              )
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$BookmarksTableProcessedTableManager =
    ProcessedTableManager<
      _$AppDatabase,
      $BookmarksTable,
      BookmarkDbRow,
      $$BookmarksTableFilterComposer,
      $$BookmarksTableOrderingComposer,
      $$BookmarksTableAnnotationComposer,
      $$BookmarksTableCreateCompanionBuilder,
      $$BookmarksTableUpdateCompanionBuilder,
      (
        BookmarkDbRow,
        BaseReferences<_$AppDatabase, $BookmarksTable, BookmarkDbRow>,
      ),
      BookmarkDbRow,
      PrefetchHooks Function()
    >;
typedef $$NotesTableCreateCompanionBuilder =
    NotesCompanion Function({
      required String id,
      required String refKey,
      required String textValue,
      required DateTime createdAt,
      Value<int> rowid,
    });
typedef $$NotesTableUpdateCompanionBuilder =
    NotesCompanion Function({
      Value<String> id,
      Value<String> refKey,
      Value<String> textValue,
      Value<DateTime> createdAt,
      Value<int> rowid,
    });

class $$NotesTableFilterComposer extends Composer<_$AppDatabase, $NotesTable> {
  $$NotesTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get refKey => $composableBuilder(
    column: $table.refKey,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get textValue => $composableBuilder(
    column: $table.textValue,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnFilters(column),
  );
}

class $$NotesTableOrderingComposer
    extends Composer<_$AppDatabase, $NotesTable> {
  $$NotesTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get refKey => $composableBuilder(
    column: $table.refKey,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get textValue => $composableBuilder(
    column: $table.textValue,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get createdAt => $composableBuilder(
    column: $table.createdAt,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$NotesTableAnnotationComposer
    extends Composer<_$AppDatabase, $NotesTable> {
  $$NotesTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get refKey =>
      $composableBuilder(column: $table.refKey, builder: (column) => column);

  GeneratedColumn<String> get textValue =>
      $composableBuilder(column: $table.textValue, builder: (column) => column);

  GeneratedColumn<DateTime> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);
}

class $$NotesTableTableManager
    extends
        RootTableManager<
          _$AppDatabase,
          $NotesTable,
          NoteDbRow,
          $$NotesTableFilterComposer,
          $$NotesTableOrderingComposer,
          $$NotesTableAnnotationComposer,
          $$NotesTableCreateCompanionBuilder,
          $$NotesTableUpdateCompanionBuilder,
          (NoteDbRow, BaseReferences<_$AppDatabase, $NotesTable, NoteDbRow>),
          NoteDbRow,
          PrefetchHooks Function()
        > {
  $$NotesTableTableManager(_$AppDatabase db, $NotesTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$NotesTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$NotesTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$NotesTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<String> id = const Value.absent(),
                Value<String> refKey = const Value.absent(),
                Value<String> textValue = const Value.absent(),
                Value<DateTime> createdAt = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => NotesCompanion(
                id: id,
                refKey: refKey,
                textValue: textValue,
                createdAt: createdAt,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String id,
                required String refKey,
                required String textValue,
                required DateTime createdAt,
                Value<int> rowid = const Value.absent(),
              }) => NotesCompanion.insert(
                id: id,
                refKey: refKey,
                textValue: textValue,
                createdAt: createdAt,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable<$NotesTable, NoteDbRow>(table),
                  BaseReferences<_$AppDatabase, $NotesTable, NoteDbRow>(
                    db,
                    table,
                    e,
                  ),
                ),
              )
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$NotesTableProcessedTableManager =
    ProcessedTableManager<
      _$AppDatabase,
      $NotesTable,
      NoteDbRow,
      $$NotesTableFilterComposer,
      $$NotesTableOrderingComposer,
      $$NotesTableAnnotationComposer,
      $$NotesTableCreateCompanionBuilder,
      $$NotesTableUpdateCompanionBuilder,
      (NoteDbRow, BaseReferences<_$AppDatabase, $NotesTable, NoteDbRow>),
      NoteDbRow,
      PrefetchHooks Function()
    >;
typedef $$HighlightsTableCreateCompanionBuilder =
    HighlightsCompanion Function({
      required String id,
      required String refKey,
      required int colorValue,
      Value<int> rowid,
    });
typedef $$HighlightsTableUpdateCompanionBuilder =
    HighlightsCompanion Function({
      Value<String> id,
      Value<String> refKey,
      Value<int> colorValue,
      Value<int> rowid,
    });

class $$HighlightsTableFilterComposer
    extends Composer<_$AppDatabase, $HighlightsTable> {
  $$HighlightsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get refKey => $composableBuilder(
    column: $table.refKey,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get colorValue => $composableBuilder(
    column: $table.colorValue,
    builder: (column) => ColumnFilters(column),
  );
}

class $$HighlightsTableOrderingComposer
    extends Composer<_$AppDatabase, $HighlightsTable> {
  $$HighlightsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get refKey => $composableBuilder(
    column: $table.refKey,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get colorValue => $composableBuilder(
    column: $table.colorValue,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$HighlightsTableAnnotationComposer
    extends Composer<_$AppDatabase, $HighlightsTable> {
  $$HighlightsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get refKey =>
      $composableBuilder(column: $table.refKey, builder: (column) => column);

  GeneratedColumn<int> get colorValue => $composableBuilder(
    column: $table.colorValue,
    builder: (column) => column,
  );
}

class $$HighlightsTableTableManager
    extends
        RootTableManager<
          _$AppDatabase,
          $HighlightsTable,
          HighlightDbRow,
          $$HighlightsTableFilterComposer,
          $$HighlightsTableOrderingComposer,
          $$HighlightsTableAnnotationComposer,
          $$HighlightsTableCreateCompanionBuilder,
          $$HighlightsTableUpdateCompanionBuilder,
          (
            HighlightDbRow,
            BaseReferences<_$AppDatabase, $HighlightsTable, HighlightDbRow>,
          ),
          HighlightDbRow,
          PrefetchHooks Function()
        > {
  $$HighlightsTableTableManager(_$AppDatabase db, $HighlightsTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$HighlightsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$HighlightsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$HighlightsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<String> id = const Value.absent(),
                Value<String> refKey = const Value.absent(),
                Value<int> colorValue = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => HighlightsCompanion(
                id: id,
                refKey: refKey,
                colorValue: colorValue,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String id,
                required String refKey,
                required int colorValue,
                Value<int> rowid = const Value.absent(),
              }) => HighlightsCompanion.insert(
                id: id,
                refKey: refKey,
                colorValue: colorValue,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable<$HighlightsTable, HighlightDbRow>(table),
                  BaseReferences<
                    _$AppDatabase,
                    $HighlightsTable,
                    HighlightDbRow
                  >(db, table, e),
                ),
              )
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$HighlightsTableProcessedTableManager =
    ProcessedTableManager<
      _$AppDatabase,
      $HighlightsTable,
      HighlightDbRow,
      $$HighlightsTableFilterComposer,
      $$HighlightsTableOrderingComposer,
      $$HighlightsTableAnnotationComposer,
      $$HighlightsTableCreateCompanionBuilder,
      $$HighlightsTableUpdateCompanionBuilder,
      (
        HighlightDbRow,
        BaseReferences<_$AppDatabase, $HighlightsTable, HighlightDbRow>,
      ),
      HighlightDbRow,
      PrefetchHooks Function()
    >;
typedef $$CollectionsTableCreateCompanionBuilder =
    CollectionsCompanion Function({
      required String id,
      required String name,
      Value<int> rowid,
    });
typedef $$CollectionsTableUpdateCompanionBuilder =
    CollectionsCompanion Function({
      Value<String> id,
      Value<String> name,
      Value<int> rowid,
    });

class $$CollectionsTableFilterComposer
    extends Composer<_$AppDatabase, $CollectionsTable> {
  $$CollectionsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get name => $composableBuilder(
    column: $table.name,
    builder: (column) => ColumnFilters(column),
  );
}

class $$CollectionsTableOrderingComposer
    extends Composer<_$AppDatabase, $CollectionsTable> {
  $$CollectionsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get name => $composableBuilder(
    column: $table.name,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$CollectionsTableAnnotationComposer
    extends Composer<_$AppDatabase, $CollectionsTable> {
  $$CollectionsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get name =>
      $composableBuilder(column: $table.name, builder: (column) => column);
}

class $$CollectionsTableTableManager
    extends
        RootTableManager<
          _$AppDatabase,
          $CollectionsTable,
          CollectionDbRow,
          $$CollectionsTableFilterComposer,
          $$CollectionsTableOrderingComposer,
          $$CollectionsTableAnnotationComposer,
          $$CollectionsTableCreateCompanionBuilder,
          $$CollectionsTableUpdateCompanionBuilder,
          (
            CollectionDbRow,
            BaseReferences<_$AppDatabase, $CollectionsTable, CollectionDbRow>,
          ),
          CollectionDbRow,
          PrefetchHooks Function()
        > {
  $$CollectionsTableTableManager(_$AppDatabase db, $CollectionsTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$CollectionsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$CollectionsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$CollectionsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<String> id = const Value.absent(),
                Value<String> name = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => CollectionsCompanion(id: id, name: name, rowid: rowid),
          createCompanionCallback:
              ({
                required String id,
                required String name,
                Value<int> rowid = const Value.absent(),
              }) =>
                  CollectionsCompanion.insert(id: id, name: name, rowid: rowid),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable<$CollectionsTable, CollectionDbRow>(table),
                  BaseReferences<
                    _$AppDatabase,
                    $CollectionsTable,
                    CollectionDbRow
                  >(db, table, e),
                ),
              )
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$CollectionsTableProcessedTableManager =
    ProcessedTableManager<
      _$AppDatabase,
      $CollectionsTable,
      CollectionDbRow,
      $$CollectionsTableFilterComposer,
      $$CollectionsTableOrderingComposer,
      $$CollectionsTableAnnotationComposer,
      $$CollectionsTableCreateCompanionBuilder,
      $$CollectionsTableUpdateCompanionBuilder,
      (
        CollectionDbRow,
        BaseReferences<_$AppDatabase, $CollectionsTable, CollectionDbRow>,
      ),
      CollectionDbRow,
      PrefetchHooks Function()
    >;
typedef $$RecentItemsTableCreateCompanionBuilder =
    RecentItemsCompanion Function({
      required String refKey,
      required String title,
      required String subtitle,
      required int kind,
      required DateTime touchedAt,
      Value<int> rowid,
    });
typedef $$RecentItemsTableUpdateCompanionBuilder =
    RecentItemsCompanion Function({
      Value<String> refKey,
      Value<String> title,
      Value<String> subtitle,
      Value<int> kind,
      Value<DateTime> touchedAt,
      Value<int> rowid,
    });

class $$RecentItemsTableFilterComposer
    extends Composer<_$AppDatabase, $RecentItemsTable> {
  $$RecentItemsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get refKey => $composableBuilder(
    column: $table.refKey,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get subtitle => $composableBuilder(
    column: $table.subtitle,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get kind => $composableBuilder(
    column: $table.kind,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<DateTime> get touchedAt => $composableBuilder(
    column: $table.touchedAt,
    builder: (column) => ColumnFilters(column),
  );
}

class $$RecentItemsTableOrderingComposer
    extends Composer<_$AppDatabase, $RecentItemsTable> {
  $$RecentItemsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get refKey => $composableBuilder(
    column: $table.refKey,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get subtitle => $composableBuilder(
    column: $table.subtitle,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get kind => $composableBuilder(
    column: $table.kind,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<DateTime> get touchedAt => $composableBuilder(
    column: $table.touchedAt,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$RecentItemsTableAnnotationComposer
    extends Composer<_$AppDatabase, $RecentItemsTable> {
  $$RecentItemsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get refKey =>
      $composableBuilder(column: $table.refKey, builder: (column) => column);

  GeneratedColumn<String> get title =>
      $composableBuilder(column: $table.title, builder: (column) => column);

  GeneratedColumn<String> get subtitle =>
      $composableBuilder(column: $table.subtitle, builder: (column) => column);

  GeneratedColumn<int> get kind =>
      $composableBuilder(column: $table.kind, builder: (column) => column);

  GeneratedColumn<DateTime> get touchedAt =>
      $composableBuilder(column: $table.touchedAt, builder: (column) => column);
}

class $$RecentItemsTableTableManager
    extends
        RootTableManager<
          _$AppDatabase,
          $RecentItemsTable,
          RecentDbRow,
          $$RecentItemsTableFilterComposer,
          $$RecentItemsTableOrderingComposer,
          $$RecentItemsTableAnnotationComposer,
          $$RecentItemsTableCreateCompanionBuilder,
          $$RecentItemsTableUpdateCompanionBuilder,
          (
            RecentDbRow,
            BaseReferences<_$AppDatabase, $RecentItemsTable, RecentDbRow>,
          ),
          RecentDbRow,
          PrefetchHooks Function()
        > {
  $$RecentItemsTableTableManager(_$AppDatabase db, $RecentItemsTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$RecentItemsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$RecentItemsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$RecentItemsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<String> refKey = const Value.absent(),
                Value<String> title = const Value.absent(),
                Value<String> subtitle = const Value.absent(),
                Value<int> kind = const Value.absent(),
                Value<DateTime> touchedAt = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => RecentItemsCompanion(
                refKey: refKey,
                title: title,
                subtitle: subtitle,
                kind: kind,
                touchedAt: touchedAt,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String refKey,
                required String title,
                required String subtitle,
                required int kind,
                required DateTime touchedAt,
                Value<int> rowid = const Value.absent(),
              }) => RecentItemsCompanion.insert(
                refKey: refKey,
                title: title,
                subtitle: subtitle,
                kind: kind,
                touchedAt: touchedAt,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable<$RecentItemsTable, RecentDbRow>(table),
                  BaseReferences<_$AppDatabase, $RecentItemsTable, RecentDbRow>(
                    db,
                    table,
                    e,
                  ),
                ),
              )
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$RecentItemsTableProcessedTableManager =
    ProcessedTableManager<
      _$AppDatabase,
      $RecentItemsTable,
      RecentDbRow,
      $$RecentItemsTableFilterComposer,
      $$RecentItemsTableOrderingComposer,
      $$RecentItemsTableAnnotationComposer,
      $$RecentItemsTableCreateCompanionBuilder,
      $$RecentItemsTableUpdateCompanionBuilder,
      (
        RecentDbRow,
        BaseReferences<_$AppDatabase, $RecentItemsTable, RecentDbRow>,
      ),
      RecentDbRow,
      PrefetchHooks Function()
    >;
typedef $$SearchDocumentsTableCreateCompanionBuilder =
    SearchDocumentsCompanion Function({
      required String id,
      required String kind,
      required String refKey,
      required String title,
      required String subtitle,
      required String body,
      required String normalizedBody,
      required String normalizedTitle,
      Value<int?> surah,
      Value<int?> ayah,
      Value<String?> editionId,
      Value<String?> tafsirId,
      Value<String?> collectionId,
      Value<String?> hadithNumber,
      Value<String?> book,
      Value<int> rowid,
    });
typedef $$SearchDocumentsTableUpdateCompanionBuilder =
    SearchDocumentsCompanion Function({
      Value<String> id,
      Value<String> kind,
      Value<String> refKey,
      Value<String> title,
      Value<String> subtitle,
      Value<String> body,
      Value<String> normalizedBody,
      Value<String> normalizedTitle,
      Value<int?> surah,
      Value<int?> ayah,
      Value<String?> editionId,
      Value<String?> tafsirId,
      Value<String?> collectionId,
      Value<String?> hadithNumber,
      Value<String?> book,
      Value<int> rowid,
    });

class $$SearchDocumentsTableFilterComposer
    extends Composer<_$AppDatabase, $SearchDocumentsTable> {
  $$SearchDocumentsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get kind => $composableBuilder(
    column: $table.kind,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get refKey => $composableBuilder(
    column: $table.refKey,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get subtitle => $composableBuilder(
    column: $table.subtitle,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get body => $composableBuilder(
    column: $table.body,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get normalizedBody => $composableBuilder(
    column: $table.normalizedBody,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get normalizedTitle => $composableBuilder(
    column: $table.normalizedTitle,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get surah => $composableBuilder(
    column: $table.surah,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get ayah => $composableBuilder(
    column: $table.ayah,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get editionId => $composableBuilder(
    column: $table.editionId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get tafsirId => $composableBuilder(
    column: $table.tafsirId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get collectionId => $composableBuilder(
    column: $table.collectionId,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get hadithNumber => $composableBuilder(
    column: $table.hadithNumber,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get book => $composableBuilder(
    column: $table.book,
    builder: (column) => ColumnFilters(column),
  );
}

class $$SearchDocumentsTableOrderingComposer
    extends Composer<_$AppDatabase, $SearchDocumentsTable> {
  $$SearchDocumentsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get kind => $composableBuilder(
    column: $table.kind,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get refKey => $composableBuilder(
    column: $table.refKey,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get title => $composableBuilder(
    column: $table.title,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get subtitle => $composableBuilder(
    column: $table.subtitle,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get body => $composableBuilder(
    column: $table.body,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get normalizedBody => $composableBuilder(
    column: $table.normalizedBody,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get normalizedTitle => $composableBuilder(
    column: $table.normalizedTitle,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get surah => $composableBuilder(
    column: $table.surah,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get ayah => $composableBuilder(
    column: $table.ayah,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get editionId => $composableBuilder(
    column: $table.editionId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get tafsirId => $composableBuilder(
    column: $table.tafsirId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get collectionId => $composableBuilder(
    column: $table.collectionId,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get hadithNumber => $composableBuilder(
    column: $table.hadithNumber,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get book => $composableBuilder(
    column: $table.book,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$SearchDocumentsTableAnnotationComposer
    extends Composer<_$AppDatabase, $SearchDocumentsTable> {
  $$SearchDocumentsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get kind =>
      $composableBuilder(column: $table.kind, builder: (column) => column);

  GeneratedColumn<String> get refKey =>
      $composableBuilder(column: $table.refKey, builder: (column) => column);

  GeneratedColumn<String> get title =>
      $composableBuilder(column: $table.title, builder: (column) => column);

  GeneratedColumn<String> get subtitle =>
      $composableBuilder(column: $table.subtitle, builder: (column) => column);

  GeneratedColumn<String> get body =>
      $composableBuilder(column: $table.body, builder: (column) => column);

  GeneratedColumn<String> get normalizedBody => $composableBuilder(
    column: $table.normalizedBody,
    builder: (column) => column,
  );

  GeneratedColumn<String> get normalizedTitle => $composableBuilder(
    column: $table.normalizedTitle,
    builder: (column) => column,
  );

  GeneratedColumn<int> get surah =>
      $composableBuilder(column: $table.surah, builder: (column) => column);

  GeneratedColumn<int> get ayah =>
      $composableBuilder(column: $table.ayah, builder: (column) => column);

  GeneratedColumn<String> get editionId =>
      $composableBuilder(column: $table.editionId, builder: (column) => column);

  GeneratedColumn<String> get tafsirId =>
      $composableBuilder(column: $table.tafsirId, builder: (column) => column);

  GeneratedColumn<String> get collectionId => $composableBuilder(
    column: $table.collectionId,
    builder: (column) => column,
  );

  GeneratedColumn<String> get hadithNumber => $composableBuilder(
    column: $table.hadithNumber,
    builder: (column) => column,
  );

  GeneratedColumn<String> get book =>
      $composableBuilder(column: $table.book, builder: (column) => column);
}

class $$SearchDocumentsTableTableManager
    extends
        RootTableManager<
          _$AppDatabase,
          $SearchDocumentsTable,
          SearchDocumentDbRow,
          $$SearchDocumentsTableFilterComposer,
          $$SearchDocumentsTableOrderingComposer,
          $$SearchDocumentsTableAnnotationComposer,
          $$SearchDocumentsTableCreateCompanionBuilder,
          $$SearchDocumentsTableUpdateCompanionBuilder,
          (
            SearchDocumentDbRow,
            BaseReferences<
              _$AppDatabase,
              $SearchDocumentsTable,
              SearchDocumentDbRow
            >,
          ),
          SearchDocumentDbRow,
          PrefetchHooks Function()
        > {
  $$SearchDocumentsTableTableManager(
    _$AppDatabase db,
    $SearchDocumentsTable table,
  ) : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$SearchDocumentsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$SearchDocumentsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$SearchDocumentsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<String> id = const Value.absent(),
                Value<String> kind = const Value.absent(),
                Value<String> refKey = const Value.absent(),
                Value<String> title = const Value.absent(),
                Value<String> subtitle = const Value.absent(),
                Value<String> body = const Value.absent(),
                Value<String> normalizedBody = const Value.absent(),
                Value<String> normalizedTitle = const Value.absent(),
                Value<int?> surah = const Value.absent(),
                Value<int?> ayah = const Value.absent(),
                Value<String?> editionId = const Value.absent(),
                Value<String?> tafsirId = const Value.absent(),
                Value<String?> collectionId = const Value.absent(),
                Value<String?> hadithNumber = const Value.absent(),
                Value<String?> book = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => SearchDocumentsCompanion(
                id: id,
                kind: kind,
                refKey: refKey,
                title: title,
                subtitle: subtitle,
                body: body,
                normalizedBody: normalizedBody,
                normalizedTitle: normalizedTitle,
                surah: surah,
                ayah: ayah,
                editionId: editionId,
                tafsirId: tafsirId,
                collectionId: collectionId,
                hadithNumber: hadithNumber,
                book: book,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String id,
                required String kind,
                required String refKey,
                required String title,
                required String subtitle,
                required String body,
                required String normalizedBody,
                required String normalizedTitle,
                Value<int?> surah = const Value.absent(),
                Value<int?> ayah = const Value.absent(),
                Value<String?> editionId = const Value.absent(),
                Value<String?> tafsirId = const Value.absent(),
                Value<String?> collectionId = const Value.absent(),
                Value<String?> hadithNumber = const Value.absent(),
                Value<String?> book = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => SearchDocumentsCompanion.insert(
                id: id,
                kind: kind,
                refKey: refKey,
                title: title,
                subtitle: subtitle,
                body: body,
                normalizedBody: normalizedBody,
                normalizedTitle: normalizedTitle,
                surah: surah,
                ayah: ayah,
                editionId: editionId,
                tafsirId: tafsirId,
                collectionId: collectionId,
                hadithNumber: hadithNumber,
                book: book,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable<$SearchDocumentsTable, SearchDocumentDbRow>(
                    table,
                  ),
                  BaseReferences<
                    _$AppDatabase,
                    $SearchDocumentsTable,
                    SearchDocumentDbRow
                  >(db, table, e),
                ),
              )
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$SearchDocumentsTableProcessedTableManager =
    ProcessedTableManager<
      _$AppDatabase,
      $SearchDocumentsTable,
      SearchDocumentDbRow,
      $$SearchDocumentsTableFilterComposer,
      $$SearchDocumentsTableOrderingComposer,
      $$SearchDocumentsTableAnnotationComposer,
      $$SearchDocumentsTableCreateCompanionBuilder,
      $$SearchDocumentsTableUpdateCompanionBuilder,
      (
        SearchDocumentDbRow,
        BaseReferences<
          _$AppDatabase,
          $SearchDocumentsTable,
          SearchDocumentDbRow
        >,
      ),
      SearchDocumentDbRow,
      PrefetchHooks Function()
    >;

class $AppDatabaseManager {
  final _$AppDatabase _db;
  $AppDatabaseManager(this._db);
  $$AppMetaTableTableManager get appMeta =>
      $$AppMetaTableTableManager(_db, _db.appMeta);
  $$BookmarksTableTableManager get bookmarks =>
      $$BookmarksTableTableManager(_db, _db.bookmarks);
  $$NotesTableTableManager get notes =>
      $$NotesTableTableManager(_db, _db.notes);
  $$HighlightsTableTableManager get highlights =>
      $$HighlightsTableTableManager(_db, _db.highlights);
  $$CollectionsTableTableManager get collections =>
      $$CollectionsTableTableManager(_db, _db.collections);
  $$RecentItemsTableTableManager get recentItems =>
      $$RecentItemsTableTableManager(_db, _db.recentItems);
  $$SearchDocumentsTableTableManager get searchDocuments =>
      $$SearchDocumentsTableTableManager(_db, _db.searchDocuments);
}
