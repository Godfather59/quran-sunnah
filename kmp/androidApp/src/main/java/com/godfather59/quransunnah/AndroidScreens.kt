package com.godfather59.quransunnah

import android.Manifest
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.godfather59.quransunnah.audio.AyahAudioItem
import com.godfather59.quransunnah.audio.AyahDownload
import com.godfather59.quransunnah.audio.DownloadCancelledException
import com.godfather59.quransunnah.audio.DownloadControl
import com.godfather59.quransunnah.audio.DownloadQueue
import com.godfather59.quransunnah.audio.QueuedSurah
import com.godfather59.quransunnah.audio.Reciter
import com.godfather59.quransunnah.audio.globalAyahNumber
import com.godfather59.quransunnah.audio.reciterById
import com.godfather59.quransunnah.audio.reciters
import com.godfather59.quransunnah.data.AndroidAssetReader
import com.godfather59.quransunnah.data.loadQuranEdition
import com.godfather59.quransunnah.data.loadTafsirSurah
import com.godfather59.quransunnah.data.loadWordsSurah
import com.godfather59.quransunnah.data.parsePipeMap
import com.godfather59.quransunnah.db.LibraryStore
import com.godfather59.quransunnah.device.AndroidBackupPicker
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
import com.godfather59.quransunnah.prayer.nextPrayer
import com.godfather59.quransunnah.prayer.prayerCityPresets
import com.godfather59.quransunnah.prayer.qiblaBearing
import com.godfather59.quransunnah.library.CustomCollection
import com.godfather59.quransunnah.library.Highlight
import com.godfather59.quransunnah.library.UserNote
import com.godfather59.quransunnah.text.normalizeArabic
import com.godfather59.quransunnah.text.toArabicIndic
import com.godfather59.quransunnah.data.HadithSection
import com.godfather59.quransunnah.data.parseHadithIndex
import com.godfather59.quransunnah.data.parseHadithSection
import com.godfather59.quransunnah.hadith.Hadith
import com.godfather59.quransunnah.hadith.HadithCollection
import com.godfather59.quransunnah.hadith.hadithCollections
import com.godfather59.quransunnah.library.Bookmark
import com.godfather59.quransunnah.library.BookmarkKind
import com.godfather59.quransunnah.library.RecentItem
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
import com.godfather59.quransunnah.search.SearchOptions
import com.godfather59.quransunnah.search.SearchResults
import com.godfather59.quransunnah.ui.LayoutDirection
import com.godfather59.quransunnah.ui.AppFontAssets
import com.godfather59.quransunnah.ui.layoutDirectionForLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import java.util.TimeZone

private enum class QuranIndexMode {
    SURAHS,
    JUZ,
    HIZB,
    RUB,
    PAGE,
}

private data class ReaderEdition(
    val id: String,
    val label: String,
    val riwayaName: String,
    val scriptName: String,
)

private data class TafsirOption(
    val id: String,
    val labelRes: Int,
)

private data class ReaderFontOption(
    val id: String,
    val label: String,
    val assetPath: String?,
)

private val readerEditions = listOf(
    ReaderEdition("hafs-an-asim__uthmani", "Hafs · Uthmani", "hafsAsim", "uthmani"),
    ReaderEdition("hafs-an-asim__imlai", "Hafs · Imla'i", "hafsAsim", "imlai"),
    ReaderEdition("hafs-an-asim__indopak", "Hafs · IndoPak", "hafsAsim", "indopak"),
    ReaderEdition("warsh-an-nafi__uthmani", "Warsh · Uthmani", "warshNafi", "uthmani"),
    ReaderEdition("qalun-an-nafi__uthmani", "Qalun · Uthmani", "qalunNafi", "uthmani"),
)

private val tafsirOptions = listOf(
    TafsirOption("jalalayn", R.string.jalalayn),
    TafsirOption("siraj", R.string.siraj),
)

private val readerFontOptions = listOf(
    ReaderFontOption("uthmani", "Amiri Quran", AppFontAssets.AMIRI_QURAN),
    ReaderFontOption("naskh", "Noto Naskh", AppFontAssets.NOTO_NASKH_ARABIC),
    ReaderFontOption("system", "System", null),
)

@Composable
fun HomeScreen(
    onOpenAyahRef: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val language = currentLanguage()
    var selectedSurah by rememberSaveable { mutableStateOf<Int?>(null) }
    var showPrayer by rememberSaveable { mutableStateOf(false) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var showDhikr by rememberSaveable { mutableStateOf(false) }
    var showMemorization by rememberSaveable { mutableStateOf(false) }
    var libraryRefreshToken by rememberSaveable { mutableIntStateOf(0) }
    val alignment = if (layoutDirectionForLanguage(language) == LayoutDirection.RTL) {
        Alignment.End
    } else {
        Alignment.Start
    }
    val libraryStore = remember(context) { AndroidStores.library(context) }
    var bookmarks by remember { mutableStateOf(emptyList<Bookmark>()) }
    var recentItems by remember { mutableStateOf(emptyList<RecentItem>()) }
    // First-frame prefs + solar math off Main: PrayerPrefs/Dhikr/Memorization
    // disk reads + calculatePrayerTimes ran in LazyColumn composition before.
    var prayerSnap by remember { mutableStateOf<PrayerSnapshot?>(null) }
    var dhikrState by remember { mutableStateOf<DhikrState?>(null) }
    var memorizedCount by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(libraryStore, libraryRefreshToken) {
        val loaded = withContext(Dispatchers.IO) {
            val bookmarkRows = libraryStore.bookmarks()
                .filter { it.kind == BookmarkKind.AYAH }
                .take(5)
            val recentRows = libraryStore.recent().take(5)
            bookmarkRows to recentRows
        }
        bookmarks = loaded.first
        recentItems = loaded.second
    }
    LaunchedEffect(context, showPrayer, showDhikr, showMemorization, libraryRefreshToken) {
        withContext(Dispatchers.IO) {
            val pp = try {
                PrayerPrefs.load(context)
            } catch (_: Exception) {
                null
            }
            val snap = try {
                if (pp == null) {
                    null
                } else {
                    prayerSnapshot(
                        pp.methodName,
                        pp.city,
                        pp.latitude,
                        pp.longitude,
                        pp.tzOffsetHours,
                        System.currentTimeMillis(),
                    )
                }
            } catch (_: Exception) {
                null
            }
            val dhikr = try {
                DhikrPrefs.load(context)
            } catch (_: Exception) {
                null
            }
            val memCount = try {
                MemorizationPrefs.load(context).size
            } catch (_: Exception) {
                null
            }
            prayerSnap = snap
            dhikrState = dhikr
            memorizedCount = memCount
        }
    }

    selectedSurah?.let { surahNumber ->
        QuranReaderScreen(
            surahNumber = surahNumber,
            onBack = {
                selectedSurah = null
                libraryRefreshToken += 1
            },
        )
        return
    }

    if (showPrayer) {
        PrayerScreen(onBack = { showPrayer = false })
        return
    }

    if (showSearch) {
        SearchScreen(
            onOpenAyahRef = {
                onOpenAyahRef(it)
                showSearch = false
            },
            onBack = { showSearch = false },
        )
        return
    }

    if (showDhikr) {
        DhikrScreen(onBack = { showDhikr = false })
        return
    }

    if (showMemorization) {
        MemorizationScreen(
            onOpenAyahRef = {
                onOpenAyahRef(it)
                showMemorization = false
            },
            onBack = { showMemorization = false },
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = alignment,
    ) {
        item {
            Text(
                text = stringResource(R.string.appTitle),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        item {
            Button(
                onClick = { showSearch = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.search))
            }
        }
        item {
            HomeHero(
                language = language,
                refreshToken = libraryRefreshToken,
                onOpenReference = { surah, ayah ->
                    ReaderPrefs.saveLastPosition(context, surah, ayah)
                    selectedSurah = surah
                },
            )
        }
        item {
            val snap = prayerSnap
            if (snap == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            RoundedCornerShape(12.dp),
                        ),
                )
            } else {
                PrayerHeroCard(
                    snap = snap,
                    language = language,
                    onClick = { showPrayer = true },
                )
            }
        }
        item {
            val dhikr = dhikrState
            if (dhikr == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            RoundedCornerShape(12.dp),
                        ),
                )
            } else {
                DhikrHeroCard(
                    todayTotal = dhikr.today.values.sum(),
                    total = dhikr.total,
                    language = language,
                    onClick = { showDhikr = true },
                )
            }
        }
        item {
            val count = memorizedCount
            if (count == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            RoundedCornerShape(12.dp),
                        ),
                )
            } else {
                MemorizationHeroCard(
                    totalMemorized = count,
                    language = language,
                    onClick = { showMemorization = true },
                )
            }
        }
        item {
            SectionTitle(stringResource(R.string.atAGlance))
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatCard(
                    label = stringResource(R.string.surahs),
                    value = surahMetadata.size.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(R.string.ayat),
                    value = surahMetadata.sumOf { it.ayahCount }.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatCard(
                    label = stringResource(R.string.hadithCollections),
                    value = hadithCollections.size.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(R.string.translations),
                    value = translationAssets.size.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            SectionTitle(stringResource(R.string.dailyAyah))
        }
        item {
            DailyAyahCard(
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
            DailyHadithCard()
        }
        item {
            SectionTitle(stringResource(R.string.bookmarks))
        }
        if (bookmarks.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.noBookmarksYet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(bookmarks, key = { it.id }) { item ->
                BookmarkRow(
                    item = item,
                    onClick = {
                        val parts = item.refKey.split(":")
                        val surah = parts.getOrNull(0)?.toIntOrNull()
                        val ayah = parts.getOrNull(1)?.toIntOrNull()
                        if (surah != null && ayah != null) {
                            ReaderPrefs.saveLastPosition(context, surah, ayah)
                            selectedSurah = surah
                        }
                    },
                )
            }
        }
        item {
            SectionTitle(stringResource(R.string.recentlyViewed))
        }
        if (recentItems.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.noBookmarksYet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(recentItems, key = { it.refKey }) { item ->
                RecentRow(
                    item = item,
                    onClick = {
                        val parts = item.refKey.split(":")
                        val surah = parts.getOrNull(0)?.toIntOrNull()
                        val ayah = parts.getOrNull(1)?.toIntOrNull()
                        if (surah != null && ayah != null) {
                            ReaderPrefs.saveLastPosition(context, surah, ayah)
                            selectedSurah = surah
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun HomeHero(
    language: String,
    refreshToken: Int = 0,
    onOpenReference: (surah: Int, ayah: Int) -> Unit,
) {
    val context = LocalContext.current
    // Disk reads off Main: SharedPreferences first access hits disk.
    var lastSurah by remember { mutableStateOf(2) }
    var lastAyah by remember { mutableStateOf(255) }
    var streak by remember { mutableStateOf(0) }
    var prefsReady by remember { mutableStateOf(false) }
    LaunchedEffect(context, refreshToken) {
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
            }
            streak = s
            prefsReady = true
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
    val juz = remember(global) { ((global - 1) * 30 / 6236) + 1 }

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
        LinearProgressIndicator(
            progress = { (global / 6236f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
        )
        AssistChip(
            onClick = {},
            label = {
                Text("$streak · ${stringResource(R.string.juz)} $juz")
            },
        )
        Text(
            text = stringResource(R.string.verifiedDatasetsInstalled),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun todayDayIndex(): Int {
    val cal = Calendar.getInstance()
    return (cal.get(Calendar.DAY_OF_YEAR) - 1).coerceAtLeast(0)
}

@Composable
private fun DailyAyahCard(
    onOpen: (surah: Int, ayah: Int) -> Unit,
) {
    val context = LocalContext.current
    val reader = remember(context) { AndroidAssetReader(context) }
    val dayIndex = remember { todayDayIndex() }
    val (surah, ayah) = remember(dayIndex) { dailyAyahRef(dayIndex) }
    // Asset parse stays off the UI thread: this card sits on MAIN's very
    // first frame, where a 1.3 MB synchronous parse risks an ANR kill.
    var text by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(reader, surah, ayah) {
        text = withContext(Dispatchers.IO) {
            loadQuranEdition(reader, editionId = "hafs-an-asim__uthmani")
                ?.firstOrNull { it.surah == surah && it.ayah == ayah }
                ?.text
                ?.takeIf { it.isNotBlank() }
        }
        loaded = true
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.End,
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

private fun loadAllBukhari(reader: AndroidAssetReader): List<Hadith> {
    val raw = reader.readText("assets/hadith/bukhari/index.json") ?: return emptyList()
    val index = try {
        parseHadithIndex(raw)
    } catch (_: Exception) {
        return emptyList()
    }
    val out = mutableListOf<Hadith>()
    for (section in index.sections) {
        if (section.count <= 0) continue
        val sectionRaw = reader.readText("assets/hadith/bukhari/sections/${section.section}.json")
            ?: continue
        try {
            out.addAll(parseHadithSection("bukhari", section.title, sectionRaw))
        } catch (_: Exception) {
        }
    }
    return out.filter { !it.isPlaceholder }
}

@Composable
private fun DailyHadithCard() {
    val context = LocalContext.current
    val language = currentLanguage()
    val reader = remember(context) { AndroidAssetReader(context) }
    var hadith by remember { mutableStateOf<Hadith?>(null) }
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(reader) {
        val list = withContext(Dispatchers.IO) { loadAllBukhari(reader) }
        if (list.isNotEmpty()) {
            hadith = list[todayDayIndex() % list.size]
        }
        loaded = true
    }
    Card(Modifier.fillMaxWidth()) {
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
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                )
                Text(
                    text = if (language == "ar") {
                        "صحيح البخاري · ${stringResource(R.string.hadith)} ${item.hadithNumber}"
                    } else {
                        "Sahih al-Bukhari · ${stringResource(R.string.hadith)} ${item.hadithNumber}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun QuranIndexScreen(
    deepLinkSurah: Int? = null,
    deepLinkSurahToken: Int = 0,
) {
    val context = LocalContext.current
    val mushafReader = remember(context) { AndroidStores.mushafMetadataReader(context) }
    var mushafMeta by remember { mutableStateOf<MushafMetadata?>(null) }
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var selectedSurah by rememberSaveable { mutableStateOf<Int?>(null) }
    var mushafPage by rememberSaveable { mutableStateOf<Int?>(null) }
    val selected = QuranIndexMode.entries[mode]

    LaunchedEffect(mushafReader) {
        mushafMeta = try {
            withContext(Dispatchers.IO) { mushafReader.load() }
        } catch (_: Exception) {
            null
        }
    }

    // `quran://s/a` deep links (cold-start included): the ayah travels via
    // ReaderPrefs (see AdaptiveAppScaffold), the surah opens the reader here.
    LaunchedEffect(deepLinkSurahToken) {
        if (deepLinkSurah != null) {
            mushafPage = null
            selectedSurah = deepLinkSurah
        }
    }

    mushafPage?.let { page ->
        MushafReaderScreen(
            initialPage = page,
            onBack = { mushafPage = null },
        )
        return
    }

    selectedSurah?.let { surahNumber ->
        QuranReaderScreen(
            surahNumber = surahNumber,
            onBack = { selectedSurah = null },
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.quran),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(QuranIndexMode.entries) { item ->
                FilterChip(
                    selected = selected == item,
                    onClick = { mode = item.ordinal },
                    label = { Text(item.label()) },
                )
            }
        }

        when (selected) {
            QuranIndexMode.SURAHS -> SurahList(onSurahSelected = { selectedSurah = it })
            QuranIndexMode.JUZ -> NumberGrid(
                1..30,
                stringResource(R.string.juz),
                onSelected = { juz ->
                    val meta = mushafMeta
                    val start = meta?.juzStarts?.getOrNull(juz - 1)
                    if (meta != null && start != null) {
                        mushafPage = meta.pageOf(start.surah, start.ayah)
                    }
                },
            )
            QuranIndexMode.HIZB -> NumberGrid(
                1..60,
                stringResource(R.string.hizb),
                onSelected = { hizb ->
                    val meta = mushafMeta
                    val start = meta?.quarterStarts?.getOrNull((hizb - 1) * 4)
                    if (meta != null && start != null) {
                        mushafPage = meta.pageOf(start.surah, start.ayah)
                    }
                },
            )
            QuranIndexMode.RUB -> NumberGrid(
                1..240,
                stringResource(R.string.rub),
                onSelected = { quarter ->
                    val meta = mushafMeta
                    val start = meta?.quarterStarts?.getOrNull(quarter - 1)
                    if (meta != null && start != null) {
                        mushafPage = meta.pageOf(start.surah, start.ayah)
                    }
                },
            )
            QuranIndexMode.PAGE -> NumberGrid(
                1..604,
                stringResource(R.string.page),
                onSelected = { mushafPage = it },
            )
        }
    }
}

@Composable
private fun SurahList(onSurahSelected: (Int) -> Unit) {
    val language = currentLanguage()
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(surahMetadata, key = { it.number }) { surah ->
            SurahRow(
                surah = surah,
                language = language,
                onClick = { onSurahSelected(surah.number) },
            )
        }
    }
}

@Composable
private fun SurahRow(
    surah: SurahMeta,
    language: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = surah.number.toString(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = surah.localizedName(language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${surah.ayahCount} ${stringResource(R.string.ayat)} · " +
                        stringResource(if (surah.makki) R.string.makki else R.string.madani),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = surah.nameAr,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun QuranReaderScreen(
    surahNumber: Int,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val language = currentLanguage()
    val surah = surahMetadata.first { it.number == surahNumber }
    val prefs = remember(context) { ReaderPrefs.load(context) }
    val initialEditionId = remember(prefs) { prefs.readerEditionId() }
    val initialAyah = remember(prefs, surahNumber) {
        if (prefs.lastSurah == surahNumber) {
            prefs.lastAyah.coerceIn(1, surah.ayahCount)
        } else {
            1
        }
    }
    var editionId by rememberSaveable { mutableStateOf(initialEditionId) }
    var fontName by rememberSaveable { mutableStateOf(prefs.fontName) }
    var reciterId by rememberSaveable { mutableStateOf(prefs.reciterId) }
    var showTranslation by rememberSaveable { mutableStateOf(prefs.showTranslation) }
    val numberStyle = remember(prefs) { prefs.ayahNumberStyleName }
    var selectedAyah by remember { mutableStateOf<Ayah?>(null) }
    var tafsirAyah by remember { mutableStateOf<Ayah?>(null) }
    var compareAyah by remember { mutableStateOf<Ayah?>(null) }
    var studyAyah by remember { mutableStateOf<Ayah?>(null) }
    var showFontSheet by remember { mutableStateOf(false) }
    var showMushaf by rememberSaveable { mutableStateOf(false) }
    var hideMode by rememberSaveable { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(emptySet<String>()) }
    var memorized by remember { mutableStateOf(MemorizationPrefs.load(context)) }

    if (showMushaf) {
        MushafReaderScreen(
            initialSurah = surahNumber,
            onBack = { showMushaf = false },
        )
        return
    }
    val reader = remember(context) { AndroidAssetReader(context) }
    val quranFontFamily = remember(context, fontName) {
        quranFontFamily(context, fontName)
    }
    val libraryStore = remember(context) { AndroidStores.library(context) }
    val ayahListState = rememberLazyListState(
        initialFirstVisibleItemIndex = (initialAyah - 1).coerceAtLeast(0),
    )
    LaunchedEffect(context, libraryStore, language, surahNumber, initialAyah) {
        ReaderPrefs.saveLastPosition(context, surahNumber, initialAyah)
        KhatmaPrefs.touchToday(context)
        withContext(Dispatchers.IO) {
            libraryStore.touchRecent(
                RecentItem(
                    refKey = "$surahNumber:$initialAyah",
                    title = surah.localizedName(language),
                    subtitle = "$surahNumber:$initialAyah",
                    kind = BookmarkKind.AYAH,
                ),
            )
        }
    }
    // Heavy verified-asset parse off the Main thread: 1.3 MB text + filter.
    // remember() ran this in composition and janked surah/edition switches.
    var ayahs by remember { mutableStateOf<List<Ayah>>(emptyList()) }
    LaunchedEffect(surahNumber, editionId) {
        ayahs = withContext(Dispatchers.IO) {
            try {
                loadQuranEdition(
                    reader,
                    editionId = editionId,
                )?.filter { it.surah == surahNumber }.orEmpty()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
    var translation by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    LaunchedEffect(showTranslation) {
        translation = withContext(Dispatchers.IO) {
            if (!showTranslation) {
                emptyMap()
            } else {
                try {
                    val path = translationAssets["en-sahih"]
                    val raw = path?.let { reader.readText(it) }
                    if (raw == null) emptyMap() else parsePipeMap(raw)
                } catch (_: Exception) {
                    emptyMap()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onBack) {
                Text(stringResource(R.string.surahs))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = surah.localizedName(language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${surah.ayahCount} ${stringResource(R.string.ayat)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(
                onClick = {
                    hideMode = !hideMode
                    if (!hideMode) revealed = emptySet()
                },
            ) {
                Icon(
                    imageVector = if (hideMode) {
                        Icons.Default.VisibilityOff
                    } else {
                        Icons.Default.Visibility
                    },
                    contentDescription = null,
                )
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(readerEditions, key = { it.id }) { edition ->
                FilterChip(
                    selected = edition.id == editionId,
                    onClick = {
                        editionId = edition.id
                        ReaderPrefs.saveReaderDisplay(
                            context = context,
                            riwayaName = edition.riwayaName,
                            scriptName = edition.scriptName,
                        )
                    },
                    label = { Text(edition.label) },
                )
            }
            items(reciters) { reciter ->
                FilterChip(
                    selected = reciter.identifier == reciterId,
                    onClick = {
                        reciterId = reciter.identifier
                        ReaderPrefs.saveReciter(context, reciter.identifier)
                    },
                    label = { Text(reciter.localizedName(language)) },
                )
            }
            item {
                FilterChip(
                    selected = false,
                    onClick = { showFontSheet = true },
                    label = { Text(stringResource(R.string.quranFont)) },
                )
            }
            item {
                FilterChip(
                    selected = showTranslation,
                    onClick = {
                        showTranslation = !showTranslation
                        ReaderPrefs.saveShowTranslation(context, showTranslation)
                    },
                    label = { Text(stringResource(R.string.translation)) },
                )
            }
            item {
                FilterChip(
                    selected = false,
                    onClick = { showMushaf = true },
                    label = { Text(stringResource(R.string.mushaf)) },
                )
            }
        }

        if (ayahs.isEmpty()) {
            EmptyVerifiedContent()
        } else {
            if (showFontSheet) {
                FontSheet(
                    selectedFontName = fontName,
                    onSelected = { selected ->
                        fontName = selected
                        ReaderPrefs.saveReaderFont(context, selected)
                    },
                    onDismiss = { showFontSheet = false },
                )
            }
            selectedAyah?.let { ayah ->
                val reference = "${surah.localizedName(language)} ${ayah.surah}:${ayah.ayah}"
                AyahActionSheet(
                    ayah = ayah,
                    reference = reference,
                    translation = translation["${ayah.surah}:${ayah.ayah}"],
                    quranFontFamily = quranFontFamily,
                    reciter = reciterById(reciterId) ?: reciters.first(),
                    libraryStore = libraryStore,
                    onOpenTafsir = {
                        selectedAyah = null
                        tafsirAyah = ayah
                    },
                    onOpenCompare = {
                        selectedAyah = null
                        compareAyah = ayah
                    },
                    onOpenStudy = {
                        selectedAyah = null
                        studyAyah = ayah
                    },
                    onDismiss = { selectedAyah = null },
                )
            }
            tafsirAyah?.let { ayah ->
                val reference = "${surah.localizedName(language)} ${ayah.surah}:${ayah.ayah}"
                TafsirSheet(
                    reader = reader,
                    ayah = ayah,
                    reference = reference,
                    quranFontFamily = quranFontFamily,
                    initialTafsirId = prefs.tafsirId,
                    onDismiss = { tafsirAyah = null },
                )
            }
            compareAyah?.let { ayah ->
                val reference = "${surah.localizedName(language)} ${ayah.surah}:${ayah.ayah}"
                CompareRiwayatSheet(
                    reader = reader,
                    ayah = ayah,
                    reference = reference,
                    quranFontFamily = quranFontFamily,
                    onDismiss = { compareAyah = null },
                )
            }
            studyAyah?.let { ayah ->
                val reference = "${surah.localizedName(language)} ${ayah.surah}:${ayah.ayah}"
                WordStudySheet(
                    reader = reader,
                    ayah = ayah,
                    editionId = editionId,
                    reference = reference,
                    quranFontFamily = quranFontFamily,
                    onDismiss = { studyAyah = null },
                )
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
                state = ayahListState,
            ) {
                items(ayahs, key = { "${it.surah}:${it.ayah}" }) { ayah ->
                    val refKey = "${ayah.surah}:${ayah.ayah}"
                    val hidden = hideMode && refKey !in revealed
                    val isMemorized = refKey in memorized
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (hidden) {
                                    revealed = revealed + refKey
                                } else {
                                    ReaderPrefs.saveLastPosition(context, ayah.surah, ayah.ayah)
                                    selectedAyah = ayah
                                }
                            },
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End,
                        ) {
                            if (hidden) {
                                Text(
                                    text = "﴿$refKey﴾ • • •",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontFamily = quranFontFamily,
                                    textAlign = TextAlign.End,
                                )
                            } else {
                                Text(
                                    text = ayah.text,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontFamily = quranFontFamily,
                                    textAlign = TextAlign.End,
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                            Text(
                                text = "${ayahNumberText(ayah.surah, numberStyle)}:${ayahNumberText(ayah.ayah, numberStyle)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                                Spacer(Modifier.weight(1f))
                                IconButton(
                                    onClick = {
                                        memorized = MemorizationPrefs.toggle(
                                            context,
                                            ayah.surah,
                                            ayah.ayah,
                                        )
                                    },
                                ) {
                                    Icon(
                                        imageVector = if (isMemorized) {
                                            Icons.Default.CheckCircle
                                        } else {
                                            Icons.Default.RadioButtonUnchecked
                                        },
                                        contentDescription = null,
                                        tint = if (isMemorized) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }
                            }
                            translation["${ayah.surah}:${ayah.ayah}"]?.let { translated ->
                                Text(
                                    text = translated,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Start,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MushafReaderScreen(
    initialSurah: Int = 1,
    initialPage: Int = 1,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val language = currentLanguage()
    val prefs = remember(context) { ReaderPrefs.load(context) }
    val initialEditionId = remember(prefs) { prefs.readerEditionId() }
    val fontName = remember(prefs) { prefs.fontName }
    val numberStyle = remember(prefs) { prefs.ayahNumberStyleName }
    val reader = remember(context) { AndroidAssetReader(context) }
    val quranFontFamily = remember(context, fontName) {
        quranFontFamily(context, fontName)
    }
    val libraryStore = remember(context) { AndroidStores.library(context) }
    val mushafReader = remember(context) { AndroidStores.mushafMetadataReader(context) }

    val editionId = if (initialEditionId == "hafs-an-asim__uthmani") initialEditionId else "hafs-an-asim__uthmani"
    val hasVerifiedPageMap = editionId == "hafs-an-asim__uthmani"
    val safeInitialPage = initialPage.coerceIn(1, 604)

    val pagerState = rememberPagerState(
        initialPage = (safeInitialPage - 1).coerceIn(0, 603),
        pageCount = { 604 },
    )
    var currentPage by remember { mutableStateOf(safeInitialPage) }
    var mushafMetadata by remember { mutableStateOf<MushafMetadata?>(null) }
    // Full Hafs/Uthmani edition cached once on IO: per-page filtering is then
    // pure in-memory. Previously every page re-parsed the 1.3 MB file on Main.
    var mushafEditionAyahs by remember { mutableStateOf<List<Ayah>>(emptyList()) }
    var surahJumpDone by remember { mutableStateOf(false) }
    var showPageDialog by remember { mutableStateOf(false) }
    var pageInputText by remember { mutableStateOf("$safeInitialPage") }
    var selectedAyah by remember { mutableStateOf<Ayah?>(null) }
    var tafsirAyah by remember { mutableStateOf<Ayah?>(null) }
    var compareAyah by remember { mutableStateOf<Ayah?>(null) }
    var studyAyah by remember { mutableStateOf<Ayah?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(editionId) {
        mushafEditionAyahs = withContext(Dispatchers.IO) {
            try {
                loadQuranEdition(reader, editionId = editionId).orEmpty()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    LaunchedEffect(hasVerifiedPageMap) {
        if (!hasVerifiedPageMap) return@LaunchedEffect
        val meta = try {
            withContext(Dispatchers.IO) { mushafReader.load() }
        } catch (_: Exception) {
            // Keep loading indicator instead of stuck/crash; retry on re-entry.
            return@LaunchedEffect
        }
        mushafMetadata = meta
        if (initialSurah > 1 && !surahJumpDone) {
            val page = meta.pageOf(initialSurah, 1).coerceIn(1, 604)
            currentPage = page
            pagerState.animateScrollToPage(page - 1)
            surahJumpDone = true
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val page = pagerState.currentPage + 1
        if (page != currentPage) {
            currentPage = page
        }
    }

    LaunchedEffect(currentPage, mushafMetadata) {
        val meta = mushafMetadata
        if (meta == null) return@LaunchedEffect
        val startRef = meta.firstAyahOnPage(currentPage)
        if (startRef != null) {
            val surah = startRef.surah
            val ayah = startRef.ayah
            if (prefs.lastSurah != surah || prefs.lastAyah != ayah) {
                ReaderPrefs.saveLastPosition(context, surah, ayah)
                withContext(Dispatchers.IO) {
                    libraryStore.touchRecent(
                        RecentItem(
                            refKey = "$surah:$ayah",
                            title = surahMetadata.first { it.number == surah }.localizedName(language),
                            subtitle = "$surah:$ayah",
                            kind = BookmarkKind.AYAH,
                        ),
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onBack) {
                Text(stringResource(R.string.surahs))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "${stringResource(R.string.mushaf)} · ${stringResource(R.string.page)} $currentPage",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                val metaForHeader = mushafMetadata
                val headerRef = metaForHeader?.firstAyahOnPage(currentPage)
                if (headerRef != null) {
                    val juz = metaForHeader.juzOf(headerRef.surah, headerRef.ayah)
                    val hizb = metaForHeader.hizbOf(headerRef.surah, headerRef.ayah)
                    Text(
                        text = "${stringResource(R.string.juz)} $juz · ${stringResource(R.string.hizb)} $hizb",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (!hasVerifiedPageMap) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.mushafPageMapUnavailable),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            val meta = mushafMetadata
            Box(modifier = Modifier.weight(1f)) {
                if (meta == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(stringResource(R.string.downloadingContent))
                    }
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        pageSpacing = 0.dp,
                        reverseLayout = true,
                    ) { pageIndex ->
                        val page = pageIndex + 1
                        MushafPage(
                            page = page,
                            ayahs = remember(page, mushafEditionAyahs, meta) {
                                val startRef = meta.firstAyahOnPage(page)
                                val endRef = meta.lastAyahOnPage(page)
                                if (startRef == null || endRef == null) emptyList()
                                else {
                                    mushafEditionAyahs.filter { ayah ->
                                        val ref = AyahRef(ayah.surah, ayah.ayah)
                                        ref.compareTo(startRef) >= 0 && ref.compareTo(endRef) <= 0
                                    }
                                }
                            },
                            quranFontFamily = quranFontFamily,
                            onAyahClick = { selectedAyah = it },
                            mushafMetadata = meta,
                            numberStyle = numberStyle,
                        )
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 4.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = {
                                if (currentPage > 1) {
                                    scope.launch { pagerState.animateScrollToPage(currentPage - 2) }
                                }
                            },
                            enabled = currentPage > 1,
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = stringResource(R.string.previousPage),
                            )
                        }
                        val totalPages = mushafMetadata?.pageStarts?.size ?: 604
                        Text(
                            text = "${stringResource(R.string.page)} $currentPage ${stringResource(R.string.of)} $totalPages",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        IconButton(
                            onClick = {
                                showPageDialog = true
                                pageInputText = currentPage.toString()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.FindInPage,
                                contentDescription = stringResource(R.string.goToPage),
                            )
                        }
                    }
                }
            }
        }
    }

    selectedAyah?.let { ayah ->
        val reference = "${surahMetadata.first { it.number == ayah.surah }.localizedName(language)} ${ayah.surah}:${ayah.ayah}"
        AyahActionSheet(
            ayah = ayah,
            reference = reference,
            translation = null,
            quranFontFamily = quranFontFamily,
            reciter = reciterById(prefs.reciterId) ?: reciters.first(),
            libraryStore = libraryStore,
            onOpenTafsir = {
                selectedAyah = null
                tafsirAyah = ayah
            },
            onOpenCompare = {
                selectedAyah = null
                compareAyah = ayah
            },
            onOpenStudy = {
                selectedAyah = null
                studyAyah = ayah
            },
            onDismiss = { selectedAyah = null },
        )
    }
    tafsirAyah?.let { ayah ->
        val reference = "${surahMetadata.first { it.number == ayah.surah }.localizedName(language)} ${ayah.surah}:${ayah.ayah}"
        TafsirSheet(
            reader = reader,
            ayah = ayah,
            reference = reference,
            quranFontFamily = quranFontFamily,
            initialTafsirId = prefs.tafsirId,
            onDismiss = { tafsirAyah = null },
        )
    }
    compareAyah?.let { ayah ->
        val reference = "${surahMetadata.first { it.number == ayah.surah }.localizedName(language)} ${ayah.surah}:${ayah.ayah}"
        CompareRiwayatSheet(
            reader = reader,
            ayah = ayah,
            reference = reference,
            quranFontFamily = quranFontFamily,
            onDismiss = { compareAyah = null },
        )
    }
    studyAyah?.let { ayah ->
        val reference = "${surahMetadata.first { it.number == ayah.surah }.localizedName(language)} ${ayah.surah}:${ayah.ayah}"
        WordStudySheet(
            reader = reader,
            ayah = ayah,
            editionId = editionId,
            reference = reference,
            quranFontFamily = quranFontFamily,
            onDismiss = { studyAyah = null },
        )
    }

    if (showPageDialog) {
        val totalPages = mushafMetadata?.pageStarts?.size ?: 604
        AlertDialog(
            onDismissRequest = { showPageDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            title = { Text(stringResource(R.string.goToPage)) },
            text = {
                TextField(
                    value = pageInputText,
                    onValueChange = { pageInputText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val page = pageInputText.toIntOrNull()
                    if (page != null && page in 1..totalPages) {
                        scope.launch { pagerState.animateScrollToPage(page - 1) }
                    }
                    showPageDialog = false
                }) {
                    Text(stringResource(R.string.go))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MushafPage(
    page: Int,
    ayahs: List<Ayah>,
    quranFontFamily: FontFamily?,
    onAyahClick: (Ayah) -> Unit,
    mushafMetadata: MushafMetadata,
    numberStyle: String = "arabicIndic",
) {
    val language = currentLanguage()
    if (ayahs.isEmpty()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.contentUnavailable),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        }
        return
    }

    val firstAyah = ayahs.first()
    val juz = mushafMetadata.juzOf(firstAyah.surah, firstAyah.ayah)
    val hizb = mushafMetadata.hizbOf(firstAyah.surah, firstAyah.ayah)
    val rub = mushafMetadata.rubOf(firstAyah.surah, firstAyah.ayah)
    val hasSajda = ayahs.any { mushafMetadata.isSajda(it.surah, it.ayah) }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssistChip(
                    label = { Text("${stringResource(R.string.juz)} $juz") },
                    onClick = { },
                )
                Spacer(Modifier.width(8.dp))
                AssistChip(
                    label = { Text("${stringResource(R.string.hizb)} $hizb") },
                    onClick = { },
                )
                Spacer(Modifier.width(8.dp))
                AssistChip(
                    label = { Text("${stringResource(R.string.rub)} $rub") },
                    onClick = { },
                )
                if (hasSajda) {
                    Spacer(Modifier.width(8.dp))
                    AssistChip(
                        label = { Text("۩ ${stringResource(R.string.sajda)}") },
                        onClick = { },
                    )
                }
            }
            HorizontalDivider()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ayahs.forEach { ayah ->
                    val displayAyah = ayahNumberText(ayah.displayAyahNumber, numberStyle)
                    val ayahWithNumber = "${ayah.text} ﴿$displayAyah﴿ ${if (mushafMetadata.isSajda(ayah.surah, ayah.ayah)) "۩" else ""}"
                    Text(
                        text = ayahWithNumber,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = quranFontFamily,
                            textAlign = TextAlign.End,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onAyahClick(ayah) },
                                onLongClick = { onAyahClick(ayah) },
                            ),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AyahActionSheet(
    ayah: Ayah,
    reference: String,
    translation: String?,
    quranFontFamily: FontFamily?,
    reciter: Reciter,
    libraryStore: LibraryStore,
    onOpenTafsir: () -> Unit,
    onOpenCompare: () -> Unit,
    onOpenStudy: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audioPlayer = remember(context) {
        try {
            AndroidStores.audio(context)
        } catch (_: Exception) {
            null
        }
    }
    val refKey = "${ayah.surah}:${ayah.ayah}"
    val audioAlbum = stringResource(R.string.quranAudio)
    var isBookmarked by remember(refKey) { mutableStateOf(false) }
    LaunchedEffect(libraryStore, refKey) {
        isBookmarked = withContext(Dispatchers.IO) {
            libraryStore.isBookmarked(refKey)
        }
    }
    val shareText = buildString {
        appendLine(reference)
        appendLine()
        appendLine(ayah.text)
        if (!translation.isNullOrBlank()) {
            appendLine()
            append(translation)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = reference,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = ayah.text,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = quranFontFamily,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
            )
            translation?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val player = audioPlayer
                                ?: throw IllegalStateException("audio unavailable")
                            QuranAudioService.ensureStarted(context)
                            // Local-first: play the downloaded file when the
                            // surah is on disk, otherwise stream the verified
                            // CDN URL for the selected reciter.
                            val url = AudioFiles.localUrlOrNull(
                                context,
                                reciter.identifier,
                                ayah.surah,
                                ayah.ayah,
                            ) ?: reciter.fileUrl(
                                globalAyahNumber(ayah.surah, ayah.ayah),
                            )
                            val audioItem = AyahAudioItem(
                                url = url,
                                refKey = refKey,
                                title = reference,
                                artist = reciter.nameEn,
                                album = audioAlbum,
                            )
                            player.setAyahSources(listOf(audioItem))
                            player.setSpeed(AudioPrefs.speed(context).toFloat())
                            player.play()
                            onDismiss()
                        } catch (_: Exception) {
                            android.widget.Toast.makeText(
                                context,
                                context.getString(R.string.downloadFailed),
                                android.widget.Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.play))
            }
            Button(
                onClick = {
                    scope.launch {
                        isBookmarked = withContext(Dispatchers.IO) {
                            libraryStore.toggleAyah(ayah.surah, ayah.ayah)
                            libraryStore.isBookmarked(refKey)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (isBookmarked) {
                        stringResource(R.string.bookmarks)
                    } else {
                        stringResource(R.string.bookmark)
                    },
                )
            }
            Button(
                onClick = onOpenTafsir,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.tafsir))
            }
            Button(
                onClick = onOpenCompare,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.compareRiwayat))
            }
            Button(
                onClick = onOpenStudy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.wordMeanings))
            }
            Button(
                onClick = {
                    AndroidClipboard(context).copy(shareText)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.copy))
            }
            Button(
                onClick = {
                    AndroidSharer(context).shareText(shareText, reference)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.share))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontSheet(
    selectedFontName: String,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.quranFont),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            readerFontOptions.forEach { option ->
                FilterChip(
                    selected = selectedFontName == option.id,
                    onClick = { onSelected(option.id) },
                    label = { Text(option.label) },
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TafsirSheet(
    reader: AndroidAssetReader,
    ayah: Ayah,
    reference: String,
    quranFontFamily: FontFamily?,
    initialTafsirId: String = "jalalayn",
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var selectedTafsir by rememberSaveable(ayah.surah, ayah.ayah) {
        mutableStateOf(
            tafsirOptions.firstOrNull { it.id == initialTafsirId }?.id
                ?: tafsirOptions.first().id,
        )
    }
    // JSON parse off Main: per-surah tafsir read blocked sheet open.
    var tafsirEntries by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    LaunchedEffect(reader, selectedTafsir, ayah.surah) {
        tafsirEntries = withContext(Dispatchers.IO) {
            try {
                loadTafsirSurah(
                    reader = reader,
                    tafsirId = selectedTafsir,
                    surah = ayah.surah,
                ).orEmpty()
            } catch (_: Exception) {
                emptyMap()
            }
        }
    }
    val tafsirText = tafsirEntries[ayah.ayah]

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = reference,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tafsirOptions) { option ->
                    FilterChip(
                        selected = selectedTafsir == option.id,
                        onClick = {
                            selectedTafsir = option.id
                            ReaderPrefs.saveTafsir(context, option.id)
                        },
                        label = { Text(stringResource(option.labelRes)) },
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    if (tafsirText.isNullOrBlank()) {
                        Text(
                            text = stringResource(R.string.contentUnavailable),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = tafsirText,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = quranFontFamily,
                            textAlign = TextAlign.End,
                        )
                    }
                }
                item {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompareRiwayatSheet(
    reader: AndroidAssetReader,
    ayah: Ayah,
    reference: String,
    quranFontFamily: FontFamily?,
    onDismiss: () -> Unit,
) {
    // 5 × 1.3 MB parse off Main: previously froze sheet open for seconds.
    var rows by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    LaunchedEffect(reader, ayah.surah, ayah.ayah) {
        rows = withContext(Dispatchers.IO) {
            try {
                readerEditions.mapNotNull { edition ->
                    val text = loadQuranEdition(reader, edition.id)
                        ?.firstOrNull { it.surah == ayah.surah && it.ayah == ayah.ayah }
                        ?.text
                        ?: return@mapNotNull null
                    edition.label to text
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.compareRiwayat),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = reference,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (rows.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.contentUnavailable),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(rows, key = { it.first }) { (label, text) ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = text,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontFamily = quranFontFamily,
                                    textAlign = TextAlign.End,
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordStudySheet(
    reader: AndroidAssetReader,
    ayah: Ayah,
    editionId: String,
    reference: String,
    quranFontFamily: FontFamily?,
    onDismiss: () -> Unit,
) {
    // Word breakdown exists only for the Hafs dataset; other editions get
    // an honest unavailable state instead of a mismatched mapping.
    val hafsOnly = editionId.startsWith("hafs-an-asim")
    var words by remember { mutableStateOf<List<com.godfather59.quransunnah.data.WordInfo>>(emptyList()) }
    LaunchedEffect(reader, ayah.surah, ayah.ayah, hafsOnly) {
        words = withContext(Dispatchers.IO) {
            if (!hafsOnly) {
                emptyList()
            } else {
                try {
                    loadWordsSurah(reader, ayah.surah)
                        ?.firstOrNull { it.ayah == ayah.ayah }
                        ?.words
                        .orEmpty()
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.wordMeanings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = reference,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.wordMorphologyHint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (words.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.datasetUnavailable),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(words, key = { it.word + it.lemma + it.pos }) { word ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = word.word,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontFamily = quranFontFamily,
                                    textAlign = TextAlign.End,
                                )
                                val details = listOf(
                                    word.lemma.takeIf { it.isNotBlank() },
                                    word.root.takeIf { it.isNotBlank() }?.let { "√$it" },
                                    word.pos.takeIf { it.isNotBlank() },
                                ).filterNotNull()
                                if (details.isNotEmpty()) {
                                    Text(
                                        text = details.joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun NumberGrid(
    range: IntRange,
    label: String,
    onSelected: (Int) -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val step = when (range.last) {
                30 -> 5
                60 -> 10
                240 -> 40
                else -> 100
            }
            items(range.step(step).toList()) { start ->
                AssistChip(
                    onClick = {},
                    label = { Text("$start-${minOf(start + step - 1, range.last)}") },
                )
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(72.dp),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(range.toList()) { number ->
                Card(
                    modifier = Modifier.clickable { onSelected(number) },
                ) {
                    Text(
                        text = "$label $number",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(titleRes: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(4.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun StatCard(
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
private fun BookmarkRow(
    item: Bookmark,
    onClick: () -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = item.refKey,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecentRow(
    item: RecentItem,
    onClick: () -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyVerifiedContent() {
    Card(Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.contentUnavailable),
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun currentLanguage(): String =
    LocalConfiguration.current.locales[0].language

private class QueueDownloadControl(
    private val queue: DownloadQueue,
) : DownloadControl {
    override fun isCancelled(key: String): Boolean = queue.isCancelled(key)
    override fun isPaused(key: String): Boolean = queue.isPaused(key)

    override suspend fun waitWhilePaused(key: String) {
        while (queue.isPaused(key) && !queue.isCancelled(key)) {
            delay(200)
        }
        if (queue.isCancelled(key)) throw DownloadCancelledException(key)
    }
}

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

// App-scoped downloads: composition scope is cancelled on tab leave /
// rotation and would kill mid-download. This singleton survives UI.
private object AppDownloadScope {
    val scope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + Dispatchers.IO,
    )
}

@Composable
fun DownloadsScreen() {
    val context = LocalContext.current
    val language = currentLanguage()
    val audioDownloader = remember(context) { AndroidStores.audioDownloader(context) }
    val queue = remember { DownloadQueue() }
    val control = remember(queue) { QueueDownloadControl(queue) }
    var selectedReciterId by rememberSaveable { mutableStateOf("ar.alafasy") }
    var activeKey by remember { mutableStateOf<String?>(null) }
    var queuedKeys by remember { mutableStateOf(emptySet<String>()) }
    var pausedKeys by remember { mutableStateOf(emptySet<String>()) }
    var failedKeys by remember { mutableStateOf(emptySet<String>()) }
    var doneAyahs by remember { mutableStateOf(emptyMap<String, Int>()) }
    var completedKeys by remember { mutableStateOf(emptySet<String>()) }
    var completedBytes by remember { mutableStateOf(emptyMap<String, Long>()) }
    var pumpRunning by remember { mutableStateOf(false) }
    // Atomic guard: check-then-set on plain Boolean races when pump() is
    // re-entered from the IO tail-rescan below.
    val pumpGuard = remember { java.util.concurrent.atomic.AtomicBoolean(false) }
    var scanToken by rememberSaveable { mutableIntStateOf(0) }
    @Suppress("UNUSED_VARIABLE")
    val scope = rememberCoroutineScope()

    // Runs on IO (pump launches on Dispatchers.IO): file probes + network
    // must never run on Main. Compose snapshot writes from here are safe.
    suspend fun downloadSurah(reciterId: String, surah: Int) {
        val key = "$reciterId:$surah"
        val count = surahMetadata.first { it.number == surah }.ayahCount
        val reciter = reciterById(reciterId) ?: return
        for (ayah in 1..count) {
            control.waitWhilePaused(key)
            if (control.isCancelled(key)) throw DownloadCancelledException(key)
            val dest = AudioFiles.ayahFile(context, reciterId, surah, ayah)
            if (dest.exists()) {
                doneAyahs = doneAyahs + (key to ((doneAyahs[key] ?: 0) + 1))
                continue
            }
            val part = File(dest.absolutePath + ".part")
            val resume = try {
                if (part.exists()) part.length() else 0L
            } catch (_: Exception) {
                0L
            }
            audioDownloader.downloadAyah(
                key,
                AyahDownload(
                    url = reciter.fileUrl(globalAyahNumber(surah, ayah)),
                    destPath = dest.absolutePath,
                    resumeFromBytes = resume,
                ),
                control,
            ) { _, _ -> }
            doneAyahs = doneAyahs + (key to ((doneAyahs[key] ?: 0) + 1))
        }
    }

    fun pump() {
        if (!pumpGuard.compareAndSet(false, true)) return
        pumpRunning = true
        AppDownloadScope.scope.launch {
            try {
                while (true) {
                    val item = synchronized(queue) { queue.next() } ?: break
                    val key = "${item.reciterId}:${item.surah}"
                    queuedKeys = queuedKeys - key
                    activeKey = key
                    failedKeys = failedKeys - key
                    try {
                        downloadSurah(item.reciterId, item.surah)
                    } catch (_: DownloadCancelledException) {
                        // Partial files stay for resume; nothing to mark.
                    } catch (_: Exception) {
                        failedKeys = failedKeys + key
                    } finally {
                        activeKey = null
                    }
                }
                val fresh = withContext(Dispatchers.IO) {
                    val done = mutableSetOf<String>()
                    val sizes = mutableMapOf<String, Long>()
                    for (m in surahMetadata) {
                        if (AudioFiles.isSurahDownloaded(context, selectedReciterId, m.number)) {
                            val k = "$selectedReciterId:${m.number}"
                            done.add(k)
                            sizes[k] = AudioFiles.surahBytes(context, selectedReciterId, m.number)
                        }
                    }
                    done to sizes
                }
                completedKeys = fresh.first
                completedBytes = fresh.second
            } finally {
                pumpGuard.set(false)
                pumpRunning = false
                // Items enqueued during the rescan above would otherwise stall:
                // their pump() call saw the guard set and returned.
                if (queue.pendingCount() > 0) pump()
            }
        }
    }

    LaunchedEffect(selectedReciterId, scanToken) {
        val fresh = withContext(Dispatchers.IO) {
            val done = mutableSetOf<String>()
            val sizes = mutableMapOf<String, Long>()
            for (m in surahMetadata) {
                if (AudioFiles.isSurahDownloaded(context, selectedReciterId, m.number)) {
                    val k = "$selectedReciterId:${m.number}"
                    done.add(k)
                    sizes[k] = AudioFiles.surahBytes(context, selectedReciterId, m.number)
                }
            }
            done to sizes
        }
        completedKeys = fresh.first
        completedBytes = fresh.second
    }

    val surahMetadata = remember { surahMetadata }
    val reciters = remember { reciters }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.downloadManager),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
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
                    queue.enqueue(QueuedSurah(selectedReciterId, surahNumber, 0L))
                    queuedKeys = queuedKeys + key
                    failedKeys = failedKeys - key
                    pump()
                },
                onDelete = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            AudioFiles.deleteSurah(context, selectedReciterId, surahNumber)
                        }
                        scanToken += 1
                    }
                },
                onPauseToggle = {
                    if (key in pausedKeys) {
                        queue.resume(key)
                        pausedKeys = pausedKeys - key
                    } else {
                        queue.pause(key)
                        pausedKeys = pausedKeys + key
                    }
                },
                onCancel = {
                    queue.cancel(key)
                    queuedKeys = queuedKeys - key
                    pausedKeys = pausedKeys - key
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
                LinearProgressIndicator(
                    progress = {
                        (state.doneAyahs.toFloat() / state.totalAyahs).coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SunnahHomeScreen() {
    val context = LocalContext.current
    val language = currentLanguage()
    val reader = remember(context) { AndroidAssetReader(context) }
    val libraryStore = remember(context) { AndroidStores.library(context) }
    val engine = remember { AndroidStores.searchEngine() }
    val scope = rememberCoroutineScope()
    var collectionId by rememberSaveable { mutableStateOf("bukhari") }
    var selectedSection by remember(collectionId) { mutableStateOf<HadithSection?>(null) }
    var selectedHadith by remember { mutableStateOf<Hadith?>(null) }
    var numberInput by rememberSaveable { mutableStateOf("") }
    var numberError by remember { mutableStateOf(false) }
    var showFilter by remember { mutableStateOf(false) }

    val openHadith: (Hadith, String) -> Unit = { hadith, subtitle ->
        selectedHadith = hadith
        scope.launch {
            withContext(Dispatchers.IO) {
                libraryStore.touchRecent(
                    RecentItem(
                        refKey = hadith.id,
                        title = "${hadithCollections.firstOrNull { it.id == hadith.collectionId }?.localizedName(language) ?: hadith.collectionId} ${hadith.hadithNumber}",
                        subtitle = subtitle,
                        kind = BookmarkKind.HADITH,
                    ),
                )
            }
        }
    }

    val collection = remember(collectionId) {
        hadithCollections.firstOrNull { it.id == collectionId }
            ?: hadithCollections.first()
    }
    // index.json + section JSON parse off Main: Bukhari-scale files blocked tab switch.
    var index by remember { mutableStateOf<com.godfather59.quransunnah.data.HadithIndex?>(null) }
    var indexLoading by remember { mutableStateOf(true) }
    LaunchedEffect(reader, collectionId) {
        indexLoading = true
        index = withContext(Dispatchers.IO) {
            try {
                reader.readText("assets/hadith/$collectionId/index.json")
                    ?.let { parseHadithIndex(it) }
            } catch (_: Exception) {
                null
            }
        }
        indexLoading = false
    }
    val books = remember(index) {
        index?.sections?.filter { it.count > 0 }.orEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.sunnah),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = { showFilter = true }) {
                Text(stringResource(R.string.filters))
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(hadithCollections, key = { it.id }) { item ->
                FilterChip(
                    selected = item.id == collectionId,
                    onClick = {
                        collectionId = item.id
                        selectedSection = null
                        selectedHadith = null
                        numberError = false
                    },
                    label = { Text(item.localizedName(language)) },
                )
            }
        }

        if (indexLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (index == null) {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = collection.localizedName(language),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.datasetUnavailable),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else if (selectedSection == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = numberInput,
                    onValueChange = {
                        numberInput = it
                        numberError = false
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text(stringResource(R.string.hadithNumber)) },
                    isError = numberError,
                )
                var jumping by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        val number = numberInput.trim().toIntOrNull()
                        val section = if (number == null) {
                            null
                        } else {
                            books.firstOrNull { number in it.first..it.last }
                        }
                        if (section == null) {
                            numberError = true
                            return@Button
                        }
                        // Section JSON parse off Main.
                        jumping = true
                        scope.launch {
                            val found = withContext(Dispatchers.IO) {
                                try {
                                    reader
                                        .readText("assets/hadith/$collectionId/sections/${section.section}.json")
                                        ?.let { parseHadithSection(collectionId, section.title, it) }
                                        .orEmpty()
                                        .firstOrNull { it.hadithNumber == number.toString() }
                                } catch (_: Exception) {
                                    null
                                }
                            }
                            jumping = false
                            if (found == null) {
                                numberError = true
                                return@launch
                            }
                            numberError = false
                            selectedHadith = found
                            withContext(Dispatchers.IO) {
                                libraryStore.touchRecent(
                                    RecentItem(
                                        refKey = found.id,
                                        title = "${collection.localizedName(language)} ${found.hadithNumber}",
                                        subtitle = section.title,
                                        kind = BookmarkKind.HADITH,
                                    ),
                                )
                            }
                        }
                    },
                    enabled = !jumping,
                ) {
                    Text(stringResource(R.string.go))
                }
            }
            if (numberError) {
                Text(
                    text = stringResource(R.string.noHadithMatches),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(books, key = { it.section }) { book ->
                    HadithBookRow(
                        section = book,
                        onClick = { selectedSection = book },
                    )
                }
            }
        } else {
            val section = selectedSection
            if (section == null) {
                EmptyVerifiedContent()
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(onClick = { selectedSection = null }) {
                        Text(stringResource(R.string.booksChapters))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = section.title.ifBlank { "${stringResource(R.string.book)} ${section.section}" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                    )
                }
                var hadiths by remember { mutableStateOf<List<Hadith>>(emptyList()) }
                var hadithsLoading by remember { mutableStateOf(true) }
                LaunchedEffect(reader, collectionId, section) {
                    hadithsLoading = true
                    hadiths = withContext(Dispatchers.IO) {
                        try {
                            reader
                                .readText("assets/hadith/$collectionId/sections/${section.section}.json")
                                ?.let { parseHadithSection(collectionId, section.title, it) }
                                .orEmpty()
                        } catch (_: Exception) {
                            emptyList()
                        }
                    }
                    hadithsLoading = false
                }
                if (hadithsLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (hadiths.isEmpty()) {
                    EmptyVerifiedContent()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(hadiths, key = { it.id }) { hadith ->
                            HadithRow(
                                hadith = hadith,
                                onClick = {
                                    selectedHadith = hadith
                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            libraryStore.touchRecent(
                                                RecentItem(
                                                    refKey = hadith.id,
                                                    title = "${collection.localizedName(language)} ${hadith.hadithNumber}",
                                                    subtitle = section.title,
                                                    kind = BookmarkKind.HADITH,
                                                ),
                                            )
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    selectedHadith?.let { hadith ->
        HadithReaderSheet(
            hadith = hadith,
            collection = collection,
            libraryStore = libraryStore,
            onDismiss = { selectedHadith = null },
        )
    }

    if (showFilter) {
        HadithFilterSheet(
            collectionId = collectionId,
            books = books,
            onOpenHadith = { openHadith(it, it.book) },
            onDismiss = { showFilter = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HadithFilterSheet(
    collectionId: String,
    books: List<HadithSection>,
    onOpenHadith: (Hadith) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val reader = remember(context) { AndroidAssetReader(context) }
    val engine = remember { AndroidStores.searchEngine() }
    val scope = rememberCoroutineScope()
    var query by rememberSaveable(collectionId) { mutableStateOf("") }
    var bookQuery by rememberSaveable(collectionId) { mutableStateOf("") }
    var numberQuery by rememberSaveable(collectionId) { mutableStateOf("") }
    var gradeFilter by rememberSaveable(collectionId) { mutableStateOf(emptySet<String>()) }
    var indexReady by remember { mutableStateOf(engine.documentCount() > 0) }
    var hits by remember { mutableStateOf(emptyList<Hadith>()) }
    var searched by remember { mutableStateOf(false) }
    val gradeOptions = remember { listOf("Sahih", "Hasan", "Daif") }

    LaunchedEffect(reader) {
        ensureSearchIndex(reader, engine)
        indexReady = true
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.filters),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.searchHint)) },
                enabled = indexReady,
            )
            TextField(
                value = bookQuery,
                onValueChange = { bookQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.book)) },
            )
            TextField(
                value = numberQuery,
                onValueChange = { numberQuery = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.hadithNumber)) },
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(gradeOptions, key = { it }) { grade ->
                    FilterChip(
                        selected = grade in gradeFilter,
                        onClick = {
                            gradeFilter = if (grade in gradeFilter) {
                                gradeFilter - grade
                            } else {
                                gradeFilter + grade
                            }
                        },
                        label = { Text(grade) },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        query = ""
                        bookQuery = ""
                        numberQuery = ""
                        gradeFilter = emptySet()
                        hits = emptyList()
                        searched = false
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.deselectAll))
                }
                Button(
                    onClick = {
                        scope.launch {
                            val found = withContext(Dispatchers.Default) {
                                engine.search(
                                    query,
                                    SearchOptions(
                                        collectionIds = setOf(collectionId),
                                        limitPerCategory = 50,
                                    ),
                                ).hadith
                                    .mapNotNull { it.hadith }
                                    .filter { h ->
                                        (bookQuery.isBlank() ||
                                            h.book.contains(bookQuery.trim(), ignoreCase = true)) &&
                                            (numberQuery.isBlank() ||
                                                h.hadithNumber == numberQuery.trim()) &&
                                            (gradeFilter.isEmpty() ||
                                                gradeFilter.any { g ->
                                                    h.grade?.contains(g, ignoreCase = true) == true
                                                })
                                    }
                            }
                            hits = found
                            searched = true
                        }
                    },
                    enabled = indexReady && query.isNotBlank(),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.applyFilters))
                }
            }
            if (searched) {
                if (hits.isEmpty()) {
                    Text(
                        text = stringResource(R.string.noHadithMatches),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(hits, key = { it.id }) { hadith ->
                            HadithRow(
                                hadith = hadith,
                                onClick = { onOpenHadith(hadith) },
                            )
                        }
                    }
                }
            } else if (books.isNotEmpty()) {
                Text(
                    text = "${books.size} ${stringResource(R.string.booksChapters)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HadithBookRow(
    section: HadithSection,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = section.section.toString(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = section.title.ifBlank { "${section.first}–${section.last}" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                )
                Text(
                    text = "${section.first}–${section.last} · ${section.count} ${stringResource(R.string.hadithCountUnit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HadithRow(
    hadith: Hadith,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "№ ${hadith.hadithNumber}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            if (hadith.matnAr.isBlank()) {
                Text(
                    text = stringResource(R.string.arabicMatnPlaceholderHint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                )
            } else {
                Text(
                    text = hadith.matnAr,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HadithReaderSheet(
    hadith: Hadith,
    collection: HadithCollection,
    libraryStore: LibraryStore,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val language = currentLanguage()
    val reference = "${collection.localizedName(language)} · № ${hadith.hadithNumber}"
    var isBookmarked by remember(hadith.id) { mutableStateOf(false) }
    LaunchedEffect(libraryStore, hadith.id) {
        isBookmarked = withContext(Dispatchers.IO) {
            libraryStore.isBookmarked(hadith.id)
        }
    }
    val shareText = buildString {
        appendLine(reference)
        if (hadith.book.isNotBlank()) appendLine(hadith.book)
        appendLine()
        append(hadith.matnAr)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    text = reference,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (hadith.book.isNotBlank()) {
                item {
                    Text(
                        text = hadith.book,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                if (hadith.matnAr.isBlank()) {
                    Text(
                        text = stringResource(R.string.arabicMatnPlaceholderHint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = hadith.matnAr,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            item {
                if (hadith.grade.isNullOrBlank()) {
                    Text(
                        text = "${stringResource(R.string.gradeLabel)}: ${stringResource(R.string.gradeUnavailable)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    val authority = hadith.gradingAuthority?.takeIf { it.isNotBlank() }
                    Text(
                        text = if (authority == null) {
                            "${stringResource(R.string.gradeLabel)}: ${hadith.grade}"
                        } else {
                            "${stringResource(R.string.gradeLabel)}: ${hadith.grade} · $authority"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                Text(
                    text = "${stringResource(R.string.source)}: ${collection.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Button(
                    onClick = {
                        scope.launch {
                            isBookmarked = withContext(Dispatchers.IO) {
                                libraryStore.toggleHadith(
                                    hadith.id,
                                    "${collection.localizedName(language)} ${hadith.hadithNumber}",
                                )
                                libraryStore.isBookmarked(hadith.id)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        if (isBookmarked) {
                            stringResource(R.string.bookmarks)
                        } else {
                            stringResource(R.string.bookmark)
                        },
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        AndroidClipboard(context).copy(shareText)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.copy))
                }
            }
            item {
                Button(
                    onClick = {
                        AndroidSharer(context).shareText(shareText, reference)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.share))
                }
            }
            item {
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

private fun HadithCollection.localizedName(language: String): String =
    when (language) {
        "ar" -> nameAr
        "fr" -> nameFr
        else -> nameEn
    }

@Composable
fun LibraryScreen(
    onOpenAyahRef: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val libraryStore = remember(context) { AndroidStores.library(context) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var query by rememberSaveable { mutableStateOf("") }
    var refreshToken by rememberSaveable { mutableIntStateOf(0) }
    var bookmarks by remember { mutableStateOf(emptyList<Bookmark>()) }
    var notes by remember { mutableStateOf(emptyList<UserNote>()) }
    var highlights by remember { mutableStateOf(emptyList<Highlight>()) }
    var collections by remember { mutableStateOf(emptyList<CustomCollection>()) }
    var expandedCollections by remember { mutableStateOf(emptySet<String>()) }
    var collectionDialog by remember { mutableStateOf<CustomCollection?>(null) }
    var showNewCollectionDialog by remember { mutableStateOf(false) }
    var noteDialog by remember { mutableStateOf<UserNote?>(null) }
    var backupStatus by remember { mutableStateOf<String?>(null) }
    val deleteLabel = stringResource(R.string.delete)
    val cancelLabel = stringResource(R.string.cancel)

    LaunchedEffect(libraryStore, refreshToken) {
        val loaded = withContext(Dispatchers.IO) {
            libraryStore.ensureDefaults()
            val b = libraryStore.bookmarks()
            val n = libraryStore.notes()
            val h = libraryStore.highlights()
            val c = libraryStore.collections()
            listOf(b, n, h, c)
        }
        @Suppress("UNCHECKED_CAST")
        bookmarks = loaded[0] as List<Bookmark>
        @Suppress("UNCHECKED_CAST")
        notes = loaded[1] as List<UserNote>
        @Suppress("UNCHECKED_CAST")
        highlights = loaded[2] as List<Highlight>
        @Suppress("UNCHECKED_CAST")
        collections = loaded[3] as List<CustomCollection>
    }

    fun matches(vararg fields: String?): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return true
        val normalized = normalizeArabic(q).lowercase()
        return fields.filterNotNull().any { value ->
            normalizeArabic(value).lowercase().contains(normalized) ||
                value.lowercase().contains(q.lowercase())
        }
    }

    fun collectionName(id: String): String =
        collections.firstOrNull { it.id == id }?.name ?: id

    fun offerUndo(deletedKind: String, restore: suspend () -> Unit) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(
                message = deletedKind,
                actionLabel = cancelLabel,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                withContext(Dispatchers.IO) { restore() }
                refreshToken += 1
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bytes = withContext(Dispatchers.IO) {
                AndroidBackupPicker.readPickedBytes(context.contentResolver, uri)
            }
            if (bytes == null) {
                backupStatus = context.getString(R.string.invalidBackup)
                return@launch
            }
            try {
                withContext(Dispatchers.IO) {
                    libraryStore.restoreBackup(bytes.decodeToString())
                }
                refreshToken += 1
                backupStatus = context.getString(R.string.backupRestored)
            } catch (_: Exception) {
                backupStatus = context.getString(R.string.invalidBackup)
            }
        }
    }

    val filteredBookmarks = remember(bookmarks, collections, query) {
        bookmarks.filter { b ->
            matches(
                b.refKey,
                b.title,
                b.subtitle,
                b.collectionId?.let(::collectionName),
            )
        }
    }
    val filteredNotes = remember(notes, query) {
        notes.filter { matches(it.refKey, it.text) }
    }
    val filteredHighlights = remember(highlights, query) {
        highlights.filter { matches(it.refKey) }
    }
    val filteredCollections = remember(collections, bookmarks, query) {
        collections.filter { c ->
            if (matches(c.name)) return@filter true
            bookmarks
                .filter { it.collectionId == c.id }
                .any { matches(it.refKey, it.title, it.subtitle) }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.library),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.importBackup))
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                val json = withContext(Dispatchers.IO) {
                                    libraryStore.backup()
                                }
                                val file = withContext(Dispatchers.IO) {
                                    val out = File(
                                        context.cacheDir,
                                        "quran-sunnah-library-backup.json",
                                    )
                                    out.writeText(json)
                                    out
                                }
                                AndroidSharer(context).shareFile(
                                    file.absolutePath,
                                    "application/json",
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.exportBackup))
                    }
                }
            }
            backupStatus?.let { status ->
                item {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.searchLibrary)) },
                )
            }

            item { SectionTitle(stringResource(R.string.bookmarks)) }
            if (filteredBookmarks.isEmpty()) {
                item { EmptyVerifiedContent() }
            } else {
                items(filteredBookmarks, key = { it.id }) { bookmark ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        enabled = bookmark.kind == BookmarkKind.AYAH,
                                    ) {
                                        if (bookmark.kind == BookmarkKind.AYAH) {
                                            onOpenAyahRef(bookmark.refKey)
                                        }
                                    },
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = bookmark.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                val bookmarkCollectionId = bookmark.collectionId
                                val subtitle = buildString {
                                    append(bookmark.subtitle)
                                    if (bookmarkCollectionId != null) {
                                        append(" · ${collectionName(bookmarkCollectionId)}")
                                    }
                                }
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            libraryStore.removeBookmark(bookmark.id)
                                        }
                                        refreshToken += 1
                                        offerUndo(bookmark.title) {
                                            if (bookmark.kind == BookmarkKind.AYAH) {
                                                val parts = bookmark.refKey.split(":")
                                                val su = parts.getOrNull(0)?.toIntOrNull()
                                                val ay = parts.getOrNull(1)?.toIntOrNull()
                                                if (su != null && ay != null) {
                                                    libraryStore.toggleAyah(su, ay)
                                                    if (bookmark.collectionId != null) {
                                                        libraryStore.setAyahCollection(
                                                            su,
                                                            ay,
                                                            bookmark.collectionId,
                                                        )
                                                    }
                                                }
                                            } else if (bookmark.kind == BookmarkKind.HADITH) {
                                                libraryStore.toggleHadith(
                                                    bookmark.refKey,
                                                    bookmark.title,
                                                )
                                            }
                                        }
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = deleteLabel,
                                )
                            }
                        }
                    }
                }
            }

            item { SectionTitle(stringResource(R.string.customCollections)) }
            items(filteredCollections, key = { it.id }) { collection ->
                val members = bookmarks.filter { it.collectionId == collection.id }
                val expanded = collection.id in expandedCollections
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedCollections = if (expanded) {
                                        expandedCollections - collection.id
                                    } else {
                                        expandedCollections + collection.id
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = collection.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = "${members.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(
                                onClick = { collectionDialog = collection },
                            ) {
                                Text(stringResource(R.string.rename))
                            }
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            libraryStore.removeCollection(collection.id)
                                        }
                                        refreshToken += 1
                                        offerUndo(collection.name) {
                                            libraryStore.addCollection(collection.name)
                                        }
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = deleteLabel,
                                )
                            }
                        }
                        if (expanded) {
                            members.forEach { member ->
                                Text(
                                    text = "${member.title} · ${member.refKey}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                            }
                        }
                    }
                }
            }
            item {
                Button(onClick = { showNewCollectionDialog = true }) {
                    Text(stringResource(R.string.newCollection))
                }
            }

            item { SectionTitle(stringResource(R.string.noteLabel)) }
            if (filteredNotes.isEmpty()) {
                item { EmptyVerifiedContent() }
            } else {
                items(filteredNotes, key = { it.id }) { note ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = note.refKey,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = note.text,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(onClick = { noteDialog = note }) {
                                    Text(stringResource(R.string.notes))
                                }
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            withContext(Dispatchers.IO) {
                                                libraryStore.removeNote(note.id)
                                            }
                                            refreshToken += 1
                                            offerUndo(note.refKey) {
                                                libraryStore.upsertNote(note.refKey, note.text)
                                            }
                                        }
                                    },
                                ) {
                                    Text(stringResource(R.string.delete))
                                }
                            }
                        }
                    }
                }
            }

            item { SectionTitle(stringResource(R.string.highlight)) }
            if (filteredHighlights.isEmpty()) {
                item { EmptyVerifiedContent() }
            } else {
                items(filteredHighlights, key = { it.id }) { highlight ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = highlight.refKey,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            libraryStore.removeHighlight(highlight.id)
                                        }
                                        refreshToken += 1
                                        offerUndo(highlight.refKey) {
                                            libraryStore.toggleHighlight(
                                                highlight.refKey,
                                                highlight.colorValue,
                                            )
                                        }
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = deleteLabel,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewCollectionDialog || collectionDialog != null) {
        val editing = collectionDialog
        var name by remember(editing) { mutableStateOf(editing?.name.orEmpty()) }
        AlertDialog(
            onDismissRequest = {
                showNewCollectionDialog = false
                collectionDialog = null
            },
            title = {
                Text(
                    if (editing == null) {
                        stringResource(R.string.newCollection)
                    } else {
                        stringResource(R.string.customCollections)
                    },
                )
            },
            text = {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                if (editing == null) {
                                    libraryStore.addCollection(name)
                                } else {
                                    libraryStore.renameCollection(editing.id, name)
                                }
                            }
                            showNewCollectionDialog = false
                            collectionDialog = null
                            refreshToken += 1
                        }
                    },
                ) {
                    Text(
                        if (editing == null) {
                            stringResource(R.string.newCollection)
                        } else {
                            stringResource(R.string.rename)
                        },
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNewCollectionDialog = false
                        collectionDialog = null
                    },
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    noteDialog?.let { note ->
        var text by remember(note) { mutableStateOf(note.text) }
        AlertDialog(
            onDismissRequest = { noteDialog = null },
            title = { Text("${stringResource(R.string.notes)} · ${note.refKey}") },
            text = {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                libraryStore.upsertNote(note.refKey, text)
                            }
                            noteDialog = null
                            refreshToken += 1
                        }
                    },
                ) {
                    Text(stringResource(R.string.notes))
                }
            },
            dismissButton = {
                TextButton(onClick = { noteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
fun SettingsScreen(
    onThemeChange: (String, Boolean) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val language = currentLanguage()
    val reader = remember(context) { AndroidAssetReader(context) }
    val prefs = remember(context) { ReaderPrefs.load(context) }
    val themePrefs = remember(context) { ThemePrefs.load(context) }
    var riwayaName by rememberSaveable { mutableStateOf(prefs.riwayaName) }
    var scriptName by rememberSaveable { mutableStateOf(prefs.scriptName) }
    var fontName by rememberSaveable { mutableStateOf(prefs.fontName) }
    var showTranslation by rememberSaveable { mutableStateOf(prefs.showTranslation) }
    var reciterId by rememberSaveable { mutableStateOf(prefs.reciterId) }
    var themeName by rememberSaveable { mutableStateOf(themePrefs.themeName) }
    var dynamicColor by rememberSaveable { mutableStateOf(themePrefs.dynamicColor) }
    var speed by rememberSaveable { mutableStateOf(AudioPrefs.speed(context)) }
    var locale by rememberSaveable { mutableStateOf(OnboardingPrefs.locale(context) ?: language) }
    var showLicenses by remember { mutableStateOf(false) }
    val quranFontFamily = remember(context, fontName) {
        quranFontFamily(context, fontName)
    }
    var sampleAyah by remember { mutableStateOf<String?>(null) }
    var sampleLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(reader) {
        sampleAyah = withContext(Dispatchers.IO) {
            loadQuranEdition(reader, editionId = "hafs-an-asim__uthmani")
                ?.firstOrNull { it.surah == 1 && it.ayah == 1 }
                ?.text
        }
        sampleLoaded = true
    }
    val availableRiwayat = remember { riwayatCatalog.filter { it.isAvailableOfflineSeed } }
    val scriptOptions = remember {
        listOf(
            "uthmani" to R.string.uthmani,
            "imlai" to R.string.imlai,
            "indopak" to R.string.indopak,
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        item { SectionTitle(stringResource(R.string.quranGroup)) }
        item {
            Text(
                text = stringResource(R.string.defaultRiwaya),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableRiwayat, key = { it.id.storageName }) { info ->
                    FilterChip(
                        selected = info.id.storageName == riwayaName,
                        onClick = {
                            riwayaName = info.id.storageName
                            ReaderPrefs.saveReaderDisplay(context, riwayaName, scriptName)
                        },
                        label = { Text(info.riwayaLabel(language)) },
                    )
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.quranScript),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(scriptOptions, key = { it.first }) { (id, labelRes) ->
                    FilterChip(
                        selected = id == scriptName,
                        onClick = {
                            scriptName = id
                            ReaderPrefs.saveReaderDisplay(context, riwayaName, scriptName)
                        },
                        label = { Text(stringResource(labelRes)) },
                    )
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.quranFont),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!sampleLoaded) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerHighest,
                                    RoundedCornerShape(6.dp),
                                ),
                        )
                    } else if (sampleAyah.isNullOrBlank()) {
                        EmptyVerifiedContent()
                    } else {
                        val sample = sampleAyah
                        Text(
                            text = sample ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = quranFontFamily,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(readerFontOptions, key = { it.id }) { option ->
                            FilterChip(
                                selected = option.id == fontName,
                                onClick = {
                                    fontName = option.id
                                    ReaderPrefs.saveReaderFont(context, option.id)
                                },
                                label = { Text(option.label) },
                            )
                        }
                    }
                }
            }
        }
        item {
            FilterChip(
                selected = showTranslation,
                onClick = {
                    showTranslation = !showTranslation
                    ReaderPrefs.saveShowTranslation(context, showTranslation)
                },
                label = { Text(stringResource(R.string.translation)) },
            )
        }
        item {
            Text(
                text = stringResource(R.string.preferredTafsir),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            var tafsirId by rememberSaveable { mutableStateOf(prefs.tafsirId) }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tafsirOptions, key = { it.id }) { option ->
                    FilterChip(
                        selected = option.id == tafsirId,
                        onClick = {
                            tafsirId = option.id
                            ReaderPrefs.saveTafsir(context, option.id)
                        },
                        label = { Text(stringResource(option.labelRes)) },
                    )
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.ayahNumberStyle),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            var numberStyleSetting by rememberSaveable { mutableStateOf(prefs.ayahNumberStyleName) }
            val numberOptions = remember {
                listOf(
                    "arabicIndic" to "١٢٣",
                    "easternArabic" to "۱۲۳",
                    "latin" to "123",
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(numberOptions, key = { it.first }) { (id, sample) ->
                    FilterChip(
                        selected = id == numberStyleSetting,
                        onClick = {
                            numberStyleSetting = id
                            ReaderPrefs.saveAyahNumberStyle(context, id)
                        },
                        label = { Text(sample) },
                    )
                }
            }
        }

        item { SectionTitle(stringResource(R.string.audioGroup)) }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reciters, key = { it.identifier }) { reciter ->
                    FilterChip(
                        selected = reciter.identifier == reciterId,
                        onClick = {
                            reciterId = reciter.identifier
                            ReaderPrefs.saveReciter(context, reciter.identifier)
                        },
                        label = { Text(reciter.localizedName(language)) },
                    )
                }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(
                    listOf(0.75, 1.0, 1.25, 1.5),
                    key = { it },
                ) { option ->
                    FilterChip(
                        selected = speed == option,
                        onClick = {
                            speed = option
                            AudioPrefs.saveSpeed(context, option)
                        },
                        label = { Text("${option}x") },
                    )
                }
            }
        }

        item { SectionTitle(stringResource(R.string.appearanceGroup)) }
        item {
            val themeOptions = remember {
                listOf(
                    "system" to R.string.system,
                    "light" to R.string.light,
                    "dark" to R.string.dark,
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(themeOptions, key = { it.first }) { (id, labelRes) ->
                    FilterChip(
                        selected = id == themeName,
                        onClick = {
                            themeName = id
                            ThemePrefs.save(context, id, dynamicColor)
                            onThemeChange(id, dynamicColor)
                        },
                        label = { Text(stringResource(labelRes)) },
                    )
                }
            }
        }
        item {
            FilterChip(
                selected = dynamicColor,
                onClick = {
                    dynamicColor = !dynamicColor
                    ThemePrefs.save(context, themeName, dynamicColor)
                    onThemeChange(themeName, dynamicColor)
                },
                label = { Text(stringResource(R.string.dynamicColor)) },
            )
        }
        item {
            Text(
                text = stringResource(R.string.dynamicColorHint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item { SectionTitle(stringResource(R.string.languageGroup)) }
        item {
            val localeOptions = remember {
                listOf("ar" to "العربية", "en" to "English", "fr" to "Français")
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(localeOptions, key = { it.first }) { (id, label) ->
                    FilterChip(
                        selected = id == locale,
                        onClick = {
                            locale = id
                            OnboardingPrefs.saveLocale(context, id)
                            applyAppLocale(id)
                        },
                        label = { Text(label) },
                    )
                }
            }
        }

        item { SectionTitle(stringResource(R.string.storageGroup)) }
        item {
            Button(
                onClick = { showLicenses = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.dataSourcesLicenses))
            }
        }
        item {
            Text(
                text = "${stringResource(R.string.versionLabel)} 1.0.0-kmp0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Text(
                text = stringResource(R.string.privacyNote),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showLicenses) {
        LicensesSheet(onDismiss = { showLicenses = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LicensesSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val reader = remember(context) { AndroidAssetReader(context) }
    var notices by remember { mutableStateOf<String?>(null) }
    var noticesLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(reader) {
        notices = withContext(Dispatchers.IO) {
            try {
                reader.readText("assets/licenses/DATA_NOTICES.txt")
            } catch (_: Exception) {
                null
            }
        }
        noticesLoaded = true
    }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.dataSourcesLicenses),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = stringResource(R.string.contentProvenance),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    if (!noticesLoaded) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        Text(
                            text = notices ?: stringResource(R.string.dataNoticesUnavailable),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                item {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

// ---- Prayer & Qibla (shared solar math + Android sensor/GPS) ----

private fun prayerMethodFlutterName(method: PrayerCalcMethod): String = when (method) {
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

private fun prayerMethodLabelRes(method: PrayerCalcMethod): Int = when (method) {
    PrayerCalcMethod.MUSLIM_WORLD_LEAGUE -> R.string.prayerMethodMuslimWorldLeague
    PrayerCalcMethod.ISNA -> R.string.prayerMethodIsna
    PrayerCalcMethod.EGYPT -> R.string.prayerMethodEgypt
    PrayerCalcMethod.UMM_AL_QURA -> R.string.prayerMethodUmmAlQura
    PrayerCalcMethod.KARACHI -> R.string.prayerMethodKarachi
    PrayerCalcMethod.JAFARI -> R.string.prayerMethodJafari
    PrayerCalcMethod.MOROCCO -> R.string.prayerMethodMorocco
}

internal data class ResolvedPrayerPlace(
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val tzOffsetHours: Double,
)

internal fun resolvePrayerPlace(prefs: PrayerPrefsState): ResolvedPrayerPlace {
    val lat = prefs.latitude
    val lng = prefs.longitude
    val tz = prefs.tzOffsetHours
    if (lat != null && lng != null && tz != null) {
        return ResolvedPrayerPlace(
            city = prefs.city.ifBlank { "GPS" },
            latitude = lat,
            longitude = lng,
            tzOffsetHours = tz,
        )
    }
    val preset = prayerCityPresets.firstOrNull { it.name == prefs.city }
        ?: prayerCityPresets.first()
    return ResolvedPrayerPlace(
        city = preset.name,
        latitude = preset.latitude,
        longitude = preset.longitude,
        tzOffsetHours = preset.tzOffsetHours,
    )
}

internal data class PrayerSnapshot(
    val method: PrayerCalcMethod,
    val place: ResolvedPrayerPlace,
    val times: PrayerTimes,
    val next: NextPrayer,
    val nowMinutes: Int,
)

internal fun prayerSnapshot(
    methodName: String,
    city: String,
    latitude: Double?,
    longitude: Double?,
    tzOffsetHours: Double?,
    atMillis: Long,
): PrayerSnapshot {
    val method = prayerMethodFromName(methodName)
    val place = resolvePrayerPlace(
        PrayerPrefsState(methodName, city, latitude, longitude, tzOffsetHours),
    )
    val cal = Calendar.getInstance().apply { timeInMillis = atMillis }
    val times = calculatePrayerTimes(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH),
        place.latitude,
        place.longitude,
        place.tzOffsetHours,
        method,
    )
    val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    return PrayerSnapshot(method, place, times, nextPrayer(times, nowMinutes), nowMinutes)
}

/** Trilingual prayer names mirroring Flutter's hardcoded `_prayerName`. */
private fun prayerName(key: String, language: String): String = when (language) {
    "ar" -> when (key) {
        "fajr" -> "الفجر"
        "sunrise" -> "الشروق"
        "dhuhr" -> "الظهر"
        "asr" -> "العصر"
        "maghrib" -> "المغرب"
        "isha" -> "العشاء"
        else -> key
    }
    "fr" -> when (key) {
        "fajr" -> "Fajr"
        "sunrise" -> "Lever du soleil"
        "dhuhr" -> "Dhuhr"
        "asr" -> "Asr"
        "maghrib" -> "Maghrib"
        "isha" -> "Isha"
        else -> key
    }
    else -> when (key) {
        "fajr" -> "Fajr"
        "sunrise" -> "Sunrise"
        "dhuhr" -> "Dhuhr"
        "asr" -> "Asr"
        "maghrib" -> "Maghrib"
        "isha" -> "Isha"
        else -> key
    }
}

private fun prayerScreenTitle(language: String): String = when (language) {
    "ar" -> "الصلاة والقبلة"
    "fr" -> "Prière & Qibla"
    else -> "Prayer & Qibla"
}

private fun qiblaHint(bearing: String, language: String): String = when (language) {
    "ar" -> "اتجه $bearing° من الشمال الحقيقي نحو الكعبة. تحقق مع مسجدك المحلي."
    "fr" -> "Dirigez-vous à $bearing° du nord vrai vers la Kaaba. Vérifiez avec votre mosquée."
    else -> "Face $bearing° from true north toward the Kaaba. Confirm with your local mosque."
}

private fun remainingText(hours: Int, minutes: Int, city: String, language: String): String =
    when (language) {
        "ar" -> "المتبقي $hours س $minutes د · $city"
        "fr" -> "Reste ${hours}h ${minutes}m · $city"
        else -> "In ${hours}h ${minutes}m · $city"
    }

private fun locationMethodTitle(language: String): String = when (language) {
    "ar" -> "الموقع وطريقة الحساب"
    else -> "Location & method"
}

private fun methodLabel(language: String): String = when (language) {
    "ar" -> "الطريقة"
    else -> "Method"
}

private fun cityLabel(language: String): String = when (language) {
    "ar" -> "المدينة"
    else -> "City"
}

private fun useGpsLabel(language: String): String = when (language) {
    "ar" -> "استخدام GPS"
    "fr" -> "Utiliser le GPS"
    else -> "Use GPS"
}

private fun gpsUnavailableText(language: String): String = when (language) {
    "ar" -> "تعذر تحديد الموقع — أدخل المدينة يدويًا"
    else -> "GPS unavailable — pick a city manually"
}

private fun prayerDisclaimer(language: String): String = when (language) {
    "ar" -> "حساب فلكي دون اتصال (±2 دقيقة). اعتمد مسجدك للأذان."
    else -> "Offline astronomical calc (±2 min). Follow your mosque for adhan."
}

private fun notificationsTitle(language: String): String = when (language) {
    "ar" -> "تنبيهات الصلاة"
    "fr" -> "Notifications de prière"
    else -> "Prayer notifications"
}

private fun notificationDeniedText(language: String): String = when (language) {
    "ar" -> "تم رفض إذن التنبيهات"
    else -> "Notification permission denied"
}

@Composable
fun PrayerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val language = currentLanguage()
    val prefs = remember(context) { PrayerPrefs.load(context) }
    var methodName by rememberSaveable { mutableStateOf(prefs.methodName) }
    var city by rememberSaveable { mutableStateOf(prefs.city) }
    var latitude by remember { mutableStateOf(prefs.latitude) }
    var longitude by remember { mutableStateOf(prefs.longitude) }
    var tzOffsetHours by remember { mutableStateOf(prefs.tzOffsetHours) }
    var heading by remember { mutableStateOf<Float?>(null) }
    var gpsBusy by remember { mutableStateOf(false) }
    var gpsFailed by remember { mutableStateOf(false) }
    var notifEnabled by rememberSaveable { mutableStateOf(prefs.notificationsEnabled) }
    var tick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val compass = remember(context) { AndroidCompass(context) }
    val locator = remember(context) { AndroidLocator(context) }

    DisposableEffect(compass) {
        compass.start { heading = it }
        onDispose { compass.stop() }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            tick = System.currentTimeMillis()
        }
    }

    val snap = remember(methodName, city, latitude, longitude, tzOffsetHours, tick) {
        prayerSnapshot(methodName, city, latitude, longitude, tzOffsetHours, tick)
    }
    val bearing = remember(snap) { qiblaBearing(snap.place.latitude, snap.place.longitude) }
    val bearingText = remember(bearing) { "%.0f".format(bearing) }
    val remaining = (snap.next.minutes - snap.nowMinutes + 1440) % 1440

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            PrayerPrefs.saveNotificationsEnabled(context, true)
            notifEnabled = true
            PrayerNotifications.scheduleDaily(context)
        } else {
            scope.launch {
                snackbar.showSnackbar(notificationDeniedText(language))
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
                    val tz = TimeZone.getDefault()
                        .getOffset(System.currentTimeMillis()) / 3600000.0
                    PrayerPrefs.saveGps(context, fix.city, fix.latitude, fix.longitude, tz)
                    city = fix.city
                    latitude = fix.latitude
                    longitude = fix.longitude
                    tzOffsetHours = tz
                }
            }
        } else {
            gpsFailed = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(onClick = onBack) {
                        Text(stringResource(R.string.home))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = prayerScreenTitle(language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "${prayerName(snap.next.key, language)} · ${snap.times.format(snap.next.minutes)}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = remainingText(
                                remaining / 60,
                                remaining % 60,
                                snap.place.city,
                                language,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column {
                        snap.times.ordered.forEach { (key, minutes) ->
                            val isNext = key == snap.next.key
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = prayerName(key, language),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isNext) FontWeight.ExtraBold else null,
                                    color = if (isNext) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = snap.times.format(minutes),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isNext) FontWeight.ExtraBold else null,
                                    color = if (isNext) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            }
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
                            text = qiblaHint(bearingText, language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = locationMethodTitle(language),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = methodLabel(language),
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
                                    label = { Text(stringResource(prayerMethodLabelRes(method))) },
                                )
                            }
                        }
                        Text(
                            text = cityLabel(language),
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
                                        if (isMoroccanCity(preset.name) &&
                                            methodName == "muslimWorldLeague"
                                        ) {
                                            methodName = "morocco"
                                            PrayerPrefs.saveMethod(context, "morocco")
                                        }
                                    },
                                    label = { Text(preset.name) },
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
                            Text(useGpsLabel(language))
                        }
                        if (gpsFailed) {
                            Text(
                                text = gpsUnavailableText(language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = notificationsTitle(language),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Switch(
                                checked = notifEnabled,
                                onCheckedChange = { enabled ->
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
                                },
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
                            text = tzLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = prayerDisclaimer(language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QiblaDial(
    bearing: Double,
    heading: Float?,
) {
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.surfaceContainerHighest
    Canvas(modifier = Modifier.size(200.dp)) {
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

@Composable
private fun PrayerHeroCard(
    snap: PrayerSnapshot,
    language: String,
    onClick: () -> Unit,
) {
    val remaining = (snap.next.minutes - snap.nowMinutes + 1440) % 1440
    HeroGradientCard(onClick = onClick) {
        Text(
            text = "${prayerName(snap.next.key, language)} · ${snap.times.format(snap.next.minutes)}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = remainingText(remaining / 60, remaining % 60, snap.place.city, language),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

/**
 * Builds the process-wide search index once (Quran + all bundled hadith +
 * Jalalayn tafsir). Callers must invoke off the UI thread; repeat calls
 * are cheap no-ops once documents exist.
 */
private val searchIndexMutex = kotlinx.coroutines.sync.Mutex()

private suspend fun ensureSearchIndex(
    reader: AndroidAssetReader,
    engine: SearchEngine,
) {
    // Guarded: SearchScreen + filter sheets can race and double-index
    // (SearchEngine.docs is not thread-safe for concurrent index+search).
    if (engine.documentCount() > 0) return
    searchIndexMutex.withLock {
        if (engine.documentCount() > 0) return@withLock
        withContext(Dispatchers.IO) {
        loadQuranEdition(reader, editionId = "hafs-an-asim__uthmani")
            ?.let { engine.indexQuran(it) }
        val names = hadithCollections.associate { it.id to it.nameEn }
        val all = mutableListOf<Hadith>()
        for (collection in hadithCollections) {
            val raw = reader.readText("assets/hadith/${collection.id}/index.json")
                ?: continue
            val index = try {
                parseHadithIndex(raw)
            } catch (_: Exception) {
                continue
            }
            for (section in index.sections) {
                if (section.count <= 0) continue
                val sectionRaw = reader.readText(
                    "assets/hadith/${collection.id}/sections/${section.section}.json",
                ) ?: continue
                try {
                    all.addAll(parseHadithSection(collection.id, section.title, sectionRaw))
                } catch (_: Exception) {
                }
            }
        }
        engine.indexHadith(all, names)
        val tafsir = mutableListOf<TafsirEntry>()
        for (surah in 1..114) {
            val entries = try {
                loadTafsirSurah(reader, "jalalayn", surah)
            } catch (_: Exception) {
                null
            } ?: continue
            for ((ayah, text) in entries) {
                tafsir.add(TafsirEntry(surah, ayah, "jalalayn", "Jalalayn", text))
            }
        }
        engine.indexTafsir(tafsir, "Jalalayn", "Jalalayn")
        }
    }
}

@Composable
fun SearchScreen(
    onOpenAyahRef: (String) -> Unit = {},
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val language = currentLanguage()
    val reader = remember(context) { AndroidAssetReader(context) }
    val libraryStore = remember(context) { AndroidStores.library(context) }
    val engine = remember { AndroidStores.searchEngine() }
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    var debounced by rememberSaveable { mutableStateOf("") }
    var indexReady by remember { mutableStateOf(engine.documentCount() > 0) }
    var results by remember { mutableStateOf(SearchResults()) }
    var selectedHadith by remember { mutableStateOf<Hadith?>(null) }

    LaunchedEffect(reader) {
        ensureSearchIndex(reader, engine)
        indexReady = true
    }

    LaunchedEffect(query) {
        delay(300)
        debounced = query
    }
    LaunchedEffect(debounced, indexReady) {
        if (!indexReady || debounced.isBlank()) {
            results = SearchResults()
            return@LaunchedEffect
        }
        results = withContext(Dispatchers.Default) {
            engine.search(debounced, SearchOptions(limitPerCategory = 20))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onBack) {
                Text(stringResource(R.string.home))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.search),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.searchHint)) },
        )
        if (!indexReady) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (debounced.isBlank()) {
            EmptyVerifiedContent()
        } else if (results.total == 0) {
            Card(Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.noLibraryResults),
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (results.surahs.isNotEmpty()) {
                    item {
                        SectionTitle(
                            "${stringResource(R.string.surahs)} (${results.surahs.size})",
                        )
                    }
                    items(results.surahs, key = { it.refKey }) { hit ->
                        SearchHitRow(
                            title = hit.title,
                            subtitle = hit.subtitle,
                            onClick = {
                                hit.surah?.let { onOpenAyahRef("$it:1") }
                            },
                        )
                    }
                }
                if (results.quran.isNotEmpty()) {
                    item {
                        val count = results.quran.size
                        SectionTitle(
                            "${stringResource(R.string.quran)} ($count${if (results.truncated) "+" else ""})",
                        )
                    }
                    items(results.quran, key = { it.refKey }) { hit ->
                        SearchHitRow(
                            title = hit.title,
                            subtitle = hit.snippet,
                            rtlSnippet = true,
                            onClick = {
                                if (hit.surah != null && hit.ayah != null) {
                                    onOpenAyahRef("${hit.surah}:${hit.ayah}")
                                }
                            },
                        )
                    }
                }
                if (results.hadith.isNotEmpty()) {
                    item {
                        val count = results.hadith.size
                        SectionTitle(
                            "${stringResource(R.string.sunnah)} ($count${if (results.truncated) "+" else ""})",
                        )
                    }
                    items(results.hadith, key = { it.refKey }) { hit ->
                        SearchHitRow(
                            title = hit.title,
                            subtitle = hit.snippet,
                            rtlSnippet = true,
                            onClick = {
                                hit.hadith?.let { selectedHadith = it }
                            },
                        )
                    }
                }
                if (results.tafsir.isNotEmpty()) {
                    item {
                        val count = results.tafsir.size
                        SectionTitle(
                            "${stringResource(R.string.tafsir)} ($count${if (results.truncated) "+" else ""})",
                        )
                    }
                    items(results.tafsir, key = { it.refKey }) { hit ->
                        SearchHitRow(
                            title = hit.title,
                            subtitle = hit.snippet,
                            onClick = {
                                if (hit.surah != null && hit.ayah != null) {
                                    onOpenAyahRef("${hit.surah}:${hit.ayah}")
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    selectedHadith?.let { hadith ->
        val collection = hadithCollections.firstOrNull { it.id == hadith.collectionId }
        if (collection != null) {
            HadithReaderSheet(
                hadith = hadith,
                collection = collection,
                libraryStore = libraryStore,
                onDismiss = { selectedHadith = null },
            )
            LaunchedEffect(hadith.id) {
                withContext(Dispatchers.IO) {
                    libraryStore.touchRecent(
                        RecentItem(
                            refKey = hadith.id,
                            title = "${collection.localizedName(language)} ${hadith.hadithNumber}",
                            subtitle = hadith.book,
                            kind = BookmarkKind.HADITH,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchHitRow(
    title: String,
    subtitle: String,
    rtlSnippet: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = if (rtlSnippet) TextAlign.End else TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
            )
        }
    }
}

// ---- Dhikr counter (shared seed + daily device prefs) ----

private fun dhikrTitle(language: String): String = when (language) {
    "ar" -> "الأذكار"
    else -> "Dhikr"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DhikrScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val language = currentLanguage()
    var state by remember { mutableStateOf(DhikrPrefs.load(context)) }
    var showHelp by remember { mutableStateOf(false) }
    val todayTotal = state.today.values.sum()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onBack) {
                Text(stringResource(R.string.home))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = dhikrTitle(language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (language == "ar") {
                            "اليوم: $todayTotal · الكل: ${state.total}"
                        } else {
                            "Today: $todayTotal · Total: ${state.total}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (language == "ar") {
                            "اضغط على البطاقة للعد"
                        } else {
                            "Tap a card to count"
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            items(kAdhkar, key = { it.id }) { dhikr ->
                val count = state.today[dhikr.id] ?: 0
                val done = count >= dhikr.target
                Card(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { state = DhikrPrefs.tap(context, dhikr.id) },
                                onLongClick = { state = DhikrPrefs.reset(context, dhikr.id) },
                            )
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = dhikr.textAr,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text = when (language) {
                                "ar" -> dhikr.transliteration
                                "fr" -> "${dhikr.transliteration} · ${dhikr.translationFr}"
                                else -> "${dhikr.transliteration} · ${dhikr.translationEn}"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        LinearProgressIndicator(
                            progress = { (count.toFloat() / dhikr.target).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$count / ${dhikr.target}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (done) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                            if (done) {
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = dhikr.source,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            item {
                TextButton(onClick = { showHelp = true }) {
                    Text(
                        if (language == "ar") {
                            "كيف يعمل؟"
                        } else {
                            "How it works?"
                        },
                    )
                }
            }
        }
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("ℹ") },
            text = {
                Text(
                    if (language == "ar") {
                        "اضغط مطولًا لإعادة التصفير. العد يومي ويُحفظ على جهازك."
                    } else {
                        "Long-press to reset. Counts are daily, stored on-device."
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) {
                    Text("OK")
                }
            },
        )
    }
}

@Composable
private fun DhikrHeroCard(
    todayTotal: Int,
    total: Int,
    language: String,
    onClick: () -> Unit,
) {
    HeroGradientCard(onClick = onClick) {
        Text(
            text = if (language == "ar") {
                "الأذكار · اليوم: $todayTotal · الكل: $total"
            } else {
                "Dhikr · Today: $todayTotal · Total: $total"
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = if (language == "ar") {
                "اضغط على البطاقة للعد"
            } else {
                "Tap a card to count"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

private fun memorizationTitle(language: String): String = when (language) {
    "ar" -> "الحفظ"
    "fr" -> "Mémorisation"
    else -> "Memorization"
}

private fun memorizationHowTo(language: String): String = when (language) {
    "ar" -> "أخفِ الآيات من زر العين في القارئ، سمّع، ثم اكشف وعلّم ✓"
    "fr" -> "Masquez via l’œil dans le lecteur, récitez, révélez, cochez ✓"
    else -> "Hide via the eye in the reader, recite, reveal, check ✓"
}

@Composable
fun MemorizationScreen(
    onOpenAyahRef: (String) -> Unit = {},
    onBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val language = currentLanguage()
    var memorized by remember { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(Unit) {
        memorized = withContext(Dispatchers.IO) { MemorizationPrefs.load(context) }
    }
    val totalMemorized = memorized.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onBack) {
                Text(stringResource(R.string.home))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = memorizationTitle(language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (language == "ar") {
                        "محفوظ: $totalMemorized / 6236"
                    } else {
                        "$totalMemorized / 6236"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                LinearProgressIndicator(
                    progress = { (totalMemorized / 6236f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = memorizationHowTo(language),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            items(surahMetadata, key = { it.number }) { surah ->
                val done = MemorizationPrefs.countForSurah(
                    memorized,
                    surah.number,
                    surah.ayahCount,
                )
                if (done == 0) {
                    SurahRow(
                        surah = surah,
                        language = language,
                        onClick = { onOpenAyahRef("${surah.number}:1") },
                    )
                } else {
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val start = MemorizationPrefs.firstUnmemorized(
                                        memorized,
                                        surah.number,
                                        surah.ayahCount,
                                    )
                                    onOpenAyahRef("${surah.number}:$start")
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Text(
                                    text = surah.number.toString(),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = surah.localizedName(language),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = {
                                        (done.toFloat() / surah.ayahCount).coerceIn(0f, 1f)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "$done / ${surah.ayahCount}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemorizationHeroCard(
    totalMemorized: Int,
    language: String,
    onClick: () -> Unit,
) {
    HeroGradientCard(onClick = onClick) {
        Text(
            text = if (language == "ar") {
                "${memorizationTitle(language)} · محفوظ: $totalMemorized / 6236"
            } else {
                "${memorizationTitle(language)} · $totalMemorized / 6236"
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = memorizationHowTo(language),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

/**
 * Formats an ayah number per the persisted style. Mirrors Flutter: both
 * Arabic styles render Arabic-Indic digits, Latin stays as-is.
 */
private fun ayahNumberText(number: Int, styleName: String): String = when (styleName) {
    "latin" -> number.toString()
    else -> toArabicIndic(number)
}

private fun SurahMeta.localizedName(language: String): String =
    when (language) {
        "ar" -> nameAr
        "fr" -> nameFr
        else -> nameEn
    }

private fun Reciter.localizedName(language: String): String =
    when (language) {
        "ar" -> nameAr
        else -> nameEn
    }

private fun ReaderPrefsState.readerEditionId(): String =
    readerEditions.firstOrNull {
        it.riwayaName == riwayaName && it.scriptName == scriptName
    }?.id ?: readerEditions.first().id

private fun quranFontFamily(
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
private fun QuranIndexMode.label(): String =
    when (this) {
        QuranIndexMode.SURAHS -> stringResource(R.string.surahs)
        QuranIndexMode.JUZ -> stringResource(R.string.juz)
        QuranIndexMode.HIZB -> stringResource(R.string.hizb)
        QuranIndexMode.RUB -> stringResource(R.string.rub)
        QuranIndexMode.PAGE -> stringResource(R.string.page)
    }
