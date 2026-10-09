package com.godfather59.quransunnah

import com.godfather59.quransunnah.ui.AppFontAssets
import com.godfather59.quransunnah.ui.TextRole
import com.godfather59.quransunnah.ui.fontAssetForRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TypographyTest {
    @Test
    fun sacredAndArabicUiRolesUseBundledArabicFonts() {
        assertEquals(AppFontAssets.AMIRI_QURAN, fontAssetForRole(TextRole.QURAN))
        assertEquals(
            AppFontAssets.NOTO_NASKH_ARABIC,
            fontAssetForRole(TextRole.ARABIC_UI),
        )
        assertNull(fontAssetForRole(TextRole.TRANSLATION))
    }
}
