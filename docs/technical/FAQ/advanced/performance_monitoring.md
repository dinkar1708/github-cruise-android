# Performance & Network Monitoring in Android/Compose

## Purpose

Learn how to monitor, debug, and optimize performance in Android applications across **UI Rendering (FPS/Recomposition)**, **Memory/CPU**, and **Network Usage (API Calls & Payloads)**.

---

## 1. Network Usage & API Call Monitoring

Monitoring network traffic helps identify slow endpoints, oversized payloads, duplicate requests, and battery-draining network calls.

### A. Android Studio Network Inspector (Recommended)
**Access**: *View → Tool Windows → App Inspection → Network Inspector*

```
Timeline View:
[12:00:01.200]  GET https://api.github.com/users/octocat           200 OK  (1.4 KB)  180ms ───┐
[12:00:01.202]  GET https://api.github.com/users/octocat/repos     200 OK  (42 KB)   410ms ─────────┐
```

- **Features**:
  - Real-time stream of all HTTP/HTTPS requests.
  - Inspect Request/Response headers, JSON bodies, and status codes.
  - View call stacks showing which ViewModel / Coroutine triggered the network call.
  - Measure round-trip latency waterfalls.

---

### B. OkHttp Logging Interceptor (Codebase Implementation)
In our codebase, [`NetworkDataSourceModule.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/di/NetworkDataSourceModule.kt#L41) installs `HttpLoggingInterceptor` to log full request and response bodies directly to Logcat:

```kotlin
@Provides
@Singleton
fun provideNetworkDataSource(
    moshi: Moshi,
    tokenManager: SecureTokenManager
): NetworkDataSource {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(RetryInterceptor())
        .addInterceptor(ApiInterceptor(moshi, tokenManager))
        .build()
    // ...
}
```

#### Real Logcat Output:
```
--> GET https://api.github.com/users/octocat
--> END GET
<-- 200 OK https://api.github.com/users/octocat (259ms)
<-- Content-Type: application/json; charset=utf-8
<-- Content-Length: 1420
{ "login": "octocat", "id": 583231, "public_repos": 8 ... }
<-- END HTTP (1420-byte body)
```

---

### C. Measuring API Latencies in ViewModels
Track execution durations and compare serial vs. parallel API calls using timestamps:

```kotlin
val startTime = System.currentTimeMillis()
val userProfile = userRepositoryUseCase.getUserProfile(login).singleOrNull()
val duration = System.currentTimeMillis() - startTime
Timber.d("loadUserProfile - END (${duration}ms)")
```
- See real benchmark logs and comparisons in [API_CALL_PATTERNS.md](../../../technical/API_CALL_PATTERNS.md) and [`UserRepoScreenViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/userrepository/UserRepoScreenViewModel.kt).

---

## 2. UI & Recomposition Monitoring

Target: **60 FPS** (16.67ms/frame) or **120 FPS** (8.33ms/frame) without dropped frames.

### A. Layout Inspector
**Access**: *Tools → Layout Inspector*
- Enable **"Show Recomposition Counts"**.
- Blue/Normal = Good.
- Red/Yellow with high counts = Bad (unnecessary recompositions during scrolling).

### B. Compose Compiler Stability Reports
Check if your state classes and models are marked as stable/skippable by the compiler.
- Always use `@Immutable` for UI state classes with lists (e.g. [`RepoDetailsState.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/repodetails/RepoDetailsState.kt)).

---

## 3. CPU & Memory Profiling

**Access**: *View → Tool Windows → Profiler*

| Profiler | What It Monitors | Key Metric to Watch |
| :--- | :--- | :--- |
| **CPU Profiler** | Main thread vs Background threads | UI thread blocking operations (> 16ms) |
| **Memory Profiler** | Heap allocations & garbage collection churn | Memory leaks, uncollected Bitmaps/ViewModels |
| **Energy Profiler** | Battery drain from radios and CPU | Continuous background wake locks or polling |

---

## Quick Performance Checklist

```
✓ Network: Payload sizes minimized; independent calls run in parallel (async / separate launch)
✓ Network: Logging interceptor enabled in Debug builds for inspection
✓ Compose: LazyColumn items use stable keys (key = { it.id }) and contentType
✓ Compose: UI state classes marked with @Immutable
✓ Compose: Derived calculations wrapped in remember / derivedStateOf
✓ Coroutines: Network operations dispatched on Dispatchers.IO (off the Main thread)
```

---

## Code Reference & Interactive Demo

- **Network Module & Interceptors**: [`NetworkDataSourceModule.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/di/NetworkDataSourceModule.kt)
- **Serial vs Parallel API Benchmarks**: [API_CALL_PATTERNS.md](../../../technical/API_CALL_PATTERNS.md) & [`UserRepoScreenViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/userrepository/UserRepoScreenViewModel.kt)
- **Interactive Performance Sample Screen**: [`PerformanceMonitoringScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/advanced/PerformanceMonitoringScreen.kt)
- **Interactive Memory Leak Sample Screen**: [`MemoryLeakExamplesScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/advanced/MemoryLeakExamplesScreen.kt)
