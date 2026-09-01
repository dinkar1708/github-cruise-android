package com.cruise.apm.network

import android.os.SystemClock
import com.cruise.apm.CruiseApm
import com.cruise.apm.model.NetworkMetric
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Non-destructive OkHttp Interceptor that automatically records HTTP request/response metrics.
 *
 * Captures latency (ms), HTTP status codes, request/response body sizes, and network exceptions.
 *
 * Example usage:
 * ```kotlin
 * val okHttpClient = OkHttpClient.Builder()
 *     .addInterceptor(CruiseApmOkHttpInterceptor())
 *     .build()
 * ```
 */
class CruiseApmOkHttpInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val method = request.method
        val requestSize = request.body?.contentLength()?.coerceAtLeast(0L) ?: 0L
        val startTime = SystemClock.elapsedRealtime()

        try {
            val response = chain.proceed(request)
            val durationMs = (SystemClock.elapsedRealtime() - startTime).coerceAtLeast(0L)
            val responseSize = response.body?.contentLength()?.coerceAtLeast(0L) ?: 0L

            val metric = NetworkMetric(
                url = sanitizeUrl(url),
                httpMethod = method,
                statusCode = response.code,
                durationMs = durationMs,
                requestSizeBytes = requestSize,
                responseSizeBytes = responseSize,
                isSuccess = response.isSuccessful,
                errorMessage = null
            )
            CruiseApm.recordNetworkMetric(metric)

            return response
        } catch (e: Throwable) {
            val durationMs = (SystemClock.elapsedRealtime() - startTime).coerceAtLeast(0L)
            val metric = NetworkMetric(
                url = sanitizeUrl(url),
                httpMethod = method,
                statusCode = -1,
                durationMs = durationMs,
                requestSizeBytes = requestSize,
                responseSizeBytes = 0L,
                isSuccess = false,
                errorMessage = e.localizedMessage ?: e.javaClass.simpleName
            )
            CruiseApm.recordNetworkMetric(metric)

            if (e is IOException) throw e else throw IOException(e)
        }
    }

    private fun sanitizeUrl(rawUrl: String): String {
        // Strip sensitive query parameters if present
        return try {
            val uri = java.net.URI(rawUrl)
            "${uri.scheme}://${uri.host}${if (uri.port != -1 && uri.port != 80 && uri.port != 443) ":${uri.port}" else ""}${uri.path ?: ""}"
        } catch (e: Throwable) {
            rawUrl
        }
    }
}
