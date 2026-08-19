# ViewModel & Architecture in Jetpack Compose

## Purpose

The `ViewModel` stores and manages UI-related state in a lifecycle-conscious way, surviving configuration changes (such as screen rotations and dark mode toggles) without losing data.

---

## Core Concept: ViewModel Lifecycle

```
[Screen Created]      ──► ViewModel Created (init)
[Screen Rotates]      ──► Activity/Composable destroys & recreates
                          └──► SAME ViewModel instance is retained
[Back Press / Exit]   ──► Screen permanently removed
                          └──► ViewModel Destroyed (onCleared())
```

Unlike plain variables inside a Composable (which reset unless saved), a `ViewModel` stays in memory until the user navigates away from that screen.

---

## Modern Standard: Hilt + Compose ViewModel

In our codebase, all feature ViewModels follow the standard Hilt dependency injection pattern:

```kotlin
@HiltViewModel
class RepositorySearchViewModel @Inject constructor(
    private val repositorySearchUseCase: RepositorySearchUseCase
) : ViewModel() {

    // 1. Private mutable state, public immutable state
    private val _uiState = MutableStateFlow(RepositorySearchState())
    val uiState: StateFlow<RepositorySearchState> = _uiState.asStateFlow()

    // 2. Coroutines bound to ViewModel lifecycle
    fun searchRepositories(query: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            repositorySearchUseCase.searchRepositories(query)
                .catch { e -> _uiState.update { it.copy(errorMessage = e.message, isLoading = false) } }
                .collect { result -> _uiState.update { it.copy(repositories = result, isLoading = false) } }
        }
    }
}
```

### Consuming in Jetpack Compose Screen

```kotlin
@Composable
fun RepositorySearchScreen(
    navController: NavHostController,
    viewModel: RepositorySearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Render UI based on uiState
}
```

See implementation in:
- [`RepositorySearchViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/repositorysearch/RepositorySearchViewModel.kt)
- [`RepositorySearchScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/repositorysearch/RepositorySearchScreen.kt)

---

## Key Patterns in Our Codebase

### 1. StateFlow + Atomic Updates
Always use `_uiState.update { ... }` instead of direct assignment `_uiState.value = ...` to prevent race conditions during concurrent updates.

- Real project example: [`FavoritesViewModel.kt:L50`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/favorites/FavoritesViewModel.kt#L50)

### 2. Lifecycle-Aware Coroutines (`viewModelScope`)
Coroutines launched in `viewModelScope` automatically cancel when `onCleared()` is called, preventing memory leaks and background crashes.

- Real project example: [`BaseViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/base/BaseViewModel.kt)

### 3. One-Off Events vs. UI State
- **UI State (`StateFlow`)**: For persistent data (lists, loading indicators, input text).
- **One-Off Events (`Channel` / `SharedFlow`)**: For transient actions (navigation, snackbars, toast messages).

- Real project example: [`ViewModelFlowExampleViewModel.kt:L35-L50`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/ViewModelFlowExampleViewModel.kt#L35-L50)

### 4. Shared ViewModel across Screens
When multiple screens need to share the same data in a navigation sub-flow, scope the ViewModel to the `NavBackStackEntry`.

- Real project example: [`PassingDataExampleViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/PassingDataExampleViewModel.kt) and [`SamplesNavGraph.kt:L140`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/SamplesNavGraph.kt#L140)

---

## Common Pitfalls & Anti-Patterns

| Anti-Pattern | Why It Breaks | Correct Pattern |
| :--- | :--- | :--- |
| **Passing `Activity` or `Context` into ViewModel** | Leaks the Activity context on rotation, causing memory leaks. | Inject `@ApplicationContext` or use dependency injection. |
| **Exposing `MutableStateFlow` to UI** | Composables could mutate state from outside the ViewModel. | Expose immutable `StateFlow` via `.asStateFlow()`. |
| **Using `GlobalScope` instead of `viewModelScope`** | Coroutines will continue running after screen is destroyed. | Always launch coroutines inside `viewModelScope`. |
| **Manual `ViewModelProvider.Factory`** | Verbose boilerplate with unsafe casts. | Use `@HiltViewModel` and `hiltViewModel()`. |

---

## Code Reference & Interactive Demo

- **Interactive Sample Screen**: [`ViewModelFlowExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/ViewModelFlowExampleScreen.kt)
- **Interactive Sample ViewModel**: [`ViewModelFlowExampleViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/ViewModelFlowExampleViewModel.kt)
- **Base Architecture ViewModel**: [`BaseViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/base/BaseViewModel.kt)
- **Feature ViewModels in Production**:
  - [`RepositorySearchViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/repositorysearch/RepositorySearchViewModel.kt)
  - [`FavoritesViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/favorites/FavoritesViewModel.kt)
  - [`UsersListViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/users/UsersListViewModel.kt)
  - [`PassingDataExampleViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/PassingDataExampleViewModel.kt)
