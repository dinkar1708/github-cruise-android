# Live Stream Architecture: Broadcaster Uplink and Audience Playback

## Overview

This document details the complete end-to-end live streaming architecture, connecting the **Broadcaster (Creator Uplink)** with the **Audience Stream Room (Viewer Downlink)**, including real-time video transport, adaptive bitrate, and interactive overlay systems.

---

## 1. End-to-End Live Stream Architecture

```
1. Broadcaster (Creator Uplink)
CameraX + MediaCodec (H.264/AAC)
      │
      ▼  RTMPS (Port 443 / TLS)
2. Live Media Server & Cloud CDN
Transcoding to LL-HLS (.m3u8) / DASH (.mpd)
      │
      ▼  HTTPS Edge Delivery
3. Audience Room (Viewer Downlink)
AndroidX Media3 ExoPlayer + Compose Canvas Interactions
```

---

## 2. Technology Stack Comparison

| Component | Broadcaster (Creator Uplink) | Audience Room (Viewer Downlink) |
| :--- | :--- | :--- |
| **Primary Role** | Video capture and live encoding | Video playback and live interaction |
| **Android Framework** | AndroidX CameraX 1.4.1 | AndroidX Media3 1.4.1 (ExoPlayer) |
| **Encoding / Decoding**| Hardware MediaCodec (H.264/AVC) | Hardware GPU video decoder |
| **Audio Pipeline** | AudioRecord + MediaCodec (AAC-LC) | ExoPlayer audio track sink |
| **Transport Protocol** | RTMPS (Port 443 with TLS 1.3) / SRT | LL-HLS (.m3u8), MP4, DASH (.mpd) |
| **Network Resilience** | Dynamic Queue-Depth ABR Controller | Buffer chunk caching & edge seeking |
| **Interactive UI** | Viewfinder, flip camera, telemetry | 120 FPS floating hearts, live chat, gifts |

---

## 3. Codebase File References

### Broadcaster (Creator Uplink) Files
- Broadcaster Screen: [`LiveBroadcastingScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/livebroadcasting/LiveBroadcastingScreen.kt)
- Broadcaster ViewModel: [`LiveBroadcastingViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/livebroadcasting/LiveBroadcastingViewModel.kt)
- Adaptive Bitrate Algorithm: [`AdaptiveBitrateController.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/livebroadcasting/abr/AdaptiveBitrateController.kt)
- Telemetry and Encoder Specs: [`BroadcastTelemetry.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/livebroadcasting/model/BroadcastTelemetry.kt)
- Ingest UI State and Presets: [`LiveBroadcastingState.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/livebroadcasting/state/LiveBroadcastingState.kt)
- Technical Guide: [`rtmp_live_broadcasting.md`](rtmp_live_broadcasting.md)

### Viewer (Audience Downlink) Files
- Live Stream Room Screen: [`LiveStreamRoomScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/advanced/LiveStreamRoomScreen.kt)
- Samples Sub-Graph Navigation: [`SamplesNavGraph.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/SamplesNavGraph.kt)
- Samples Hub Screen: [`SamplesListScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/SamplesListScreen.kt)
- Main Navigation Graph: [`NavGraph.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/NavGraph.kt)

---

## 4. Key Implementation Patterns

### A. Broadcaster ABR Bitrate Adjustment
```kotlin
// Dynamically adjusts MediaCodec target bitrate without re-initializing encoder
val bundle = Bundle().apply {
    putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, newTargetBitrateBps)
}
mediaCodec.setParameters(bundle)
```

### B. Viewer Media3 ExoPlayer Setup
```kotlin
// Configures ExoPlayer with cross-protocol redirect support for CDN streams
val httpDataSourceFactory = DefaultHttpDataSource.Factory()
    .setAllowCrossProtocolRedirects(true)
    .setConnectTimeoutMs(20000)
    .setReadTimeoutMs(20000)
    .setUserAgent("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")

val mediaSourceFactory = DefaultMediaSourceFactory(context)
    .setDataSourceFactory(httpDataSourceFactory)

val exoPlayer = ExoPlayer.Builder(context)
    .setMediaSourceFactory(mediaSourceFactory)
    .build()
```
