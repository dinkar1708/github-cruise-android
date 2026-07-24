package com.jetpack.compose.github.github.cruise.domain.repository

import com.jetpack.compose.github.github.cruise.domain.model.UserProfile
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user operations
 *
 * Located in domain layer following Clean Architecture principles
 */
interface UserRepository {
    suspend fun getUserProfile(userName: String): Flow<UserProfile>

    suspend fun getUserRepositories(
        userName: String,
        page: Int,
        pageSize: Int,
    ): Flow<List<UserRepo>>
}
