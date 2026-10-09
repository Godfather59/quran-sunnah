package com.godfather59.quransunnah.library

// Library models: bookmarks, notes, highlights, custom collections.
// Ported from lib/data/models/library.dart. Personal data only —
// never mixed with sacred text (see backup format v1).

enum class BookmarkKind { AYAH, HADITH, TAFSIR }

data class Bookmark(
    val id: String,
    val kind: BookmarkKind,
    /** "2:255" or "bukhari:123". */
    val refKey: String,
    val title: String,
    val subtitle: String,
    val collectionId: String? = null,
    /** Epoch seconds. Null when unknown (legacy rows). */
    val createdAtSeconds: Long? = null,
)

data class UserNote(
    val id: String,
    val refKey: String,
    val text: String,
    val createdAtSeconds: Long? = null,
)

data class Highlight(
    val id: String,
    val refKey: String,
    /** 32-bit ARGB as unsigned value (e.g. 0xFFFFD54F > Int.MAX). */
    val colorValue: Long,
)

data class CustomCollection(
    val id: String,
    val name: String,
)

data class RecentItem(
    val refKey: String,
    val title: String,
    val subtitle: String,
    val kind: BookmarkKind,
)

val defaultCollections = listOf(
    CustomCollection(id = "fav-ayat", name = "Favorite Ayat"),
    CustomCollection(id = "prayer", name = "Prayer Hadith"),
    CustomCollection(id = "ramadan", name = "Ramadan"),
)
