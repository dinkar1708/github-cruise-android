package com.jetpack.compose.github.github.cruise.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.jetpack.compose.github.github.cruise.data.datastore.FavoritesDataStore
import com.jetpack.compose.github.github.cruise.data.datastore.ThemeDataStore
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Extension property for DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "github_cruise_preferences"
)

/**
 * Hilt module for providing preferences dependencies
 *
 * Migrated from SharedPreferences to DataStore for better performance and type safety
 */
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideThemeDataStore(
        dataStore: DataStore<Preferences>
    ): ThemeDataStore {
        return ThemeDataStore(dataStore)
    }

    @Provides
    @Singleton
    fun provideFavoritesDataStore(
        dataStore: DataStore<Preferences>,
        moshi: Moshi
    ): FavoritesDataStore {
        return FavoritesDataStore(dataStore, moshi)
    }
}
