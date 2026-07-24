package com.jetpack.compose.github.github.cruise.ui.features.repositorysearch.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jetpack.compose.github.github.cruise.domain.model.Repository
import com.jetpack.compose.github.github.cruise.domain.model.RepositoryOwner
import com.jetpack.compose.github.github.cruise.ui.shared.NetworkImageView
import com.jetpack.compose.github.github.cruise.ui.theme.AppShapes
import com.jetpack.compose.github.github.cruise.ui.theme.Dimension
import com.jetpack.compose.github.github.cruise.ui.theme.Elevation
import com.jetpack.compose.github.github.cruise.ui.theme.GithubCruiseTheme
import com.jetpack.compose.github.github.cruise.ui.theme.Spacing
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber

/**
 * List view for repositories
 * Feature 2.1: Repository Search Screen
 */
@Composable
fun RepositoriesListView(
    repositories: List<Repository>,
    lastVisibleItemIndex: Int,
    onItemClick: (Repository) -> Unit,
    onListScrolledToEnd: (Int) -> Unit
) {
    // Scroll State Management:
    // rememberLazyListState() uses rememberSaveable internally with LazyListState.Saver
    // This automatically saves and restores scroll position across rotation/configuration changes
    // No need to manually pass initialFirstVisibleItemIndex - framework handles it automatically
    // We only need scrollState reference for pagination detection in LaunchedEffect below
    val scrollState = rememberLazyListState()
    var scrolledToEnd by rememberSaveable { mutableStateOf(false) }

    LazyColumn(state = scrollState) {
        itemsIndexed(repositories) { index, repository ->
            key(repository.id) {
                RepositoryCard(
                    repository = repository,
                    onItemClick = onItemClick
                )
            }
        }
    }

    // Detect when scrolled to end for pagination
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.layoutInfo.visibleItemsInfo }
            .distinctUntilChanged()
            .collect { visibleItems ->
                if (visibleItems.lastOrNull()?.index == (repositories.size - 1) && !scrolledToEnd) {
                    val lastIndex = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    Timber.d("Scrolled to end, loading next page. Last index: $lastIndex")
                    scrolledToEnd = true
                    onListScrolledToEnd(lastIndex)
                }
            }
    }

    LaunchedEffect(repositories) {
        if (scrolledToEnd && repositories.isNotEmpty()) {
            scrolledToEnd = false
        }
    }
}

@Composable
fun RepositoryCard(
    repository: Repository,
    onItemClick: (Repository) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small)
            .clickable { onItemClick(repository) },
        shape = AppShapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.level2)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium)
        ) {
            // Owner and repo name
            Row(verticalAlignment = Alignment.CenterVertically) {
                NetworkImageView(
                    imageUrl = repository.owner.avatarUrl,
                    contentDescription = repository.owner.login,
                    modifier = Modifier.size(Dimension.avatarSizeSmall)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = repository.owner.login,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = repository.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Description
            if (!repository.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.small))
                Text(
                    text = repository.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Stats row
            Spacer(modifier = Modifier.height(Spacing.small))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Language
                if (!repository.language.isNullOrEmpty()) {
                    Text(
                        text = repository.language,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(Spacing.medium))
                }

                // Stars
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Stars",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = repository.stargazersCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(Spacing.medium))

                // Forks
                Text(
                    text = "⑂ ${repository.forksCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RepositoryCardPreview() {
    GithubCruiseTheme {
        RepositoryCard(
            repository = Repository(
                id = 1,
                name = "awesome-android",
                fullName = "JetBrains/awesome-android",
                owner = RepositoryOwner(
                    login = "JetBrains",
                    avatarUrl = "",
                    htmlUrl = ""
                ),
                description = "A curated list of awesome Android libraries and resources",
                htmlUrl = "",
                language = "Kotlin",
                stargazersCount = 1234,
                forksCount = 567,
                watchersCount = 89,
                openIssuesCount = 10,
                score = 0.0,
                fork = false,
                createdAt = "",
                updatedAt = ""
            ),
            onItemClick = {}
        )
    }
}
