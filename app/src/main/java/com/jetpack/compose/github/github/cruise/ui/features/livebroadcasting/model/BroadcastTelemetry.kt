package com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.model

import androidx.compose.runtime.Immutable

/**
 * Network connection quality for live broadcasting
 */
enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    CRITICAL
}

/**
 * Live broadcasting lifecycle state
 */
enum class BroadcastStatus {
    IDLE,
    CONNECTING,
    LIVE,
    RECONNECTING,
    STOPPED,
    ERROR
}

/**
 * Hardware video encoding configuration (MediaCodec H.264/AVC)
 */
@Immutable
data class VideoEncoderConfig(
    val codec: String = "video/avc (H.264)",
    val width: Int = 1080,
    val height: Int = 1920,
    val targetFps: Int = 30,
    val targetBitrateKbps: Int = 2500,
    val minBitrateKbps: Int = 500,
    val maxBitrateKbps: Int = 4500,
    val keyframeIntervalSeconds: Int = 2,
    val profile: String = "High Profile (CABAC)"
)

/**
 * Audio encoding configuration (AudioRecord + MediaCodec AAC)
 */
@Immutable
data class AudioEncoderConfig(
    val codec: String = "audio/mp4a-latm (AAC-LC)",
    val sampleRate: Int = 44100,
    val channels: Int = 2,
    val bitrateKbps: Int = 128
)

/**
 * Real-time stream telemetry metrics for Adaptive Bitrate (ABR)
 */
@Immutable
data class StreamTelemetry(
    val currentBitrateKbps: Int = 2500,
    val currentFps: Double = 30.0,
    val droppedFrames: Long = 0,
    val socketBufferQueueDepth: Int = 0, // Number of packets queued waiting to be flushed
    val roundTripTimeMs: Long = 45,
    val networkQuality: NetworkQuality = NetworkQuality.EXCELLENT,
    val totalBytesSent: Long = 0,
    val broadcastDurationSeconds: Long = 0
)
