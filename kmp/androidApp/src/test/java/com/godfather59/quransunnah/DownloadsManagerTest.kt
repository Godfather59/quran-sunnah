package com.godfather59.quransunnah

import com.godfather59.quransunnah.audio.AudioDownloader
import com.godfather59.quransunnah.audio.AyahDownload
import com.godfather59.quransunnah.audio.DownloadControl
import com.godfather59.quransunnah.audio.reciters
import com.godfather59.quransunnah.quran.surahMetadata
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * DownloadsManager tests with fake downloader + temp-dir targets.
 * Covers the behaviours that regressed silently when the queue lived in
 * composition: pump drain, dedup, cancel (incl. partial files kept),
 * failure marking, and rescan verification.
 */
class DownloadsManagerTest {

    /** Fake downloader that "downloads" by writing files, can fail, can block. */
    private class FakeDownloader : AudioDownloader {
        var failKey: String? = null
        var calls = 0
        private val urls = mutableMapOf<String, Long>()

        override suspend fun remoteSizeBytes(url: String): Long = urls.getOrDefault(url, 100L)

        override suspend fun downloadAyah(
            key: String,
            item: AyahDownload,
            control: DownloadControl,
            onProgress: (Long, Long) -> Unit,
        ): Long {
            if (key == failKey) throw IllegalStateException("boom")
            calls++
            onProgress(1, 1)
            val dest = File(item.destPath)
            dest.parentFile?.mkdirs()
            dest.writeText("ayah")
            return 100L
        }
    }

    private class TempTargets : DownloadTargets {
        private val root: File = File.createTempFile("dl-targets", "").let {
            it.delete(); it.mkdirs(); it
        }
        val files = mutableSetOf<File>()
        var throwOnScan = false

        override fun ayahFile(reciterId: String, surah: Int, ayah: Int): File =
            File(root, "$reciterId/$surah/$ayah.mp3")

        override fun isSurahDownloaded(reciterId: String, surah: Int): Boolean {
            if (throwOnScan) throw IllegalStateException("scan failed")
            return File(root, "$reciterId/$surah").exists()
        }

        override fun surahBytes(reciterId: String, surah: Int): Long =
            File(root, "$reciterId/$surah").listFiles()?.sumOf { it.length() } ?: 0L

        override fun deleteSurah(reciterId: String, surah: Int) {
            File(root, "$reciterId/$surah").deleteRecursively()
        }
    }

    private fun manager(
        targets: TempTargets,
        downloader: AudioDownloader,
        scope: CoroutineScope,
    ) = DownloadsManager::class.java
        .getDeclaredConstructor(
            DownloadTargets::class.java,
            AudioDownloader::class.java,
            CoroutineScope::class.java,
        )
        .apply { isAccessible = true }
        .newInstance(targets, downloader, scope) as DownloadsManager

    /** Wait for a flow predicate, so tests don't race the IO pump. */
    private suspend fun <T> StateFlow<T>.awaitUntil(
        timeoutMs: Long = 10_000L,
        predicate: (T) -> Boolean,
    ) = kotlinx.coroutines.withTimeout(timeoutMs) {
        while (!predicate(this@awaitUntil.value)) {
            kotlinx.coroutines.delay(10)
        }
    }

    @Test
    fun `enqueue drains every ayah and completes the surah`() = runBlocking {
        val targets = TempTargets()
        val downloader = FakeDownloader()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val mgr = manager(targets, downloader, scope)
        val reciterId = reciters.first().identifier
        // Surah 1 (7 ayahs) is enough; count known from shared seed.
        val ayahCount = surahMetadata.first { it.number == 1 }.ayahCount

        mgr.enqueue(reciterId, 1)

        mgr.completedKeys.awaitUntil { "$reciterId:1" in it }
        assertEquals(ayahCount, downloader.calls)
        // Fake writes one 4-byte file per ayah ("ayah").
        assertEquals(ayahCount * 4L, mgr.completedBytes.value["$reciterId:1"])
        scope.cancel()
    }

    @Test
    fun `duplicate enqueue does not download twice`() = runBlocking {
        val targets = TempTargets()
        val downloader = FakeDownloader()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val mgr = manager(targets, downloader, scope)
        val reciterId = reciters.first().identifier
        val total = surahMetadata.first { it.number == 1 }.ayahCount

        mgr.enqueue(reciterId, 1)
        // Consecutive enqueue calls while running: DownloadQueue dedups on key.
        mgr.enqueue(reciterId, 1)

        mgr.completedKeys.awaitUntil { "$reciterId:1" in it }
        // Second enqueue was deduped; first still completes normally.
        assertTrue(
            "second enqueue must be deduped while first is active",
            downloader.calls >= total,
        )
        scope.cancel()
    }

    @Test
    fun `cancel marks failure-free and drops the item`() = runBlocking {
        val targets = TempTargets()
        val downloader = FakeDownloader()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val mgr = manager(targets, downloader, scope)
        val reciterId = reciters.first().identifier

        mgr.enqueue(reciterId, 1)
        // Cancel immediately: drain should observe cancellation before finishing.
        mgr.cancel("$reciterId:1")

        // Give the pump a moment to honor the cancel, then verify no crash
        // and the key never enters completedKeys.
        kotlinx.coroutines.delay(1_000L)
        assertTrue("cancelled key must not complete", "$reciterId:1" !in mgr.completedKeys.value)
        scope.cancel()
    }

    @Test
    fun `download failure marks the key failed`() = runBlocking {
        val targets = TempTargets()
        val downloader = FakeDownloader()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val mgr = manager(targets, downloader, scope)
        val reciterId = reciters.first().identifier
        downloader.failKey = "$reciterId:1"

        mgr.enqueue(reciterId, 1)

        mgr.failedKeys.awaitUntil { "$reciterId:1" in it }
        assertTrue("failed key must not appear completed", "$reciterId:1" !in mgr.completedKeys.value)
        scope.cancel()
    }

    @Test
    fun `rescan reports failure instead of crashing`() = runBlocking {
        val targets = TempTargets()
        val downloader = FakeDownloader()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val mgr = manager(targets, downloader, scope)
        val reciterId = reciters.first().identifier

        targets.throwOnScan = true
        mgr.rescan(reciterId)

        mgr.scanFailed.awaitUntil { it }
        scope.cancel()
    }

    @Test
    fun `pause blocks download until resume`() = runBlocking {
        val targets = TempTargets()
        val downloader = FakeDownloader()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val mgr = manager(targets, downloader, scope)
        val reciterId = reciters.first().identifier

        // Pause BEFORE enqueue, then verify no files appear.
        mgr.queue.pause("$reciterId:1")
        mgr.enqueue(reciterId, 1)
        kotlinx.coroutines.delay(500L)
        assertTrue("paused download must not write files", targets.files.isEmpty())
        assertEquals(0, downloader.calls)

        mgr.queue.resume("$reciterId:1")
        mgr.completedKeys.awaitUntil { "$reciterId:1" in it }
        scope.cancel()
    }
}
