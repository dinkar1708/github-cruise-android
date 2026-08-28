package com.cruise.apm.internal.watchdog

import android.os.Handler
import android.os.Looper
import com.cruise.apm.model.AnrRecord
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Background watchdog thread that monitors the Main Looper for unresponsive UI hangs (ANRs).
 *
 * Runs a periodic heartbeat runnable on the UI Handler. If the main thread fails to execute
 * the heartbeat within [timeoutMs], it captures the main thread's full stack trace.
 */
internal class AnrWatchdog(
    private val timeoutMs: Long = 5000L,
    private val onAnrDetected: (AnrRecord) -> Unit
) : Thread("CruiseApm-AnrWatchdog") {

    private val uiHandler = Handler(Looper.getMainLooper())
    private val isRunning = AtomicBoolean(false)
    private val tickCounter = AtomicLong(0L)
    private val reportedForCurrentFreeze = AtomicBoolean(false)

    private val tickerRunnable = Runnable {
        tickCounter.incrementAndGet()
        reportedForCurrentFreeze.set(false)
    }

    override fun run() {
        while (isRunning.get()) {
            val lastTick = tickCounter.get()
            uiHandler.post(tickerRunnable)

            try {
                sleep(timeoutMs)
            } catch (e: InterruptedException) {
                break
            }

            // Check if ticker completed on main thread
            if (tickCounter.get() == lastTick && isRunning.get()) {
                if (!reportedForCurrentFreeze.getAndSet(true)) {
                    val mainThread = Looper.getMainLooper().thread
                    val stackTraceElements = mainThread.stackTrace
                    val formattedTrace = buildStackTraceString(mainThread, stackTraceElements)

                    val record = AnrRecord(
                        durationBlockedMs = timeoutMs,
                        mainThreadStackTrace = formattedTrace,
                        allThreadsDump = dumpAllThreads()
                    )
                    onAnrDetected(record)
                }
            }
        }
    }

    fun startWatchdog() {
        if (isRunning.compareAndSet(false, true)) {
            isDaemon = true
            priority = NORM_PRIORITY - 1
            start()
        }
    }

    fun stopWatchdog() {
        isRunning.set(false)
        interrupt()
    }

    private fun buildStackTraceString(thread: Thread, elements: Array<StackTraceElement>): String {
        return buildString {
            appendLine("Thread [${thread.name}] (State: ${thread.state}):")
            for (element in elements) {
                appendLine("    at $element")
            }
        }
    }

    private fun dumpAllThreads(): String {
        return try {
            buildString {
                Thread.getAllStackTraces().forEach { (t, stack) ->
                    appendLine("Thread [${t.name}] (State: ${t.state}, Priority: ${t.priority}):")
                    stack.take(5).forEach { elem ->
                        appendLine("    at $elem")
                    }
                    appendLine()
                }
            }
        } catch (e: Throwable) {
            ""
        }
    }
}
