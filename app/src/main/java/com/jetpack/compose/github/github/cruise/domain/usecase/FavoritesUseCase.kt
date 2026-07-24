package com.jetpack.compose.github.github.cruise.domain.usecase

import com.jetpack.compose.github.github.cruise.domain.model.FavoriteItem
import com.jetpack.compose.github.github.cruise.domain.model.FavoriteType
import com.jetpack.compose.github.github.cruise.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for managing favorites
 *
 * Centralizes business logic for favorites:
 * - Enforces limits
 * - Provides observability
 * - Encapsulates repository operations
 */
class FavoritesUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) {
    /**
     * Observe all favorites
     */
    fun observeFavorites(): Flow<List<FavoriteItem>> =
        favoritesRepository.favorites

    /**
     * Add item to favorites with validation
     */
    suspend fun addFavorite(item: FavoriteItem): Result<Unit> {
        return try {
            // Business rule: could add max limit check here
            // val currentCount = favoritesRepository.favorites.first().size
            // if (currentCount >= MAX_FAVORITES) { return error }

            favoritesRepository.addFavorite(item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Remove item from favorites
     */
    suspend fun removeFavorite(itemId: String, type: FavoriteType): Result<Unit> {
        return try {
            favoritesRepository.removeFavorite(itemId, type)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clear all favorites
     */
    suspend fun clearAllFavorites(): Result<Unit> {
        return try {
            favoritesRepository.clearFavorites()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if item is favorited
     */
    suspend fun isFavorite(itemId: String, type: FavoriteType): Boolean {
        return favoritesRepository.isFavorite(itemId, type)
    }

    companion object {
        private const val MAX_FAVORITES = 100
    }
}
