package com.godfather59.quransunnah

import com.godfather59.quransunnah.data.AssetReader
import com.godfather59.quransunnah.data.parseHadithSection
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HadithReaderRouteTest {
    private val root = generateSequence(File(requireNotNull(System.getProperty("user.dir")))) { it.parentFile }
        .first { File(it, "assets/hadith/bukhari/index.json").isFile }
    private val reader = object : AssetReader {
        override fun readBytes(path: String): ByteArray? =
            File(root, path).takeIf { it.isFile }?.readBytes()
    }

    @Test
    fun savedCoordinatesRestoreExactVerifiedRowAndBookSequence() {
        val route = loadHadithReaderRoute(reader, "bukhari", "bukhari:3")
        assertNotNull(route)
        val restored = requireNotNull(route)
        val expected = parseHadithSection(
            "bukhari", restored.hadith.book,
            requireNotNull(reader.readText("assets/hadith/bukhari/sections/1.json")),
        )

        assertEquals(1, restored.section)
        assertEquals(expected.first { it.id == "bukhari:3" }, restored.hadith)
        assertEquals(expected, restored.sequence)
    }

    @Test
    fun staleSectionHintCannotResolveAnotherNarration() {
        val restored = requireNotNull(loadHadithReaderRoute(reader, "bukhari", "bukhari:3", preferredSection = 2))
        assertEquals("bukhari:3", restored.hadith.id)
        assertEquals(1, restored.section)
    }

    @Test
    fun mismatchedOrUnknownSourceAndMissingAssetsDoNotInventContent() {
        assertNull(loadHadithReaderRoute(reader, "muslim", "bukhari:3"))
        assertNull(loadHadithReaderRoute(reader, "unknown", "unknown:3"))
        val absentReader = object : AssetReader {
            override fun readBytes(path: String): ByteArray? = null
        }
        assertNull(loadHadithReaderRoute(absentReader, "bukhari", "bukhari:3"))
    }
}
