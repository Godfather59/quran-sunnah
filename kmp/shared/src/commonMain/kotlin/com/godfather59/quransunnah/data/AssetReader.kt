package com.godfather59.quransunnah.data

/**
 * Read-only access to verified bundled assets (Quran/Hadith/Tafsir files).
 * Paths look like "assets/quran/hafs-an-asim/uthmani.txt".
 * Never throws — absent assets return null so callers render the
 * "Content unavailable" state instead of inventing text.
 */
interface AssetReader {
    fun readBytes(path: String): ByteArray?

    fun readText(path: String): String? {
        val bytes = readBytes(path) ?: return null
        return bytes.decodeToString()
    }
}
