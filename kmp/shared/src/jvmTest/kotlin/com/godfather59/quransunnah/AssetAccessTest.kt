package com.godfather59.quransunnah

import com.godfather59.quransunnah.data.loadTafsirSurah
import com.godfather59.quransunnah.data.parseTafsirSurahJson
import com.godfather59.quransunnah.ui.AppFontAssets
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Reads the real single-copy repo assets (../../assets from kmp/shared).
// Proves the no-duplication wiring works on JVM.
class AssetAccessTest {
    private val root = TestAssets.repoRoot
    private val reader = TestAssets.reader

    @Test
    fun repoAssetsPresent() {
        assertTrue(
            File(root, "assets/integrity_manifest.json").isFile,
            "missing repo assets under ${root.absolutePath}",
        )
        assertNotNull(reader.readText("assets/integrity_manifest.json"))
        assertNotNull(reader.readText("assets/content_packages.json"))
    }

    @Test
    fun quranTextShape() {
        val raw = reader.readText("assets/quran/hafs-an-asim/uthmani.txt")!!
        // Tanzil copyright footer (provenance) trails the rows.
        assertTrue(raw.lineSequence().any { it.startsWith("#") })
        val lines = raw.lineSequence()
            .filter { it.isNotBlank() && !it.startsWith("#") }.toList()
        assertEquals(6236, lines.size)
        assertTrue(lines.first().startsWith("1|1|"))
        assertTrue(lines.last().startsWith("114|6|"))
    }

    @Test
    fun arabicFontsAreBundled() {
        val amiri = assertNotNull(reader.readBytes(AppFontAssets.AMIRI_QURAN))
        val noto = assertNotNull(reader.readBytes(AppFontAssets.NOTO_NASKH_ARABIC))

        assertTrue(amiri.size > 100_000, "Amiri font size=${amiri.size}")
        assertTrue(noto.size > 100_000, "Noto font size=${noto.size}")
        assertTrue(amiri.isTrueTypeOrOpenType())
        assertTrue(noto.isTrueTypeOrOpenType())
    }

    @Test
    fun tafsirSurahJsonLoadsVerifiedRows() {
        val jalalayn = assertNotNull(loadTafsirSurah(reader, "jalalayn", 1))
        val siraj = assertNotNull(loadTafsirSurah(reader, "siraj", 1))

        assertEquals(7, jalalayn.size)
        assertEquals(7, siraj.size)
        assertTrue(jalalayn[1]!!.contains("بسم الله"))
        assertTrue(siraj[1]!!.contains("بِسْمِ اللهِ"))
    }

    @Test
    fun tafsirParserFailsClosedOnWrongSurah() {
        val raw = reader.readText("assets/quran/tafsir/jalalayn/1.json")!!
        assertTrue(parseTafsirSurahJson(raw, expectedSurah = 2).isEmpty())
    }

    @Test
    fun jalalaynTafsirCoversAllAyat() {
        var total = 0
        for (surah in 1..114) {
            total += assertNotNull(loadTafsirSurah(reader, "jalalayn", surah)).size
        }
        assertEquals(6236, total)
    }

    @Test
    fun missingAssetIsNull() {
        assertNull(reader.readBytes("assets/quran/nope/missing.txt"))
        assertNull(loadTafsirSurah(reader, "unknown-tafsir", 1))
    }
}

private fun ByteArray.isTrueTypeOrOpenType(): Boolean {
    if (size < 4) return false
    val tag = decodeToString(endIndex = 4)
    return tag == "OTTO" ||
        (this[0] == 0.toByte() &&
            this[1] == 1.toByte() &&
            this[2] == 0.toByte() &&
            this[3] == 0.toByte())
}
