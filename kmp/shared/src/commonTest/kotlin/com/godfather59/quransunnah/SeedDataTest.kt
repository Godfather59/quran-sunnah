package com.godfather59.quransunnah

import com.godfather59.quransunnah.audio.reciterById
import com.godfather59.quransunnah.audio.reciters
import com.godfather59.quransunnah.audio.recitersForRiwaya
import com.godfather59.quransunnah.dhikr.kAdhkar
import com.godfather59.quransunnah.quran.dailyAyahRef
import com.godfather59.quransunnah.hadith.KUTUB_SITTAH
import com.godfather59.quransunnah.hadith.SAHIHAYN
import com.godfather59.quransunnah.hadith.hadithCollections
import com.godfather59.quransunnah.library.BookmarkKind
import com.godfather59.quransunnah.library.RecentItem
import com.godfather59.quransunnah.library.defaultCollections
import com.godfather59.quransunnah.prayer.isMoroccanCity
import com.godfather59.quransunnah.prayer.prayerCityPresets
import com.godfather59.quransunnah.quran.RiwayaId
import com.godfather59.quransunnah.quran.riwayatCatalog
import com.godfather59.quransunnah.quran.storageKey
import com.godfather59.quransunnah.quran.surahMetadata
import com.godfather59.quransunnah.quran.tafsirCatalog
import com.godfather59.quransunnah.quran.translationCatalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SeedDataTest {
    @Test
    fun surahMetadataShape() {
        assertEquals(114, surahMetadata.size)
        assertEquals((1..114).toList(), surahMetadata.map { it.number })
        // 6236 verses total (canonical coordinate system).
        assertEquals(6236, surahMetadata.sumOf { it.ayahCount })
        val baqarah = surahMetadata.first { it.number == 2 }
        assertEquals(286, baqarah.ayahCount)
        assertEquals("البقرة", baqarah.nameAr)
        assertFalse(baqarah.makki)
        assertTrue(surahMetadata.first { it.number == 1 }.makki)
    }

    @Test
    fun riwayatCatalogShape() {
        assertEquals(14, riwayatCatalog.size)
        assertEquals(
            RiwayaId.entries.toList(),
            riwayatCatalog.map { it.id },
        )
        assertEquals("hafs-an-asim", RiwayaId.HAFS_ASIM.storageKey)
        assertEquals("warsh-an-nafi", RiwayaId.WARSH_NAFI.storageKey)
        // Odd-but-canonical key, preserved from Dart.
        assertEquals("albazzi-an-ibn-kathir", RiwayaId.BAZZI_IBN_KATHIR.storageKey)
        assertEquals(3, riwayatCatalog.count { it.isAvailableOfflineSeed })
    }

    @Test
    fun hadithCollectionsShape() {
        assertEquals(14, hadithCollections.size)
        val bukhari = hadithCollections.first { it.id == "bukhari" }
        assertEquals(7589, bukhari.totalHadith)
        assertEquals("صحيح البخاري", bukhari.nameAr)
        assertEquals(SAHIHAYN, setOf("bukhari", "muslim"))
        assertEquals(
            setOf("bukhari", "muslim", "abudawud", "tirmidhi", "nasai", "ibnmajah"),
            KUTUB_SITTAH,
        )
    }

    @Test
    fun libraryModels() {
        assertEquals(3, BookmarkKind.entries.size)
        assertEquals(3, defaultCollections.size)
        val recent = RecentItem(
            refKey = "2:255",
            title = "t",
            subtitle = "s",
            kind = BookmarkKind.AYAH,
        )
        assertEquals("2:255", recent.refKey)
    }

    @Test
    fun translationAndTafsirCatalogs() {
        assertEquals(4, translationCatalog.size)
        assertEquals(2, translationCatalog.count { it.bundled })
        assertTrue(translationCatalog.any {
            it.id == "en-sahih" && it.translator == "Saheeh International"
        })
        assertEquals(6, tafsirCatalog.size)
        assertEquals(2, tafsirCatalog.count { it.bundled })
        assertTrue(tafsirCatalog.any { it.id == "jalalayn" && it.bundled })
    }

    @Test
    fun recitersVerifiedHafsOnly() {
        assertEquals(8, reciters.size)
        assertTrue(reciters.all { it.riwayaKey == "hafs-an-asim" })
        assertEquals("Mahmoud Al-Husary", reciterById("ar.husary")?.nameEn)
        assertEquals(
            "https://cdn.islamic.network/quran/audio/128/ar.alafasy/1.mp3",
            reciters.first { it.identifier == "ar.alafasy" }.fileUrl(1),
        )
        assertTrue(recitersForRiwaya("warsh-an-nafi").isEmpty())
    }

    @Test
    fun adhkarSeed() {
        assertEquals(8, kAdhkar.size)
        assertEquals(kAdhkar.map { it.id }.toSet().size, kAdhkar.size)
        assertTrue(kAdhkar.all { it.target > 0 && it.textAr.isNotBlank() })
        assertEquals(33, kAdhkar.first { it.id == "tasbih" }.target)
        assertEquals(100, kAdhkar.first { it.id == "tahlil" }.target)
    }

    @Test
    fun dailyAyahRefDeterministic() {
        assertEquals(Pair(1, 1), dailyAyahRef(0))
        assertEquals(Pair(1, 7), dailyAyahRef(6))
        assertEquals(Pair(2, 1), dailyAyahRef(7))
        assertEquals(Pair(114, 6), dailyAyahRef(6235))
        assertEquals(Pair(1, 1), dailyAyahRef(6236))
        assertEquals(dailyAyahRef(10), dailyAyahRef(10 + 6236))
    }

    @Test
    fun prayerCityPresets() {
        assertEquals(13, prayerCityPresets.size)
        val casa = prayerCityPresets.first { it.name == "Casablanca" }
        assertEquals(33.5731, casa.latitude, 0.0001)
        assertEquals(1.0, casa.tzOffsetHours)
        assertTrue(isMoroccanCity("Rabat"))
        assertTrue(isMoroccanCity("Marrakech"))
        assertFalse(isMoroccanCity("Cairo"))
    }
}
