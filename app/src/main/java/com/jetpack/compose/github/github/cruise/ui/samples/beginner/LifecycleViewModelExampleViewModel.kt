package com.jetpack.compose.github.github.cruise.ui.samples.beginner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ViewModelLifecycleUiState(
    val instanceHashCode: String = "",
    val createdTimestamp: String = "",
    val timerCount: Int = 0,
    val isTimerRunning: Boolean = false,
    val logs: List<String> = emptyList()
)

/**
 * Demonstrates Android ViewModel Lifecycle:
 * 1. ViewModel creation & memory retention across configuration changes (rotation)
 * 2. SavedStateHandle persistence for system process death
 * 3. Automatic cancellation of viewModelScope coroutines when onCleared() is called
 */
@HiltViewModel
class LifecycleViewModelExampleViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ViewModelLifecycleUiState(
            instanceHashCode = Integer.toHexString(System.identityHashCode(this)).uppercase(),
            createdTimestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        )
    )
    val uiState: StateFlow<ViewModelLifecycleUiState> = _uiState.asStateFlow()

    // SavedStateHandle key to survive process death
    val savedTextInput: StateFlow<String> = savedStateHandle.getStateFlow("saved_user_note", "")

    private var timerJob: Job? = null

    init {
        addLog("🟢 ViewModel INSTANTIATED (Hash: ${_uiState.value.instanceHashCode})")
        startBackgroundTimer()
    }

    fun onTextInputChanged(newText: String) {
        savedStateHandle["saved_user_note"] = newText
        addLog("💾 SavedStateHandle updated: \"$newText\"")
    }

    fun startBackgroundTimer() {
        if (timerJob?.isActive == true) return

        timerJob = viewModelScope.launch {
            _uiState.update { it.copy(isTimerRunning = true) }
            addLog("⏱️ viewModelScope timer STARTED")

            while (true) {
                delay(1000L)
                _uiState.update { it.copy(timerCount = it.timerCount + 1) }
            }
        }
    }

    fun stopBackgroundTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.update { it.copy(isTimerRunning = false) }
        addLog("🛑 viewModelScope timer STOPPED")
    }

    fun resetTimer() {
        _uiState.update { it.copy(timerCount = 0) }
        addLog("🔄 Timer reset to 0")
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }

    private fun addLog(message: String) {
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logEntry = "[$time] $message"
        Timber.d(logEntry)
        _uiState.update { it.copy(logs = listOf(logEntry) + it.logs) }
    }

    override fun onCleared() {
        super.onCleared()
        // Called when screen is permanently finished / back stack popped
        Timber.w("💀 ViewModel ON_CLEARED invoked! viewModelScope cancelled automatically.")
    }
}
