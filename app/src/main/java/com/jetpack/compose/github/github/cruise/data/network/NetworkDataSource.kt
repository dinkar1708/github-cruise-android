package com.jetpack.compose.github.github.cruise.data.network

import com.jetpack.compose.github.github.cruise.domain.model.SearchUser
import com.jetpack.compose.github.github.cruise.domain.model.UserProfile
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo

/**
 * Created by Dinakar Maurya on 2024/05/12
 */
interface NetworkDataSource {
    suspend fun searchUser(
        userName: String,
        page: Int,
        pageSize: Int,
    ): SearchUser

    suspend fun getUserRepositories(
        userName: String,
        page: Int,
        pageSize: Int,
    ): List<UserRepo>

    suspend fun getUserProfile(userName: String): UserProfile

}