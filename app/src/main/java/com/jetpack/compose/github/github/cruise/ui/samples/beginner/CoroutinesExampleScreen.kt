package com.jetpack.compose.github.github.cruise.ui.samples.beginner

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Coroutines Basics & Error Handling Suite Screen
 *
 * Demonstrates:
 * 1. Coroutine Builders: launch vs async
 * 2. Error Pattern 1: In-Place try-catch (Local handling)
 * 3. Error Pattern 2: supervisorScope vs coroutineScope (Parallel Isolation vs Cascading Failure)
 * 4. Error Pattern 3: CoroutineExceptionHandler (Global Uncaught Safety Net)
 * 5. Error Pattern 5: Reactive Flow .catch {} and .retryWhen {} with Backoff
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoroutinesExampleScreen(
    onBackClick: () -> Unit,
    viewModel: CoroutinesExampleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coroutines & Error Handling") },
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Loading State Banner
            if (state.isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.height(24.dp).width(24.dp), strokeWidth = 2.dp)
                        Text("Coroutine is executing...", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Success or Error Output
            if (state.result.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Text(
                        text = "✅ Result: ${state.result}",
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            if (state.error.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = "⚠️ Caught Error: ${state.error}",
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Real-Time Terminal Logs Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💻 Live Execution Logcat", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${state.logs.size} entries", color = Color.Gray, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Color(0xFF121212), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (state.logs.isEmpty()) {
                            Text("Press any button below to trigger coroutines and inspect live traces...", color = Color.DarkGray, fontSize = 11.sp)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                state.logs.forEach { log ->
                                    Text(log, color = Color(0xFFE0E0E0), fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Section 1: Builders
            Text("🚀 1. Coroutine Builders (launch & async)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.launchExample() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("launch (Fire & Forget)", fontSize = 11.sp)
                }
                Button(
                    onClick = { viewModel.asyncExample() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("async (Parallel await)", fontSize = 11.sp)
                }
            }

            // Section 2: Error Pattern 1 (In-Place try-catch)
            Text("🛡️ 2. Pattern 1: Local In-Place try-catch", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Catches simulated HTTP 503 locally while rethrowing CancellationException to keep scope healthy.", fontSize = 12.sp)
                    Button(
                        onClick = { viewModel.safeTryCatchExample() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Run Safe try-catch")
                    }
                }
            }

            // Section 3: Error Pattern 2 (supervisorScope vs coroutineScope)
            Text("⚖️ 3. Pattern 2: supervisorScope vs coroutineScope", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Comparison: What happens when 1 parallel child fails?", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.coroutineScopeFailureTrapExample() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("❌ Standard Scope (Kills All)", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                        }
                        Button(
                            onClick = { viewModel.supervisorScopeResilientExample() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("✅ supervisorScope (Isolated)", fontSize = 10.sp)
                        }
                    }
                }
            }

            // Section 4: Error Pattern 3 (CoroutineExceptionHandler)
            Text("🎯 4. Pattern 3: CoroutineExceptionHandler (CEH)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Acts as a global last-resort crash catcher for Root `launch` operations (e.g. Firebase Crashlytics logging).", fontSize = 12.sp)
                    Button(
                        onClick = { viewModel.coroutineExceptionHandlerExample() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Root CEH Safety Net")
                    }
                }
            }

            // Section 5: Error Pattern 5 (Flow .catch & .retryWhen)
            Text("🌊 5. Pattern 5: Flow .catch {} & .retryWhen {}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Simulates a failing stream that performs 2 retries with backoff, then recovers via `.catch {}` to emit offline cached fallback data.", fontSize = 12.sp)
                    Button(
                        onClick = { viewModel.flowCatchAndRetryExample() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006874))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Flow Retries & Fallback")
                    }
                }
            }
        }
    }
}
