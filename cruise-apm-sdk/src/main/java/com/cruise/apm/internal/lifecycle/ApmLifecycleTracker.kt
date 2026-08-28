package com.cruise.apm.internal.lifecycle

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Bundle

/**
 * Observes Android Activity lifecycle transitions, app foreground/background sessions, and OS low-memory warnings.
 */
internal class ApmLifecycleTracker(
    private val enableLogging: Boolean,
    private val onScreenResumed: (screenName: String) -> Unit,
    private val onSessionStateChanged: (isForeground: Boolean) -> Unit,
    private val onLowMemoryWarning: (level: Int) -> Unit
) : Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    private var startedActivityCount = 0

    override fun onActivityStarted(activity: Activity) {
        if (startedActivityCount == 0) {
            if (enableLogging) android.util.Log.d("CruiseApm", "App moved to FOREGROUND")
            onSessionStateChanged(true)
        }
        startedActivityCount++
    }

    override fun onActivityResumed(activity: Activity) {
        val screenName = activity.javaClass.simpleName
        if (enableLogging) android.util.Log.d("CruiseApm", "Screen resumed: $screenName")
        onScreenResumed(screenName)
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {
        startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)
        if (startedActivityCount == 0) {
            if (enableLogging) android.util.Log.d("CruiseApm", "App moved to BACKGROUND")
            onSessionStateChanged(false)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    // ComponentCallbacks2 (Low Memory)
    override fun onTrimMemory(level: Int) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            if (enableLogging) android.util.Log.w("CruiseApm", "OS Low Memory Warning (level=$level)")
            onLowMemoryWarning(level)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {}
    override fun onLowMemory() {
        onLowMemoryWarning(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
    }
}
