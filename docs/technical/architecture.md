# GitHub Cruise - Architecture Documentation

This document provides a comprehensive overview of the technical architecture, design patterns, and implementation details of the GitHub Cruise Android application.

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Layer Architecture](#layer-architecture)
3. [Design Patterns](#design-patterns)
4. [Dependency Injection](#dependency-injection)
5. [State Management](#state-management)
6. [Navigation](#navigation)
7. [Network Layer](#network-layer)
8. [Data Flow](#data-flow)
9. [Testing Strategy](#testing-strategy)
10. [Code Organization](#code-organization)

---

## Architecture Overview

GitHub Cruise follows **Clean Architecture** principles combined with **MVVM (Model-View-ViewModel)** pattern, ensuring separation of concerns, testability, and maintainability.

### Architectural Principles

1. **Separation of Concerns**: Each layer has a distinct responsibility
2. **Dependency Rule**: Dependencies point inward (UI → Domain → Data)
3. **Testability**: Each layer can be tested independently
4. **Scalability**: Easy to add new features without affecting existing code
5. **Maintainability**: Clear structure makes code easy to understand and modify

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                     UI Layer (Compose)                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Screens    │  │   ViewModels │  │  UI States   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                    Domain Layer                          │
│  ┌──────────────┐  ┌──────────────┐                     │
│  │  Use Cases   │  │    Models    │                     │
│  └──────────────┘  └──────────────┘                     │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                     Data Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Repositories │  │ Data Sources │  │  API Models  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                  External Services                       │
│                   (GitHub API v3)                        │
└─────────────────────────────────────────────────────────┘
```

---

## Layer Architecture

### 1. UI Layer (Presentation)

**Location:** `ui/` package

**Responsibility:** Display data to users and handle user interactions

#### Components:

##### Screens (Composables)
- `SplashScreen.kt`: Entry point with brand animation
- `UsersListScreen.kt`: User search and list display
- `UserRepoScreen.kt`: User profile and repositories (native screen)
- `RepositorySearchScreen.kt`: Repository search functionality
- `EnhancedRepoDetailsScreen.kt`: Native repository details with statistics and actions
- `FavoritesScreen.kt`: User and repository favorites list
- `SettingsScreen.kt`: App preferences and settings

##### ViewModels
```kotlin
class UsersListViewModel @Inject constructor(
    private val searchRepositoryUseCase: SearchRepositoryUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(UsersListState())
    val state: StateFlow<UsersListState> = _state.asStateFlow()

    // Business logic and state updates
}
```

**Characteristics:**
- Extends `ViewModel` from Android Architecture Components
- Exposes UI state via `StateFlow`
- Handles user actions
- Communicates with domain layer through use cases
- No direct dependency on data layer

##### UI States
```kotlin
data class UsersListState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true
)
```

**Characteristics:**
- Immutable data classes
- Single source of truth
- All UI state in one place
- Easy to test and debug

##### Shared Components
- `AppActionBarView`: Reusable app bar
- `NetworkImageView`: Image loading component
- `SharedProgressIndicator`: Loading indicator
- `SharedErrorView`: Error display
- `StateContentBox`: State wrapper component

---

### 2. Domain Layer

**Location:** `domain/` package

**Responsibility:** Business logic and application rules

#### Components:

##### Use Cases
```kotlin
class SearchRepositoryUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend operator fun invoke(
        query: String,
        page: Int,
        perPage: Int
    ): Result<List<User>> {
        return searchRepository.searchUsers(query, page, perPage)
    }
}
```

**Characteristics:**
- Single responsibility (one action per use case)
- Reusable across different ViewModels
- Contains business logic
- No Android dependencies
- Easy to test

##### Domain Models
```kotlin
data class User(
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val score: Double
)

data class UserProfile(
    val login: String,
    val name: String?,
    val avatarUrl: String,
    val followers: Int,
    val following: Int,
    val bio: String?
)

data class UserRepo(
    val id: Long,
    val name: String,
    val description: String?,
    val language: String?,
    val stargazersCount: Int,
    val fork: Boolean,
    val htmlUrl: String
)
```

**Characteristics:**
- Pure Kotlin data classes
- No Android or framework dependencies
- Business-focused models
- Immutable
- Easy to test

---

### 3. Data Layer

**Location:** `data/repository/` and `data/network/` packages

**Responsibility:** Data operations and external service communication

#### Components:

##### Repositories (Implementation)
```kotlin
class SearchRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val dispatcher: CoroutineDispatcher
) : SearchRepository {
    override suspend fun searchUsers(
        query: String,
        page: Int,
        perPage: Int
    ): Result<List<User>> = withContext(dispatcher) {
        try {
            val response = networkDataSource.searchUsers(query, page, perPage)
            Result.success(response.items.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Characteristics:**
- Interface-based design
- Hilt-injected dependencies
- Error handling
- Thread management with Dispatchers
- Data mapping (Network models → Domain models)

##### Network Data Source
```kotlin
interface NetworkDataSource {
    suspend fun searchUsers(query: String, page: Int, perPage: Int): SearchResponse
    suspend fun getUserProfile(username: String): UserProfileResponse
    suspend fun getUserRepos(username: String): List<UserRepoResponse>
}

class NetworkDataSourceImpl @Inject constructor(
    private val apiInterface: ApiInterface
) : NetworkDataSource {
    // Implementation using Retrofit
}
```

##### API Interface (Retrofit)
```kotlin
interface ApiInterface {
    @GET("search/users")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): SearchResponse

    @GET("users/{username}")
    suspend fun getUserProfile(
        @Path("username") username: String
    ): UserProfileResponse

    @GET("users/{username}/repos")
    suspend fun getUserRepos(
        @Path("username") username: String
    ): List<UserRepoResponse>
}
```

---

## Design Patterns

### 1. MVVM (Model-View-ViewModel)

**Implementation:**
- **Model**: Domain models and repositories
- **View**: Jetpack Compose UI
- **ViewModel**: State management and business logic coordination

**Benefits:**
- Clear separation between UI and business logic
- Testable ViewModels (no Android dependencies)
- Lifecycle-aware components
- Automatic configuration change handling

### 2. Repository Pattern

**Purpose**: Abstract data sources from the domain layer

**Implementation:**
```kotlin
interface SearchRepository {
    suspend fun searchUsers(query: String, page: Int, perPage: Int): Result<List<User>>
}
```

**Benefits:**
- Single source of truth
- Easy to swap data sources
- Centralized data logic
- Mockable for testing

### 3. Use Case Pattern

**Purpose**: Encapsulate business logic into single-responsibility classes

**Implementation:**
```kotlin
class UserRepositoryUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun getUserProfile(username: String): Result<UserProfile>
    suspend fun getUserRepos(username: String): Result<List<UserRepo>>
}
```

**Benefits:**
- Reusable business logic
- Single responsibility principle
- Easy to test
- Clear intent

### 4. Dependency Injection Pattern

**Framework**: Hilt (Dagger)

**Benefits:**
- Loose coupling
- Easy testing with mocks
- Automatic lifecycle management
- Compile-time dependency verification

### 5. Observer Pattern

**Implementation**: StateFlow/Flow

**Benefits:**
- Reactive UI updates
- Lifecycle-aware observation
- Automatic UI synchronization
- Thread-safe state updates

---

## Dependency Injection

### Hilt Configuration

#### Application Setup
```kotlin
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
```

#### Modules

##### 1. CoroutinesModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {
    @Provides
    @Singleton
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
}
```

##### 2. NetworkDataSourceModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkDataSourceModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiInterface(retrofit: Retrofit): ApiInterface {
        return retrofit.create(ApiInterface::class.java)
    }
}
```

##### 3. RepositoryModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindSearchRepository(
        repository: SearchRepositoryImpl
    ): SearchRepository

    @Singleton
    @Binds
    abstract fun bindUserRepository(
        repository: UserRepositoryImpl
    ): UserRepository
}
```

#### ViewModel Injection
```kotlin
@HiltViewModel
class UsersListViewModel @Inject constructor(
    private val searchRepositoryUseCase: SearchRepositoryUseCase
) : ViewModel()
```

#### Composable Integration
```kotlin
@Composable
fun UsersListScreen(
    viewModel: UsersListViewModel = hiltViewModel()
) {
    // UI implementation
}
```

---

## State Management

### Unidirectional Data Flow (UDF)

```
User Action
    ↓
ViewModel (processes action)
    ↓
Update State
    ↓
StateFlow emits new state
    ↓
UI recomposes
```

### State Implementation

```kotlin
class UsersListViewModel @Inject constructor(
    private val searchRepositoryUseCase: SearchRepositoryUseCase
) : ViewModel() {

    // Private mutable state
    private val _state = MutableStateFlow(UsersListState())

    // Public immutable state
    val state: StateFlow<UsersListState> = _state.asStateFlow()

    // User action
    fun searchUsers(query: String) {
        viewModelScope.launch {
            // Update loading state
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            // Execute use case
            val result = searchRepositoryUseCase(query, 1, 30)

            // Update state based on result
            _state.update {
                when {
                    result.isSuccess -> it.copy(
                        isLoading = false,
                        users = result.getOrNull() ?: emptyList()
                    )
                    else -> it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
}
```

### UI State Collection

```kotlin
@Composable
fun UsersListScreen(viewModel: UsersListViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // UI based on state
    StateContentBox(
        isLoading = state.isLoading,
        errorMessage = state.errorMessage
    ) {
        UsersList(users = state.users)
    }
}
```

---

## Navigation

### Navigation Setup

**Library**: Jetpack Navigation Compose

#### Navigation Graph
```kotlin
@Composable
fun GithubCruiseNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToUsersList = {
                    navController.navigate("users") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("users") {
            UsersListScreen(
                onUserClick = { username ->
                    navController.navigate("user/$username")
                }
            )
        }

        composable(
            route = "user/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username")
            UserRepoScreen(
                username = username ?: "",
                onBackClick = { navController.popBackStack() },
                onRepoClick = { repoUrl ->
                    navController.navigate("repo-details?url=$repoUrl")
                }
            )
        }

        composable(
            route = "user_repo_details/{html_url}",
            arguments = listOf(navArgument("html_url") { type = NavType.StringType })
        ) { backStackEntry ->
            val decodedUrl = CommonUtils.decodeUrl(
                backStackEntry.arguments?.getString("html_url") ?: ""
            )
            EnhancedRepoDetailsScreen(
                navController = navController,
                htmlUrl = decodedUrl
            )
        }
    }
}
```

### Navigation Flow
```
Splash Screen (3 seconds)
    ↓
Users List Screen
    ↓ (click user)
User Repository Screen
    ↓ (click repository)
Repository Details (WebView)
```

---

## Network Layer

### Configuration

#### Retrofit Setup
```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.API_BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()
```

#### OkHttp Client
```kotlin
private fun createOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(ApiInterceptor()) // Add API version header
        .addInterceptor(loggingInterceptor) // Log requests in debug
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
}
```

#### API Interceptor
```kotlin
class ApiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("X-GitHub-Api-Version", BuildConfig.API_VERSION)
            .build()
        return chain.proceed(request)
    }
}
```

### Error Handling

```kotlin
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: ApiException) : NetworkResult<Nothing>()
    data class Loading(val isLoading: Boolean) : NetworkResult<Nothing>()
}

data class ApiException(
    val code: Int,
    val errorMessage: String
) : Exception(errorMessage)
```

### API Endpoints

1. **Search Users**
   - Endpoint: `GET /search/users`
   - Parameters: query, page, per_page
   - Response: `SearchResponse`

2. **Get User Profile**
   - Endpoint: `GET /users/{username}`
   - Response: `UserProfileResponse`

3. **Get User Repositories**
   - Endpoint: `GET /users/{username}/repos`
   - Response: `List<UserRepoResponse>`

---

## Data Flow

### Complete Data Flow Example

#### 1. User Searches for "android"

**UI Layer:**
```kotlin
// User types in search box
SearchBar(
    query = state.searchQuery,
    onQueryChange = { query ->
        viewModel.onSearchQueryChange(query)
    }
)
```

**ViewModel:**
```kotlin
fun onSearchQueryChange(query: String) {
    _state.update { it.copy(searchQuery = query) }
    searchUsers(query)
}

private fun searchUsers(query: String) {
    viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }

        val result = searchRepositoryUseCase(
            query = query,
            page = 1,
            perPage = 30
        )

        _state.update {
            when {
                result.isSuccess -> it.copy(
                    isLoading = false,
                    users = result.getOrNull() ?: emptyList()
                )
                else -> it.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }
}
```

**Use Case:**
```kotlin
suspend operator fun invoke(
    query: String,
    page: Int,
    perPage: Int
): Result<List<User>> {
    return searchRepository.searchUsers(query, page, perPage)
}
```

**Repository:**
```kotlin
override suspend fun searchUsers(
    query: String,
    page: Int,
    perPage: Int
): Result<List<User>> = withContext(ioDispatcher) {
    try {
        val response = networkDataSource.searchUsers(query, page, perPage)
        val users = response.items.map { it.toDomainModel() }
        Result.success(users)
    } catch (e: Exception) {
        Result.failure(ApiException(e.message))
    }
}
```

**Network Data Source:**
```kotlin
override suspend fun searchUsers(
    query: String,
    page: Int,
    perPage: Int
): SearchResponse {
    return apiInterface.searchUsers(query, page, perPage)
}
```

**API Interface (Retrofit):**
```kotlin
@GET("search/users")
suspend fun searchUsers(
    @Query("q") query: String,
    @Query("page") page: Int,
    @Query("per_page") perPage: Int
): SearchResponse
```

#### 2. Response Flow Back

**Network → Repository:**
- Parse JSON to `SearchResponse`
- Map to domain models
- Wrap in `Result`

**Repository → Use Case:**
- Return `Result<List<User>>`

**Use Case → ViewModel:**
- Handle result
- Update state

**ViewModel → UI:**
- Emit new state via StateFlow
- UI recomposes with new data

---

## Testing Strategy

### 1. Unit Tests

#### ViewModel Tests
```kotlin
@Test
fun `search users should update state with results`() = runTest {
    // Given
    val mockUseCase = mockk<SearchRepositoryUseCase>()
    val users = listOf(User(1, "test", "url", 1.0))
    coEvery { mockUseCase(any(), any(), any()) } returns Result.success(users)

    val viewModel = UsersListViewModel(mockUseCase)

    // When
    viewModel.searchUsers("test")
    advanceUntilIdle()

    // Then
    assertEquals(users, viewModel.state.value.users)
    assertEquals(false, viewModel.state.value.isLoading)
}
```

#### Repository Tests
```kotlin
@Test
fun `searchUsers should return success result`() = runTest {
    // Given
    val mockDataSource = mockk<NetworkDataSource>()
    val response = SearchResponse(items = listOf(...))
    coEvery { mockDataSource.searchUsers(any(), any(), any()) } returns response

    val repository = SearchRepositoryImpl(mockDataSource, Dispatchers.Unconfined)

    // When
    val result = repository.searchUsers("test", 1, 30)

    // Then
    assertTrue(result.isSuccess)
}
```

### 2. UI Tests

```kotlin
@Test
fun usersList_displaysUsers() {
    composeTestRule.setContent {
        UsersListScreen()
    }

    composeTestRule.onNodeWithText("User1").assertExists()
}
```

---

## Code Organization

### Package Structure
```
com.jetpack.compose.github.github.cruise/
│
├── App.kt                              # Application class
├── MainActivity.kt                      # Single activity
│
├── di/                                  # Dependency Injection
│   ├── CoroutinesModule.kt
│   ├── NetworkDataSourceModule.kt
│   └── RepositoryModule.kt
│
├── domain/                              # Business Logic Layer
│   ├── model/                          # Domain Models
│   │   ├── SearchUser.kt
│   │   ├── User.kt
│   │   ├── UserProfile.kt
│   │   └── UserRepo.kt
│   └── usecase/                        # Use Cases
│       ├── SearchRepositoryUseCase.kt
│       └── UserRepositoryUseCase.kt
│
├── data/                                # Data Layer
│   ├── network/                        # Network Layer
│   │   ├── api/                       # API Configuration
│   │   │   ├── ApiConstants.kt
│   │   │   ├── ApiInterceptor.kt
│   │   │   └── ApiInterface.kt
│   │   ├── model/                     # Network Models
│   │   │   ├── ApiErrorResponse.kt
│   │   │   └── ApiException.kt
│   │   ├── NetworkDataSource.kt
│   │   └── NetworkDataSourceImpl.kt
│   ├── repository/                     # Data Repositories
│   │   ├── search/
│   │   │   ├── SearchRepository.kt
│   │   │   └── SearchRepositoryImpl.kt
│   │   └── user/
│   │       ├── UserRepository.kt
│   │       └── UserRepositoryImpl.kt
│   └── preferences/                    # Data Preferences
│
└── ui/                                  # Presentation Layer
    ├── features/                        # Feature Screens
    │   ├── splash/
    │   │   └── SplashScreen.kt
    │   ├── users/
    │   │   ├── UsersListScreen.kt
    │   │   ├── UsersListViewModel.kt
    │   │   ├── state/
    │   │   │   └── UsersListState.kt
    │   │   └── view/
    │   │       └── UsersListView.kt
    │   ├── userrepository/
    │   │   ├── UserRepoScreen.kt
    │   │   ├── UserRepoScreenViewModel.kt
    │   │   ├── state/
    │   │   └── view/
    │   ├── repodetails/
    │   │   ├── EnhancedRepoDetailsScreen.kt
    │   │   ├── RepoDetailsViewModel.kt
    │   │   └── RepoDetailsState.kt
    │   ├── repositorysearch/
    │   │   ├── RepositorySearchScreen.kt
    │   │   ├── RepositorySearchViewModel.kt
    │   │   └── view/
    │   ├── favorites/
    │   │   ├── FavoritesScreen.kt
    │   │   ├── FavoritesViewModel.kt
    │   │   └── view/
    │   └── settings/
    │       ├── SettingsScreen.kt
    │       └── SettingsViewModel.kt
    ├── shared/                          # Reusable Components
    │   ├── AppActionbarView.kt
    │   ├── HorizontalLineView.kt
    │   ├── NetworkImageView.kt
    │   ├── SharedCircularProgressIndicator.kt
    │   ├── SharedErrorView.kt
    │   ├── SharedWebView.kt
    │   ├── StateContentBox.kt
    │   ├── extension/
    │   └── utils/
    ├── theme/                           # Design System
    │   ├── Color.kt
    │   ├── Dimension.kt
    │   ├── Elevation.kt
    │   ├── Shape.kt
    │   ├── Spacing.kt
    │   ├── TextStyle.kt
    │   └── Theme.kt
    ├── GithubCruiseRootComposable.kt
    └── NavGraph.kt
```

---

## Technology Stack

### Core
- **Language**: Kotlin 1.9+
- **Minimum SDK**: 21 (Android 5.0 Lollipop)
- **Target SDK**: 34 (Android 14)
- **JDK**: Java 17

### UI Framework
- **Jetpack Compose**: Modern declarative UI
- **Material Design 3**: Latest design system
- **Navigation Compose**: Type-safe navigation

### Architecture Components
- **ViewModel**: Lifecycle-aware state holder
- **Lifecycle**: Lifecycle-aware components
- **StateFlow**: Reactive state management

### Dependency Injection
- **Hilt**: Dependency injection framework

### Networking
- **Retrofit 2**: HTTP client
- **Moshi**: JSON parsing
- **OkHttp**: HTTP client with interceptors

### Async
- **Kotlin Coroutines**: Asynchronous programming
- **Flow**: Reactive streams

### Image Loading
- **Coil**: Image loading library for Compose

### Logging
- **Timber**: Logging utility

### Testing
- **JUnit 4**: Unit testing framework
- **MockK**: Mocking library for Kotlin
- **Coroutines Test**: Testing coroutines

---

## Best Practices Implemented

### 1. Clean Code
- Meaningful naming conventions
- Single responsibility principle
- DRY (Don't Repeat Yourself)
- SOLID principles

### 2. Error Handling
- Proper exception handling
- User-friendly error messages
- Retry mechanisms
- Graceful degradation

### 3. Performance
- Efficient state management
- Proper use of remember and derivedStateOf
- Image caching
- Pagination for large datasets
- Debounced search

### 4. Security
- HTTPS only
- No hardcoded secrets
- ProGuard ready
- Secure data handling

### 5. Testability
- Interface-based design
- Dependency injection
- Pure functions where possible
- Mockable components

### 6. Code Quality
- Consistent code style
- Proper documentation
- Modular architecture
- Reusable components

---

**Last Updated:** 2024-06-10
**Architecture Version:** 1.0.0
**Maintainer:** GitHub Cruise Team
