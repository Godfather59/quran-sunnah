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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
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

@Composable
fun SunnahHomeScreen(isActive: Boolean = true) {
    val context = LocalContext.current
    val language = currentLanguage()
    val reader = remember(context) { AndroidAssetReader(context) }
    val engine = remember { AndroidStores.searchEngine() }
    var collectionId by rememberSaveable { mutableStateOf("all") }
    // Rotation-safe selection: persist only the stable book key
    // ("collectionId:sectionNumber"), then resolve to the loaded book object.
    // Previously `remember(collectionId)` dropped the open book on rotation.
    var selectedBookKey by rememberSaveable(collectionId) { mutableStateOf<String?>(null) }
    // Save only source coordinates. Exact text and the reading sequence are
    // reloaded from the verified section after rotation or pager disposal.
    var selectedHadithId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedHadithCollectionId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedHadithSection by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedHadith by remember { mutableStateOf<Hadith?>(null) }
    var fullReader by rememberSaveable { mutableStateOf(false) }
    var readerSequence by remember { mutableStateOf<List<Hadith>>(emptyList()) }
    val readerListState = rememberLazyListState()
    var readerScrollHadithId by rememberSaveable { mutableStateOf<String?>(null) }
    var readerLoadFailed by remember { mutableStateOf(false) }
    var readerRetryToken by remember { mutableIntStateOf(0) }
    LaunchedEffect(selectedHadithId) {
        if (readerScrollHadithId != selectedHadithId) {
            readerListState.scrollToItem(0)
            readerScrollHadithId = selectedHadithId
        }
    }
    var hadiths by remember(selectedBookKey) { mutableStateOf<List<Hadith>>(emptyList()) }
    val isAllMode = collectionId == "all"
    val selectedCollectionTab = if (isAllMode) {
        0
    } else {
        (hadithCollections.indexOfFirst { it.id == collectionId } + 1).coerceAtLeast(0)
    }
    var showFilter by remember { mutableStateOf(false) }

    fun closeHadith() {
        showFilter = false
        fullReader = false
        selectedHadithId = null
        selectedHadithCollectionId = null
        selectedHadithSection = null
        selectedHadith = null
        readerSequence = emptyList()
        readerLoadFailed = false
    }
    BackHandler(enabled = isActive && selectedBookKey != null && selectedHadithId == null && !showFilter) {
        selectedBookKey = null
    }

    val collection = remember(collectionId) {
        hadithCollections.firstOrNull { it.id == collectionId }
            ?: hadithCollections.first()
    }
    // index.json + section JSON parse off Main: Bukhari-scale files blocked tab switch.
    // Skipped in "All rawis" mode (no single index.json exists for "all").
    var index by remember { mutableStateOf<com.godfather59.quransunnah.data.HadithIndex?>(null) }
    var indexLoading by remember { mutableStateOf(true) }
    LaunchedEffect(reader, collectionId, isActive) {
        if (!isActive) return@LaunchedEffect
        if (collectionId == "all") {
            index = null
            indexLoading = false
            return@LaunchedEffect
        }
        indexLoading = true
        index = HadithCatalogCache.index(reader, collectionId)
        indexLoading = false
    }
    val books = remember(index) {
        index?.sections?.filter { it.count > 0 }.orEmpty()
    }

    // "All rawis" directory: every book of every bundled collection,
    // loaded once from the small index files (section bodies stay lazy).
    val cachedAllBooks = remember { HadithCatalogCache.peekAllBooks().orEmpty() }
    var allBooks by remember { mutableStateOf(cachedAllBooks) }
    var allBooksLoading by remember { mutableStateOf(cachedAllBooks.isEmpty()) }
    LaunchedEffect(reader, isAllMode, isActive) {
        if (!isActive) return@LaunchedEffect
        if (!isAllMode || allBooks.isNotEmpty()) return@LaunchedEffect
        allBooksLoading = true
        allBooks = HadithCatalogCache.allBooks(reader)
        allBooksLoading = false
    }

    fun bookKey(book: CollectionBook): String = "${book.collectionId}:${book.section.section}"
    val singleBooks = remember(books, collectionId) {
        books.map { CollectionBook(collectionId, it) }
    }
    val selectedBook: CollectionBook? = remember(selectedBookKey, singleBooks, allBooks) {
        val key = selectedBookKey ?: return@remember null
        (singleBooks + allBooks).firstOrNull { bookKey(it) == key }
    }

    val openHadith: (Hadith, String) -> Unit = { hadith, _ ->
        showFilter = false
        fullReader = false
        selectedHadithId = hadith.id
        selectedHadithCollectionId = hadith.collectionId
        selectedHadith = hadith
        readerLoadFailed = false
        val existingBook = selectedBook?.takeIf {
            it.collectionId == hadith.collectionId && hadiths.any { row -> row.id == hadith.id }
        }
        selectedHadithSection = existingBook?.section?.section
        readerSequence = if (existingBook != null) hadiths else emptyList()
    }
    LaunchedEffect(reader, selectedHadithId, selectedHadithCollectionId, readerRetryToken, isActive) {
        if (!isActive) return@LaunchedEffect
        val id = selectedHadithId ?: return@LaunchedEffect
        val source = selectedHadithCollectionId ?: run {
            readerLoadFailed = true
            return@LaunchedEffect
        }
        if (selectedHadith?.id == id && selectedHadith?.collectionId == source &&
            readerSequence.any { it.id == id && it.collectionId == source }
        ) return@LaunchedEffect
        readerLoadFailed = false
        val sectionHint = selectedHadithSection
        val restored = withContext(Dispatchers.IO) {
            try {
                loadHadithReaderRoute(reader, source, id, sectionHint)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            }
        }
        if (restored != null) {
            selectedHadith = restored.hadith
            readerSequence = restored.sequence
            selectedHadithSection = restored.section
        } else {
            readerLoadFailed = true
        }
    }
    val readingHadith = selectedHadith?.takeIf {
        it.id == selectedHadithId && it.collectionId == selectedHadithCollectionId
    }
    if (selectedHadithId != null && (readingHadith == null || (fullReader && readerSequence.isEmpty()))) {
        BackHandler(enabled = isActive) { closeHadith() }
        Column(
            Modifier.fillMaxSize().statusBarsPadding().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { closeHadith() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Text(stringResource(R.string.sunnah), style = MaterialTheme.typography.titleLarge)
            }
            if (readerLoadFailed) {
                EmptyVerifiedContent(onRetry = { readerRetryToken += 1 })
            } else {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        return
    }
    if (fullReader && readingHadith != null) {
        HadithReadingScreen(readingHadith,
            hadithCollections.firstOrNull { it.id == readingHadith.collectionId } ?: collection,
            sequence = readerSequence,
            listState = readerListState,
            isActive = isActive, onBack = { closeHadith() },
            onSelect = {
                selectedHadith = it
                selectedHadithId = it.id
                selectedHadithCollectionId = it.collectionId
            })
        return
    }

    EmeraldScaffold(
        header = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.sunnah),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = { showFilter = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                ) {
                    Text(stringResource(R.string.filters))
                }
            }
        },
        body = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 14.dp, bottom = 8.dp)
                    .testTag("sunnahHome"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
        ScrollableTabRow(
            selectedTabIndex = selectedCollectionTab,
            edgePadding = 0.dp,
            modifier = Modifier.testTag("sunnahCollections"),
        ) {
            Tab(
                selected = isAllMode,
                onClick = {
                    collectionId = "all"
                    selectedBookKey = null
                    closeHadith()
                },
                text = { Text(stringResource(R.string.all), maxLines = 1) },
            )
            hadithCollections.forEach { item ->
                Tab(
                    selected = item.id == collectionId,
                    onClick = {
                        collectionId = item.id
                        selectedBookKey = null
                        closeHadith()
                    },
                    text = { Text(item.localizedName(language), maxLines = 1) },
                )
            }
        }

        val openedBook = selectedBook
        if (openedBook != null) {
            val section = openedBook.section
            val openedCollectionId = openedBook.collectionId
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { selectedBookKey = null }) {
                    Text(stringResource(R.string.booksChapters))
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = (hadithCollections
                        .firstOrNull { it.id == openedCollectionId }
                        ?.localizedName(language).orEmpty()) + " · " +
                        bookDisplayTitle(
                            openedCollectionId,
                            section.section,
                            section.title,
                            language,
                            section.first,
                            section.last,
                        ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    modifier = Modifier.weight(1f),
                )
            }
            var hadithsLoading by remember(openedBook) { mutableStateOf(true) }
            LaunchedEffect(reader, openedBook, isActive) {
                if (!isActive) return@LaunchedEffect
                hadithsLoading = true
                hadiths = withContext(Dispatchers.IO) {
                    try {
                        reader.readText(
                            "assets/hadith/$openedCollectionId/sections/${section.section}.json",
                        )?.let { parseHadithSection(openedCollectionId, section.title, it) }
                            .orEmpty()
                    } catch (_: Exception) {
                        emptyList()
                    }
                }
                hadithsLoading = false
            }
            if (hadithsLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (hadiths.isEmpty()) {
                EmptyVerifiedContent()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(
                        items = hadiths,
                        key = { it.id },
                        contentType = { "hadith" },
                    ) { hadith ->
                        HadithRow(
                            hadith = hadith,
                            onClick = { openHadith(hadith, section.title) },
                        )
                    }
                }
            }
        } else if (isAllMode) {
            if (allBooksLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                SunnahSearchAndBooks(
                    context = context,
                    reader = reader,
                    engine = engine,
                    resetToken = "all",
                    collectionIds = null,
                    onOpenHadith = openHadith,
                    books = allBooks,
                    onBookClick = { selectedBookKey = bookKey(it) },
                )
            }
        } else if (indexLoading) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
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
        } else {
            SunnahSearchAndBooks(
                context = context,
                reader = reader,
                engine = engine,
                resetToken = collectionId,
                collectionIds = setOf(collectionId),
                onOpenHadith = openHadith,
                books = singleBooks,
                onBookClick = { selectedBookKey = bookKey(it) },
            )
            }
            }
        },
    )

    if (isActive) readingHadith?.let { hadith ->
        HadithReaderSheet(
            hadith = hadith,
            collection = hadithCollections.firstOrNull { it.id == hadith.collectionId }
                ?: collection,
            onDismiss = { closeHadith() },
            onOpenFullReader = { showFilter = false; fullReader = true },
        )
    }

    if (isActive && showFilter) {
        HadithFilterSheet(
            collectionId = collectionId,
            onOpenHadith = { openHadith(it, it.book) },
            onDismiss = { showFilter = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HadithFilterSheet(
    collectionId: String,
    onOpenHadith: (Hadith) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val reader = remember(context) { AndroidAssetReader(context) }
    val engine = remember { AndroidStores.searchEngine() }
    val state = rememberHadithFilterState(reader, engine, collectionId)

    val query by state.query.collectAsState()
    val bookQuery by state.bookQuery.collectAsState()
    val numberQuery by state.numberQuery.collectAsState()
    val gradeFilter by state.gradeFilter.collectAsState()
    val indexReady by state.indexReady.collectAsState()
    val hits by state.hits.collectAsState()
    val searched by state.searched.collectAsState()

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
                onValueChange = { state.onQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.searchHint)) },
                enabled = indexReady,
            )
            TextField(
                value = bookQuery,
                onValueChange = { state.onBookQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.book)) },
            )
            TextField(
                value = numberQuery,
                onValueChange = { state.onNumberQueryChange(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.hadithNumber)) },
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.gradeOptions, key = { it }) { grade ->
                    FilterChip(
                        selected = grade in gradeFilter,
                        onClick = { state.toggleGrade(grade) },
                        label = { Text(grade) },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { state.clearAll() },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.deselectAll))
                }
                Button(
                    onClick = { state.applyFilters() },
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
                        modifier = Modifier.testTag("hadithFilterEmpty"),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .testTag("hadithFilterResults"),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(hits, key = { it.id }) { hadith ->
                            HadithRow(
                                hadith = hadith,
                                onClick = { onOpenHadith(hadith) },
                                highlight = query,
                            )
                        }
                    }
                }
            } else if (state.gradeOptions.isNotEmpty()) {
                Text(
                    text = "${state.gradeOptions.size} ${stringResource(R.string.booksChapters)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** One book in the "All rawis" directory: which collection it belongs to. */
private data class CollectionBook(
    val collectionId: String,
    val section: HadithSection,
)

/** Process cache for small hadith indexes and the combined all-collections directory. */
private object HadithCatalogCache {
    private val indexMutex = kotlinx.coroutines.sync.Mutex()
    private val allBooksMutex = kotlinx.coroutines.sync.Mutex()

    @Volatile
    private var indexes: Map<String, com.godfather59.quransunnah.data.HadithIndex> = emptyMap()

    @Volatile
    private var cachedAllBooks: List<CollectionBook>? = null

    suspend fun index(
        reader: AndroidAssetReader,
        collectionId: String,
    ): com.godfather59.quransunnah.data.HadithIndex? {
        indexes[collectionId]?.let { return it }
        return indexMutex.withLock {
            indexes[collectionId]?.let { return@withLock it }
            val loaded = withContext(Dispatchers.IO) {
                try {
                    reader.readText("assets/hadith/$collectionId/index.json")
                        ?.let(::parseHadithIndex)
                } catch (_: Exception) {
                    null
                }
            }
            if (loaded != null) indexes = indexes + (collectionId to loaded)
            loaded
        }
    }

    suspend fun allBooks(reader: AndroidAssetReader): List<CollectionBook> {
        cachedAllBooks?.let { return it }
        return allBooksMutex.withLock {
            cachedAllBooks?.let { return@withLock it }
            val books = buildList {
                for (collection in hadithCollections) {
                    val index = index(reader, collection.id) ?: continue
                    index.sections
                        .filter { it.count > 0 }
                        .forEach { add(CollectionBook(collection.id, it)) }
                }
            }
            if (books.isNotEmpty()) cachedAllBooks = books
            books
        }
    }

    fun peekAllBooks(): List<CollectionBook>? = cachedAllBooks
}

/**
 * Word search field + Go button + results, shared by the single-collection
 * view and the "All rawis" directory. [collectionIds] null searches every
 * bundled collection; [books] + [onBookClick] render the idle book list.
 */
@Composable
private fun ColumnScope.SunnahSearchAndBooks(
    context: Context,
    reader: AndroidAssetReader,
    engine: SearchEngine,
    resetToken: Any,
    collectionIds: Set<String>?,
    onOpenHadith: (Hadith, String) -> Unit,
    books: List<CollectionBook>,
    onBookClick: (CollectionBook) -> Unit,
) {
    val searchState = rememberSunnahSearchState(reader, engine, collectionIds, resetToken)
    val wordInput by searchState.input.collectAsState()
    val wordHits by searchState.hits.collectAsState()
    val wordSearching by searchState.searching.collectAsState()
    val wordSearched by searchState.searched.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            value = wordInput,
            onValueChange = { searchState.onInputChange(it) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(
                onSearch = { searchState.submit(wordInput) },
            ),
            modifier = Modifier.weight(1f),
            singleLine = true,
            label = { Text(stringResource(R.string.search)) },
        )
        Button(
            onClick = { searchState.submit(wordInput) },
            enabled = !wordSearching && wordInput.trim().isNotEmpty(),
        ) {
            Text(stringResource(R.string.go))
        }
    }
    if (wordSearching) {
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else if (wordSearched) {
        if (wordHits.isEmpty()) {
            Text(
                text = stringResource(R.string.noHadithMatches),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(
                    items = wordHits,
                    key = { it.id },
                    contentType = { "hadith" },
                ) { hadith ->
                    HadithRow(
                        hadith = hadith,
                        onClick = { onOpenHadith(hadith, hadith.book) },
                        highlight = wordInput,
                    )
                }
            }
        }
    } else {
        val mixedCollections = remember(books) {
            books.map { it.collectionId }.toSet().size > 1
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(
                items = books,
                key = { it.collectionId + ":" + it.section.section },
                contentType = { "book" },
            ) { book ->
                HadithBookRow(
                    section = book.section,
                    collectionId = book.collectionId,
                    tag = if (mixedCollections) {
                        hadithCollections
                            .firstOrNull { it.id == book.collectionId }
                            ?.localizedName(currentLanguage())
                    } else {
                        null
                    },
                    onClick = { onBookClick(book) },
                )
            }
        }
    }
}

@Composable
private fun HadithBookRow(
    section: HadithSection,
    collectionId: String,
    onClick: () -> Unit,
    tag: String? = null,
) {
    val language = currentLanguage()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hadithBook_${collectionId}_${section.section}")
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
                    text = bookDisplayTitle(
                        collectionId,
                        section.section,
                        section.title,
                        language,
                        section.first,
                        section.last,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                )
                Text(
                    text = "${section.first}–${section.last} · ${section.count} ${stringResource(R.string.hadithCountUnit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (tag != null) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun HadithRow(
    hadith: Hadith,
    onClick: () -> Unit,
    highlight: String = "",
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hadithRow_${hadith.id}")
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
                    text = highlightedText(hadith.matnAr, highlight),
                    style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Rtl),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HadithReaderSheet(
    hadith: Hadith,
    collection: HadithCollection,
    onDismiss: () -> Unit,
    onOpenFullReader: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val language = currentLanguage()
    val reference = "${collection.localizedName(language)} · № ${hadith.hadithNumber}"
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
            if (onOpenFullReader != null) {
                item {
                    Button(onClick = onOpenFullReader, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.openFullReader))
                    }
                }
            }
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
                        style = MaterialTheme.typography.bodySmall.copy(textDirection = TextDirection.Rtl),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(),
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
                        style = MaterialTheme.typography.titleLarge.copy(textDirection = TextDirection.Rtl),
                        textAlign = TextAlign.Right,
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
