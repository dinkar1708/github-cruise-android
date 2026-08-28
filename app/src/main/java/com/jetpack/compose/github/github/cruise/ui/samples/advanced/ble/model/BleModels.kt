package com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble.model

import java.util.UUID

// =============================================================================
// BLE GATT CLIENT & SERVER DATA MODELS
// =============================================================================

data class DiscoveredBleDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val bondState: String = "BOND_NONE",
    val isConnectable: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)

data class BleDescriptorModel(
    val uuid: String,
    val name: String,
    val value: String = ""
)

data class BleCharacteristicModel(
    val uuid: String,
    val name: String,
    val properties: List<String>,
    val currentValue: String = "",
    val isNotifying: Boolean = false,
    val descriptors: List<BleDescriptorModel> = emptyList()
)

data class BleServiceModel(
    val uuid: String,
    val name: String,
    val characteristics: List<BleCharacteristicModel> = emptyList()
)

data class GattQueueItem(
    val id: String = UUID.randomUUID().toString().take(8),
    val operationType: String,
    val targetUuid: String,
    val payload: String = "",
    val status: String = "QUEUED",
    val executionTimeMs: Long = 0L
)

enum class GattConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCOVERING_SERVICES,
    SERVICES_DISCOVERED,
    DISCONNECTING
}

data class GattServerState(
    val isServerRunning: Boolean = false,
    val isAdvertising: Boolean = false,
    val connectedCentralsCount: Int = 0,
    val advertisedServiceName: String = "Smart Heart Rate & Telemetry Hub",
    val advertisedServiceUuid: String = "0000180D-0000-1000-8000-00805F9B34FB",
    val heartRateBpm: Int = 72
)

data class BleGattUiState(
    val isScanning: Boolean = false,
    val scanMode: String = "SCAN_MODE_LOW_LATENCY",
    val discoveredDevices: List<DiscoveredBleDevice> = emptyList(),
    val targetDeviceAddress: String = "C4:D3:58:90:12:AB",
    val connectionStatus: GattConnectionStatus = GattConnectionStatus.DISCONNECTED,
    val negotiatedMtu: Int = 23,
    val connectionPriority: String = "CONNECTION_PRIORITY_BALANCED",
    val discoveredServices: List<BleServiceModel> = emptyList(),
    val lastNotificationValue: String = "",
    val notificationStreamHistory: List<String> = emptyList(),
    val deviceBondState: String = "BOND_NONE",
    val bondSecurityLevel: String = "Level 3 (MITM Authenticated Passkey)",
    val serverState: GattServerState = GattServerState(),
    val gattQueue: List<GattQueueItem> = emptyList(),
    val isQueueActive: Boolean = false,
    val terminalLogs: List<String> = emptyList(),
    val isHardwareBluetoothActive: Boolean = false
)

// =============================================================================
// BLE BEACON DATA MODELS
// =============================================================================

data class BleBeaconItem(
    val frameType: String = "iBeacon", // iBeacon, AltBeacon, Eddystone-UID
    val uuid: String,
    val major: Int,
    val minor: Int,
    val rssi: Int,
    val txPower: Int = -59,
    val estimatedDistanceMeters: Double,
    val proximityZone: String, // Immediate (<1m), Near (1-3m), Far (>3m)
    val packetsCount: Int = 1,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)

data class BleScannerUiState(
    val isScanning: Boolean = false,
    val scanMode: String = "SCAN_MODE_BALANCED", // LOW_POWER, BALANCED, LOW_LATENCY
    val rssiThreshold: Int = -90, // Minimum dBm
    val discoveredBeacons: List<BleBeaconItem> = emptyList(),
    val totalPacketsCaptured: Int = 0,
    val isKalmanFilterEnabled: Boolean = true,
    val terminalLogs: List<String> = emptyList()
)
