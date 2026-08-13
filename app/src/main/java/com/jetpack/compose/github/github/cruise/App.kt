package com.jetpack.compose.github.github.cruise

import android.app.Application
import android.content.Context
import com.jetpack.compose.github.github.cruise.BuildConfig.DEBUG
import com.jetpack.compose.github.github.cruise.data.datastore.LocaleDataStore
import com.jetpack.compose.github.github.cruise.data.security.SecureTokenManager
import com.jetpack.compose.github.github.cruise.utils.LocaleManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Dinakar Maurya on 2024/05/12.
 */
@HiltAndroidApp
class GithubCruiseApplication : Application() {

    @Inject
    lateinit var localeDataStore: LocaleDataStore

    @Inject
    lateinit var secureTokenManager: SecureTokenManager

    override fun onCreate() {
        super.onCreate()
        if (DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize secure API key from BuildConfig (demo flow)
        initializeSecureApiKey()
    }

    /**
     * Demonstrates the secure approach:
     * 1. Read from encrypted assets file (NOT BuildConfig)
     * 2. Decrypt the value
     * 3. Save to SecureTokenManager (double encryption)
     *
     * Flow:
     * - Build time: local.properties → Encrypted → assets/secure_config.enc
     * - Runtime: assets/secure_config.enc → Decrypt → SecureTokenManager
     *
     * Security improvement over BuildConfig:
     * - Value in APK is ENCRYPTED, not plain text
     * - Requires attacker to find both encrypted data AND decryption key
     *
     * In production, you would fetch from backend after user login instead.
     */
    private fun initializeSecureApiKey() {
        try {
            // Check if already initialized
            if (secureTokenManager.hasSecureApiKey()) {
                Timber.d("Secure API key already exists in encrypted storage")
                return
            }

            // Read encrypted API key from assets (NOT from BuildConfig)
            val apiKeyFromAssets = com.jetpack.compose.github.github.cruise.data.security.AssetEncryptionUtil
                .readEncryptedApiKey(this)

            if (!apiKeyFromAssets.isNullOrBlank()) {
                // Save to SecureTokenManager (double encryption layer)
                secureTokenManager.saveSecureApiKey(apiKeyFromAssets)
                Timber.d("Secure API key loaded from encrypted assets and saved to SecureTokenManager")
            } else {
                Timber.w("No encrypted API key found in assets")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error initializing secure API key from encrypted assets")
        }
    }

    override fun attachBaseContext(base: Context) {
        // Apply saved locale before app is created
        val locale = base.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
            .getString("locale", LocaleDataStore.LOCALE_SYSTEM_DEFAULT)
            ?: LocaleDataStore.LOCALE_SYSTEM_DEFAULT

        val context = LocaleManager.setLocale(base, locale)
        super.attachBaseContext(context)
    }
}