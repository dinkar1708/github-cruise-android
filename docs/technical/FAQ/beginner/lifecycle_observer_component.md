# LifecycleEventObserver in Jetpack Compose: Hardware & Battery Safety

## Purpose

This guide explains how to use **`LocalLifecycleOwner.current`**, **`LifecycleEventObserver`**, and **`DisposableEffect`** inside Jetpack Compose to build self-contained, lifecycle-aware components. This pattern is essential for managing hardware sensors (Accelerometer, Gyroscope), CameraX previews, GPS tracking, and video playback without leaking memory or draining battery in the background.

---

## 🔗 Code Implementation Reference

* **Interactive Demo Screen**: [`LifecycleObserverExampleScreen.kt`](../../../../app/src/main/java/com/jetpack/compose/github/github/cruise/ui/samples/beginner/LifecycleObserverExampleScreen.kt)
* **Sample Navigation Route**: `SamplesDestinations.LIFECYCLE_OBSERVER_ROUTE`

---

## ❓ The Problem: Why Composables Need Lifecycle Observers

In Jetpack Compose, Composables are declarative UI functions. They do **NOT** have native `onResume()` or `onPause()` callbacks like legacy Android Activities.

### ⚠️ What Happens WITHOUT Lifecycle Observation?
```kotlin
// ❌ WRONG: Registering hardware without lifecycle awareness
@Composable
fun BadSensorComponent(context: Context = LocalContext.current) {
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    // This listener runs FOREVER even when the phone is locked in a pocket!
    // 🔋 Battery drains in hours, phone overheats!
    sensorManager.registerListener(myListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
}
```

---

## 🛡️ The Solution: The LifecycleEventObserver + DisposableEffect Pattern

```kotlin
@Composable
fun LifecycleAwareSensorComponent(context: Context = LocalContext.current) {
    // 1. Get the current screen's LifecycleOwner
    val lifecycleOwner = LocalLifecycleOwner.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                // Process sensor coordinates: X, Y, Z
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
    }

    // 2. Bind hardware lifecycle to this Composable
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                // 🟢 Screen is active & in foreground: TURN SENSOR ON!
                Lifecycle.Event.ON_RESUME -> {
                    sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
                }

                // 🛑 User minimized app / locked phone: TURN SENSOR OFF (Save battery!)
                Lifecycle.Event.ON_PAUSE -> {
                    sensorManager.unregisterListener(sensorListener)
                }
                else -> Unit
            }
        }

        // 3. Attach observer to the LifecycleOwner
        lifecycleOwner.lifecycle.addObserver(observer)

        // 4. 🧹 MANDATORY CLEANUP: Remove observer & unregister when leaving screen
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            sensorManager.unregisterListener(sensorListener)
        }
    }

    // Composable UI
    Text("Sensor active only while viewing this screen!")
}
```

---

## 📊 Lifecycle Event Execution Flow

```
====================================================================================================
  USER ACTION                      LIFECYCLE EVENT        COMPONENT REACTION
====================================================================================================

  1. 📱 User opens screen          🟢 ON_RESUME           ▶️ Registers Accelerometer listener
                                                             (Hardware ACTIVE)

  2. 🏠 User presses Home button   ⏸️ ON_PAUSE            🛑 Unregisters Accelerometer listener
     (Phone locked in pocket)                                (Hardware PAUSED - 0% Battery Waste!)

  3. 📱 User returns from Recents  🟢 ON_RESUME           🔁 Re-registers Accelerometer listener
                                                             (Hardware RESUMES)

  4. 🔙 User presses Back button   🧹 onDispose           🗑️ Removes observer & unregisters listener
     (Leaves screen permanently)                             (ZERO memory leaks!)
====================================================================================================
```

---

## 🎯 Top Real-World Use Cases

1. **📱 Hardware Sensors:** Accelerometer, Gyroscope, Magnetometer, Step Counter.
2. **📹 CameraX / QR Code Scanners:** Starting camera preview on `ON_RESUME` and releasing the camera lens on `ON_PAUSE`.
3. **🎬 Native Video Players (ExoPlayer / Media3):** Pausing playback decoders on `ON_PAUSE`.
4. **📍 GPS Location Tracking:** Unregistering fine location polling when the screen is obscured.

---

## 🎙️ Staff Interview Pitch (15 Seconds)

> *"In Jetpack Compose, UI functions lack native lifecycle callbacks.  
> To prevent background battery drain when using hardware sensors or CameraX, we encapsulate lifecycle management inside **`DisposableEffect(lifecycleOwner)`** with a **`LifecycleEventObserver`**.  
> We register listeners on **`ON_RESUME`**, unregister on **`ON_PAUSE`** to protect battery, and remove observers in **`onDispose`** to guarantee zero memory leaks."*
