package com.godfather59.quransunnah.data

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// Verified Tajweed annotation loader (Hafs/Uthmani ONLY, like the Medina
// page map and word morphology). assets/quran/tajweed/hafs/<surah>.json:
// {"surah":N,"entries":[{"ayah":N,"annotations":[{"rule","start","end"}]}]}
// Spans are [start, end) character offsets into the exact bundled Tanzil
// verse text. Rendering only colors spans; the verse text is never altered.
// Ported from tajweed_repository.dart (same rule palette + grey fallback).

data class TajweedSpan(
    val rule: String,
    val start: Int,
    val end: Int,
)

data class TajweedRuleInfo(
    val rule: String,
    val nameAr: String,
    val nameEn: String,
    /** ARGB color as in the Flutter palette (e.g. 0xFF2E7D32). */
    val colorValue: Long,
)

val tajweedRules: List<TajweedRuleInfo> = listOf(
    TajweedRuleInfo("ghunnah", "غنة", "Ghunnah", 0xFF2E7D32),
    TajweedRuleInfo("hamzat_wasl", "همزة الوصل", "Hamzat al-Wasl", 0xFF9E9E9E),
    TajweedRuleInfo("idghaam_ghunnah", "إدغام بغنة", "Idgham with Ghunnah", 0xFFEF6C00),
    TajweedRuleInfo("idghaam_no_ghunnah", "إدغام بغير غنة", "Idgham without Ghunnah", 0xFFF9A825),
    TajweedRuleInfo("idghaam_shafawi", "إدغام شفوي", "Idgham Shafawi", 0xFFFB8C00),
    TajweedRuleInfo("ikhfa", "إخفاء", "Ikhfa", 0xFF00838F),
    TajweedRuleInfo("ikhfa_shafawi", "إخفاء شفوي", "Ikhfa Shafawi", 0xFF00ACC1),
    TajweedRuleInfo("iqlab", "إقلاب", "Iqlab", 0xFF6A1B9A),
    TajweedRuleInfo("madd_2", "مد طبيعي", "Madd (2)", 0xFF1565C0),
    TajweedRuleInfo("madd_246", "مد جائز", "Madd (2/4/6)", 0xFF1E88E5),
    TajweedRuleInfo("madd_6", "مد لازم", "Madd (6)", 0xFF0D47A1),
    TajweedRuleInfo("qalqalah", "قلقلة", "Qalqalah", 0xFFC62828),
    TajweedRuleInfo("madd_muttasil", "مد متصل", "Madd Muttasil", 0xFF0D47A1),
    TajweedRuleInfo("madd_munfasil", "مد منفصل", "Madd Munfasil", 0xFF42A5F5),
    TajweedRuleInfo("lam_shamsiyyah", "لام شمسية", "Lam Shamsiyyah", 0xFF78909C),
    TajweedRuleInfo("silent", "حرف لا يُنطق", "Silent letter", 0xFFBDBDBD),
    TajweedRuleInfo("idghaam_mutajanisayn", "إدغام متجانسين", "Idgham Mutajanisayn", 0xFFFF8F00),
    TajweedRuleInfo("idghaam_mutaqaribayn", "إدغام متقاربين", "Idgham Mutaqaribayn", 0xFFFFB300),
)

/** Unknown rules from future datasets fall back to grey, never vanish. */
fun tajweedRuleInfo(rule: String): TajweedRuleInfo =
    tajweedRules.firstOrNull { it.rule == rule }
        ?: TajweedRuleInfo(rule, rule, rule, 0xFF757575)

/** Hafs/Uthmani is the only edition with verified Tajweed annotations. */
fun isTajweedEdition(editionId: String): Boolean =
    editionId == "hafs-an-asim__uthmani"

fun parseTajweedSurah(raw: String, expectedSurah: Int): Map<Int, List<TajweedSpan>> {
    val root = try {
        parseJsonObject(raw)
    } catch (_: Exception) {
        return emptyMap()
    }
    val surah = root["surah"]?.jsonPrimitive?.content?.toIntOrNull()
        ?: return emptyMap()
    if (surah != expectedSurah) return emptyMap()
    val entries = try {
        root["entries"]?.jsonArray ?: return emptyMap()
    } catch (_: Exception) {
        return emptyMap()
    }
    val out = LinkedHashMap<Int, MutableList<TajweedSpan>>()
    for (element in entries) {
        try {
            val m = element.jsonObject
            val ayah = m["ayah"]?.jsonPrimitive?.content?.toIntOrNull()
                ?: continue
            if (ayah <= 0) continue
            val annotations = try {
                m["annotations"]?.jsonArray ?: continue
            } catch (_: Exception) {
                continue
            }
            for (span in annotations) {
                try {
                    val o = span.jsonObject
                    val rule = o["rule"]?.jsonPrimitive?.content ?: continue
                    if (rule.isEmpty()) continue
                    val start = o["start"]?.jsonPrimitive?.content?.toIntOrNull()
                        ?: continue
                    val end = o["end"]?.jsonPrimitive?.content?.toIntOrNull()
                        ?: continue
                    if (start < 0 || end <= start) continue
                    out.getOrPut(ayah) { mutableListOf() }
                        .add(TajweedSpan(rule, start, end))
                } catch (_: Exception) {
                    continue
                }
            }
        } catch (_: Exception) {
            continue
        }
    }
    return out
}

/** Loads one verified Tajweed file. Null when absent (Hafs-only dataset). */
fun loadTajweedSurah(reader: AssetReader, surah: Int): Map<Int, List<TajweedSpan>>? {
    val raw = reader.readText("assets/quran/tajweed/hafs/$surah.json") ?: return null
    return parseTajweedSurah(raw, surah)
}
