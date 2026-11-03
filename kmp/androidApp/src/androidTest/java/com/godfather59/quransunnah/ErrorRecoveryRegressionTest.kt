package com.godfather59.quransunnah

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.godfather59.quransunnah.data.AndroidAssetReader
import com.godfather59.quransunnah.data.loadQuranEdition
import com.godfather59.quransunnah.search.SearchEngine
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** Run with Wi-Fi and mobile data disabled; every successful result uses bundled text. */
class ErrorRecoveryRegressionTest {
    @get:Rule
    val compose = createComposeRule()

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext

    @Test
    fun defaultSearchFindsAndOpensVerifiedAyahWhileDisconnected() {
        assertDisconnected()
        val ayah = verifiedAyah()
        val opened = AtomicReference<String?>(null)
        compose.setContent {
            MaterialTheme {
                SearchScreen(onOpenAyahRef = { opened.set(it) })
            }
        }

        searchForAyah()
        awaitAyahResult()
        compose.onNodeWithTag("searchHit_2:255")
            .assertIsDisplayed()
            .assertTextContains(ayah.text)
            .performClick()
        compose.runOnIdle { assertEquals("2:255", opened.get()) }
        assertDisconnected()
    }

    @Test
    fun failedIndexShowsRetryThenLoadsVerifiedOfflineResult() {
        assertDisconnected()
        val engine = SearchEngine()
        val attempts = AtomicInteger(0)
        val failFirstLoad = CompletableDeferred<Unit>()
        val finishRetry = CompletableDeferred<Unit>()
        val opened = AtomicReference<String?>(null)
        val ayah = verifiedAyah()
        val loader: suspend (AndroidAssetReader, SearchEngine, Context, (Boolean) -> Unit) -> Unit =
            { reader, targetEngine, _, progress ->
                when (attempts.incrementAndGet()) {
                    1 -> {
                        failFirstLoad.await()
                        throw IOException("Deliberate local index failure")
                    }
                    else -> {
                        finishRetry.await()
                        loadBundledQuran(reader, targetEngine)
                        progress(true)
                    }
                }
            }
        compose.setContent {
            MaterialTheme {
                SearchScreen(
                    onOpenAyahRef = { opened.set(it) },
                    searchEngine = engine,
                    loadIndex = loader,
                )
            }
        }

        compose.waitUntil(5_000) { attempts.get() == 1 }
        compose.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
        // Enter the query before failure: retry must preserve it and execute it
        // against the newly loaded index without a second input event.
        searchForAyah()
        failFirstLoad.complete(Unit)
        val error = context.getString(R.string.searchFailed)
        compose.waitUntil(5_000) { compose.onAllNodesWithText(error).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(error).assertIsDisplayed()
        compose.onNodeWithTag("searchHit_2:255").assertDoesNotExist()

        compose.onNodeWithText(context.getString(R.string.retry)).performClick()
        compose.waitUntil(5_000) { attempts.get() == 2 }
        compose.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
        compose.onNodeWithText(error).assertDoesNotExist()
        finishRetry.complete(Unit)

        awaitAyahResult()
        compose.onNodeWithTag("searchHit_2:255")
            .assertIsDisplayed()
            .assertTextContains(ayah.text)
            .performClick()
        compose.runOnIdle {
            assertEquals(2, attempts.get())
            assertEquals("2:255", opened.get())
        }
        assertDisconnected()
    }

    @Test
    fun leavingSearchCancelsIndexLoadWithoutShowingFailureAndReentryLoadsNormally() {
        assertDisconnected()
        val active = mutableStateOf(true)
        val engine = SearchEngine()
        val attempts = AtomicInteger(0)
        val firstLoadCancelled = CompletableDeferred<Unit>()
        val loader: suspend (AndroidAssetReader, SearchEngine, Context, (Boolean) -> Unit) -> Unit =
            { reader, targetEngine, _, progress ->
                if (attempts.incrementAndGet() == 1) {
                    try {
                        awaitCancellation()
                    } finally {
                        firstLoadCancelled.complete(Unit)
                    }
                } else {
                    loadBundledQuran(reader, targetEngine)
                    progress(true)
                }
            }
        compose.setContent {
            MaterialTheme {
                SearchScreen(isActive = active.value, searchEngine = engine, loadIndex = loader)
            }
        }

        compose.waitUntil(5_000) { attempts.get() == 1 }
        searchForAyah()
        compose.runOnIdle { active.value = false }
        compose.waitUntil(5_000) { firstLoadCancelled.isCompleted }
        compose.onNodeWithText(context.getString(R.string.searchFailed)).assertDoesNotExist()
        compose.runOnIdle { active.value = true }

        awaitAyahResult()
        compose.onNodeWithTag("searchHit_2:255").assertIsDisplayed().assertTextContains(verifiedAyah().text)
        compose.runOnIdle { assertEquals(2, attempts.get()) }
    }

    private fun searchForAyah() {
        compose.onNode(hasSetTextAction()).performTextInput("2:255")
        compose.onNode(hasSetTextAction()).performImeAction()
    }

    private fun awaitAyahResult() {
        // The real loader may also index bundled Hadith/Tafsir on a cold CI
        // emulator. Bounded polling permits that work without fixed sleeps.
        compose.waitUntil(timeoutMillis = 60_000) {
            compose.onAllNodesWithTag("searchHit_2:255").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun verifiedAyah() = requireNotNull(loadQuranEdition(AndroidAssetReader(context), "hafs-an-asim__uthmani"))
        .first { it.surah == 2 && it.ayah == 255 }

    private suspend fun loadBundledQuran(reader: AndroidAssetReader, engine: SearchEngine) {
        withContext(Dispatchers.IO) {
            engine.indexQuran(requireNotNull(loadQuranEdition(reader, "hafs-an-asim__uthmani")))
        }
    }

    private fun assertDisconnected() {
        if (Build.VERSION.SDK_INT >= 29) {
            // Borrow the shell's read permission only; the app itself does not
            // need ACCESS_NETWORK_STATE and the test never changes connectivity.
            val automation = instrumentation.uiAutomation
            automation.adoptShellPermissionIdentity(Manifest.permission.ACCESS_NETWORK_STATE)
            try {
                val manager = requireNotNull(context.getSystemService(ConnectivityManager::class.java))
                assertNull("Disable Wi-Fi and mobile data before this offline regression suite", manager.activeNetwork)
            } finally {
                automation.dropShellPermissionIdentity()
            }
        } else {
            assertEquals("Wi-Fi must be disabled", "0", shellSetting("wifi_on"))
            assertEquals("Mobile data must be disabled", "0", shellSetting("mobile_data"))
        }
    }

    private fun shellSetting(key: String): String =
        ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand("settings get global $key"))
            .bufferedReader().use { it.readText().trim() }
}
