# Kotlin Coroutines Dispatchers Guide: Types & In-Short Use Cases

**Staff-Level Architecture Guide: Understanding Thread Pools, Thread Confinement, and Workload Optimization in Android**

📖 **Official Kotlin Documentation:** [Kotlin Coroutines: Coroutine Dispatchers](https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html)

---

## 🧭 Master Dispatchers Comparison Matrix

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       COROUTINE DISPATCHERS AT A GLANCE                     │
├───────────────────────┬─────────────────────────┬───────────────────────────┤
│   🚀 DISPATCHER       │   🧵 THREAD POOL SIZE   │   🎯 OPTIMIZED FOR        │
├───────────────────────┼─────────────────────────┼───────────────────────────┤
│  Dispatchers.Main     │  1 (Main UI Thread)     │  UI updates & Compose     │
│  Dispatchers.IO       │  Elastic (Max 64)       │  Disk & Network Blocking  │
│  Dispatchers.Default  │  Fixed (CPU Core Count) │  Heavy Calculations/JSON  │
│  Dispatchers.Unconfined│ Non-confined (Random)  │  Unit tests & Lock-free   │
│  SingleThreadContext  │  1 Dedicated BG Thread  │  Sequential Mutex Safety  │
└───────────────────────┴─────────────────────────┴───────────────────────────┘
```

---

## 🛠️ The 5 Dispatcher Types & In-Short Use Cases

### 1. `Dispatchers.Main` & `Dispatchers.Main.immediate`
* **Thread:** Single Android Main Thread (UI Looper).
* **Behavior:** Dispatches execution to the Android UI message queue.
* **`Main.immediate` Advantage:** If already running on the Main Thread, it executes **immediately** without waiting for the next Looper loop frame (saving UI render time).
* **🎯 Top Use Cases (In-Short):**
  - Updating Compose `State` or `StateFlow`
  - Showing `Snackbar`, `Toast`, or navigating screens
  - Triggering UI animations and View visibility changes

```kotlin
// In-Short Example:
withContext(Dispatchers.Main.immediate) {
    _uiState.value = UiState.Success(data)
}
```

---

### 2. `Dispatchers.IO` (Network & Disk Operations)
* **Thread Pool:** Elastic on-demand pool (defaults to **64 threads** or CPU core count, whichever is larger).
* **Behavior:** Sized for threads that spend most of their time **waiting/blocked** on hardware I/O rather than consuming CPU.
* **🎯 Top Use Cases (In-Short):**
  - **Room Database / SQLite:** Queries, inserts, and database migrations
  - **Network Requests:** Retrofit, OkHttp, downloading files
  - **File System:** Reading/writing local files, DataStore, SharedPreferences
  - **Image I/O:** Reading cached raw bytes from disk into Coil/Glide

```kotlin
// In-Short Example:
val cachedArticles = withContext(Dispatchers.IO) {
    roomDatabase.articleDao().getAllArticles()
}
```

---

### 3. `Dispatchers.Default` (CPU-Intensive Work)
* **Thread Pool:** Fixed pool sized **strictly to the number of CPU cores** (minimum 2, typically 4–8 cores).
* **Behavior:** Sized for tasks that saturate CPU cores at 100%. Creating more threads than CPU cores causes expensive OS thread context switching.
* **🎯 Top Use Cases (In-Short):**
  - **JSON Serialization:** Parsing a 5MB JSON payload via Moshi / Kotlinx Serialization
  - **Image Processing:** Bitmap cropping, resizing, color matrix filtering
  - **Data Processing:** Sorting 50,000 items, searching, filtering large lists
  - **Cryptography:** AES-256 encryption/decryption, SHA-256 hashing

```kotlin
// In-Short Example:
val parsedModel = withContext(Dispatchers.Default) {
    moshi.adapter(LargeFeedResponse::class.java).fromJson(rawJsonString)
}
```

---

### 4. `Dispatchers.Unconfined` (No Specific Thread)
* **Thread:** Starts in the caller's thread; after suspension, resumes in whichever thread the suspending function finished on.
* **Behavior:** Non-deterministic! Never confines execution to any specific pool.
* **🎯 Top Use Cases (In-Short):**
  - **Unit Testing:** Fast synchronous test execution without thread hopping
  - Low-overhead lock-free event pipelines where thread context does not matter
  - ⚠️ **Avoid in Production UI code** (can accidentally run heavy work on Main Thread!).

```kotlin
// In-Short Example (Unit Tests):
runTest(Dispatchers.Unconfined) {
    viewModel.onSearchQueryChanged("android")
    assertEquals("android", viewModel.queryState.value)
}
```

---

### 5. `newSingleThreadContext` / Dedicated Dispatcher (Thread Confinement)
* **Thread:** Exactly 1 dedicated background thread.
* **Behavior:** Guarantees strict sequential execution without mutexes or synchronized blocks.
* **🎯 Top Use Cases (In-Short):**
  - Legacy non-thread-safe C/C++ native libraries
  - Sequential hardware serial port / Bluetooth write queues

```kotlin
// In-Short Example:
val serialPortDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
withContext(serialPortDispatcher) {
    hardwarePort.write(packet)
}
```

---

## ⚡ Staff Architecture Decision Rules

| Workload Type | Correct Dispatcher | Why? |
| :--- | :--- | :--- |
| **Updating UI State** | `Dispatchers.Main.immediate` | Prevents ANRs and executes without Looper delay |
| **Room SQLite Queries** | `Dispatchers.IO` | Prevents disk I/O from blocking CPU pools |
| **Parsing 10,000 JSON items** | `Dispatchers.Default` | Saturates CPU cores without context-switch lag |
| **Reading File from Storage** | `Dispatchers.IO` | Uses elastic 64-thread I/O pool |
| **Bitmap Downsampling / Filter** | `Dispatchers.Default` | Heavy mathematical pixel math |

---

## 🔗 Related Documentation

- **Coroutines Basics**: [`coroutines_basics.md`](coroutines_basics.md)
- **Coroutines Scopes Guide**: [`../intermediate/when_to_use_coroutine_scopes.md`](../intermediate/when_to_use_coroutine_scopes.md)
- **Coroutines Error Handling**: [`../intermediate/coroutines_error_handling.md`](../intermediate/coroutines_error_handling.md)
- **Coroutines Execution Order Quiz**: [`../intermediate/coroutines_execution_order_quiz.md`](../intermediate/coroutines_execution_order_quiz.md)
