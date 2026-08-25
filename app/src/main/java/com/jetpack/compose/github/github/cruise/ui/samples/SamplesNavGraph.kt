package com.jetpack.compose.github.github.cruise.ui.samples

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.jetpack.compose.github.github.cruise.ui.samples.beginner.CoroutinesExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.beginner.DataClassesExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.beginner.LifecycleActivityExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.beginner.LifecycleComposeExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.beginner.LifecycleObserverExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.beginner.LifecycleViewModelExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.beginner.NullSafetyExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.beginner.SealedClassesExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.beginner.StateRecompositionExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.ComposeSideEffectsSampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.ComposeSwipingPatternsSampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.CoroutineScopesUsageScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.CoroutinesExecutionOrderScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.HiltDIExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.LaunchedEffectExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.PassingDataDetailsScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.PassingDataExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.PassingDataProfileScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.PassingDataSharedScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.StateFlowVsSharedFlowExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.ViewModelFlowExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.WorkManagerSampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.MemoryLeakExamplesScreen
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.PerformanceMonitoringScreen

/**
 * Samples navigation routes
 */
object SamplesDestinations {
    const val SAMPLES_GRAPH_ROUTE = "samples_graph"
    const val SAMPLES_LIST_ROUTE = "samples_list"

    // Beginner
    const val NULL_SAFETY_ROUTE = "null_safety"
    const val STATE_RECOMPOSITION_ROUTE = "state_recomposition"
    const val DATA_CLASSES_ROUTE = "data_classes"
    const val SEALED_CLASSES_ROUTE = "sealed_classes"
    const val COROUTINES_ROUTE = "coroutines"
    const val LIFECYCLE_ACTIVITY_ROUTE = "lifecycle_activity"
    const val LIFECYCLE_OBSERVER_ROUTE = "lifecycle_observer"
    const val LIFECYCLE_COMPOSE_ROUTE = "lifecycle_compose"
    const val LIFECYCLE_VIEWMODEL_ROUTE = "lifecycle_viewmodel"

    // Intermediate
    const val COROUTINE_SCOPES_USAGE_ROUTE = "coroutine_scopes_usage"
    const val COROUTINES_EXECUTION_ORDER_ROUTE = "coroutines_execution_order"
    const val COMPOSE_SIDE_EFFECTS_ROUTE = "compose_side_effects"
    const val STATE_FLOW_VS_SHARED_FLOW_ROUTE = "state_flow_vs_shared_flow"
    const val LAUNCHED_EFFECT_ROUTE = "launched_effect"
    const val VIEWMODEL_FLOW_ROUTE = "viewmodel_flow"
    const val HILT_DI_ROUTE = "hilt_di"
    const val WORK_MANAGER_ROUTE = "work_manager"
    const val COMPOSE_SWIPING_PATTERNS_ROUTE = "compose_swiping_patterns"
    const val PASSING_DATA_ROUTE = "passing_data"
    const val PASSING_DATA_DETAILS_ROUTE = "passing_data/details/{itemId}"
    const val PASSING_DATA_PROFILE_ROUTE = "passing_data/profile/{userId}/{userName}"
    const val PASSING_DATA_SHARED_ROUTE = "passing_data/shared"

    // Advanced (Dev Tools & Media)
    const val MEMORY_LEAK_ROUTE = "memory_leak"
    const val PERFORMANCE_MONITORING_ROUTE = "performance_monitoring"
    const val LIVE_BROADCASTING_ROUTE = "live_broadcasting"
    const val LIVE_STREAM_ROOM_ROUTE = "live_stream_room"
    const val MULTI_TAB_FEED_ROUTE = "multi_tab_feed"
}



/**
 * Adds the samples navigation graph to the app
 *
 * Usage in main NavGraph:
 * ```
 * NavHost(navController, startDestination = HOME_SCREEN_ROUTE) {
 *     // ... other routes
 *     samplesNavGraph(navController)
 * }
 * ```
 *
 * Then navigate from any screen:
 * ```
 * navController.navigate(SamplesDestinations.SAMPLES_LIST_ROUTE)
 * ```
 */
fun NavGraphBuilder.samplesNavGraph(
    navController: NavHostController
) {
    navigation(
        startDestination = SamplesDestinations.SAMPLES_LIST_ROUTE,
        route = SamplesDestinations.SAMPLES_GRAPH_ROUTE
    ) {
        // Samples List
        composable(SamplesDestinations.SAMPLES_LIST_ROUTE) {
            SamplesListScreen(
                onBackClick = { navController.navigateUp() },
                onSampleClick = { route ->
                    navController.navigate(route)
                }
            )
        }

        // Beginner Examples
        composable(SamplesDestinations.NULL_SAFETY_ROUTE) {
            NullSafetyExampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.STATE_RECOMPOSITION_ROUTE) {
            StateRecompositionExampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.DATA_CLASSES_ROUTE) {
            DataClassesExampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.SEALED_CLASSES_ROUTE) {
            SealedClassesExampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.COROUTINES_ROUTE) {
            CoroutinesExampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.LIFECYCLE_ACTIVITY_ROUTE) {
            LifecycleActivityExampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.LIFECYCLE_OBSERVER_ROUTE) {
            LifecycleObserverExampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.LIFECYCLE_COMPOSE_ROUTE) {
            LifecycleComposeExampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.LIFECYCLE_VIEWMODEL_ROUTE) {
            LifecycleViewModelExampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        // Intermediate Examples
        composable(SamplesDestinations.COROUTINE_SCOPES_USAGE_ROUTE) {
            CoroutineScopesUsageScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.COROUTINES_EXECUTION_ORDER_ROUTE) {
            CoroutinesExecutionOrderScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.COMPOSE_SIDE_EFFECTS_ROUTE) {
            ComposeSideEffectsSampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.STATE_FLOW_VS_SHARED_FLOW_ROUTE) {
            StateFlowVsSharedFlowExampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.LAUNCHED_EFFECT_ROUTE) {
            LaunchedEffectExampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.VIEWMODEL_FLOW_ROUTE) {
            ViewModelFlowExampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.HILT_DI_ROUTE) {
            HiltDIExampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.WORK_MANAGER_ROUTE) {
            WorkManagerSampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.COMPOSE_SWIPING_PATTERNS_ROUTE) {
            ComposeSwipingPatternsSampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        // Passing Data Examples
        composable(SamplesDestinations.PASSING_DATA_ROUTE) {
            PassingDataExampleScreen(
                onBackClick = { navController.navigateUp() },
                onNavigateWithId = { itemId ->
                    navController.navigate("passing_data/details/$itemId")
                },
                onNavigateWithMultiple = { userId, userName ->
                    navController.navigate("passing_data/profile/$userId/$userName")
                },
                onNavigateWithSharedViewModel = {
                    navController.navigate(SamplesDestinations.PASSING_DATA_SHARED_ROUTE)
                }
            )
        }

        composable(SamplesDestinations.PASSING_DATA_DETAILS_ROUTE) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId")
            PassingDataDetailsScreen(
                itemId = itemId,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.PASSING_DATA_PROFILE_ROUTE) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            val userName = backStackEntry.arguments?.getString("userName")
            PassingDataProfileScreen(
                userId = userId,
                userName = userName,
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.PASSING_DATA_SHARED_ROUTE) {
            PassingDataSharedScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        // Advanced (Dev Tools) Examples
        composable(SamplesDestinations.MEMORY_LEAK_ROUTE) {
            MemoryLeakExamplesScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(SamplesDestinations.PERFORMANCE_MONITORING_ROUTE) {
            PerformanceMonitoringScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        // Live Broadcasting Example (RTMP/S Creator Experience)
        composable(SamplesDestinations.LIVE_BROADCASTING_ROUTE) {
            com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.LiveBroadcastingScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        // Live Stream Room & Audience Interaction (Floating Hearts & Chat Ticker)
        composable(SamplesDestinations.LIVE_STREAM_ROOM_ROUTE) {
            com.jetpack.compose.github.github.cruise.ui.samples.advanced.LiveStreamRoomScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        // Multi-Tab Dynamic Feed Example (HorizontalPager 3-page windowing & Low-Memory Cache Trim)
        composable(SamplesDestinations.MULTI_TAB_FEED_ROUTE) {
            MultiTabFeedSampleScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}


