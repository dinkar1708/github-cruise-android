package com.jetpack.compose.github.github.cruise.ui.features.users.state

import androidx.compose.runtime.Immutable
import com.jetpack.compose.github.github.cruise.domain.model.User

/**
 * Immutable UI state for Users List screen
 *
 * @Immutable annotation helps Compose skip unnecessary recompositions
 */
@Immutable
data class UsersListState(
    val userList: List<User> = emptyList(),
    val lastVisibleItemIndex: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val totalCount: Int = 0
)