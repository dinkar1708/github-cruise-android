package com.jetpack.compose.github.github.cruise.ui.samples.intermediate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class NavEvent {
    object ToHome : NavEvent()
    data class ToDetails(val id: String) : NavEvent()
}

data class FlowExampleState(
    val count: Int = 0,
    val isLoading: Boolean = false,
    val message: String = ""
)

@HiltViewModel
class ViewModelFlowExampleViewModel @Inject constructor() : ViewModel() {

    // StateFlow - For state
    private val _state = MutableStateFlow(FlowExampleState())
    val state: StateFlow<FlowExampleState> = _state.asStateFlow()

    // SharedFlow - For events (without replay)
    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    // SharedFlow with replay
    private val _navEvents = MutableSharedFlow<NavEvent>(replay = 1)
    val navEvents: SharedFlow<NavEvent> = _navEvents.asSharedFlow()

    // Channel - Single event delivery
    private val _toastEvents = Channel<String>()
    val toastEvents = _toastEvents.receiveAsFlow()

    // Cold Flow example
    fun getColdFlow() = flow {
        Timber.d("Cold Flow started")
        repeat(5) { i ->
            delay(500)
            emit(i)
            Timber.d("Cold Flow emitted: $i")
        }
    }

    init {
        Timber.d("ViewModel created: ${this.hashCode()}")
    }

    fun incrementCount() {
        _state.update { it.copy(count = it.count + 1) }
        Timber.d("Count incremented to: ${_state.value.count}")

        viewModelScope.launch {
            _events.emit("Count changed to ${_state.value.count}")
        }
    }

    fun sendEvent(message: String) {
        viewModelScope.launch {
            _events.emit(message)
            Timber.d("Event emitted: $message")
        }
    }

    fun sendNavEvent(event: NavEvent) {
        viewModelScope.launch {
            _navEvents.emit(event)
            Timber.d("Nav event emitted: $event")
        }
    }

    fun sendToast(message: String) {
        viewModelScope.launch {
            _toastEvents.send(message)
            Timber.d("Toast sent: $message")
        }
    }

    fun loadData() {
        Timber.d("Loading data...")
        _state.update { it.copy(isLoading = true, message = "") }

        viewModelScope.launch {
            delay(2000)
            _state.update {
                it.copy(
                    isLoading = false,
                    message = "Data loaded at ${System.currentTimeMillis() % 10000}"
                )
            }
            Timber.d("Data loaded")
        }
    }

    override fun onCleared() {
        Timber.d("ViewModel cleared: ${this.hashCode()}")
        super.onCleared()
    }
}
