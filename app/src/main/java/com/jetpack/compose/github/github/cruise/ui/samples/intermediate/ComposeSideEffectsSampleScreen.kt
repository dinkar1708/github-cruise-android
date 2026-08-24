package com.jetpack.compose.github.github.cruise.ui.samples.intermediate

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Interactive Sample Screen demonstrating ALL 8 Jetpack Compose Side-Effect APIs:
 * 1. LaunchedEffect(key)
 * 2. rememberCoroutineScope()
 * 3. DisposableEffect(key)
 * 4. SideEffect { }
 * 5. rememberUpdatedState(value)
 * 6. derivedStateOf { }
 * 7. produceState(initial)
 * 8. snapshotFlow { }
 *
 * Companion Documentation: `docs/technical/FAQ/intermediate/compose_side_effects_guide.md`
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeSideEffectsSampleScreen(
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Compose Side Effects Master") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Jetpack Compose Side-Effects Suite",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Interact with each handler below to see how lifecycle, cancellation, state buffering, and cleanup work.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 1. LaunchedEffect(key)
            item { LaunchedEffectInteractiveCard() }

            // 2. rememberCoroutineScope()
            item { RememberCoroutineScopeInteractiveCard(snackbarHostState) }

            // 3. DisposableEffect(key)
            item { DisposableEffectInteractiveCard() }

            // 4. SideEffect { }
            item { SideEffectInteractiveCard() }

            // 5. rememberUpdatedState(value)
            item { RememberUpdatedStateInteractiveCard(snackbarHostState) }

            // 6. derivedStateOf { }
            item { DerivedStateOfInteractiveCard() }

            // 7. produceState(initial)
            item { ProduceStateInteractiveCard() }

            // 8. snapshotFlow { }
            item { SnapshotFlowInteractiveCard() }
        }
    }
}

// =========================================================================
// 1. LaunchedEffect(key)
// =========================================================================
@Composable
private fun LaunchedEffectInteractiveCard() {
    var searchQuery by remember { mutableStateOf("") }
    var debouncedOutput by remember { mutableStateOf("Type to search...") }
    var isDebouncing by remember { mutableStateOf(false) }

    // Re-launches debounce timer every time `searchQuery` changes:
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            isDebouncing = true
            delay(600L) // 600ms debounce
            debouncedOutput = "🔍 Search Executed for: '$searchQuery'"
            isDebouncing = false
        } else {
            debouncedOutput = "Type to search..."
            isDebouncing = false
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            EffectHeader("1. LaunchedEffect(key)", "Cancels and restarts coroutine when key changes", Color(0xFF4CAF50))
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Repository (600ms Debounce)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isDebouncing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Debouncing...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                } else {
                    Text(debouncedOutput, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                }
            }
        }
    }
}

// =========================================================================
// 2. rememberCoroutineScope()
// =========================================================================
@Composable
private fun RememberCoroutineScopeInteractiveCard(snackbarHostState: SnackbarHostState) {
    val coroutineScope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            EffectHeader("2. rememberCoroutineScope()", "User-initiated coroutines in onClick callbacks", Color(0xFF2196F3))
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        isProcessing = true
                        delay(1000L)
                        isProcessing = false
                        snackbarHostState.showSnackbar("✅ Action completed in rememberCoroutineScope!")
                    }
                },
                enabled = !isProcessing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing...")
                } else {
                    Text("Trigger UI Coroutine (1s Delay + Snackbar)")
                }
            }
        }
    }
}

// =========================================================================
// 3. DisposableEffect(key)
// =========================================================================
@Composable
private fun DisposableEffectInteractiveCard() {
    var isListenerEnabled by remember { mutableStateOf(true) }
    var listenerStatus by remember { mutableStateOf("Inactive") }

    if (isListenerEnabled) {
        DisposableEffect(Unit) {
            listenerStatus = "🟢 Listener Registered & Active"
            Timber.d("DisposableEffect: Registered")

            onDispose {
                listenerStatus = "🛑 Listener Disposed & Cleaned Up (0% leak)"
                Timber.d("DisposableEffect: onDispose called")
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            EffectHeader("3. DisposableEffect(key)", "Setup with mandatory onDispose cleanup", Color(0xFFFF9800))
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Simulated Broadcast Listener:")
                Switch(checked = isListenerEnabled, onCheckedChange = { isListenerEnabled = it })
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(listenerStatus, fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// =========================================================================
// 4. SideEffect { }
// =========================================================================
@Composable
private fun SideEffectInteractiveCard() {
    var clickCounter by remember { mutableIntStateOf(0) }
    var externalAnalyticsLoggedCount by remember { mutableIntStateOf(0) }

    // SideEffect runs after every successful recomposition:
    SideEffect {
        externalAnalyticsLoggedCount = clickCounter
        Timber.d("SideEffect synced with external analytics: $externalAnalyticsLoggedCount")
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            EffectHeader("4. SideEffect { }", "Runs after every successful recomposition (Syncs non-Compose state)", Color(0xFF9C27B0))
            Spacer(modifier = Modifier.height(10.dp))
            Text("Compose State Taps: $clickCounter | External Analytics Count: $externalAnalyticsLoggedCount")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { clickCounter++ }, modifier = Modifier.fillMaxWidth()) {
                Text("Tap to Trigger Recomposition ($clickCounter)")
            }
        }
    }
}

// =========================================================================
// 5. rememberUpdatedState(value)
// =========================================================================
@Composable
private fun RememberUpdatedStateInteractiveCard(snackbarHostState: SnackbarHostState) {
    var timerMessage by remember { mutableStateOf("Initial Message (Tap button to change)") }
    var timerRunning by remember { mutableStateOf(false) }

    if (timerRunning) {
        SplashTimerEffect(message = timerMessage) { finalMsg ->
            timerRunning = false
            snackbarHostState.showSnackbar("Timer Finished! Received: $finalMsg")
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            EffectHeader("5. rememberUpdatedState(value)", "Captures newest value in long-running job without restarting effect", Color(0xFF009688))
            Spacer(modifier = Modifier.height(10.dp))
            Text("Current Message: $timerMessage", fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        timerMessage = "Updated at ${System.currentTimeMillis() % 10000}"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Change Msg")
                }
                Button(
                    onClick = { timerRunning = true },
                    enabled = !timerRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (timerRunning) "Running 3s..." else "Start 3s Timer")
                }
            }
        }
    }
}

@Composable
private fun SplashTimerEffect(message: String, onFinished: suspend (String) -> Unit) {
    // Wrap changing values in rememberUpdatedState:
    val currentMessage by rememberUpdatedState(message)
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        // Runs once for 3 seconds without restarting when `message` changes!
        delay(3000L)
        currentOnFinished(currentMessage) // Passes the latest updated message!
    }
}

// =========================================================================
// 6. derivedStateOf { }
// =========================================================================
@Composable
private fun DerivedStateOfInteractiveCard() {
    var rawItemCount by remember { mutableIntStateOf(0) }

    // derivedStateOf recalculates ONLY when the boolean condition changes:
    val hasQualifiedItems by remember {
        derivedStateOf { rawItemCount >= 5 }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            EffectHeader("6. derivedStateOf { }", "State buffering to minimize redundant UI recompositions", Color(0xFFE91E63))
            Spacer(modifier = Modifier.height(10.dp))
            Text("Raw Items: $rawItemCount | Threshold Qualified (>=5): $hasQualifiedItems")
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { rawItemCount++ }, modifier = Modifier.weight(1f)) {
                    Text("+1 Item")
                }
                Button(onClick = { rawItemCount = 0 }, modifier = Modifier.weight(1f)) {
                    Text("Reset")
                }
            }
        }
    }
}

// =========================================================================
// 7. produceState(initial)
// =========================================================================
@SuppressLint("ProduceStateDoesNotAssignValue")
@Composable
private fun ProduceStateInteractiveCard() {
    var userId by remember { mutableIntStateOf(101) }

    // Converts async network simulation into Compose State:
    val userProfileState by produceState(initialValue = "Loading profile...", userId) {
        value = "Loading profile for User #$userId..."
        delay(1200L) // Simulating network API call
        value = "✅ Profile Loaded: Developer #$userId (Senior Android Engineer)"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            EffectHeader("7. produceState(initial, key)", "Converts external data / callbacks into Compose State", Color(0xFF3F51B5))
            Spacer(modifier = Modifier.height(10.dp))
            Text(userProfileState, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { userId++ }, modifier = Modifier.fillMaxWidth()) {
                Text("Next User Profile (Key Change -> User #$userId)")
            }
        }
    }
}

// =========================================================================
// 8. snapshotFlow { }
// =========================================================================
@Composable
private fun SnapshotFlowInteractiveCard() {
    var sliderValue by remember { mutableIntStateOf(10) }
    var flowEmittedLogs by remember { mutableStateOf("Flow waiting for state changes...") }

    // Converts Compose state into a Kotlin Flow:
    LaunchedEffect(Unit) {
        snapshotFlow { sliderValue }
            .distinctUntilChanged()
            .collect { value ->
                flowEmittedLogs = "🌊 snapshotFlow emitted: Value = $value"
                Timber.d(flowEmittedLogs)
            }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            EffectHeader("8. snapshotFlow { }", "Converts Compose State<T> into standard Kotlin Flow<T>", Color(0xFF607D8B))
            Spacer(modifier = Modifier.height(10.dp))
            Text(flowEmittedLogs, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { sliderValue += 5 }, modifier = Modifier.weight(1f)) {
                    Text("+5 to State")
                }
                Button(onClick = { sliderValue -= 5 }, modifier = Modifier.weight(1f)) {
                    Text("-5 to State")
                }
            }
        }
    }
}

@Composable
private fun EffectHeader(title: String, subtitle: String, indicatorColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(indicatorColor, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
