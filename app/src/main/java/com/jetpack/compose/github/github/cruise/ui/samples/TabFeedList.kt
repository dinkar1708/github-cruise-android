package com.jetpack.compose.github.github.cruise.ui.samples

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jetpack.compose.github.github.cruise.ui.shared.NetworkImageView
import com.jetpack.compose.github.github.cruise.ui.theme.AppShapes
import com.jetpack.compose.github.github.cruise.ui.theme.GithubCruiseTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tab Feed List Component for Multi-Tab Feed Architecture
 *
 * Demonstrates:
 * - LazyColumn for efficient vertical scrolling
 * - Scroll position preservation
 * - Coil L1/L2 cache integration with visual indicators
 * - Article card layout with hero images
 */
@Composable
fun TabFeedList(
    articles: List<NewsArticle>,
    modifier: Modifier = Modifier
) {
    // LazyListState preserves scroll position
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = articles,
            key = { it.articleId } // Stable key for efficient recomposition
        ) { article ->
            ArticleCard(
                article = article,
                onClick = {
                    // Navigate to article detail
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * Article card component with hero image
 *
 * Uses NetworkImageView with L1/L2 cache indicators:
 * - 🟢 Green badge = L1 (Memory Cache) - Fastest!
 * - 🟡 Yellow badge = L2 (Disk Cache) - Fast!
 * - 🔴 Red badge = Network download
 */
@Composable
private fun ArticleCard(
    article: NewsArticle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Hero Image with Coil L1/L2 cache
            // Shows cache indicator badge in debug builds
            NetworkImageView(
                imageUrl = article.heroImageUrl,
                contentDescription = article.title,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 12.dp),
                shape = AppShapes.medium,
                showCacheIndicator = true // Shows L1/L2/NET badge
            )

            // Article Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Top)
            ) {
                // Title
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Publisher
                Text(
                    text = article.publisherName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Summary
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp
                Text(
                    text = formatTimestamp(article.publishedTimestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * Format timestamp for display
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 3600000 -> "${diff / 60000}m ago" // Less than 1 hour
        diff < 86400000 -> "${diff / 3600000}h ago" // Less than 1 day
        else -> {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TabFeedListPreview() {
    GithubCruiseTheme {
        val sampleArticles = FeedSampleData.generateArticlesForCategory("tech", 5)
        TabFeedList(articles = sampleArticles)
    }
}

@Preview(showBackground = true)
@Composable
fun ArticleCardPreview() {
    GithubCruiseTheme {
        val sampleArticle = NewsArticle(
            articleId = "sample_1",
            title = "Next-Gen AI Chip Released by Major Tech Company",
            publisherName = "TechCrunch",
            heroImageUrl = "https://picsum.photos/400/300",
            publishedTimestamp = System.currentTimeMillis() - 7200000,
            summary = "A major breakthrough in artificial intelligence processing announced today."
        )
        ArticleCard(
            article = sampleArticle,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
