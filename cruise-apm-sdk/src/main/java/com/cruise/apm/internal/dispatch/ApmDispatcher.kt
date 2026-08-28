package com.cruise.apm.internal.dispatch

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 * Dedicated threading dispatchers and CoroutineScope for CruiseAPM operations.
 *
 * Ensures SDK tasks run on a dedicated daemon thread and never starve the host application's dispatchers.
 */
internal object ApmDispatcher {

    private val singleThreadExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "CruiseApm-Worker").apply {
            isDaemon = true
            priority = Thread.NORM_PRIORITY - 1
        }
    }

    val workerDispatcher: CoroutineDispatcher = singleThreadExecutor.asCoroutineDispatcher()
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        android.util.Log.e("CruiseApm", "Unhandled exception in CruiseAPM coroutine", throwable)
    }

    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + workerDispatcher + exceptionHandler)
}
