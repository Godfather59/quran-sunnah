package com.godfather59.quransunnah

import android.content.Context
import com.godfather59.quransunnah.audio.AndroidAudioDownloader
import com.godfather59.quransunnah.audio.AndroidAudioPlayer
import com.godfather59.quransunnah.audio.AudioDownloader
import com.godfather59.quransunnah.audio.AudioPlayer
import com.godfather59.quransunnah.db.LibraryStore
import com.godfather59.quransunnah.db.createAndroidDatabase
import com.godfather59.quransunnah.quran.AndroidMushafMetadataReader
import com.godfather59.quransunnah.quran.MushafMetadataReader
import com.godfather59.quransunnah.search.SearchEngine

object AndroidStores {
    @Volatile
    private var libraryStore: LibraryStore? = null
    @Volatile
    private var searchEngine: SearchEngine? = null
    @Volatile
    private var audioPlayer: AudioPlayer? = null
    @Volatile
    private var audioDownloader: AudioDownloader? = null
    @Volatile
    private var mushafMetadataReader: MushafMetadataReader? = null

    fun library(context: Context): LibraryStore =
        libraryStore ?: synchronized(this) {
            libraryStore ?: LibraryStore(
                createAndroidDatabase(context.applicationContext),
            ).also { libraryStore = it }
        }

    fun audio(context: Context): AudioPlayer =
        audioPlayer ?: synchronized(this) {
            audioPlayer ?: AndroidAudioPlayer(context.applicationContext)
                .also { audioPlayer = it }
        }

    fun audioDownloader(context: Context): AudioDownloader =
        audioDownloader ?: synchronized(this) {
            audioDownloader ?: AndroidAudioDownloader()
                .also { audioDownloader = it }
        }

    fun mushafMetadataReader(context: Context): MushafMetadataReader =
        mushafMetadataReader ?: synchronized(this) {
            mushafMetadataReader ?: AndroidMushafMetadataReader(context.applicationContext)
                .also { mushafMetadataReader = it }
        }

    /**
     * Process-wide search index (Quran + Hadith + Tafsir). Built once on
     * first search open; FTS5 persistence can replace this without changing
     * query semantics.
     */
    fun searchEngine(): SearchEngine =
        searchEngine ?: synchronized(this) {
            searchEngine ?: SearchEngine().also { searchEngine = it }
        }
}
