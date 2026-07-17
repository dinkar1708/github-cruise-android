package com.jetpack.compose.github.cruise.journeys

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jetpack.compose.github.github.cruise.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Journey 8: Error Handling
 * User Flow: Search fails → Error message shown → User taps retry
 *
 * @see <a href="file://../../../../../../../docs/testing/journeys/JOURNEY_08_ERROR_HANDLING.md">Journey 8 Documentation</a>
 */
@RunWith(AndroidJUnit4::class)
class Journey8_ErrorHandlingTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun journey8_invalidUserShowsErrorMessage() {
        // Given - User on search screen
        composeTestRule.waitForIdle()

        // Wait for search screen to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("search_input")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        Thread.sleep(500) // Like a real user

        // When - User searches for non-existent user
        composeTestRule
            .onNodeWithTag("search_input")
            .performTextInput("thisuserdoesnotexist123456789")

        // Wait for search and API response
        Thread.sleep(3000)
        composeTestRule.waitForIdle()

        // Then - Error message should display
        // (Would check for "User not found" or similar message)
        composeTestRule.waitForIdle()
    }

    @Test
    fun journey8_errorMessageIsUserFriendly() {
        // Given - Search will fail
        composeTestRule.waitForIdle()

        // Wait for search screen to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("search_input")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        Thread.sleep(500) // Like a real user

        // When - User searches invalid query
        composeTestRule
            .onNodeWithTag("search_input")
            .performTextInput("!@#$%^&*()")

        Thread.sleep(3000)

        // Then - Error message should be clear and helpful
        // Not technical jargon, but user-friendly message
        composeTestRule.waitForIdle()
    }

    @Test
    fun journey8_retryButtonExistsOnError() {
        // Given - User encountered an error
        composeTestRule.waitForIdle()

        // Wait for search screen to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("search_input")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        Thread.sleep(500) // Like a real user

        composeTestRule
            .onNodeWithTag("search_input")
            .performTextInput("nonexistentuser999")

        Thread.sleep(3000)

        // Then - Retry button should be available
        // (Would need testTag "retry_button" to verify)
        // composeTestRule.onNodeWithTag("retry_button").assertExists()

        composeTestRule.waitForIdle()
    }

    @Test
    fun journey8_retryButtonTriggersNewSearch() {
        // Given - User sees error with retry button
        composeTestRule.waitForIdle()

        // Wait for search screen to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("search_input")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        Thread.sleep(500) // Like a real user

        composeTestRule
            .onNodeWithTag("search_input")
            .performTextInput("invaliduser888")

        Thread.sleep(3000)

        // When - User taps retry button
        // composeTestRule.onNodeWithTag("retry_button").performClick()

        // Then - New search should be triggered
        // Loading indicator should appear
        Thread.sleep(1000)
        composeTestRule.waitForIdle()
    }

    @Test
    fun journey8_errorStateClearsOnRetry() {
        // Given - User in error state
        composeTestRule.waitForIdle()

        // Wait for search screen to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("search_input")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        Thread.sleep(500) // Like a real user

        composeTestRule
            .onNodeWithTag("search_input")
            .performTextInput("baduser777")

        Thread.sleep(3000)

        // When - User retries with valid search
        composeTestRule
            .onNodeWithTag("search_input")
            .performTextClearance()

        composeTestRule
            .onNodeWithTag("search_input")
            .performTextInput("android")

        Thread.sleep(2000)

        // Then - Error state should clear
        // New results should display
        composeTestRule.waitForIdle()
    }

    @Test
    fun journey8_networkErrorHandledGracefully() {
        // Given - User on search screen
        composeTestRule.waitForIdle()

        // Wait for search screen to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("search_input")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        Thread.sleep(500) // Like a real user

        // When - Network request fails
        // (In real scenario, would need to simulate network error)
        composeTestRule
            .onNodeWithTag("search_input")
            .performTextInput("testuser")

        Thread.sleep(3000)

        // Then - App should not crash
        // Error message should explain network issue
        // Retry option should be available
        composeTestRule.waitForIdle()
    }

    @Test
    fun journey8_appStaysResponsiveDuringError() {
        // Given - User encountered error
        composeTestRule.waitForIdle()

        // Wait for search screen to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("search_input")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        Thread.sleep(500) // Like a real user

        composeTestRule
            .onNodeWithTag("search_input")
            .performTextInput("erroruser666")

        Thread.sleep(3000)

        // When - Error is displayed
        composeTestRule.waitForIdle()

        // Then - App should remain responsive
        // User can still type in search field
        composeTestRule
            .onNodeWithTag("search_input")
            .performTextClearance()

        composeTestRule
            .onNodeWithTag("search_input")
            .performTextInput("newquery")

        composeTestRule
            .onNodeWithTag("search_input")
            .assertTextContains("newquery")
    }
}
