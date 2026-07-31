package com.jetpack.compose.github.github.cruise.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore for managing app locale/language preferences
 */
private val Context.localeDataStore: DataStore<Preferences> by preferencesDataStore(name = "locale_preferences")

@Singleton
class LocaleDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val LOCALE_KEY = stringPreferencesKey("app_locale")
        const val LOCALE_ENGLISH = "en"
        const val LOCALE_JAPANESE = "ja"
        const val LOCALE_SYSTEM_DEFAULT = "system"
    }

    /**
     * Get current app locale
     * Returns: "en", "ja", or "system"
     */
    val currentLocale: Flow<String> = context.localeDataStore.data
        .map { preferences ->
            preferences[LOCALE_KEY] ?: LOCALE_SYSTEM_DEFAULT
        }

    /**
     * Set app locale
     * @param locale "en", "ja", or "system"
     */
    suspend fun setLocale(locale: String) {
        // Save to DataStore
        context.localeDataStore.edit { preferences ->
            preferences[LOCALE_KEY] = locale
        }

        // Also save to SharedPreferences for immediate access in attachBaseContext
        context.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("locale", locale)
            .apply()
    }
}
