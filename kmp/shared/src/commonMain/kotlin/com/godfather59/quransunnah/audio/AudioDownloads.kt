package com.godfather59.quransunnah.audio

import com.godfather59.quransunnah.util.CommonLock

// Per-surah audio download model + queue. Ported from AudioDownloadTask /
// queueSurahDownload / pause-resume-cancel in audio_service.dart.
// I/O (HTTP, files) lives behind AudioDownloader (platform); everything
// here is pure and unit-tested.

enum class AudioDownloadStatus {
    QUEUED,
    MEASURING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELED,
}

data class AudioDownloadTask(
    val reciterId: String,
    val surah: Int,
    val status: AudioDownloadStatus,
    val progress: Double = 0.0,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val startedAtMs: Long? = null,
    val error: String? = null,
) {
    val key: String get() = "$reciterId:$surah"

    /**
     * Estimated remaining seconds from observed throughput, null if unknown.
     * Null while paused/canceled/failed so wall-clock pauses never inflate
     * the ETA (Dart etaSeconds parity). [nowMs] is injected for tests.
     */
    fun etaSeconds(nowMs: Long): Int? {
        if (status != AudioDownloadStatus.DOWNLOADING) return null
        val started = startedAtMs
        if (started == null || downloadedBytes <= 0 || totalBytes <= 0) {
            return null
        }
        val elapsed = (nowMs - started) / 1000.0
        if (elapsed < 1) return null
        val speed = downloadedBytes / elapsed
        if (speed <= 0) return null
        val remaining = totalBytes - downloadedBytes
        if (remaining <= 0) return 0
        return (remaining / speed).toInt()
    }
}

/** Statuses that block re-queueing (Dart queueSurahDownload guard). */
fun isDownloadActive(status: AudioDownloadStatus): Boolean = when (status) {
    AudioDownloadStatus.QUEUED,
    AudioDownloadStatus.DOWNLOADING,
    AudioDownloadStatus.MEASURING,
    AudioDownloadStatus.PAUSED,
    -> true
    AudioDownloadStatus.COMPLETED,
    AudioDownloadStatus.FAILED,
    AudioDownloadStatus.CANCELED,
    -> false
}

/**
 * Shifts the throughput window past a pause (Dart resumeDownload parity):
 * startedAtMs moves forward by the paused duration so ETA excludes it.
 */
fun shiftStartedAt(
    startedAtMs: Long?,
    pausedAtMs: Long?,
    nowMs: Long,
): Long? {
    if (pausedAtMs == null || startedAtMs == null) return startedAtMs
    return startedAtMs + (nowMs - pausedAtMs)
}

/** Single surah item waiting for the drain loop. */
data class QueuedSurah(
    val reciterId: String,
    val surah: Int,
    val totalBytes: Long,
)

/** FIFO queue with cancel/pause sets (Dart _downloadQueue parity). */
class DownloadQueue {
    private val queue = ArrayDeque<QueuedSurah>()
    private val cancelled = mutableSetOf<String>()
    private val paused = mutableSetOf<String>()
    // KMP is multi-threaded (IO drain + Main UI): guard all mutable state.
    // CommonLock is expect/actual (synchronized on JVM/Android, NSRecursiveLock on iOS).
    private val lock = CommonLock()

    fun enqueue(item: QueuedSurah) {
        lock.withLock {
            // Re-queue after cancel must work: a fresh enqueue consumes the
            // stale cancelled flag. Without this the key is skipped forever.
            cancelled.remove("${item.reciterId}:${item.surah}")
            // Avoid double-download of the same key already waiting.
            if (queue.none { it.reciterId == item.reciterId && it.surah == item.surah }) {
                queue.addLast(item)
            }
        }
    }

    fun cancel(key: String) {
        lock.withLock {
            cancelled.add(key)
            paused.remove(key)
        }
    }

    /** Clears a stale cancelled flag (e.g. after the UI has observed it). */
    fun consumeCancel(key: String) {
        lock.withLock {
            cancelled.remove(key)
        }
    }

    fun pause(key: String) {
        lock.withLock {
            paused.add(key)
        }
    }

    fun resume(key: String) {
        lock.withLock {
            paused.remove(key)
        }
    }

    fun isPaused(key: String): Boolean = lock.withLock { key in paused }

    fun isCancelled(key: String): Boolean = lock.withLock { key in cancelled }

    fun pendingCount(): Int = lock.withLock { queue.size }

    /** Next non-cancelled item, or null when drained. */
    fun next(): QueuedSurah? {
        lock.withLock {
            while (queue.isNotEmpty()) {
                val item = queue.removeFirst()
                if (cancelled.contains("${item.reciterId}:${item.surah}")) continue
                return item
            }
            return null
        }
    }
}
