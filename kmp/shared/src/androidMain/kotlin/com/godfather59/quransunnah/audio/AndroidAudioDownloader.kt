package com.godfather59.quransunnah.audio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

// Resumable ayah-file downloads over HttpURLConnection.
// Range resume + ".part" staging + atomic rename on completion,
// cancel clears staging (mirrors the Flutter downloader).

private const val CONNECT_TIMEOUT_MS = 15_000
private const val READ_TIMEOUT_MS = 30_000

private class UrlConnectionStream(
    private val connection: HttpURLConnection,
) : ByteChunkStream {
    override fun read(buffer: ByteArray): Int {
        return try {
            connection.inputStream.read(buffer)
        } catch (_: Exception) {
            throw IOException("stream failed")
        }
    }

    override fun close() {
        try {
            connection.inputStream.close()
        } catch (_: Exception) {
        }
        connection.disconnect()
    }
}

private object UrlRangeOpener : RangeOpener {
    override suspend fun open(url: String, from: Long): OpenedRange {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        connection.instanceFollowRedirects = true
        if (from > 0) {
            connection.setRequestProperty("Range", "bytes=$from-")
        }
        connection.connect()
        val code = connection.responseCode
        if (code != HttpURLConnection.HTTP_OK &&
            code != HttpURLConnection.HTTP_PARTIAL
        ) {
            connection.disconnect()
            throw IOException("HTTP $code for $url")
        }
        val remaining = connection.getHeaderFieldLong("Content-Length", -1L)
        return OpenedRange(
            stream = UrlConnectionStream(connection),
            resumed = code == HttpURLConnection.HTTP_PARTIAL,
            totalBytes = if (remaining >= 0 && code == HttpURLConnection.HTTP_PARTIAL) {
                from + remaining
            } else {
                remaining
            },
        )
    }

    suspend fun headSizeBytes(url: String): Long {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "HEAD"
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.connect()
            val code = connection.responseCode
            val length = connection.getHeaderFieldLong("Content-Length", -1L)
            return if (code in 200..299 && length > 0) length else 0L
        } catch (_: Exception) {
            return 0L
        } finally {
            connection.disconnect()
        }
    }
}

class AndroidAudioDownloader : AudioDownloader {
    override suspend fun remoteSizeBytes(url: String): Long =
        withContext(Dispatchers.IO) {
            UrlRangeOpener.headSizeBytes(url)
        }

    override suspend fun downloadAyah(
        key: String,
        item: AyahDownload,
        control: DownloadControl,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): Long = withContext(Dispatchers.IO) {
        val dest = File(item.destPath)
        val part = File(item.destPath + ".part")
        // Parent dirs must exist before RandomAccessFile creates the .part
        // file, otherwise the first download to a new reciter/surah dir
        // throws FileNotFoundException.
        try {
            part.parentFile?.mkdirs()
            dest.parentFile?.mkdirs()
        } catch (_: Exception) {
        }
        var downloaded = if (item.resumeFromBytes > 0 && part.exists()) {
            part.length()
        } else {
            if (part.exists()) part.delete()
            0L
        }
        try {
            val out = RandomAccessFile(part, "rw")
            try {
                out.seek(downloaded)
                downloaded = runResumableDownload(
                    key = key,
                    url = item.url,
                    alreadyHave = downloaded,
                    control = control,
                    opener = UrlRangeOpener,
                    onRestart = {
                        out.setLength(0)
                        out.seek(0)
                    },
                    writeChunk = { bytes, len -> out.write(bytes, 0, len) },
                    onProgress = onProgress,
                )
            } finally {
                out.close()
            }
            dest.parentFile?.mkdirs()
            if (dest.exists()) dest.delete()
            if (!part.renameTo(dest)) {
                throw IOException("rename failed for ${item.destPath}")
            }
            downloaded
        } catch (e: DownloadCancelledException) {
            part.delete()
            throw e
        } catch (e: Exception) {
            // Partial file stays for resume; surface the failure.
            throw e
        }
    }
}
