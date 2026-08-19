package com.jetpack.compose.github.github.cruise.data.network.interceptor

import com.jetpack.compose.github.github.cruise.data.network.circuitbreaker.CircuitBreaker
import com.jetpack.compose.github.github.cruise.data.network.circuitbreaker.CircuitBreakerOpenException
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * OkHttp Interceptor that implements resilient retries with Full Jitter Exponential Backoff
 * and Circuit Breaker integration.
 *
 * Retries failed requests for:
 * - Network timeouts / IOExceptions (except UnknownHostException / no internet)
 * - 5xx server errors (GitHub service issues)
 *
 * Does NOT retry:
 * - 4xx client errors (bad request, not found, unauthorized, etc.)
 * - UnknownHostException (offline)
 * - When CircuitBreaker is OPEN
 */
@Singleton
class RetryInterceptor @Inject constructor(
    private val circuitBreaker: CircuitBreaker
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // Fail-fast if circuit breaker is OPEN
        if (!circuitBreaker.canExecute()) {
            Timber.w("RetryInterceptor: Circuit breaker is OPEN. Fast-failing request.")
            throw CircuitBreakerOpenException(
                "Circuit breaker is OPEN. Requests blocked to protect backend."
            )
        }

        val request = chain.request()
        var response: Response? = null
        var lastException: IOException? = null
        var attempt = 0

        while (attempt < MAX_RETRIES) {
            // Check if call was cancelled before proceeding
            if (chain.call().isCanceled()) {
                throw IOException("Canceled")
            }

            // Check circuit breaker before each retry attempt
            if (attempt > 0 && !circuitBreaker.canExecute()) {
                Timber.w("RetryInterceptor: Circuit breaker tripped OPEN during retries. Aborting.")
                throw CircuitBreakerOpenException(
                    "Circuit breaker tripped OPEN during retry attempts."
                )
            }

            try {
                response?.close()
                response = chain.proceed(request)

                // 2xx or 4xx client error -> record success (server is reachable) and return
                if (response.isSuccessful || response.code < 500) {
                    circuitBreaker.recordSuccess()
                    return response
                }

                // 5xx Server error -> record failure and retry
                circuitBreaker.recordFailure()
                Timber.w("Server error HTTP ${response.code} on attempt ${attempt + 1}/$MAX_RETRIES")
                response.close()
                response = null

            } catch (e: IOException) {
                response?.close()
                response = null
                lastException = e

                // If request was cancelled (e.g. user typed new query or navigated away), abort immediately
                if (chain.call().isCanceled() || e.message?.contains("canceled", ignoreCase = true) == true) {
                    Timber.d("Request was cancelled. Aborting retries immediately.")
                    throw e
                }

                // Offline / DNS resolution failure -> do not retry, and DO NOT trip circuit breaker
                if (e is UnknownHostException ||
                    e is com.jetpack.compose.github.github.cruise.data.network.model.ApiError.NetworkError ||
                    e.message?.contains("unable to resolve host", ignoreCase = true) == true ||
                    e.message?.contains("no address associated with hostname", ignoreCase = true) == true
                ) {
                    Timber.w("No internet connection / offline. Skipping retries without tripping circuit breaker.")
                    throw e
                }

                circuitBreaker.recordFailure()
                Timber.w(e, "Network error on attempt ${attempt + 1}/$MAX_RETRIES: ${e.message}")
            }

            // Wait with Full Jitter Exponential Backoff before next attempt
            if (attempt < MAX_RETRIES - 1) {
                val delayMs = calculateBackoffWithJitter(attempt)
                Timber.d("Retrying attempt ${attempt + 2}/$MAX_RETRIES in ${delayMs}ms (Full Jitter)...")
                try {
                    Thread.sleep(delayMs)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException("Retry interrupted", e)
                }
            }

            attempt++
        }

        // All retries exhausted
        if (lastException != null) {
            throw lastException
        }
        return response ?: throw IOException("Max retries ($MAX_RETRIES) exhausted with server errors.")
    }

    /**
     * Calculate delay using Full Jitter Exponential Backoff:
     * Formula: Random.nextLong(0, min(MAX_DELAY_MS, BASE_DELAY_MS * 2^attempt))
     *
     * Prevents the "thundering herd" problem where thousands of clients retry simultaneously.
     */
    fun calculateBackoffWithJitter(attempt: Int): Long {
        val exponentialCap = (BASE_DELAY_MS * (1 shl attempt)).coerceAtMost(MAX_DELAY_MS)
        return Random.nextLong(0, exponentialCap + 1)
    }

    companion object {
        const val MAX_RETRIES = 3
        const val BASE_DELAY_MS = 1000L // 1 second
        const val MAX_DELAY_MS = 8000L  // 8 seconds cap
    }
}

