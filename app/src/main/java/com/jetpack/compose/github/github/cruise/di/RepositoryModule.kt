package com.jetpack.compose.github.github.cruise.di

import com.jetpack.compose.github.github.cruise.data.repository.favorites.FavoritesRepositoryImpl
import com.jetpack.compose.github.github.cruise.data.repository.repositorydetails.RepositoryDetailsRepository
import com.jetpack.compose.github.github.cruise.data.repository.repositorydetails.RepositoryDetailsRepositoryImpl
import com.jetpack.compose.github.github.cruise.data.repository.repositorysearch.RepositorySearchRepository
import com.jetpack.compose.github.github.cruise.data.repository.repositorysearch.RepositorySearchRepositoryImpl
import com.jetpack.compose.github.github.cruise.data.repository.search.SearchRepositoryImpl
import com.jetpack.compose.github.github.cruise.data.repository.user.UserRepositoryImpl
import com.jetpack.compose.github.github.cruise.domain.repository.FavoritesRepository
import com.jetpack.compose.github.github.cruise.domain.repository.SearchRepository
import com.jetpack.compose.github.github.cruise.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/* allow to bind
class SearchRepositoryImpl @Inject constructor(
    private val networkDataSource: NetworkDataSource,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
)
*/

/**
 * Created by Dinakar Maurya on 2024/05/13.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindSearchRepository(repository: SearchRepositoryImpl): SearchRepository

    @Singleton
    @Binds
    abstract fun bindRepositorySearchRepository(
        repository: RepositorySearchRepositoryImpl
    ): RepositorySearchRepository

    @Singleton
    @Binds
    abstract fun bindUserRepository(repository: UserRepositoryImpl): UserRepository

    @Singleton
    @Binds
    abstract fun bindFavoritesRepository(repository: FavoritesRepositoryImpl): FavoritesRepository

    @Singleton
    @Binds
    abstract fun bindRepositoryDetailsRepository(
        repository: RepositoryDetailsRepositoryImpl
    ): RepositoryDetailsRepository
}
