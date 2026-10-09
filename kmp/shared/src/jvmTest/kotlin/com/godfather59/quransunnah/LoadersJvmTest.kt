package com.godfather59.quransunnah

import com.godfather59.quransunnah.content.parseContentManifest
import com.godfather59.quransunnah.data.loadQuranEdition
import com.godfather59.quransunnah.data.parseHadithIndex
import com.godfather59.quransunnah.data.parseHadithSection
import com.godfather59.quransunnah.data.parsePipeMap
import com.godfather59.quransunnah.data.parseTafsirSurah
import com.godfather59.quransunnah.data.parseWordsSurah
import com.godfather59.quransunnah.data.verifiedQuranAssets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoadersJvmTest {
    private val reader = TestAssets.reader

    @Test
    fun quranEditionsAre6236() {
        for ((editionId, _) in verifiedQuranAssets) {
            val ayahs = assertNotNull(
                loadQuranEdition(reader, editionId),
                editionId,
            )
            assertEquals(6236, ayahs.size, editionId)
            assertTrue(ayahs.none { it.isPlaceholder || it.text.isEmpty() })
        }
    }

    @Test
    fun quranFirstAyahSpotCheck() {
        val ayahs = loadQuranEdition(reader, "hafs-an-asim__uthmani")!!
        val first = ayahs.first()
        assertEquals(1, first.surah)
        assertEquals(1, first.ayah)
        assertEquals("1:1", first.key)
        assertTrue(first.text.startsWith("بِسْمِ"))
        val last = ayahs.last()
        assertEquals(114, last.surah)
        assertEquals(6, last.ayah)
    }

    @Test
    fun unknownEditionIsNull() {
        assertNull(loadQuranEdition(reader, "nope__uthmani"))
    }

    @Test
    fun translationMapIs6236() {
        val raw = reader.readText("assets/quran/translations/en-sahih.txt")!!
        val map = parsePipeMap(raw)
        assertEquals(6236, map.size)
        assertNotNull(map["2:255"])
        val fr = parsePipeMap(
            reader.readText("assets/quran/translations/fr-hamidullah.txt")!!,
        )
        assertEquals(6236, fr.size)
    }

    @Test
    fun bukhariIndexShape() {
        val raw = reader.readText("assets/hadith/bukhari/index.json")!!
        val index = parseHadithIndex(raw)
        assertEquals(98, index.sections.size)
        assertEquals(7589, index.sections.sumOf { it.count })
        val revelation = index.sections.first { it.section == 1 }
        assertEquals("Revelation", revelation.title)
        assertEquals(7, revelation.count)
    }

    @Test
    fun bukhariSectionOneShape() {
        val raw = reader.readText("assets/hadith/bukhari/sections/1.json")!!
        val hadiths = parseHadithSection("bukhari", "Revelation", raw)
        assertEquals(7, hadiths.size)
        val first = hadiths.first()
        assertEquals("bukhari:1", first.id)
        assertEquals("1", first.hadithNumber)
        assertEquals("Revelation", first.book)
        assertEquals("Revelation", first.chapter)
        assertNull(first.grade)
        assertNull(first.sanadAr)
        assertTrue(first.matnAr.isNotEmpty())
        assertTrue(hadiths.all { !it.isPlaceholder })
    }

    @Test
    fun gradesParsedFirstEntryWins() {
        val raw = reader.readText("assets/hadith/abudawud/sections/1.json")!!
        val hadiths = parseHadithSection("abudawud", "Purification", raw)
        val first = hadiths.first()
        assertEquals("Hasan Sahih", first.grade)
        assertEquals("Al-Albani", first.gradingAuthority)
        val second = hadiths.first { it.hadithNumber == "2" }
        assertEquals("Sahih", second.grade)
    }

    @Test
    fun ungradedCollectionsStayNull() {
        val raw = reader.readText("assets/hadith/bukhari/sections/1.json")!!
        val hadiths = parseHadithSection("bukhari", "Revelation", raw)
        assertTrue(hadiths.all { it.grade == null && it.gradingAuthority == null })
    }

    @Test
    fun tafsirSurahSpotCheck() {
        val raw =
            reader.readText("assets/quran/tafsir/jalalayn/1.json")!!
        val entries = parseTafsirSurah(raw)
        assertTrue(entries.isNotEmpty())
        assertTrue(entries.keys.all { it in 1..7 })
        assertTrue(entries.values.all { it.isNotEmpty() })
    }

    @Test
    fun wordsSurahSpotCheck() {
        val raw = reader.readText("assets/quran/words/hafs/1.json")!!
        val entries = parseWordsSurah(raw, 1)
        assertEquals(7, entries.size)
        val first = entries.first()
        assertEquals(1, first.ayah)
        assertEquals("بِسْمِ", first.words.first().word)
        assertEquals("سمو", first.words.first().root)
        assertTrue(entries.all { it.words.isNotEmpty() })
    }

    @Test
    fun wordsSurahWrongSurahIsEmpty() {
        val raw = reader.readText("assets/quran/words/hafs/1.json")!!
        assertTrue(parseWordsSurah(raw, 2).isEmpty())
    }

    @Test
    fun contentManifestParses() {
        val raw = reader.readText("assets/content_packages.json")!!
        val manifest = parseContentManifest(raw)
        assertEquals(1, manifest.schemaVersion)
        assertTrue(manifest.packages.isNotEmpty())
        val en = manifest.packages.first { it.id == "quran:en-sahih" }
        assertEquals(64, en.sha256.length)
        assertTrue(en.files.isNotEmpty())
        assertTrue(en.files.all { it.sha256.length == 64 && it.sizeBytes > 0 })
    }
}
