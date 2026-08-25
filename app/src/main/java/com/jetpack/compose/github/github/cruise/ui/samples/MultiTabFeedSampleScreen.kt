package com.jetpack.compose.github.github.cruise.ui.samples

import android.content.ComponentCallbacks2
import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.imageLoader
import com.jetpack.compose.github.github.cruise.ui.theme.GithubCruiseTheme
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Multi-Tab Dynamic Feed Sample Implementation
 *
 * Demonstrates:
 *
 * 1. Architecture:
 * - HorizontalPager with beyondViewportPageCount = 1 (3-page sliding window for memory efficiency)
 * - ScrollableTabRow for dynamic category navigation
 * - Isolated tab content per category
 *
 * 2. ViewPager Memory Optimization:
 * - beyondViewportPageCount = 1 ensures only 3 tabs in memory (Active + Left + Right)
 * - Memory efficient: prevents OOM when browsing dozens of dynamic categories
 *
 * 3. Fast Tab Switching & Low Memory Callbacks:
 * - Instant tab switching with preserved state
 * - Smooth horizontal swipe gestures
 * - ComponentCallbacks2 integration to aggressively trim L1/L2 caches under memory pressure
 *
 * 4. TODO: Alternative Modern Jetpack Compose Swiping & Paging Options (100% XML-Free):
 * - TODO 1: Material 3 HorizontalMultiBrowseCarousel (Adaptive Hero item expanding/shrinking carousel)
 * - TODO 2: 3D Cube / Depth Page Transformations via Modifier.graphicsLayer on HorizontalPager
 * - TODO 3: Centered Card Snapping with LazyRow + rememberSnapFlingBehavior
 * - TODO 4: Custom Gesture Dragging via Modifier.anchoredDraggable() (Tinder-style card swipe deck)
 * - TODO 5: Custom SubcomposeLayout / Layout for Physics-based Cover Flow
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MultiTabFeedSampleScreen(
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null
) {
    val categories = remember { FeedSampleData.sampleCategories }
    val context = LocalContext.current

    // HorizontalPager state manages current page and scroll position
    // beyondViewportPageCount = 1 means only 3 pages in memory at once
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { categories.size }
    )

    val coroutineScope = rememberCoroutineScope()

    // Low Memory Callback - onTrimMemory L1/L2 cache purge
    // Implements ComponentCallbacks2 to handle Android OS memory pressure
    DisposableEffect(Unit) {
        val memoryCallback = object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) {
                // Not needed for memory management
            }

            override fun onLowMemory() {
                // CRITICAL: System is running very low on memory
                Timber.w("🔴 LOW MEMORY: onLowMemory() called - System critically low on memory!")
                handleMemoryPressure(context, "CRITICAL")
            }

            override fun onTrimMemory(level: Int) {
                // Handle different memory trim levels
                when (level) {
                    ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                        Timber.w("🔴 TRIM MEMORY: RUNNING_CRITICAL - App in foreground, memory critically low!")
                        handleMemoryPressure(context, "RUNNING_CRITICAL")
                    }
                    ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                        Timber.w("🟡 TRIM MEMORY: RUNNING_LOW - App in foreground, memory low")
                        handleMemoryPressure(context, "RUNNING_LOW")
                    }
                    ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> {
                        Timber.i("🟢 TRIM MEMORY: RUNNING_MODERATE - App in foreground, memory getting low")
                        handleMemoryPressure(context, "RUNNING_MODERATE")
                    }
                    ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                        Timber.i("⚪ TRIM MEMORY: UI_HIDDEN - App in background, UI not visible")
                        handleMemoryPressure(context, "UI_HIDDEN")
                    }
                    ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> {
                        Timber.i("⚪ TRIM MEMORY: BACKGROUND - App in background, trim caches")
                        handleMemoryPressure(context, "BACKGROUND")
                    }
                    ComponentCallbacks2.TRIM_MEMORY_MODERATE -> {
                        Timber.i("⚪ TRIM MEMORY: MODERATE - System memory moderately low")
                        handleMemoryPressure(context, "MODERATE")
                    }
                    ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                        Timber.w("🔴 TRIM MEMORY: COMPLETE - System extremely low on memory, app may be killed!")
                        handleMemoryPressure(context, "COMPLETE")
                    }
                    else -> {
                        Timber.d("TRIM MEMORY: Level $level")
                    }
                }
            }
        }

        context.registerComponentCallbacks(memoryCallback)

        onDispose {
            context.unregisterComponentCallbacks(memoryCallback)
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Multi-Tab Feed Demo") },
            navigationIcon = {
                if (onBackClick != null) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        // Scrollable Tab Row for categories
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 8.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            categories.forEachIndexed { index, category ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        // Smooth scroll to selected tab
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(category.title) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // HorizontalPager
        // beyondViewportPageCount = 1 for 3-page active window (memory optimization)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            beyondViewportPageCount = 1, // KEY: Only load Active + Left + Right pages
            pageSpacing = 0.dp
        ) { pageIndex ->
            val category = categories[pageIndex]

            // Each page shows a category feed
            CategoryFeedPage(
                category = category,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Individual category feed page
 * Represents one tab's content in the HorizontalPager
 */
@Composable
private fun CategoryFeedPage(
    category: NewsCategory,
    modifier: Modifier = Modifier
) {
    // Generate sample articles for this category
    val articles = remember(category.categoryId) {
        FeedSampleData.generateArticlesForCategory(category.categoryId)
    }

    TabFeedList(
        articles = articles,
        modifier = modifier
    )
}

/**
 * Handles memory pressure events by clearing caches
 *
 * Memory Clearing Strategy:
 * - MODERATE: Clear Coil memory cache (L1)
 * - LOW: Clear Coil memory + disk cache (L1 + L2)
 * - CRITICAL: Aggressive cleanup of all caches
 */
@OptIn(coil.annotation.ExperimentalCoilApi::class)
private fun handleMemoryPressure(context: android.content.Context, level: String) {
    Timber.w("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    Timber.w("🚨 MEMORY PRESSURE DETECTED: $level")
    Timber.w("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

    val imageLoader = context.imageLoader
    val memoryCache = imageLoader.memoryCache
    val diskCache = imageLoader.diskCache

    // Log current cache sizes before clearing
    val memoryCacheSize = memoryCache?.size ?: 0
    val diskCacheSize = diskCache?.size ?: 0

    Timber.i("📊 Current Cache State:")
    Timber.i("   L1 (Memory): ${memoryCacheSize / 1024 / 1024}MB")
    Timber.i("   L2 (Disk): ${diskCacheSize / 1024 / 1024}MB")

    when (level) {
        "RUNNING_MODERATE", "MODERATE" -> {
            // Clear L1 (Memory Cache) only
            Timber.w("🧹 Clearing L1 Memory Cache...")
            memoryCache?.clear()
            Timber.i("✅ L1 Memory Cache cleared")
        }
        "RUNNING_LOW", "LOW", "BACKGROUND" -> {
            // Clear L1 Memory Cache
            Timber.w("🧹 Clearing L1 Memory Cache...")
            memoryCache?.clear()
            Timber.i("✅ L1 Memory Cache cleared")
        }
        "RUNNING_CRITICAL", "CRITICAL", "COMPLETE", "UI_HIDDEN" -> {
            // Aggressive cleanup: Clear both L1 and L2
            Timber.w("🧹🧹 AGGRESSIVE CLEANUP: Clearing L1 + L2 Caches...")
            memoryCache?.clear()
            diskCache?.clear()

            // Force garbage collection
            System.gc()

            Timber.i("✅ L1 Memory Cache cleared")
            Timber.i("✅ L2 Disk Cache cleared")
            Timber.i("✅ Garbage collection requested")
        }
    }

    // Log memory stats after clearing
    val runtime = Runtime.getRuntime()
    val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
    val maxMemory = runtime.maxMemory() / 1024 / 1024
    val availableMemory = maxMemory - usedMemory

    Timber.i("📊 Memory Stats After Cleanup:")
    Timber.i("   Used: ${usedMemory}MB / ${maxMemory}MB")
    Timber.i("   Available: ${availableMemory}MB")
    Timber.w("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
}

@Preview(showBackground = true)
@Composable
fun MultiTabFeedSampleScreenPreview() {
    GithubCruiseTheme {
        MultiTabFeedSampleScreen()
    }
}
