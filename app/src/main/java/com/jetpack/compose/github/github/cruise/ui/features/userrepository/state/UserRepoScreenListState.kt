package com.jetpack.compose.github.github.cruise.ui.features.userrepository.state

import androidx.compose.runtime.Immutable

import com.jetpack.compose.github.github.cruise.domain.model.UserRepo

/**
 * Created by Dinakar Maurya on 2024/05/14.
 */
@Immutable
data class UserRepoViewListState(
    val login: String = "",
    val userRepoList: List<UserRepo> = emptyList(),
    val totalRepos: Int = 0,
    val isShowingForkRepo: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)