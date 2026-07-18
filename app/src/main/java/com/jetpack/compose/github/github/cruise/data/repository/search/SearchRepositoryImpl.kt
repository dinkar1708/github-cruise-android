package com.jetpack.compose.github.github.cruise.data.repository.search

import com.jetpack.compose.github.github.cruise.di.DefaultDispatcher
import com.jetpack.compose.github.github.cruise.domain.model.SearchUser
import com.jetpack.compose.github.github.cruise.data.network.NetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Dinakar Maurya on 2024/05/13
 */
class SearchRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) : SearchRepository {
    override suspend fun searchUsers(
        userName: String, page: Int,
        pageSize: Int,
    ): Flow<SearchUser> = flow {
        val users = withContext(dispatcher) {
            networkDataSource.searchUser(userName = userName, page = page, pageSize = pageSize)
        }
        emit(users)
    }.catch { e ->
        Timber.e(e, "Error search Users $e")
        // TODO handle error better way, common error class?
        throw e
    }.flowOn(dispatcher)

}
