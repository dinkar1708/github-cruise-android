package com.jetpack.compose.github.github.cruise.ui.features.settings

import com.jetpack.compose.github.github.cruise.data.preferences.ThemePreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SettingsViewModel
 */
class SettingsViewModelTest {

    private lateinit var mockThemePreferences: ThemePreferences
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        mockThemePreferences = mockk(relaxed = true)
        viewModel = SettingsViewModel(mockThemePreferences)
    }

    @Test
    fun `test isDarkMode returns flow from preferences`() {
        // Given
        val darkModeFlow = MutableStateFlow(true)
        every { mockThemePreferences.isDarkMode } returns darkModeFlow

        // When
        val viewModel = SettingsViewModel(mockThemePreferences)

        // Then
        Assert.assertEquals(darkModeFlow, viewModel.isDarkMode)
    }

    @Test
    fun `test setDarkMode calls themePreferences setDarkMode with true`() {
        // Given
        val enabled = true

        // When
        viewModel.setDarkMode(enabled)

        // Then
        verify { mockThemePreferences.setDarkMode(true) }
    }

    @Test
    fun `test setDarkMode calls themePreferences setDarkMode with false`() {
        // Given
        val enabled = false

        // When
        viewModel.setDarkMode(enabled)

        // Then
        verify { mockThemePreferences.setDarkMode(false) }
    }

    @Test
    fun `test setDarkMode toggles between true and false`() {
        // When - Enable dark mode
        viewModel.setDarkMode(true)

        // Then
        verify { mockThemePreferences.setDarkMode(true) }

        // When - Disable dark mode
        viewModel.setDarkMode(false)

        // Then
        verify { mockThemePreferences.setDarkMode(false) }
    }

    @Test
    fun `test isDarkMode flow is properly exposed`() {
        // Given
        val darkModeFlow = MutableStateFlow(false)
        every { mockThemePreferences.isDarkMode } returns darkModeFlow

        // When
        val viewModel = SettingsViewModel(mockThemePreferences)

        // Then
        Assert.assertNotNull(viewModel.isDarkMode)
        Assert.assertEquals(false, viewModel.isDarkMode.value)
    }
}
