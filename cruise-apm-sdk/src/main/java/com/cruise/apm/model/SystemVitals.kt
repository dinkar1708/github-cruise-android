package com.cruise.apm.model

/**
 * Snapshot of real-time device health, memory, battery, and network status.
 *
 * @property usedHeapMb Heap memory currently occupied by the application process (in MB).
 * @property maxHeapMb Maximum heap memory available to the JVM process (in MB).
 * @property freeHeapMb Free memory remaining within the allocated heap (in MB).
 * @property heapUtilizationPercent Percent of total available heap in use (0.0 to 100.0).
 * @property batteryLevelPercent Device battery percentage (0 to 100).
 * @property isCharging Whether the device is currently plugged into power.
 * @property networkType Active network transport type (e.g. "WIFI", "CELLULAR_5G", "CELLULAR_4G", "OFFLINE").
 * @property isNetworkMetered Whether the current connection is metered (e.g. mobile data).
 * @property thermalStatus Hardware thermal throttling state (e.g. "NONE", "MODERATE", "SEVERE", "CRITICAL").
 * @property activeThreadCount Total active threads in the application process.
 * @property timestamp Epoch timestamp (ms) when this snapshot was captured.
 */
data class SystemVitals(
    val usedHeapMb: Double,
    val maxHeapMb: Double,
    val freeHeapMb: Double,
    val heapUtilizationPercent: Double,
    val batteryLevelPercent: Int,
    val isCharging: Boolean,
    val networkType: String,
    val isNetworkMetered: Boolean,
    val thermalStatus: String,
    val activeThreadCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
