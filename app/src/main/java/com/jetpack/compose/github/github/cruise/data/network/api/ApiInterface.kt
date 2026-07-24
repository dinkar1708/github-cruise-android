package com.jetpack.compose.github.github.cruise.data.network.api

import com.jetpack.compose.github.github.cruise.domain.model.RepositoryDetails
import com.jetpack.compose.github.github.cruise.domain.model.SearchRepository
import com.jetpack.compose.github.github.cruise.domain.model.SearchUser
import com.jetpack.compose.github.github.cruise.domain.model.UserProfile
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by Dinakar Maurya on 2024/05/12
 */
interface APIInterface {
    // search apis
    @GET("/search/users")
    suspend fun getSearchUsers(
        @Query("q") userName: String,
        @Query("page") page: Int,
        @Query("per_page") pageSize: Int,
    ): SearchUser

    @GET("/search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") pageSize: Int,
    ): SearchRepository

    @GET("/users/{userName}")
    suspend fun getUserProfile(@Path("userName") userName: String): UserProfile

    @GET("/users/{userName}/repos")
    suspend fun getUserRepositories(
        @Path("userName") userName: String,
        @Query("page") page: Int,
        @Query("per_page") pageSize: Int,
    ): List<UserRepo>

    @GET("/repos/{owner}/{repo}")
    suspend fun getRepositoryDetails(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): RepositoryDetails

}