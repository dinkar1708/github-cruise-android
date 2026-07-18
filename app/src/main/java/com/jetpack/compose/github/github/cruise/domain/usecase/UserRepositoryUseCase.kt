package com.jetpack.compose.github.github.cruise.domain.usecase

import com.jetpack.compose.github.github.cruise.domain.model.UserProfile
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo
import com.jetpack.compose.github.github.cruise.data.repository.user.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Created by Dinakar Maurya on 2024/05/14
 */
class UserRepositoryUseCase @Inject constructor(private val userRepository: UserRepository) {

    suspend fun filterUserRepositories(
        isShowingForkRepo: Boolean,
        login: String, page: Int,
        pageSize: Int,
    ): Flow<List<UserRepo>> {
        return userRepository.getUserRepositories(login, page, pageSize)
            .map { repos ->
                // filter repositories
                repos.filter { isShowingForkRepo == it.fork }
            }
    }

    suspend fun getUserProfile(
        login: String,
    ): Flow<UserProfile> =
        userRepository.getUserProfile(login)
}
