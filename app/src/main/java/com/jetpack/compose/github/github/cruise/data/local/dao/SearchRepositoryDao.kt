package com.jetpack.compose.github.github.cruise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jetpack.compose.github.github.cruise.data.local.entity.SearchRepositoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Search Repository operations
 *
 * Caches repository search results for offline access
 * Cache retention: 24 hours
 */
@Dao
interface SearchRepositoryDao {

    /**
     * Get search results by query (reactive)
     */
    @Query("SELECT * FROM search_repositories WHERE query = :query ORDER BY score DESC")
    fun getSearchResults(query: String): Flow<List<SearchRepositoryEntity>>

    /**
     * Get search results by query (one-shot)
     */
    @Query("SELECT * FROM search_repositories WHERE query = :query ORDER BY score DESC")
    suspend fun getSearchResultsOneShot(query: String): List<SearchRepositoryEntity>

    /**
     * Insert or update search result
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResult(searchRepository: SearchRepositoryEntity)

    /**
     * Insert or update multiple search results
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(searchRepositories: List<SearchRepositoryEntity>)

    /**
     * Delete search results for a query
     */
    @Query("DELETE FROM search_repositories WHERE query = :query")
    suspend fun deleteSearchResults(query: String)

    /**
     * Delete all search results
     */
    @Query("DELETE FROM search_repositories")
    suspend fun deleteAllSearchResults()

    /**
     * Delete old search results (older than 24 hours)
     */
    @Query("DELETE FROM search_repositories WHERE cachedAt < :timestamp")
    suspend fun deleteOldSearchResults(timestamp: Long)

    /**
     * Get count of cached results for a query
     */
    @Query("SELECT COUNT(*) FROM search_repositories WHERE query = :query")
    suspend fun getCachedResultCount(query: String): Int
}
