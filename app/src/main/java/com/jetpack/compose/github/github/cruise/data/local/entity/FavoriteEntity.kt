package com.jetpack.compose.github.github.cruise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jetpack.compose.github.github.cruise.domain.model.FavoriteItem
import com.jetpack.compose.github.github.cruise.domain.model.FavoriteType

/**
 * Room entity for storing favorites
 *
 * Replaces JSON-based DataStore storage with proper database table
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val rowId: Long = 0,
    val id: String,
    val type: String, // USER or REPOSITORY
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Convert FavoriteEntity to domain model
 */
fun FavoriteEntity.toDomainModel() = FavoriteItem(
    id = id,
    type = FavoriteType.valueOf(type),
    name = name,
    description = description,
    avatarUrl = avatarUrl,
    url = url,
    timestamp = timestamp
)

/**
 * Convert domain model to FavoriteEntity
 */
fun FavoriteItem.toEntity() = FavoriteEntity(
    id = id,
    type = type.name,
    name = name,
    description = description,
    avatarUrl = avatarUrl,
    url = url,
    timestamp = timestamp
)
