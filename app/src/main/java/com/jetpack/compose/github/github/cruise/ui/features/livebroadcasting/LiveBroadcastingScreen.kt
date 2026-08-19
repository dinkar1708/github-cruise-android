package com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.model.BroadcastStatus
import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.model.NetworkQuality
import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.state.DEFAULT_RTMP_PRESETS
import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.state.LiveBroadcastingState
import timber.log.Timber

/**
 * RTMP/RTMPS Live Broadcasting Screen
 * Demonstrates Camera capture (CameraX), MediaCodec H.264/AAC encoding, and Adaptive Bitrate (ABR)
 */
@OptIn(androidx.camera.core.ExperimentalGetImage::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveBroadcastingScreen(
    onBackClick: () -> Unit,
    viewModel: LiveBroadcastingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Permission handling for Camera and Audio recording
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RTMP/S Live Broadcasting") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            // 1. RTMP Ingest & Destination Provider Configuration
            RtmpConfigurationCard(uiState, viewModel)

            // 2. Camera Viewfinder & Live Status Overlay (Powered by CameraX)
            CameraViewfinderCard(uiState, viewModel, hasCameraPermission)

            // 3. Real-Time Telemetry & ABR Metrics
            StreamTelemetryCard(uiState)

            // 4. Adaptive Bitrate (ABR) Strategy Controls
            AdaptiveBitrateControlCard(uiState, viewModel)

            // 5. Network Condition Simulation (Test Dynamic Adaptation)
            NetworkSimulationCard(viewModel)

            // 6. Hardware Pipeline Specifications Card
            PipelineSpecsCard(uiState)

            // 7. Diagnostics & Event Logs
            DiagnosticsConsoleCard(uiState)
        }
    }
}

/**
 * RTMP Ingest Configuration Card (YouTube, Cloud RTMP, Twitch, Local)
 */
@Composable
private fun RtmpConfigurationCard(
    uiState: LiveBroadcastingState,
    viewModel: LiveBroadcastingViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("📡 RTMP Destination & Stream Key", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "Select a provider preset or paste your custom live stream key:",
                color = Color.Gray,
                fontSize = 12.sp
            )

            // Preset Provider Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DEFAULT_RTMP_PRESETS.forEachIndexed { index, preset ->
                    val isSelected = uiState.selectedPresetIndex == index
                    Surface(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = uiState.status != BroadcastStatus.LIVE) {
                                viewModel.selectPreset(index)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = preset.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Editable RTMP URL / Stream Key Field
            OutlinedTextField(
                value = uiState.rtmpEndpointUrl,
                onValueChange = { viewModel.updateRtmpUrl(it) },
                label = { Text("RTMP Ingest URL + Key") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.status != BroadcastStatus.LIVE,
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

/**
 * Camera Viewfinder Card with Live CameraX Stream Preview
 */
@Composable
private fun CameraViewfinderCard(
    uiState: LiveBroadcastingState,
    viewModel: LiveBroadcastingViewModel,
    hasCameraPermission: Boolean
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasCameraPermission) {
                // Live Hardware CameraX Preview Feed
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val cameraSelector = if (uiState.isFrontCamera) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                }
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                            } catch (e: Exception) {
                                Timber.e(e, "CameraX binding exception: ${e.message}")
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    update = { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val cameraSelector = if (uiState.isFrontCamera) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                }
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                            } catch (e: Exception) {
                                Timber.e(e, "CameraX update exception: ${e.message}")
                            }
                        }, ContextCompat.getMainExecutor(previewView.context))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Fallback Placeholder when Camera permission is not yet granted
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("📷 Camera Permission Required", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Grant camera access to enable the live hardware viewfinder.", color = Color.Gray, fontSize = 12.sp)
                }
            }

            // Top Status Badges (LIVE Indicator + Duration)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val liveBadgeColor by animateColorAsState(
                    targetValue = when (uiState.status) {
                        BroadcastStatus.LIVE -> Color(0xFFE53935)
                        BroadcastStatus.CONNECTING -> Color(0xFFFFA000)
                        else -> Color.Black.copy(alpha = 0.6f)
                    },
                    label = "live_color"
                )

                Surface(
                    color = liveBadgeColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.status.name,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (uiState.status == BroadcastStatus.LIVE) {
                    val duration = uiState.telemetry.broadcastDurationSeconds
                    val mins = duration / 60
                    val secs = duration % 60
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = String.format("⏱ %02d:%02d", mins, secs),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Bottom Floating Controls (Go Live, Camera Flip, Mute)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (uiState.status == BroadcastStatus.LIVE) viewModel.stopBroadcast()
                        else viewModel.startBroadcast()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.status == BroadcastStatus.LIVE) Color(0xFFE53935) else Color(0xFF43A047)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.status == BroadcastStatus.LIVE) Icons.Default.Refresh else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.status == BroadcastStatus.LIVE) "End Live" else "Start Broadcast",
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.switchCamera() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Flip 🔄")
                }

                OutlinedButton(
                    onClick = { viewModel.toggleAudioMute() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (uiState.isAudioMuted) Color(0xFFFF5252) else Color.White
                    )
                ) {
                    Text(if (uiState.isAudioMuted) "Unmute 🎤" else "Mute 🔇")
                }
            }
        }
    }
}

@Composable
private fun StreamTelemetryCard(uiState: LiveBroadcastingState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📊 Live Telemetry & Socket Metrics", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                val qualityColor = when (uiState.telemetry.networkQuality) {
                    NetworkQuality.EXCELLENT -> Color(0xFF43A047)
                    NetworkQuality.GOOD -> Color(0xFF7CB342)
                    NetworkQuality.FAIR -> Color(0xFFFB8C00)
                    NetworkQuality.POOR -> Color(0xFFE53935)
                    NetworkQuality.CRITICAL -> Color(0xFFB71C1C)
                }

                Surface(
                    color = qualityColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Network: ${uiState.telemetry.networkQuality.name}",
                        color = qualityColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryMetricItem("Current Bitrate", "${uiState.telemetry.currentBitrateKbps} kbps")
                TelemetryMetricItem("Frame Rate", "%.1f fps".format(uiState.telemetry.currentFps))
                TelemetryMetricItem("Socket Queue", "${uiState.telemetry.socketBufferQueueDepth} frames")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val totalMb = "%.2f MB".format(uiState.telemetry.totalBytesSent / (1024.0 * 1024.0))
                TelemetryMetricItem("Data Uploaded", totalMb)
                TelemetryMetricItem("Dropped Frames", "${uiState.telemetry.droppedFrames}")
                TelemetryMetricItem("Encoder Target", "${uiState.videoConfig.targetBitrateKbps} kbps")
            }
        }
    }
}

@Composable
private fun TelemetryMetricItem(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun AdaptiveBitrateControlCard(
    uiState: LiveBroadcastingState,
    viewModel: LiveBroadcastingViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                Column {
                    Text("⚡ Adaptive Bitrate (ABR)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Dynamically lowers bitrate when socket queue builds up", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = uiState.isAutoAdaptiveBitrateEnabled,
                    onCheckedChange = { viewModel.toggleAutoAdaptiveBitrate(it) }
                )
            }

            AnimatedVisibility(visible = !uiState.isAutoAdaptiveBitrateEnabled) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Manual Bitrate: ${uiState.telemetry.currentBitrateKbps} kbps", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Slider(
                        value = uiState.telemetry.currentBitrateKbps.toFloat(),
                        onValueChange = { viewModel.setManualBitrate(it.toInt()) },
                        valueRange = 800f..4500f,
                        steps = 7
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkSimulationCard(viewModel: LiveBroadcastingViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("🧪 Simulate Variable Network Conditions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Test how the ABR algorithm dynamically prevents broadcast disconnections:", fontSize = 12.sp, color = Color.Gray)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.simulateNetworkCondition("Normal (5G)", 0, 0, NetworkQuality.EXCELLENT) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("5G Fast", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.simulateNetworkCondition("Congested 4G", 4, 2, NetworkQuality.FAIR) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("4G Jitter", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.simulateNetworkCondition("Tunnel / Subway", 12, 10, NetworkQuality.CRITICAL) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tunnel 📉", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun PipelineSpecsCard(uiState: LiveBroadcastingState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("🛠 Hardware Pipeline Specifications", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("• Video: ${uiState.videoConfig.codec} @ ${uiState.videoConfig.width}x${uiState.videoConfig.height} (30 FPS, Keyframe interval: ${uiState.videoConfig.keyframeIntervalSeconds}s)", fontSize = 12.sp)
            Text("• Audio: ${uiState.audioConfig.codec} @ ${uiState.audioConfig.sampleRate / 1000}kHz (${uiState.audioConfig.bitrateKbps} kbps AAC-LC)", fontSize = 12.sp)
            Text("• Protocol: RTMPS over TLS (Port 443 with TLS 1.3)", fontSize = 12.sp)
        }
    }
}

@Composable
private fun DiagnosticsConsoleCard(uiState: LiveBroadcastingState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("💻 Hardware & Socket Console Logs", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 13.sp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                uiState.logMessages.forEach { log ->
                    Text(
                        text = log,
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
