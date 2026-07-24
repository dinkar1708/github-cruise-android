package com.jetpack.compose.github.github.cruise.ui.features.userrepository

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.jetpack.compose.github.github.cruise.R
import com.jetpack.compose.github.github.cruise.domain.model.UserProfile
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo
import com.jetpack.compose.github.github.cruise.ui.MainDestinations.USER_REPO_DETAILS_SCREEN_ROUTE
import com.jetpack.compose.github.github.cruise.ui.features.userrepository.view.UserProfileView
import com.jetpack.compose.github.github.cruise.ui.features.userrepository.view.UserRepoListView
import com.jetpack.compose.github.github.cruise.ui.shared.AppActionBarView
import com.jetpack.compose.github.github.cruise.ui.shared.HorizontalLineView
import com.jetpack.compose.github.github.cruise.ui.shared.StateContentBox
import com.jetpack.compose.github.github.cruise.ui.shared.utils.CommonUtils
import com.jetpack.compose.github.github.cruise.ui.theme.GithubCruiseTheme
import com.jetpack.compose.github.github.cruise.ui.widgets.FavoriteButton
import com.jetpack.compose.github.github.cruise.domain.model.FavoriteItem
import com.jetpack.compose.github.github.cruise.domain.model.FavoriteType
import androidx.hilt.navigation.compose.hiltViewModel
import com.jetpack.compose.github.github.cruise.ui.features.favorites.FavoritesViewModel

/**
 * Created by Dinakar Maurya on 2024/05/12.
 */
@Composable
fun UserRepoScreen(
    navController: NavHostController,
    viewModel: UserRepoScreenViewModel,
    login: String,
    favoritesViewModel: FavoritesViewModel = hiltViewModel()
) {
    val viewState by viewModel.uiStateRepository.collectAsStateWithLifecycle()
    val viewStateProfile by viewModel.uiStateProfile.collectAsStateWithLifecycle()
    val favoritesState by favoritesViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = login) {
        viewModel.loadApiData(login)
    }

    Column(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppActionBarView(
            modifier = Modifier
                .fillMaxWidth(),
            headerText = login,
            showBackButton = true,
            onBackClick = {
                navController.popBackStack()
            },
            actions = {
                // Favorite button for user profile
                viewStateProfile.userProfile?.let { profile ->
                    val isFavorite = favoritesState.favorites.any {
                        it.id == profile.id.toString() && it.type == FavoriteType.USER
                    }
                    FavoriteButton(
                        isFavorite = isFavorite,
                        onToggleFavorite = {
                            if (isFavorite) {
                                favoritesViewModel.removeFavorite(profile.id.toString(), FavoriteType.USER)
                            } else {
                                val favoriteItem = FavoriteItem(
                                    id = profile.id.toString(),
                                    type = FavoriteType.USER,
                                    name = profile.login,
                                    description = profile.name,
                                    avatarUrl = profile.avatarUrl,
                                    url = "https://github.com/${profile.login}"
                                )
                                favoritesViewModel.addFavorite(favoriteItem)
                            }
                        }
                    )
                }
            }
        )
        UserRepoListScreenContentsProfile(
            isLoading = viewStateProfile.isLoading,
            userProfile = viewStateProfile.userProfile,
            errorMessage = viewStateProfile.errorMessage
        )

        HorizontalLineView()

        UserRepoListScreenContents(
            isLoading = viewState.isLoading,
            userRepoList = viewState.userRepoList,
            totalRepos = viewState.totalRepos,
            errorMessage = viewState.errorMessage,
            isShowForkRepo =
            {
                viewModel.filterRepositories(it, login)
            },
            openRepoDetails = {
                val encodedUrl = CommonUtils.encodeUrl(it)
                navController.navigate("$USER_REPO_DETAILS_SCREEN_ROUTE/$encodedUrl")
            }
        )
    }
}


@Composable
fun UserRepoListScreenContents(
    isLoading: Boolean,
    userRepoList: List<UserRepo>,
    totalRepos: Int,
    errorMessage: String,
    isShowForkRepo: (Boolean) -> Unit,
    openRepoDetails: (String) -> Unit
) {
    var isShowingFork by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    R.string.user_repository_title_repositories,
                    userRepoList.size
                ),
                style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onBackground)
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                modifier = Modifier.padding(horizontal = 8.dp),
                checked = isShowingFork,
                onCheckedChange = {
                    isShowingFork = it
                    isShowForkRepo(isShowingFork)
                }
            )
            Text(
                text = stringResource(
                    R.string.user_repository_fork_status,
                    if (isShowingFork) "On" else "Off"
                ),
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onBackground)
            )
        }

        // Fixed header showing repository count
        if (userRepoList.isNotEmpty() && totalRepos > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.repo_list_results_count,
                        userRepoList.size,
                        totalRepos
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        StateContentBox(
            isLoading = isLoading,
            errorMessage = errorMessage
        ) {
            UserRepoListView(
                modifier = Modifier.fillMaxWidth(),
                userRepoList = userRepoList,
                openRepoDetails = openRepoDetails
            )
        }
    }
}


@Composable
fun UserRepoListScreenContentsProfile(
    isLoading: Boolean,
    userProfile: UserProfile?,
    errorMessage: String,
) {
    val configuration = LocalConfiguration.current
    // use 25% height of the screen
    val screenHeight = configuration.screenHeightDp.dp / 4
    StateContentBox(
        isLoading = isLoading,
        errorMessage = errorMessage
    ) {
        Box(
            modifier = Modifier
                .heightIn(max = screenHeight)
        ) {
            // assume user profile is not null at this point
            if (userProfile != null) {
                UserProfileView(userProfile)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserRepoListHeaderPreview() {

    val userRepoList = mutableListOf(
        UserRepo(
            id = 1,
            name = "Repo",
            language = "JAVA",
            stargazersCount = "10",
            description = "Android Repo Desc", fork = false
        )
    )
    val userProfile = UserProfile(
        id = 1,
        followers = 10,
        following = 20,
        name = "Dinakar Maurya",
        avatarUrl = "url",
        login = "dinkar1708"
    )


    GithubCruiseTheme {
        Surface {
            Column {
                UserRepoListScreenContentsProfile(
                    userProfile = userProfile,
                    isLoading = false,
                    errorMessage = "Dinkar Maurya"
                )
                UserRepoListScreenContents(
                    isLoading = false,
                    userRepoList = userRepoList,
                    totalRepos = 50,
                    errorMessage = "Error occured.",
                    isShowForkRepo = {},
                    openRepoDetails = {}
                )
            }
        }
    }
}
