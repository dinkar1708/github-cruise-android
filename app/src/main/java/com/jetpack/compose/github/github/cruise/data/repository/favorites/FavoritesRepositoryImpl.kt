package com.jetpack.compose.github.github.cruise.data.repository.favorites

import com.jetpack.compose.github.github.cruise.data.local.dao.FavoriteDao
import com.jetpack.compose.github.github.cruise.data.local.entity.toDomainModel
import com.jetpack.compose.github.github.cruise.data.local.entity.toEntity
import com.jetpack.compose.github.github.cruise.di.IoDispatcher
import com.jetpack.compose.github.github.cruise.domain.model.FavoriteItem
import com.jetpack.compose.github.github.cruise.domain.model.FavoriteType
import com.jetpack.compose.github.github.cruise.domain.repository.FavoritesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of FavoritesRepository using Room Database
 *
 * Migrated from DataStore to Room for:
 * - Proper database storage instead of JSON strings
 * - Better querying capabilities
 * - Relational data support
 * - Offline-first architecture
 */
@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : FavoritesRepository {

    // Create a scope for StateFlow
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    // Convert Room Flow to StateFlow
    override val favorites: StateFlow<List<FavoriteItem>> = favoriteDao.getAllFavorites()
        .map { entities -> entities.map { it.toDomainModel() } }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    override suspend fun addFavorite(item: FavoriteItem) {
        withContext(dispatcher) {
            favoriteDao.insertFavorite(item.toEntity())
        }
    }

    override suspend fun removeFavorite(itemId: String, type: FavoriteType) {
        withContext(dispatcher) {
            favoriteDao.deleteFavorite(itemId, type.name)
        }
    }

    override suspend fun isFavorite(itemId: String, type: FavoriteType): Boolean {
        return withContext(dispatcher) {
            favoriteDao.isFavorited(itemId, type.name)
        }
    }

    override suspend fun toggleFavorite(item: FavoriteItem) {
        withContext(dispatcher) {
            if (isFavorite(item.id, item.type)) {
                favoriteDao.deleteFavorite(item.id, item.type.name)
            } else {
                favoriteDao.insertFavorite(item.toEntity())
            }
        }
    }

    override suspend fun clearFavorites() {
        withContext(dispatcher) {
            favoriteDao.deleteAllFavorites()
        }
    }
}
