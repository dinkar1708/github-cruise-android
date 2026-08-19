# Kotlin Coroutines Basics

> **Official Documentation**: For pure Kotlin language fundamentals, syntax, and theoretical background, refer to the official [Kotlin Coroutines Basics Guide](https://kotlinlang.org/docs/coroutines-basics.html). This guide focuses on applying coroutines in modern Android architecture.

## Purpose

Coroutines simplify asynchronous programming, making async code look sequential and easy to read.

---

## What are Coroutines?

Lightweight threads that can be suspended and resumed without blocking the main thread.

```kotlin
fun main() = runBlocking {
    launch {
        delay(1000L)
        println("World")
    }
    println("Hello")
}

// Output:
// Hello
// World (after 1 second)
```

---

## Coroutine Builders

### launch
Fire and forget - returns Job:

```kotlin
viewModelScope.launch {
    val data = fetchData()
    updateUI(data)
}
```

### async
Returns result - use with await:

```kotlin
val deferred = async {
    fetchData()
}
val result = deferred.await()
```

### runBlocking
Blocks thread - use only in tests/main:

```kotlin
fun main() = runBlocking {
    delay(1000L)
    println("Done")
}
```

---

## Suspend Functions

Functions that can be paused and resumed:

```kotlin
suspend fun fetchUser(): User {
    delay(1000L)  // Suspends, doesn't block
    return api.getUser()
}

// Call from coroutine
viewModelScope.launch {
    val user = fetchUser()  // Suspends here
    updateUI(user)
}
```

---

## Coroutine Scopes

### viewModelScope
Tied to ViewModel lifecycle:

```kotlin
class MyViewModel : ViewModel() {
    fun loadData() {
        viewModelScope.launch {
            val data = repository.getData()
            _state.value = data
        }
    }
}
// Auto-cancelled when ViewModel cleared
```

### lifecycleScope
Tied to Activity/Fragment lifecycle:

```kotlin
class MyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updateUI(state)
                }
            }
        }
    }
}
```

### GlobalScope
Never cancelled - avoid in apps:

```kotlin
// DON'T USE
GlobalScope.launch {
    // Runs forever, memory leak
}
```

---

## Dispatchers

### Dispatchers.Main
UI operations:

```kotlin
launch(Dispatchers.Main) {
    textView.text = "Updated"
}
```

### Dispatchers.IO
Network/disk operations:

```kotlin
launch(Dispatchers.IO) {
    val data = database.getUsers()
}
```

### Dispatchers.Default
CPU-intensive work:

```kotlin
launch(Dispatchers.Default) {
    val result = complexCalculation()
}
```

---

## Practical Examples

### Example 1: Network Call
```kotlin
class UserViewModel : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    fun loadUsers() {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    api.getUsers()
                }
                _users.value = result
            } catch (e: Exception) {
                Log.e("UserVM", "Error: ${e.message}")
            }
        }
    }
}
```

### Example 2: Parallel Calls
```kotlin
suspend fun loadUserData(userId: String) {
    coroutineScope {
        val userDeferred = async { api.getUser(userId) }
        val reposDeferred = async { api.getRepos(userId) }

        val user = userDeferred.await()
        val repos = reposDeferred.await()

        updateUI(user, repos)
    }
}
```

### Example 3: Sequential Calls
```kotlin
suspend fun login(email: String, password: String) {
    val token = api.login(email, password)  // Wait for this
    val user = api.getProfile(token)         // Then this
    saveUser(user)
}
```

---

## Error Handling

### Try-Catch
```kotlin
viewModelScope.launch {
    try {
        val data = repository.getData()
        _state.value = Success(data)
    } catch (e: Exception) {
        _state.value = Error(e.message)
    }
}
```

### CoroutineExceptionHandler
```kotlin
val handler = CoroutineExceptionHandler { _, exception ->
    Log.e("Coroutine", "Caught: ${exception.message}")
}

viewModelScope.launch(handler) {
    throw Exception("Error")
}
```

---

## Flow Collection

```kotlin
viewModelScope.launch {
    repository.getUsers()
        .catch { e -> emit(emptyList()) }
        .collect { users ->
            _state.value = users
        }
}
```

---

## Common Patterns

### Pattern 1: Load-Update Pattern
```kotlin
fun refresh() {
    viewModelScope.launch {
        _isLoading.value = true
        try {
            val data = repository.refresh()
            _data.value = data
        } finally {
            _isLoading.value = false
        }
    }
}
```

### Pattern 2: Retry Pattern
```kotlin
suspend fun <T> retryIO(
    times: Int = 3,
    block: suspend () -> T
): T {
    repeat(times - 1) {
        try {
            return block()
        } catch (e: IOException) {
            // Continue to next attempt
        }
    }
    return block() // Last attempt
}
```

### Pattern 3: Timeout Pattern
```kotlin
viewModelScope.launch {
    try {
        withTimeout(5000L) {
            val data = api.getData()
            updateUI(data)
        }
    } catch (e: TimeoutCancellationException) {
        showError("Request timeout")
    }
}
```

---

## Cancellation

### Manual Cancellation
```kotlin
val job = viewModelScope.launch {
    repeat(1000) {
        delay(100)
        println("Working...")
    }
}

// Cancel after 2 seconds
delay(2000)
job.cancel()
```

### Check Cancellation
```kotlin
suspend fun doWork() {
    repeat(1000) {
        ensureActive()  // Throws if cancelled
        // Do work
    }
}
```

---

## Common Questions

**Q: What's the difference between launch and async?**
A: launch returns Job (fire and forget), async returns Deferred (get result with await).

**Q: When to use which Dispatcher?**
A: Main for UI, IO for network/disk, Default for heavy computation.

**Q: How to avoid memory leaks?**
A: Use viewModelScope or lifecycleScope, they auto-cancel.

**Q: Can I call suspend function from regular function?**
A: No, must be called from coroutine or another suspend function.

---

## Best Practices

1. Use viewModelScope in ViewModels
2. Use lifecycleScope in Activities/Fragments
3. Always handle exceptions
4. Use appropriate Dispatchers
5. Avoid GlobalScope
6. Cancel long-running operations
7. Use withContext to switch Dispatchers

---

## Console Logs

```kotlin
viewModelScope.launch {
    Log.d("Coroutine", "Starting on: ${Thread.currentThread().name}")

    val result = withContext(Dispatchers.IO) {
        Log.d("Coroutine", "Network call on: ${Thread.currentThread().name}")
        api.getData()
    }

    Log.d("Coroutine", "Back to: ${Thread.currentThread().name}")
}

// Output:
// Starting on: main
// Network call on: DefaultDispatcher-worker-1
// Back to: main
```

---

## Code Reference & Interactive Demo

- **Interactive Sample Screen**: [`CoroutinesExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/beginner/CoroutinesExampleScreen.kt)
- **Sample ViewModel (`viewModelScope`, Dispatchers)**: [`CoroutinesExampleViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/beginner/CoroutinesExampleViewModel.kt)
- **Base ViewModel Coroutines Management**: [`BaseViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/base/BaseViewModel.kt)
- **Repository Suspend Functions**: [`SearchRepositoryImpl.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/data/repository/search/SearchRepositoryImpl.kt)

