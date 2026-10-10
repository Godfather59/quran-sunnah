package com.godfather59.quransunnah.device

import com.godfather59.quransunnah.audio.AudioDownloader
import com.godfather59.quransunnah.audio.AudioPlayer
import com.godfather59.quransunnah.audio.AyahAudioItem
import com.godfather59.quransunnah.audio.AyahDownload
import com.godfather59.quransunnah.audio.DownloadControl
import com.godfather59.quransunnah.audio.PlayerListener
import com.godfather59.quransunnah.audio.RepeatMode

// Phase 6 iOS platform services. Fail-soft (no-op / null) so missing
// AVPlayer/CoreLocation/UIKit wiring can never crash shared logic; the
// SwiftUI app layer replaces these with real implementations on macOS.
// Every method never throws.
class IosAudioPlayer : AudioPlayer {
    override fun setListener(listener: PlayerListener?) {}
    override fun removeListener(listener: PlayerListener) {}
    override suspend fun setAyahSources(items: List<AyahAudioItem>) {}
    override suspend fun play() {}
    override suspend fun pause() {}
    override suspend fun stop() {}
    override suspend fun next() {}
    override suspend fun previous() {}
    override suspend fun seekTo(positionMs: Long) {}
    override fun positionMs(): Long = 0L
    override fun durationMs(): Long = 0L
    override suspend fun setSpeed(speed: Float) {}
    override suspend fun setRepeatMode(mode: RepeatMode) {}
    override fun sleepTimer(durationMs: Long?) {}
    override fun release() {}
}

class IosAudioDownloader : AudioDownloader {
    override suspend fun remoteSizeBytes(url: String): Long = 0L
    override suspend fun downloadAyah(
        key: String,
        item: AyahDownload,
        control: DownloadControl,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): Long = 0L
}

class IosLocator : Locator {
    override suspend fun currentFix(timeoutMs: Long): GpsFix? = null
}

class IosCompass : Compass {
    override fun start(listener: (Float) -> Unit) {}
    override fun stop() {}
}

class IosSharer : Sharer {
    override fun shareText(text: String, subject: String?) {}
    override fun shareFile(path: String, mimeType: String) {}
}

class IosClipboard : Clipboard {
    override fun copy(text: String) {}
}

class IosBackupFilePicker : BackupFilePicker {
    override suspend fun pickJsonBytes(): ByteArray? = null
}
