package com.godfather59.quransunnah.audio

// Byte-transfer contract. Platform implementations stream with resume
// (HTTP Range) into a ".part" file and rename atomically on completion.

/** Observed by the transfer loop; backed by DownloadQueue sets. */
interface DownloadControl {
    fun isCancelled(key: String): Boolean
    fun isPaused(key: String): Boolean

    /** Suspends while paused; throws on cancel. */
    suspend fun waitWhilePaused(key: String)
}

data class AyahDownload(
    val url: String,
    /** Final destination path (platform file space). */
    val destPath: String,
    val resumeFromBytes: Long = 0L,
)

interface AudioDownloader {
    /** HEAD-style size probe in bytes, 0 when unknown. */
    suspend fun remoteSizeBytes(url: String): Long

    /**
     * Downloads one ayah file with resume. Calls [onProgress] with
     * (downloadedBytes, totalBytes). Returns final size on completion.
     */
    suspend fun downloadAyah(
        key: String,
        item: AyahDownload,
        control: DownloadControl,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): Long
}

class DownloadCancelledException(key: String) :
    Exception("Download cancelled: $key")
