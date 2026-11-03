package com.godfather59.quransunnah

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.godfather59.quransunnah.audio.reciters
import com.godfather59.quransunnah.data.AndroidAssetReader
import com.godfather59.quransunnah.data.loadQuranEdition
import com.godfather59.quransunnah.data.parsePipeMap
import com.godfather59.quransunnah.quran.translationAssets
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

/** Long verified content must remain navigable at a large system font scale. */
class ReaderLayoutTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun longestAyahSheetKeepsShareActionReachable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val reader = AndroidAssetReader(context)
        val ayah = requireNotNull(loadQuranEdition(reader, "hafs-an-asim__uthmani"))
            .first { it.surah == 2 && it.ayah == 282 }
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                    AyahActionSheet(
                        ayah = ayah,
                        reference = "2:282",
                        translation = null,
                        quranFontFamily = null,
                        reciter = reciters.first(),
                        onOpenTafsir = {},
                        onOpenCompare = {},
                        onOpenStudy = {},
                        onDismiss = {},
                        reader = reader,
                        editionId = ayah.editionId,
                        surahDisplayName = "2",
                    )
                }
            }
        }

        compose.onNodeWithTag("ayahSheetShare").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun mushafLastAyahRemainsReachableInSmallViewport() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val metadata = runBlocking { AndroidStores.mushafMetadataReader(context).load() }
        val page = 3
        val ayahs = requireNotNull(loadQuranEdition(AndroidAssetReader(context), "hafs-an-asim__uthmani"))
            .filter { metadata.pageOf(it.surah, it.ayah) == page }
        val last = ayahs.last()
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2f)) {
                    Box(Modifier.width(320.dp).height(420.dp)) {
                        MushafPage(
                            page = page,
                            ayahs = ayahs,
                            quranFontFamily = null,
                            onAyahClick = {},
                            mushafMetadata = metadata,
                        )
                    }
                }
            }
        }

        compose.onNodeWithTag("mushafAyah_${last.surah}_${last.ayah}")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun arabicAndBracketedEnglishKeepSourceDirectionInRtlInterface() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val reader = AndroidAssetReader(context)
        val ayah = requireNotNull(loadQuranEdition(reader, "hafs-an-asim__uthmani"))
            .first { it.surah == 55 && it.ayah == 64 }
        val english = requireNotNull(parsePipeMap(
            requireNotNull(reader.readText(translationAssets.getValue("en-sahih"))),
        )["55:64"])
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(Modifier.width(320.dp).testTag("bidiReaderColumn")) {
                        TajweedAyahText(
                            ayah = ayah,
                            showTajweed = false,
                            tajweedSpans = emptyMap(),
                            quranFontFamily = null,
                            modifier = Modifier.testTag("bidiArabic"),
                        )
                        ReaderTranslationText(
                            text = english,
                            textSize = 16f,
                            lineSpacing = 1.65f,
                            modifier = Modifier.testTag("bidiEnglish"),
                        )
                    }
                }
            }
        }

        val width = compose.onNodeWithTag("bidiReaderColumn").fetchSemanticsNode().boundsInRoot.width
        val arabic = textLayout("bidiArabic")
        val translation = textLayout("bidiEnglish")
        assertEquals(ResolvedTextDirection.Rtl, arabic.getParagraphDirection(0))
        assertEquals(width, arabic.size.width.toFloat(), 1f)
        assertEquals(width, arabic.getLineRight(0), 1f)
        assertEquals(ResolvedTextDirection.Ltr, translation.getParagraphDirection(0))
        assertEquals(0f, translation.getLineLeft(0), 1f)
        assertEquals(ResolvedTextDirection.Ltr, translation.getBidiRunDirection(english.indexOf('[')))
        assertEquals(ResolvedTextDirection.Ltr, translation.getBidiRunDirection(english.lastIndex))
        assertEquals(english, translation.layoutInput.text.text)
    }

    private fun textLayout(tag: String): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        compose.onNodeWithTag(tag).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        return results.single()
    }
}
