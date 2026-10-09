package com.godfather59.quransunnah.quran

// Mushaf (Medina page) metadata. Ported from lib/data/repositories/quran_metadata.dart.
// Applies to Hafs/Uthmani ONLY — other riwayat have their own page mappings.

data class AyahRef(
    val surah: Int,
    val ayah: Int,
) {
    fun compareTo(other: AyahRef): Int {
        if (surah != other.surah) return surah.compareTo(other.surah)
        return ayah.compareTo(other.ayah)
    }
}

data class MushafMetadata(
    val juzStarts: List<AyahRef>,        // 30
    val quarterStarts: List<AyahRef>,    // 240
    val pageStarts: List<AyahRef>,       // 604
    val sajdas: List<AyahRef>,           // 15
) {
    private fun containing(starts: List<AyahRef>, ref: AyahRef): Int {
        var idx = 1
        for (i in starts.indices) {
            if (starts[i].compareTo(ref) <= 0) {
                idx = i + 1
            } else {
                break
            }
        }
        return idx
    }

    fun juzOf(surah: Int, ayah: Int): Int = containing(juzStarts, AyahRef(surah, ayah))

    fun quarterOf(surah: Int, ayah: Int): Int = containing(quarterStarts, AyahRef(surah, ayah))

    fun hizbOf(surah: Int, ayah: Int): Int = ((quarterOf(surah, ayah) - 1) / 4) + 1

    fun rubOf(surah: Int, ayah: Int): Int = ((quarterOf(surah, ayah) - 1) % 4) + 1

    fun pageOf(surah: Int, ayah: Int): Int = containing(pageStarts, AyahRef(surah, ayah))

    fun isSajda(surah: Int, ayah: Int): Boolean = sajdas.any { it.surah == surah && it.ayah == ayah }

    /** Returns the first ayah (surah, ayah) on the given 1-based page. */
    fun firstAyahOnPage(page: Int): AyahRef? {
        if (page < 1 || page > pageStarts.size) return null
        return pageStarts[page - 1]
    }

    /** Returns the last ayah on the given 1-based page. */
    fun lastAyahOnPage(page: Int): AyahRef? {
        if (page < 1 || page > pageStarts.size) return null
        val nextPage = page + 1
        if (nextPage > pageStarts.size) {
            // Last page runs to the end of Surah 114.
            return AyahRef(114, surahMetadata.last().ayahCount)
        }
        val endRef = pageStarts[nextPage - 1]
        if (endRef.ayah > 1) {
            return AyahRef(endRef.surah, endRef.ayah - 1)
        }
        // Next page starts at ayah 1: borrow the last ayah of the previous surah.
        // Page starts are ordered, so the previous surah is endRef.surah - 1.
        val prevSurah = endRef.surah - 1
        if (prevSurah < 1) return null
        val prevMeta = surahMetadata.firstOrNull { it.number == prevSurah }
            ?: return null
        return AyahRef(prevSurah, prevMeta.ayahCount)
    }
}

interface MushafMetadataReader {
    suspend fun load(): MushafMetadata
}