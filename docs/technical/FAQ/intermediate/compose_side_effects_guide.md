# Jetpack Compose Side Effects: The Complete Master Guide

## Overview

In Jetpack Compose, Composable functions should ideally be **pure** (free of side effects), predictable, and fast.  
A **Side Effect** is any operation that escapes the scope of a Composable function—such as:
* Making network requests or querying databases
* Mutating shared state outside Compose
* Registering/unregistering broadcast receivers or hardware sensors
* Starting timers or animations
* Logging analytics on recomposition

To perform side effects safely without causing UI jank, memory leaks, or endless recomposition loops, Compose provides a dedicated suite of **Side-Effect Handlers**.

---

## 🔗 Code Implementation Reference

* **Interactive Demo Screen**: [`ComposeSideEffectsSampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/ComposeSideEffectsSampleScreen.kt)
* **Sample Navigation Route**: `SamplesDestinations.COMPOSE_SIDE_EFFECTS_ROUTE`

---

## 📊 Master Side-Effect Handlers Cheatsheet

| Side-Effect API | Execution Trigger | Cancellation / Cleanup | Coroutine Scope? | Primary Use Case |
| :--- | :--- | :--- | :--- | :--- |
| **`LaunchedEffect(key)`** | On entering composition or when `key` changes | Auto-cancelled when leaving composition or when `key` changes | ✅ **Yes** | One-off API calls, animations, timers, observing Flow channels. |
| **`rememberCoroutineScope()`** | Manual invocation (e.g. `onClick`) | Auto-cancelled when calling Composable leaves composition | ✅ **Yes** | User-driven actions (Button taps, snackbars, list scroll animations). |
| **`DisposableEffect(key)`** | On entering composition or when `key` changes | Executes mandatory `onDispose { ... }` on key change/exit | ❌ **No** | Sensor listeners, broadcast receivers, lifecycle observers. |
| **`SideEffect { }`** | After **every** successful recomposition | No cleanup hook | ❌ **No** | Pushing Compose state to external non-Compose objects / analytics. |
| **`rememberUpdatedState(value)`** | Updated on every recomposition | Keeps reference alive without restarting effect | ❌ **No** | Passing fresh lambdas/callbacks to long-running effects. |
| **`derivedStateOf { }`** | Recalculates only when internal state changes | N/A (State calculation optimization) | ❌ **No** | Minimizing recomposition when observing rapidly changing state (e.g. scroll position). |
| **`produceState(initial)`** | Launches coroutine on composition | Auto-cancelled on exit | ✅ **Yes** | Converting external non-Compose data / RxJava / Callbacks into Compose `State`. |
| **`snapshotFlow { }`** | Collects whenever observed Compose state changes | Standard Flow cancellation | ❌ **No (Flow)** | Converting Compose `State<T>` into a standard Kotlin `Flow<T>`. |

---

## 🛠️ Deep Dive & Full Code Patterns

### 1. `LaunchedEffect` (Coroutine Bound to Lifecycle / Key)

* **Behavior:** Launches a coroutine when entering composition. If the `key` changes, the previous coroutine is **cancelled** and a new one is launched.

```kotlin
@Composable
fun SearchBar(query: String, onSearch: (String) -> Unit) {
    // Re-launches debounce timer every time `query` changes:
    LaunchedEffect(query) {
        if (query.isNotBlank()) {
            delay(500L) // 500ms debounce
            onSearch(query)
        }
    }
}
```

---

### 2. `rememberCoroutineScope()` (User-Initiated Coroutines)

* **Behavior:** Creates a coroutine scope bound to the Composable. Used inside non-composable callbacks like `onClick`.

```kotlin
@Composable
fun ScrollToTopButton(listState: LazyListState) {
    val coroutineScope = rememberCoroutineScope()

    Button(onClick = {
        // ✅ Launch animation coroutine from click event:
        coroutineScope.launch {
            listState.animateScrollToItem(0)
        }
    }) {
        Text("Top")
    }
}
```

---

### 3. `DisposableEffect` (Setup + Mandatory Cleanup)

* **Behavior:** For side effects that require teardown (e.g. unregistering listeners). `onDispose` is **guaranteed** to run when keys change or when the Composable leaves composition.

```kotlin
@Composable
fun SystemBroadcastReceiver(systemAction: String, onReceive: (Intent?) -> Unit) {
    val context = LocalContext.current

    DisposableEffect(context, systemAction) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) = onReceive(intent)
        }
        context.registerReceiver(receiver, IntentFilter(systemAction))

        // 🧹 MANDATORY CLEANUP:
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}
```

---

### 4. `SideEffect` (Publishing State to External Code)

* **Behavior:** Runs **after every successful recomposition**. Never use this for suspend operations! Used to sync Compose state with external non-Compose objects (e.g. Analytics trackers or system window bars).

```kotlin
@Composable
fun AnalyticsTracker(screenName: String, userProperties: UserProperties) {
    // Runs after every successful UI pass to push fresh state to legacy SDK:
    SideEffect {
        LegacyAnalyticsSdk.getInstance().setUserProperties(userProperties.toMap())
        Timber.d("Pushed fresh state to analytics for $screenName")
    }
}
```

---

### 5. `rememberUpdatedState` (Capture Fresh Values Without Restarting Effects)

* **The Problem:** If a long-running `LaunchedEffect` captures a callback `onTimeout: () -> Unit`, passing `onTimeout` as a key restarts the timer! If you DON'T pass it as a key, the effect captures a stale lambda.
* **The Solution:** Wrap `onTimeout` in `rememberUpdatedState`.

```kotlin
@Composable
fun SplashTimer(onTimeout: () -> Unit) {
    // ✅ Holds a mutable reference to the newest onTimeout lambda:
    val currentOnTimeout by rememberUpdatedState(onTimeout)

    // Key is Unit (runs once and never restarts):
    LaunchedEffect(Unit) {
        delay(3000L)
        currentOnTimeout() // Calls the latest lambda safely!
    }
}
```

---

### 6. `derivedStateOf` (Recomposition Optimization)

* **Behavior:** Buffers rapid state changes (e.g. scroll position pixels) and emits a new state **only when the boolean condition changes**. Prevents unnecessary UI recompositions.

```kotlin
@Composable
fun ScrollBackToTopButton(listState: LazyListState) {
    // ❌ BAD: Causes recomposition on EVERY pixel scrolled!
    // val showButton = listState.firstVisibleItemIndex > 0

    // ✅ GOOD: Recomposes ONLY when transitioning from false -> true or true -> false!
    val showButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    if (showButton) {
        FloatingActionButton(onClick = { /* Scroll */ }) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
        }
    }
}
```

---

### 7. `produceState` (Converting External Data to Compose State)

* **Behavior:** Launches a coroutine that produces Compose `State<T>` from external network, Room, or Flow sources.

```kotlin
@Composable
fun loadNetworkImage(url: String, repository: ImageRepository): State<Result<Bitmap>> {
    return produceState<Result<Bitmap>>(initialValue = Result.Loading, url) {
        // Runs coroutine on composition and when `url` changes:
        value = repository.fetchBitmap(url)
    }
}
```

---

### 8. `snapshotFlow` (Converting Compose State to Kotlin Flow)

* **Behavior:** Converts Compose `State<T>` into a cold Kotlin `Flow<T>`, enabling standard Flow operators like `filter`, `map`, and `debounce`.

```kotlin
@Composable
fun TrackScrollAnalytics(listState: LazyListState) {
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .filter { it > 10 }
            .collect { index ->
                Timber.d("User scrolled past index: $index")
            }
    }
}
```

---

## 🚫 Common Anti-Patterns & Pitfalls

| Anti-Pattern | Why It Breaks | Correct Replacement |
| :--- | :--- | :--- |
| **Launching coroutines directly in body** | Spawns brand new coroutines on *every* single recomposition pass. | Wrap inside `LaunchedEffect(key)` or `rememberCoroutineScope()`. |
| **Using `Thread.sleep()` in Composable** | Freezes the entire 120 FPS UI thread causing ANR crashes. | Use `delay()` inside `LaunchedEffect`. |
| **Omitting `onDispose` in `DisposableEffect`** | Leaks listeners, broadcast receivers, and Activity context in RAM. | Always implement `onDispose { ... }`. |
| **Calling `LaunchedEffect` inside `onClick`** | Compiler error: Cannot invoke `@Composable` inside a regular lambda. | Use `rememberCoroutineScope().launch { }`. |
| **Missing `rememberUpdatedState` in Splash timers** | Calling stale lambdas or restarting timers on recomposition. | Wrap callback in `rememberUpdatedState(callback)`. |

---

## 🎙️ Staff Interview Defense Script (30 Seconds)

> *"In Jetpack Compose, Composables must remain side-effect free.  
> We control asynchronous operations using **`LaunchedEffect`** for lifecycle-bound coroutines and **`rememberCoroutineScope`** for user-initiated clicks.  
> For hardware or system listeners requiring cleanup, we enforce **`DisposableEffect` with `onDispose`** to guarantee zero memory leaks.  
> When handling high-frequency state like scrolling, we use **`derivedStateOf`** to prevent redundant recompositions, and **`rememberUpdatedState`** to prevent stale callback captures in long-running jobs."*
