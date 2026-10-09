package com.godfather59.quransunnah

import com.godfather59.quransunnah.integrity.parseIntegrityManifest
import com.godfather59.quransunnah.integrity.verifyIntegrity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// Full-manifest cross-check: every bundled file, byte-exact.
// This simultaneously proves the SHA-1 port, manifest parsing,
// and single-copy asset wiring.
class IntegrityJvmTest {
    private val reader = TestAssets.reader

    @Test
    fun manifestParses() {
        val raw = assertNotNull(
            reader.readText("assets/integrity_manifest.json"),
        )
        val manifest = parseIntegrityManifest(raw)
        assertEquals("git-blob-sha1", manifest.algorithm)
        assertTrue(manifest.files.size > 800, "files=${manifest.files.size}")
        assertNotNull(manifest.files["assets/quran/hafs-an-asim/uthmani.txt"])
    }

    @Test
    fun allBundledFilesVerify() {
        val raw = reader.readText("assets/integrity_manifest.json")!!
        val manifest = parseIntegrityManifest(raw)
        val bad = verifyIntegrity(manifest, reader)
        assertEquals(emptyList(), bad)
    }

    @Test
    fun tamperedHashIsReported() {
        val raw = reader.readText("assets/integrity_manifest.json")!!
        val manifest = parseIntegrityManifest(raw)
        val tampered = manifest.copy(
            files = manifest.files + (
                "assets/quran/hafs-an-asim/uthmani.txt" to "0".repeat(40)
                ),
        )
        assertEquals(
            listOf("assets/quran/hafs-an-asim/uthmani.txt"),
            verifyIntegrity(tampered, reader),
        )
    }
}
