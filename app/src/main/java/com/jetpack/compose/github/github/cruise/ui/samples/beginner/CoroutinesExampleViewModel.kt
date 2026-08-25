package com.jetpack.compose.github.github.cruise.ui.samples.beginner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private fun addLog(message: String) {
        val timestamp = timeFormat.format(Date())
        val log = "[$timestamp] $message"
        _state.update { it.copy(logs = it.logs + log) }
        Timber.d(log)
    }

    // =========================================================================
    // 1. launch vs async Basics
    // =========================================================================
    fun launchExample() {
        addLog("🚀 [launch] Starting fire-and-forget background job...")
        _state.update { it.copy(isLoading = true, result = "", error = "") }

        viewModelScope.launch {
            addLog("⚡ Running on thread: ${Thread.currentThread().name}")
            delay(1500)
            addLog("✅ [launch] Completed successfully")
            _state.update {
                it.copy(
                    isLoading = false,
                    result = "Launch completed successfully"
                )
            }
        }
    }

    fun asyncExample() {
        addLog("⚡ [async] Launching 2 parallel tasks...")
        _state.update { it.copy(isLoading = true, result = "", error = "") }

        viewModelScope.launch {
            val d1 = async {
                delay(1200)
                addLog("📦 Task 1 finished (1200ms)")
                "User Profile"
            }
            val d2 = async {
                delay(800)
                addLog("📦 Task 2 finished (800ms)")
                "Order History"
            }

            val combined = "${d1.await()} + ${d2.await()}"
            addLog("🎉 [async] Combined: $combined")
            _state.update {
                it.copy(
                    isLoading = false,
                    result = combined
                )
            }
        }
    }

    // =========================================================================
    // 2. ERROR HANDLING PATTERN 1: In-Place try-catch with Cancellation Check
    // =========================================================================
    fun safeTryCatchExample() {
        addLog("🛡️ [Pattern 1: In-Place try-catch] Starting risky network call...")
        _state.update { it.copy(isLoading = true, result = "", error = "") }

        viewModelScope.launch {
            try {
                addLog("🌐 Sending HTTP request...")
                delay(1000)
                throw IOException("HTTP 503: Service Unavailable")
            } catch (e: Exception) {
                // ✅ Always check and rethrow CancellationException!
                if (e is CancellationException) {
                    addLog("🛑 CancellationException caught - rethrowing!")
                    throw e
                }
                addLog("✅ Caught locally: ${e.message} (Scope stays healthy!)")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Handled: ${e.message}"
                    )
                }
            }
        }
    }

    // =========================================================================
    // 3. ERROR HANDLING PATTERN 2: supervisorScope vs coroutineScope (Parallel Tasks)
    // =========================================================================
    fun coroutineScopeFailureTrapExample() {
        addLog("⚠️ [Pattern 2: Standard coroutineScope] Launching 2 parallel tasks...")
        _state.update { it.copy(isLoading = true, result = "", error = "") }

        viewModelScope.launch {
            try {
                coroutineScope {
                    val d1 = async {
                        delay(500)
                        addLog("💥 Task 1 throwing HTTP 500 error!")
                        throw IOException("HTTP 500 Internal Server Error")
                    }
                    val d2 = async {
                        delay(1500)
                        addLog("📦 Task 2 healthy finished (NEVER REACHED if cancelled!)")
                        "Healthy Data"
                    }

                    d1.await()
                    d2.await()
                }
            } catch (e: Exception) {
                addLog("💥 [CASCADING FAILURE] Parent coroutineScope died! Task 2 was killed!")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "coroutineScope cancelled both: ${e.message}"
                    )
                }
            }
        }
    }

    fun supervisorScopeResilientExample() {
        addLog("🛡️ [Pattern 2: supervisorScope] Launching 2 parallel tasks with isolation...")
        _state.update { it.copy(isLoading = true, result = "", error = "") }

        viewModelScope.launch {
            supervisorScope {
                val d1 = async {
                    delay(500)
                    addLog("💥 Task 1 failing with HTTP 500 (Isolated)")
                    runCatching { throw IOException("HTTP 500 in Avatar") }.getOrNull()
                }
                val d2 = async {
                    delay(1200)
                    addLog("✅ Task 2 completed successfully! (Profile Feed)")
                    "Main News Feed"
                }

                val r1 = d1.await()
                val r2 = d2.await()

                addLog("🎉 [ISOLATED SUCCESS] Task 2 survived: '$r2' | Task 1 fallback: '$r1'")
                _state.update {
                    it.copy(
                        isLoading = false,
                        result = "Dashboard: Feed = '$r2', Avatar = '${r1 ?: "Default Avatar"}'"
                    )
                }
            }
        }
    }

    // =========================================================================
    // 4. ERROR HANDLING PATTERN 3: CoroutineExceptionHandler (CEH)
    // =========================================================================
    fun coroutineExceptionHandlerExample() {
        addLog("🎯 [Pattern 3: CoroutineExceptionHandler] Testing Root CEH safety net...")
        _state.update { it.copy(isLoading = true, result = "", error = "") }

        val ceh = CoroutineExceptionHandler { _, throwable ->
            addLog("🛡️ [CEH Intercepted] Uncaught crash captured: ${throwable.message}")
            _state.update {
                it.copy(
                    isLoading = false,
                    error = "CEH Captured: ${throwable.message}"
                )
            }
        }

        val testScope = CoroutineScope(Dispatchers.Main + SupervisorJob() + ceh)
        testScope.launch {
            addLog("💥 Launching unhandled root failure...")
            delay(800)
            throw RuntimeException("Fatal crash in background daemon")
        }
    }

    // =========================================================================
    // 5. ERROR HANDLING PATTERN 5: Flow .catch {} and .retryWhen {}
    // =========================================================================
    fun flowCatchAndRetryExample() {
        addLog("🌊 [Pattern 5: Flow catch & retry] Starting reactive stream with retries...")
        _state.update { it.copy(isLoading = true, result = "", error = "") }

        var attemptCount = 0
        val failingStream = flow {
            attemptCount++
            addLog("📡 Stream attempt #$attemptCount connecting...")
            delay(600)
            if (attemptCount < 3) {
                addLog("💥 Network lost on attempt #$attemptCount!")
                throw IOException("Network Timeout on attempt #$attemptCount")
            }
            emit("Live Stream Payload (Connected on attempt #3)")
        }

        viewModelScope.launch {
            failingStream
                .retryWhen { cause, attempt ->
                    if (cause is IOException && attempt < 3) {
                        addLog("🔄 [retryWhen] Backing off 500ms before retry #${attempt + 1}...")
                        delay(500)
                        true
                    } else {
                        false
                    }
                }
                .catch { exception ->
                    addLog("🛡️ [.catch] Flow caught error: ${exception.message} ➔ Emitting fallback offline cache!")
                    emit("Offline Cache Data (Fallback)")
                }
                .collect { data ->
                    addLog("🎉 [Collected Value] $data")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            result = data
                        )
                    }
                }
        }
    }

    fun clearLogs() {
        _state.update { it.copy(logs = emptyList(), result = "", error = "") }
    }
}
