package com.jetpack.compose.github.github.cruise.ui.features.repositorysearch.state

import androidx.compose.runtime.Immutable

import com.jetpack.compose.github.github.cruise.domain.model.Repository

/**
 * State for Repository Search Screen
 * Feature 2.1: Repository Search Screen
 */
@Immutable
data class RepositorySearchState(
    val repositories: List<Repository> = emptyList(),
    val lastVisibleItemIndex: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val searchQuery: String = "",
    val hasMorePages: Boolean = true
)
