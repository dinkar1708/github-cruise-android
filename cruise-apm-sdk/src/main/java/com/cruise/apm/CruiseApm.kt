package com.cruise.apm

import android.app.Application
import android.content.Context
import android.util.Log
import com.cruise.apm.internal.dispatch.ApmDispatcher
import com.cruise.apm.internal.dispatch.EventBufferChannel
import com.cruise.apm.internal.lifecycle.ApmLifecycleTracker
import com.cruise.apm.internal.storage.OfflineEventStore
import com.cruise.apm.internal.vitals.NetworkStateObserver
import com.cruise.apm.internal.vitals.VitalsCollector
import com.cruise.apm.internal.watchdog.AnrWatchdog
import com.cruise.apm.model.ApmEvent
import com.cruise.apm.model.ApmResult
import com.cruise.apm.model.NetworkMetric
import com.cruise.apm.model.SystemVitals
import com.cruise.apm.trace.ApmTrace
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Public Singleton Entry Point for CruiseAPM (Application Performance Monitoring) SDK.
 *
 * Provides real-time network observability, trace performance timers, hardware vitals,
 * ANR watchdog detection, and resilient offline batch queuing.
 *
 * Example:
 * ```kotlin
 * // 1. Initialize at App Startup
 * CruiseApm.initialize(
 *     context = applicationContext,
 *     config = CruiseApmConfig.Builder("apiKey_123")
 *         .setEnvironment(SdkEnvironment.PRODUCTION)
 *         .setLoggingEnabled(true)
 *         .build()
 * )
 *
 * // 2. Measure Custom Trace Durations
 * val trace = CruiseApm.newTrace("fetch_feed")
 * trace.start()
 * // ... do work ...
 * trace.stop()
 * ```
 */
object CruiseApm {
    private const val TAG = "CruiseApm"
    const val SDK_VERSION = "1.0.0"

    @Volatile
    private var isInitialized = false

    @Volatile
    private var config: CruiseApmConfig? = null

    private var appContext: Context? = null
    private var eventBuffer: EventBufferChannel? = null
    private var vitalsCollector: VitalsCollector? = null
    private var networkObserver: NetworkStateObserver? = null
    private var anrWatchdog: AnrWatchdog? = null
    private var lifecycleTracker: ApmLifecycleTracker? = null

    @Volatile
    private var currentUserId: String? = null

    @Volatile
    private var currentSessionId: String = UUID.randomUUID().toString()

    /**
     * Initializes CruiseAPM SDK.
     *
     * @param context Application context instance.
     * @param config Configuration parameters built with [CruiseApmConfig.Builder].
     */
    @JvmStatic
    @Synchronized
    fun initialize(context: Context, config: CruiseApmConfig) {
        val app = context.applicationContext as? Application ?: (context.applicationContext)
        this.appContext = context.applicationContext
        this.config = config

        val offlineStore = OfflineEventStore(context.applicationContext)
        val buffer = EventBufferChannel(
            batchSize = config.batchFlushSize,
            offlineStore = offlineStore,
            enableLogging = config.enableLogging
        )
        this.eventBuffer = buffer

        val networkObs = NetworkStateObserver(context.applicationContext).apply { start() }
        this.networkObserver = networkObs
        this.vitalsCollector = VitalsCollector(context.applicationContext, networkObs)

        // Setup auto lifecycle tracking
        if (config.enableAutoLifecycleTracking && app is Application) {
            lifecycleTracker?.let {
                app.unregisterActivityLifecycleCallbacks(it)
                app.unregisterComponentCallbacks(it)
            }

            val tracker = ApmLifecycleTracker(
                enableLogging = config.enableLogging,
                onScreenResumed = { screenName ->
                    logEvent(
                        category = "SCREEN",
                        name = "screen_resumed",
                        attributes = mapOf("screen_name" to screenName)
                    )
                },
                onSessionStateChanged = { isForeground ->
                    if (isForeground) {
                        currentSessionId = UUID.randomUUID().toString()
                        logEvent(
                            category = "SESSION",
                            name = "session_start",
                            attributes = mapOf("session_id" to currentSessionId)
                        )
                    } else {
                        logEvent(
                            category = "SESSION",
                            name = "session_end",
                            attributes = mapOf("session_id" to currentSessionId)
                        )
                        // Trigger batch flush when entering background
                        flushEvents()
                    }
                },
                onLowMemoryWarning = { level ->
                    logEvent(
                        category = "VITALS",
                        name = "os_low_memory_warning",
                        attributes = mapOf("trim_level" to level)
                    )
                }
            )

            app.registerActivityLifecycleCallbacks(tracker)
            app.registerComponentCallbacks(tracker)
            this.lifecycleTracker = tracker
        }

        // Setup ANR Watchdog
        if (config.enableAnrWatchdog) {
            anrWatchdog?.stopWatchdog()
            val watchdog = AnrWatchdog(
                timeoutMs = config.anrTimeoutMs,
                onAnrDetected = { anrRecord ->
                    logEvent(
                        category = "ANR",
                        name = "main_thread_freeze",
                        durationMs = anrRecord.durationBlockedMs,
                        attributes = mapOf(
                            "stack_trace" to anrRecord.mainThreadStackTrace,
                            "threads_dump" to anrRecord.allThreadsDump
                        )
                    )
                    if (config.enableLogging) {
                        Log.e(TAG, "ANR Freeze detected on Main Thread (${anrRecord.durationBlockedMs}ms)")
                    }
                }
            )
            watchdog.startWatchdog()
            this.anrWatchdog = watchdog
        }

        this.isInitialized = true

        if (config.enableLogging) {
            Log.i(TAG, "CruiseAPM v$SDK_VERSION initialized in ${config.environment.name} mode.")
        }

        // Log initialization event
        logEvent(
            category = "SYSTEM",
            name = "apm_initialized",
            attributes = mapOf(
                "sdk_version" to SDK_VERSION,
                "environment" to config.environment.name
            )
        )
    }

    /**
     * Returns whether the SDK has been initialized.
     */
    @JvmStatic
    fun isInitialized(): Boolean = isInitialized

    /**
     * Returns current active configuration or null if not yet initialized.
     */
    @JvmStatic
    fun getConfig(): CruiseApmConfig? = config

    /**
     * Creates a new performance trace to measure execution duration of custom code blocks.
     *
     * @param name Unique name for the trace (e.g. "feed_data_load", "sql_query").
     */
    @JvmStatic
    fun newTrace(name: String): ApmTrace {
        return ApmTrace(name) { completedTrace ->
            logEvent(
                category = "TRACE",
                name = completedTrace.name,
                durationMs = completedTrace.getDurationMs(),
                attributes = completedTrace.getAttributes()
            )
        }
    }

    /**
     * Records an HTTP network request/response metric.
     */
    @JvmStatic
    fun recordNetworkMetric(metric: NetworkMetric) {
        if (config?.enableNetworkMonitoring == false) return
        logEvent(
            category = "NETWORK",
            name = "${metric.httpMethod} ${metric.url}",
            durationMs = metric.durationMs,
            attributes = mapOf(
                "url" to metric.url,
                "method" to metric.httpMethod,
                "status_code" to metric.statusCode,
                "request_size_bytes" to metric.requestSizeBytes,
                "response_size_bytes" to metric.responseSizeBytes,
                "is_success" to metric.isSuccess,
                "error_message" to (metric.errorMessage ?: "")
            )
        )
    }

    /**
     * Gathers and returns live system, RAM, battery, thermal, and network vitals snapshot.
     */
    @JvmStatic
    fun getVitals(): SystemVitals {
        val collector = vitalsCollector ?: throw IllegalStateException("CruiseApm is not initialized. Call CruiseApm.initialize(context, config) first.")
        val vitals = collector.captureVitals()
        eventBuffer?.updateVitals(vitals)
        return vitals
    }

    /**
     * Real-time Kotlin StateFlow for observing system vitals.
     */
    @JvmStatic
    fun getVitalsStream(): StateFlow<SystemVitals?> {
        val buffer = eventBuffer ?: throw IllegalStateException("CruiseApm is not initialized.")
        return buffer.vitalsStream
    }

    /**
     * Real-time Kotlin SharedFlow stream of ingested APM events.
     */
    @JvmStatic
    fun getEventStream(): SharedFlow<ApmEvent> {
        val buffer = eventBuffer ?: throw IllegalStateException("CruiseApm is not initialized.")
        return buffer.eventStream
    }

    /**
     * Logs a custom telemetry event with key-value attributes.
     */
    @JvmStatic
    fun logCustomEvent(name: String, attributes: Map<String, Any> = emptyMap()) {
        logEvent(
            category = "CUSTOM",
            name = name,
            attributes = attributes
        )
    }

    /**
     * Associates subsequent events with a specific user id.
     */
    @JvmStatic
    fun identifyUser(userId: String, traits: Map<String, Any> = emptyMap()) {
        this.currentUserId = userId
        logEvent(
            category = "USER",
            name = "user_identified",
            attributes = traits + mapOf("user_id" to userId)
        )
    }

    /**
     * Flushes buffered in-memory events to persistent storage / transport pipeline.
     */
    @JvmStatic
    fun flushEvents(callback: CruiseApmCallback<List<ApmEvent>>? = null) {
        ApmDispatcher.scope.launch {
            try {
                val flushed = eventBuffer?.flush() ?: emptyList()
                callback?.onComplete(ApmResult.Success(flushed))
            } catch (e: Throwable) {
                callback?.onComplete(ApmResult.Failure(e))
            }
        }
    }

    /**
     * Returns un-flushed events currently held in the in-memory queue.
     */
    @JvmStatic
    fun getPendingEvents(): List<ApmEvent> = eventBuffer?.getPendingEvents() ?: emptyList()

    /**
     * Returns the count of events safely persisted in offline storage.
     */
    @JvmStatic
    fun getPersistedEventCount(): Int = eventBuffer?.getPersistedCount() ?: 0

    private fun logEvent(
        category: String,
        name: String,
        durationMs: Long? = null,
        attributes: Map<String, Any> = emptyMap()
    ) {
        if (!isInitialized) return
        val event = ApmEvent(
            category = category,
            name = name,
            timestamp = System.currentTimeMillis(),
            durationMs = durationMs,
            attributes = attributes,
            userId = currentUserId,
            sessionId = currentSessionId
        )
        eventBuffer?.enqueue(event)
    }

    /**
     * Teardown and reset SDK state (primarily for unit tests).
     */
    @Synchronized
    internal fun reset() {
        isInitialized = false
        config = null
        currentUserId = null
        anrWatchdog?.stopWatchdog()
        anrWatchdog = null
        networkObserver?.stop()
        networkObserver = null
        eventBuffer?.clear()
        eventBuffer = null
        vitalsCollector = null
    }
}
