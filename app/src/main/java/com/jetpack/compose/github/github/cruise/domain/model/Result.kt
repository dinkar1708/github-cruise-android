package com.jetpack.compose.github.github.cruise.domain.model

/**
 * A generic wrapper for handling success and error states in a type-safe way.
 * This follows the Result/Either pattern for better error handling.
 *
 * Usage example:
 * ```
 * when (result) {
 *     is Result.Success -> handleData(result.data)
 *     is Result.Error -> handleError(result.error)
 * }
 * ```
 */
sealed class Result<out T> {
    /**
     * Success state with data
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Error state with error message
     */
    data class Error(val error: String, val throwable: Throwable? = null) : Result<Nothing>()

    /**
     * Check if result is successful
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Check if result is an error
     */
    val isError: Boolean
        get() = this is Error

    /**
     * Get data if success, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * Get data if success, or default value if error
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        is Error -> defaultValue
    }

    /**
     * Execute action on success
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Execute action on error
     */
    inline fun onError(action: (String, Throwable?) -> Unit): Result<T> {
        if (this is Error) action(error, throwable)
        return this
    }

    companion object {
        /**
         * Create a success result
         */
        fun <T> success(data: T): Result<T> = Success(data)

        /**
         * Create an error result
         */
        fun error(message: String, throwable: Throwable? = null): Result<Nothing> =
            Error(message, throwable)
    }
}
