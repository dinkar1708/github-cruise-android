# BLE (Bluetooth Low Energy) Beacon Scanning & Frame Decoding Guide

**Staff-Level Architecture Guide: BluetoothLeScanner, iBeacon/AltBeacon Binary Frame Decoding, RSSI Path-Loss Math, and Android 12+/14+ Permissions**

📖 **Interactive Demo Reference**: [`BleBeaconScannerScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/advanced/ble/BleBeaconScannerScreen.kt)  
🧭 **Navigation Route**: `SamplesDestinations.BLE_BEACON_SCANNER_ROUTE`

---

## 🧭 Executive Summary

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    BLE BEACON SCANNING & SIGNAL PIPELINE                    │
├─────────────────────────┬─────────────────────────┬─────────────────────────┤
│ 1. BluetoothLeScanner   │ 2. Binary Frame Parser  │ 3. Path Loss & Kalman   │
│  - ScanSettings Modes   │  - 0x004C Apple iBeacon │  - d = 10^((Tx-RSSI)/20)│
│  - ScanFilters (UUID)   │  - Major, Minor, TxPower│  - Proximity Zones      │
└─────────────────────────┴─────────────────────────┴─────────────────────────┘
```

---

## 📡 1. BLE Advertising Packets & Binary Frame Decoding

BLE Beacons broadcast continuous non-connectable advertising packets containing identification and transmit power calibration.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    STANDARD iBEACON ADVERTISING PACKET                      │
├───────────────┬──────────────┬──────────────────┬──────────┬────────┬───────┤
│ Company ID    │ Beacon Type  │ Proximity UUID   │ Major    │ Minor  │ TxPwr │
│ (0x004C - 2B) │ (0x0215 - 2B)│ (128-bit - 16B)  │ (16-bit) │(16-bit)│ (1B)  │
└───────────────┴──────────────┴──────────────────┴──────────┴────────┴───────┘
```

### 1. BluetoothLeScanner Frame Decoding Implementation
```kotlin
internal class BleBeaconParser {

    fun parseScanRecord(scanRecordBytes: ByteArray?, rssi: Int): BleBeaconItem? {
        if (scanRecordBytes == null || scanRecordBytes.size < 30) return null

        // 1. Verify Manufacturer Data Header (iBeacon: 0x004C Company ID + 0x0215 Type)
        var startByte = 2
        while (startByte <= 5) {
            if ((scanRecordBytes[startByte + 2].toInt() and 0xff == 0x02) &&
                (scanRecordBytes[startByte + 3].toInt() and 0xff == 0x15)
            ) {
                // 2. Extract 16-byte UUID
                val uuidBytes = scanRecordBytes.copyOfRange(startByte + 4, startByte + 20)
                val uuid = formatUuid(uuidBytes)

                // 3. Extract Major & Minor (Big Endian 16-bit ints)
                val major = (scanRecordBytes[startByte + 20].toInt() and 0xff shl 8) or
                            (scanRecordBytes[startByte + 21].toInt() and 0xff)
                val minor = (scanRecordBytes[startByte + 22].toInt() and 0xff shl 8) or
                            (scanRecordBytes[startByte + 23].toInt() and 0xff)

                // 4. Extract Measured TxPower at 1 meter
                val txPower = scanRecordBytes[startByte + 24].toInt()

                // 5. Calculate Estimated Distance via Path Loss model
                val distance = calculateDistance(txPower, rssi)

                return BleBeaconItem(
                    frameType = "iBeacon",
                    uuid = uuid,
                    major = major,
                    minor = minor,
                    rssi = rssi,
                    txPower = txPower,
                    estimatedDistanceMeters = distance,
                    proximityZone = classifyProximity(distance)
                )
            }
            startByte++
        }
        return null
    }

    /**
     * Path Loss Distance Calculation:
     * d = 10 ^ ((TxPower - RSSI) / (10 * N)) where N = 2.0 (free-space path loss exponent)
     */
    private fun calculateDistance(txPower: Int, rssi: Int, pathLossExponent: Double = 2.0): Double {
        if (rssi == 0) return -1.0
        val ratio = (txPower - rssi) / (10.0 * pathLossExponent)
        return (10.0.pow(ratio) * 100.0).roundToInt() / 100.0
    }
}
```

---

## 📐 2. Signal Processing & Noise Reduction (Kalman Filter)

Raw RSSI signals from BLE beacons fluctuate wildly ($\pm 15\text{ dBm}$) due to multipath RF reflections and human body obstruction.

* **Low-Pass Kalman Filter:** A 1D Kalman state estimator smooths raw RSSI readings to produce stable distance estimates without lag.
* **Proximity Zones:**
  - **Immediate ($< 1\text{m}$):** High certainty of direct interaction (e.g. at a cashier register or display kiosk).
  - **Near ($1 - 3\text{m}$):** In the same room or aisle.
  - **Far ($> 3\text{m}$):** Peripheral detection.

---

## 🔒 3. Android 12+ (API 31+) & 14+ Bluetooth Permissions

```xml
<!-- Android 12+ Runtime Permissions -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- Required for beacon distance triangulation & background discovery -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
```

---

## 🔗 Related References & Code

- **Interactive Screen**: [`BleBeaconScannerScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/advanced/BleBeaconScannerScreen.kt)
- **ViewModel Implementation**: [`BleBeaconScannerViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/advanced/BleBeaconScannerViewModel.kt)
- **Location SDK Guide**: [`location_sdk_data_pipeline.md`](location_sdk_data_pipeline.md)
