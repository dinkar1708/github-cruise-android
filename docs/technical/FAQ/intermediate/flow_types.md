# StateFlow vs SharedFlow vs Flow in Android

## Purpose

Kotlin Coroutines provides multiple reactive stream primitives (`Flow`, `StateFlow`, `SharedFlow`, and `Channel`). This guide clarifies when and how to use each in Android application architecture.

---

## Quick Comparison

| Primitive | Stream Type | Initial Value | Replay | Collectors | Primary Android Use Case |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`Flow`** | Cold | No | No | Each collector gets a fresh stream | Database queries (Room), DataStore, network requests |
| **`StateFlow`** | Hot | Yes (required) | 1 (latest) | All share the same current state | UI state holders in ViewModel (`uiState`) |
| **`SharedFlow`** | Hot | No | Configurable (`replay`) | Broadcasts to all active subscribers | Multi-subscriber event bus |
| **`Channel`** | Hot | No | 0 (buffered/rendezvous) | 1 subscriber receives each event | Single-delivery UI events (toasts, one-shot navigation) |

---

## Practical Implementations in Our Codebase

### 1. `StateFlow` for UI State
Maintains current UI state in ViewModel. Emits updates only when state changes (`distinctUntilChanged` is automatic).

```kotlin
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesState())
    val uiState: StateFlow<FavoritesState> = _uiState.asStateFlow()

    private fun observeFavorites() {
        favoritesRepository.favorites
            .onEach { favorites -> _uiState.update { it.copy(favorites = favorites) } }
            .launchIn(viewModelScope)
    }
}
```

See real implementation in [`FavoritesViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/favorites/FavoritesViewModel.kt).

### 2. `Flow` (Cold Stream) for Data Layers
Only executes work when actively collected. Used in Room databases and DataStore preferences.

```kotlin
// DataStore emits theme changes reactively
val themeFlow: Flow<AppTheme> = dataStore.data.map { preferences ->
    preferences[THEME_KEY] ?: AppTheme.SYSTEM
}
```

See real implementation in [`ThemeDataStore.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/data/datastore/ThemeDataStore.kt).

### 3. `Channel` for One-Off Events
Prevents repeated triggers on screen rotation because each event is consumed exactly once.

```kotlin
private val _toastEvents = Channel<String>()
val toastEvents: Flow<String> = _toastEvents.receiveAsFlow()

fun showMessage(msg: String) {
    viewModelScope.launch { _toastEvents.send(msg) }
}
```

See real implementation in [`ViewModelFlowExampleViewModel.kt:L48`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/ViewModelFlowExampleViewModel.kt#L48).

---

## Collecting Flows Safely in Jetpack Compose

Always use `collectAsStateWithLifecycle()` rather than `collectAsState()`. This automatically stops flow collection when the app goes into the background, saving CPU cycles and battery.

```kotlin
@Composable
fun FavoritesScreen(viewModel: FavoritesViewModel = hiltViewModel()) {
    // Safe collection tied to Activity/Compose lifecycle
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        CircularProgressIndicator()
    } else {
        FavoritesList(uiState.favorites)
    }
}
```

---

## Common Pitfalls & Junior Tips

1. **Never Expose `MutableStateFlow`**: Keep `_uiState` private and expose `uiState` via `.asStateFlow()`.
2. **Use `.update { }`**: Always use `_uiState.update { it.copy(...) }` to ensure atomic updates across coroutines.
3. **Cold vs Hot**:
   - Calling `repository.getUsers()` returning a cold `Flow` runs the query *every time* someone collects.
   - Using `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)` converts cold flows into hot cached `StateFlow`s.

---

## Code Reference & Interactive Demo

- **Interactive Sample Screen**: [`ViewModelFlowExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/ViewModelFlowExampleScreen.kt)
- **Interactive Sample ViewModel**: [`ViewModelFlowExampleViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/ViewModelFlowExampleViewModel.kt)
- **DataStore Reactive Flow**: [`ThemeDataStore.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/data/datastore/ThemeDataStore.kt)
- **Production UI State Collection**: [`FavoritesViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/favorites/FavoritesViewModel.kt)
