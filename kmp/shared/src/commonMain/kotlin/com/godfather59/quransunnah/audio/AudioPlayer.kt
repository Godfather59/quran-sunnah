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
)

interface PlayerListener {
    fun onPlayingChanged(playing: Boolean)
    fun onCurrentRefKey(refKey: String)
    fun onCurrentItem(item: AyahAudioItem?)
}

interface AudioPlayer {
    fun setListener(listener: PlayerListener?)
    suspend fun setAyahSources(items: List<AyahAudioItem>)
    suspend fun play()
    suspend fun pause()
    suspend fun stop()
    suspend fun setSpeed(speed: Float)
    suspend fun setRepeatMode(mode: RepeatMode)
    /** Null duration cancels the timer. */
    fun sleepTimer(durationMs: Long?)
    fun release()
}
