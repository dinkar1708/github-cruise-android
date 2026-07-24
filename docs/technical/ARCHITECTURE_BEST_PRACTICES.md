# Architecture & Best Practices Documentation

## 📚 Table of Contents
1. [Coroutines & Threading](#coroutines--threading)
2. [MVVM Architecture](#mvvm-architecture)
3. [Memory Leak Prevention](#memory-leak-prevention)
4. [Scroll State Restoration](#scroll-state-restoration)
5. [Higher-Order Functions](#higher-order-functions)
6. [Clean Architecture](#clean-architecture)
7. [Official Documentation References](#official-documentation-references)

---

## 🧵 Coroutines & Threading

### Current Implementation (Following Android Official Docs)

```kotlin
┌─────────────────────────────────────────────────────┐
│ UI Layer (Compose)                                  │
│ Thread: Dispatchers.Main                            │
│ - collectAsState() on StateFlow                     │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│ ViewModel Layer                                      │
│ Thread: Dispatchers.Main (viewModelScope default)   │
│ - viewModelScope.launch { } ← NO dispatcher param   │
│ - Collects Flow on Main thread                      │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│ Repository Layer                                     │
│ Thread: Switches to Dispatchers.IO                  │
│ - Uses flowOn(Dispatchers.IO)                       │
│ - Network calls execute on IO thread                │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│ Network Layer (Retrofit)                             │
│ Thread: IO (via flowOn from Repository)             │
│ - Suspend functions called on IO thread             │
└─────────────────────────────────────────────────────┘
```

### 📝 Example Code

**Repository (Data Layer):**
```kotlin
class SearchRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SearchRepository {
    override suspend fun searchUsers(...): Flow<SearchUser> = flow {
        // Network call executed on IO thread via flowOn below
        val users = networkDataSource.searchUser(...)
        emit(users)
    }.catch { e ->
        Timber.e(e, "Error search Users")
        throw e
    }.flowOn(ioDispatcher) // ← Execute on IO dispatcher
}
```

**ViewModel (UI Layer):**
```kotlin
@HiltViewModel
class UsersListViewModel @Inject constructor(
    private val searchRepositoryUseCase: SearchRepositoryUseCase
) : BaseViewModel() {

    private fun loadUsers() = viewModelScope.launch {
        // ← NO dispatcher param, runs on Main by default
        searchRepositoryUseCase.searchUsers(...)
            .collect { searchUser ->
                _uiState.update { /* UI update on Main thread */ }
            }
    }
}
```

### 🔑 Key Points
- **ViewModels**: No dispatcher parameter, `viewModelScope` runs on `Dispatchers.Main`
- **Repositories**: Use `flowOn(Dispatchers.IO)` for network/database operations
- **NO** manual thread switching in ViewModels (unlike iOS pattern)
- Flow collection happens on Main thread (safe for UI updates)

**Official Reference:**
- [Kotlin Coroutines Best Practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)
- [Coroutines on Android](https://developer.android.com/kotlin/coroutines)

---

## 🏗️ MVVM Architecture

### Layer Responsibilities

```
┌───────────────────────────────────────────────────┐
│ UI Layer (Jetpack Compose)                       │
│ - Composable functions                            │
│ - collectAsState() on StateFlow/Flow             │
│ - NO business logic                               │
│ Files: *Screen.kt, *View.kt                       │
└────────────────────┬──────────────────────────────┘
                     │
┌────────────────────▼──────────────────────────────┐
│ ViewModel Layer                                   │
│ - Manages UI state (StateFlow)                    │
│ - Handles user actions                            │
│ - NO direct network/database calls                │
│ - Uses viewModelScope (auto-cancels on clear)    │
│ Files: *ViewModel.kt                              │
└────────────────────┬──────────────────────────────┘
                     │
┌────────────────────▼──────────────────────────────┐
│ Domain Layer (Use Cases)                          │
│ - Business logic                                  │
│ - Data transformation                             │
│ - Combines multiple repositories                  │
│ - NO threading logic                              │
│ Files: *UseCase.kt                                │
└────────────────────┬──────────────────────────────┘
                     │
┌────────────────────▼──────────────────────────────┐
│ Repository Layer                                  │
│ - Single source of truth                          │
│ - Abstracts data sources (network/local)          │
│ - Handles threading (flowOn)                      │
│ Files: *Repository.kt, *RepositoryImpl.kt         │
└────────────────────┬──────────────────────────────┘
                     │
┌────────────────────▼──────────────────────────────┐
│ Data Layer                                        │
│ - Network calls (Retrofit)                        │
│ - Database operations (Room)                      │
│ - SharedPreferences                               │
│ Files: NetworkDataSource, APIInterface            │
└───────────────────────────────────────────────────┘
```

**Official Reference:**
- [Guide to App Architecture](https://developer.android.com/topic/architecture)
- [ViewModel Overview](https://developer.android.com/topic/libraries/architecture/viewmodel)

---

## 🚫 Memory Leak Prevention

### Current Implementation

#### 1. **viewModelScope Auto-Cancellation**
```kotlin
@HiltViewModel
class UsersListViewModel @Inject constructor(...) : ViewModel() {

    private fun loadUsers() = viewModelScope.launch {
        // Automatically cancelled when ViewModel.onCleared() is called
        searchRepositoryUseCase.searchUsers(...)
            .collect { /* ... */ }
    }
}
```
- `viewModelScope` automatically cancels all coroutines when ViewModel is cleared
- NO manual job management needed
- NO memory leaks

#### 2. **Compose collectAsState()**
```kotlin
@Composable
fun UsersListScreen(viewModel: UsersListViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    // Automatically cancels collection when Composable leaves composition
    // Lifecycle-aware, no manual cleanup needed
}
```
- `collectAsState()` is lifecycle-aware
- Automatically stops collection when Composable is removed
- NO manual collection management

#### 3. **StateFlow vs LiveData**
```kotlin
// StateFlow (Current - Better)
private val _uiState = MutableStateFlow(UsersListState())
val uiState: StateFlow<UsersListState> = _uiState.asStateFlow()

// Benefits:
// Always has a value (no null checks)
// Compose-friendly with collectAsState()
// Coroutine-based (modern)
// No lifecycle issues in Compose
```

#### 4. **@Immutable State Classes**
```kotlin
@Immutable
data class UsersListState(
    val userList: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)
```
- Optimizes Compose recomposition
- Prevents unnecessary UI updates
- Better performance

**Official Reference:**
- [StateFlow and SharedFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- [Jetpack Compose State](https://developer.android.com/jetpack/compose/state)
- [Lifecycle-aware components](https://developer.android.com/topic/libraries/architecture/lifecycle)

---

## 📜 Scroll State Restoration

### Current Implementation

#### 1. **LazyColumn (Lazy Lists)**
```kotlin
@Composable
fun UsersListView(
    users: List<User>,
    lastVisibleItemIndex: Int, // From ViewModel state
    onUpdateVisibleIndex: (Int) -> Unit
) {
    // Restore scroll position from ViewModel (survives rotation)
    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = lastVisibleItemIndex.coerceAtLeast(0)
    )

    var scrolledToEnd by rememberSaveable { mutableStateOf(false) }

    LazyColumn(state = scrollState) {
        itemsIndexed(users) { index, user ->
            key(user.id) { // ← Important for item stability
                UserCard(user)
            }
        }
    }

    // Save scroll position to ViewModel state
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex }
            .collect { index ->
                onUpdateVisibleIndex(index)
            }
    }
}
```

**Key Points:**
- Scroll position stored in ViewModel `StateFlow` (survives rotation)
- `rememberLazyListState()` restores position from ViewModel state
- `key()` ensures item stability during recomposition
- `rememberSaveable` for local state that survives process death

#### 2. **Regular Column (Vertical Scroll)**
```kotlin
@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
        // rememberScrollState() automatically survives configuration changes
    ) {
        // Content
    }
}
```

**ViewModel State Management:**
```kotlin
@Immutable
data class UsersListState(
    val userList: List<User> = emptyList(),
    val lastVisibleItemIndex: Int = 0, // ← Scroll position
    val isLoading: Boolean = false
)

class UsersListViewModel : ViewModel() {
    fun updateLastVisibleIndex(index: Int) {
        _uiState.update { it.copy(lastVisibleItemIndex = index) }
    }
}
```

**Official Reference:**
- [Jetpack Compose Lists](https://developer.android.com/jetpack/compose/lists)
- [Save UI state](https://developer.android.com/topic/libraries/architecture/saving-states)
- [rememberSaveable](https://developer.android.com/jetpack/compose/state#restore-ui-state)

---

## 🔧 Higher-Order Functions

### Current Implementation

Higher-order functions are **extensively used** following Kotlin best practices:

#### 1. **Flow Builders**
```kotlin
override suspend fun searchUsers(...): Flow<SearchUser> = flow {
    // flow { } is a higher-order function
    val users = networkDataSource.searchUser(...)
    emit(users)
}
```

#### 2. **Flow Operators**
```kotlin
flow { ... }
    .catch { e -> /* catch is a higher-order function */ }
    .flowOn(ioDispatcher)
    .collect { result -> /* collect is a higher-order function */ }
```

#### 3. **Compose Composables**
```kotlin
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit // ← Higher-order function
) {
    Card(modifier = modifier) {
        content() // Execute lambda
    }
}

// Usage:
AppCard {
    Text("Hello") // Lambda passed as parameter
}
```

#### 4. **State Management**
```kotlin
_uiState.update { currentState ->
    // update { } is a higher-order function
    currentState.copy(isLoading = true)
}
```

#### 5. **LaunchedEffect**
```kotlin
LaunchedEffect(key) {
    // LaunchedEffect { } is a higher-order function
    // Executes lambda in coroutine scope
    doSomething()
}
```

**Benefits:**
- Declarative, readable code
- Reduces boilerplate
- Type-safe
- Standard Kotlin/Jetpack Compose pattern

**Official Reference:**
- [Kotlin Higher-Order Functions](https://kotlinlang.org/docs/lambdas.html)
- [Kotlin Flow](https://kotlinlang.org/docs/flow.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose/tutorial)

---

## 🏛️ Clean Architecture

### Dependency Rule

```
┌──────────────────────────────────────────────┐
│ Domain Layer (domain/)                       │
│ - Interfaces: Repository interfaces          │
│ - Models: Data classes                       │
│ - Use Cases: Business logic                  │
│                                              │
│ NO DEPENDENCIES on other layers             │
└──────────────────────────────────────────────┘
         ▲                           ▲
         │                           │
         │ depends on                │ depends on
         │                           │
┌────────┴─────────────┐   ┌────────┴──────────┐
│ UI Layer (ui/)       │   │ Data Layer        │
│ - ViewModels         │   │ (data/)           │
│ - Composables        │   │ - Repositories    │
│ - Screens            │   │ - Network         │
└──────────────────────┘   │ - Database        │
                           └───────────────────┘
```

### Example: Repository Interface

**Domain Layer (domain/repository/SearchRepository.kt):**
```kotlin
package com.jetpack.compose.github.github.cruise.domain.repository

interface SearchRepository {
    suspend fun searchUsers(...): Flow<SearchUser>
}
```

**Data Layer (data/repository/SearchRepositoryImpl.kt):**
```kotlin
package com.jetpack.compose.github.github.cruise.data.repository

class SearchRepositoryImpl @Inject constructor(...) : SearchRepository {
    override suspend fun searchUsers(...): Flow<SearchUser> = flow {
        // Implementation
    }
}
```

**Dependency Injection (di/RepositoryModule.kt):**
```kotlin
@Binds
abstract fun bindSearchRepository(
    repository: SearchRepositoryImpl
): SearchRepository // ← Binds implementation to interface
```

**Benefits:**
- Domain layer independent of framework/libraries
- Easy to test (mock interfaces)
- Easy to swap implementations
- Follows Dependency Inversion Principle (SOLID)

**Official Reference:**
- [Guide to App Architecture - Data Layer](https://developer.android.com/topic/architecture/data-layer)
- [Clean Architecture (Uncle Bob)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

---

## Official Documentation References

### Android Official Documentation

1. **Architecture**
   - [Guide to app architecture](https://developer.android.com/topic/architecture)
   - [App architecture: Data layer](https://developer.android.com/topic/architecture/data-layer)
   - [App architecture: UI layer](https://developer.android.com/topic/architecture/ui-layer)

2. **Coroutines & Threading**
   - [Coroutines on Android](https://developer.android.com/kotlin/coroutines)
   - [Coroutines best practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)
   - [Background work with coroutines](https://developer.android.com/kotlin/coroutines/coroutines-adv)

3. **Jetpack Compose**
   - [Jetpack Compose tutorial](https://developer.android.com/jetpack/compose/tutorial)
   - [State and Jetpack Compose](https://developer.android.com/jetpack/compose/state)
   - [Side-effects in Compose](https://developer.android.com/jetpack/compose/side-effects)
   - [Lists and grids](https://developer.android.com/jetpack/compose/lists)

4. **ViewModel & Lifecycle**
   - [ViewModel overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
   - [StateFlow and SharedFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
   - [Lifecycle-aware components](https://developer.android.com/topic/libraries/architecture/lifecycle)
   - [Saving UI states](https://developer.android.com/topic/libraries/architecture/saving-states)

5. **Dependency Injection**
   - [Dependency injection with Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
   - [Hilt best practices](https://developer.android.com/training/dependency-injection/hilt-testing)

6. **Testing**
   - [Test app architecture](https://developer.android.com/topic/architecture/testing)
   - [Test Kotlin coroutines](https://developer.android.com/kotlin/coroutines/test)

### Kotlin Official Documentation

1. **Kotlin Language**
   - [Kotlin documentation](https://kotlinlang.org/docs/home.html)
   - [Higher-Order Functions and Lambdas](https://kotlinlang.org/docs/lambdas.html)
   - [Coroutines guide](https://kotlinlang.org/docs/coroutines-guide.html)

2. **Kotlin Flow**
   - [Asynchronous Flow](https://kotlinlang.org/docs/flow.html)
   - [Flow operators](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/)

### Material Design

1. **Design Guidelines**
   - [Material Design 3](https://m3.material.io/)
   - [Material Design for Android](https://developer.android.com/design)

---

## Verification Checklist

### Threading ✅
- [x] ViewModels use `viewModelScope.launch { }` without dispatcher
- [x] Repositories use `flowOn(Dispatchers.IO)` for network/database
- [x] No manual thread switching in UI layer
- [x] No redundant `withContext()` + `flowOn()`

### MVVM ✅
- [x] ViewModels expose `StateFlow` for UI state
- [x] UI collects state with `collectAsState()`
- [x] No business logic in Composables
- [x] Use Cases handle complex business logic

### Memory Leaks ✅
- [x] `viewModelScope` auto-cancels on ViewModel clear
- [x] `collectAsState()` is lifecycle-aware
- [x] No manual coroutine job management in ViewModels
- [x] All state classes marked `@Immutable`

### Scroll State ✅
- [x] LazyColumn uses `rememberLazyListState()` with initial position
- [x] Scroll position saved in ViewModel `StateFlow`
- [x] Items use `key()` for stability
- [x] Local state uses `rememberSaveable`

### Higher-Order Functions ✅
- [x] Extensive use of Flow operators (`.catch`, `.flowOn`, `.collect`)
- [x] Compose uses lambda parameters
- [x] State management uses lambdas (`.update { }`)
- [x] Standard Kotlin patterns throughout

### Clean Architecture ✅
- [x] Repository interfaces in `domain/` layer
- [x] Implementations in `data/` layer
- [x] Domain layer has no dependencies on framework
- [x] Dependency injection binds interfaces to implementations

---

##  Conclusion

This codebase follows **Android official best practices** for:
- Coroutines & Threading
- MVVM Architecture
- Memory Leak Prevention
- Scroll State Restoration
- Higher-Order Functions
- Clean Architecture

All implementations are **production-ready** and match official Android documentation patterns.
