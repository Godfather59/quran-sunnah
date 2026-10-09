package com.godfather59.quransunnah.prefs

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

// Typed wrapper over key-value storage with the EXACT key names and
// semantics of the Flutter app (SharedPreferences keys). Storage only —
// normalization/fallbacks live in the app layers (Phase 5/6).
//
// Lists have no native Settings type, so they are stored as unit-separated
// strings (U+001F never appears in ids/keys/counts; Dart stored real
// string lists — values round-trip identically).

/** Unit separator: never appears in ids, keys, counts or queries. */
internal val LIST_SEP: String = 31.toChar().toString()

fun List<String>.encodeList(): String = joinToString(LIST_SEP)

fun String.decodeList(): List<String> =
    if (isEmpty()) emptyList() else split(LIST_SEP)

/** Dhikr day key suffix: "dhikr.2026-10-07". */
fun dhikrDayKey(year: Int, month: Int, day: Int): String =
    "dhikr.${year.toString().padStart(4, '0')}-" +
        "${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

class Prefs(private val settings: Settings) {
    // -- Quran reading prefs (q.*) --
    var quranRiwayaName: String? get() = settings.getStringOrNull("q.riwayaName"); set(v) = setOrRemove("q.riwayaName", v)
    var quranScriptName: String? get() = settings.getStringOrNull("q.scriptName"); set(v) = setOrRemove("q.scriptName", v)
    var quranFontName: String? get() = settings.getStringOrNull("q.fontName"); set(v) = setOrRemove("q.fontName", v)
    var quranModeName: String? get() = settings.getStringOrNull("q.modeName"); set(v) = setOrRemove("q.modeName", v)
    var quranAyahNumberStyleName: String? get() = settings.getStringOrNull("q.ayahNumberStyleName"); set(v) = setOrRemove("q.ayahNumberStyleName", v)
    var quranFontSize: Double get() = settings.getDouble("q.fontSize", 24.0); set(v) = settings.set("q.fontSize", v)
    var quranLineHeight: Double get() = settings.getDouble("q.lineHeight", 1.9); set(v) = settings.set("q.lineHeight", v)
    var quranAyahSpacing: Double get() = settings.getDouble("q.ayahSpacing", 12.0); set(v) = settings.set("q.ayahSpacing", v)
    var quranMargins: Double get() = settings.getDouble("q.margins", 16.0); set(v) = settings.set("q.margins", v)
    var quranTranslations: List<String>
        get() = settings.getStringOrNull("q.translations")?.decodeList() ?: listOf("en-sahih")
        set(v) = settings.set("q.translations", v.encodeList())
    var quranTafsirId: String get() = settings.getString("q.tafsirId", "jalalayn"); set(v) = settings.set("q.tafsirId", v)
    var quranLastSurah: Int get() = settings.getInt("q.lastSurah", 2); set(v) = settings.set("q.lastSurah", v)
    var quranLastAyah: Int get() = settings.getInt("q.lastAyah", 255); set(v) = settings.set("q.lastAyah", v)
    var quranShowTranslation: Boolean get() = settings.getBoolean("q.showTr", true); set(v) = settings.set("q.showTr", v)
    var quranShowTajweed: Boolean get() = settings.getBoolean("q.showTajweed", false); set(v) = settings.set("q.showTajweed", v)

    // -- App prefs (app.*) --
    var appLocale: String get() = settings.getString("app.locale", "ar"); set(v) = settings.set("app.locale", v)
    var appThemeName: String? get() = settings.getStringOrNull("app.themeName"); set(v) = setOrRemove("app.themeName", v)
    var appDynamicColor: Boolean get() = settings.getBoolean("app.dynamicColor", false); set(v) = settings.set("app.dynamicColor", v)
    var appQari: String get() = settings.getString("app.qari", ""); set(v) = settings.set("app.qari", v)
    var appPlaybackSpeed: Double get() = settings.getDouble("app.playbackSpeed", 1.0); set(v) = settings.set("app.playbackSpeed", v)
    var appOnboarded: Boolean get() = settings.getBoolean("app.onboarded", false); set(v) = settings.set("app.onboarded", v)
    var appDisplaySanad: Boolean get() = settings.getBoolean("app.sanad", true); set(v) = settings.set("app.sanad", v)
    var appDisplayGrade: Boolean get() = settings.getBoolean("app.grade", true); set(v) = settings.set("app.grade", v)

    // -- Prayer prefs (prayer.*) --
    var prayerMethod: String? get() = settings.getStringOrNull("prayer.method"); set(v) = setOrRemove("prayer.method", v)
    var prayerLat: Double? get() = settings.getDoubleOrNull("prayer.lat"); set(v) = setOrRemoveDouble("prayer.lat", v)
    var prayerLng: Double? get() = settings.getDoubleOrNull("prayer.lng"); set(v) = setOrRemoveDouble("prayer.lng", v)
    var prayerCity: String? get() = settings.getStringOrNull("prayer.city"); set(v) = setOrRemove("prayer.city", v)
    var prayerTz: Double? get() = settings.getDoubleOrNull("prayer.tz"); set(v) = setOrRemoveDouble("prayer.tz", v)
    var prayerNotif: Boolean get() = settings.getBoolean("prayer.notif", false); set(v) = settings.set("prayer.notif", v)

    // -- Home layout (home.*) --
    var homeOrder: List<String>? get() = settings.getStringOrNull("home.order")?.decodeList(); set(v) = setOrRemove("home.order", v?.encodeList())
    var homeHidden: List<String> get() = settings.getStringOrNull("home.hidden")?.decodeList() ?: emptyList(); set(v) = settings.set("home.hidden", v.encodeList())

    // -- Khatma/streak --
    var khatmaLastDayMs: Int? get() = settings.getIntOrNull("khatma.lastDay"); set(v) = setOrRemoveInt("khatma.lastDay", v)
    var khatmaStreak: Int get() = settings.getInt("khatma.streak", 0); set(v) = settings.set("khatma.streak", v)

    // -- Memorization --
    var memorizedAyahs: List<String> get() = settings.getStringOrNull("memorized.ayahs.v1")?.decodeList() ?: emptyList(); set(v) = settings.set("memorized.ayahs.v1", v.encodeList())

    // -- Dhikr --
    fun dhikrDay(key: String): List<String> = settings.getStringOrNull(key)?.decodeList() ?: emptyList()
    fun setDhikrDay(key: String, entries: List<String>) = settings.set(key, entries.encodeList())
    var dhikrTotal: Int get() = settings.getInt("dhikr.total", 0); set(v) = settings.set("dhikr.total", v)

    // -- Search history (max 10, most-recent first, deduped) --
    fun searchHistory(): List<String> = settings.getStringOrNull("search.history")?.decodeList() ?: emptyList()
    fun pushSearchHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        val next = (listOf(trimmed) + searchHistory().filter { it != trimmed }).take(10)
        settings.set("search.history", next.encodeList())
    }

    private fun setOrRemove(key: String, value: String?) {
        if (value == null) settings.remove(key) else settings.set(key, value)
    }

    private fun setOrRemoveDouble(key: String, value: Double?) {
        if (value == null) settings.remove(key) else settings.set(key, value)
    }

    private fun setOrRemoveInt(key: String, value: Int?) {
        if (value == null) settings.remove(key) else settings.set(key, value)
    }
}
