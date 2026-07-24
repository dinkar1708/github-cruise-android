package com.jetpack.compose.github.github.cruise.ui.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * Reusable component for displaying statistics with a label
 *
 * Eliminates duplicate StatItem implementations across screens
 * Commonly used for displaying counts (stars, forks, followers, etc.)
 *
 * @param label The label text (shown below the count)
 * @param count The count value to display
 * @param modifier Optional modifier
 */
@Composable
fun StatItem(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Reusable component for displaying statistics with a string value
 *
 * Useful when count is already formatted as string (e.g., "1.2k")
 *
 * @param label The label text (shown below the value)
 * @param value The value to display
 * @param modifier Optional modifier
 */
@Composable
fun StatItemString(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
