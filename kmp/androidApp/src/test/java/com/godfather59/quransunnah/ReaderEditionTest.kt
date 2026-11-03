package com.godfather59.quransunnah

import com.godfather59.quransunnah.data.verifiedQuranAssets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderEditionTest {
    @Test
    fun validEditionsKeepTheirExactSelection() {
        readerEditions.forEach { edition ->
            assertEquals(edition, resolveReaderEdition(edition.riwayaName, edition.scriptName))
        }
    }

    @Test
    fun warshWithUnsupportedIndopakKeepsWarsh() {
        val edition = resolveReaderEdition("warshNafi", "indopak")

        assertEquals("warsh-an-nafi__uthmani", edition.id)
        assertEquals("warshNafi", edition.riwayaName)
        assertEquals("uthmani", edition.scriptName)
    }

    @Test
    fun qalunWithUnsupportedImlaiKeepsQalun() {
        val edition = resolveReaderEdition("qalunNafi", "imlai")

        assertEquals("qalun-an-nafi__uthmani", edition.id)
        assertEquals("qalunNafi", edition.riwayaName)
        assertEquals("uthmani", edition.scriptName)
    }

    @Test
    fun unknownSelectionFallsBackToVerifiedDefault() {
        listOf("unknown", "").forEach { riwaya ->
            listOf("unknown", "", "uthmani", "imlai").forEach { script ->
                assertEquals("hafs-an-asim__uthmani", resolveReaderEdition(riwaya, script).id)
            }
        }
    }

    @Test
    fun everyResolvedSelectionHasABundledVerifiedAsset() {
        val riwayat = readerEditions.map { it.riwayaName }.distinct() + listOf("unknown", "")
        val scripts = readerEditions.map { it.scriptName }.distinct() + listOf("unknown", "")

        riwayat.forEach { riwaya ->
            scripts.forEach { script ->
                val resolved = resolveReaderEdition(riwaya, script)
                assertTrue("Unverified edition for $riwaya/$script: ${resolved.id}", resolved.id in verifiedQuranAssets)
            }
        }
    }
}
