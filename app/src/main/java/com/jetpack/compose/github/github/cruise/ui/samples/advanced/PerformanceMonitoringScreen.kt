package com.jetpack.compose.github.github.cruise.ui.samples.advanced

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.random.Random
import kotlin.system.measureTimeMillis

/**
 * Performance Monitoring Examples
 *
 * Demonstrates how to monitor and optimize performance in Jetpack Compose.
 *
 * Tools to use:
 * - Android Studio Profiler (CPU, Memory, Network)
 * - Compose Layout Inspector
 * - Recomposition highlighting in Layout Inspector
 * - Systrace/Perfetto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceMonitoringScreen(
    onBackClick: () -> Unit
) {
    var activeExample by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance Monitoring") },
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
            // Header
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
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Performance Monitoring",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Monitor and optimize Compose performance using:\n" +
                                "• Android Studio Profiler (View → Tool Windows → Profiler)\n" +
                                "• Layout Inspector for recomposition tracking\n" +
                                "• Systrace for frame timing\n" +
                                "• Baseline Profiles for startup optimization",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Example 1: Recomposition Counter
            if (activeExample == null || activeExample == "recomposition") {
                PerformanceExampleCard(
                    title = "1. Recomposition Counter",
                    description = "Track how many times composables recompose",
                    isActive = activeExample == "recomposition",
                    onToggle = {
                        activeExample = if (activeExample == "recomposition") null else "recomposition"
                    }
                ) {
                    RecompositionCounterExample()
                }
            }

            // Example 2: LazyColumn Performance
            if (activeExample == null || activeExample == "lazy") {
                PerformanceExampleCard(
                    title = "2. LazyColumn Performance",
                    description = "Measure list scrolling performance",
                    isActive = activeExample == "lazy",
                    onToggle = {
                        activeExample = if (activeExample == "lazy") null else "lazy"
                    }
                ) {
                    LazyColumnPerformanceExample()
                }
            }

            // Example 3: Expensive Calculations
            if (activeExample == null || activeExample == "expensive") {
                PerformanceExampleCard(
                    title = "3. Expensive Calculations",
                    description = "Compare remember vs recomputing on every frame",
                    isActive = activeExample == "expensive",
                    onToggle = {
                        activeExample = if (activeExample == "expensive") null else "expensive"
                    }
                ) {
                    ExpensiveCalculationExample()
                }
            }

            // Example 4: derivedStateOf Usage
            if (activeExample == null || activeExample == "derived") {
                PerformanceExampleCard(
                    title = "4. derivedStateOf Optimization",
                    description = "Reduce recompositions with derived state",
                    isActive = activeExample == "derived",
                    onToggle = {
                        activeExample = if (activeExample == "derived") null else "derived"
                    }
                ) {
                    DerivedStateExample()
                }
            }

            // Monitoring Tools Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Performance Monitoring Tools",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "1. Android Studio Profiler:\n" +
                                "   • CPU: Track expensive operations\n" +
                                "   • Memory: Monitor allocations\n" +
                                "   • Energy: Battery impact\n\n" +
                                "2. Layout Inspector:\n" +
                                "   • Enable 'Show Recomposition Counts'\n" +
                                "   • Highlight frequently recomposing areas\n\n" +
                                "3. Compose Metrics:\n" +
                                "   androidx.compose.compiler:compiler:1.5.0+\n" +
                                "   Add -P plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=...\n\n" +
                                "4. Baseline Profiles:\n" +
                                "   Optimize startup and runtime performance",
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
                        "Performance Best Practices",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "✓ Use remember for expensive calculations\n" +
                                "✓ Use derivedStateOf to reduce recompositions\n" +
                                "✓ Add keys to LazyColumn items\n" +
                                "✓ Use Modifier.graphicsLayer for animations\n" +
                                "✓ Avoid reading State in composition frequently\n" +
                                "✓ Use immutable/stable data classes\n" +
                                "✓ Measure performance in release builds\n" +
                                "✓ Enable R8/ProGuard optimization",
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PerformanceExampleCard(
    title: String,
    description: String,
    isActive: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(description, fontSize = 13.sp)
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = { onToggle() }
                )
            }

            if (isActive) {
                content()
            }
        }
    }
}

/**
 * Example 1: Recomposition Counter
 * Tracks how many times a composable recomposes
 */
@Composable
private fun RecompositionCounterExample() {
    var counter by remember { mutableStateOf(0) }
    var recomposeCount by remember { mutableStateOf(0) }

    // Track recompositions
    LaunchedEffect(Unit) {
        recomposeCount++
        Timber.d("RecompositionCounter recomposed: $recomposeCount times")
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Button clicks: $counter", fontSize = 14.sp)
        Text(
            "Recompositions: $recomposeCount",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Button(onClick = { counter++ }) {
            Text("Increment Counter")
        }

        Text(
            "ℹ️ Each click should cause only 1 recomposition.\n" +
                    "Check Layout Inspector to verify.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

/**
 * Example 2: LazyColumn Performance
 * Demonstrates proper LazyColumn usage with keys
 */
@Composable
private fun LazyColumnPerformanceExample() {
    var useKeys by remember { mutableStateOf(true) }
    val items = remember { mutableStateListOf<String>().apply {
        repeat(50) { add("Item $it") }
    } }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Use stable keys:", fontSize = 14.sp)
            Switch(checked = useKeys, onCheckedChange = { useKeys = it })
        }

        OutlinedButton(
            onClick = {
                items.shuffle()
                Timber.d("List shuffled - watch recompositions")
            }
        ) {
            Text("Shuffle List")
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
        ) {
            LazyColumn {
                if (useKeys) {
                    items(items, key = { it }) { item ->
                        ListItemWithCounter(item)
                    }
                } else {
                    items(items) { item ->
                        ListItemWithCounter(item)
                    }
                }
            }
        }

        Text(
            if (useKeys) {
                "✓ With keys: Only reorders, no recomposition"
            } else {
                "✗ Without keys: Full recomposition on shuffle"
            },
            fontSize = 12.sp,
            color = if (useKeys) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun ListItemWithCounter(item: String) {
    var renderCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        renderCount++
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(item, fontSize = 13.sp)
        Text("Renders: $renderCount", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
    }
}

/**
 * Example 3: Expensive Calculations
 */
@Composable
private fun ExpensiveCalculationExample() {
    var counter by remember { mutableStateOf(0) }
    var useRemember by remember { mutableStateOf(true) }
    var executionTime by remember { mutableStateOf(0L) }

    // Expensive calculation
    val result = if (useRemember) {
        remember(counter) {
            val time = measureTimeMillis {
                // Simulate expensive work
                (1..10000).fold(0) { acc, i -> acc + i }
            }
            executionTime = time
            Timber.d("Expensive calculation with remember: ${time}ms")
            counter * 100
        }
    } else {
        val time = measureTimeMillis {
            (1..10000).fold(0) { acc, i -> acc + i }
        }
        executionTime = time
        Timber.d("Expensive calculation WITHOUT remember: ${time}ms")
        counter * 100
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Use remember:", fontSize = 14.sp)
            Switch(checked = useRemember, onCheckedChange = { useRemember = it })
        }

        Text("Counter: $counter", fontSize = 14.sp)
        Text("Result: $result", fontSize = 14.sp)
        Text(
            "Execution time: ${executionTime}ms",
            fontSize = 14.sp,
            color = if (executionTime < 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )

        Button(onClick = { counter++ }) {
            Text("Increment")
        }

        Text(
            if (useRemember) {
                "✓ Cached: Only recalculates when counter changes"
            } else {
                "✗ Not cached: Recalculates on every recomposition!"
            },
            fontSize = 12.sp,
            color = if (useRemember) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

/**
 * Example 4: derivedStateOf
 */
@Composable
private fun DerivedStateExample() {
    var scrollPosition by remember { mutableStateOf(0) }
    var recomposeCount by remember { mutableStateOf(0) }

    // Without derivedStateOf - recomposes on every scroll change
    val isScrolledBad = scrollPosition > 50

    // With derivedStateOf - only recomposes when the derived value changes
    val isScrolledGood by remember {
        derivedStateOf {
            scrollPosition > 50
        }
    }

    LaunchedEffect(isScrolledGood) {
        recomposeCount++
        Timber.d("Recomposed due to isScrolled change: $recomposeCount")
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Scroll Position: $scrollPosition", fontSize = 14.sp)
        Text("Is Scrolled: $isScrolledGood", fontSize = 14.sp)
        Text(
            "Recompose count: $recomposeCount",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Button(onClick = { scrollPosition += 10 }) {
            Text("Scroll +10")
        }

        Button(onClick = { scrollPosition = 0 }) {
            Text("Reset")
        }

        Text(
            "✓ derivedStateOf: Only recomposes when threshold crossed\n" +
                    "✗ Without: Recomposes on every position change",
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
