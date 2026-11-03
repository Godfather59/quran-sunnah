package com.godfather59.quransunnah.search

import com.godfather59.quransunnah.text.normalizeArabic

/**
 * Search-term highlight ranges over display text.
 *
 * The search index matches diacritic-insensitively ([normalizeArabic]), so a
 * literal highlight would miss ("الحمد" never literally occurs in "ٱلْحَمْدُ").
 * This mirrors the index normalization on both sides while mapping every
 * normalized char back to its original index, so spans land on display text
 * that is never mutated. Latin matching is case-insensitive.
 *
 * Pure Kotlin (no Compose) so it is unit-testable and reusable on both
 * platforms. Returned ranges are sorted, merged (overlap + adjacency), and
 * end-inclusive.
 */
fun findHighlightRanges(text: String, query: String): List<IntRange> {
    if (text.isEmpty()) return emptyList()
    val tokens = query.split(' ', '\t', '\n', '\r').filter { it.isNotBlank() }
    if (tokens.isEmpty()) return emptyList()

    // Normalized haystack + back-map to original indices. Stripped chars
    // (tashkeel/tatweel) emit nothing; 1:1 maps (alef unify) emit one char.
    val norm = StringBuilder()
    val back = mutableListOf<Int>()
    for (i in text.indices) {
        val n = normalizeArabic(text[i].toString())
        for (ch in n) {
            norm.append(ch.lowercaseChar())
            back.add(i)
        }
    }
    if (norm.isEmpty()) return emptyList()
    val hay = norm.toString()

    val found = mutableListOf<IntRange>()
    for (token in tokens) {
        val needle = normalizeArabic(token).lowercase()
        if (needle.isEmpty()) continue
        var from = 0
        while (true) {
            val idx = hay.indexOf(needle, from)
            if (idx < 0) break
            found.add(back[idx]..back[idx + needle.length - 1])
            from = idx + 1
        }
    }
    if (found.isEmpty()) return emptyList()

    val sorted = found.sortedBy { it.first }
    val merged = mutableListOf(sorted[0])
    for (r in sorted.drop(1)) {
        val last = merged.last()
        if (r.first <= last.last + 1) {
            merged[merged.lastIndex] = last.first..maxOf(last.last, r.last)
        } else {
            merged.add(r)
        }
    }
    return merged
}
