package com.jetpack.compose.github.github.cruise.data.network.authenticator

import com.jetpack.compose.github.github.cruise.data.security.SecureTokenManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Authenticator that handles 401 Unauthorized responses.
 *
 * Uses a coroutine Mutex to prevent race conditions when multiple concurrent requests
 * receive 401s simultaneously. Only the first thread performs the token refresh,
 * while subsequent waiting threads use the freshly updated token without calling the backend refresh endpoint again.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: SecureTokenManager
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loops if authentication repeatedly fails
        if (responseCount(response) >= MAX_RETRY_COUNT) {
            Timber.w("TokenAuthenticator: Retry limit reached ($MAX_RETRY_COUNT). Giving up.")
            return null
        }

        // Extract token used in the failed request
        val requestToken = extractBearerToken(response.request)

        return runBlocking {
            mutex.withLock {
                val currentToken = tokenManager.getToken()

                // Check if another concurrent thread already refreshed the token
                val validToken = if (!currentToken.isNullOrBlank() && currentToken != requestToken) {
                    Timber.d("TokenAuthenticator: Token was already refreshed by another concurrent thread. Reusing new token.")
                    currentToken
                } else {
                    Timber.d("TokenAuthenticator: Refreshing expired/invalid token...")
                    tokenManager.refreshToken()
                }

                if (!validToken.isNullOrBlank()) {
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $validToken")
                        .build()
                } else {
                    Timber.w("TokenAuthenticator: Token refresh failed. Clearing tokens.")
                    tokenManager.clearToken()
                    null
                }
            }
        }
    }

    private fun extractBearerToken(request: Request): String? {
        val header = request.header("Authorization") ?: return null
        return if (header.startsWith("Bearer ", ignoreCase = true)) {
            header.substring(7).trim()
        } else {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    companion object {
        private const val MAX_RETRY_COUNT = 3
    }
}
