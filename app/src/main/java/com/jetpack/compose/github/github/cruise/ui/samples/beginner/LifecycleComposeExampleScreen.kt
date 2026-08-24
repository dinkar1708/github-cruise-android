package com.jetpack.compose.github.github.cruise.ui.samples.beginner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
 * Interactive Jetpack Compose Lifecycle Example Screen
 *
 * Demonstrates:
 * 1. The 3 Phases of Composition: Enter ➔ Recompose ➔ Leave (Disposal)
 * 2. remember vs rememberSaveable across composition & config changes
 * 3. DisposableEffect lifecycle cleanup (onDispose)
 * 4. SideEffect execution on every successful recomposition
 * 5. LifecycleEventObserver bridge between Activity OS events and Compose
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifecycleComposeExampleScreen(
    onBackClick: () -> Unit
) {
    var isChildVisible by remember { mutableStateOf(true) }
    var parentRecomposeTrigger by remember { mutableIntStateOf(0) }
    val logs = remember { mutableStateListOf<String>() }

    fun addLog(msg: String) {
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        logs.add(0, "[$time] $msg")
    }

    // Observe Activity/Host Lifecycle transitions
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            addLog("🏛️ Host Lifecycle Event: ${event.name}")
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compose Lifecycle & Phases") },
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
            // Header Explanation Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "The 3 Phases of Composable Lifecycle",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "1. Enter Composition (First render / Init)\n" +
                                    "2. Recomposition (0 or more times on state change)\n" +
                                    "3. Leave Composition (Disposed / onDispose called)",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Controls Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Lifecycle Controls", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { isChildVisible = !isChildVisible },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isChildVisible) "Remove from UI" else "Add to UI")
                            }

                            OutlinedButton(
                                onClick = { parentRecomposeTrigger++ },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Recompose Parent ($parentRecomposeTrigger)")
                            }
                        }

                        Button(
                            onClick = { logs.clear() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear Lifecycle Logs")
                        }
                    }
                }
            }

            // Live Lifecycle Observable Child Component
            item {
                if (isChildVisible) {
                    LifecycleTrackedChildCard(
                        onLog = { addLog(it) }
                    )
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "🛑 Child Composable has LEFT the composition!\n(All internal remembered state was reset)",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Real-time Event Logs Section
            item {
                Text(
                    "Real-Time Lifecycle Event Log (${logs.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            items(logs) { log ->
                Text(
                    text = log,
                    fontSize = 12.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
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

/**
 * Child composable to demonstrate Enter, Recompose, and Leave lifecycle
 */
@Composable
private fun LifecycleTrackedChildCard(
    onLog: (String) -> Unit
) {
    // 1. remember: Lost when leaving composition and on rotation
    var rememberedCount by remember { mutableIntStateOf(0) }

    // 2. rememberSaveable: Lost when leaving composition, BUT survives screen rotation
    var saveableCount by rememberSaveable { mutableIntStateOf(0) }

    var recompositionCount by remember { mutableIntStateOf(0) }

    // 3. SideEffect: Runs on EVERY successful recomposition
    SideEffect {
        recompositionCount++
        onLog("⚡ [SideEffect] Child recomposed (Total: $recompositionCount times)")
    }

    // 4. DisposableEffect: Runs when entering composition & cleans up when leaving
    DisposableEffect(Unit) {
        onLog("🟢 [DisposableEffect] Child ENTERED Composition (Initial Render)")

        onDispose {
            onLog("🔴 [DisposableEffect.onDispose] Child LEFT Composition (Disposed!)")
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "🟢 Active Child Composable",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                "• Recomposition Count: $recompositionCount\n" +
                        "• remember counter: $rememberedCount (resets on rotation)\n" +
                        "• rememberSaveable counter: $saveableCount (survives rotation)",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        rememberedCount++
                        saveableCount++
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Increment States")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LifecycleComposeExampleScreenPreview() {
    GithubCruiseTheme {
        LifecycleComposeExampleScreen(onBackClick = {})
    }
}
