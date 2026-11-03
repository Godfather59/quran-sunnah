package com.godfather59.quransunnah

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class DownloadsScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun downloadsListRenders() {
        compose.setContent {
            MaterialTheme {
                DownloadsScreen(isActive = true, onBack = null)
            }
        }

        compose.onNodeWithTag("downloadsList").assertIsDisplayed()
    }
}

class SettingsScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun settingsListRenders() {
        compose.setContent {
            MaterialTheme {
                SettingsScreen(isActive = true, onBack = null)
            }
        }

        // Settings loads prefs on IO; list appears after load.
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("settingsList").fetchSemanticsNodes().any {
                it.boundsInRoot.width > 0 && it.boundsInRoot.height > 0
            }
        }
        compose.onNodeWithTag("settingsList").assertIsDisplayed()
    }
}

class SunnahHomeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun sunnahHomeRendersCollections() {
        compose.setContent {
            MaterialTheme {
                SunnahHomeScreen(isActive = true)
            }
        }

        compose.onNodeWithTag("sunnahHome").assertIsDisplayed()
        compose.onNodeWithTag("sunnahCollections").assertIsDisplayed()
    }
}
