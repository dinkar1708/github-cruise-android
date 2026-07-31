package com.jetpack.compose.github.github.cruise.utils

import android.content.Context
import android.os.Build
import java.util.Locale

/**
 * Utility for managing app locale/language changes
 *
 * Supports:
 * - English (en)
 * - Japanese (ja)
 * - System default
 */
object LocaleManager {

    /**
     * Apply locale to context
     * @param context Application context
     * @param languageCode "en", "ja", or "system"
     * @return Context with updated locale
     */
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            "en" -> Locale("en")
            "ja" -> Locale("ja")
            "system" -> getSystemLocale()
            else -> Locale("en")
        }

        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = android.content.res.Configuration(resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }

    /**
     * Get system default locale
     */
    private fun getSystemLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            android.content.res.Resources.getSystem().configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            android.content.res.Resources.getSystem().configuration.locale
        }
    }

    /**
     * Get locale display name
     * @param languageCode "en", "ja", or "system"
     * @return Display name (e.g., "English", "日本語", "System Default")
     */
    fun getLocaleDisplayName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "ja" -> "日本語"
            "system" -> "System Default"
            else -> "English"
        }
    }

    /**
     * Get all supported locales
     * @return List of locale codes and display names
     */
    fun getSupportedLocales(): List<Pair<String, String>> {
        return listOf(
            "system" to "System Default",
            "en" to "English",
            "ja" to "日本語 (Japanese)"
        )
    }
}
