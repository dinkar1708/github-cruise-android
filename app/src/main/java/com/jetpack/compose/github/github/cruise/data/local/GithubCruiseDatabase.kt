package com.jetpack.compose.github.github.cruise.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jetpack.compose.github.github.cruise.data.local.dao.FavoriteDao
import com.jetpack.compose.github.github.cruise.data.local.dao.RepositoryDao
import com.jetpack.compose.github.github.cruise.data.local.dao.SearchRepositoryDao
import com.jetpack.compose.github.github.cruise.data.local.dao.SearchUserDao
import com.jetpack.compose.github.github.cruise.data.local.dao.UserDao
import com.jetpack.compose.github.github.cruise.data.local.entity.FavoriteEntity
import com.jetpack.compose.github.github.cruise.data.local.entity.RepositoryEntity
import com.jetpack.compose.github.github.cruise.data.local.entity.SearchRepositoryEntity
import com.jetpack.compose.github.github.cruise.data.local.entity.SearchUserEntity
import com.jetpack.compose.github.github.cruise.data.local.entity.UserEntity

/**
 * Room Database for Github Cruise app
 *
 * Implements offline-first architecture with local caching
 *
 * Official Guide: https://developer.android.com/training/data-storage/room
 */
@Database(
    entities = [
        UserEntity::class,
        RepositoryEntity::class,
        FavoriteEntity::class,
        SearchUserEntity::class,
        SearchRepositoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class GithubCruiseDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun repositoryDao(): RepositoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun searchUserDao(): SearchUserDao
    abstract fun searchRepositoryDao(): SearchRepositoryDao

    companion object {
        const val DATABASE_NAME = "github_cruise_db"
    }
}
