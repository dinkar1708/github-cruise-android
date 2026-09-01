package com.jetpack.compose.github.github.cruise.di

import com.cruise.apm.CruiseApm
import com.cruise.apm.network.CruiseApmOkHttpInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Dependency Injection module for CruiseAPM Observability SDK.
 *
 * Exposes CruiseAPM singleton and its OkHttp interceptor to the host app's dependency graph.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApmModule {

    @Provides
    @Singleton
    fun provideCruiseApm(): CruiseApm {
        return CruiseApm
    }

    @Provides
    @Singleton
    fun provideCruiseApmOkHttpInterceptor(): CruiseApmOkHttpInterceptor {
        return CruiseApmOkHttpInterceptor()
    }
}
