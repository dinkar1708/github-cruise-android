package com.jetpack.compose.github.github.cruise.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jetpack.compose.github.github.cruise.ui.theme.*

/**
 * Reusable chip component for tags, labels, and categories
 *
 * Commonly used for displaying programming languages, topics, etc.
 *
 * @param text The text to display in the chip
 * @param modifier Optional modifier
 * @param backgroundColor Background color (default: secondary container)
 * @param textColor Text color (default: on secondary container)
 * @param onClick Optional click handler
 */
@Composable
fun AppChip(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    textColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(clickableModifier)
            .clip(shape)
            .background(backgroundColor)
            .padding(horizontal = Spacing.medium, vertical = Spacing.small)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

/**
 * Outlined variant of AppChip with border and no background
 *
 * @param text The text to display in the chip
 * @param modifier Optional modifier
 * @param borderColor Border color (default: outline)
 * @param textColor Text color (default: on surface)
 * @param onClick Optional click handler
 */
@Composable
fun OutlinedChip(
    text: String,
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(clickableModifier)
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = Spacing.medium, vertical = Spacing.small)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

/**
 * Language chip with color indicator
 *
 * @param language Programming language name
 * @param modifier Optional modifier
 * @param onClick Optional click handler
 */
@Composable
fun LanguageChip(
    language: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    AppChip(
        text = language,
        modifier = modifier,
        backgroundColor = getLanguageColor(language),
        textColor = Color.White,
        onClick = onClick
    )
}

/**
 * Get color for programming language
 * Uses theme-defined language colors from Color.kt
 * Based on common GitHub language colors for consistent UI
 */
private fun getLanguageColor(language: String): Color {
    return when (language.lowercase()) {
        "kotlin" -> LanguageKotlin
        "java" -> LanguageJava
        "python" -> LanguagePython
        "javascript" -> LanguageJavaScript
        "typescript" -> LanguageTypeScript
        "swift" -> LanguageSwift
        "go" -> LanguageGo
        "rust" -> LanguageRust
        "c++" -> LanguageCPlusPlus
        "c#" -> LanguageCSharp
        "ruby" -> LanguageRuby
        "php" -> LanguagePHP
        "dart" -> LanguageDart
        else -> LanguageDefault
    }
}
