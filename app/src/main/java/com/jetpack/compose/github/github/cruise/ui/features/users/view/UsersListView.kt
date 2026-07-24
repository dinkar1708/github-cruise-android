package com.jetpack.compose.github.github.cruise.ui.features.users.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.jetpack.compose.github.github.cruise.R
import com.jetpack.compose.github.github.cruise.domain.model.User
import com.jetpack.compose.github.github.cruise.ui.shared.NetworkImageView
import com.jetpack.compose.github.github.cruise.ui.theme.AppShapes
import com.jetpack.compose.github.github.cruise.ui.theme.Dimension
import com.jetpack.compose.github.github.cruise.ui.theme.Elevation
import com.jetpack.compose.github.github.cruise.ui.theme.GithubCruiseTheme
import com.jetpack.compose.github.github.cruise.ui.theme.Spacing
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber

/**
 * Created by Dinakar Maurya on 2024/05/13.
 */
@Composable
fun UsersListView(
    modifier: Modifier,
    userList: List<User>,
    lastVisibleItemIndex: Int,
    onItemClick: (User) -> Unit,
    onListScrolledToEnd: (Int) -> Unit
) {
    // Restore scroll position from ViewModel state (survives rotation)
    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = lastVisibleItemIndex.coerceAtLeast(0)
    )
    var scrolledToEnd by rememberSaveable { mutableStateOf(false) }
    val tag = "UsersListView"

    LazyColumn(modifier = modifier, state = scrollState) {
        itemsIndexed(userList) { _, user ->
            key(user.id) {
                UserListItem(user = user, onItemClick = onItemClick)
            }
        }
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.layoutInfo.visibleItemsInfo }
            .distinctUntilChanged()
            .collect { visibleItems ->
                if (visibleItems.lastOrNull()?.index == (userList.size - 1) && !scrolledToEnd) {
                    val lastVisibleItemIndex =
                        scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    Timber.d("$tag SET variable user scrolled to last page...lastVisibleItemIndex $lastVisibleItemIndex user size ${userList.size}")
                    // scrolled to ended true
                    scrolledToEnd = true
                    // Set a variable in the ViewModel to save it for the next recomposition.
                    onListScrolledToEnd(lastVisibleItemIndex)
                }
            }
    }
}


@Composable
fun UserListItem(user: User, onItemClick: (User) -> Unit) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.small),
        onClick = { onItemClick(user) },
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(
            defaultElevation = Elevation.card
        ),
        shape = AppShapes.card
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NetworkImageView(
                modifier = Modifier.size(Dimension.avatarSizeLarge),
                imageUrl = user.avatarUrl,
                contentDescription = stringResource(
                    R.string.profile_picture_off_icon_content_desc,
                    user.login
                )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = Spacing.medium)
            ) {
                Text(
                    text = user.login,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.user_list_score, user.score),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.extraSmall)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserListPreview() {
    val users = mutableListOf(
        User(
            id = 1,
            login = "dinkar1708",
            avatarUrl = "https://avatars.githubusercontent.com/u/14831652?v=4",
        ), User(
            id = 2,
            login = "dipsujidipsujidipsujidipsujidipsujidipsujidipsuji",
            avatarUrl = "https://avatars.githubusercontent.com/u/14831652?v=4",
        )
    )

    GithubCruiseTheme {
        UsersListView(
            modifier = Modifier.fillMaxWidth(), userList = users, onItemClick = {},
            onListScrolledToEnd = {}, lastVisibleItemIndex = 2
        )
    }
}
