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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
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
internal fun dhikrTitle(): String = stringResource(R.string.dhikrTitle)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DhikrScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val language = currentLanguage()
    var state by remember { mutableStateOf(DhikrState()) }
    LaunchedEffect(context) {
        state = withContext(Dispatchers.IO) { DhikrPrefs.load(context) }
    }
    var showHelp by remember { mutableStateOf(false) }
    val todayTotal = state.today.values.sum()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
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
                text = dhikrTitle(),
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
                        text = stringResource(R.string.dhikrTodayTotal, todayTotal, state.total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.tapToCount),
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
                            .semantics {
                                role = Role.Button
                                contentDescription =
                                    "${dhikr.transliteration} $count / ${dhikr.target}"
                            }
                            .testTag("dhikrCard_${dhikr.id}")
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    stateDescription =
                                        "${(count.toFloat() / dhikr.target * 100).toInt().coerceIn(0, 100)}%"
                                }
                                .testTag("dhikrProgress_${dhikr.id}"),
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
                    Text(stringResource(R.string.howItWorks))
                }
            }
        }
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text(stringResource(R.string.howItWorks)) },
            text = {
                Text(stringResource(R.string.dhikrHelpText))
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) {
                    Text(stringResource(R.string.okButton))
                }
            },
        )
    }
}

@Composable
internal fun DhikrHeroCard(
    todayTotal: Int,
    total: Int,
    language: String,
    onClick: () -> Unit,
) {
    HeroGradientCard(onClick = onClick) {
        Text(
            text = stringResource(R.string.dhikrHeroLine, dhikrTitle(), todayTotal, total),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )
        Text(
            text = stringResource(R.string.tapToCount),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.85f),
        )
    }
}

@Composable
internal fun memorizationTitle(): String = stringResource(R.string.memorizationTitle)

@Composable
private fun memorizationHowTo(): String = stringResource(R.string.memorizationHowTo)

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
            .statusBarsPadding()
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
                text = memorizationTitle(),
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
                    text = stringResource(R.string.memorizedCountLabel, totalMemorized),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                LinearProgressIndicator(
                    progress = { (totalMemorized / 6236f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            stateDescription =
                                "${(totalMemorized / 6236f * 100).toInt().coerceIn(0, 100)}%"
                        }
                        .testTag("memorizationProgress"),
                )
                Text(
                    text = memorizationHowTo(),
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            stateDescription =
                                                "${(done.toFloat() / surah.ayahCount * 100).toInt().coerceIn(0, 100)}%"
                                        }
                                        .testTag("memorizationSurahProgress_${surah.number}"),
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
internal fun MemorizationHeroCard(
    totalMemorized: Int,
    language: String,
    onClick: () -> Unit,
) {
    HeroGradientCard(onClick = onClick) {
        Text(
            text = stringResource(R.string.memorizationHeroLine, memorizationTitle(), totalMemorized),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )
        Text(
            text = memorizationHowTo(),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.85f),
        )
    }
}

/**
 * Formats an ayah number per the persisted style. Mirrors Flutter: both
 * Arabic styles render Arabic-Indic digits, Latin stays as-is.
 */
