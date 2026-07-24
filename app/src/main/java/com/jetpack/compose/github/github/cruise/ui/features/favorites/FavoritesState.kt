package com.jetpack.compose.github.github.cruise.ui.features.favorites

import androidx.compose.runtime.Immutable

import com.jetpack.compose.github.github.cruise.domain.model.FavoriteItem

/**
 * UI state for Favorites screen
 */
@Immutable
data class FavoritesState(
    val favorites: List<FavoriteItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
