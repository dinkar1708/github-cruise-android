package com.jetpack.compose.github.github.cruise.ui.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.navigation.NavHostController
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.jetpack.compose.github.github.cruise.BuildConfig
import com.jetpack.compose.github.github.cruise.R
import com.jetpack.compose.github.github.cruise.ui.MainDestinations.WEBVIEW_SCREEN_ROUTE
import com.jetpack.compose.github.github.cruise.ui.shared.AppActionBarView
import com.jetpack.compose.github.github.cruise.ui.theme.AppShapes
import com.jetpack.compose.github.github.cruise.ui.theme.Dimension
import com.jetpack.compose.github.github.cruise.ui.theme.Elevation
import com.jetpack.compose.github.github.cruise.ui.theme.GithubCruiseTheme
import com.jetpack.compose.github.github.cruise.ui.theme.Spacing

/**
 * Settings screen with app preferences
 */
@Composable
fun SettingsScreen(
    navController: androidx.navigation.NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    val darkModeEnabled by viewModel.isDarkMode.collectAsState()

    // Extract string resources at Composable level
    val privacyTitle = stringResource(R.string.settings_privacy)
    val termsTitle = stringResource(R.string.settings_terms)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        AppActionBarView(
            modifier = Modifier.fillMaxWidth(),
            headerText = stringResource(R.string.settings_title),
            showBackButton = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.medium)
        ) {
            // General Section
            SectionHeader(title = stringResource(R.string.settings_section_general))

            SettingsItemSwitch(
                icon = Icons.Filled.Notifications,
                title = stringResource(R.string.settings_notifications),
                subtitle = stringResource(R.string.settings_notifications_subtitle),
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )

            SettingsItemSwitch(
                icon = Icons.Filled.Settings,
                title = stringResource(R.string.settings_dark_mode),
                subtitle = stringResource(R.string.settings_dark_mode_subtitle),
                checked = darkModeEnabled,
                onCheckedChange = { viewModel.setDarkMode(it) }
            )

            // Preferences Section
            SectionHeader(title = stringResource(R.string.settings_section_preferences))

            SettingsItem(
                icon = Icons.Filled.Build,
                title = stringResource(R.string.settings_language),
                subtitle = stringResource(R.string.settings_language_subtitle),
                onClick = { /* TODO: Open language selector */ }
            )

            // Legal Section
            SectionHeader(title = stringResource(R.string.settings_section_legal))

            SettingsItem(
                icon = Icons.Filled.Lock,
                title = stringResource(R.string.settings_privacy),
                subtitle = stringResource(R.string.settings_privacy_subtitle),
                onClick = {
                    val url = com.jetpack.compose.github.github.cruise.ui.shared.utils.CommonUtils.encodeUrl(
                        "https://www.google.com"
                    )
                    navController.navigate("$WEBVIEW_SCREEN_ROUTE/$url/$privacyTitle")
                }
            )

            SettingsItem(
                icon = Icons.Filled.Info,
                title = stringResource(R.string.settings_terms),
                subtitle = stringResource(R.string.settings_terms_subtitle),
                onClick = {
                    val url = com.jetpack.compose.github.github.cruise.ui.shared.utils.CommonUtils.encodeUrl(
                        "https://www.google.com"
                    )
                    navController.navigate("$WEBVIEW_SCREEN_ROUTE/$url/$termsTitle")
                }
            )

            // Version display at the bottom
            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Remove "debug" suffix from version name for clean display
                val cleanVersionName = BuildConfig.VERSION_NAME.removeSuffix("debug")

                Text(
                    text = "Version $cleanVersionName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Code ${BuildConfig.VERSION_CODE}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Spacing.extraSmall)
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            top = Spacing.large,
            bottom = Spacing.small
        )
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.extraSmall)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Elevation.card
        ),
        shape = AppShapes.card
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimension.iconSizeLarge)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.medium)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Dimension.iconSizeMedium)
            )
        }
    }
}

@Composable
fun SettingsItemSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.extraSmall),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Elevation.card
        ),
        shape = AppShapes.card
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimension.iconSizeLarge)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.medium)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    GithubCruiseTheme {
        // SettingsScreen requires NavHostController, can't preview easily
    }
}
