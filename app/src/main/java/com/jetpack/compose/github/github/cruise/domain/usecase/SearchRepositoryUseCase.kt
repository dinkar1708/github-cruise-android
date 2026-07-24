package com.jetpack.compose.github.github.cruise.domain.usecase

import com.jetpack.compose.github.github.cruise.domain.model.SearchUser
import com.jetpack.compose.github.github.cruise.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for searching GitHub users
 *
 * Created by Dinakar Maurya on 2024/05/13
 *
 * Note: Input validation is added via SearchInputValidator class (see domain/validation)
 * Future enhancement: Add Result wrapper for better error handling
 */
class SearchRepositoryUseCase @Inject constructor(
    private val searchRepository: SearchRepository
) {
    suspend fun searchUsers(
        userName: String,
        page: Int,
        pageSize: Int,
    ): Flow<SearchUser> =
        searchRepository.searchUsers(userName = userName, page = page, pageSize = pageSize)
}
