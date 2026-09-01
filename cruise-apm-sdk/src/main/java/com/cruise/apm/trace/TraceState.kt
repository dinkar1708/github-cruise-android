package com.cruise.apm.trace

/**
 * Lifecycle state of a performance trace.
 */
enum class TraceState {
    NOT_STARTED,
    RUNNING,
    STOPPED,
    CANCELLED
}
