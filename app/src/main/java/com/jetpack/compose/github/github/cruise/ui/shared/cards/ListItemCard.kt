package com.jetpack.compose.github.github.cruise.ui.shared.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.jetpack.compose.github.github.cruise.ui.theme.Spacing

/**
 * Reusable list item card with leading/trailing content support
 *
 * Common pattern for user lists, repository lists, favorites, etc.
 *
 * @param title Main title text
 * @param subtitle Optional subtitle text
 * @param leadingContent Optional composable for left side (e.g., avatar)
 * @param trailingContent Optional composable for right side (e.g., favorite button)
 * @param onClick Click handler
 */
@Composable
fun ListItemCard(
    title: String,
    subtitle: String? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    AppCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading content (e.g., avatar)
            leadingContent?.let {
                it()
                Spacer(modifier = Modifier.width(Spacing.medium))
            }

            // Title and subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.extraSmall),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Trailing content (e.g., favorite button, arrow)
            trailingContent?.let {
                Spacer(modifier = Modifier.width(Spacing.small))
                it()
            }
        }
    }
}
