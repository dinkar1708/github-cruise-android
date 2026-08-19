package com.jetpack.compose.github.github.cruise.ui.samples.intermediate

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * LaunchedEffect Example Screen
 * Demonstrates side effects in Jetpack Compose
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchedEffectExampleScreen(
    onBackClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LaunchedEffect Examples") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Example 1: Run Once
            RunOnceExample()

            // Example 2: Key Changes
            KeyChangesExample(snackbarHostState)

            // Example 3: Timer
            TimerExample()

            // Example 4: Search Debounce
            SearchDebounceExample()
        }
    }
}

@Composable
private fun RunOnceExample() {
    var counter by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        Timber.d("LaunchedEffect(Unit) - Runs only once")
    }

    ExampleCard(
        title = "1. Run Once - LaunchedEffect(Unit)",
        description = "Executes only when composable enters composition"
    ) {
        Text("Counter: $counter", fontSize = 20.sp)
        Button(onClick = { counter++ }) {
            Text("Increment (triggers recomposition)")
        }
        Text("LaunchedEffect runs once - check logs", fontSize = 12.sp)
    }
}

@Composable
private fun KeyChangesExample(snackbarHostState: SnackbarHostState) {
    var userId by remember { mutableStateOf("1") }

    LaunchedEffect(userId) {
        Timber.d("LaunchedEffect restarted for userId: $userId")
        delay(500)
        snackbarHostState.showSnackbar("Loaded data for user $userId")
    }

    ExampleCard(
        title = "2. Rerun on Key Change",
        description = "Relaunches when key changes"
    ) {
        Text("Current User ID: $userId", fontSize = 18.sp)

        Button(onClick = { userId = (userId.toInt() + 1).toString() }) {
            Text("Change User ID")
        }

        Text(
            "LaunchedEffect reruns on every userId change",
            fontSize = 12.sp
        )
    }
}

@Composable
private fun TimerExample() {
    var seconds by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            Timber.d("Timer started")
            while (isRunning) {
                delay(1000)
                seconds++
            }
        } else {
            Timber.d("Timer stopped")
        }
    }

    ExampleCard(
        title = "3. Timer Example",
        description = "Continuous coroutine while key is true"
    ) {
        Text("Elapsed: $seconds seconds", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Button(onClick = {
            isRunning = !isRunning
            if (!isRunning) seconds = 0
        }) {
            Text(if (isRunning) "Stop" else "Start")
        }
    }
}

@Composable
private fun SearchDebounceExample() {
    var query by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf("") }

    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            Timber.d("Debouncing search for: $query")
            delay(300) // Debounce
            Timber.d("Executing search for: $query")
            searchResult = "Results for: $query"
        } else {
            searchResult = ""
        }
    }

    ExampleCard(
        title = "4. Search with Debounce",
        description = "Wait before executing (prevents too many calls)"
    ) {
        TextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (searchResult.isNotEmpty()) {
            Text(searchResult, color = MaterialTheme.colorScheme.primary)
        }

        Text("300ms debounce - check logs", fontSize = 12.sp)
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
