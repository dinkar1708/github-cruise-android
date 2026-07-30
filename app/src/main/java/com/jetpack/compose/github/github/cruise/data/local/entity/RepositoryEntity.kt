package com.jetpack.compose.github.github.cruise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jetpack.compose.github.github.cruise.domain.model.UserRepo

/**
 * Room entity for storing repository data locally
 *
 * Supports offline-first architecture by caching repositories
 */
@Entity(tableName = "repositories")
data class RepositoryEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val fullName: String,
    val ownerLogin: String,
    val ownerAvatarUrl: String?,
    val description: String?,
    val htmlUrl: String?,
    val language: String?,
    val stargazersCount: Int,
    val forksCount: Int,
    val watchersCount: Int,
    val openIssuesCount: Int,
    val createdAt: String?,
    val updatedAt: String?,
    val cachedAt: Long = System.currentTimeMillis()
)

/**
 * Convert RepositoryEntity to domain model
 */
fun RepositoryEntity.toDomainModel() = UserRepo(
    id = id,
    name = name,
    fullName = fullName,
    description = description,
    htmlUrl = htmlUrl ?: "",
    language = language,
    stargazersCount = stargazersCount.toString(),
    fork = false
)

/**
 * Convert domain model to RepositoryEntity
 */
fun UserRepo.toEntity() = RepositoryEntity(
    id = id,
    name = name,
    fullName = fullName,
    ownerLogin = "",
    ownerAvatarUrl = null,
    description = description,
    htmlUrl = htmlUrl,
    language = language,
    stargazersCount = stargazersCount.toIntOrNull() ?: 0,
    forksCount = 0,
    watchersCount = 0,
    openIssuesCount = 0,
    createdAt = null,
    updatedAt = null
)
