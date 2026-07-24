package com.jetpack.compose.github.github.cruise.ui.features.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.jetpack.compose.github.github.cruise.R
import com.jetpack.compose.github.github.cruise.ui.MainDestinations.HOME_SCREEN_ROUTE
import com.jetpack.compose.github.github.cruise.ui.MainDestinations.SPLASH_SCREEN_ROUTE
import com.jetpack.compose.github.github.cruise.ui.theme.Spacing
import kotlinx.coroutines.delay

/**
 * Professional splash screen with smooth animations
 *
 * Design principles:
 * - Material Design 3 theming with dynamic colors
 * - Icon and text animations for modern feel
 * - Fade and scale animations
 * - Material Design 3 typography
 * - Auto-navigates after 2.5 seconds with proper cancellation handling
 */
@Composable
fun SplashScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(SPLASH_DURATION_MS)

        // Only navigate if still on splash screen (handles back press during delay)
        if (navController.currentDestination?.route == SPLASH_SCREEN_ROUTE) {
            navController.popBackStack()
            navController.navigate(HOME_SCREEN_ROUTE)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    ),
                ),
            ),
        contentAlignment = Alignment.Center
    ) {
        val iconScale = remember { Animatable(0f) }
        val iconAlpha = remember { Animatable(0f) }
        val titleScale = remember { Animatable(0f) }
        val titleAlpha = remember { Animatable(0f) }
        val taglineAlpha = remember { Animatable(0f) }

        LaunchedEffect(startAnimation) {
            if (startAnimation) {
                // Icon animation
                iconAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                )
                iconScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                )

                // Title animation (starts slightly after icon)
                delay(200)
                titleAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                )
                titleScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                )

                // Tagline animation
                delay(200)
                taglineAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Icon with circular border
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(iconScale.value)
                    .alpha(iconAlpha.value)
                    .border(
                        border = BorderStroke(3.dp, MaterialTheme.colorScheme.onPrimary),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            // Title
            Text(
                text = stringResource(R.string.splash_animation_github_cruise),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .scale(titleScale.value)
                    .alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            // Tagline
            Text(
                text = stringResource(R.string.splash_tagline),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha.value)
            )
        }
    }
}

/**
 * Constants for splash screen behavior
 */
private const val SPLASH_DURATION_MS = 2500L
