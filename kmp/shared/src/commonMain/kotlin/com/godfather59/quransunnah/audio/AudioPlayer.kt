package com.godfather59.quransunnah.audio

// Playback contract. Platform implementations: Media3/ExoPlayer (Android,
// Phase 3), AVPlayer (iOS, Phase 6). A-B repeat maps to repeat modes;
// background session + lock-screen metadata are app-layer concerns.

enum class RepeatMode { OFF, ONE, ALL }

data class AyahAudioItem(
    /** Stream or local-file URL. */
    val url: String,
    /** "surah:ayah" canonical ref. */
    val refKey: String,
    val title: String,
    val artist: String,
    val album: String,
    /** Verified Quran edition id (for loading display text in UI). */
    val editionId: String = "",
)

interface PlayerListener {
    fun onPlayingChanged(playing: Boolean)
    fun onCurrentRefKey(refKey: String)
    fun onCurrentItem(item: AyahAudioItem?)
}

interface AudioPlayer {
    /** Adds a listener (null clears all — prefer [removeListener]). */
    fun setListener(listener: PlayerListener?)

    /** Removes a previously added listener. */
    fun removeListener(listener: PlayerListener)
    suspend fun setAyahSources(items: List<AyahAudioItem>)
    suspend fun play()
    suspend fun pause()
    suspend fun stop()
    suspend fun next()
    suspend fun previous()
    /** Seek within the current ayah. No-op when duration is unknown. */
    suspend fun seekTo(positionMs: Long)
    /** Current position in ms, or 0 when unknown. Main-thread safe. */
    fun positionMs(): Long
    /** Current ayah duration in ms, or 0 when unknown. Main-thread safe. */
    fun durationMs(): Long
    suspend fun setSpeed(speed: Float)
    suspend fun setRepeatMode(mode: RepeatMode)
    /** Null duration cancels the timer. */
    fun sleepTimer(durationMs: Long?)
    fun release()
}
