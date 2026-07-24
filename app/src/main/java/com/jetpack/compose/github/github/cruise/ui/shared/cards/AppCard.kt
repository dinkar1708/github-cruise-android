package com.jetpack.compose.github.github.cruise.ui.shared.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jetpack.compose.github.github.cruise.ui.theme.AppShapes
import com.jetpack.compose.github.github.cruise.ui.theme.Elevation
import com.jetpack.compose.github.github.cruise.ui.theme.Spacing

/**
 * Reusable Material Design 3 Card component following app design system
 *
 * This card provides consistent styling across the app including:
 * - Material 3 color scheme
 * - App-specific elevation
 * - Rounded corners from AppShapes
 * - Standard padding
 *
 * @param modifier Modifier for the card
 * @param onClick Optional click handler - makes card clickable
 * @param content Card content in a ColumnScope
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Card(
        modifier = cardModifier
            .fillMaxWidth()
            .padding(vertical = Spacing.small),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Elevation.card
        ),
        shape = AppShapes.card,
        content = content
    )
}
