package com.godfather59.quransunnah.prayer

// City presets: (name, latitude, longitude, tzOffsetHours).
// Ported from kPrayerCityPresets in lib/state/prayer_provider.dart.
// Morocco observes UTC+1 except during Ramadan (UTC+0) — GPS mode handles
// this automatically; preset users confirm with their mosque that week.

data class PrayerCity(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val tzOffsetHours: Double,
)

val prayerCityPresets: List<PrayerCity> = listOf(
    PrayerCity("Mecca", 21.4225, 39.8262, 3.0),
    PrayerCity("Medina", 24.5247, 39.5692, 3.0),
    PrayerCity("Cairo", 30.0444, 31.2357, 2.0),
    PrayerCity("Casablanca", 33.5731, -7.5898, 1.0),
    PrayerCity("Rabat", 34.0209, -6.8416, 1.0),
    PrayerCity("Marrakech", 31.6295, -7.9811, 1.0),
    PrayerCity("Algiers", 36.7538, 3.0588, 1.0),
    PrayerCity("Tunis", 36.8065, 10.1815, 1.0),
    PrayerCity("Istanbul", 41.0082, 28.9784, 3.0),
    PrayerCity("Paris", 48.8566, 2.3522, 1.0),
    PrayerCity("London", 51.5074, -0.1278, 0.0),
    PrayerCity("New York", 40.7128, -74.006, -5.0),
    PrayerCity("Jakarta", -6.2088, 106.8456, 7.0),
)

fun isMoroccanCity(city: String): Boolean =
    city == "Casablanca" || city == "Rabat" || city == "Marrakech"

private val cityNamesAr: Map<String, String> = mapOf(
    "Mecca" to "مكة",
    "Medina" to "المدينة",
    "Cairo" to "القاهرة",
    "Casablanca" to "الدار البيضاء",
    "Rabat" to "الرباط",
    "Marrakech" to "مراكش",
    "Algiers" to "الجزائر",
    "Tunis" to "تونس",
    "Istanbul" to "إسطنبول",
    "Paris" to "باريس",
    "London" to "لندن",
    "New York" to "نيويورك",
    "Jakarta" to "جاكرتا",
)

private val cityNamesFr: Map<String, String> = mapOf(
    "Mecca" to "La Mecque",
    "Medina" to "Médine",
    "Cairo" to "Le Caire",
    "Algiers" to "Alger",
    "London" to "Londres",
    "New York" to "New York",
    "Jakarta" to "Jakarta",
)

/** Display name for a preset city (GPS "GPS x, y" fixes pass through). */
fun localizedCityName(name: String, language: String): String = when (language) {
    "ar" -> cityNamesAr[name] ?: name
    "fr" -> cityNamesFr[name] ?: name
    else -> name
}
