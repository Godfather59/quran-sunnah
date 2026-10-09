package com.godfather59.quransunnah.audio

import com.godfather59.quransunnah.quran.surahMetadata

/** Global ayah number 1..6236 (CDN file id). Throws on invalid refs. */
fun globalAyahNumber(surah: Int, ayah: Int): Int {
    require(surah in 1..114 && ayah >= 1) { "invalid $surah:$ayah" }
    var n = 0
    for (m in surahMetadata) {
        if (m.number < surah) {
            n += m.ayahCount
        } else if (m.number == surah) {
            require(ayah <= m.ayahCount) { "invalid $surah:$ayah" }
            return n + ayah
        } else {
            break
        }
    }
    throw IllegalArgumentException("invalid $surah:$ayah")
}

/** Local-first ayah audio cache layout: `<docs>/audio/<reciter>/<surah>/<ayah>.mp3`. */
fun audioDirFor(reciterId: String, surah: Int): String =
    "audio/$reciterId/$surah"

fun audioFileName(ayah: Int): String = "$ayah.mp3"

fun audioDownloadKey(reciterId: String, surah: Int): String =
    "$reciterId:$surah"

/** Checks if all ayahs for a surah are downloaded locally for a reciter. */
fun isSurahDownloaded(
    reciterId: String,
    surah: Int,
    getFile: (String) -> Boolean,
): Boolean {
    val surahMeta = surahMetadata.firstOrNull { it.number == surah }
    if (surahMeta == null) return false
    for (ayah in 1..surahMeta.ayahCount) {
        val fileName = audioFileName(ayah)
        val path = "${audioDirFor(reciterId, surah)}/$fileName"
        if (!getFile(path)) return false
    }
    return true
}

/** Returns local file URL if ayah is downloaded, otherwise null. */
fun localAyahUrlIfDownloaded(
    reciterId: String,
    surah: Int,
    ayah: Int,
    getFile: (String) -> String?,
): String? {
    val path = "${audioDirFor(reciterId, surah)}/${audioFileName(ayah)}"
    return getFile(path)?.let { "file://$it" }
}
