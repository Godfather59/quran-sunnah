package com.godfather59.quransunnah.ui

object AppFontAssets {
    const val AMIRI_QURAN = "assets/fonts/AmiriQuran-Regular.ttf"
    const val NOTO_NASKH_ARABIC = "assets/fonts/NotoNaskhArabic-Variable.ttf"
}

enum class TextRole {
    QURAN,
    ARABIC_UI,
    TRANSLATION,
}

fun fontAssetForRole(role: TextRole): String? =
    when (role) {
        TextRole.QURAN -> AppFontAssets.AMIRI_QURAN
        TextRole.ARABIC_UI -> AppFontAssets.NOTO_NASKH_ARABIC
        TextRole.TRANSLATION -> null
    }
