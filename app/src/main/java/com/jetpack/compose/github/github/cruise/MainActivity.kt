package com.jetpack.compose.github.github.cruise

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jetpack.compose.github.github.cruise.data.datastore.LocaleDataStore
import com.jetpack.compose.github.github.cruise.data.datastore.ThemeDataStore
import com.jetpack.compose.github.github.cruise.ui.GithubCruiseRootComposable
import com.jetpack.compose.github.github.cruise.ui.theme.GithubCruiseTheme
import com.jetpack.compose.github.github.cruise.utils.LocaleManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeDataStore: ThemeDataStore

    @Inject
    lateinit var localeDataStore: LocaleDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // -----------------------------------------------------------------------------------------
        // 🏛️ WHY USE lifecycleScope + repeatOnLifecycle IN MainActivity?
        // Although Compose handles screen UI, MainActivity is the OS-level Host Container.
        // Real-World Use Cases where MainActivity uses repeatOnLifecycle(STARTED):
        // 1. Google Play In-App Updates: `AppUpdateManager.startUpdateFlowForResult(activity, ...)`
        // 2. Biometric Auth Prompts: `BiometricPrompt.authenticate(...)` (Requires Activity Context)
        // 3. Global Force-Update / Session Expired Dialogs: Triggering system-level modal alerts
        // 4. Global Deep Link / Push Intent Routing: Dispatching notification intents safely
        // 5. Network Connectivity Monitoring: Triggering global offline banners when Activity is visible
        //
        // 🔋 Battery Protection: When user presses Home (ON_STOP), repeatOnLifecycle PAUSES
        // the coroutine immediately so no background CPU/battery is wasted. It resumes on ON_START!
        // -----------------------------------------------------------------------------------------
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                Timber.d("👋 Hello from MainActivity lifecycleScope coroutine! (Activity is STARTED)")
                
                // 🌐 Calling a suspend API function from inside the coroutine:
                val apiResponse = simulateRemoteConfigApiCall()
                Timber.d("🎉 Result received in MainActivity: $apiResponse")
            }
        }

        // -----------------------------------------------------------------------------------------
        // 🏛️ WHY & HOW TO USE LifecycleEventObserver ON LifecycleOwner:
        //
        // 🎯 Real-World Use Cases:
        // 1. Hardware & Sensor Management: Unregistering GPS, Accelerometer, or CameraX sensors on ON_PAUSE/ON_STOP.
        // 2. Native Video / Audio Players: Explicitly detaching SurfaceView / pausing ExoPlayer decoders on ON_PAUSE.
        // 3. User Session Analytics: Logging "User Session Started" on ON_START and "User Session Ended" on ON_STOP.
        //
        // 🧹 Memory Management & Cleanup Rules:
        // - When added directly to Activity lifecycle (`lifecycle.addObserver(...)`), the Activity cleans it up on onDestroy().
        // - BUT when used inside Compose or a Custom Component, you MUST call `lifecycle.removeObserver(observer)`
        //   inside `DisposableEffect.onDispose { ... }` to prevent permanent memory leaks!
        //
        // 📋 TODO: [DEBUG - Delete before production release] This observer logs OS state transitions for verification.
        // -----------------------------------------------------------------------------------------
        lifecycle.addObserver(LifecycleEventObserver { source, event ->
            Timber.d("🏛️ LifecycleOwner event: ${event.name} (Source: ${source.javaClass.simpleName})")
            when (event) {
                Lifecycle.Event.ON_START -> Timber.d("🟢 Activity ON_START - Screen is becoming visible")
                Lifecycle.Event.ON_RESUME -> Timber.d("⚡ Activity ON_RESUME - Screen is interactive")
                Lifecycle.Event.ON_PAUSE -> Timber.d("⏸️ Activity ON_PAUSE - Screen partially obscured")
                Lifecycle.Event.ON_STOP -> Timber.d("🛑 Activity ON_STOP - Screen is hidden in background")
                Lifecycle.Event.ON_DESTROY -> Timber.d("💀 Activity ON_DESTROY - Cleaning up resources")
                else -> Unit
            }
        })

        setContent {
            val isDarkMode by themeDataStore.isDarkMode.collectAsState(initial = false)

            GithubCruiseTheme(darkTheme = isDarkMode) {
                // Configure system bars based on theme
                ConfigureSystemBars(isDarkMode)

                // Set background color for all views
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GithubCruiseRootComposable()
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        // Apply saved locale before activity is created
        val prefs = newBase.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
        val locale = prefs.getString("locale", LocaleDataStore.LOCALE_SYSTEM_DEFAULT)
            ?: LocaleDataStore.LOCALE_SYSTEM_DEFAULT

        val context = LocaleManager.setLocale(newBase, locale)
        super.attachBaseContext(context)
    }

    @Composable
    private fun ConfigureSystemBars(isDarkMode: Boolean) {
        val statusBarColor = MaterialTheme.colorScheme.background
        val surfaceColor = MaterialTheme.colorScheme.surface

        SideEffect {
            window?.let { window ->
                // Set status bar to match app background
                window.statusBarColor = statusBarColor.toArgb()

                // Set navigation bar to match surface (for bottom nav)
                window.navigationBarColor = surfaceColor.toArgb()

                // Set icon colors based on theme
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !isDarkMode
                    isAppearanceLightNavigationBars = !isDarkMode
                }
            }
        }
    }

    /**
     * Example suspend function simulating an asynchronous network API call.
     * Demonstrates that `lifecycleScope.launch` provides a coroutine context to call suspend functions with a 2-second delay.
     */
    private suspend fun simulateRemoteConfigApiCall(): String {
        Timber.d("🌐 [API Call Started] Fetching remote app configuration from server...")
        delay(2000L) // ⏳ Simulates 2-second network latency (non-blocking!)
        Timber.d("✅ [API Call Finished] Received response: App Remote Config Loaded (Version 2026.1)")
        return "App Remote Config Loaded (Version 2026.1)"
    }
}