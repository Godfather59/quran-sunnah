package com.godfather59.quransunnah.db

import app.cash.sqldelight.db.QueryResult
import com.godfather59.quransunnah.hadith.Hadith
import com.godfather59.quransunnah.search.normalizeForIndex
import com.godfather59.quransunnah.search.tokenizeQuery
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Mirrors SearchIndexService.ftsQuery: `tok* AND tok*` prefix matching. */
fun ftsQuery(raw: String): String =
    tokenizeQuery(raw).joinToString(" AND ") { "$it*" }

// Search index over SQLDelight + FTS5. Mirrors the Dart search_index +
// app_database searchIndex (external-content FTS5, unicode61, bm25).
// Ranking for UI stays in SearchEngine; this store is the persisted,
// fingerprint-gated document layer (Phase 2), queried here for parity.

data class SearchDocument(
    val id: String,
    val kind: String, // "quran" | "hadith" | "tafsir"
    val refKey: String,
    val title: String,
    val subtitle: String,
    val body: String,
    val surah: Long? = null,
    val ayah: Long? = null,
    val editionId: String? = null,
    val tafsirId: String? = null,
    val collectionId: String? = null,
    val hadithNumber: String? = null,
    val book: String? = null,
    val hadith: Hadith? = null,
    val normalizedBody: String? = null,
    val normalizedTitle: String? = null,
)

data class FtsHit(
    val id: String,
    val kind: String,
    val refKey: String,
    val title: String,
    val subtitle: String,
    val body: String,
    val surah: Long?,
    val ayah: Long?,
    val editionId: String?,
    val tafsirId: String?,
    val collectionId: String?,
    val hadithNumber: String?,
    val book: String?,
    val score: Double?,
    val hadith: Hadith? = null,
)

private fun encodeHadith(hadith: Hadith): String = buildJsonObject {
    put("id", hadith.id)
    put("collectionId", hadith.collectionId)
    put("book", hadith.book)
    put("bookAr", hadith.bookAr)
    put("chapter", hadith.chapter)
    put("chapterAr", hadith.chapterAr)
    put("hadithNumber", hadith.hadithNumber)
    put("matnAr", hadith.matnAr)
    putNullable("matnTranslation", hadith.matnTranslation)
    putNullable("sanadAr", hadith.sanadAr)
    putNullable("narrator", hadith.narrator)
    putNullable("grade", hadith.grade)
    putNullable("gradingAuthority", hadith.gradingAuthority)
    put("topics", buildJsonArray { hadith.topics.forEach { add(JsonPrimitive(it)) } })
    put("parallelIds", buildJsonArray { hadith.parallelIds.forEach { add(JsonPrimitive(it)) } })
    put("isPlaceholder", hadith.isPlaceholder)
}.toString()

private fun kotlinx.serialization.json.JsonObjectBuilder.putNullable(key: String, value: String?) {
    put(key, value?.let(::JsonPrimitive) ?: JsonNull)
}

private fun decodeHadith(raw: String?): Hadith? {
    if (raw == null) return null
    return try {
    val obj = Json.parseToJsonElement(raw).jsonObject
    fun text(key: String): String = obj[key]?.jsonPrimitive?.contentOrNull.orEmpty()
    fun optional(key: String): String? = obj[key]?.let {
        if (it == JsonNull) null else it.jsonPrimitive.contentOrNull
    }
    fun strings(key: String): List<String> =
        obj[key]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }.orEmpty()
    Hadith(
        id = text("id"),
        collectionId = text("collectionId"),
        book = text("book"),
        bookAr = text("bookAr"),
        chapter = text("chapter"),
        chapterAr = text("chapterAr"),
        hadithNumber = text("hadithNumber"),
        matnAr = text("matnAr"),
        matnTranslation = optional("matnTranslation"),
        sanadAr = optional("sanadAr"),
        narrator = optional("narrator"),
        grade = optional("grade"),
        gradingAuthority = optional("gradingAuthority"),
        topics = strings("topics"),
        parallelIds = strings("parallelIds"),
        isPlaceholder = obj["isPlaceholder"]?.jsonPrimitive?.contentOrNull?.toBoolean() ?: false,
    )
} catch (_: Exception) {
    null
}
}

data class FtsQuery(
    val kind: String,
    val fts: String,
    val editionId: String? = null,
    val tafsirId: String? = null,
    val collectionIds: Set<String> = emptySet(),
    val book: String? = null,
    val hadithNumber: String? = null,
    val surah: Long? = null,
    val limit: Long = 50,
)

class SearchIndexStore(handle: SearchDatabase) {
    private val db = handle.db
    private val driver = handle.driver
    private val q get() = db.searchQueries

    fun clear(kind: String, editionId: String? = null, tafsirId: String? = null) {
        if (kind == "hadith" && editionId == null && tafsirId == null) {
            driver.execute(
                null,
                "DELETE FROM search_hadith_details WHERE id IN " +
                    "(SELECT id FROM search_documents WHERE kind = ?)",
                1,
            ) { bindString(0, kind) }
        }
        when {
            editionId != null -> driver.execute(
                null,
                "DELETE FROM search_documents WHERE kind = ? AND edition_id = ?",
                2,
            ) {
                bindString(0, kind)
                bindString(1, editionId)
            }
            tafsirId != null -> driver.execute(
                null,
                "DELETE FROM search_documents WHERE kind = ? AND tafsir_id = ?",
                2,
            ) {
                bindString(0, kind)
                bindString(1, tafsirId)
            }
            else -> driver.execute(
                null,
                "DELETE FROM search_documents WHERE kind = ?",
                1,
            ) {
                bindString(0, kind)
            }
        }
    }

    fun insert(documents: Iterable<SearchDocument>) {
        db.transaction {
            for (d in documents) {
                driver.execute(
                    null,
                    "INSERT INTO search_documents(id, kind, ref_key, title, " +
                        "subtitle, body, normalized_body, normalized_title, " +
                        "surah, ayah, edition_id, tafsir_id, collection_id, " +
                        "hadith_number, book) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    15,
                ) {
                    var i = 0
                    bindString(i++, d.id)
                    bindString(i++, d.kind)
                    bindString(i++, d.refKey)
                    bindString(i++, d.title)
                    bindString(i++, d.subtitle)
                    bindString(i++, d.body)
                    bindString(i++, normalizeForIndex(d.body))
                    bindString(i++, normalizeForIndex(d.title))
                    bindLong(i++, d.surah)
                    bindLong(i++, d.ayah)
                    bindString(i++, d.editionId)
                    bindString(i++, d.tafsirId)
                    bindString(i++, d.collectionId)
                    bindString(i++, d.hadithNumber)
                    bindString(i++, d.book)
                }
                d.hadith?.let { hadith ->
                    driver.execute(
                        null,
                        "INSERT OR REPLACE INTO search_hadith_details(id, payload) VALUES (?, ?)",
                        2,
                    ) {
                        bindString(0, d.id)
                        bindString(1, encodeHadith(hadith))
                    }
                }
            }
        }
    }

    /** Loads the persisted document cache for rehydrating the in-memory matcher. */
    fun documents(kind: String? = null): List<SearchDocument> {
        val rows = mutableListOf<SearchDocument>()
        val sql = "SELECT d.id, d.kind, d.ref_key, d.title, d.subtitle, d.body, " +
            "d.surah, d.ayah, d.edition_id, d.tafsir_id, d.collection_id, " +
            "d.hadith_number, d.book, d.normalized_body, d.normalized_title, " +
            "h.payload FROM search_documents d " +
            "LEFT JOIN search_hadith_details h ON h.id = d.id " +
            (if (kind == null) "" else "WHERE d.kind = ? ") +
            "ORDER BY d.kind, d.id"
        driver.executeQuery<Unit>(
            identifier = null,
            sql = sql,
            mapper = { cursor ->
                while (cursor.next().value) {
                    rows.add(
                        SearchDocument(
                            id = cursor.getString(0)!!,
                            kind = cursor.getString(1)!!,
                            refKey = cursor.getString(2)!!,
                            title = cursor.getString(3)!!,
                            subtitle = cursor.getString(4)!!,
                            body = cursor.getString(5)!!,
                            surah = cursor.getLong(6),
                            ayah = cursor.getLong(7),
                            editionId = cursor.getString(8),
                            tafsirId = cursor.getString(9),
                            collectionId = cursor.getString(10),
                            hadithNumber = cursor.getString(11),
                            book = cursor.getString(12),
                            normalizedBody = cursor.getString(13),
                            normalizedTitle = cursor.getString(14),
                            hadith = decodeHadith(cursor.getString(15)),
                        ),
                    )
                }
                QueryResult.Value(Unit)
            },
            parameters = if (kind == null) 0 else 1,
            binders = if (kind == null) null else ({ bindString(0, kind) }),
        )
        return rows
    }

    fun count(kind: String? = null): Long {
        var result = 0L
        val advance: (app.cash.sqldelight.db.SqlCursor) -> QueryResult<Unit> = { cursor ->
            while (cursor.next().value) {
                result = cursor.getLong(0) ?: 0L
            }
            QueryResult.Value(Unit)
        }
        if (kind == null) {
            driver.executeQuery(
                identifier = null,
                sql = "SELECT COUNT(*) FROM search_documents",
                mapper = advance,
                parameters = 0,
                binders = null,
            )
        } else {
            driver.executeQuery(
                identifier = null,
                sql = "SELECT COUNT(*) FROM search_documents WHERE kind = ?",
                mapper = advance,
                parameters = 1,
            ) {
                bindString(0, kind)
            }
        }
        return result
    }

    fun getFingerprint(metaKey: String): String? =
        q.getMeta(metaKey).executeAsOneOrNull()

    private fun setFingerprint(metaKey: String, fingerprint: String) {
        if (q.getMeta(metaKey).executeAsOneOrNull() == null) {
            q.insertMeta(metaKey, fingerprint)
        } else {
            q.updateMetaValue(fingerprint, metaKey)
        }
    }

    /**
     * Rebuilds the index only when [fingerprint] differs from the stored
     * one (mirrors Dart _ensure* fingerprint gates). Returns true when a
     * rebuild ran.
     */
    fun ensureFresh(
        metaKey: String,
        fingerprint: String,
        rebuild: () -> Unit,
    ): Boolean {
        if (q.getMeta(metaKey).executeAsOneOrNull() == fingerprint) {
            return false
        }
        rebuild()
        setFingerprint(metaKey, fingerprint)
        return true
    }

    /** Replace one category and publish its fingerprint only after all rows are written. */
    fun replaceIndex(
        metaKey: String,
        fingerprint: String,
        kind: String,
        documents: List<SearchDocument>,
    ) {
        driver.execute(null, "DELETE FROM app_meta WHERE key = ?", 1) {
            bindString(0, metaKey)
        }
        clear(kind)
        insert(documents)
        setFingerprint(metaKey, fingerprint)
    }

    /**
     * FTS5 MATCH query with bm25 ranking (mirrors Dart customSelect). Raw
     * SQL rather than codegen: codegen would resolve search_fts at compile
     * time and force it into Schema.create, which must never fail.
     */
    fun search(query: FtsQuery): List<FtsHit> {
        // Empty strings mean "no filter" (Dart checks isNullOrEmpty too).
        val book = query.book?.ifEmpty { null }
        val number = query.hadithNumber?.ifEmpty { null }
        val collectionFilter = if (query.collectionIds.isNotEmpty()) {
            "AND d.collection_id IN (${query.collectionIds.joinToString(",") { "?" }}) "
        } else {
            ""
        }
        val sql = "SELECT d.id, d.kind, d.ref_key, d.title, d.subtitle, " +
            "d.body, d.surah, d.ayah, d.edition_id, d.tafsir_id, " +
            "d.collection_id, d.hadith_number, d.book, bm25(search_fts), h.payload " +
            "FROM search_fts " +
            "JOIN search_documents d ON d.rowid = search_fts.rowid " +
            "LEFT JOIN search_hadith_details h ON h.id = d.id " +
            "WHERE search_fts MATCH ? " +
            "AND d.kind = ? " +
            collectionFilter +
            "AND (? IS NULL OR d.edition_id = ?) " +
            "AND (? IS NULL OR d.tafsir_id = ?) " +
            "AND (? IS NULL OR d.book = ?) " +
            "AND (? IS NULL OR d.hadith_number = ?) " +
            "AND (? IS NULL OR d.surah = ?) " +
            "ORDER BY bm25(search_fts), d.rowid " +
            "LIMIT ?"
        var parameterIndex = 0
        val rows = mutableListOf<FtsHit>()
        driver.executeQuery<Unit>(
            identifier = null,
            sql = sql,
            mapper = { cursor ->
                while (cursor.next().value) {
                    rows.add(
                        FtsHit(
                            id = cursor.getString(0)!!,
                            kind = cursor.getString(1)!!,
                            refKey = cursor.getString(2)!!,
                            title = cursor.getString(3)!!,
                            subtitle = cursor.getString(4)!!,
                            body = cursor.getString(5)!!,
                            surah = cursor.getLong(6),
                            ayah = cursor.getLong(7),
                            editionId = cursor.getString(8),
                            tafsirId = cursor.getString(9),
                            collectionId = cursor.getString(10),
                            hadithNumber = cursor.getString(11),
                            book = cursor.getString(12),
                            score = cursor.getDouble(13),
                            hadith = decodeHadith(cursor.getString(14)),
                        ),
                    )
                }
                QueryResult.Value(Unit)
            },
            parameters = 13 + query.collectionIds.size,
        ) {
            bindString(parameterIndex++, query.fts)
            bindString(parameterIndex++, query.kind)
            for (collectionId in query.collectionIds) {
                bindString(parameterIndex++, collectionId)
            }
            bindString(parameterIndex++, query.editionId)
            bindString(parameterIndex++, query.editionId)
            bindString(parameterIndex++, query.tafsirId)
            bindString(parameterIndex++, query.tafsirId)
            bindString(parameterIndex++, book)
            bindString(parameterIndex++, book)
            bindString(parameterIndex++, number)
            bindString(parameterIndex++, number)
            // Surah binds as Long to match the INTEGER column.
            bindLong(parameterIndex++, query.surah)
            bindLong(parameterIndex++, query.surah)
            bindLong(parameterIndex++, query.limit)
        }
        return rows
    }
}
