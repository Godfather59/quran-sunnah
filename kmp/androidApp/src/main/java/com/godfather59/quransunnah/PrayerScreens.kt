package com.godfather59.quransunnah
import androidx.core.net.toUri

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.godfather59.quransunnah.audio.AyahAudioItem
import com.godfather59.quransunnah.audio.AyahDownload
import com.godfather59.quransunnah.audio.DownloadCancelledException
import com.godfather59.quransunnah.audio.PlayerListener
import com.godfather59.quransunnah.audio.RepeatMode
import com.godfather59.quransunnah.audio.DownloadControl
import com.godfather59.quransunnah.audio.DownloadQueue
import com.godfather59.quransunnah.audio.QueuedSurah
import com.godfather59.quransunnah.audio.Reciter
import com.godfather59.quransunnah.audio.globalAyahNumber
import com.godfather59.quransunnah.audio.reciterById
import com.godfather59.quransunnah.audio.reciters
import com.godfather59.quransunnah.data.AndroidAssetReader
import com.godfather59.quransunnah.data.loadQuranEdition
import com.godfather59.quransunnah.data.TajweedSpan
import com.godfather59.quransunnah.data.isTajweedEdition
import com.godfather59.quransunnah.data.loadTajweedSurah
import com.godfather59.quransunnah.data.loadTafsirSurah
import com.godfather59.quransunnah.data.loadWordsSurah
import com.godfather59.quransunnah.data.tajweedRuleInfo
import com.godfather59.quransunnah.data.tajweedRules
import com.godfather59.quransunnah.data.parsePipeMap
import com.godfather59.quransunnah.device.AndroidClipboard
import com.godfather59.quransunnah.device.AndroidCompass
import com.godfather59.quransunnah.device.AndroidLocator
import com.godfather59.quransunnah.device.AndroidSharer
import com.godfather59.quransunnah.dhikr.kAdhkar
import com.godfather59.quransunnah.prayer.NextPrayer
import com.godfather59.quransunnah.prayer.PrayerCalcMethod
import com.godfather59.quransunnah.prayer.PrayerTimes
import com.godfather59.quransunnah.prayer.calculatePrayerTimes
import com.godfather59.quransunnah.prayer.isMoroccanCity
import com.godfather59.quransunnah.prayer.localizedCityName
import com.godfather59.quransunnah.prayer.nextPrayer
import com.godfather59.quransunnah.prayer.prayerCityPresets
import com.godfather59.quransunnah.prayer.qiblaBearing
import com.godfather59.quransunnah.text.parseQuranRef
import com.godfather59.quransunnah.text.toArabicIndic
import com.godfather59.quransunnah.data.HadithSection
import com.godfather59.quransunnah.data.parseHadithIndex
import com.godfather59.quransunnah.data.parseHadithSection
import com.godfather59.quransunnah.hadith.Hadith
import com.godfather59.quransunnah.hadith.HadithCollection
import com.godfather59.quransunnah.hadith.bookDisplayTitle
import com.godfather59.quransunnah.hadith.hadithCollections
import com.godfather59.quransunnah.quran.Ayah
import com.godfather59.quransunnah.quran.AyahRef
import com.godfather59.quransunnah.quran.MushafMetadata
import com.godfather59.quransunnah.quran.SurahMeta
import com.godfather59.quransunnah.quran.TafsirEntry
import com.godfather59.quransunnah.quran.dailyAyahRef
import com.godfather59.quransunnah.quran.riwayatCatalog
import com.godfather59.quransunnah.quran.surahMetadata
import com.godfather59.quransunnah.quran.translationAssets
import com.godfather59.quransunnah.search.SearchEngine
import com.godfather59.quransunnah.search.SearchKind
import com.godfather59.quransunnah.search.SearchOptions
import com.godfather59.quransunnah.search.SearchResults
import com.godfather59.quransunnah.ui.LayoutDirection
import com.godfather59.quransunnah.ui.AppFontAssets
import com.godfather59.quransunnah.ui.layoutDirectionForLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import java.util.TimeZone

// ---- Prayer & Qibla (shared solar math + Android sensor/GPS) ----

internal fun prayerMethodFlutterName(method: PrayerCalcMethod): String = when (method) {
    PrayerCalcMethod.MUSLIM_WORLD_LEAGUE -> "muslimWorldLeague"
    PrayerCalcMethod.ISNA -> "isna"
    PrayerCalcMethod.EGYPT -> "egypt"
    PrayerCalcMethod.UMM_AL_QURA -> "ummAlQura"
    PrayerCalcMethod.KARACHI -> "karachi"
    PrayerCalcMethod.JAFARI -> "jafari"
    PrayerCalcMethod.MOROCCO -> "morocco"
}

internal fun prayerMethodFromName(name: String): PrayerCalcMethod =
    PrayerCalcMethod.entries.firstOrNull { prayerMethodFlutterName(it) == name }
        ?: PrayerCalcMethod.MUSLIM_WORLD_LEAGUE

internal fun prayerMethodLabelRes(method: PrayerCalcMethod): Int = when (method) {
    PrayerCalcMethod.MUSLIM_WORLD_LEAGUE -> R.string.prayerMethodMuslimWorldLeague
    PrayerCalcMethod.ISNA -> R.string.prayerMethodIsna
    PrayerCalcMethod.EGYPT -> R.string.prayerMethodEgypt
    PrayerCalcMethod.UMM_AL_QURA -> R.string.prayerMethodUmmAlQura
    PrayerCalcMethod.KARACHI -> R.string.prayerMethodKarachi
    PrayerCalcMethod.JAFARI -> R.string.prayerMethodJafari
    PrayerCalcMethod.MOROCCO -> R.string.prayerMethodMorocco
}

internal data class HomeDashboardSnapshot(
    val prayer: PrayerSnapshot?,
)

private data class HomeDashboardCacheKey(
    val refreshToken: Int,
    val year: Int,
    val dayOfYear: Int,
    val minuteOfDay: Int,
)

/** Reuses Home's prayer and activity summary when returning to the tab. */
internal object HomeDashboardCache {
    private val mutex = kotlinx.coroutines.sync.Mutex()

    @Volatile
    private var cached: Pair<HomeDashboardCacheKey, HomeDashboardSnapshot>? = null

    fun peek(refreshToken: Int, atMillis: Long = System.currentTimeMillis()): HomeDashboardSnapshot? {
        val cachedValue = cached ?: return null
        val now = Calendar.getInstance().apply { timeInMillis = atMillis }
        val key = cachedValue.first
        if (key.refreshToken != refreshToken ||
            key.year != now.get(Calendar.YEAR) ||
            key.dayOfYear != now.get(Calendar.DAY_OF_YEAR)
        ) {
            return null
        }
        val prayer = cachedValue.second.prayer?.let { snapshot ->
            refreshPrayerSnapshot(snapshot, now.timeInMillis)
        }
        return cachedValue.second.copy(prayer = prayer)
    }

    suspend fun load(context: Context, refreshToken: Int, atMillis: Long = System.currentTimeMillis()): HomeDashboardSnapshot {
        val now = Calendar.getInstance().apply { timeInMillis = atMillis }
        val key = HomeDashboardCacheKey(
            refreshToken = refreshToken,
            year = now.get(Calendar.YEAR),
            dayOfYear = now.get(Calendar.DAY_OF_YEAR),
            minuteOfDay = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE),
        )
        cached?.takeIf { it.first == key }?.let { return it.second }
        return mutex.withLock {
            cached?.takeIf { it.first == key }?.let { return@withLock it.second }
            withContext(Dispatchers.IO) {
                val prefs = try {
                    PrayerPrefs.load(context)
                } catch (_: Exception) {
                    null
                }
                val prayer = try {
                    prefs?.let {
                        prayerSnapshot(it, atMillis)
                    }
                } catch (_: Exception) {
                    null
                }
                HomeDashboardSnapshot(prayer)
                    .also { cached = key to it }
            }
        }
    }
}

/** Prayer names from verified string resources (was hardcoded trilingual). */
@Composable
internal fun prayerName(key: String): String = when (key) {
    "fajr" -> stringResource(R.string.prayerNameFajr)
    "sunrise" -> stringResource(R.string.prayerNameSunrise)
    "dhuhr" -> stringResource(R.string.prayerNameDhuhr)
    "asr" -> stringResource(R.string.prayerNameAsr)
    "maghrib" -> stringResource(R.string.prayerNameMaghrib)
    "isha" -> stringResource(R.string.prayerNameIsha)
    else -> key
}

@Composable
internal fun prayerScreenTitle(): String = stringResource(R.string.prayerScreenTitle)

@Composable
internal fun qiblaHint(bearing: String): String = stringResource(R.string.qiblaHint, bearing)

@Composable
internal fun remainingText(hours: Int, minutes: Int, city: String): String =
    stringResource(R.string.remainingText, hours, minutes, city)

@Composable
private fun countdownLabel(hours: Int, minutes: Int): String =
    stringResource(R.string.countdownLabel, hours, minutes)

@Composable
private fun nextPrayerLabel(): String = stringResource(R.string.nextPrayerLabel)

@Composable
private fun prayerScheduleLabel(): String = stringResource(R.string.prayerScheduleLabel)

@Composable
private fun changeLabel(): String = stringResource(R.string.changeLabel)

@Composable
private fun locationMethodTitle(): String = stringResource(R.string.locationMethodTitle)

@Composable
private fun methodLabel(): String = stringResource(R.string.methodLabel)

@Composable
private fun cityLabel(): String = stringResource(R.string.cityLabel)

@Composable
private fun useGpsLabel(): String = stringResource(R.string.useGpsLabel)

@Composable
private fun gpsUnavailableText(): String = stringResource(R.string.gpsUnavailableText)

@Composable
private fun prayerDisclaimer(): String = stringResource(R.string.prayerDisclaimer)

@Composable
private fun notificationsTitle(): String = stringResource(R.string.notificationsTitle)

@Composable
private fun notificationDeniedText(): String = stringResource(R.string.notificationDeniedText)

@Composable
fun PrayerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val language = currentLanguage()
    var prefsLoaded by remember { mutableStateOf(false) }
    var methodName by rememberSaveable { mutableStateOf("muslimWorldLeague") }
    var city by rememberSaveable { mutableStateOf("Mecca") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var tzOffsetHours by remember { mutableStateOf<Double?>(null) }
    var timeZoneId by remember { mutableStateOf<String?>(null) }
    var heading by remember { mutableStateOf<Float?>(null) }
    var gpsBusy by remember { mutableStateOf(false) }
    var gpsFailed by remember { mutableStateOf(false) }
    var locationExpanded by rememberSaveable { mutableStateOf(false) }
    var notifEnabled by rememberSaveable { mutableStateOf(false) }
    var deliveryStatus by remember { mutableStateOf(PrayerDeliveryStatus.DISABLED) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val compass = remember(context) { AndroidCompass(context) }
    val locator = remember(context) { AndroidLocator(context) }

    LaunchedEffect(context) {
        val prefs = withContext(Dispatchers.IO) { PrayerPrefs.load(context) }
        methodName = prefs.methodName
        city = prefs.city
        latitude = prefs.latitude
        longitude = prefs.longitude
        tzOffsetHours = prefs.tzOffsetHours
        timeZoneId = prefs.timeZoneId
        notifEnabled = prefs.notificationsEnabled
        prefsLoaded = true
    }
    if (!prefsLoaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    DisposableEffect(compass) {
        compass.start { heading = it }
        onDispose { compass.stop() }
    }
    // Qibla + prayer-times recompute tick: aligned to 30s intervals and
    // paused while the app is backgrounded (was a bare while(true) loop).
    val tick = rememberIntervalTicker(intervalMs = 30_000L)

    val snap = remember(methodName, city, latitude, longitude, tzOffsetHours, timeZoneId, tick) {
        prayerSnapshot(methodName, city, latitude, longitude, tzOffsetHours, tick, timeZoneId)
    }
    val bearing = remember(snap) { qiblaBearing(snap.place.latitude, snap.place.longitude) }
    val bearingText = remember(bearing) { "%.0f".format(bearing) }
    val remaining = remainingPrayerMinutes(snap, tick)
    val appIsResumed = rememberAppIsResumed()
    LaunchedEffect(appIsResumed, notifEnabled, methodName, city, timeZoneId) {
        if (appIsResumed) {
            deliveryStatus = withContext(Dispatchers.IO) {
                PrayerNotifications.scheduleDaily(context)
                PrayerNotifications.deliveryStatus(context)
            }
        }
    }
    // Read here: the permission callbacks below are not @Composable contexts.
    val deniedText = notificationDeniedText()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            PrayerPrefs.saveNotificationsEnabled(context, true)
            notifEnabled = true
            PrayerNotifications.scheduleDaily(context)
        } else {
            scope.launch {
                snackbar.showSnackbar(deniedText)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants.values.any { it }) {
            scope.launch {
                gpsBusy = true
                gpsFailed = false
                val fix = withContext(Dispatchers.IO) { locator.currentFix() }
                gpsBusy = false
                if (fix == null) {
                    gpsFailed = true
                } else {
                    val zone = TimeZone.getDefault()
                    val tz = zone.getOffset(System.currentTimeMillis()) / 3600000.0
                    PrayerPrefs.saveGps(context, fix.city, fix.latitude, fix.longitude, tz, zone.id)
                    city = fix.city
                    latitude = fix.latitude
                    longitude = fix.longitude
                    tzOffsetHours = tz
                    timeZoneId = zone.id
                }
            }
        } else {
            gpsFailed = true
        }
    }

    val todayLine = remember(language, timeZoneId, tick / 60_000L) {
        try {
            val loc = when (language) {
                "ar" -> java.util.Locale("ar")
                "fr" -> java.util.Locale.FRENCH
                else -> java.util.Locale.ENGLISH
            }
            java.text.SimpleDateFormat("dd MMMM, yyyy", loc)
                .apply { timeZone = snap.place.timeZone }
                .format(java.util.Date(tick))
        } catch (_: Exception) {
            ""
        }
    }

    EmeraldScaffold(
        header = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color.White,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = prayerScreenTitle(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    if (todayLine.isNotEmpty()) {
                        Text(
                            text = todayLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
            }
        },
        body = {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbar) },
                containerColor = Color.Transparent,
            ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentPadding = PaddingValues(bottom = padding.calculateBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
            fun togglePrayerNotifications(enabled: Boolean) {
                if (enabled) {
                    if (android.os.Build.VERSION.SDK_INT >= 33) {
                        notificationPermissionLauncher.launch(
                            Manifest.permission.POST_NOTIFICATIONS,
                        )
                    } else {
                        PrayerPrefs.saveNotificationsEnabled(context, true)
                        notifEnabled = true
                        PrayerNotifications.scheduleDaily(context)
                    }
                } else {
                    PrayerPrefs.saveNotificationsEnabled(context, false)
                    notifEnabled = false
                    PrayerNotifications.cancelAll(context)
                }
            }
            item {
                HeroGradientCard {
                    Text(
                        text = nextPrayerLabel(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.82f),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayerName(snap.next.key),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = snap.times.format(snap.next.minutes),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color.White.copy(alpha = 0.16f),
                            contentColor = Color.White,
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(
                                text = countdownLabel(remaining / 60, remaining % 60),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = localizedCityName(snap.place.city, language),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                    }
                }
            }
            item {
                Text(
                    text = prayerScheduleLabel(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        snap.times.ordered.forEachIndexed { rowIdx, (key, minutes) ->
                            val isNext = key == snap.next.key
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    color = if (isNext) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHighest
                                    },
                                    shape = CircleShape,
                                ) {
                                    Spacer(
                                        Modifier
                                            .padding(6.dp)
                                            .size(8.dp),
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = prayerName(key),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isNext) FontWeight.Bold else null,
                                    color = if (isNext) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                                if (isNext) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        shape = RoundedCornerShape(50),
                                    ) {
                                        Text(
                                            text = stringResource(R.string.nextBadge),
                                            modifier = Modifier.padding(
                                                horizontal = 9.dp,
                                                vertical = 4.dp,
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                }
                                Text(
                                    text = snap.times.format(minutes),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isNext) FontWeight.Bold else null,
                                    color = if (isNext) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
                            if (rowIdx < snap.times.ordered.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(
                                        alpha = 0.6f,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { locationExpanded = !locationExpanded },
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = locationMethodTitle(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "${localizedCityName(snap.place.city, language)} · " +
                                        stringResource(
                                            prayerMethodLabelRes(prayerMethodFromName(methodName)),
                                        ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(onClick = { locationExpanded = !locationExpanded }) {
                                Text(changeLabel())
                            }
                        }

                        if (locationExpanded) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                text = methodLabel(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(
                                    PrayerCalcMethod.entries,
                                    key = { prayerMethodFlutterName(it) },
                                ) { method ->
                                    val flutterName = prayerMethodFlutterName(method)
                                    FilterChip(
                                        selected = flutterName == methodName,
                                        onClick = {
                                            methodName = flutterName
                                            PrayerPrefs.saveMethod(context, flutterName)
                                        },
                                        label = {
                                            Text(stringResource(prayerMethodLabelRes(method)))
                                        },
                                    )
                                }
                            }
                            Text(
                                text = cityLabel(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(prayerCityPresets, key = { it.name }) { preset ->
                                    FilterChip(
                                        selected = preset.name == city,
                                        onClick = {
                                            PrayerPrefs.savePresetCity(
                                                context,
                                                preset.name,
                                                preset.latitude,
                                                preset.longitude,
                                                preset.tzOffsetHours,
                                            )
                                            city = preset.name
                                            latitude = preset.latitude
                                            longitude = preset.longitude
                                            tzOffsetHours = preset.tzOffsetHours
                                            timeZoneId = preset.timeZoneId
                                            if (isMoroccanCity(preset.name) &&
                                                methodName == "muslimWorldLeague"
                                            ) {
                                                methodName = "morocco"
                                                PrayerPrefs.saveMethod(context, "morocco")
                                            }
                                        },
                                        label = {
                                            Text(localizedCityName(preset.name, language))
                                        }
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    gpsFailed = false
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                        ),
                                    )
                                },
                                enabled = !gpsBusy,
                            ) {
                                if (gpsBusy) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(useGpsLabel())
                            }
                            if (gpsFailed) {
                                Text(
                                    text = gpsUnavailableText(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                            val tzLabel = run {
                                val tz = snap.place.tzOffsetHours
                                val sign = if (tz >= 0) "+" else ""
                                "%.2f, %.2f · UTC$sign%.1f".format(
                                    snap.place.latitude,
                                    snap.place.longitude,
                                    tz,
                                )
                            }
                            Text(
                                text = listOfNotNull(snap.place.timeZoneId, tzLabel).joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (snap.place.timeZoneId == null) {
                                Text(
                                    text = stringResource(R.string.prayerTimeZoneRefresh),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = prayerDisclaimer(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = notificationsTitle(),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Switch(
                                checked = notifEnabled,
                                onCheckedChange = { togglePrayerNotifications(it) },
                                modifier = Modifier.semantics {
                                    contentDescription = context.getString(R.string.notificationsTitle)
                                },
                            )
                        }
                        Text(
                            text = stringResource(when (deliveryStatus) {
                                PrayerDeliveryStatus.DISABLED -> R.string.prayerRemindersOff
                                PrayerDeliveryStatus.BLOCKED -> R.string.prayerRemindersBlocked
                                PrayerDeliveryStatus.FAILED -> R.string.prayerRemindersFailed
                                PrayerDeliveryStatus.APPROXIMATE -> R.string.prayerRemindersApproximate
                                PrayerDeliveryStatus.EXACT -> R.string.prayerRemindersReady
                            }),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (deliveryStatus in setOf(PrayerDeliveryStatus.BLOCKED, PrayerDeliveryStatus.FAILED))
                                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (deliveryStatus == PrayerDeliveryStatus.BLOCKED) {
                            TextButton(onClick = {
                                openPrayerSystemSettings(context, exactAlarms = false)
                            }) { Text(stringResource(R.string.prayerNotificationSettings)) }
                        }
                        if (deliveryStatus == PrayerDeliveryStatus.APPROXIMATE && Build.VERSION.SDK_INT >= 31) {
                            TextButton(onClick = {
                                openPrayerSystemSettings(context, exactAlarms = true)
                            }) { Text(stringResource(R.string.prayerAllowExactAlarms)) }
                        }
                        if (deliveryStatus == PrayerDeliveryStatus.FAILED) {
                            TextButton(onClick = {
                                scope.launch {
                                    deliveryStatus = withContext(Dispatchers.IO) {
                                        PrayerNotifications.scheduleDaily(context, force = true)
                                        PrayerNotifications.deliveryStatus(context)
                                    }
                                }
                            }) { Text(stringResource(R.string.retry)) }
                        }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Qibla",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "$bearingText°",
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            QiblaDial(bearing = bearing, heading = heading)
                        }
                        Text(
                            text = qiblaHint(bearingText),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        }
    },
    )
}

private fun openPrayerSystemSettings(context: Context, exactAlarms: Boolean) {
    val packageUri = "package:${context.packageName}".toUri()
    val intent = when {
        exactAlarms && Build.VERSION.SDK_INT >= 31 -> android.content.Intent(
            android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri,
        )
        Build.VERSION.SDK_INT >= 26 -> android.content.Intent(
            android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS,
        ).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
        else -> android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
    }
    try {
        context.startActivity(intent)
    } catch (_: android.content.ActivityNotFoundException) {
        context.startActivity(android.content.Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri,
        ))
    }
}

@Composable
private fun QiblaDial(
    bearing: Double,
    heading: Float?,
) {
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.surfaceContainerHighest
    val dialDescription = remember(bearing, heading) {
        val h = heading?.let { "%.0f".format(it) } ?: "--"
        "Qibla ${"%.0f".format(bearing)} degrees, heading $h degrees"
    }
    Canvas(
        modifier = Modifier
            .size(200.dp)
            .semantics { contentDescription = dialDescription }
            .testTag("qiblaDial"),
    ) {
        val radius = size.minDimension / 2 - 8f
        drawCircle(
            color = outline,
            radius = radius,
            style = Stroke(width = 4f),
        )
        listOf(0f, 90f, 180f, 270f).forEach { tickDegrees ->
            rotate(tickDegrees) {
                drawLine(
                    color = outline,
                    start = center + Offset(0f, -radius),
                    end = center + Offset(0f, -radius + 18f),
                    strokeWidth = 8f,
                    cap = StrokeCap.Round,
                )
            }
        }
        val relative = (((bearing - (heading?.toDouble() ?: 0.0)) % 360) + 360) % 360
        rotate(relative.toFloat()) {
            drawLine(
                color = primary,
                start = center,
                end = center + Offset(0f, -radius + 10f),
                strokeWidth = 12f,
                cap = StrokeCap.Round,
            )
        }
        drawCircle(color = primary, radius = 10f, center = center)
    }
}

/**
 * Builds the process-wide search index once (Quran + all bundled hadith +
 * Jalalayn tafsir). Callers must invoke off the UI thread; repeat calls
 * are cheap no-ops once documents exist.
 */
