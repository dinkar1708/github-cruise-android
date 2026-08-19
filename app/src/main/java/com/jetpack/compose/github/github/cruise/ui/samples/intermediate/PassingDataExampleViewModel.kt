package com.jetpack.compose.github.github.cruise.ui.samples.intermediate

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

data class SampleItem(
    val id: String,
    val title: String,
    val description: String
)

@HiltViewModel
class PassingDataExampleViewModel @Inject constructor() : ViewModel() {

    // Shared data using ViewModel (Method 3)
    private val _selectedItem = MutableStateFlow<SampleItem?>(null)
    val selectedItem: StateFlow<SampleItem?> = _selectedItem.asStateFlow()

    private val _items = MutableStateFlow(
        listOf(
            SampleItem("1", "Item One", "Description for item one"),
            SampleItem("2", "Item Two", "Description for item two"),
            SampleItem("3", "Item Three", "Description for item three")
        )
    )
    val items: StateFlow<List<SampleItem>> = _items.asStateFlow()

    fun selectItem(item: SampleItem) {
        _selectedItem.value = item
        Timber.d("Selected item: ${item.title}")
    }

    fun clearSelection() {
        _selectedItem.value = null
        Timber.d("Cleared selection")
    }

    init {
        Timber.d("PassingDataExampleViewModel created")
    }

    override fun onCleared() {
        Timber.d("PassingDataExampleViewModel cleared")
        super.onCleared()
    }
}
