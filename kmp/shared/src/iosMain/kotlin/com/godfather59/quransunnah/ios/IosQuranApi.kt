package com.godfather59.quransunnah.ios

import com.godfather59.quransunnah.data.IosBundleAssetReader
import com.godfather59.quransunnah.data.loadQuranEdition
import com.godfather59.quransunnah.data.loadTafsirSurah
import com.godfather59.quransunnah.data.parseHadithIndex
import com.godfather59.quransunnah.data.parseHadithSection
import com.godfather59.quransunnah.data.parsePipeMap
import com.godfather59.quransunnah.data.verifiedQuranAssets
import com.godfather59.quransunnah.hadith.hadithCollections
import com.godfather59.quransunnah.prayer.calculatePrayerTimes
import com.godfather59.quransunnah.prayer.nextPrayer
import com.godfather59.quransunnah.prayer.prayerMethodFromFlutterName
import com.godfather59.quransunnah.prayer.qiblaBearing
import com.godfather59.quransunnah.quran.surahMetadata
import com.godfather59.quransunnah.quran.translationAssets
import com.godfather59.quransunnah.search.SearchEngine
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put

/** Stable JSON boundary for the native iOS app; all Quran text and search use shared Kotlin code. */
object IosQuranApi {
    private val reader = IosBundleAssetReader()
    private val editionId = "hafs-an-asim__uthmani"
    private val ayahs by lazy { loadQuranEdition(reader, editionId).orEmpty() }
    private val searchEngine by lazy { SearchEngine().apply { indexQuran(ayahs) } }

    fun catalogJson(): String = buildJsonObject {
        put("editionId", editionId)
        put("surahs", buildJsonArray {
            surahMetadata.forEach { surah ->
                add(buildJsonObject {
                    put("number", surah.number)
                    put("arabicName", surah.nameAr)
                    put("englishName", surah.nameEn)
                    put("transliteration", surah.nameEn)
                    put("ayahCount", surah.ayahCount)
                    put("makki", surah.makki)
                })
            }
        })
        put("ayahs", buildJsonArray {
            ayahs.forEach { ayah ->
                add(buildJsonObject {
                    put("surah", ayah.surah)
                    put("number", ayah.ayah)
                    put("text", ayah.text)
                })
            }
        })
    }.toString()

    fun searchQuranJson(query: String): String {
        if (query.isBlank() || ayahs.isEmpty()) return "[]"
        val hits = searchEngine.search(query).quran
        return buildJsonArray {
            hits.forEach { hit ->
                add(buildJsonObject {
                    put("title", hit.title)
                    put("subtitle", hit.subtitle)
                    put("snippet", hit.snippet)
                    put("surah", hit.surah ?: 0)
                    put("ayah", hit.ayah ?: 0)
                })
            }
        }.toString()
    }

    /**
     * Verified verses for any bundled edition. Unknown/absent edition returns
     * `[]` — never synthesized text (fail-closed, same rule as Android).
     */
    fun ayahsForEditionJson(requestedEditionId: String): String {
        if (requestedEditionId !in verifiedQuranAssets) return "[]"
        val list = try {
            loadQuranEdition(reader, requestedEditionId)
        } catch (_: Exception) {
            null
        }.orEmpty()
        if (list.isEmpty()) return "[]"
        return buildJsonArray {
            list.forEach { ayah ->
                add(buildJsonObject {
                    put("surah", ayah.surah)
                    put("number", ayah.ayah)
                    put("text", ayah.text)
                    put("editionId", requestedEditionId)
                })
            }
        }.toString()
    }

    /** All 14 collection metadata rows (counts/names only, no matn). */
    fun hadithCollectionsJson(): String = buildJsonArray {
        hadithCollections.forEach { c ->
            add(buildJsonObject {
                put("id", c.id)
                put("nameAr", c.nameAr)
                put("nameEn", c.nameEn)
                put("nameFr", c.nameFr)
                put("compiler", c.compiler)
                if (c.totalHadith != null) put("totalHadith", c.totalHadith)
            })
        }
    }.toString()

    /** Sections for one collection; absent/unparseable returns `[]`. */
    fun hadithIndexJson(collectionId: String): String {
        val raw = try {
            reader.readText("assets/hadith/$collectionId/index.json")
        } catch (_: Exception) {
            null
        } ?: return "[]"
        val index = try {
            parseHadithIndex(raw)
        } catch (_: Exception) {
            return "[]"
        }
        return buildJsonArray {
            index.sections.forEach { s ->
                add(buildJsonObject {
                    put("section", s.section)
                    put("title", s.title)
                    put("first", s.first)
                    put("last", s.last)
                    put("count", s.count)
                })
            }
        }.toString()
    }

    /** Verified hadiths for one section; placeholders keep empty matn honestly. */
    fun hadithSectionJson(collectionId: String, section: Int): String {
        val indexRaw = try {
            reader.readText("assets/hadith/$collectionId/index.json")
        } catch (_: Exception) {
            null
        } ?: return "[]"
        val title = try {
            parseHadithIndex(indexRaw).sections.firstOrNull { it.section == section }?.title.orEmpty()
        } catch (_: Exception) {
            return "[]"
        }
        val raw = try {
            reader.readText("assets/hadith/$collectionId/sections/$section.json")
        } catch (_: Exception) {
            null
        } ?: return "[]"
        val list = try {
            parseHadithSection(collectionId, title, raw)
        } catch (_: Exception) {
            return "[]"
        }
        return buildJsonArray {
            list.forEach { h ->
                add(buildJsonObject {
                    put("id", h.id)
                    put("hadithNumber", h.hadithNumber)
                    put("book", h.book)
                    put("matnAr", h.matnAr)
                    if (h.grade != null) put("grade", h.grade)
                    if (h.gradingAuthority != null) put("gradingAuthority", h.gradingAuthority)
                    put("isPlaceholder", h.isPlaceholder)
                })
            }
        }.toString()
    }

    /** Verified translation rows for one surah; unknown id returns `[]`. */
    fun translationSurahJson(translationId: String, surah: Int): String {
        val path = translationAssets[translationId] ?: return "[]"
        val raw = try {
            reader.readText(path)
        } catch (_: Exception) {
            null
        } ?: return "[]"
        val map = try {
            parsePipeMap(raw)
        } catch (_: Exception) {
            return "[]"
        }
        val prefix = "$surah:"
        return buildJsonArray {
            map.entries
                .filter { (k, v) -> k.startsWith(prefix) && v.isNotEmpty() }
                .forEach { (k, v) ->
                    val ayah = k.substringAfter(":").toIntOrNull() ?: return@forEach
                    add(buildJsonObject {
                        put("surah", surah)
                        put("ayah", ayah)
                        put("text", v)
                    })
                }
        }.toString()
    }

    /** Verified tafsir entries for one surah; unknown id returns `[]`. */
    fun tafsirSurahJson(tafsirId: String, surah: Int): String {
        val map = try {
            loadTafsirSurah(reader, tafsirId, surah)
        } catch (_: Exception) {
            null
        }.orEmpty()
        if (map.isEmpty()) return "[]"
        return buildJsonArray {
            map.entries.sortedBy { it.key }.forEach { (ayah, text) ->
                add(buildJsonObject {
                    put("surah", surah)
                    put("ayah", ayah)
                    put("text", text)
                })
            }
        }.toString()
    }

    /** Offline prayer times (minutes-from-midnight + HH:MM), datetime-free core. */
    fun prayerTimesJson(
        year: Int,
        month: Int,
        day: Int,
        latitude: Double,
        longitude: Double,
        tzOffsetHours: Double,
        methodName: String,
        nowMinutes: Int,
    ): String {
        val times = try {
            calculatePrayerTimes(
                year, month, day, latitude, longitude, tzOffsetHours,
                prayerMethodFromFlutterName(methodName),
            )
        } catch (_: Exception) {
            return "{}"
        }
        val next = try {
            nextPrayer(times, nowMinutes)
        } catch (_: Exception) {
            null
        }
        return buildJsonObject {
            put("fajr", times.fajr)
            put("sunrise", times.sunrise)
            put("dhuhr", times.dhuhr)
            put("asr", times.asr)
            put("maghrib", times.maghrib)
            put("isha", times.isha)
            put("fajrText", times.format(times.fajr))
            put("sunriseText", times.format(times.sunrise))
            put("dhuhrText", times.format(times.dhuhr))
            put("asrText", times.format(times.asr))
            put("maghribText", times.format(times.maghrib))
            put("ishaText", times.format(times.isha))
            if (next != null) {
                put("nextKey", next.key)
                put("nextMinutes", next.minutes)
                put("nextIsTomorrow", next.isTomorrow)
            }
        }.toString()
    }

    fun qiblaJson(latitude: Double, longitude: Double): String {
        val bearing = try {
            qiblaBearing(latitude, longitude)
        } catch (_: Exception) {
            return "{}"
        }
        return buildJsonObject {
            put("bearing", bearing)
        }.toString()
    }
}
