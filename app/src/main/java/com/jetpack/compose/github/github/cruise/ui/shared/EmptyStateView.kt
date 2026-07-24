package com.jetpack.compose.github.github.cruise.ui.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jetpack.compose.github.github.cruise.ui.theme.Spacing

/**
 * Reusable empty state component for displaying when there's no content
 *
 * Used across the app for:
 * - Empty search results
 * - Empty favorites list
 * - No repositories
 * - Any empty state scenario
 *
 * @param icon Icon to display
 * @param title Main empty state message
 * @param description Optional detailed description
 * @param actionButton Optional action button (e.g., "Try again", "Add item")
 */
@Composable
fun EmptyStateView(
    icon: ImageVector = Icons.Filled.Info,
    title: String,
    description: String? = null,
    actionButton: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(Spacing.medium))

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Description
        description?.let {
            Spacer(modifier = Modifier.height(Spacing.small))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )
        }

        // Action button
        actionButton?.let {
            Spacer(modifier = Modifier.height(Spacing.medium))
            it()
        }
    }
}
