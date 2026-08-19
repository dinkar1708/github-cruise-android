package com.jetpack.compose.github.github.cruise.ui.features.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.navigation.NavHostController
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag as semanticTestTag
import androidx.compose.ui.tooling.preview.Preview
import com.jetpack.compose.github.github.cruise.R
import com.jetpack.compose.github.github.cruise.ui.theme.GithubCruiseTheme

/**
 * Bottom navigation item data class
 */
data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean = false,
    val badgeCount: Int? = null
)

/**
 * Home screen with bottom navigation
 *
 * @param navController Navigation controller for navigating to other screens
 * @param usersListContent Content for the Users tab
 * @param repositoriesSearchContent Content for the Repository Search tab
 * @param favoritesContent Content for the Favorites tab
 * @param settingsContent Content for the Settings tab
 */
@Composable
fun HomeScreen(
    navController: NavHostController,
    usersListContent: @Composable () -> Unit,
    repositoriesSearchContent: @Composable () -> Unit,
    favoritesContent: @Composable () -> Unit,
    settingsContent: @Composable () -> Unit
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        BottomNavItem(
            title = stringResource(R.string.bottom_nav_home),
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            title = stringResource(R.string.bottom_nav_repositories),
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search
        ),
        BottomNavItem(
            title = stringResource(R.string.bottom_nav_favorites),
            selectedIcon = Icons.Filled.Star,
            unselectedIcon = Icons.Outlined.Star
        ),
        BottomNavItem(
            title = stringResource(R.string.bottom_nav_settings),
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

    Scaffold(
        floatingActionButton = {
            // Show FAB only on first tab (Home)
            if (selectedTabIndex == 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        navController.navigate("samples_list")
                    },
                    icon = { Icon(Icons.Filled.List, "Open Samples") },
                    text = { Text("Open Samples") }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = com.jetpack.compose.github.github.cruise.ui.theme.Elevation.level2
            ) {
                tabs.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier.semantics {
                            semanticTestTag = "tab_${item.title.lowercase().replace(" ", "_")}"
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (item.badgeCount != null) {
                                        Badge {
                                            Text(text = item.badgeCount.toString())
                                        }
                                    } else if (item.hasNews) {
                                        Badge()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (selectedTabIndex == index) {
                                        item.selectedIcon
                                    } else {
                                        item.unselectedIcon
                                    },
                                    contentDescription = item.title
                                )
                            }
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Use when statement - ViewModels are scoped at NavGraph level
            // so state will be preserved even when tabs are removed from composition
            when (selectedTabIndex) {
                0 -> usersListContent()
                1 -> repositoriesSearchContent()
                2 -> favoritesContent()
                3 -> settingsContent()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GithubCruiseTheme {
        // Note: Preview doesn't include navController functionality
        // For full testing, run the app
        Box {
            Text("Preview: Run app to see navigation")
        }
    }
}
