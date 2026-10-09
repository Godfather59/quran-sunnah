package com.godfather59.quransunnah

import com.godfather59.quransunnah.hadith.Hadith
import com.godfather59.quransunnah.quran.Ayah
import com.godfather59.quransunnah.quran.TafsirEntry
import com.godfather59.quransunnah.search.SearchEngine
import com.godfather59.quransunnah.search.SearchKind
import com.godfather59.quransunnah.search.SearchOptions
import com.godfather59.quransunnah.search.normalizeForIndex
import com.godfather59.quransunnah.search.tokenizeQuery
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun ayah(surah: Int, ayah: Int, text: String) = Ayah(
    surah = surah, ayah = ayah, text = text, editionId = "test",
)

private fun hadith(id: String, matn: String, book: String = "Book") = Hadith(
    id = id, collectionId = "bukhari", book = book, bookAr = "",
    chapter = book, chapterAr = "", hadithNumber = id.substringAfter(':'),
    matnAr = matn,
)

class SearchTest {
    @Test
    fun tokenizeBasics() {
        assertEquals(listOf("hello", "world"), tokenizeQuery("Hello, World!"))
        assertEquals(listOf("2", "255"), tokenizeQuery("2:255"))
        assertEquals(emptyList(), tokenizeQuery("   "))
        assertEquals(emptyList(), tokenizeQuery("!!!"))
        // Diacritics collapse to the same tokens.
        assertEquals(
            tokenizeQuery("ٱلْحَمْدُ"),
            tokenizeQuery("الحمد"),
        )
    }

    @Test
    fun normalizeForIndexLowercases() {
        assertEquals("abc", normalizeForIndex("ABC"))
    }

    @Test
    fun emptyQueryIsEmpty() {
        val engine = SearchEngine()
        engine.indexQuran(listOf(ayah(1, 1, "hello world")))
        assertEquals(0, engine.search("").total)
        assertEquals(0, engine.search("   ").total)
    }

    @Test
    fun andSemanticsAndPrefix() {
        val engine = SearchEngine()
        engine.indexQuran(
            listOf(
                ayah(1, 1, "alpha beta"),
                ayah(1, 2, "alpha gamma"),
                ayah(1, 3, "alphabet soup"),
            ),
        )
        val both = engine.search("alpha beta")
        assertEquals(listOf("1:1"), both.quran.map { it.refKey })
        // Prefix: "alph" matches alpha + alphabet.
        val prefix = engine.search("alph")
        assertEquals(setOf("1:1", "1:2", "1:3"), prefix.quran.map { it.refKey }.toSet())
        assertTrue(prefix.quran.all { it.matchedTerms == listOf("alph") })
    }

    @Test
    fun exactRanksAbovePrefix() {
        val engine = SearchEngine()
        engine.indexQuran(
            listOf(
                ayah(2, 1, "alphabet soup"),
                ayah(1, 1, "alpha beta"),
            ),
        )
        val hits = engine.search("alpha")
        assertEquals("1:1", hits.quran.first().refKey)
    }

    @Test
    fun diacriticInsensitiveMatch() {
        val engine = SearchEngine()
        engine.indexQuran(listOf(ayah(1, 2, "ٱلْحَمْدُ لِلَّهِ")))
        val hits = engine.search("الحمد")
        assertEquals(1, hits.quran.size)
        assertEquals("1:2", hits.quran.first().refKey)
    }

    @Test
    fun directVerseFirstAndDeduped() {
        val engine = SearchEngine()
        engine.indexQuran(
            listOf(
                ayah(2, 255, "sometext about the verse"),
                ayah(2, 256, "other"),
            ),
        )
        val hits = engine.search("2:255")
        assertTrue(hits.quran.isNotEmpty())
        assertEquals("2:255", hits.quran.first().refKey)
        assertEquals("Direct verse reference", hits.quran.first().subtitle)
        assertEquals(1, hits.quran.count { it.refKey == "2:255" })
    }

    @Test
    fun invalidVerseRefFallsBackToFts() {
        val engine = SearchEngine()
        engine.indexQuran(listOf(ayah(1, 1, "nothing relevant here")))
        // Surah out of range: no direct hit, no crash.
        assertTrue(engine.search("999:1").quran.isEmpty())
        // quran:// URIs parse but are NOT shortcuts (Dart parity).
        assertTrue(engine.search("quran://1/1").quran.isEmpty())
    }

    @Test
    fun limitAndTruncated() {
        val engine = SearchEngine()
        engine.indexQuran((1..5).map { ayah(1, it, "common word") })
        val hits = engine.search("common", SearchOptions(limitPerCategory = 2))
        assertEquals(2, hits.quran.size)
        assertTrue(hits.truncated)
        val full = engine.search("common", SearchOptions(limitPerCategory = 50))
        assertEquals(5, full.quran.size)
        assertTrue(!full.truncated)
    }

    @Test
    fun surahScopeAndEditionFilter() {
        val engine = SearchEngine()
        engine.indexQuran(
            listOf(
                ayah(1, 1, "shared word"),
                ayah(2, 1, "shared word"),
            ),
        )
        val scoped = engine.search("shared", SearchOptions(surahScope = 2))
        assertEquals(listOf("2:1"), scoped.quran.map { it.refKey })
    }

    @Test
    fun hadithFilters() {
        val engine = SearchEngine()
        engine.indexHadith(
            listOf(
                hadith("bukhari:1", "faith prayer charity", "Faith"),
                hadith("bukhari:2", "faith fasting", "Fasting"),
            ),
            mapOf("bukhari" to "Sahih al-Bukhari"),
        )
        assertEquals(2, engine.search("faith").hadith.size)
        assertEquals(
            1,
            engine.search("faith", SearchOptions(book = "Fasting")).hadith.size,
        )
        assertEquals(
            0,
            engine.search("faith", SearchOptions(collectionIds = setOf("muslim"))).hadith.size,
        )
        assertEquals(
            1,
            engine.search("faith", SearchOptions(number = "2")).hadith.size,
        )
        val hit = engine.search("prayer").hadith.first()
        assertEquals("bukhari:1", hit.refKey)
        assertEquals("bukhari", hit.hadith!!.collectionId)
        assertTrue(hit.snippet.isNotEmpty())
    }

    @Test
    fun tafsirSearch() {
        val engine = SearchEngine()
        engine.indexTafsir(
            listOf(
                TafsirEntry(
                    surah = 1, ayah = 1, tafsirId = "jalalayn",
                    source = "src", text = "praise belongs to the lord",
                ),
            ),
            titleEn = "Tafsir al-Jalalayn",
            source = "src",
        )
        val hits = engine.search("praise")
        assertEquals(1, hits.tafsir.size)
        assertEquals("jalalayn:1:1", hits.tafsir.first().refKey)
        val scoped = engine.search("praise", SearchOptions(tafsirId = "siraj"))
        assertTrue(scoped.tafsir.isEmpty())
    }

    @Test
    fun surahSearchForms() {
        val engine = SearchEngine()
        val ar = engine.searchSurahs("بقرة")
        assertTrue(ar.any { it.surah == 2 })
        val en = engine.searchSurahs("cow")
        assertTrue(en.isEmpty()) // English name is "Al-Baqarah".
        assertTrue(engine.searchSurahs("baqarah").any { it.surah == 2 })
        assertTrue(engine.searchSurahs("114").any { it.surah == 114 })
        assertTrue(engine.searchSurahs("").isEmpty())
        val hit = engine.searchSurahs("1").first { it.surah == 1 }
        assertEquals("surah:1", hit.refKey)
        assertEquals(SearchKind.SURAH, hit.kind)
    }

    @Test
    fun snippetTruncates() {
        assertEquals("abc", SearchEngine.snippet("abc"))
        val long = "x".repeat(300)
        val cut = SearchEngine.snippet(long)
        assertEquals(221, cut.length)
        assertTrue(cut.endsWith("…"))
    }
}
