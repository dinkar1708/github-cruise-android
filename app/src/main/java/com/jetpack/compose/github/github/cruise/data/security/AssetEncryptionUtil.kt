package com.jetpack.compose.github.github.cruise.data.security

import android.content.Context
import timber.log.Timber
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Utility to decrypt API keys from encrypted assets file
 *
 * Flow:
 * 1. Build time: Gradle reads local.properties → Encrypts → Saves to assets/secure_config.enc
 * 2. Runtime: Read assets/secure_config.enc → Decrypt → Use
 *
 * Security:
 * - API key is NOT in BuildConfig (plain text)
 * - API key is in assets folder as ENCRYPTED string
 * - Decryption key is obfuscated in code (hardened with ProGuard/R8)
 * - Better than BuildConfig, but not perfect (key is still in APK)
 *
 * Production recommendation: Fetch from backend after authentication
 */
object AssetEncryptionUtil {

    // Obfuscated key - in production, use NDK or key derivation
    // This is still extractable from APK but requires more effort
    private const val ENCRYPTION_KEY = "GithubCruise2024SecureKey!@#$"

    /**
     * Read and decrypt API key from assets folder
     *
     * @param context Application context
     * @param assetFileName Name of encrypted file in assets
     * @return Decrypted API key, or null if error
     */
    fun readEncryptedApiKey(context: Context, assetFileName: String = "secure_config.enc"): String? {
        return try {
            // Read encrypted content from assets
            val encryptedData = context.assets.open(assetFileName).bufferedReader().use { it.readText() }

            // Decrypt
            decrypt(encryptedData, ENCRYPTION_KEY)
        } catch (e: Exception) {
            Timber.e(e, "Error reading encrypted API key from assets")
            null
        }
    }

    /**
     * Decrypt string using AES
     */
    private fun decrypt(encryptedData: String, key: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKey = SecretKeySpec(key.toByteArray().copyOf(16), "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)

        val decodedBytes = Base64.getDecoder().decode(encryptedData)
        val decryptedBytes = cipher.doFinal(decodedBytes)

        return String(decryptedBytes)
    }

    /**
     * Encrypt string using AES (for build-time Gradle script)
     *
     * This method is NOT used in Android code, but shown for reference
     * The actual encryption happens in Gradle build script
     */
    fun encrypt(plainText: String, key: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val secretKey = SecretKeySpec(key.toByteArray().copyOf(16), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encryptedBytes = cipher.doFinal(plainText.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }
}
