package com.jetpack.compose.github.github.cruise.ui.samples.intermediate

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// =============================================================================
// 🧠 VIEWMODEL (IN THE SAME FILE FOR SEAMLESS REFERENCE)
// =============================================================================

/**
 * Self-contained ViewModel demonstrating the differences between:
 * 1. StateFlow (Persistent UI State, Replay = 1, Hot)
 * 2. SharedFlow (Event Bus, Replay = 0, Multi-Subscriber, Hot)
 * 3. Channel (Single Consumer One-Off Events, Hot)
 * 4. Flow (Cold On-Demand Stream)
 *
 * Companion Documentation: `docs/technical/FAQ/intermediate/flow_types.md`
 */
@HiltViewModel
class StateFlowVsSharedFlowViewModel @Inject constructor() : ViewModel() {

    // -------------------------------------------------------------------------
    // 1. StateFlow: Persistent UI State (Requires Initial Value, Replay = 1)
    // -------------------------------------------------------------------------
    private val _counterStateFlow = MutableStateFlow<Int>(0)
    val counterStateFlow: StateFlow<Int> = _counterStateFlow.asStateFlow()

    fun incrementStateFlow() {
        _counterStateFlow.update { it + 1 }
        Timber.d("StateFlow updated to: ${_counterStateFlow.value}")
    }

    fun emitSameStateFlowValue() {
        // StateFlow automatically skips emitting if the value is unchanged!
        _counterStateFlow.value = _counterStateFlow.value
        Timber.d("StateFlow emitted same value (distinctUntilChanged skips recomposition)")
    }

    // -------------------------------------------------------------------------
    // 2. SharedFlow: Broadcast Events (No Initial Value, Replay = 0, Multi-Subscriber)
    // -------------------------------------------------------------------------
    private val _broadcastSharedFlow = MutableSharedFlow<String>(replay = 0)
    val broadcastSharedFlow: SharedFlow<String> = _broadcastSharedFlow.asSharedFlow()

    fun sendBroadcastEvent(message: String) {
        viewModelScope.launch {
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            _broadcastSharedFlow.emit("📢 [$timestamp] $message")
            Timber.d("SharedFlow broadcast emitted: $message")
        }
    }

    // -------------------------------------------------------------------------
    // 3. Channel: One-Off Events (Single Subscriber, No Re-trigger on Rotation)
    // -------------------------------------------------------------------------
    private val _singleDeliveryChannel = Channel<String>(Channel.BUFFERED)
    val singleDeliveryEvents: Flow<String> = _singleDeliveryChannel.receiveAsFlow()

    fun sendOneOffEvent(message: String) {
        viewModelScope.launch {
            _singleDeliveryChannel.send("🍞 One-Off Event: $message")
            Timber.d("Channel sent: $message")
        }
    }

    // -------------------------------------------------------------------------
    // 4. Cold Flow: Starts work from scratch on every collector
    // -------------------------------------------------------------------------
    fun createColdNumberStream(): Flow<Int> = flow {
        Timber.d("Cold Flow started collecting...")
        for (i in 1..5) {
            delay(400L)
            emit(i)
        }
        Timber.d("Cold Flow finished.")
    }
}

// =============================================================================
// 🎨 COMPOSABLE SCREEN (IN THE SAME FILE)
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateFlowVsSharedFlowExampleScreen(
    onBackClick: () -> Unit,
    viewModel: StateFlowVsSharedFlowViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val coroutineScope = rememberCoroutineScope()

    // 1. Observe StateFlow
    val stateFlowValue by viewModel.counterStateFlow.collectAsState()

    // 2. Observe SharedFlow (Multi-Subscriber Log)
    val sharedFlowLogs = remember { mutableStateListOf<String>() }
    LaunchedEffect(Unit) {
        viewModel.broadcastSharedFlow.collect { event ->
            sharedFlowLogs.add(0, event)
            if (sharedFlowLogs.size > 10) sharedFlowLogs.removeLast()
        }
    }

    // 3. Observe Channel for One-Off Toast/Snackbar
    LaunchedEffect(Unit) {
        viewModel.singleDeliveryEvents.collect { eventText ->
            snackbarHostState.showSnackbar(eventText)
        }
    }

    // 4. Cold Flow Local State
    var coldFlowOutput by remember { mutableStateOf("Ready to start") }
    var isColdFlowRunning by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("StateFlow vs SharedFlow vs Channel") },
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
                                text = "Flow Types Master Comparison",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Test each stream primitive below to see how StateFlow (state), SharedFlow (events), Channel (one-off), and Cold Flow (on-demand) behave.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // =================================================================
            // 1. StateFlow
            // =================================================================
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FlowHeader("1. StateFlow (UI State Holder)", "Hot • Replay = 1 • Requires Initial Value • Automatic distinctUntilChanged", Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Current State Value: $stateFlowValue",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.incrementStateFlow() }, modifier = Modifier.weight(1f)) {
                                Text("+1 State")
                            }
                            OutlinedButton(onClick = { viewModel.emitSameStateFlowValue() }, modifier = Modifier.weight(1f)) {
                                Text("Emit Same (No-Op)")
                            }
                        }
                    }
                }
            }

            // =================================================================
            // 2. SharedFlow
            // =================================================================
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FlowHeader("2. SharedFlow (Broadcast Event Bus)", "Hot • Replay = 0 • Multi-Subscriber • Drops events if no active listener", Color(0xFF2196F3))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.sendBroadcastEvent("Push Notification / Chat Message") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Emit Broadcast Event")
                        }

                        if (sharedFlowLogs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Received Broadcast Stream:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            sharedFlowLogs.forEach { log ->
                                Text(log, fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }

            // =================================================================
            // 3. Channel
            // =================================================================
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FlowHeader("3. Channel (One-Off Single Delivery)", "Hot • 1 Subscriber receives each item • Safe from duplicate execution on rotation", Color(0xFFFF9800))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.sendOneOffEvent("Item Saved to Database") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Send One-Off Snackbar Event")
                        }
                    }
                }
            }

            // =================================================================
            // 4. Cold Flow
            // =================================================================
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FlowHeader("4. Cold Flow (On-Demand Data Stream)", "Cold • Starts new stream for each collector • Used in Room DB & DataStore", Color(0xFF9C27B0))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Cold Flow Stream: $coldFlowOutput",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isColdFlowRunning = true
                                    viewModel.createColdNumberStream().collect { num ->
                                        coldFlowOutput = "Emitted: $num of 5"
                                    }
                                    coldFlowOutput = "✅ Stream Finished (5 items)"
                                    isColdFlowRunning = false
                                }
                            },
                            enabled = !isColdFlowRunning,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isColdFlowRunning) "Streaming..." else "Start Cold Stream (1..5)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowHeader(title: String, subtitle: String, indicatorColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(indicatorColor, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
