package com.jetpack.compose.github.github.cruise.data.repository.user

import com.jetpack.compose.github.github.cruise.data.local.dao.RepositoryDao
import com.jetpack.compose.github.github.cruise.data.local.dao.UserDao
import com.jetpack.compose.github.github.cruise.data.local.entity.toDomainModel
import com.jetpack.compose.github.github.cruise.data.local.entity.toEntity
import com.jetpack.compose.github.github.cruise.data.network.NetworkDataSource
import com.jetpack.compose.github.github.cruise.di.IoDispatcher
import com.jetpack.compose.github.github.cruise.domain.model.UserProfile
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo
import com.jetpack.compose.github.github.cruise.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Dinakar Maurya on 2024/05/14.
 *
 * Implements offline-first architecture with Room Database:
 * - Try to load from local cache first
 * - Fetch from network and update cache
 * - Falls back to cache on network error
 *
 * Threading: Network calls executed on IO dispatcher via flowOn()
 */
class UserRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    private val userDao: UserDao,
    private val repositoryDao: RepositoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRepository {

    override fun getUserProfile(userName: String): Flow<UserProfile> = flow {
        // First, try to get from cache
        val cachedUser = userDao.getUserByLoginOneShot(userName)
        if (cachedUser != null) {
            emit(cachedUser.toDomainModel())
        }

        // Then fetch from network and update cache
        try {
            val user = networkDataSource.getUserProfile(userName)
            userDao.insertUser(user.toEntity())
            emit(user)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching user profile from network")
            // If network fails and we have no cache, throw error
            if (cachedUser == null) {
                throw e
            }
            // Otherwise, we already emitted the cached version
        }
    }.catch { e ->
        Timber.e(e, "Error getUserProfile $e")
        throw e
    }.flowOn(ioDispatcher) // Execute on IO dispatcher

    override fun getUserRepositories(
        userName: String,
        page: Int,
        pageSize: Int,
    ): Flow<List<UserRepo>> = flow {
        // For first page, try to get from cache
        if (page == 1) {
            val cachedRepos = repositoryDao.getRepositoriesByOwnerOneShot(userName)
            if (cachedRepos.isNotEmpty()) {
                emit(cachedRepos.map { it.toDomainModel() })
            }
        }

        // Then fetch from network and update cache
        try {
            val repos = networkDataSource.getUserRepositories(userName, page, pageSize)

            // Clear old cache and insert new data for first page
            if (page == 1) {
                repositoryDao.deleteRepositoriesByOwner(userName)
            }
            repositoryDao.insertRepositories(repos.map { it.toEntity() })

            emit(repos)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching repositories from network")
            // If network fails on first page and we have no cache, throw error
            if (page == 1) {
                val cachedRepos = repositoryDao.getRepositoriesByOwnerOneShot(userName)
                if (cachedRepos.isEmpty()) {
                    throw e
                }
            } else {
                throw e // For pagination, always throw on error
            }
        }
    }.catch { e ->
        Timber.e(e, "Error search getUserRepositories $e")
        throw e
    }.flowOn(ioDispatcher) // Execute on IO dispatcher
}