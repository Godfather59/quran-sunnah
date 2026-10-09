package com.godfather59.quransunnah.quran

// Translation + tafsir catalogs. Ported from kTranslationCatalog and
// kTafsirCatalog. Unbundled ids plug into the same layout once sourced.

val translationAssets: Map<String, String> = mapOf(
    "en-sahih" to "assets/quran/translations/en-sahih.txt",
    "fr-hamidullah" to "assets/quran/translations/fr-hamidullah.txt",
)

val translationCatalog: List<QuranTranslation> = listOf(
    QuranTranslation(
        id = "en-sahih",
        language = "en",
        translator = "Saheeh International",
        source = "Tanzil — https://tanzil.net/trans/en.sahih",
    ),
    QuranTranslation(
        id = "fr-hamidullah",
        language = "fr",
        translator = "Muhammad Hamidullah",
        source = "Tanzil — https://tanzil.net/trans/fr.hamidullah",
    ),
    QuranTranslation(
        id = "en-rowwad",
        language = "en",
        translator = "Rowwad Translation Center",
        source = "QuranEnc · key english_rwwad",
        version = "1.0.19 (2026-03-12)",
        bundled = false,
    ),
    QuranTranslation(
        id = "fr-rachid",
        language = "fr",
        translator = "Rachid Maach",
        source = "QuranEnc · key french_rashid",
        version = "1.0.3 (2026-06-21)",
        bundled = false,
    ),
)

val tafsirCatalog: List<TafsirInfo> = listOf(
    TafsirInfo(
        id = "jalalayn",
        titleAr = "تفسير الجلالين",
        titleEn = "Tafsir al-Jalalayn",
        language = "ar",
        source = "quran-api@1 ara-jalaladdinalmah (tanzil.net)",
        bundled = true,
    ),
    TafsirInfo(
        id = "siraj",
        titleAr = "السراج في تفسير القرآن",
        titleEn = "Al-Siraj Tafsir",
        language = "ar",
        source = "quran-api@1 ara-sirajtafseer (quranenc.com)",
        bundled = true,
    ),
    TafsirInfo(
        id = "ibn-kathir",
        titleAr = "تفسير ابن كثير",
        titleEn = "Tafsir Ibn Kathir",
        language = "ar",
        source = "Verified licensed dataset required",
        bundled = false,
    ),
    TafsirInfo(
        id = "tabari",
        titleAr = "تفسير الطبري",
        titleEn = "Tafsir al-Tabari",
        language = "ar",
        source = "Verified licensed dataset required",
        bundled = false,
    ),
    TafsirInfo(
        id = "saadi",
        titleAr = "تفسير السعدي",
        titleEn = "Tafsir al-Sa‘di",
        language = "ar",
        source = "QuranEnc · key arabic_saadi · V1.0.0 (2026-07-27)",
        bundled = false,
    ),
    TafsirInfo(
        id = "qurtubi",
        titleAr = "تفسير القرطبي",
        titleEn = "Tafsir al-Qurtubi",
        language = "ar",
        source = "Verified licensed dataset required",
        bundled = false,
    ),
)
