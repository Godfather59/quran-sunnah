package com.godfather59.quransunnah.data

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// Verified word-morphology loader (Hafs only, like the Medina page map).
// assets/quran/words/hafs/<surah>.json:
// {"surah":N,"entries":[{"ayah":N,"words":[{"w","lemma","root","pos"}]}]}
// Empty roots/lemmas are honest gaps in the source (e.g. particles).

data class WordInfo(
    val word: String,
    val lemma: String,
    val root: String,
    val pos: String,
)

data class AyahWords(
    val ayah: Int,
    val words: List<WordInfo>,
)

fun parseWordsSurah(raw: String, expectedSurah: Int): List<AyahWords> {
    val root = try {
        parseJsonObject(raw)
    } catch (_: Exception) {
        return emptyList()
    }
    val surah = root["surah"]?.jsonPrimitive?.content?.toIntOrNull()
        ?: return emptyList()
    if (surah != expectedSurah) return emptyList()
    val entries = root["entries"]?.jsonArray ?: return emptyList()
    return entries.mapNotNull { element ->
        try {
            val m = element.jsonObject
            val ayah = m["ayah"]?.jsonPrimitive?.content?.toIntOrNull()
                ?: return@mapNotNull null
            if (ayah <= 0) return@mapNotNull null
            val words = m["words"]?.jsonArray?.mapNotNull { w ->
                try {
                    val o = w.jsonObject
                    val word = o["w"]?.jsonPrimitive?.content.orEmpty()
                    if (word.isEmpty()) return@mapNotNull null
                    WordInfo(
                        word = word,
                        lemma = o["lemma"]?.jsonPrimitive?.content.orEmpty(),
                        root = o["root"]?.jsonPrimitive?.content.orEmpty(),
                        pos = o["pos"]?.jsonPrimitive?.content.orEmpty(),
                    )
                } catch (_: Exception) {
                    null
                }
            }.orEmpty()
            if (words.isEmpty()) return@mapNotNull null
            AyahWords(ayah, words)
        } catch (_: Exception) {
            null
        }
    }
}

/** Loads one verified word file. Null when absent (Hafs-only dataset). */
fun loadWordsSurah(reader: AssetReader, surah: Int): List<AyahWords>? {
    val raw = reader.readText("assets/quran/words/hafs/$surah.json") ?: return null
    return parseWordsSurah(raw, surah)
}
