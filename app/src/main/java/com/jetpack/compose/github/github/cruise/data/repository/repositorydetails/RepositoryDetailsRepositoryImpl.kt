package com.jetpack.compose.github.github.cruise.data.repository.repositorydetails

import com.jetpack.compose.github.github.cruise.data.network.NetworkDataSource
import com.jetpack.compose.github.github.cruise.di.IoDispatcher
import com.jetpack.compose.github.github.cruise.domain.model.RepositoryDetails
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementation of RepositoryDetailsRepository
 *
 * Threading: Network calls executed on IO dispatcher via flowOn()
 */
class RepositoryDetailsRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RepositoryDetailsRepository {

    override suspend fun getRepositoryDetails(
        owner: String,
        repo: String
    ): Flow<RepositoryDetails> = flow {
        // Network call executed on IO thread via flowOn below
        val details = networkDataSource.getRepositoryDetails(owner, repo)
        emit(details)
    }.catch { e ->
        Timber.e(e, "Error fetching repository details for $owner/$repo")
        throw e
    }.flowOn(ioDispatcher) // Execute network call on IO dispatcher
}
