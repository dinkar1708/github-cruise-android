package com.jetpack.compose.github.github.cruise.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jetpack.compose.github.github.cruise.domain.model.FavoriteItem
import com.jetpack.compose.github.github.cruise.domain.model.FavoriteType
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore-based implementation for favorites storage
 *
 * Migration from SharedPreferences to DataStore provides:
 * - Type-safe, asynchronous API
 * - No UI thread blocking
 * - Flow-based reactive updates
 * - Better error handling
 *
 * Official Guide: https://developer.android.com/topic/libraries/architecture/datastore
 */
@Singleton
class FavoritesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val moshi: Moshi
) {
    private val adapter = moshi.adapter<List<FavoriteItem>>(
        Types.newParameterizedType(List::class.java, FavoriteItem::class.java)
    )

    /**
     * Flow of favorites list
     * Automatically updates when favorites change
     */
    val favorites: Flow<List<FavoriteItem>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.e(exception, "Error reading favorites from DataStore")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val json = preferences[FAVORITES_KEY]
            if (json != null) {
                try {
                    adapter.fromJson(json) ?: emptyList()
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing favorites JSON")
                    emptyList()
                }
            } else {
                emptyList()
            }
        }

    /**
     * Add item to favorites
     */
    suspend fun addFavorite(item: FavoriteItem) {
        dataStore.edit { preferences ->
            val currentJson = preferences[FAVORITES_KEY]
            val currentFavorites = if (currentJson != null) {
                try {
                    adapter.fromJson(currentJson)?.toMutableList() ?: mutableListOf()
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing current favorites")
                    mutableListOf()
                }
            } else {
                mutableListOf()
            }

            // Add if not already present
            if (currentFavorites.none { it.id == item.id && it.type == item.type }) {
                currentFavorites.add(0, item) // Add to the beginning
                try {
                    val newJson = adapter.toJson(currentFavorites)
                    preferences[FAVORITES_KEY] = newJson
                } catch (e: Exception) {
                    Timber.e(e, "Error serializing favorites")
                }
            }
        }
    }

    /**
     * Remove item from favorites
     */
    suspend fun removeFavorite(itemId: String, type: FavoriteType) {
        dataStore.edit { preferences ->
            val currentJson = preferences[FAVORITES_KEY]
            val currentFavorites = if (currentJson != null) {
                try {
                    adapter.fromJson(currentJson)?.toMutableList() ?: mutableListOf()
                } catch (e: Exception) {
                    Timber.e(e, "Error parsing current favorites")
                    mutableListOf()
                }
            } else {
                mutableListOf()
            }

            currentFavorites.removeAll { it.id == itemId && it.type == type }

            try {
                val newJson = adapter.toJson(currentFavorites)
                preferences[FAVORITES_KEY] = newJson
            } catch (e: Exception) {
                Timber.e(e, "Error serializing favorites")
            }
        }
    }

    /**
     * Clear all favorites
     */
    suspend fun clearFavorites() {
        dataStore.edit { preferences ->
            preferences.remove(FAVORITES_KEY)
        }
    }

    companion object {
        private val FAVORITES_KEY = stringPreferencesKey("favorites")
    }
}
