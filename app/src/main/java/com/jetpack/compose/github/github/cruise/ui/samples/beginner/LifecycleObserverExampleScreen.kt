package com.jetpack.compose.github.github.cruise.ui.samples.beginner

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Interactive Sample Screen demonstrating LifecycleEventObserver inside a Composable
 *
 * Companion Documentation: `docs/technical/FAQ/beginner/lifecycle_observer_component.md`
 *
 * Concepts Demonstrated:
 * 1. Using `LocalLifecycleOwner.current` to observe host screen lifecycle events.
 * 2. Registering hardware listeners (Accelerometer / Sensor) on `ON_RESUME`.
 * 3. Unregistering hardware listeners on `ON_PAUSE` to protect battery life.
 * 4. Cleaning up observers and listeners in `DisposableEffect.onDispose` to prevent memory leaks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifecycleObserverExampleScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Real-time state
    var currentLifecycleState by remember { mutableStateOf("INITIALIZING") }
    var isSensorActive by remember { mutableStateOf(false) }
    var sensorX by remember { mutableStateOf(0f) }
    var sensorY by remember { mutableStateOf(0f) }
    var sensorZ by remember { mutableStateOf(9.8f) }
    val eventLogs = remember { mutableStateListOf<String>() }

    fun addLog(event: String) {
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        eventLogs.add(0, "[$time] $event")
        if (eventLogs.size > 20) eventLogs.removeLast()
    }

    // -------------------------------------------------------------------------
    // 🔌 LIFECYCLE EVENT OBSERVER:
    // Safely binds sensor lifecycle to this Composable component!
    // -------------------------------------------------------------------------
    DisposableEffect(lifecycleOwner) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.values.size >= 3) {
                    sensorX = event.values[0]
                    sensorY = event.values[1]
                    sensorZ = event.values[2]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        val observer = LifecycleEventObserver { _, event ->
            currentLifecycleState = event.name
            addLog("Event: ${event.name}")
            Timber.d("🏛️ Composable Lifecycle: ${event.name}")

            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // 🟢 Screen is active: Turn sensor ON!
                    if (accelerometer != null && sensorManager != null) {
                        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
                        isSensorActive = true
                        addLog("⚡ Sensor REGISTERED (SensorManager.registerListener)")
                    } else {
                        isSensorActive = true
                        addLog("⚡ Simulated Sensor ACTIVE (No physical accelerometer)")
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    // 🛑 User minimized app / locked phone: Turn sensor OFF to save battery!
                    sensorManager?.unregisterListener(sensorListener)
                    isSensorActive = false
                    addLog("⏸️ Sensor UNREGISTERED (Protected battery life)")
                }

                else -> Unit
            }
        }

        // 1. Attach observer to this Composable's lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)
        addLog("LifecycleObserver attached to LocalLifecycleOwner")

        // 2. 🧹 Mandatory Cleanup: Remove observer and unregister sensor when leaving screen
        onDispose {
            Timber.d("🧹 onDispose: Cleaning up LifecycleObserver & SensorListener")
            lifecycleOwner.lifecycle.removeObserver(observer)
            sensorManager?.unregisterListener(sensorListener)
            isSensorActive = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lifecycle Observer Sample") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { eventLogs.clear() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Clear logs")
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
            // Header Info Card
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
                                text = "Component-Level Lifecycle Safety",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This component uses `LocalLifecycleOwner.current` and `DisposableEffect` to automatically pause hardware listeners when you press the Home button and resume them when returning.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Live Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Current Screen State",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        if (isSensorActive) Color(0xFF4CAF50) else Color(0xFFFF9800),
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentLifecycleState,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = if (isSensorActive) "🟢 Hardware Active" else "⏸️ Hardware Paused",
                                fontWeight = FontWeight.Medium,
                                color = if (isSensorActive) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Accelerometer Live Values:",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = String.format(Locale.US, "X: %.2f  |  Y: %.2f  |  Z: %.2f", sensorX, sensorY, sensorZ),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Instructions Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🧪 Try It Out:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "1. Press the Home button to minimize the app.\n2. Re-open the app from Recents.\n3. Notice how ON_PAUSE unregisters the sensor and ON_RESUME restarts it automatically without battery leaks!",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Real-Time Event Logs Header
            item {
                Text(
                    text = "Live Lifecycle Event Stream:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            // Event Logs List
            items(eventLogs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = log,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
