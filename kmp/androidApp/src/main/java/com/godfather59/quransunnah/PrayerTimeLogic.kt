package com.godfather59.quransunnah

import com.godfather59.quransunnah.notifications.prayerAlarmId
import com.godfather59.quransunnah.prayer.NextPrayer
import com.godfather59.quransunnah.prayer.PrayerCalcMethod
import com.godfather59.quransunnah.prayer.PrayerTimes
import com.godfather59.quransunnah.prayer.calculatePrayerTimes
import com.godfather59.quransunnah.prayer.prayerCityPresets
import com.godfather59.quransunnah.prayer.prayerMethodFromFlutterName
import java.util.Calendar
import java.util.SimpleTimeZone
import java.util.TimeZone

internal data class ResolvedPrayerPlace(
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val tzOffsetHours: Double,
    val timeZoneId: String? = null,
) {
    val timeZone: TimeZone
        get() = timeZoneId?.takeIf { it in availableTimeZones }?.let(TimeZone::getTimeZone)
            ?: SimpleTimeZone((tzOffsetHours * 3_600_000).toInt(), "Saved UTC offset")
}

private val availableTimeZones by lazy { TimeZone.getAvailableIDs().toSet() }

internal fun resolvePrayerPlace(prefs: PrayerPrefsState): ResolvedPrayerPlace {
    val preset = prayerCityPresets.firstOrNull { it.name == prefs.city }
    val hasCoordinates = prefs.latitude != null && prefs.longitude != null
    val fallback = preset ?: prayerCityPresets.first()
    return ResolvedPrayerPlace(
        city = if (hasCoordinates) prefs.city.ifBlank { "GPS" } else fallback.name,
        latitude = if (hasCoordinates) prefs.latitude!! else fallback.latitude,
        longitude = if (hasCoordinates) prefs.longitude!! else fallback.longitude,
        tzOffsetHours = prefs.tzOffsetHours ?: fallback.tzOffsetHours,
        timeZoneId = prefs.timeZoneId?.takeIf { it in availableTimeZones }
            ?: if (hasCoordinates && preset == null) null else fallback.timeZoneId,
    )
}

internal data class PrayerSnapshot(
    val method: PrayerCalcMethod,
    val place: ResolvedPrayerPlace,
    val times: PrayerTimes,
    val next: NextPrayer,
    val nowMinutes: Int,
    val nextAtMillis: Long,
)

/** Calendar in the selected place, independent of the device's timezone. */
internal fun prayerCalendar(place: ResolvedPrayerPlace, atMillis: Long): Calendar =
    Calendar.getInstance(place.timeZone).apply { timeInMillis = atMillis }

private fun prayerTimesForDate(
    method: PrayerCalcMethod,
    place: ResolvedPrayerPlace,
    localDate: Calendar,
): PrayerTimes {
    val noon = (localDate.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calculatePrayerTimes(
        localDate.get(Calendar.YEAR), localDate.get(Calendar.MONTH) + 1,
        localDate.get(Calendar.DAY_OF_MONTH), place.latitude, place.longitude,
        place.timeZone.getOffset(noon.timeInMillis) / 3_600_000.0, method,
    )
}

/** Convert local wall-clock minutes on a local date, including DST boundaries. */
internal fun prayerTimeMillis(localDate: Calendar, minutes: Int): Long =
    (localDate.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, minutes)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

internal fun prayerSnapshot(
    methodName: String,
    city: String,
    latitude: Double?,
    longitude: Double?,
    tzOffsetHours: Double?,
    atMillis: Long,
    timeZoneId: String? = null,
): PrayerSnapshot = prayerSnapshot(
    PrayerPrefsState(methodName, city, latitude, longitude, tzOffsetHours, timeZoneId = timeZoneId),
    atMillis,
)

internal fun prayerSnapshot(prefs: PrayerPrefsState, atMillis: Long): PrayerSnapshot {
    val method = prayerMethodFromFlutterName(prefs.methodName)
    val place = resolvePrayerPlace(prefs)
    return snapshotForPlace(method, place, atMillis)
}

internal fun refreshPrayerSnapshot(snapshot: PrayerSnapshot, atMillis: Long): PrayerSnapshot =
    snapshotForPlace(snapshot.method, snapshot.place, atMillis)

private fun snapshotForPlace(
    method: PrayerCalcMethod,
    place: ResolvedPrayerPlace,
    atMillis: Long,
): PrayerSnapshot {
    val today = prayerCalendar(place, atMillis)
    val times = prayerTimesForDate(method, place, today)
    val upcoming = times.ordered.firstOrNull { prayerTimeMillis(today, it.second) > atMillis }
    val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
    val nextMinutes = upcoming?.second ?: prayerTimesForDate(method, place, tomorrow).fajr
    val next = NextPrayer(upcoming?.first ?: "fajr", nextMinutes, upcoming == null)
    return PrayerSnapshot(
        method = method,
        place = place.copy(tzOffsetHours = place.timeZone.getOffset(atMillis) / 3_600_000.0),
        times = times,
        next = next,
        nowMinutes = today.get(Calendar.HOUR_OF_DAY) * 60 + today.get(Calendar.MINUTE),
        nextAtMillis = prayerTimeMillis(if (upcoming == null) tomorrow else today, nextMinutes),
    )
}

internal fun remainingPrayerMinutes(snapshot: PrayerSnapshot, atMillis: Long): Int =
    ((snapshot.nextAtMillis - atMillis).coerceAtLeast(0L) + 59_999L).div(60_000L).toInt()

internal data class ScheduledPrayer(val id: Int, val key: String, val atMillis: Long)

/** One future occurrence per prayer; every delivered alarm renews this window. */
internal fun nextPrayerAlarms(prefs: PrayerPrefsState, atMillis: Long): List<ScheduledPrayer> {
    val place = resolvePrayerPlace(prefs)
    val method = prayerMethodFromFlutterName(prefs.methodName)
    val today = prayerCalendar(place, atMillis)
    val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
    val todayTimes = prayerTimesForDate(method, place, today)
    val tomorrowTimes = prayerTimesForDate(method, place, tomorrow).ordered.toMap()
    return todayTimes.ordered.filter { it.first != "sunrise" }.map { (key, minutes) ->
        val todayAt = prayerTimeMillis(today, minutes)
        ScheduledPrayer(
            prayerAlarmId(key), key,
            if (todayAt > atMillis) todayAt else prayerTimeMillis(tomorrow, tomorrowTimes.getValue(key)),
        )
    }.sortedBy { it.atMillis }
}
