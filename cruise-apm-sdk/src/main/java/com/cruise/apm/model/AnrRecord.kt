package com.cruise.apm.model

/**
 * Diagnostic report generated when the Main UI Looper hangs or is unresponsive.
 *
 * @property durationBlockedMs Duration in milliseconds that the main thread was blocked.
 * @property mainThreadStackTrace Formatted stack trace of the Main Looper thread during the freeze.
 * @property allThreadsDump Optional snapshot of all running thread names and states.
 * @property timestamp Epoch timestamp (ms) when the ANR freeze was detected.
 */
data class AnrRecord(
    val durationBlockedMs: Long,
    val mainThreadStackTrace: String,
    val allThreadsDump: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
