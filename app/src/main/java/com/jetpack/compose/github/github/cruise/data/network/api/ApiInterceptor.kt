package com.jetpack.compose.github.github.cruise.data.network.api

import com.jetpack.compose.github.github.cruise.data.network.model.ApiError
import com.jetpack.compose.github.github.cruise.data.network.model.ApiErrorResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Created by Dinakar Maurya on 2024/05/12.
 */
class ApiInterceptor(private val moshi: Moshi) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("X-GitHub-Api-Version", ApiConstants.API_VERSION)
            .build()

        try {
            val response = chain.proceed(request)

            if (!response.isSuccessful) {
                // Close response before throwing exception to prevent OkHttp leak
                response.close()

                when (response.code) {
                    // 3xx Redirection
                    304 -> throw ApiError.NotModifiedError("Not modified")

                    // 4xx Client Errors
                    400 -> throw ApiError.BadRequestError("Bad request - check your parameters")
                    401 -> throw ApiError.UnauthorizedError("Unauthorized - authentication required")
                    403 -> throw ApiError.ForbiddenError("Forbidden - check your access permissions")
                    404 -> throw ApiError.ResourceNotFoundError("Resource not found")
                    422 -> throw ApiError.ValidationFailedError("Validation failed or the endpoint has been spammed")
                    429 -> {
                        // GitHub rate limit - extract retry-after header
                        val retryAfter = response.header("X-RateLimit-Reset")?.toLongOrNull()
                        throw ApiError.RateLimitExceededError(
                            "GitHub API rate limit exceeded. Please try again later.",
                            retryAfter
                        )
                    }

                    // 5xx Server Errors
                    500 -> throw ApiError.ServerError("Internal server error - please try again later")
                    502 -> throw ApiError.BadGatewayError("Bad gateway - GitHub service issue")
                    503 -> throw ApiError.ServiceUnavailableError("Service unavailable - GitHub may be down")
                    504 -> throw ApiError.GatewayTimeoutError("Gateway timeout - request took too long")

                    // Generic error handling for other status codes
                    else -> response.body?.let { responseBody ->
                        handleError(responseBody)
                    }
                }
            }
            return response
        } catch (exception: UnknownHostException) {
            throw ApiError.NetworkError(exception.message ?: "Unknown host error")
        }
    }

    private fun handleError(responseBody: ResponseBody) {
        try {
            val adapter: JsonAdapter<ApiErrorResponse> = moshi.adapter(ApiErrorResponse::class.java)
            val errorResponse = adapter.fromJson(responseBody.string())
            errorResponse?.let {
                throw ApiError.ApiException(it)
            } ?: run {
                throw ApiError.UnknownError
            }
        } catch (timeoutException: SocketTimeoutException) {
            throw ApiError.TimeoutError(timeoutException.message ?: "Timeout error")
        } catch (ioException: IOException) {
            throw ApiError.NetworkError(ioException.message ?: "Unknown network error")
        } catch (exception: Exception) {
            throw ApiError.UnknownError
        }
    }
}
