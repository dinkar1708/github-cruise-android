package com.jetpack.compose.github.github.cruise.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure token manager using Android Keystore and EncryptedSharedPreferences
 *
 * Stores GitHub personal access tokens securely
 * Reference: https://developer.android.com/topic/security/data
 */
@Singleton
class SecureTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_FILENAME = "github_cruise_secure_prefs"
        private const val KEY_GITHUB_TOKEN = "github_personal_access_token"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_FILENAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Timber.e(e, "Error creating EncryptedSharedPreferences")
            throw e
        }
    }

    /**
     * Save GitHub personal access token securely
     *
     * @param token GitHub personal access token (starts with ghp_, gho_, etc.)
     */
    fun saveToken(token: String) {
        try {
            encryptedPrefs.edit()
                .putString(KEY_GITHUB_TOKEN, token)
                .apply()
            Timber.d("GitHub token saved securely")
        } catch (e: Exception) {
            Timber.e(e, "Error saving token")
            throw e
        }
    }

    /**
     * Get GitHub personal access token
     *
     * @return Token if exists, null otherwise
     */
    fun getToken(): String? {
        return try {
            encryptedPrefs.getString(KEY_GITHUB_TOKEN, null)
        } catch (e: Exception) {
            Timber.e(e, "Error retrieving token")
            null
        }
    }

    /**
     * Check if token exists
     *
     * @return true if token is saved, false otherwise
     */
    fun hasToken(): Boolean {
        return !getToken().isNullOrBlank()
    }

    /**
     * Remove stored token
     */
    fun clearToken() {
        try {
            encryptedPrefs.edit()
                .remove(KEY_GITHUB_TOKEN)
                .apply()
            Timber.d("GitHub token cleared")
        } catch (e: Exception) {
            Timber.e(e, "Error clearing token")
        }
    }

    /**
     * Validate token format (basic check)
     *
     * GitHub tokens typically start with:
     * - ghp_ (personal access token)
     * - gho_ (OAuth token)
     * - ghu_ (user-to-server token)
     * - ghs_ (server-to-server token)
     * - ghr_ (refresh token)
     *
     * @return true if format is valid, false otherwise
     */
    fun isValidTokenFormat(token: String): Boolean {
        val validPrefixes = listOf("ghp_", "gho_", "ghu_", "ghs_", "ghr_")
        return token.isNotBlank() && validPrefixes.any { token.startsWith(it) }
    }
}
