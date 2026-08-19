package com.jetpack.compose.github.github.cruise.data.network.circuitbreaker

import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Circuit Breaker state machine:
 * - CLOSED: Normal operating state. Requests pass through.
 * - OPEN: Downstream service is failing. Requests fail-fast without hitting network.
 * - HALF_OPEN: Trial state after timeout. Canary requests are allowed to test backend recovery.
 */
enum class CircuitState {
    CLOSED,
    OPEN,
    HALF_OPEN
}

@Singleton
class CircuitBreaker @Inject constructor() {

    private val state = AtomicReference(CircuitState.CLOSED)
    private val failureCount = AtomicInteger(0)
    private val halfOpenSuccessCount = AtomicInteger(0)
    private val lastStateChangeTime = AtomicLong(System.currentTimeMillis())

    var failureThreshold: Int = DEFAULT_FAILURE_THRESHOLD
    var resetTimeoutMs: Long = DEFAULT_RESET_TIMEOUT_MS
    var halfOpenSuccessThreshold: Int = DEFAULT_HALF_OPEN_SUCCESS_THRESHOLD

    /**
     * Check if request execution is permitted by the circuit breaker.
     * Transitions from OPEN -> HALF_OPEN when resetTimeoutMs has elapsed.
     */
    fun canExecute(): Boolean {
        val currentState = state.get()

        return when (currentState) {
            CircuitState.CLOSED -> true

            CircuitState.OPEN -> {
                val timeInOpen = System.currentTimeMillis() - lastStateChangeTime.get()
                if (timeInOpen >= resetTimeoutMs) {
                    if (state.compareAndSet(CircuitState.OPEN, CircuitState.HALF_OPEN)) {
                        lastStateChangeTime.set(System.currentTimeMillis())
                        halfOpenSuccessCount.set(0)
                        Timber.i("CircuitBreaker transitioned from OPEN to HALF_OPEN (timeout elapsed)")
                    }
                    true
                } else {
                    false
                }
            }

            CircuitState.HALF_OPEN -> true
        }
    }

    /**
     * Record a successful network request.
     */
    fun recordSuccess() {
        val currentState = state.get()
        when (currentState) {
            CircuitState.HALF_OPEN -> {
                val successes = halfOpenSuccessCount.incrementAndGet()
                if (successes >= halfOpenSuccessThreshold) {
                    if (state.compareAndSet(CircuitState.HALF_OPEN, CircuitState.CLOSED)) {
                        failureCount.set(0)
                        lastStateChangeTime.set(System.currentTimeMillis())
                        Timber.i("CircuitBreaker transitioned from HALF_OPEN to CLOSED (service recovered)")
                    }
                }
            }
            CircuitState.CLOSED -> {
                failureCount.set(0)
            }
            CircuitState.OPEN -> {
                // In OPEN state, no action without state transition
            }
        }
    }

    /**
     * Record a network / server failure.
     */
    fun recordFailure() {
        val currentState = state.get()
        when (currentState) {
            CircuitState.CLOSED -> {
                val failures = failureCount.incrementAndGet()
                if (failures >= failureThreshold) {
                    if (state.compareAndSet(CircuitState.CLOSED, CircuitState.OPEN)) {
                        lastStateChangeTime.set(System.currentTimeMillis())
                        Timber.w("CircuitBreaker transitioned from CLOSED to OPEN (failure threshold $failures reached)")
                    }
                }
            }
            CircuitState.HALF_OPEN -> {
                // Immediate trip back to OPEN on probe failure
                state.set(CircuitState.OPEN)
                lastStateChangeTime.set(System.currentTimeMillis())
                Timber.w("CircuitBreaker transitioned from HALF_OPEN to OPEN (probe request failed)")
            }
            CircuitState.OPEN -> {
                lastStateChangeTime.set(System.currentTimeMillis())
            }
        }
    }

    /**
     * Execute a block protected by the circuit breaker
     */
    @Throws(Exception::class)
    fun <T> execute(block: () -> T): T {
        if (!canExecute()) {
            throw CircuitBreakerOpenException(
                "Circuit breaker is OPEN. Failing fast to protect backend from overload."
            )
        }

        return try {
            val result = block()
            recordSuccess()
            result
        } catch (e: Exception) {
            recordFailure()
            throw e
        }
    }

    /**
     * Current state of the circuit breaker
     */
    fun getState(): CircuitState {
        // Evaluate OPEN -> HALF_OPEN on query if timeout expired
        if (state.get() == CircuitState.OPEN) {
            val timeInOpen = System.currentTimeMillis() - lastStateChangeTime.get()
            if (timeInOpen >= resetTimeoutMs) {
                if (state.compareAndSet(CircuitState.OPEN, CircuitState.HALF_OPEN)) {
                    lastStateChangeTime.set(System.currentTimeMillis())
                    halfOpenSuccessCount.set(0)
                }
            }
        }
        return state.get()
    }

    /**
     * Reset circuit breaker to initial CLOSED state (for testing or manual recovery)
     */
    fun reset() {
        state.set(CircuitState.CLOSED)
        failureCount.set(0)
        halfOpenSuccessCount.set(0)
        lastStateChangeTime.set(System.currentTimeMillis())
    }

    companion object {
        const val DEFAULT_FAILURE_THRESHOLD = 5
        const val DEFAULT_RESET_TIMEOUT_MS = 60_000L // 60 seconds
        const val DEFAULT_HALF_OPEN_SUCCESS_THRESHOLD = 2
    }
}
