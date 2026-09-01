package com.jetpack.compose.github.github.cruise.di

import com.jetpack.compose.github.github.cruise.data.network.NetworkDataSource
import com.jetpack.compose.github.github.cruise.data.network.NetworkDataSourceImpl
import com.jetpack.compose.github.github.cruise.data.network.api.APIInterface
import com.jetpack.compose.github.github.cruise.data.network.api.ApiConstants
import com.jetpack.compose.github.github.cruise.data.network.api.ApiInterceptor
import com.jetpack.compose.github.github.cruise.data.network.authenticator.TokenAuthenticator
import com.jetpack.compose.github.github.cruise.data.network.circuitbreaker.CircuitBreaker
import com.jetpack.compose.github.github.cruise.data.network.interceptor.RetryInterceptor
import com.jetpack.compose.github.github.cruise.data.security.SecureTokenManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Created by Dinakar Maurya on 2024/05/13.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkDataSourceModule {

    @Singleton
    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    @Singleton
    @Provides
    fun provideCircuitBreaker(): CircuitBreaker {
        return CircuitBreaker()
    }

    @Singleton
    @Provides
    fun provideTokenAuthenticator(
        tokenManager: SecureTokenManager
    ): TokenAuthenticator {
        return TokenAuthenticator(tokenManager)
    }

    @Singleton
    @Provides
    fun provideRetryInterceptor(
        circuitBreaker: CircuitBreaker
    ): RetryInterceptor {
        return RetryInterceptor(circuitBreaker)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        moshi: Moshi,
        tokenManager: SecureTokenManager,
        tokenAuthenticator: TokenAuthenticator,
        retryInterceptor: RetryInterceptor,
        apmInterceptor: com.cruise.apm.network.CruiseApmOkHttpInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val apiInterceptor = ApiInterceptor(moshi, tokenManager)

        return OkHttpClient.Builder()
            .authenticator(tokenAuthenticator) // Mutex-protected token refresh on 401
            .addInterceptor(loggingInterceptor)
            .addInterceptor(retryInterceptor) // Resilient retries with full jitter + circuit breaker
            .addInterceptor(apiInterceptor)
            .addInterceptor(apmInterceptor) // Injected via Hilt from ApmModule
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideNetworkDataSource(
        moshi: Moshi,
        okHttpClient: OkHttpClient
    ): NetworkDataSource {
        val retrofitBuilder: Retrofit.Builder =
            Retrofit.Builder()
                // release and debug url setting
                .baseUrl(ApiConstants.BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(okHttpClient)

        val retrofit: Retrofit = retrofitBuilder.build()

        return NetworkDataSourceImpl(
            api = retrofit.create(APIInterface::class.java),
        )
    }
}