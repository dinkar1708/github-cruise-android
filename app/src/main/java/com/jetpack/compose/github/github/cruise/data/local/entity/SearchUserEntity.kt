package com.jetpack.compose.github.github.cruise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jetpack.compose.github.github.cruise.domain.model.User

/**
 * Room entity for caching search results
 *
 * Stores search user data for offline access
 */
@Entity(tableName = "search_users")
data class SearchUserEntity(
    @PrimaryKey
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val score: Double,
    val query: String, // The search query that returned this result
    val cachedAt: Long = System.currentTimeMillis()
)

/**
 * Convert SearchUserEntity to domain model (User, not SearchUser)
 */
fun SearchUserEntity.toDomainModel() = User(
    id = id,
    login = login,
    avatarUrl = avatarUrl,
    score = score
)

/**
 * Convert domain model to SearchUserEntity
 */
fun User.toEntity(query: String) = SearchUserEntity(
    id = id,
    login = login,
    avatarUrl = avatarUrl,
    score = score,
    query = query.lowercase()
)
