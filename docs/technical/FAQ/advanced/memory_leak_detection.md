# Memory Leak Detection in Android

## Purpose

Learn how to detect, diagnose, and fix memory leaks in Android applications using Android Studio Profiler and LeakCanary.

---

## What is a Memory Leak?

A memory leak occurs when objects are no longer needed but cannot be garbage collected because they're still referenced by other objects. This leads to:
- Increasing memory usage over time
- OutOfMemoryError crashes
- Poor app performance
- Battery drain

---

## Common Causes

### 1. Non-Static Inner Classes
```kotlin
// BAD - Inner class holds reference to outer Activity
class MyActivity : ComponentActivity() {
    inner class MyHandler : Handler() {
        override fun handleMessage(msg: Message) {
            // This holds reference to MyActivity!
        }
    }
}

// GOOD - Static/separate class
class MyHandler : Handler() {
    private val weakActivity: WeakReference<MyActivity>

    constructor(activity: MyActivity) {
        weakActivity = WeakReference(activity)
    }
}
```

### 2. Uncancelled Coroutines
```kotlin
// BAD - Coroutine continues after screen is destroyed
GlobalScope.launch {
    while (true) {
        delay(1000)
        updateUI() // Holds reference to destroyed Activity/Composable
    }
}

// GOOD - Use viewModelScope
class MyViewModel : ViewModel() {
    fun startWork() {
        viewModelScope.launch {
            // Automatically cancelled when ViewModel is cleared
        }
    }
}
```

### 3. Listener Leaks
```kotlin
// BAD - Listener not removed
class MyScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        GlobalEventBus.register(listener)
        // Listener holds reference to Activity
    }
    // Missing: onDestroy() { GlobalEventBus.unregister(listener) }
}

// GOOD - Cleanup in DisposableEffect (Compose)
@Composable
fun MyComposable() {
    DisposableEffect(Unit) {
        val listener = MyListener()
        GlobalEventBus.register(listener)

        onDispose {
            GlobalEventBus.unregister(listener)
        }
    }
}
```

### 4. Handler Leaks
```kotlin
// BAD - Handler posts delayed runnable, never removed
val handler = Handler(Looper.getMainLooper())
handler.postDelayed({ doWork() }, 10000)
// Screen destroyed before 10 seconds, handler still holds reference

// GOOD - Remove callbacks in cleanup
DisposableEffect(Unit) {
    val handler = Handler(Looper.getMainLooper())
    val runnable = Runnable { doWork() }
    handler.postDelayed(runnable, 10000)

    onDispose {
        handler.removeCallbacks(runnable)
    }
}
```

### 5. Static References
```kotlin
// BAD - Static field holds Activity reference
companion object {
    var currentActivity: Activity? = null // Leaks Activity!
}

// GOOD - Use Application context for static storage
companion object {
    var appContext: Context? = null // OK if it's Application context
}
```

---

## Detection Tools

### 1. Android Studio Profiler

**How to use**:
1. Open Android Studio
2. View → Tool Windows → Profiler
3. Select your app process
4. Click Memory timeline
5. Click "Record" to start allocation tracking

**What to look for**:
- Memory growth over time (heap size increasing)
- Retained objects after navigation
- Multiple instances of Activities/Fragments

**Steps**:
```
1. Start profiler
2. Navigate to a screen
3. Press back button
4. Force GC (garbage can icon)
5. Take heap dump
6. Look for retained Activity/Fragment instances
```

### 2. Heap Dump Analysis

**Take a heap dump**:
1. Click "Dump Java heap" in Profiler
2. Wait for analysis
3. Filter by class name (e.g., "MyActivity")
4. Check instance count (should be 0 after navigation)

**Analyze retained objects**:
```
1. Find leaked object in heap dump
2. Right-click → "Go to Instance"
3. Check "References" tab
4. Find GC root keeping object alive
5. Identify leak source
```

### 3. LeakCanary (Recommended)

**Setup**:
```gradle
dependencies {
    debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'
}
```

**How it works**:
- Automatically detects leaked Activities
- Shows notification when leak detected
- Provides leak trace to find root cause
- No code changes needed!

**Reading leak traces**:
```
LeakCanary detected:
┬───
│ GC Root: System class
├─ SomeGlobalManager instance
│    ↓ SomeGlobalManager.listener
├─ MyListener instance
│    ↓ MyListener.callback
└─ MyActivity instance
     Leaking: YES (Activity destroyed but not collected)
```

### 4. Compose-Specific Tools

**Layout Inspector**:
1. Tools → Layout Inspector
2. Select process
3. Look for multiple composable instances
4. Check if disposables are being called

**Recomposition Profiling**:
- Enable "Show Recomposition Counts" in Layout Inspector
- Look for composables that recompose excessively
- These might indicate state management issues leading to leaks

---

## Fixing Common Patterns

### Pattern 1: ViewModel with Coroutines

```kotlin
// GOOD - Use viewModelScope
class MyViewModel : ViewModel() {
    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state = _state.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            // Automatically cancelled when ViewModel cleared
            val data = repository.getData()
            _state.value = UiState.Success(data)
        }
    }

    override fun onCleared() {
        // viewModelScope automatically cancelled
        Timber.d("ViewModel cleared, coroutines cancelled")
    }
}
```

### Pattern 2: Compose Cleanup

```kotlin
@Composable
fun MyScreen() {
    DisposableEffect(Unit) {
        // Setup code
        val listener = createListener()
        registerListener(listener)

        onDispose {
            // Cleanup code - ALWAYS CALLED
            unregisterListener(listener)
            Timber.d("Cleanup executed")
        }
    }
}
```

### Pattern 3: LaunchedEffect with Cleanup

```kotlin
@Composable
fun TickingClock() {
    var time by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(1000)
            time = System.currentTimeMillis()
        }
        // Automatically cancelled when composable leaves composition
    }

    Text("Time: $time")
}
```

### Pattern 4: Weak References for Callbacks

```kotlin
class MyCallback(activity: MyActivity) {
    private val weakActivity = WeakReference(activity)

    fun onEvent() {
        weakActivity.get()?.updateUI() // Safely access, might be null
    }
}
```

---

## Best Practices

### 1. Use Proper Scopes
- ✅ `viewModelScope` for ViewModel coroutines
- ✅ `lifecycleScope` for Activity/Fragment coroutines
- ✅ `LaunchedEffect` for Compose coroutines
- ❌ Avoid `GlobalScope` unless absolutely necessary

### 2. Always Cleanup
```kotlin
// Compose
DisposableEffect(key) {
    val resource = acquire()
    onDispose { resource.release() }
}

// ViewModel
override fun onCleared() {
    // Cancel jobs, close streams, remove listeners
}
```

### 3. Use Immutable Data
```kotlin
// Prevents accidental holding of large objects
@Immutable
data class UiState(val data: List<String>)
```

### 4. Avoid Static Context References
```kotlin
// BAD
companion object {
    lateinit var context: Context // May leak Activity!
}

// GOOD
companion object {
    lateinit var appContext: Context // Application context is OK
}

// Usage
MyClass.appContext = applicationContext // Not 'this' from Activity!
```

### 5. Test for Leaks
```kotlin
// In your tests
@Test
fun `no memory leak after screen destroy`() {
    // Navigate to screen
    // Navigate back
    // Force GC
    // Assert no instances remain
}
```

---

## Testing Checklist

When testing for leaks:

1. **Navigate through app flows**
   - Open screen → Go back → Check memory

2. **Rotate device multiple times**
   - Configuration changes create new instances

3. **Check after background/foreground**
   - onStop/onStart cycles

4. **Run for extended period**
   - Memory should stabilize, not grow continuously

5. **Use LeakCanary in debug builds**
   - Catches leaks during development

6. **Profile in release builds**
   - R8/ProGuard may hide debug-only leaks

---

## Debugging Workflow

```
1. Notice memory growth or OOM crashes
2. Open Profiler and take heap dump
3. Filter by suspected class name
4. Check instance count (should be low)
5. If high count:
   a. Select instance
   b. View "References" tab
   c. Find GC root
   d. Identify leak source
6. Fix leak source (add cleanup code)
7. Repeat heap dump test
8. Verify instance count drops to 0
```

---

## Common Mistakes

### Mistake 1: Forgetting onDispose
```kotlin
// BAD
LaunchedEffect(Unit) {
    val timer = Timer()
    timer.schedule(...)
    // Timer never cancelled!
}

// GOOD
DisposableEffect(Unit) {
    val timer = Timer()
    timer.schedule(...)
    onDispose { timer.cancel() }
}
```

### Mistake 2: Capturing Context in Coroutines
```kotlin
// BAD
fun Activity.loadData() {
    GlobalScope.launch {
        delay(10000)
        findViewById<TextView>(R.id.text).text = "Done"
        // Activity destroyed, but coroutine still holds reference!
    }
}

// GOOD
fun ViewModel.loadData() {
    viewModelScope.launch {
        // ViewModel-scoped, cancelled automatically
    }
}
```

### Mistake 3: Static Listener Collections
```kotlin
// BAD
object EventBus {
    val listeners = mutableListOf<Listener>() // Leaks all listeners!
}

// GOOD
object EventBus {
    val listeners = mutableListOf<WeakReference<Listener>>()
    // Or provide unregister() method and ensure it's called
}
```

---

## Resources

- [Android Memory Profiler](https://developer.android.com/studio/profile/memory-profiler)
- [LeakCanary](https://square.github.io/leakcanary/)
- [Compose Memory Management](https://developer.android.com/jetpack/compose/performance)

---

## Code Reference & Interactive Demo

- **Interactive Sample Screen**: [`MemoryLeakExamplesScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/advanced/MemoryLeakExamplesScreen.kt)

Remember: **Prevention is easier than debugging! Always clean up resources.**

