package com.godfather59.quransunnah

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import com.godfather59.quransunnah.data.AndroidAssetReader
import com.godfather59.quransunnah.data.loadQuranEdition
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Run on the isolated emulator with Wi-Fi and mobile data disabled. */
class ReaderFlowRegressionTest {
    @get:Rule
    val compose = createComposeRule()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val preferences get() = context.getSharedPreferences("quran_sunnah_kmp", Context.MODE_PRIVATE)
    private val contentVisible = mutableStateOf(true)
    private var savedPreferences: Map<String, *> = emptyMap<String, Any>()
    private var contentWasSet = false

    @Before
    fun useKnownReaderPreferences() {
        savedPreferences = preferences.all.toMap()
        ReaderPrefs.saveReaderDisplay(context, "hafsAsim", "uthmani")
        ReaderPrefs.saveLastPosition(context, 1, 1)
        ReaderPrefs.saveShowTranslation(context, false)
        ReaderPrefs.saveReaderFont(context, "uthmani")
        ReaderPrefs.saveReadingStyle(context, 26f, 16f, 1.65f)
    }

    @After
    fun restoreReaderPreferences() {
        if (contentWasSet) {
            compose.runOnIdle { contentVisible.value = false }
            compose.waitForIdle()
        }
        val editor = preferences.edit().clear()
        savedPreferences.forEach { (key, value) ->
            when (value) {
                is String -> editor.putString(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
            }
        }
        assertTrue("Reader test preferences must be restored", editor.commit())
    }

    @Test
    fun warshSelectionSurvivesCancellingAndEnteringHafsMushaf() {
        val restored = StateRestorationTester(compose)
        setReaderContent(surah = 1, restoration = restored)
        awaitTag("readerAyah_1_1")
        compose.onNodeWithTag("readerSettings").performClick()
        compose.onNodeWithTag("readerEditionChoices").performScrollTo()
            .performScrollToNode(hasTestTag("readerEdition_warsh-an-nafi__uthmani"))
        compose.onNodeWithTag("readerEdition_warsh-an-nafi__uthmani").performClick().assertIsSelected()
        pressBack()
        assertReaderText("warsh-an-nafi__uthmani", 1, 1)
        assertEquals("warsh-an-nafi__uthmani", ReaderPrefs.load(context).readerEditionId())

        openMushafConfirmation()
        compose.onNodeWithTag("mushafEditionCancel").performClick()
        assertReaderText("warsh-an-nafi__uthmani", 1, 1)
        assertEquals("warsh-an-nafi__uthmani", ReaderPrefs.load(context).readerEditionId())

        openMushafConfirmation()
        compose.onNodeWithTag("mushafEditionConfirm").performClick()
        awaitTag("mushafAyah_1_1")
        val hafsText = verifiedAyah("hafs-an-asim__uthmani", 1, 1)
        compose.onNodeWithTag("mushafAyah_1_1").assertTextContains(hafsText, substring = true)
        assertEquals("warsh-an-nafi__uthmani", ReaderPrefs.load(context).readerEditionId())

        restored.emulateSavedInstanceStateRestore()
        awaitTag("mushafAyah_1_1")
        compose.onNodeWithTag("mushafAyah_1_1").assertTextContains(hafsText, substring = true)
        compose.onNodeWithTag("mushafBack").performClick()
        assertReaderText("warsh-an-nafi__uthmani", 1, 1)
        assertEquals("warsh-an-nafi__uthmani", ReaderPrefs.load(context).readerEditionId())
    }

    @Test
    fun savedReaderPositionReturnsToTheSameVerifiedAyahAfterRestoration() {
        ReaderPrefs.saveLastPosition(context, 2, 1)
        val restored = StateRestorationTester(compose)
        setReaderContent(surah = 2, restoration = restored)
        awaitTag("readerAyahList")
        compose.onNodeWithTag("readerAyahList").performScrollToIndex(49)
        assertReaderText("hafs-an-asim__uthmani", 2, 50)
        compose.waitUntil(timeoutMillis = 15_000) {
            ReaderPrefs.load(context).let { it.lastSurah == 2 && it.lastAyah == 50 }
        }

        restored.emulateSavedInstanceStateRestore()

        assertReaderText("hafs-an-asim__uthmani", 2, 50)
        assertEquals(2, ReaderPrefs.load(context).lastSurah)
        assertEquals(50, ReaderPrefs.load(context).lastAyah)
    }

    @Test
    fun offlineReaderRendersExactBundledAyah() {
        assertOffline()
        ReaderPrefs.saveLastPosition(context, 2, 255)
        setReaderContent(surah = 2)

        assertReaderText("hafs-an-asim__uthmani", 2, 255)
        assertOffline()
    }

    private fun setReaderContent(surah: Int, restoration: StateRestorationTester? = null) {
        val content: @Composable () -> Unit = {
            MaterialTheme {
                if (contentVisible.value) QuranReaderScreen(surahNumber = surah, onBack = {})
            }
        }
        if (restoration == null) compose.setContent(content) else restoration.setContent(content)
        contentWasSet = true
    }

    private fun openMushafConfirmation() {
        awaitTag("readerSettings")
        compose.onNodeWithTag("readerSettings").performClick()
        compose.onNodeWithTag("readerOpenMushaf").performScrollTo().performClick()
        awaitTag("mushafEditionConfirm")
    }

    private fun assertReaderText(edition: String, surah: Int, ayah: Int) {
        val tag = "readerAyah_${surah}_$ayah"
        val expected = verifiedAyah(edition, surah, ayah)
        compose.waitUntil(timeoutMillis = 15_000) {
            compose.onAllNodes(hasTestTag(tag) and hasText(expected)).fetchSemanticsNodes().any {
                it.boundsInRoot.width > 0 && it.boundsInRoot.height > 0
            }
        }
        compose.onNodeWithTag(tag).assertIsDisplayed().assertTextContains(expected)
    }

    private fun verifiedAyah(edition: String, surah: Int, ayah: Int): String =
        requireNotNull(loadQuranEdition(AndroidAssetReader(context), edition))
            .first { it.surah == surah && it.ayah == ayah }.text

    private fun awaitTag(tag: String) {
        compose.waitUntil(timeoutMillis = 15_000) {
            compose.onAllNodesWithTag(tag).fetchSemanticsNodes().any {
                it.boundsInRoot.width > 0 && it.boundsInRoot.height > 0
            }
        }
    }

    private fun assertOffline() {
        // The application needs no connectivity permission for normal use.
        // Adopt only this read permission to verify the test's offline setup.
        instrumentation.uiAutomation.adoptShellPermissionIdentity(Manifest.permission.ACCESS_NETWORK_STATE)
        try {
            val connectivity = context.getSystemService(ConnectivityManager::class.java)
            assertNull("Run ReaderFlowRegressionTest with Wi-Fi and mobile data disabled", connectivity.activeNetwork)
        } finally {
            instrumentation.uiAutomation.dropShellPermissionIdentity()
        }
    }
}
