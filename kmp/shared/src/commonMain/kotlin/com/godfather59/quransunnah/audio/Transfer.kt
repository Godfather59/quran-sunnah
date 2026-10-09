package com.godfather59.quransunnah.audio

// Resumable single-file transfer loop. Pure orchestration over injected
// I/O so it is unit-testable; platform code supplies HTTP + files.
// Mirrors the Dart downloader: Range resume, restart when the server
// ignores Range, progress callbacks, cancel via exception.

/** Sequential byte source positioned at [from]. -1 from read() = EOF. */
interface ByteChunkStream {
    fun read(buffer: ByteArray): Int
    fun close()
}

data class OpenedRange(
    val stream: ByteChunkStream,
    /** False when the server ignored Range (restart from scratch). */
    val resumed: Boolean,
    /** Total file size, or -1 when unknown. */
    val totalBytes: Long,
)

interface RangeOpener {
    suspend fun open(url: String, from: Long): OpenedRange
    suspend fun close(stream: ByteChunkStream) {
        stream.close()
    }
}

/**
 * Downloads [url] into [writeChunk], resuming from [alreadyHave] bytes.
 * Calls [onRestart] first when the server ignored Range (caller truncates).
 * Throws [DownloadCancelledException] on cancel (caller clears staging).
 * Returns total downloaded bytes.
 */
suspend fun runResumableDownload(
    key: String,
    url: String,
    alreadyHave: Long,
    control: DownloadControl,
    opener: RangeOpener,
    onRestart: () -> Unit,
    writeChunk: (bytes: ByteArray, length: Int) -> Unit,
    onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    bufferSize: Int = 32 * 1024,
): Long {
    require(bufferSize > 0) { "bufferSize must be > 0" }
    require(alreadyHave >= 0) { "alreadyHave must be >= 0" }
    val opened = opener.open(url, alreadyHave)
    try {
        var downloaded = alreadyHave
        if (alreadyHave > 0 && !opened.resumed) {
            onRestart()
            downloaded = 0L
        }
        val buffer = ByteArray(bufferSize)
        while (true) {
            if (control.isCancelled(key)) {
                throw DownloadCancelledException(key)
            }
            control.waitWhilePaused(key)
            val read = opened.stream.read(buffer)
            if (read < 0) break
            // Defensive: InputStream with a non-empty buffer should never
            // return 0, but a 0 must not spin the loop or corrupt progress.
            if (read == 0) continue
            require(read <= buffer.size) { "read $read exceeds buffer ${buffer.size}" }
            writeChunk(buffer, read)
            downloaded += read
            onProgress(downloaded, opened.totalBytes)
        }
        return downloaded
    } finally {
        opener.close(opened.stream)
    }
}
