package com.godfather59.quransunnah

import com.godfather59.quransunnah.notifications.prayerAlarmId
import com.godfather59.quransunnah.notifications.upcomingAlarms
import com.godfather59.quransunnah.prayer.PrayerCalcMethod
import com.godfather59.quransunnah.prayer.calculatePrayerTimes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrayerAlarmsTest {
    private val noonTimes = calculatePrayerTimes(
        year = 2026, month = 1, day = 15,
        latitude = 33.5731, longitude = -7.5898, tzOffsetHours = 1.0,
        method = PrayerCalcMethod.MOROCCO,
    )
    // fajr=07:10(430), sunrise=08:35(515), dhuhr=13:40(820),
    // asr=16:25(985), maghrib=18:44(1124), isha=20:09(1209).

    @Test
    fun skipsPastAndSunriseIdsFrom100() {
        val alarms = upcomingAlarms(noonTimes, 12 * 60)
        assertEquals(
            listOf("dhuhr", "asr", "maghrib", "isha"),
            alarms.map { it.prayerKey },
        )
        // Stable per-prayer ids (not 100+i over the filtered list).
        assertEquals(listOf(102, 103, 104, 105), alarms.map { it.id })
        assertEquals(820, alarms.first().minutes)
    }

    @Test
    fun emptyAfterIsha() {
        assertTrue(upcomingAlarms(noonTimes, 23 * 60).isEmpty())
    }

    @Test
    fun exactMinuteIsIncluded() {
        val alarms = upcomingAlarms(noonTimes, 820)
        assertEquals("dhuhr", alarms.first().prayerKey)
    }

    @Test
    fun idsAreStableAcrossTimesOfDay() {
        // Same prayer keeps its id whether scheduled at dawn or noon,
        // so AlarmManager cancel/update always hits the right PendingIntent.
        val dawn = upcomingAlarms(noonTimes, 0).associate { it.prayerKey to it.id }
        val noon = upcomingAlarms(noonTimes, 12 * 60).associate { it.prayerKey to it.id }
        assertEquals(dawn["dhuhr"], noon["dhuhr"])
        assertEquals(dawn["asr"], noon["asr"])
        assertEquals(100, prayerAlarmId("fajr"))
        assertEquals(102, prayerAlarmId("dhuhr"))
        assertEquals(103, prayerAlarmId("asr"))
        assertEquals(104, prayerAlarmId("maghrib"))
        assertEquals(105, prayerAlarmId("isha"))
    }
}
