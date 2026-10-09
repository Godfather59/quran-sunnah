package com.godfather59.quransunnah

import com.godfather59.quransunnah.text.normalizeArabic
import com.godfather59.quransunnah.text.normalizeLatin
import com.godfather59.quransunnah.text.parseQuranRef
import com.godfather59.quransunnah.text.toArabicIndic
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// Vectors generated from the Dart implementation
// (tool/gen_kmp_vectors.dart, since removed). Byte-exact parity.
class TextUtilsTest {
    @Test
    fun normalizeArabicVectors() {
        val cases = listOf(
            "بِسْمِ ٱللَّهِ" to "بسم الله",
            "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ" to "الحمد لله رب العٰلمين",
            "الٓمٓ" to "الم",
            "مَٰلِكِ يَوْمِ ٱلدِّينِ" to "مٰلك يوم الدين",
            "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ" to "اياك نعبد واياك نستعين",
            "ٱهْدِنَا ٱلصِّرَٰطَ ٱلْمُسْتَقِيمَ" to "اهدنا الصرٰط المستقيم",
            "صِرَٰطَ ٱلَّذِينَ أَنْعَمْتَ عَلَيْهِمْ" to "صرٰط الذين انعمت عليهم",
            "ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ" to "الله لا الٰه الا هو",
            "  مُحَمَّدٌ  " to "محمد",
            "التَّوْبَة" to "التوبه",
            "ٱقْرَأْ بِٱسْمِ رَبِّكَ" to "اقرا باسم ربك",
        )
        for ((input, expected) in cases) {
            assertEquals(expected, normalizeArabic(input), "input=$input")
        }
    }

    @Test
    fun normalizeArabicEdgeCases() {
        assertEquals("", normalizeArabic(""))
        assertEquals("", normalizeArabic("   "))
        // Superscript alef U+0670 is NOT stripped (Dart behavior).
        assertEquals("مٰلك", normalizeArabic("مَٰلِكِ"))
    }

    @Test
    fun normalizeLatinAscii() {
        assertEquals("hello world", normalizeLatin("  Hello World "))
    }

    @Test
    fun toArabicIndicVectors() {
        assertEquals("٢٥٥", toArabicIndic(255))
        assertEquals("٦٢٣٦", toArabicIndic(6236))
        assertEquals("٢:٢٥٥", toArabicIndic(2) + ":" + toArabicIndic(255))
    }

    @Test
    fun parseQuranRefForms() {
        assertEquals(Pair(2, 255), parseQuranRef("2:255"))
        assertEquals(Pair(2, 255), parseQuranRef("  2 : 255  "))
        assertEquals(Pair(2, 255), parseQuranRef("/quran/2/255"))
        assertEquals(Pair(2, 255), parseQuranRef("quran://2/255"))
        assertEquals(Pair(114, 6), parseQuranRef("114:6"))
    }

    @Test
    fun parseQuranRefInvalid() {
        assertNull(parseQuranRef(""))
        assertNull(parseQuranRef("hello"))
        assertNull(parseQuranRef("2:"))
        assertNull(parseQuranRef(":255"))
        assertNull(parseQuranRef("2:255:256"))
        assertNull(parseQuranRef("2222:1"))
    }
}
