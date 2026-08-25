package com.jetpack.compose.github.github.cruise.ui.samples.intermediate

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.jetpack.compose.github.github.cruise.data.worker.BackgroundFeedSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class WorkManagerUiState(
    val currentWorkId: UUID? = null,
    val workState: WorkInfo.State? = null,
    val progress: Int = 0,
    val currentStep: String = "Idle / Not Enqueued",
    val outputSyncedCount: Int? = null,
    val outputDurationMs: Long? = null,
    val isPeriodic: Boolean = false,
    val requireUnmeteredNetwork: Boolean = true,
    val requireCharging: Boolean = false,
    val requireBatteryNotLow: Boolean = true,
    val requireStorageNotLow: Boolean = false,
    val eventLogs: List<String> = emptyList()
)

@HiltViewModel
class WorkManagerSampleViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    private val _uiState = MutableStateFlow(WorkManagerUiState())
    val uiState: StateFlow<WorkManagerUiState> = _uiState.asStateFlow()

    private var workObserverJob: Job? = null

    init {
        addLog("WorkManager initialized. Ready to schedule background tasks.")
    }

    fun startOneTimeSync(simulateFailure: Boolean = false) {
        val constraints = buildConstraints()

        val inputData = workDataOf(
            BackgroundFeedSyncWorker.KEY_SIMULATE_FAILURE to simulateFailure
        )

        val workRequest = OneTimeWorkRequestBuilder<BackgroundFeedSyncWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10, TimeUnit.SECONDS
            )
            .addTag("FEED_SYNC_ONE_TIME")
            .build()

        val workId = workRequest.id
        addLog("📋 Enqueueing OneTimeWorkRequest (ID: ${workId.toString().take(8)}...)")
        addLog("⚙️ Constraints: [Unmetered=${uiState.value.requireUnmeteredNetwork}, Charging=${uiState.value.requireCharging}, BatteryNotLow=${uiState.value.requireBatteryNotLow}]")

        workManager.enqueueUniqueWork(
            "UNIQUE_FEED_SYNC_WORK",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        _uiState.update {
            it.copy(
                currentWorkId = workId,
                isPeriodic = false,
                progress = 0,
                currentStep = "Enqueued (Awaiting OS Dispatcher)",
                outputSyncedCount = null,
                outputDurationMs = null
            )
        }

        observeWorkInfo(workId)
    }

    fun startPeriodicSync() {
        val constraints = buildConstraints()

        val periodicRequest = PeriodicWorkRequestBuilder<BackgroundFeedSyncWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES // 5-minute flex window
        ).setConstraints(constraints)
            .addTag("FEED_SYNC_PERIODIC")
            .build()

        val workId = periodicRequest.id
        addLog("⏰ Enqueueing PeriodicWorkRequest (15 min interval, ID: ${workId.toString().take(8)}...)")

        workManager.enqueueUniquePeriodicWork(
            "UNIQUE_PERIODIC_FEED_SYNC",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )

        _uiState.update {
            it.copy(
                currentWorkId = workId,
                isPeriodic = true,
                progress = 0,
                currentStep = "Periodic Schedule Active (Runs every 15 min)",
                outputSyncedCount = null,
                outputDurationMs = null
            )
        }

        observeWorkInfo(workId)
    }

    fun cancelActiveWork() {
        val workId = uiState.value.currentWorkId
        if (workId != null) {
            addLog("🛑 Cancelling Work ID: ${workId.toString().take(8)}...")
            workManager.cancelWorkById(workId)
        } else {
            addLog("🛑 Cancelling all unique work...")
            workManager.cancelUniqueWork("UNIQUE_FEED_SYNC_WORK")
            workManager.cancelUniqueWork("UNIQUE_PERIODIC_FEED_SYNC")
        }
    }

    private fun observeWorkInfo(workId: UUID) {
        workObserverJob?.cancel()
        workObserverJob = viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(workId).collect { workInfo ->
                if (workInfo != null) {
                    val state = workInfo.state
                    val progressValue = workInfo.progress.getInt(BackgroundFeedSyncWorker.KEY_PROGRESS, 0)
                    val stepName = workInfo.progress.getString(BackgroundFeedSyncWorker.KEY_STEP_NAME) ?: state.name

                    var syncedCount: Int? = null
                    var durationMs: Long? = null

                    if (state == WorkInfo.State.SUCCEEDED) {
                        syncedCount = workInfo.outputData.getInt(BackgroundFeedSyncWorker.KEY_SYNCED_COUNT, 0)
                        durationMs = workInfo.outputData.getLong(BackgroundFeedSyncWorker.KEY_SYNC_DURATION_MS, 0L)
                        addLog("🎉 [WorkInfo] SUCCEEDED! Result Data: { syncedCount: $syncedCount, duration: ${durationMs}ms }")
                    } else if (state == WorkInfo.State.FAILED) {
                        addLog("❌ [WorkInfo] FAILED!")
                    } else if (state == WorkInfo.State.CANCELLED) {
                        addLog("🛑 [WorkInfo] CANCELLED by user or OS constraint change")
                    } else {
                        addLog("⚡ [WorkInfo State] $state | Progress: $progressValue% ($stepName)")
                    }

                    _uiState.update { current ->
                        current.copy(
                            workState = state,
                            progress = if (state == WorkInfo.State.SUCCEEDED) 100 else progressValue,
                            currentStep = stepName,
                            outputSyncedCount = syncedCount ?: current.outputSyncedCount,
                            outputDurationMs = durationMs ?: current.outputDurationMs
                        )
                    }
                }
            }
        }
    }

    private fun buildConstraints(): Constraints {
        val state = uiState.value
        val networkType = if (state.requireUnmeteredNetwork) NetworkType.UNMETERED else NetworkType.CONNECTED

        return Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .setRequiresCharging(state.requireCharging)
            .setRequiresBatteryNotLow(state.requireBatteryNotLow)
            .setRequiresStorageNotLow(state.requireStorageNotLow)
            .build()
    }

    fun toggleUnmetered(enabled: Boolean) {
        _uiState.update { it.copy(requireUnmeteredNetwork = enabled) }
        addLog("⚙️ Constraint: Require Wi-Fi (Unmetered) set to $enabled")
    }

    fun toggleCharging(enabled: Boolean) {
        _uiState.update { it.copy(requireCharging = enabled) }
        addLog("⚙️ Constraint: Requires Device Charging set to $enabled")
    }

    fun toggleBatteryNotLow(enabled: Boolean) {
        _uiState.update { it.copy(requireBatteryNotLow = enabled) }
        addLog("⚙️ Constraint: Requires Battery Not Low set to $enabled")
    }

    fun toggleStorageNotLow(enabled: Boolean) {
        _uiState.update { it.copy(requireStorageNotLow = enabled) }
        addLog("⚙️ Constraint: Requires Storage Not Low set to $enabled")
    }

    fun clearLogs() {
        _uiState.update { it.copy(eventLogs = emptyList()) }
    }

    private fun addLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val formattedLog = "[$timestamp] $message"
        Timber.d(formattedLog)
        _uiState.update {
            val updated = listOf(formattedLog) + it.eventLogs
            it.copy(eventLogs = updated.take(50))
        }
    }
}
