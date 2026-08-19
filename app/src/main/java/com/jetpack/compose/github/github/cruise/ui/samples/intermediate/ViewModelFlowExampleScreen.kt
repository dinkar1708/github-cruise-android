package com.jetpack.compose.github.github.cruise.ui.samples.intermediate

import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import timber.log.Timber

/**
 * ViewModel & Flow Example Screen
 * Demonstrates StateFlow, SharedFlow, Channel patterns
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewModelFlowExampleScreen(
    onBackClick: () -> Unit,
    viewModel: ViewModelFlowExampleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var eventLog by remember { mutableStateOf<List<String>>(emptyList()) }
    var coldFlowValues by remember { mutableStateOf<List<Int>>(emptyList()) }

    // Collect SharedFlow events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            eventLog = eventLog + event
            Timber.d("Event collected: $event")
        }
    }

    // Collect toast events (Channel)
    LaunchedEffect(Unit) {
        viewModel.toastEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            Timber.d("Toast shown: $message")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ViewModel & Flow Examples") },
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
            // StateFlow Example
            ExampleCard(
                title = "1. StateFlow - For State",
                description = "Always has a value, replays latest to new collectors"
            ) {
                Text("Count: ${state.count}", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                if (state.isLoading) {
                    CircularProgressIndicator()
                }

                if (state.message.isNotEmpty()) {
                    Text(state.message, color = MaterialTheme.colorScheme.primary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.incrementCount() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Increment")
                    }
                    Button(
                        onClick = { viewModel.loadData() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Load Data")
                    }
                }

                Text("StateFlow survives rotation", fontSize = 12.sp)
            }

            // SharedFlow Example
            ExampleCard(
                title = "2. SharedFlow - For Events",
                description = "No initial value, broadcasts to active collectors"
            ) {
                Button(onClick = {
                    viewModel.sendEvent("Button clicked at ${System.currentTimeMillis() % 10000}")
                }) {
                    Text("Send Event")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Event Log:", fontWeight = FontWeight.Bold)
                eventLog.takeLast(5).forEach { event ->
                    Text("• $event", fontSize = 12.sp)
                }
            }

            // Channel Example
            ExampleCard(
                title = "3. Channel - Single Delivery",
                description = "Each event delivered to exactly one collector"
            ) {
                Button(onClick = {
                    viewModel.sendToast("Toast at ${System.currentTimeMillis() % 10000}")
                }) {
                    Text("Send Toast")
                }

                Text("Guarantees single delivery", fontSize = 12.sp)
            }

            // Cold Flow Example
            ExampleCard(
                title = "4. Cold Flow",
                description = "New stream for each collector"
            ) {
                Button(onClick = {
                    coldFlowValues = emptyList()
                    Timber.d("Starting cold flow collection")
                }) {
                    Text("Collect Cold Flow")
                }

                LaunchedEffect(coldFlowValues.isEmpty()) {
                    if (coldFlowValues.isEmpty()) {
                        viewModel.getColdFlow().collect { value ->
                            coldFlowValues = coldFlowValues + value
                        }
                    }
                }


                Text("Values: ${coldFlowValues.joinToString()}")
                Text("Each collection starts new stream", fontSize = 12.sp)
            }

            // ViewModel Lifecycle
            ExampleCard(
                title = "5. ViewModel Lifecycle",
                description = "Survives configuration changes"
            ) {
                Text("ViewModel hashCode: ${viewModel.hashCode()}", fontSize = 12.sp)
                Text("Rotate screen - same instance", fontSize = 12.sp)
                Text("Press back - ViewModel.onCleared()", fontSize = 12.sp)
                Text("Check Logcat for lifecycle logs", fontSize = 12.sp)
            }

            // Summary
            ExampleCard(
                title = "Summary",
                description = "When to use what"
            ) {
                Text("StateFlow:", fontWeight = FontWeight.Bold)
                Text("  • UI state, form state, data state")

                Spacer(modifier = Modifier.height(4.dp))

                Text("SharedFlow:", fontWeight = FontWeight.Bold)
                Text("  • One-time events, navigation, snackbars")

                Spacer(modifier = Modifier.height(4.dp))

                Text("Channel:", fontWeight = FontWeight.Bold)
                Text("  • Guaranteed single delivery")

                Spacer(modifier = Modifier.height(4.dp))

                Text("Cold Flow:", fontWeight = FontWeight.Bold)
                Text("  • Database queries, API calls")
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
