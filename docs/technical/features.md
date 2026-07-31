# GitHub Cruise - Features Documentation

This document provides a comprehensive overview of all features implemented in the GitHub Cruise Android application.

## Table of Contents
1. [Core Features](#core-features)
2. [User Interface Features](#user-interface-features)
3. [Technical Features](#technical-features)
4. [Accessibility Features](#accessibility-features)
5. [Developer Features](#developer-features)

---

## Core Features

### 1. Splash Screen
**Location:** `ui/features/splash/SplashScreen.kt`

A dynamic splash screen that provides a smooth entry point to the application.

**Features:**
- Gradient background animation (PrimaryPurple → PrimaryPink)
- Smooth scale animation for app branding
- 3-second display duration
- Auto-navigation to Users List screen
- Material Design 3 compliant typography

**User Experience:**
- Professional first impression
- Smooth transition animations
- Brand identity establishment

---

### 2. User Search & List

**Location:** `ui/features/users/UsersListScreen.kt`

Search and browse GitHub users with a powerful, paginated list view.

#### 2.1 Search Functionality
**Features:**
- Real-time search input field
- Fixed search bar at the top of the screen
- Search by GitHub username or keywords
- Debounced API calls to reduce network requests
- Search query persistence during screen rotation

**User Experience:**
- Pill-shaped search bar following Material Design 3
- Clear visual feedback during search
- Instant results display

#### 2.2 User List Display
**Features:**
- Fixed header showing results count ("Showing X of Y results")
  - Updates in real-time as user scrolls and loads more pages
  - Material Design 3 themed with secondaryContainer background
  - Internationalized (English & Japanese)
- Vertically scrollable list of GitHub users
- Each user item displays:
  - Avatar/profile image (circular)
  - GitHub username
  - User score/relevance score
- Pagination support for handling large result sets
- Pull-to-refresh functionality
- Empty state when no results found
- Loading state with progress indicator
- Error state with retry option

**Interaction:**
- Tap any user to navigate to their repository screen
- Smooth scroll performance
- Optimized list rendering with Compose

**Technical Implementation:**
- MVVM architecture
- StateFlow for state management
- Coroutines for async operations
- Pagination implemented with GitHub API

---

### 3. User Repository Screen

**Location:** `ui/features/userrepository/UserRepoScreen.kt`

Displays comprehensive user profile information and their GitHub repositories.

#### 3.1 User Profile Section
**Features:**
- Displays at the top of the screen:
  - User avatar (large, circular)
  - GitHub username
  - Full name
  - Number of followers
  - Number of following
- Professional card-style layout
- Responsive design for different screen sizes

#### 3.2 Repository List
**Features:**
- Lists all repositories for the selected user
- Each repository item shows:
  - Repository name
  - Programming language
  - Star count
  - Repository description
  - Fork indicator

**Filtering:**
- Toggle switch to filter repositories
- Option to show/hide forked repositories
- Real-time filtering without API calls (client-side)

**Interaction:**
- Tap any repository to open it in WebView
- Smooth scrolling
- Loading states for profile and repositories
- Error handling with retry option

**Technical Implementation:**
- Separate ViewModels for profile and repository list
- Efficient state management
- Cached data strategy

---

### 4. Repository Details (Native Screen)

**Location:** `ui/features/repodetails/EnhancedRepoDetailsScreen.kt`

Native screen displaying comprehensive repository information with Material Design 3.

**Features:**
- Repository header with owner avatar and full name
- Statistics grid (stars, forks, issues, watchers)
- Topics/tags display
- Repository information (language, license, default branch, homepage)
- Action buttons (open in browser, copy clone URL, share)
- Favorite button integration in app bar
- Loading states and error handling
- Native Compose UI components

**User Experience:**
- Fast, native performance
- Consistent Material Design 3 styling
- Offline capability with cached data
- Better accessibility
- No need to leave the app
- Quick access to repository information

---

## User Interface Features

### 1. Theme Support

**Location:** `ui/theme/Theme.kt`

Complete light and dark mode support following Material Design 3 guidelines.

#### Light Theme
- Background: White
- Primary: PrimaryPurple (#893788)
- Text: Dark Gray
- High contrast for readability

#### Dark Theme
- Background: Dark Gray (#1C1C1E)
- Primary: Light Gray
- Text: White
- Reduced eye strain in low-light conditions

**Implementation:**
- Automatic theme switching based on system settings
- All components support both themes
- Proper color contrast ratios (WCAG AA compliant)
- Dynamic color scheme updates

---

### 2. Localization Support

**Location:** `app/src/main/res/values/strings.xml` and `values-ja/strings.xml`

Multi-language support with user-selectable languages.

**Supported Languages:**
- English (default)
- Japanese (日本語)
- System Default (auto-detect)

**Features:**
- Complete UI text localization
- In-app language selector (Settings screen)
- Persistent language preference via DataStore
- Automatic language detection from device settings
- RTL layout support ready
- Localized error messages
- Localized date/time formats
- No hardcoded strings

**Implementation:**
- `LocaleManager` handles locale switching
- `LocaleDataStore` persists user preference
- Settings screen provides Material 3 language selection dialog
- App restart applies language changes

**Coverage:**
- All screen titles and labels
- Button and dialog text
- Error and validation messages
- Search hints
- Empty state messages

---

### 3. Material Design 3 System

**Location:** `ui/theme/` folder

Complete implementation of Google's Material Design 3 specification.

#### Design Tokens
- **Spacing:** Consistent 4dp grid system (extraSmall to extraLarge)
- **Elevation:** 6 elevation levels (0dp to 12dp)
- **Shape:** 7 corner radius tokens (none to full/pill)
- **Colors:** Comprehensive color palette with semantic naming
- **Typography:** Complete Material 3 type scale (Display, Headline, Title, Body, Label)
- **Dimensions:** Component-specific sizing tokens

**Benefits:**
- Consistent design across the app
- Easy theme customization
- Maintainable and scalable
- Professional appearance

---

### 4. Responsive Design

Multi-device support with adaptive layouts.

**Features:**
- Support for phones (small to large)
- Support for tablets
- Landscape and portrait orientation support
- Adaptive layouts based on screen size
- Touch target optimization (minimum 48dp)
- Proper spacing and padding for all screen sizes

**Screen Rotation:**
- State preservation during rotation
- Search query persistence
- Scroll position restoration
- No data loss on configuration changes

---

### 5. Reusable UI Components

**Location:** `ui/shared/` folder

Professional, reusable Compose components.

#### AppActionBarView
- Custom app bar with back navigation
- Title display
- Material Design 3 compliant
- Consistent across all screens

#### NetworkImageView
- Async image loading with Coil
- Circular avatar shape
- Placeholder support
- Error state handling
- Automatic caching

#### SharedProgressIndicator
- Centered loading spinner
- Material Design 3 styling
- Consistent size and color

#### SharedErrorView
- User-friendly error messages
- Retry button
- Icon support
- Localized error text

#### HorizontalLineView
- Material divider
- Consistent thickness (1dp)
- Theme-aware color

#### StateContentBox
- Smart state management wrapper
- Handles loading, error, and content states automatically
- Reduces boilerplate code

---

## Technical Features

### 1. Architecture

**Pattern:** MVVM (Model-View-ViewModel)

**Components:**
- **Model:** Data classes and repository layer
- **View:** Jetpack Compose UI
- **ViewModel:** State management and business logic

**Benefits:**
- Separation of concerns
- Testable code
- Easy to maintain and extend
- Clear data flow

**Layers:**
```
UI Layer (Compose)
    ↓
ViewModel (State Management)
    ↓
UseCase (Business Logic)
    ↓
Repository (Data Layer)
    ↓
Network Data Source (API)
```

---

### 2. Dependency Injection

**Framework:** Hilt (Dagger)

**Features:**
- Constructor injection
- Module-based organization
- ViewModel injection
- Repository injection
- Network client injection

**Modules:**
- `CoroutinesModule`: Coroutine dispatchers
- `NetworkDataSourceModule`: Retrofit and API setup
- `RepositoryModule`: Repository bindings

**Benefits:**
- Loose coupling
- Easy testing with mocks
- Reduced boilerplate
- Compile-time dependency verification

---

### 3. Networking

**Libraries:** Retrofit, Moshi, OkHttp

**Features:**
- RESTful API integration with GitHub API v3
- JSON parsing with Moshi
- HTTP logging interceptor (debug builds)
- Custom API version header injection
- Error response handling
- Network timeout configuration

**API Endpoints:**
- User search with pagination
- User profile details
- User repositories list

**Error Handling:**
- Network errors
- HTTP error codes (4xx, 5xx)
- API rate limiting handling
- User-friendly error messages

---

### 4. Offline-First Architecture (Room Database)

**Location:** `data/local/` folder

Complete offline-first architecture using Room Database for local data persistence.

**Database Structure:**
- **SQLite Database:** Local persistence with Room ORM
- **4 Entities:**
  - `UserEntity` - Cached user profiles
  - `RepositoryEntity` - Cached repositories
  - `SearchUserEntity` - Cached search results
  - `FavoriteEntity` - User-saved favorites
- **4 DAOs:** Type-safe database access with Flow-based reactive queries

**Caching Strategy:**
```
Network-First with Cache Fallback:
1. Check local cache → Emit cached data instantly
2. Fetch from network → Update cache
3. Emit fresh network data
4. If network fails → Use cached data (graceful degradation)
5. Only error if no cache AND network fails
```

**Cache Retention:**
- User profiles: 7 days
- User repositories: 7 days
- Search results: 24 hours
- Favorites: Permanent (until user deletes)

**Data Flow:**
```
UI ← ViewModel ← Repository ← [Cache + Network]
                                    ↓
                                Room Database
```

**Benefits:**
- Works completely offline
- Instant data display (cache-first)
- Reduced network usage
- Handles poor network conditions
- Smart cache invalidation
- Automatic cleanup of old data

**Technical Stack:**
- Room Database 2.6.1
- Flow for reactive data streams
- Coroutines for async operations
- Hilt DI for database injection
- ProGuard rules for release builds

**Official Guide:** https://developer.android.com/training/data-storage/room

---

### 5. Local Data Storage (DataStore)

**Location:** `data/datastore/` folder

Modern key-value storage using Jetpack DataStore for app preferences.

**Features:**
- **Theme Preferences:**
  - Dark/Light mode user selection
  - Persistent across app restarts
  - Reactive Flow-based updates (UI auto-updates)

**Data Stored:**
- User theme preference (dark/light)
- App settings
- Simple configuration values

**Implementation:**
- DataStore Preferences 1.1.1
- Type-safe preference keys
- Asynchronous, non-blocking operations
- Kotlin coroutines and Flow integration

**DataStore vs Room:**
| Storage | Use Case | Example |
|---------|----------|---------|
| DataStore | Simple key-value preferences | Theme, settings |
| Room | Structured relational data | Users, repos, favorites |

**Why Both:**
- DataStore: Lightweight preferences (theme, flags)
- Room: Complex data with relationships
- Right tool for the right job

**Official Guide:** https://developer.android.com/topic/libraries/architecture/datastore

---

### 6. State Management

**Pattern:** Unidirectional Data Flow (UDF)

**Implementation:**
- StateFlow for UI state
- Immutable state objects
- Single source of truth
- Predictable state updates

**State Classes:**
- `UsersListState`: Search results and pagination
- `UserRepoScreenProfileState`: User profile data
- `UserRepoScreenListState`: Repository list data

**Benefits:**
- Predictable state updates
- Easy debugging
- Configuration change handling
- Thread-safe state management

---

### 7. Coroutines & Flow

**Features:**
- Async operations with Kotlin Coroutines
- StateFlow for reactive state
- CoroutineScope management
- Dispatcher injection for testability

**Usage:**
- Network requests
- Data transformation
- UI state updates
- Background processing

---

### 8. Image Loading

**Library:** Coil

**Features:**
- Async image loading
- Automatic caching (memory + disk)
- Placeholder support
- Error image fallback
- Image transformation (circular crop)
- Jetpack Compose integration

---

### 9. Navigation

**Library:** Navigation Compose

**Features:**
- Type-safe navigation
- Screen arguments passing
- Back stack management
- Deep linking ready

**Navigation Graph:**
- Splash → Users List → User Repository → Repository Details

---

## Accessibility Features

### 1. Content Descriptions
- All images have proper content descriptions
- Icon buttons have semantic labels
- Screen reader support

### 2. Color Contrast
- WCAG AA compliant color ratios
- High contrast text on backgrounds
- Theme-aware color selection

### 3. Touch Targets
- Minimum 48dp touch targets
- Adequate spacing between interactive elements
- Large, easy-to-tap buttons

### 4. Text Scaling
- Support for user font size preferences
- Layouts adapt to larger text sizes
- No text truncation issues

### 5. Keyboard Navigation
- Supports external keyboard input
- Proper focus management
- Tab order optimization

---

## Developer Features

### 1. Build Variants

#### Debug Build
- App name: "DebugGithubCruise"
- Application ID: `com.jetpack.compose.github.github.cruise.debug`
- API URL: `https://api.github.com`
- Logging enabled
- HTTP request logging
- Can run alongside release build

#### Release Build
- App name: "GithubCruise"
- Application ID: `com.jetpack.compose.github.github.cruise`
- API URL: `https://release.api.github.com`
- Logging disabled
- ProGuard ready
- Optimized for production

---

### 2. Testing Support

**Test Framework:**
- JUnit 4 - Test framework
- MockK - Mocking library for Kotlin
- Coroutines Test - Async testing
- Compose UI Test - UI testing framework

**Test Coverage (107 tests):**
- Unit Tests: 55 tests
  - ViewModel logic
  - Repository layer
  - Use case layer
  - Data transformation
  - Offline cache behavior (19 tests)
- UI Tests: 48 tests
  - 10 user journey tests
  - Complete user flows
- Integration Tests: 4 tests
  - ViewModel + UseCase flows

**Test Categories:**
- Business logic (ViewModels, UseCases)
- Data layer (Repositories, DAOs)
- Offline cache (cache hits, cache misses, network failures)
- UI journeys (user search, navigation, error handling)

**Test Utilities:**
- JaCoCo code coverage reports
- Android Studio test runner
- Gradle test commands

---

### 3. Logging

**Library:** Timber

**Features:**
- Debug logging in debug builds
- Automatic log tagging
- No logging in release builds
- Crash reporting ready

---

### 4. CI/CD Support

**Platform:** GitHub Actions

**Features:**
- Automated builds on push
- Pull request checks
- Lint checks
- Test execution
- Build artifact generation

**Configuration:** `.github/workflows/build.yml`

---

### 5. Code Organization

**Package Structure:**
```
com.jetpack.compose.github.github.cruise/
├── di/                    # Dependency Injection
├── domain/                # Business logic & models
│   ├── model/            # Domain models
│   └── usecase/          # Use cases
├── data/                 # Data layer
│   ├── network/         # Network layer
│   │   ├── api/        # Retrofit interfaces
│   │   └── model/      # Network models
│   ├── repository/      # Data repositories
│   └── preferences/     # Data preferences
├── ui/                   # UI layer
│   ├── features/        # Feature screens
│   ├── shared/          # Reusable components
│   └── theme/           # Design system
```

**Benefits:**
- Easy to navigate
- Clear separation of concerns
- Feature-based organization
- Scalable structure

---

### 6. Jetpack Compose Preview

**Features:**
- Live preview of composables
- Multiple preview configurations
- Dark/light theme previews
- Different device previews
- Interactive preview mode

**Benefits:**
- Rapid UI development
- Instant feedback
- No need to run the app for UI changes
- Multiple configuration testing

---

## Performance Features

### 1. Pagination
- Efficient data loading
- Reduced memory footprint
- Smooth scrolling
- Load more on demand

### 2. Image Caching
- Memory cache
- Disk cache
- Reduced network calls
- Faster image loading

### 3. State Optimization
- Immutable state objects (@Immutable annotations)
- Efficient recomposition
- Remember and derived state
- Stable keys for lists
- Compose performance best practices

### 4. Network Optimization
- Offline-first caching (100% feature coverage)
- Smart retry logic with exponential backoff
- Debounced search input
- Connection pooling
- Gzip compression support
- Network-first with cache fallback

**Offline Support:**
- User search: 24-hour cache
- User profiles: 7-day cache
- User repositories: 7-day cache
- Repository search: 24-hour cache
- Favorites: Permanent storage

### 5. Database Performance
- Room database v2 with 5 entities
- Efficient queries with Flow for reactive updates
- Automatic cache invalidation based on TTL
- Cleanup strategies: 24 hours (search), 7 days (profiles), permanent (favorites)
- Network-first with transparent cache fallback
- Lazy loading with pagination support

### 6. Build Optimization

**ProGuard/R8 Configuration:**
- Code shrinking enabled for release builds
- Resource shrinking enabled
- Comprehensive ProGuard rules for all libraries
- Production-ready optimization

**Optimizations:**
- Dead code elimination
- Code obfuscation (security)
- Resource optimization
- Unused resource removal

**Libraries Configured:**
- Retrofit, OkHttp, Moshi (networking)
- Hilt/Dagger (dependency injection)
- Room Database (persistence)
- DataStore (preferences)
- Jetpack Compose (UI)
- Kotlin Coroutines (async)
- Coil (image loading)

**Release Build Results:**
- Optimized APK: 6.6MB
- Fast app startup time
- Reduced memory footprint
- Enhanced runtime performance
- Secure code (obfuscation)

---

## Security Features

### 1. API Security
- HTTPS only communication
- API version headers (X-GitHub-Api-Version: 2022-11-28)
- GitHub personal access token support with Android Keystore
- Secure token storage using EncryptedSharedPreferences (AES256-GCM)
- Token validation for GitHub formats (ghp_, gho_, ghu_, ghs_, ghr_)
- Rate limit optimization: 60/hour unauthenticated, 5,000/hour with token

**Implementation:**
- `SecureTokenManager` class handles encrypted token storage
- `ApiInterceptor` automatically injects Authorization headers
- Tokens stored in Android Keystore (hardware-backed when available)

### 2. Data Handling
- Encrypted token storage (Android Keystore + EncryptedSharedPreferences)
- Secure network communication (HTTPS only)
- No sensitive data in logs (production mode)
- ProGuard rules for security obfuscation

---

## Future Enhancements (Roadmap)

See [README.md](../README.md#todo) for the complete list of planned features including:
- Advanced search with debounce/throttling
- Repository caching and refresh strategy
- UI tests implementation
- Tablet optimization
- Pagination for user repositories
- Code coverage verification
- Enhanced navigation patterns

---

**Last Updated:** 2026-07-30
**Version:** 2.0.0
**Minimum Android SDK:** 21 (Lollipop)
**Target Android SDK:** 34 (Android 14)

---

## Testing & Quality Assurance

### Test Coverage Summary

**Total Tests: 107 tests**
- Unit Tests: 55 tests (business logic, repositories, offline cache)
- UI Tests: 48 tests (10 user journeys)
- Integration Tests: 4 tests (multi-layer flows)

**Test Categories:**

**1. Unit Tests (55 tests)**
- ViewModel Tests (search, pagination, error handling)
- Repository Tests (data fetching, transformations)
- UseCase Tests (business logic)
- Offline Cache Tests (19 tests):
  - Cache hit + network success
  - Cache miss + network success
  - Network failure with cache fallback
  - Network failure without cache
  - Pagination behavior
  - Query normalization

**2. UI Tests (48 tests)**
- Journey 1: App Launch (3 tests)
- Journey 2: User Search (4 tests)
- Journey 3: View User Profile (3 tests)
- Journey 4: View Repositories (4 tests)
- Journey 5: Filter Repositories (4 tests)
- Journey 6: View Repository Details (5 tests)
- Journey 7: Empty Search (6 tests)
- Journey 8: Error Handling (7 tests)
- Journey 9: Pull to Refresh (6 tests)
- Journey 10: Back Navigation (6 tests)

**3. Integration Tests (4 tests)**
- ViewModel + Repository + UseCase flows
- End-to-end feature testing

**Test Status:**
- ✅ All 107 tests passing
- ✅ 10% overall code coverage
- ✅ 70-100% business logic coverage
- ✅ Offline scenarios fully tested

**Testing Tools:**
- JUnit 4 (test framework)
- MockK (mocking)
- Coroutines Test (async testing)
- Compose UI Test (UI testing)
- JaCoCo (code coverage reports)

**Documentation:**
- `docs/technical/testing-types.md` - Complete testing guide
- `docs/testing/ui-testing-guide.md` - UI test setup
- `docs/testing/ui-test-journeys.md` - 10 user journeys
- `docs/technical/OFFLINE_CACHE_STATUS.md` - Offline implementation
