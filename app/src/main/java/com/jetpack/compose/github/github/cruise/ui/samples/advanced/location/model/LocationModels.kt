package com.jetpack.compose.github.github.cruise.ui.samples.advanced.location.model

import java.util.UUID

/**
 * High-throughput location & sensor telemetry record (Snowflake / DWH ingestion schema)
 */
data class LocationTelemetryRecord(
    val eventId: String = UUID.randomUUID().toString().take(8),
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val activityType: String, // STILL, WALKING, IN_VEHICLE
    val speedKmh: Float,
    val batteryPercent: Int,
    val payloadSizeBytes: Int = 142
)

/**
 * Location SDK State
 */
data class LocationSdkUiState(
    val isSdkInitialized: Boolean = false,
    val isTrackingActive: Boolean = false,
    val sdkVersion: String = "2.4.0-stable",
    val apiKey: String = "sdk_live_k8s_9a87f1c4...",
    val currentActivity: String = "STILL", // STILL, WALKING, IN_VEHICLE
    val currentSamplingIntervalSec: Int = 120,
    val estimatedBatteryDrainPerHour: Double = 0.6, // in percent
    val localBufferQueue: List<LocationTelemetryRecord> = emptyList(),
    val uploadedBatchesCount: Int = 0,
    val totalRecordsIngested: Int = 0,
    val isSyncingBatch: Boolean = false,
    val terminalLogs: List<String> = emptyList()
)
