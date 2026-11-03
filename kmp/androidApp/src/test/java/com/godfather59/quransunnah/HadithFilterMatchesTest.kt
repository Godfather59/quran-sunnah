package com.godfather59.quransunnah

import com.godfather59.quransunnah.hadith.Hadith
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the filter-sheet predicate extracted into pure code
 * (`HadithFilterState.hadithFilterMatches`). Runs on JVM, no Android context.
 */
class HadithFilterMatchesTest {

    private fun hadith(
        book: String = "",
        number: String = "",
        grade: String? = null,
    ) = Hadith(
        id = "bukhari:$number",
        collectionId = "bukhari",
        book = book,
        bookAr = "",
        chapter = "",
        chapterAr = "",
        hadithNumber = number,
        matnAr = "نص",
        grade = grade,
    )

    @Test
    fun `empty filters match every hadith`() {
        val h = hadith(book = "باب الإيمان", number = "402.2", grade = null)
        assertTrue(hadithFilterMatches(h, bookQuery = "", numberQuery = "", grades = emptySet()))
    }

    @Test
    fun `blank book query is treated as no filter`() {
        val h = hadith(book = "Book of Faith")
        assertTrue(hadithFilterMatches(h, bookQuery = "   ", numberQuery = "", grades = emptySet()))
    }

    @Test
    fun `book query matches case-insensitively and trims`() {
        val h = hadith(book = "Book of Faith")
        assertTrue(hadithFilterMatches(h, bookQuery = "  faith ", numberQuery = "", grades = emptySet()))
        assertFalse(hadithFilterMatches(h, bookQuery = "purification", numberQuery = "", grades = emptySet()))
    }

    @Test
    fun `number query is exact, not partial`() {
        val h = hadith(number = "402.2")
        assertTrue(hadithFilterMatches(h, bookQuery = "", numberQuery = "402.2", grades = emptySet()))
        assertFalse(hadithFilterMatches(h, bookQuery = "", numberQuery = "402", grades = emptySet()))
        assertTrue(hadithFilterMatches(h, bookQuery = "", numberQuery = "  402.2  ", grades = emptySet()))
    }

    @Test
    fun `grade matches when any selected grade is contained`() {
        val h = hadith(grade = "Sahih al-Bukhari")
        assertTrue(
            hadithFilterMatches(h, bookQuery = "", numberQuery = "", grades = setOf("Hasan", "Sahih")),
        )
        assertFalse(
            hadithFilterMatches(h, bookQuery = "", numberQuery = "", grades = setOf("Daif")),
        )
    }

    @Test
    fun `ungraded hadith never matches a grade filter`() {
        val h = hadith(grade = null)
        assertFalse(
            hadithFilterMatches(h, bookQuery = "", numberQuery = "", grades = setOf("Sahih")),
        )
    }

    @Test
    fun `filters combine conjunctively`() {
        val h = hadith(book = "Book of Prayer", number = "150", grade = "Sahih")
        assertTrue(hadithFilterMatches(h, bookQuery = "prayer", numberQuery = "150", grades = setOf("Sahih")))
        assertFalse(hadithFilterMatches(h, bookQuery = "faith", numberQuery = "150", grades = setOf("Sahih")))
        assertFalse(hadithFilterMatches(h, bookQuery = "prayer", numberQuery = "999", grades = setOf("Sahih")))
        assertFalse(hadithFilterMatches(h, bookQuery = "prayer", numberQuery = "150", grades = setOf("Daif")))
    }
}
