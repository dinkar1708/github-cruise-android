package com.jetpack.compose.github.github.cruise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jetpack.compose.github.github.cruise.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Favorite operations
 *
 * Provides reactive database operations with Flow
 */
@Dao
interface FavoriteDao {

    /**
     * Get all favorites (reactive, ordered by most recent first)
     */
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    /**
     * Get favorite by id and type (reactive)
     */
    @Query("SELECT * FROM favorites WHERE id = :id AND type = :type LIMIT 1")
    fun getFavorite(id: String, type: String): Flow<FavoriteEntity?>

    /**
     * Check if item is favorited (one-shot)
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id AND type = :type)")
    suspend fun isFavorited(id: String, type: String): Boolean

    /**
     * Get favorites by type (reactive)
     */
    @Query("SELECT * FROM favorites WHERE type = :type ORDER BY timestamp DESC")
    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>>

    /**
     * Insert or update favorite
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    /**
     * Delete favorite by id and type
     */
    @Query("DELETE FROM favorites WHERE id = :id AND type = :type")
    suspend fun deleteFavorite(id: String, type: String)

    /**
     * Delete all favorites
     */
    @Query("DELETE FROM favorites")
    suspend fun deleteAllFavorites()

    /**
     * Get favorites count
     */
    @Query("SELECT COUNT(*) FROM favorites")
    fun getFavoritesCount(): Flow<Int>
}
