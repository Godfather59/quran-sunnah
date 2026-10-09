package com.godfather59.quransunnah.data

import com.godfather59.quransunnah.quran.Ayah
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// Verified Quran text loader. Files are `surah|ayah|text` per line.
// Mirrors verified_asset_quran_repository: only exact rows from the file
// become Ayah.text; anything else is a placeholder upstream.

/** Edition id -> bundled asset path for the verified core editions. */
val verifiedQuranAssets: Map<String, String> = mapOf(
    "hafs-an-asim__uthmani" to "assets/quran/hafs-an-asim/uthmani.txt",
    "hafs-an-asim__imlai" to "assets/quran/hafs-an-asim/imlai.txt",
    "hafs-an-asim__indopak" to "assets/quran/hafs-an-asim/indopak.txt",
    "warsh-an-nafi__uthmani" to "assets/quran/warsh-an-nafi/uthmani.txt",
    "qalun-an-nafi__uthmani" to "assets/quran/qalun-an-nafi/uthmani.txt",
)

/** Tafsir id -> bundled per-surah JSON directory. */
val tafsirAssets: Map<String, String> = mapOf(
    "jalalayn" to "assets/quran/tafsir/jalalayn",
    "siraj" to "assets/quran/tafsir/siraj",
)

/** Parses `surah|ayah|text` lines. Blank/malformed/empty-text rows are dropped. */
fun parseQuranLines(raw: String, editionId: String): List<Ayah> {
    val out = mutableListOf<Ayah>()
    // Defensive: some verified exports carry a UTF-8 BOM; Kotlin's trim()
    // does not strip U+FEFF, so the first "1|1|..." line would fail to parse.
    val clean = raw.trimStart('\uFEFF')
    for (rawLine in clean.lineSequence()) {
        // Strip a stray BOM if it appears mid-stream plus CRLF artifacts.
        val line = rawLine.trimStart('\uFEFF').trimEnd('\r')
        if (line.isBlank()) continue
        val parts = line.split('|', limit = 3)
        if (parts.size != 3) continue
        val surah = parts[0].trim().trimStart('\uFEFF').toIntOrNull() ?: continue
        val ayah = parts[1].trim().toIntOrNull() ?: continue
        val text = parts[2].trimEnd('\r')
        if (surah !in 1..114 || ayah < 1 || text.isEmpty()) continue
        out.add(Ayah(surah = surah, ayah = ayah, text = text, editionId = editionId))
    }
    return out
}

/** Loads one verified edition. Null when the asset is absent. */
fun loadQuranEdition(
    reader: AssetReader,
    editionId: String,
): List<Ayah>? {
    val path = verifiedQuranAssets[editionId] ?: return null
    val raw = reader.readText(path) ?: return null
    return parseQuranLines(raw, editionId)
}

/** Translation/edition-agnostic `surah|ayah|text` map: "surah:ayah" -> text. */
fun parsePipeMap(raw: String): Map<String, String> {
    val out = LinkedHashMap<String, String>()
    val clean = raw.trimStart('\uFEFF')
    for (rawLine in clean.lineSequence()) {
        val line = rawLine.trimStart('\uFEFF').trimEnd('\r')
        if (line.isBlank()) continue
        val parts = line.split('|', limit = 3)
        if (parts.size != 3) continue
        val s = parts[0].trim().trimStart('\uFEFF')
        val a = parts[1].trim()
        // Validate numeric coordinates so "a:b" keys never enter the map.
        if (s.toIntOrNull() == null || a.toIntOrNull() == null) continue
        val key = "$s:$a"
        val text = parts[2].trimEnd('\r')
        if (text.isNotEmpty()) {
            out[key] = text
        }
    }
    return out
}

/** Parses per-surah Tafsir JSON into ayah -> verified commentary text. */
fun parseTafsirSurahJson(raw: String, expectedSurah: Int): Map<Int, String> {
    val parsed = try {
        Json.parseToJsonElement(raw.trimStart('\uFEFF')).jsonObject
    } catch (_: Exception) {
        return emptyMap()
    }
    val surah = parsed["surah"]?.jsonPrimitive?.content?.toIntOrNull()
        ?: return emptyMap()
    if (surah != expectedSurah) return emptyMap()
    val entries = parsed["entries"]?.jsonArray ?: return emptyMap()
    val out = LinkedHashMap<Int, String>()
    for (entry in entries) {
        val obj = entry.jsonObject
        val ayah = obj["ayah"]?.jsonPrimitive?.content?.toIntOrNull()
            ?: continue
        val text = obj["text"]?.jsonPrimitive?.content.orEmpty()
        if (ayah > 0 && text.isNotBlank()) {
            out[ayah] = text
        }
    }
    return out
}

/** Loads one verified tafsir surah. Null when the tafsir id/path is absent. */
fun loadTafsirSurah(
    reader: AssetReader,
    tafsirId: String,
    surah: Int,
): Map<Int, String>? {
    val dir = tafsirAssets[tafsirId] ?: return null
    val raw = reader.readText("$dir/$surah.json") ?: return null
    return parseTafsirSurahJson(raw, surah)
}
