package com.godfather59.quransunnah.hadith

// Hadith domain models. Ported from lib/data/models/hadith.dart.
// Every field preserves source provenance.
// Never merge parallel narrations into one fabricated text.

data class HadithCollection(
    val id: String, // e.g. "bukhari"
    val nameAr: String,
    val nameEn: String,
    val nameFr: String,
    val compiler: String,
    val source: String,
    val version: String,
    val totalHadith: Int? = null,
    val isDownloaded: Boolean = false,
    val downloadSizeMb: Double? = null,
)

data class Hadith(
    val id: String, // "<collection>:<number>"
    val collectionId: String,
    val book: String,
    val bookAr: String,
    val chapter: String,
    val chapterAr: String,
    val hadithNumber: String,
    val matnAr: String,
    val matnTranslation: String? = null,
    val sanadAr: String? = null,
    val narrator: String? = null,
    /** e.g. "Sahih", "Hasan", "Da'if" — null when source gives none. */
    val grade: String? = null,
    /** Scholar/source responsible for the grade. */
    val gradingAuthority: String? = null,
    val topics: List<String> = emptyList(),
    val parallelIds: List<String> = emptyList(),
    val isPlaceholder: Boolean = false,
)

data class NarratorProfile(
    val nameAr: String,
    val nameEn: String,
    val bio: String = "",
)

/** Structured query over verified collections. Ported from HadithFilter. */
data class HadithFilter(
    val collectionIds: Set<String> = emptySet(),
    val book: String? = null,
    val narrator: String? = null,
    val grade: String? = null,
    val topic: String? = null,
    val query: String? = null,
    val number: String? = null,
)

data class HadithCapabilities(
    val narrator: Boolean = false,
    val sanad: Boolean = false,
    val grade: Boolean = false,
    val topics: Boolean = false,
) {
    val anyStructured: Boolean get() = narrator || sanad || grade || topics
}
