package com.godfather59.quransunnah.db

import com.godfather59.quransunnah.library.Bookmark
import com.godfather59.quransunnah.library.BookmarkKind
import com.godfather59.quransunnah.library.CustomCollection
import com.godfather59.quransunnah.library.Highlight
import com.godfather59.quransunnah.library.RecentItem
import com.godfather59.quransunnah.library.UserNote
import com.godfather59.quransunnah.library.defaultCollections
import com.godfather59.quransunnah.util.currentEpochSeconds
import com.godfather59.quransunnah.util.epochSecondsToIsoUtc
import com.godfather59.quransunnah.util.isoToEpochSeconds
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

// Personal-library store over SQLDelight. Mirrors
// lib/data/database/library_store.dart 1:1 (SQL, guards, id shapes).
// BookmarkKind is stored as its Kotlin ordinal, which matches Dart
// BookmarkKind.index (AYAH=0, HADITH=1, TAFSIR=2) — keep in sync.

private fun kindToInt(kind: BookmarkKind): Long = when (kind) {
    BookmarkKind.AYAH -> 0L
    BookmarkKind.HADITH -> 1L
    BookmarkKind.TAFSIR -> 2L
}

private fun kindFromInt(value: Long): BookmarkKind? = when (value) {
    0L -> BookmarkKind.AYAH
    1L -> BookmarkKind.HADITH
    2L -> BookmarkKind.TAFSIR
    else -> null
}

class LibraryStore(private val handle: LibraryDatabase) {
    private val db get() = handle.db
    private val q get() = db.libraryQueries
    private var idSeq = 0L

    private fun freshId(prefix: String, key: String): String {
        val id = "$prefix-$key-${currentEpochSeconds()}-${idSeq++}"
        return id
    }

    suspend fun bookmarks(): List<Bookmark> =
        q.selectBookmarks().executeAsList().mapNotNull { r ->
            val kind = kindFromInt(r.kind) ?: return@mapNotNull null
            Bookmark(
                id = r.id,
                kind = kind,
                refKey = r.ref_key,
                title = r.title,
                subtitle = r.subtitle,
                collectionId = r.collection_id,
                createdAtSeconds = r.created_at,
            )
        }

    suspend fun toggleAyah(surah: Int, ayah: Int) {
        val key = "$surah:$ayah"
        if (q.countBookmarkByRef(key).executeAsOne() > 0) {
            q.deleteBookmarkByRef(key)
            return
        }
        val now = currentEpochSeconds()
        q.insertBookmark(
            freshId("bm", key), 0L, key,
            "Surah $surah · Ayah $ayah", "Quran bookmark", null, now,
        )
    }

    suspend fun setAyahCollection(surah: Int, ayah: Int, collectionId: String?) {
        val key = "$surah:$ayah"
        if (q.countBookmarkByRef(key).executeAsOne() == 0L) {
            val now = currentEpochSeconds()
            q.insertBookmark(
                freshId("bm", key), 0L, key,
                "Surah $surah · Ayah $ayah", "Quran bookmark", collectionId, now,
            )
            return
        }
        q.updateBookmarkCollection(collectionId, key)
    }

    suspend fun toggleHadith(id: String, title: String) {
        if (q.countBookmarkByRef(id).executeAsOne() > 0) {
            q.deleteBookmarkByRef(id)
            return
        }
        val now = currentEpochSeconds()
        q.insertBookmark(
            freshId("bm", id), 1L, id, title, "Hadith bookmark", null, now,
        )
    }

    suspend fun removeBookmark(id: String) {
        q.deleteBookmarkById(id)
    }

    suspend fun isBookmarked(refKey: String): Boolean =
        q.countBookmarkByRef(refKey).executeAsOne() > 0

    suspend fun notes(): List<UserNote> =
        q.selectNotes().executeAsList().map { r ->
            UserNote(
                id = r.id,
                refKey = r.ref_key,
                text = r.text_value,
                createdAtSeconds = r.created_at,
            )
        }

    suspend fun noteForRef(refKey: String): UserNote? =
        notes().firstOrNull { it.refKey == refKey }

    suspend fun upsertNote(refKey: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            q.deleteNoteByRef(refKey)
            return
        }
        val existing = q.noteIdByRef(refKey).executeAsOneOrNull()
        if (existing == null) {
            q.insertNote(
                freshId("note", refKey), refKey, trimmed,
                currentEpochSeconds(),
            )
        } else {
            q.updateNoteText(trimmed, refKey)
        }
    }

    suspend fun removeNote(id: String) {
        q.deleteNoteById(id)
    }

    suspend fun highlights(): List<Highlight> =
        q.selectHighlights().executeAsList().map { r ->
            Highlight(id = r.id, refKey = r.ref_key, colorValue = r.color_value)
        }

    suspend fun highlightForRef(refKey: String): Highlight? =
        highlights().firstOrNull { it.refKey == refKey }

    suspend fun toggleHighlight(refKey: String, colorValue: Long) {
        val current = q.highlightColorByRef(refKey).executeAsOneOrNull()
        if (current != null && current == colorValue) {
            q.deleteHighlightByRef(refKey)
            return
        }
        if (current == null) {
            q.insertHighlight("hl-$refKey", refKey, colorValue)
        } else {
            // In-place recolor (preserves row order, like Dart ON CONFLICT).
            q.updateHighlightColor(colorValue, refKey)
        }
    }

    suspend fun removeHighlight(id: String) {
        q.deleteHighlightById(id)
    }

    suspend fun collections(): List<CustomCollection> =
        q.selectCollections().executeAsList().map { r ->
            CustomCollection(id = r.id, name = r.name)
        }

    suspend fun addCollection(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        q.insertCollection(freshId("c", trimmed), trimmed)
    }

    suspend fun renameCollection(id: String, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        q.renameCollection(trimmed, id)
    }

    suspend fun removeCollection(id: String) {
        db.transaction {
            q.detachCollectionBookmarks(id)
            q.deleteCollection(id)
        }
    }

    suspend fun recent(): List<RecentItem> =
        q.selectRecent().executeAsList().mapNotNull { r ->
            val kind = kindFromInt(r.kind) ?: return@mapNotNull null
            RecentItem(
                refKey = r.ref_key,
                title = r.title,
                subtitle = r.subtitle,
                kind = kind,
            )
        }

    suspend fun touchRecent(item: RecentItem) {
        val now = currentEpochSeconds()
        if (q.countRecentByRef(item.refKey).executeAsOne() == 0L) {
            q.insertRecentRow(
                item.refKey, item.title, item.subtitle,
                kindToInt(item.kind), now,
            )
        } else {
            q.updateRecentRow(
                item.title, item.subtitle,
                kindToInt(item.kind), now, item.refKey,
            )
        }
        q.pruneRecent()
    }

    suspend fun ensureDefaults() {
        if (q.countCollections().executeAsOne() != 0L) return
        db.transaction {
            for (c in defaultCollections) {
                q.insertCollection(c.id, c.name)
            }
        }
    }

    suspend fun getMeta(key: String): String? =
        q.getMeta(key).executeAsOneOrNull()

    suspend fun setMeta(key: String, value: String) {
        if (q.getMeta(key).executeAsOneOrNull() == null) {
            q.insertMeta(key, value)
        } else {
            q.updateMetaValue(value, key)
        }
    }

    // -- Versioned JSON backup (format "quran-sunnah-library", version 1) --

    suspend fun backup(): String {
        // Hoisted: builders below are not suspend lambdas.
        val bookmarkRows = bookmarks()
        val noteRows = notes()
        val highlightRows = highlights()
        val collectionRows = collections()
        val recentRows = recent()
        val obj = buildJsonObject {
            put("format", "quran-sunnah-library")
            put("version", 1)
            put("exportedAt", epochSecondsToIsoUtc(currentEpochSeconds()))
            putJsonArray("bookmarks") {
                for (b in bookmarkRows) {
                    add(buildJsonObject {
                        put("id", b.id)
                        put("kind", kindToInt(b.kind).toInt())
                        put("refKey", b.refKey)
                        put("title", b.title)
                        put("subtitle", b.subtitle)
                        if (b.collectionId == null) {
                            put("collectionId", JsonNull)
                        } else {
                            put("collectionId", b.collectionId)
                        }
                        val created = b.createdAtSeconds
                        if (created == null) {
                            put("createdAt", JsonNull)
                        } else {
                            put("createdAt", epochSecondsToIsoUtc(created))
                        }
                    })
                }
            }
            putJsonArray("notes") {
                for (n in noteRows) {
                    add(buildJsonObject {
                        put("id", n.id)
                        put("refKey", n.refKey)
                        put("text", n.text)
                        val created = n.createdAtSeconds
                        if (created == null) {
                            put("createdAt", JsonNull)
                        } else {
                            put("createdAt", epochSecondsToIsoUtc(created))
                        }
                    })
                }
            }
            putJsonArray("highlights") {
                for (h in highlightRows) {
                    add(buildJsonObject {
                        put("id", h.id)
                        put("refKey", h.refKey)
                        put("colorValue", h.colorValue)
                    })
                }
            }
            putJsonArray("collections") {
                for (c in collectionRows) {
                    add(buildJsonObject {
                        put("id", c.id)
                        put("name", c.name)
                    })
                }
            }
            putJsonArray("recent") {
                for (r in recentRows) {
                    add(buildJsonObject {
                        put("refKey", r.refKey)
                        put("title", r.title)
                        put("subtitle", r.subtitle)
                        put("kind", kindToInt(r.kind).toInt())
                    })
                }
            }
        }
        return obj.toString()
    }

    suspend fun restoreBackup(raw: String) {
        // Lenient per-row guards: one bad row never fails the restore.
        // 2 MB cap mirrors the Android picker guard; larger payloads are
        // rejected before they can OOM the process.
        if (raw.length > 2 * 1024 * 1024) {
            throw IllegalArgumentException("Library backup too large.")
        }
        val parsed = try {
            Json.parseToJsonElement(raw).jsonObject
        } catch (_: Exception) {
            throw IllegalArgumentException("Invalid library backup JSON.")
        }
        if (parsed["format"]?.jsonPrimitive?.content != "quran-sunnah-library" ||
            parsed["version"]?.jsonPrimitive?.content != "1"
        ) {
            throw IllegalArgumentException("Unsupported library backup format.")
        }
        fun rows(key: String): List<Map<String, String?>> {
            val list = parsed[key]?.jsonArray ?: return emptyList()
            return list.mapNotNull { element ->
                try {
                    element.jsonObject.mapValues { (_, v) ->
                        try {
                            v.jsonPrimitive.content
                        } catch (_: Exception) {
                            null
                        }
                    }
                } catch (_: Exception) {
                    null
                }
            }
        }

        val collectionRows = rows("collections")
        val bookmarkRows = rows("bookmarks")
        val noteRows = rows("notes")
        val highlightRows = rows("highlights")
        val recentRows = rows("recent")

        db.transaction {
            q.clearAllBookmarks()
            q.clearAllNotes()
            q.clearAllHighlights()
            q.clearAllRecent()
            q.clearAllCollections()

            for (row in collectionRows) {
                val id = row["id"].orEmpty()
                val name = (row["name"] ?: "").trim()
                if (id.isEmpty() || name.isEmpty()) continue
                try {
                    q.insertCollection(id, name)
                } catch (_: Exception) {
                    continue
                }
            }

            for (row in bookmarkRows) {
                val id = row["id"].orEmpty()
                val refKey = row["refKey"].orEmpty()
                val kind = row["kind"]?.toLongOrNull()?.let(::kindFromInt)
                if (id.isEmpty() || refKey.isEmpty() || kind == null) continue
                val collectionId = row["collectionId"]?.takeIf { it.isNotEmpty() }
                val knownCollection = collectionId == null ||
                    collectionRows.any { it["id"] == collectionId }
                val created = row["createdAt"]?.let(::isoToEpochSeconds)
                    ?: currentEpochSeconds()
                try {
                    q.insertBookmark(
                        id, kindToInt(kind), refKey,
                        row["title"] ?: "", row["subtitle"] ?: "",
                        if (knownCollection) collectionId else null, created,
                    )
                } catch (_: Exception) {
                    // Duplicate id or ref_key (UNIQUE): skip the row, keep restoring.
                    continue
                }
            }

            for (row in noteRows) {
                val id = row["id"].orEmpty()
                val refKey = row["refKey"].orEmpty()
                val text = (row["text"] ?: "").trim()
                if (id.isEmpty() || refKey.isEmpty() || text.isEmpty()) continue
                val created = row["createdAt"]?.let(::isoToEpochSeconds)
                    ?: currentEpochSeconds()
                try {
                    q.insertNote(id, refKey, text, created)
                } catch (_: Exception) {
                    continue
                }
            }

            for (row in highlightRows) {
                val id = row["id"].orEmpty()
                val refKey = row["refKey"].orEmpty()
                val color = row["colorValue"]?.toLongOrNull()
                if (id.isEmpty() || refKey.isEmpty() || color == null) continue
                try {
                    q.insertHighlight(id, refKey, color)
                } catch (_: Exception) {
                    continue
                }
            }

            var offset = 0L
            for (row in recentRows.take(50)) {
                val refKey = row["refKey"].orEmpty()
                val kind = row["kind"]?.toLongOrNull()?.let(::kindFromInt)
                if (refKey.isEmpty() || kind == null) continue
                // Tables were just cleared, so plain inserts suffice.
                try {
                    q.insertRecentRow(
                        refKey, row["title"] ?: "", row["subtitle"] ?: "",
                        kindToInt(kind), currentEpochSeconds() - offset++,
                    )
                } catch (_: Exception) {
                    continue
                }
            }
        }

        ensureDefaults()
    }
}
