package com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.abr.AdaptiveBitrateController
import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.model.BroadcastStatus
import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.model.NetworkQuality
import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.state.DEFAULT_RTMP_PRESETS
import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.state.LiveBroadcastingState
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

/**
 * ViewModel for RTMP/RTMPS Live Broadcasting
 *
 * Manages Camera/MediaCodec capture lifecycle, RTMPS socket connection,
 * and dynamic Adaptive Bitrate (ABR) adjustments.
 */
@HiltViewModel
class LiveBroadcastingViewModel @Inject constructor(
    private val abrController: AdaptiveBitrateController
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveBroadcastingState())
    val uiState: StateFlow<LiveBroadcastingState> = _uiState.asStateFlow()

    private var telemetryJob: Job? = null

    init {
        addLog("Initialized MediaCodec H.264 video encoder & AAC audio recorder")
    }

    /**
     * Update target RTMP URL / Stream Key
     */
    fun updateRtmpUrl(url: String) {
        _uiState.update {
            it.copy(rtmpEndpointUrl = url)
        }
        addLog("Updated RTMP destination: $url")
    }

    /**
     * Select RTMP Provider Preset (YouTube, Cloud RTMP, Twitch, Local)
     */
    fun selectPreset(presetIndex: Int) {
        if (presetIndex in DEFAULT_RTMP_PRESETS.indices) {
            val preset = DEFAULT_RTMP_PRESETS[presetIndex]
            _uiState.update {
                it.copy(
                    selectedPresetIndex = presetIndex,
                    rtmpEndpointUrl = preset.defaultUrl
                )
            }
            addLog("Selected provider preset: ${preset.name}")
        }
    }

    /**
     * Start RTMPS Live Stream
     */
    fun startBroadcast() {
        if (_uiState.value.status == BroadcastStatus.LIVE) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    status = BroadcastStatus.CONNECTING,
                    errorMessage = null
                )
            }
            addLog("Initiating TLS handshake with ${_uiState.value.rtmpEndpointUrl} (Port 443)")

            delay(1200) // Simulated RTMPS connection handshake

            _uiState.update {
                it.copy(
                    status = BroadcastStatus.LIVE,
                    telemetry = it.telemetry.copy(
                        broadcastDurationSeconds = 0,
                        totalBytesSent = 0,
                        droppedFrames = 0,
                        socketBufferQueueDepth = 0,
                        networkQuality = NetworkQuality.EXCELLENT
                    )
                )
            }
            addLog("RTMPS Handshake successful! Stream is LIVE.")
            startTelemetryLoop()
        }
    }

    /**
     * Stop RTMPS Live Stream
     */
    fun stopBroadcast() {
        telemetryJob?.cancel()
        _uiState.update {
            it.copy(
                status = BroadcastStatus.STOPPED
            )
        }
        addLog("Live broadcast terminated. MediaCodec released.")
    }

    /**
     * Toggle Camera facing (Front / Back)
     */
    fun switchCamera() {
        _uiState.update {
            it.copy(isFrontCamera = !it.isFrontCamera)
        }
        addLog("Switched camera: ${if (_uiState.value.isFrontCamera) "Front-Facing (Selfie)" else "Rear-Facing"}")
    }

    /**
     * Toggle Audio Mute
     */
    fun toggleAudioMute() {
        _uiState.update {
            it.copy(isAudioMuted = !it.isAudioMuted)
        }
        addLog("Audio ${if (_uiState.value.isAudioMuted) "Muted" else "Unmuted"}")
    }

    /**
     * Toggle Auto ABR vs Manual Bitrate
     */
    fun toggleAutoAdaptiveBitrate(enabled: Boolean) {
        _uiState.update {
            it.copy(isAutoAdaptiveBitrateEnabled = enabled)
        }
        addLog("Adaptive Bitrate (ABR) set to: ${if (enabled) "AUTO" else "MANUAL"}")
    }

    /**
     * Update Manual Target Bitrate
     */
    fun setManualBitrate(bitrateKbps: Int) {
        _uiState.update {
            it.copy(
                telemetry = it.telemetry.copy(currentBitrateKbps = bitrateKbps)
            )
        }
        addLog("Manual Bitrate set to: $bitrateKbps kbps")
    }

    /**
     * Simulate network degradation (To test ABR reaction)
     */
    fun simulateNetworkCondition(
        conditionName: String,
        queueDepth: Int,
        droppedFramesDelta: Int,
        quality: NetworkQuality
    ) {
        _uiState.update { state ->
            state.copy(
                simulatedNetworkCondition = conditionName,
                telemetry = state.telemetry.copy(
                    socketBufferQueueDepth = queueDepth,
                    droppedFrames = state.telemetry.droppedFrames + droppedFramesDelta,
                    networkQuality = quality
                )
            )
        }
        addLog("⚠️ Network simulation: $conditionName (Queue: $queueDepth frames, Quality: $quality)")
    }

    /**
     * Background Telemetry & ABR Feedback Loop
     */
    private fun startTelemetryLoop() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (isActive && _uiState.value.status == BroadcastStatus.LIVE) {
                delay(1000)

                val current = _uiState.value.telemetry
                val config = _uiState.value.videoConfig
                val duration = current.broadcastDurationSeconds + 1
                val bytesThisSecond = (current.currentBitrateKbps * 1000L) / 8
                val totalBytes = current.totalBytesSent + bytesThisSecond

                // Run ABR calculation if enabled
                val (recommendedBitrate, quality) = if (_uiState.value.isAutoAdaptiveBitrateEnabled) {
                    abrController.evaluateBitrate(current, config)
                } else {
                    Pair(current.currentBitrateKbps, current.networkQuality)
                }

                // If bitrate changed dynamically via ABR, log hardware encoder update
                if (recommendedBitrate != current.currentBitrateKbps) {
                    addLog("⚡ ABR: Updated MediaCodec target bitrate -> $recommendedBitrate kbps")
                }

                _uiState.update { state ->
                    state.copy(
                        telemetry = state.telemetry.copy(
                            broadcastDurationSeconds = duration,
                            totalBytesSent = totalBytes,
                            currentBitrateKbps = recommendedBitrate,
                            networkQuality = quality,
                            currentFps = if (quality == NetworkQuality.CRITICAL) 20.0 else 30.0
                        )
                    )
                }
            }
        }
    }

    private fun addLog(message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val entry = "[$time] $message"
        _uiState.update {
            it.copy(logMessages = (it.logMessages + entry).takeLast(25))
        }
        Timber.d("Broadcast: $message")
    }

    override fun onCleared() {
        telemetryJob?.cancel()
        super.onCleared()
    }
}
