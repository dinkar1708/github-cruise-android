package com.jetpack.compose.github.github.cruise.ui.samples.beginner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import timber.log.Timber

// Example sealed classes
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: String) : UiState()
    data class Error(val message: String) : UiState()
}

sealed class NavigationEvent {
    object Back : NavigationEvent()
    data class ToDetails(val id: String) : NavigationEvent()
    data class ToSettings(val tab: String) : NavigationEvent()
}

/**
 * Sealed Classes Example Screen
 * Demonstrates sealed classes and when expressions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SealedClassesExampleScreen(
    onBackClick: () -> Unit
) {
    var uiState: UiState by remember { mutableStateOf(UiState.Loading) }
    var navEvent: NavigationEvent? by remember { mutableStateOf(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sealed Classes Examples") },
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
            // Example 1: Sealed Class Definition
            ExampleCard(
                title = "1. Sealed Class Definition",
                description = "Restricted class hierarchy"
            ) {
                Text(
                    """
                    sealed class UiState {
                        object Loading : UiState()
                        data class Success(val data: String) : UiState()
                        data class Error(val message: String) : UiState()
                    }
                    """.trimIndent(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Text("\nAll subclasses are known at compile-time", fontSize = 12.sp)
            }

            // Example 2: When Expression (Exhaustive)
            ExampleCard(
                title = "2. Exhaustive When Expression",
                description = "No else needed - compiler knows all cases"
            ) {
                Text("Current state: ${uiState::class.simpleName}")

                Spacer(modifier = Modifier.height(8.dp))

                // Render based on state
                when (uiState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator()
                        Text("Loading...")
                    }
                    is UiState.Success -> {
                        val data = (uiState as UiState.Success).data
                        Text("Success: $data", color = Color.Green, fontWeight = FontWeight.Bold)
                    }
                    is UiState.Error -> {
                        val message = (uiState as UiState.Error).message
                        Text("Error: $message", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                    // No else needed!
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = {
                    uiState = UiState.Loading
                    Timber.d("State changed to: Loading")
                }) {
                    Text("Set Loading")
                }

                Button(onClick = {
                    uiState = UiState.Success("Data loaded successfully!")
                    Timber.d("State changed to: Success")
                }) {
                    Text("Set Success")
                }

                Button(onClick = {
                    uiState = UiState.Error("Network error occurred")
                    Timber.d("State changed to: Error")
                }) {
                    Text("Set Error")
                }
            }

            // Example 3: Smart Casting
            ExampleCard(
                title = "3. Smart Casting",
                description = "Automatic type casting in when branches"
            ) {
                Text(
                    """
                    when (state) {
                        is UiState.Success -> {
                            // state is smart cast to Success
                            println(state.data)
                        }
                    }
                    """.trimIndent(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                Text("\nNo manual casting needed!", fontSize = 12.sp)
            }

            // Example 4: Navigation Events
            ExampleCard(
                title = "4. Navigation Events",
                description = "Another common use case"
            ) {
                Text("sealed class NavigationEvent", fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    navEvent = NavigationEvent.Back
                    Timber.d("Navigation: Back")
                }) {
                    Text("Navigate Back")
                }

                Button(onClick = {
                    navEvent = NavigationEvent.ToDetails("123")
                    Timber.d("Navigation: ToDetails(123)")
                }) {
                    Text("Navigate to Details")
                }

                Button(onClick = {
                    navEvent = NavigationEvent.ToSettings("profile")
                    Timber.d("Navigation: ToSettings(profile)")
                }) {
                    Text("Navigate to Settings")
                }

                navEvent?.let { event ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Last event: ${getEventDescription(event)}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Example 5: Sealed vs Enum
            ExampleCard(
                title = "5. Sealed Class vs Enum",
                description = "When to use what"
            ) {
                Column {
                    Text("Enum:", fontWeight = FontWeight.Bold)
                    Text("• Fixed instances")
                    Text("• Same structure")
                    Text("• Simple constants")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Sealed Class:", fontWeight = FontWeight.Bold)
                    Text("• Multiple instances")
                    Text("• Different data")
                    Text("• Complex states")
                }
            }

            // Console logs
            ExampleCard(
                title = "Console Logs",
                description = "Check Logcat for state changes"
            ) {
                Text("Watch Timber logs for:", fontWeight = FontWeight.Bold)
                Text("• State transitions")
                Text("• Navigation events")
                Text("• When expression evaluations")
            }
        }
    }
}

@Composable
private fun getEventDescription(event: NavigationEvent): String {
    return when (event) {
        is NavigationEvent.Back -> "Back"
        is NavigationEvent.ToDetails -> "Details(${event.id})"
        is NavigationEvent.ToSettings -> "Settings(${event.tab})"
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
