package com.godfather59.quransunnah

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

class OnboardingFlowTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun flowRendersProgressAndNext() {
        compose.setContent {
            MaterialTheme {
                OnboardingFlow(onComplete = {})
            }
        }

        compose.onNodeWithTag("onboardingFlow").assertIsDisplayed()
        compose.onNodeWithTag("onboardingProgress").assertIsDisplayed()
        compose.onNodeWithTag("onboardingNext").assertIsDisplayed()
        compose.onNodeWithTag("onboardingSkip").assertIsDisplayed()
    }

    @Test
    fun nextAdvancesStep() {
        compose.setContent {
            MaterialTheme {
                OnboardingFlow(onComplete = {})
            }
        }

        // First page shows language choices; tapping Next must keep flow visible.
        compose.onNodeWithTag("onboardingNext").performClick()
        compose.onNodeWithTag("onboardingFlow").assertIsDisplayed()
        compose.onNodeWithTag("onboardingProgress").assertIsDisplayed()
    }

    @Test
    fun skipCompletesWithDefaults() {
        var completed = false
        compose.setContent {
            MaterialTheme {
                OnboardingFlow(onComplete = { completed = true })
            }
        }

        compose.onNodeWithTag("onboardingSkip").performClick()
        assert(completed) { "Skip must call onComplete" }
    }

    @Test
    fun skippingRemainingSetupKeepsSelectedEdition() {
        var completed: OnboardingChoices? = null
        compose.setContent { MaterialTheme { OnboardingFlow(onComplete = { completed = it }) } }
        compose.onNodeWithTag("onboardingNext").performClick()
        compose.onNodeWithTag("onboardingEdition_warsh-an-nafi__uthmani").performClick()
        compose.onNodeWithTag("onboardingSkip").performClick()
        assertEquals("warshNafi", completed?.riwayaName)
        assertEquals("uthmani", completed?.scriptName)
    }
}
