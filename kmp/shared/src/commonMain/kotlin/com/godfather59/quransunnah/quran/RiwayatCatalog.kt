package com.godfather59.quransunnah.quran

// Extensible catalog. Adding a riwaya = appending here + shipping its
// verified dataset file. No UI change required.
// Ported from lib/data/seed/riwayat_catalog.dart.

val riwayatCatalog: List<RiwayaInfo> = listOf(
    RiwayaInfo(
        id = RiwayaId.HAFS_ASIM,
        qiraaAr = "عاصم", qiraaEn = "ʿĀṣim", qiraaFr = "ʿÂsim",
        riwayaAr = "حفص عن عاصم", riwayaEn = "Ḥafṣ ʿan ʿĀṣim", riwayaFr = "Ḥafṣ ʿan ʿĀṣim",
        datasetVersion = "1.1 (Tanzil, Feb 2021)",
        source = "Tanzil Project — https://tanzil.net (bundled)",
        isAvailableOfflineSeed = true,
    ),
    RiwayaInfo(
        id = RiwayaId.SHUBAH_ASIM,
        qiraaAr = "عاصم", qiraaEn = "ʿĀṣim", qiraaFr = "ʿÂsim",
        riwayaAr = "شعبة عن عاصم", riwayaEn = "Shuʿbah ʿan ʿĀṣim", riwayaFr = "Shuʿbah ʿan ʿĀṣim",
        datasetVersion = "pending", source = "Verified dataset required",
    ),
    RiwayaInfo(
        id = RiwayaId.WARSH_NAFI,
        qiraaAr = "نافع", qiraaEn = "Nāfiʿ", qiraaFr = "Nāfiʿ",
        riwayaAr = "ورش عن نافع", riwayaEn = "Warsh ʿan Nāfiʿ", riwayaFr = "Warsh ʿan Nāfiʿ",
        datasetVersion = "quran-api@1 v8 (QuranComplex)",
        source = "quran-api ara-quranwarsh — bundled",
        isAvailableOfflineSeed = true,
    ),
    RiwayaInfo(
        id = RiwayaId.QALUN_NAFI,
        qiraaAr = "نافع", qiraaEn = "Nāfiʿ", qiraaFr = "Nāfiʿ",
        riwayaAr = "قالون عن نافع", riwayaEn = "Qālūn ʿan Nāfiʿ", riwayaFr = "Qālūn ʿan Nāfiʿ",
        datasetVersion = "quran-api@1 v8 (QuranComplex)",
        source = "quran-api ara-quranqaloon — bundled",
        isAvailableOfflineSeed = true,
    ),
    RiwayaInfo(
        id = RiwayaId.BAZZI_IBN_KATHIR,
        qiraaAr = "ابن كثير", qiraaEn = "Ibn Kathīr", qiraaFr = "Ibn Kathīr",
        riwayaAr = "البزي عن ابن كثير", riwayaEn = "Al-Bazzī ʿan Ibn Kathīr", riwayaFr = "Al-Bazzī ʿan Ibn Kathīr",
        datasetVersion = "quran-api@1 candidate",
        source = "quran-api ara-quranbazzi · direct redistribution terms unresolved",
    ),
    RiwayaInfo(
        id = RiwayaId.QUNBUL_IBN_KATHIR,
        qiraaAr = "ابن كثير", qiraaEn = "Ibn Kathīr", qiraaFr = "Ibn Kathīr",
        riwayaAr = "قنبل عن ابن كثير", riwayaEn = "Qunbul ʿan Ibn Kathīr", riwayaFr = "Qunbul ʿan Ibn Kathīr",
        datasetVersion = "pending", source = "Verified dataset required",
    ),
    RiwayaInfo(
        id = RiwayaId.DURI_ABI_AMR,
        qiraaAr = "أبو عمرو", qiraaEn = "Abū ʿAmr", qiraaFr = "Abū ʿAmr",
        riwayaAr = "الدوري عن أبي عمرو", riwayaEn = "Al-Dūrī ʿan Abī ʿAmr", riwayaFr = "Al-Dūrī ʿan Abī ʿAmr",
        datasetVersion = "pending", source = "Verified dataset required",
    ),
    RiwayaInfo(
        id = RiwayaId.SUSI_ABI_AMR,
        qiraaAr = "أبو عمرو", qiraaEn = "Abū ʿAmr", qiraaFr = "Abū ʿAmr",
        riwayaAr = "السوسي عن أبي عمرو", riwayaEn = "Al-Sūsī ʿan Abī ʿAmr", riwayaFr = "Al-Sūsī ʿan Abī ʿAmr",
        datasetVersion = "pending", source = "Verified dataset required",
    ),
    RiwayaInfo(
        id = RiwayaId.HISHAM_IBN_AMIR,
        qiraaAr = "ابن عامر", qiraaEn = "Ibn ʿĀmir", qiraaFr = "Ibn ʿÂmir",
        riwayaAr = "هشام عن ابن عامر", riwayaEn = "Hishām ʿan Ibn ʿĀmir", riwayaFr = "Hishām ʿan Ibn ʿĀmir",
        datasetVersion = "pending", source = "Verified dataset required",
    ),
    RiwayaInfo(
        id = RiwayaId.IBN_DHAKWAN_IBN_AMIR,
        qiraaAr = "ابن عامر", qiraaEn = "Ibn ʿĀmir", qiraaFr = "Ibn ʿÂmir",
        riwayaAr = "ابن ذكوان عن ابن عامر", riwayaEn = "Ibn Dhakwān ʿan Ibn ʿĀmir", riwayaFr = "Ibn Dhakwān ʿan Ibn ʿÂmir",
        datasetVersion = "pending", source = "Verified dataset required",
    ),
    RiwayaInfo(
        id = RiwayaId.KHALAF_HAMZAH,
        qiraaAr = "حمزة", qiraaEn = "Ḥamzah", qiraaFr = "Ḥamzah",
        riwayaAr = "خلف عن حمزة", riwayaEn = "Khalaf ʿan Ḥamzah", riwayaFr = "Khalaf ʿan Ḥamzah",
        datasetVersion = "pending", source = "Verified dataset required",
    ),
    RiwayaInfo(
        id = RiwayaId.KHALLAD_HAMZAH,
        qiraaAr = "حمزة", qiraaEn = "Ḥamzah", qiraaFr = "Ḥamzah",
        riwayaAr = "خلاد عن حمزة", riwayaEn = "Khallād ʿan Ḥamzah", riwayaFr = "Khallād ʿan Ḥamzah",
        datasetVersion = "pending", source = "Verified dataset required",
    ),
    RiwayaInfo(
        id = RiwayaId.ABUL_HARITH_KISAI,
        qiraaAr = "الكسائي", qiraaEn = "Al-Kisāʾī", qiraaFr = "Al-Kisāʾī",
        riwayaAr = "أبو الحارث عن الكسائي", riwayaEn = "Abū al-Ḥārith ʿan Al-Kisāʾī", riwayaFr = "Abū al-Ḥārith ʿan Al-Kisāʾī",
        datasetVersion = "pending", source = "Verified dataset required",
    ),
    RiwayaInfo(
        id = RiwayaId.DURI_KISAI,
        qiraaAr = "الكسائي", qiraaEn = "Al-Kisāʾī", qiraaFr = "Al-Kisāʾī",
        riwayaAr = "الدوري عن الكسائي", riwayaEn = "Al-Dūrī ʿan Al-Kisāʾī", riwayaFr = "Al-Dūrī ʿan Al-Kisāʾī",
        datasetVersion = "pending", source = "Verified dataset required",
    ),
)
