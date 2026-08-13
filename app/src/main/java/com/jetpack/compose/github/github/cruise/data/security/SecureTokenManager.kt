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
        private const val KEY_API_KEY_EXTRA_SECURE = "api_key_extra_secure"
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

    // ========================================
    // SECURE API KEY MANAGEMENT (Example)
    // ========================================
    // This demonstrates the CORRECT way to handle sensitive API keys
    // Unlike BuildConfig, these values are:
    // 1. NOT compiled into the APK
    // 2. Encrypted using AES256-GCM
    // 3. Stored in Android Keystore (hardware-backed)
    // 4. Only accessible at runtime after user sets it
    // ========================================

    /**
     * Save a secure API key (e.g., for third-party services)
     *
     * Example: API keys for payment processors, analytics, etc.
     * The key is encrypted and stored securely, NOT extractable from APK.
     *
     * @param apiKey The API key to store securely
     */
    fun saveSecureApiKey(apiKey: String) {
        try {
            encryptedPrefs.edit()
                .putString(KEY_API_KEY_EXTRA_SECURE, apiKey)
                .apply()
            Timber.d("Secure API key saved successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error saving secure API key")
            throw e
        }
    }

    /**
     * Get the secure API key
     *
     * @return Encrypted API key if exists, null otherwise
     */
    fun getSecureApiKey(): String? {
        return try {
            encryptedPrefs.getString(KEY_API_KEY_EXTRA_SECURE, null)
        } catch (e: Exception) {
            Timber.e(e, "Error retrieving secure API key")
            null
        }
    }

    /**
     * Check if secure API key exists
     *
     * @return true if key is saved, false otherwise
     */
    fun hasSecureApiKey(): Boolean {
        return !getSecureApiKey().isNullOrBlank()
    }

    /**
     * Remove stored secure API key
     */
    fun clearSecureApiKey() {
        try {
            encryptedPrefs.edit()
                .remove(KEY_API_KEY_EXTRA_SECURE)
                .apply()
            Timber.d("Secure API key cleared")
        } catch (e: Exception) {
            Timber.e(e, "Error clearing secure API key")
        }
    }
}
