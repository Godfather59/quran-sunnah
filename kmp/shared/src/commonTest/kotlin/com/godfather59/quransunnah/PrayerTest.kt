package com.godfather59.quransunnah

import com.godfather59.quransunnah.prayer.PrayerCalcMethod
import com.godfather59.quransunnah.prayer.PrayerTimes
import com.godfather59.quransunnah.prayer.calculatePrayerTimes
import com.godfather59.quransunnah.prayer.localizedCityName
import com.godfather59.quransunnah.prayer.nextPrayer
import com.godfather59.quransunnah.prayer.prayerMethodFromFlutterName
import com.godfather59.quransunnah.prayer.prayerMethods
import com.godfather59.quransunnah.prayer.qiblaBearing
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Time vectors generated from the Dart implementation
// (tool/gen_kmp_vectors.dart, since removed). Exact HH:MM parity.
class PrayerTest {
    private fun PrayerTimes.formatted(): String =
        ordered.joinToString(",") { (k, v) -> "$k=${format(v)}" }

    @Test
    fun casablancaMoroccoVectors() {
        val t = calculatePrayerTimes(
            year = 2026, month = 1, day = 15,
            latitude = 33.5731, longitude = -7.5898, tzOffsetHours = 1.0,
            method = PrayerCalcMethod.MOROCCO,
        )
        assertEquals(
            "fajr=07:10,sunrise=08:35,dhuhr=13:40,asr=16:25,maghrib=18:44,isha=20:09",
            t.formatted(),
        )
    }

    @Test
    fun casablancaMwlVectors() {
        val t = calculatePrayerTimes(
            year = 2026, month = 1, day = 15,
            latitude = 33.5731, longitude = -7.5898, tzOffsetHours = 1.0,
            method = PrayerCalcMethod.MUSLIM_WORLD_LEAGUE,
        )
        assertEquals(
            "fajr=07:07,sunrise=08:35,dhuhr=13:40,asr=16:25,maghrib=18:44,isha=20:07",
            t.formatted(),
        )
    }

    @Test
    fun meccaUmmAlQuraVectors() {
        val t = calculatePrayerTimes(
            year = 2026, month = 6, day = 21,
            latitude = 21.4225, longitude = 39.8262, tzOffsetHours = 3.0,
            method = PrayerCalcMethod.UMM_AL_QURA,
        )
        assertEquals(
            "fajr=04:11,sunrise=05:39,dhuhr=12:22,asr=15:42,maghrib=19:06,isha=20:36",
            t.formatted(),
        )
    }

    @Test
    fun rawPrayerTimesStayOnTheirLocalDateAcrossMarchEquinox() {
        val places = listOf(
            Triple(21.4225, 39.8262, 3.0), // Mecca
            Triple(33.5731, -7.5898, 0.0), // Casablanca
            Triple(40.7128, -74.0060, -4.0), // New York
        )
        for (year in 2025..2027) {
            for ((latitude, longitude, offset) in places) {
                var previous: PrayerTimes? = null
                for (day in 20..23) {
                    val times = calculatePrayerTimes(year, 3, day, latitude, longitude, offset)
                    val label = "$year-03-$day at $latitude,$longitude"
                    val minutes = times.ordered.map { it.second }
                    // format() normalizes +/-1440 and would hide a date error.
                    assertTrue(minutes.all { it in 0 until 1440 }, "Raw times leave their local date: $label $minutes")
                    assertEquals(minutes.sorted(), minutes, "Prayer order changed: $label")
                    assertTrue(times.dhuhr in 11 * 60..15 * 60, "Dhuhr must be near local noon: $label")
                    previous?.ordered?.zip(times.ordered)?.forEach { (before, after) ->
                        assertTrue(abs(after.second - before.second) <= 10,
                            "Daily time discontinuity for ${after.first}: $label")
                    }
                    previous = times
                }
            }
        }
    }

    @Test
    fun cairoEgyptVectors() {
        val t = calculatePrayerTimes(
            year = 2026, month = 1, day = 15,
            latitude = 30.0444, longitude = 31.2357, tzOffsetHours = 2.0,
            method = PrayerCalcMethod.EGYPT,
        )
        assertEquals(
            "fajr=05:21,sunrise=06:52,dhuhr=12:04,asr=14:57,maghrib=17:17,isha=18:38",
            t.formatted(),
        )
    }

    @Test
    fun allMethodsRegistered() {
        assertEquals(7, prayerMethods.size)
    }

    @Test
    fun qiblaVectors() {
        assertEquals(93.6758, qiblaBearing(33.5731, -7.5898), 0.001)
        assertEquals(94.6176, qiblaBearing(34.0209, -6.8416), 0.001)
        assertEquals(0.0, qiblaBearing(21.4225, 39.8262), 0.001)
        assertEquals(295.1517, qiblaBearing(-6.2088, 106.8456), 0.001)
        assertEquals(58.4817, qiblaBearing(40.7128, -74.0060), 0.001)
    }

    @Test
    fun timesOrderedAndNextPrayer() {
        val t = calculatePrayerTimes(
            year = 2026, month = 1, day = 15,
            latitude = 33.5731, longitude = -7.5898, tzOffsetHours = 1.0,
            method = PrayerCalcMethod.MOROCCO,
        )
        val values = t.ordered.map { it.second }
        assertEquals(values.sorted(), values)
        // 12:00 -> dhuhr at 13:40.
        val next = nextPrayer(t, 12 * 60)
        assertEquals("dhuhr", next.key)
        assertEquals(t.dhuhr, next.minutes)
        // After isha -> tomorrow fajr.
        val wrapped = nextPrayer(t, 23 * 60 + 59)
        assertEquals("fajr", wrapped.key)
        assertTrue(wrapped.isTomorrow)
    }

    @Test
    fun cityNamesLocalized() {
        assertEquals("مكة", localizedCityName("Mecca", "ar"))
        assertEquals("الدار البيضاء", localizedCityName("Casablanca", "ar"))
        assertEquals("La Mecque", localizedCityName("Mecca", "fr"))
        assertEquals("Mecca", localizedCityName("Mecca", "en"))
        assertEquals("GPS 1.0, 2.0", localizedCityName("GPS 1.0, 2.0", "ar"))
    }

    @Test
    fun flutterNameMappingMatchesAndroidPrefs() {
        assertEquals(PrayerCalcMethod.MUSLIM_WORLD_LEAGUE, prayerMethodFromFlutterName("muslimWorldLeague"))
        assertEquals(PrayerCalcMethod.ISNA, prayerMethodFromFlutterName("isna"))
        assertEquals(PrayerCalcMethod.EGYPT, prayerMethodFromFlutterName("egypt"))
        assertEquals(PrayerCalcMethod.UMM_AL_QURA, prayerMethodFromFlutterName("ummAlQura"))
        assertEquals(PrayerCalcMethod.KARACHI, prayerMethodFromFlutterName("karachi"))
        assertEquals(PrayerCalcMethod.JAFARI, prayerMethodFromFlutterName("jafari"))
        assertEquals(PrayerCalcMethod.MOROCCO, prayerMethodFromFlutterName("morocco"))
        assertEquals(PrayerCalcMethod.MOROCCO, prayerMethodFromFlutterName("habous"))
        assertEquals(PrayerCalcMethod.MUSLIM_WORLD_LEAGUE, prayerMethodFromFlutterName("bogus"))
        assertEquals(PrayerCalcMethod.MUSLIM_WORLD_LEAGUE, prayerMethodFromFlutterName(""))
    }
}
