package com.godfather59.quransunnah

import com.godfather59.quransunnah.prefs.Prefs
import com.godfather59.quransunnah.prefs.decodeList
import com.godfather59.quransunnah.prefs.dhikrDayKey
import com.godfather59.quransunnah.prefs.encodeList
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PrefsJvmTest {
    private fun prefs() = Prefs(MapSettings(mutableMapOf()))

    @Test
    fun listCodecRoundTrip() {
        assertEquals(emptyList(), "".decodeList())
        val values = listOf("a", "b:c", "دعاء", "x y")
        assertEquals(values, values.encodeList().decodeList())
    }

    @Test
    fun dhikrDayKeyFormat() {
        assertEquals("dhikr.2026-10-07", dhikrDayKey(2026, 10, 7))
        assertEquals("dhikr.2026-01-05", dhikrDayKey(2026, 1, 5))
    }

    @Test
    fun quranDefaults() {
        val p = prefs()
        assertNull(p.quranRiwayaName)
        assertEquals(24.0, p.quranFontSize)
        assertEquals(1.9, p.quranLineHeight)
        assertEquals(listOf("en-sahih"), p.quranTranslations)
        assertEquals("jalalayn", p.quranTafsirId)
        assertEquals(2, p.quranLastSurah)
        assertEquals(255, p.quranLastAyah)
        assertTrue(p.quranShowTranslation)
    }

    @Test
    fun quranWriteRead() {
        val p = prefs()
        p.quranRiwayaName = "hafsAsim"
        p.quranTranslations = listOf("en-sahih", "fr-hamidullah")
        p.quranLastSurah = 114
        p.quranShowTajweed = true
        assertEquals("hafsAsim", p.quranRiwayaName)
        assertEquals(listOf("en-sahih", "fr-hamidullah"), p.quranTranslations)
        assertEquals(114, p.quranLastSurah)
        assertTrue(p.quranShowTajweed)
        p.quranRiwayaName = null
        assertNull(p.quranRiwayaName)
    }

    @Test
    fun appDefaultsAndWrite() {
        val p = prefs()
        assertEquals("ar", p.appLocale)
        assertNull(p.appThemeName)
        assertEquals("", p.appQari)
        assertEquals(1.0, p.appPlaybackSpeed)
        assertTrue(!p.appOnboarded)
        assertTrue(p.appDisplaySanad)
        p.appLocale = "fr"
        p.appOnboarded = true
        p.appThemeName = "dark"
        assertEquals("fr", p.appLocale)
        assertTrue(p.appOnboarded)
        assertEquals("dark", p.appThemeName)
    }

    @Test
    fun prayerNullableRoundTrip() {
        val p = prefs()
        assertNull(p.prayerLat)
        assertNull(p.prayerTz)
        p.prayerLat = 33.5731
        p.prayerLng = -7.5898
        p.prayerCity = "Casablanca"
        p.prayerTz = 1.0
        p.prayerNotif = true
        assertEquals(33.5731, p.prayerLat)
        assertEquals("Casablanca", p.prayerCity)
        assertTrue(p.prayerNotif)
        // Clearing the timezone removes the key (clearTz parity).
        p.prayerTz = null
        assertNull(p.prayerTz)
    }

    @Test
    fun homeKhatmaMemorizedDhikr() {
        val p = prefs()
        assertNull(p.homeOrder)
        assertEquals(emptyList(), p.homeHidden)
        p.homeOrder = listOf("a", "b")
        p.homeHidden = listOf("b")
        assertEquals(listOf("a", "b"), p.homeOrder)
        assertEquals(0, p.khatmaStreak)
        p.khatmaStreak = 5
        p.khatmaLastDayMs = 123
        assertEquals(5, p.khatmaStreak)
        assertEquals(123, p.khatmaLastDayMs)
        p.memorizedAyahs = listOf("1:1", "1:2")
        assertEquals(listOf("1:1", "1:2"), p.memorizedAyahs)
        p.setDhikrDay("dhikr.2026-10-07", listOf("a:3"))
        assertEquals(listOf("a:3"), p.dhikrDay("dhikr.2026-10-07"))
        p.dhikrTotal = 42
        assertEquals(42, p.dhikrTotal)
    }

    @Test
    fun searchHistoryTrimDedupMax10() {
        val p = prefs()
        assertEquals(emptyList(), p.searchHistory())
        p.pushSearchHistory("  ")
        assertEquals(emptyList(), p.searchHistory())
        p.pushSearchHistory("alpha")
        p.pushSearchHistory("beta")
        p.pushSearchHistory("alpha")
        assertEquals(listOf("alpha", "beta"), p.searchHistory())
        for (i in 1..12) {
            p.pushSearchHistory("q$i")
        }
        val history = p.searchHistory()
        assertEquals(10, history.size)
        assertEquals("q12", history.first())
    }
}
