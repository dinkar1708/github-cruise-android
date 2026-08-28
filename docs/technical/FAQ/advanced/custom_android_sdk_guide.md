# CruiseAPM Custom Android SDK Architecture & Master Guide

A complete, production-grade guide to designing, developing, packaging, testing, and distributing custom Android SDKs, featuring **CruiseAPM (`cruise-apm-sdk`)**—an Application Performance Monitoring and Network Observability library.

---

## 1. SDK Overview & Architecture Principles

**CruiseAPM** is designed using industry-standard SDK best practices:

| Principle | Why It Matters | Implementation in `cruise-apm-sdk` |
|---|---|---|
| **Context Safety** | Retaining Activity references causes fatal memory leaks. | Strictly captures `context.applicationContext` in `initialize()`. |
| **API Encapsulation** | Prevents host apps from coupling to internal logic. | Marks internal engines (`ApmDispatcher`, `OfflineEventStore`, `VitalsCollector`, `AnrWatchdog`) as `internal`. |
| **Consumer ProGuard** | Ensures host app's R8 shrinking doesn't strip SDK entrypoints. | Ships `consumer-rules.pro` via `consumerProguardFiles`. |
| **Dedicated Concurrency** | Host app coroutines must not be blocked or starved by SDK telemetry. | Operates on a dedicated single-thread daemon worker (`ApmDispatcher`). |
| **Lock-Free Ingestion** | High-frequency events from multiple threads must never block UI. | Uses Kotlin `Channel<ApmEvent>(Channel.BUFFERED)` with Actor pattern. |
| **Offline Persistence** | Telemetry must survive process death or network disconnects. | Buffers events in atomic local JSON file spool, batch-flushing on reconnect. |

---

## 2. Library Build Configuration (`cruise-apm-sdk/build.gradle.kts`)

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

android {
    namespace = "com.cruise.apm"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.cruise.sdk"
                artifactId = "cruise-apm"
                version = "1.0.0"
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.logging.interceptor)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

---

## 3. Build & Packaging Commands

```bash
# Run unit tests:
./gradlew :cruise-apm-sdk:testReleaseUnitTest

# Assemble Release AAR library:
./gradlew :cruise-apm-sdk:assembleRelease

# Publish to local Maven repository (~/.m2/repository):
./gradlew :cruise-apm-sdk:publishToMavenLocal
```

**Output Artifact:**
`cruise-apm-sdk/build/outputs/aar/cruise-apm-sdk-release.aar`

---

## 4. Integration into Host Application

### Step 1: Add Dependency in `app/build.gradle.kts`
```kotlin
dependencies {
    // Direct multi-module project dependency:
    implementation(project(":cruise-apm-sdk"))

    // Or if consumed via Maven:
    // implementation("com.cruise.sdk:cruise-apm:1.0.0")
}
```

### Step 2: Initialize in `Application.onCreate()`
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

### Step 3: Attach OkHttp Interceptor for Automatic Network Tracing
```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(CruiseApmOkHttpInterceptor())
    .build()
```

### Step 4: Measure Custom Traces & Operations
```kotlin
val trace = CruiseApm.newTrace("fetch_user_repositories")
trace.start()
trace.putAttribute("query", "jetpack-compose")

// ... async work ...

val durationMs = trace.stop()
```

### Step 5: Live Hardware & System Vitals
```kotlin
val vitals = CruiseApm.getVitals()
println("Heap Used: ${vitals.usedHeapMb}MB / ${vitals.maxHeapMb}MB (${vitals.heapUtilizationPercent}%)")
println("Battery: ${vitals.batteryLevelPercent}% (Charging: ${vitals.isCharging})")
println("Network: ${vitals.networkType}")
```

---

## 5. Troubleshooting (Hilt & Gradle 9.x Integration)

### Issue: `Could not determine dependencies of task ':app:hiltJavaCompileDebug' (artifactType 'jar-for-dagger')`
When using **Gradle 9.x** with Dagger/Hilt and local project library modules, Gradle 9's strict variant resolution flags ambiguous transformation chains for `jar-for-dagger`.

### Solution:
In `app/build.gradle.kts`, set:
```kotlin
hilt {
    enableAggregatingTask = false
}
```
