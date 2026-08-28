# CruiseAPM Android SDK (`cruise-apm-sdk`)

A production-grade, enterprise-ready **Mobile Application Performance Monitoring (APM) and Telemetry SDK** for Android.

`CruiseAPM` provides zero-configuration network observability, custom execution trace timers, live system diagnostics (RAM, Battery, Thermal, Network), Main Looper ANR detection, and resilient offline batch queuing.

---

## 🚀 Key Features

- **🌐 Non-Destructive Network Interceptor**: `CruiseApmOkHttpInterceptor` automatically tracks latency, HTTP status codes, error rates, and payload byte sizes without mutating response streams.
- **⏱️ Trace Timing Engine**: `CruiseApm.newTrace("cold_start")` with monotonic nanosecond precision and custom key-value metadata attributes.
- **📊 Real-Time System Vitals**: Continuous snapshots of JVM Heap utilization (Used/Max/Free MB), Battery level & charging state, Thermal throttling flags, and Network transport types (`WIFI`, `CELLULAR`, `OFFLINE`).
- **🐕 Main Looper ANR Watchdog**: Background heartbeat thread posting to `Looper.getMainLooper()` to detect and capture UI thread hangs ($>5000\text{ ms}$) without crashing the host app.
- **💾 Resilient Offline Event Store**: Thread-safe file spooling buffer that preserves telemetry during network disconnects or process death.
- **⚡ Reactive Kotlin Concurrency**: Dedicated daemon worker coroutine dispatcher, `StateFlow` vitals streams, and `SharedFlow` event ingestion with Kotlin Channels.
- **🛡️ Defensive Crash Sandbox**: All internal entrypoints isolated with `runCatching`—an SDK error **never** crashes the host application.

---

## 📦 Quickstart & Integration

### 1. Add Dependency in Host `app/build.gradle.kts`
```kotlin
dependencies {
    implementation(project(":cruise-apm-sdk"))
}
```

### 2. Initialize in `Application.onCreate()`
```kotlin
package com.jetpack.compose.github.github.cruise

import android.app.Application
import com.cruise.apm.CruiseApm
import com.cruise.apm.CruiseApmConfig
import com.cruise.apm.model.SdkEnvironment

class GithubCruiseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        CruiseApm.initialize(
            context = this,
            config = CruiseApmConfig.Builder(apiKey = "ghc_live_token_12345")
                .setEnvironment(if (BuildConfig.DEBUG) SdkEnvironment.SANDBOX else SdkEnvironment.PRODUCTION)
                .setNetworkMonitoringEnabled(true)
                .setAnrWatchdogEnabled(true)
                .setLoggingEnabled(BuildConfig.DEBUG)
                .build()
        )
    }
}
```

### 3. Attach OkHttp Interceptor for Network Observability
```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(CruiseApmOkHttpInterceptor())
    .build()
```

### 4. Measure Custom Traces & Operations
```kotlin
val trace = CruiseApm.newTrace("search_repositories")
trace.start()
trace.putAttribute("query", "compose")

// ... perform async repository fetch ...

trace.stop()
```

### 5. Stream Real-Time System Vitals in Compose
```kotlin
@Composable
fun DashboardScreen() {
    val vitals by CruiseApm.getVitalsStream().collectAsState()

    Text("Heap Usage: ${vitals?.heapUtilizationPercent}%")
    Text("Battery: ${vitals?.batteryLevelPercent}% (Charging: ${vitals?.isCharging})")
    Text("Network: ${vitals?.networkType}")
}
```

---

## 🛠️ Build, Test & Publish

```bash
# Run SDK Unit Tests
./gradlew :cruise-apm-sdk:testReleaseUnitTest

# Build Release .aar library package
./gradlew :cruise-apm-sdk:assembleRelease

# Publish to Local Maven (~/.m2/repository)
./gradlew :cruise-apm-sdk:publishToMavenLocal
```

Output Artifact:
`cruise-apm-sdk/build/outputs/aar/cruise-apm-sdk-release.aar`

---

## ✅ What Was Accomplished

- [x] **Modular Library Architecture**: Standalone `:cruise-apm-sdk` library module with `minSdk 21`, `compileSdk 34`, ProGuard keep rules, and Maven publishing metadata.
- [x] **Zero DI Framework Bloat**: Pure Kotlin coroutines and channels without hard Dagger/Hilt dependencies inside the SDK, ensuring 100% compatibility with any host app.
- [x] **Network Observability Engine**: `CruiseApmOkHttpInterceptor` capturing round-trip latency (`ms`), status codes, payload bytes, and network exceptions.
- [x] **Monotonic Execution Tracing**: Monotonic nanosecond timer (`ApmTrace`) for measuring operation durations across threads and screen navigations.
- [x] **Real-Time Hardware Vitals**: Live monitoring of JVM heap utilization, battery percentage, charging state, thermal flags, and network connectivity transitions (`WIFI`/`CELLULAR`/`OFFLINE`).
- [x] **Main Looper ANR Watchdog**: Background heartbeat thread posting to Main Looper detecting UI freezes ($>5000\text{ ms}$) with stack traces.
- [x] **L1/L2 Multi-Level Offline Caching**: L1 in-memory buffered `Channel` + L2 atomic disk file spool (`events_spool.log`) surviving process termination and network disconnects.
- [x] **Host App Hilt DI & Demo UI**: Provided via `ApmModule.kt` in the host app and demonstrated in [`CruiseApmExampleScreen.kt`](../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/beginner/CruiseApmExampleScreen.kt).
- [x] **Automated Verification**: Comprehensive unit test suite with 100% pass rate.

---

## 🔮 Future Roadmap & Potential Enhancements

1. **🎨 Jetpack Compose Jank & Dropped Frame Tracker**:
   - Integrate `Window.OnFrameMetricsAvailableListener` and AndroidX `JankStats` to detect frame rendering drops ($>16.6\text{ ms}$) during lazy list scrolling.
2. **💥 Fatal Crash & Uncaught Exception Handler**:
   - Chain `Thread.UncaughtExceptionHandler` to capture uncaught fatal crashes and spool stack traces alongside breadcrumb navigation logs.
3. **📦 Gzip File Compression**:
   - Gzip-compress offline disk spool files before transmitting to backend servers to reduce mobile data consumption by up to ~80%.
4. **🌐 OpenTelemetry (OTel) Standard Compatibility**:
   - Support OpenTelemetry Protocol (OTLP) payload exports to enable plug-and-play streaming to Prometheus, Grafana, Datadog, or Honeycomb.
5. **⚡ Cold/Warm App Startup Timers (TTID / TTFD)**:
   - Automated Cold Start (`Time to Initial Display`) and Warm Start (`Time to Full Display`) measurements using `Activity.reportFullyDrawn()`.
6. **📶 Network Bandwidth & Jitter Estimation**:
   - Calculate moving-average network throughput (Kbps) to dynamically adapt video/image quality based on real-time mobile network quality.
