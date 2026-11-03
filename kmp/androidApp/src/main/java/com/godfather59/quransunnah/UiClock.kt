package com.godfather59.quransunnah

import androidx.compose.runtime.staticCompositionLocalOf

/** Null follows the device clock; a supplied instant makes previews reproducible. */
internal val LocalUiClockMillis = staticCompositionLocalOf<Long?> { null }
