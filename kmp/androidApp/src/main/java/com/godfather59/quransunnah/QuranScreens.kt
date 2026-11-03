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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.material3.RadioButton
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
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
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuranIndexScreen(
    deepLinkSurah: Int? = null,
    deepLinkSurahToken: Int = 0,
    isActive: Boolean = true,
) {
    val context = LocalContext.current
    val mushafReader = remember(context) { AndroidStores.mushafMetadataReader(context) }
    var mushafMeta by remember { mutableStateOf<MushafMetadata?>(null) }
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var selectedSurah by rememberSaveable { mutableStateOf<Int?>(null) }
    var mushafPage by rememberSaveable { mutableStateOf<Int?>(null) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    val isWideReader = LocalConfiguration.current.screenWidthDp >= 1000
    val indexPagerState = rememberPagerState(
        initialPage = mode,
        pageCount = { QuranIndexMode.entries.size },
    )
    val selected = QuranIndexMode.entries[mode]
    val language = currentLanguage()

    LaunchedEffect(indexPagerState) {
        snapshotFlow { indexPagerState.settledPage }
            .distinctUntilChanged()
            .collect { mode = it }
    }
    LaunchedEffect(mode) {
        if (indexPagerState.currentPage != mode) {
            indexPagerState.animateScrollToPage(mode)
        }
    }

    LaunchedEffect(mushafReader, isActive) {
        if (!isActive) return@LaunchedEffect
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
            isActive = isActive,
        )
        return
    }

    selectedSurah?.takeIf { !isWideReader }?.let { surahNumber ->
        QuranReaderScreen(
            surahNumber = surahNumber,
            onBack = { selectedSurah = null },
            isActive = isActive,
        )
        return
    }

    if (showSearch) {
        BackHandler(enabled = isActive) { showSearch = false }
        SearchScreen(
            isActive = isActive,
            onOpenAyahRef = { raw ->
                val parsed = parseQuranRef(raw)
                if (parsed != null) {
                    val (su, ay) = parsed
                    if (su in 1..114 && ay >= 1) {
                        val count = surahMetadata.first { it.number == su }.ayahCount
                        ReaderPrefs.saveLastPosition(
                            context,
                            su,
                            ay.coerceIn(1, count),
                        )
                        selectedSurah = su
                    }
                }
                showSearch = false
            },
            onBack = { showSearch = false },
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
                    text = selectedSurah?.let { surahMetadata[it - 1].localizedName(language) }
                        ?: stringResource(R.string.quran),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
                if (selectedSurah != null) {
                    IconButton(onClick = { selectedSurah = null }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.close),
                            tint = Color.White,
                        )
                    }
                } else {
                    IconButton(onClick = { showSearch = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search),
                            tint = Color.White,
                        )
                    }
                }
            }
        },
        body = {
            if (isWideReader && selectedSurah != null) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.38f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.surahs),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        SurahList(onSurahSelected = { selectedSurah = it })
                    }
                    VerticalDivider()
                    QuranReaderScreen(
                        surahNumber = selectedSurah!!,
                        onBack = { selectedSurah = null },
                        modifier = Modifier
                            .weight(0.62f)
                            .fillMaxHeight(),
                        applyStatusBarInset = false,
                        isActive = isActive,
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                LastReadCard(onOpen = { selectedSurah = it })

                ScrollableTabRow(
                    selectedTabIndex = mode,
                    edgePadding = 0.dp,
                ) {
                    QuranIndexMode.entries.forEach { item ->
                        Tab(
                            selected = selected == item,
                            onClick = { mode = item.ordinal },
                            text = { Text(item.label()) },
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    HorizontalPager(
                        state = indexPagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = false,
                        key = { it },
                    ) { page ->
                        when (QuranIndexMode.entries[page]) {
                            QuranIndexMode.SURAHS -> SurahList(
                                onSurahSelected = { selectedSurah = it },
                            )
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
                }
            }
        },
    )
}

@Composable
private fun SurahList(onSurahSelected: (Int) -> Unit) {
    val language = currentLanguage()
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(
            items = surahMetadata,
            key = { it.number },
            contentType = { "surah" },
        ) { surah ->
            SurahRow(
                surah = surah,
                language = language,
                onClick = { onSurahSelected(surah.number) },
            )
        }
    }
}

@Composable
private fun LastReadCard(
    onOpen: (Int) -> Unit,
) {
    val context = LocalContext.current
    val language = currentLanguage()
    var lastSurah by remember { mutableStateOf(1) }
    var lastAyah by remember { mutableStateOf(1) }
    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(context) {
        withContext(Dispatchers.IO) {
            try {
                val state = ReaderPrefs.load(context)
                lastSurah = state.lastSurah.coerceIn(1, 114)
                lastAyah = state.lastAyah.coerceAtLeast(1)
            } catch (_: Exception) {
            }
            ready = true
        }
    }
    if (!ready) return
    val surah = surahMetadata[lastSurah - 1]
    val ayah = lastAyah.coerceIn(1, surah.ayahCount)
    Card(
        onClick = { onOpen(lastSurah) },
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp).size(28.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.continueReading),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${surah.localizedName(language)} $lastSurah:$ayah",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = surah.nameAr,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
internal fun SurahRow(
    surah: SurahMeta,
    language: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("surahRow_${surah.number}")
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
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    text = surah.number.toString(),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = surah.localizedName(language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Text(
                    text = "${surah.ayahCount} ${stringResource(R.string.ayat)} · " +
                        stringResource(if (surah.makki) R.string.makki else R.string.madani),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Text(
                text = surah.nameAr,
                style = MaterialTheme.typography.titleLarge.copy(textDirection = TextDirection.Rtl),
                textAlign = TextAlign.Right,
            )
        }
    }
}

@Composable
private fun TajweedLegendDialog(onDismiss: () -> Unit) {
    val language = currentLanguage()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
        title = { Text(stringResource(R.string.tajweedLegend)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text(
                        text = stringResource(R.string.tajweedVerifiedHint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(tajweedRules, key = { it.rule }) { rule ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(tajweedRuleInfo(rule.rule).colorValue),
                            shape = CircleShape,
                        ) {
                            Spacer(Modifier.padding(7.dp).size(10.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = if (language == "ar") rule.nameAr else rule.nameEn,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                item {
                    Text(
                        text = stringResource(R.string.tajweedHafsOnly),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}

/**
 * Verse text with optional Tajweed coloring (Hafs/Uthmani overlay only).
 * Falls back to plain text while loading, on error, or for other editions.
 */
@Composable
internal fun TajweedAyahText(
    ayah: Ayah,
    showTajweed: Boolean,
    tajweedSpans: Map<Int, List<TajweedSpan>>,
    quranFontFamily: FontFamily?,
    modifier: Modifier = Modifier,
    textSize: Float = 26f,
    lineSpacing: Float = 1.65f,
) {
    val active = showTajweed && isTajweedEdition(ayah.editionId)
    val list = if (active) tajweedSpans[ayah.ayah].orEmpty() else emptyList()
    if (!active || list.isEmpty()) {
        Text(
            text = ayah.text,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = textSize.sp,
                lineHeight = (textSize * lineSpacing).sp,
                textDirection = TextDirection.Rtl,
            ),
            fontFamily = quranFontFamily,
            textAlign = TextAlign.Right,
            modifier = modifier.fillMaxWidth(),
        )
        return
    }
    val annotated = remember(ayah.text, list) {
        androidx.compose.ui.text.buildAnnotatedString {
            append(ayah.text)
            for (span in list) {
                val from = span.start.coerceIn(0, ayah.text.length)
                val to = span.end.coerceIn(0, ayah.text.length)
                if (to > from) {
                    addStyle(
                        androidx.compose.ui.text.SpanStyle(
                            color = Color(tajweedRuleInfo(span.rule).colorValue),
                        ),
                        from,
                        to,
                    )
                }
            }
        }
    }
    Text(
        text = annotated,
        style = MaterialTheme.typography.titleLarge.copy(
            fontSize = textSize.sp,
            lineHeight = (textSize * lineSpacing).sp,
            textDirection = TextDirection.Rtl,
        ),
        fontFamily = quranFontFamily,
        textAlign = TextAlign.Right,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun QuranReaderScreen(
    surahNumber: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    applyStatusBarInset: Boolean = true,
    isActive: Boolean = true,
) {
    BackHandler(enabled = isActive, onBack = onBack)
    val context = LocalContext.current
    val language = currentLanguage()
    val surah = surahMetadata.first { it.number == surahNumber }
    var prefsState by remember(context) { mutableStateOf<ReaderPrefsState?>(null) }
    LaunchedEffect(context) {
        prefsState = withContext(Dispatchers.IO) { ReaderPrefs.load(context) }
    }
    val loadedPrefs = prefsState
    if (loadedPrefs == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val prefs = loadedPrefs
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
    var translationId by rememberSaveable { mutableStateOf(prefs.translationId) }
    var quranTextSize by rememberSaveable { mutableStateOf(prefs.quranTextSize) }
    var translationTextSize by rememberSaveable { mutableStateOf(prefs.translationTextSize) }
    var lineSpacing by rememberSaveable { mutableStateOf(prefs.lineSpacing) }
    var focusMode by rememberSaveable { mutableStateOf(false) }
    var showTajweed by rememberSaveable { mutableStateOf(prefs.showTajweed) }
    var showTajweedLegend by remember { mutableStateOf(false) }
    var showReaderSettings by rememberSaveable { mutableStateOf(false) }
    val numberStyle = remember(prefs) { prefs.ayahNumberStyleName }
    var selectedAyah by remember { mutableStateOf<Ayah?>(null) }
    var tafsirAyah by remember { mutableStateOf<Ayah?>(null) }
    var compareAyah by remember { mutableStateOf<Ayah?>(null) }
    var studyAyah by remember { mutableStateOf<Ayah?>(null) }
    var showFontSheet by remember { mutableStateOf(false) }
    var showMushaf by rememberSaveable { mutableStateOf(false) }
    var hideMode by rememberSaveable { mutableStateOf(false) }
    var readerModeName by rememberSaveable { mutableStateOf(ReaderMode.READ.name) }
    val readerMode = remember(readerModeName) { readerModeFromName(readerModeName) }
    var revealed by remember { mutableStateOf(emptySet<String>()) }
    var memorized by remember { mutableStateOf(emptySet<String>()) }
    LaunchedEffect(context) {
        memorized = withContext(Dispatchers.IO) { MemorizationPrefs.load(context) }
    }

    if (showMushaf) {
        MushafReaderScreen(
            initialSurah = surahNumber,
            onBack = { showMushaf = false },
            isActive = isActive,
        )
        return
    }
    val reader = remember(context) { AndroidAssetReader(context) }
    val quranFontFamily = remember(context, fontName) {
        quranFontFamily(context, fontName)
    }
    val ayahListState = rememberLazyListState(
        initialFirstVisibleItemIndex = (initialAyah - 1).coerceAtLeast(0),
    )
    val visibleAyahIndex by remember(ayahListState) {
        derivedStateOf {
            ayahListState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
        }
    }
    LaunchedEffect(context, language, surahNumber, initialAyah) {
        ReaderPrefs.saveLastPosition(context, surahNumber, initialAyah)
        KhatmaPrefs.touchToday(context)
    }
    // Heavy verified-asset parse off the Main thread: 1.3 MB text + filter.
    // remember() ran this in composition and janked surah/edition switches.
    var ayahs by remember { mutableStateOf<List<Ayah>>(emptyList()) }
    var ayahsLoaded by remember { mutableStateOf(false) }
    var ayahLoadFailed by remember { mutableStateOf(false) }
    var ayahRetryToken by remember { mutableIntStateOf(0) }
    LaunchedEffect(surahNumber, editionId, ayahRetryToken) {
        ayahsLoaded = false
        ayahLoadFailed = false
        val result = withContext(Dispatchers.IO) {
            runCatching {
                loadQuranEdition(reader, editionId = editionId)
                    ?.filter { it.surah == surahNumber }
                    ?: throw IllegalStateException("Quran edition unavailable")
            }
        }
        ayahs = result.getOrDefault(emptyList())
        ayahLoadFailed = result.isFailure || ayahs.isEmpty()
        ayahsLoaded = true
    }
    var tajweedSpans by remember(surahNumber) {
        mutableStateOf<Map<Int, List<TajweedSpan>>>(emptyMap())
    }
    LaunchedEffect(surahNumber, showTajweed, editionId) {
        tajweedSpans = emptyMap()
        if (showTajweed && isTajweedEdition(editionId)) {
            tajweedSpans = withContext(Dispatchers.IO) {
                try {
                    loadTajweedSurah(reader, surahNumber).orEmpty()
                } catch (_: Exception) {
                    emptyMap()
                }
            }
        }
    }
    LaunchedEffect(surahNumber, visibleAyahIndex, ayahs.size) {
        val visibleAyah = ayahs.getOrNull(visibleAyahIndex) ?: return@LaunchedEffect
        delay(500)
        if (!ayahListState.isScrollInProgress) {
            ReaderPrefs.saveLastPosition(context, surahNumber, visibleAyah.ayah)
        }
    }
    var translation by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    LaunchedEffect(showTranslation, translationId) {
        translation = withContext(Dispatchers.IO) {
            if (!showTranslation) {
                emptyMap()
            } else {
                try {
                    val path = translationAssets[translationId]
                    val raw = path?.let { reader.readText(it) }
                    if (raw == null) emptyMap() else parsePipeMap(raw)
                } catch (_: Exception) {
                    emptyMap()
                }
            }
        }
    }
    LaunchedEffect(readerMode) {
        if (readerMode == ReaderMode.MEMORIZE) {
            hideMode = true
            revealed = emptySet()
        } else if (hideMode) {
            hideMode = false
            revealed = emptySet()
        }
    }

    val largeReaderText = LocalDensity.current.fontScale >= 1.5f
    val readerHeading: @Composable (Modifier) -> Unit = { headingModifier ->
        Column(headingModifier) {
            Text(
                text = surah.localizedName(language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(
                    R.string.activeEditionLabel,
                    readerEditionLabel(readerEditions.first { it.id == editionId }),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .then(if (applyStatusBarInset) Modifier.statusBarsPadding() else Modifier)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.surahs),
                )
            }
            if (largeReaderText) Spacer(Modifier.weight(1f)) else readerHeading(Modifier.weight(1f))
            IconButton(
                onClick = { focusMode = !focusMode },
                modifier = Modifier.testTag("readerFocusMode"),
            ) {
                Icon(
                    imageVector = if (focusMode) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = stringResource(
                        if (focusMode) R.string.readerShowControls else R.string.readerFocusMode,
                    ),
                )
            }
            IconButton(
                onClick = { showReaderSettings = true },
                modifier = Modifier.testTag("readerSettings"),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                )
            }
            if (readerMode == ReaderMode.MEMORIZE) {
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
                        contentDescription = stringResource(
                            if (hideMode) R.string.revealVerses else R.string.hideVerses,
                        ),
                    )
                }
            }
        }
        if (largeReaderText) readerHeading(Modifier.fillMaxWidth())
        if (!focusMode) {
        val currentAyahNumber = ayahs.getOrNull(visibleAyahIndex)?.ayah ?: initialAyah
        val readingProgress = if (ayahs.isEmpty()) {
            0f
        } else {
            ((visibleAyahIndex + 1).toFloat() / ayahs.size).coerceIn(0f, 1f)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${stringResource(R.string.ayat)} $currentAyahNumber / ${surah.ayahCount}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${(readingProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        LinearProgressIndicator(
            progress = { readingProgress },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    stateDescription = "${(readingProgress * 100).toInt().coerceIn(0, 100)}%"
                }
                .testTag("readerProgress"),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ReaderMode.values()) { mode ->
                FilterChip(
                    selected = readerMode == mode,
                    onClick = { readerModeName = mode.name },
                    label = { Text(readerModeLabel(mode)) },
                )
            }
        }
        }

        if (isActive && showReaderSettings) {
            ModalBottomSheet(onDismissRequest = { showReaderSettings = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    ReaderStyleControls(
                        quranTextSize = quranTextSize,
                        translationTextSize = translationTextSize,
                        lineSpacing = lineSpacing,
                        onStyleChanged = { arabic, translated, spacing ->
                            quranTextSize = arabic
                            translationTextSize = translated
                            lineSpacing = spacing
                        },
                        onStyleChangeFinished = {
                            ReaderPrefs.saveReadingStyle(context, quranTextSize, translationTextSize, lineSpacing)
                        },
                        quranFontFamily = quranFontFamily,
                        previewAyah = ayahs.firstOrNull()?.text.orEmpty(),
                        previewTranslation = ayahs.firstOrNull()?.let { translation["${it.surah}:${it.ayah}"] },
                    )
                    if (readerMode != ReaderMode.MEMORIZE) {
                        Text(stringResource(R.string.qiraaLabel), style = MaterialTheme.typography.titleSmall)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.testTag("readerEditionChoices"),
                        ) {
                            items(readerEditions, key = { it.id }) { edition ->
                                FilterChip(
                                    selected = edition.id == editionId,
                                    onClick = {
                                        editionId = edition.id
                                        ReaderPrefs.saveReaderDisplay(context, edition.riwayaName, edition.scriptName)
                                    },
                                    label = { Text(readerEditionLabel(edition)) },
                                    modifier = Modifier.testTag("readerEdition_${edition.id}"),
                                )
                            }
                        }
                    }
                    if (readerMode == ReaderMode.LISTEN) {
                        Text(stringResource(R.string.reciter), style = MaterialTheme.typography.titleSmall)
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
                    if (readerMode != ReaderMode.MEMORIZE) {
                        TextButton(onClick = {
                            showReaderSettings = false
                            showFontSheet = true
                        }) { Text(stringResource(R.string.quranFont)) }
                    }
                    if (readerMode != ReaderMode.LISTEN) {
                        val translationLabel = stringResource(R.string.translation)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(translationLabel)
                            Switch(
                                checked = showTranslation,
                                modifier = Modifier.semantics { contentDescription = translationLabel },
                                onCheckedChange = {
                                    showTranslation = it
                                    ReaderPrefs.saveShowTranslation(context, it)
                                },
                            )
                        }
                        if (showTranslation) {
                            ReaderTranslationChoices(
                                selectedId = translationId,
                                onSelected = {
                                    translationId = it
                                    ReaderPrefs.saveTranslation(context, it)
                                },
                            )
                        }
                    }
                    if (readerMode == ReaderMode.READ || readerMode == ReaderMode.STUDY) {
                        val tajweedLabel = stringResource(R.string.tajweedColors)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(tajweedLabel)
                            Switch(
                                checked = showTajweed,
                                modifier = Modifier.semantics { contentDescription = tajweedLabel },
                                onCheckedChange = {
                                    showTajweed = it
                                    ReaderPrefs.saveShowTajweed(context, it)
                                },
                            )
                        }
                    }
                    if (readerMode == ReaderMode.STUDY && showTajweed) {
                        TextButton(onClick = {
                            showReaderSettings = false
                            showTajweedLegend = true
                        }) { Text(stringResource(R.string.tajweedLegend)) }
                    }
                    if (readerMode == ReaderMode.READ || readerMode == ReaderMode.MEMORIZE || readerMode == ReaderMode.LISTEN) {
                        TextButton(
                            onClick = {
                                showReaderSettings = false
                                showMushaf = true
                            },
                            modifier = Modifier.testTag("readerOpenMushaf"),
                        ) { Text(stringResource(R.string.mushaf)) }
                    }
                }
            }
        }
        if (!ayahsLoaded) {
            LoadingVerifiedContent(Modifier.weight(1f))
        } else if (ayahLoadFailed) {
            EmptyVerifiedContent(
                messageRes = R.string.datasetUnavailable,
                onRetry = { ayahRetryToken += 1 },
            )
        } else if (ayahs.isEmpty()) {
            EmptyVerifiedContent()
        } else {
            if (isActive && showFontSheet) {
                FontSheet(
                    selectedFontName = fontName,
                    onSelected = { selected ->
                        fontName = selected
                        ReaderPrefs.saveReaderFont(context, selected)
                    },
                    onDismiss = { showFontSheet = false },
                )
            }
            if (isActive && showTajweedLegend) {
                TajweedLegendDialog(onDismiss = { showTajweedLegend = false })
            }
            if (isActive) selectedAyah?.let { ayah ->
                val reference = "${surah.localizedName(language)} ${ayah.surah}:${ayah.ayah}"
                AyahActionSheet(
                    ayah = ayah,
                    reference = reference,
                    translation = translation["${ayah.surah}:${ayah.ayah}"],
                    quranFontFamily = quranFontFamily,
                    reciter = reciterById(reciterId) ?: reciters.first(),
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
                    reader = reader,
                    editionId = editionId,
                    surahDisplayName = surah.localizedName(language),
                    surahAyahs = ayahs,
                    quranTextSize = quranTextSize,
                    translationTextSize = translationTextSize,
                    lineSpacing = lineSpacing,
                )
            }
            if (isActive) tafsirAyah?.let { ayah ->
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
            if (isActive) compareAyah?.let { ayah ->
                val reference = "${surah.localizedName(language)} ${ayah.surah}:${ayah.ayah}"
                CompareRiwayatSheet(
                    reader = reader,
                    ayah = ayah,
                    reference = reference,
                    quranFontFamily = quranFontFamily,
                    onDismiss = { compareAyah = null },
                )
            }
            if (isActive) studyAyah?.let { ayah ->
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
                modifier = Modifier.fillMaxSize().testTag("readerAyahList"),
                state = ayahListState,
            ) {
                items(
                    ayahs,
                    key = { "${it.surah}:${it.ayah}" },
                    contentType = { "ayah" },
                ) { ayah ->
                    val refKey = "${ayah.surah}:${ayah.ayah}"
                    val hidden = hideMode && refKey !in revealed
                    val isMemorized = refKey in memorized
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .testTag("readerAyah_${ayah.surah}_${ayah.ayah}")
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
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = quranTextSize.sp,
                                        lineHeight = (quranTextSize * lineSpacing).sp,
                                        textDirection = TextDirection.Rtl,
                                    ),
                                    fontFamily = quranFontFamily,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            } else {
                                TajweedAyahText(
                                    ayah = ayah,
                                    showTajweed = showTajweed,
                                    tajweedSpans = tajweedSpans,
                                    quranFontFamily = quranFontFamily,
                                    textSize = quranTextSize,
                                    lineSpacing = lineSpacing,
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
                                val memorizedLabel = stringResource(
                                    if (isMemorized) R.string.memorizedState else R.string.notMemorizedState,
                                )
                                IconButton(
                                    onClick = {
                                        memorized = MemorizationPrefs.toggle(
                                            context,
                                            ayah.surah,
                                            ayah.ayah,
                                        )
                                    },
                                    modifier = Modifier.semantics { stateDescription = memorizedLabel },
                                ) {
                                    Icon(
                                        imageVector = if (isMemorized) {
                                            Icons.Default.CheckCircle
                                        } else {
                                            Icons.Default.RadioButtonUnchecked
                                        },
                                        contentDescription = stringResource(
                                            if (isMemorized) R.string.unmarkAyahMemorized else R.string.markAyahMemorized,
                                        ),
                                        tint = if (isMemorized) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                    )
                                }
                            }
                            translation["${ayah.surah}:${ayah.ayah}"]?.let { translated ->
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = readerTranslationLabel(translationId),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    // Selectable (long-press) while the Arabic stays
                                    // tap-only: taps anywhere on the card still open
                                    // the action sheet via the Card clickable.
                                    SelectionContainer {
                                        ReaderTranslationText(translated, translationTextSize, lineSpacing)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun readerModeFromName(name: String): ReaderMode =
    ReaderMode.values().firstOrNull { it.name == name } ?: ReaderMode.READ

@Composable
private fun readerModeLabel(mode: ReaderMode): String = when (mode) {
    ReaderMode.READ -> stringResource(R.string.readerModeRead)
    ReaderMode.STUDY -> stringResource(R.string.readerModeStudy)
    ReaderMode.MEMORIZE -> stringResource(R.string.readerModeMemorize)
    ReaderMode.LISTEN -> stringResource(R.string.readerModeListen)
}

@Composable
private fun readerModeHint(mode: ReaderMode): String = when (mode) {
    ReaderMode.READ -> stringResource(R.string.readerModeHintRead)
    ReaderMode.STUDY -> stringResource(R.string.readerModeHintStudy)
    ReaderMode.MEMORIZE -> stringResource(R.string.readerModeHintMemorize)
    ReaderMode.LISTEN -> stringResource(R.string.readerModeHintListen)
}

@Composable
private fun ReaderModeHintCard(
    mode: ReaderMode,
    language: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Text(
            text = readerModeHint(mode),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun readerTranslationLabel(id: String): String = stringResource(
    if (id == "fr-hamidullah") R.string.translationFrench else R.string.translationEnglish,
)

/** Translation direction follows the source language, independent of the UI locale. */
@Composable
internal fun ReaderTranslationText(
    text: String,
    textSize: Float,
    lineSpacing: Float,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = textSize.sp,
            lineHeight = (textSize * lineSpacing).sp,
            textDirection = TextDirection.Ltr,
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Left,
    )
}

@Composable
internal fun ReaderTranslationChoices(selectedId: String, onSelected: (String) -> Unit) {
    Column {
        listOf("en-sahih", "fr-hamidullah").forEach { id ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = selectedId == id, role = Role.RadioButton, onClick = { onSelected(id) })
                    .heightIn(min = 48.dp)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selectedId == id, onClick = null)
                Text(readerTranslationLabel(id), modifier = Modifier.weight(1f).padding(start = 12.dp))
            }
        }
    }
}

@Composable
internal fun ReaderStyleControls(
    quranTextSize: Float,
    translationTextSize: Float,
    lineSpacing: Float,
    onStyleChanged: (Float, Float, Float) -> Unit,
    quranFontFamily: FontFamily?,
    previewAyah: String,
    previewTranslation: String? = null,
    onStyleChangeFinished: () -> Unit = {},
) {
    val arabicSizeLabel = stringResource(R.string.arabicTextSize)
    val translationSizeLabel = stringResource(R.string.translationTextSize)
    val spacingLabel = stringResource(R.string.readerLineSpacing)
    val spacingValue = ((lineSpacing * 100).roundToInt() / 100f).toString()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$arabicSizeLabel · ${quranTextSize.toInt()}", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = quranTextSize.coerceIn(20f, 44f),
            onValueChange = { onStyleChanged(it, translationTextSize, lineSpacing) },
            onValueChangeFinished = onStyleChangeFinished,
            valueRange = 20f..44f,
            steps = 23,
            modifier = Modifier.testTag("readerArabicSize").semantics {
                contentDescription = arabicSizeLabel
                stateDescription = quranTextSize.toInt().toString()
            },
        )
        Text("$translationSizeLabel · ${translationTextSize.toInt()}", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = translationTextSize.coerceIn(12f, 28f),
            onValueChange = { onStyleChanged(quranTextSize, it, lineSpacing) },
            onValueChangeFinished = onStyleChangeFinished,
            valueRange = 12f..28f,
            steps = 15,
            modifier = Modifier.testTag("readerTranslationSize").semantics {
                contentDescription = translationSizeLabel
                stateDescription = translationTextSize.toInt().toString()
            },
        )
        Text("$spacingLabel · $spacingValue×", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = lineSpacing.coerceIn(1.3f, 2.2f),
            onValueChange = { onStyleChanged(quranTextSize, translationTextSize, it) },
            onValueChangeFinished = onStyleChangeFinished,
            valueRange = 1.3f..2.2f,
            modifier = Modifier.testTag("readerLineSpacing").semantics {
                contentDescription = spacingLabel
                stateDescription = spacingValue
            },
        )
        if (previewAyah.isNotBlank()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.readerStylePreview), style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = previewAyah,
                        fontFamily = quranFontFamily,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = quranTextSize.sp,
                            lineHeight = (quranTextSize * lineSpacing).sp,
                            textDirection = TextDirection.Rtl,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right,
                    )
                    if (!previewTranslation.isNullOrBlank()) {
                        ReaderTranslationText(previewTranslation, translationTextSize, lineSpacing)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MushafReaderScreen(
    initialSurah: Int = 1,
    initialPage: Int = 1,
    onBack: () -> Unit,
    isActive: Boolean = true,
) {
    BackHandler(enabled = isActive, onBack = onBack)
    val context = LocalContext.current
    val language = currentLanguage()
    var prefsState by remember(context) { mutableStateOf<ReaderPrefsState?>(null) }
    LaunchedEffect(context) {
        prefsState = withContext(Dispatchers.IO) { ReaderPrefs.load(context) }
    }
    val loadedPrefs = prefsState
    if (loadedPrefs == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val prefs = loadedPrefs
    val initialEditionId = remember(prefs) { prefs.readerEditionId() }
    val fontName = remember(prefs) { prefs.fontName }
    val numberStyle = remember(prefs) { prefs.ayahNumberStyleName }
    var confirmedHafsMushaf by rememberSaveable { mutableStateOf(false) }
    if (initialEditionId != "hafs-an-asim__uthmani" && !confirmedHafsMushaf) {
        if (isActive) AlertDialog(
            onDismissRequest = onBack,
            title = { Text(stringResource(R.string.mushafEditionTitle)) },
            text = {
                Text(stringResource(
                    R.string.mushafEditionMessage,
                    readerEditionLabel(readerEditions.first { it.id == initialEditionId }),
                ))
            },
            confirmButton = {
                TextButton(
                    onClick = { confirmedHafsMushaf = true },
                    modifier = Modifier.testTag("mushafEditionConfirm"),
                ) {
                    Text(stringResource(R.string.openHafsMushaf))
                }
            },
            dismissButton = {
                TextButton(onClick = onBack, modifier = Modifier.testTag("mushafEditionCancel")) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
        return
    }
    val reader = remember(context) { AndroidAssetReader(context) }
    val quranFontFamily = remember(context, fontName) {
        quranFontFamily(context, fontName)
    }
    val mushafReader = remember(context) { AndroidStores.mushafMetadataReader(context) }

    // Page mapping uses Hafs/Uthmani; other reading editions require the
    // explicit opt-in above and the saved reading edition is left intact.
    val editionId = "hafs-an-asim__uthmani"
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
    var editionLoaded by remember { mutableStateOf(false) }
    var editionLoadFailed by remember { mutableStateOf(false) }
    var metadataLoadFailed by remember { mutableStateOf(false) }
    var retryToken by remember { mutableIntStateOf(0) }
    var surahJumpDone by remember { mutableStateOf(false) }
    var showPageDialog by remember { mutableStateOf(false) }
    var pageInputText by remember { mutableStateOf("$safeInitialPage") }
    var selectedAyah by remember { mutableStateOf<Ayah?>(null) }
    var tafsirAyah by remember { mutableStateOf<Ayah?>(null) }
    var compareAyah by remember { mutableStateOf<Ayah?>(null) }
    var studyAyah by remember { mutableStateOf<Ayah?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(editionId, retryToken) {
        editionLoaded = false
        editionLoadFailed = false
        mushafEditionAyahs = withContext(Dispatchers.IO) {
            try {
                loadQuranEdition(reader, editionId = editionId).orEmpty()
            } catch (_: Exception) {
                emptyList()
            }
        }
        editionLoadFailed = mushafEditionAyahs.isEmpty()
        editionLoaded = true
    }

    LaunchedEffect(hasVerifiedPageMap, retryToken) {
        if (!hasVerifiedPageMap) return@LaunchedEffect
        metadataLoadFailed = false
        val meta = try {
            withContext(Dispatchers.IO) { mushafReader.load() }
        } catch (_: Exception) {
            metadataLoadFailed = true
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
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("mushafBack")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.surahs))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${stringResource(R.string.mushaf)} · ${stringResource(R.string.page)} $currentPage",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.activeEditionLabel, readerEditionLabel(readerEditions.first())),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                if (metadataLoadFailed || editionLoadFailed) {
                    EmptyVerifiedContent(onRetry = { retryToken += 1 })
                } else if (meta == null || !editionLoaded) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Pre-index pages once: previously every swipe filtered all
                    // 6236 ayahs (O(N) per page). Now O(N) once via pageOf groupBy.
                    val ayahsByPage = remember(mushafEditionAyahs, meta) {
                        mushafEditionAyahs.groupBy { meta.pageOf(it.surah, it.ayah) }
                    }
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("mushafPager"),
                        pageSpacing = 0.dp,
                        reverseLayout = true,
                    ) { pageIndex ->
                        val page = pageIndex + 1
                        MushafPage(
                            page = page,
                            ayahs = remember(page, ayahsByPage) {
                                ayahsByPage[page].orEmpty()
                            },
                            quranFontFamily = quranFontFamily,
                            onAyahClick = { selectedAyah = it },
                            mushafMetadata = meta,
                            numberStyle = numberStyle,
                            quranTextSize = prefs.quranTextSize,
                            lineSpacing = prefs.lineSpacing,
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
                        IconButton(
                            onClick = {
                                if (currentPage < 604) {
                                    scope.launch { pagerState.animateScrollToPage(currentPage) }
                                }
                            },
                            enabled = currentPage < 604,
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = stringResource(R.string.nextPage))
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

    if (isActive) selectedAyah?.let { ayah ->
        val reference = "${surahMetadata.first { it.number == ayah.surah }.localizedName(language)} ${ayah.surah}:${ayah.ayah}"
        AyahActionSheet(
            ayah = ayah,
            reference = reference,
            translation = null,
            quranFontFamily = quranFontFamily,
            reciter = reciterById(prefs.reciterId) ?: reciters.first(),
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
            reader = reader,
            editionId = editionId,
            surahDisplayName = surahMetadata.first { it.number == ayah.surah }.localizedName(language),
            quranTextSize = prefs.quranTextSize,
            translationTextSize = prefs.translationTextSize,
            lineSpacing = prefs.lineSpacing,
        )
    }
    if (isActive) tafsirAyah?.let { ayah ->
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
    if (isActive) compareAyah?.let { ayah ->
        val reference = "${surahMetadata.first { it.number == ayah.surah }.localizedName(language)} ${ayah.surah}:${ayah.ayah}"
        CompareRiwayatSheet(
            reader = reader,
            ayah = ayah,
            reference = reference,
            quranFontFamily = quranFontFamily,
            onDismiss = { compareAyah = null },
        )
    }
    if (isActive) studyAyah?.let { ayah ->
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

    if (isActive && showPageDialog) {
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun MushafPage(
    page: Int,
    ayahs: List<Ayah>,
    quranFontFamily: FontFamily?,
    onAyahClick: (Ayah) -> Unit,
    mushafMetadata: MushafMetadata,
    numberStyle: String = "arabicIndic",
    quranTextSize: Float = 26f,
    lineSpacing: Float = 1.65f,
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
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                InfoBadge("${stringResource(R.string.juz)} $juz")
                InfoBadge("${stringResource(R.string.hizb)} $hizb")
                InfoBadge("${stringResource(R.string.rub)} $rub")
                if (hasSajda) {
                    InfoBadge("۩ ${stringResource(R.string.sajda)}")
                }
            }
            HorizontalDivider()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .testTag("mushafPageContent_$page"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ayahs.forEach { ayah ->
                    val displayAyah = ayahNumberText(ayah.displayAyahNumber, numberStyle)
                    val ayahWithNumber = "${ayah.text} ﴿$displayAyah﴿ ${if (mushafMetadata.isSajda(ayah.surah, ayah.ayah)) "۩" else ""}"
                    val ayahDescription = stringResource(R.string.ayahReferenceAccessibility, ayah.surah, ayah.ayah)
                    Text(
                        text = ayahWithNumber,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = quranFontFamily,
                            textAlign = TextAlign.Right,
                            textDirection = TextDirection.Rtl,
                            fontSize = quranTextSize.sp,
                            lineHeight = (quranTextSize * lineSpacing).sp,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                role = Role.Button
                                contentDescription = ayahDescription
                            }
                            .testTag("mushafAyah_${ayah.surah}_${ayah.ayah}")
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
internal fun AyahActionSheet(
    ayah: Ayah,
    reference: String,
    translation: String?,
    quranFontFamily: FontFamily?,
    reciter: Reciter,
    onOpenTafsir: () -> Unit,
    onOpenCompare: () -> Unit,
    onOpenStudy: () -> Unit,
    onDismiss: () -> Unit,
    reader: AndroidAssetReader,
    editionId: String,
    surahDisplayName: String,
    surahAyahs: List<Ayah> = emptyList(),
    quranTextSize: Float = 26f,
    translationTextSize: Float = 16f,
    lineSpacing: Float = 1.65f,
) {
    val context = LocalContext.current
    val language = currentLanguage()
    val scope = rememberCoroutineScope()
    val audioPlayer = remember(context) {
        try {
            AndroidStores.audio(context)
        } catch (_: Exception) {
            null
        }
    }
    val refKey = "${ayah.surah}:${ayah.ayah}"
    // Media/lock-screen controls need POST_NOTIFICATIONS (Android 13+).
    // Playback works without it; the notification just stays hidden.
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }
    val audioAlbum = stringResource(R.string.quranAudio)
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
                .verticalScroll(rememberScrollState())
                .testTag("ayahActionSheetContent")
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
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = quranTextSize.sp,
                    lineHeight = (quranTextSize * lineSpacing).sp,
                    textDirection = TextDirection.Rtl,
                ),
                fontFamily = quranFontFamily,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
            )
            translation?.let {
                SelectionContainer {
                    ReaderTranslationText(it, translationTextSize, lineSpacing)
                }
            }
            var startingPlayback by remember(refKey) { mutableStateOf(false) }
            Button(
                onClick = {
                    scope.launch {
                        startingPlayback = true
                        try {
                            val player = audioPlayer
                                ?: throw IllegalStateException("audio unavailable")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notifPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS,
                                )
                            }
                            QuranAudio.ensureStarted(context)
                            // Continuous surah queue from the tapped ayah to
                            // the surah end (single surah — recitation flows,
                            // Next/Prev work, repeat ALL loops the surah).
                            // Reader passes its list; Mushaf loads on IO.
                            val list = if (surahAyahs.isNotEmpty()) {
                                surahAyahs
                            } else {
                                withContext(Dispatchers.IO) {
                                    try {
                                        loadQuranEdition(reader, editionId = editionId)
                                            ?.filter { it.surah == ayah.surah }
                                            .orEmpty()
                                    } catch (_: Exception) {
                                        emptyList()
                                    }
                                }
                            }
                            val startIdx = list
                                .indexOfFirst { it.surah == ayah.surah && it.ayah == ayah.ayah }
                                .coerceAtLeast(0)
                            val queue = list.drop(startIdx).map { a ->
                                // Local-first per ayah: downloaded file when
                                // present, otherwise the verified CDN URL.
                                val url = AudioFiles.localUrlOrNull(
                                    context,
                                    reciter.identifier,
                                    a.surah,
                                    a.ayah,
                                ) ?: reciter.fileUrl(
                                    globalAyahNumber(a.surah, a.ayah),
                                )
                                AyahAudioItem(
                                    url = url,
                                    refKey = "${a.surah}:${a.ayah}",
                                    title = "$surahDisplayName ${a.surah}:${a.ayah}",
                                    artist = reciter.localizedName(language),
                                    album = audioAlbum,
                                    editionId = editionId,
                                )
                            }
                            if (queue.isEmpty()) throw IllegalStateException("empty queue")
                            player.setAyahSources(queue)
                            val savedSpeed = withContext(Dispatchers.IO) {
                                AudioPrefs.speed(context)
                            }
                            player.setSpeed(savedSpeed.toFloat())
                            player.play()
                            NowPlaying.playQueue(queue)
                            onDismiss()
                            NowPlaying.requestOpen()
                        } catch (_: Exception) {
                            android.widget.Toast.makeText(
                                context,
                                context.getString(R.string.downloadFailed),
                                android.widget.Toast.LENGTH_SHORT,
                            ).show()
                        } finally {
                            startingPlayback = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ayahSheetPlay"),
                enabled = !startingPlayback,
            ) {
                Text(stringResource(R.string.play))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                text = stringResource(R.string.readerModeStudy),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = onOpenTafsir,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ayahSheetTafsir"),
            ) {
                Text(stringResource(R.string.tafsir))
            }
            TextButton(
                onClick = onOpenCompare,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ayahSheetCompare"),
            ) {
                Text(stringResource(R.string.compareRiwayat))
            }
            TextButton(
                onClick = onOpenStudy,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ayahSheetStudy"),
            ) {
                Text(stringResource(R.string.wordMeanings))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                text = stringResource(R.string.share),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = {
                    AndroidClipboard(context).copy(shareText)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ayahSheetCopy"),
            ) {
                Text(stringResource(R.string.copy))
            }
            Button(
                onClick = {
                    AndroidSharer(context).shareText(shareText, reference)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ayahSheetShare"),
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
internal fun TafsirSheet(
    reader: AndroidAssetReader,
    ayah: Ayah,
    reference: String,
    quranFontFamily: FontFamily?,
    initialTafsirId: String? = null,
    highlightQuery: String = "",
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var selectedTafsir by rememberSaveable(ayah.surah, ayah.ayah) {
        mutableStateOf(
            tafsirOptions.firstOrNull { it.id == initialTafsirId }?.id
                ?: tafsirOptions.first().id,
        )
    }
    LaunchedEffect(context, initialTafsirId) {
        if (initialTafsirId == null) {
            val preferred = withContext(Dispatchers.IO) { ReaderPrefs.load(context).tafsirId }
            selectedTafsir = tafsirOptions.firstOrNull { it.id == preferred }?.id ?: tafsirOptions.first().id
        }
    }
    // JSON parse off Main: per-surah tafsir read blocked sheet open.
    var tafsirEntries by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var tafsirLoaded by remember { mutableStateOf(false) }
    var retryToken by remember { mutableIntStateOf(0) }
    LaunchedEffect(reader, selectedTafsir, ayah.surah, retryToken) {
        tafsirLoaded = false
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
        tafsirLoaded = true
    }
    val tafsirText = tafsirEntries[ayah.ayah]

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tafsirContent"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = reference,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            item {
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
            }
            item {
                if (!tafsirLoaded) {
                    Box(Modifier.fillMaxWidth().height(96.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (tafsirText.isNullOrBlank()) {
                    EmptyVerifiedContent(onRetry = { retryToken += 1 })
                } else {
                    SelectionContainer {
                        Text(
                            text = highlightedText(tafsirText, highlightQuery),
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Rtl),
                            fontFamily = quranFontFamily,
                            textAlign = TextAlign.Right,
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
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
    var rowsLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(reader, ayah.surah, ayah.ayah) {
        rowsLoaded = false
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
        rowsLoaded = true
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.compareRiwayat),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            item {
                Text(
                    text = reference,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!rowsLoaded) {
                item { CircularProgressIndicator() }
            } else if (rows.isEmpty()) {
                item { EmptyVerifiedContent() }
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
                                style = MaterialTheme.typography.titleLarge.copy(textDirection = TextDirection.Rtl),
                                fontFamily = quranFontFamily,
                                textAlign = TextAlign.Right,
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
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
    var wordsLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(reader, ayah.surah, ayah.ayah, hafsOnly) {
        wordsLoaded = false
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
        wordsLoaded = true
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text(
                text = stringResource(R.string.wordMeanings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            ) }
            item { Text(
                text = reference,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ) }
            item { Text(
                text = stringResource(R.string.wordMorphologyHint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ) }
                if (!wordsLoaded) {
                    item { CircularProgressIndicator() }
                } else if (words.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.datasetUnavailable),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    itemsIndexed(words, key = { index, word -> "$index:${word.word}" }) { _, word ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = word.word,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.titleLarge.copy(textDirection = TextDirection.Rtl),
                                    fontFamily = quranFontFamily,
                                    textAlign = TextAlign.Right,
                                )
                                val details = listOf(
                                    word.lemma.takeIf { it.isNotBlank() },
                                    word.root.takeIf { it.isNotBlank() }?.let { "√$it" },
                                    word.pos.takeIf { it.isNotBlank() },
                                ).filterNotNull()
                                if (details.isNotEmpty()) {
                                    Text(
                                        text = details.joinToString(" · "),
                                        modifier = Modifier.fillMaxWidth(),
                                        style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.ContentOrRtl),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Right,
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

@Composable
private fun NumberGrid(
    range: IntRange,
    label: String,
    onSelected: (Int) -> Unit = {},
) {
    val numbers = remember(range) { range.toList() }
    val step = remember(range) {
        when (range.last) {
            30 -> 5
            60 -> 10
            240 -> 40
            else -> 100
        }
    }
    val badges = remember(range, step) { range.step(step).toList() }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(badges, key = { it }, contentType = { "badge" }) { start ->
                InfoBadge("$start-${minOf(start + step - 1, range.last)}")
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(72.dp),
            modifier = Modifier.fillMaxWidth().weight(1f, fill = true),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = numbers,
                key = { it },
                contentType = { "number" },
            ) { number ->
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
