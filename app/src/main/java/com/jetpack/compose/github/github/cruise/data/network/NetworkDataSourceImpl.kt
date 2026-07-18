package com.jetpack.compose.github.github.cruise.data.network

import com.jetpack.compose.github.github.cruise.domain.model.SearchUser
import com.jetpack.compose.github.github.cruise.domain.model.UserProfile
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo
import com.jetpack.compose.github.github.cruise.data.network.api.APIInterface

/**
 * Created by Dinakar Maurya on 2024/05/12
 */
class NetworkDataSourceImpl(
    private val api: APIInterface
) : NetworkDataSource {
    override suspend fun searchUser(
        userName: String,
        page: Int,
        pageSize: Int,
    ): SearchUser {
        return api.getSearchUsers(userName, page, pageSize)
    }

    override suspend fun getUserRepositories(
        userName: String,
        page: Int,
        pageSize: Int,
    ): List<UserRepo> {
        return api.getUserRepositories(userName)
    }

    override suspend fun getUserProfile(userName: String): UserProfile {
        return api.getUserProfile(userName)
    }
}