package com.godfather59.quransunnah

import com.godfather59.quransunnah.db.LibraryStore
import com.godfather59.quransunnah.db.createJvmMemoryDatabase
import com.godfather59.quransunnah.library.BookmarkKind
import com.godfather59.quransunnah.library.RecentItem
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Mirrors test/library_test.dart behavior on the KMP store.
class LibraryStoreJvmTest {
    private fun store(): LibraryStore =
        LibraryStore(createJvmMemoryDatabase())

    @Test
    fun notesUpsertTrimsEditsAndEmptyDeletes(): Unit = runBlocking {
        val s = store()
        s.upsertNote("2:255", "  my note  ")
        assertEquals("my note", s.notes().single().text)
        s.upsertNote("2:255", "edited")
        assertEquals("edited", s.notes().single().text)
        s.upsertNote("2:255", "   ")
        assertTrue(s.notes().isEmpty())
    }

    @Test
    fun highlightsToggleAndRecolor(): Unit = runBlocking {
        val s = store()
        s.toggleHighlight("2:255", 0xFFFFD54FL)
        assertEquals(1, s.highlights().size)
        // Same color toggles off.
        s.toggleHighlight("2:255", 0xFFFFD54FL)
        assertTrue(s.highlights().isEmpty())
        // Different color replaces.
        s.toggleHighlight("2:255", 0xFFFFD54FL)
        s.toggleHighlight("2:255", 0xFFA5D6A7L)
        assertEquals(0xFFA5D6A7L, s.highlights().single().colorValue)
        s.removeHighlight(s.highlights().single().id)
        assertTrue(s.highlights().isEmpty())
    }

    @Test
    fun bookmarksToggleAndRemove(): Unit = runBlocking {
        val s = store()
        assertTrue(!s.isBookmarked("2:255"))
        s.toggleAyah(2, 255)
        assertTrue(s.isBookmarked("2:255"))
        assertEquals("2:255", s.bookmarks().single().refKey)
        assertEquals(BookmarkKind.AYAH, s.bookmarks().single().kind)
        s.toggleHadith("bukhari:1", "Hadith 1")
        assertEquals(2, s.bookmarks().size)
        s.toggleAyah(2, 255)
        assertEquals(listOf("bukhari:1"), s.bookmarks().map { it.refKey })
        s.removeBookmark(s.bookmarks().single().id)
        assertTrue(s.bookmarks().isEmpty())
    }

    @Test
    fun collectionsSurviveAndDetachOnRemove(): Unit = runBlocking {
        val s = store()
        s.addCollection("  ")
        assertTrue(s.collections().isEmpty())
        s.addCollection("Mine")
        val id = s.collections().single().id
        s.renameCollection(id, "Renamed")
        assertEquals("Renamed", s.collections().single().name)
        s.renameCollection(id, "  ")
        assertEquals("Renamed", s.collections().single().name)
        s.toggleAyah(1, 1)
        s.setAyahCollection(1, 1, id)
        assertEquals(id, s.bookmarks().single().collectionId)
        s.removeCollection(id)
        assertTrue(s.collections().isEmpty())
        // Bookmark survives, detached.
        assertEquals(1, s.bookmarks().size)
        assertNull(s.bookmarks().single().collectionId)
    }

    @Test
    fun recentTouchUpsertsSameKey(): Unit = runBlocking {
        val s = store()
        val item = RecentItem(
            refKey = "1:1", title = "t", subtitle = "s",
            kind = BookmarkKind.AYAH,
        )
        s.touchRecent(item)
        s.touchRecent(item)
        assertEquals(1, s.recent().size)
        assertEquals("1:1", s.recent().single().refKey)
    }

    @Test
    fun recentCapsAtFiftyOldestEvicted(): Unit = runBlocking {
        // Restore stamps rows with distinct now-offset seconds, so the
        // 50-cap eviction order is deterministic (rapid touchRecent calls
        // share one epoch second, where SQL order ties are undefined).
        val s = store()
        val rows = (1..55).joinToString(",") { i ->
            """{"refKey":"1:$i","title":"t","subtitle":"s","kind":0}"""
        }
        s.restoreBackup(
            """{"format":"quran-sunnah-library","version":1,"recent":[$rows]}""",
        )
        val recent = s.recent()
        assertEquals(50, recent.size)
        assertEquals("1:1", recent.first().refKey)
        assertEquals("1:50", recent.last().refKey)
        assertTrue(recent.none { it.refKey == "1:51" })
        assertTrue(recent.none { it.refKey == "1:55" })
    }

    @Test
    fun defaultsEnsuredOnce(): Unit = runBlocking {
        val s = store()
        s.ensureDefaults()
        s.ensureDefaults()
        assertEquals(3, s.collections().size)
    }

    @Test
    fun metaRoundTrip(): Unit = runBlocking {
        val s = store()
        assertNull(s.getMeta("k"))
        s.setMeta("k", "v")
        assertEquals("v", s.getMeta("k"))
        s.setMeta("k", "v2")
        assertEquals("v2", s.getMeta("k"))
    }

    @Test
    fun backupRoundTripIntoCleanDb(): Unit = runBlocking {
        val s = store()
        s.ensureDefaults()
        s.toggleAyah(2, 255)
        s.upsertNote("2:255", "note")
        s.toggleHighlight("2:255", 0xFFFFD54FL)
        s.touchRecent(
            RecentItem(
                refKey = "2:255", title = "t", subtitle = "s",
                kind = BookmarkKind.AYAH,
            ),
        )
        val json = s.backup()

        val clean = store()
        clean.restoreBackup(json)
        assertEquals(1, clean.bookmarks().size)
        assertEquals("2:255", clean.bookmarks().single().refKey)
        assertEquals("note", clean.notes().single().text)
        assertEquals(0xFFFFD54FL, clean.highlights().single().colorValue)
        assertEquals(3, clean.collections().size)
        assertEquals("2:255", clean.recent().single().refKey)
    }

    @Test
    fun invalidBackupsThrow(): Unit = runBlocking {
        val s = store()
        assertFailsWith<IllegalArgumentException> {
            s.restoreBackup("not json")
        }
        assertFailsWith<IllegalArgumentException> {
            s.restoreBackup("""{"format":"nope","version":1}""")
        }
        assertFailsWith<IllegalArgumentException> {
            s.restoreBackup("""{"format":"quran-sunnah-library","version":2}""")
        }
        // Nothing was wiped by the failed restores.
        s.toggleAyah(1, 1)
        assertFailsWith<IllegalArgumentException> {
            s.restoreBackup("""{"format":"nope","version":1}""")
        }
        assertEquals(1, s.bookmarks().size)
    }

    @Test
    fun garbageRowsAreSkipped(): Unit = runBlocking {
        val s = store()
        s.restoreBackup(
            """
            {"format":"quran-sunnah-library","version":1,
             "bookmarks":[
               {"id":"","refKey":"","kind":-1},
               {"id":"b1","refKey":"1:1","kind":0,"title":"t","subtitle":"s"},
               {"id":"b2","refKey":"1:2","kind":99,"title":"t","subtitle":"s"},
               {"id":"b3","refKey":"1:3","kind":1,"title":"t","subtitle":"s","collectionId":"ghost"}
             ],
             "notes":[
               {"id":"n1","refKey":"1:1","text":"  "},
               {"id":"n2","refKey":"1:1","text":"ok"}
             ],
             "highlights":[
               {"id":"","refKey":"","colorValue":"x"},
               {"id":"h1","refKey":"1:1","colorValue":7}
             ],
             "collections":[{"id":"","name":""},{"id":"c1","name":"C"}],
             "recent":[
               {"refKey":"","kind":0},
               {"refKey":"1:1","kind":0,"title":"t","subtitle":"s"}
             ]}
            """.trimIndent(),
        )
        // b1 ok; b2 bad kind; b3 unknown collection -> detached but kept.
        assertEquals(2, s.bookmarks().size)
        val b3 = s.bookmarks().first { it.id == "b3" }
        assertNull(b3.collectionId)
        assertEquals("ok", s.notes().single().text)
        assertEquals(1, s.highlights().size)
        assertEquals(listOf("c1"), s.collections().map { it.id })
        assertEquals("1:1", s.recent().single().refKey)
        // Defaults are NOT added when user collections exist.
        assertEquals(1, s.collections().size)
    }

    @Test
    fun backupJsonShape(): Unit = runBlocking {
        val s = store()
        s.toggleAyah(2, 255)
        val json = s.backup()
        assertTrue(json.contains("\"format\":\"quran-sunnah-library\""))
        assertTrue(json.contains("\"version\":1"))
        assertTrue(json.contains("\"refKey\":\"2:255\""))
        assertNotNull(s.bookmarks().single().createdAtSeconds)
    }
}
