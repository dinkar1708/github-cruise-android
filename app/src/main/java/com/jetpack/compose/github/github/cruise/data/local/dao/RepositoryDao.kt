package com.jetpack.compose.github.github.cruise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.jetpack.compose.github.github.cruise.data.local.entity.RepositoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Repository operations
 *
 * Provides reactive database operations with Flow
 */
@Dao
interface RepositoryDao {

    /**
     * Atomically replace cached repositories for an owner
     */
    @Transaction
    suspend fun replaceRepositoriesByOwner(ownerLogin: String, repositories: List<RepositoryEntity>) {
        deleteRepositoriesByOwner(ownerLogin)
        insertRepositories(repositories)
    }

    /**
     * Get repositories by owner login (reactive)
     */
    @Query("SELECT * FROM repositories WHERE ownerLogin = :ownerLogin ORDER BY stargazersCount DESC")
    fun getRepositoriesByOwner(ownerLogin: String): Flow<List<RepositoryEntity>>

    /**
     * Get repositories by owner login (one-shot)
     */
    @Query("SELECT * FROM repositories WHERE ownerLogin = :ownerLogin ORDER BY stargazersCount DESC")
    suspend fun getRepositoriesByOwnerOneShot(ownerLogin: String): List<RepositoryEntity>

    /**
     * Get repository by ID
     */
    @Query("SELECT * FROM repositories WHERE id = :id")
    fun getRepositoryById(id: Long): Flow<RepositoryEntity?>

    /**
     * Insert or update repository
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepository(repository: RepositoryEntity)

    /**
     * Insert or update multiple repositories
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositories(repositories: List<RepositoryEntity>)

    /**
     * Delete repositories by owner
     */
    @Query("DELETE FROM repositories WHERE ownerLogin = :ownerLogin")
    suspend fun deleteRepositoriesByOwner(ownerLogin: String)

    /**
     * Delete all repositories
     */
    @Query("DELETE FROM repositories")
    suspend fun deleteAllRepositories()

    /**
     * Delete old cached repositories (older than 7 days)
     */
    @Query("DELETE FROM repositories WHERE cachedAt < :timestamp")
    suspend fun deleteOldRepositories(timestamp: Long)

    /**
     * Get all cached repositories
     */
    @Query("SELECT * FROM repositories ORDER BY cachedAt DESC")
    fun getAllRepositories(): Flow<List<RepositoryEntity>>
}
