package com.jetpack.compose.github.github.cruise.ui.features.settings

import com.jetpack.compose.github.github.cruise.data.datastore.ThemeDataStore
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SettingsViewModel
 * Updated for DataStore migration
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockThemeDataStore: ThemeDataStore
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockThemeDataStore = mockk(relaxed = true)
        every { mockThemeDataStore.isDarkMode } returns flowOf(false)
        viewModel = SettingsViewModel(mockThemeDataStore)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test isDarkMode returns flow from datastore`() {
        // Given
        every { mockThemeDataStore.isDarkMode } returns flowOf(true)

        // When
        val viewModel = SettingsViewModel(mockThemeDataStore)

        // Then
        Assert.assertNotNull(viewModel.isDarkMode)
    }

    @Test
    fun `test setDarkMode calls themeDataStore setDarkMode with true`() {
        // Given
        val enabled = true

        // When
        viewModel.setDarkMode(enabled)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockThemeDataStore.setDarkMode(true) }
    }

    @Test
    fun `test setDarkMode calls themeDataStore setDarkMode with false`() {
        // Given
        val enabled = false

        // When
        viewModel.setDarkMode(enabled)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockThemeDataStore.setDarkMode(false) }
    }

    @Test
    fun `test setDarkMode toggles between true and false`() {
        // When - Enable dark mode
        viewModel.setDarkMode(true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockThemeDataStore.setDarkMode(true) }

        // When - Disable dark mode
        viewModel.setDarkMode(false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { mockThemeDataStore.setDarkMode(false) }
    }

    @Test
    fun `test isDarkMode flow is properly exposed`() {
        // Given
        every { mockThemeDataStore.isDarkMode } returns flowOf(false)

        // When
        val viewModel = SettingsViewModel(mockThemeDataStore)

        // Then
        Assert.assertNotNull(viewModel.isDarkMode)
    }
}
