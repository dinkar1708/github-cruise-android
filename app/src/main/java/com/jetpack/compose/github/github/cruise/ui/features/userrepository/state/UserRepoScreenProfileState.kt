package com.jetpack.compose.github.github.cruise.ui.features.userrepository.state

import androidx.compose.runtime.Immutable

import com.jetpack.compose.github.github.cruise.domain.model.UserProfile

/**
 * Created by Dinakar Maurya on 2024/05/14.
 */
@Immutable
data class UserRepoScreenProfileState(
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)
