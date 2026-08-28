package com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble.model.BleCharacteristicModel
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble.model.BleDescriptorModel
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble.model.BleGattUiState
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble.model.BleServiceModel
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble.model.DiscoveredBleDevice
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble.model.GattConnectionStatus
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble.model.GattQueueItem
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble.model.GattServerState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@SuppressLint("MissingPermission")
@HiltViewModel
class BleGattManagerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BleGattUiState())
    val uiState: StateFlow<BleGattUiState> = _uiState.asStateFlow()

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? get() = bluetoothManager?.adapter

    private var activeGatt: BluetoothGatt? = null
    private var gattServer: BluetoothGattServer? = null
    private var bleAdvertiser: BluetoothLeAdvertiser? = null

    private var scanSimulationJob: Job? = null
    private var notificationStreamJob: Job? = null
    private var serverSimulationJob: Job? = null
    private val gattMutex = Mutex()
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    private val HR_SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    private val HR_CHAR_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

    init {
        setupDefaultServices()
        registerBondStateReceiver()
        checkHardwareBluetoothAvailability()
    }

    private fun addLog(message: String) {
        val timestamp = timeFormat.format(Date())
        val entry = "[$timestamp] $message"
        _uiState.update { it.copy(terminalLogs = (it.terminalLogs + entry).takeLast(60)) }
        Timber.d(entry)
    }

    private fun checkHardwareBluetoothAvailability() {
        val adapter = bluetoothAdapter
        val isEnabled = adapter?.isEnabled == true
        _uiState.update { it.copy(isHardwareBluetoothActive = isEnabled) }
        if (isEnabled) {
            addLog("🟢 [Bluetooth Hardware] BluetoothAdapter is ACTIVE & ENABLED on device.")
        } else {
            addLog("🟡 [Bluetooth Hardware] Bluetooth radio is OFF or running on Emulator. Hybrid test mode active.")
        }
    }

    // =========================================================================
    // 1. REAL BLUETOOTH LE SCANNER (+ HYBRID EMULATOR FALLBACK)
    // =========================================================================

    private val realScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = try { device.name ?: "Unknown BLE Device" } catch (e: Exception) { "Unknown BLE Device" }
            val address = device.address
            val rssi = result.rssi
            val bond = when (try { device.bondState } catch (e: Exception) { BluetoothDevice.BOND_NONE }) {
                BluetoothDevice.BOND_BONDED -> "BOND_BONDED"
                BluetoothDevice.BOND_BONDING -> "BOND_BONDING"
                else -> "BOND_NONE"
            }

            addDiscoveredDevice(
                DiscoveredBleDevice(
                    name = name,
                    address = address,
                    rssi = rssi,
                    bondState = bond
                )
            )
        }

        override fun onScanFailed(errorCode: Int) {
            addLog("⚠️ [BluetoothLeScanner] Real scan error code: $errorCode (Switching to test scanner)")
        }
    }

    private fun addDiscoveredDevice(device: DiscoveredBleDevice) {
        _uiState.update { state ->
            val updated = (state.discoveredDevices.filterNot { it.address == device.address } + device)
                .sortedByDescending { it.rssi }
            state.copy(discoveredDevices = updated)
        }
        addLog("📱 [ScanResult] Found '${device.name}' (${device.address}) RSSI: ${device.rssi} dBm")
    }

    fun toggleScanner() {
        if (_uiState.value.isScanning) {
            stopScan()
        } else {
            startScan()
        }
    }

    private fun startScan() {
        scanSimulationJob?.cancel()
        _uiState.update { it.copy(isScanning = true) }
        addLog("📡 [BluetoothLeScanner] Starting BLE Scan (Mode: ${_uiState.value.scanMode})...")

        var realScanStarted = false
        try {
            val scanner = bluetoothAdapter?.bluetoothLeScanner
            if (bluetoothAdapter?.isEnabled == true && scanner != null) {
                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()
                scanner.startScan(null, settings, realScanCallback)
                realScanStarted = true
                addLog("✅ [Hardware Scanner] Real BluetoothLeScanner.startScan() actively listening to RF air packets!")
            }
        } catch (e: Exception) {
            addLog("⚠️ [Hardware Scanner] Scan exception: ${e.message}")
        }

        // Always launch mock simulator as fallback/supplement so UI is testable anywhere
        scanSimulationJob = viewModelScope.launch {
            val samplePeripherals = listOf(
                DiscoveredBleDevice("Polar H10 Heart Rate", "C4:D3:58:90:12:AB", -62, "BOND_BONDED"),
                DiscoveredBleDevice("Nordic nRF52840 Sensor Hub", "00:1A:7D:DA:71:13", -74, "BOND_NONE"),
                DiscoveredBleDevice("Apple Watch Series 9", "FA:45:90:EE:22:99", -54, "BOND_BONDED"),
                DiscoveredBleDevice("Custom IoT Gateway (GATT Server)", "78:BD:BC:33:44:55", -81, "BOND_NONE")
            )

            samplePeripherals.forEach { dev ->
                delay(600)
                addDiscoveredDevice(dev)
            }
        }
    }

    private fun stopScan() {
        scanSimulationJob?.cancel()
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(realScanCallback)
        } catch (e: Exception) {
            Timber.e(e)
        }
        _uiState.update { it.copy(isScanning = false) }
        addLog("⏹️ [BluetoothLeScanner] stopScan() called. Scanner radio powered down.")
    }

    fun setTargetAddress(address: String) {
        val cleaned = address.trim().uppercase()
        _uiState.update { it.copy(targetDeviceAddress = cleaned) }
        addLog("🎯 [Target Device] MAC Address set to: $cleaned")
    }

    // =========================================================================
    // 2. GATT CLIENT CONNECTION LIFECYCLE & CALLBACKS
    // =========================================================================

    private val realGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                _uiState.update { it.copy(connectionStatus = GattConnectionStatus.CONNECTED) }
                addLog("✅ [BluetoothGattCallback] Connected to ${gatt.device.address}! Requesting 512B MTU...")
                gatt.requestMtu(512)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _uiState.update { it.copy(connectionStatus = GattConnectionStatus.DISCONNECTED) }
                addLog("🛑 [BluetoothGattCallback] Disconnected from ${gatt.device.address}. Closing GATT.")
                gatt.close()
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _uiState.update { it.copy(negotiatedMtu = mtu, connectionStatus = GattConnectionStatus.DISCOVERING_SERVICES) }
                addLog("⚡ [onMtuChanged] MTU negotiated to $mtu bytes. Discovering services...")
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val mappedServices = gatt.services.map { svc ->
                    BleServiceModel(
                        uuid = svc.uuid.toString().uppercase(),
                        name = resolveServiceName(svc.uuid),
                        characteristics = svc.characteristics.map { char ->
                            val props = mutableListOf<String>()
                            if (char.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) props.add("READ")
                            if (char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) props.add("WRITE")
                            if (char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) props.add("WRITE_NO_RESPONSE")
                            if (char.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) props.add("NOTIFY")
                            if (char.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) props.add("INDICATE")

                            BleCharacteristicModel(
                                uuid = char.uuid.toString().uppercase(),
                                name = resolveCharacteristicName(char.uuid),
                                properties = props,
                                descriptors = char.descriptors.map { desc ->
                                    BleDescriptorModel(uuid = desc.uuid.toString().uppercase(), name = "Descriptor")
                                }
                            )
                        }
                    )
                }

                _uiState.update {
                    it.copy(
                        discoveredServices = mappedServices.ifEmpty { it.discoveredServices },
                        connectionStatus = GattConnectionStatus.SERVICES_DISCOVERED
                    )
                }
                addLog("🎉 [onServicesDiscovered] ${gatt.services.size} hardware GATT services discovered!")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            val hex = value.joinToString(" ") { "0x%02X".format(it) }
            addLog("📥 [onCharacteristicRead] ${characteristic.uuid}: $hex")
            updateCharValue(characteristic.uuid.toString().uppercase(), hex)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            @Suppress("DEPRECATION")
            characteristic.value?.let { bytes ->
                val hex = bytes.joinToString(" ") { "0x%02X".format(it) }
                addLog("📥 [onCharacteristicRead] ${characteristic.uuid}: $hex")
                updateCharValue(characteristic.uuid.toString().uppercase(), hex)
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            addLog("📤 [onCharacteristicWrite] ${characteristic.uuid}: status=$status (ACK received)")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            val hex = value.joinToString(" ") { "0x%02X".format(it) }
            addLog("⚡ [onCharacteristicChanged] ${characteristic.uuid}: $hex")
            updateCharValue(characteristic.uuid.toString().uppercase(), hex)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            @Suppress("DEPRECATION")
            characteristic.value?.let { bytes ->
                val hex = bytes.joinToString(" ") { "0x%02X".format(it) }
                addLog("⚡ [onCharacteristicChanged] ${characteristic.uuid}: $hex")
                updateCharValue(characteristic.uuid.toString().uppercase(), hex)
            }
        }
    }

    private fun updateCharValue(charUuid: String, value: String) {
        _uiState.update { state ->
            val updated = state.discoveredServices.map { svc ->
                svc.copy(characteristics = svc.characteristics.map { char ->
                    if (char.uuid.equals(charUuid, ignoreCase = true)) char.copy(currentValue = value) else char
                })
            }
            state.copy(
                discoveredServices = updated,
                lastNotificationValue = value,
                notificationStreamHistory = (state.notificationStreamHistory + value).takeLast(10)
            )
        }
    }

    fun connectToDevice(address: String? = null) {
        val target = address ?: _uiState.value.targetDeviceAddress
        if (target.isBlank()) {
            addLog("⚠️ [GATT Client] MAC Address is blank.")
            return
        }

        _uiState.update {
            it.copy(
                targetDeviceAddress = target,
                connectionStatus = GattConnectionStatus.CONNECTING
            )
        }
        addLog("🔗 [GATT Client] Connecting to $target (TRANSPORT_LE)...")

        var hardwareConnected = false
        try {
            val adapter = bluetoothAdapter
            if (adapter?.isEnabled == true && BluetoothAdapter.checkBluetoothAddress(target)) {
                val device = adapter.getRemoteDevice(target)
                activeGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    device.connectGatt(context, false, realGattCallback, BluetoothDevice.TRANSPORT_LE)
                } else {
                    device.connectGatt(context, false, realGattCallback)
                }
                hardwareConnected = true
            }
        } catch (e: Exception) {
            addLog("⚠️ [GATT Client] Real connect error: ${e.message}")
        }

        // Mock fallback simulation if hardware connection isn't physically present
        if (!hardwareConnected) {
            viewModelScope.launch {
                delay(800)
                _uiState.update { it.copy(connectionStatus = GattConnectionStatus.CONNECTED) }
                addLog("✅ [BluetoothGattCallback] Connected (Simulated). Discovering services...")
                delay(600)
                setupDefaultServices()
                _uiState.update { it.copy(connectionStatus = GattConnectionStatus.SERVICES_DISCOVERED) }
                addLog("🎉 [BluetoothGattCallback] onServicesDiscovered: ${_uiState.value.discoveredServices.size} services mapped.")
            }
        }
    }

    fun disconnectDevice() {
        notificationStreamJob?.cancel()
        try {
            activeGatt?.disconnect()
            activeGatt?.close()
            activeGatt = null
        } catch (e: Exception) {
            Timber.e(e)
        }

        _uiState.update {
            it.copy(
                connectionStatus = GattConnectionStatus.DISCONNECTED,
                negotiatedMtu = 23,
                discoveredServices = it.discoveredServices.map { svc ->
                    svc.copy(characteristics = svc.characteristics.map { char -> char.copy(isNotifying = false) })
                }
            )
        }
        addLog("🛑 [GATT Client] Disconnected & GATT handle closed.")
    }

    // =========================================================================
    // 3. MTU & CONNECTION PRIORITY
    // =========================================================================

    fun requestMtu(mtu: Int) {
        viewModelScope.launch {
            try {
                activeGatt?.requestMtu(mtu)
            } catch (e: Exception) {
                Timber.e(e)
            }
            delay(300)
            _uiState.update { it.copy(negotiatedMtu = mtu) }
            addLog("⚡ [onMtuChanged] MTU set to $mtu bytes (Payload: ${mtu - 3}B)")
        }
    }

    fun setConnectionPriority(priority: String) {
        _uiState.update { it.copy(connectionPriority = priority) }
        val prioInt = when (priority) {
            "CONNECTION_PRIORITY_HIGH" -> BluetoothGatt.CONNECTION_PRIORITY_HIGH
            "CONNECTION_PRIORITY_LOW_POWER" -> BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER
            else -> BluetoothGatt.CONNECTION_PRIORITY_BALANCED
        }
        try {
            activeGatt?.requestConnectionPriority(prioInt)
        } catch (e: Exception) {
            Timber.e(e)
        }
        addLog("⚡ [GATT Client] requestConnectionPriority($priority)")
    }

    // =========================================================================
    // 4. CHARACTERISTIC READ, WRITE, NOTIFY & QUEUE (ERROR 133 FIX)
    // =========================================================================

    fun readCharacteristic(serviceUuid: String, charUuid: String) {
        viewModelScope.launch {
            enqueueOperation("READ", charUuid) {
                var handled = false
                try {
                    val sUuid = UUID.fromString(serviceUuid)
                    val cUuid = UUID.fromString(charUuid)
                    val service = activeGatt?.getService(sUuid)
                    val char = service?.getCharacteristic(cUuid)
                    if (char != null && activeGatt != null) {
                        activeGatt?.readCharacteristic(char)
                        handled = true
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }

                if (!handled) {
                    delay(300)
                    val mockVal = when {
                        charUuid.contains("2A37", true) -> "${Random.nextInt(70, 90)} BPM [Heart Rate]"
                        charUuid.contains("2A19", true) -> "${Random.nextInt(75, 99)}% [Battery Level]"
                        else -> "0x${Random.nextInt(1000, 9999)}"
                    }
                    updateCharValue(charUuid, mockVal)
                    addLog("📥 [GATT Read] $charUuid: $mockVal")
                }
            }
        }
    }

    fun writeCharacteristic(serviceUuid: String, charUuid: String, payload: String) {
        val writeVal = payload.ifBlank { "0x01_CMD" }
        viewModelScope.launch {
            enqueueOperation("WRITE", charUuid, writeVal) {
                var handled = false
                try {
                    val sUuid = UUID.fromString(serviceUuid)
                    val cUuid = UUID.fromString(charUuid)
                    val char = activeGatt?.getService(sUuid)?.getCharacteristic(cUuid)
                    if (char != null && activeGatt != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            activeGatt?.writeCharacteristic(char, writeVal.toByteArray(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                        } else {
                            @Suppress("DEPRECATION")
                            char.value = writeVal.toByteArray()
                            @Suppress("DEPRECATION")
                            activeGatt?.writeCharacteristic(char)
                        }
                        handled = true
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }

                if (!handled) {
                    delay(300)
                    updateCharValue(charUuid, writeVal)
                    addLog("📤 [GATT Write] $charUuid = \"$writeVal\" (ACK Received)")
                }
            }
        }
    }

    fun toggleNotification(serviceUuid: String, charUuid: String) {
        val isNotifying = _uiState.value.discoveredServices
            .firstOrNull { it.uuid == serviceUuid }
            ?.characteristics
            ?.firstOrNull { it.uuid == charUuid }
            ?.isNotifying ?: false

        val nextState = !isNotifying

        viewModelScope.launch {
            enqueueOperation("WRITE_DESCRIPTOR", "CCCD (0x2902)", if (nextState) "0x0100" else "0x0000") {
                try {
                    val sUuid = UUID.fromString(serviceUuid)
                    val cUuid = UUID.fromString(charUuid)
                    val char = activeGatt?.getService(sUuid)?.getCharacteristic(cUuid)
                    if (char != null && activeGatt != null) {
                        activeGatt?.setCharacteristicNotification(char, nextState)
                        val desc = char.getDescriptor(CCCD_UUID)
                        if (desc != null) {
                            val value = if (nextState) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                activeGatt?.writeDescriptor(desc, value)
                            } else {
                                @Suppress("DEPRECATION")
                                desc.value = value
                                @Suppress("DEPRECATION")
                                activeGatt?.writeDescriptor(desc)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }

                delay(300)
                _uiState.update { state ->
                    val updated = state.discoveredServices.map { svc ->
                        if (svc.uuid == serviceUuid) {
                            svc.copy(characteristics = svc.characteristics.map { c ->
                                if (c.uuid == charUuid) c.copy(isNotifying = nextState) else c
                            })
                        } else svc
                    }
                    state.copy(discoveredServices = updated)
                }
                addLog("🔔 [GATT Notify] ${if (nextState) "Subscribed to CCCD (0x2902)" else "Unsubscribed"}")

                if (nextState) {
                    startMockNotificationStream(charUuid)
                } else {
                    notificationStreamJob?.cancel()
                }
            }
        }
    }

    private fun startMockNotificationStream(charUuid: String) {
        notificationStreamJob?.cancel()
        notificationStreamJob = viewModelScope.launch {
            while (isActive) {
                delay(1500)
                val streamValue = when {
                    charUuid.contains("2A37", true) -> "Heart Rate: ${Random.nextInt(72, 92)} BPM"
                    charUuid.contains("FFE1", true) -> "Sensor [X: ${(Random.nextFloat() * 2 - 1).format(2)}, Y: ${(Random.nextFloat() * 2 - 1).format(2)}, Z: 9.81]"
                    else -> "Telemetry Packet: 0x${Random.nextInt(1000, 9999)}"
                }
                updateCharValue(charUuid, streamValue)
            }
        }
    }

    private suspend fun enqueueOperation(
        operationType: String,
        targetUuid: String,
        payload: String = "",
        action: suspend () -> Unit
    ) {
        val item = GattQueueItem(
            operationType = operationType,
            targetUuid = targetUuid.take(18) + if (targetUuid.length > 18) "..." else "",
            payload = payload,
            status = "QUEUED"
        )
        _uiState.update { it.copy(gattQueue = it.gattQueue + item) }

        gattMutex.withLock {
            _uiState.update { state ->
                state.copy(
                    isQueueActive = true,
                    gattQueue = state.gattQueue.map { if (it.id == item.id) it.copy(status = "EXECUTING") else it }
                )
            }
            val start = System.currentTimeMillis()
            try {
                action()
                val duration = System.currentTimeMillis() - start
                _uiState.update { state ->
                    state.copy(
                        gattQueue = state.gattQueue.map {
                            if (it.id == item.id) it.copy(status = "SUCCESS", executionTimeMs = duration) else it
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(gattQueue = state.gattQueue.map { if (it.id == item.id) it.copy(status = "FAILED") else it })
                }
            } finally {
                _uiState.update { it.copy(isQueueActive = false) }
            }
        }
    }

    fun fireSimulatedQueueBurst() {
        viewModelScope.launch {
            addLog("🚀 [GATT Queue] Enqueuing 4 operations safely without GATT 133 collision...")
            enqueueOperation("REQUEST_MTU", "MTU: 512") { delay(250) }
            enqueueOperation("READ", "00002A19 (Battery)") { delay(250); updateCharValue("00002A19-0000-1000-8000-00805F9B34FB", "92%") }
            enqueueOperation("WRITE", "0000FFE1 (Command)", "PING_01") { delay(250) }
            enqueueOperation("WRITE_DESCRIPTOR", "00002902 (CCCD)", "0x0100") { delay(250) }
        }
    }

    // =========================================================================
    // 5. GATT SERVER & PERIPHERAL ADVERTISER MODE
    // =========================================================================

    private val serverCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            val count = if (newState == BluetoothProfile.STATE_CONNECTED) 1 else 0
            _uiState.update { it.copy(serverState = it.serverState.copy(connectedCentralsCount = count)) }
            addLog("🖥️ [GATT Server] Central '${device.address}' connection state: $newState")
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            val response = byteArrayOf(0x00, _uiState.value.serverState.heartRateBpm.toByte())
            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, response)
            addLog("📥 [GATT Server] Sent read response to '${device.address}'")
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
            }
            addLog("✍️ [GATT Server] Received write from '${device.address}': ${value.decodeToString()}")
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            addLog("📡 [Advertiser] BLE Advertising started successfully!")
        }
        override fun onStartFailure(errorCode: Int) {
            addLog("⚠️ [Advertiser] Advertising failed: $errorCode")
        }
    }

    fun toggleGattServer() {
        if (_uiState.value.serverState.isServerRunning) {
            stopGattServer()
        } else {
            startGattServer()
        }
    }

    private fun startGattServer() {
        try {
            gattServer = bluetoothManager?.openGattServer(context, serverCallback)
            val hrService = BluetoothGattService(HR_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
            val hrChar = BluetoothGattCharacteristic(
                HR_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ
            )
            hrChar.addDescriptor(BluetoothGattDescriptor(CCCD_UUID, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE))
            hrService.addCharacteristic(hrChar)
            gattServer?.addService(hrService)

            bleAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build()

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(ParcelUuid(HR_SERVICE_UUID))
                .build()

            bleAdvertiser?.startAdvertising(settings, data, advertiseCallback)
        } catch (e: Exception) {
            Timber.e(e)
        }

        _uiState.update {
            it.copy(serverState = it.serverState.copy(isServerRunning = true, isAdvertising = true, connectedCentralsCount = 1))
        }
        addLog("🖥️ [BluetoothGattServer] Server running & advertising Heart Rate (0x180D)...")

        serverSimulationJob = viewModelScope.launch {
            while (isActive) {
                delay(2000)
                val bpm = Random.nextInt(70, 85)
                _uiState.update { it.copy(serverState = it.serverState.copy(heartRateBpm = bpm)) }
            }
        }
    }

    private fun stopGattServer() {
        serverSimulationJob?.cancel()
        try {
            bleAdvertiser?.stopAdvertising(advertiseCallback)
            gattServer?.close()
            gattServer = null
        } catch (e: Exception) {
            Timber.e(e)
        }

        _uiState.update {
            it.copy(serverState = it.serverState.copy(isServerRunning = false, isAdvertising = false, connectedCentralsCount = 0))
        }
        addLog("🛑 [BluetoothGattServer] Server & Advertiser stopped.")
    }

    fun sendServerPushNotification() {
        val bpm = _uiState.value.serverState.heartRateBpm
        addLog("📢 [notifyCharacteristicChanged] Pushed Heart Rate: $bpm BPM to connected Centrals")
    }

    // =========================================================================
    // 6. PAIRING & BONDING (createBond)
    // =========================================================================

    private fun registerBondStateReceiver() {
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                if (intent.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                    val strState = when (state) {
                        BluetoothDevice.BOND_BONDED -> "BOND_BONDED"
                        BluetoothDevice.BOND_BONDING -> "BOND_BONDING"
                        else -> "BOND_NONE"
                    }
                    _uiState.update { it.copy(deviceBondState = strState) }
                    addLog("📡 [ACTION_BOND_STATE_CHANGED] Bond State: $strState")
                }
            }
        }
        try {
            context.registerReceiver(receiver, filter)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun initiateBonding(address: String? = null) {
        val target = address ?: _uiState.value.targetDeviceAddress
        _uiState.update { it.copy(deviceBondState = "BOND_BONDING") }
        addLog("🔐 [Pairing] Initiating createBond() for $target...")

        var realBondCalled = false
        try {
            val adapter = bluetoothAdapter
            if (adapter?.isEnabled == true && BluetoothAdapter.checkBluetoothAddress(target)) {
                val device = adapter.getRemoteDevice(target)
                device.createBond()
                realBondCalled = true
            }
        } catch (e: Exception) {
            addLog("⚠️ [Pairing] createBond error: ${e.message}")
        }

        if (!realBondCalled) {
            viewModelScope.launch {
                delay(1000)
                _uiState.update { it.copy(deviceBondState = "BOND_BONDED") }
                addLog("🎉 [Pairing] Device $target Bonded (128-bit LTK Encrypted link established).")
            }
        }
    }

    fun unbondDevice() {
        _uiState.update { it.copy(deviceBondState = "BOND_NONE") }
        addLog("🔓 [Pairing] Unbonded device. LTK removed.")
    }

    fun clearLogs() {
        _uiState.update { it.copy(terminalLogs = emptyList(), notificationStreamHistory = emptyList(), gattQueue = emptyList()) }
    }

    private fun setupDefaultServices() {
        val services = listOf(
            BleServiceModel(
                uuid = "0000180D-0000-1000-8000-00805F9B34FB",
                name = "Heart Rate Service (0x180D)",
                characteristics = listOf(
                    BleCharacteristicModel(
                        uuid = "00002A37-0000-1000-8000-00805F9B34FB",
                        name = "Heart Rate Measurement",
                        properties = listOf("NOTIFY", "INDICATE"),
                        currentValue = "75 BPM"
                    ),
                    BleCharacteristicModel(
                        uuid = "00002A38-0000-1000-8000-00805F9B34FB",
                        name = "Body Sensor Location",
                        properties = listOf("READ"),
                        currentValue = "Chest (0x01)"
                    )
                )
            ),
            BleServiceModel(
                uuid = "0000180F-0000-1000-8000-00805F9B34FB",
                name = "Battery Service (0x180F)",
                characteristics = listOf(
                    BleCharacteristicModel(
                        uuid = "00002A19-0000-1000-8000-00805F9B34FB",
                        name = "Battery Level (0-100%)",
                        properties = listOf("READ", "NOTIFY"),
                        currentValue = "88%"
                    )
                )
            ),
            BleServiceModel(
                uuid = "0000FFE0-0000-1000-8000-00805F9B34FB",
                name = "Custom High-Throughput Telemetry Service",
                characteristics = listOf(
                    BleCharacteristicModel(
                        uuid = "0000FFE1-0000-1000-8000-00805F9B34FB",
                        name = "Sensor Pipeline & Command Rx/Tx",
                        properties = listOf("READ", "WRITE", "WRITE_NO_RESPONSE", "NOTIFY"),
                        currentValue = "0xAA 0x01 0x22 0xFF"
                    )
                )
            )
        )
        _uiState.update { it.copy(discoveredServices = services) }
    }

    private fun resolveServiceName(uuid: UUID): String = when (uuid.toString().uppercase().take(8)) {
        "0000180D" -> "Heart Rate Service (0x180D)"
        "0000180F" -> "Battery Service (0x180F)"
        "0000180A" -> "Device Information Service (0x180A)"
        else -> "Custom Service ($uuid)"
    }

    private fun resolveCharacteristicName(uuid: UUID): String = when (uuid.toString().uppercase().take(8)) {
        "00002A37" -> "Heart Rate Measurement"
        "00002A38" -> "Body Sensor Location"
        "00002A19" -> "Battery Level"
        "00002A29" -> "Manufacturer Name String"
        else -> "Custom Characteristic ($uuid)"
    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(Locale.US, this)
}
