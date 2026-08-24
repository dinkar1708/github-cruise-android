# Jetpack Compose Composable Lifecycle

## Purpose

Understanding the **Jetpack Compose Lifecycle** is critical for writing performant, bug-free declarative UI. Unlike imperative View hierarchies or Android Activities with strict `onCreate`/`onDestroy` callbacks, Composables follow a reactive **Composition Lifecycle**.

---

## 🔄 The 3 Phases of Composable Lifecycle

A Composable's lifecycle is much simpler than an Activity or Fragment. It consists of three fundamental events:

```
           [ Enter the Composition ]
          (Initial Composition / First Draw)
                      │
                      ▼
         ┌─────────────────────────┐
         │   In the Composition    │ ◄─── Recomposition (0 or more times)
         │   (UI Rendered & State) │      (Triggered by State / Input changes)
         └────────────┬────────────┘
                      │
                      ▼
          [ Leave the Composition ]
         (Disposed / Removed from UI)
```

1. **Enter the Composition**: When Compose first renders a Composable function. It runs the Composable, resolves layouts, and records state dependencies.
2. **Recomposition (0 or more times)**: When state read by the Composable changes, Compose re-executes only the affected Composable scopes to update the UI.
3. **Leave the Composition**: When the Composable is removed from the UI hierarchy (e.g., `if (isVisible)` becomes false or user navigates to another screen), its internal remembered state is discarded, and cleanup effects are invoked.

---

## ⚖️ Activity Lifecycle vs. Composable Lifecycle

| Dimension | Android `Activity` Lifecycle | Jetpack `Composable` Lifecycle |
| :--- | :--- | :--- |
| **Model** | Imperative state machine (`CREATED`, `STARTED`, `RESUMED`) | Declarative reactive node in Composition Tree |
| **Creation** | `onCreate()` called once by Android OS | Initial composition when node enters tree |
| **Updates** | Explicit mutation calls (`setText()`, `notifyDataSetChanged()`) | Automatic **Recomposition** on observable state changes |
| **Destruction** | `onDestroy()` on finish or config change | **Disposal / Leaving Composition** when pruned from tree |
| **State Retention** | `onSaveInstanceState(Bundle)` / `ViewModel` | `remember` (recomposition), `rememberSaveable` (activity recreate) |

---

## 🛠️ Composable Lifecycle Hooks & Side Effects

Compose provides specific APIs to attach logic and cleanup to Composable lifecycle events:

### 1. `remember` vs. `rememberSaveable`
* **`remember { ... }`**: Retains value across **Recompositions**. When the Composable leaves composition, value is destroyed. Reset on configuration changes (e.g., screen rotation).
* **`rememberSaveable { ... }`**: Retains value across **Recompositions AND Configuration Changes AND Process Death** (saved into Android `SavedStateRegistry`).

```kotlin
// Lost on rotation:
val scrollCount = remember { mutableIntStateOf(0) }

// Preserved on rotation & process death:
val userSearchInput = rememberSaveable { mutableStateOf("") }
```

---

### 2. `DisposableEffect` (Setup & Cleanup)
Used when a Composable needs to acquire an external resource when entering composition and **safely release it** when leaving composition:

```kotlin
@Composable
fun SensorMonitor(sensorManager: SensorManager, listener: SensorEventListener) {
    DisposableEffect(sensorManager) {
        // 1. Enter Composition: Register sensor listener
        sensorManager.registerListener(listener, ...)

        onDispose {
            // 2. Leave Composition: Unregister sensor to prevent battery drain & memory leak
            sensorManager.unregisterListener(listener)
        }
    }
}
```

---

### 3. `LaunchedEffect` (Lifecycle-Aware Coroutines)
Launches a coroutine when entering the Composition. Automatically **cancels** the coroutine when:
1. The Composable leaves the Composition.
2. Any `key` argument changes (cancels old coroutine and restarts new one).

```kotlin
@Composable
fun AutoRefreshFeed(categoryId: String, onRefresh: suspend () -> Unit) {
    LaunchedEffect(categoryId) {
        // Runs in coroutine; cancelled automatically when leaving screen or categoryId changes
        onRefresh()
    }
}
```

---

### 4. `SideEffect` (Publishing Compose State to Non-Compose Code)
Executes after **every successful recomposition**:

```kotlin
@Composable
fun AnalyticsTracker(screenName: String) {
    SideEffect {
        // Reports latest rendered state to external analytics SDK
        AnalyticsSdk.setCurrentScreen(screenName)
    }
}
```

---

## 📱 Bridge: Connecting Activity Lifecycle to Composables

When a Composable needs to react to OS-level Activity lifecycle events (like pausing video when app goes to background):

### Using Modern `LifecycleEventEffect` / `LifecycleResumeEffect`
```kotlin
@Composable
fun VideoPlayerScreen(player: ExoPlayer) {
    // Automatically runs when Activity becomes RESUMED, cleans up on PAUSE
    LifecycleResumeEffect(Unit) {
        player.play()
        onPauseOrDispose {
            player.pause()
        }
    }
}
```

### Using `LocalLifecycleOwner.current` + `LifecycleEventObserver`
```kotlin
@Composable
fun CameraPreviewScreen() {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> startCamera()
                Lifecycle.Event.ON_STOP -> stopCamera()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
```

---

## ⚠️ Common Pitfalls & Junior Traps

1. **Calling Non-Idempotent Side Effects Directly in Composable Body:**
   ```kotlin
   // ❌ BAD: Runs on EVERY recomposition, making multiple API calls!
   viewModel.fetchUserData()

   // ✅ GOOD: Runs safely in LaunchedEffect
   LaunchedEffect(Unit) {
       viewModel.fetchUserData()
   }
   ```

2. **Forgetting `onDispose` in `DisposableEffect`:**
   Always unregister callbacks, broadcast receivers, or sensor listeners to prevent severe memory leaks.

3. **Confusing `remember` with `rememberSaveable`:**
   Use `rememberSaveable` for user inputs, scroll positions, and critical transient UI state that must survive screen rotation.

## 📋 Real-World Logcat Verification

Below are actual runtime Logcat traces from `LifecycleComposeExampleScreen.kt` demonstrating Composable entry, recomposition counting via `SideEffect`, and disposal via `onDispose`:

### 1. Composable Enter, Recomposition & Disposal Flow
```text
D/LifecycleComposeExampleScreen: 🟢 [DisposableEffect] Child ENTERED Composition (Initial Render)
D/LifecycleComposeExampleScreen: ⚡ [SideEffect] Child recomposed (Total: 1 times)
D/LifecycleComposeExampleScreen: ⚡ [SideEffect] Child recomposed (Total: 2 times)
D/LifecycleComposeExampleScreen: 🔴 [DisposableEffect.onDispose] Child LEFT Composition (Disposed!)
```
*(Notice: When child composable is toggled off from UI hierarchy, `onDispose` immediately cleans up resources and all remembered states are cleared.)*

### 2. Host Activity Lifecycle Bridge inside Compose
```text
D/LifecycleComposeExampleScreen: 🏛️ Host Lifecycle Event: ON_PAUSE
D/LifecycleComposeExampleScreen: 🏛️ Host Lifecycle Event: ON_RESUME
```

---

## Code Reference & Interactive Demo

- **Interactive Compose Lifecycle Screen**: [`LifecycleComposeExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/beginner/LifecycleComposeExampleScreen.kt)
- **Lifecycle Observer Component Screen**: [`LifecycleObserverExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/beginner/LifecycleObserverExampleScreen.kt)
- **Side Effects Master Guide**: [`compose_side_effects_guide.md`](../intermediate/compose_side_effects_guide.md)
