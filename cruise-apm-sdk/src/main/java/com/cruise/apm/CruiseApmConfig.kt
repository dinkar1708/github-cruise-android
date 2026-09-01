package com.cruise.apm

import com.cruise.apm.model.SdkEnvironment

/**
 * Immutable configuration settings for CruiseAPM SDK initialization.
 *
 * @property apiKey Unique client application token or API Key.
 * @property environment Target backend environment ([SdkEnvironment.SANDBOX], [SdkEnvironment.STAGING], or [SdkEnvironment.PRODUCTION]).
 * @property enableNetworkMonitoring Controls whether HTTP network metrics are recorded.
 * @property enableAnrWatchdog Enables background watchdog for detecting main thread UI hangs.
 * @property anrTimeoutMs Freeze duration threshold before an ANR event is recorded (default 5000ms).
 * @property enableAutoLifecycleTracking Automatically records screen transitions and session intervals.
 * @property batchFlushSize Maximum in-memory events before automatic persistence flush.
 * @property enableLogging Controls whether debug diagnostic logs are emitted to Logcat.
 */
data class CruiseApmConfig(
    val apiKey: String,
    val environment: SdkEnvironment = SdkEnvironment.SANDBOX,
    val enableNetworkMonitoring: Boolean = true,
    val enableAnrWatchdog: Boolean = true,
    val anrTimeoutMs: Long = 5000L,
    val enableAutoLifecycleTracking: Boolean = true,
    val batchFlushSize: Int = 20,
    val enableLogging: Boolean = false
) {
    class Builder(private val apiKey: String) {
        private var environment: SdkEnvironment = SdkEnvironment.SANDBOX
        private var enableNetworkMonitoring: Boolean = true
        private var enableAnrWatchdog: Boolean = true
        private var anrTimeoutMs: Long = 5000L
        private var enableAutoLifecycleTracking: Boolean = true
        private var batchFlushSize: Int = 20
        private var enableLogging: Boolean = false

        fun setEnvironment(env: SdkEnvironment) = apply { this.environment = env }
        fun setNetworkMonitoringEnabled(enabled: Boolean) = apply { this.enableNetworkMonitoring = enabled }
        fun setAnrWatchdogEnabled(enabled: Boolean) = apply { this.enableAnrWatchdog = enabled }
        fun setAnrTimeoutMs(timeoutMs: Long) = apply { this.anrTimeoutMs = timeoutMs }
        fun setAutoLifecycleTrackingEnabled(enabled: Boolean) = apply { this.enableAutoLifecycleTracking = enabled }
        fun setBatchFlushSize(size: Int) = apply { this.batchFlushSize = size }
        fun setLoggingEnabled(enabled: Boolean) = apply { this.enableLogging = enabled }

        fun build(): CruiseApmConfig = CruiseApmConfig(
            apiKey = apiKey,
            environment = environment,
            enableNetworkMonitoring = enableNetworkMonitoring,
            enableAnrWatchdog = enableAnrWatchdog,
            anrTimeoutMs = anrTimeoutMs,
            enableAutoLifecycleTracking = enableAutoLifecycleTracking,
            batchFlushSize = batchFlushSize,
            enableLogging = enableLogging
        )
    }
}
