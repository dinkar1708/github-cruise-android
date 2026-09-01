package com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble.model.BleBeaconItem
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble.model.BleScannerUiState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * BLE (Bluetooth Low Energy) Beacon Scanner & Frame Decoder Screen
 *
 * Demonstrates:
 * 1. BluetoothLeScanner with ScanSettings (LOW_POWER, BALANCED, LOW_LATENCY)
 * 2. iBeacon, AltBeacon & Eddystone manufacturer data binary decoding
 * 3. RSSI signal strength meters & Path-Loss distance modeling
 * 4. Android 12+ / 14+ Bluetooth permission requirements
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleBeaconScannerScreen(
    onBackClick: () -> Unit,
    viewModel: BleBeaconScannerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Live Scanner", "Packet Decoder", "Permissions & Setup")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BLE Beacon Scanner") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Logs")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scanner Control Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (state.isScanning) "Scanner: ACTIVE" else "Scanner: IDLE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (state.isScanning) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Mode: ${state.scanMode} | Packets: ${state.totalPacketsCaptured}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = { viewModel.toggleScanner() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isScanning) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                        )
                    ) {
                        Text(if (state.isScanning) "Stop Scan" else "Start Scan", fontSize = 11.sp)
                    }
                }
            }

            // Tab Bar
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> BleLiveScannerTab(state, viewModel)
                    1 -> BlePacketDecoderTab(state, viewModel)
                    2 -> BlePermissionsTab()
                }
            }

            // Live Terminal Console Strip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("💻 BLE Scanner Live Logs", color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${state.terminalLogs.size} lines", color = Color.Gray, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF121212), RoundedCornerShape(4.dp))
                            .padding(6.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (state.terminalLogs.isEmpty()) {
                            Text("Start scanning above to inspect raw BLE advertisement packets & RSSI...", color = Color.DarkGray, fontSize = 10.sp)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                state.terminalLogs.forEach { log ->
                                    Text(log, color = Color(0xFFE0E0E0), fontFamily = FontFamily.Monospace, fontSize = 10.sp, lineHeight = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BleLiveScannerTab(
    state: BleScannerUiState,
    viewModel: BleBeaconScannerViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Scan Settings Filter Chips
        Text("⚙️ ScanSettings & Signal Smoothing:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
                selected = state.scanMode == "SCAN_MODE_LOW_POWER",
                onClick = { viewModel.setScanMode("SCAN_MODE_LOW_POWER") },
                label = { Text("Low Power", fontSize = 10.sp) }
            )
            FilterChip(
                selected = state.scanMode == "SCAN_MODE_BALANCED",
                onClick = { viewModel.setScanMode("SCAN_MODE_BALANCED") },
                label = { Text("Balanced", fontSize = 10.sp) }
            )
            FilterChip(
                selected = state.scanMode == "SCAN_MODE_LOW_LATENCY",
                onClick = { viewModel.setScanMode("SCAN_MODE_LOW_LATENCY") },
                label = { Text("Low Latency", fontSize = 10.sp) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Kalman Filter Noise Smoothing:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Switch(
                checked = state.isKalmanFilterEnabled,
                onCheckedChange = { viewModel.toggleKalmanFilter() }
            )
        }

        Text("📡 Discovered Beacon Hardware (${state.discoveredBeacons.size}):", fontWeight = FontWeight.Bold, fontSize = 14.sp)

        if (state.discoveredBeacons.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        if (state.isScanning) "Searching for BLE advertising packets..." else "Tap 'Start Scan' above to scan for nearby beacons.",
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            state.discoveredBeacons.forEach { beacon ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${beacon.frameType} Frame", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (beacon.estimatedDistanceMeters < 1.0) Color(0xFF2E7D32) else Color(0xFF1565C0))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(beacon.proximityZone, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text("UUID: ${beacon.uuid}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Major: ${beacon.major}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Minor: ${beacon.minor}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("RSSI: ${beacon.rssi} dBm", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("📐 Distance: ${beacon.estimatedDistanceMeters}m (Tx: ${beacon.txPower} dBm)", fontSize = 11.sp, color = Color(0xFFE65100), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BlePacketDecoderTab(
    state: BleScannerUiState,
    viewModel: BleBeaconScannerViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("🔬 BLE Advertisement Packet Binary Structure", fontWeight = FontWeight.Bold, fontSize = 15.sp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Standard iBeacon 30-Byte Frame Format:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = """
Byte 0-1:   0x02, 0x01 (Flags)
Byte 2-3:   0x1A, 0xFF (Manufacturer Data Length & Type)
Byte 4-5:   0x4C, 0x00 (Apple Company Identifier)
Byte 6-7:   0x02, 0x15 (iBeacon Sub-Type & Length: 21 bytes)
Byte 8-23:  16-Byte Proximity UUID
Byte 24-25: Major Number (Big Endian 16-bit)
Byte 26-27: Minor Number (Big Endian 16-bit)
Byte 28:    Measured TxPower at 1 meter (Signed 8-bit)
                        """.trimIndent(),
                        color = Color(0xFF81D4FA),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("💡 Path Loss Distance Formula:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("• Formula: Distance = 10 ^ ((TxPower - RSSI) / (10 * N)) where N = 2.0 (path loss exponent).", fontSize = 11.sp)
                Text("• Low-pass Kalman filtering removes multi-path RF reflections and human body attenuation noise.", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun BlePermissionsTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("🔒 Android 12+ (API 31+) & 14+ Bluetooth Permissions", fontWeight = FontWeight.Bold, fontSize = 15.sp)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AndroidManifest.xml Declarations:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = """
<!-- Android 12+ Runtime Permissions -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation" /> <!-- Omit flag if using beacon for location -->
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- Required for beacon distance triangulation -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
                        """.trimIndent(),
                        color = Color(0xFFC3E88D),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
