package com.godfather59.quransunnah

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.godfather59.quransunnah.data.AndroidAssetReader
import com.godfather59.quransunnah.data.parsePipeMap
import com.godfather59.quransunnah.quran.translationAssets
import com.godfather59.quransunnah.prayer.localizedCityName
import java.io.File
import java.security.MessageDigest
import java.nio.ByteBuffer
import java.util.Locale
import java.util.TimeZone
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

data class ScreenshotCase(val screen: String, val locale: String, val dark: Boolean, val fontScale: Float) {
    val fileName: String get() = "$screen-$locale-${if (dark) "dark" else "light"}-${(fontScale * 100).toInt()}.png"
    override fun toString(): String = fileName.removeSuffix(".png")
}

/** Actual production composables, with a fixed clock, viewport and source-backed content. */
@RunWith(Parameterized::class)
@SdkSuppress(minSdkVersion = 26)
class VisualRegressionTest(private val spec: ScreenshotCase) {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private var originalPreferences: Map<String, *> = emptyMap<String, Any>()
    private lateinit var originalZone: TimeZone
    private val showContent = mutableStateOf(true)
    private var contentSet = false

    @Before
    fun seedReproduciblePreferences() {
        originalZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        originalPreferences = preferences.all.toMap()
        check(preferences.edit().clear()
            .putBoolean("app.onboarded", true)
            .putString("app.locale", spec.locale)
            .putString("app.themeName", if (spec.dark) "dark" else "light")
            .putString("q.riwayaName", "hafsAsim")
            .putString("q.scriptName", "uthmani")
            .putString("q.fontName", "uthmani")
            .putInt("q.lastSurah", 55)
            .putInt("q.lastAyah", 64)
            .putFloat("q.textSize", 26f)
            .putFloat("q.translationTextSize", 16f)
            .putFloat("q.lineSpacing", 1.65f)
            .putBoolean("q.showTr", true)
            .putString("q.translations", if (spec.locale == "fr") "fr-hamidullah" else "en-sahih")
            .putString("app.qari", "ar.alafasy")
            .putInt("khatma.streak", 0)
            .putString("prayer.city", "Mecca")
            .putBoolean("prayer.notif", false)
            .commit())
    }

    @After
    fun restorePreferences() {
        if (contentSet) {
            compose.runOnIdle { showContent.value = false }
            compose.waitForIdle()
        }
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear()
        originalPreferences.forEach { (key, value) ->
            when (value) {
                is String -> editor.putString(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
            }
        }
        check(editor.commit())
        TimeZone.setDefault(originalZone)
    }

    @Test
    fun captureProductionScreen() {
        val locale = Locale.forLanguageTag(spec.locale)
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
            fontScale = spec.fontScale
            densityDpi = 320
            screenWidthDp = 360
            screenHeightDp = 740
            smallestScreenWidthDp = 360
            orientation = Configuration.ORIENTATION_PORTRAIT
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                if (spec.dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        }
        val localizedContext = context.createConfigurationContext(configuration)
        val reader = AndroidAssetReader(localizedContext)
        val expectedTranslation = requireNotNull(parsePipeMap(requireNotNull(reader.readText(
            translationAssets.getValue(if (spec.locale == "fr") "fr-hamidullah" else "en-sahih"),
        )))["55:64"])
        compose.setContent {
            if (showContent.value) {
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides configuration,
                LocalLayoutDirection provides if (spec.locale == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr,
                LocalDensity provides Density(2f, spec.fontScale),
                LocalUiClockMillis provides FIXTURE_MILLIS,
            ) {
                MaterialTheme(colorScheme = if (spec.dark) appDarkScheme() else appLightScheme(), shapes = appShapes()) {
                    Surface(Modifier.requiredSize(360.dp, 740.dp).testTag(ROOT)) {
                        Box(Modifier.consumeWindowInsets(WindowInsets.statusBars.union(WindowInsets.navigationBars))) {
                            when (spec.screen) {
                                "home" -> HomeScreen()
                                "reader" -> QuranReaderScreen(55, onBack = {}, applyStatusBarInset = false)
                                "mushaf" -> MushafReaderScreen(initialPage = 3, onBack = {})
                                "settings" -> SettingsScreen(onBack = {})
                            }
                        }
                    }
                }
            }
            }
        }
        contentSet = true
        compose.waitUntil(timeoutMillis = 30_000) {
            when (spec.screen) {
                "home" -> compose.onAllNodesWithText(localizedContext.getString(R.string.continueReading))
                    .fetchSemanticsNodes().isNotEmpty() &&
                    compose.onAllNodesWithText(localizedCityName("Mecca", spec.locale), substring = true)
                        .fetchSemanticsNodes().isNotEmpty() &&
                    listOf("dailyAyahCard", "dailyHadithCard").all { tag ->
                        compose.onAllNodesWithTag(tag).fetchSemanticsNodes().all { node ->
                            node.config.getOrElse(UiContentReady) { false }
                        }
                    }
                "reader" -> compose.onAllNodesWithTag("readerAyah_55_64").fetchSemanticsNodes().any {
                    it.boundsInRoot.height > 0 && it.boundsInRoot.top < 1480
                } && compose.onAllNodesWithText(expectedTranslation).fetchSemanticsNodes().isNotEmpty()
                "settings" -> compose.onAllNodesWithTag("settingsList").fetchSemanticsNodes().any {
                    it.boundsInRoot.height > 0 && it.config.getOrElse(UiContentReady) { false }
                }
                else -> compose.onAllNodesWithTag("mushafPageContent_3").fetchSemanticsNodes().isNotEmpty()
            }
        }
        // Wait for real async content/font layout to settle; never approve a loading placeholder.
        var lastHash: String? = null
        var equalFrames = 0
        var bitmap: Bitmap? = null
        compose.waitUntil(timeoutMillis = 15_000) {
            val candidate = compose.onNodeWithTag(ROOT).captureToImage().asAndroidBitmap()
            val pixels = ByteBuffer.allocate(candidate.byteCount)
            candidate.copyPixelsToBuffer(pixels)
            val hash = MessageDigest.getInstance("SHA-256").digest(pixels.array()).joinToString("") { "%02x".format(it) }
            equalFrames = if (hash == lastHash) equalFrames + 1 else 0
            lastHash = hash
            bitmap = candidate
            equalFrames >= 3
        }
        val captured = requireNotNull(bitmap)
        assertEquals(720, captured.width)
        assertEquals(1480, captured.height)
        val output = outputDirectory()
        File(output, spec.fileName).outputStream().use { check(captured.compress(Bitmap.CompressFormat.PNG, 100, it)) }
    }

    companion object {
        private const val PREFS = "quran_sunnah_kmp"
        private const val ROOT = "visualRegressionRoot"
        private const val FIXTURE_MILLIS = 1768478400000L

        private fun outputDirectory(): File {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            return File(requireNotNull(context.getExternalFilesDir(null)), "visual-regression")
        }

        @JvmStatic @Parameterized.Parameters(name = "{0}")
        fun cases(): List<Array<Any>> = listOf("home", "reader", "mushaf", "settings").flatMap { screen ->
            listOf("ar", "en", "fr").flatMap { locale ->
                listOf(false, true).flatMap { dark -> listOf(1f, 2f).map { arrayOf<Any>(ScreenshotCase(screen, locale, dark, it)) } }
            }
        }

        @JvmStatic @BeforeClass
        fun prepareOutput() {
            check(Build.VERSION.SDK_INT == 35) { "Visual baselines require the pinned API 35 AOSP emulator." }
            val output = outputDirectory()
            val parent = requireNotNull(output.parentFile).canonicalFile
            check(output.canonicalFile.parentFile == parent && output.name == "visual-regression")
            if (output.exists()) check(output.deleteRecursively())
            check(output.mkdirs())
            val plannedCaptures = JSONArray()
            cases().forEach { arguments ->
                val capture = arguments.single() as ScreenshotCase
                plannedCaptures.put(JSONObject().put("file", capture.fileName).put("screen", capture.screen)
                    .put("locale", capture.locale).put("theme", if (capture.dark) "dark" else "light")
                    .put("fontScale", capture.fontScale))
            }
            File(output, "manifest.json").writeText(JSONObject().put("schemaVersion", 1).put("profile", JSONObject()
                .put("apiLevel", 35).put("viewportWidthDp", 360).put("viewportHeightDp", 740).put("density", 2.0)
                .put("fixtureMillis", FIXTURE_MILLIS).put("imageFingerprint", Build.FINGERPRINT))
                .put("captures", plannedCaptures).toString(2) + "\n")
        }
    }
}
