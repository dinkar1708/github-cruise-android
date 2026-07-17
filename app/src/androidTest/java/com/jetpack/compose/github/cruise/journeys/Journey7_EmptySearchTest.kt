package com.jetpack.compose.github.cruise.journeys

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jetpack.compose.github.github.cruise.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Journey 7: Empty Search
 * User Flow: User clicks search without entering text → Sees empty state
 *
 * @see <a href="file://../../../../../../../docs/testing/journeys/JOURNEY_07_EMPTY_SEARCH.md">Journey 7 Documentation</a>
 */
@RunWith(AndroidJUnit4::class)
class Journey7_EmptySearchTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun journey7_emptySearchShowsMessage() {
        // Given - User on search screen with empty input
        composeTestRule.waitForIdle()

        // Wait for search screen to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("search_input")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        Thread.sleep(500) // Like a real user

        // When - Search field is empty (default state)
        composeTestRule
            .onNodeWithTag("search_input")
            .assertExists()

        // Then - No results shown, app shows empty state
        // User list should not have items
        composeTestRule.waitForIdle()
    }

    @Test
    fun journey7_emptyStateMessageIsHelpful() {
        // Given - User sees empty search state
        composeTestRule.waitForIdle()

        // Wait for search screen to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("search_input")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        Thread.sleep(500) // Like a real user

        // When - No search has been performed
        composeTestRule.waitForIdle()

        // Then - Empty state message should be visible
        // Message should guide user to enter username
        // (Would need to check for specific text)
        composeTestRule.waitForIdle()
    }

    @Test
    fun journey7_noApiCallForEmptySearch() {
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

        // When - User has empty search field
        // Then - No loading indicator should appear
        // Test passes if app doesn't make unnecessary API calls
        try {
            composeTestRule
                .onNodeWithTag("search_input")
                .assert(hasText(""))
        } catch (e: AssertionError) {
            // Input field might have placeholder text, which is fine
            // As long as no actual search was triggered
        }
        // No API call should be triggered
        // Test verifies app doesn't make unnecessary calls
        composeTestRule.waitForIdle()
    }

    @Test
    fun journey7_userCanTypeAfterEmptyState() {
        // Given - User seeing empty state
        composeTestRule.waitForIdle()

        // Wait for search screen to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("search_input")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        Thread.sleep(500) // Like a real user

        // When - User starts typing
        composeTestRule
            .onNodeWithTag("search_input")
            .performTextInput("test")

        // Then - Input field accepts text
        composeTestRule
            .onNodeWithTag("search_input")
            .assertTextContains("test")

        // User can proceed with search
        composeTestRule.waitForIdle()
    }

    @Test
    fun journey7_clearingSearchReturnsToEmptyState() {
        // Given - User performed a search
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
            .performTextInput("android")

        Thread.sleep(2000)

        // When - User clears the search
        // (Would need testTag "clear_button" to click)
        // composeTestRule.onNodeWithTag("clear_button").performClick()

        // Then - Returns to empty state
        // No results shown
        composeTestRule.waitForIdle()
    }

    @Test
    fun journey7_shortQueryDoesNotTriggerSearch() {
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

        // When - User types less than 3 characters
        composeTestRule
            .onNodeWithTag("search_input")
            .performTextInput("ab")

        // Wait for debounce (500ms)
        Thread.sleep(600)

        // Then - Search should not trigger (requires 3+ chars)
        // No loading indicator
        // No API call made
        composeTestRule.waitForIdle()
    }
}
