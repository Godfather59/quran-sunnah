package com.godfather59.quransunnah.data

import com.godfather59.quransunnah.hadith.Hadith
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Dart `(m['k'] as num).toInt()`: ints and fractional doubles (e.g. 402.2). */
internal fun JsonPrimitive.asLenientInt(): Int = try {
    int
} catch (_: IllegalArgumentException) {
    double.toInt()
}

// Verified Hadith loader. Mirrors verified_asset_hadith_repository:
// - index.json: {"sections":[{section,title,first,last,count}...], name, source}
// - sections/N.json: {"metadata":{...},"hadiths":[{hadithnumber,arabicnumber,
//   text,grades[{name,grade}],reference:{book,hadith}}]}
// Fields the source does not provide (sanad breakdown, Arabic book titles)
// stay null so the UI renders them as unavailable — never invented.
// Grades come from the `grades[]` array (first entry wins in source order;
// authorities genuinely disagree, so no canonical ranking is imposed).
// Collections without graded entries (Bukhari, Muslim, Nawawi, Qudsi,
// Dehlawi) keep null grades and render the honest unavailable note.

data class HadithSection(
    val section: Int,
    val title: String,
    val first: Int,
    val last: Int,
    val count: Int,
)

data class HadithIndex(
    val name: String,
    val source: String,
    val sections: List<HadithSection>,
)

fun parseHadithIndex(raw: String): HadithIndex {
    val root = try {
        parseJsonObject(raw)
    } catch (_: Exception) {
        return HadithIndex(name = "", source = "", sections = emptyList())
    }
    val sectionsArray = try {
        root["sections"]?.jsonArray ?: return HadithIndex(
            name = root["name"]?.jsonPrimitive?.content ?: "",
            source = root["source"]?.jsonPrimitive?.content ?: "",
            sections = emptyList(),
        )
    } catch (_: Exception) {
        return HadithIndex(name = "", source = "", sections = emptyList())
    }
    return HadithIndex(
        name = root["name"]?.jsonPrimitive?.content ?: "",
        source = root["source"]?.jsonPrimitive?.content ?: "",
        sections = sectionsArray.mapNotNull { element ->
            try {
                val s = element.jsonObject
                val section = try {
                    s["section"]?.jsonPrimitive?.asLenientInt() ?: return@mapNotNull null
                } catch (_: Exception) {
                    return@mapNotNull null
                }
                HadithSection(
                    section = section,
                    title = s["title"]?.jsonPrimitive?.content ?: "",
                    first = s["first"]?.jsonPrimitive?.asLenientInt() ?: 0,
                    last = s["last"]?.jsonPrimitive?.asLenientInt() ?: 0,
                    count = s["count"]?.jsonPrimitive?.asLenientInt() ?: 0,
                )
            } catch (_: Exception) {
                null
            }
        },
    )
}

/**
 * Parses one section file. book/chapter carry the section title (the source
 * has no Arabic book titles); sanad stays null (no structured source).
 * Grade comes from the first non-blank `grades[]` entry (name = authority).
 */
fun parseHadithSection(
    collectionId: String,
    sectionTitle: String,
    raw: String,
): List<Hadith> {
    val root = try {
        parseJsonObject(raw)
    } catch (_: Exception) {
        return emptyList()
    }
    val hadithsArray = try {
        root["hadiths"]?.jsonArray ?: return emptyList()
    } catch (_: Exception) {
        return emptyList()
    }
    // Duplicate numbers (e.g. fractional 402.2 -> 402) get #2 suffixes,
    // exactly like Dart _loadSection (sameCount).
    val seen = mutableMapOf<Int, Int>()
    return hadithsArray.mapNotNull { element ->
        val m = try {
            element.jsonObject
        } catch (_: Exception) {
            return@mapNotNull null
        }
        val number = try {
            m["hadithnumber"]?.jsonPrimitive?.asLenientInt() ?: return@mapNotNull null
        } catch (_: Exception) {
            return@mapNotNull null
        }
        val same = seen[number] ?: 0
        seen[number] = same + 1
        val text = m["text"]?.jsonPrimitive?.content ?: ""
        val (grade, authority) = primaryGrade(m)
        Hadith(
            id = if (same == 0) {
                "$collectionId:$number"
            } else {
                "$collectionId:$number#${same + 1}"
            },
            collectionId = collectionId,
            book = sectionTitle,
            bookAr = "",
            chapter = sectionTitle,
            chapterAr = "",
            hadithNumber = number.toString(),
            matnAr = text,
            sanadAr = null,
            grade = grade,
            gradingAuthority = authority,
            // Upstream numbers without matn keep their reference honestly.
            isPlaceholder = text.isEmpty(),
        )
    }
}

/** First non-blank `grades[]` entry, or (null, null) when ungraded. */
private fun primaryGrade(m: JsonObject): Pair<String?, String?> {
    val grades = try {
        m["grades"]?.jsonArray
    } catch (_: Exception) {
        null
    } ?: return Pair(null, null)
    for (element in grades) {
        try {
            val o = element.jsonObject
            val grade = o["grade"]?.jsonPrimitive?.content?.trim()
            if (grade.isNullOrEmpty()) continue
            val authority = o["name"]?.jsonPrimitive?.content?.trim()
            return Pair(grade, authority?.takeIf { it.isNotEmpty() })
        } catch (_: Exception) {
        }
    }
    return Pair(null, null)
}

/** Tafsir per-surah file: {"entries":[{"ayah":N,"text":"..."}]}. */
fun parseTafsirSurah(raw: String): Map<Int, String> {
    val root = try {
        parseJsonObject(raw)
    } catch (_: Exception) {
        return emptyMap()
    }
    val entries = try {
        root["entries"]?.jsonArray ?: return emptyMap()
    } catch (_: Exception) {
        return emptyMap()
    }
    val out = LinkedHashMap<Int, String>()
    for (element in entries) {
        try {
            val m = element.jsonObject
            val text = m["text"]?.jsonPrimitive?.content ?: ""
            if (text.isEmpty()) continue
            val ayah = try {
                m["ayah"]?.jsonPrimitive?.asLenientInt() ?: continue
            } catch (_: Exception) {
                continue
            }
            if (ayah <= 0) continue
            out[ayah] = text
        } catch (_: Exception) {
            continue
        }
    }
    return out
}
