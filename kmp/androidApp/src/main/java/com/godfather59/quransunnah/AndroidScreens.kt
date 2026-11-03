package com.godfather59.quransunnah

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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.godfather59.quransunnah.audio.AyahAudioItem
import com.godfather59.quransunnah.audio.RepeatMode
import com.godfather59.quransunnah.audio.Reciter
import com.godfather59.quransunnah.audio.globalAyahNumber
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
import com.godfather59.quransunnah.ui.LayoutDirection
import com.godfather59.quransunnah.ui.AppFontAssets
import com.godfather59.quransunnah.ui.layoutDirectionForLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.TimeZone

internal enum class QuranIndexMode {
    SURAHS,
    JUZ,
    HIZB,
    RUB,
    PAGE,
}

internal enum class ReaderMode {
    READ,
    STUDY,
    MEMORIZE,
    LISTEN,
}

internal enum class SearchCategory {
    ALL,
    QURAN,
    SUNNAH,
    TAFSIR,
    SURAHS,
}

internal data class ReaderEdition(
    val id: String,
    val label: String,
    val riwayaName: String,
    val scriptName: String,
)

internal data class TafsirOption(
    val id: String,
    val labelRes: Int,
)

internal data class ReaderFontOption(
    val id: String,
    val label: String,
    val assetPath: String?,
)

internal val readerEditions = listOf(
    ReaderEdition("hafs-an-asim__uthmani", "Hafs · Uthmani", "hafsAsim", "uthmani"),
    ReaderEdition("hafs-an-asim__imlai", "Hafs · Imla'i", "hafsAsim", "imlai"),
    ReaderEdition("hafs-an-asim__indopak", "Hafs · IndoPak", "hafsAsim", "indopak"),
    ReaderEdition("warsh-an-nafi__uthmani", "Warsh · Uthmani", "warshNafi", "uthmani"),
    ReaderEdition("qalun-an-nafi__uthmani", "Qalun · Uthmani", "qalunNafi", "uthmani"),
)

internal val tafsirOptions = listOf(
    TafsirOption("jalalayn", R.string.jalalayn),
    TafsirOption("siraj", R.string.siraj),
)

internal val readerFontOptions = listOf(
    ReaderFontOption("uthmani", "Amiri Quran", AppFontAssets.AMIRI_QURAN),
    ReaderFontOption("naskh", "Noto Naskh", AppFontAssets.NOTO_NASKH_ARABIC),
    ReaderFontOption("system", "System", null),
)

@Composable
fun HomeScreen(
    onOpenAyahRef: (String) -> Unit = {},
    onOpenTab: (Int) -> Unit = {},
    isActive: Boolean = true,
) {
    val context = LocalContext.current
    val language = currentLanguage()
    val fixedNow = LocalUiClockMillis.current
    var selectedSurah by rememberSaveable { mutableStateOf<Int?>(null) }
    var showPrayer by rememberSaveable { mutableStateOf(false) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var showDhikr by rememberSaveable { mutableStateOf(false) }
    var showMemorization by rememberSaveable { mutableStateOf(false) }
    var homeRefreshToken by rememberSaveable { mutableIntStateOf(0) }
    val alignment = if (layoutDirectionForLanguage(language) == LayoutDirection.RTL) {
        Alignment.End
    } else {
        Alignment.Start
    }
    // First-frame prefs + solar math off Main: PrayerPrefs/Dhikr/Memorization
    // disk reads + calculatePrayerTimes ran in LazyColumn composition before.
    val cachedDashboard = remember(homeRefreshToken, fixedNow) {
        HomeDashboardCache.peek(homeRefreshToken, fixedNow ?: System.currentTimeMillis())
    }
    var prayerSnap by remember(homeRefreshToken) {
        mutableStateOf(cachedDashboard?.prayer)
    }
    LaunchedEffect(context, homeRefreshToken, isActive, fixedNow) {
        if (!isActive) return@LaunchedEffect
        val snapshot = HomeDashboardCache.load(context, homeRefreshToken, fixedNow ?: System.currentTimeMillis())
        prayerSnap = snapshot.prayer
    }
    // Minute-boundary prayer countdown ticker: only while Home is the
    // settled destination and the app is foreground (was a bare loop).
    val minuteTick = rememberMinuteTicker(active = isActive && fixedNow == null)
    LaunchedEffect(minuteTick, prayerSnap, fixedNow) {
        if (fixedNow != null) return@LaunchedEffect
        val snapshot = prayerSnap ?: return@LaunchedEffect
        if (minuteTick <= 0L) return@LaunchedEffect
        prayerSnap = refreshPrayerSnapshot(snapshot, System.currentTimeMillis())
    }

    selectedSurah?.let { surahNumber ->
        BackHandler(enabled = isActive) {
            selectedSurah = null
            homeRefreshToken += 1
        }
        QuranReaderScreen(
            surahNumber = surahNumber,
            isActive = isActive,
            onBack = {
                selectedSurah = null
                homeRefreshToken += 1
            },
        )
        return
    }

    if (showPrayer) {
        BackHandler(enabled = isActive) {
            showPrayer = false
            homeRefreshToken += 1
        }
        PrayerScreen(onBack = {
            showPrayer = false
            homeRefreshToken += 1
        })
        return
    }

    if (showSearch) {
        BackHandler(enabled = isActive) { showSearch = false }
        SearchScreen(
            isActive = isActive,
            onOpenAyahRef = {
                onOpenAyahRef(it)
                showSearch = false
            },
            onBack = { showSearch = false },
        )
        return
    }

    if (showDhikr) {
        BackHandler(enabled = isActive) {
            showDhikr = false
            homeRefreshToken += 1
        }
        DhikrScreen(onBack = {
            showDhikr = false
            homeRefreshToken += 1
        })
        return
    }

    if (showMemorization) {
        BackHandler(enabled = isActive) { showMemorization = false }
        MemorizationScreen(
            onOpenAyahRef = {
                onOpenAyahRef(it)
                showMemorization = false
            },
            onBack = {
                showMemorization = false
                homeRefreshToken += 1
            },
        )
        return
    }

    EmeraldScaffold(
        header = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = hijriToday(language, fixedNow),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { showSearch = true }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search),
                        tint = Color.White,
                    )
                }
            }
            val snap = prayerSnap
            if (snap == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .background(
                            Color.White.copy(alpha = 0.18f),
                            RoundedCornerShape(16.dp),
                        ),
                )
            } else {
                val rem = remainingPrayerMinutes(snap, fixedNow ?: System.currentTimeMillis())
                Surface(color = Color.White.copy(alpha = 0.12f), shape = MaterialTheme.shapes.medium,
                    onClick = { showPrayer = true }) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(prayerName(snap.next.key), style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold, color = Color.White)
                            Text(remainingText(rem / 60, rem % 60, localizedCityName(snap.place.city, language)),
                                style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.White)
                    }
                }
            }
        },
        body = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = alignment,
            ) {
                item {
                    HomeHero(
                        language = language,
                        isActive = isActive,
                        onOpenReference = { surah, ayah ->
                            ReaderPrefs.saveLastPosition(context, surah, ayah)
                            selectedSurah = surah
                        },
                    )
                }
                item {
                    HomeShortcutGrid(
                        onOpenPrayer = { showPrayer = true },
                        onOpenDhikr = { showDhikr = true },
                        onOpenMemorization = { showMemorization = true },
                    )
                }
                item {
                    SectionTitle(stringResource(R.string.dailyAyah))
                }
                item {
                    DailyAyahCard(
                        isActive = isActive,
                        onOpen = { surah, ayah ->
                            ReaderPrefs.saveLastPosition(context, surah, ayah)
                            selectedSurah = surah
                        },
                    )
                }
                item {
                    SectionTitle(stringResource(R.string.dailyHadith))
                }
                item {
                    DailyHadithCard(isActive = isActive)
                }
            }
        },
    )
}

private data class HomeShortcut(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
)

@Composable
private fun HomeShortcutGrid(
    onOpenPrayer: () -> Unit,
    onOpenDhikr: () -> Unit,
    onOpenMemorization: () -> Unit,
) {
    val tiles = listOf(
        HomeShortcut(Icons.Default.Schedule, prayerScreenTitle(), onOpenPrayer),
        HomeShortcut(Icons.Default.Favorite, dhikrTitle(), onOpenDhikr),
        HomeShortcut(Icons.Default.School, memorizationTitle(), onOpenMemorization),
    )
    if (LocalDensity.current.fontScale >= 1.5f) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tiles.forEach { tile ->
                Card(
                    onClick = tile.onClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = tile.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = tile.label,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { tile ->
                    Card(
                        onClick = tile.onClick,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Icon(
                                    imageVector = tile.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(10.dp).size(24.dp),
                                )
                            }
                            Text(
                                text = tile.label,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                repeat(3 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HomeHero(
    language: String,
    isActive: Boolean,
    onOpenReference: (surah: Int, ayah: Int) -> Unit,
) {
    val context = LocalContext.current
    // Disk reads off Main: SharedPreferences first access hits disk.
    var lastSurah by rememberSaveable { mutableIntStateOf(2) }
    var lastAyah by rememberSaveable { mutableIntStateOf(255) }
    var streak by rememberSaveable { mutableIntStateOf(0) }
    var juz by rememberSaveable { mutableStateOf<Int?>(null) }
    var prefsReady by rememberSaveable { mutableStateOf(false) }
    var loadedPositionRevision by rememberSaveable { mutableIntStateOf(Int.MIN_VALUE) }
    val positionRevision = ReaderPrefs.positionRevision
    LaunchedEffect(context, positionRevision, isActive) {
        if (!isActive) return@LaunchedEffect
        if (prefsReady && loadedPositionRevision == positionRevision) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val state = try {
                ReaderPrefs.load(context)
            } catch (_: Exception) {
                null
            }
            val s = try {
                KhatmaPrefs.streak(context)
            } catch (_: Exception) {
                0
            }
            if (state != null) {
                lastSurah = state.lastSurah.coerceIn(1, 114)
                lastAyah = state.lastAyah
                juz = if (state.readerEditionId() == "hafs-an-asim__uthmani") {
                    runCatching { AndroidStores.mushafMetadataReader(context).load().juzOf(lastSurah, lastAyah) }.getOrNull()
                } else null
            }
            streak = s
            prefsReady = true
            loadedPositionRevision = positionRevision
        }
    }
    if (!prefsReady) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                    RoundedCornerShape(16.dp),
                ),
        )
        return
    }
    val surah = surahMetadata[lastSurah - 1]
    val ayah = lastAyah.coerceIn(1, surah.ayahCount)
    val global = remember(lastSurah, lastAyah) { globalAyahNumber(surah.number, ayah) }

    HeroGradientCard(
        onClick = { onOpenReference(surah.number, ayah) },
    ) {
        Text(
            text = stringResource(R.string.continueReading),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "${surah.localizedName(language)} ${surah.number}:$ayah",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "${stringResource(R.string.positionInQuran)} · ${(global * 100 / 6236)}%",
            style = MaterialTheme.typography.labelMedium,
        )
        LinearProgressIndicator(
            progress = { (global / 6236f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            juz?.let { part ->
                Text(text = "${stringResource(R.string.juz)} $part", style = MaterialTheme.typography.labelLarge)
            }
            if (streak > 0) {
            Text(
                text = stringResource(R.string.readingStreak, streak),
                style = MaterialTheme.typography.labelMedium,
            )
            }
        }
    }
}

private fun todayDayIndex(atMillis: Long? = null): Int {
    val cal = Calendar.getInstance()
    if (atMillis != null) {
        cal.timeZone = TimeZone.getTimeZone("UTC")
        cal.timeInMillis = atMillis
    }
    return (cal.get(Calendar.DAY_OF_YEAR) - 1).coerceAtLeast(0)
}

@Composable
private fun DailyAyahCard(
    isActive: Boolean,
    onOpen: (surah: Int, ayah: Int) -> Unit,
) {
    val context = LocalContext.current
    val reader = remember(context) { AndroidAssetReader(context) }
    val fixedNow = LocalUiClockMillis.current
    val dayIndex = remember(fixedNow) { todayDayIndex(fixedNow) }
    val (surah, ayah) = remember(dayIndex) { dailyAyahRef(dayIndex) }
    // Asset parse stays off the UI thread: this card sits on MAIN's very
    // first frame, where a 1.3 MB synchronous parse risks an ANR kill.
    var text by remember(dayIndex, surah, ayah) {
        mutableStateOf(HomeDailyContentCache.peekAyah(dayIndex, surah, ayah))
    }
    var loaded by remember(dayIndex, surah, ayah) {
        mutableStateOf(HomeDailyContentCache.peekAyah(dayIndex, surah, ayah) != null)
    }
    LaunchedEffect(reader, surah, ayah, isActive) {
        if (!isActive) return@LaunchedEffect
        text = HomeDailyContentCache.ayah(reader, dayIndex, surah, ayah)
        loaded = true
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dailyAyahCard")
            .semantics { uiContentReady = loaded && !text.isNullOrBlank() }
            .clickable { onOpen(surah, ayah) },
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!loaded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            RoundedCornerShape(6.dp),
                        ),
                )
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(22.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            RoundedCornerShape(6.dp),
                        ),
                )
            } else {
                val loadedText = text
                if (loadedText == null) {
                    Text(
                        text = stringResource(R.string.contentUnavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = loadedText,
                        style = MaterialTheme.typography.titleLarge.copy(textDirection = TextDirection.Rtl),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "${stringResource(R.string.surah)} $surah · ${stringResource(R.string.ayahLabel)} $ayah",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Deterministic daily hadith without parsing all 98 Bukhari sections (~7589
 * rows caused the Home scroll jank). Resolves the day's global ordinal into
 * its section via the small index.json, then loads that ONE section file.
 */
private fun loadDailyBukhari(reader: AndroidAssetReader, dayIndex: Int): Hadith? {
    val raw = reader.readText("assets/hadith/bukhari/index.json") ?: return null
    val index = try {
        parseHadithIndex(raw)
    } catch (_: Exception) {
        return null
    }
    val sections = index.sections.filter { it.count > 0 }
    val total = sections.sumOf { it.count }
    if (total <= 0) return null
    var ordinal = ((dayIndex % total) + total) % total
    for (section in sections) {
        if (ordinal < section.count) {
            val sectionRaw = reader.readText(
                "assets/hadith/bukhari/sections/${section.section}.json",
            ) ?: return null
            return try {
                val hadiths = parseHadithSection("bukhari", section.title, sectionRaw)
                    .filter { !it.isPlaceholder }
                hadiths.getOrNull(ordinal % hadiths.size.coerceAtLeast(1))
            } catch (_: Exception) {
                null
            }
        }
        ordinal -= section.count
    }
    return null
}

private data class DailyAyahCacheEntry(
    val dayIndex: Int,
    val surah: Int,
    val ayah: Int,
    val text: String?,
)

private data class DailyHadithCacheEntry(
    val dayIndex: Int,
    val hadith: Hadith?,
)

/** Keeps Home's verified daily picks in memory across top-level tab changes. */
private object HomeDailyContentCache {
    private val ayahMutex = kotlinx.coroutines.sync.Mutex()
    private val hadithMutex = kotlinx.coroutines.sync.Mutex()

    @Volatile
    private var ayahEntry: DailyAyahCacheEntry? = null

    @Volatile
    private var hadithEntry: DailyHadithCacheEntry? = null

    fun peekAyah(dayIndex: Int, surah: Int, ayah: Int): String? =
        ayahEntry?.takeIf {
            it.dayIndex == dayIndex && it.surah == surah && it.ayah == ayah
        }?.text

    fun peekHadith(dayIndex: Int): Hadith? =
        hadithEntry?.takeIf { it.dayIndex == dayIndex }?.hadith

    suspend fun ayah(
        reader: AndroidAssetReader,
        dayIndex: Int,
        surah: Int,
        ayah: Int,
    ): String? {
        ayahEntry?.takeIf {
            it.dayIndex == dayIndex && it.surah == surah && it.ayah == ayah
        }?.let { return it.text }
        return ayahMutex.withLock {
            ayahEntry?.takeIf {
                it.dayIndex == dayIndex && it.surah == surah && it.ayah == ayah
            }?.let { return@withLock it.text }
            val text = withContext(Dispatchers.IO) {
                loadQuranEdition(reader, editionId = "hafs-an-asim__uthmani")
                    ?.firstOrNull { it.surah == surah && it.ayah == ayah }
                    ?.text
                    ?.takeIf { it.isNotBlank() }
            }
            ayahEntry = DailyAyahCacheEntry(dayIndex, surah, ayah, text)
            text
        }
    }

    suspend fun hadith(reader: AndroidAssetReader, dayIndex: Int): Hadith? {
        hadithEntry?.takeIf { it.dayIndex == dayIndex }?.let { return it.hadith }
        return hadithMutex.withLock {
            hadithEntry?.takeIf { it.dayIndex == dayIndex }?.let { return@withLock it.hadith }
            val hadith = withContext(Dispatchers.IO) { loadDailyBukhari(reader, dayIndex) }
            hadithEntry = DailyHadithCacheEntry(dayIndex, hadith)
            hadith
        }
    }
}

@Composable
private fun DailyHadithCard(isActive: Boolean) {
    val context = LocalContext.current
    val language = currentLanguage()
    val reader = remember(context) { AndroidAssetReader(context) }
    val fixedNow = LocalUiClockMillis.current
    val dayIndex = remember(fixedNow) { todayDayIndex(fixedNow) }
    var hadith by remember(dayIndex) {
        mutableStateOf(HomeDailyContentCache.peekHadith(dayIndex))
    }
    var loaded by remember(dayIndex) {
        mutableStateOf(HomeDailyContentCache.peekHadith(dayIndex) != null)
    }
    LaunchedEffect(reader, dayIndex, isActive) {
        if (!isActive) return@LaunchedEffect
        hadith = HomeDailyContentCache.hadith(reader, dayIndex)
        loaded = true
    }
    Card(Modifier.fillMaxWidth().testTag("dailyHadithCard").semantics { uiContentReady = loaded && hadith != null }) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val item = hadith
            if (!loaded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            RoundedCornerShape(6.dp),
                        ),
                )
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(18.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            RoundedCornerShape(6.dp),
                        ),
                )
            } else if (item == null) {
                Text(
                    text = stringResource(R.string.contentUnavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = item.matnAr,
                    style = MaterialTheme.typography.titleMedium.copy(textDirection = TextDirection.Rtl),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                )
                Text(
                    text = stringResource(
                        R.string.dailyBukhariSource,
                        stringResource(R.string.hadith),
                        item.hadithNumber,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun MoreScreen(
    onOpenAyahRef: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    isActive: Boolean,
) {
    var route by rememberSaveable { mutableStateOf("menu") }
    BackHandler(enabled = isActive && route != "menu") { route = "menu" }

    when (route) {
        "search" -> {
            SearchScreen(
                isActive = isActive,
                onOpenAyahRef = onOpenAyahRef,
                onBack = { route = "menu" },
            )
        }
        "downloads" -> DownloadsScreen(
            isActive = isActive,
            onBack = { route = "menu" },
        )
        "settings" -> SettingsScreen(
            onThemeChange = onThemeChange,
            isActive = isActive,
            onBack = { route = "menu" },
        )
        else -> MoreMenuContent(
            onSearch = { route = "search" },
            onDownloads = { route = "downloads" },
            onSettings = { route = "settings" },
        )
    }
}

@Composable
private fun MoreMenuContent(
    onSearch: () -> Unit,
    onDownloads: () -> Unit,
    onSettings: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = 760.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.more),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            MoreMenuItem(R.string.search, Icons.Default.Search, onSearch)
            MoreMenuItem(R.string.downloads, Icons.Default.Download, onDownloads)
            MoreMenuItem(R.string.settings, Icons.Default.Settings, onSettings)
        }
    }
}

@Composable
private fun MoreMenuItem(
    titleRes: Int,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
fun PlaceholderScreen(titleRes: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.contentUnavailable),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun SectionTitle(text: String) {
    Spacer(Modifier.height(4.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
internal fun InfoBadge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = CircleShape,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
internal fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun LoadingVerifiedContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun EmptyVerifiedContent(
    messageRes: Int = R.string.contentUnavailable,
    onRetry: (() -> Unit)? = null,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

@Composable
internal fun currentLanguage(): String =
    LocalConfiguration.current.locales[0].language

private data class SurahDownloadUi(
    val downloaded: Boolean = false,
    val bytes: Long = 0L,
    val doneAyahs: Int = 0,
    val totalAyahs: Int = 0,
    val active: Boolean = false,
    val paused: Boolean = false,
    val queued: Boolean = false,
    val failed: Boolean = false,
)

// Current audio session: the sheet builds the queue, the full player screen
// and MiniPlayer observe it. openToken wakes the player screen without
// prop-drilling callbacks through four composable layers.
internal object NowPlaying {
    var items by mutableStateOf<List<AyahAudioItem>>(emptyList())
    var openToken by mutableIntStateOf(0)

    fun playQueue(queue: List<AyahAudioItem>) {
        items = queue
    }

    fun requestOpen() {
        openToken += 1
    }
}

@Composable
fun DownloadsScreen(
    isActive: Boolean = true,
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val language = currentLanguage()
    val manager = remember(context) { AndroidStores.downloadsManager(context) }
    var selectedReciterId by rememberSaveable { mutableStateOf("ar.alafasy") }
    var scanToken by rememberSaveable { mutableIntStateOf(0) }

    val activeKey by manager.activeKey.collectAsState()
    val queuedKeys by manager.queuedKeys.collectAsState()
    val pausedKeys by manager.pausedKeys.collectAsState()
    val failedKeys by manager.failedKeys.collectAsState()
    val doneAyahs by manager.doneAyahs.collectAsState()
    val completedKeys by manager.completedKeys.collectAsState()
    val completedBytes by manager.completedBytes.collectAsState()
    val downloadScanLoading by manager.scanLoading.collectAsState()
    val downloadScanFailed by manager.scanFailed.collectAsState()

    LaunchedEffect(selectedReciterId, scanToken, isActive) {
        if (!isActive) return@LaunchedEffect
        manager.rescan(selectedReciterId)
    }

    val surahMetadata = remember { surahMetadata }
    val reciters = remember { reciters }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp)
            .testTag("downloadsList"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.downloadManager),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.audioDownloadsHint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Text(
                text = stringResource(R.string.reciterStorage),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reciters) { reciter ->
                    FilterChip(
                        selected = reciter.identifier == selectedReciterId,
                        onClick = { selectedReciterId = reciter.identifier },
                        label = { Text(reciter.localizedName(language)) },
                    )
                }
            }
        }
        item {
            Spacer(Modifier.height(16.dp))
        }
        item {
            SectionTitle(stringResource(R.string.quranAudioDownloads))
        }
        if (downloadScanLoading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(
                        text = stringResource(R.string.scanningDownloads),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else if (downloadScanFailed) {
            item {
                EmptyVerifiedContent(
                    messageRes = R.string.downloadScanFailed,
                    onRetry = { scanToken += 1 },
                )
            }
        }
        items(surahMetadata, key = { it.number }) { surah ->
            val surahNumber = surah.number
            val key = "$selectedReciterId:$surahNumber"
            val ui = SurahDownloadUi(
                downloaded = key in completedKeys,
                bytes = completedBytes[key] ?: 0L,
                doneAyahs = doneAyahs[key] ?: 0,
                totalAyahs = surah.ayahCount,
                active = activeKey == key,
                paused = key in pausedKeys,
                queued = key in queuedKeys,
                failed = key in failedKeys,
            )
            DownloadSurahCard(
                surah = surah,
                state = ui,
                onDownload = {
                    manager.enqueue(selectedReciterId, surahNumber)
                },
                onDelete = {
                    manager.deleteSurah(selectedReciterId, surahNumber) {
                        scanToken += 1
                    }
                },
                onPauseToggle = {
                    manager.togglePause(key)
                },
                onCancel = {
                    manager.cancel(key)
                },
            )
        }
    }
}

@Composable
private fun DownloadSurahCard(
    surah: SurahMeta,
    state: SurahDownloadUi,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onPauseToggle: () -> Unit,
    onCancel: () -> Unit,
) {
    val language = currentLanguage()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = surah.localizedName(language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    val statusLine = when {
                        state.downloaded -> {
                            "${stringResource(R.string.downloaded)} · ${AudioFiles.formatBytes(state.bytes)}"
                        }
                        state.active -> {
                            "${stringResource(R.string.downloading)} ${state.doneAyahs}/${state.totalAyahs}"
                        }
                        state.queued -> stringResource(R.string.queued)
                        state.failed -> stringResource(R.string.downloadFailed)
                        else -> "${surah.ayahCount} ${stringResource(R.string.ayat)}"
                    }
                    Text(
                        text = statusLine,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.failed) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                if (state.downloaded) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                    ) {
                        Text(stringResource(R.string.remove))
                    }
                } else if (state.active) {
                    TextButton(
                        onClick = onPauseToggle,
                    ) {
                        Text(
                            if (state.paused) {
                                stringResource(R.string.resume)
                            } else {
                                stringResource(R.string.pause)
                            },
                        )
                    }
                    TextButton(onClick = onCancel) {
                        Text(stringResource(R.string.cancel))
                    }
                } else if (state.queued) {
                    TextButton(onClick = onCancel) {
                        Text(stringResource(R.string.cancel))
                    }
                } else {
                    Button(onClick = onDownload) {
                        Text(
                            if (state.failed) {
                                stringResource(R.string.retry)
                            } else {
                                stringResource(R.string.download)
                            },
                        )
                    }
                }
            }
            if (state.active && state.totalAyahs > 0) {
                val percent = ((state.doneAyahs.toFloat() / state.totalAyahs) * 100)
                    .toInt()
                    .coerceIn(0, 100)
                LinearProgressIndicator(
                    progress = {
                        (state.doneAyahs.toFloat() / state.totalAyahs).coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            stateDescription = "$percent%"
                        }
                        .testTag("surahDownloadProgress"),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
internal fun ayahNumberText(number: Int, styleName: String): String = when (styleName) {
    "latin" -> number.toString()
    else -> toArabicIndic(number)
}

internal fun SurahMeta.localizedName(language: String): String =
    when (language) {
        "ar" -> nameAr
        "fr" -> nameFr
        else -> nameEn
    }

internal fun Reciter.localizedName(language: String): String =
    when (language) {
        "ar" -> nameAr
        else -> nameEn
    }

internal fun ReaderPrefsState.readerEditionId(): String =
    resolveReaderEdition(riwayaName, scriptName).id

/** Repair an unsupported script within its Riwaya, without changing the transmission. */
internal fun resolveReaderEdition(riwayaName: String, scriptName: String): ReaderEdition =
    readerEditions.firstOrNull { it.riwayaName == riwayaName && it.scriptName == scriptName }
        ?: readerEditions.firstOrNull { it.riwayaName == riwayaName }
        ?: readerEditions.first()

private fun formatPlayerTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    return "${totalSec / 60}:${(totalSec % 60).toString().padStart(2, '0')}"
}

@Composable
internal fun AudioPlayerScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val language = currentLanguage()
    val queue = NowPlaying.items
    val reader = remember(context) { AndroidAssetReader(context) }
    val audioPlayer = remember(context) {
        try {
            AndroidStores.audio(context)
        } catch (_: Exception) {
            null
        }
    }
    val scope = rememberCoroutineScope()
    var prefs by remember { mutableStateOf(ReaderPrefsState()) }
    var speed by remember { mutableStateOf(1f) }
    LaunchedEffect(context) {
        val loaded = withContext(Dispatchers.IO) {
            ReaderPrefs.load(context) to AudioPrefs.speed(context).toFloat()
        }
        prefs = loaded.first
        speed = loaded.second
    }
    val quranFontFamily = remember(context, prefs) {
        quranFontFamily(context, prefs.fontName)
    }

    val playerState = rememberPlayerState(audioPlayer)
    val currentRefKey = playerState.currentRefKey
    val currentItem = playerState.currentItem
    val isPlaying = playerState.isPlaying
    val positionMs = playerState.positionMs
    val durationMs = playerState.durationMs
    var sliderOverride by remember { mutableStateOf<Long?>(null) }
    var repeatMode by remember { mutableStateOf(RepeatMode.OFF) }
    var sleepMinutes by remember { mutableStateOf<Int?>(null) }
    var texts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var translationMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var textsLoading by remember { mutableStateOf(true) }

    // Verified texts for the queued surah + translation, off Main.
    LaunchedEffect(reader, queue) {
        textsLoading = true
        val loaded = withContext(Dispatchers.IO) {
            try {
                val editionId = queue.firstOrNull()?.editionId
                    ?.takeIf { it.isNotBlank() } ?: "hafs-an-asim__uthmani"
                val surah = queue.firstOrNull()?.refKey
                    ?.substringBefore(':')?.toIntOrNull()
                val map = loadQuranEdition(reader, editionId = editionId)
                    ?.filter { surah == null || it.surah == surah }
                    ?.associate { "${it.surah}:${it.ayah}" to it.text }
                    .orEmpty()
                val trPath = translationAssets["en-sahih"]
                val trRaw = trPath?.let { reader.readText(it) }
                map to if (trRaw == null) emptyMap() else parsePipeMap(trRaw)
            } catch (_: Exception) {
                emptyMap<String, String>() to emptyMap()
            }
        }
        texts = loaded.first
        translationMap = loaded.second
        textsLoading = false
    }

    if (queue.isEmpty()) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                    )
                }
                Text(
                    text = stringResource(R.string.audioPlayer),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
    ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.contentUnavailable),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        return
    }

    val refKey = currentRefKey ?: queue.first().refKey
    val refParts = refKey.split(':')
    val refSurah = refParts.getOrNull(0)?.toIntOrNull() ?: 1
    val refAyah = refParts.getOrNull(1)?.toIntOrNull() ?: 1
    val queueIndex = queue.indexOfFirst { it.refKey == refKey }.coerceAtLeast(0)
    val item = currentItem ?: queue[queueIndex]
    val arabicText = texts[refKey]
    val translated = translationMap[refKey]
    val sliderPos = sliderOverride ?: positionMs
    val hasDuration = durationMs > 0

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.audioPlayer),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = item.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "${queueIndex + 1} / ${queue.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (textsLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHighest,
                                    RoundedCornerShape(8.dp),
                                ),
                        )
                    } else if (arabicText.isNullOrBlank()) {
                        Text(
                            text = stringResource(R.string.contentUnavailable),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    } else {
                        Text(
                            text = arabicText,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.headlineSmall.copy(textDirection = TextDirection.Rtl),
                            fontFamily = quranFontFamily,
                            textAlign = TextAlign.Right,
                        )
                    }
                    if (!translated.isNullOrBlank()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = stringResource(R.string.saheehInternational),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = translated,
                                style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr),
                                textAlign = TextAlign.Left,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        text = "${stringResource(R.string.surah)} $refSurah · ${stringResource(R.string.ayahLabel)} $refAyah",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Slider(
                    value = sliderPos.toFloat(),
                    onValueChange = { sliderOverride = it.toLong() },
                    onValueChangeFinished = {
                        val target = sliderOverride
                        sliderOverride = null
                        if (target != null) {
                            scope.launch {
                                try {
                                    audioPlayer?.seekTo(target)
                                } catch (_: Exception) {
                                }
                            }
                        }
                    },
                    valueRange = 0f..(if (hasDuration) durationMs.toFloat() else 1f),
                    enabled = hasDuration,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = formatPlayerTime(sliderPos),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = if (hasDuration) formatPlayerTime(durationMs) else "--:--",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            try {
                                audioPlayer?.previous()
                            } catch (_: Exception) {
                            }
                        }
                    },
                    enabled = queueIndex > 0,
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = stringResource(R.string.previous),
                        modifier = Modifier.size(36.dp),
                    )
                }
                IconButton(
                    onClick = {
                        scope.launch {
                            try {
                                if (isPlaying) audioPlayer?.pause() else audioPlayer?.play()
                            } catch (_: Exception) {
                            }
                        }
                    },
                    modifier = Modifier.size(64.dp),
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) {
                            stringResource(R.string.pause)
                        } else {
                            stringResource(R.string.play)
                        },
                        modifier = Modifier.size(48.dp),
                    )
                }
                IconButton(
                    onClick = {
                        scope.launch {
                            try {
                                audioPlayer?.next()
                            } catch (_: Exception) {
                            }
                        }
                    },
                    enabled = queueIndex < queue.size - 1,
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = stringResource(R.string.next),
                        modifier = Modifier.size(36.dp),
                    )
                }
                IconButton(
                    onClick = {
                        QuranAudio.stopAll(context)
                        onBack()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = stringResource(R.string.stop),
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.speed),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(0.75f, 1f, 1.25f, 1.5f, 2f)) { value ->
                    val label = if (value == 1f) "1×" else "${value}×"
                    FilterChip(
                        selected = speed == value,
                        onClick = {
                            speed = value
                            AudioPrefs.saveSpeed(context, value.toDouble())
                            scope.launch {
                                try {
                                    audioPlayer?.setSpeed(value)
                                } catch (_: Exception) {
                                }
                            }
                        },
                        label = { Text(label) },
                    )
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.repeat),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        item {
            val repeatOffLabel = stringResource(R.string.repeatOff)
            val repeatOneLabel = stringResource(R.string.repeatOne)
            val repeatAllLabel = stringResource(R.string.repeatAll)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val modes = listOf(
                    RepeatMode.OFF to repeatOffLabel,
                    RepeatMode.ONE to repeatOneLabel,
                    RepeatMode.ALL to repeatAllLabel,
                )
                items(modes, key = { it.first }) { (mode, label) ->
                    FilterChip(
                        selected = repeatMode == mode,
                        onClick = {
                            repeatMode = mode
                            scope.launch {
                                try {
                                    audioPlayer?.setRepeatMode(mode)
                                } catch (_: Exception) {
                                }
                            }
                        },
                        label = { Text(label) },
                    )
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.sleepTimer),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val options: List<Int?> = listOf(null, 5, 15, 30, 60)
                items(options, key = { it?.toString() ?: "off" }) { minutes ->
                    val label = minutes?.toString()
                        ?: stringResource(R.string.sleepOff)
                    FilterChip(
                        selected = sleepMinutes == minutes,
                        onClick = {
                            sleepMinutes = minutes
                            audioPlayer?.sleepTimer(
                                minutes?.times(60_000L),
                            )
                        },
                        label = { Text(label) },
                    )
                }
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
        }
        }
    }
}

internal fun quranFontFamily(
    context: Context,
    fontName: String,
): FontFamily? {
    val option = readerFontOptions.firstOrNull { it.id == fontName }
        ?: readerFontOptions.first()
    val assetPath = option.assetPath ?: return null
    // Missing/corrupt font asset must fall back to system, never crash.
    return try {
        FontFamily(
            Font(
                path = assetPath.removePrefix("assets/"),
                assetManager = context.applicationContext.assets,
            ),
        )
    } catch (_: Exception) {
        null
    }
}

@Composable
internal fun QuranIndexMode.label(): String =
    when (this) {
        QuranIndexMode.SURAHS -> stringResource(R.string.surahs)
        QuranIndexMode.JUZ -> stringResource(R.string.juz)
        QuranIndexMode.HIZB -> stringResource(R.string.hizb)
        QuranIndexMode.RUB -> stringResource(R.string.rub)
        QuranIndexMode.PAGE -> stringResource(R.string.page)
    }
