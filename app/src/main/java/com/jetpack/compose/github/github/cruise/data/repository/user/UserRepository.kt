package com.jetpack.compose.github.github.cruise.data.repository.user

import com.jetpack.compose.github.github.cruise.domain.model.UserProfile
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo
import kotlinx.coroutines.flow.Flow

/**
 * Created by Dinakar Maurya on 2024/05/14.
 */
interface UserRepository {

    suspend fun getUserProfile(userName: String): Flow<UserProfile>

    suspend fun getUserRepositories(
        userName: String,
        page: Int,
        pageSize: Int,
    ): Flow<List<UserRepo>>
}
