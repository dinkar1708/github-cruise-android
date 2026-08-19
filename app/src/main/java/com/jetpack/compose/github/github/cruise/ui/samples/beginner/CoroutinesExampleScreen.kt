package com.jetpack.compose.github.github.cruise.ui.samples.beginner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Coroutines Example Screen
 * Demonstrates Kotlin coroutines basics with viewModelScope
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
                title = { Text("Coroutines Examples") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
            // Loading indicator
            if (state.isLoading) {
                CircularProgressIndicator()
            }

            // Result/Error
            if (state.result.isNotEmpty()) {
                Text(
                    "Result: ${state.result}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            if (state.error.isNotEmpty()) {
                Text(
                    "Error: ${state.error}",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            // Example 1: launch
            ExampleCard(
                title = "1. launch - Fire and Forget",
                description = "Returns Job, doesn't return result"
            ) {
                Text(
                    """
                    viewModelScope.launch {
                        delay(1000)
                        updateUI()
                    }
                    """.trimIndent(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )

                Button(
                    onClick = { viewModel.launchExample() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Run launch Example")
                }
            }

            // Example 2: async
            ExampleCard(
                title = "2. async - Get Result",
                description = "Returns Deferred, use await() for result"
            ) {
                Text(
                    """
                    val result1 = async { fetchData1() }
                    val result2 = async { fetchData2() }
                    val combined = result1.await() + result2.await()
                    """.trimIndent(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )

                Button(
                    onClick = { viewModel.asyncExample() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Run async Example (Parallel)")
                }
            }

            // Example 3: Dispatchers
            ExampleCard(
                title = "3. Dispatchers",
                description = "Switch threads for different work"
            ) {
                Text("Dispatchers.Main - UI operations")
                Text("Dispatchers.IO - Network/disk")
                Text("Dispatchers.Default - CPU work")

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.dispatchersExample() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Run Dispatchers Example")
                }
            }

            // Example 4: Error Handling
            ExampleCard(
                title = "4. Error Handling",
                description = "Use try-catch in coroutines"
            ) {
                Text(
                    """
                    viewModelScope.launch {
                        try {
                            val data = api.getData()
                        } catch (e: Exception) {
                            showError(e)
                        }
                    }
                    """.trimIndent(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )

                Button(
                    onClick = { viewModel.errorHandlingExample() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Run Error Handling Example")
                }
            }

            // Logs
            ExampleCard(
                title = "Execution Logs",
                description = "Real-time coroutine execution"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    if (state.logs.isEmpty()) {
                        Text("No logs yet - run an example", fontSize = 12.sp)
                    } else {
                        state.logs.takeLast(10).forEach { log ->
                            Text(
                                log,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.clearLogs() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Logs")
                }
            }

            // Key Points
            ExampleCard(
                title = "Key Points",
                description = "Remember"
            ) {
                Text("• launch for fire-and-forget")
                Text("• async for getting results")
                Text("• Use appropriate Dispatcher")
                Text("• Always handle errors")
                Text("• viewModelScope auto-cancels")
                Text("• Check Logcat for thread names")
            }
        }
    }
}

@Composable
private fun ExampleCard(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}
