package com.godfather59.quransunnah

import com.godfather59.quransunnah.audio.AudioDownloadStatus
import com.godfather59.quransunnah.audio.AudioDownloadTask
import com.godfather59.quransunnah.audio.DownloadQueue
import com.godfather59.quransunnah.audio.QueuedSurah
import com.godfather59.quransunnah.audio.audioDirFor
import com.godfather59.quransunnah.audio.audioDownloadKey
import com.godfather59.quransunnah.audio.audioFileName
import com.godfather59.quransunnah.audio.globalAyahNumber
import com.godfather59.quransunnah.audio.isDownloadActive
import com.godfather59.quransunnah.audio.shiftStartedAt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AudioLogicTest {
    @Test
    fun globalAyahVectors() {
        // From Dart search_audio_test (global ayah numbering anchors CDN).
        assertEquals(1, globalAyahNumber(1, 1))
        assertEquals(7, globalAyahNumber(1, 7))
        assertEquals(8, globalAyahNumber(2, 1))
        assertEquals(6236, globalAyahNumber(114, 6))
        assertFailsWith<IllegalArgumentException> { globalAyahNumber(0, 1) }
        assertFailsWith<IllegalArgumentException> { globalAyahNumber(115, 1) }
        assertFailsWith<IllegalArgumentException> { globalAyahNumber(2, 287) }
    }

    @Test
    fun cachePathsAndKeys() {
        // From Dart audio cache path tests.
        assertEquals("audio/ar.alafasy/112", audioDirFor("ar.alafasy", 112))
        assertEquals("4.mp3", audioFileName(4))
        assertEquals("ar.alafasy:112", audioDownloadKey("ar.alafasy", 112))
        assertEquals(
            "ar.alafasy:112",
            AudioDownloadTask("ar.alafasy", 112, AudioDownloadStatus.QUEUED).key,
        )
    }

    @Test
    fun etaSecondsRules() {
        fun task(status: AudioDownloadStatus) = AudioDownloadTask(
            reciterId = "r", surah = 1, status = status,
            totalBytes = 1000, downloadedBytes = 500, startedAtMs = 0,
        )
        // 500 bytes in 10s -> 50 B/s -> 500 left -> 10s.
        assertEquals(
            10,
            task(AudioDownloadStatus.DOWNLOADING).etaSeconds(10_000),
        )
        // Non-downloading statuses never report ETA (no wall-clock inflation).
        assertNull(task(AudioDownloadStatus.PAUSED).etaSeconds(10_000))
        assertNull(task(AudioDownloadStatus.FAILED).etaSeconds(10_000))
        assertNull(task(AudioDownloadStatus.CANCELED).etaSeconds(10_000))
        assertNull(task(AudioDownloadStatus.QUEUED).etaSeconds(10_000))
        // Unknown totals/speeds.
        assertNull(
            task(AudioDownloadStatus.DOWNLOADING)
                .copy(totalBytes = 0).etaSeconds(10_000),
        )
        assertNull(
            task(AudioDownloadStatus.DOWNLOADING).etaSeconds(500),
        )
        // Finished.
        assertEquals(
            0,
            task(AudioDownloadStatus.DOWNLOADING)
                .copy(downloadedBytes = 1000).etaSeconds(10_000),
        )
    }

    @Test
    fun activeStatusesBlockRequeue() {
        assertTrue(isDownloadActive(AudioDownloadStatus.QUEUED))
        assertTrue(isDownloadActive(AudioDownloadStatus.DOWNLOADING))
        assertTrue(isDownloadActive(AudioDownloadStatus.MEASURING))
        assertTrue(isDownloadActive(AudioDownloadStatus.PAUSED))
        assertFalse(isDownloadActive(AudioDownloadStatus.COMPLETED))
        assertFalse(isDownloadActive(AudioDownloadStatus.FAILED))
        assertFalse(isDownloadActive(AudioDownloadStatus.CANCELED))
    }

    @Test
    fun pauseShiftsThroughputWindow() {
        // Started at 0, paused at 10s, resumed at 30s -> window shifts +20s.
        assertEquals(20_000L, shiftStartedAt(0L, 10_000L, 30_000L))
        assertEquals(5L, shiftStartedAt(5L, null, 30_000L))
        assertNull(shiftStartedAt(null, 10_000L, 30_000L))
    }

    @Test
    fun downloadQueueSkipsCancelled() {
        val q = DownloadQueue()
        q.enqueue(QueuedSurah("r", 1, 100))
        q.enqueue(QueuedSurah("r", 2, 100))
        q.cancel("r:1")
        assertEquals(2, q.next()?.surah)
        assertEquals(null, q.next())
        assertEquals(0, q.pendingCount())
        assertTrue(q.isCancelled("r:1"))
    }

    @Test
    fun downloadQueuePauseResume() {
        val q = DownloadQueue()
        assertFalse(q.isPaused("r:1"))
        q.pause("r:1")
        assertTrue(q.isPaused("r:1"))
        q.resume("r:1")
        assertFalse(q.isPaused("r:1"))
        q.pause("r:1")
        q.cancel("r:1")
        assertFalse(q.isPaused("r:1"))
    }

    @Test
    fun downloadQueueRequeueAfterCancel() {
        val q = DownloadQueue()
        q.enqueue(QueuedSurah("r", 1, 100))
        q.cancel("r:1")
        // Cancelled item is skipped...
        assertEquals(null, q.next())
        // ...but a fresh enqueue consumes the stale flag and runs again.
        q.enqueue(QueuedSurah("r", 1, 100))
        assertFalse(q.isCancelled("r:1"))
        assertEquals(1, q.next()?.surah)
    }

    @Test
    fun downloadQueueDedupsWaiting() {
        val q = DownloadQueue()
        q.enqueue(QueuedSurah("r", 1, 100))
        q.enqueue(QueuedSurah("r", 1, 100))
        assertEquals(1, q.pendingCount())
    }
}
