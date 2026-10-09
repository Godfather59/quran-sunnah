package com.godfather59.quransunnah.notifications

import com.godfather59.quransunnah.prayer.PrayerTimes

// Daily prayer reminder plan. Ported from schedulePrayerNotifications:
// stable ids 100+i, sunrise excluded, past prayers skipped. Delivery
// (AlarmManager / UNUserNotificationCenter) is platform/app-layer.

data class PrayerAlarm(
    val id: Int,
    val prayerKey: String,
    /** Minutes from local midnight. */
    val minutes: Int,
)

/** Stable per-prayer notification/alarm ids (sunrise never scheduled). */
fun prayerAlarmId(prayerKey: String): Int = when (prayerKey) {
    "fajr" -> 100
    "sunrise" -> 101
    "dhuhr" -> 102
    "asr" -> 103
    "maghrib" -> 104
    "isha" -> 105
    else -> 100 + (prayerKey.hashCode() and 0x7fffffff) % 100
}

/**
 * Alarms for prayers at/after [nowMinutes], sunrise excluded.
 * Ids are stable per prayer (not 100+i over the filtered list) so a
 * platform AlarmManager/PendingIntent can be cancelled or updated
 * regardless of what time of day the schedule is recomputed.
 */
fun upcomingAlarms(times: PrayerTimes, nowMinutes: Int): List<PrayerAlarm> {
    return times.ordered
        .filter { (key, at) -> key != "sunrise" && at >= nowMinutes }
        .map { (key, at) ->
            PrayerAlarm(id = prayerAlarmId(key), prayerKey = key, minutes = at)
        }
}
