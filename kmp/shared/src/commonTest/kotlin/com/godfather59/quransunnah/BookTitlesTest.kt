package com.godfather59.quransunnah

import com.godfather59.quransunnah.hadith.bookDisplayTitle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BookTitlesTest {
    private fun covers(collectionId: String, sections: IntRange) {
        for (section in sections) {
            val title = bookDisplayTitle(collectionId, section, "English $section", "ar")
            assertTrue(title.isNotBlank() && title != "English $section", "$collectionId $section")
        }
    }

    @Test
    fun allCollectionsCoveredInArabic() {
        covers("bukhari", 1..97)
        covers("muslim", 0..56)
        covers("abudawud", 1..43)
        covers("tirmidhi", 1..49)
        covers("nasai", 1..51)
        covers("ibnmajah", 0..37)
        covers("malik", 1..61)
        covers("nawawi", 1..1)
        covers("qudsi", 1..1)
        covers("dehlawi", 1..1)
    }

    @Test
    fun spotChecks() {
        assertEquals("بدء الوحي", bookDisplayTitle("bukhari", 1, "Revelation", "ar"))
        assertEquals("الإيمان", bookDisplayTitle("muslim", 1, "The Book of Faith", "ar"))
        assertEquals("الصلاة", bookDisplayTitle("abudawud", 2, "Prayer (Kitab Al-Salat)", "ar"))
        assertEquals("الصوم", bookDisplayTitle("tirmidhi", 8, "The Book on Fasting", "ar"))
        assertEquals("الحج", bookDisplayTitle("nasai", 24, "The Book of Hajj", "ar"))
        assertEquals("الزهد", bookDisplayTitle("ibnmajah", 37, "Zuhd", "ar"))
        assertEquals("الحج", bookDisplayTitle("malik", 20, "Hajj", "ar"))
        assertEquals("الأربعون النووية", bookDisplayTitle("nawawi", 1, "Forty Hadith of an-Nawawi", "ar"))
    }

    @Test
    fun fallbackKeepsSource() {
        assertEquals("Revelation", bookDisplayTitle("bukhari", 1, "Revelation", "en"))
        assertEquals("Revelation", bookDisplayTitle("bukhari", 1, "Revelation", "fr"))
        assertEquals("Unknown Book", bookDisplayTitle("muslim", 200, "Unknown Book", "ar"))
        assertEquals("5–9", bookDisplayTitle("bukhari", 200, "", "ar", 5, 9))
    }
}
