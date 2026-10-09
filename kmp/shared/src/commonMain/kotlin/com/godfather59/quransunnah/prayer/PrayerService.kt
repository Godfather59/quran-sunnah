package com.godfather59.quransunnah.prayer

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.tan

private fun toDegrees(rad: Double): Double = rad * 180.0 / PI
private fun toRadians(deg: Double): Double = deg * PI / 180.0

// Offline prayer-time calculation (no network, no new deps).
// Ported 1:1 from lib/data/services/prayer_service.dart.
//
// Standard solar algorithm: declination + equation of time + hour angles.
// Angles per method; Asr shadow factor 1 (Standard) / 2 (Hanafi).
// Times are minutes-from-local-midnight for the given date + tz offset.
// Accuracy ~±2min, suitable for a companion app — user confirms with
// local mosque. (Platform layers map minutes to real dates; the core stays
// datetime-free so commonTest vectors are hermetic.)

enum class PrayerCalcMethod {
    MUSLIM_WORLD_LEAGUE,
    ISNA,
    EGYPT,
    UMM_AL_QURA,
    KARACHI,
    JAFARI,
    /** Ministry of Habous (Morocco): Fajr 17.5 / Isha 17.5, Asr Standard. */
    MOROCCO,
}

data class PrayerMethodParams(
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutesAfterMaghrib: Double? = null,
    val asrFactor: Double = 1.0,
)

val prayerMethods: Map<PrayerCalcMethod, PrayerMethodParams> = mapOf(
    PrayerCalcMethod.MUSLIM_WORLD_LEAGUE to
        PrayerMethodParams(fajrAngle = 18.0, ishaAngle = 17.0),
    PrayerCalcMethod.ISNA to
        PrayerMethodParams(fajrAngle = 15.0, ishaAngle = 15.0),
    PrayerCalcMethod.EGYPT to
        PrayerMethodParams(fajrAngle = 19.5, ishaAngle = 17.5),
    PrayerCalcMethod.UMM_AL_QURA to PrayerMethodParams(
        fajrAngle = 18.5, ishaAngle = 0.0, ishaMinutesAfterMaghrib = 90.0,
    ),
    PrayerCalcMethod.KARACHI to
        PrayerMethodParams(fajrAngle = 18.0, ishaAngle = 18.0),
    PrayerCalcMethod.JAFARI to
        PrayerMethodParams(fajrAngle = 16.0, ishaAngle = 14.0),
    PrayerCalcMethod.MOROCCO to
        PrayerMethodParams(fajrAngle = 17.5, ishaAngle = 17.5),
)

/** All times are minutes from local midnight. */
data class PrayerTimes(
    val fajr: Int,
    val sunrise: Int,
    val dhuhr: Int,
    val asr: Int,
    val maghrib: Int,
    val isha: Int,
) {
    val ordered: List<Pair<String, Int>>
        get() = listOf(
            "fajr" to fajr,
            "sunrise" to sunrise,
            "dhuhr" to dhuhr,
            "asr" to asr,
            "maghrib" to maghrib,
            "isha" to isha,
        )

    fun format(minutes: Int): String {
        val m = ((minutes % 1440) + 1440) % 1440
        return "${(m / 60).toString().padStart(2, '0')}:${(m % 60).toString().padStart(2, '0')}"
    }
}

data class NextPrayer(
    val key: String,
    val minutes: Int,
    val isTomorrow: Boolean,
)

/** Next prayer at/after [nowMinutes] (wraps to tomorrow Fajr). */
fun nextPrayer(times: PrayerTimes, nowMinutes: Int): NextPrayer {
    for ((key, at) in times.ordered) {
        if (at >= nowMinutes) return NextPrayer(key, at, false)
    }
    return NextPrayer("fajr", times.fajr, true)
}

/** Qibla bearing from (lat,lng) to Kaaba (21.4225, 39.8262), degrees 0..360. */
fun qiblaBearing(lat: Double, lng: Double): Double {
    val kaabaLat = toRadians(21.4225)
    val kaabaLng = toRadians(39.8262)
    val phi = toRadians(lat)
    val lambda = toRadians(lng)
    val dLng = kaabaLng - lambda
    val y = sin(dLng)
    val x = cos(phi) * tan(kaabaLat) - sin(phi) * cos(dLng)
    val brng = toDegrees(atan2(y, x))
    return (brng + 360.0) % 360.0
}

fun calculatePrayerTimes(
    year: Int,
    month: Int,
    day: Int,
    latitude: Double,
    longitude: Double,
    tzOffsetHours: Double,
    method: PrayerCalcMethod = PrayerCalcMethod.MUSLIM_WORLD_LEAGUE,
): PrayerTimes {
    val p = requireNotNull(prayerMethods[method])
    val julian = julianDay(year, month, day)
    val decl = sunDeclination(julian)
    val eqt = equationOfTime(julian)
    val noon = midday(longitude, tzOffsetHours, eqt)
    fun t(minutes: Double): Double = noon + minutes

    val sunriseHA = hourAngleMinutes(-0.833, latitude, decl)
    val fajrHA = hourAngleMinutes(-p.fajrAngle, latitude, decl)
    val asrHA = asrHourAngle(p.asrFactor, latitude, decl)

    fun at(minutesFromMidnight: Double): Int =
        round(minutesFromMidnight).toInt()

    val dhuhr = at(t(0.0))
    val sunrise = at(t(-sunriseHA))
    val sunset = at(t(sunriseHA))
    val fajr = at(t(-fajrHA))
    val asr = at(t(asrHA))
    val maghrib = sunset
    val isha = if (p.ishaMinutesAfterMaghrib != null) {
        maghrib + round(p.ishaMinutesAfterMaghrib).toInt()
    } else {
        at(t(hourAngleMinutes(-p.ishaAngle, latitude, decl)))
    }

    return PrayerTimes(
        fajr = fajr,
        sunrise = sunrise,
        dhuhr = dhuhr,
        asr = asr,
        maghrib = maghrib,
        isha = isha,
    )
}

private fun julianDay(year: Int, month: Int, day: Int): Double {
    var y = year
    var m = month
    val d = day.toDouble()
    if (m <= 2) {
        y -= 1
        m += 12
    }
    val a = floor(y / 100.0)
    val b = 2 - a + floor(a / 4.0)
    return floor(365.25 * (y + 4716)) +
        floor(30.6001 * (m + 1)) +
        d + b - 1524.5
}

private fun sunDeclination(jd: Double): Double {
    val d = jd - 2451545.0
    val g = fixAngle(357.529 + 0.98560028 * d)
    val q = fixAngle(280.459 + 0.98564736 * d)
    val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2 * g))
    val e = 23.439 - 0.00000036 * d
    return toDegrees(asin(sinDeg(e) * sinDeg(l)))
}

private fun equationOfTime(jd: Double): Double {
    val d = jd - 2451545.0
    val g = fixAngle(357.529 + 0.98560028 * d)
    val q = fixAngle(280.459 + 0.98564736 * d)
    val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2 * g))
    val e = 23.439 - 0.00000036 * d
    val ra = toDegrees(
        atan2(
            cos(toRadians(e)) * sinDeg(l),
            cos(toRadians(l)),
        ),
    ) / 15.0
    val eqt = q / 15 - fixHour(ra)
    return eqt * 60
}

private fun midday(lng: Double, tz: Double, eqt: Double): Double =
    720 - 4 * lng - eqt + tz * 60
private fun asrHourAngle(factor: Double, lat: Double, decl: Double): Double {
    // Shadow length factor: cot(alt) = factor + tan(|lat-decl|).
    val diff = toRadians(abs(lat - decl))
    val alt = atan(1 / (factor + tan(diff)))
    return hourAngleMinutes(toDegrees(alt), lat, decl)
}

private fun hourAngleMinutes(angleDeg: Double, lat: Double, decl: Double): Double {
    val num = sinDeg(angleDeg) - sinDeg(lat) * sinDeg(decl)
    val den = cos(toRadians(lat)) *
        cos(toRadians(decl))
    val v = (num / den).coerceIn(-1.0, 1.0)
    // acos gives hour angle in degrees -> convert to minutes (4 min/deg).
    return toDegrees(acos(v)) * 4.0
}

private fun sinDeg(d: Double): Double = sin(toRadians(d))

private fun fixAngle(a: Double): Double = a - 360 * floor(a / 360)

private fun fixHour(h: Double): Double = h - 24 * floor(h / 24)
