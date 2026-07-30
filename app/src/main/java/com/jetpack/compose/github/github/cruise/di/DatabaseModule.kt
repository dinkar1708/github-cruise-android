package com.jetpack.compose.github.github.cruise.di

import android.content.Context
import androidx.room.Room
import com.jetpack.compose.github.github.cruise.data.local.GithubCruiseDatabase
import com.jetpack.compose.github.github.cruise.data.local.dao.FavoriteDao
import com.jetpack.compose.github.github.cruise.data.local.dao.RepositoryDao
import com.jetpack.compose.github.github.cruise.data.local.dao.SearchUserDao
import com.jetpack.compose.github.github.cruise.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database dependencies
 *
 * Provides Room database and DAOs as singletons
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGithubCruiseDatabase(
        @ApplicationContext context: Context
    ): GithubCruiseDatabase {
        return Room.databaseBuilder(
            context,
            GithubCruiseDatabase::class.java,
            GithubCruiseDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For development, remove in production
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: GithubCruiseDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideRepositoryDao(database: GithubCruiseDatabase): RepositoryDao {
        return database.repositoryDao()
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(database: GithubCruiseDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    @Singleton
    fun provideSearchUserDao(database: GithubCruiseDatabase): SearchUserDao {
        return database.searchUserDao()
    }
}
