# Standalone Mobile Location SDK & Data Ingestion Pipeline Guide

**Staff-Level Architecture Guide: Mobile SDK Engineering, Zero-Crash Isolation, Battery Adaptive Duty-Cycling, and Cloud DWH Batch Ingestion**

📖 **Interactive Demo Reference**: [`LocationSdkPipelineScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/advanced/location/LocationSdkPipelineScreen.kt)  
🧭 **Navigation Route**: `SamplesDestinations.LOCATION_SDK_PIPELINE_ROUTE`

---

## 🧭 Executive Summary

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                 MOBILE LOCATION SDK & DATA INGESTION PIPELINE               │
├─────────────────────────┬─────────────────────────┬─────────────────────────┤
│ 1. MODULAR SDK DESIGN   │ 2. ADAPTIVE DUTY-CYCLE  │ 3. HIGH-THROUGHPUT DWH  │
│  - Strict Encapsulation │  - Activity Recognition │  - Local SQLite Queue   │
│  - Zero-Crash Barrier   │  - 85%+ Battery Savings │  - GZIP Batch Ingestion │
└─────────────────────────┴─────────────────────────┴─────────────────────────┘
```

---

## 🏛️ 1. Standalone Mobile SDK Architecture & Clean API Design

### 1. The Public Initialization Pattern
The host application initializes the SDK once in its `Application.onCreate()`:

```kotlin
// Public API in SDK:
class LocationSdk private constructor(private val config: LocationSdkConfig) {

    companion object {
        @Volatile
        private var instance: LocationSdk? = null

        @JvmStatic
        fun initialize(context: Context, config: LocationSdkConfig) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        // 🔒 PREVENT MEMORY LEAK: Always extract applicationContext
                        val appContext = context.applicationContext
                        instance = LocationSdk(config).apply { startInternal(appContext) }
                    }
                }
            }
        }

        @JvmStatic
        fun getInstance(): LocationSdk = instance
            ?: throw IllegalStateException("LocationSdk must be initialized prior to calling getInstance()")
    }
}
```

### 2. Core SDK Engineering Rules:
* **🔒 Strict Internal Encapsulation:** All internal classes, Room DAOs, and network dispatchers are marked `internal` in Kotlin so they never pollute the host app namespace.
* **🛡️ Zero-Crash Guarantee:** The host application's stability is paramount. All public SDK entry points are wrapped with internal crash barriers. An SDK error **never** crashes the host app.
* **📜 Consumer Proguard Rules (`consumer-rules.pro`):** Shipped directly inside the `.aar` library bundle so host applications automatically preserve required telemetry models during R8/Proguard minification.
* **📦 Minimal Binary Footprint:** Zero heavy 3rd-party transitive dependencies to keep the compiled `.aar` binary footprint under $350\text{KB}$.

---

## 🔋 2. Battery-Optimized Adaptive Duty-Cycling

Continuous GPS polling at maximum frequency drains the device battery at $\approx 18-25\%/\text{hour}$.

### The Adaptive Activity-Driven Strategy
By coupling `FusedLocationProviderClient` with the Google `ActivityRecognitionClient`, the SDK dynamically throttles location hardware:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ADAPTIVE DUTY-CYCLE STATE MACHINE                        │
├───────────────┬──────────────────┬───────────────────────┬──────────────────┤
│ User Activity │ GPS Interval     │ Trigger Mechanism     │ Battery Drain    │
├───────────────┼──────────────────┼───────────────────────┼──────────────────┤
│ 🧍 STILL      │ 120s – 300s      │ Accelerometer Rest    │ ~0.6% / hour     │
│ 🚶 WALKING    │ 30s              │ Step Detector / AR    │ ~1.8% / hour     │
│ 🚗 IN_VEHICLE │ 5s               │ High Speed Transition │ ~4.2% / hour     │
└───────────────┴──────────────────┴───────────────────────┴──────────────────┘
```

---

## ☁️ 3. High-Throughput Batch Ingestion & Cloud DWH Pipeline

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                  HIGH-THROUGHPUT OFFLINE-FIRST PIPELINE                     │
├─────────────────────────────────────────────────────────────────────────────┤
│ Location Ping ──▶ SQLite / Room Queue Buffer                                │
│                          │                                                  │
│               Threshold: 50 records OR 15 seconds                           │
│                          │                                                  │
│                          ▼                                                  │
│               GZIP Binary Compression (75% bandwidth reduction)             │
│                          │                                                  │
│                          ▼                                                  │
│               HTTPS POST /ingest/v1/telemetry-batch                         │
│                          │                                                  │
│                          ▼                                                  │
│               Cloud Gateway (Kinesis / Firehose / S3)                        │
│                          │                                                  │
│                          ▼                                                  │
│               DWH / Snowflake Analytics Tables                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Snowflake / DWH Ingestion Event Schema
```json
{
  "event_id": "9a8f21b0",
  "device_id": "anon_sha256_e4b18c9...",
  "timestamp_epoch_ms": 1723618400120,
  "lat": 35.68124,
  "lng": 139.76712,
  "accuracy_meters": 5.0,
  "activity_type": "WALKING",
  "speed_kmh": 4.8,
  "sdk_version": "2.4.0",
  "battery_pct": 88
}
```

---

## 🔗 Related References & Code

- **Interactive Screen**: [`LocationSdkPipelineScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/advanced/LocationSdkPipelineScreen.kt)
- **ViewModel Implementation**: [`LocationSdkPipelineViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/advanced/LocationSdkPipelineViewModel.kt)
- **BLE Beacon Scanning Guide**: [`ble_beacon_scanning_guide.md`](ble_beacon_scanning_guide.md)
- **WorkManager Background Processing**: [`../intermediate/workmanager_background_sync.md`](../intermediate/workmanager_background_sync.md)
