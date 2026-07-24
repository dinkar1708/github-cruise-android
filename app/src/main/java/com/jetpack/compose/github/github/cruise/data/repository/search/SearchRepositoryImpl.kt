package com.jetpack.compose.github.github.cruise.data.repository.search

import com.jetpack.compose.github.github.cruise.di.IoDispatcher
import com.jetpack.compose.github.github.cruise.domain.model.SearchUser
import com.jetpack.compose.github.github.cruise.domain.repository.SearchRepository
import com.jetpack.compose.github.github.cruise.data.network.NetworkDataSource
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
 * Threading: Network calls executed on IO dispatcher via flowOn()
 */
class SearchRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SearchRepository {
    override suspend fun searchUsers(
        userName: String, page: Int,
        pageSize: Int,
    ): Flow<SearchUser> = flow {
        // Network call executed on IO thread via flowOn below
        val users = networkDataSource.searchUser(userName = userName, page = page, pageSize = pageSize)
        emit(users)
    }.catch { e ->
        Timber.e(e, "Error search Users $e")
        throw e
    }.flowOn(ioDispatcher) // Execute network call on IO dispatcher

}
