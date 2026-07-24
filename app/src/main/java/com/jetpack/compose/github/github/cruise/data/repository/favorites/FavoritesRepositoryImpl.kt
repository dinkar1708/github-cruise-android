package com.jetpack.compose.github.github.cruise.data.repository.favorites

import com.jetpack.compose.github.github.cruise.data.preferences.FavoritesPreferences
import com.jetpack.compose.github.github.cruise.di.IoDispatcher
import com.jetpack.compose.github.github.cruise.domain.model.FavoriteItem
import com.jetpack.compose.github.github.cruise.domain.model.FavoriteType
import com.jetpack.compose.github.github.cruise.domain.repository.FavoritesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Implementation of FavoritesRepository
 */
class FavoritesRepositoryImpl @Inject constructor(
    private val favoritesPreferences: FavoritesPreferences,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : FavoritesRepository {

    override val favorites: StateFlow<List<FavoriteItem>> = favoritesPreferences.favorites

    override suspend fun addFavorite(item: FavoriteItem) {
        withContext(dispatcher) {
            favoritesPreferences.addFavorite(item)
        }
    }

    override suspend fun removeFavorite(itemId: String, type: FavoriteType) {
        withContext(dispatcher) {
            favoritesPreferences.removeFavorite(itemId, type)
        }
    }

    override suspend fun isFavorite(itemId: String, type: FavoriteType): Boolean {
        return withContext(dispatcher) {
            favoritesPreferences.isFavorite(itemId, type)
        }
    }

    override suspend fun toggleFavorite(item: FavoriteItem) {
        withContext(dispatcher) {
            if (favoritesPreferences.isFavorite(item.id, item.type)) {
                favoritesPreferences.removeFavorite(item.id, item.type)
            } else {
                favoritesPreferences.addFavorite(item)
            }
        }
    }

    override suspend fun clearFavorites() {
        withContext(dispatcher) {
            favoritesPreferences.clearFavorites()
        }
    }
}
