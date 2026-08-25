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

### 2. `DisposableEffect` (Setup & Cleanup for Callbacks/Listeners)
Used when a Composable needs to acquire an external resource (Listener, Callback, Observer, BroadcastReceiver) when entering composition and **safely and synchronously release it** when leaving composition:

```kotlin
@Composable
fun SensorMonitor(sensorManager: SensorManager, listener: SensorEventListener) {
    DisposableEffect(sensorManager) {
        // 1. Enter Composition: Register sensor listener (Setup)
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI)

        onDispose {
            // 2. Leave Composition: Unregister sensor to prevent memory leak & battery drain (Cleanup)
            sensorManager.unregisterListener(listener)
        }
    }
}
```

> [!TIP]
> **In Short: Why `DisposableEffect` instead of `LaunchedEffect` for Listeners?**
> * **`DisposableEffect`** ➔ **Setup on Enter** + **Guaranteed `onDispose` Cleanup on Exit** (for Sensors, Callbacks, BroadcastReceivers).
> * **`LaunchedEffect`** ➔ **Runs Coroutines** (has **NO `onDispose` block**; listeners registered here cannot be cleanly unregistered and cause memory leaks & battery drain!).

> [!IMPORTANT]
> **Why NOT `LaunchedEffect` for registering listeners?**
> - `LaunchedEffect` is designed for **asynchronous suspend coroutines** (network calls, timers, collecting Flows). It does **NOT** provide a compile-time `onDispose` block.
> - `DisposableEffect` is designed for **synchronous setup and teardown of non-coroutine objects**. It **forces** you to define `onDispose { ... }` at compile time so you can NEVER forget to unregister your listener, preventing memory leaks and background battery drain.

| Feature | `DisposableEffect` | `LaunchedEffect` |
| :--- | :--- | :--- |
| **Primary Purpose** | Synchronous setup & cleanup (Listeners, Observers) | Asynchronous suspend Coroutines (Network, Delays) |
| **Cleanup Hook** | **`onDispose { ... }` (Mandatory & Synchronous)** | Coroutine cancellation (via `Job.cancel()`) |
| **Runs Coroutine?** | ❌ No (Synchronous block) | ✅ Yes (`CoroutineScope`) |
| **Best For** | `SensorManager`, `BroadcastReceiver`, `ExoPlayer.Listener` | `api.fetchData()`, `delay(1000)`, `snapshotFlow` |

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
Executes **after every successful composition/recomposition**. It is used to publish Compose state to external objects (SDKs, hardware, system UI, or non-Compose controllers) that are not managed by Compose.

#### 🎯 Top 5 Use Cases for `SideEffect`:

##### 1. Synchronizing System Status Bar & Navigation Bar (System UI)
Sync the OS status bar color or light/dark icon theme with dynamic Compose theme changes:
```kotlin
@Composable
fun SystemBarThemeSync(darkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
}
```

##### 2. Updating Crashlytics & Analytics Breadcrumbs
Ensure Crashlytics keys always reflect the latest successfully rendered UI state:
```kotlin
@Composable
fun CrashlyticsStateTracker(currentCategory: String, isPremium: Boolean) {
    SideEffect {
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("active_feed_category", currentCategory)
            setCustomKey("user_is_premium", isPremium)
        }
    }
}
```

##### 3. Syncing State with External Hardware / Bluetooth SDK
Push the latest slider or toggle state to a connected Bluetooth device or external peripheral:
```kotlin
@Composable
fun SmartLightControl(brightness: Float, lightController: BluetoothLightSdk) {
    SideEffect {
        // Keeps the external Bluetooth controller in sync with Compose UI
        lightController.setBrightness(brightness)
    }
}
```

##### 4. Updating Non-Compose Custom Views & Map Controllers
Bridge state updates to a legacy View, MapView, or Game Engine controller:
```kotlin
@Composable
fun MapLocationSync(mapController: GoogleMapController, userLocation: LatLng) {
    SideEffect {
        // Directly updates the non-Compose map camera without re-creating the map
        mapController.updateTargetPosition(userLocation)
    }
}
```

##### 5. APM Telemetry & Recomposition Profiling
Report UI render breadcrumbs or measure recomposition count for performance monitoring:
```kotlin
@Composable
fun FeedRecompositionTelemetry(feedId: String) {
    SideEffect {
        PerformanceMonitor.recordRenderEvent(
            screen = "MultiTabFeed",
            tag = feedId,
            timestamp = System.currentTimeMillis()
        )
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
