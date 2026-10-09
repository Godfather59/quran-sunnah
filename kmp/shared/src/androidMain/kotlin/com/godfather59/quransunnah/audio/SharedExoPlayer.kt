package com.godfather59.quransunnah.audio

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

// Single ExoPlayer shared by the app-layer AudioPlayer and the background
// MediaSessionService. One queue drives the reader UI, the MiniPlayer, the
// foreground notification, and lock-screen controls together.
object SharedExoPlayer {
    @Volatile
    private var instance: ExoPlayer? = null

    fun get(context: Context): ExoPlayer =
        instance ?: synchronized(this) {
            instance ?: ExoPlayer.Builder(context.applicationContext).build()
                .also { instance = it }
        }

    /** Same instance as a generic Player for session wiring. */
    fun player(context: Context): Player = get(context)

    /**
     * Releases the shared instance and clears it so the next [get] builds
     * a fresh player. Without the null-out, any release() permanently kills
     * audio for the whole process (released instance is still returned).
     */
    fun release() {
        synchronized(this) {
            try {
                instance?.release()
            } catch (_: Exception) {
            } finally {
                instance = null
            }
        }
    }
}
