# Low Memory Callback Testing Guide

## Overview

The Multi-Tab Feed implementation includes low memory callbacks that automatically clear image caches when Android detects memory pressure to avoid Out-Of-Memory (OOM) situations.

## Implementation Details

### ComponentCallbacks2 Integration

The app registers a `ComponentCallbacks2` listener that responds to:

1. **`onLowMemory()`** - System critically low on memory
2. **`onTrimMemory(level)`** - Various memory pressure levels

### Memory Clearing Strategy

| Memory Level | Action | Caches Cleared |
|-------------|--------|----------------|
| **RUNNING_MODERATE** | Clear L1 only | Memory cache |
| **RUNNING_LOW** | Clear L1 only | Memory cache |
| **RUNNING_CRITICAL** | Clear L1 + L2 + GC | Memory + Disk caches |
| **UI_HIDDEN** | Clear L1 + L2 + GC | Memory + Disk caches |
| **BACKGROUND** | Clear L1 only | Memory cache |
| **MODERATE** | Clear L1 only | Memory cache |
| **COMPLETE** | Clear L1 + L2 + GC | Memory + Disk caches |

## How to Test Low Memory Callbacks

### Method 1: ADB Command (Recommended)

The easiest way to trigger low memory callbacks is using ADB:

```bash
# 1. Make sure app is running on device/emulator
# 2. Find your app's package name
adb shell dumpsys package com.jetpack.compose.github.github.cruise | grep userId

# 3. Trigger low memory warning
adb shell am send-trim-memory com.jetpack.compose.github.github.cruise RUNNING_CRITICAL

# Available memory levels:
# - RUNNING_MODERATE
# - RUNNING_LOW
# - RUNNING_CRITICAL
# - UI_HIDDEN
# - BACKGROUND
# - MODERATE
# - COMPLETE
```

### Method 2: Android Studio Profiler

1. Open Android Studio
2. Run the app
3. Go to **View → Tool Windows → Profiler**
4. Click the app process
5. In the Memory profiler, click the **garbage can icon** to force GC
6. Watch Logcat for memory callbacks

### Method 3: Simulate in Device Settings

1. Enable **Developer Options** on your device
2. Go to **Settings → Developer Options**
3. Find **Don't keep activities** and enable it
4. Navigate away from the app
5. Return to the app - should trigger `TRIM_MEMORY_UI_HIDDEN`

### Method 4: Programmatic Trigger (For Testing)

Add a test button to trigger memory callbacks manually:

```kotlin
Button(onClick = {
    // Simulate low memory
    context.applicationContext.apply {
        val callback = object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) {}
            override fun onLowMemory() {}
            override fun onTrimMemory(level: Int) {
                // Process manually
            }
        }
        callback.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
    }
}) {
    Text("Trigger Low Memory")
}
```

## What to Look For in Logcat

Filter Logcat by `MEMORY` or `LOW MEMORY` to see the callbacks:

### Example Log Output

```
W/Timber: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
W/Timber: 🚨 MEMORY PRESSURE DETECTED: RUNNING_CRITICAL
W/Timber: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
I/Timber: 📊 Current Cache State:
I/Timber:    L1 (Memory): 45MB
I/Timber:    L2 (Disk): 12MB
W/Timber: 🧹🧹 AGGRESSIVE CLEANUP: Clearing L1 + L2 Caches...
I/Timber: ✅ L1 Memory Cache cleared
I/Timber: ✅ L2 Disk Cache cleared
I/Timber: ✅ Garbage collection requested
I/Timber: 📊 Memory Stats After Cleanup:
I/Timber:    Used: 128MB / 512MB
I/Timber:    Available: 384MB
W/Timber: ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### Different Memory Levels

```
# RUNNING_MODERATE (Green)
🟢 TRIM MEMORY: RUNNING_MODERATE - App in foreground, memory getting low
🧹 Clearing L1 Memory Cache...

# RUNNING_LOW (Yellow)
🟡 TRIM MEMORY: RUNNING_LOW - App in foreground, memory low
🧹 Clearing L1 Memory Cache...

# RUNNING_CRITICAL (Red)
🔴 TRIM MEMORY: RUNNING_CRITICAL - App in foreground, memory critically low!
🧹🧹 AGGRESSIVE CLEANUP: Clearing L1 + L2 Caches...

# COMPLETE (Red)
🔴 TRIM MEMORY: COMPLETE - System extremely low on memory, app may be killed!
🧹🧹 AGGRESSIVE CLEANUP: Clearing L1 + L2 Caches...
```

## Testing Workflow

### Step-by-Step Test

1. **Start the app** and navigate to Multi-Tab Feed screen
2. **Scroll through tabs** to load images into L1 and L2 caches
3. **Check Logcat** - you should see images loading from Network (🔴)
4. **Scroll back** - images now load from L1 cache (🟢)
5. **Trigger low memory:**
   ```bash
   adb shell am send-trim-memory com.jetpack.compose.github.github.cruise RUNNING_CRITICAL
   ```
6. **Watch Logcat** for memory pressure logs
7. **Scroll tabs again** - images now reload from Network (🔴) since caches were cleared
8. **Test different levels:**
   ```bash
   # Moderate cleanup (L1 only)
   adb shell am send-trim-memory com.jetpack.compose.github.github.cruise RUNNING_MODERATE

   # Critical cleanup (L1 + L2)
   adb shell am send-trim-memory com.jetpack.compose.github.github.cruise RUNNING_CRITICAL
   ```

## Verification Checklist

- [ ] Logcat shows memory pressure detection
- [ ] Cache sizes are logged before clearing
- [ ] Appropriate caches cleared based on level
- [ ] Memory stats logged after cleanup
- [ ] Images reload from network after cache clear
- [ ] No crashes or errors during cleanup
- [ ] App continues to function normally

## Production Considerations

### What Happens in Production?

1. **User scrolls through tabs** → Images cached to L1 + L2
2. **System low on memory** → Android calls `onTrimMemory()`
3. **App clears caches** → Frees up memory immediately
4. **User continues using app** → Images re-downloaded as needed
5. **No data loss** → In production, Room SQLite retains article data

### Why Memory Pressure Management Matters

> *"When `onTrimMemory(TRIM_MEMORY_RUNNING_CRITICAL)` is received, repositories should immediately clear in-memory L1 caches for all non-active tabs. Because all feed data and scroll coordinates are safely stored in Room SQLite, the user experiences zero data loss when returning to those tabs."*

**Key Benefits:**
- ✅ Prevents OOM crashes
- ✅ Responsive to Android system requests
- ✅ No data loss (Room SQLite persists everything)
- ✅ Graceful degradation under memory pressure
- ✅ Better user experience on low-memory devices

## ADB Commands Reference

```bash
# Trigger different memory levels
adb shell am send-trim-memory <package> <level>

# Examples:
adb shell am send-trim-memory com.jetpack.compose.github.github.cruise RUNNING_MODERATE
adb shell am send-trim-memory com.jetpack.compose.github.github.cruise RUNNING_LOW
adb shell am send-trim-memory com.jetpack.compose.github.github.cruise RUNNING_CRITICAL
adb shell am send-trim-memory com.jetpack.compose.github.github.cruise COMPLETE

# Check app memory usage
adb shell dumpsys meminfo com.jetpack.compose.github.github.cruise

# Monitor memory in real-time
adb shell top | grep com.jetpack.compose.github.github.cruise
```

## Troubleshooting

### Not seeing logs?

1. Make sure you're running a **Debug build** (Timber only logs in debug)
2. Check Logcat filter - use tag `Timber` or search for `MEMORY`
3. Verify the package name is correct in ADB command

### Cache not clearing?

1. Verify the callback is registered (check logs at app start)
2. Try a higher severity level (RUNNING_CRITICAL or COMPLETE)
3. Check that Coil ImageLoader is properly configured

### App crashes on low memory?

1. Check if there are null pointer exceptions
2. Verify cache?.clear() calls are safe
3. Look for unhandled exceptions in Logcat

---

## Related Files

- `MultiTabFeedSampleScreen.kt:57` - ComponentCallbacks2 registration
- `MultiTabFeedSampleScreen.kt:204` - handleMemoryPressure function
- `ImageLoadingModule.kt:1` - Coil L1/L2 cache configuration
- `NetworkImageView.kt:1` - Cache indicator badges

---

**Status:** ✅ Fully implemented and tested
