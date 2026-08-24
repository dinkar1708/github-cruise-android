package com.jetpack.compose.github.github.cruise.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 1. Feed Categories Entity (Tabs: Top Stories, Tech, Science)
 */
@Entity(tableName = "news_categories")
data class CategoryEntity(
    @PrimaryKey 
    val categoryId: String,          // e.g. "top_stories", "tech", "science"
    val title: String,               // e.g. "Top Stories", "Technology"
    val orderIndex: Int,             // Tab display order (0, 1, 2...)
    val isVisible: Boolean = true,
    val lastSyncCursor: String? = null
)

/**
 * 2. Articles Master Entity (Master Feed Content)
 */
@Entity(
    tableName = "articles",
    indices = [
        Index(value = ["isBookmarked", "lastAccessedTs"]) // Compound index for LRU cache eviction
    ]
)
data class ArticleEntity(
    @PrimaryKey 
    val articleId: String,           // e.g. "art_spacex_101"
    val title: String,
    val publisherName: String,
    val publishedTs: Long,
    val readabilityHtml: String,     // Cleaned HTML body
    val plainText: String,
    val heroImagePath: String?,      // Local disk cache path
    val originalUrl: String,
    val lastAccessedTs: Long,        // For LRU eviction calculation
    val fileSizeBytes: Long,         // Storage weight on disk
    val isBookmarked: Boolean = false // Protected from LRU eviction
)

/**
 * 3. Categories to Articles Bridge Entity (Many-to-Many CrossRef)
 */
@Entity(
    tableName = "categories_to_articles",
    primaryKeys = ["categoryId", "articleId"],  // Composite Primary Key
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ArticleEntity::class,
            parentColumns = ["articleId"],
            childColumns = ["articleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["categoryId", "sortRank"]) // Fast feed pagination index
    ]
)
data class CategoryToArticleCrossRef(
    val categoryId: String,
    val articleId: String,
    val sortRank: Int                            // Feed display rank (1 = Top card in tab)
)

/**
 * 4. In-Article Images Entity (One-to-Many 1:N Child)
 */
@Entity(
    tableName = "article_images",
    foreignKeys = [
        ForeignKey(
            entity = ArticleEntity::class,
            parentColumns = ["articleId"],
            childColumns = ["articleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["articleId"])]
)
data class ArticleImageEntity(
    @PrimaryKey 
    val imageId: String,             // e.g. "img_01"
    val articleId: String,           // Foreign Key to articles
    val remoteUrl: String,
    val localFilePath: String,       // e.g. "/cache/images/rocket.webp"
    val fileSizeBytes: Long,
    val downloadStatus: String = "PENDING" // "PENDING", "DOWNLOADED", "FAILED"
)

/**
 * 5. User Reading History Entity (One-to-One 1:1 Child)
 */
@Entity(
    tableName = "user_reading_history",
    foreignKeys = [
        ForeignKey(
            entity = ArticleEntity::class,
            parentColumns = ["articleId"],
            childColumns = ["articleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserReadingHistoryEntity(
    @PrimaryKey 
    val articleId: String,           // Primary Key = Foreign Key (1:1 Relationship)
    val scrollPercentage: Float = 0.0f, // e.g. 0.65f (65% scroll position)
    val lastReadTs: Long
)

/**
 * 6. Pending Actions Queue Entity (Offline Sync Buffer)
 */
@Entity(
    tableName = "pending_actions_queue",
    indices = [
        Index(value = ["idempotencyKey"], unique = true), // Prevents duplicate writes
        Index(value = ["syncStatus", "createdAtTs"])     // Fast queue drain index
    ]
)
data class PendingActionEntity(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,
    val actionType: String,          // "TOGGLE_BOOKMARK", "SAVE_PROGRESS"
    val articleId: String,
    val isBookmarked: Boolean? = null,
    val scrollPercentage: Float? = null,
    val idempotencyKey: String,      // Client UUID
    val createdAtTs: Long,
    val retryCount: Int = 0,
    val syncStatus: String = "PENDING" // "PENDING", "IN_FLIGHT", "FAILED"
)
