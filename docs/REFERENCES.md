# References

Official Android documentation and resources referenced in this project.

---

## Official Android Developer Documentation

### Architecture & Design Patterns

#### Data Layer & Repository Pattern
- **URL:** https://developer.android.com/topic/architecture/data-layer
- **Topics:** Repository pattern, error handling strategies, data layer architecture
- **Key Concepts:**
  - Repository pattern implementation
  - Error handling with `Result<T>` wrapper pattern
  - Single source of truth principle
  - Offline-first architecture

#### Offline-First Apps
- **URL:** https://developer.android.com/topic/architecture/data-layer/offline-first
- **Topics:** Building resilient apps with local-first data
- **Key Concepts:**
  - Using `catch` operator on Flows to minimize errors
  - Local data source as source of truth
  - Network data synchronization strategies

### Kotlin Flow & Coroutines

#### Kotlin Flows on Android
- **URL:** https://developer.android.com/kotlin/flow
- **Topics:** Reactive streams, error handling with Flow
- **Key Concepts:**
  - Using `catch` operator for exception handling
  - Flow operators (map, filter, collect)
  - StateFlow and SharedFlow for UI state management
  - Emitting fallback values on errors

#### Kotlin Coroutines on Android
- **URL:** https://developer.android.com/kotlin/coroutines
- **Topics:** Asynchronous programming, structured concurrency
- **Key Concepts:**
  - CoroutineScope and lifecycle awareness
  - Dispatchers (Main, IO, Default)
  - Error handling with try-catch blocks
  - Structured concurrency principles

#### StateFlow and SharedFlow
- **URL:** https://developer.android.com/kotlin/flow/stateflow-and-sharedflow
- **Topics:** State management with hot flows
- **Key Concepts:**
  - StateFlow for UI state
  - SharedFlow for events
  - Hot vs cold flows

### Repository Pattern & Dependency Injection

#### Add Repository and Manual DI
- **URL:** https://developer.android.com/codelabs/basic-android-kotlin-compose-add-repository
- **Topics:** Repository pattern implementation codelab
- **Key Concepts:**
  - Separating data layer from UI layer
  - Dependency injection patterns
  - Clean architecture principles

---

## Error Handling Approaches

### Approach 1: Flow.catch() Operator (Recommended by Android)

**Source:** https://developer.android.com/kotlin/flow

**Implementation:**
```kotlin
// Repository
override suspend fun searchUsers(...): Flow<SearchUser> = flow {
    emit(networkDataSource.searchUser(...))
}.catch { e ->
    Timber.e(e, "Error searching users")
    throw e
}.flowOn(ioDispatcher)

// ViewModel
searchUseCase.searchUsers()
    .catch { e ->
        _uiState.value = UsersListState(error = e.message)
    }
    .collect { users ->
        _uiState.value = UsersListState(users = users)
    }
```

**Pros:**
- Official Android recommendation
- Simple and declarative
- Built into Kotlin Flow
- Less boilerplate

**Cons:**
- Less explicit error types
- Harder to handle multiple error scenarios

### Approach 2: Result<T> Wrapper Pattern

**Source:** https://developer.android.com/topic/architecture/data-layer

**Quote from docs:**
> "This pattern models errors and other signals that can happen as part of processing the result. In this pattern, the data layer returns a `Result<T>` type instead of `T`, making the UI aware of known errors that could occur in certain scenarios."

**Implementation:**
```kotlin
// Repository
override suspend fun searchUsers(...): Flow<Result<SearchUser>> = flow {
    try {
        val result = networkDataSource.searchUser(...)
        emit(Result.Success(result))
    } catch (e: Exception) {
        emit(Result.Error(e.message ?: "Unknown error"))
    }
}.flowOn(ioDispatcher)

// ViewModel
searchUseCase.searchUsers().collect { result ->
    when (result) {
        is Result.Success -> _uiState.value = UsersListState(users = result.data)
        is Result.Error -> _uiState.value = UsersListState(error = result.error)
    }
}
```

**Pros:**
- Explicit error modeling
- Type-safe error handling
- Better for complex error scenarios with error codes
- Errors as values (functional approach)

**Cons:**
- More boilerplate
- Extra wrapper layer
- Requires custom implementation

---

## This Project's Approach

**Current Implementation:** Flow.catch() operator (Approach 1)

**Rationale:**
- Follows official Android documentation recommendations
- Simpler implementation with less boilerplate
- Sufficient for this project's error handling needs
- Better for straightforward error scenarios

**Files:**
- Repository implementations: `app/src/main/java/com/jetpack/compose/github/github/cruise/data/repository/`
- Error handling: Using Flow's `catch` operator with exception throwing
- ViewModel error handling: Catching exceptions in Flow collectors

---

## Additional Resources

### Jetpack Compose

#### Compose Documentation
- **URL:** https://developer.android.com/jetpack/compose
- **Topics:** Modern UI toolkit, declarative UI

#### Compose BOM (Bill of Materials)
- **URL:** https://developer.android.com/jetpack/compose/bom
- **Topics:** Version alignment for Compose libraries
- **Current Version:** 2024.05.00

#### Compose Compiler
- **URL:** https://developer.android.com/jetpack/androidx/releases/compose-compiler
- **Topics:** Kotlin compiler plugin for Compose

#### Material 3 Design
- **URL:** https://m3.material.io
- **Topics:** Design system, components, theming
- **Developer Guide:** https://developer.android.com/jetpack/compose/designsystems/material3

### Navigation

#### Navigation Compose
- **URL:** https://developer.android.com/jetpack/compose/navigation
- **Topics:** Type-safe navigation, navigation graphs, deep linking
- **Current Version:** 2.7.7
- **Key Concepts:**
  - NavController and NavHost
  - Navigation arguments and type safety
  - Bottom navigation integration
  - Navigation testing

#### Safe Args (Type-safe Navigation)
- **URL:** https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args
- **Topics:** Type-safe argument passing between destinations

### Lifecycle & ViewModel

#### Lifecycle-Aware Components
- **URL:** https://developer.android.com/topic/libraries/architecture/lifecycle
- **Topics:** Lifecycle observers, LiveData
- **Current Version:** 2.7.0

#### ViewModel
- **URL:** https://developer.android.com/topic/libraries/architecture/viewmodel
- **Topics:** UI state holder, survive configuration changes
- **Key Concepts:**
  - ViewModel scope and lifecycle
  - SavedStateHandle for process death
  - ViewModelProvider and factories

#### Lifecycle Runtime Compose
- **URL:** https://developer.android.com/jetpack/compose/lifecycle
- **Topics:** collectAsStateWithLifecycle, Lifecycle effects
- **Key APIs:**
  - `collectAsStateWithLifecycle()` - Lifecycle-aware Flow collection
  - `DisposableEffect` - Cleanup when composable leaves composition
  - `LaunchedEffect` - Launch coroutines in composition

### Dependency Injection

#### Hilt (Dagger for Android)
- **URL:** https://developer.android.com/training/dependency-injection/hilt-android
- **Topics:** Compile-time dependency injection
- **Current Version:** 2.54
- **Key Concepts:**
  - Component scopes (Singleton, ViewModel, Activity)
  - Qualifier annotations
  - Module organization
  - Testing with Hilt

#### Hilt Navigation Compose
- **URL:** https://developer.android.com/jetpack/compose/libraries#hilt-navigation
- **Topics:** hiltViewModel() for Compose
- **Current Version:** 1.2.0

### Networking

#### Retrofit
- **URL:** https://square.github.io/retrofit/
- **Official Android Guide:** https://developer.android.com/training/data-storage/networking
- **Topics:** Type-safe HTTP client, coroutines support
- **Current Version:** 2.11.0
- **Key Concepts:**
  - Service interfaces with annotations
  - Call adapters for coroutines
  - Converters (Moshi, Gson)
  - Interceptors for logging/auth

#### OkHttp (Logging Interceptor)
- **URL:** https://square.github.io/okhttp/
- **Interceptors Guide:** https://square.github.io/okhttp/features/interceptors/
- **Topics:** HTTP client, interceptors, connection pooling
- **Current Version:** 4.12.0

#### Moshi
- **URL:** https://github.com/square/moshi
- **Topics:** JSON parser for Kotlin
- **Current Version:** 1.12.0
- **Key Features:**
  - Kotlin-first JSON parsing
  - Code generation with KSP
  - Custom adapters
  - Null-safe by default

### Data Storage

#### DataStore Preferences
- **URL:** https://developer.android.com/topic/libraries/architecture/datastore
- **Topics:** Type-safe key-value storage, Flow-based
- **Current Version:** 1.1.1
- **Key Concepts:**
  - Preferences DataStore (key-value)
  - Proto DataStore (typed objects)
  - Migration from SharedPreferences
  - Flow-based reactive API

**Why DataStore over SharedPreferences:**
- Type-safe with Kotlin coroutines and Flow
- Handles data migration elegantly
- Built on Kotlin coroutines (asynchronous)
- Protects against runtime exceptions

#### Room Database (Not Currently Used - Recommended)
- **URL:** https://developer.android.com/training/data-storage/room
- **Topics:** SQLite abstraction, compile-time SQL verification
- **Key Benefits:**
  - Compile-time SQL query verification
  - Less boilerplate than SQLite
  - Observable queries with Flow
  - Migration support

### Image Loading

#### Coil (Compose)
- **URL:** https://coil-kt.github.io/coil/
- **Compose Guide:** https://coil-kt.github.io/coil/compose/
- **Topics:** Image loading library for Compose
- **Current Version:** 2.6.0
- **Key Features:**
  - Kotlin-first, coroutines-based
  - Compose-native integration
  - Memory and disk caching
  - Transformations and placeholders

### Logging

#### Timber
- **URL:** https://github.com/JakeWharton/timber
- **Topics:** Extensible logging utility
- **Current Version:** 5.0.1
- **Best Practice:** Only plant trees in debug builds

### Testing

#### Testing Guide
- **URL:** https://developer.android.com/training/testing
- **Topics:** Unit testing, UI testing, integration testing

#### JUnit 4
- **URL:** https://junit.org/junit4/
- **Android Guide:** https://developer.android.com/training/testing/local-tests
- **Current Version:** 4.13.2

#### Kotlin Coroutines Test
- **URL:** https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/
- **Topics:** Testing coroutines and flows
- **Current Version:** 1.8.0
- **Key APIs:**
  - `runTest` - Test coroutines
  - `TestDispatcher` - Control coroutine execution
  - `advanceUntilIdle()` - Complete all pending coroutines

#### MockK
- **URL:** https://mockk.io/
- **Topics:** Mocking library for Kotlin
- **Current Version:** 1.13.4

#### Compose UI Testing
- **URL:** https://developer.android.com/jetpack/compose/testing
- **Topics:** Testing Compose UI, semantics, interactions
- **Key APIs:**
  - `composeTestRule` - Test rule for Compose
  - `onNodeWithText/Tag` - Find composables
  - `performClick/performTextInput` - Simulate interactions
  - `assertIsDisplayed/assertExists` - Verify UI state

#### Paparazzi (Snapshot Testing)
- **URL:** https://github.com/cashapp/paparazzi
- **Topics:** Screenshot testing for Compose
- **Current Version:** 1.3.4

### Build Tools

#### Gradle
- **URL:** https://developer.android.com/build
- **Version Catalogs:** https://developer.android.com/build/migrate-to-catalogs
- **Topics:** Build configuration, version catalogs

#### KSP (Kotlin Symbol Processing)
- **URL:** https://kotlinlang.org/docs/ksp-overview.html
- **Topics:** Annotation processing for Kotlin
- **Current Version:** 2.1.0-1.0.29
- **Used By:** Hilt, Moshi, Room

#### Jacoco (Code Coverage)
- **URL:** https://www.jacoco.org/jacoco/
- **Android Guide:** https://developer.android.com/studio/test/code-coverage
- **Topics:** Code coverage reporting

### Kotlin

#### Kotlin Language
- **URL:** https://kotlinlang.org/docs/home.html
- **Current Version:** 2.1.0

#### Kotlin Coroutines
- **URL:** https://kotlinlang.org/docs/coroutines-overview.html
- **Android Guide:** https://developer.android.com/kotlin/coroutines
- **Topics:** Structured concurrency, suspend functions
- **Current Version:** 1.8.0

### Android Platform

#### Android Developer Guides
- **URL:** https://developer.android.com/guide
- **Topics:** Platform features, APIs, best practices

#### Android SDK
- **Compile SDK:** 34 (Android 14)
- **Target SDK:** 34
- **Min SDK:** 21 (Android 5.0 Lollipop)

#### Activity Compose
- **URL:** https://developer.android.com/jetpack/compose/migrate/interoperability-apis/compose-in-existing-app
- **Topics:** Integrating Compose in Activities
- **Current Version:** 1.9.0

---

## Library Version Status

| Library | Current Version | Latest Available | Status |
|---------|----------------|------------------|---------|
| Kotlin | 2.1.0 | 2.1.0 | ✅ Up to date |
| Compose BOM | 2024.05.00 | 2024.12.01 | ⚠️ Update available |
| AGP | 8.7.3 | 8.8.x | ✅ Recent |
| Hilt | 2.54 | 2.54 | ✅ Up to date |
| Retrofit | 2.11.0 | 2.11.0 | ✅ Up to date |
| Navigation | 2.7.7 | 2.8.x | ⚠️ Update available |
| Lifecycle | 2.7.0 | 2.8.x | ⚠️ Known issue (see note) |
| Moshi | 1.12.0 | 1.15.1 | ⚠️ Update recommended |
| Coil | 2.6.0 | 2.7.0 | ⚠️ Update available |
| Timber | 5.0.1 | 5.0.1 | ✅ Up to date |
| DataStore | 1.1.1 | 1.1.1 | ✅ Up to date |

**Note on Lifecycle 2.7.0:**
As noted in `libs.versions.toml` line 8, version 2.8.0+ has a known issue with `LocalLifecycleOwner`. Staying on 2.7.0 is intentional until resolved.

---

## Official Sample Projects

### Google Samples
- **Now in Android:** https://github.com/android/nowinandroid
  - Modern Android development showcase
  - Architecture, testing, performance

- **Compose Samples:** https://github.com/android/compose-samples
  - Official Compose samples from Google

- **Architecture Samples:** https://github.com/android/architecture-samples
  - Clean Architecture, MVVM patterns

### Recommended Reading
- **Android Developers Blog:** https://android-developers.googleblog.com/
- **Medium - Android Developers:** https://medium.com/androiddevelopers
- **Kotlin Blog:** https://blog.jetbrains.com/kotlin/

---

## Related Project Documentation

- [Architecture](technical/ARCHITECTURE_BEST_PRACTICES.md) - This project's architecture details
- [API Call Patterns](technical/API_CALL_PATTERNS.md) - Network call implementation patterns
- [Best Practices](master/BEST_PRACTICES.md) - Engineering standards

---

**Last Updated:** 2026-07-30
**Maintained By:** Development Team
