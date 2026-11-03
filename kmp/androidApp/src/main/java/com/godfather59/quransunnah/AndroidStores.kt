package com.godfather59.quransunnah

import android.content.Context
import com.godfather59.quransunnah.audio.AndroidAudioDownloader
import com.godfather59.quransunnah.audio.AndroidAudioPlayer
import com.godfather59.quransunnah.audio.AudioDownloader
import com.godfather59.quransunnah.audio.AudioPlayer
import com.godfather59.quransunnah.db.SearchIndexStore
import com.godfather59.quransunnah.db.createAndroidDatabase
import com.godfather59.quransunnah.quran.AndroidMushafMetadataReader
import com.godfather59.quransunnah.quran.MushafMetadataReader
import com.godfather59.quransunnah.search.SearchEngine

object AndroidStores {
    @Volatile
    private var searchEngine: SearchEngine? = null
    @Volatile
    private var searchIndexStore: SearchIndexStore? = null
    @Volatile
    private var audioPlayer: AudioPlayer? = null
    @Volatile
    private var audioDownloader: AudioDownloader? = null
    @Volatile
    private var mushafMetadataReader: MushafMetadataReader? = null
    @Volatile
    private var downloadsManager: DownloadsManager? = null

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
     * App-scoped download manager. Survives rotation/tab-leave, unlike the
     * previous composition-local queue.
     */
    fun downloadsManager(context: Context): DownloadsManager =
        downloadsManager ?: synchronized(this) {
            downloadsManager ?: DownloadsManager.get(context, audioDownloader(context))
                .also { downloadsManager = it }
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

    /** Persistent search document cache; create/use it from Dispatchers.IO. */
    fun searchIndexStore(context: Context): SearchIndexStore =
        searchIndexStore ?: synchronized(this) {
            searchIndexStore ?: SearchIndexStore(
                createAndroidDatabase(
                    context.applicationContext,
                    name = "quran_search_index.db",
                ),
            ).also { searchIndexStore = it }
        }
}
