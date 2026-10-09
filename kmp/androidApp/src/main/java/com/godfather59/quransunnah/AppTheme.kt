package com.godfather59.quransunnah

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
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

// Calm brand identity ported from Flutter's AppTheme (no default Material
// purple anywhere): deep green-ink + muted teal + warm bronze over warm
// paper (light) / night ink (dark).
//
// Glass treatment: `surface`/`background` are transparent so one subtle
// backdrop gradient shows through everywhere, while the container roles
// stay nearly opaque (0.78–0.92) so sacred text keeps full legibility and
// only spacing reads as frosted glass.

private val Ink = Color(0xFF1A2E2A)
private val Teal = Color(0xFF0F6A5F)
private val Bronze = Color(0xFF9A7B4F)
private val Paper = Color(0xFFF7F3EA)
private val Sand = Color(0xFFEDE6D6)
private val Night = Color(0xFF0E1513)
private val NightSurface = Color(0xFF182220)
private val TealLight = Color(0xFF8FD0C2)
private val BronzeLight = Color(0xFFD3B98C)

fun appLightScheme() = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5EAE4),
    onPrimaryContainer = Ink,
    secondary = Bronze,
    onSecondary = Color.White,
    secondaryContainer = Sand,
    onSecondaryContainer = Color(0xFF4A3F2C),
    tertiary = Color(0xFF5B7A6E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE3EDE7),
    onTertiaryContainer = Ink,
    error = Color(0xFFA63A2E),
    onError = Color.White,
    errorContainer = Color(0xFFF3DAD4),
    onErrorContainer = Color(0xFF4A1D17),
    background = Color.Transparent,
    onBackground = Ink,
    surface = Color.Transparent,
    onSurface = Ink,
    surfaceVariant = Sand.copy(alpha = 0.85f),
    onSurfaceVariant = Color(0xFF4E5D58),
    surfaceTint = Teal,
    surfaceDim = Sand,
    surfaceBright = Color.White,
    surfaceContainerLowest = Color.White.copy(alpha = 0.92f),
    surfaceContainerLow = Color(0xFFFFFDF7).copy(alpha = 0.88f),
    surfaceContainer = Color(0xFFFAF6EC).copy(alpha = 0.85f),
    surfaceContainerHigh = Color(0xFFF3EDDF).copy(alpha = 0.85f),
    surfaceContainerHighest = Sand.copy(alpha = 0.85f),
    outline = Color(0xFF7E8D87),
    outlineVariant = Color(0xFFD8D2C0),
    scrim = Color.Black,
    inverseSurface = Ink,
    inverseOnSurface = Paper,
    inversePrimary = TealLight,
)

fun appDarkScheme() = darkColorScheme(
    primary = TealLight,
    onPrimary = Color(0xFF06231F),
    primaryContainer = Color(0xFF0B3B34),
    onPrimaryContainer = Color(0xFFD5EAE4),
    secondary = BronzeLight,
    onSecondary = Color(0xFF2E2515),
    secondaryContainer = Color(0xFF3A3423),
    onSecondaryContainer = Color(0xFFEDE6D6),
    tertiary = Color(0xFFA9C6BB),
    onTertiary = Color(0xFF0B2420),
    tertiaryContainer = Color(0xFF24423B),
    onTertiaryContainer = Color(0xFFE3EDE7),
    error = Color(0xFFE5A396),
    onError = Color(0xFF4A1D17),
    errorContainer = Color(0xFF5E231B),
    onErrorContainer = Color(0xFFF3DAD4),
    background = Color.Transparent,
    onBackground = Color(0xFFE9E7DC),
    surface = Color.Transparent,
    onSurface = Color(0xFFE9E7DC),
    surfaceVariant = Color(0xFF243330).copy(alpha = 0.85f),
    onSurfaceVariant = Color(0xFFB9C6C0),
    surfaceTint = TealLight,
    surfaceDim = Night,
    surfaceBright = Color(0xFF243330),
    surfaceContainerLowest = Night.copy(alpha = 0.92f),
    surfaceContainerLow = Color(0xFF131D1A).copy(alpha = 0.88f),
    surfaceContainer = NightSurface.copy(alpha = 0.85f),
    surfaceContainerHigh = Color(0xFF1E2C29).copy(alpha = 0.85f),
    surfaceContainerHighest = Color(0xFF243330).copy(alpha = 0.85f),
    outline = Color(0xFF84948D),
    outlineVariant = Color(0xFF33433F),
    scrim = Color.Black,
    inverseSurface = Paper,
    inverseOnSurface = Ink,
    inversePrimary = Teal,
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
 * Diagonal teal→bronze hero card with a hairline glass border. Content
 * keeps the existing onPrimaryContainer/onSurface text colors.
 */
@Composable
fun HeroGradientCard(
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val brush = remember(scheme) {
        Brush.linearGradient(
            colors = listOf(scheme.primaryContainer, scheme.secondaryContainer),
            start = Offset.Zero,
            end = Offset.Infinite,
        )
    }
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.large,
    ) {
        Box(
            modifier = Modifier
                .background(brush)
                .padding(20.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                content = content,
            )
        }
    }
}
