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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

/**
 * Interactive Sample Screen demonstrating ALL 4 Cases of Coroutine Scope Usage in Android:
 * 1. `viewModelScope` (95% of business logic, survives rotation)
 * 2. `rememberCoroutineScope()` (UI-initiated suspend calls + immediate Snackbar/Animation)
 * 3. `lifecycleScope` / `repeatOnLifecycle` (Activity/Host flow collection)
 * 4. `ApplicationScope` (Fire-and-Forget background tasks that survive screen exit)
 *
 * Companion Documentation: `docs/technical/FAQ/intermediate/when_to_use_coroutine_scopes.md`
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoroutineScopesUsageScreen(
    onBackClick: () -> Unit,
    viewModel: CoroutineScopesUsageViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val vmState by viewModel.viewModelScopeState.collectAsState()
    val appScopeLogs by viewModel.applicationScopeLogs.collectAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Coroutine Scopes Usage") },
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
            // Header Info Banner
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
                                text = "Structured Concurrency in Action",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Test each of the 4 real-world Coroutine Scopes below to see how lifecycle, cancellation, and execution work in production.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // =========================================================================
            // CASE 1: viewModelScope
            // =========================================================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF4CAF50), CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Case 1: viewModelScope (95% of App Logic)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Survives screen rotation.\n• Automatically cancelled when screen is closed (onCleared).\n• Best for: API network requests and Room DB operations.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Status: $vmState",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.fetchDataViaViewModelScope() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("1. Launch API Call in viewModelScope")
                        }
                    }
                }
            }

            // =========================================================================
            // CASE 2: rememberCoroutineScope() Calling ViewModel Suspend Function
            // =========================================================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF2196F3), CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Case 2: rememberCoroutineScope()", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Bound to this Composable's position in UI tree.\n• Allows UI to call a ViewModel `suspend` function and directly await the result to show a Snackbar.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                // ✅ Calling ViewModel suspend function from rememberCoroutineScope:
                                coroutineScope.launch {
                                    val result = viewModel.performActionWithSuspendResult("Quick Action")
                                    snackbarHostState.showSnackbar(result) // Awaited and shown immediately!
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("2. Call Suspend API + Show Instant Snackbar")
                        }
                    }
                }
            }

            // =========================================================================
            // CASE 3: lifecycleScope / repeatOnLifecycle (Host Level)
            // =========================================================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF9800), CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Case 3: lifecycleScope (Activity / Host Level)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Used in MainActivity via `repeatOnLifecycle(STARTED)`.\n• Automatically PAUSES when you press Home (ON_STOP) to save 100% battery, and RESUMES on return.\n• Best for: In-App Updates, Biometric Prompts, and System Dialogs.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Check Logcat logs to see MainActivity's lifecycleScope in action!",
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize = 12.sp,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            // =========================================================================
            // CASE 4: Injected ApplicationScope (Fire-and-Forget) vs GlobalScope
            // =========================================================================
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF9C27B0), CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Case 4: Injected ApplicationScope", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Safe replacement for GlobalScope.\n• Injected via Hilt (`SupervisorJob() + Dispatchers.Default`).\n• Survives even if you press Back button and leave this screen!\n• Best for: Offline sync, disk cache flush, and analytics logging.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { viewModel.triggerApplicationScopeTask() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("4. Trigger Fire-and-Forget Background Task")
                        }

                        if (appScopeLogs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Background Task Stream:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            appScopeLogs.forEach { log ->
                                Text(text = log, fontFamily = FontFamily.Monospace, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
