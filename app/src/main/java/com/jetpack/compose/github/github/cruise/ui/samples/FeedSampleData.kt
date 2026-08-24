package com.jetpack.compose.github.github.cruise.ui.samples

/**
 * Sample data models for Multi-Tab Dynamic Feed demonstration
 *
 * Core architecture patterns:
 * - Multi-tab feed architecture
 * - Dynamic category feeds
 * - HorizontalPager with windowing (beyondViewportPageCount)
 */

/**
 * Represents a feed category/tab (e.g., Top Stories, Tech, Sports)
 */
data class NewsCategory(
    val categoryId: String,
    val title: String,
    val orderIndex: Int,
    val isVisible: Boolean = true,
    val isPinned: Boolean = false
)

/**
 * Represents an article item in a feed
 */
data class NewsArticle(
    val articleId: String,
    val title: String,
    val publisherName: String,
    val heroImageUrl: String,
    val publishedTimestamp: Long,
    val summary: String
)

/**
 * Sample data generator for demo purposes
 */
object FeedSampleData {

    /**
     * Sample categories for multi-tab feed navigation
     */
    val sampleCategories = listOf(
        NewsCategory("top_stories", "Top Stories", 0, isPinned = true),
        NewsCategory("tech", "Technology", 1),
        NewsCategory("sports", "Sports", 2),
        NewsCategory("politics", "Politics", 3),
        NewsCategory("business", "Business", 4),
        NewsCategory("entertainment", "Entertainment", 5),
        NewsCategory("science", "Science", 6),
        NewsCategory("health", "Health", 7)
    )

    /**
     * Generate sample articles for a given category
     */
    fun generateArticlesForCategory(categoryId: String, count: Int = 20): List<NewsArticle> {
        val baseTimestamp = System.currentTimeMillis()

        return (1..count).map { index ->
            NewsArticle(
                articleId = "${categoryId}_article_$index",
                title = generateTitle(categoryId, index),
                publisherName = samplePublishers.random(),
                heroImageUrl = generateImageUrl(index),
                publishedTimestamp = baseTimestamp - (index * 3600000), // 1 hour apart
                summary = "This is a sample article summary for ${categoryId} article $index. " +
                        "It demonstrates the multi-tab feed architecture."
            )
        }
    }

    private fun generateTitle(categoryId: String, index: Int): String {
        return when (categoryId) {
            "top_stories" -> "Breaking: Major Development in Story $index"
            "tech" -> "Tech Innovation: New AI Breakthrough $index"
            "sports" -> "Sports Update: Championship Game $index Highlights"
            "politics" -> "Political News: Policy Update $index"
            "business" -> "Business Report: Market Analysis $index"
            "entertainment" -> "Entertainment: Celebrity News $index"
            "science" -> "Science Discovery: Research Finding $index"
            "health" -> "Health Update: Medical Breakthrough $index"
            else -> "Article $index for $categoryId"
        }
    }

    private fun generateImageUrl(index: Int): String {
        val imageId = 100 + index
        return "https://picsum.photos/400/300?random=$imageId"
    }

    private val samplePublishers = listOf(
        "TechCrunch",
        "Reuters",
        "Bloomberg",
        "The Verge",
        "ESPN",
        "CNN",
        "BBC News",
        "Forbes",
        "Wired",
        "NPR"
    )
}
