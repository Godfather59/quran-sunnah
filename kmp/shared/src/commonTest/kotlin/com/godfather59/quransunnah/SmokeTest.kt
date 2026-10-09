package com.godfather59.quransunnah

import kotlin.test.Test
import kotlin.test.assertTrue

class SmokeTest {
    @Test
    fun appTagHasPlatform() {
        assertTrue(appTag().startsWith("QuranSunnah/"))
    }
}
