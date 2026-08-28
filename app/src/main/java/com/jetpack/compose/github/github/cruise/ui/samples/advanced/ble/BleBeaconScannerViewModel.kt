package com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble.model.BleBeaconItem
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.ble.model.BleScannerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

@HiltViewModel
class BleBeaconScannerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BleScannerUiState())
    val uiState: StateFlow<BleScannerUiState> = _uiState.asStateFlow()

    private var scannerJob: Job? = null
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private fun addLog(message: String) {
        val timestamp = timeFormat.format(Date())
        val entry = "[$timestamp] $message"
        _uiState.update { it.copy(terminalLogs = (it.terminalLogs + entry).takeLast(60)) }
        Timber.d(entry)
    }

    fun toggleScanner() {
        if (_uiState.value.isScanning) {
            stopScan()
        } else {
            startScan()
        }
    }

    fun setScanMode(mode: String) {
        _uiState.update { it.copy(scanMode = mode) }
        addLog("⚙️ [ScanSettings] ScanMode changed to '$mode'")
        if (_uiState.value.isScanning) {
            startScan()
        }
    }

    fun toggleKalmanFilter() {
        val next = !_uiState.value.isKalmanFilterEnabled
        _uiState.update { it.copy(isKalmanFilterEnabled = next) }
        addLog("📐 [Signal Processing] Low-Pass Kalman Filter ${if (next) "ENABLED (Smooth distance)" else "DISABLED (Raw noisy RSSI)"}")
    }

    private fun startScan() {
        scannerJob?.cancel()
        _uiState.update { it.copy(isScanning = true) }
        addLog("📡 [BluetoothLeScanner] Started scanning with ${_uiState.value.scanMode}...")

        scannerJob = viewModelScope.launch {
            val sampleBeacons = listOf(
                Pair("E2C56DB5-DFFB-48D2-B060-D0F5A71096E0", "iBeacon"),
                Pair("FDA50693-A4E2-4FB1-AFCF-C6EB07647825", "iBeacon"),
                Pair("BEAC0123-4567-89AB-CDEF-0123456789AB", "AltBeacon"),
                Pair("EDD10000-0000-0000-0000-000000000001", "Eddystone-UID")
            )

            val delayInterval = when (_uiState.value.scanMode) {
                "SCAN_MODE_LOW_LATENCY" -> 1000L
                "SCAN_MODE_BALANCED" -> 2500L
                else -> 5000L
            }

            while (isActive) {
                delay(delayInterval)

                val count = Random.nextInt(1, sampleBeacons.size + 1)
                val updatedList = (0 until count).map { index ->
                    val (uuid, type) = sampleBeacons[index]
                    val rawRssi = Random.nextInt(-85, -45)
                    val txPower = if (type == "AltBeacon") -65 else -59

                    // Path Loss: d = 10 ^ ((Tx - RSSI) / (10 * N))
                    val pathExponent = 2.0
                    val ratio = (txPower - rawRssi) / (10.0 * pathExponent)
                    val rawDistance = 10.0.pow(ratio)

                    val finalDistance = if (_uiState.value.isKalmanFilterEnabled) {
                        (rawDistance * 100.0).roundToInt() / 100.0
                    } else {
                        ((rawDistance + Random.nextDouble(-0.8, 0.8)).coerceAtLeast(0.1) * 100.0).roundToInt() / 100.0
                    }

                    val proximity = when {
                        finalDistance < 1.0 -> "Immediate (<1m)"
                        finalDistance < 3.0 -> "Near (1-3m)"
                        else -> "Far (>3m)"
                    }

                    BleBeaconItem(
                        frameType = type,
                        uuid = uuid,
                        major = 100 + index,
                        minor = 200 + index,
                        rssi = rawRssi,
                        txPower = txPower,
                        estimatedDistanceMeters = finalDistance,
                        proximityZone = proximity,
                        packetsCount = Random.nextInt(5, 45)
                    )
                }

                _uiState.update { state ->
                    state.copy(
                        discoveredBeacons = updatedList,
                        totalPacketsCaptured = state.totalPacketsCaptured + updatedList.size
                    )
                }

                addLog("📡 [ScanResult] Discovered ${updatedList.size} beacon frames. Top RSSI: ${updatedList.maxByOrNull { it.rssi }?.rssi} dBm")
            }
        }
    }

    private fun stopScan() {
        scannerJob?.cancel()
        _uiState.update { it.copy(isScanning = false) }
        addLog("⏹️ [BluetoothLeScanner] Scan STOPPED. Radio returned to idle state.")
    }

    fun clearLogs() {
        _uiState.update { it.copy(terminalLogs = emptyList()) }
    }
}
