package com.godfather59.quransunnah

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.godfather59.quransunnah.audio.AudioPlayer
import com.godfather59.quransunnah.audio.AyahAudioItem
import com.godfather59.quransunnah.audio.PlayerListener
import com.godfather59.quransunnah.device.AndroidClipboard
import com.godfather59.quransunnah.device.AndroidSharer
import com.godfather59.quransunnah.quran.surahMetadata
import com.godfather59.quransunnah.text.parseQuranRef
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Applies an ar/en/fr app locale process-wide (per-app language). */
fun applyAppLocale(tag: String) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
}

class MainActivity : AppCompatActivity() {
    /** Raw `quran://s/a` link string; handled once the MAIN scaffold composes. */
    private var pendingDeepLink by mutableStateOf<String?>(null)
    private var deepLinkToken by mutableIntStateOf(0)
    private var safeMode by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installCrashLog()
        applySavedLocale()
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)
        safeMode = crashLogFile().exists()
        setContent {
            if (safeMode) {
                MaterialTheme {
                    SafeModeScreen(
                        onRetry = {
                            crashLogFile().delete()
                            safeMode = false
                        },
                    )
                }
            } else {
                QuranSunnahApp(
                    deepLink = pendingDeepLink,
                    deepLinkToken = deepLinkToken,
                    onDeepLinkConsumed = { pendingDeepLink = null },
                    onOpenRef = { raw ->
                        pendingDeepLink = raw
                        deepLinkToken += 1
                    },
                )
            }
        }
    }

    private fun crashLogFile(): java.io.File =
        java.io.File(filesDir, "crash.log")

    /**
     * Persists the next uncaught stack trace, then chains to the system
     * handler. On the following launch the app opens the safe-mode screen
     * instead of dying again, so a startup crash becomes a readable,
     * shareable report rather than a permanent boot loop.
     */
    private fun installCrashLog() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                crashLogFile().writeText(
                    buildString {
                        appendLine(java.util.Date().toString())
                        appendLine(throwable.stackTraceToString().take(12000))
                    }.take(60000),
                )
            } catch (_: Exception) {
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun applySavedLocale() {
        val tag = OnboardingPrefs.locale(this) ?: return
        if (tag in setOf("ar", "en", "fr")) applyAppLocale(tag)
    }

    private fun handleDeepLink(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return
        val raw = intent.dataString ?: return
        if (intent.data?.scheme != "quran" && !raw.contains("quran")) return
        // Validate with the shared parser before touching UI state.
        val parsed = parseQuranRef(raw) ?: return
        if (parsed.first !in 1..114 || parsed.second < 1) return
        pendingDeepLink = raw
        deepLinkToken += 1
    }
}

/**
 * Minimal crash-report screen. Shown instead of the normal UI when the
 * previous launch died with an uncaught exception (see installCrashLog),
 * so a startup crash loop becomes a shareable report + a retry button
 * rather than a permanently dead app. Deliberately dependency-free:
 * no database, no audio, no assets, no prefs beyond a file read.
 */
@Composable
private fun SafeModeScreen(onRetry: () -> Unit) {
    val context = LocalContext.current
    val trace = remember {
        try {
            java.io.File(context.filesDir, "crash.log")
                .readText()
                .take(8000)
                .ifBlank { "Unavailable." }
        } catch (_: Exception) {
            "Unavailable."
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Safe mode",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )
        Text(
            text = "The app closed unexpectedly on start. Copy or share this report with the developer, then retry.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Text(
                text = trace,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { AndroidClipboard(context).copy(trace) },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.copy))
            }
            Button(
                onClick = {
                    AndroidSharer(context).shareText(trace, "Quran & Sunnah crash log")
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.share))
            }
        }
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.retry))
        }
    }
}

private data class AppDestination(
    val titleRes: Int,
    val icon: ImageVector,
)

private val destinations = listOf(
    AppDestination(R.string.home, Icons.Filled.Home),
    AppDestination(R.string.quran, Icons.AutoMirrored.Filled.MenuBook),
    AppDestination(R.string.sunnah, Icons.AutoMirrored.Filled.LibraryBooks),
    AppDestination(R.string.library, Icons.Filled.Star),
    AppDestination(R.string.downloads, Icons.Filled.Download),
    AppDestination(R.string.settings, Icons.Filled.Settings),
)

private enum class AppPhase {
    SPLASH,
    ONBOARDING,
    MAIN,
}

@Composable
private fun QuranSunnahApp(
    deepLink: String? = null,
    deepLinkToken: Int = 0,
    onDeepLinkConsumed: () -> Unit = {},
    onOpenRef: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val savedTheme = remember(context) { ThemePrefs.load(context) }
    var themeName by rememberSaveable { mutableStateOf(savedTheme.themeName) }
    var dynamicColor by rememberSaveable { mutableStateOf(savedTheme.dynamicColor) }
    val dark = when (themeName) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dark -> appDarkScheme()
        else -> appLightScheme()
    }
    MaterialTheme(colorScheme = colorScheme, shapes = appShapes()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(appBackgroundBrush()),
            ) {
                AppStart(
                    deepLink = deepLink,
                    deepLinkToken = deepLinkToken,
                    onDeepLinkConsumed = onDeepLinkConsumed,
                    onOpenRef = onOpenRef,
                    onThemeChange = { name, dynamic ->
                        themeName = name
                        dynamicColor = dynamic
                    },
                )
            }
        }
    }
}

@Composable
private fun AppStart(
    deepLink: String? = null,
    deepLinkToken: Int = 0,
    onDeepLinkConsumed: () -> Unit = {},
    onOpenRef: (String) -> Unit = {},
    onThemeChange: (String, Boolean) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    var phase by rememberSaveable {
        mutableStateOf(
            if (OnboardingPrefs.isDone(context)) {
                AppPhase.MAIN
            } else {
                AppPhase.SPLASH
            },
        )
    }

    LaunchedEffect(Unit) {
        if (phase == AppPhase.SPLASH) {
            delay(300)
            phase = AppPhase.ONBOARDING
        }
    }

    when (phase) {
        AppPhase.SPLASH -> SplashScreen()
        AppPhase.ONBOARDING -> OnboardingFlow { choices ->
            OnboardingPrefs.save(context, choices)
            // Apply the chosen language immediately (recreates for the
            // new locale); MAIN renders on the other side of it.
            if (choices.locale in setOf("ar", "en", "fr")) {
                applyAppLocale(choices.locale)
            }
            phase = AppPhase.MAIN
        }
        AppPhase.MAIN -> AdaptiveAppScaffold(
            deepLink = deepLink,
            deepLinkToken = deepLinkToken,
            onDeepLinkConsumed = onDeepLinkConsumed,
            onOpenRef = onOpenRef,
            onThemeChange = onThemeChange,
        )
    }
}

@Composable
private fun AdaptiveAppScaffold(
    deepLink: String? = null,
    deepLinkToken: Int = 0,
    onDeepLinkConsumed: () -> Unit = {},
    onOpenRef: (String) -> Unit = {},
    onThemeChange: (String, Boolean) -> Unit = { _, _ -> },
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    var deepLinkSurah by rememberSaveable { mutableStateOf<Int?>(null) }
    var deepLinkSurahToken by rememberSaveable { mutableIntStateOf(0) }
    val stateHolder = rememberSaveableStateHolder()
    val context = LocalContext.current

    // Cold-start and warm `quran://s/a` links: persist the position (the
    // reader picks its initial ayah up from prefs), switch to the Quran
    // tab, and stage the surah for QuranIndexScreen. Runs only in MAIN,
    // so links arriving before onboarding completes wait for it.
    LaunchedEffect(deepLink, deepLinkToken) {
        val raw = deepLink ?: return@LaunchedEffect
        val parsed = parseQuranRef(raw)
        onDeepLinkConsumed()
        if (parsed == null) return@LaunchedEffect
        val (su, ay) = parsed
        if (su !in 1..114 || ay < 1) return@LaunchedEffect
        val ayahCount = surahMetadata.first { it.number == su }.ayahCount
        ReaderPrefs.saveLastPosition(context, su, ay.coerceIn(1, ayahCount))
        deepLinkSurah = su
        deepLinkSurahToken += 1
        selectedIndex = 1
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 840.dp
        if (wide) {
            Row(Modifier.fillMaxSize()) {
                AppNavigationRail(
                    selectedIndex = selectedIndex,
                    onDestinationSelected = { selectedIndex = it },
                )
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                )
                MainPane(
                    selectedIndex = selectedIndex,
                    deepLinkSurah = deepLinkSurah,
                    deepLinkSurahToken = deepLinkSurahToken,
                    onOpenRef = onOpenRef,
                    onThemeChange = onThemeChange,
                )
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    stateHolder.SaveableStateProvider(selectedIndex) {
                        DestinationScreen(
                            selectedIndex = selectedIndex,
                            deepLinkSurah = deepLinkSurah,
                            deepLinkSurahToken = deepLinkSurahToken,
                            onOpenRef = onOpenRef,
                            onThemeChange = onThemeChange,
                        )
                    }
                }
                MiniPlayer()
                NavigationBar {
                    destinations.forEachIndexed { index, destination ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            icon = {
                                Icon(
                                    destination.icon,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(destination.titleRes)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppNavigationRail(
    selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit,
) {
    NavigationRail {
        destinations.forEachIndexed { index, destination ->
            NavigationRailItem(
                selected = selectedIndex == index,
                onClick = { onDestinationSelected(index) },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(stringResource(destination.titleRes)) },
            )
        }
    }
}

@Composable
private fun MainPane(
    selectedIndex: Int,
    deepLinkSurah: Int? = null,
    deepLinkSurahToken: Int = 0,
    onOpenRef: (String) -> Unit = {},
    onThemeChange: (String, Boolean) -> Unit = { _, _ -> },
) {
    val stateHolder = rememberSaveableStateHolder()
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            stateHolder.SaveableStateProvider(selectedIndex) {
                DestinationScreen(
                    selectedIndex = selectedIndex,
                    deepLinkSurah = deepLinkSurah,
                    deepLinkSurahToken = deepLinkSurahToken,
                    onOpenRef = onOpenRef,
                    onThemeChange = onThemeChange,
                )
            }
        }
        MiniPlayer()
    }
}

@Composable
private fun DestinationScreen(
    selectedIndex: Int,
    deepLinkSurah: Int? = null,
    deepLinkSurahToken: Int = 0,
    onOpenRef: (String) -> Unit = {},
    onThemeChange: (String, Boolean) -> Unit = { _, _ -> },
) {
    when (selectedIndex) {
        0 -> HomeScreen(onOpenAyahRef = onOpenRef)
        1 -> QuranIndexScreen(
            deepLinkSurah = deepLinkSurah,
            deepLinkSurahToken = deepLinkSurahToken,
        )
        2 -> SunnahHomeScreen()
        3 -> LibraryScreen(onOpenAyahRef = onOpenRef)
        4 -> DownloadsScreen()
        5 -> SettingsScreen(onThemeChange = onThemeChange)
        else -> PlaceholderScreen(destinations[selectedIndex].titleRes)
    }
}

@Composable
private fun MiniPlayer() {
    val context = LocalContext.current
    // Player creation can throw on devices with broken media codecs — a
    // dead MiniPlayer must never take down the whole screen.
    val audioPlayer = remember(context) {
        try {
            AndroidStores.audio(context)
        } catch (_: Exception) {
            null
        }
    } ?: return
    var isPlaying by remember { mutableStateOf(false) }
    var currentItem by remember { mutableStateOf<AyahAudioItem?>(null) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(audioPlayer) {
        val listener = object : PlayerListener {
            override fun onPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onCurrentRefKey(refKey: String) {
            }

            override fun onCurrentItem(item: AyahAudioItem?) {
                currentItem = item
            }
        }
        audioPlayer.setListener(listener)
        onDispose {
            audioPlayer.setListener(null)
        }
    }

    val isVisible = isPlaying || currentItem != null
    if (!isVisible) {
        return@MiniPlayer
    }

    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable {
                        if (isPlaying) {
                            coroutineScope.launch { audioPlayer.pause() }
                        } else {
                            QuranAudio.ensureStarted(context)
                            coroutineScope.launch { audioPlayer.play() }
                        }
                    }
                    .padding(end = 12.dp),
            )
            currentItem?.let { item ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.artist,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = if (isPlaying) stringResource(R.string.playingFrom) else stringResource(R.string.off),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}