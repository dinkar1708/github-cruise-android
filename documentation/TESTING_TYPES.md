# Android Testing Types - Complete Guide

A comprehensive guide to all officially supported test types in Android development and their implementation in this project.

## Overview

Android officially supports multiple test types organized into two main categories:
- **Local Tests** - Run on your development machine's JVM
- **Instrumented Tests** - Run on Android devices or emulators

**Official Documentation:** https://developer.android.com/training/testing

---

## Current Implementation Status

| Test Type | Status | Coverage | Location |
|-----------|--------|----------|----------|
| Unit Tests | Implemented (35 tests) | 10% overall, 70-100% business logic | `app/src/test/` |
| Integration Tests | Implemented (4 tests) | ViewModel + UseCase flows | `app/src/test/integration/` |
| Screenshot Tests | Configured (Paparazzi ready) | 0% (not written yet) | Ready to use |
| Instrumented Tests | Not Implemented | 0% | N/A |
| UI Tests (Compose) | Not Implemented | 0% | N/A |
| End-to-End Tests | Not Implemented | 0% | N/A |

---

## Test Types Explained

### 1. Unit Tests (Local Tests)

**Description:** Tests that run on your local JVM without needing an Android device. Fast and ideal for testing business logic.

**Status:** ✓ Implemented (30 tests)

**What we test:**
- ViewModels (pagination, error handling, state management)
- Repositories (data fetching, transformations)
- Use Cases (business logic)
- Domain Models (data classes)

**Tools Used:**
- JUnit 4 - Test framework
- MockK - Mocking library for Kotlin
- Coroutines Test - Testing async code
- JaCoCo - Code coverage

**Location:** `app/src/test/java/`

**Example:**
```kotlin
@Test
fun `test searchUsers() for valid user list on API success`() = runTest {
    // Given
    val user = User(id = 1, login = "dinkar1708")
    coEvery { mockSearchUseCase.searchUsers(...) } returns flowOf(user)

    // When
    viewModel.searchUsers("dinkar1708")
    advanceUntilIdle()

    // Then
    Assert.assertEquals(user, viewModel.uiState.value.userList.first())
}
```

**Run Command:**
```bash
./gradlew test
./gradlew testDebugUnitTest
```

**Official Docs:** https://developer.android.com/training/testing/local-tests

---

### 2. Instrumented Tests (Android Tests)

**Description:** Tests that run on Android devices or emulators. Required for testing Android framework dependencies.

**Status:** ✗ Not Implemented

**What can be tested:**
- Database operations (Room)
- Content Providers
- Services
- Broadcast Receivers
- Android framework APIs

**Tools Available:**
- AndroidJUnit4 - Android test runner
- Espresso - UI testing framework
- UI Automator - System UI testing

**Location:** Would be in `app/src/androidTest/java/` (directory doesn't exist)

**Example:**
```kotlin
@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    @Test
    fun writeAndReadUser() {
        val user = User(id = 1, login = "test")
        database.userDao().insert(user)
        val retrieved = database.userDao().getById(1)
        assertEquals(user, retrieved)
    }
}
```

**Run Command:**
```bash
./gradlew connectedAndroidTest
```

**Official Docs:** https://developer.android.com/training/testing/instrumented-tests

---

### 3. UI Tests (Compose UI Testing)

**Description:** Tests for Jetpack Compose UI components. Can verify UI elements, interactions, and navigation.

**Status:** ✗ Not Implemented

**What can be tested:**
- Compose screens and components
- User interactions (clicks, scrolls, input)
- Navigation flows
- UI state changes

**Tools Available:**
- Compose Test Framework
- Compose Test Rules
- Semantics matchers

**Dependencies Needed:**
```kotlin
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
debugImplementation("androidx.compose.ui:ui-test-manifest")
```

**Example:**
```kotlin
@Test
fun testUserListDisplayed() {
    composeTestRule.setContent {
        UsersListScreen()
    }

    composeTestRule.onNodeWithText("Search Users")
        .assertIsDisplayed()
}
```

**Run Command:**
```bash
./gradlew connectedAndroidTest
```

**Official Docs:** https://developer.android.com/jetpack/compose/testing

---

### 4. Integration Tests

**Description:** Tests that verify multiple components working together. Tests the interaction between layers.

**Status:** ✓ Implemented (4 tests in `app/src/test/integration/`)

**What can be tested:**
- ViewModel + Repository + UseCase integration
- API to Database flow
- End-to-end feature flows
- Error propagation across layers

**Can be implemented as:**
- Local tests (if mocking Android dependencies)
- Instrumented tests (if using real Android components)

**Example:**
```kotlin
@Test
fun `test complete user search flow`() = runTest {
    // Given - Real repository with fake API
    val repository = UserRepositoryImpl(fakeApi)
    val useCase = SearchUseCase(repository)
    val viewModel = UsersListViewModel(useCase)

    // When
    viewModel.searchUsers("dinkar")

    // Then
    assertTrue(viewModel.uiState.value.userList.isNotEmpty())
}
```

**Official Docs:** https://developer.android.com/training/testing/integration-testing

---

### 5. End-to-End Tests (E2E)

**Description:** Tests complete user journeys through the app. Tests the app as a whole.

**Status:** ✗ Not Implemented

**What can be tested:**
- Complete user flows (search → view profile → view repos)
- Cross-screen navigation
- Real network calls (optional)
- Data persistence

**Tools Available:**
- Espresso
- UI Automator
- Compose UI Testing
- Maestro (third-party)

**Example Flow:**
1. Launch app
2. Search for user
3. Tap on user
4. Verify profile displayed
5. Tap on repository
6. Verify repository details

**Run Command:**
```bash
./gradlew connectedAndroidTest
```

**Official Docs:** https://developer.android.com/training/testing/integration-testing/ui-testing

---

### 6. Screenshot Tests (Visual Regression)

**Description:** Automated tests that capture screenshots and compare against baselines to detect visual regressions.

**Status:** ✓ Configured (Paparazzi ready - just need to write tests)

**What can be tested:**
- UI appearance across different themes
- Component layouts
- Screen states (loading, error, success)
- Dark mode support
- Different screen sizes

**Tools Available:**
- **Paparazzi** - JVM-based screenshot testing (recommended)
- **Shot** - Facebook's screenshot testing
- **Android Screenshot Testing** - Google's official tool

**Recommended: Paparazzi**
```kotlin
dependencies {
    testImplementation("app.cash.paparazzi:paparazzi:1.3.1")
}
```

**Example:**
```kotlin
class ScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun testUsersListScreen() {
        paparazzi.snapshot {
            UsersListScreen()
        }
    }
}
```

**Run Command:**
```bash
./gradlew recordPaparazziDebug  # Create baseline
./gradlew verifyPaparazziDebug  # Verify against baseline
```

**Official Docs:**
- Paparazzi: https://github.com/cashapp/paparazzi
- Android Screenshots: https://developer.android.com/training/testing/ui-testing/screenshot-testing

---

### 7. Performance Tests

**Description:** Tests to measure app performance, startup time, frame drops, memory usage.

**Status:** ✗ Not Implemented

**What can be tested:**
- App startup time
- Screen rendering performance
- Memory leaks
- Battery consumption
- Network performance

**Tools Available:**
- Macrobenchmark - App startup and performance
- Microbenchmark - Code-level performance
- Profiler

**Example:**
```kotlin
@Test
fun startupCompilationNone() = macrobenchmark(
    StartupMode.COLD,
    iterations = 5
) {
    pressHome()
    startActivityAndWait()
}
```

**Official Docs:** https://developer.android.com/topic/performance/benchmarking

---

## Test Pyramid Strategy

```
        E2E Tests (5%)
           /\
          /  \
    Integration (15%)
       /      \
      /        \
 Unit Tests (80%)
```

**Recommended Distribution:**
- 80% Unit Tests - Fast, reliable, test business logic
- 15% Integration Tests - Test component interactions
- 5% E2E Tests - Test critical user journeys

**Current Project:**
- 100% Unit Tests
- 0% Integration Tests
- 0% E2E Tests

---

## Code Coverage Tools

### 1. JaCoCo (Currently Implemented)

**Status:** ✓ Implemented

**What it measures:**
- Line coverage
- Branch coverage
- Method coverage
- Class coverage

**Reports:**
- HTML reports (visual)
- XML reports (CI/CD)

**Run:**
```bash
./gradlew testDebugUnitTest jacocoTestReport
open app/build/reports/jacoco/jacocoTestReport/html/index.html
```

**Configuration:** `app/build.gradle.kts`

**Official Docs:** https://docs.gradle.org/current/userguide/jacoco_plugin.html

### 2. Android Studio Coverage

**Status:** Available (built-in)

**Run:**
1. Right-click test
2. "Run with Coverage"
3. View in Coverage tool window

**Official Docs:** https://developer.android.com/studio/test/test-in-android-studio#run-with-coverage

---

## Recommended Next Steps

### Phase 1: Improve Unit Test Coverage
- Add SettingsViewModel tests
- Add repository edge cases
- Target: 15-20% overall coverage

### Phase 2: Add Screenshot Tests
- Implement Paparazzi
- Create snapshots for key screens
- Test light/dark themes
- Estimated effort: 4-8 hours

### Phase 3: Add Integration Tests
- Test ViewModel + Repository + UseCase flows
- Test error propagation
- Estimated effort: 6-8 hours

### Phase 4: Add E2E Tests (Optional)
- Test critical user journeys
- Use Compose UI Testing
- Estimated effort: 8-12 hours

---

## Testing Best Practices

### Do's
- Write tests before or alongside code (TDD)
- Test behavior, not implementation
- Keep tests isolated and independent
- Use descriptive test names
- Follow Given-When-Then pattern
- Mock external dependencies
- Test edge cases and error paths

### Don'ts
- Don't test framework code (Android SDK, Compose)
- Don't test generated code (Hilt, Room)
- Don't test UI extensively (focus on business logic)
- Don't create flaky tests
- Don't duplicate coverage between test types

---

## Official Resources

### Android Testing Guides
- Testing Basics: https://developer.android.com/training/testing
- Test Your App: https://developer.android.com/studio/test
- Testing Fundamentals: https://developer.android.com/training/testing/fundamentals
- Best Practices: https://developer.android.com/training/testing/best-practices

### Framework-Specific
- Compose Testing: https://developer.android.com/jetpack/compose/testing
- Room Testing: https://developer.android.com/training/data-storage/room/testing-db
- ViewModel Testing: https://developer.android.com/codelabs/android-testing

### Tools Documentation
- JUnit: https://junit.org/junit4/
- MockK: https://mockk.io/
- Espresso: https://developer.android.com/training/testing/espresso
- UI Automator: https://developer.android.com/training/testing/other-components/ui-automator

### Advanced Topics
- Test Doubles: https://developer.android.com/training/testing/fundamentals/test-doubles
- Dependency Injection Testing: https://developer.android.com/training/dependency-injection/hilt-testing
- Coroutines Testing: https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/

---

## Summary

**Currently Implemented:**
- Unit Tests (35 tests, 10% overall coverage, 70-100% business logic)
- Integration Tests (4 tests, ViewModel + UseCase flows)
- Screenshot Tests (Paparazzi configured, ready to use)
- JaCoCo code coverage
- MockK for mocking
- Coroutines testing

**Not Implemented (Recommended):**
1. Screenshot Tests Implementation (High ROI, low effort) - Tool ready, just need to write tests
2. UI Tests (Low ROI for Compose, high effort)
3. E2E Tests (Low ROI, high effort)
4. Instrumented Tests (Only if needed for database/framework)

**Philosophy:**
Focus on high-value tests (unit + integration + screenshot) rather than comprehensive coverage. UI testing for Compose apps has low ROI due to brittleness and maintenance cost.

---

**Last Updated:** July 15, 2026
**Project Coverage:** 10% overall, 70-100% business logic
**Test Count:** 39 tests across 8 test files (35 unit + 4 integration)
