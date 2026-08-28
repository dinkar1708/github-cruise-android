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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cruise.apm.CruiseApm
import com.cruise.apm.CruiseApmConfig
import com.cruise.apm.model.ApmEvent
import com.cruise.apm.model.ApmResult
import com.cruise.apm.model.NetworkMetric
import com.cruise.apm.model.SdkEnvironment
import com.cruise.apm.model.SystemVitals
import com.cruise.apm.trace.ApmTrace

/**
 * Interactive Demo and Live Architecture Showcase for CruiseAPM SDK (:cruise-apm-sdk).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CruiseApmExampleScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var selectedEnv by remember { mutableStateOf(SdkEnvironment.SANDBOX) }
    var activeTrace by remember { mutableStateOf<ApmTrace?>(null) }
    var traceNameInput by remember { mutableStateOf("fetch_github_repos") }
    var customEventName by remember { mutableStateOf("user_checkout_clicked") }
    var simulatedUrl by remember { mutableStateOf("https://api.github.com/users/octocat") }
    var simulatedMethod by remember { mutableStateOf("GET") }
    var simulatedStatus by remember { mutableIntStateOf(200) }
    var executionLog by remember { mutableStateOf("Ready. Select an action below to test CruiseAPM SDK.") }

    var liveVitals by remember {
        mutableStateOf(if (CruiseApm.isInitialized()) CruiseApm.getVitals() else null)
    }

    val recentEvents = remember { mutableStateListOf<ApmEvent>() }
    var pendingCount by remember { mutableIntStateOf(CruiseApm.getPendingEvents().size) }
    var persistedCount by remember { mutableIntStateOf(CruiseApm.getPersistedEventCount()) }

    // Listen for live APM events from SDK's SharedFlow
    LaunchedEffect(Unit) {
        if (CruiseApm.isInitialized()) {
            CruiseApm.getEventStream().collect { event ->
                recentEvents.add(0, event)
                if (recentEvents.size > 15) recentEvents.removeLast()
                pendingCount = CruiseApm.getPendingEvents().size
                persistedCount = CruiseApm.getPersistedEventCount()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CruiseAPM & Observability SDK") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Header Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🚀 CruiseAPM Mobile Observability SDK",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A production-ready Android library module (:cruise-apm-sdk) delivering OkHttp network metrics, monotonic trace timers, live RAM/battery vitals, ANR watchdog detection, and persistent offline spooling.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Live Diagnostics & System Vitals Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 Live System Vitals & Health",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        OutlinedButton(
                            onClick = {
                                liveVitals = CruiseApm.getVitals()
                                executionLog = "Refreshed live hardware vitals snapshot."
                            }
                        ) {
                            Text("Refresh", fontSize = 12.sp)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SDK Status:", fontSize = 14.sp)
                        Text(
                            text = if (CruiseApm.isInitialized()) "🟢 Active (v${CruiseApm.SDK_VERSION})" else "🔴 Not Initialized",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    liveVitals?.let { v ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Heap Usage:", fontSize = 14.sp)
                            Text("${v.usedHeapMb} MB / ${v.maxHeapMb} MB (${v.heapUtilizationPercent}%)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        LinearProgressIndicator(
                            progress = { (v.heapUtilizationPercent / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Battery & Power:", fontSize = 14.sp)
                            Text("${v.batteryLevelPercent}% (Charging: ${v.isCharging})", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Network & Transport:", fontSize = 14.sp)
                            Text("${v.networkType} (Metered: ${v.isNetworkMetered})", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Thermal / Active Threads:", fontSize = 14.sp)
                            Text("${v.thermalStatus} / ${v.activeThreadCount} threads", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // 1. Performance Traces Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "1. Performance Trace Timers (`ApmTrace`)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Measure custom execution durations with monotonic nanosecond precision.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = traceNameInput,
                        onValueChange = { traceNameInput = it },
                        label = { Text("Trace Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val trace = CruiseApm.newTrace(traceNameInput)
                                trace.start()
                                trace.putAttribute("environment", selectedEnv.name)
                                trace.putAttribute("caller", "CruiseApmExampleScreen")
                                activeTrace = trace
                                executionLog = "Started trace: '$traceNameInput'. Perform your task then click Stop Trace."
                            },
                            enabled = activeTrace == null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Start Trace")
                        }

                        Button(
                            onClick = {
                                activeTrace?.let { trace ->
                                    val durationMs = trace.stop()
                                    executionLog = "Stopped trace '${trace.name}': Duration = ${durationMs}ms (Dispatched to APM pipeline)"
                                    activeTrace = null
                                }
                            },
                            enabled = activeTrace != null,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Stop Trace")
                        }
                    }
                }
            }

            // 2. Network Observability Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "2. Network Observability (`CruiseApmOkHttpInterceptor`)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Simulate HTTP request/response metrics captured by OkHttp interceptor.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = simulatedUrl,
                        onValueChange = { simulatedUrl = it },
                        label = { Text("Endpoint URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("GET", "POST", "PUT", "DELETE").forEach { method ->
                            FilterChip(
                                selected = simulatedMethod == method,
                                onClick = { simulatedMethod = method },
                                label = { Text(method) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(200, 201, 404, 500).forEach { code ->
                            FilterChip(
                                selected = simulatedStatus == code,
                                onClick = { simulatedStatus = code },
                                label = { Text("HTTP $code") }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val simulatedDuration = (45..350).random().toLong()
                            val metric = NetworkMetric(
                                url = simulatedUrl,
                                httpMethod = simulatedMethod,
                                statusCode = simulatedStatus,
                                durationMs = simulatedDuration,
                                requestSizeBytes = if (simulatedMethod == "POST") 1024L else 0L,
                                responseSizeBytes = 4096L,
                                isSuccess = simulatedStatus in 200..299,
                                errorMessage = if (simulatedStatus >= 400) "HTTP Error $simulatedStatus" else null
                            )
                            CruiseApm.recordNetworkMetric(metric)
                            executionLog = "Recorded Network Metric: $simulatedMethod $simulatedUrl -> $simulatedStatus (${simulatedDuration}ms)"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Record Network Metric")
                    }
                }
            }

            // 3. Offline Spool & Event Queue Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "3. Offline Disk Spool & Batch Ingestion",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("In-Memory Queue: $pendingCount", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        Text("Persisted On-Disk: $persistedCount", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.tertiary)
                    }

                    OutlinedTextField(
                        value = customEventName,
                        onValueChange = { customEventName = it },
                        label = { Text("Custom Event Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                CruiseApm.logCustomEvent(
                                    name = customEventName,
                                    attributes = mapOf("screen" to "CruiseApmExampleScreen", "timestamp" to System.currentTimeMillis())
                                )
                                executionLog = "Enqueued custom event: '$customEventName'."
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Enqueue Event")
                        }

                        OutlinedButton(
                            onClick = {
                                CruiseApm.flushEvents { res ->
                                    executionLog = when (res) {
                                        is ApmResult.Success -> "Flushed ${res.data.size} events to offline file spool."
                                        is ApmResult.Failure -> "Flush error: ${res.message}"
                                    }
                                    pendingCount = CruiseApm.getPendingEvents().size
                                    persistedCount = CruiseApm.getPersistedEventCount()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Flush Batch")
                        }
                    }
                }
            }

            // Execution Output Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SDK Execution Output",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = executionLog,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Architecture Step-by-Step Guide
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🏗️ CruiseAPM Architectural Patterns",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = """
1. Dedicated Worker Dispatcher:
   Runs all SDK operations on a single daemon thread ("CruiseApm-Worker") to never starve host app coroutine dispatchers.

2. Non-Destructive Network Interceptor:
   Implements `okhttp3.Interceptor` to measure round-trip latency, response codes, and payload bytes without consuming streams.

3. Lock-Free Channel Ingestion:
   Uses Kotlin `Channel<ApmEvent>(Channel.BUFFERED)` with Actor pattern for safe concurrent event ingestion from any thread.

4. Offline-First Disk Spooling:
   Buffers telemetry in local JSON files during offline periods, batch-flushing on reconnect.

5. Main Looper ANR Watchdog:
   Heartbeat runnable on Main Looper detects UI thread freezes (>5000ms) and dumps thread traces safely.
                        """.trimIndent(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}
