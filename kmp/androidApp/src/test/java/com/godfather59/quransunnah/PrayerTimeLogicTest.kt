package com.godfather59.quransunnah

import com.godfather59.quransunnah.prayer.PrayerCalcMethod
import com.godfather59.quransunnah.prayer.calculatePrayerTimes
import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerTimeLogicTest {
    private fun instant(zone: String, year: Int, month: Int, day: Int, hour: Int, minute: Int = 0): Long =
        Calendar.getInstance(TimeZone.getTimeZone(zone)).apply {
            clear()
            set(year, month - 1, day, hour, minute, 0)
        }.timeInMillis

    @Test
    fun `positive offset local midnight is on previous UTC day`() {
        val place = resolvePrayerPlace(PrayerPrefsState(city = "Mecca"))
        val date = prayerCalendar(place, instant("UTC", 2026, 10, 28, 23))
        assertEquals(29, date.get(Calendar.DAY_OF_MONTH))
        assertEquals(instant("UTC", 2026, 10, 28, 21), prayerTimeMillis(date, 0))
    }

    @Test
    fun `selected city date and clock ignore device timezone`() {
        val oldZone = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
            val at = instant("UTC", 2026, 10, 28, 23)
            val snapshot = prayerSnapshot(PrayerPrefsState(city = "Mecca"), at)
            assertEquals(2 * 60, snapshot.nowMinutes)
            val expected = calculatePrayerTimes(2026, 10, 29, 21.4225, 39.8262, 3.0)
            assertEquals(expected, snapshot.times)
        } finally {
            TimeZone.setDefault(oldZone)
        }
    }

    @Test
    fun `presets use date-specific daylight saving offsets`() {
        val prefs = PrayerPrefsState(city = "London")
        val winter = prayerSnapshot(prefs, instant("UTC", 2026, 1, 15, 12))
        val summer = prayerSnapshot(prefs, instant("UTC", 2026, 6, 15, 12))
        assertEquals("Europe/London", summer.place.timeZoneId)
        assertEquals(0.0, winter.place.tzOffsetHours, 0.0)
        assertEquals(1.0, summer.place.tzOffsetHours, 0.0)
        assertEquals(
            calculatePrayerTimes(2026, 6, 15, 51.5074, -0.1278, 1.0),
            summer.times,
        )
    }

    @Test
    fun `Morocco preset follows Ramadan clock change without GPS refresh`() {
        val prefs = PrayerPrefsState(city = "Casablanca", tzOffsetHours = 1.0)
        val january = prayerSnapshot(prefs, instant("UTC", 2026, 1, 15, 12))
        val ramadan = prayerSnapshot(prefs, instant("UTC", 2026, 2, 28, 12))
        assertEquals(1.0, january.place.tzOffsetHours, 0.0)
        assertEquals(0.0, ramadan.place.tzOffsetHours, 0.0)
    }

    @Test
    fun `legacy GPS keeps its saved half-hour offset until refreshed`() {
        val prefs = PrayerPrefsState(
            city = "GPS 28.61, 77.21", latitude = 28.61, longitude = 77.21,
            tzOffsetHours = 5.5,
        )
        val place = resolvePrayerPlace(prefs)
        val date = prayerCalendar(place, instant("UTC", 2026, 1, 15, 0))
        assertEquals(5, date.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, date.get(Calendar.MINUTE))
        assertEquals(instant("UTC", 2026, 1, 14, 18, 30), prayerTimeMillis(date, 0))
    }

    @Test
    fun `next day uses local calendar across a 23 hour DST day`() {
        val place = resolvePrayerPlace(PrayerPrefsState(city = "London"))
        val before = prayerCalendar(place, instant("UTC", 2026, 3, 29, 12))
        val after = (before.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        assertEquals(23 * 3_600_000L, prayerTimeMillis(after, 0) - prayerTimeMillis(before, 0))
    }

    @Test
    fun `after Isha all five reminders roll into tomorrow with newly calculated Fajr`() {
        val prefs = PrayerPrefsState(city = "Mecca", methodName = "ummAlQura")
        val now = instant("Asia/Riyadh", 2026, 6, 21, 23)
        val alarms = nextPrayerAlarms(prefs, now)
        assertEquals(5, alarms.size)
        assertEquals(setOf(100, 102, 103, 104, 105), alarms.map { it.id }.toSet())
        assertFalse(alarms.any { it.key == "sunrise" })
        assertTrue(alarms.all { it.atMillis > now })
        val tomorrow = calculatePrayerTimes(
            2026, 6, 22, 21.4225, 39.8262, 3.0, PrayerCalcMethod.UMM_AL_QURA,
        )
        val expectedFajr = instant("Asia/Riyadh", 2026, 6, 22, tomorrow.fajr / 60, tomorrow.fajr % 60)
        assertEquals(expectedFajr, alarms.first { it.key == "fajr" }.atMillis)
        val snapshot = prayerSnapshot(prefs, now)
        assertTrue(snapshot.next.isTomorrow)
        assertEquals(expectedFajr, snapshot.nextAtMillis)
    }

    @Test
    fun `delivered prayer advances to next day while later prayers stay today`() {
        val prefs = PrayerPrefsState(city = "Mecca")
        val initial = nextPrayerAlarms(prefs, instant("Asia/Riyadh", 2026, 1, 15, 0))
        val fajr = initial.first { it.key == "fajr" }
        val renewed = nextPrayerAlarms(prefs, fajr.atMillis + 1)
        assertTrue(renewed.first { it.key == "fajr" }.atMillis > fajr.atMillis + 12 * 3_600_000L)
        assertEquals(initial.first { it.key == "dhuhr" }, renewed.first { it.key == "dhuhr" })
        assertTrue(renewed.all { it.atMillis > fajr.atMillis })
    }

    @Test
    fun `March equinox reminders always target future occurrences without a day shift`() {
        val prefs = PrayerPrefsState(city = "Mecca", methodName = "ummAlQura")
        for (year in 2025..2027) {
            for (day in 20..23) {
                for (hour in listOf(0, 12, 23)) {
                    val now = instant("Asia/Riyadh", year, 3, day, hour)
                    val label = "$year-03-$day $hour:00"
                    val alarms = nextPrayerAlarms(prefs, now)
                    assertEquals(5, alarms.size)
                    assertEquals(5, alarms.map { it.id }.distinct().size)
                    assertTrue("Past or skipped-day alarm at $label", alarms.all {
                        it.atMillis > now && it.atMillis < now + 30 * 3_600_000L
                    })
                    val next = prayerSnapshot(prefs, now).nextAtMillis
                    assertTrue("Next-prayer date shifted at $label",
                        next > now && next < now + 30 * 3_600_000L)
                }
            }
        }
    }
}
