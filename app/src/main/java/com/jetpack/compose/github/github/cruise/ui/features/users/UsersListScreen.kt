package com.jetpack.compose.github.github.cruise.ui.features.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.jetpack.compose.github.github.cruise.R
import com.jetpack.compose.github.github.cruise.domain.model.User
import com.jetpack.compose.github.github.cruise.ui.MainDestinations.USER_REPO_SCREEN_ROUTE
import com.jetpack.compose.github.github.cruise.ui.features.users.view.UsersListView
import com.jetpack.compose.github.github.cruise.ui.shared.AppActionBarView
import com.jetpack.compose.github.github.cruise.ui.shared.SearchBox
import com.jetpack.compose.github.github.cruise.ui.shared.StateContentBox
import com.jetpack.compose.github.github.cruise.ui.theme.GithubCruiseTheme
import com.jetpack.compose.github.github.cruise.ui.theme.Spacing

/**
 * Created by Dinakar Maurya on 2024/05/12.
 */
@Composable
fun UsersListScreen(
    navController: NavHostController,
    viewModel: UsersListViewModel
) {
    val viewState by viewModel.uiState.collectAsStateWithLifecycle()

    UsersListScreenContent(
        isLoading = viewState.isLoading,
        userList = viewState.userList,
        lastVisibleItemIndex = viewState.lastVisibleItemIndex,
        errorMessage = viewState.errorMessage,
        onItemClick = {
            navController.navigate("${USER_REPO_SCREEN_ROUTE}/${it.login}")
        },
        onSearchSubmitted = { viewModel.searchUsers(it) },
        onClearInput = {
            // clear text
            viewModel.searchUsers("")
        },
        onListScrolledToEnd = { i ->
            viewModel.updateLastVisibleIndex(i)
            viewModel.loadNextPage()
        }
    )
}

/**
 * Users list screen content with search and pagination
 *
 * Design principles:
 * - Clear visual hierarchy with AppBar, search, and content
 * - Consistent spacing using design tokens
 * - Proper state management (loading, error, content)
 */
@Composable
fun UsersListScreenContent(
    isLoading: Boolean,
    userList: List<User>,
    lastVisibleItemIndex: Int,
    errorMessage: String,
    onItemClick: (User) -> Unit,
    onSearchSubmitted: (String) -> Unit,
    onClearInput: () -> Unit,
    onListScrolledToEnd: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        AppActionBarView(
            modifier = Modifier.fillMaxWidth(),
            headerText = stringResource(R.string.users_page_title),
            showBackButton = false
        )

        SearchBox(
            placeholder = stringResource(R.string.user_search_field_help),
            onSearchSubmitted = onSearchSubmitted,
            onClearInput = onClearInput,
            testTag = "search_input"
        )

        StateContentBox(
            modifier = Modifier.padding(top = Spacing.medium),
            isLoading = isLoading,
            errorMessage = errorMessage
        ) {
            UsersListView(
                modifier = Modifier
                    .padding(horizontal = Spacing.medium)
                    .fillMaxWidth()
                    .semantics { testTag = "user_list" },
                userList = userList,
                lastVisibleItemIndex = lastVisibleItemIndex,
                onItemClick = onItemClick,
                onListScrolledToEnd = onListScrolledToEnd
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun UserListScreenContentPreview() {
    GithubCruiseTheme {
        UsersListScreenContent(
            isLoading = false,
            userList = emptyList(),
            lastVisibleItemIndex = 0,
            errorMessage = "Error",
            onItemClick = { }, onSearchSubmitted = {},
            onClearInput = {},
            onListScrolledToEnd = {},
        )
    }
}

