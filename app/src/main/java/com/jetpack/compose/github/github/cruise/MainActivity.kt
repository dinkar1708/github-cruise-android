package com.jetpack.compose.github.github.cruise

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.lifecycle.lifecycleScope
import com.jetpack.compose.github.github.cruise.data.datastore.ThemeDataStore
import com.jetpack.compose.github.github.cruise.ui.GithubCruiseRootComposable
import com.jetpack.compose.github.github.cruise.ui.theme.GithubCruiseTheme
import com.jetpack.compose.github.github.cruise.ui.theme.White
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeDataStore: ThemeDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

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
}