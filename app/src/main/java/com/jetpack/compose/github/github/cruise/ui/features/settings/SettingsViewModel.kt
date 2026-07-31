package com.jetpack.compose.github.github.cruise.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpack.compose.github.github.cruise.data.datastore.LocaleDataStore
import com.jetpack.compose.github.github.cruise.data.datastore.ThemeDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen
 * Migrated to DataStore for better performance
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeDataStore: ThemeDataStore,
    private val localeDataStore: LocaleDataStore
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = themeDataStore.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val currentLocale: StateFlow<String> = localeDataStore.currentLocale
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LocaleDataStore.LOCALE_SYSTEM_DEFAULT
        )

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            themeDataStore.setDarkMode(enabled)
        }
    }

    fun setLocale(locale: String) {
        viewModelScope.launch {
            localeDataStore.setLocale(locale)
        }
    }
}
