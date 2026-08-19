# Android Activity Lifecycle & Compose Architecture

## Purpose

Understanding how Android manages Activity lifecycle states is fundamental for preventing memory leaks, managing system resources, and preserving user state across configuration changes and process termination.

---

## The 6 Core Lifecycle States

```
                 onCreate()
                     ↓
┌─────────────────────────────────────────────┐
│ Created: Views & dependencies initialized   │
└─────────────────────────────────────────────┘
                  onStart()
                     ↓
┌─────────────────────────────────────────────┐
│ Started: Activity visible to user           │
└─────────────────────────────────────────────┘
                 onResume()
                     ↓
┌─────────────────────────────────────────────┐
│ Resumed (Foreground): Fully interactive     │
└─────────────────────────────────────────────┘
                  onPause()
                     ↓
┌─────────────────────────────────────────────┐
│ Paused: Partially obscured (dialog, split)  │
└─────────────────────────────────────────────┘
                  onStop()
                     ↓
┌─────────────────────────────────────────────┐
│ Stopped: Hidden from view                   │
└─────────────────────────────────────────────┘
                 onDestroy()
                     ↓
┌─────────────────────────────────────────────┐
│ Destroyed: Resources released               │
└─────────────────────────────────────────────┘
```

---

## Modern Single-Activity Compose Architecture

In modern Android development (like GitHub Cruise), applications typically use a **Single Activity** (`MainActivity`) with Jetpack Compose handling UI screens:

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GithubCruiseTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
```
See implementation in [`MainActivity.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/MainActivity.kt).

---

## State Preservation: Configuration Change vs. Process Death

| Scenario | What Happens | How State Is Preserved |
| :--- | :--- | :--- |
| **Screen Rotation / Dark Mode** | `Activity` destroyed & recreated | `ViewModel` stays in memory; `rememberSaveable` retains Compose UI state |
| **System Low-Memory Process Kill** | App process killed in background | `SavedStateHandle` in ViewModel & `rememberSaveable` in Compose |
| **User Back Press / App Exit** | Screen permanently closed | `ViewModel.onCleared()` called; resources freed |

### Preserving State in Compose
```kotlin
// 1. Transient UI state that survives rotation
var searchQuery by rememberSaveable { mutableStateOf("") }

// 2. Business data managed in ViewModel (survives rotation)
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

---

## Observing Activity Lifecycles in Jetpack Compose

When a Composable needs to react to Activity lifecycle events (such as starting a camera preview on `ON_RESUME` or pausing playback on `ON_PAUSE`):

```kotlin
@Composable
fun LifecycleAwareComponent(
    onStart: () -> Unit = {},
    onStop: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> onStart()
                Lifecycle.Event.ON_STOP -> onStop()
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

## Common Pitfalls & Junior Tips

1. **Avoid Heavy Operations in `onCreate` / `onPause`**: Keep lifecycle methods lean to avoid ANRs (Application Not Responding).
2. **Never Store Activity Context Statically**: Holding a static reference to an `Activity` will cause massive memory leaks on rotation.
3. **Use `viewModelScope` / `repeatOnLifecycle`**: Never launch unmanaged coroutines in Activities; always scope them properly.

---

## Code Reference & Project Examples

- **Main Activity Host**: [`MainActivity.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/MainActivity.kt)
- **Application Class Lifecycle**: [`App.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/App.kt)
- **ViewModel Lifecycle Management**: [`BaseViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/base/BaseViewModel.kt)
