package com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble.model.BleGattUiState
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble.model.GattConnectionStatus
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
 * Production-Grade BLE GATT Client, GATT Server, Device Scanning & Pairing Screen
 *
 * Demonstrates:
 * 1. BluetoothLeScanner with device discovery & MAC address assignment
 * 2. BluetoothGatt Client (connectGatt, discoverServices, requestMtu, priority)
 * 3. Characteristic Read / Write (WRITE_TYPE_DEFAULT) & Descriptor CCCD Notifications
 * 4. BluetoothGattServer (openGattServer) & BluetoothLeAdvertiser peripheral mode
 * 5. Device Pairing & Bonding (createBond, ACTION_BOND_STATE_CHANGED)
 * 6. Sequential GATT Operation Queue preventing native Error Code 133
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleGattManagerScreen(
    onBackClick: () -> Unit,
    viewModel: BleGattManagerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "GATT Client",
        "GATT Server (Peripheral)",
        "Device Pairing (createBond)",
        "GATT 133 & Queue Architecture"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BLE GATT & Peripheral Suite") },
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
            // Master Connection & Status Banner
            GattMasterStatusBar(state = state, viewModel = viewModel)

            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // Main Tab Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                when (selectedTab) {
                    0 -> GattClientTab(state = state, viewModel = viewModel)
                    1 -> GattServerTab(state = state, viewModel = viewModel)
                    2 -> GattPairingTab(state = state, viewModel = viewModel)
                    3 -> GattQueueArchitectureTab(state = state, viewModel = viewModel)
                }
            }

            // Live GATT Terminal Logs
            GattTerminalConsole(logs = state.terminalLogs)
        }
    }
}

// =============================================================================
// MASTER STATUS BANNER
// =============================================================================

@Composable
private fun GattMasterStatusBar(
    state: BleGattUiState,
    viewModel: BleGattManagerViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val statusColor = when (state.connectionStatus) {
                        GattConnectionStatus.DISCONNECTED -> Color.Gray
                        GattConnectionStatus.CONNECTING -> Color(0xFFFFA000)
                        GattConnectionStatus.CONNECTED -> Color(0xFF1976D2)
                        GattConnectionStatus.DISCOVERING_SERVICES -> Color(0xFF7B1FA2)
                        GattConnectionStatus.SERVICES_DISCOVERED -> Color(0xFF2E7D32)
                        GattConnectionStatus.DISCONNECTING -> Color.Red
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Text(
                        text = "Status: ${state.connectionStatus.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = statusColor
                    )
                }
                Text(
                    text = "Target: ${state.targetDeviceAddress} | MTU: ${state.negotiatedMtu}B",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (state.connectionStatus == GattConnectionStatus.DISCONNECTED) {
                    Button(
                        onClick = { viewModel.connectToDevice() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Connect GATT", fontSize = 11.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.disconnectDevice() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Disconnect", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 1: GATT CLIENT & DISCOVERY
// =============================================================================

@Composable
private fun GattClientTab(
    state: BleGattUiState,
    viewModel: BleGattManagerViewModel
) {
    var customAddressInput by remember { mutableStateOf(state.targetDeviceAddress) }
    var writePayloadInput by remember { mutableStateOf("0x01_CONFIG") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Target Device MAC Address Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🎯 Target BLE Device MAC Address (Assume Any Address):", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customAddressInput,
                        onValueChange = { customAddressInput = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("MAC Address (e.g. C4:D3:58:90:12:AB)", fontSize = 11.sp) },
                        singleLine = true
                    )
                    Button(
                        onClick = { viewModel.setTargetAddress(customAddressInput) },
                        enabled = customAddressInput.isNotBlank()
                    ) {
                        Text("Set", fontSize = 11.sp)
                    }
                }

                // Quick Preset Chips
                Text("Hardware Presets / Mock Targets:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = state.targetDeviceAddress == "C4:D3:58:90:12:AB",
                        onClick = {
                            customAddressInput = "C4:D3:58:90:12:AB"
                            viewModel.setTargetAddress("C4:D3:58:90:12:AB")
                        },
                        label = { Text("Polar H10", fontSize = 9.sp) }
                    )
                    FilterChip(
                        selected = state.targetDeviceAddress == "00:1A:7D:DA:71:13",
                        onClick = {
                            customAddressInput = "00:1A:7D:DA:71:13"
                            viewModel.setTargetAddress("00:1A:7D:DA:71:13")
                        },
                        label = { Text("nRF52840", fontSize = 9.sp) }
                    )
                    FilterChip(
                        selected = state.targetDeviceAddress == "78:BD:BC:33:44:55",
                        onClick = {
                            customAddressInput = "78:BD:BC:33:44:55"
                            viewModel.setTargetAddress("78:BD:BC:33:44:55")
                        },
                        label = { Text("GATT Server", fontSize = 9.sp) }
                    )
                }
            }
        }

        // BluetoothLeScanner Control
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("📡 Nearby BLE Peripherals Scanner", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            if (state.isScanning) "Active Scan (SCAN_MODE_LOW_LATENCY)..." else "Scanner Idle",
                            fontSize = 10.sp,
                            color = if (state.isScanning) Color(0xFF2E7D32) else Color.Gray
                        )
                    }
                    Button(
                        onClick = { viewModel.toggleScanner() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isScanning) MaterialTheme.colorScheme.error else Color(0xFF1976D2)
                        )
                    ) {
                        Text(if (state.isScanning) "Stop Scan" else "Scan Devices", fontSize = 10.sp)
                    }
                }

                if (state.discoveredDevices.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.discoveredDevices.forEach { dev ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(dev.name, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                    Text(
                                        text = "${dev.address} | RSSI: ${dev.rssi} dBm | ${dev.bondState}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        customAddressInput = dev.address
                                        viewModel.setTargetAddress(dev.address)
                                        viewModel.connectToDevice(dev.address)
                                    }
                                ) {
                                    Text("Connect", fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // MTU & Connection Priority Optimizer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("⚡ MTU Negotiation & Connection Priority", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current MTU: ${state.negotiatedMtu} Bytes", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        Text("Max Payload: ${state.negotiatedMtu - 3} Bytes (ATT Header = 3B)", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(onClick = { viewModel.requestMtu(247) }) {
                            Text("247B", fontSize = 10.sp)
                        }
                        Button(onClick = { viewModel.requestMtu(512) }) {
                            Text("512B", fontSize = 10.sp)
                        }
                    }
                }

                Text("Connection Priority (Interval):", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = state.connectionPriority == "CONNECTION_PRIORITY_HIGH",
                        onClick = { viewModel.setConnectionPriority("CONNECTION_PRIORITY_HIGH") },
                        label = { Text("High (11.25ms)", fontSize = 9.sp) }
                    )
                    FilterChip(
                        selected = state.connectionPriority == "CONNECTION_PRIORITY_BALANCED",
                        onClick = { viewModel.setConnectionPriority("CONNECTION_PRIORITY_BALANCED") },
                        label = { Text("Balanced (30-50ms)", fontSize = 9.sp) }
                    )
                    FilterChip(
                        selected = state.connectionPriority == "CONNECTION_PRIORITY_LOW_POWER",
                        onClick = { viewModel.setConnectionPriority("CONNECTION_PRIORITY_LOW_POWER") },
                        label = { Text("Low Power (100ms)", fontSize = 9.sp) }
                    )
                }
            }
        }

        // GATT Services & Characteristics Explorer
        Text("🌲 Discovered GATT Profile Tree:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

        state.discoveredServices.forEach { service ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Service Header
                    Column {
                        Text(service.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text("UUID: ${service.uuid}", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Characteristics
                    service.characteristics.forEach { char ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(char.name, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                    Text(char.uuid, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color.Gray)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    char.properties.forEach { prop ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(0xFF37474F))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(prop, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Current Value
                            if (char.currentValue.isNotBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1E1E1E), RoundedCornerShape(4.dp))
                                        .padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Val: ${char.currentValue}",
                                        color = Color(0xFF81D4FA),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Action Controls (Read / Write / Notify)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (char.properties.contains("READ")) {
                                    OutlinedButton(
                                        onClick = { viewModel.readCharacteristic(service.uuid, char.uuid) }
                                    ) {
                                        Text("Read", fontSize = 10.sp)
                                    }
                                }

                                if (char.properties.contains("WRITE") || char.properties.contains("WRITE_NO_RESPONSE")) {
                                    Button(
                                        onClick = { viewModel.writeCharacteristic(service.uuid, char.uuid, writePayloadInput) }
                                    ) {
                                        Text("Write", fontSize = 10.sp)
                                    }
                                }

                                if (char.properties.contains("NOTIFY") || char.properties.contains("INDICATE")) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(if (char.isNotifying) "Notify ON" else "Notify OFF", fontSize = 10.sp)
                                        Switch(
                                            checked = char.isNotifying,
                                            onCheckedChange = { viewModel.toggleNotification(service.uuid, char.uuid) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 2: GATT SERVER (PERIPHERAL MODE)
// =============================================================================

@Composable
private fun GattServerTab(
    state: BleGattUiState,
    viewModel: BleGattManagerViewModel
) {
    val server = state.serverState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🖥️ BluetoothGattServer & Advertiser", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = if (server.isServerRunning) "GATT Server: RUNNING & ADVERTISING" else "GATT Server: STOPPED",
                            color = if (server.isServerRunning) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = { viewModel.toggleGattServer() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (server.isServerRunning) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                        )
                    ) {
                        Text(if (server.isServerRunning) "Stop Server" else "Start Server", fontSize = 10.sp)
                    }
                }

                Text(
                    text = "• Advertised Service: ${server.advertisedServiceName}\n• UUID: ${server.advertisedServiceUuid}\n• Connected Centrals: ${server.connectedCentralsCount}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Push Notification Emitter Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📢 Push Telemetry Notification to Centrals", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(
                    text = "Simulates BluetoothGattServer.notifyCharacteristicChanged() -> pushes instant sensor readings to all subscribed central apps without client polling.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Heart Rate: ${server.heartRateBpm} BPM", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFD32F2F))
                    Button(
                        onClick = { viewModel.sendServerPushNotification() },
                        enabled = server.isServerRunning
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Broadcast Notification", fontSize = 10.sp)
                    }
                }
            }
        }

        // Server Architecture Code Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("🛠️ Android GATT Server Lifecycle Flow:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = """
1. openGattServer(context, gattServerCallback)
2. BluetoothGattService(UUID, SERVICE_TYPE_PRIMARY)
3. service.addCharacteristic(charReadWriteNotify)
4. gattServer.addService(service)
5. BluetoothLeAdvertiser.startAdvertising(settings, data, advCallback)
6. onCharacteristicReadRequest -> sendResponse(GATT_SUCCESS, value)
7. onCharacteristicWriteRequest -> sendResponse(GATT_SUCCESS)
8. notifyCharacteristicChanged(device, characteristic, confirm)
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

// =============================================================================
// TAB 3: PAIRING & BONDING
// =============================================================================

@Composable
private fun GattPairingTab(
    state: BleGattUiState,
    viewModel: BleGattManagerViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("🔐 Bluetooth Device Pairing & Security Manager", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Target Device: ${state.targetDeviceAddress}", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        Text(
                            text = "Bond State: ${state.deviceBondState}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = when (state.deviceBondState) {
                                "BOND_BONDED" -> Color(0xFF2E7D32)
                                "BOND_BONDING" -> Color(0xFFFFA000)
                                else -> Color.Gray
                            }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (state.deviceBondState != "BOND_BONDED") {
                            Button(
                                onClick = { viewModel.initiateBonding() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                            ) {
                                Text("createBond()", fontSize = 10.sp)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.unbondDevice() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Remove Bond", fontSize = 10.sp)
                            }
                        }
                    }
                }

                Text(
                    text = "Security Level: ${state.bondSecurityLevel}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Bluetooth Security Model Breakdown Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🛡️ BLE Security & Pairing Modes (SMP Protocol):", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("• Just Works: Unauthenticated key exchange. No MITM (Man-in-the-Middle) protection. Used for simple displays/peripherals.", fontSize = 10.sp)
                    Text("• Passkey Entry: 6-digit numeric PIN shown on peripheral and typed on central phone. Authenticated against MITM.", fontSize = 10.sp)
                    Text("• Numeric Comparison (LE Secure Connections): Both devices display 6-digit numbers; user taps 'Yes' if they match. ECDH P-256 curve encryption.", fontSize = 10.sp)
                    Text("• LTK (Long-Term Key): Saved in Android OS KeyStore so subsequent reconnections are automatically encrypted without re-pairing.", fontSize = 10.sp)
                }
            }
        }
    }
}

// =============================================================================
// TAB 4: GATT 133 & OPERATION QUEUE ARCHITECTURE
// =============================================================================

@Composable
private fun GattQueueArchitectureTab(
    state: BleGattUiState,
    viewModel: BleGattManagerViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("⚠️ Why GATT Error 133 Happens & The Sequential Queue Fix", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = "Android's Fluoride/BlueDroid BLE stack is strictly single-threaded per connection. If you invoke writeCharacteristic() while a readCharacteristic() or requestMtu() is in-flight, Android drops the command and yields fatal error 133 (GATT_ERROR).",
                    fontSize = 11.sp
                )

                Button(
                    onClick = { viewModel.fireSimulatedQueueBurst() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isQueueActive
                ) {
                    if (state.isQueueActive) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("Run Sequential Mutex Queue Demo (4 Operations)", fontSize = 11.sp)
                }
            }
        }

        // Live Queue State Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📋 In-Flight & Completed Queue Items (${state.gattQueue.size}):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    if (state.isQueueActive) {
                        Text("MUTEX LOCKED", color = Color(0xFFFFA000), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (state.gattQueue.isEmpty()) {
                    Text("Tap 'Run Sequential Mutex Queue Demo' above to observe thread-safe serialized execution.", fontSize = 10.sp, color = Color.Gray)
                } else {
                    state.gattQueue.takeLast(6).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${item.operationType} (${item.targetUuid})", fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                                if (item.payload.isNotBlank()) {
                                    Text("Payload: ${item.payload}", fontSize = 9.sp, color = Color.Gray)
                                }
                            }
                            val statusColor = when (item.status) {
                                "SUCCESS" -> Color(0xFF2E7D32)
                                "EXECUTING" -> Color(0xFF1976D2)
                                "FAILED" -> Color.Red
                                else -> Color.Gray
                            }
                            Text("${item.status} ${if (item.executionTimeMs > 0) "${item.executionTimeMs}ms" else ""}", color = statusColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// LIVE TERMINAL CONSOLE COMPONENT
// =============================================================================

@Composable
private fun GattTerminalConsole(logs: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("💻 BLE GATT & Server Opcode Logs", color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("${logs.size} entries", color = Color.Gray, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212), RoundedCornerShape(4.dp))
                    .padding(6.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (logs.isEmpty()) {
                    Text("Live GATT callbacks, opcodes, and server events will appear here...", color = Color.DarkGray, fontSize = 10.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        logs.forEach { log ->
                            Text(
                                text = log,
                                color = Color(0xFFE0E0E0),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
