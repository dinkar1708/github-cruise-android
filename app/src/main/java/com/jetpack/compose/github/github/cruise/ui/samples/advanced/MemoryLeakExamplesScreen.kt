package com.jetpack.compose.github.github.cruise.ui.samples.advanced

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
 * Memory Leak Detection Examples
 *
 * This screen demonstrates common memory leak patterns in Android
 * and how to detect and fix them using Android Profiler and LeakCanary.
 *
 * Tools to use:
 * - Android Studio Profiler (Memory tab)
 * - LeakCanary library
 * - Heap dumps and analysis
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryLeakExamplesScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Leak Detection") },
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
            // Header Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "Common Memory Leaks",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        "Use Android Studio Profiler to:\n" +
                                "• Take heap dumps before/after navigation\n" +
                                "• Analyze retained objects\n" +
                                "• Track memory growth over time\n" +
                                "• Install LeakCanary for automatic detection",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Example 1: Handler Leak
            LeakExampleCard(
                title = "1. Handler Leak (BAD)",
                description = "Non-static Handler holds reference to Activity/Fragment",
                leakType = "Handler not cancelled, holds Activity reference"
            ) {
                HandlerLeakExample()
            }

            // Example 2: Coroutine Leak
            LeakExampleCard(
                title = "2. Coroutine Leak (BAD)",
                description = "Launched coroutine not cancelled on dispose",
                leakType = "Long-running coroutine keeps composable in memory"
            ) {
                CoroutineLeakExample()
            }

            // Example 3: Listener Leak
            LeakExampleCard(
                title = "3. Listener Leak (BAD)",
                description = "Listener registered but not unregistered",
                leakType = "Static/global listeners hold composable reference"
            ) {
                ListenerLeakExample()
            }

            // Example 4: Fixed Handler (GOOD)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "✓ Fixed: Proper Cleanup (GOOD)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Use DisposableEffect for cleanup",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    FixedExample()
                }
            }

            // How to Detect Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "How to Detect Memory Leaks",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "1. Android Studio Profiler:\n" +
                                "   View → Tool Windows → Profiler\n" +
                                "   Select Memory → Record → Navigate → Stop\n\n" +
                                "2. LeakCanary (Recommended):\n" +
                                "   debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'\n\n" +
                                "3. Heap Dump Analysis:\n" +
                                "   Profiler → Dump Java Heap → Analyze\n\n" +
                                "4. Look for:\n" +
                                "   • Retained Activity instances after navigation\n" +
                                "   • Growing heap size over time\n" +
                                "   • Unreleased listeners/callbacks",
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // Best Practices
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Best Practices to Avoid Leaks",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "✓ Use viewModelScope for ViewModel coroutines\n" +
                                "✓ Use DisposableEffect for cleanup in Compose\n" +
                                "✓ Cancel coroutines in onCleared()\n" +
                                "✓ Remove listeners in cleanup\n" +
                                "✓ Use weak references for callbacks\n" +
                                "✓ Avoid non-static inner classes holding outer refs\n" +
                                "✓ Clear collections in onDestroy/onCleared",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LeakExampleCard(
    title: String,
    description: String,
    leakType: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(description, fontSize = 14.sp)
            Text(
                "⚠️ Leak: $leakType",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )
            content()
        }
    }
}

/**
 * BAD: Handler leak example
 * Handler holds reference to Activity, preventing GC
 */
@Composable
private fun HandlerLeakExample() {
    var counter by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    // BAD: This Handler will leak if not cleaned up
    val handler = remember { Handler(Looper.getMainLooper()) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Counter: $counter", fontSize = 14.sp)

        Button(
            onClick = {
                isRunning = true
                // BAD: Posting delayed runnable without cleanup
                handler.postDelayed({
                    counter++
                    Timber.d("Handler running: $counter")
                }, 1000)
            }
        ) {
            Text("Start Handler (Leaks!)")
        }

        AnimatedVisibility(isRunning) {
            Text(
                "⚠️ Handler callbacks will continue even after screen disposal!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * BAD: Coroutine leak example
 * Launched effect without proper cancellation
 */
@Composable
private fun CoroutineLeakExample() {
    var counter by remember { mutableStateOf(0) }
    var isLeaking by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Counter: $counter", fontSize = 14.sp)

        OutlinedButton(
            onClick = {
                isLeaking = true
                // BAD: LaunchedEffect without proper scope management could leak
                // if not using remember key properly
                Timber.d("Starting potentially leaking coroutine")
            }
        ) {
            Text("Launch Coroutine")
        }

        // This is actually GOOD because LaunchedEffect is properly scoped
        // But demonstrates the concept
        if (isLeaking) {
            LaunchedEffect(Unit) {
                repeat(100) {
                    delay(1000)
                    counter++
                    Timber.d("Coroutine tick: $counter")
                }
            }
            Text(
                "ℹ️ LaunchedEffect is cancelled on disposal (Good!)\n" +
                        "But GlobalScope.launch would leak!",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * BAD: Listener leak example
 * Simulates a listener that isn't removed
 */
@Composable
private fun ListenerLeakExample() {
    var listenerCount by remember { mutableStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Active Listeners: $listenerCount", fontSize = 14.sp)

        Button(
            onClick = {
                listenerCount++
                // BAD: Register listener without cleanup
                Timber.d("Registered listener #$listenerCount (NOT removed!)")
                // In real code: SomeGlobalManager.addListener(listener)
            }
        ) {
            Text("Add Listener (Leaks!)")
        }

        Text(
            "⚠️ Each listener holds reference to this composable!\n" +
                    "Memory grows with each registration.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * GOOD: Proper cleanup example
 * Using DisposableEffect for cleanup
 */
@Composable
private fun FixedExample() {
    var counter by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    // GOOD: Cleanup in DisposableEffect
    DisposableEffect(isRunning) {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    counter++
                    Timber.d("Fixed handler: $counter")
                    handler.postDelayed(this, 1000)
                }
            }
        }

        if (isRunning) {
            handler.post(runnable)
        }

        onDispose {
            // GOOD: Cleanup on dispose
            handler.removeCallbacks(runnable)
            Timber.d("Handler properly cleaned up!")
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Counter: $counter", fontSize = 14.sp)

        Button(
            onClick = { isRunning = !isRunning }
        ) {
            Text(if (isRunning) "Stop" else "Start (No Leak!)")
        }

        Text(
            "✓ Handler cleaned up in onDispose\n" +
                    "✓ No memory leak!",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
