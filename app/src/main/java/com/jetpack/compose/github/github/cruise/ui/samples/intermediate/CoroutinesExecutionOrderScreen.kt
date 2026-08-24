package com.jetpack.compose.github.github.cruise.ui.samples.intermediate

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Interactive Screen for Tricky Coroutines Execution Order & Interview Questions:
 * Tests order of execution with `runBlocking`, `launch`, `async`, `await`, `join`, `coroutineScope`, and `Dispatchers`.
 *
 * Companion Documentation: `docs/technical/FAQ/intermediate/coroutines_execution_order_quiz.md`
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoroutinesExecutionOrderScreen(
    onBackClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coroutines Execution Order Quiz") },
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
            // Header Info Card with Memory Rules
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
                                text = "🧠 6 Golden Rules of Execution Order",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• 🚀 launch ➔ Fire & Forget (Does NOT wait; caller continues)", fontSize = 12.sp)
                        Text("• ⏳ await() ➔ Pause for Value (Waits for async result; 0ms if done)", fontSize = 12.sp)
                        Text("• 🛑 coroutineScope { } ➔ Parent Gate (Waits for ALL children before next block)", fontSize = 12.sp)
                        Text("• 🧱 runBlocking { } ➔ Thread Freeze (Blocks thread until all children finish)", fontSize = 12.sp)
                        Text("• 🚧 job.join() ➔ Specific Barrier (Waits for 1 single job to finish)", fontSize = 12.sp)
                        Text("• ⚡ Unconfined ➔ Instant Start (Runs immediately until first suspension)", fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "⚠️ VERY IMPORTANT NOTE:\n• async starts work in background (caller NEVER waits; 'Start' prints at T=0ms).\n• await() ONLY pauses when explicitly called (0ms wait if already finished earlier).",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // PUZZLE 1: launch vs Sequential Flow
            // =========================================================================
            item {
                QuizCard(
                    title = "Puzzle 1: launch & Non-Blocking Delay",
                    difficulty = "Junior / Mid",
                    codeSnippet = """
println("1")
launch {
    delay(100L)
    println("2")
}
println("3")
                    """.trimIndent(),
                    explanation = "1 prints first. `launch` schedules a background job. The main thread continues immediately and prints 3. After 100ms, the coroutine resumes and prints 2.\n👉 Expected: 1 ➔ 3 ➔ 2",
                    onRun = { log ->
                        log("1")
                        launch {
                            delay(100L)
                            log("2")
                        }
                        log("3")
                    }
                )
            }

            // =========================================================================
            // PUZZLE 2: runBlocking with Nested launch
            // =========================================================================
            item {
                QuizCard(
                    title = "Puzzle 2: runBlocking vs Nested launch",
                    difficulty = "Mid / Senior (Very Common)",
                    codeSnippet = """
println("A")
runBlocking {
    println("B")
    launch {
        println("C")
    }
    println("D")
}
println("E")
                    """.trimIndent(),
                    explanation = "`launch` does not suspend the current block; it queues a coroutine in runBlocking's event loop. 'D' prints before 'C'. runBlocking waits for all children before exiting, so 'E' prints last!\n👉 Expected: A ➔ B ➔ D ➔ C ➔ E",
                    onRun = { log ->
                        withContext(Dispatchers.Default) {
                            log("A")
                            runBlocking {
                                log("B")
                                launch {
                                    log("C")
                                }
                                log("D")
                            }
                            log("E")
                        }
                    }
                )
            }

            // =========================================================================
            // PUZZLE 3.1: Short Classic async vs await() Order
            // =========================================================================
            item {
                QuizCard(
                    title = "Puzzle 3.1: Short Classic async / await()",
                    difficulty = "Mid / Senior (Classic)",
                    codeSnippet = """
val d1 = async { delay(200L); "Result 1" }
val d2 = async { delay(100L); "Result 2" }
println("Start")
println(d1.await())
println(d2.await())
println("End")
                    """.trimIndent(),
                    explanation = "Both async tasks start in parallel. d2 finishes first (100ms), but we call `d1.await()` first (200ms). The caller waits 200ms, prints 'Result 1', and then 'Result 2' returns instantly with 0ms wait!\n👉 Expected: Start ➔ Result 1 ➔ Result 2 ➔ End",
                    onRun = { log ->
                        val d1 = async {
                            delay(200L)
                            "Result 1"
                        }
                        val d2 = async {
                            delay(100L)
                            "Result 2"
                        }
                        log("Start")
                        log(d1.await())
                        log(d2.await())
                        log("End")
                    }
                )
            }

            // =========================================================================
            // PUZZLE 3.2: Advanced Parallel async with Before/After Delay & Await Timing
            // =========================================================================
            item {
                QuizCard(
                    title = "Puzzle 3.2: Detailed async Before/After Delay & Timing",
                    difficulty = "Senior / Staff (Deep Timing)",
                    codeSnippet = """
println("1. Main Starts")
val d1 = async {
    println("2. Async1 Before Delay")
    delay(300L)
    println("4. Async1 After Delay")
    "Result 1"
}
val d2 = async {
    println("3. Async2 Before Delay")
    delay(100L)
    println("5. Async2 After Delay")
    "Result 2"
}
println("6. Before Await 1")
println("7. Got: " + d1.await())
println("8. Between Awaits")
println("9. Got: " + d2.await())
println("10. All Done")
                    """.trimIndent(),
                    explanation = """
⏱️ Exact Timeline:
• T=0ms: "1. Main Starts", "2. Async1 Before", "3. Async2 Before", "6. Before Await 1" print immediately.
• T=0ms: Main thread suspends at `d1.await()` waiting for 300ms total.
• T=100ms: Async2 finishes delay & prints "5. Async2 After Delay" (Result 2 is cached in memory).
• T=300ms: Async1 finishes delay & prints "4. Async1 After Delay".
• T=300ms: `d1.await()` unblocks! "7. Got: Result 1" prints.
• T=300ms: "8. Between Awaits" prints.
• T=300ms: `d2.await()` returns INSTANTLY (0ms wait) because it already completed at T=100ms! Prints "9. Got: Result 2".
• T=300ms: "10. All Done" prints.

👉 Output Sequence:
1. Main Starts ➔ 2. Async1 Before ➔ 3. Async2 Before ➔ 6. Before Await 1 ➔ 5. Async2 After (100ms) ➔ 4. Async1 After (300ms) ➔ 7. Got: Result 1 ➔ 8. Between Awaits ➔ 9. Got: Result 2 ➔ 10. All Done
                    """.trimIndent(),
                    onRun = { log ->
                        log("1. Main Starts")
                        val d1 = async {
                            log("2. Async1 Before Delay")
                            delay(300L)
                            log("4. Async1 After Delay")
                            "Result 1"
                        }
                        val d2 = async {
                            log("3. Async2 Before Delay")
                            delay(100L)
                            log("5. Async2 After Delay")
                            "Result 2"
                        }
                        log("6. Before Await 1")
                        val r1 = d1.await()
                        log("7. Got: $r1")
                        log("8. Between Awaits")
                        val r2 = d2.await()
                        log("9. Got: $r2")
                        log("10. All Done")
                    }
                )
            }

            // =========================================================================
            // PUZZLE 4: Structured Concurrency with coroutineScope { }
            // =========================================================================
            item {
                QuizCard(
                    title = "Puzzle 4: coroutineScope Suspension Boundary",
                    difficulty = "Senior / Staff",
                    codeSnippet = """
println("Start")
coroutineScope {
    launch {
        delay(200L)
        println("Inside Launch")
    }
    println("Inside Scope Body")
}
println("End")
                    """.trimIndent(),
                    explanation = "`coroutineScope` creates a structured boundary and SUSPENDS until ALL internal children complete! Therefore, 'End' CANNOT print until 'Inside Launch' finishes!\n(In contrast, separate `viewModelScope.launch` calls without `coroutineScope` are fire-and-forget and do NOT wait!)\n👉 Expected: Start ➔ Inside Scope Body ➔ Inside Launch ➔ End",
                    onRun = { log ->
                        log("Start")
                        coroutineScope {
                            launch {
                                delay(200L)
                                log("Inside Launch")
                            }
                            log("Inside Scope Body")
                        }
                        log("End")
                    }
                )
            }

            // =========================================================================
            // PUZZLE 5: job.join() Synchronous Barrier
            // =========================================================================
            item {
                QuizCard(
                    title = "Puzzle 5: job.join() vs launch",
                    difficulty = "Mid",
                    codeSnippet = """
println("Alpha")
val job = launch {
    delay(150L)
    println("Beta")
}
println("Gamma")
job.join()
println("Delta")
                    """.trimIndent(),
                    explanation = "'Alpha' prints, job starts in background, 'Gamma' prints. Then `job.join()` suspends until the job completes. 'Beta' prints, and finally 'Delta' prints!\n👉 Expected: Alpha ➔ Gamma ➔ Beta ➔ Delta",
                    onRun = { log ->
                        log("Alpha")
                        val job = launch {
                            delay(150L)
                            log("Beta")
                        }
                        log("Gamma")
                        job.join()
                        log("Delta")
                    }
                )
            }

            // =========================================================================
            // PUZZLE 6: Dispatchers.Unconfined
            // =========================================================================
            item {
                QuizCard(
                    title = "Puzzle 6: Dispatchers.Unconfined Trick",
                    difficulty = "Staff Level",
                    codeSnippet = """
launch(Dispatchers.Unconfined) {
    println("Unconfined 1")
    delay(100L)
    println("Unconfined 2")
}
println("Caller after launch")
                    """.trimIndent(),
                    explanation = "`Dispatchers.Unconfined` starts executing IMMEDIATELY on the caller's current thread until the first suspension point (`delay`), then yields to caller!\n👉 Expected: Unconfined 1 ➔ Caller after launch ➔ Unconfined 2",
                    onRun = { log ->
                        launch(Dispatchers.Unconfined) {
                            log("Unconfined 1")
                            delay(100L)
                            log("Unconfined 2")
                        }
                        log("Caller after launch")
                    }
                )
            }
        }
    }
}

@Composable
private fun QuizCard(
    title: String,
    difficulty: String,
    codeSnippet: String,
    explanation: String,
    onRun: suspend CoroutineScope.((String) -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val logs = remember { mutableStateListOf<String>() }
    var isRunning by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title & Difficulty Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(difficulty, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Code Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = codeSnippet,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFFD4D4D4),
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        logs.clear()
                        isRunning = true
                        coroutineScope.launch {
                            val startTime = System.currentTimeMillis()
                            onRun { msg ->
                                val elapsed = System.currentTimeMillis() - startTime
                                val formatted = "[+${elapsed}ms] $msg"
                                logs.add(formatted)
                                Timber.d("[$title] $formatted")
                            }
                            delay(400L) // Buffer to ensure trailing coroutines settle
                            isRunning = false
                        }
                    },
                    enabled = !isRunning,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Running...")
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Run Quiz")
                    }
                }

                OutlinedButton(
                    onClick = { showExplanation = !showExplanation },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (showExplanation) "Hide Answer" else "💡 Answer")
                }
            }

            // Live Print Execution Output
            if (logs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Actual Output Sequence:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2D2D2D), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    logs.forEachIndexed { index, log ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${index + 1}.",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50),
                                fontSize = 12.sp,
                                modifier = Modifier.width(20.dp)
                            )
                            Text(
                                text = log,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = Color(0xFFE0E0E0)
                            )
                        }
                    }
                }
            }

            // Explanation Box
            AnimatedVisibility(visible = showExplanation) {
                Column(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text("💡 Interview Explanation:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(explanation, style = MaterialTheme.typography.bodySmall, lineHeight = 16.sp)
                }
            }
        }
    }
}
