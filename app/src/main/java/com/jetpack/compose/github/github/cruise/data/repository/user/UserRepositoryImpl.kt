package com.jetpack.compose.github.github.cruise.data.repository.user

import com.jetpack.compose.github.github.cruise.di.IoDispatcher
import com.jetpack.compose.github.github.cruise.domain.model.UserProfile
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo
import com.jetpack.compose.github.github.cruise.domain.repository.UserRepository
import com.jetpack.compose.github.github.cruise.data.network.NetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Dinakar Maurya on 2024/05/14.
 *
 * Threading: Network calls executed on IO dispatcher via flowOn()
 */
class UserRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {
    override suspend fun getUserProfile(userName: String): Flow<UserProfile> = flow {
        // Network call executed on IO thread via flowOn below
        val users = networkDataSource.getUserProfile(userName)
        emit(users)
    }.catch { e ->
        Timber.e(e, "Error getUserProfile $e")
        throw e
    }.flowOn(ioDispatcher) // Execute network call on IO dispatcher

    override suspend fun getUserRepositories(
        userName: String, page: Int,
        pageSize: Int,
    ): Flow<List<UserRepo>> = flow {
        // Network call executed on IO thread via flowOn below
        val users = networkDataSource.getUserRepositories(userName, page, pageSize)
        emit(users)
    }.catch { e ->
        Timber.e(e, "Error search getUserRepositories $e")
        throw e
    }.flowOn(ioDispatcher) // Execute network call on IO dispatcher
}