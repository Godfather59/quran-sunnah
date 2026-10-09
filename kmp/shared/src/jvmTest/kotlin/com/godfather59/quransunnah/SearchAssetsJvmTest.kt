package com.godfather59.quransunnah

import com.godfather59.quransunnah.data.loadQuranEdition
import com.godfather59.quransunnah.data.parseHadithIndex
import com.godfather59.quransunnah.data.parseHadithSection
import com.godfather59.quransunnah.data.parseTafsirSurah
import com.godfather59.quransunnah.hadith.Hadith
import com.godfather59.quransunnah.quran.TafsirEntry
import com.godfather59.quransunnah.search.SearchEngine
import com.godfather59.quransunnah.search.SearchOptions
import com.godfather59.quransunnah.search.tokenizeQuery
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Search over the real single-copy assets.
class SearchAssetsJvmTest {
    private val reader = TestAssets.reader

    @Test
    fun quranShortcutResolves() {
        val engine = SearchEngine()
        engine.indexQuran(loadQuranEdition(reader, "hafs-an-asim__uthmani")!!)
        assertEquals(6236, engine.documentCount())
        val hits = engine.search("2:255")
        assertTrue(hits.quran.isNotEmpty())
        assertEquals("2:255", hits.quran.first().refKey)
        assertEquals("Direct verse reference", hits.quran.first().subtitle)
        assertTrue(hits.quran.first().snippet.isNotEmpty())
        assertTrue(hits.quran.first().matchedTerms.isEmpty())
    }

    @Test
    fun quranWordSearchFindsVerses() {
        val engine = SearchEngine()
        engine.indexQuran(loadQuranEdition(reader, "hafs-an-asim__uthmani")!!)
        val hits = engine.search("الله")
        assertTrue(hits.quran.isNotEmpty())
        assertTrue(hits.truncated) // thousands of matches, capped at 50.
        assertEquals(50, hits.quran.size)
        // Scoped to one surah: everything returned belongs to it.
        val scoped = engine.search("الله", SearchOptions(surahScope = 112))
        assertTrue(scoped.quran.isNotEmpty())
        assertTrue(scoped.quran.all { it.surah == 112 })
    }

    @Test
    fun nonsenseQueryIsEmpty() {
        val engine = SearchEngine()
        engine.indexQuran(loadQuranEdition(reader, "hafs-an-asim__uthmani")!!)
        val hits = engine.search("zzzqx-no-such-word")
        assertEquals(0, hits.total)
    }

    @Test
    fun fullBukhariIndexesAndSearches() {
        val index = parseHadithIndex(
            reader.readText("assets/hadith/bukhari/index.json")!!,
        )
        val titles = index.sections.associate { it.section to it.title }
        val all = mutableListOf<Hadith>()
        for (section in index.sections) {
            val raw = reader.readText(
                "assets/hadith/bukhari/sections/${section.section}.json",
            ) ?: continue
            all += parseHadithSection(
                "bukhari", titles[section.section] ?: "", raw,
            )
        }
        assertEquals(7589, all.size)
        // Duplicate numbers (e.g. fractional 402.2) get #2 suffixes.
        val ids = all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        // Entries without matn are honest placeholders (Dart parity:
        // isPlaceholder when text is empty); the index skips them exactly
        // like SearchIndexService._ensureHadith does.
        val indexable = all.count { it.matnAr.isNotEmpty() }
        val engine = SearchEngine()
        engine.indexHadith(all, mapOf("bukhari" to "Sahih al-Bukhari"))
        assertEquals(indexable, engine.documentCount())
        val hits = engine.search("الوحي")
        assertTrue(hits.hadith.isNotEmpty())
        assertTrue(hits.hadith.all { it.hadith!!.collectionId == "bukhari" })
        // Collection filter excludes everything when nothing matches it.
        val filtered = engine.search(
            "الوحي", SearchOptions(collectionIds = setOf("muslim")),
        )
        assertTrue(filtered.hadith.isEmpty())
    }

    @Test
    fun tafsirSelfValidatingSearch() {
        val entries = parseTafsirSurah(
            reader.readText("assets/quran/tafsir/jalalayn/1.json")!!,
        )
        val engine = SearchEngine()
        engine.indexTafsir(
            entries.map { (ayah, text) ->
                TafsirEntry(
                    surah = 1, ayah = ayah, tafsirId = "jalalayn",
                    source = "src", text = text,
                )
            },
            titleEn = "Tafsir al-Jalalayn",
            source = "src",
        )
        // Take a real word from a real entry — no transcription risk.
        val words = entries.values.flatMap { tokenizeQuery(it) }
            .filter { it.length > 3 }
        assertTrue(words.isNotEmpty())
        val hits = engine.search(words.first())
        assertTrue(hits.tafsir.isNotEmpty())
    }
}
