package com.jetpack.compose.github.github.cruise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jetpack.compose.github.github.cruise.domain.model.Repository
import com.jetpack.compose.github.github.cruise.domain.model.RepositoryOwner

/**
 * Room entity for caching repository search results
 *
 * Stores search repository data for offline access
 * Cache retention: 24 hours
 */
@Entity(tableName = "search_repositories")
data class SearchRepositoryEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val fullName: String,
    val ownerLogin: String,
    val ownerAvatarUrl: String,
    val ownerHtmlUrl: String,
    val description: String?,
    val htmlUrl: String,
    val language: String?,
    val stargazersCount: Int,
    val forksCount: Int,
    val watchersCount: Int,
    val openIssuesCount: Int,
    val score: Double,
    val fork: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val query: String, // The search query that returned this result
    val cachedAt: Long = System.currentTimeMillis()
)

/**
 * Convert SearchRepositoryEntity to domain model (Repository)
 */
fun SearchRepositoryEntity.toDomainModel() = Repository(
    id = id,
    name = name,
    fullName = fullName,
    owner = RepositoryOwner(
        login = ownerLogin,
        avatarUrl = ownerAvatarUrl,
        htmlUrl = ownerHtmlUrl
    ),
    description = description,
    htmlUrl = htmlUrl,
    language = language,
    stargazersCount = stargazersCount,
    forksCount = forksCount,
    watchersCount = watchersCount,
    openIssuesCount = openIssuesCount,
    score = score,
    fork = fork,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Convert domain model to SearchRepositoryEntity
 */
fun Repository.toSearchEntity(query: String) = SearchRepositoryEntity(
    id = id,
    name = name,
    fullName = fullName,
    ownerLogin = owner.login,
    ownerAvatarUrl = owner.avatarUrl,
    ownerHtmlUrl = owner.htmlUrl,
    description = description,
    htmlUrl = htmlUrl,
    language = language,
    stargazersCount = stargazersCount,
    forksCount = forksCount,
    watchersCount = watchersCount,
    openIssuesCount = openIssuesCount,
    score = score,
    fork = fork,
    createdAt = createdAt,
    updatedAt = updatedAt,
    query = query.lowercase()
)
