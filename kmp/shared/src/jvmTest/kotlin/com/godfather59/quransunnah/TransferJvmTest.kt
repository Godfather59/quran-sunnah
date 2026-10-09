package com.godfather59.quransunnah

import com.godfather59.quransunnah.audio.ByteChunkStream
import com.godfather59.quransunnah.audio.DownloadCancelledException
import com.godfather59.quransunnah.audio.DownloadControl
import com.godfather59.quransunnah.audio.OpenedRange
import com.godfather59.quransunnah.audio.RangeOpener
import com.godfather59.quransunnah.audio.runResumableDownload
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

// Hermetic: in-memory bytes, no network.
class TransferJvmTest {
    private class MemoryStream(
        private val data: ByteArray,
        position: Long,
        private val failAfterReads: Int = -1,
    ) : ByteChunkStream {
        private var pos = position.toInt()
        private var reads = 0

        override fun read(buffer: ByteArray): Int {
            if (failAfterReads >= 0 && reads++ >= failAfterReads) {
                throw IOException("boom")
            }
            if (pos >= data.size) return -1
            val n = minOf(buffer.size, data.size - pos)
            data.copyInto(buffer, 0, pos, pos + n)
            pos += n
            return n
        }

        override fun close() {}
    }

    private class FakeOpener(
        val data: ByteArray,
        val honorRange: Boolean = true,
        val failAfterReads: Int = -1,
    ) : RangeOpener {
        var opens = 0
        var lastFrom = -1L

        override suspend fun open(url: String, from: Long): OpenedRange {
            opens++
            lastFrom = from
            val start = if (honorRange) from else 0L
            return OpenedRange(
                MemoryStream(data, start, failAfterReads),
                honorRange,
                data.size.toLong(),
            )
        }
    }

    private class ManualControl : DownloadControl {
        var paused = false
        var cancelled = false

        override fun isCancelled(key: String): Boolean = cancelled
        override fun isPaused(key: String): Boolean = paused
        override suspend fun waitWhilePaused(key: String) {
            while (paused && !cancelled) delay(10)
            if (cancelled) throw DownloadCancelledException(key)
        }
    }

    private val data = ByteArray(10_000) { (it % 251).toByte() }

    private fun run(
        alreadyHave: Long = 0L,
        opener: FakeOpener = FakeOpener(data),
        control: ManualControl = ManualControl(),
        onProgress: (Long, Long) -> Unit = { _, _ -> },
    ): Triple<Long, ByteArray, Int> {
        val written = mutableListOf<Byte>()
        var restarts = 0
        val total = runBlocking {
            runResumableDownload(
                key = "k",
                url = "https://example.test/f.mp3",
                alreadyHave = alreadyHave,
                control = control,
                opener = opener,
                onRestart = { restarts++ },
                writeChunk = { bytes, len ->
                    for (i in 0 until len) written.add(bytes[i])
                },
                onProgress = onProgress,
            )
        }
        return Triple(total, ByteArray(written.size) { written[it] }, restarts)
    }

    @Test
    fun fullDownload(): Unit {
        val progress = mutableListOf<Pair<Long, Long>>()
        val (total, bytes, restarts) = run(onProgress = { d, t ->
            progress.add(Pair(d, t))
        })
        assertEquals(10_000L, total)
        assertContentEquals(data, bytes)
        assertEquals(0, restarts)
        assertTrue(progress.isNotEmpty())
        assertEquals(Pair(10_000L, 10_000L), progress.last())
        assertTrue(progress.map { it.first }.sorted() == progress.map { it.first })
    }

    @Test
    fun resumeHonored(): Unit {
        val opener = FakeOpener(data, honorRange = true)
        val (total, bytes, restarts) = run(alreadyHave = 4_000L, opener = opener)
        assertEquals(4_000L, opener.lastFrom)
        assertEquals(10_000L, total)
        assertContentEquals(data.copyOfRange(4_000, 10_000), bytes)
        assertEquals(0, restarts)
    }

    @Test
    fun restartWhenRangeIgnored(): Unit {
        val opener = FakeOpener(data, honorRange = false)
        val (total, bytes, restarts) = run(alreadyHave = 4_000L, opener = opener)
        assertEquals(10_000L, total)
        assertContentEquals(data, bytes)
        assertEquals(1, restarts)
    }

    @Test
    fun cancelThrows(): Unit {
        val control = ManualControl().apply { cancelled = true }
        assertFailsWith<DownloadCancelledException> {
            runBlocking {
                runResumableDownload(
                    key = "k", url = "u", alreadyHave = 0L,
                    control = control, opener = FakeOpener(data),
                    onRestart = {}, writeChunk = { _, _ -> },
                    onProgress = { _, _ -> },
                )
            }
        }
    }

    @Test
    fun streamFailurePropagates(): Unit {
        assertFailsWith<IOException> {
            run(opener = FakeOpener(data, failAfterReads = 0))
        }
    }

    @Test
    fun pauseBlocksUntilResumed(): Unit = runBlocking {
        val control = ManualControl().apply { paused = true }
        val written = mutableListOf<Byte>()
        val job = async {
            runResumableDownload(
                key = "k", url = "u", alreadyHave = 0L,
                control = control, opener = FakeOpener(data),
                onRestart = {},
                writeChunk = { bytes, len ->
                    for (i in 0 until len) written.add(bytes[i])
                },
                onProgress = { _, _ -> },
            )
        }
        delay(100)
        assertTrue(written.isEmpty())
        control.paused = false
        assertEquals(10_000L, job.await())
        assertEquals(10_000, written.size)
    }
}
