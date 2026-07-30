package com.jetpack.compose.github.github.cruise.data.repository.search

import com.jetpack.compose.github.github.cruise.data.local.dao.SearchUserDao
import com.jetpack.compose.github.github.cruise.data.local.entity.toDomainModel
import com.jetpack.compose.github.github.cruise.data.local.entity.toEntity
import com.jetpack.compose.github.github.cruise.data.network.NetworkDataSource
import com.jetpack.compose.github.github.cruise.di.IoDispatcher
import com.jetpack.compose.github.github.cruise.domain.model.SearchUser
import com.jetpack.compose.github.github.cruise.domain.repository.SearchRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of SearchRepository
 *
 * Implements the domain layer interface from domain/repository
 * Follows Clean Architecture: domain doesn't depend on data layer
 *
 * Implements offline-first architecture with Room Database:
 * - Try to load from local cache first
 * - Fetch from network and update cache
 * - Falls back to cache on network error
 *
 * Threading: Network calls executed on IO dispatcher via flowOn()
 */
class SearchRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val searchUserDao: SearchUserDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SearchRepository {

    override fun searchUsers(
        userName: String,
        page: Int,
        pageSize: Int,
    ): Flow<SearchUser> = flow {
        val normalizedQuery = userName.lowercase()

        // For first page, try to get from cache
        if (page == 1) {
            val cachedResults = searchUserDao.getSearchResultsOneShot(normalizedQuery)
            if (cachedResults.isNotEmpty()) {
                // Map cached results to SearchUser and emit
                val searchUser = SearchUser(
                    totalCount = cachedResults.size,
                    incompleteResults = false,
                    users = cachedResults.map { it.toDomainModel() }
                )
                emit(searchUser)
            }
        }

        // Then fetch from network and update cache
        try {
            val searchResult = networkDataSource.searchUser(userName, page, pageSize)

            // Clear old cache and insert new data for first page
            if (page == 1) {
                searchUserDao.deleteSearchResults(normalizedQuery)
                searchUserDao.insertSearchResults(
                    searchResult.users.map { it.toEntity(normalizedQuery) }
                )
            }

            emit(searchResult)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching search results from network")
            // If network fails on first page and we have no cache, throw error
            if (page == 1) {
                val cachedResults = searchUserDao.getSearchResultsOneShot(normalizedQuery)
                if (cachedResults.isEmpty()) {
                    throw e
                }
            } else {
                throw e // For pagination, always throw on error
            }
        }
    }.catch { e ->
        Timber.e(e, "Error search Users $e")
        throw e
    }.flowOn(ioDispatcher) // Execute on IO dispatcher
}
