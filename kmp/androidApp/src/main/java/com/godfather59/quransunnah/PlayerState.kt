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
import com.godfather59.quransunnah.audio.AudioPlayer
import com.godfather59.quransunnah.audio.AyahAudioItem
import com.godfather59.quransunnah.audio.PlayerListener
import kotlinx.coroutines.delay

/**
 * Shared playback UI state. Centralizes the [PlayerListener] wiring + position
 * polling that was previously duplicated in MiniPlayer and AudioPlayerScreen.
 *
 * ExoPlayer does not push position updates, so polling is required — but it
 * runs only while [isPlaying] is true (plus one refresh on pause/seek so the
 * bar never sticks). Callers just read [PlayerUiState].
 */
data class PlayerUiState(
    val isPlaying: Boolean = false,
    val currentItem: AyahAudioItem? = null,
    val currentRefKey: String? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
) {
    val progress: Float =
        if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val isVisible: Boolean = isPlaying || currentItem != null
}

@Composable
fun rememberPlayerState(
    player: AudioPlayer?,
    positionPollMs: Long = 500L,
): PlayerUiState {
    var isPlaying by remember(player) { mutableStateOf(false) }
    var currentItem by remember(player) { mutableStateOf<AyahAudioItem?>(null) }
    var currentRefKey by remember(player) { mutableStateOf<String?>(null) }
    var positionMs by remember(player) { mutableLongStateOf(0L) }
    var durationMs by remember(player) { mutableLongStateOf(0L) }
    // ExoPlayer has no position callback, so polling is required — but only
    // while playing AND foreground (never while the app is backgrounded).
    val isResumed by rememberUpdatedState(rememberAppIsResumed())

    DisposableEffect(player) {
        if (player == null) return@DisposableEffect onDispose {}
        val listener = object : PlayerListener {
            override fun onPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onCurrentRefKey(refKey: String) {
                currentRefKey = refKey
            }

            override fun onCurrentItem(item: AyahAudioItem?) {
                currentItem = item
            }
        }
        player.setListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(player, isPlaying, isResumed) {
        if (player == null) return@LaunchedEffect
        if (!isPlaying || !isResumed) {
            try {
                positionMs = player.positionMs()
                durationMs = player.durationMs()
            } catch (_: Exception) {
            }
            return@LaunchedEffect
        }
        while (true) {
            try {
                positionMs = player.positionMs()
                durationMs = player.durationMs()
            } catch (_: Exception) {
            }
            delay(positionPollMs)
        }
    }

    return remember(isPlaying, currentItem, currentRefKey, positionMs, durationMs) {
        PlayerUiState(isPlaying, currentItem, currentRefKey, positionMs, durationMs)
    }
}
