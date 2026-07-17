# UI Test Journeys

This folder contains UI tests organized by user journeys. Each journey represents a complete user flow through the app.

## Journey Structure

```
journeys/
├── README.md                           # This file
├── Journey1_AppLaunchTest.kt          # App launch and splash screen
├── Journey2_UserSearchTest.kt         # Search for GitHub users
├── Journey3_ViewUserProfileTest.kt    # View user profile details
├── Journey4_ViewRepositoriesTest.kt   # Browse user repositories
├── Journey5_FilterRepositoriesTest.kt # Filter forked repositories
├── Journey6_ViewRepositoryDetailsTest.kt # Open repository in WebView
├── Journey7_EmptySearchTest.kt        # Handle empty search
├── Journey8_ErrorHandlingTest.kt      # Handle errors and retry
├── Journey9_PullToRefreshTest.kt      # Pull to refresh functionality
└── Journey10_BackNavigationTest.kt    # Back button navigation flow
```

## Journey Naming Convention

**Format:** `Journey{Number}_{FlowName}Test.kt`

**Example:** `Journey2_UserSearchTest.kt`

## Journey Descriptions

### Journey 1: App Launch
**User Flow:** User opens app → Sees splash screen → Auto-navigates to search screen

**What to test:**
- App launches without crashing
- Splash screen displays for 3 seconds
- App auto-navigates after splash
- Search screen appears

---

### Journey 2: User Search
**User Flow:** User enters search query → Views search results

**What to test:**
- Search input field accepts text
- Search button is clickable
- Results display after search
- User list shows correct data
- Loading state during search
- Empty state when no results

---

### Journey 3: View User Profile
**User Flow:** User searches → Taps user from list → Views profile

**What to test:**
- User item is clickable
- Navigation to profile screen
- Profile displays avatar
- Profile displays username, name
- Followers and following count visible
- Loading state during profile fetch

---

### Journey 4: View Repositories
**User Flow:** User views profile → Sees repository list

**What to test:**
- Repository list displays
- Each repo shows name, language, stars
- Repository list is scrollable
- Loading state during repo fetch

---

### Journey 5: Filter Repositories
**User Flow:** User views repos → Toggles fork filter → Sees filtered results

**What to test:**
- Filter toggle exists
- Toggle changes state
- Forked repos hidden when toggled
- Forked repos shown when untoggled
- Filter works without API call

---

### Journey 6: View Repository Details
**User Flow:** User taps repository → Opens in WebView

**What to test:**
- Repository item is clickable
- WebView screen opens
- GitHub URL loads
- Loading indicator shows
- Back button works

---

### Journey 7: Empty Search
**User Flow:** User clicks search without entering text → Sees empty state

**What to test:**
- Empty search shows message
- Message text is correct
- No API call made
- User can enter text after

---

### Journey 8: Error Handling
**User Flow:** Search fails → Error message shown → User taps retry

**What to test:**
- Error message displays
- Retry button exists
- Retry button triggers new search
- Error state clears on retry

---

### Journey 9: Pull to Refresh
**User Flow:** User pulls down screen → Data refreshes

**What to test:**
- Pull gesture works
- Loading indicator shows
- Data refreshes
- UI updates with new data

---

### Journey 10: Back Navigation
**User Flow:** User navigates deep → Presses back → Returns to previous screen

**What to test:**
- Back from profile to search
- Back from repo details to profile
- Back stack works correctly
- State persists on back

---

## How to Run Journeys

### Run All Journeys
```bash
./gradlew connectedDebugAndroidTest
```

### Run Single Journey
```bash
./gradlew connectedDebugAndroidTest --tests Journey2_UserSearchTest
```

### Run Specific Test in Journey
```bash
./gradlew connectedDebugAndroidTest --tests Journey2_UserSearchTest.userCanSearchAndSeeResults
```

## Journey Test Template

```kotlin
package com.jetpack.compose.github.cruise.journeys

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jetpack.compose.github.cruise.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Journey{Number}_{FlowName}Test {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun journey{Number}_{testName}() {
        // Given - Setup initial state

        // When - Perform user actions

        // Then - Verify expected results
    }
}
```

## Best Practices

1. **One journey = One file**
   - Keep each user flow in its own file
   - Don't mix different journeys

2. **Descriptive test names**
   - Use clear, readable names
   - Format: `journey{Number}_{whatUserDoes}`
   - Example: `journey2_userCanSearchForGitHubUser`

3. **Complete flows**
   - Test the entire user journey
   - Include all steps from start to finish
   - Verify final state

4. **Independent tests**
   - Each test should run independently
   - Don't rely on other tests
   - Clean up after each test

5. **Use testTag**
   - Add testTag to UI components
   - Makes tests more stable
   - Easier to find elements

## Example Journey Test

```kotlin
@RunWith(AndroidJUnit4::class)
class Journey2_UserSearchTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun journey2_userCanSearchForGitHubUser() {
        // Given - User is on search screen
        composeTestRule.waitForIdle()

        // When - User searches for a user
        composeTestRule
            .onNodeWithTag("search_input")
            .performTextInput("dinkar")

        composeTestRule
            .onNodeWithTag("search_button")
            .performClick()

        // Then - User sees search results
        composeTestRule
            .onNodeWithTag("user_list")
            .assertExists()

        composeTestRule
            .onNodeWithText("dinkar")
            .assertIsDisplayed()
    }
}
```

---

**Last Updated:** July 2026
**Total Journeys:** 10
**Test Framework:** Compose Testing (Google's official recommendation)
