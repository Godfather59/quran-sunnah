package com.godfather59.quransunnah

import android.content.Context
import androidx.core.content.edit
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.godfather59.quransunnah.hadith.hadithCollections
import com.godfather59.quransunnah.audio.reciterById
import com.godfather59.quransunnah.audio.reciters
import com.godfather59.quransunnah.quran.RiwayaInfo
import com.godfather59.quransunnah.quran.riwayatCatalog

private const val PREF_FILE = "quran_sunnah_kmp"
private const val LIST_SEP = "\u001F"

data class OnboardingChoices(
    val locale: String = "ar",
    val riwayaName: String = "hafsAsim",
    val scriptName: String = "uthmani",
    val hadithSources: Set<String> = setOf("bukhari", "muslim"),
    val optionalDownloads: Set<String> = emptySet(),
)

data class ReaderPrefsState(
    val lastSurah: Int = 1,
    val lastAyah: Int = 1,
    val riwayaName: String = "hafsAsim",
    val scriptName: String = "uthmani",
    val fontName: String = "uthmani",
    val reciterId: String = "ar.alafasy",
    val showTranslation: Boolean = true,
    val ayahNumberStyleName: String = "arabicIndic",
    val tafsirId: String = "jalalayn",
)

object OnboardingPrefs {
    fun isDone(context: Context): Boolean =
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .getBoolean("app.onboarded", false)

    fun locale(context: Context): String? =
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .getString("app.locale", null)

    fun saveLocale(context: Context, locale: String) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
            putString("app.locale", locale)
            }
    }

    fun save(context: Context, choices: OnboardingChoices) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
            putBoolean("app.onboarded", true)
            putString("app.locale", choices.locale)
            putString("q.riwayaName", choices.riwayaName)
            putString("q.scriptName", choices.scriptName)
            putString("q.fontName", "uthmani")
            putString("q.modeName", "reading")
            putString("q.ayahNumberStyleName", "arabicIndic")
            putString("q.translations", listOf("en-sahih").joinToString(LIST_SEP))
            putString("q.tafsirId", "jalalayn")
            putBoolean("q.showTr", true)
            putString("app.qari", reciters.first().identifier)
            putString("app.hadithSources", choices.hadithSources.joinToString(LIST_SEP))
            putString("app.optionalDownloads", choices.optionalDownloads.joinToString(LIST_SEP))
            }
    }
}

object ReaderPrefs {
    fun load(context: Context): ReaderPrefsState {
        val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        return ReaderPrefsState(
            lastSurah = prefs.getInt("q.lastSurah", 1).coerceIn(1, 114),
            lastAyah = prefs.getInt("q.lastAyah", 1).coerceAtLeast(1),
            riwayaName = prefs.getString("q.riwayaName", null) ?: "hafsAsim",
            scriptName = prefs.getString("q.scriptName", null) ?: "uthmani",
            fontName = prefs.getString("q.fontName", null) ?: "uthmani",
            reciterId = prefs.getString("app.qari", null)
                ?.takeIf { reciterById(it) != null }
                ?: reciters.first().identifier,
            showTranslation = prefs.getBoolean("q.showTr", true),
            ayahNumberStyleName = prefs.getString("q.ayahNumberStyleName", null) ?: "arabicIndic",
            tafsirId = prefs.getString("q.tafsirId", null) ?: "jalalayn",
        )
    }

    fun saveLastPosition(
        context: Context,
        surah: Int,
        ayah: Int,
    ) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
            putInt("q.lastSurah", surah.coerceIn(1, 114))
            putInt("q.lastAyah", ayah.coerceAtLeast(1))
            }
    }

    fun saveReaderDisplay(
        context: Context,
        riwayaName: String,
        scriptName: String,
    ) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
            putString("q.riwayaName", riwayaName)
            putString("q.scriptName", scriptName)
            }
    }

    fun saveReaderFont(
        context: Context,
        fontName: String,
    ) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
            putString("q.fontName", fontName)
            }
    }

    fun saveReciter(
        context: Context,
        reciterId: String,
    ) {
        if (reciterById(reciterId) == null) return
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
            putString("app.qari", reciterId)
            }
    }

    fun saveShowTranslation(
        context: Context,
        showTranslation: Boolean,
    ) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
            putBoolean("q.showTr", showTranslation)
            }
    }

    fun saveAyahNumberStyle(
        context: Context,
        styleName: String,
    ) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
            putString("q.ayahNumberStyleName", styleName)
            }
    }

    fun saveTafsir(
        context: Context,
        tafsirId: String,
    ) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
            putString("q.tafsirId", tafsirId)
            }
    }
}

data class PrayerPrefsState(
    val methodName: String = "muslimWorldLeague",
    val city: String = "Mecca",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val tzOffsetHours: Double? = null,
    val notificationsEnabled: Boolean = false,
)

/**
 * Prayer prefs with exact Flutter key names/values (`prayer.method` holds
 * the Dart enum name, `prayer.city` the preset name). Doubles use the same
 * raw-long-bits codec as Flutter's SharedPreferences plugin so GPS fixes
 * round-trip across both apps; absent/undecodable values fall back to the
 * shared city presets by name.
 */
object PrayerPrefs {
    fun load(context: Context): PrayerPrefsState {
        val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        return PrayerPrefsState(
            methodName = prefs.getString("prayer.method", null) ?: "muslimWorldLeague",
            city = prefs.getString("prayer.city", null) ?: "Mecca",
            latitude = prefs.getDoubleCompat("prayer.lat"),
            longitude = prefs.getDoubleCompat("prayer.lng"),
            tzOffsetHours = prefs.getDoubleCompat("prayer.tz"),
            notificationsEnabled = try {
                prefs.getBoolean("prayer.notif", false)
            } catch (_: Exception) {
                false
            },
        )
    }

    fun saveNotificationsEnabled(
        context: Context,
        enabled: Boolean,
    ) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
            putBoolean("prayer.notif", enabled)
            }
    }

    fun saveMethod(
        context: Context,
        methodName: String,
    ) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
            putString("prayer.method", methodName)
            }
    }

    fun savePresetCity(
        context: Context,
        city: String,
        latitude: Double,
        longitude: Double,
        tzOffsetHours: Double,
    ) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
            putString("prayer.city", city)
            putDoubleCompat("prayer.lat", latitude)
            putDoubleCompat("prayer.lng", longitude)
            putDoubleCompat("prayer.tz", tzOffsetHours)
            }
    }

    fun saveGps(
        context: Context,
        city: String,
        latitude: Double,
        longitude: Double,
        tzOffsetHours: Double,
    ) {
        savePresetCity(context, city, latitude, longitude, tzOffsetHours)
    }

    private fun android.content.SharedPreferences.getDoubleCompat(key: String): Double? {
        if (!contains(key)) return null
        return try {
            java.lang.Double.longBitsToDouble(getLong(key, 0L))
                .takeIf { it.isFinite() }
        } catch (_: Exception) {
            try {
                getString(key, null)?.toDoubleOrNull()?.takeIf { it.isFinite() }
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun android.content.SharedPreferences.Editor.putDoubleCompat(
        key: String,
        value: Double,
    ) = putLong(key, java.lang.Double.doubleToRawLongBits(value))
}

data class DhikrState(
    val today: Map<String, Int> = emptyMap(),
    val total: Int = 0,
)

/**
 * Per-dhikr tap counts with exact Flutter keys: daily map under
 * `dhikr.YYYY-MM-DD` (StringSet of "id:count", like Dart's StringList)
 * plus lifetime `dhikr.total`.
 */
object DhikrPrefs {
    fun dayKey(year: Int, month: Int, day: Int): String =
        "dhikr.%04d-%02d-%02d".format(year, month, day)

    fun load(context: Context): DhikrState {
        val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val cal = java.util.Calendar.getInstance()
        val today = prefs.getStringSet(
            dayKey(
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1,
                cal.get(java.util.Calendar.DAY_OF_MONTH),
            ),
            emptySet(),
        ).orEmpty()
            .mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    parts[0] to (parts[1].toIntOrNull() ?: 0)
                } else {
                    null
                }
            }
            .toMap()
        return DhikrState(today, prefs.getInt("dhikr.total", 0))
    }

    fun tap(context: Context, id: String): DhikrState {
        val current = load(context)
        val next = current.today.toMutableMap()
        next[id] = (next[id] ?: 0) + 1
        return persist(context, next, current.total + 1)
    }

    fun reset(context: Context, id: String): DhikrState {
        val current = load(context)
        val next = current.today.toMutableMap()
        next.remove(id)
        return persist(context, next, current.total)
    }

    private fun persist(
        context: Context,
        today: Map<String, Int>,
        total: Int,
    ): DhikrState {
        val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val cal = java.util.Calendar.getInstance()
        prefs.edit {
            putStringSet(
                dayKey(
                    cal.get(java.util.Calendar.YEAR),
                    cal.get(java.util.Calendar.MONTH) + 1,
                    cal.get(java.util.Calendar.DAY_OF_MONTH),
                ),
                today.map { (id, count) -> "$id:$count" }.toSet(),
            )
            putInt("dhikr.total", total)
        }
        return DhikrState(today, total)
    }
}

data class ThemePrefsState(
    val themeName: String = "system",
)

/** Appearance prefs. The brand emerald scheme is fixed: any legacy
 * `app.dynamicColor` value left by older builds is ignored (dynamic
 * wallpaper colors clashed with the brand and served purple schemes). */
object ThemePrefs {
    fun load(context: Context): ThemePrefsState {
        val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        return ThemePrefsState(
            themeName = prefs.getString("app.themeName", null) ?: "system",
        )
    }

    fun save(context: Context, themeName: String) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
            putString("app.themeName", themeName)
            remove("app.dynamicColor")
            }
    }
}

/**
 * Playback speed with the exact Flutter key (`app.playbackSpeed`, Dart
 * double = raw-long-bits like the prayer coordinates).
 */
object AudioPrefs {
    fun speed(context: Context): Double {
        val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        if (!prefs.contains("app.playbackSpeed")) return 1.0
        return try {
            java.lang.Double.longBitsToDouble(
                prefs.getLong("app.playbackSpeed", java.lang.Double.doubleToRawLongBits(1.0)),
            ).takeIf { it.isFinite() && it > 0 } ?: 1.0
        } catch (_: Exception) {
            try {
                prefs.getString("app.playbackSpeed", null)?.toDoubleOrNull()
                    ?.takeIf { it.isFinite() && it > 0 } ?: 1.0
            } catch (_: Exception) {
                1.0
            }
        }
    }

    fun saveSpeed(context: Context, speed: Double) {
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
            putLong("app.playbackSpeed", java.lang.Double.doubleToRawLongBits(speed))
            }
    }
}

/**
 * Memorized ayahs as "surah:ayah" canonical keys with the exact Flutter
 * key (`memorized.ayahs.v1`, Dart StringList = Android StringSet).
 * Local-only, offline.
 */
object MemorizationPrefs {
    fun load(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        return try {
            prefs.getStringSet("memorized.ayahs.v1", emptySet()).orEmpty().toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun toggle(context: Context, surah: Int, ayah: Int): Set<String> {
        val key = "$surah:$ayah"
        val next = load(context).toMutableSet()
        if (!next.add(key)) {
            next.remove(key)
        }
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
            putStringSet("memorized.ayahs.v1", next)
            }
        return next
    }

    fun countForSurah(keys: Set<String>, surah: Int, ayahCount: Int): Int {
        var n = 0
        for (a in 1..ayahCount) {
            if (keys.contains("$surah:$a")) n++
        }
        return n
    }

    /** First unmemorized ayah in [surah], or 1 if all done. */
    fun firstUnmemorized(keys: Set<String>, surah: Int, ayahCount: Int): Int {
        for (a in 1..ayahCount) {
            if (!keys.contains("$surah:$a")) return a
        }
        return 1
    }
}

/**
 * Daily-reading streak with exact Flutter keys (`khatma.lastDay` =
 * midnight millis, `khatma.streak` = days). Call on reader open.
 */
object KhatmaPrefs {
    fun streak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        return try {
            prefs.getInt("khatma.streak", 0)
        } catch (_: Exception) {
            0
        }
    }

    fun touchToday(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
            cal.set(java.util.Calendar.MINUTE, 0)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val todayMs = cal.timeInMillis
            val lastMs = try {
                prefs.getLong("khatma.lastDay", Long.MIN_VALUE)
            } catch (_: Exception) {
                Long.MIN_VALUE
            }
            val state = if (lastMs == Long.MIN_VALUE) {
                1
            } else {
                val last = java.util.Calendar.getInstance()
                last.timeInMillis = lastMs
                val sameDay = last.get(java.util.Calendar.YEAR) == cal.get(java.util.Calendar.YEAR) &&
                    last.get(java.util.Calendar.DAY_OF_YEAR) == cal.get(java.util.Calendar.DAY_OF_YEAR)
                if (sameDay) return
                last.add(java.util.Calendar.DAY_OF_YEAR, 1)
                val consecutive = last.get(java.util.Calendar.YEAR) == cal.get(java.util.Calendar.YEAR) &&
                    last.get(java.util.Calendar.DAY_OF_YEAR) == cal.get(java.util.Calendar.DAY_OF_YEAR)
                if (consecutive) streak(context) + 1 else 1
            }
            prefs.edit {
                putLong("khatma.lastDay", todayMs)
                putInt("khatma.streak", state)
                }
        } catch (_: Exception) {
        }
    }
}

@Composable
fun SplashScreen() {
    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.appTitle),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = appTag(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun OnboardingFlow(
    onComplete: (OnboardingChoices) -> Unit,
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var locale by rememberSaveable { mutableStateOf("ar") }
    var riwaya by rememberSaveable { mutableStateOf("hafsAsim") }
    var script by rememberSaveable { mutableStateOf("uthmani") }
    var sources by rememberSaveable { mutableStateOf(listOf("bukhari", "muslim")) }
    var downloads by rememberSaveable { mutableStateOf(emptyList<String>()) }

    val choices = OnboardingChoices(
        locale = locale,
        riwayaName = riwaya,
        scriptName = script,
        hadithSources = sources.toSet(),
        optionalDownloads = downloads.toSet(),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
    ) {
        LinearProgressIndicator(
            progress = { (step + 1) / 5f },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.appTitle),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(16.dp))

        when (step) {
            0 -> LanguagePage(locale) { locale = it }
            1 -> RiwayaPage(locale, riwaya) { riwaya = it }
            2 -> ScriptPage(script) { script = it }
            3 -> SourcesPage(locale, sources.toSet()) { sources = it.sorted() }
            else -> DownloadsPage(downloads.toSet()) { downloads = it.sorted() }
        }

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { onComplete(OnboardingChoices()) }) {
                Text(stringResource(R.string.skip))
            }
            Button(
                onClick = {
                    if (step == 4) {
                        onComplete(choices)
                    } else {
                        step += 1
                    }
                },
            ) {
                Text(stringResource(if (step == 4) R.string.obDone else R.string.obNext))
            }
        }
    }
}

@Composable
private fun LanguagePage(
    selected: String,
    onSelected: (String) -> Unit,
) {
    OnboardingPage(title = stringResource(R.string.obLanguage)) {
        ChoiceButton("العربية", selected == "ar") { onSelected("ar") }
        ChoiceButton("English", selected == "en") { onSelected("en") }
        ChoiceButton("Français", selected == "fr") { onSelected("fr") }
    }
}

@Composable
private fun RiwayaPage(
    locale: String,
    selected: String,
    onSelected: (String) -> Unit,
) {
    val available = riwayatCatalog.filter { it.isAvailableOfflineSeed }
    OnboardingPage(title = stringResource(R.string.obRiwaya)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(available) { info ->
                ChoiceButton(
                    label = info.riwayaLabel(locale),
                    selected = selected == info.id.storageName,
                    supporting = info.datasetVersion,
                ) {
                    onSelected(info.id.storageName)
                }
            }
        }
    }
}

@Composable
private fun ScriptPage(
    selected: String,
    onSelected: (String) -> Unit,
) {
    OnboardingPage(title = stringResource(R.string.obScript)) {
        ChoiceButton(
            label = stringResource(R.string.uthmani),
            selected = selected == "uthmani",
            supporting = stringResource(R.string.uthmaniHint),
        ) { onSelected("uthmani") }
        ChoiceButton(
            label = stringResource(R.string.imlai),
            selected = selected == "imlai",
            supporting = stringResource(R.string.imlaiHint),
        ) { onSelected("imlai") }
        ChoiceButton(
            label = stringResource(R.string.indopak),
            selected = selected == "indopak",
            supporting = stringResource(R.string.indopakHint),
        ) { onSelected("indopak") }
    }
}

@Composable
private fun SourcesPage(
    locale: String,
    selected: Set<String>,
    onSelected: (Set<String>) -> Unit,
) {
    OnboardingPage(title = stringResource(R.string.obSources)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(hadithCollections.take(6)) { collection ->
                val checked = collection.id in selected
                ChoiceButton(
                    label = when (locale) {
                        "ar" -> collection.nameAr
                        "fr" -> collection.nameFr
                        else -> collection.nameEn
                    },
                    selected = checked,
                    supporting = "${collection.totalHadith} ${stringResource(R.string.hadithCountUnit)}",
                ) {
                    onSelected(
                        if (checked) {
                            selected - collection.id
                        } else {
                            selected + collection.id
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadsPage(
    selected: Set<String>,
    onSelected: (Set<String>) -> Unit,
) {
    val options = listOf(
        "translation:en-sahih" to stringResource(R.string.saheehInternational),
        "translation:fr-hamidullah" to stringResource(R.string.hamidullahFrench),
        "tafsir:jalalayn" to stringResource(R.string.jalalayn),
    )
    OnboardingPage(
        title = stringResource(R.string.obDownloads),
        subtitle = stringResource(R.string.additionalContentHint),
    ) {
        options.forEach { (id, label) ->
            val checked = id in selected
            ChoiceButton(label, checked) {
                onSelected(if (checked) selected - id else selected + id)
            }
        }
    }
}

@Composable
private fun OnboardingPage(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
    )
    if (subtitle != null) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(16.dp))
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
private fun ChoiceButton(
    label: String,
    selected: Boolean,
    supporting: String? = null,
    onClick: () -> Unit,
) {
    // Supporting text must follow the button surface: onSurfaceVariant is
    // near-invisible on the filled primary background when selected.
    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(label, fontWeight = if (selected) FontWeight.Bold else null)
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
    if (selected) {
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            content()
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

internal fun RiwayaInfo.riwayaLabel(locale: String): String =
    when (locale) {
        "ar" -> riwayaAr
        "fr" -> riwayaFr
        else -> riwayaEn
    }

internal val com.godfather59.quransunnah.quran.RiwayaId.storageName: String
    get() = when (this) {
        com.godfather59.quransunnah.quran.RiwayaId.HAFS_ASIM -> "hafsAsim"
        com.godfather59.quransunnah.quran.RiwayaId.SHUBAH_ASIM -> "shubahAsim"
        com.godfather59.quransunnah.quran.RiwayaId.WARSH_NAFI -> "warshNafi"
        com.godfather59.quransunnah.quran.RiwayaId.QALUN_NAFI -> "qalunNafi"
        com.godfather59.quransunnah.quran.RiwayaId.BAZZI_IBN_KATHIR -> "bazziIbnKathir"
        com.godfather59.quransunnah.quran.RiwayaId.QUNBUL_IBN_KATHIR -> "qunbulIbnKathir"
        com.godfather59.quransunnah.quran.RiwayaId.DURI_ABI_AMR -> "duriAbiAmr"
        com.godfather59.quransunnah.quran.RiwayaId.SUSI_ABI_AMR -> "susiAbiAmr"
        com.godfather59.quransunnah.quran.RiwayaId.HISHAM_IBN_AMIR -> "hishamIbnAmir"
        com.godfather59.quransunnah.quran.RiwayaId.IBN_DHAKWAN_IBN_AMIR -> "ibnDhakwanIbnAmir"
        com.godfather59.quransunnah.quran.RiwayaId.KHALAF_HAMZAH -> "khalafHamzah"
        com.godfather59.quransunnah.quran.RiwayaId.KHALLAD_HAMZAH -> "khalladHamzah"
        com.godfather59.quransunnah.quran.RiwayaId.ABUL_HARITH_KISAI -> "abulHarithKisai"
        com.godfather59.quransunnah.quran.RiwayaId.DURI_KISAI -> "duriKisai"
    }
