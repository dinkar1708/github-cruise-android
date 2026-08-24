# Side Effects & LaunchedEffect in Jetpack Compose

## Purpose

Composable functions should be pure (side-effect free). Side effects (such as making network requests, triggering timers, navigating on events, or registering callbacks) must be handled using Compose Side-Effect APIs to respect the Composable lifecycle.

---

## Quick Comparison: Compose Effect Handlers

| Handler | Execution Trigger | Cancellation / Teardown | Use Case |
| :--- | :--- | :--- | :--- |
| **`LaunchedEffect(key)`** | On entering composition or when `key` changes | Coroutine is cancelled automatically on exit/key change | API requests, observing ViewModel event channels, animations |
| **`DisposableEffect(key)`** | On entering composition or when `key` changes | Must provide cleanup logic in `onDispose { }` | Registering broadcast receivers, sensor listeners, lifecycle observers |
| **`SideEffect { }`** | After **every** successful recomposition | No automatic cleanup | Publishing Compose state to external non-compose libraries or analytics |
| **`rememberCoroutineScope()`** | Manual invocation (e.g., inside `onClick`) | Scope is cancelled when calling Composable leaves composition | User-initiated coroutines (button taps, pull-to-refresh) |

---

## Core Patterns

### 1. Observing ViewModel Events with `LaunchedEffect`
```kotlin
@Composable
fun UserProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Listens to one-off events from ViewModel
    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }
}
```

### 2. Cleanup with `DisposableEffect`
```kotlin
@Composable
fun SystemBroadcastListener(onEvent: () -> Unit) {
    val context = LocalContext.current

    DisposableEffect(context) {
        val receiver = createBroadcastReceiver(onEvent)
        context.registerReceiver(receiver, IntentFilter("ACTION_DATA_CHANGED"))

        // Guaranteed cleanup when Composable leaves screen
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}
```

### 3. User Actions with `rememberCoroutineScope`
```kotlin
@Composable
fun FavoriteAction(onSave: suspend () -> Unit) {
    val scope = rememberCoroutineScope()

    Button(onClick = {
        // DO NOT call LaunchedEffect inside onClick! Use rememberCoroutineScope:
        scope.launch { onSave() }
    }) {
        Text("Save Favorite")
    }
}
```

---

## Common Pitfalls & Anti-Patterns

| Anti-Pattern | Why It Breaks | Correct Approach |
| :--- | :--- | :--- |
| **Launching coroutines directly in Composable body** | Runs and spawns new jobs on *every* single recomposition. | Wrap inside `LaunchedEffect(key)` or `rememberCoroutineScope()`. |
| **Using `LaunchedEffect(Unit)` for dynamic parameters** | Won't re-execute when parameters (e.g., `userId`) change. | Pass the parameter as key: `LaunchedEffect(userId)`. |
| **Calling `LaunchedEffect` inside `onClick`** | Compile error (cannot call `@Composable` inside a regular lambda). | Use `rememberCoroutineScope().launch { }`. |
| **Forgetting `onDispose` in `DisposableEffect`** | Leaks listeners and memory when screen closes. | Always provide `onDispose { ... }`. |

---

## Code Reference & Interactive Demo

- **Full Compose Side Effects Master Guide**: [`compose_side_effects_guide.md`](compose_side_effects_guide.md)
- **Interactive Side Effects Suite Screen**: [`ComposeSideEffectsSampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/ComposeSideEffectsSampleScreen.kt)
- **Interactive LaunchedEffect Screen**: [`LaunchedEffectExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/LaunchedEffectExampleScreen.kt)
- **ViewModel Effect/Event Flows**: [`BaseViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/base/BaseViewModel.kt)
- **Sample Event Handling**: [`ViewModelFlowExampleViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/intermediate/ViewModelFlowExampleViewModel.kt)
