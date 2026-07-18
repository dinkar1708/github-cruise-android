package com.jetpack.compose.github.github.cruise.data.repository.search

import com.jetpack.compose.github.github.cruise.domain.model.SearchUser
import kotlinx.coroutines.flow.Flow

/**
 * Created by Dinakar Maurya on 2024/05/13
 */
interface SearchRepository {
    suspend fun searchUsers(
        userName: String, page: Int,
        pageSize: Int,
    ): Flow<SearchUser>
}