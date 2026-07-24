package com.jetpack.compose.github.github.cruise.domain.usecase

import com.jetpack.compose.github.github.cruise.domain.filter.RepositoryFilter
import com.jetpack.compose.github.github.cruise.domain.model.UserProfile
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo
import com.jetpack.compose.github.github.cruise.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Created by Dinakar Maurya on 2024/05/14
 */
class UserRepositoryUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val repositoryFilter: RepositoryFilter
) {

    suspend fun filterUserRepositories(
        isShowingForkRepo: Boolean,
        login: String, page: Int,
        pageSize: Int,
    ): Flow<List<UserRepo>> {
        return userRepository.getUserRepositories(userName = login, page = page, pageSize = pageSize)
            .map { repos ->
                // filter repositories using filter service
                repositoryFilter.filterByForkStatus(repos, isShowingForkRepo)
            }
    }

    suspend fun getUserProfile(
        login: String,
    ): Flow<UserProfile> =
        userRepository.getUserProfile(userName = login)
}
