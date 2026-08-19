# Live Video Broadcasting Architecture & Tech Stack (Creator Ingest)

## Overview

This guide details the complete Android architecture and tech stack for **live video broadcasting (creator uplink)** under variable mobile 4G/5G network conditions, as used in production live streaming applications.

---

## 1. Complete Broadcaster Tech Stack

```
┌─────────────────────────────────────────────────────────────┐
│ 1. VIDEO CAPTURE        ➔  AndroidX CameraX 1.4.1           │
├─────────────────────────────────────────────────────────────┤
│ 2. HARDWARE ENCODING    ➔  Android MediaCodec (H.264 & AAC) │
├─────────────────────────────────────────────────────────────┤
│ 3. INGEST PROTOCOL      ➔  RTMPS over TLS (Port 443) / SRT  │
├─────────────────────────────────────────────────────────────┤
│ 4. NETWORK STABILITY    ➔  Dynamic Adaptive Bitrate (ABR)   │
├─────────────────────────────────────────────────────────────┤
│ 5. ARCHITECTURE         ➔  Jetpack Compose + Dagger-Hilt    │
└─────────────────────────────────────────────────────────────┘
```

| Layer | Technology | Role & Why It Was Selected |
| :--- | :--- | :--- |
| **Capture** | **AndroidX CameraX 1.4.1** | Lifecycle-aware camera pipeline, auto aspect-ratio, preview surface binding, and instant front/back camera flipping. |
| **Video Codec** | **Android `MediaCodec` (H.264/AVC)** | Hardware GPU accelerated encoding at 1080p @ 30 FPS, High Profile CABAC, 2s keyframe interval. Zero CPU heating. |
| **Audio Codec** | **Android `MediaCodec` (AAC-LC)** | 44.1 kHz stereo audio encoded at 128 kbps from `AudioRecord`. |
| **Uplink Transport**| **RTMPS (Port 443) / SRT** | Encrypted TLS streaming compatible with YouTube Live, Twitch, AWS IVS, Cloud RTMP, and custom Nginx/SRS servers. |
| **ABR Engine** | **Dynamic Queue-Depth ABR** | Continuously tracks TCP socket buffer backlog and adjusts encoder bitrate on-the-fly to prevent stream disconnects. |

---

## 2. Hardware Acceleration Pipeline

```
┌─────────────────────────┐          ┌───────────────────────────┐
│ AndroidX CameraX        │ ───────► │ MediaCodec (H.264/AVC)    │
│ (1080p @ 30 FPS)        │ Surface  │ Hardware Video Encoder    │
└─────────────────────────┘          └─────────────┬─────────────┘
                                                   │
                                                   ▼
┌─────────────────────────┐          ┌───────────────────────────┐          ┌─────────────────────────┐
│ AudioRecord API         │ ───────► │ MediaCodec (AAC-LC)       │ ───────► │ RTMPS Transport (TLS)   │
│ (44.1kHz Stereo PCM)    │ Buffer   │ Hardware Audio Encoder    │ FLV Mux  │ Port 443 ──► Live CDN   │
└─────────────────────────┘          └───────────────────────────┘          └─────────────────────────┘
```

- **Surface-to-Surface Zero-Copy**: CameraX writes directly to the `MediaCodec` input Surface via GPU hardware buffers without Java heap memory allocations.
- **Audio Filters**: Uses `AcousticEchoCanceler` and `NoiseSuppressor` on the `AudioRecord` session.

---

## 3. Dynamic Adaptive Bitrate (ABR) Strategy

### The Problem
When creators stream over 4G/5G, bandwidth can suddenly drop from `10 Mbps` to `500 kbps` (e.g. entering elevators). If the encoder continues producing 2.5 Mbps, packets accumulate in the TCP buffer until the socket crashes.

### The Solution: Queue-Depth Aware ABR
We monitor socket buffer queue depth in real-time and update `MediaCodec` without restarting the encoder:

```kotlin
// Dynamic bitrate adjustment without dropping the broadcast
val bundle = Bundle().apply {
    putInt(MediaCodec.PARAMETER_KEY_VIDEO_BITRATE, newBitrateBps)
}
mediaCodec.setParameters(bundle)
```

| Socket Buffer Depth | Network State | ABR Action |
| :--- | :--- | :--- |
| **0 – 2 frames** | **EXCELLENT / 5G** | Step up bitrate toward target (2,500 kbps). |
| **3 – 7 frames** | **GOOD / 4G** | Maintain stable target bitrate. |
| **8 – 15 frames** | **FAIR / Congested** | Throttle bitrate down by 20% to relieve buffer. |
| **> 18 frames** | **CRITICAL / Tunnel**| Drop bitrate to `minBitrate` (500 kbps) and drop non-keyframe buffers to preserve connection. |

---

## 4. Codebase References

### Broadcaster (Creator Uplink) Files
- **Broadcaster Screen**: [`LiveBroadcastingScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/livebroadcasting/LiveBroadcastingScreen.kt)
- **Broadcaster ViewModel**: [`LiveBroadcastingViewModel.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/livebroadcasting/LiveBroadcastingViewModel.kt)
- **ABR Controller Algorithm**: [`AdaptiveBitrateController.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/livebroadcasting/abr/AdaptiveBitrateController.kt)
- **Telemetry & Config Models**: [`BroadcastTelemetry.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/livebroadcasting/model/BroadcastTelemetry.kt)
- **Ingest State & Presets**: [`LiveBroadcastingState.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/features/livebroadcasting/state/LiveBroadcastingState.kt)

### Viewer (Audience Downlink) Files
- **Live Stream Room Screen**: [`LiveStreamRoomScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/advanced/LiveStreamRoomScreen.kt)
- **Audience Playback Guide**: [`live_stream_audience_interactions.md`](live_stream_audience_interactions.md)
