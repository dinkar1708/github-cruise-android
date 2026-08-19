# API Call Patterns: Serial vs. Parallel Async vs. Separate Launch

This document explains the three API execution patterns implemented in [`UserRepoScreenViewModel.kt`](../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/userrepository/UserRepoScreenViewModel.kt).

---

## 1. Serial API Calls (`loadApiDataSerial`)

Calls execute **one after another** in a single coroutine. The second call waits for the first to complete.

```kotlin
fun loadApiDataSerial(login: String) = viewModelScope.launch {
    loadUserProfile(login)       // Call 1: Waits for completion (~1650ms)
    loadUserRepositories()       // Call 2: Starts only after #1 finishes (~320ms)
}
// Total time: time1 + time2 (~1970ms)
```

- **When to use**: Only when API #2 strictly depends on the result of API #1 (e.g. login token before profile fetch).

---

## 2. Parallel via Async / Await (`loadApiDataParallelAsync`)

Calls execute **simultaneously** inside a single coroutine using `async` and wait for both via `await()`.

```kotlin
fun loadApiDataParallelAsync(login: String) = viewModelScope.launch {
    val profile = async { loadUserProfile(login) }
    val repos = async { loadUserRepositories() }

    profile.await()
    repos.await()
}
// Total time: max(time1, time2) (~430ms) -> ~40% faster
```

- **When to use**: When you need data from **both** independent APIs before computing a final combined result.

---

## 3. Parallel via Separate `launch` (`loadApiDataParallelSeparateLaunch`)

Calls execute in **two independent coroutines**. Each coroutine updates its own UI state immediately when ready.

```kotlin
fun loadApiDataParallelSeparateLaunch(login: String) {
    // 🚀 Coroutine 1: Profile renders in ~250ms
    viewModelScope.launch { loadUserProfile(login) }

    // 🚀 Coroutine 2: Repositories render in ~430ms
    viewModelScope.launch { loadUserRepositories() }
}
```

- **When to use**: **Industry Standard for multi-section UI (Progressive Loading)**.
  - Profile card displays immediately in 250ms without waiting for the repository list.
  - Failure in one call does not cancel or delay the other.

---

## Benchmark Log Comparison

```
1. SERIAL (loadApiDataSerial):
   loadUserProfile - START -> END (1654ms)
   loadUserRepositories - START -> END (318ms)
   Total Time: 1972ms (Serial)

2. PARALLEL (loadApiDataParallelSeparateLaunch / Async):
   loadUserProfile - START (12:55:37.961)
   loadUserRepositories - START (12:55:37.963)  <- 2ms gap (Simultaneous)
   loadUserProfile - END (259ms)
   loadUserRepositories - END (431ms)
   Total Time: ~433ms (~40% faster than serial)
```

---

## Code Reference

- **ViewModel**: [`UserRepoScreenViewModel.kt:L45-L115`](../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/userrepository/UserRepoScreenViewModel.kt#L45-L115)
- **UI Screen**: [`UserRepoScreen.kt:L65-L68`](../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/userrepository/UserRepoScreen.kt#L65-L68)
