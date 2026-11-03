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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
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

@Composable
fun SettingsScreen(
    onThemeChange: (String) -> Unit = {},
    isActive: Boolean = true,
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val language = currentLanguage()
    val reader = remember(context) { AndroidAssetReader(context) }
    var prefs by remember { mutableStateOf(ReaderPrefsState()) }
    var settingsLoaded by rememberSaveable { mutableStateOf(false) }
    var riwayaName by rememberSaveable { mutableStateOf("hafsAsim") }
    var scriptName by rememberSaveable { mutableStateOf("uthmani") }
    var fontName by rememberSaveable { mutableStateOf("uthmani") }
    var showTranslation by rememberSaveable { mutableStateOf(true) }
    var reciterId by rememberSaveable { mutableStateOf("ar.alafasy") }
    var themeName by rememberSaveable { mutableStateOf("system") }
    var speed by rememberSaveable { mutableStateOf(1.0) }
    var locale by rememberSaveable { mutableStateOf(language) }
    var tafsirId by rememberSaveable { mutableStateOf("jalalayn") }
    var numberStyleSetting by rememberSaveable { mutableStateOf("arabicIndic") }
    var showLicenses by remember { mutableStateOf(false) }
    var picker by rememberSaveable { mutableStateOf<String?>(null) }
    var quranTextSize by rememberSaveable { mutableStateOf(26f) }
    var translationTextSize by rememberSaveable { mutableStateOf(16f) }
    var lineSpacing by rememberSaveable { mutableStateOf(1.65f) }
    var translationId by rememberSaveable { mutableStateOf("en-sahih") }
    val quranFontFamily = remember(context, fontName) {
        quranFontFamily(context, fontName)
    }
    var sampleAyah by remember { mutableStateOf<String?>(null) }
    var sampleLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(context, isActive) {
        if (!isActive || settingsLoaded) return@LaunchedEffect
        val loaded = withContext(Dispatchers.IO) {
            Triple(
                ReaderPrefs.load(context),
                ThemePrefs.load(context),
                AudioPrefs.speed(context) to OnboardingPrefs.locale(context),
            )
        }
        prefs = loaded.first
        val edition = resolveReaderEdition(loaded.first.riwayaName, loaded.first.scriptName)
        riwayaName = edition.riwayaName
        scriptName = edition.scriptName
        fontName = loaded.first.fontName
        showTranslation = loaded.first.showTranslation
        reciterId = loaded.first.reciterId
        themeName = loaded.second.themeName
        speed = loaded.third.first
        locale = loaded.third.second ?: language
        tafsirId = loaded.first.tafsirId
        numberStyleSetting = loaded.first.ayahNumberStyleName
        quranTextSize = loaded.first.quranTextSize
        translationTextSize = loaded.first.translationTextSize
        lineSpacing = loaded.first.lineSpacing
        translationId = loaded.first.translationId
        settingsLoaded = true
    }
    LaunchedEffect(reader, isActive, riwayaName, scriptName) {
        if (!isActive) return@LaunchedEffect
        sampleLoaded = false
        sampleAyah = null
        sampleAyah = withContext(Dispatchers.IO) {
            loadQuranEdition(reader, editionId = resolveReaderEdition(riwayaName, scriptName).id)
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

    if (!settingsLoaded) {
        Box(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp)
            .testTag("settingsList")
            .semantics { uiContentReady = sampleLoaded && !sampleAyah.isNullOrBlank() },
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
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        item {
            Card(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.languageGroup),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    val localeOptions = remember {
                        listOf("ar" to "العربية", "en" to "English", "fr" to "Français")
                    }
                    SettingsOptionChips(localeOptions, selected = locale) { id ->
                        locale = id
                        OnboardingPrefs.saveLocale(context, id)
                        applyAppLocale(id)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = stringResource(R.string.appearanceGroup),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    val themeOptions = remember {
                        listOf(
                            "system" to R.string.system,
                            "light" to R.string.light,
                            "dark" to R.string.dark,
                        )
                    }
                    SettingsOptionChips(
                        themeOptions.map { (id, labelRes) -> id to stringResource(labelRes) },
                        selected = themeName,
                    ) { id ->
                        themeName = id
                        ThemePrefs.save(context, id)
                        onThemeChange(id)
                    }
                }
            }
        }

        item { SectionTitle(stringResource(R.string.quranGroup)) }
        item {
            SettingsChoiceRow(stringResource(R.string.defaultRiwaya),
                readerEditionLabel(resolveReaderEdition(riwayaName, scriptName))) { picker = "edition" }
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
                    }
                    SettingsChoiceRow(stringResource(R.string.quranFont),
                        readerFontOptions.firstOrNull { it.id == fontName }?.label.orEmpty()) { picker = "font" }
                    ReaderStyleControls(quranTextSize, translationTextSize, lineSpacing,
                        onStyleChanged = { q, t, spacing -> quranTextSize = q; translationTextSize = t; lineSpacing = spacing },
                        quranFontFamily = quranFontFamily, previewAyah = sampleAyah.orEmpty(),
                        onStyleChangeFinished = { ReaderPrefs.saveReadingStyle(context, quranTextSize, translationTextSize, lineSpacing) })
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
            SettingsChoiceRow(stringResource(R.string.translation), readerTranslationLabel(translationId)) { picker = "translation" }
        }
        item {
            SettingsChoiceRow(stringResource(R.string.preferredTafsir),
                stringResource(tafsirOptions.firstOrNull { it.id == tafsirId }?.labelRes ?: R.string.jalalayn)) { picker = "tafsir" }
        }
        item {
            Text(
                text = stringResource(R.string.ayahNumberStyle),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
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
            SettingsChoiceRow(stringResource(R.string.reciter),
                reciters.firstOrNull { it.identifier == reciterId }?.localizedName(language).orEmpty()) { picker = "reciter" }
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

        item { SectionTitle(stringResource(R.string.storageGroup)) }
        item {
            Text(stringResource(R.string.verifiedDatasetsInstalled), style = MaterialTheme.typography.bodyMedium)
            Text("${surahMetadata.size} ${stringResource(R.string.surahs)} · ${hadithCollections.size} ${stringResource(R.string.hadithCollections)}",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
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
    picker?.let { kind ->
        val title = stringResource(when (kind) {
            "edition" -> R.string.defaultRiwaya
            "font" -> R.string.quranFont
            "reciter" -> R.string.reciter
            "tafsir" -> R.string.preferredTafsir
            else -> R.string.translation
        })
        val options = when (kind) {
            "edition" -> readerEditions.map { it.id to readerEditionLabel(it) }
            "font" -> readerFontOptions.map { it.id to it.label }
            "reciter" -> reciters.map { it.identifier to it.localizedName(language) }
            "tafsir" -> tafsirOptions.map { it.id to stringResource(it.labelRes) }
            else -> listOf("en-sahih" to readerTranslationLabel("en-sahih"), "fr-hamidullah" to readerTranslationLabel("fr-hamidullah"))
        }
        val selected = when (kind) {
            "edition" -> resolveReaderEdition(riwayaName, scriptName).id
            "font" -> fontName
            "reciter" -> reciterId
            "tafsir" -> tafsirId
            else -> translationId
        }
        SettingsPickerDialog(title, options, selected, onDismiss = { picker = null }) { id ->
            when (kind) {
                "edition" -> readerEditions.first { it.id == id }.let {
                    riwayaName = it.riwayaName; scriptName = it.scriptName
                    ReaderPrefs.saveReaderDisplay(context, riwayaName, scriptName)
                }
                "font" -> { fontName = id; ReaderPrefs.saveReaderFont(context, id) }
                "reciter" -> { reciterId = id; ReaderPrefs.saveReciter(context, id) }
                "tafsir" -> { tafsirId = id; ReaderPrefs.saveTafsir(context, id) }
                else -> { translationId = id; ReaderPrefs.saveTranslation(context, id) }
            }
            picker = null
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsOptionChips(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    val optionChip: @Composable (Pair<String, String>) -> Unit = { (id, label) ->
        FilterChip(
            selected = id == selected,
            onClick = { onSelect(id) },
            label = { Text(label) },
        )
    }
    if (LocalDensity.current.fontScale >= 1.3f) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            options.forEach { optionChip(it) }
        }
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options, key = { it.first }) { optionChip(it) }
        }
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
