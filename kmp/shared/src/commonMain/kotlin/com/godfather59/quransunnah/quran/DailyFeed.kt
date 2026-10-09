package com.godfather59.quransunnah.quran

// Deterministic daily ayah coordinate. Ported from _DailyAyah in
// lib/features/home/home_screen.dart: days-since-Jan-1 (0-based) mod 6236,
// walked across surah metadata in mushaf order.

fun dailyAyahRef(dayIndex: Int): Pair<Int, Int> {
    var remaining = ((dayIndex % 6236) + 6236) % 6236
    for (m in surahMetadata) {
        if (remaining < m.ayahCount) return Pair(m.number, remaining + 1)
        remaining -= m.ayahCount
    }
    // Unreachable: totals are exactly 6236 (count-tested).
    return Pair(114, 6)
}
