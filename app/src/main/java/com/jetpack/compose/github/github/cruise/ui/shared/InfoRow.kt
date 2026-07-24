package com.jetpack.compose.github.github.cruise.ui.shared

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jetpack.compose.github.github.cruise.ui.theme.Spacing

/**
 * Reusable component for displaying label-value pairs in a row
 *
 * Eliminates duplicate InfoRow implementations across screens
 *
 * @param label The label text (left side, bold)
 * @param value The value text (right side)
 * @param modifier Optional modifier
 * @param labelWidth Width for label column (default 120dp)
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelWidth: Dp = 120.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.extraSmall)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(labelWidth)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Reusable component for displaying label-value pairs when value may be null
 *
 * @param label The label text
 * @param value The optional value text
 * @param defaultValue Text to show if value is null (default: "N/A")
 * @param modifier Optional modifier
 * @param labelWidth Width for label column (default 120dp)
 */
@Composable
fun InfoRowNullable(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    defaultValue: String = "N/A",
    labelWidth: Dp = 120.dp
) {
    InfoRow(
        label = label,
        value = value ?: defaultValue,
        modifier = modifier,
        labelWidth = labelWidth
    )
}
