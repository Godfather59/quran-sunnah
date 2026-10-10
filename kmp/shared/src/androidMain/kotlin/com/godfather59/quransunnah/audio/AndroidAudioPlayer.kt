package com.godfather59.quransunnah.audio

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

// Media3/ExoPlayer implementation of AudioPlayer. Uses the shared ExoPlayer
// so the app-layer MediaSessionService (background notification + lock-screen
// metadata) and the UI stay on one queue. Call suspend methods from the main
// thread (app-layer convention).
class AndroidAudioPlayer(context: Context) : AudioPlayer {
    private val player: ExoPlayer = SharedExoPlayer.get(context)
    private val listeners = mutableSetOf<PlayerListener>()
    private val sleepHandler = Handler(Looper.getMainLooper())
    private var sleepRunnable: Runnable? = null
    private var currentItem: AyahAudioItem? = null
    private var playlistItems: List<AyahAudioItem> = emptyList()

    init {
        player.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    listeners.toList().forEach { it.onPlayingChanged(isPlaying) }
                }

                override fun onMediaItemTransition(
                    mediaItem: MediaItem?,
                    reason: Int,
                ) {
                    val id = mediaItem?.mediaId
                    if (id != null) {
                        currentItem = findItemByRefKey(id)
                        listeners.toList().forEach {
                            it.onCurrentRefKey(id)
                            it.onCurrentItem(currentItem)
                        }
                    } else {
                        currentItem = null
                        listeners.toList().forEach { it.onCurrentItem(null) }
                    }
                }
            },
        )
    }

    private fun findItemByRefKey(refKey: String): AyahAudioItem? {
        return playlistItems.firstOrNull { it.refKey == refKey }
    }

    override fun setListener(listener: PlayerListener?) {
        if (listener == null) {
            listeners.clear()
        } else {
            listeners.add(listener)
        }
    }

    override fun removeListener(listener: PlayerListener) {
        listeners.remove(listener)
    }

    override suspend fun setAyahSources(items: List<AyahAudioItem>) {
        playlistItems = items
        player.setMediaItems(
            items.map { item ->
                MediaItem.Builder()
                    .setUri(item.url)
                    .setMediaId(item.refKey)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(item.title)
                            .setArtist(item.artist)
                            .setAlbumTitle(item.album)
                            .build(),
                    )
                    .build()
            },
        )
        player.prepare()
    }

    override suspend fun play() {
        player.play()
    }

    override suspend fun pause() {
        player.pause()
    }

    override suspend fun stop() {
        player.stop()
        // Stopped = nothing current: MiniPlayer hides, player screen resets.
        currentItem = null
        listeners.toList().forEach {
            it.onPlayingChanged(false)
            it.onCurrentItem(null)
        }
    }

    override suspend fun next() {
        if (player.hasNextMediaItem()) player.seekToNextMediaItem()
    }

    override suspend fun previous() {
        if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem()
    }

    override suspend fun seekTo(positionMs: Long) {
        val duration = player.duration
        if (duration != C.TIME_UNSET && duration > 0) {
            player.seekTo(positionMs.coerceIn(0, duration))
        }
    }

    override fun positionMs(): Long {
        return try {
            player.currentPosition.coerceAtLeast(0)
        } catch (_: Exception) {
            0L
        }
    }

    override fun durationMs(): Long {
        return try {
            val d = player.duration
            if (d == C.TIME_UNSET || d <= 0) 0L else d
        } catch (_: Exception) {
            0L
        }
    }

    override suspend fun setSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    override suspend fun setRepeatMode(mode: RepeatMode) {
        player.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    override fun sleepTimer(durationMs: Long?) {
        sleepRunnable?.let {
            sleepHandler.removeCallbacks(it)
            sleepRunnable = null
        }
        if (durationMs != null) {
            val task = Runnable { player.pause() }
            sleepRunnable = task
            sleepHandler.postDelayed(task, durationMs)
        }
    }

    override fun release() {
        sleepTimer(null)
        try {
            player.stop()
            player.clearMediaItems()
        } catch (_: Exception) {
        }
        // Do NOT release the shared ExoPlayer here: it also drives the
        // foreground MediaSessionService. Use SharedExoPlayer.release()
        // only at process teardown.
    }
}
