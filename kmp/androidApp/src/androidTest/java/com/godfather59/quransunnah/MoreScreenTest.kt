package com.godfather59.quransunnah

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class MoreScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun moreMenuShowsSearchDownloadsAndSettings() {
        compose.setContent {
            MaterialTheme {
                MoreScreen(
                    onOpenAyahRef = {},
                    onThemeChange = {},
                    isActive = true,
                )
            }
        }

        compose.onNodeWithText(context.getString(R.string.more)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.search)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.downloads)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.settings)).assertIsDisplayed()
    }

    @Test
    fun settingsDestinationOpensFromMoreMenu() {
        compose.setContent {
            MaterialTheme {
                MoreScreen(
                    onOpenAyahRef = {},
                    onThemeChange = {},
                    isActive = true,
                )
            }
        }

        compose.onNodeWithText(context.getString(R.string.settings)).performClick()

        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("settingsList").fetchSemanticsNodes().any {
                it.boundsInRoot.width > 0 && it.boundsInRoot.height > 0
            }
        }

        compose.onNodeWithContentDescription(context.getString(R.string.back)).assertIsDisplayed()
    }
}
