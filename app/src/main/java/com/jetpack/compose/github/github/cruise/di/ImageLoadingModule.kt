package com.jetpack.compose.github.github.cruise.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.jetpack.compose.github.github.cruise.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Singleton

/**
 * Hilt module for configuring Coil image loading with advanced caching.
 *
 * Cache Hierarchy:
 * 1. L1 Cache (Memory): Fast, in-memory cache for recently loaded images
 * 2. L2 Cache (Disk): Persistent storage for offline access
 *
 * Cache Flow:
 * - Request image → Check L1 (Memory) → Check L2 (Disk) → Network download
 * - On success: Save to both L1 and L2 for future requests
 */
@Module
@InstallIn(SingletonComponent::class)
object ImageLoadingModule {

    /**
     * Provides a singleton ImageLoader with configured L1 and L2 caches.
     *
     * L1 Cache (MemoryCache):
     * - Size: 25% of available memory
     * - Fastest access (in-memory)
     * - Cleared when app is killed
     *
     * L2 Cache (DiskCache):
     * - Size: 50 MB persistent storage
     * - Slower than memory but persists across app restarts
     * - Used for offline-first scenarios
     */
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            // L1 Cache: Memory Cache Configuration
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25) // Use 25% of available memory
                    .build()
            }
            // L2 Cache: Disk Cache Configuration
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache")) // Custom directory
                    .maxSizeBytes(50 * 1024 * 1024) // 50 MB disk cache
                    .build()
            }
            // Cache policies
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            // Enable debug logging in debug builds
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
}
