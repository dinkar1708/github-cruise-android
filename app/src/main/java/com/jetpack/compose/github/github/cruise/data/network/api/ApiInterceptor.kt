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
                when (response.code) {
                    // handle github api exception as per documentation
                    // https://docs.github.com/en/rest/search/search?apiVersion=2022-11-28#search-users--status-codes
                    // https://docs.github.com/en/rest/users/users?apiVersion=2022-11-28#get-a-user--status-codes
                    404 -> throw ApiError.ResourceNotFoundError("Resource not found")
                    304 -> throw ApiError.NotModifiedError("Not modified")
                    422 -> throw ApiError.ValidationFailedError("Validation failed or the endpoint has been spammed")
                    503 -> throw ApiError.ServiceUnavailableError("Service unavailable")
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
