package com.jetpack.compose.github.github.cruise.ui.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.jetpack.compose.github.github.cruise.ui.theme.AppShapes
import com.jetpack.compose.github.github.cruise.ui.theme.Elevation
import com.jetpack.compose.github.github.cruise.ui.theme.Spacing
import kotlinx.coroutines.delay

/**
 * Shared search box component with auto-search
 *
 * Design principles:
 * - Material Design 3 pill shape
 * - Auto-search with 500ms debounce
 * - Clear button for better UX
 * - State persistence across configuration changes
 * - Prevents auto-search on tab switching
 *
 * @param placeholder Placeholder text for the search field
 * @param onSearchSubmitted Callback when search is triggered
 * @param onClearInput Callback when clear button is clicked
 * @param testTag Test tag for UI testing
 */
@Composable
fun SearchBox(
    placeholder: String,
    onSearchSubmitted: (String) -> Unit,
    onClearInput: () -> Unit,
    testTag: String = "search_input"
) {
    // Keep search text across screen rotation
    var searchText by rememberSaveable { mutableStateOf("") }
    // Track if user has interacted to prevent auto-search on initial composition
    var hasUserInteracted by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-search with debounce after user stops typing
    LaunchedEffect(searchText) {
        if (hasUserInteracted && (searchText.length >= 3 || searchText.isEmpty())) {
            delay(500) // Wait 500ms after user stops typing
            onSearchSubmitted(searchText)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium),
        shape = AppShapes.searchBar,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = Elevation.level1,
        tonalElevation = Elevation.level2
    ) {
        TextField(
            value = searchText,
            onValueChange = {
                hasUserInteracted = true
                searchText = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { this.testTag = testTag },
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable {
                            hasUserInteracted = true
                            searchText = ""
                            onClearInput()
                        }
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                // Remove underline for pill shape
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    hasUserInteracted = true
                    keyboardController?.hide()
                    onSearchSubmitted(searchText)
                }
            ),
            singleLine = true
        )
    }
}
