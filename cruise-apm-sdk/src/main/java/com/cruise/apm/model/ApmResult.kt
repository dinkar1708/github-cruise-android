package com.cruise.apm.model

/**
 * Result wrapper representing the outcome of an SDK operation.
 */
sealed class ApmResult<out T> {
    data class Success<out T>(val data: T) : ApmResult<T>()
    data class Failure(
        val error: Throwable,
        val message: String = error.localizedMessage ?: "Unknown CruiseAPM SDK error"
    ) : ApmResult<Nothing>()
}
