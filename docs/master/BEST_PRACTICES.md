# BEST PRACTICES & STANDARDS
## GitHub API Portfolio Suite - Engineering Guidelines

**Based On:** GithubCruiseAndroid

---

## Portfolio Projects

This best practices guide applies to:

| Platform | Repository | URL |
|----------|-----------|-----|
| **Android** | github-cruise-android | [github.com/dinkar1708/github-cruise-android](https://github.com/dinkar1708/github-cruise-android) |
| **iOS** | github-repo-search-ios | [github.com/dinkar1708/github-repo-search-ios](https://github.com/dinkar1708/github-repo-search-ios) |
| **Flutter** | flutter_riverpod_template | [github.com/dinkar1708/flutter_riverpod_template](https://github.com/dinkar1708/flutter_riverpod_template) |

All platforms must follow these engineering standards for consistency and quality.

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture Standards](#architecture-standards)
3. [Code Organization](#code-organization)
4. [Naming Conventions](#naming-conventions)
5. [Testing Standards](#testing-standards)
6. [UI/UX Standards](#uiux-standards)
7. [API Integration](#api-integration)
8. [Error Handling](#error-handling)
9. [Performance](#performance)
10. [Security](#security)
11. [Documentation](#documentation)
12. [CI/CD](#cicd)

---

## Overview

This document defines the **engineering standards and best practices** used across all three GitHub API portfolio projects (Android, iOS, Flutter). These standards ensure:

- Code Quality: Consistent, maintainable, production-ready code
- Testability: High test coverage with meaningful tests
- Performance: Fast, responsive, battery-efficient apps
- Security: Secure API communication and data handling
- Accessibility: WCAG AA compliant, inclusive design
- Scalability: Easy to extend with new features

---

## Architecture Standards

### 1. Architecture Pattern: MVVM + Clean Architecture

**All platforms must follow:**

```
UI Layer (View)
    ↓
ViewModel (Presentation Logic)
    ↓
UseCase (Business Logic)
    ↓
Repository (Data Abstraction)
    ↓
Data Source (API/Database)
```

**Key Principles:**
- Separation of Concerns: Each layer has one responsibility
- Dependency Rule: Dependencies point inward (UI → Domain → Data)
- Testability: Each layer independently testable
- Platform-Specific Implementation: Adapt to platform idioms

---

### 2. Layer Responsibilities

#### **UI Layer (View)**
**Responsibility:** Display data, handle user input

**Platform Implementations:**
- **Android:** Jetpack Compose with `@Composable` functions
- **iOS:** SwiftUI with `View` protocol
- **Flutter:** Widgets (StatelessWidget/StatefulWidget)

**Rules:**
- NO business logic in UI
- NO direct API calls
- NO data transformation
- Only UI state rendering
- Only user event forwarding

---

#### **ViewModel Layer**
**Responsibility:** Presentation logic, state management

**Platform Implementations:**
- **Android:** `ViewModel` + `StateFlow` + Hilt
- **iOS:** `@Observable` class with `@Published` properties
- **Flutter:** Riverpod `StateNotifier` or `ChangeNotifier`

**Rules:**
- Manage UI state
- Handle user actions
- Call use cases
- Transform domain models → UI models
- NO direct repository access
- NO Android/iOS/Flutter framework dependencies

---

#### **UseCase Layer**
**Responsibility:** Business logic

**Rules:**
- Single Responsibility (one use case = one action)
- Reusable across features
- Platform-agnostic
- Contains validation logic
- NO UI dependencies
- NO framework-specific code

**Naming Convention:**
- `SearchUsersUseCase`
- `GetUserProfileUseCase`
- `GetUserRepositoriesUseCase`

---

#### **Repository Layer**
**Responsibility:** Data abstraction, single source of truth

**Rules:**
- Interface-based (protocol/abstract class)
- Decide data source (API, cache, database)
- Map network models → domain models
- Handle errors
- NO business logic

**Pattern:**
```kotlin
// Android
interface UserRepository {
    suspend fun searchUsers(query: String, page: Int): Result<SearchUsersResponse>
}

class UserRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource
) : UserRepository {
    override suspend fun searchUsers(query: String, page: Int): Result<SearchUsersResponse> {
        return networkDataSource.searchUsers(query, page)
    }
}
```

---

#### **Data Source Layer**
**Responsibility:** API calls, database operations

**Platform Implementations:**
- **Android:** Retrofit + OkHttp + Moshi/Gson
- **iOS:** URLSession with async/await + Codable
- **Flutter:** Dio/http package + json_serializable/freezed

**Rules:**
- Handle HTTP requests
- Parse JSON responses
- Manage network errors
- NO business logic
- NO UI logic

---

### 3. Dependency Injection

**All platforms must use DI:**

| Platform | Recommended Framework | Alternative | Pattern |
|----------|----------------------|-------------|---------|
| Android | Hilt (Dagger) | Koin | Constructor injection |
| iOS | Manual DI | Resolver, Swinject | Protocol-based injection |
| Flutter | Riverpod | GetIt, Provider | Provider-based injection |

**Benefits:**
- Testability (easy mocking)
- Loose coupling
- Single responsibility
- Compile-time safety

---

### 4. State Management

**All platforms must implement:**

#### **State Pattern:**
```
State = Loading | Success(data) | Error(message) | Empty
```

**Platform Implementations:**

**Android (Sealed Interface):**
```kotlin
sealed interface UsersListState {
    object Loading : UsersListState
    data class Success(val users: List<User>) : UsersListState
    data class Error(val message: String) : UsersListState
    object Empty : UsersListState
}
```

**iOS (@Observable):**
```swift
@Observable
class UsersListViewModel {
    var users: [User] = []
    var isLoading = false
    var errorMessage: String?
    var isEmpty = false
}
```

**Flutter (Riverpod AsyncValue):**
```dart
final usersProvider = FutureProvider<List<User>>((ref) async {
    // Returns AsyncValue.loading, .data, or .error
});
```

**Rules:**
- Immutable state objects
- Single source of truth
- Unidirectional data flow
- All states handled in UI

---

## Code Organization

### Project Structure (All Platforms)

**Standard folder structure (adapted per platform):**

```
Android (Kotlin):
app/src/main/java/package/
├── ui/
│   ├── features/              # Feature screens
│   ├── shared/                # Shared components
│   └── theme/                 # Theme/styles
├── domain/
│   ├── model/
│   └── usecase/
├── data/
│   ├── repository/
│   ├── network/
│   └── model/
├── di/                        # Hilt modules
└── util/

iOS (Swift):
ProjectName/
├── UI/
│   ├── Features/              # Screens
│   ├── Shared/                # Shared views
│   └── Theme/                 # Colors/styles
├── Domain/
│   ├── Models/
│   └── UseCases/
├── Data/
│   ├── Repositories/
│   ├── Network/
│   └── Models/
└── Utilities/

Flutter (Dart):
lib/
├── ui/
│   ├── features/              # Screens
│   ├── shared/                # Shared widgets
│   └── theme/                 # Theme data
├── domain/
│   ├── models/
│   └── usecases/
├── data/
│   ├── repositories/
│   ├── network/
│   └── models/
└── utils/

tests/ (structure varies by platform)
Android:
├── test/                     # Unit tests
└── androidTest/              # UI/Integration tests

iOS:
├── ProjectTests/             # Unit tests
└── ProjectUITests/           # UI tests

Flutter:
└── test/                     # All tests (unit, widget, integration)

docs/                         # Documentation
├── technical/               # Architecture, API, features
├── product/                 # Requirements, roadmap
└── testing/                 # Test guides, journeys
```

---

## Naming Conventions

### 1. Files & Classes

| File Type | Android (PascalCase) | iOS (PascalCase) | Flutter (snake_case) |
|-----------|---------------------|------------------|---------------------|
| **Screens** | `UsersListScreen.kt` | `UsersListView.swift` | `users_list_screen.dart` |
| **ViewModels** | `UsersListViewModel.kt` | `UsersListViewModel.swift` | `users_list_view_model.dart` |
| **UseCases** | `SearchUsersUseCase.kt` | `SearchUsersUseCase.swift` | `search_users_use_case.dart` |
| **Repositories (Interface)** | `UserRepository.kt` | `UserRepository.swift` (protocol) | `user_repository.dart` (abstract) |
| **Repositories (Impl)** | `UserRepositoryImpl.kt` | `UserRepositoryImpl.swift` | `user_repository_impl.dart` |

**Naming Convention Rules:**
- **Android/iOS:** PascalCase for files (e.g., `UsersListScreen`)
- **Flutter:** snake_case for files (e.g., `users_list_screen.dart`)
- **All Platforms:** Descriptive names, no abbreviations

---

### 2. Variables & Functions

**Naming Rules:**
- Descriptive names (no abbreviations)
- camelCase for variables/functions
- PascalCase for classes
- UPPERCASE for constants

**Examples:**

**Good:**
```kotlin
val searchQuery: String
val userList: List<User>
fun searchUsers(query: String)
const val MAX_PAGE_SIZE = 100
```

**Bad:**
```kotlin
val sq: String              // Too short
val list: List<User>        // Not descriptive
fun search(q: String)       // Unclear
const val max = 100         // Not uppercase
```

---

### 3. Boolean Variables

**Prefix with `is`, `has`, `should`:**

```kotlin
val isLoading: Boolean
val hasError: Boolean
val shouldShowEmpty: Boolean
val isEmpty: Boolean
```

---

## Testing Standards

### 1. Test Coverage Requirements

| Layer | Target Coverage | Priority |
|-------|----------------|----------|
| Repository | 80-90% | High |
| UseCase | 70-80% | High |
| ViewModel | 60-70% | Medium |
| UI Components | 40-50% | Low |
| **Overall** | **70%+** | **Required** |

---

### 2. Test Pyramid

```
        E2E (10%)
      Integration (30%)
    Unit Tests (60%)
```

**Distribution:**
- **Unit Tests:** 60% - Fast, isolated, business logic (Repository, UseCase, ViewModel)
- **Integration Tests:** 30% - Multi-component flows (ViewModel + Repository together)
- **E2E Tests:** 10% - Full user journeys (screen navigation, real user interactions)

---

### 3. Test Naming Convention

**Pattern:** `test_<scenario>_<expected>`

```kotlin
// Good
fun test_searchUsers_withValidQuery_returnsSuccess()
fun test_searchUsers_withEmptyQuery_returnsEmpty()
fun test_searchUsers_withNetworkError_returnsError()
fun test_pagination_withValidPage_loadsNextPage()
fun test_pagination_whenAlreadyLoading_preventsDoubleLoad()

// Bad
fun testSearch()              // Not descriptive
fun test1()                   // Meaningless
fun searchTest()              // Wrong format
```

---

### 4. Test Structure (AAA Pattern)

**Arrange → Act → Assert:**

```kotlin
@Test
fun test_searchUsers_withValidQuery_returnsSuccess() = runTest {
    // Arrange (Given)
    val query = "dinkar1708"
    val mockResponse = createMockUsers()
    coEvery { repository.searchUsers(query, 1) } returns Result.Success(mockResponse)

    // Act (When)
    viewModel.searchUsers(query)
    advanceUntilIdle()

    // Assert (Then)
    val state = viewModel.state.value
    assertTrue(state is UsersListState.Success)
    assertEquals(1, state.users.size)
}
```

---

### 5. Test Requirements Per Feature

**Minimum tests per feature:**

**Unit Tests (7 minimum):**
1. Happy path (valid input → success)
2. Empty input handling
3. Invalid input handling
4. Error handling (network error)
5. Error handling (API error)
6. Pagination success
7. Pagination edge cases

**Integration Tests (2 minimum):**
1. Complete feature flow (end-to-end)
2. Error recovery flow

**E2E Tests (3 minimum):**
1. Complete user journey (e.g., search → view profile → view repos)
2. Error states display correctly
3. Navigation between screens works

**Platform-Specific Frameworks:**
- Android: Jetpack Compose Testing / Espresso
- iOS: XCUITest
- Flutter: Integration tests / Maestro

---

### 6. Mock vs Real Dependencies

**When to Mock:**
- External services (APIs)
- Databases
- Network calls
- File system

**When to Use Real:**
- Domain models
- Data transformations
- Pure functions
- Utilities

---

## UI/UX Standards

### 1. Design System

**All platforms must implement:**

#### **Material Design 3 (Android)**
- Color palette with semantic naming
- Typography scale (Display, Headline, Title, Body, Label)
- Spacing system (4dp grid)
- Elevation levels
- Shape tokens (corner radius)

#### **Human Interface Guidelines (iOS)**
- SF Symbols for icons
- System colors with semantic naming
- Dynamic Type support
- Standard spacing (8pt grid)
- Platform-appropriate components

#### **Material Design / Cupertino (Flutter)**
- Adaptive widgets (Material + Cupertino)
- Platform-aware theming
- Consistent spacing
- Responsive layouts

---

### 2. Theme Support (Required)

**Both themes must be implemented:**

**Light Mode:**
- Background: White/Light Gray
- Text: Dark Gray/Black
- Primary: Brand color
- High contrast ratios (WCAG AA)

**Dark Mode:**
- Background: Dark Gray/Black
- Text: White/Light Gray
- Primary: Adjusted brand color
- High contrast ratios (WCAG AA)

**Rules:**
- Automatic system theme detection
- All screens support both themes
- Smooth theme transitions
- Color contrast ratio ≥ 4.5:1 (text)
- Color contrast ratio ≥ 3:1 (large text)

---

### 3. Internationalization (i18n)

**Required languages:**
- English (default)
- Japanese (日本語)

**Implementation:**
- All UI text externalized (no hardcoded strings)
- Automatic locale detection
- Date/time formatting per locale
- Number formatting per locale
- RTL layout support (ready)

**String keys format:**
```
// Android (strings.xml)
<string name="screen_users_title">Search Users</string>
<string name="error_network">Network error. Please try again.</string>

// iOS (Localizable.strings)
"screen.users.title" = "Search Users";
"error.network" = "Network error. Please try again.";

// Flutter (intl)
screen_users_title: Search Users
error_network: Network error. Please try again.
```

---

### 4. State Representations

**All screens must handle:**

#### **Loading State:**
- Show spinner/progress indicator
- Disable user input (prevent multiple submissions)
- Clear previous errors

#### **Content State:**
- Display data in appropriate layout
- Enable user interactions
- Update immediately on data changes

#### **Error State:**
- Show user-friendly error message
- Provide retry button
- Log error for debugging
- Clear loading indicator

#### **Empty State:**
- Show appropriate empty message
- Provide guidance (e.g., "Search for users")
- Optionally show illustration

---

### 5. Accessibility (WCAG AA Compliance)

**Requirements:**

**Visual:**
- Color contrast ratio ≥ 4.5:1 (normal text)
- Color contrast ratio ≥ 3:1 (large text)
- Don't rely on color alone for information
- Support Dynamic Type / Font Scaling

**Touch Targets:**
- Minimum 48dp × 48dp (Android)
- Minimum 44pt × 44pt (iOS)
- Adequate spacing between targets

**Screen Readers:**
- Content descriptions for images
- Labels for interactive elements
- Semantic HTML/Accessibility labels
- Meaningful reading order

**Keyboard Navigation:**
- All features accessible via keyboard
- Visible focus indicators
- Logical tab order

---

## API Integration

### 1. HTTP Client Configuration

**All platforms must use:**

**Base URL:**
```
https://api.github.com
```

**Required Headers:**
```
X-GitHub-Api-Version: 2022-11-28
Accept: application/vnd.github+json
User-Agent: <AppName>/<Version>
```

**Timeouts:**
- Connect timeout: 10 seconds
- Read timeout: 30 seconds
- Write timeout: 30 seconds

**Interceptors:**
- Logging interceptor (debug builds only)
- Header injection
- Error handling
- NO sensitive data logging (tokens, passwords)

---

### 2. API Error Handling

**HTTP Status Codes:**

| Code | Meaning | Handling |
|------|---------|----------|
| 200 | Success | Parse response |
| 304 | Not Modified | Use cached data |
| 401 | Unauthorized | Show login prompt |
| 403 | Forbidden | Show "Access denied" |
| 404 | Not Found | Show "Not found" |
| 422 | Validation Failed | Show validation error |
| 429 | Rate Limit | Show "Try again later" |
| 500+ | Server Error | Show "Server error, try again" |

**Error Message Guidelines:**
- User-friendly messages (no technical jargon)
- Actionable (tell user what to do)
- Localized
- Log technical details separately

**Examples:**

**Good:**
```
"Unable to search users. Please check your connection and try again."
"Rate limit exceeded. Please try again in a few minutes."
"No users found matching your search."
```

**Bad:**
```
"HTTP 500 Internal Server Error"
"NullPointerException at line 42"
"API call failed"
```

---

### 3. Response Parsing

**Rules:**
- Use type-safe parsing (Moshi, Codable, json_serializable)
- Handle missing/null fields gracefully
- Validate data before use
- Log parsing errors
- NO manual JSON parsing (except for debugging)

---

### 4. Pagination

**Standard implementation:**

```kotlin
// Parameters
page: Int          // Current page (1-indexed)
per_page: Int      // Items per page (30 for users, 40 for repos)

// State tracking
currentPage: Int
isLoading: Boolean
hasMorePages: Boolean
```

**Rules:**
- Start at page 1
- Prevent duplicate loads (check `isLoading`)
- Handle last page (empty response or less than per_page)
- Reset pagination on new search
- Show loading indicator during page load

---

### 5. Request Debouncing (Search)

**Implementation:**
- Debounce delay: 300-500ms
- Cancel previous requests
- Only search on valid input (> 0 characters)

**Platform-Specific Implementation:**

| Platform | Debouncing Approach | Code Example |
|----------|--------------------|--------------|
| **Android** | Coroutines delay | `viewModelScope.launch { delay(300); searchUsers(query) }` |
| **iOS** | Task.sleep | `Task { try? await Task.sleep(for: .milliseconds(300)); await searchUsers(query) }` |
| **Flutter** | Timer | `Timer(Duration(milliseconds: 300), () { searchUsers(query); });` |

---

## Error Handling

### 1. Error Types

**Network Errors:**
- No internet connection
- Timeout
- DNS resolution failure

**API Errors:**
- HTTP error codes (4xx, 5xx)
- Invalid response format
- Rate limiting

**App Errors:**
- Null pointer / Optional unwrapping
- Type casting failures
- Invalid state transitions

---

### 2. Error Propagation

**Pattern:** `Result<T>` or similar wrapper

**Android:**
```kotlin
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val message: String) : Result<Nothing>
}
```

**iOS:**
```swift
enum Result<T> {
    case success(T)
    case failure(Error)
}
```

**Flutter (Riverpod):**
```dart
AsyncValue<T>  // .loading, .data(T), .error(Object, StackTrace)
```

---

### 3. Error Logging

**Rules:**
- Log all errors (even handled ones)
- Include context (user action, screen, timestamp)
- Log full stack trace
- NO PII (personally identifiable information)
- NO sensitive data (tokens, passwords)

**Platform-Specific Logging:**

| Platform | Tool | Usage |
|----------|------|-------|
| Android | Timber, Logcat | `Timber.d("message")` |
| iOS | OSLog, print | `os_log("message", log: .default)` |
| Flutter | logger package, debugPrint | `log.d("message")` or `debugPrint()` |

---

## Performance

### 1. App Startup

**Target:** < 2 seconds cold start

**Optimization:**
- Lazy initialization
- Minimize splash screen time (2-3 seconds max)
- Defer non-critical initialization
- Use background threads for heavy work

---

### 2. Network Performance

**Optimization:**
- Request caching (HTTP cache headers)
- Image caching (memory + disk)
- Response compression (gzip)
- Connection pooling
- Pagination (don't load all at once)

---

### 3. UI Performance

**Target:** 60 FPS (16.67ms per frame)

**Optimization:**
- Avoid expensive operations in UI thread
- Use lazy lists (LazyColumn, List, ListView)
- Image optimization (resize, compress)
- Minimize recomposition/re-renders
- Use keys for list items

---

### 4. Memory Management

**Rules:**
- Cancel network requests on screen exit
- Clear image cache when memory low
- Avoid memory leaks (weak references)
- Release resources in lifecycle methods

---

## Security

### 1. API Keys & Secrets

**Rules:**
- NO hardcoded API keys in code
- NO commit secrets to version control
- Use environment variables / build configs
- Use `.gitignore` for secret files
- Rotate keys regularly

---

### 2. HTTPS Only

**Rules:**
- All API calls via HTTPS
- Certificate pinning (production)
- NO HTTP fallback
- NO self-signed certificates (production)

---

### 3. Data Storage

**Rules:**
- NO sensitive data in logs
- NO sensitive data in plain text
- Encrypt sensitive data (if stored)
- Use platform secure storage (Keychain, KeyStore)

---

## Documentation

### 1. Code Documentation

**Required:**
- All public classes/functions documented
- Complex logic explained
- Edge cases noted
- TODO comments for known issues

**Format:**

**Kotlin (KDoc):**
```kotlin
/**
 * Searches for GitHub users matching the query.
 *
 * @param query Search term (username or keywords)
 * @param page Page number (1-indexed)
 * @return Result containing list of users or error
 */
suspend fun searchUsers(query: String, page: Int): Result<List<User>>
```

**Swift (DocC):**
```swift
/// Searches for GitHub users matching the query.
///
/// - Parameters:
///   - query: Search term (username or keywords)
///   - page: Page number (1-indexed)
/// - Returns: Result containing list of users or error
func searchUsers(query: String, page: Int) async -> Result<[User], Error>
```

---

### 2. README Standards

**Every project must have:**
- Project overview
- Features list
- Screenshots/demo
- Tech stack
- Setup instructions
- Testing guide
- Architecture overview
- Contributing guidelines

---

### 3. Architecture Documentation

**Required files:**
- `docs/technical/architecture.md` - System design
- `docs/technical/api.md` - API specifications
- `docs/technical/features.md` - Feature details
- `docs/testing/` - Test guides

---

## CI/CD

### 1. GitHub Actions

**Required workflows:**

**Pull Request Checks:**
- Build verification
- Lint checks
- Unit tests
- Code coverage report
- Code quality analysis

**Main Branch:**
- All PR checks
- UI tests (if possible)
- Build artifacts
- Release notes generation

---

### 2. Build Variants

**Required:**

**Debug:**
- App name: `Debug<AppName>`
- Package/Bundle ID: `com.example.app.debug`
- API URL: Development
- Logging: Enabled
- Can install alongside release

**Release:**
- App name: `<AppName>`
- Package/Bundle ID: `com.example.app`
- API URL: Production
- Logging: Disabled
- Optimized (ProGuard, etc.)

---

### 3. Version Control

**Branch Strategy:**
- `main` - Stable, production-ready
- `develop` - Integration branch
- `feature/<name>` - Feature branches
- `bugfix/<name>` - Bug fix branches

**Commit Messages:**
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `test`: Tests
- `refactor`: Code refactoring
- `style`: Formatting
- `chore`: Build, dependencies

**Example:**
```
feat(users): add user search with pagination

- Implement search users API integration
- Add pagination support (30 items per page)
- Add debounced search input (300ms)
- Add unit tests for search functionality

Closes #123
```

---

## Checklist: Feature Completion

Before marking a feature as "complete", ensure:

**Functionality:**
- [ ] Feature works as specified
- [ ] All edge cases handled
- [ ] Error handling implemented
- [ ] Loading states implemented
- [ ] Empty states implemented

**Testing:**
- [ ] Unit tests written (7+ tests)
- [ ] UI tests written (3+ tests)
- [ ] All tests passing
- [ ] Code coverage ≥ 70%

**UI/UX:**
- [ ] Works in light mode
- [ ] Works in dark mode
- [ ] All strings localized (EN/JP)
- [ ] Accessibility labels added
- [ ] Touch targets ≥ 48dp/44pt

**Performance:**
- [ ] No lag/jank in UI
- [ ] API calls optimized (caching, debouncing)
- [ ] Images optimized
- [ ] Memory leaks checked

**Documentation:**
- [ ] Code documented
- [ ] README updated
- [ ] API docs updated (if new endpoints)

**CI/CD:**
- [ ] Build passing
- [ ] Lint checks passing
- [ ] Tests passing in CI

---

## Summary

These best practices ensure:
- High quality code across all platforms
- Consistent architecture (MVVM + Clean)
- Comprehensive testing (70%+ coverage)
- Excellent UX (dark mode, i18n, accessibility)
- Production-ready (CI/CD, error handling, performance)
- Interview-ready (demonstrates professional engineering)

**All platforms (Android, iOS, Flutter) must follow these standards for feature parity and quality consistency.**

---

*End of Document*
