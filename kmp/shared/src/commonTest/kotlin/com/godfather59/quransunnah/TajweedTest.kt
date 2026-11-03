package com.godfather59.quransunnah

import com.godfather59.quransunnah.data.isTajweedEdition
import com.godfather59.quransunnah.data.parseTajweedSurah
import com.godfather59.quransunnah.data.tajweedRuleInfo
import com.godfather59.quransunnah.data.tajweedRules
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TajweedTest {
    @Test
    fun paletteHas18Rules() {
        assertEquals(18, tajweedRules.size)
        assertEquals("غنة", tajweedRuleInfo("ghunnah").nameAr)
        assertEquals(0xFFC62828, tajweedRuleInfo("qalqalah").colorValue)
    }

    @Test
    fun unknownRuleFallsBackToGrey() {
        val info = tajweedRuleInfo("future_rule")
        assertEquals(0xFF757575, info.colorValue)
        assertEquals("future_rule", info.nameEn)
    }

    @Test
    fun onlyHafsUthmaniHasTajweed() {
        assertTrue(isTajweedEdition("hafs-an-asim__uthmani"))
        assertFalse(isTajweedEdition("hafs-an-asim__imlai"))
        assertFalse(isTajweedEdition("warsh-an-nafi__uthmani"))
    }

    @Test
    fun parseSurahOneShape() {
        val raw = """{"surah":1,"entries":[
            {"ayah":1,"annotations":[
              {"rule":"hamzat_wasl","start":7,"end":8},
              {"rule":"madd_2","start":24,"end":25},
              {"rule":"","start":1,"end":2},
              {"rule":"madd_2","start":5,"end":5},
              {"rule":"madd_2","start":-1,"end":2}
            ]},
            {"ayah":0,"annotations":[{"rule":"madd_2","start":1,"end":2}]}
          ]}"""
        val parsed = parseTajweedSurah(raw, 1)
        assertEquals(1, parsed.size)
        val spans = parsed[1]!!
        assertEquals(2, spans.size)
        assertEquals("hamzat_wasl", spans[0].rule)
        assertEquals(7, spans[0].start)
        assertEquals(8, spans[0].end)
    }

    @Test
    fun wrongSurahAndGarbageAreEmpty() {
        assertTrue(parseTajweedSurah("""{"surah":2,"entries":[]}""", 1).isEmpty())
        assertTrue(parseTajweedSurah("not json", 1).isEmpty())
    }
}
