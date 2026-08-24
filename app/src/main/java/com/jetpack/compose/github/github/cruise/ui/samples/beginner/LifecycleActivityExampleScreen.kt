package com.jetpack.compose.github.github.cruise.ui.samples.beginner

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.jetpack.compose.github.github.cruise.ui.theme.GithubCruiseTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * Interactive Android Activity Lifecycle Demonstration Screen
 *
 * Demonstrates:
 * 1. The 6 core Activity lifecycle states: onCreate, onStart, onResume, onPause, onStop, onDestroy
 * 2. Scenario A (Partially Obscured): onPause ➔ onResume (System dialog, biometric prompt, PiP)
 * 3. Scenario B (Completely Hidden): onPause ➔ onStop ➔ onRestart ➔ onStart ➔ onResume (Home press, app switch)
 * 4. Calling finish() in onCreate() trap (skips onStart, onResume, onPause, onStop)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifecycleActivityExampleScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showDialogOverlay by remember { mutableStateOf(false) }
    var currentLifecycleState by remember { mutableStateOf(lifecycleOwner.lifecycle.currentState.name) }
    val logs = remember { mutableStateListOf<String>() }

    fun addLog(event: String, description: String) {
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        logs.add(0, "[$time] $event — $description")
    }

    // Real-time Activity Lifecycle Observer
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            currentLifecycleState = lifecycleOwner.lifecycle.currentState.name
            when (event) {
                Lifecycle.Event.ON_CREATE -> addLog("🟢 ON_CREATE", "Activity created & initialized")
                Lifecycle.Event.ON_START -> addLog("🟢 ON_START", "Activity is visible to user")
                Lifecycle.Event.ON_RESUME -> addLog("⚡ ON_RESUME", "Activity in foreground & fully interactive")
                Lifecycle.Event.ON_PAUSE -> addLog("⏸️ ON_PAUSE", "Activity partially obscured (Lost user focus)")
                Lifecycle.Event.ON_STOP -> addLog("🛑 ON_STOP", "Activity 100% hidden (App in background)")
                Lifecycle.Event.ON_DESTROY -> addLog("💀 ON_DESTROY", "Activity destroyed & memory released")
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Lifecycle & Transitions") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current State Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "📱 Current Activity Lifecycle State",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "STATE: $currentLifecycleState",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "The Activity is the OS-level Window Host. Press Home or switch apps to see live ON_PAUSE ➔ ON_STOP events!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Scenario A: Partial Obscuring (Dialog Overlay)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🧪 Scenario A: Partially Obscured", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "When a system dialog, permission sheet, or transparent window opens, the Activity stays VISIBLE but loses focus.\n" +
                                    "Trigger: ONLY onPause() ➔ onResume() (onStop is NEVER called).",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { showDialogOverlay = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Dialog Overlay (Trigger Partial Focus Loss)")
                        }
                    }
                }
            }

            // finish() in onCreate() Trap Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "⚠️ The finish() in onCreate() Trap",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "• Calling finish() inside onCreate() jumps DIRECTLY to onDestroy()!\n" +
                                    "• It SKIPS onStart(), onResume(), onPause(), and onStop().\n" +
                                    "• ALWAYS add 'return' after finish() because code below it continues to execute!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Real-Time Event Logs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Live Transition Logs (${logs.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    TextButton(onClick = { logs.clear() }) {
                        Text("Clear Logs")
                    }
                }
            }

            items(logs) { log ->
                Text(
                    text = log,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.extraSmall
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }

    // Modal Dialog to test focus loss
    if (showDialogOverlay) {
        AlertDialog(
            onDismissRequest = { showDialogOverlay = false },
            title = { Text("Overlay Modal Dialog") },
            text = { Text("The Activity behind this dialog remains partially visible. Dismiss to return focus.") },
            confirmButton = {
                TextButton(onClick = { showDialogOverlay = false }) {
                    Text("Dismiss (Regain Focus)")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LifecycleActivityExampleScreenPreview() {
    GithubCruiseTheme {
        LifecycleActivityExampleScreen(onBackClick = {})
    }
}
