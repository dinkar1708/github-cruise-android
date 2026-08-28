package com.cruise.apm.trace

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

/**
 * Handle for measuring the execution duration of custom flows, code paths, or operations.
 *
 * Example usage:
 * ```kotlin
 * val trace = CruiseApm.newTrace("fetch_repos")
 * trace.start()
 * trace.putAttribute("repo_count", 42)
 * // ... perform async operation ...
 * trace.stop()
 * ```
 */
class ApmTrace internal constructor(
    val name: String,
    private val onTraceCompleted: (ApmTrace) -> Unit
) {
    private val state = AtomicReference(TraceState.NOT_STARTED)
    private var startNanoTime: Long = 0L
    private var endNanoTime: Long = 0L
    private val attributes = ConcurrentHashMap<String, Any>()

    val startTimeEpoch: Long = System.currentTimeMillis()

    /**
     * Starts the performance trace timer.
     */
    fun start(): ApmTrace {
        if (state.compareAndSet(TraceState.NOT_STARTED, TraceState.RUNNING)) {
            startNanoTime = System.nanoTime()
        }
        return this
    }

    /**
     * Adds custom key-value metadata to this trace.
     */
    fun putAttribute(key: String, value: Any): ApmTrace {
        attributes[key] = value
        return this
    }

    /**
     * Returns all attributes attached to this trace.
     */
    fun getAttributes(): Map<String, Any> = attributes.toMap()

    /**
     * Stops the trace timer, calculates elapsed milliseconds, and dispatches the trace event.
     */
    fun stop(): Long {
        if (state.compareAndSet(TraceState.RUNNING, TraceState.STOPPED)) {
            endNanoTime = System.nanoTime()
            onTraceCompleted(this)
            return getDurationMs()
        }
        return getDurationMs()
    }

    /**
     * Cancels this trace without recording metrics.
     */
    fun cancel() {
        state.set(TraceState.CANCELLED)
    }

    /**
     * Returns the elapsed duration in milliseconds.
     */
    fun getDurationMs(): Long {
        return when (state.get()) {
            TraceState.RUNNING -> ((System.nanoTime() - startNanoTime) / 1_000_000L).coerceAtLeast(0L)
            TraceState.STOPPED -> ((endNanoTime - startNanoTime) / 1_000_000L).coerceAtLeast(0L)
            else -> 0L
        }
    }

    /**
     * Returns current lifecycle state of this trace.
     */
    fun getState(): TraceState = state.get()
}
