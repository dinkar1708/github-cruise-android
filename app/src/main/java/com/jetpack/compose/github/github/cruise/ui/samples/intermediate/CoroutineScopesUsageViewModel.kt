package com.jetpack.compose.github.github.cruise.ui.samples.intermediate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel demonstrating when and how to use different Coroutine Scopes.
 *
 * Companion Documentation: `docs/technical/FAQ/intermediate/when_to_use_coroutine_scopes.md`
 */
@HiltViewModel
class CoroutineScopesUsageViewModel @Inject constructor() : ViewModel() {

    // 1. State for Case 1: viewModelScope
    private val _viewModelScopeState = MutableStateFlow<String>("Ready (Idle)")
    val viewModelScopeState: StateFlow<String> = _viewModelScopeState.asStateFlow()

    // 2. State for Case 4: ApplicationScope vs GlobalScope
    private val _applicationScopeLogs = MutableStateFlow<List<String>>(emptyList())
    val applicationScopeLogs: StateFlow<List<String>> = _applicationScopeLogs.asStateFlow()

    // Simulated ApplicationScope (In production, inject @ApplicationScope CoroutineScope via Hilt)
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Case 1: Standard viewModelScope API Call (95% of App Logic)
     * Handles network fetching, survives screen rotation, and automatically cancels on onCleared().
     */
    fun fetchDataViaViewModelScope() {
        viewModelScope.launch {
            _viewModelScopeState.value = "⏳ [viewModelScope] Fetching GitHub Data (1.5s latency)..."
            delay(1500L) // Simulating network latency
            _viewModelScopeState.value = "✅ [viewModelScope] Loaded 30 Repositories successfully! (Survives rotation)"
            Timber.d("viewModelScope task completed")
        }
    }

    /**
     * Case 2: Suspend Function called from `rememberCoroutineScope()`
     * Demonstrates a suspend method that returns a direct result for the UI to await and show a Snackbar.
     */
    suspend fun performActionWithSuspendResult(actionName: String): String {
        Timber.d("Starting suspend API call: $actionName")
        delay(1000L) // Simulating async work
        return "Action '$actionName' completed at ${System.currentTimeMillis() % 10000}ms"
    }

    /**
     * Case 4: Injected ApplicationScope for Fire-and-Forget Background Tasks
     * Survives screen exit even after ViewModel and Composable are destroyed.
     */
    fun triggerApplicationScopeTask() {
        val timestamp = System.currentTimeMillis() % 100000
        addAppScopeLog("🚀 [AppScope] Task #$timestamp dispatched in background...")

        appScope.launch {
            delay(2000L) // Simulating background analytics flush / cache sync
            addAppScopeLog("✅ [AppScope] Task #$timestamp finished (Survives screen exit!)")
            Timber.d("ApplicationScope background task completed: #$timestamp")
        }
    }

    private fun addAppScopeLog(message: String) {
        _applicationScopeLogs.value = listOf(message) + _applicationScopeLogs.value.take(10)
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("🧹 ViewModel onCleared(): viewModelScope cancelled automatically!")
    }
}
