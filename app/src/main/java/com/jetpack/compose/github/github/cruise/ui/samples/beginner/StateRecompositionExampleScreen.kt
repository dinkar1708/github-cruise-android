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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import timber.log.Timber

/**
 * State & Recomposition Example Screen
 * Demonstrates Compose state management and recomposition
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateRecompositionExampleScreen(
    onBackClick: () -> Unit
) {
    Timber.d("StateRecompositionExampleScreen composed")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("State & Recomposition") },
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
            // Example 1: remember
            RememberExample()

            // Example 2: State Hoisting
            StateHoistingExample()

            // Example 3: remember vs rememberSaveable
            RememberVsSaveableExample()

            // Example 4: Recomposition Scope
            RecompositionScopeExample()

            // Console logs
            ConsoleLogsCard()
        }
    }
}

@Composable
private fun RememberExample() {
    var count by remember { mutableStateOf(0) }
    Timber.d("RememberExample recomposed with count: $count")

    ExampleCard(
        title = "1. remember",
        description = "Preserve state across recompositions"
    ) {
        Text("var count by remember { mutableStateOf(0) }", fontWeight = FontWeight.Bold)
        Text("Count: $count", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    count++
                    Timber.d("Count incremented to: $count")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Increment")
            }
            Button(
                onClick = {
                    count = 0
                    Timber.d("Count reset to 0")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset")
            }
        }
        Text("State survives recomposition", fontSize = 12.sp)
    }
}

@Composable
private fun StateHoistingExample() {
    var hoistedCount by remember { mutableStateOf(0) }

    ExampleCard(
        title = "2. State Hoisting",
        description = "Move state up to make composables reusable"
    ) {
        Text("State is hoisted to parent", fontWeight = FontWeight.Bold)
        Text("Child composable is stateless and reusable", fontSize = 12.sp)

        Spacer(modifier = Modifier.height(8.dp))

        // Stateless child
        StatelessCounter(
            count = hoistedCount,
            onIncrement = {
                hoistedCount++
                Timber.d("Hoisted count incremented: $hoistedCount")
            },
            onDecrement = {
                hoistedCount--
                Timber.d("Hoisted count decremented: $hoistedCount")
            }
        )
    }
}

@Composable
private fun StatelessCounter(
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Timber.d("StatelessCounter recomposed with count: $count")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Stateless Component", fontWeight = FontWeight.Bold)
        Text("Count: $count", fontSize = 20.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onDecrement, modifier = Modifier.weight(1f)) {
                Text("-")
            }
            Button(onClick = onIncrement, modifier = Modifier.weight(1f)) {
                Text("+")
            }
        }
    }
}

@Composable
private fun RememberVsSaveableExample() {
    var rememberValue by remember { mutableStateOf("") }
    var saveableValue by rememberSaveable { mutableStateOf("") }

    ExampleCard(
        title = "3. remember vs rememberSaveable",
        description = "rememberSaveable survives configuration changes"
    ) {
        Text("remember - Lost on rotation", fontWeight = FontWeight.Bold)
        TextField(
            value = rememberValue,
            onValueChange = { rememberValue = it },
            label = { Text("remember") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("rememberSaveable - Survives rotation", fontWeight = FontWeight.Bold)
        TextField(
            value = saveableValue,
            onValueChange = { saveableValue = it },
            label = { Text("rememberSaveable") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Rotate screen to see difference", fontSize = 12.sp)
    }
}

@Composable
private fun RecompositionScopeExample() {
    var triggerCount by remember { mutableStateOf(0) }

    ExampleCard(
        title = "4. Recomposition Scope",
        description = "Only changed composables recompose"
    ) {
        Text("Static Text - Never recomposes", color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(8.dp))

        // This recomposes
        Text(
            "Dynamic: $triggerCount",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        ).also {
            Timber.d("Dynamic text recomposed with: $triggerCount")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            triggerCount++
            Timber.d("Trigger count: $triggerCount")
        }) {
            Text("Trigger Recomposition")
        }

        Text(
            "Only the dynamic text recomposes, not static text",
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ConsoleLogsCard() {
    ExampleCard(
        title = "Console Logs",
        description = "Check Logcat for recomposition logs"
    ) {
        Text("Watch Timber logs to see:", fontWeight = FontWeight.Bold)
        Text("• When composables recompose")
        Text("• Which composables are affected")
        Text("• State changes")
        Text("\nFilter by tag: StateRecompositionExampleScreen")
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
