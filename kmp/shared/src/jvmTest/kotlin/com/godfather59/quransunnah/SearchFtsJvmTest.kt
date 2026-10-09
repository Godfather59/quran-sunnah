package com.godfather59.quransunnah

import com.godfather59.quransunnah.db.FtsQuery
import com.godfather59.quransunnah.db.SearchDocument
import com.godfather59.quransunnah.db.SearchIndexStore
import com.godfather59.quransunnah.db.createJvmMemoryDatabase
import com.godfather59.quransunnah.db.ftsQuery
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchFtsJvmTest {
    private fun store() = SearchIndexStore(createJvmMemoryDatabase())

    private fun doc(
        id: String,
        body: String,
        kind: String = "quran",
        editionId: String? = "hafs-an-asim__uthmani",
    ) = SearchDocument(
        id = id, kind = kind, refKey = id, title = "T $id",
        subtitle = "S", body = body, editionId = editionId,
    )

    @Test
    fun ftsQueryShape() {
        assertEquals("alpha* AND beta*", ftsQuery("Alpha, beta!"))
        assertEquals("", ftsQuery("  !!!  "))
    }

    @Test
    fun prefixMatchAndKindFilter() {
        val s = store()
        s.insert(
            listOf(
                doc("quran:1:1", "alpha beta gamma"),
                doc("quran:1:2", "alpha delta"),
                doc("hadith:bukhari:1", "alpha beta", kind = "hadith"),
            ),
        )
        assertEquals(2L, s.count("quran"))
        assertEquals(3L, s.count())
        val hits = s.search(FtsQuery(kind = "quran", fts = "alph*"))
        assertEquals(2, hits.size)
        // bm25 orders the tighter match first; both share the verse set.
        assertTrue(hits.all { it.kind == "quran" })
        assertTrue(hits.all { it.score != null })
        val scoped = s.search(
            FtsQuery(kind = "quran", fts = "alph* AND beta*"),
        )
        assertEquals(listOf("quran:1:1"), scoped.map { it.id })
    }

    @Test
    fun editionAndLimit() {
        val s = store()
        s.insert(
            listOf(
                doc("a", "common word", editionId = "e1"),
                doc("b", "common word", editionId = "e1"),
                doc("c", "common word", editionId = "e2"),
            ),
        )
        assertEquals(
            2,
            s.search(FtsQuery(kind = "quran", fts = "common*", editionId = "e1")).size,
        )
        assertEquals(
            1,
            s.search(
                FtsQuery(kind = "quran", fts = "common*", editionId = "e1", limit = 1),
            ).size,
        )
    }

    @Test
    fun clearAndFingerprintRebuild() {
        val s = store()
        var builds = 0
        val key = "search.quran.e1"
        assertTrue(
            s.ensureFresh(key, "fp1") {
                builds++
                s.insert(listOf(doc("a", "hello world")))
            },
        )
        assertEquals(1, builds)
        assertEquals(1L, s.count("quran"))
        // Same fingerprint: no rebuild.
        assertFalse(
            s.ensureFresh(key, "fp1") {
                builds++
            },
        )
        assertEquals(1, builds)
        // Changed fingerprint: rebuild runs (caller clears first).
        assertTrue(
            s.ensureFresh(key, "fp2") {
                builds++
                s.clear("quran")
                s.insert(listOf(doc("b", "other words")))
            },
        )
        assertEquals(2, builds)
        assertEquals(1L, s.count("quran"))
        assertEquals("b", s.search(FtsQuery(kind = "quran", fts = "other*")).single().id)
    }

    @Test
    fun realQuranFtsRoundTrip() {
        val raw = TestAssets.reader.readText("assets/quran/hafs-an-asim/uthmani.txt")!!
        val docs = raw.lineSequence()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .mapNotNull { line ->
                val parts = line.split('|', limit = 3)
                if (parts.size != 3 || parts[2].isEmpty()) {
                    null
                } else {
                    val ref = "${parts[0].trim()}:${parts[1].trim()}"
                    doc("quran:$ref", parts[2])
                }
            }.toList()
        val s = store()
        s.insert(docs)
        assertEquals(6236L, s.count("quran"))
        // Numeric tokens never occur in Arabic bodies (verse shortcuts are
        // engine-level); a real word must hit through the FTS index.
        assertTrue(
            s.search(FtsQuery(kind = "quran", fts = "zzzqx*")).isEmpty(),
        )
        val hits = s.search(FtsQuery(kind = "quran", fts = ftsQuery("الله")))
        assertTrue(hits.isNotEmpty())
    }
}
