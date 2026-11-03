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

private val searchIndexMutex = kotlinx.coroutines.sync.Mutex()
@Volatile
private var searchIndexComplete = false

private const val SEARCH_INDEX_REVISION = "search-index-v1"

private fun searchIndexFingerprint(reader: AndroidAssetReader, kind: SearchKind): String? {
    val raw = reader.readText("assets/integrity_manifest.json") ?: return null
    val manifest = try {
        com.godfather59.quransunnah.integrity.parseIntegrityManifest(raw)
    } catch (_: Exception) {
        return null
    }
    val paths: (String) -> Boolean = when (kind) {
        SearchKind.QURAN -> {
            val path = com.godfather59.quransunnah.data.verifiedQuranAssets["hafs-an-asim__uthmani"]
                ?: return null
            { candidate -> candidate == path }
        }
        SearchKind.HADITH -> { candidate -> candidate.startsWith("assets/hadith/") }
        SearchKind.TAFSIR -> { candidate -> candidate.startsWith("assets/quran/tafsir/jalalayn/") }
        SearchKind.SURAH -> return null
    }
    val entries = manifest.files.filterKeys(paths).toSortedMap()
    if (entries.isEmpty()) return null
    val material = buildString {
        append(SEARCH_INDEX_REVISION).append('\n')
        append(kind.name).append('\n')
        entries.forEach { (path, digest) -> append(path).append('=').append(digest).append('\n') }
    }
    return com.godfather59.quransunnah.crypto.Sha256.hex(material.encodeToByteArray())
}

private fun indexMetaKey(kind: SearchKind) = "search.$SEARCH_INDEX_REVISION.${kind.name.lowercase()}"

private suspend fun restorePersistedCategory(
    context: Context,
    reader: AndroidAssetReader,
    engine: SearchEngine,
    kind: SearchKind,
): Boolean {
    val fingerprint = withContext(Dispatchers.IO) { searchIndexFingerprint(reader, kind) } ?: return false
    val docs = withContext(Dispatchers.IO) {
        val store = AndroidStores.searchIndexStore(context)
        if (store.getFingerprint(indexMetaKey(kind)) != fingerprint) {
            emptyList()
        } else {
            store.documents(kind.name.lowercase())
        }
    }
    if (docs.isEmpty()) return false
    withContext(Dispatchers.Default) { engine.restoreIndexedDocuments(docs) }
    return engine.hasDocuments(kind)
}

private suspend fun persistCategory(
    context: Context,
    reader: AndroidAssetReader,
    engine: SearchEngine,
    kind: SearchKind,
) {
    val fingerprint = withContext(Dispatchers.IO) { searchIndexFingerprint(reader, kind) } ?: return
    val documents = withContext(Dispatchers.Default) { engine.exportIndexedDocuments(kind) }
    if (documents.isEmpty()) return
    withContext(Dispatchers.IO) {
        AndroidStores.searchIndexStore(context).replaceIndex(
            metaKey = indexMetaKey(kind),
            fingerprint = fingerprint,
            kind = kind.name.lowercase(),
            documents = documents,
        )
    }
}

private suspend fun indexQuranIfNeeded(
    reader: AndroidAssetReader,
    engine: SearchEngine,
) {
    if (!engine.hasDocuments(SearchKind.QURAN)) {
        withContext(Dispatchers.IO) {
            loadQuranEdition(reader, editionId = "hafs-an-asim__uthmani")
                ?.let { engine.indexQuran(it) }
        }
    }
}

private suspend fun indexHadithIfNeeded(
    reader: AndroidAssetReader,
    engine: SearchEngine,
) {
    if (!engine.hasDocuments(SearchKind.HADITH)) {
        val hadiths = withContext(Dispatchers.IO) {
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
            all
        }
        withContext(Dispatchers.IO) {
            engine.indexHadith(hadiths, hadithCollections.associate { it.id to it.nameEn })
        }
    }
}

private suspend fun indexTafsirIfNeeded(
    reader: AndroidAssetReader,
    engine: SearchEngine,
) {
    if (!engine.hasDocuments(SearchKind.TAFSIR)) {
        val tafsir = withContext(Dispatchers.IO) {
            val entries = mutableListOf<TafsirEntry>()
            for (surah in 1..114) {
                val verses = try {
                    loadTafsirSurah(reader, "jalalayn", surah)
                } catch (_: Exception) {
                    null
                } ?: continue
                for ((ayah, text) in verses) {
                    entries.add(TafsirEntry(surah, ayah, "jalalayn", "Jalalayn", text))
                }
            }
            entries
        }
        withContext(Dispatchers.IO) { engine.indexTafsir(tafsir, "Jalalayn", "Jalalayn") }
    }
}

internal suspend fun ensureHadithIndex(
    reader: AndroidAssetReader,
    engine: SearchEngine,
    context: Context,
) {
    if (engine.hasDocuments(SearchKind.HADITH)) return
    searchIndexMutex.withLock {
        if (engine.hasDocuments(SearchKind.HADITH)) return@withLock
        if (!restorePersistedCategory(context, reader, engine, SearchKind.HADITH)) {
            indexHadithIfNeeded(reader, engine)
            persistCategory(context, reader, engine, SearchKind.HADITH)
        }
    }
}

private suspend fun ensureSearchIndex(
    reader: AndroidAssetReader,
    engine: SearchEngine,
    context: Context,
    onProgress: (Boolean) -> Unit,
) {
    if (searchIndexComplete) {
        onProgress(true)
        return
    }
    searchIndexMutex.withLock {
        if (searchIndexComplete) {
            onProgress(true)
            return@withLock
        }
        if (!engine.hasDocuments(SearchKind.QURAN) &&
            !restorePersistedCategory(context, reader, engine, SearchKind.QURAN)
        ) {
            indexQuranIfNeeded(reader, engine)
            persistCategory(context, reader, engine, SearchKind.QURAN)
        }
        withContext(Dispatchers.Main.immediate) { onProgress(false) }
        if (!engine.hasDocuments(SearchKind.HADITH) &&
            !restorePersistedCategory(context, reader, engine, SearchKind.HADITH)
        ) {
            indexHadithIfNeeded(reader, engine)
            persistCategory(context, reader, engine, SearchKind.HADITH)
        }
        withContext(Dispatchers.Main.immediate) { onProgress(false) }
        if (!engine.hasDocuments(SearchKind.TAFSIR) &&
            !restorePersistedCategory(context, reader, engine, SearchKind.TAFSIR)
        ) {
            indexTafsirIfNeeded(reader, engine)
            persistCategory(context, reader, engine, SearchKind.TAFSIR)
        }
        searchIndexComplete = true
        withContext(Dispatchers.Main.immediate) { onProgress(true) }
    }
}

@Composable
fun SearchScreen(
    onOpenAyahRef: (String) -> Unit = {},
    onBack: () -> Unit = {},
    isActive: Boolean = true,
    // Tests can exercise recovery with an isolated real engine and an asset
    // loader that fails before retrying. Production always uses these defaults.
    searchEngine: SearchEngine? = null,
    loadIndex: suspend (AndroidAssetReader, SearchEngine, Context, (Boolean) -> Unit) -> Unit = ::ensureSearchIndex,
) {
    val context = LocalContext.current
    val language = currentLanguage()
    val reader = remember(context) { AndroidAssetReader(context) }
    val engine = remember(searchEngine) { searchEngine ?: AndroidStores.searchEngine() }
    var query by rememberSaveable { mutableStateOf("") }
    var debounced by rememberSaveable { mutableStateOf("") }
    var indexReady by remember { mutableStateOf(engine.hasDocuments(SearchKind.QURAN)) }
    var indexComplete by remember { mutableStateOf(false) }
    var indexRevision by remember { mutableIntStateOf(0) }
    var indexFailed by remember { mutableStateOf(false) }
    var retryIndex by remember { mutableIntStateOf(0) }
    var resultsQuery by remember { mutableStateOf<String?>(null) }
    var searching by remember { mutableStateOf(false) }
    var searchFailed by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf(SearchResults()) }
    var selectedHadith by remember { mutableStateOf<Hadith?>(null) }
    var selectedTafsir by remember { mutableStateOf<com.godfather59.quransunnah.search.SearchHit?>(null) }
    var resultLimit by rememberSaveable(debounced) { mutableIntStateOf(20) }
    val resultListState = rememberLazyListState()
    var selectedCategoryName by rememberSaveable { mutableStateOf(SearchCategory.ALL.name) }
    val selectedCategory = remember(selectedCategoryName) {
        SearchCategory.values().firstOrNull { it.name == selectedCategoryName }
            ?: SearchCategory.ALL
    }

    LaunchedEffect(reader, engine, loadIndex, isActive, retryIndex) {
        if (!isActive) return@LaunchedEffect
        indexFailed = false
        try {
            loadIndex(reader, engine, context.applicationContext) { complete ->
                indexReady = engine.hasDocuments(SearchKind.QURAN)
                indexComplete = complete
                indexRevision += 1
            }
            indexFailed = !indexReady
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            indexFailed = true
        }
    }

    LaunchedEffect(query, isActive) {
        if (!isActive) return@LaunchedEffect
        delay(180)
        debounced = query
    }
    LaunchedEffect(debounced, indexReady, indexRevision, isActive, resultLimit) {
        if (!isActive) return@LaunchedEffect
        if (!indexReady || debounced.isBlank()) {
            results = SearchResults()
            resultsQuery = debounced
            searching = false
            return@LaunchedEffect
        }
        searching = true
        searchFailed = false
        try {
            val found = withContext(Dispatchers.Default) {
                engine.search(debounced, SearchOptions(limitPerCategory = resultLimit))
            }
            results = found
            resultsQuery = debounced
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            searchFailed = true
        } finally {
            searching = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = stringResource(R.string.search),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.searchSubtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(stringResource(R.string.searchHint)) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.clearSearch),
                        )
                    }
                }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { debounced = query.trim() }),
        )

        if (indexReady && !indexComplete) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("searchIndexProgress"),
            )
        }

        if (indexReady && query.trim() == debounced.trim() &&
            debounced.isNotBlank() && results.total > 0
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SearchCategory.values().toList()) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategoryName = category.name },
                        label = {
                            Text(
                                "${searchCategoryLabel(category)}  " +
                                    searchCategoryCount(category, results),
                            )
                        },
                    )
                }
            }
        }

        if (indexFailed || searchFailed) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.searchFailed))
                    Button(onClick = { retryIndex += 1; indexRevision += 1 }) { Text(stringResource(R.string.retry)) }
                }
            }
        } else if (!indexReady) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (query.isBlank()) {
            SearchIntro(language = language, onExample = { query = it })
        } else if (query.trim() != debounced.trim() || searching || resultsQuery != debounced) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("searchDebounceProgress"),
            )
        } else if (results.total == 0) {
            Card(Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.noSearchResults),
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            val showSurahs = selectedCategory == SearchCategory.ALL ||
                selectedCategory == SearchCategory.SURAHS
            val showQuran = selectedCategory == SearchCategory.ALL ||
                selectedCategory == SearchCategory.QURAN
            val showHadith = selectedCategory == SearchCategory.ALL ||
                selectedCategory == SearchCategory.SUNNAH
            val showTafsir = selectedCategory == SearchCategory.ALL ||
                selectedCategory == SearchCategory.TAFSIR
            val visibleCount =
                (if (showSurahs) results.surahs.size else 0) +
                    (if (showQuran) results.quran.size else 0) +
                    (if (showHadith) results.hadith.size else 0) +
                    (if (showTafsir) results.tafsir.size else 0)
            if (visibleCount == 0) {
                Card(Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.noResultsInCategory),
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    state = resultListState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    if (showSurahs && results.surahs.isNotEmpty()) {
                        item {
                            SectionTitle(
                                "${stringResource(R.string.surahs)} (${results.surahs.size})",
                            )
                        }
                        items(results.surahs, key = { it.refKey }) { hit ->
                            SearchHitRow(
                                title = hit.title,
                                subtitle = hit.subtitle,
                                highlight = debounced,
                                tag = "searchHit_${hit.refKey}",
                                onClick = {
                                    hit.surah?.let { onOpenAyahRef("$it:1") }
                                },
                            )
                        }
                    }
                    if (showQuran && results.quran.isNotEmpty()) {
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
                                highlight = debounced,
                                tag = "searchHit_${hit.refKey}",
                                onClick = {
                                    if (hit.surah != null && hit.ayah != null) {
                                        onOpenAyahRef("${hit.surah}:${hit.ayah}")
                                    }
                                },
                            )
                        }
                    }
                    if (showHadith && results.hadith.isNotEmpty()) {
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
                                highlight = debounced,
                                tag = "searchHit_${hit.refKey}",
                                onClick = {
                                    hit.hadith?.let { selectedHadith = it }
                                },
                            )
                        }
                    }
                    if (showTafsir && results.tafsir.isNotEmpty()) {
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
                                rtlSnippet = true,
                                highlight = debounced,
                                tag = "searchHit_${hit.refKey}",
                                onClick = { selectedTafsir = hit },
                            )
                        }
                    }
                    if (results.truncated) {
                        item {
                            Button(onClick = { resultLimit += 20 }, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.showMoreResults))
                            }
                        }
                    }
                }
            }
        }
    }

    if (isActive) selectedHadith?.let { hadith ->
        val collection = hadithCollections.firstOrNull { it.id == hadith.collectionId }
        if (collection != null) {
            HadithReaderSheet(
                hadith = hadith,
                collection = collection,
                onDismiss = { selectedHadith = null },
            )
        }
    }
    if (isActive) selectedTafsir?.let { hit ->
        SearchTafsirResult(reader, hit, debounced, onDismiss = { selectedTafsir = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTafsirResult(
    reader: AndroidAssetReader,
    hit: com.godfather59.quransunnah.search.SearchHit,
    query: String,
    onDismiss: () -> Unit,
) {
    var ayah by remember(hit.refKey) { mutableStateOf<Ayah?>(null) }
    var loaded by remember(hit.refKey) { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val font = remember(context) { quranFontFamily(context, "uthmani") }
    LaunchedEffect(hit.refKey, retry) {
        loaded = false
        ayah = withContext(Dispatchers.IO) {
            runCatching {
                loadQuranEdition(reader, "hafs-an-asim__uthmani")
                    ?.firstOrNull { it.surah == hit.surah && it.ayah == hit.ayah }
            }.getOrNull()
        }
        loaded = true
    }
    val verse = ayah
    if (verse != null) {
        TafsirSheet(reader = reader, ayah = verse, reference = "${verse.surah}:${verse.ayah}",
            quranFontFamily = font, initialTafsirId = hit.refKey.substringBefore(":"),
            highlightQuery = query, onDismiss = onDismiss)
    } else {
        ModalBottomSheet(onDismissRequest = onDismiss) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!loaded) CircularProgressIndicator() else {
                    Text(stringResource(R.string.contentUnavailable))
                    Button(onClick = { retry += 1 }) { Text(stringResource(R.string.retry)) }
                }
            }
        }
    }
}

@Composable
private fun SearchIntro(
    language: String,
    onExample: (String) -> Unit,
) {
    val examples = listOf("2:255", "Al-Fatiha", "الرحمن")
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FindInPage,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.searchExploreSources),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                text = stringResource(R.string.searchExploreHint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(examples) { example ->
                    AssistChip(
                        onClick = { onExample(example) },
                        label = { Text(example) },
                    )
                }
            }
        }
    }
}

@Composable
private fun searchCategoryLabel(category: SearchCategory): String =
    stringResource(
        when (category) {
            SearchCategory.ALL -> R.string.searchCatAll
            SearchCategory.QURAN -> R.string.searchCatQuran
            SearchCategory.SUNNAH -> R.string.searchCatSunnah
            SearchCategory.TAFSIR -> R.string.searchCatTafsir
            SearchCategory.SURAHS -> R.string.searchCatSurahs
        },
    )

private fun searchCategoryCount(category: SearchCategory, results: SearchResults): Int =
    when (category) {
        SearchCategory.ALL -> results.total
        SearchCategory.QURAN -> results.quran.size
        SearchCategory.SUNNAH -> results.hadith.size
        SearchCategory.TAFSIR -> results.tafsir.size
        SearchCategory.SURAHS -> results.surahs.size
    }

@Composable
private fun SearchHitRow(
    title: String,
    subtitle: String,
    rtlSnippet: Boolean = false,
    onClick: () -> Unit,
    highlight: String = "",
    tag: String = "",
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (tag.isNotEmpty()) Modifier.testTag(tag) else Modifier)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = highlightedText(title, highlight),
                style = MaterialTheme.typography.titleSmall.copy(textDirection = TextDirection.Ltr),
                textAlign = TextAlign.Left,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = highlightedText(subtitle, highlight),
                style = MaterialTheme.typography.bodySmall.copy(
                    textDirection = if (rtlSnippet) TextDirection.Rtl else TextDirection.Ltr,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = if (rtlSnippet) TextAlign.Right else TextAlign.Left,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
            )
        }
    }
}

// ---- Dhikr counter (shared seed + daily device prefs) ----
