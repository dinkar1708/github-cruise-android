package com.jetpack.compose.github.github.cruise.utils

import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * Retry policy for network requests with exponential backoff
 *
 * Provides smart retry logic with configurable attempts and delays
 */
class RetryPolicy(
    private val maxAttempts: Int = 3,
    private val initialDelayMs: Long = 1000L,
    private val maxDelayMs: Long = 10000L,
    private val factor: Double = 2.0
) {
    /**
     * Execute block with retry logic
     *
     * @param shouldRetry Lambda to determine if error should be retried
     * @param block Suspending function to execute
     * @return Result of type T
     */
    suspend fun <T> execute(
        shouldRetry: (Exception) -> Boolean = { true },
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastException: Exception? = null

        repeat(maxAttempts) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e

                // Check if we should retry
                if (!shouldRetry(e) || attempt == maxAttempts - 1) {
                    throw e
                }

                Timber.w(e, "Attempt ${attempt + 1}/$maxAttempts failed, retrying in ${currentDelay}ms")

                // Exponential backoff delay
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMs)
            }
        }

        // Should never reach here, but throw last exception if it does
        throw lastException ?: IllegalStateException("Retry failed with no exception")
    }

    companion object {
        /**
         * Default retry policy for network requests
         */
        val Default = RetryPolicy(
            maxAttempts = 3,
            initialDelayMs = 1000L,
            maxDelayMs = 10000L,
            factor = 2.0
        )

        /**
         * Aggressive retry policy for critical operations
         */
        val Aggressive = RetryPolicy(
            maxAttempts = 5,
            initialDelayMs = 500L,
            maxDelayMs = 5000L,
            factor = 1.5
        )

        /**
         * Quick retry policy for fast operations
         */
        val Quick = RetryPolicy(
            maxAttempts = 2,
            initialDelayMs = 500L,
            maxDelayMs = 2000L,
            factor = 2.0
        )

        /**
         * No retry policy (fail fast)
         */
        val None = RetryPolicy(
            maxAttempts = 1,
            initialDelayMs = 0L,
            maxDelayMs = 0L,
            factor = 1.0
        )
    }
}

/**
 * Extension function for easier retry execution
 */
suspend fun <T> retryWithPolicy(
    policy: RetryPolicy = RetryPolicy.Default,
    shouldRetry: (Exception) -> Boolean = { true },
    block: suspend () -> T
): T {
    return policy.execute(shouldRetry, block)
}
