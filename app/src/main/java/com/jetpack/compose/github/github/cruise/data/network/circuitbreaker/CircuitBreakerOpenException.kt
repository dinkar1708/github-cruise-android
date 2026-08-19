package com.jetpack.compose.github.github.cruise.data.network.circuitbreaker

import java.io.IOException

/**
 * Exception thrown when a network request is blocked because the CircuitBreaker is OPEN.
 * Inherits from IOException so OkHttp and repository layers can gracefully handle or fall back to cache.
 */
class CircuitBreakerOpenException(
    message: String = "Circuit breaker is OPEN. Requests blocked to prevent cascading failure."
) : IOException(message)
