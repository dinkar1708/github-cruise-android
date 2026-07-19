package com.jetpack.compose.github.github.cruise.ui.features.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.jetpack.compose.github.github.cruise.R
import com.jetpack.compose.github.github.cruise.ui.MainDestinations.USER_REPO_DETAILS_SCREEN_ROUTE
import com.jetpack.compose.github.github.cruise.ui.MainDestinations.USER_REPO_SCREEN_ROUTE
import com.jetpack.compose.github.github.cruise.ui.features.favorites.view.FavoritesListView
import com.jetpack.compose.github.github.cruise.ui.shared.AppActionBarView
import com.jetpack.compose.github.github.cruise.ui.shared.utils.CommonUtils
import com.jetpack.compose.github.github.cruise.ui.theme.GithubCruiseTheme
import com.jetpack.compose.github.github.cruise.ui.theme.Spacing

/**
 * Favorites screen showing saved users and repositories
 */
@Composable
fun FavoritesScreen(
    navController: NavHostController,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val viewState by viewModel.uiState.collectAsStateWithLifecycle()

    FavoritesScreenContent(
        viewState = viewState,
        onFavoriteClick = { favorite ->
            // Navigate based on type
            when (favorite.type) {
                com.jetpack.compose.github.github.cruise.domain.model.FavoriteType.USER -> {
                    navController.navigate("$USER_REPO_SCREEN_ROUTE/${favorite.name}")
                }
                com.jetpack.compose.github.github.cruise.domain.model.FavoriteType.REPOSITORY -> {
                    val encodedUrl = CommonUtils.encodeUrl(favorite.url)
                    navController.navigate("$USER_REPO_DETAILS_SCREEN_ROUTE/$encodedUrl")
                }
            }
        },
        onRemoveFavorite = { itemId, type ->
            viewModel.removeFavorite(itemId, type)
        },
        onClearAll = {
            viewModel.clearAllFavorites()
        }
    )
}

@Composable
private fun FavoritesScreenContent(
    viewState: FavoritesState,
    onFavoriteClick: (com.jetpack.compose.github.github.cruise.domain.model.FavoriteItem) -> Unit,
    onRemoveFavorite: (String, com.jetpack.compose.github.github.cruise.domain.model.FavoriteType) -> Unit,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        AppActionBarView(
            modifier = Modifier.fillMaxWidth(),
            headerText = stringResource(R.string.favorites_title),
            showBackButton = false
        )

        if (viewState.favorites.isEmpty()) {
            EmptyFavoritesView()
        } else {
            FavoritesListView(
                favorites = viewState.favorites,
                onFavoriteClick = onFavoriteClick,
                onRemoveFavorite = onRemoveFavorite,
                onClearAll = onClearAll
            )
        }
    }
}

/**
 * Empty state view when no favorites
 */
@Composable
private fun EmptyFavoritesView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(Spacing.large)
            )
            Text(
                text = stringResource(R.string.favorites_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.favorites_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Spacing.extraLarge)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    GithubCruiseTheme {
        FavoritesScreen(
            navController = rememberNavController()
        )
    }
}
