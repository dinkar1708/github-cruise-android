package com.jetpack.compose.github.github.cruise.domain.model

import androidx.compose.runtime.Immutable

/**
 * Comprehensive error state model for better error handling and user feedback
 *
 * Provides detailed error information and recovery actions
 */
@Immutable
sealed class ErrorState {
    abstract val message: String
    abstract val isRetryable: Boolean
    abstract val userMessage: String

    /**
     * Network connectivity error (no internet)
     */
    data class NetworkError(
        override val message: String = "No internet connection",
        override val userMessage: String = "Please check your internet connection and try again"
    ) : ErrorState() {
        override val isRetryable: Boolean = true
    }

    /**
     * API rate limit exceeded
     */
    data class RateLimitError(
        val retryAfter: Long? = null,
        override val message: String = "API rate limit exceeded",
        override val userMessage: String = "You've made too many requests. Please try again later."
    ) : ErrorState() {
        override val isRetryable: Boolean = true
    }

    /**
     * Server error (5xx)
     */
    data class ServerError(
        override val message: String = "Server error",
        override val userMessage: String = "GitHub servers are experiencing issues. Please try again later."
    ) : ErrorState() {
        override val isRetryable: Boolean = true
    }

    /**
     * Resource not found (404)
     */
    data class NotFoundError(
        override val message: String = "Resource not found",
        override val userMessage: String = "The requested resource was not found"
    ) : ErrorState() {
        override val isRetryable: Boolean = false
    }

    /**
     * Authentication error (401, 403)
     */
    data class AuthError(
        override val message: String = "Authentication failed",
        override val userMessage: String = "Your GitHub token may be invalid or expired"
    ) : ErrorState() {
        override val isRetryable: Boolean = false
    }

    /**
     * Timeout error
     */
    data class TimeoutError(
        override val message: String = "Request timed out",
        override val userMessage: String = "The request took too long. Please try again."
    ) : ErrorState() {
        override val isRetryable: Boolean = true
    }

    /**
     * Database error
     */
    data class DatabaseError(
        override val message: String = "Database error",
        override val userMessage: String = "Failed to access local data"
    ) : ErrorState() {
        override val isRetryable: Boolean = true
    }

    /**
     * Validation error (bad request)
     */
    data class ValidationError(
        override val message: String = "Invalid input",
        override val userMessage: String = "Please check your input and try again"
    ) : ErrorState() {
        override val isRetryable: Boolean = false
    }

    /**
     * Unknown error
     */
    data class UnknownError(
        override val message: String = "An unexpected error occurred",
        override val userMessage: String = "Something went wrong. Please try again."
    ) : ErrorState() {
        override val isRetryable: Boolean = true
    }

    companion object {
        /**
         * Convert exception to ErrorState
         */
        fun fromException(exception: Exception): ErrorState {
            return when {
                exception is com.jetpack.compose.github.github.cruise.data.network.circuitbreaker.CircuitBreakerOpenException ||
                exception.message?.contains("circuit breaker", ignoreCase = true) == true ->
                    ServerError(
                        message = "Circuit breaker open",
                        userMessage = "GitHub servers are temporarily unavailable. Please try again shortly."
                    )
                exception.message?.contains("unable to resolve host", ignoreCase = true) == true ||
                exception.message?.contains("no address associated", ignoreCase = true) == true ->
                    NetworkError()
                exception.message?.contains("timeout", ignoreCase = true) == true ->
                    TimeoutError()
                exception.message?.contains("rate limit", ignoreCase = true) == true ->
                    RateLimitError()
                exception.message?.contains("not found", ignoreCase = true) == true ->
                    NotFoundError()
                exception.message?.contains("unauthorized", ignoreCase = true) == true ||
                exception.message?.contains("forbidden", ignoreCase = true) == true ->
                    AuthError()
                exception.message?.contains("server error", ignoreCase = true) == true ->
                    ServerError()
                else -> UnknownError(message = exception.message ?: "Unknown error")
            }
        }
    }
}
