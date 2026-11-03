package com.godfather59.quransunnah

import com.godfather59.quransunnah.data.AssetReader
import com.godfather59.quransunnah.data.parseHadithIndex
import com.godfather59.quransunnah.data.parseHadithSection
import com.godfather59.quransunnah.hadith.Hadith
import com.godfather59.quransunnah.hadith.hadithCollections

/** Transient verified content, rebuilt from the saved collection/id/section route. */
internal data class LoadedHadithReaderRoute(
    val hadith: Hadith,
    val sequence: List<Hadith>,
    val section: Int,
)

/** Call off Main. Only exact rows from the bundled source may resolve a route. */
internal fun loadHadithReaderRoute(
    reader: AssetReader,
    collectionId: String,
    hadithId: String,
    preferredSection: Int? = null,
): LoadedHadithReaderRoute? {
    if (hadithCollections.none { it.id == collectionId } || !hadithId.startsWith("$collectionId:")) return null
    val number = hadithId.substringAfter(':').substringBefore('#').toIntOrNull() ?: return null
    val rawIndex = reader.readText("assets/hadith/$collectionId/index.json") ?: return null
    val sections = parseHadithIndex(rawIndex).sections.filter { it.count > 0 }
    // A saved section is a hint, never authority: an exact id must exist in
    // that file. Range lookup keeps the common restore to one section read.
    val candidates = sections.sortedWith(
        compareByDescending<com.godfather59.quransunnah.data.HadithSection> { it.section == preferredSection }
            .thenByDescending { number in it.first..it.last },
    )
    for (section in candidates) {
        val raw = reader.readText("assets/hadith/$collectionId/sections/${section.section}.json") ?: continue
        val rows = parseHadithSection(collectionId, section.title, raw)
        val hadith = rows.firstOrNull { it.id == hadithId } ?: continue
        return LoadedHadithReaderRoute(hadith, rows, section.section)
    }
    return null
}
