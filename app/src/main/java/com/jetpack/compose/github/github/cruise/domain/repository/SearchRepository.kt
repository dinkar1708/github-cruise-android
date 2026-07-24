package com.jetpack.compose.github.github.cruise.domain.repository

import com.jetpack.compose.github.github.cruise.domain.model.SearchUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for search operations
 *
 * Located in domain layer following Clean Architecture principles
 * This ensures the domain layer doesn't depend on data layer
 */
interface SearchRepository {
    suspend fun searchUsers(
        userName: String,
        page: Int,
        pageSize: Int,
    ): Flow<SearchUser>
}
