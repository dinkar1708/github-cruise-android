package com.jetpack.compose.github.github.cruise.ui.base

import androidx.lifecycle.ViewModel
import com.jetpack.compose.github.github.cruise.data.network.model.ApiError
import timber.log.Timber

/**
 * Base ViewModel providing common functionality for all ViewModels
 *
 * Benefits:
 * - Centralized error handling logic
 * - Consistent logging patterns
 * - Reduces code duplication across ViewModels
 */
abstract class BaseViewModel : ViewModel() {

    /**
     * Get the tag for logging purposes
     * Override in child ViewModels to provide specific tags
     */
    protected open val TAG: String = this::class.simpleName ?: "BaseViewModel"

    /**
     * Safely handle exceptions and convert them to user-friendly error messages
     *
     * @param exception The exception to handle
     * @param context Additional context about what operation failed
     * @return A user-friendly error message
     */
    protected fun handleError(exception: Throwable, context: String = ""): String {
        val errorMessage = when (exception) {
            is ApiError -> exception.message
            else -> "An unexpected error occurred: ${exception.message}"
        }

        // Log the error with context
        if (context.isNotEmpty()) {
            Timber.e(exception, "$TAG $context: $errorMessage")
        } else {
            Timber.e(exception, "$TAG Error: $errorMessage")
        }

        return errorMessage
    }

    /**
     * Log debug information
     */
    protected fun logDebug(message: String) {
        Timber.d("$TAG $message")
    }

    /**
     * Log information
     */
    protected fun logInfo(message: String) {
        Timber.i("$TAG $message")
    }

    /**
     * Log warning
     */
    protected fun logWarning(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.w(throwable, "$TAG $message")
        } else {
            Timber.w("$TAG $message")
        }
    }

    /**
     * Log error
     */
    protected fun logError(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Timber.e(throwable, "$TAG $message")
        } else {
            Timber.e("$TAG $message")
        }
    }
}
