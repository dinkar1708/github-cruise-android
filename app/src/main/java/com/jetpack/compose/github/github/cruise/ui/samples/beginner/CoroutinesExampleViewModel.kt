package com.jetpack.compose.github.github.cruise.ui.samples.beginner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

data class CoroutinesExampleState(
    val isLoading: Boolean = false,
    val result: String = "",
    val error: String = "",
    val logs: List<String> = emptyList()
)

@HiltViewModel
class CoroutinesExampleViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(CoroutinesExampleState())
    val state: StateFlow<CoroutinesExampleState> = _state.asStateFlow()

    private fun addLog(message: String) {
        val timestamp = System.currentTimeMillis() % 10000
        val log = "[$timestamp] $message"
        _state.update { it.copy(logs = it.logs + log) }
        Timber.d(message)
    }

    fun launchExample() {
        addLog("launch: Starting...")
        _state.update { it.copy(isLoading = true, result = "", error = "") }

        viewModelScope.launch {
            addLog("launch: Running on ${Thread.currentThread().name}")
            delay(2000)
            addLog("launch: Completed")
            _state.update {
                it.copy(
                    isLoading = false,
                    result = "Launch completed successfully"
                )
            }
        }
    }

    fun asyncExample() {
        addLog("async: Starting...")
        _state.update { it.copy(isLoading = true, result = "", error = "") }

        viewModelScope.launch {
            try {
                val result1 = async {
                    addLog("async: Task 1 on ${Thread.currentThread().name}")
                    delay(1500)
                    "Result 1"
                }

                val result2 = async {
                    addLog("async: Task 2 on ${Thread.currentThread().name}")
                    delay(1500)
                    "Result 2"
                }

                val combined = "${result1.await()} + ${result2.await()}"
                addLog("async: Both completed")

                _state.update {
                    it.copy(
                        isLoading = false,
                        result = combined
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error"
                    )
                }
            }
        }
    }

    fun dispatchersExample() {
        addLog("Dispatchers: Starting...")
        _state.update { it.copy(isLoading = true, result = "", error = "") }

        viewModelScope.launch {
            addLog("Main: ${Thread.currentThread().name}")

            val ioResult = withContext(Dispatchers.IO) {
                addLog("IO: ${Thread.currentThread().name}")
                delay(1000)
                "IO work done"
            }

            val defaultResult = withContext(Dispatchers.Default) {
                addLog("Default: ${Thread.currentThread().name}")
                delay(1000)
                "CPU work done"
            }

            addLog("Back to Main: ${Thread.currentThread().name}")

            _state.update {
                it.copy(
                    isLoading = false,
                    result = "$ioResult, $defaultResult"
                )
            }
        }
    }

    fun errorHandlingExample() {
        addLog("Error handling: Starting...")
        _state.update { it.copy(isLoading = true, result = "", error = "") }

        viewModelScope.launch {
            try {
                addLog("Attempting risky operation...")
                delay(1000)
                throw Exception("Simulated error!")
            } catch (e: Exception) {
                addLog("Caught exception: ${e.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Handled: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearLogs() {
        _state.update { it.copy(logs = emptyList(), result = "", error = "") }
    }
}
