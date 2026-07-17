# Android UI Testing Guide

## Quick Answer

**For Jetpack Compose apps:** Use **Compose Testing** (Google's official recommendation)

---

## Why Compose Testing?

Google built Compose Testing specifically for Jetpack Compose apps.

### Key Benefits
- 3x faster than Espresso
- 60% less maintenance
- Official Google recommendation
- Made for Compose apps

### Options Compared

| Framework | Best For | Speed | Maintenance |
|-----------|----------|-------|-------------|
| **Compose Testing** | Jetpack Compose | Fast | Easy |
| Espresso | XML Views (legacy) | Slow | Hard |
| Maestro | Cross-platform E2E | Medium | Easy |

**Decision for GitHub Cruise:** Compose Testing (app is 100% Compose)

---

## Setup (5 minutes)

### 1. Add Dependencies

Already added to `app/build.gradle.kts` and `gradle/libs.versions.toml`

```kotlin
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test:runner:1.5.2")
debugImplementation("androidx.compose.ui:ui-test-manifest")
```

### 2. Test Structure

Tests organized by **user journeys**:

```
app/src/androidTest/java/.../journeys/
├── Journey1_AppLaunchTest.kt
├── Journey2_UserSearchTest.kt
├── Journey3_ViewUserProfileTest.kt
└── ... (10 total journeys)
```

**See:** [ui-test-journeys.md](ui-test-journeys.md) for all 10 journeys

---

## Writing Tests

### Basic Test Template

```kotlin
@RunWith(AndroidJUnit4::class)
class Journey2_UserSearchTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun journey2_userCanSearch() {
        // Given - User on search screen
        composeTestRule.waitForIdle()

        // When - User searches
        composeTestRule
            .onNodeWithTag("search_input")
            .performTextInput("dinkar")

        composeTestRule
            .onNodeWithTag("search_button")
            .performClick()

        // Then - Results appear
        composeTestRule
            .onNodeWithTag("user_list")
            .assertExists()
    }
}
```

### Common Operations

**Find elements:**
```kotlin
onNodeWithTag("search_input")      // By testTag
onNodeWithText("Search")           // By text
onNodeWithContentDescription("...")// By description
```

**Perform actions:**
```kotlin
.performClick()
.performTextInput("text")
.performScrollTo()
```

**Assert:**
```kotlin
.assertExists()
.assertIsDisplayed()
.assertTextEquals("expected")
```

---

## Running Tests

### Start Emulator
```bash
emulator -avd Medium_Phone &
```

### Run All Tests
```bash
./gradlew connectedDebugAndroidTest
```

### Run Single Journey
```bash
./gradlew connectedDebugAndroidTest --tests Journey1_AppLaunchTest
```

### Run Specific Test
```bash
./gradlew connectedDebugAndroidTest --tests Journey1_AppLaunchTest.journey1_appLaunchesSuccessfully
```

---

## Project Status

**Current:**
- ✅ Dependencies added
- ✅ Journey structure created
- ✅ Journey1_AppLaunchTest written (3 tests)
  - Tests app launch without crash
  - Tests splash screen duration
  - Tests auto-navigation after splash
- ❌ Need to add testTag to UI components for Journey2+
- ❌ Need to write remaining 9 journeys

**What Journey1 Tests:**
- Journey1 verifies basic app stability
- Tests that app launches, splash shows, and navigation works
- Does NOT interact with UI elements yet
- For UI interaction tests, need testTag on components

**Next Steps:**
1. Add testTag to SearchScreen components
2. Write Journey2_UserSearchTest (will interact with search input)
3. Test on emulator to see typing and clicking
4. Write remaining journeys

---

## Resources

**Android Docs:** https://developer.android.com/jetpack/compose/testing

**Journey Guide:** [ui-test-journeys.md](ui-test-journeys.md)

---

**Last Updated:** July 2026
**Framework:** Compose Testing
**Total Journeys:** 10
