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

## 🛑 What Happens When `finish()` is Called in `onCreate()`?

When you call `finish()` inside `Activity.onCreate()` (e.g. user authentication check fails, deep link invalid, or redirecting to login):

```
┌────────────────────────────────────────────────────────────────────────┐
│                        CALLING finish() IN onCreate()                  │
│                                                                        │
│  onCreate() ───▶ [ finish() called ] ───▶ onDestroy()                  │
│                                                                        │
│  ❌ SKIPPED: onStart(), onResume(), onPause(), onStop()                │
└────────────────────────────────────────────────────────────────────────┘
```

### 1. Which Lifecycle Methods are Called?
* ❌ **`onStart()`** $\rightarrow$ **SKIPPED**
* ❌ **`onResume()`** $\rightarrow$ **SKIPPED**
* ❌ **`onPause()`** $\rightarrow$ **SKIPPED**
* ❌ **`onStop()`** $\rightarrow$ **SKIPPED**
* ✅ **`onDestroy()`** $\rightarrow$ **CALLED IMMEDIATELY** after `onCreate()` returns!

### 2. ⚠️ The Critical `return` Trap (Very Common Bug!):
Calling `finish()` **does NOT immediately exit** the `onCreate()` function! The CPU continues executing all code below `finish()` until the method finishes unless you explicitly add a `return` statement:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (!isUserLoggedIn) {
        startActivity(Intent(this, LoginActivity::class.java))
        finish() 
        return // 🚨 CRUCIAL: Without `return`, setupHeavyUi() BELOW WILL STILL RUN!
    }

    // This will throw NullPointer or crash if return is omitted:
    setupHeavyUi()
}
```

> [!IMPORTANT]
> **Key Lifecycle Takeaway:**  
> When `finish()` is called inside `onCreate()`, the Activity will **NEVER** enter `onStart()`, `onResume()`, `onPause()`, or `onStop()`. It transitions **directly from `onCreate()` to `onDestroy()`**.

> [!WARNING]
> **Always Add `return` After `finish()`:**  
> `finish()` only schedules the Activity destruction with the Android OS Window Manager. It **does not stop the Kotlin method execution**. Always pair `finish()` with `return` when you want to abort initialization immediately!

---

## 🔄 Lifecycle Transition Scenarios Comparison

### 1. Scenario A: When are ONLY `onPause()` ➔ `onResume()` Called?
*(Without triggering `onStop()` or `onStart()`)*

```
┌────────────────────────────────────────────────────────────────────────┐
│               PARTIALLY OBSCURED (STILL VISIBLE TO USER)               │
│                                                                        │
│  [Activity in Foreground] ──▶ onPause() ──▶ [Visible but Lost Focus]   │
│                                                 │                      │
│  [Activity Regains Focus] ◀── onResume() ◀──────┘                      │
│                                                                        │
│  ❌ NEVER CALLS: onStop(), onRestart(), onStart()                      │
└────────────────────────────────────────────────────────────────────────┘
```

**Key Rule:** `onStop()` is **ONLY** called when the Activity becomes **100% invisible**. If the Activity is **still visible in the background** but has lost user focus:

* 🛡️ **System Permission Dialog:** OS runtime permission prompt (`POST_NOTIFICATIONS`, `ACCESS_FINE_LOCATION`) pops up over the Activity.
* 📱 **Multi-Window / Split-Screen Mode:** App is visible on top/bottom half of screen, but user is touching the other app.
* 🖼️ **Picture-in-Picture (PiP) Mode:** Video playback transitions to a floating window.
* 🪟 **Translucent / Dialog-Themed Activity:** Another Activity with a transparent/dialog theme (`Theme.Translucent` or `Theme.Dialog`) opens on top.
* 👆 **Biometric Prompt / Fingerprint Dialog:** System authentication overlay appears.
* 💳 **Google Play In-App Purchase Sheet:** Billing bottom-sheet covers part of the screen.

When the overlay is dismissed $\rightarrow$ Activity regains focus $\rightarrow$ **`onResume()` is called directly**!

---

### 2. Scenario B: When are `onPause()` ➔ `onStop()` ➔ `onRestart()` ➔ `onStart()` ➔ `onResume()` Called?

```
┌────────────────────────────────────────────────────────────────────────┐
│               COMPLETELY HIDDEN (100% INVISIBLE TO USER)               │
│                                                                        │
│  [Activity Running] ──▶ onPause() ──▶ onStop() ──▶ [Activity Hidden]   │
│                                                           │            │
│  onResume() ◀── onStart() ◀── onRestart() ◀───────────────┘            │
└────────────────────────────────────────────────────────────────────────┘
```

**Key Rule:** The Activity was **completely hidden from the screen** and later brought back into the foreground without being destroyed:

* 🏠 **User presses HOME button or Swipes to Home Screen:** App moves to background; user taps app icon later to return.
* 🔀 **App Switcher / Recent Apps:** User switches to another full-screen app (e.g., WhatsApp/Chrome), then returns back.
* 🚀 **Opening a Full-Screen Second Activity:** Activity A opens Activity B. When user taps Back on Activity B $\rightarrow$ Activity A receives `onRestart()` $\rightarrow$ `onStart()` $\rightarrow$ `onResume()`.
* 🔒 **Screen Lock / Device Sleep:** User presses Power Button to turn off the display $\rightarrow$ turns screen back on and unlocks device.
* 📞 **Incoming Full-Screen Phone Call:** Phone dialer occupies the entire screen.

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

## 📋 Real-World Logcat Verification

Below are actual runtime Logcat traces from `MainActivity.kt` and `LifecycleActivityExampleScreen.kt` demonstrating OS transitions and `repeatOnLifecycle` execution:

### 1. App Startup & `repeatOnLifecycle(STARTED)` Coroutine Flow
```text
2026-08-25 00:19:31.015 D/MainActivity: 🏛️ LifecycleOwner event: ON_CREATE (Source: MainActivity)
2026-08-25 00:19:31.018 D/MainActivity$onCreate: 👋 Hello from MainActivity lifecycleScope coroutine! (Activity is STARTED)
2026-08-25 00:19:31.018 D/MainActivity: 🌐 [API Call Started] Fetching remote app configuration from server...
2026-08-25 00:19:31.019 D/MainActivity: 🏛️ LifecycleOwner event: ON_START (Source: MainActivity)
2026-08-25 00:19:31.020 D/MainActivity: 🟢 Activity ON_START - Screen is becoming visible
2026-08-25 00:19:31.024 D/MainActivity: 🏛️ LifecycleOwner event: ON_RESUME (Source: MainActivity)
2026-08-25 00:19:31.024 D/MainActivity: ⚡ Activity ON_RESUME - Screen is interactive
2026-08-25 00:19:33.020 D/MainActivity: ✅ [API Call Finished] Received response: App Remote Config Loaded (Version 2026.1)
2026-08-25 00:19:33.020 D/MainActivity$onCreate: 🎉 Result received in MainActivity: App Remote Config Loaded (Version 2026.1)
```

### 2. Scenario A: Partial Obscuring (Dialog / Permission Prompt)
```text
D/LifecycleActivityExampleScreen: ⏸️ ON_PAUSE — Activity partially obscured (Lost user focus)
D/LifecycleActivityExampleScreen: ⚡ ON_RESUME — Activity in foreground & fully interactive
```
*(Notice: `ON_STOP` and `ON_START` are NEVER called)*

### 3. Scenario B: Home Button / App Switch (Completely Hidden)
```text
D/LifecycleActivityExampleScreen: ⏸️ ON_PAUSE — Activity partially obscured (Lost user focus)
D/LifecycleActivityExampleScreen: 🛑 ON_STOP — Activity 100% hidden (App in background)
```

---

## Code Reference & Project Examples

- **Interactive Activity Lifecycle Screen**: [`LifecycleActivityExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/beginner/LifecycleActivityExampleScreen.kt)
- **Main Activity Host**: [`MainActivity.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/MainActivity.kt)
- **Lifecycle Observer Component**: [`LifecycleObserverExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/beginner/LifecycleObserverExampleScreen.kt)
- **Application Class Lifecycle**: [`App.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/App.kt)
- **ViewModel Lifecycle Management**: [`BaseViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/base/BaseViewModel.kt)
