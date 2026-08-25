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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkManagerSampleScreen(
    onBackClick: () -> Unit,
    viewModel: WorkManagerSampleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WorkManager Background Sync") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Task Status & Live Progress Card
            item {
                TaskStatusCard(uiState = uiState)
            }

            // 2. Execution Controls Card
            item {
                ExecutionControlsCard(
                    isRunning = uiState.workState == WorkInfo.State.RUNNING,
                    onStartOneTime = { viewModel.startOneTimeSync(simulateFailure = false) },
                    onStartPeriodic = { viewModel.startPeriodicSync() },
                    onSimulateFailure = { viewModel.startOneTimeSync(simulateFailure = true) },
                    onCancel = { viewModel.cancelActiveWork() }
                )
            }

            // 3. Execution Constraints Card
            item {
                ConstraintsConfigCard(
                    uiState = uiState,
                    onToggleUnmetered = viewModel::toggleUnmetered,
                    onToggleCharging = viewModel::toggleCharging,
                    onToggleBatteryNotLow = viewModel::toggleBatteryNotLow,
                    onToggleStorageNotLow = viewModel::toggleStorageNotLow
                )
            }

            // 4. Live Event Logs Terminal
            item {
                LiveLogsCard(
                    logs = uiState.eventLogs,
                    onClearLogs = viewModel::clearLogs
                )
            }

            // 5. Architectural Guide Card
            item {
                ArchitecturalGuideCard()
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun TaskStatusCard(uiState: WorkManagerUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Worker Status",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                StateBadge(state = uiState.workState)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.currentWorkId != null) {
                Text(
                    text = "Work ID: ${uiState.currentWorkId.toString().take(18)}...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = uiState.currentStep,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { uiState.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progress: ${uiState.progress}%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (uiState.isPeriodic) {
                    Text(
                        text = "Mode: Periodic (15m)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF673AB7)
                    )
                }
            }

            if (uiState.outputSyncedCount != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📦 Synced ${uiState.outputSyncedCount} articles in ${uiState.outputDurationMs}ms (Room SQLite Updated)",
                        modifier = Modifier.padding(10.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

@Composable
private fun StateBadge(state: WorkInfo.State?) {
    val (text, color) = when (state) {
        WorkInfo.State.ENQUEUED -> "ENQUEUED" to Color(0xFF2196F3)
        WorkInfo.State.RUNNING -> "RUNNING" to Color(0xFFFF9800)
        WorkInfo.State.SUCCEEDED -> "SUCCEEDED" to Color(0xFF4CAF50)
        WorkInfo.State.FAILED -> "FAILED" to Color(0xFFF44336)
        WorkInfo.State.CANCELLED -> "CANCELLED" to Color(0xFF9E9E9E)
        WorkInfo.State.BLOCKED -> "BLOCKED" to Color(0xFF9C27B0)
        null -> "IDLE" to Color(0xFF757575)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun ExecutionControlsCard(
    isRunning: Boolean,
    onStartOneTime: () -> Unit,
    onStartPeriodic: () -> Unit,
    onSimulateFailure: () -> Unit,
    onCancel: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚡ Execution Controls",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartOneTime,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("One-Time")
                }

                Button(
                    onClick = onStartPeriodic,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Periodic (15m)")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSimulateFailure,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Test Retry Backoff", fontSize = 12.sp)
                }

                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Cancel Work")
                }
            }
        }
    }
}

@Composable
private fun ConstraintsConfigCard(
    uiState: WorkManagerUiState,
    onToggleUnmetered: (Boolean) -> Unit,
    onToggleCharging: (Boolean) -> Unit,
    onToggleBatteryNotLow: (Boolean) -> Unit,
    onToggleStorageNotLow: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚙️ OS Execution Constraints",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "WorkManager waits until all constraints are met by the OS",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            ConstraintSwitchRow(
                title = "Require Wi-Fi (Unmetered Network)",
                checked = uiState.requireUnmeteredNetwork,
                onCheckedChange = onToggleUnmetered
            )

            ConstraintSwitchRow(
                title = "Require Device Charging",
                checked = uiState.requireCharging,
                onCheckedChange = onToggleCharging
            )

            ConstraintSwitchRow(
                title = "Require Battery Not Low (≥20%)",
                checked = uiState.requireBatteryNotLow,
                onCheckedChange = onToggleBatteryNotLow
            )

            ConstraintSwitchRow(
                title = "Require Storage Not Low",
                checked = uiState.requireStorageNotLow,
                onCheckedChange = onToggleStorageNotLow
            )
        }
    }
}

@Composable
private fun ConstraintSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun LiveLogsCard(
    logs: List<String>,
    onClearLogs: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 Real-Time Logcat Terminal",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
                OutlinedButton(
                    onClick = onClearLogs,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                ) {
                    Text("Clear", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                if (logs.isEmpty()) {
                    Text(
                        text = "No logs yet. Trigger a sync above...",
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                } else {
                    LazyColumn {
                        items(logs) { log ->
                            Text(
                                text = log,
                                color = when {
                                    log.contains("SUCCEEDED") || log.contains("COMPLETE") -> Color(0xFF81C784)
                                    log.contains("FAILED") || log.contains("STOPPED") || log.contains("❌") -> Color(0xFFE57373)
                                    log.contains("RUNNING") || log.contains("Progress") -> Color(0xFFFFB74D)
                                    else -> Color(0xFFE0E0E0)
                                },
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchitecturalGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Staff Architecture Insights",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "• Guaranteed Execution: Tasks persist in internal SQLite and survive app kill & phone reboots.\n" +
                        "• Battery & Doze Friendly: OS batches background work to minimize radio wakeups.\n" +
                        "• Exponential Backoff: Failed network requests retry automatically with exponential delays.\n" +
                        "• Unique Work Policies: REPLACE, KEEP, or APPEND prevent duplicate parallel sync jobs.",
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
