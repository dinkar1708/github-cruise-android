package com.jetpack.compose.github.github.cruise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jetpack.compose.github.github.cruise.data.local.entity.SearchUserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Search User operations
 *
 * Caches search results for offline access
 */
@Dao
interface SearchUserDao {

    /**
     * Get search results by query (reactive)
     */
    @Query("SELECT * FROM search_users WHERE query = :query ORDER BY score DESC")
    fun getSearchResults(query: String): Flow<List<SearchUserEntity>>

    /**
     * Get search results by query (one-shot)
     */
    @Query("SELECT * FROM search_users WHERE query = :query ORDER BY score DESC")
    suspend fun getSearchResultsOneShot(query: String): List<SearchUserEntity>

    /**
     * Insert or update search result
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResult(searchUser: SearchUserEntity)

    /**
     * Insert or update multiple search results
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(searchUsers: List<SearchUserEntity>)

    /**
     * Delete search results for a query
     */
    @Query("DELETE FROM search_users WHERE query = :query")
    suspend fun deleteSearchResults(query: String)

    /**
     * Delete all search results
     */
    @Query("DELETE FROM search_users")
    suspend fun deleteAllSearchResults()

    /**
     * Delete old search results (older than 1 day)
     */
    @Query("DELETE FROM search_users WHERE cachedAt < :timestamp")
    suspend fun deleteOldSearchResults(timestamp: Long)
}
