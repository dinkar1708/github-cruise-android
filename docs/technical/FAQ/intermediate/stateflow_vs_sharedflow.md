# StateFlow vs SharedFlow: The Master Comparison Guide

**Staff-Level Architecture Guide: Under-the-Hood Mechanics, Replay Buffers, Overflow Strategies, WhileSubscribed(5000), and Lifecycle Safety in Android**

📖 **Official Kotlin Documentation:** [StateFlow and SharedFlow Guide](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/)

---

## 🧭 Executive Summary & Core Differences

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          STATEFLOW vs SHAREDFLOW                            │
├────────────────────────────┬────────────────────────────────────────────────┤
│       `StateFlow`          │                 `SharedFlow`                   │
│   (State Holder / UI State)│         (Event / Stream Broadcaster)           │
├────────────────────────────┼────────────────────────────────────────────────┤
│ • Always has INITIAL value │ • NO initial value required                    │
│ • Direct read: `flow.value`│ • NO synchronous `.value` read (event-based)   │
│ • Replay is fixed to 1     │ • Configurable `replay` (0, 1, 2... N)         │
│ • Drops duplicate values   │ • Emits ALL values (even consecutive identical)│
│   (`distinctUntilChanged`) │                                                │
│ • Ideal for: Screen UI state│ • Ideal for: Events, Broadcasts, Telemetry    │
└────────────────────────────┴────────────────────────────────────────────────┘
```

---

## 📊 Comprehensive Comparison Matrix

| Dimension | `StateFlow<T>` | `SharedFlow<T>` | `Channel<T>` |
| :--- | :--- | :--- | :--- |
| **Stream Category** | Hot Stream | Hot Stream | Hot Pipe (Queue) |
| **Initial Value** | **Required** (e.g. `MutableStateFlow(Idle)`) | ❌ None | ❌ None |
| **Synchronous Read** | ✅ Yes via `flow.value` | ❌ No (read-only stream) | ❌ No |
| **Replay Cache** | **Fixed to 1** (Always caches latest) | **Configurable** (`replay = 0..N`) | 0 (FIFO queue) |
| **Duplicate Emissions** | 🛑 **Filtered out** (`a == b` is ignored) | ✅ **All emitted** | ✅ **All emitted** |
| **Subscribers** | Multiple (Broadcasts to all) | Multiple (Broadcasts to all) | **Single Consumer** (1-to-1) |
| **Primary Use Case** | Screen UI State (`UiState`) | Analytics, WebSocket broadcasts | Navigation, One-shot Snackbars |

---

## 🔍 Deep Dive: Under-The-Hood Mechanics

### 1. `StateFlow` is a Specialized `SharedFlow`
`StateFlow` is conceptually identical to a `SharedFlow` configured with specific parameters:

```kotlin
// StateFlow is equivalent to:
val stateFlowEquivalent = MutableSharedFlow<UiState>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
).distinctUntilChanged()
```

#### Why `StateFlow` filters duplicates:
```kotlin
val stateFlow = MutableStateFlow("HELLO")
stateFlow.value = "HELLO" // 🛑 Ignored! No recomposition triggered.
stateFlow.value = "WORLD" // ✅ Emits update to collectors!
```

---

### 2. `SharedFlow` Buffer & Overflow Configurations
`MutableSharedFlow` provides 3 configuration parameters:

```kotlin
val eventFlow = MutableSharedFlow<UserAction>(
    replay = 0,               // Number of past events replayed to NEW subscribers
    extraBufferCapacity = 64, // Extra buffer space for slow subscribers
    onBufferOverflow = BufferOverflow.DROP_OLDEST // What to do when buffer is full
)
```

#### `BufferOverflow` Options:
1. **`BufferOverflow.SUSPEND` (Default)**: Suspends `emit()` until slow subscribers consume items.
2. **`BufferOverflow.DROP_OLDEST`**: Drops the oldest item in the buffer to accept the new emission (never suspends).
3. **`BufferOverflow.DROP_LATEST`**: Drops the newly emitted item without adding it to the buffer.

---

## 🛠️ Production Code Implementations

### Scenario A: UI State with `StateFlow` & `stateIn()`
Converts a cold Room Database / Repository `Flow` into a hot, lifecycle-aware `StateFlow`:

```kotlin
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository
) : ViewModel() {

    // ✅ Converts cold flow into StateFlow with 5-second rotation protection
    val uiState: StateFlow<FeedUiState> = feedRepository.observeArticles()
        .map { articles -> FeedUiState.Success(articles) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L), // 🛡️ Survives rotation!
            initialValue = FeedUiState.Loading
        )
}
```

> [!IMPORTANT]
> **Why `SharingStarted.WhileSubscribed(5_000L)`?**
> When the user rotates their device, the Activity is destroyed and recreated in ~300ms.
> The `5000ms` (5 seconds) timeout prevents the upstream network/database Flow from cancelling and restarting during rotation, saving CPU and battery while still stopping after 5s when navigating away!

---

### Scenario B: Multi-Subscriber Broadcast with `SharedFlow`
Use `SharedFlow` when multiple components need to react to the same event stream (e.g. Session Expiry / Global Logout):

```kotlin
@Singleton
class SessionManager @Inject constructor() {
    private val _logoutEvents = MutableSharedFlow<LogoutReason>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val logoutEvents: SharedFlow<LogoutReason> = _logoutEvents.asSharedFlow()

    suspend fun triggerLogout(reason: LogoutReason) {
        _logoutEvents.emit(reason) // Broadcasts to all active screens simultaneously
    }
}
```

---

### Scenario C: One-Shot UI Navigation with `Channel`
For events that must **only be executed ONCE** (e.g. navigating to a screen or showing a Toast), avoid `SharedFlow(replay = 1)` because it re-triggers on screen rotation! Use `Channel`:

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    // ✅ Channel ensures single-delivery (no duplicate navigations on rotate)
    private val _navigationChannel = Channel<NavigationRoute>(Channel.BUFFERED)
    val navigationEvents = _navigationChannel.receiveAsFlow()

    fun onLoginSuccess() {
        viewModelScope.launch {
            _navigationChannel.send(NavigationRoute.HomeScreen)
        }
    }
}
```

---

## 📱 Safe Flow Collection in Jetpack Compose

Always use `collectAsStateWithLifecycle()` to prevent background collection:

```kotlin
@Composable
fun FeedScreen(viewModel: FeedViewModel = hiltViewModel()) {
    // 🛡️ Automatically stops collection when app goes into background
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val currentState = state) {
        is FeedUiState.Loading -> LoadingSpinner()
        is FeedUiState.Success -> ArticleList(currentState.articles)
        is FeedUiState.Error -> ErrorBanner(currentState.message)
    }
}
```

---

## ⚡ Quick Decision Guide

```
What are you trying to manage?
│
├── 1. Screen UI State (Loading, Data, Error)?
│      └── 👉 USE: `StateFlow<UiState>` (Provides initial value, holds state across rotation)
│
├── 2. Multi-screen Broadcast (Session Expired, Location updates, WebSocket)?
│      └── 👉 USE: `SharedFlow<Event>` (Broadcasts to multiple active subscribers)
│
└── 3. Single-execution UI Event (Show Snackbar, Navigate to Route)?
       └── 👉 USE: `Channel<UiEvent>` (Guarantees single delivery, no re-trigger on rotate)
```

---

## 🔗 Related Documentation

- **Coroutines Dispatchers Types**: [`../beginner/dispatchers_types.md`](../beginner/dispatchers_types.md)
- **Coroutine Scopes Guide**: [`when_to_use_coroutine_scopes.md`](when_to_use_coroutine_scopes.md)
- **Coroutines Error Handling**: [`coroutines_error_handling.md`](coroutines_error_handling.md)
- **Execution Order Quiz**: [`coroutines_execution_order_quiz.md`](coroutines_execution_order_quiz.md)
