package com.jetpack.compose.github.github.cruise.di

import com.jetpack.compose.github.github.cruise.data.repository.search.SearchRepository
import com.jetpack.compose.github.github.cruise.data.repository.search.SearchRepositoryImpl
import com.jetpack.compose.github.github.cruise.data.repository.user.UserRepository
import com.jetpack.compose.github.github.cruise.data.repository.user.UserRepositoryImpl
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
    abstract fun bindUserRepository(repository: UserRepositoryImpl): UserRepository
}
