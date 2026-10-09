package com.godfather59.quransunnah

import android.content.Context
import android.net.Uri
import com.godfather59.quransunnah.audio.audioDirFor
import com.godfather59.quransunnah.audio.audioFileName
import com.godfather59.quransunnah.audio.isSurahDownloaded
import java.io.File

// Local-first ayah audio files under the app file space:
// `<files>/audio/<reciter>/<surah>/<ayah>.mp3` (matches shared AudioMath).
object AudioFiles {
    fun ayahFile(
        context: Context,
        reciterId: String,
        surah: Int,
        ayah: Int,
    ): File = File(
        context.filesDir,
        "${audioDirFor(reciterId, surah)}/${audioFileName(ayah)}",
    )

    fun surahDir(
        context: Context,
        reciterId: String,
        surah: Int,
    ): File = File(context.filesDir, audioDirFor(reciterId, surah))

    fun isSurahDownloaded(
        context: Context,
        reciterId: String,
        surah: Int,
    ): Boolean = isSurahDownloaded(reciterId, surah) { path ->
        File(context.filesDir, path).exists()
    }

    fun surahBytes(
        context: Context,
        reciterId: String,
        surah: Int,
    ): Long = surahDir(context, reciterId, surah)
        .listFiles()
        ?.filter { it.isFile && it.extension == "mp3" }
        ?.sumOf { it.length() }
        ?: 0L

    fun deleteSurah(
        context: Context,
        reciterId: String,
        surah: Int,
    ) {
        surahDir(context, reciterId, surah).deleteRecursively()
    }

    /** `file://` URL when the ayah is on disk, else null (caller streams). */
    fun localUrlOrNull(
        context: Context,
        reciterId: String,
        surah: Int,
        ayah: Int,
    ): String? {
        val file = ayahFile(context, reciterId, surah, ayah)
        return if (file.exists()) Uri.fromFile(file).toString() else null
    }

    fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return "%.0f KB".format(kb)
        return "%.1f MB".format(kb / 1024.0)
    }
}
