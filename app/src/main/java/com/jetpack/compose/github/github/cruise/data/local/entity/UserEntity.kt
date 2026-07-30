package com.jetpack.compose.github.github.cruise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jetpack.compose.github.github.cruise.domain.model.UserProfile

/**
 * Room entity for storing user data locally
 *
 * Supports offline-first architecture by caching user profiles
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val login: String,
    val id: Long,
    val avatarUrl: String?,
    val name: String?,
    val company: String?,
    val blog: String?,
    val location: String?,
    val email: String?,
    val bio: String?,
    val publicRepos: Int,
    val publicGists: Int,
    val followers: Int,
    val following: Int,
    val createdAt: String?,
    val updatedAt: String?,
    val htmlUrl: String?,
    val cachedAt: Long = System.currentTimeMillis()
)

/**
 * Convert UserEntity to domain model
 */
fun UserEntity.toDomainModel() = UserProfile(
    login = login,
    id = id,
    avatarUrl = avatarUrl ?: "",
    name = name,
    followers = followers,
    following = following,
    publicRepos = publicRepos
)

/**
 * Convert domain model to UserEntity
 */
fun UserProfile.toEntity() = UserEntity(
    login = login,
    id = id,
    avatarUrl = avatarUrl,
    name = name,
    company = null,
    blog = null,
    location = null,
    email = null,
    bio = null,
    publicRepos = publicRepos,
    publicGists = 0,
    followers = followers,
    following = following,
    createdAt = null,
    updatedAt = null,
    htmlUrl = null
)
