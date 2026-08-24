# Application Not Responding (ANR): Root Causes, Diagnosis & Architectural Fixes

## Purpose

A comprehensive **Staff Android Engineering Guide** on **Application Not Responding (ANR)** errors in Android. Explains the system watchdog thresholds, the top root causes, diagnostic tools (Play Vitals, `traces.txt`, StrictMode, Perfetto), and modern architectural patterns (Coroutines, Dispatchers, DataStore, App Startup) to permanently eliminate ANRs.

---

## 🛑 What is an ANR?

In Android, the **Main Thread (UI Thread)** runs an event loop (`Looper.loop()`) responsible for rendering UI frames at 60/120 fps, dispatching user touches, and executing lifecycle events.

If the Main Thread is blocked and cannot process a user event or system callback within the OS threshold, the Android Window Manager displays the **ANR Dialog** (*"App isn't responding - Close app / Wait"*), causing user churn and tanking Google Play Vitals.

---

## ⏱️ System ANR Timeout Thresholds

| Trigger Event | Timeout Threshold | Occurs When |
| :--- | :--- | :--- |
| 📱 **Input Dispatching** | **5 seconds** | User taps, scrolls, or presses a key, but the Main Thread hasn't finished processing previous messages. |
| 📡 **BroadcastReceiver** | **10s** (foreground) / **60s** (background) | `onReceive()` on the Main Thread takes too long to execute. |
| ⚙️ **Service Lifecycle** | **20s** (foreground) / **200s** (background) | `onCreate()`, `onStartCommand()`, or `onBind()` blocks the Main Thread. |
| 🗄️ **ContentProvider** | **10 seconds** | `onCreate()` or query publishing takes too long during startup. |

---

## 🔥 Top 5 Root Causes of ANRs

```
┌──────────────────────────────────────────────────────────────────────────────────────────┐
│                                 TOP CAUSES OF ANRs                                       │
│                                                                                          │
│ 1. 💽 Disk / DB I/O on Main Thread     ➔ Synchronous Room, SharedPreferences, File reads │
│ 2. 🌐 Network Calls on Main Thread     ➔ Synchronous HTTP/Socket calls                   │
│ 3. 🔒 Lock Contention / Deadlocks      ➔ Main thread blocked waiting for background lock │
│ 4. 🧠 Heavy CPU Calculations           ➔ Large JSON parsing, heavy RegEx, image resizing │
│ 5. 🚀 Bloated Application.onCreate()   ➔ 20+ heavy SDK initializations during cold start │
└──────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ How to Fix ANRs (Architectural Solutions)

### 1. Offload Heavy Work to Appropriate Coroutine Dispatchers

Never execute blocking operations on `Dispatchers.Main`. Route them to dedicated thread pools:

```kotlin
class UserRepositoryImpl @Inject constructor(
    private val api: GithubApiService,
    private val userDao: UserDao,
    private val jsonParser: Json
) : UserRepository {

    // 💽 Disk & Network I/O ➔ Dispatchers.IO (64+ on-demand thread pool)
    override suspend fun fetchUser(login: String): User = withContext(Dispatchers.IO) {
        val userDto = api.getUser(login)
        userDao.insertUser(userDto.toEntity())
        userDto.toDomain()
    }

    // 🧠 Heavy CPU / JSON Parsing ➔ Dispatchers.Default (Fixed to CPU core count)
    override suspend fun parseLargeRepoPayload(rawJson: String): List<Repository> = 
        withContext(Dispatchers.Default) {
            jsonParser.decodeFromString<List<RepositoryDto>>(rawJson).map { it.toDomain() }
        }
}
```

---

### 2. Replace Blocking `SharedPreferences` with `Jetpack DataStore`

`SharedPreferences.getXXX()` and `.apply()` can cause Main Thread disk stalls and memory locks during app startup. `DataStore` uses Kotlin Coroutines and Flow asynchronously:

```kotlin
// ❌ BAD: Synchronous blocking read on Main Thread
val token = sharedPreferences.getString("auth_token", null) // Can block Main Thread!

// ✅ GOOD: Asynchronous, non-blocking Flow read
val authTokenFlow: Flow<String?> = context.dataStore.data
    .map { preferences -> preferences[AUTH_TOKEN_KEY] }
    .flowOn(Dispatchers.IO)
```

---

### 3. Eliminate Lock Contention & Deadlocks

If a background thread holds a `synchronized(lock)` while doing I/O, and the Main Thread asks for the same lock, the Main Thread freezes!

```kotlin
// ❌ BAD: Main Thread blocks waiting for background thread to release lock
class BadTokenManager {
    private val lock = Any()

    fun getTokenOnMainThread(): String = synchronized(lock) {
        // Blocks UI Thread if background thread is currently refreshing token!
        return cachedToken
    }
}

// ✅ GOOD: Non-blocking Coroutines Mutex or StateFlow
class GoodTokenManager {
    private val mutex = Mutex()
    private val _tokenState = MutableStateFlow<String?>(null)
    val tokenState: StateFlow<String?> = _tokenState.asStateFlow()

    suspend fun refreshToken() = withContext(Dispatchers.IO) {
        mutex.withLock {
            val freshToken = api.fetchNewToken()
            _tokenState.value = freshToken
        }
    }
}
```

---

### 4. Optimize `Application.onCreate()` with App Startup / Lazy Init

Never initialize 15 analytics, logging, and crash SDKs sequentially inside `Application.onCreate()`:

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 1. Critical Crash Reporting (Keep synchronous on Main Thread)
        Timber.plant(Timber.DebugTree())
        
        // 2. Non-critical SDKs ➔ Initialize asynchronously in background!
        CoroutineScope(Dispatchers.Default).launch {
            initHeavyAnalyticsSdk()
            initPaymentGatewaySdk()
            initAdNetworkSdk()
        }
    }
}
```

---

### 5. Use `goAsync()` in `BroadcastReceiver`

`BroadcastReceiver.onReceive()` runs directly on the Main Thread with a strict **10-second limit**:

```kotlin
class PushNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 🛑 Without goAsync(), doing DB/Network here causes ANR!
        val pendingResult = goAsync() // Tells OS this receiver will finish asynchronously
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Perform async work safely off Main Thread
                saveNotificationToDatabase(intent)
            } finally {
                pendingResult.finish() // Must call finish() when done!
            }
        }
    }
}
```

---

## 🔍 How to Diagnose and Debug ANRs

### 1. Read the `traces.txt` File
When an ANR occurs, Android OS dumps thread stack traces to `/data/anr/anr_*` or Google Play Vitals. Look for the `"main"` thread:

```
"main" prio=5 tid=1 Blocked
  | group="main" sCount=1 dsCount=0 flags=1 obj=0x72a81230 self=0x7c2b412000
  | sysTid=14205 nice=-10 cpm=0 sched=0/0 handle=0x7d2834b548
  | state=S schedstat=( 1234 5678 90 ) utm=12 stm=5 core=2 HZ=100
  | stack=0x7fd1234000-0x7fd1236000 stackSize=8MB
  at com.myapp.data.DatabaseManager.getUserSynchronously(DatabaseManager.kt:42)
  - waiting to lock <0x0a12b34> (a com.myapp.data.DatabaseManager) held by thread 18
```
👉 **Diagnosis:** `main` thread is `Blocked` waiting for lock `<0x0a12b34>` held by `thread 18` (Background Worker).

---

### 2. Enable `StrictMode` in Debug Builds
Catch Main Thread disk and network violations immediately during development:

```kotlin
class DebugApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .penaltyFlashScreen()
                    .build()
            )
        }
    }
}
```

---

### 3. Query `ApplicationExitInfo` (Android 11+ / API 30+)
Inspect past ANRs programmatically on app launch:

```kotlin
val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
val exitReasons = activityManager.getHistoricalProcessExitReasons(packageName, 0, 5)

for (reason in exitReasons) {
    if (reason.reason == ApplicationExitInfo.REASON_ANR) {
        val traceInputStream = reason.traceInputStream
        Timber.e("Found historical ANR trace: ${reason.description}")
    }
}
```

---

### 4. Capture ANRs & Custom Breadcrumb Logs in Firebase Crashlytics

Firebase Crashlytics (v18.2.4+) **automatically records ANRs** and allows you to attach **custom breadcrumb logs** and metadata to ANR reports:

```kotlin
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

// 1. Custom Breadcrumb Logging (Recorded in the last 64KB session buffer)
FirebaseCrashlytics.getInstance().log("User tapped Checkout button with 3 items")

// 2. Custom Key-Value State
FirebaseCrashlytics.getInstance().setCustomKey("screen_name", "UserProfileScreen")
FirebaseCrashlytics.getInstance().setCustomKey("is_offline_mode", true)

// 3. Production Timber Integration with Crashlytics
class CrashlyticsTimberTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Skip verbose and debug logs in production
        if (priority == android.util.Log.VERBOSE || priority == android.util.Log.DEBUG) return

        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log("[$tag] $message") // Attached to ANRs and Crash breadcrumbs!

        if (t != null) {
            crashlytics.recordException(t) // Non-fatal exception logging
        }
    }
}
```

---

## 📊 How to Check & Monitor ANRs in Google Play Console (Step-by-Step)

```
┌──────────────────────────────────────────────────────────────────────────────────────────────────┐
│                             GOOGLE PLAY CONSOLE ANR WORKFLOW                                     │
│                                                                                                  │
│ [Play Console] ➔ [Quality] ➔ [Android Vitals] ➔ [Crashes & ANRs] ➔ [ANRs Tab] ➔ [Cluster Detail]  │
└──────────────────────────────────────────────────────────────────────────────────────────────────┘
```

### 🧭 Navigation Steps:

1. **Log in to Google Play Console:**
   * Go to [https://play.google.com/console](https://play.google.com/console) and select your application.
2. **Navigate to Android Vitals:**
   * In the left sidebar navigation menu, scroll down to the **Quality** section.
   * Click on **Android Vitals** $\rightarrow$ Select **Crashes and ANRs**.
3. **Switch to ANRs Tab:**
   * Click on the **ANRs** tab at the top of the dashboard.
4. **Apply Analytical Filters:**
   * **App Version:** Filter by latest production rollout vs previous releases.
   * **Time Period:** Select `Last 7 days`, `Last 30 days`, or `Custom range`.
   * **Device Properties:** Filter by RAM (`< 2GB`, `2-4GB`, `> 4GB`), Android OS version, or Device SoC/OEM.
5. **Analyze ANR Clusters:**
   * ANRs are automatically grouped into clusters based on stack trace similarity.
   * Review **Users Affected (%)** and **Total Occurrences** to prioritize top impact bugs.
6. **Inspect Stack Trace & Thread Dumps:**
   * Click on any ANR cluster to open its **Details Page**.
   * View the **Main Thread Stack Trace** highlighting the exact file name and line number where the UI thread hung.
   * View **All Thread Traces** to identify background threads holding locks causing lock contention.
   * Click **Download trace** to download the raw text dump for offline debugging.

---

### 🚨 Google Play Vitals Bad Behavior Thresholds & Penalties:

Google Play strictly monitors ANR metrics across two critical thresholds:

| Metric | Google Play Bad Behavior Threshold | What it Measures |
| :--- | :--- | :--- |
| **Overall ANR Rate** | **0.47%** | Percentage of daily active users who experienced at least one ANR. |
| **User-Perceived ANR Rate** | **0.34%** | ANRs that occurred while the user was actively engaging with the app. |

> [!WARNING]
> **Store Ranking Penalties for Exceeding Thresholds:**  
> If your app exceeds the **0.47%** overall or **0.34%** user-perceived threshold:  
> 1. Google Play algorithms **suppress organic store discovery** and search rankings.  
> 2. The Play Store may display a public warning badge on your store listing for devices with high failure rates (*"Recent data shows this app may stop working on your device"*).

---

## 🎙️ Staff Interview Defense Script (30 Seconds)

> *"An ANR occurs when the Main Thread is blocked for more than 5 seconds for user inputs, 10 seconds for BroadcastReceivers, or 20 seconds for Services.  
> 
> The top root causes are Main Thread Disk/DB I/O, synchronous network calls, lock contention with background threads, and bloated `Application.onCreate()` initializations.  
> 
> We eliminate ANRs by:  
> 1. Routing Disk/DB/Network I/O to `Dispatchers.IO` and CPU tasks to `Dispatchers.Default`.  
> 2. Replacing synchronous SharedPreferences with asynchronous DataStore.  
> 3. Using non-blocking coroutine `Mutex` instead of `synchronized` locks.  
> 4. Checking Play Vitals regularly in Google Play Console (under Quality ➔ Android Vitals ➔ Crashes and ANRs) to keep our ANR rate safely below Google's 0.47% bad behavior threshold."*
