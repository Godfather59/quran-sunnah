package com.godfather59.quransunnah

import android.content.Context
import com.godfather59.quransunnah.audio.AudioDownloader
import com.godfather59.quransunnah.audio.AyahDownload
import com.godfather59.quransunnah.audio.DownloadCancelledException
import com.godfather59.quransunnah.audio.DownloadControl
import com.godfather59.quransunnah.audio.DownloadQueue
import com.godfather59.quransunnah.audio.QueuedSurah
import com.godfather59.quransunnah.audio.globalAyahNumber
import com.godfather59.quransunnah.audio.reciterById
import com.godfather59.quransunnah.quran.surahMetadata
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class QueueDownloadControl(
    private val queue: DownloadQueue,
) : DownloadControl {
    override fun isCancelled(key: String): Boolean = queue.isCancelled(key)
    override fun isPaused(key: String): Boolean = queue.isPaused(key)

    override suspend fun waitWhilePaused(key: String) {
        while (queue.isPaused(key) && !queue.isCancelled(key)) {
            delay(200)
        }
        if (queue.isCancelled(key)) throw DownloadCancelledException(key)
    }
}

/**
 * Local-file side of a download, behind an interface so [DownloadsManager]
 * can run on JVM tests with a temp directory instead of an Android
 * `Context`. Keeps the real paths exactly where they were.
 */
interface DownloadTargets {
    fun ayahFile(reciterId: String, surah: Int, ayah: Int): File
    fun isSurahDownloaded(reciterId: String, surah: Int): Boolean
    fun surahBytes(reciterId: String, surah: Int): Long
    fun deleteSurah(reciterId: String, surah: Int)
}

/** Real implementation over the app file space (see AudioFiles). */
class AndroidDownloadTargets(private val context: Context) : DownloadTargets {
    override fun ayahFile(reciterId: String, surah: Int, ayah: Int): File =
        AudioFiles.ayahFile(context, reciterId, surah, ayah)

    override fun isSurahDownloaded(reciterId: String, surah: Int): Boolean =
        AudioFiles.isSurahDownloaded(context, reciterId, surah)

    override fun surahBytes(reciterId: String, surah: Int): Long =
        AudioFiles.surahBytes(context, reciterId, surah)

    override fun deleteSurah(reciterId: String, surah: Int) =
        AudioFiles.deleteSurah(context, reciterId, surah)
}

/**
 * App-scoped download state holder (ViewModel-lite).
 *
 * Previously all of this lived inside `DownloadsScreen` composition:
 * `remember { DownloadQueue() }` died on rotation/tab-leave while the
 * `AppDownloadScope` drain kept running against the orphaned queue.
 * This manager survives UI via [AndroidStores.downloadsManager] and exposes
 * [StateFlow]s the UI collects — no business logic in composition.
 *
 * File access goes through [DownloadTargets] so pump/queue/scan logic is
 * unit-testable on the JVM.
 */
class DownloadsManager private constructor(
    private val targets: DownloadTargets,
    private val audioDownloader: AudioDownloader,
    private val scope: CoroutineScope,
) {
    companion object {
        fun create(
            context: Context,
            downloader: AudioDownloader,
        ): DownloadsManager = DownloadsManager(
            targets = AndroidDownloadTargets(context.applicationContext),
            audioDownloader = downloader,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        )

        @Volatile
        private var instance: DownloadsManager? = null

        fun get(context: Context, downloader: AudioDownloader): DownloadsManager =
            instance ?: synchronized(this) {
                instance ?: create(context, downloader).also { instance = it }
            }
    }

    val queue = DownloadQueue()
    val control: DownloadControl = QueueDownloadControl(queue)

    private val pumpGuard = AtomicBoolean(false)

    private val _activeKey = MutableStateFlow<String?>(null)
    val activeKey: StateFlow<String?> = _activeKey.asStateFlow()

    private val _queuedKeys = MutableStateFlow(emptySet<String>())
    val queuedKeys: StateFlow<Set<String>> = _queuedKeys.asStateFlow()

    private val _pausedKeys = MutableStateFlow(emptySet<String>())
    val pausedKeys: StateFlow<Set<String>> = _pausedKeys.asStateFlow()

    private val _failedKeys = MutableStateFlow(emptySet<String>())
    val failedKeys: StateFlow<Set<String>> = _failedKeys.asStateFlow()

    private val _doneAyahs = MutableStateFlow(emptyMap<String, Int>())
    val doneAyahs: StateFlow<Map<String, Int>> = _doneAyahs.asStateFlow()

    private val _completedKeys = MutableStateFlow(emptySet<String>())
    val completedKeys: StateFlow<Set<String>> = _completedKeys.asStateFlow()

    private val _completedBytes = MutableStateFlow(emptyMap<String, Long>())
    val completedBytes: StateFlow<Map<String, Long>> = _completedBytes.asStateFlow()

    private val _scanLoading = MutableStateFlow(true)
    val scanLoading: StateFlow<Boolean> = _scanLoading.asStateFlow()

    private val _scanFailed = MutableStateFlow(false)
    val scanFailed: StateFlow<Boolean> = _scanFailed.asStateFlow()

    fun enqueue(reciterId: String, surah: Int) {
        val key = "$reciterId:$surah"
        queue.enqueue(QueuedSurah(reciterId, surah, 0L))
        _queuedKeys.value = _queuedKeys.value + key
        _failedKeys.value = _failedKeys.value - key
        pump()
    }

    fun cancel(key: String) {
        queue.cancel(key)
        _queuedKeys.value = _queuedKeys.value - key
        _pausedKeys.value = _pausedKeys.value - key
    }

    fun togglePause(key: String) {
        if (key in _pausedKeys.value) {
            queue.resume(key)
            _pausedKeys.value = _pausedKeys.value - key
        } else {
            queue.pause(key)
            _pausedKeys.value = _pausedKeys.value + key
        }
    }

    fun deleteSurah(reciterId: String, surah: Int, onDone: () -> Unit = {}) {
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    targets.deleteSurah(reciterId, surah)
                } catch (_: Exception) {
                }
            }
            rescan(reciterId)
            onDone()
        }
    }

    fun rescan(reciterId: String) {
        scope.launch {
            _scanLoading.value = true
            _scanFailed.value = false
            try {
                val fresh = withContext(Dispatchers.IO) {
                    val done = mutableSetOf<String>()
                    val sizes = mutableMapOf<String, Long>()
                    for (m in surahMetadata) {
                        if (targets.isSurahDownloaded(reciterId, m.number)) {
                            val k = "$reciterId:${m.number}"
                            done.add(k)
                            sizes[k] = targets.surahBytes(reciterId, m.number)
                        }
                    }
                    done to sizes
                }
                _completedKeys.value = fresh.first
                _completedBytes.value = fresh.second
                _scanLoading.value = false
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _scanFailed.value = true
                _scanLoading.value = false
            }
        }
    }

    private suspend fun downloadSurah(reciterId: String, surah: Int) {
        val key = "$reciterId:$surah"
        val count = surahMetadata.first { it.number == surah }.ayahCount
        val reciter = reciterById(reciterId) ?: return
        for (ayah in 1..count) {
            control.waitWhilePaused(key)
            if (control.isCancelled(key)) throw DownloadCancelledException(key)
            val dest = targets.ayahFile(reciterId, surah, ayah)
            if (dest.exists()) {
                _doneAyahs.value = _doneAyahs.value + (key to ((_doneAyahs.value[key] ?: 0) + 1))
                continue
            }
            val part = File(dest.absolutePath + ".part")
            val resume = try {
                if (part.exists()) part.length() else 0L
            } catch (_: Exception) {
                0L
            }
            audioDownloader.downloadAyah(
                key,
                AyahDownload(
                    url = reciter.fileUrl(globalAyahNumber(surah, ayah)),
                    destPath = dest.absolutePath,
                    resumeFromBytes = resume,
                ),
                control,
            ) { _, _ -> }
            _doneAyahs.value = _doneAyahs.value + (key to ((_doneAyahs.value[key] ?: 0) + 1))
        }
    }

    fun pump() {
        if (!pumpGuard.compareAndSet(false, true)) return
        scope.launch {
            var lastReciter: String? = null
            try {
                while (true) {
                    val item = queue.next() ?: break
                    lastReciter = item.reciterId
                    val key = "${item.reciterId}:${item.surah}"
                    _queuedKeys.value = _queuedKeys.value - key
                    _activeKey.value = key
                    _failedKeys.value = _failedKeys.value - key
                    try {
                        downloadSurah(item.reciterId, item.surah)
                    } catch (_: DownloadCancelledException) {
                        // Partial files stay for resume; nothing to mark.
                    } catch (_: Exception) {
                        _failedKeys.value = _failedKeys.value + key
                    } finally {
                        _activeKey.value = null
                    }
                }
                lastReciter?.let { refreshAfterDrain(it) }
            } finally {
                pumpGuard.set(false)
                if (queue.pendingCount() > 0) pump()
            }
        }
    }

    /** Refresh completed sets after drain for the given reciter. */
    fun refreshAfterDrain(reciterId: String) {
        scope.launch {
            val fresh = withContext(Dispatchers.IO) {
                val done = mutableSetOf<String>()
                val sizes = mutableMapOf<String, Long>()
                for (m in surahMetadata) {
                    if (targets.isSurahDownloaded(reciterId, m.number)) {
                        val k = "$reciterId:${m.number}"
                        done.add(k)
                        sizes[k] = targets.surahBytes(reciterId, m.number)
                    }
                }
                done to sizes
            }
            _completedKeys.value = fresh.first
            _completedBytes.value = fresh.second
        }
    }
}
