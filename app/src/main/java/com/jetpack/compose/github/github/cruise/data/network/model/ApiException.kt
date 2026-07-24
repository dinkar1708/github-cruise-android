package com.jetpack.compose.github.github.cruise.data.network.model

import java.io.IOException

/**
 * Sealed class hierarchy for API errors
 *
 * Provides type-safe error handling for all HTTP status codes and network issues
 */
sealed class ApiError(override val message: String) : IOException(message) {
    // 4xx Client Errors
    class BadRequestError(message: String) : ApiError(message)
    class UnauthorizedError(message: String) : ApiError(message)
    class ForbiddenError(message: String) : ApiError(message)
    class ResourceNotFoundError(message: String) : ApiError(message)
    class ValidationFailedError(message: String) : ApiError(message)
    class RateLimitExceededError(
        message: String,
        val retryAfter: Long? = null
    ) : ApiError(message)

    // 3xx Redirection
    class NotModifiedError(message: String) : ApiError(message)

    // 5xx Server Errors
    class ServerError(message: String) : ApiError(message)
    class BadGatewayError(message: String) : ApiError(message)
    class ServiceUnavailableError(message: String) : ApiError(message)
    class GatewayTimeoutError(message: String) : ApiError(message)

    // Network Errors
    class NetworkError(message: String) : ApiError(message)
    class TimeoutError(message: String) : ApiError(message)

    // Generic Errors
    class ApiException(errorResponse: ApiErrorResponse) : ApiError(errorResponse.message)
    data object UnknownError : ApiError("Unknown error occurred")
}
