package com.godfather59.quransunnah

import com.godfather59.quransunnah.crypto.Sha1
import com.godfather59.quransunnah.crypto.Sha256
import com.godfather59.quransunnah.crypto.gitBlobSha1Hex
import com.godfather59.quransunnah.crypto.toHex
import kotlin.test.Test
import kotlin.test.assertEquals

// Vectors from Dart crypto (tool/gen_kmp_vectors.dart, since removed).
class HashTest {
    private fun ascii(s: String): ByteArray = s.encodeToByteArray()

    @Test
    fun sha256Vectors() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            Sha256.hex(ascii("abc")),
        )
        assertEquals(
            "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
            Sha256.hex(ascii("hello")),
        )
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            Sha256.hex(ascii("")),
        )
    }

    @Test
    fun sha1Vectors() {
        assertEquals(
            "a9993e364706816aba3e25717850c26c9cd0d89d",
            Sha1.hex(ascii("abc")),
        )
        assertEquals(
            "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d",
            Sha1.hex(ascii("hello")),
        )
        assertEquals(
            "da39a3ee5e6b4b0d3255bfef95601890afd80709",
            Sha1.hex(ascii("")),
        )
    }

    @Test
    fun gitBlobSha1Vector() {
        assertEquals(
            "ce013625030ba8dba906f756967f9e9ca394464a",
            gitBlobSha1Hex(ascii("hello\n")),
        )
    }

    @Test
    fun toHexPadding() {
        assertEquals("00", byteArrayOf(0).toHex())
        assertEquals("0f", byteArrayOf(15).toHex())
        assertEquals("ff", byteArrayOf(-1).toHex())
    }
}
