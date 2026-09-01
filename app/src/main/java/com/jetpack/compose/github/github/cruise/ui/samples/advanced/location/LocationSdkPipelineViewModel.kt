package com.jetpack.compose.github.github.cruise.ui.samples.advanced.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.location.model.LocationSdkUiState
import com.jetpack.compose.github.github.cruise.ui.samples.advanced.location.model.LocationTelemetryRecord
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
import kotlin.random.Random

@HiltViewModel
class LocationSdkPipelineViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LocationSdkUiState())
    val uiState: StateFlow<LocationSdkUiState> = _uiState.asStateFlow()

    private var trackingJob: Job? = null
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private fun addLog(message: String) {
        val timestamp = timeFormat.format(Date())
        val entry = "[$timestamp] $message"
        _uiState.update { it.copy(terminalLogs = (it.terminalLogs + entry).takeLast(60)) }
        Timber.d(entry)
    }

    fun initializeSdk() {
        addLog("⚙️ Initializing Standalone Location SDK (v2.4.0)...")
        viewModelScope.launch {
            delay(500)
            addLog("🔒 Consumer Proguard & Strict Isolation verified. Zero host-app leak.")
            addLog("🔑 Auth token validated: apiKey verified with ingestion gateway.")
            _uiState.update {
                it.copy(
                    isSdkInitialized = true,
                    terminalLogs = it.terminalLogs + "[${timeFormat.format(Date())}] ✅ SDK Initialized successfully."
                )
            }
        }
    }

    fun toggleTracking() {
        if (_uiState.value.isTrackingActive) {
            stopTracking()
        } else {
            startTracking()
        }
    }

    private fun startTracking() {
        if (!_uiState.value.isSdkInitialized) {
            addLog("⚠️ Cannot start tracking: SDK not initialized!")
            return
        }

        _uiState.update { it.copy(isTrackingActive = true) }
        addLog("▶️ Location Tracking & Adaptive Sensor Pipeline STARTED.")
        startLocationSamplingLoop()
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        _uiState.update { it.copy(isTrackingActive = false) }
        addLog("⏹️ Tracking PAUSED. GPS hardware entered deep standby.")
    }

    fun setActivityState(activity: String) {
        val (intervalSec, batteryDrain) = when (activity) {
            "STILL" -> Pair(120, 0.6)
            "WALKING" -> Pair(30, 1.8)
            "IN_VEHICLE" -> Pair(5, 4.2)
            else -> Pair(60, 1.5)
        }

        _uiState.update {
            it.copy(
                currentActivity = activity,
                currentSamplingIntervalSec = intervalSec,
                estimatedBatteryDrainPerHour = batteryDrain
            )
        }

        addLog("🔄 [Activity Recognition] State changed to '$activity'")
        addLog("🔋 [Adaptive Duty-Cycle] GPS Sampling: ${intervalSec}s | Battery: ~$batteryDrain%/hr")

        if (_uiState.value.isTrackingActive) {
            startLocationSamplingLoop()
        }
    }

    private fun startLocationSamplingLoop() {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            val baseLat = 35.6812 // Tokyo Coordinates Base
            val baseLng = 139.7671

            while (isActive) {
                val intervalMs = _uiState.value.currentSamplingIntervalSec * 1000L
                val delayTime = (intervalMs / 10).coerceIn(2000L, 5000L) // Accelerated simulation speed
                delay(delayTime)

                if (!_uiState.value.isTrackingActive) break

                val newRecord = LocationTelemetryRecord(
                    latitude = baseLat + Random.nextDouble(-0.01, 0.01),
                    longitude = baseLng + Random.nextDouble(-0.01, 0.01),
                    accuracyMeters = if (_uiState.value.currentActivity == "STILL") 15f else 5f,
                    activityType = _uiState.value.currentActivity,
                    speedKmh = when (_uiState.value.currentActivity) {
                        "STILL" -> 0.0f
                        "WALKING" -> Random.nextDouble(3.5, 5.2).toFloat()
                        else -> Random.nextDouble(40.0, 75.0).toFloat()
                    },
                    batteryPercent = Random.nextInt(75, 92)
                )

                _uiState.update { state ->
                    val updatedQueue = state.localBufferQueue + newRecord
                    state.copy(localBufferQueue = updatedQueue)
                }

                addLog("📍 [GPS Ingested] Lat: ${"%.4f".format(newRecord.latitude)}, Lng: ${"%.4f".format(newRecord.longitude)} | Buffer: ${_uiState.value.localBufferQueue.size}/5 records")

                if (_uiState.value.localBufferQueue.size >= 5) {
                    flushBatchToCloud()
                }
            }
        }
    }

    fun flushBatchToCloud() {
        val recordsToUpload = _uiState.value.localBufferQueue
        if (recordsToUpload.isEmpty() || _uiState.value.isSyncingBatch) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingBatch = true) }
            addLog("📦 [Batch Ingestion] Compressing & packaging ${recordsToUpload.size} telemetry records (GZIP JSON)...")

            delay(1000)

            addLog("☁️ [Cloud Ingestion] HTTP 200 OK: ${recordsToUpload.size} records ingested into DWH pipeline.")
            _uiState.update { state ->
                state.copy(
                    localBufferQueue = emptyList(),
                    uploadedBatchesCount = state.uploadedBatchesCount + 1,
                    totalRecordsIngested = state.totalRecordsIngested + recordsToUpload.size,
                    isSyncingBatch = false
                )
            }
        }
    }

    fun clearLogs() {
        _uiState.update { it.copy(terminalLogs = emptyList()) }
    }
}
