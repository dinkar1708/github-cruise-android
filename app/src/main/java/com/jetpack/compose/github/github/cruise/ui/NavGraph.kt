package com.jetpack.compose.github.github.cruise.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jetpack.compose.github.github.cruise.ui.MainDestinations.HOME_SCREEN_ROUTE
import com.jetpack.compose.github.github.cruise.ui.MainDestinations.SPLASH_SCREEN_ROUTE
import com.jetpack.compose.github.github.cruise.ui.MainDestinations.USER_REPO_DETAILS_SCREEN_ROUTE
import com.jetpack.compose.github.github.cruise.ui.MainDestinations.USER_REPO_SCREEN_ROUTE
import com.jetpack.compose.github.github.cruise.ui.MainDestinations.WEBVIEW_SCREEN_ROUTE
import com.jetpack.compose.github.github.cruise.ui.MainDestinationsParams.USER_REPO_DETAILS_SCREEN_PARAM
import com.jetpack.compose.github.github.cruise.ui.MainDestinationsParams.USER_REPO_DETAIL_SCREEN_LOGIN_PARAM
import com.jetpack.compose.github.github.cruise.ui.MainDestinationsParams.WEBVIEW_URL_PARAM
import com.jetpack.compose.github.github.cruise.ui.MainDestinationsParams.WEBVIEW_TITLE_PARAM
import com.jetpack.compose.github.github.cruise.ui.features.favorites.FavoritesScreen
import com.jetpack.compose.github.github.cruise.ui.features.favorites.FavoritesViewModel
import com.jetpack.compose.github.github.cruise.ui.features.home.HomeScreen
import com.jetpack.compose.github.github.cruise.ui.features.repodetails.RepoDetailsScreen
import com.jetpack.compose.github.github.cruise.ui.features.repositorysearch.RepositorySearchScreen
import com.jetpack.compose.github.github.cruise.ui.features.repositorysearch.RepositorySearchViewModel
import com.jetpack.compose.github.github.cruise.ui.features.settings.SettingsScreen
import com.jetpack.compose.github.github.cruise.ui.features.splash.SplashScreen
import com.jetpack.compose.github.github.cruise.ui.features.userrepository.UserRepoScreen
import com.jetpack.compose.github.github.cruise.ui.features.userrepository.UserRepoScreenViewModel
import com.jetpack.compose.github.github.cruise.ui.features.users.UsersListScreen
import com.jetpack.compose.github.github.cruise.ui.features.users.UsersListViewModel
import com.jetpack.compose.github.github.cruise.ui.features.webview.CommonWebViewScreen
import com.jetpack.compose.github.github.cruise.ui.samples.samplesNavGraph
import com.jetpack.compose.github.github.cruise.ui.shared.utils.CommonUtils

/**
 * Navigation destinations for the app
 */
object MainDestinations {
    const val SPLASH_SCREEN_ROUTE = "splash"
    const val HOME_SCREEN_ROUTE = "home"
    const val USER_REPO_SCREEN_ROUTE = "user_repo"
    const val USER_REPO_DETAILS_SCREEN_ROUTE = "user_repo_details"
    const val WEBVIEW_SCREEN_ROUTE = "webview"
}

object MainDestinationsParams {
    const val USER_REPO_DETAIL_SCREEN_LOGIN_PARAM = "login"
    const val USER_REPO_DETAILS_SCREEN_PARAM = "html_url"
    const val WEBVIEW_URL_PARAM = "url"
    const val WEBVIEW_TITLE_PARAM = "title"
}

private const val DEFAULT_TRANSITION_MS = 300
private const val DETAIL_MODAL_TRANSITION_MS = 350

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = SPLASH_SCREEN_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(DEFAULT_TRANSITION_MS)
            ) + fadeIn(animationSpec = tween(DEFAULT_TRANSITION_MS))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(DEFAULT_TRANSITION_MS)
            ) + fadeOut(animationSpec = tween(DEFAULT_TRANSITION_MS))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(DEFAULT_TRANSITION_MS)
            ) + fadeIn(animationSpec = tween(DEFAULT_TRANSITION_MS))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(DEFAULT_TRANSITION_MS)
            ) + fadeOut(animationSpec = tween(DEFAULT_TRANSITION_MS))
        }
    ) {
        composable(SPLASH_SCREEN_ROUTE) {
            SplashScreen(navController)
        }

        composable(HOME_SCREEN_ROUTE) {
            val usersViewModel: UsersListViewModel = hiltViewModel()
            val repositorySearchViewModel: RepositorySearchViewModel = hiltViewModel()
            val favoritesViewModel: FavoritesViewModel = hiltViewModel()

            HomeScreen(
                navController = navController,
                usersListContent = {
                    UsersListScreen(navController, viewModel = usersViewModel)
                },
                repositoriesSearchContent = {
                    RepositorySearchScreen(navController, viewModel = repositorySearchViewModel)
                },
                favoritesContent = {
                    FavoritesScreen(navController, viewModel = favoritesViewModel)
                },
                settingsContent = {
                    SettingsScreen(navController)
                }
            )
        }

        composable(
            "$USER_REPO_SCREEN_ROUTE/{$USER_REPO_DETAIL_SCREEN_LOGIN_PARAM}",
            arguments = listOf(navArgument(USER_REPO_DETAIL_SCREEN_LOGIN_PARAM) {
                type = NavType.StringType
            })
        ) {
            val viewModel: UserRepoScreenViewModel = hiltViewModel()
            UserRepoScreen(
                navController,
                viewModel = viewModel,
                it.arguments?.getString(USER_REPO_DETAIL_SCREEN_LOGIN_PARAM) ?: ""
            )
        }

        // Details Screen: Custom Bottom-Up Modal Transition
        composable(
            "$USER_REPO_DETAILS_SCREEN_ROUTE/{$USER_REPO_DETAILS_SCREEN_PARAM}",
            arguments = listOf(navArgument(USER_REPO_DETAILS_SCREEN_PARAM) {
                type = NavType.StringType
            }),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(DETAIL_MODAL_TRANSITION_MS)
                ) + fadeIn(animationSpec = tween(DETAIL_MODAL_TRANSITION_MS))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(DETAIL_MODAL_TRANSITION_MS)
                ) + fadeOut(animationSpec = tween(DETAIL_MODAL_TRANSITION_MS))
            }
        ) { backStackEntry ->
            val decodedUrl = CommonUtils.decodeUrl(
                backStackEntry.arguments?.getString(USER_REPO_DETAILS_SCREEN_PARAM) ?: ""
            )
            RepoDetailsScreen(navController, htmlUrl = decodedUrl)
        }

        // Web Viewer Screen: Custom Bottom-Up Modal Transition
        composable(
            "$WEBVIEW_SCREEN_ROUTE/{$WEBVIEW_URL_PARAM}/{$WEBVIEW_TITLE_PARAM}",
            arguments = listOf(
                navArgument(WEBVIEW_URL_PARAM) { type = NavType.StringType },
                navArgument(WEBVIEW_TITLE_PARAM) { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(DETAIL_MODAL_TRANSITION_MS)
                ) + fadeIn(animationSpec = tween(DETAIL_MODAL_TRANSITION_MS))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(DETAIL_MODAL_TRANSITION_MS)
                ) + fadeOut(animationSpec = tween(DETAIL_MODAL_TRANSITION_MS))
            }
        ) { backStackEntry ->
            val url = CommonUtils.decodeUrl(
                backStackEntry.arguments?.getString(WEBVIEW_URL_PARAM) ?: ""
            )
            val title = backStackEntry.arguments?.getString(WEBVIEW_TITLE_PARAM) ?: ""
            CommonWebViewScreen(navController, url, title)
        }

        // Samples Navigation Graph
        samplesNavGraph(navController)
    }
}
