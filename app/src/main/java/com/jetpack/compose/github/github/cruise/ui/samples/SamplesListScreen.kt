package com.jetpack.compose.github.github.cruise.ui.samples

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class SampleItem(
    val title: String,
    val description: String,
    val category: String,
    val route: String
)

private val ALL_SAMPLES = listOf(
    // Beginner
    SampleItem(
        title = "Null Safety",
        description = "Nullable types, safe call, Elvis operator, let, lateinit",
        category = "Beginner",
        route = SamplesDestinations.NULL_SAFETY_ROUTE
    ),
    SampleItem(
        title = "State & Recomposition",
        description = "remember, state hoisting, rememberSaveable, recomposition scope",
        category = "Beginner",
        route = SamplesDestinations.STATE_RECOMPOSITION_ROUTE
    ),
    SampleItem(
        title = "Data Classes",
        description = "Auto-generated functions, copy(), destructuring",
        category = "Beginner",
        route = SamplesDestinations.DATA_CLASSES_ROUTE
    ),
    SampleItem(
        title = "Sealed Classes",
        description = "Restricted hierarchies, when expressions, state modeling",
        category = "Beginner",
        route = SamplesDestinations.SEALED_CLASSES_ROUTE
    ),
    SampleItem(
        title = "Coroutines Basics",
        description = "launch, async, viewModelScope, Dispatchers, error handling",
        category = "Beginner",
        route = SamplesDestinations.COROUTINES_ROUTE
    ),
    SampleItem(
        title = "Activity Lifecycle & Transitions",
        description = "onCreate to onDestroy, finish() in onCreate trap, onPause vs onStop scenarios",
        category = "Beginner",
        route = SamplesDestinations.LIFECYCLE_ACTIVITY_ROUTE
    ),
    SampleItem(
        title = "Compose Lifecycle & Phases",
        description = "Enter/Leave Composition, Recomposition, remember vs rememberSaveable, DisposableEffect",
        category = "Beginner",
        route = SamplesDestinations.LIFECYCLE_COMPOSE_ROUTE
    ),
    SampleItem(
        title = "ViewModel Lifecycle & Scopes",
        description = "Configuration change survival, SavedStateHandle, viewModelScope auto-cancellation",
        category = "Beginner",
        route = SamplesDestinations.LIFECYCLE_VIEWMODEL_ROUTE
    ),
    SampleItem(
        title = "Lifecycle Observer & Sensors",
        description = "LocalLifecycleOwner, LifecycleEventObserver, sensor registration & battery cleanup",
        category = "Beginner",
        route = SamplesDestinations.LIFECYCLE_OBSERVER_ROUTE
    ),

    // Intermediate
    SampleItem(
        title = "Coroutines Execution Order Quiz",
        description = "runBlocking, launch, async, await, join, Dispatchers print order simulator",
        category = "Intermediate",
        route = SamplesDestinations.COROUTINES_EXECUTION_ORDER_ROUTE
    ),
    SampleItem(
        title = "Coroutine Scopes Usage",
        description = "viewModelScope vs rememberCoroutineScope vs lifecycleScope vs ApplicationScope",
        category = "Intermediate",
        route = SamplesDestinations.COROUTINE_SCOPES_USAGE_ROUTE
    ),
    SampleItem(
        title = "Compose Side Effects Master",
        description = "LaunchedEffect, DisposableEffect, SideEffect, rememberUpdatedState, derivedStateOf, produceState, snapshotFlow",
        category = "Intermediate",
        route = SamplesDestinations.COMPOSE_SIDE_EFFECTS_ROUTE
    ),
    SampleItem(
        title = "LaunchedEffect",
        description = "Side effects, keys, timer, debounce patterns",
        category = "Intermediate",
        route = SamplesDestinations.LAUNCHED_EFFECT_ROUTE
    ),
    SampleItem(
        title = "StateFlow vs SharedFlow vs Channel",
        description = "StateFlow (state), SharedFlow (events), Channel (one-off), Cold Flow comparison",
        category = "Intermediate",
        route = SamplesDestinations.STATE_FLOW_VS_SHARED_FLOW_ROUTE
    ),
    SampleItem(
        title = "ViewModel & Flow Types",
        description = "StateFlow, SharedFlow, Channel, lifecycle",
        category = "Intermediate",
        route = SamplesDestinations.VIEWMODEL_FLOW_ROUTE
    ),
    SampleItem(
        title = "Hilt Dependency Injection",
        description = "@HiltViewModel, Modules, @Provides, @Binds, Qualifiers",
        category = "Intermediate",
        route = SamplesDestinations.HILT_DI_ROUTE
    ),
    SampleItem(
        title = "WorkManager Background Sync",
        description = "CoroutineWorker, Constraints (Wi-Fi, Charging), OneTime vs Periodic, live progress & Logcat logs",
        category = "Intermediate",
        route = SamplesDestinations.WORK_MANAGER_ROUTE
    ),
    SampleItem(
        title = "Compose Swiping & Paging Patterns",
        description = "3D Cube Pager, Centered Card Snapping, Tinder Drag Deck & Hero Carousel (100% XML-Free)",
        category = "Intermediate",
        route = SamplesDestinations.COMPOSE_SWIPING_PATTERNS_ROUTE
    ),
    SampleItem(
        title = "Passing Data Between Screens",
        description = "Route args, Type-safe nav, SharedViewModel, multiple methods",
        category = "Intermediate",
        route = SamplesDestinations.PASSING_DATA_ROUTE
    ),

    // Advanced (Dev Tools)
    SampleItem(
        title = "Memory Leak Detection",
        description = "Detect and fix leaks using Profiler, LeakCanary, heap dumps",
        category = "Advanced",
        route = SamplesDestinations.MEMORY_LEAK_ROUTE
    ),
    SampleItem(
        title = "Performance Monitoring",
        description = "Optimize recomposition, measure FPS, use Profiler tools",
        category = "Advanced",
        route = SamplesDestinations.PERFORMANCE_MONITORING_ROUTE
    ),
    SampleItem(
        title = "RTMP/S Live Video Broadcasting",
        description = "Camera capture, MediaCodec H.264/AAC, and Adaptive Bitrate (ABR)",
        category = "Advanced",
        route = SamplesDestinations.LIVE_BROADCASTING_ROUTE
    ),
    SampleItem(
        title = "Live Stream Room & Audience Interactions",
        description = "Floating hearts canvas, live chat ticker, digital gifts & Super Chat",
        category = "Advanced",
        route = SamplesDestinations.LIVE_STREAM_ROOM_ROUTE
    ),
    SampleItem(
        title = "Multi-Tab Dynamic Feed",
        description = "HorizontalPager windowing (beyondViewportPageCount = 1), LRU image cache & low-memory trim",
        category = "Advanced",
        route = SamplesDestinations.MULTI_TAB_FEED_ROUTE
    )
)



private val beginnerSamples = ALL_SAMPLES.filter { it.category == "Beginner" }
private val intermediateSamples = ALL_SAMPLES.filter { it.category == "Intermediate" }
private val advancedSamples = ALL_SAMPLES.filter { it.category == "Advanced" }

/**
 * Samples List Screen
 * Navigation hub for all training examples
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SamplesListScreen(
    onBackClick: () -> Unit,
    onSampleClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Training Examples") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }

                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item(contentType = "HEADER") {
                Text(
                    "Android Training Examples",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Interactive examples for Kotlin, Compose & Android",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Beginner Section
            item(contentType = "SECTION_TITLE") {
                Text(
                    "Beginner (${beginnerSamples.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(
                items = beginnerSamples,
                key = { it.route },
                contentType = { "SAMPLE_CARD" }
            ) { sample ->
                SampleCard(sample, onSampleClick)
            }

            // Intermediate Section
            item(contentType = "SECTION_TITLE") {
                Text(
                    "Intermediate (${intermediateSamples.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(
                items = intermediateSamples,
                key = { it.route },
                contentType = { "SAMPLE_CARD" }
            ) { sample ->
                SampleCard(sample, onSampleClick)
            }

            // Advanced Section
            item(contentType = "SECTION_TITLE") {
                Text(
                    "Advanced / Dev Tools (${advancedSamples.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(
                items = advancedSamples,
                key = { it.route },
                contentType = { "SAMPLE_CARD" }
            ) { sample ->
                SampleCard(sample, onSampleClick)
            }

            // Footer
            item(contentType = "FOOTER") {
                Text(
                    "\nMore examples coming soon!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun SampleCard(
    sample: SampleItem,
    onSampleClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSampleClick(sample.route) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                sample.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                sample.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
