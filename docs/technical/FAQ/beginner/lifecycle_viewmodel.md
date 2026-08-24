# Android Architecture: ViewModel Lifecycle

## Purpose

The **ViewModel Lifecycle** is specifically designed to manage and store UI-related data across Android **Configuration Changes** (such as screen rotation, folding/unfolding, dark mode toggle) while ensuring automatic resource cleanup when the screen is permanently destroyed.

---

## ⏳ The ViewModel Lifecycle Timeline

```
Activity / Screen Creation
           │
           ▼
┌──────────────────────────────────────┐
│       ViewModel Initialized          │ ◄── Injected via Hilt (@HiltViewModel)
│  (init block / StateFlow initialized)│
└──────────────────┬───────────────────┘
                   │
                   ▼
┌──────────────────────────────────────┐
│         Active UI Observation        │ ◄── UI collects StateFlow / SharedFlow
│ (Screen visible & handling user data)│
└──────────────────┬───────────────────┘
                   │
      ┌────────────┴────────────┐
      │  Configuration Change?  │ (e.g. Screen Rotation)
      └────────────┬────────────┘
                   │
         YES ──────┴────── NO
          │                 │
          ▼                 ▼
┌──────────────────┐  ┌──────────────────────────────────────┐
│ Activity Rebuilt │  │       Screen Dismissed / Finished    │
│  (VM Retained!)  │  │         (User pressed Back)          │
└──────────────────┘  └──────────────────┬───────────────────┘
                                         │
                                         ▼
                      ┌──────────────────────────────────────┐
                      │        ViewModel.onCleared()         │
                      │  (viewModelScope cancelled, cleanup) │
                      └──────────────────┬───────────────────┘
                                         │
                                         ▼
                              [ ViewModel Destroyed ]
```

---

## ⚖️ Lifecycle Comparison Matrix

| Phase | `Activity` | Jetpack `Compose` Composable | Android `ViewModel` |
| :--- | :--- | :--- | :--- |
| **Creation** | `onCreate()` | Enters Composition | Instantiated via `ViewModelProvider` |
| **Screen Rotation** | 💥 **Destroyed & Recreated** (`onDestroy` ➔ `onCreate`) | 💥 **Recomposed / Reset** (unless `rememberSaveable`) | 🛡️ **SURVIVES!** Retained in `ViewModelStore` |
| **Permanent Exit (Back Press)** | `onDestroy()` | Leaves Composition | **`onCleared()`** called; discarded from memory |
| **Process Death (Low Memory)** | 💥 Process killed | 💥 Process killed | 💥 Process killed (*Restored via `SavedStateHandle`*) |
| **Async Scoping** | `lifecycleScope` | `LaunchedEffect` | **`viewModelScope`** |

---

## 🗄️ ViewModel Scopes & Owners

ViewModels are scoped to a **`ViewModelStoreOwner`**. The lifecycle of the ViewModel matches the lifecycle of its owner:

### 1. NavBackStackEntry Scope (Default in Navigation Compose)
```kotlin
composable("user_details/{userId}") {
    // Retained as long as "user_details" is on the navigation backstack
    val viewModel: UserDetailsViewModel = hiltViewModel()
}
```
* **Behavior**: Survives rotation. When user presses Back and screen is popped, `onCleared()` is called.

### 2. Activity Scope (Shared ViewModel)
```kotlin
@Composable
fun CartSummaryScreen(
    // Scoped to MainActivity: Shared across ALL tabs/screens in the app
    sharedViewModel: SharedCartViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
)
```
* **Behavior**: Retained across the entire life of the `MainActivity`.

---

## 🛡️ Surviving Process Death with `SavedStateHandle`

While ViewModels automatically survive screen rotations, they **do not survive system process death** (when Android kills the background process due to low RAM).

To survive process death, inject **`SavedStateHandle`**:

```kotlin
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val searchRepository: SearchRepository
) : ViewModel() {

    // Automatically restored after OS process kill:
    val searchQuery = savedStateHandle.getStateFlow("search_query", initialValue = "")

    fun updateSearchQuery(query: String) {
        savedStateHandle["search_query"] = query
    }
}
```

---

## 🧹 Automatic Cleanup with `viewModelScope` & `onCleared()`

### 1. `viewModelScope`
`viewModelScope` is an extension property on `ViewModel` tied directly to `Dispatchers.Main.immediate` + `SupervisorJob()`.

* Any coroutine launched in `viewModelScope` is **automatically cancelled** when `onCleared()` is called.
* Prevents background leaks without requiring manual `job.cancel()` calls!

```kotlin
class UserProfileViewModel : ViewModel() {
    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            // If user navigates away mid-request, this coroutine is cancelled automatically!
            val profile = repository.fetchUser(userId)
            _state.value = profile
        }
    }
}
```

### 2. Custom Cleanup in `onCleared()`
Override `onCleared()` if you have non-coroutine resources to close (e.g. RxJava Disposables, Camera instances, SQLite cursors):

```kotlin
override fun onCleared() {
    super.onCleared()
    // Release native resources or non-coroutine subscriptions
    customSdk.release()
}
```

---

## ⚠️ Common Pitfalls & Junior Traps

1. **Passing `Context` or `View` into ViewModel:**
   - ❌ **NEVER** hold an `Activity` or `View` reference in a ViewModel. Because ViewModels outlive Activities during rotation, this causes massive memory leaks!
   - ✅ If application context is needed, use `AndroidViewModel(application)` or inject `@ApplicationContext`.

2. **Launching unmanaged Coroutines with `GlobalScope`:**
   - ❌ `GlobalScope.launch` does not cancel when the screen closes.
   - ✅ Always use `viewModelScope.launch`.

3. **Re-triggering data loads on every Recomposition:**
   - In Compose, avoid calling `viewModel.loadData()` directly inside Composable functions without `LaunchedEffect(Unit)` or triggering inside the ViewModel's `init { ... }` block.

## 📋 Real-World Logcat Verification

Below are actual runtime Logcat traces from `LifecycleViewModelExampleViewModel.kt` demonstrating ViewModel creation, coroutine execution, `SavedStateHandle` writes, and `onCleared()` auto-disposal:

### 1. ViewModel Creation, Background Timer & `onCleared()` Auto-Disposal
```text
2026-08-25 00:14:45.960 D/LifecycleViewModelExampleViewModel: [00:14:45.958] 🟢 ViewModel INSTANTIATED (Hash: 966CDA2)
2026-08-25 00:14:45.961 D/LifecycleViewModelExampleViewModel: [00:14:45.960] ⏱️ viewModelScope timer STARTED
2026-08-25 00:14:47.653 D/LifecycleViewModelExampleViewModel: [00:14:47.651] 🔄 Timer reset to 0
2026-08-25 00:14:48.386 D/LifecycleViewModelExampleViewModel: [00:14:48.386] 🛑 viewModelScope timer STOPPED
2026-08-25 00:14:50.664 W/LifecycleViewModelExampleViewModel: 💀 ViewModel ON_CLEARED invoked! viewModelScope cancelled automatically.
```
*(Notice: Navigating back from the screen immediately calls `onCleared()`, cancelling all running coroutines automatically.)*

### 2. `SavedStateHandle` Real-Time State Persistence
```text
2026-08-25 00:20:17.956 D/LifecycleViewModelExampleViewModel: [00:20:17.955] 💾 SavedStateHandle updated: "d"
2026-08-25 00:20:18.111 D/LifecycleViewModelExampleViewModel: [00:20:18.110] 💾 SavedStateHandle updated: "dd"
2026-08-25 00:20:18.292 D/LifecycleViewModelExampleViewModel: [00:20:18.291] 💾 SavedStateHandle updated: "ddd"
2026-08-25 00:20:18.408 D/LifecycleViewModelExampleViewModel: [00:20:18.408] 💾 SavedStateHandle updated: "dddd"
2026-08-25 00:20:18.628 D/LifecycleViewModelExampleViewModel: [00:20:18.627] 💾 SavedStateHandle updated: "ddddd"
```

---

## Code Reference & Interactive Demo

- **Interactive ViewModel Lifecycle Screen**: [`LifecycleViewModelExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/beginner/LifecycleViewModelExampleScreen.kt)
- **Interactive ViewModel Implementation**: [`LifecycleViewModelExampleViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/beginner/LifecycleViewModelExampleViewModel.kt)
- **Base ViewModel Architecture**: [`BaseViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/base/BaseViewModel.kt)
- **Coroutine Scopes Guide**: [`when_to_use_coroutine_scopes.md`](../intermediate/when_to_use_coroutine_scopes.md)
