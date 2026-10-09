package com.godfather59.quransunnah.quran

// Quran domain models. Ported from lib/data/models/quran.dart.
//
// DATA INTEGRITY: Ayah.text must ONLY be populated from a verified
// Quran dataset (per-edition files). Never synthesize verses.

/** Canonical Riwaya identifier. Each entry MUST map to its own
 * verified dataset file — never derive one riwaya from another. */
enum class RiwayaId {
    HAFS_ASIM,
    SHUBAH_ASIM,
    WARSH_NAFI,
    QALUN_NAFI,
    BAZZI_IBN_KATHIR,
    QUNBUL_IBN_KATHIR,
    DURI_ABI_AMR,
    SUSI_ABI_AMR,
    HISHAM_IBN_AMIR,
    IBN_DHAKWAN_IBN_AMIR,
    KHALAF_HAMZAH,
    KHALLAD_HAMZAH,
    ABUL_HARITH_KISAI,
    DURI_KISAI,
}

val RiwayaId.storageKey: String
    get() = when (this) {
        RiwayaId.HAFS_ASIM -> "hafs-an-asim"
        RiwayaId.SHUBAH_ASIM -> "shubah-an-asim"
        RiwayaId.WARSH_NAFI -> "warsh-an-nafi"
        RiwayaId.QALUN_NAFI -> "qalun-an-nafi"
        RiwayaId.BAZZI_IBN_KATHIR -> "albazzi-an-ibn-kathir"
        RiwayaId.QUNBUL_IBN_KATHIR -> "qunbul-an-ibn-kathir"
        RiwayaId.DURI_ABI_AMR -> "alduri-an-abi-amr"
        RiwayaId.SUSI_ABI_AMR -> "alsusi-an-abi-amr"
        RiwayaId.HISHAM_IBN_AMIR -> "hisham-an-ibn-amir"
        RiwayaId.IBN_DHAKWAN_IBN_AMIR -> "ibn-dhakwan-an-ibn-amir"
        RiwayaId.KHALAF_HAMZAH -> "khalaf-an-hamzah"
        RiwayaId.KHALLAD_HAMZAH -> "khallad-an-hamzah"
        RiwayaId.ABUL_HARITH_KISAI -> "abul-harith-an-kisai"
        RiwayaId.DURI_KISAI -> "alduri-an-kisai"
    }

data class RiwayaInfo(
    val id: RiwayaId,
    val qiraaAr: String,
    val qiraaEn: String,
    val qiraaFr: String,
    val riwayaAr: String,
    val riwayaEn: String,
    val riwayaFr: String,
    val datasetVersion: String,
    val source: String,
    val isAvailableOfflineSeed: Boolean = false,
)

/** Writing style (rasm) — INDEPENDENT from [RiwayaId]. */
enum class QuranScript {
    UTHMANI,
    IMLAI,
    INDOPAK,
    /** Legacy persisted value; resolves to Uthmani. Never select in new UI. */
    TAJWEED,
}

/** Dataset-backed script. Legacy Tajweed resolves to Uthmani. */
val QuranScript.datasetScript: QuranScript
    get() = if (this == QuranScript.TAJWEED) QuranScript.UTHMANI else this

/** Reading layout. */
enum class ReadingMode { READING, MUSHAF }

/** Ayah number rendering. */
enum class AyahNumberStyle { ARABIC_INDIC, EASTERN_ARABIC, LATIN }

/** Font choice — independent from script & riwaya. */
enum class QuranFont { UTHMANI, NASKH, NOTO_NASKH, INDOPAK }

/** One verified Quran edition = one riwaya + one script + source metadata. */
data class QuranEdition(
    val id: String, // e.g. "hafs-an-asim__uthmani"
    val riwaya: RiwayaId,
    val script: QuranScript,
    val language: String, // 'ar'
    val source: String,
    val version: String,
    val isDownloaded: Boolean = false,
    val downloadSizeMb: Double? = null,
)

data class SurahMeta(
    val number: Int,
    val nameAr: String,
    val nameEn: String,
    val nameFr: String,
    val ayahCount: Int,
    val makki: Boolean,
)

data class Ayah(
    val surah: Int,
    val ayah: Int,
    val text: String,
    val editionId: String,
    val juz: Int? = null,
    val hizb: Int? = null,
    val rub: Int? = null,
    val page: Int? = null,
    val isSajda: Boolean = false,
    /**
     * Optional mapping to the app-wide canonical verse identity.
     * Bundled datasets are normalized to the same 6236-row coordinates,
     * so these stay null and [surah]/[ayah] are used.
     */
    val canonicalSurah: Int? = null,
    val canonicalAyah: Int? = null,
    /** Edition/mushaf-facing ayah number. Defaults to [ayah]. */
    val displayAyah: Int? = null,
    /**
     * True when no verified dataset row exists yet. UI must show
     * "Content unavailable for this source." and never invent text.
     */
    val isPlaceholder: Boolean = false,
) {
    init {
        require((canonicalSurah == null) == (canonicalAyah == null)) {
            "canonicalSurah and canonicalAyah must be set together"
        }
    }

    val canonicalSurahNumber: Int get() = canonicalSurah ?: surah
    val canonicalAyahNumber: Int get() = canonicalAyah ?: ayah
    val displayAyahNumber: Int get() = displayAyah ?: ayah
    val canonicalVerseId: String get() = "$canonicalSurahNumber:$canonicalAyahNumber"
    val key: String get() = canonicalVerseId
}

data class QuranTranslation(
    val id: String,
    val language: String,
    val translator: String,
    val source: String,
    val version: String? = null,
    val bundled: Boolean = true,
)

data class TafsirEntry(
    val surah: Int,
    val ayah: Int,
    val tafsirId: String,
    val source: String,
    val text: String,
)

data class TafsirInfo(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val language: String,
    val source: String,
    val bundled: Boolean,
)
