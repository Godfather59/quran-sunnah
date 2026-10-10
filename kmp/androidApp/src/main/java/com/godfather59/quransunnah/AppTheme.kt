package com.godfather59.quransunnah

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Emerald brand identity (no default Material purple anywhere): deep
// emerald + pine on a clean mint-white background (light) / pine night
// (dark). Solid, opaque roles — no frosted-glass translucency (translucent
// tints over the dark window background rendered as murky gray-green).
// Cards are pure white with generous 16–28dp radii.

private val Ink = Color(0xFF10201C)
private val Emerald = Color(0xFF0AA97B)
private val Pine = Color(0xFF0B5C46)
private val Mint = Color(0xFFF2FAF6)
private val MintCard = Color(0xFFFFFFFF)
private val MintLine = Color(0xFFDCEBE3)
private val Night = Color(0xFF0B1512)
private val NightSurface = Color(0xFF14201C)
private val EmeraldLight = Color(0xFF7BDFC0)
private val Gold = Color(0xFF9A7B4F)

fun appLightScheme() = lightColorScheme(
    primary = Color(0xFF0A9E6C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC9F2E2),
    onPrimaryContainer = Color(0xFF07372A),
    secondary = Pine,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEBE3),
    onSecondaryContainer = Color(0xFF1E3A31),
    tertiary = Gold,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF1E8D5),
    onTertiaryContainer = Color(0xFF4A3F2C),
    error = Color(0xFFA63A2E),
    onError = Color.White,
    errorContainer = Color(0xFFF3DAD4),
    onErrorContainer = Color(0xFF4A1D17),
    background = Color(0xFFF2F7F4),
    onBackground = Ink,
    surface = Color(0xFFF2F7F4),
    onSurface = Ink,
    surfaceVariant = Color(0xFFE1EEE7),
    onSurfaceVariant = Color(0xFF47605A),
    surfaceTint = Emerald,
    surfaceDim = Color(0xFFD9E7DE),
    surfaceBright = MintCard,
    surfaceContainerLowest = MintCard,
    surfaceContainerLow = MintCard,
    surfaceContainer = MintCard,
    surfaceContainerHigh = Color(0xFFEDF4EF),
    surfaceContainerHighest = Color(0xFFE1EEE7),
    outline = Color(0xFF7E968D),
    outlineVariant = MintLine,
    scrim = Color.Black,
    inverseSurface = Ink,
    inverseOnSurface = Mint,
    inversePrimary = EmeraldLight,
)

fun appDarkScheme() = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color(0xFF053527),
    primaryContainer = Color(0xFF0B4A38),
    onPrimaryContainer = Color(0xFFC9F2E2),
    secondary = Color(0xFF9AD1BC),
    onSecondary = Color(0xFF07332A),
    secondaryContainer = Color(0xFF173E33),
    onSecondaryContainer = Color(0xFFDCEBE3),
    tertiary = Color(0xFFD3B98C),
    onTertiary = Color(0xFF2E2515),
    tertiaryContainer = Color(0xFF3A3423),
    onTertiaryContainer = Color(0xFFF1E8D5),
    error = Color(0xFFE5A396),
    onError = Color(0xFF4A1D17),
    errorContainer = Color(0xFF5E231B),
    onErrorContainer = Color(0xFFF3DAD4),
    background = Night,
    onBackground = Color(0xFFE7F0EB),
    surface = Night,
    onSurface = Color(0xFFE7F0EB),
    surfaceVariant = Color(0xFF1B2E28),
    onSurfaceVariant = Color(0xFFAFC4BB),
    surfaceTint = EmeraldLight,
    surfaceDim = Night,
    surfaceBright = Color(0xFF1B2E28),
    surfaceContainerLowest = Color(0xFF0E1A16),
    surfaceContainerLow = Color(0xFF101D18),
    surfaceContainer = NightSurface,
    surfaceContainerHigh = Color(0xFF1B2E28),
    surfaceContainerHighest = Color(0xFF224036),
    outline = Color(0xFF84A196),
    outlineVariant = Color(0xFF2C443B),
    scrim = Color.Black,
    inverseSurface = Mint,
    inverseOnSurface = Ink,
    inversePrimary = Emerald,
)

fun appShapes() = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
)

/** Subtle backdrop wash derived from the active scheme (dynamic-safe). */
@Composable
fun appBackgroundBrush(): Brush {
    val scheme = MaterialTheme.colorScheme
    return remember(scheme) {
        Brush.verticalGradient(
            0f to scheme.primaryContainer.copy(alpha = 0.38f),
            0.45f to Color.Transparent,
            1f to scheme.secondaryContainer.copy(alpha = 0.32f),
        )
    }
}

/**
 * White status-bar icons while an emerald header is on screen. Restores the
 * theme-appropriate appearance on dispose (screens without emerald headers
 * manage their own insets).
 */
@Composable
fun EmeraldStatusBar() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    androidx.compose.runtime.DisposableEffect(darkTheme) {
        val window = (context as? android.app.Activity)?.window
        val controller = window?.let {
            androidx.core.view.WindowCompat.getInsetsController(it, it.decorView)
        }
        controller?.isAppearanceLightStatusBars = false
        onDispose {
            try {
                controller?.isAppearanceLightStatusBars = !darkTheme
            } catch (_: Exception) {
            }
        }
    }
}

private val hijriMonthsAr = listOf(
    "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
    "جمادى الأولى", "جمادى الثانية", "رجب", "شعبان",
    "رمضان", "شوال", "ذو القعدة", "ذو الحجة",
)
private val hijriMonthsEn = listOf(
    "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani",
    "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Shaban",
    "Ramadan", "Shawwal", "Dhu al-Qadah", "Dhu al-Hijjah",
)
private val hijriMonthsFr = listOf(
    "Mouharram", "Safar", "Rabi al-awwal", "Rabi al-thani",
    "Joumada al-oula", "Joumada al-akhira", "Rajab", "Chaabane",
    "Ramadan", "Chawwal", "Dhou al-qada", "Dhou al-hijja",
)

/** Hijri date line ("9 Muharram 1447") via the platform Islamic calendar. */
fun hijriToday(language: String): String {
    return try {
        val cal = android.icu.util.IslamicCalendar()
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val year = cal.get(java.util.Calendar.YEAR)
        val monthIdx = cal.get(java.util.Calendar.MONTH).coerceIn(0, 11)
        val month = when (language) {
            "ar" -> hijriMonthsAr[monthIdx]
            "fr" -> hijriMonthsFr[monthIdx]
            else -> hijriMonthsEn[monthIdx]
        }
        "$day $month $year"
    } catch (_: Exception) {
        ""
    }
}

/**
 * Mockup-style screen: full-bleed emerald gradient, fixed header (drawn
 * under the status bar), and a rounded white sheet holding the scrolling
 * body. Works in light and dark schemes via [emeraldHeaderBrush].
 */
@Composable
fun EmeraldScaffold(
    header: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
    body: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    EmeraldStatusBar()
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(emeraldHeaderBrush()),
    ) {
        androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                header()
            }
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(
                        scheme.surfaceContainerLowest,
                        androidx.compose.foundation.shape.RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp,
                        ),
                    ),
            ) {
                body()
            }
        }
    }
}

@Composable
private fun emeraldHeaderBrush(): Brush {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    return remember(dark) {
        if (dark) {
            Brush.linearGradient(
                colors = listOf(Color(0xFF0E4A3A), Color(0xFF08120E)),
                start = Offset.Zero,
                end = Offset.Infinite,
            )
        } else {
            // Deep, calm emerald (not neon): easy on the eyes in daylight.
            Brush.linearGradient(
                colors = listOf(Color(0xFF0C9A6C), Color(0xFF095C46)),
                start = Offset.Zero,
                end = Offset.Infinite,
            )
        }
    }
}

/**
 * Emerald hero card (mockup-style diagonal emerald→pine gradient, white
 * content) with a hairline glass border. Default text colors follow white;
 * callers must not set dark scheme colors inside.
 */
@Composable
fun HeroGradientCard(
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val brush = remember {
        Brush.linearGradient(
            colors = listOf(Color(0xFF0C9A6C), Color(0xFF095C46)),
            start = Offset.Zero,
            end = Offset.Infinite,
        )
    }
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
        shape = MaterialTheme.shapes.large,
    ) {
        Box(
            modifier = Modifier
                .background(brush)
                .padding(20.dp),
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides Color.White,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    content = content,
                )
            }
        }
    }
}
