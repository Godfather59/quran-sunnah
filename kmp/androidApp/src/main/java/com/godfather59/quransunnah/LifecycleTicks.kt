package com.godfather59.quransunnah

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

/**
 * Background-aware polling helpers.
 *
 * The screen tickers used bare `while (true) { delay(...) }` loops, which kept
 * firing (recompute + timestamp math, and in the player's case ExoPlayer
 * reads) while the app was backgrounded. These helpers stop the loop on
 * ON_PAUSE and restart on ON_RESUME, and only tick while [active].
 */

/** True while the host is resumed (foreground). Stops background polling. */
@Composable
fun rememberAppIsResumed(): Boolean {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isResumed by remember { mutableStateOf(true) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isResumed = true
                Lifecycle.Event.ON_PAUSE -> isResumed = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return isResumed
}

/** Fixed-interval tick, re-aligned on start. Returns `System.currentTimeMillis()`. */
@Composable
fun rememberIntervalTicker(
    active: Boolean = true,
    intervalMs: Long = 30_000L,
): Long {
    var tick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val currentActive by rememberUpdatedState(active)
    val isResumed by rememberUpdatedState(rememberAppIsResumed())
    LaunchedEffect(Unit) {
        while (true) {
            delay(intervalMs)
            if (currentActive && isResumed) tick = System.currentTimeMillis()
        }
    }
    return tick
}

/**
 * Ticker aligned to the next wall-clock minute boundary — used for the Home
 * prayer countdown, which must roll over exactly when the minute changes.
 * Stops ticking while [active] is false or the app is backgrounded.
 */
@Composable
fun rememberMinuteTicker(
    active: Boolean = true,
): Long {
    var tick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val currentActive by rememberUpdatedState(active)
    val isResumed by rememberUpdatedState(rememberAppIsResumed())
    LaunchedEffect(Unit) {
        while (true) {
            val untilNextMinute = 60_000L - (System.currentTimeMillis() % 60_000L)
            delay(untilNextMinute)
            if (currentActive && isResumed) tick = System.currentTimeMillis()
        }
    }
    return tick
}
