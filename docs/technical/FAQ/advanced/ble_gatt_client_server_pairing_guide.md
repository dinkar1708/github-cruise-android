# BLE (Bluetooth Low Energy) GATT Client, Server & Device Pairing Guide

**Staff-Level Architecture Guide: BluetoothGatt Client Lifecycle, BluetoothGattServer (Peripheral Mode), BluetoothLeAdvertiser, Pairing/Bonding (`createBond()`), and the Sequential Mutex Queue to prevent GATT Error 133**

📖 **Interactive Demo Reference**: [`BleGattManagerScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/advanced/ble/BleGattManagerScreen.kt)  
🧭 **Navigation Route**: `SamplesDestinations.BLE_GATT_MANAGER_ROUTE`

---

## 🧭 Executive Summary & GATT Architecture Overview

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                   ANDROID BLUETOOTH LOW ENERGY (GATT) ECOSYSTEM                        │
├───────────────────────────────┬───────────────────────────────┬────────────────────────┤
│ 1. GATT Client (Central)      │ 2. GATT Server (Peripheral)   │ 3. Security & Bonding  │
│  - BluetoothDevice.connectGatt│  - openGattServer()           │  - createBond()        │
│  - discoverServices()         │  - BluetoothLeAdvertiser      │  - SMP Security Levels │
│  - requestMtu(512)            │  - notifyCharacteristicChange│  - Key Exchange (LTK)  │
│  - CCCD (0x2902) Subscription │  - onCharacteristicRead/Write │  - Error 133 Prevention│
└───────────────────────────────┴───────────────────────────────┴────────────────────────┘
```

In Bluetooth Low Energy (BLE), devices communicate using the **Generic Attribute Profile (GATT)** hierarchy built on top of the **Attribute Protocol (ATT)**.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          GATT PROFILE HIERARCHY                             │
├─────────────────────────────────────────────────────────────────────────────┤
│ 📦 Profile (e.g. Heart Rate Profile)                                        │
│   ├── 🛠️ Service: Heart Rate Service (UUID: 0x180D)                         │
│   │     ├── 📊 Characteristic: Heart Rate Measurement (UUID: 0x2A37)       │
│   │     │     ├── Properties: NOTIFY, INDICATE                              │
│   │     │     └── 🏷️ Descriptor: CCCD (UUID: 0x2902)                        │
│   │     └── 📊 Characteristic: Body Sensor Location (UUID: 0x2A38)          │
│   │           └── Properties: READ                                          │
│   └── 🛠️ Service: Battery Service (UUID: 0x180F)                            │
│         └── 📊 Characteristic: Battery Level (UUID: 0x2A19)                 │
│               └── Properties: READ, NOTIFY                                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📡 1. GATT Client (Central Role) Implementation

A **GATT Client** initiates outgoing connections to remote BLE peripherals, discovers services, reads/writes characteristic values, and subscribes to asynchronous notifications.

### 1.1 Connecting to Peripheral & Service Discovery
```kotlin
class BleGattClientManager(private val context: Context) {

    private var bluetoothGatt: BluetoothGatt? = null

    fun connectToDevice(deviceAddress: String) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)

        // TRANSPORT_LE is critical for dual-mode devices to force BLE instead of Classic BR/EDR
        bluetoothGatt = device.connectGatt(
            context,
            false, // autoConnect = false for immediate connection attempt (< 30s timeout)
            gattCallback,
            BluetoothDevice.TRANSPORT_LE
        )
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                Timber.d("Connected to GATT Server. Requesting 512-byte MTU...")
                // Best Practice: Request higher MTU before discovering services for high throughput
                gatt.requestMtu(512)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Timber.w("Disconnected from GATT Server. Closing handle.")
                gatt.close()
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Timber.d("MTU negotiated to: $mtu bytes (Payload capacity: ${mtu - 3} bytes)")
            gatt.discoverServices()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.services.forEach { service ->
                    Timber.d("Discovered Service: ${service.uuid}")
                }
            }
        }
    }
}
```

### 1.2 Characteristic Read, Write & CCCD Notification Subscription
```kotlin
// 1. Reading Characteristic Value
fun readBatteryLevel(gatt: BluetoothGatt) {
    val service = gatt.getService(UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB"))
    val char = service?.getCharacteristic(UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB"))
    char?.let { gatt.readCharacteristic(it) }
}

// 2. Writing Characteristic (Android 13+ Tiramisu API vs Legacy)
fun writeCommand(gatt: BluetoothGatt, char: BluetoothGattCharacteristic, payload: ByteArray) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        gatt.writeCharacteristic(char, payload, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
    } else {
        @Suppress("DEPRECATION")
        char.value = payload
        @Suppress("DEPRECATION")
        gatt.writeCharacteristic(char)
    }
}

// 3. Enabling Push Notifications via CCCD (0x2902)
fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
    // Enable notifications locally in the Android framework
    gatt.setCharacteristicNotification(characteristic, true)

    // Write to peripheral's Client Characteristic Configuration Descriptor (CCCD)
    val cccdUuid = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
    val descriptor = characteristic.getDescriptor(cccdUuid)
    descriptor?.let { desc ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(desc, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            @Suppress("DEPRECATION")
            desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            @Suppress("DEPRECATION")
            gatt.writeDescriptor(desc)
        }
    }
}
```

---

## 🖥️ 2. GATT Server (Peripheral Role) & BLE Advertising

A device acting as a **GATT Server** hosts local GATT services and characteristics, broadcasts connectable advertising packets via `BluetoothLeAdvertiser`, and responds to central read/write requests.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    GATT SERVER & ADVERTISER ARCHITECTURE                    │
├────────────────────────────────┬────────────────────────────────────────────┤
│ 1. BluetoothLeAdvertiser       │ Broadcasts Service UUID & Connectable flags│
│ 2. BluetoothGattServer         │ Hosts database of Services/Characteristics │
│ 3. Read/Write Request Dispatch │ Responds to Central via sendResponse()     │
│ 4. notifyCharacteristicChanged│ Pushes real-time telemetry to Centrals     │
└────────────────────────────────┴────────────────────────────────────────────┘
```

### 2.1 Starting Server and Advertising
```kotlin
class BleGattServerManager(private val context: Context) {

    private var gattServer: BluetoothGattServer? = null
    private var advertiser: BluetoothLeAdvertiser? = null

    fun startServerAndAdvertise() {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        // 1. Open GATT Server
        gattServer = bluetoothManager.openGattServer(context, serverCallback)

        // 2. Build and Register Primary Heart Rate Service
        val hrService = BluetoothGattService(
            UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB"),
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        val hrMeasurementChar = BluetoothGattCharacteristic(
            UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB"),
            BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        val cccdDescriptor = BluetoothGattDescriptor(
            UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"),
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        hrMeasurementChar.addDescriptor(cccdDescriptor)
        hrService.addCharacteristic(hrMeasurementChar)

        gattServer?.addService(hrService)

        // 3. Start Advertising
        advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")))
            .build()

        advertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    private val serverCallback = object : BluetoothGattServerCallback() {
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            val responseValue = byteArrayOf(0x00, 75) // Flag + 75 BPM
            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, responseValue)
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
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Timber.d("BLE Advertising started successfully!")
        }
    }
}
```

---

## 🔐 3. Device Pairing & Bonding (`createBond()`)

Pairing is the process of generating shared encryption keys; bonding stores these long-term keys (LTK) permanently so subsequent connections are instantly encrypted.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       SECURITY MANAGER PROTOCOL (SMP)                       │
├─────────┬───────────────────────────────┬───────────────────────────────────┤
│ Phase 1 │ Pairing Feature Exchange      │ IO Capabilities & Auth Requirements│
├─────────┼───────────────────────────────┼───────────────────────────────────┤
│ Phase 2 │ Key Generation & Auth         │ Numeric Comparison / Passkey Entry │
├─────────┼───────────────────────────────┼───────────────────────────────────┤
│ Phase 3 │ Transport Specific Key Dist.  │ 128-bit LTK (Long-Term Key) Store │
└─────────┴───────────────────────────────┴───────────────────────────────────┘
```

### 3.1 Initiating Bond & Monitoring Intent
```kotlin
fun pairDevice(device: BluetoothDevice) {
    if (device.bondState == BluetoothDevice.BOND_NONE) {
        // Initiates SMP Pairing protocol
        device.createBond()
    }
}

// BroadcastReceiver listening for bond state changes
class BondStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
            val prevBond = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE)
            val currentBond = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)

            when (currentBond) {
                BluetoothDevice.BOND_BONDING -> Timber.d("Pairing in progress (Passkey/PIN modal active)...")
                BluetoothDevice.BOND_BONDED -> Timber.d("Device bonded successfully! LTK saved.")
                BluetoothDevice.BOND_NONE -> Timber.w("Device unbonded / pairing rejected.")
            }
        }
    }
}
```

---

## ⚠️ 4. Preventing GATT Error 133: The Sequential Operation Queue

### Root Cause of Error 133
Android's native BLE stack (Fluoride / BlueDroid) can process **only one ATT request per device at a time**. Firing simultaneous calls:
```kotlin
// ❌ WRONG: Firing parallel calls will cause silent drops or GATT Error 133
gatt.readCharacteristic(char1)
gatt.readCharacteristic(char2) // Collides on native ATT pipeline!
```

### Staff Solution: Kotlin Coroutines Mutex Queue
```kotlin
class SequentialGattQueue {
    private val mutex = Mutex()

    suspend fun <T> execute(action: suspend () -> T): T {
        return mutex.withLock {
            action()
        }
    }
}
```

---

## 🔒 5. Android 12+ (API 31+) & 14+ Bluetooth Permissions

```xml
<!-- Legacy Bluetooth permissions for Android 11 (API 30) and below -->
<uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- Android 12+ Runtime Permissions -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation"
    tools:targetApi="s" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />

<uses-feature android:name="android.hardware.bluetooth_le" android:required="false" />
```

---

## 🔗 Related References & Code

- **Interactive Screen**: [`BleGattManagerScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/advanced/BleGattManagerScreen.kt)
- **ViewModel Implementation**: [`BleGattManagerViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/advanced/BleGattManagerViewModel.kt)
- **Beacon Scanner Guide**: [`ble_beacon_scanning_guide.md`](ble_beacon_scanning_guide.md)
- **Location SDK Architecture**: [`location_sdk_data_pipeline.md`](location_sdk_data_pipeline.md)
