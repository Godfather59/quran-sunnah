package com.godfather59.quransunnah

import com.godfather59.quransunnah.search.findHighlightRanges
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HighlightTest {
    @Test
    fun emptyQueryYieldsNoRanges() {
        assertEquals(emptyList(), findHighlightRanges("hello world", ""))
        assertEquals(emptyList(), findHighlightRanges("hello world", "   "))
    }

    @Test
    fun emptyTextYieldsNoRanges() {
        assertEquals(emptyList(), findHighlightRanges("", "hello"))
    }

    @Test
    fun exactSingleToken() {
        assertEquals(listOf(6..10), findHighlightRanges("hello world", "world"))
    }

    @Test
    fun latinIsCaseInsensitive() {
        assertEquals(listOf(0..4), findHighlightRanges("Hello world", "hELLo"))
    }

    @Test
    fun multipleTokens() {
        assertEquals(
            listOf(0..4, 6..10),
            findHighlightRanges("hello world", "hello world"),
        )
    }

    @Test
    fun overlappingMatchesMerge() {
        assertEquals(listOf(0..2), findHighlightRanges("aaa", "aa"))
    }

    @Test
    fun adjacentMatchesMerge() {
        assertEquals(listOf(0..3), findHighlightRanges("aabb", "aa bb"))
    }

    @Test
    fun noMatchYieldsEmpty() {
        assertEquals(emptyList(), findHighlightRanges("hello", "xyz"))
    }

    @Test
    fun diacriticsIgnoredLikeIndex() {
        // "ٱلْحَمْدُ" normalizes to "الحمد": the span covers display chars
        // 0..7 (the trailing damma is stripped, like the index does).
        val text = "ٱلْحَمْدُ لِلَّهِ"
        assertEquals(listOf(0..7), findHighlightRanges(text, "الحمد"))
    }

    @Test
    fun alefFormsUnified() {
        assertEquals(listOf(0..3), findHighlightRanges("أحمد", "احمد"))
    }

    @Test
    fun verseShortcutTokenIsHarmless() {
        // "2:255" rarely occurs literally; must not crash or mis-span.
        val ranges = findHighlightRanges("Al-Baqarah 2:255", "2:255")
        assertTrue(ranges.all { it.first >= 0 && it.last < "Al-Baqarah 2:255".length })
    }
}
