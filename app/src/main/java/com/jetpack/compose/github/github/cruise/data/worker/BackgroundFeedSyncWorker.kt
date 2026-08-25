package com.jetpack.compose.github.github.cruise.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Production-ready CoroutineWorker demonstrating background feed delta sync
 *
 * Key Capabilities:
 * - Guaranteed background execution even if the app process is terminated
 * - Live progress tracking via setProgress(workDataOf(...))
 * - Cooperative cancellation handling (isStopped check)
 * - Returns structured output data (synced items count, duration)
 */
class BackgroundFeedSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "BackgroundFeedSyncWorker"
        const val KEY_PROGRESS = "progress"
        const val KEY_STEP_NAME = "step_name"
        const val KEY_SYNCED_COUNT = "synced_count"
        const val KEY_SYNC_DURATION_MS = "sync_duration_ms"
        const val KEY_SIMULATE_FAILURE = "simulate_failure"

        fun getTimestamp(): String =
            SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
    }

    override suspend fun doWork(): Result {
        val startTime = System.currentTimeMillis()
        val shouldSimulateFailure = inputData.getBoolean(KEY_SIMULATE_FAILURE, false)

        log("🚀 [${id.toString().take(8)}] Worker STARTED execution in background thread (${Thread.currentThread().name})")

        try {
            // Step 1: Initialize sync & check constraints
            log("🔍 Step 1/5: Verifying device state & network constraints...")
            setProgress(workDataOf(KEY_PROGRESS to 15, KEY_STEP_NAME to "Verifying device state & constraints"))
            delay(1000)

            if (isStopped) {
                log("🛑 Worker execution was STOPPED by WorkManager (Constraint lost or cancelled)")
                return Result.failure()
            }

            // Step 2: Fetch delta news feed from remote endpoint
            log("🌐 Step 2/5: Fetching delta articles from server (Cursor: cur_2026_08)...")
            setProgress(workDataOf(KEY_PROGRESS to 40, KEY_STEP_NAME to "Fetching remote delta feed"))
            delay(1200)

            if (shouldSimulateFailure) {
                log("❌ Simulated network failure (HTTP 503 Service Unavailable)")
                return Result.retry()
            }

            if (isStopped) {
                log("🛑 Worker execution was STOPPED mid-fetch")
                return Result.failure()
            }

            // Step 3: Write articles into Room SQLite database
            log("💾 Step 3/5: Writing 24 new articles into Room database (Single Source of Truth)...")
            setProgress(workDataOf(KEY_PROGRESS to 70, KEY_STEP_NAME to "Writing entities to Room SQLite"))
            delay(1000)

            // Step 4: Evict old LRU cache & download hero bitmaps
            log("🧹 Step 4/5: Enforcing 200MB LRU disk bound & pre-caching hero images into Coil...")
            setProgress(workDataOf(KEY_PROGRESS to 90, KEY_STEP_NAME to "LRU cache eviction & image pre-caching"))
            delay(800)

            // Step 5: Finalize
            val duration = System.currentTimeMillis() - startTime
            val syncedCount = 24
            log("✅ Step 5/5: Sync COMPLETE! ($syncedCount articles in ${duration}ms)")
            setProgress(workDataOf(KEY_PROGRESS to 100, KEY_STEP_NAME to "Sync completed"))

            val outputData = workDataOf(
                KEY_SYNCED_COUNT to syncedCount,
                KEY_SYNC_DURATION_MS to duration
            )

            log("🎉 Worker FINISHED with Result.success()")
            return Result.success(outputData)

        } catch (e: Exception) {
            log("❌ Worker encountered exception: ${e.message}")
            Timber.e(e, "BackgroundFeedSyncWorker failed")
            return Result.failure()
        }
    }

    private fun log(message: String) {
        val formattedLog = "[${getTimestamp()}] $message"
        Timber.tag(TAG).d(formattedLog)
    }
}
