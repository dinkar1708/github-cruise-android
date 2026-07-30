package com.jetpack.compose.github.github.cruise.domain.repository

import com.jetpack.compose.github.github.cruise.domain.model.UserProfile
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user operations
 *
 * Located in domain layer following Clean Architecture principles
 *
 * Note: Flow is already asynchronous, so suspend modifier is not needed
 */
interface UserRepository {
    fun getUserProfile(userName: String): Flow<UserProfile>

    fun getUserRepositories(
        userName: String,
        page: Int,
        pageSize: Int,
    ): Flow<List<UserRepo>>
}
