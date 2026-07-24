package com.jetpack.compose.github.github.cruise.data.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * OkHttp Interceptor that implements retry logic with exponential backoff
 *
 * Retries failed requests for:
 * - Network errors (IOException)
 * - Timeout errors
 * - 5xx server errors (GitHub service issues)
 *
 * Does NOT retry:
 * - 4xx client errors (bad request, not found, etc.)
 * - Successful responses
 */
class RetryInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response: Response? = null
        var exception: Exception? = null
        var attempt = 0

        while (attempt < MAX_RETRIES) {
            try {
                response?.close() // Close previous response if exists
                response = chain.proceed(request)

                // Success or client error (4xx) - don't retry
                if (response.isSuccessful || response.code < 500) {
                    return response
                }

                // Server error (5xx) - retry
                Timber.w("Server error ${response.code}, attempt $attempt/$MAX_RETRIES")
                response.close()
                response = null // Mark as closed

            } catch (e: IOException) {
                response?.close() // Ensure response is closed on IOException
                response = null
                exception = e
                Timber.w(e, "Network error, attempt $attempt/$MAX_RETRIES: ${e.message}")

                // Don't retry on UnknownHostException (no internet)
                if (e is UnknownHostException) {
                    throw e
                }
            } catch (e: Exception) {
                // Catch any other exceptions (like ApiError from ApiInterceptor)
                // These are non-retryable errors (4xx client errors)
                response?.close() // Ensure response is closed
                Timber.w(e, "Non-retryable error: ${e.message}")
                throw e // Re-throw immediately, don't retry
            }

            // Don't wait after last attempt
            if (attempt < MAX_RETRIES - 1) {
                val delayMs = calculateBackoff(attempt)
                Timber.d("Retrying in ${delayMs}ms...")
                Thread.sleep(delayMs)
            }

            attempt++
        }

        // All retries exhausted
        if (exception != null) {
            throw exception
        }
        return response ?: throw IOException("Max retries ($MAX_RETRIES) exceeded")
    }

    /**
     * Calculate exponential backoff delay
     *
     * Formula: baseDelay * (2 ^ attempt)
     * - Attempt 0: 1000ms (1s)
     * - Attempt 1: 2000ms (2s)
     * - Attempt 2: 4000ms (4s)
     */
    private fun calculateBackoff(attempt: Int): Long {
        val exponentialDelay = BASE_DELAY_MS * (1 shl attempt) // 2^attempt
        return exponentialDelay.coerceAtMost(MAX_DELAY_MS)
    }

    companion object {
        private const val MAX_RETRIES = 3
        private const val BASE_DELAY_MS = 1000L // 1 second
        private const val MAX_DELAY_MS = 8000L // 8 seconds cap
    }
}
