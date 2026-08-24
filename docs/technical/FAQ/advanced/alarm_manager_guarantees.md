# AlarmManager Timing Guarantees, Doze Mode & Exact Alarms

## Purpose

A comprehensive **Staff Android Engineering Guide** on **`AlarmManager`** timing guarantees in Android. Clarifies when alarms are exact vs inexact, how **Doze Mode** and **App Standby Buckets** defer execution, **Android 12+ Exact Alarm Permissions**, and the architectural decision matrix between **`AlarmManager`** and **`WorkManager`**.

---

## 🛑 Quick Answer: Is `AlarmManager` Guaranteed to Execute on Time?

| Alarm Type | Guaranteed on Exact Time? | Survives Doze Mode? | Permissions Required | Best Use Case |
| :--- | :--- | :--- | :--- | :--- |
| **`set()` / `setRepeating()`** | ❌ **NO (Inexact / Batched)** | ❌ **No (Deferred)** | None | General non-critical triggers |
| **`setExact()`** | ⚠️ **YES, but NOT in Doze** | ❌ **No (Deferred in Doze)** | `SCHEDULE_EXACT_ALARM` (Android 12+) | Exact timing while device is active |
| **`setExactAndAllowWhileIdle()`** | ✅ **YES (Within 9-min window)** | ✅ **YES** | `SCHEDULE_EXACT_ALARM` (Android 12+) | Calendar alerts, meeting notifications |
| **`setAlarmClock()`** | 🎯 **100% GUARANTEED EXACT** | ✅ **YES (Highest Priority)** | `USE_EXACT_ALARM` or `SCHEDULE_EXACT_ALARM` | Wake-up alarm clocks, timers |

---

## 🔋 Why Android Defers Alarms (The 4 System Barriers)

```
┌──────────────────────────────────────────────────────────────────────────────────────────┐
│                             SYSTEM BARRIERS TO EXACT TIMING                              │
│                                                                                          │
│ 1. 📦 Alarm Batching (API 19+)       ➔ OS shifts alarms by minutes to batch CPU wakeups │
│ 2. 💤 Doze Mode (API 23+)            ➔ Defers all standard alarms until maintenance window│
│ 3. 🪣 App Standby Buckets (API 28+)  ➔ Rare/Restricted apps get delayed alarm frequency  │
│ 4. 🔒 Android 12+ Permission Check   ➔ Requires user-granted exact alarm permission      │
└──────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🛠️ How to Schedule True Exact Alarms in Modern Android

### 1. Declare Permissions in `AndroidManifest.xml` (Android 12+ / API 31+)

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- For general apps requiring exact alarms (e.g. reminders, tasks) -->
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />

    <!-- ONLY for Clock/Timer/Calendar apps (Auto-granted, strictly enforced on Google Play) -->
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />

</manifest>
```

---

### 2. Check Permission at Runtime before Calling `setExact()`

Calling exact alarm APIs without checking permission throws a `SecurityException` on Android 12+:

```kotlin
class ExactAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleExactReminder(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        // 🔒 Android 12+ Permission Check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Prompt user to grant permission in Settings:
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            }
        }

        // 🎯 Schedules exact alarm that fires even in Doze Mode
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}
```

---

### 3. The 100% Guaranteed Alarm: `setAlarmClock()`

For critical alarms (like a morning 7:00 AM wake-up clock or medicine alert), use `setAlarmClock()`. It **bypasses Doze Mode completely** and places an alarm icon in the user's status bar:

```kotlin
fun scheduleWakeUpAlarm(triggerAtMillis: Long, showIntent: PendingIntent, operation: PendingIntent) {
    val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent)
    
    // ⏰ Highest priority in Android OS - NEVER deferred by Doze Mode!
    alarmManager.setAlarmClock(alarmClockInfo, operation)
}
```

---

## ⚖️ `AlarmManager` vs `WorkManager`: When to Use Which?

```
                                  DO YOU NEED AN EXACT CLOCK TIME?
                                 (e.g., Exactly 07:00 AM, 14:30 PM)
                                            /           \
                                         YES             NO
                                         /                 \
                     Use AlarmManager                       Use WorkManager
                     • setAlarmClock()                      • OneTimeWorkRequest
                     • setExactAndAllowWhileIdle()          • PeriodicWorkRequest
                     (Wake-up alarms, Reminders)            (Sync DB, Upload logs, Prefetch)
```

| Feature | ⏰ `AlarmManager` | ⚙️ `WorkManager` |
| :--- | :--- | :--- |
| **Exact Timing Guarantee** | ✅ **YES** (`setAlarmClock` / `setExact`) | ❌ **NO** (Window-based; deferred to save power) |
| **Battery & Constraint Aware** | ❌ Wakes CPU regardless of battery/network | ✅ Waits for Wi-Fi, Charging, Idle state |
| **Execution Guarantee** | Lost on device reboot unless re-registered | ✅ Guaranteed execution (persists in SQLite) |
| **Primary Use Cases** | User-facing alarms, medicine reminders, calendar events | Data sync, image compression, periodic DB cleanup |

---

## 🎙️ Staff Interview Defense Script (30 Seconds)

> *"By default, `AlarmManager` is NOT guaranteed to be exact because Android batches alarms and defers them during Doze Mode to save battery.  
> 
> To guarantee exact execution:  
> 1. We must request `SCHEDULE_EXACT_ALARM` on Android 12+ and verify `canScheduleExactAlarms()`.  
> 2. We use `setExactAndAllowWhileIdle()` to wake the device during Doze Mode.  
> 3. For critical user wake-up alarms, we use `setAlarmClock()`, which has the highest system priority and is never deferred.  
> 4. For all other deferred background jobs with constraints (Wi-Fi, charging), we use `WorkManager` instead."*
