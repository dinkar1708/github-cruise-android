package com.jetpack.compose.github.github.cruise.ui.samples

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.jetpack.compose.github.github.cruise.ui.samples.beginner.CoroutinesExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.beginner.DataClassesExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.beginner.NullSafetyExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.beginner.SealedClassesExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.beginner.StateRecompositionExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.HiltDIExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.LaunchedEffectExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.PassingDataDetailsScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.PassingDataExampleScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.PassingDataProfileScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.PassingDataSharedScreen
import com.jetpack.compose.github.github.cruise.ui.samples.intermediate.ViewModelFlowExampleScreen
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

    // Intermediate
    const val LAUNCHED_EFFECT_ROUTE = "launched_effect"
    const val VIEWMODEL_FLOW_ROUTE = "viewmodel_flow"
    const val HILT_DI_ROUTE = "hilt_di"
    const val PASSING_DATA_ROUTE = "passing_data"
    const val PASSING_DATA_DETAILS_ROUTE = "passing_data/details/{itemId}"
    const val PASSING_DATA_PROFILE_ROUTE = "passing_data/profile/{userId}/{userName}"
    const val PASSING_DATA_SHARED_ROUTE = "passing_data/shared"

    // Advanced (Dev Tools & Media)
    const val MEMORY_LEAK_ROUTE = "memory_leak"
    const val PERFORMANCE_MONITORING_ROUTE = "performance_monitoring"
    const val LIVE_BROADCASTING_ROUTE = "live_broadcasting"
    const val LIVE_STREAM_ROOM_ROUTE = "live_stream_room"
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

        // Intermediate Examples
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
    }
}


