package com.godfather59.quransunnah.prayer

// The offset is retained for preference compatibility and platforms without a
// timezone database. Native schedulers use the IANA zone for each local date.

data class PrayerCity(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val tzOffsetHours: Double,
    val timeZoneId: String,
)

val prayerCityPresets: List<PrayerCity> = listOf(
    PrayerCity("Mecca", 21.4225, 39.8262, 3.0, "Asia/Riyadh"),
    PrayerCity("Medina", 24.5247, 39.5692, 3.0, "Asia/Riyadh"),
    PrayerCity("Cairo", 30.0444, 31.2357, 2.0, "Africa/Cairo"),
    PrayerCity("Casablanca", 33.5731, -7.5898, 1.0, "Africa/Casablanca"),
    PrayerCity("Rabat", 34.0209, -6.8416, 1.0, "Africa/Casablanca"),
    PrayerCity("Marrakech", 31.6295, -7.9811, 1.0, "Africa/Casablanca"),
    PrayerCity("Algiers", 36.7538, 3.0588, 1.0, "Africa/Algiers"),
    PrayerCity("Tunis", 36.8065, 10.1815, 1.0, "Africa/Tunis"),
    PrayerCity("Istanbul", 41.0082, 28.9784, 3.0, "Europe/Istanbul"),
    PrayerCity("Paris", 48.8566, 2.3522, 1.0, "Europe/Paris"),
    PrayerCity("London", 51.5074, -0.1278, 0.0, "Europe/London"),
    PrayerCity("New York", 40.7128, -74.006, -5.0, "America/New_York"),
    PrayerCity("Jakarta", -6.2088, 106.8456, 7.0, "Asia/Jakarta"),
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
