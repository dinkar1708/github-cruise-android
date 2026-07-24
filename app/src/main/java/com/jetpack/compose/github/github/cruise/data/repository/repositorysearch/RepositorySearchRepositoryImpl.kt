package com.jetpack.compose.github.github.cruise.data.repository.repositorysearch

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
 * Threading: Network calls executed on IO dispatcher via flowOn()
 */
class RepositorySearchRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RepositorySearchRepository {

    override suspend fun searchRepositories(
        query: String,
        page: Int,
        pageSize: Int,
    ): Flow<SearchRepository> = flow {
        // Network call executed on IO thread via flowOn below
        val repositories = networkDataSource.searchRepositories(
            query = query,
            page = page,
            pageSize = pageSize
        )
        emit(repositories)
    }.catch { e ->
        Timber.e(e, "Error searching repositories: $e")
        throw e
    }.flowOn(ioDispatcher) // Execute network call on IO dispatcher
}
