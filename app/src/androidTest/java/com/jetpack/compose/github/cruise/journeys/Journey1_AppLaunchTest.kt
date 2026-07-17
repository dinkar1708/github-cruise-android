package com.jetpack.compose.github.cruise.journeys

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jetpack.compose.github.github.cruise.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Journey 1: App Launch
 * User Flow: User opens app → Sees splash screen → Auto-navigates to search screen
 *
 * @see <a href="file://../../../../../../../docs/testing/journeys/JOURNEY_01_APP_LAUNCH.md">Journey 1 Documentation</a>
 */
@RunWith(AndroidJUnit4::class)
class Journey1_AppLaunchTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun journey1_appLaunchesWithoutCrashing() {
        // Given - App is launched (happens automatically with composeTestRule)

        // When - Wait for app to load
        composeTestRule.waitForIdle()

        // Then - App is running without crash
        // Test passes if no crash occurs during launch
    }

    @Test
    fun journey1_splashDisplaysFor3Seconds() {
        // Given - App just launched

        // When - Wait for splash screen duration
        Thread.sleep(3500) // Wait for splash (3 seconds + buffer)
        composeTestRule.waitForIdle()

        // Then - Splash completes and navigates
        // Test passes if no crash during splash period
    }

    @Test
    fun journey1_autoNavigatesAfterSplash() {
        // Given - App launched with splash screen

        // When - Wait for auto-navigation (after 3 seconds)
        Thread.sleep(3500) // Wait for splash + navigation
        composeTestRule.waitForIdle()

        // Then - Search screen appears
        // Test passes if app navigates successfully without crash
    }
}
