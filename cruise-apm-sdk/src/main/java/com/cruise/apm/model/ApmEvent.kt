package com.cruise.apm.model

/**
 * Universal telemetry envelope transmitted to the CruiseAPM ingestion pipeline.
 *
 * @property eventId Unique UUID string for event deduplication.
 * @property category Type category of the event ("NETWORK", "TRACE", "VITALS", "ANR", "SCREEN", "SESSION").
 * @property name Descriptive name (e.g. "github_api_request", "cold_startup", "screen_view").
 * @property timestamp Epoch timestamp (ms).
 * @property durationMs Optional duration for timed events.
 * @property attributes Custom key-value metadata.
 * @property userId Optional identified user id.
 * @property sessionId Unique session identifier for correlation.
 */
data class ApmEvent(
    val eventId: String = java.util.UUID.randomUUID().toString(),
    val category: String,
    val name: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long? = null,
    val attributes: Map<String, Any> = emptyMap(),
    val userId: String? = null,
    val sessionId: String? = null
)
