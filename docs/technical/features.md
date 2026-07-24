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

Multi-language support for international users.

**Supported Languages:**
- English (default)
- Japanese (日本語)

**Features:**
- Complete UI text localization
- Automatic language detection from device settings
- Manual language switching capability
- RTL layout support ready
- Localized error messages
- Localized date/time formats

**Coverage:**
- All screen titles
- Button labels
- Error messages
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

### 4. State Management

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

### 5. Coroutines & Flow

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

### 6. Image Loading

**Library:** Coil

**Features:**
- Async image loading
- Automatic caching (memory + disk)
- Placeholder support
- Error image fallback
- Image transformation (circular crop)
- Jetpack Compose integration

---

### 7. Navigation

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

**Unit Testing:**
- JUnit 4 framework
- MockK for mocking
- Coroutines test support
- ViewModel testing utilities

**Test Coverage:**
- ViewModel logic
- Repository layer
- Use case layer
- Data transformation

**Testing Tools:**
- Android Studio test runner
- Gradle test commands
- Code coverage reporting ready

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
- Immutable state objects
- Efficient recomposition
- Remember and derived state
- Stable keys for lists

### 4. Network Optimization
- Request caching
- Debounced search
- Connection pooling
- Gzip compression support

---

## Security Features

### 1. API Security
- HTTPS only
- API version headers
- Rate limiting awareness
- Personal access token support ready

### 2. Data Handling
- No sensitive data storage
- Secure network communication
- ProGuard ready for code obfuscation

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

**Last Updated:** 2024-06-10
**Version:** 1.0.0
**Minimum Android SDK:** 21 (Lollipop)
**Target Android SDK:** 34 (Android 14)
