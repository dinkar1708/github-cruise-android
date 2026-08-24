package com.jetpack.compose.github.github.cruise.ui.samples.beginner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetpack.compose.github.github.cruise.ui.theme.GithubCruiseTheme

/**
 * Interactive ViewModel Lifecycle Demonstration Screen
 *
 * Demonstrates:
 * 1. ViewModel retention across Screen Rotation / Configuration changes
 * 2. Automatic coroutine cancellation in viewModelScope when onCleared() is invoked
 * 3. SavedStateHandle survival across Android system process death
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifecycleViewModelExampleScreen(
    onBackClick: () -> Unit,
    viewModel: LifecycleViewModelExampleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedTextInput by viewModel.savedTextInput.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ViewModel Lifecycle & Scopes") },
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
            // Instance Retainment Card
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
                            "🛡️ Configuration Change Survival",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "ViewModel Instance Hash: #${uiState.instanceHashCode}\n" +
                                    "Created At: ${uiState.createdTimestamp}\n\n" +
                                    "🔄 TEST: Rotate your phone or toggle dark mode! The Activity will be destroyed and recreated, but the ViewModel Hash and Timer below will NOT reset!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // viewModelScope Background Timer Card
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
                        Text("⏳ viewModelScope Background Task", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        Text(
                            "Timer: ${uiState.timerCount} seconds (${if (uiState.isTimerRunning) "🟢 Running" else "🛑 Paused"})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            "Coroutines launched in viewModelScope automatically cancel when this screen is popped from the backstack (onCleared).",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (uiState.isTimerRunning) viewModel.stopBackgroundTimer()
                                    else viewModel.startBackgroundTimer()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (uiState.isTimerRunning) "Pause Timer" else "Resume Timer")
                            }

                            OutlinedButton(
                                onClick = { viewModel.resetTimer() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset Timer")
                            }
                        }
                    }
                }
            }

            // SavedStateHandle Process Death Card
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
                            "💾 SavedStateHandle (Process Death Recovery)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Text(
                            "SavedStateHandle writes to Android's SavedStateRegistry. It survives both screen rotation AND low-memory OS process termination.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        OutlinedTextField(
                            value = savedTextInput,
                            onValueChange = { viewModel.onTextInputChanged(it) },
                            label = { Text("Enter persisted note") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Live Logs Header & List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "ViewModel Event Logs (${uiState.logs.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    TextButton(onClick = { viewModel.clearLogs() }) {
                        Text("Clear Logs")
                    }
                }
            }

            items(uiState.logs) { log ->
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
}

@Preview(showBackground = true)
@Composable
fun LifecycleViewModelExampleScreenPreview() {
    GithubCruiseTheme {
        LifecycleViewModelExampleScreen(onBackClick = {})
    }
}
