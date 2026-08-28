package com.cruise.apm.internal.vitals

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import com.cruise.apm.model.SystemVitals

/**
 * Extracts live hardware, memory, battery, thermal, and network vitals.
 */
internal class VitalsCollector(
    private val context: Context,
    private val networkObserver: NetworkStateObserver
) {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    fun captureVitals(): SystemVitals {
        // 1. Heap Memory calculations
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val maxMemory = runtime.maxMemory()

        val usedBytes = totalMemory - freeMemory
        val usedMb = (usedBytes.toDouble() / (1024.0 * 1024.0)).round2()
        val maxMb = (maxMemory.toDouble() / (1024.0 * 1024.0)).round2()
        val freeMb = (freeMemory.toDouble() / (1024.0 * 1024.0)).round2()
        val utilization = if (maxMemory > 0) ((usedBytes.toDouble() / maxMemory.toDouble()) * 100.0).round2() else 0.0

        // 2. Battery status
        val (batteryPct, isCharging) = getBatteryInfo()

        // 3. Thermal status
        val thermalStatus = getThermalStatus()

        // 4. Network status
        val networkType = networkObserver.getCurrentNetworkType()
        val isMetered = networkObserver.isCurrentlyMetered()

        // 5. Active threads
        val activeThreads = Thread.activeCount()

        return SystemVitals(
            usedHeapMb = usedMb,
            maxHeapMb = maxMb,
            freeHeapMb = freeMb,
            heapUtilizationPercent = utilization,
            batteryLevelPercent = batteryPct,
            isCharging = isCharging,
            networkType = networkType,
            isNetworkMetered = isMetered,
            thermalStatus = thermalStatus,
            activeThreadCount = activeThreads
        )
    }

    private fun getBatteryInfo(): Pair<Int, Boolean> {
        return try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val intent = context.registerReceiver(null, filter)
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val batteryPct = if (level >= 0 && scale > 0) {
                ((level.toFloat() / scale.toFloat()) * 100f).toInt()
            } else 100

            Pair(batteryPct, isCharging)
        } catch (e: Throwable) {
            Pair(100, false)
        }
    }

    private fun getThermalStatus(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            try {
                when (powerManager.currentThermalStatus) {
                    PowerManager.THERMAL_STATUS_NONE -> "NONE"
                    PowerManager.THERMAL_STATUS_LIGHT -> "LIGHT"
                    PowerManager.THERMAL_STATUS_MODERATE -> "MODERATE"
                    PowerManager.THERMAL_STATUS_SEVERE -> "SEVERE"
                    PowerManager.THERMAL_STATUS_CRITICAL -> "CRITICAL"
                    PowerManager.THERMAL_STATUS_EMERGENCY -> "EMERGENCY"
                    PowerManager.THERMAL_STATUS_SHUTDOWN -> "SHUTDOWN"
                    else -> "NORMAL"
                }
            } catch (e: Throwable) {
                "NORMAL"
            }
        } else {
            "NORMAL"
        }
    }

    private fun Double.round2(): Double {
        return (this * 100.0).toLong() / 100.0
    }
}
