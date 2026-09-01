package com.cruise.apm.model

/**
 * Metric recorded for an HTTP request/response cycle.
 *
 * @property url Full request URL or sanitized endpoint path.
 * @property httpMethod HTTP verb ("GET", "POST", "PUT", "DELETE", etc.).
 * @property statusCode HTTP response status code (e.g. 200, 404, 500), or -1 if the call failed with an exception.
 * @property durationMs Total round-trip latency in milliseconds.
 * @property requestSizeBytes Request payload size in bytes.
 * @property responseSizeBytes Response payload size in bytes.
 * @property isSuccess True if statusCode is between 200 and 299.
 * @property errorMessage Exception message if the network call failed before completing.
 * @property timestamp Epoch timestamp (ms) when the request was initiated.
 */
data class NetworkMetric(
    val url: String,
    val httpMethod: String,
    val statusCode: Int,
    val durationMs: Long,
    val requestSizeBytes: Long = 0L,
    val responseSizeBytes: Long = 0L,
    val isSuccess: Boolean = statusCode in 200..299,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
