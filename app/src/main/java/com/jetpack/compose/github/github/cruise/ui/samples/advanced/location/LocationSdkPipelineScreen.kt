package com.jetpack.compose.github.github.cruise.ui.samples.advanced.location

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.location.model.LocationSdkUiState
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.location.model.LocationTelemetryRecord
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Location SDK & High-Throughput Data Pipeline Screen
 *
 * Demonstrates:
 * 1. Modular Mobile SDK Architecture (Initialization, zero-crash isolation, Proguard rules)
 * 2. Battery-Optimized Adaptive Duty-Cycling (Activity Recognition STILL vs WALKING vs VEHICLE)
 * 3. High-Throughput Batch Ingestion & Snowflake/DWH Stream Packaging
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSdkPipelineScreen(
    onBackClick: () -> Unit,
    viewModel: LocationSdkPipelineViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("SDK Architecture", "Battery & Duty Cycle", "Cloud Ingestion")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Location SDK & Data Pipeline") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Logs")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Control Strip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (state.isSdkInitialized) "SDK: v${state.sdkVersion} (Ready)" else "SDK: Uninitialized",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (state.isSdkInitialized) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (state.isTrackingActive) "Tracking: ACTIVE (${state.currentActivity})" else "Tracking: STANDBY",
                            fontSize = 11.sp,
                            color = if (state.isTrackingActive) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!state.isSdkInitialized) {
                            Button(
                                onClick = { viewModel.initializeSdk() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Init SDK", fontSize = 11.sp)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.toggleTracking() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.isTrackingActive) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                                )
                            ) {
                                Text(if (state.isTrackingActive) "Stop" else "Start Tracking", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Tab Bar
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> LocationSdkArchitectureTab(state, viewModel)
                    1 -> LocationDutyCycleTab(state, viewModel)
                    2 -> LocationCloudIngestionTab(state, viewModel)
                }
            }

            // Live Terminal Console Strip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("💻 Location SDK Pipeline Logs", color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${state.terminalLogs.size} lines", color = Color.Gray, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF121212), RoundedCornerShape(4.dp))
                            .padding(6.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (state.terminalLogs.isEmpty()) {
                            Text("Initialize the SDK or start tracking to inspect live telemetry events...", color = Color.DarkGray, fontSize = 10.sp)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                state.terminalLogs.forEach { log ->
                                    Text(log, color = Color(0xFFE0E0E0), fontFamily = FontFamily.Monospace, fontSize = 10.sp, lineHeight = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationSdkArchitectureTab(
    state: LocationSdkUiState,
    viewModel: LocationSdkPipelineViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("🏛️ Location SDK Architecture & API Design", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📦 Public Initialization Pattern:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = """
LocationSdk.initialize(
    context = applicationContext,
    config = LocationSdkConfig.Builder("sdk_live_k8s_9a87...")
        .setFlushInterval(15_000L)
        .setBatchSize(50)
        .build()
)
                        """.trimIndent(),
                        color = Color(0xFF81D4FA),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("🛡️ Production SDK Best Practices:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("• 🔒 Strict Encapsulation: All DAOs & dispatchers marked `internal` to prevent host namespace pollution.", fontSize = 12.sp)
                Text("• 🛡️ Zero-Crash Guarantee: Internal exception barriers prevent SDK crashes from affecting host apps.", fontSize = 12.sp)
                Text("• 📜 Consumer Proguard Rules: Packed in AAR (`consumer-rules.pro`) to protect serialization models.", fontSize = 12.sp)
                Text("• 📦 Ultra-lightweight: Zero heavy transitive dependencies to keep AAR < 350KB.", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun LocationDutyCycleTab(
    state: LocationSdkUiState,
    viewModel: LocationSdkPipelineViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("🔋 Adaptive Battery Duty-Cycling Engine", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Activity Recognition State:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.currentActivity == "STILL",
                        onClick = { viewModel.setActivityState("STILL") },
                        label = { Text("STILL 🧍") }
                    )
                    FilterChip(
                        selected = state.currentActivity == "WALKING",
                        onClick = { viewModel.setActivityState("WALKING") },
                        label = { Text("WALKING 🚶") }
                    )
                    FilterChip(
                        selected = state.currentActivity == "IN_VEHICLE",
                        onClick = { viewModel.setActivityState("IN_VEHICLE") },
                        label = { Text("VEHICLE 🚗") }
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("⚡ Dynamic Sensor Hardware Throttling:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GPS Sampling Interval:", fontSize = 12.sp)
                    Text("${state.currentSamplingIntervalSec} seconds", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Estimated Battery Drain:", fontSize = 12.sp)
                    Text("~${state.estimatedBatteryDrainPerHour}% / hour", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFBF360C))
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("💡 Why Adaptive Duty-Cycling Saves 85%+ Battery:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("• When user is STILL (e.g. at desk or home), GPS polling drops to 120s-300s, slashing battery consumption.", fontSize = 11.sp)
                Text("• Google `ActivityRecognitionClient` transitions instantaneously ramp up to 5s only during driving speeds.", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun LocationCloudIngestionTab(
    state: LocationSdkUiState,
    viewModel: LocationSdkPipelineViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("☁️ High-Throughput Batch Ingestion & DWH Pipeline", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Local Buffer Queue", fontSize = 11.sp)
                    Text("${state.localBufferQueue.size} records", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Ingested", fontSize = 11.sp)
                    Text("${state.totalRecordsIngested} records", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Batches Synced", fontSize = 11.sp)
                    Text("${state.uploadedBatchesCount} batches", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Button(
            onClick = { viewModel.flushBatchToCloud() },
            enabled = state.localBufferQueue.isNotEmpty() && !state.isSyncingBatch,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (state.isSyncingBatch) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compressing & Ingesting Batch...")
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Flush Batch to Cloud Gateway (${state.localBufferQueue.size} items)")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("📜 Snowflake / DWH Stream JSON Schema:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = """
{
  "event_id": "9a8f21b0",
  "timestamp_epoch_ms": 1723618400120,
  "lat": 35.68124,
  "lng": 139.76712,
  "accuracy_meters": 5.0,
  "activity_type": "WALKING",
  "speed_kmh": 4.8,
  "sdk_version": "2.4.0",
  "battery_pct": 88
}
                        """.trimIndent(),
                        color = Color(0xFFC3E88D),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
