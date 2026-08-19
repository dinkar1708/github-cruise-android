package com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.state

import androidx.compose.runtime.Immutable
import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.model.AudioEncoderConfig
import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.model.BroadcastStatus
import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.model.StreamTelemetry
import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.model.VideoEncoderConfig

@Immutable
data class RtmpProviderPreset(
    val name: String,
    val description: String,
    val defaultUrl: String
)

val DEFAULT_RTMP_PRESETS = listOf(
    RtmpProviderPreset(
        name = "YouTube Live",
        description = "YouTube Creator Studio Ingest",
        defaultUrl = "rtmp://a.rtmp.youtube.com/live2/YOUR_STREAM_KEY"
    ),
    RtmpProviderPreset(
        name = "Cloud RTMP",
        description = "Secure RTMPS Ingest Server",
        defaultUrl = "rtmps://live.streamingserver.net/app/live_stream_key_884920"
    ),
    RtmpProviderPreset(
        name = "Twitch Live",
        description = "Twitch Ingest Server",
        defaultUrl = "rtmp://live.twitch.tv/app/YOUR_STREAM_KEY"
    ),
    RtmpProviderPreset(
        name = "Local RTMP",
        description = "Local Nginx/OBS Server (Emulator 10.0.2.2)",
        defaultUrl = "rtmp://10.0.2.2:1935/live/stream"
    )
)

/**
 * UI State for RTMP/RTMPS Live Broadcasting Screen
 */
@Immutable
data class LiveBroadcastingState(
    val status: BroadcastStatus = BroadcastStatus.IDLE,
    val rtmpEndpointUrl: String = DEFAULT_RTMP_PRESETS[0].defaultUrl,
    val selectedPresetIndex: Int = 0,
    val isFrontCamera: Boolean = true,
    val isAudioMuted: Boolean = false,
    val isAutoAdaptiveBitrateEnabled: Boolean = true,
    val videoConfig: VideoEncoderConfig = VideoEncoderConfig(),
    val audioConfig: AudioEncoderConfig = AudioEncoderConfig(),
    val telemetry: StreamTelemetry = StreamTelemetry(),
    val logMessages: List<String> = emptyList(),
    val simulatedNetworkCondition: String = "Normal (4G/5G)",
    val errorMessage: String? = null
)
