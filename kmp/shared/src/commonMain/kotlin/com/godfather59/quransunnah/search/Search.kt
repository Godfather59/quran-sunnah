package com.godfather59.quransunnah.search

import com.godfather59.quransunnah.text.parseQuranRef
import com.godfather59.quransunnah.hadith.Hadith
import com.godfather59.quransunnah.quran.Ayah
import com.godfather59.quransunnah.quran.TafsirEntry
import com.godfather59.quransunnah.quran.surahMetadata
import com.godfather59.quransunnah.text.normalizeArabic
import com.godfather59.quransunnah.util.CommonLock

// Global search across verified bundled datasets. Ported from
// lib/data/services/search_service.dart (+ FTS5 semantics from
// search_index_service.dart).
//
// The Flutter app indexes SQLite FTS5 over a normalized copy; display
// strings always come from exact source text. This engine keeps the same
// contract in memory: matching happens on normalized tokens, hits carry
// exact source text. A persisted FTS5 index (SQLDelight) can replace the
// storage later without changing query semantics or these tests.

enum class SearchKind { QURAN, HADITH, TAFSIR, SURAH }

data class SearchHit(
    val kind: SearchKind,
    val title: String,
    val subtitle: String,
    val snippet: String,
    val refKey: String,
    val surah: Int? = null,
    val ayah: Int? = null,
    val hadith: Hadith? = null,
    /** Normalized query tokens that matched (for UI highlight). */
    val matchedTerms: List<String> = emptyList(),
)

data class SearchResults(
    val quran: List<SearchHit> = emptyList(),
    val hadith: List<SearchHit> = emptyList(),
    val tafsir: List<SearchHit> = emptyList(),
    val surahs: List<SearchHit> = emptyList(),
    val truncated: Boolean = false,
) {
    val total: Int get() = quran.size + hadith.size + tafsir.size + surahs.size
}

data class SearchOptions(
    val limitPerCategory: Int = 50,
    val surahScope: Int? = null,
    val collectionIds: Set<String>? = null,
    val book: String? = null,
    val number: String? = null,
    val tafsirId: String? = null,
    val editionId: String? = null,
)

/** Index-only normalization (display text is never mutated). */
fun normalizeForIndex(value: String): String =
    normalizeArabic(value).lowercase()

private fun isIndexChar(c: Char): Boolean {
    val code = c.code
    return c in 'a'..'z' || c in '0'..'9' ||
        code in 0x0600..0x06FF || code in 0x0750..0x077F || code in 0x08A0..0x08FF
}

/**
 * Tokenizes like SearchIndexService.ftsQuery: non-index chars become
 * spaces; empty tokens dropped. Empty result = no FTS matching
 * (direct verse + surah search still apply).
 */
fun tokenizeQuery(raw: String): List<String> {
    val normalized = normalizeForIndex(raw)
    val spaced = normalized.map { if (isIndexChar(it)) it else ' ' }
        .joinToString("")
    return spaced.split(' ', '\t', '\n', '\r').filter { it.isNotEmpty() }
}

private fun wordsOf(normalized: String): List<String> =
    normalized.map { if (isIndexChar(it)) it else ' ' }.joinToString("")
        .split(' ', '\t', '\n', '\r').filter { it.isNotEmpty() }

private class Doc(
    val kind: SearchKind,
    val refKey: String,
    val title: String,
    val subtitle: String,
    val body: String,
    val bodyWords: List<String>,
    val titleWords: List<String>,
    val surah: Int? = null,
    val ayah: Int? = null,
    val collectionId: String? = null,
    val hadithNumber: String? = null,
    val book: String? = null,
    val editionId: String? = null,
    val tafsirId: String? = null,
    val hadith: Hadith? = null,
)

class SearchEngine {
    private val docs = mutableListOf<Doc>()
    // Index built on IO, queried on Main/Default: guard against
    // ConcurrentModificationException (Flutter was single-threaded).
    private val lock = CommonLock()

    fun clearQuran() {
        lock.withLock { docs.removeAll { it.kind == SearchKind.QURAN } }
    }

    fun clearHadith() {
        lock.withLock { docs.removeAll { it.kind == SearchKind.HADITH } }
    }

    fun clearTafsir() {
        lock.withLock { docs.removeAll { it.kind == SearchKind.TAFSIR } }
    }

    fun clearAll() {
        lock.withLock { docs.clear() }
    }

    fun documentCount(): Int = lock.withLock { docs.size }

    fun indexQuran(ayahs: List<Ayah>) {
        // Build outside the lock (normalization is CPU-heavy), swap atomically.
        val fresh = mutableListOf<Doc>()
        for (a in ayahs) {
            if (a.isPlaceholder || a.text.isEmpty()) continue
            val norm = normalizeForIndex(a.text)
            val title = "Surah ${a.surah} · Ayah ${a.displayAyahNumber}"
            fresh.add(
                Doc(
                    kind = SearchKind.QURAN,
                    refKey = a.canonicalVerseId,
                    title = title,
                    subtitle = "Quran · ${a.editionId}",
                    body = a.text,
                    bodyWords = wordsOf(norm),
                    titleWords = wordsOf(normalizeForIndex(title)),
                    surah = a.canonicalSurahNumber,
                    ayah = a.canonicalAyahNumber,
                    editionId = a.editionId,
                ),
            )
        }
        lock.withLock {
            docs.removeAll { it.kind == SearchKind.QURAN }
            docs.addAll(fresh)
        }
    }

    fun indexHadith(hadiths: List<Hadith>, collectionNames: Map<String, String>) {
        val fresh = mutableListOf<Doc>()
        for (h in hadiths) {
            if (h.isPlaceholder || h.matnAr.isEmpty()) continue
            val collectionName = collectionNames[h.collectionId] ?: h.collectionId
            val title = "$collectionName · Hadith ${h.hadithNumber}"
            fresh.add(
                Doc(
                    kind = SearchKind.HADITH,
                    refKey = h.id,
                    title = title,
                    subtitle = h.book,
                    body = h.matnAr,
                    bodyWords = wordsOf(normalizeForIndex(h.matnAr)),
                    titleWords = wordsOf(
                        normalizeForIndex("$collectionName ${h.book} ${h.hadithNumber}"),
                    ),
                    collectionId = h.collectionId,
                    hadithNumber = h.hadithNumber,
                    book = h.book,
                    hadith = h,
                ),
            )
        }
        lock.withLock {
            docs.removeAll { it.kind == SearchKind.HADITH }
            docs.addAll(fresh)
        }
    }

    fun indexTafsir(entries: List<TafsirEntry>, titleEn: String, source: String) {
        val fresh = mutableListOf<Doc>()
        for (e in entries) {
            if (e.text.isEmpty()) continue
            val title = "$titleEn · ${e.surah}:${e.ayah}"
            fresh.add(
                Doc(
                    kind = SearchKind.TAFSIR,
                    refKey = "${e.tafsirId}:${e.surah}:${e.ayah}",
                    title = title,
                    subtitle = source,
                    body = e.text,
                    bodyWords = wordsOf(normalizeForIndex(e.text)),
                    titleWords = wordsOf(normalizeForIndex(title)),
                    surah = e.surah,
                    ayah = e.ayah,
                    tafsirId = e.tafsirId,
                ),
            )
        }
        lock.withLock {
            docs.removeAll { it.kind == SearchKind.TAFSIR }
            docs.addAll(fresh)
        }
    }

    fun searchSurahs(query: String): List<SearchHit> {
        val raw = query.trim()
        if (raw.isEmpty()) return emptyList()
        val q = raw.lowercase()
        return surahMetadata
            .filter { m ->
                m.nameAr.contains(raw) ||
                    m.nameEn.lowercase().contains(q) ||
                    m.nameFr.lowercase().contains(q) ||
                    m.number.toString() == q
            }
            .map { m ->
                SearchHit(
                    kind = SearchKind.SURAH,
                    title = "${m.number}. ${m.nameAr} — ${m.nameEn}",
                    subtitle = "${if (m.makki) "Makki" else "Madani"} · ${m.ayahCount} ayat",
                    snippet = m.nameFr,
                    refKey = "surah:${m.number}",
                    surah = m.number,
                )
            }
    }

    fun search(query: String, options: SearchOptions = SearchOptions()): SearchResults {
        val raw = query.trim()
        if (raw.isEmpty()) return SearchResults()
        // Clamp limits: negative take() throws, Int.MAX_VALUE + 1 overflows,
        // and unbounded limits let single-char prefix queries OOM the UI.
        val limit = options.limitPerCategory.coerceIn(1, 200)
        val tokens = tokenizeQuery(raw)
        // Cap tokens so a pasted paragraph can't DoS the O(docs*tokens) loop.
        val cappedTokens = tokens.take(10)

        // Snapshot under lock: indexers may replace docs mid-search.
        val snapshot: List<Doc> = lock.withLock { docs.toList() }
        // Direct verse shortcut (bare "2:255" only — display number, like
        // Dart; quran:// URIs parse but are NOT shortcuts).
        var directVerse: SearchHit? = null
        parseQuranRef(raw)?.let { (surah, displayAyah) ->
            if (surah in 1..114 && VERSE_ONLY.matches(raw)) {
                directVerse = snapshot.firstOrNull { d ->
                    d.kind == SearchKind.QURAN &&
                        d.surah == surah &&
                        d.ayah == displayAyah &&
                        (options.editionId == null || d.editionId == options.editionId)
                }?.let { d ->
                    SearchHit(
                        kind = SearchKind.QURAN,
                        title = "Surah $surah · Ayah $displayAyah",
                        subtitle = "Direct verse reference",
                        snippet = d.body,
                        refKey = d.refKey,
                        surah = d.surah,
                        ayah = d.ayah,
                    )
                }
            }
        }

        val quranHits = mutableListOf<SearchHit>()
        if (directVerse != null) quranHits.add(directVerse!!)
        var truncated = false

        if (cappedTokens.isNotEmpty()) {
            val quranRows = matchDocs(
                SearchKind.QURAN, cappedTokens, limit + 1,
                snapshot = snapshot,
            ) { d ->
                (options.surahScope == null || d.surah == options.surahScope) &&
                    (options.editionId == null || d.editionId == options.editionId) &&
                    (directVerse == null || d.refKey != directVerse!!.refKey)
            }
            if (quranRows.size > limit) truncated = true
            for ((doc, terms) in quranRows.take(limit)) {
                quranHits.add(
                    SearchHit(
                        kind = SearchKind.QURAN,
                        title = doc.title,
                        subtitle = doc.subtitle,
                        snippet = doc.body,
                        refKey = doc.refKey,
                        surah = doc.surah,
                        ayah = doc.ayah,
                        matchedTerms = terms,
                    ),
                )
            }
        }

        val hadithHits = mutableListOf<SearchHit>()
        if (cappedTokens.isNotEmpty()) {
            val rows = matchDocs(
                SearchKind.HADITH, cappedTokens, limit + 1,
                snapshot = snapshot,
            ) { d ->
                (options.collectionIds == null || d.collectionId in options.collectionIds!!) &&
                    (options.book.isNullOrEmpty() || d.book == options.book) &&
                    (options.number.isNullOrEmpty() || d.hadithNumber == options.number)
            }
            if (rows.size > limit) truncated = true
            for ((doc, terms) in rows.take(limit)) {
                val h = doc.hadith!!
                hadithHits.add(
                    SearchHit(
                        kind = SearchKind.HADITH,
                        title = doc.title,
                        subtitle = doc.subtitle,
                        snippet = snippet(doc.body),
                        refKey = doc.refKey,
                        hadith = h.copy(
                            book = doc.book ?: h.book,
                            chapter = doc.book ?: h.chapter,
                        ),
                        matchedTerms = terms,
                    ),
                )
            }
        }

        val tafsirHits = mutableListOf<SearchHit>()
        if (cappedTokens.isNotEmpty()) {
            val rows = matchDocs(
                SearchKind.TAFSIR, cappedTokens, limit + 1,
                snapshot = snapshot,
            ) { d ->
                (options.surahScope == null || d.surah == options.surahScope) &&
                    (options.tafsirId == null || d.tafsirId == options.tafsirId)
            }
            if (rows.size > limit) truncated = true
            for ((doc, terms) in rows.take(limit)) {
                tafsirHits.add(
                    SearchHit(
                        kind = SearchKind.TAFSIR,
                        title = doc.title,
                        subtitle = doc.subtitle,
                        snippet = snippet(doc.body),
                        refKey = doc.refKey,
                        surah = doc.surah,
                        ayah = doc.ayah,
                        matchedTerms = terms,
                    ),
                )
            }
        }

        return SearchResults(
            quran = quranHits.take(limit),
            hadith = hadithHits,
            tafsir = tafsirHits,
            surahs = searchSurahs(raw),
            truncated = truncated,
        )
    }

    /**
     * AND-prefix matching (mirrors `token* AND ...` FTS): every token must
     * prefix-match a body/title word. Score: exact word = 3, prefix = 1,
     * title exact = +2. Deterministic order: score desc, refKey asc.
     */
    private fun matchDocs(
        kind: SearchKind,
        tokens: List<String>,
        limit: Int,
        snapshot: List<Doc>? = null,
        filter: (Doc) -> Boolean,
    ): List<Pair<Doc, List<String>>> {
        // Default snapshot keeps old call sites safe; search() passes its copy.
        val rows = snapshot ?: lock.withLock { docs.toList() }
        val scored = mutableListOf<Triple<Doc, Int, List<String>>>()
        for (d in rows) {
            if (d.kind != kind || !filter(d)) continue
            var score = 0
            val matched = mutableListOf<String>()
            var ok = true
            for (t in tokens) {
                val bodyExact = d.bodyWords.any { it == t }
                val bodyPrefix = !bodyExact && d.bodyWords.any { it.startsWith(t) }
                val titleExact = d.titleWords.any { it == t }
                if (!bodyExact && !bodyPrefix && !titleExact) {
                    ok = false
                    break
                }
                score += (if (bodyExact) 3 else 1) + (if (titleExact) 2 else 0)
                matched.add(t)
            }
            if (ok) scored.add(Triple(d, score, matched))
        }
        return scored
            .sortedWith(compareByDescending<Triple<Doc, Int, List<String>>> { it.second }.thenBy { it.first.refKey })
            .take(limit)
            .map { Pair(it.first, it.third) }
    }

    companion object {
        fun snippet(value: String): String =
            if (value.length > 220) value.substring(0, 220) + "…" else value

        // Dart shortcut gate: bare "surah:ayah" only (not quran:// URIs).
        private val VERSE_ONLY = Regex("^\\d{1,3}\\s*:\\s*\\d{1,3}$")
    }
}
