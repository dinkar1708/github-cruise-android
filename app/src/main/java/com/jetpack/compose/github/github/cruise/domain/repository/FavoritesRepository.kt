package com.jetpack.compose.github.github.cruise.domain.repository

import com.jetpack.compose.github.github.cruise.domain.model.FavoriteItem
import com.jetpack.compose.github.github.cruise.domain.model.FavoriteType
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface for favorites operations
 *
 * Located in domain layer following Clean Architecture principles
 */
interface FavoritesRepository {
    val favorites: StateFlow<List<FavoriteItem>>

    suspend fun addFavorite(item: FavoriteItem)

    suspend fun removeFavorite(itemId: String, type: FavoriteType)

    suspend fun isFavorite(itemId: String, type: FavoriteType): Boolean

    suspend fun toggleFavorite(item: FavoriteItem)

    suspend fun clearFavorites()
}
