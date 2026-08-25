# Kotlin Coroutines Error Handling Master Guide

**Staff-Level Architecture Guide: Objective & Purpose, 5 Error Handling Strategies, `launch` vs `async` Deep-Dive, and What Happens When You Use vs Don't Use Right/Wrong Patterns**

📖 **Official Kotlin Documentation:** [Kotlin Coroutines: Exception Handling & Supervision](https://kotlinlang.org/docs/exception-handling.html)

---

## 🎯 1. Objective & Purpose: Why Error Handling in Coroutines Matters

### The Core Problem: Structured Concurrency Failure Cascade
In Kotlin Coroutines, tasks are bound together by **Structured Concurrency**. Under a standard `Job`:
1. When a child coroutine encounters an unhandled exception, it **does not fail silently in isolation**.
2. The child immediately passes the exception **UP to its parent scope**.
3. The parent scope cancels itself and **cancels ALL other sibling coroutines** running under it.
4. If unhandled at the root, **the entire Android application crashes**!

```
┌─────────────────────────────────────────────────────────────────────────────┐
│              UNHANDLED EXCEPTION CASCADE (STANDARD JOB)                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                         Parent Scope (e.g. viewModelScope)                  │
│                                    ▲                                        │
│          💥 Exception Propagates UP│  ❌ Cancels All Siblings               │
│                                    │                                        │
│               ┌────────────────────┴────────────────────┐                   │
│               │                                         │                   │
│      Child 1 (Feed API)                       Child 2 (Avatar API)          │
│      💥 Throws HTTP 500!                      ❌ CANCELLED by parent!       │
│                                               (Even though healthy!)        │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 🎯 Our 3 Core Objectives:
1. **Fault Isolation:** A failure in a non-critical background task (e.g. fetching user avatar) must **never** kill critical tasks (e.g. loading feed).
2. **Clean Cancellation:** Coroutines must stop immediately when the user leaves a screen without leaking memory.
3. **Graceful Degradation:** Users must receive clear UI feedback (Error states, retry buttons, Snackbars) instead of app crashes.

---

## 🧭 2. Total 5 Types/Tools to Handle Coroutine Errors

Here is the complete breakdown of the **5 Coroutine Error Handling Tools**, structured point-by-point:

### 1. `try-catch` Block (Local In-Place Handling)
* **📍 Where to Use:** Inside individual ViewModel methods or UI event handlers launched with `launch { ... }`.
* **🛑 How it Handles Cancellation:** ⚠️ **Warning!** Generic `catch (e: Exception)` will intercept `CancellationException`. You **MUST** check `if (e is CancellationException) throw e` to avoid breaking coroutine cancellation.
* **🛡️ Prevents Sibling Failure:** ❌ **No.** It only handles errors occurring locally within its own block.
* **🎯 Best For:** Showing in-place error banners, setting `UiState.Error`, or triggering a Snackbar.
* **📝 Quick Example:**
  ```kotlin
  try {
      val data = repository.fetchUser()
      _uiState.value = UiState.Success(data)
  } catch (e: Exception) {
      if (e is CancellationException) throw e // 🚀 Always rethrow!
      _uiState.value = UiState.Error(e.message)
  }
  ```

---

### 2. `supervisorScope` & `SupervisorJob` (Parallel Task Isolation)
* **📍 Where to Use:** Surrounding multiple parallel `async` or `launch` operations where each task is independent.
* **🛑 How it Handles Cancellation:** ✅ If the parent screen is destroyed, cancellation propagates downward to cancel all children. But if one child fails, it does **not** cancel its siblings.
* **🛡️ Prevents Sibling Failure:** ✅ **YES!** Failures are isolated to the failing child only.
* **🎯 Best For:** Loading independent parallel APIs (e.g. User Profile + Feed + Notification Count).
* **📝 Quick Example:**
  ```kotlin
  suspend fun loadDashboard() = supervisorScope {
      val profile = async { api.getProfile() }
      val feed = async { api.getFeed() } // If feed fails, profile STILL completes!
      Dashboard(profile.await(), runCatching { feed.await() }.getOrNull())
  }
  ```

---

### 3. `CoroutineExceptionHandler` (Global Uncaught Safety Net)
* **📍 Where to Use:** Attached to Root `CoroutineScope` definitions (e.g., ApplicationScope, background daemon scope).
* **🛑 How it Handles Cancellation:** ❌ Does not handle `CancellationException` (cancellations are ignored by CEH).
* **🛡️ Prevents Sibling Failure:** ❌ **No.** By the time CEH is called, the failing coroutine has already finished and cancelled its standard parent.
* **🎯 Best For:** Global crash logging, telemetry, and reporting uncaught exceptions to Firebase Crashlytics or Sentry.
* **📝 Quick Example:**
  ```kotlin
  val ceh = CoroutineExceptionHandler { _, exception ->
      Timber.e(exception, "Uncaught background crash intercepted!")
      Crashlytics.recordException(exception)
  }
  val appScope = CoroutineScope(Dispatchers.Default + SupervisorJob() + ceh)
  ```

---

### 4. `Result<T>` & `runCatching {}` (Functional Clean Architecture Wrapper)
* **📍 Where to Use:** Inside Repository and Domain Use Case layers to return safe result monads instead of throwing exceptions.
* **🛑 How it Handles Cancellation:** ⚠️ **Warning!** Standard `runCatching` catches `Throwable` (which includes `CancellationException`). Ensure you rethrow cancellation when using in coroutines.
* **🛡️ Prevents Sibling Failure:** ❌ **No.** Converts thrown exceptions into structured `Result.failure(e)` objects.
* **🎯 Best For:** Clean Architecture layers where functions return `Result<User>` to the ViewModel.
* **📝 Quick Example:**
  ```kotlin
  suspend fun getUserProfile(): Result<UserProfile> = runCatching {
      api.fetchUserProfile()
  }.onFailure { if (it is CancellationException) throw it }
  ```

---

### 5. Kotlin Flow `.catch {}` & `.retryWhen {}` (Reactive Pipeline Stream)
* **📍 Where to Use:** Inside reactive data streams in Repository or ViewModel (Room DB queries, DataStore, WebSocket feeds).
* **🛑 How it Handles Cancellation:** ✅ Fully respects downstream cancellation signals.
* **🛡️ Prevents Sibling Failure:** N/A (Flows are single sequential streams of values).
* **🎯 Best For:** Emitting fallback offline cached data (`emit(cachedList)`) or retrying failed network streams with exponential backoff.
* **📝 Quick Example:**
  ```kotlin
  fun getFeed(): Flow<List<Article>> = articleDao.observeFeed()
      .retryWhen { cause, attempt -> (cause is IOException && attempt < 3) }
      .catch { emit(emptyList()) } // Safe fallback emission
  ```

---

## 🔬 3. Deep Dive: `launch` vs `async` (Right vs Wrong Patterns 1–5)

---

### Point 1: `launch` Error Handling (In-Place `try-catch`)

* **When to use:** For fire-and-forget UI operations (updating `UiState`, submitting a form, saving to database).
* **What happens if you DON'T use `try-catch`:** The exception escapes the coroutine, cancels `viewModelScope`, and **crashes the app with an UncaughtException crash**!
* **What happens if you DO use `try-catch`:** The exception is caught locally on the current thread, and you can safely update `_uiState.value = UiState.Error(msg)`.

#### ❌ WRONG (Crashes App):
```kotlin
// ❌ WRONG: No error handling inside launch
fun loadUserData() {
    viewModelScope.launch {
        val user = api.getUser() // 💥 If this throws SocketTimeoutException, APP CRASHES!
        _uiState.value = UiState.Success(user)
    }
}
```

#### ✅ RIGHT (Safe In-Place Handling):
```kotlin
// ✅ RIGHT: Wrapped in try-catch with Cancellation check
fun loadUserData() {
    viewModelScope.launch {
        _uiState.value = UiState.Loading
        try {
            val user = api.getUser()
            _uiState.value = UiState.Success(user)
        } catch (e: Exception) {
            if (e is CancellationException) throw e // 🚀 Preserves cancellation
            _uiState.value = UiState.Error(e.localizedMessage ?: "Network error")
        }
    }
}
```

---

### Point 2: `async` with `supervisorScope` (The Silent Scope-Kill Trap)

* **When to use:** When fetching 2 or more independent resources concurrently in parallel (e.g. User Profile + Recent Orders).
* **What happens if you DON'T use `supervisorScope` (Standard `coroutineScope`):**
  - If `async 1` fails with HTTP 500, it **immediately cancels the parent `coroutineScope`**.
  - The parent cancels `async 2` **before `await()` is even reached**!
  - Wrapping `try { d1.await() } catch (...)` does **NOT** prevent the parent scope from dying!
* **What happens if you DO use `supervisorScope`:**
  - A failure in `async 1` stays isolated. `async 2` continues to completion without interruption.

#### ❌ WRONG (1 Failure Kills All Siblings):
```kotlin
// ❌ WRONG: Standard coroutineScope kills both tasks if one fails!
suspend fun loadDashboard() = coroutineScope {
    val profileDeferred = async { api.fetchProfile() } // 100% Healthy
    val feedDeferred = async { api.fetchFeed() }       // 💥 Throws HTTP 500

    try {
        // 💥 Even with try-catch here, coroutineScope is ALREADY CANCELLED!
        val profile = profileDeferred.await()
        val feed = feedDeferred.await()
        DashboardUiState.Success(profile, feed)
    } catch (e: Exception) {
        // profileDeferred was killed unnecessarily!
        DashboardUiState.Error("Failed")
    }
}
```

#### ✅ RIGHT (Isolated Parallel Execution with `supervisorScope`):
```kotlin
// ✅ RIGHT: supervisorScope isolates failures so healthy tasks finish!
suspend fun loadDashboard() = supervisorScope {
    val profileDeferred = async { 
        runCatching { api.fetchProfile() }.getOrNull() 
    }
    val feedDeferred = async { 
        runCatching { api.fetchFeed() }.getOrNull() 
    }

    val profile = profileDeferred.await()
    val feed = feedDeferred.await()

    DashboardUiState.Success(
        profile = profile ?: UserProfile.EMPTY,
        feed = feed ?: emptyList()
    )
}
```

---

### Point 3: `CoroutineExceptionHandler` (Root `launch` vs `async`)

* **What it is:** An uncaught exception handler of last resort (similar to `Thread.defaultUncaughtExceptionHandler`).
* **Where it works:** **ONLY on Root coroutines started with `launch`**.
* **Where it FAILS:**
  - ❌ Inside `async { ... }`: Exceptions in `async` are encapsulated inside the `Deferred` object and only thrown when `.await()` is called. `CoroutineExceptionHandler` is completely ignored!
  - ❌ Inside child coroutines: Child coroutines always propagate exceptions to their parent, bypassing their own local handler.

#### ❌ WRONG (CEH on `async` is Ignored):
```kotlin
// ❌ WRONG: CoroutineExceptionHandler is NEVER CALLED on async!
val ceh = CoroutineExceptionHandler { _, exception ->
    println("Caught: $exception") // 🛑 NEVER PRINTS!
}

val scope = CoroutineScope(Dispatchers.IO + ceh)
val deferred = scope.async {
    throw IllegalStateException("Async Error") // 💥 Held inside Deferred!
}

// Exception only explodes when calling await():
deferred.await() // 💥 Crashes here if not caught with try-catch!
```

#### ✅ RIGHT (CEH on Root `launch` with `SupervisorJob`):
```kotlin
// ✅ RIGHT: CEH attached to Root launch acts as safety net for telemetry
val globalHandler = CoroutineExceptionHandler { _, exception ->
    Timber.e(exception, "Uncaught root exception captured!")
    FirebaseCrashlytics.getInstance().recordException(exception)
}

val appScope = CoroutineScope(Dispatchers.Default + SupervisorJob() + globalHandler)

fun triggerGlobalSync() {
    appScope.launch {
        // If an unexpected crash occurs here, globalHandler catches it without crashing the app!
        syncEngine.performFullSync()
    }
}
```

---

### Point 4: Swallowing `CancellationException` (Silent Memory Leaks)

* **What is it:** In Kotlin, cancellation is cooperative and implemented via a special `CancellationException`.
* **What happens if you DON'T check and rethrow `CancellationException`:**
  - When the user closes the screen, `viewModelScope` cancels and throws `CancellationException` into active suspend functions (like `delay()` or network calls).
  - Catching `catch (e: Exception)` without rethrowing `CancellationException` **catches the cancellation signal and swallows it**!
  - The coroutine **refuses to die**, continuing its background loops and retaining references to destroyed ViewModels, Composables, and Contexts $\implies$ **Silent Memory Leak + Battery Drain**!
* **What happens if you DO rethrow:** The coroutine terminates immediately and the Garbage Collector reclaims all memory.

#### ❌ WRONG (Swallows Cancellation -> Leaks Memory):
```kotlin
// ❌ WRONG: Catches generic Exception, preventing coroutine from cancelling!
viewModelScope.launch {
    while (true) {
        try {
            delay(1000)
            val data = api.fetchData()
            updateUI(data) // 💥 Retains Composable reference after screen disposal!
        } catch (e: Exception) { // 💥 Intercepts CancellationException and ignores it!
            Timber.e(e, "Error occurred")
            // While loop continues running in background forever!
        }
    }
}
```

#### ✅ RIGHT (Preserves Cancellation):
```kotlin
// ✅ RIGHT: Checks and rethrows CancellationException!
viewModelScope.launch {
    while (isActive) {
        try {
            delay(1000)
            val data = api.fetchData()
            updateUI(data)
        } catch (e: Exception) {
            if (e is CancellationException) throw e // 🚀 Preserves coroutine cancellation!
            Timber.e(e, "Network error")
        }
    }
}
```

---

### Point 5: Reactive Flow Pipelines (`.catch {}` vs Downstream `try-catch`)

* **When to use:** When managing streams of data from Room databases, DataStore, or WebSocket feeds.
* **What happens if you use `try-catch` inside Flow producers:** Violates **Exception Transparency** and breaks upstream operators.
* **What happens if you use Flow `.catch {}`:** Downstream operators catch all upstream exceptions cleanly and can emit fallback values or trigger retries.

#### ❌ WRONG (Violates Exception Transparency):
```kotlin
// ❌ WRONG: Catching inside flow builder emits values in broken state
fun getArticles(): Flow<List<Article>> = flow {
    try {
        val articles = api.getArticles()
        emit(articles)
    } catch (e: Exception) {
        emit(emptyList()) // ⚠️ Violates Flow Exception Transparency!
    }
}
```

#### ✅ RIGHT (Using `.catch {}` and `.retryWhen {}`):
```kotlin
// ✅ RIGHT: Declarative Flow error handling & exponential backoff
fun getArticlesStream(): Flow<List<Article>> {
    return articleDao.observeArticles()
        .map { entities -> transformToUiModel(entities) }
        .retryWhen { cause, attempt ->
            if (cause is IOException && attempt < 3) {
                delay(1000L * (attempt + 1)) // 1s, 2s, 3s backoff
                true // Retry stream!
            } else {
                false // Forward error to .catch
            }
        }
        .catch { exception ->
            Timber.e(exception, "Database/Network stream failure")
            emit(emptyList()) // ✅ Safe fallback emission
        }
}
```

---

## ⚡ 4. Summary Decision Flowchart

```
Where is the error happening?
│
├── 1. Inside a single ViewModel operation (`launch`)?
│      └── 👉 USE: `try-catch` with `if (e is CancellationException) throw e`
│
├── 2. Across 2+ parallel background requests (`async`)?
│      └── 👉 USE: `supervisorScope { val d1 = async { ... }; val d2 = ... }`
│
├── 3. At the Root level for global crash logging/telemetry?
│      └── 👉 USE: `CoroutineExceptionHandler` attached to Root Scope with `SupervisorJob()`
│
└── 4. Inside a reactive stream pipeline (Room DB / WebSocket)?
       └── 👉 USE: Flow `.catch { emit(fallback) }` and `.retryWhen { ... }`
```

---

## 🔗 Related Documentation & References

- **Coroutine Scopes Decision Guide**: [`when_to_use_coroutine_scopes.md`](when_to_use_coroutine_scopes.md)
- **Coroutines Dispatchers Types**: [`../beginner/dispatchers_types.md`](../beginner/dispatchers_types.md)
- **Execution Order Quiz**: [`coroutines_execution_order_quiz.md`](coroutines_execution_order_quiz.md)
- **Memory Leak Detection**: [`../advanced/memory_leak_detection.md`](../advanced/memory_leak_detection.md)
