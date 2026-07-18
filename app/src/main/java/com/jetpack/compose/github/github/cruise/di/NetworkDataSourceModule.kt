package com.jetpack.compose.github.github.cruise.di

import com.jetpack.compose.github.github.cruise.data.network.NetworkDataSource
import com.jetpack.compose.github.github.cruise.data.network.NetworkDataSourceImpl
import com.jetpack.compose.github.github.cruise.data.network.api.APIInterface
import com.jetpack.compose.github.github.cruise.data.network.api.ApiConstants
import com.jetpack.compose.github.github.cruise.data.network.api.ApiInterceptor
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
import javax.inject.Singleton

/**
 * Created by Dinakar Maurya on 2024/05/13.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkDataSourceModule {

    private val moshi: Moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }

    @Singleton
    @Provides
    fun provideNetworkDataSource(): NetworkDataSource {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val apiInterceptor = ApiInterceptor(moshi)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(apiInterceptor)
            .build()

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