package com.godfather59.quransunnah

import com.godfather59.quransunnah.ui.HorizontalIconDirection
import com.godfather59.quransunnah.ui.LayoutDirection
import com.godfather59.quransunnah.ui.layoutDirectionForLanguage
import com.godfather59.quransunnah.ui.trailingChevronDirection
import kotlin.test.Test
import kotlin.test.assertEquals

class LayoutDirectionTest {
    @Test
    fun trailingChevronMirrorsForRtl() {
        assertEquals(
            HorizontalIconDirection.RIGHT,
            trailingChevronDirection(LayoutDirection.LTR),
        )
        assertEquals(
            HorizontalIconDirection.LEFT,
            trailingChevronDirection(LayoutDirection.RTL),
        )
    }

    @Test
    fun arabicUiIsRtl() {
        assertEquals(LayoutDirection.RTL, layoutDirectionForLanguage("ar"))
        assertEquals(LayoutDirection.RTL, layoutDirectionForLanguage("AR"))
        assertEquals(LayoutDirection.LTR, layoutDirectionForLanguage("en"))
        assertEquals(LayoutDirection.LTR, layoutDirectionForLanguage("fr"))
    }
}
