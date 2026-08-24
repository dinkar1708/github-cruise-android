package com.jetpack.compose.github.github.cruise.data.repository.repositorysearch

import com.jetpack.compose.github.github.cruise.data.local.dao.SearchRepositoryDao
import com.jetpack.compose.github.github.cruise.data.local.entity.toDomainModel
import com.jetpack.compose.github.github.cruise.data.local.entity.toSearchEntity
import com.jetpack.compose.github.github.cruise.data.network.NetworkDataSource
import com.jetpack.compose.github.github.cruise.di.IoDispatcher
import com.jetpack.compose.github.github.cruise.domain.model.SearchRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of RepositorySearchRepository
 * Feature 2.1: Repository Search Screen
 *
 * Implements offline-first caching strategy:
 * 1. Try network first
 * 2. Cache results for 24 hours
 * 3. Fallback to cache on network error
 *
 * Threading: Network calls executed on IO dispatcher via flowOn()
 */
class RepositorySearchRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val searchRepositoryDao: SearchRepositoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RepositorySearchRepository {

    companion object {
        private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L // 24 hours
    }

    override suspend fun searchRepositories(
        query: String,
        page: Int,
        pageSize: Int,
    ): Flow<SearchRepository> = flow {
        try {
            // Clean old cache entries (older than 24 hours)
            val expiryTime = System.currentTimeMillis() - CACHE_DURATION_MS
            searchRepositoryDao.deleteOldSearchResults(expiryTime)

            // Try network first (network-first strategy)
            val networkResult = networkDataSource.searchRepositories(
                query = query,
                page = page,
                pageSize = pageSize
            )

            // Cache new results atomically for offline access
            val searchEntities = networkResult.repositories.map { repo ->
                repo.toSearchEntity(query)
            }

            if (page == 1) {
                // Atomically clear old results and insert new ones in a single transaction
                searchRepositoryDao.replaceSearchResults(query.lowercase(), searchEntities)
            } else {
                searchRepositoryDao.insertSearchResults(searchEntities)
            }

            emit(networkResult)

        } catch (e: Exception) {
            Timber.w(e, "Network error, trying cache for query: $query")

            // Fallback to cache on network error
            val cachedResults = searchRepositoryDao.getSearchResultsOneShot(query.lowercase())

            if (cachedResults.isNotEmpty()) {
                Timber.d("Returning ${cachedResults.size} cached results for: $query")
                val repositories = cachedResults.map { it.toDomainModel() }
                emit(
                    SearchRepository(
                        totalCount = cachedResults.size,
                        incompleteResults = true, // Mark as incomplete since it's cached
                        repositories = repositories
                    )
                )
            } else {
                // No cache available, rethrow the error
                Timber.e(e, "No cached results available for: $query")
                throw e
            }
        }
    }.flowOn(ioDispatcher) // Execute on IO dispatcher
}
