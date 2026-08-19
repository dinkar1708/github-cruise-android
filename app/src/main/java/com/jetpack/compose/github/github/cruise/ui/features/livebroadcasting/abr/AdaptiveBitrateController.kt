package com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.abr

import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.model.NetworkQuality
import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.model.StreamTelemetry
import com.jetpack.compose.github.github.cruise.ui.features.livebroadcasting.model.VideoEncoderConfig
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adaptive Bitrate (ABR) Controller for RTMP/RTMPS Streaming
 *
 * Prevents buffer overflow and socket disconnects under variable cellular network conditions
 * by dynamically adjusting hardware encoder bitrate in real-time.
 */
@Singleton
class AdaptiveBitrateController @Inject constructor() {

    companion object {
        private const val QUEUE_DEPTH_CRITICAL = 18
        private const val QUEUE_DEPTH_CONGESTED = 8
        private const val QUEUE_DEPTH_HEALTHY = 2

        private const val RTT_HIGH_MS = 250
        private const val RTT_CRITICAL_MS = 450
    }

    /**
     * Calculates the next recommended encoder bitrate based on socket queue depth & latency
     *
     * @param telemetry Current telemetry metrics
     * @param config Encoder limits
     * @return Pair of recommended bitrate in kbps and calculated NetworkQuality
     */
    fun evaluateBitrate(
        telemetry: StreamTelemetry,
        config: VideoEncoderConfig
    ): Pair<Int, NetworkQuality> {
        val currentBitrate = telemetry.currentBitrateKbps
        val queueDepth = telemetry.socketBufferQueueDepth
        val rtt = telemetry.roundTripTimeMs

        return when {
            // Case 1: Severe Network Congestion / Buffer Bloat
            queueDepth >= QUEUE_DEPTH_CRITICAL || rtt >= RTT_CRITICAL_MS -> {
                val newBitrate = (currentBitrate * 0.65).toInt().coerceAtLeast(config.minBitrateKbps)
                Timber.w("ABR: Critical network congestion (Queue=$queueDepth, RTT=${rtt}ms). Dropping bitrate $currentBitrate -> $newBitrate kbps")
                Pair(newBitrate, NetworkQuality.CRITICAL)
            }

            // Case 2: Moderate Congestion
            queueDepth >= QUEUE_DEPTH_CONGESTED || rtt >= RTT_HIGH_MS -> {
                val newBitrate = (currentBitrate * 0.85).toInt().coerceAtLeast(config.minBitrateKbps)
                Timber.d("ABR: Network congested. Decreasing bitrate $currentBitrate -> $newBitrate kbps")
                Pair(newBitrate, NetworkQuality.POOR)
            }

            // Case 3: Minor Backlog
            queueDepth > QUEUE_DEPTH_HEALTHY -> {
                Pair(currentBitrate, NetworkQuality.FAIR)
            }

            // Case 4: Stable & Clear Connection -> Probe upward gradually
            else -> {
                val newBitrate = if (currentBitrate < config.targetBitrateKbps) {
                    (currentBitrate + 150).coerceAtMost(config.targetBitrateKbps)
                } else {
                    currentBitrate
                }
                val quality = if (currentBitrate >= 2000) NetworkQuality.EXCELLENT else NetworkQuality.GOOD
                Pair(newBitrate, quality)
            }
        }
    }
}
