package com.cruise.apm.internal.dispatch

import com.cruise.apm.internal.storage.OfflineEventStore
import com.cruise.apm.model.ApmEvent
import com.cruise.apm.model.SystemVitals
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Concurrency coordinator for event buffering using Kotlin Channels and Coroutines.
 *
 * Provides non-blocking event ingestion, in-memory batching, disk spooling, and real-time Flow streams.
 */
internal class EventBufferChannel(
    private val batchSize: Int,
    private val offlineStore: OfflineEventStore,
    private val enableLogging: Boolean,
    private val onBatchReady: ((List<ApmEvent>) -> Unit)? = null
) {
    private val incomingChannel = Channel<ApmEvent>(capacity = Channel.BUFFERED)
    private val inMemoryBuffer = ConcurrentLinkedQueue<ApmEvent>()

    private val _eventStream = MutableSharedFlow<ApmEvent>(replay = 50, extraBufferCapacity = 100)
    val eventStream: SharedFlow<ApmEvent> = _eventStream.asSharedFlow()

    private val _vitalsStream = MutableStateFlow<SystemVitals?>(null)
    val vitalsStream: StateFlow<SystemVitals?> = _vitalsStream.asStateFlow()

    init {
        startConsumerLoop()
    }

    private fun startConsumerLoop() {
        ApmDispatcher.scope.launch {
            for (event in incomingChannel) {
                inMemoryBuffer.offer(event)
                _eventStream.emit(event)

                if (enableLogging) {
                    android.util.Log.d("CruiseApm", "Ingested ${event.category} [${event.name}] (Queue size: ${inMemoryBuffer.size})")
                }

                if (inMemoryBuffer.size >= batchSize) {
                    flushInternal()
                }
            }
        }
    }

    fun enqueue(event: ApmEvent): Boolean {
        return incomingChannel.trySend(event).isSuccess
    }

    fun updateVitals(vitals: SystemVitals) {
        _vitalsStream.value = vitals
    }

    @Synchronized
    fun flush(): List<ApmEvent> {
        return flushInternal()
    }

    private fun flushInternal(): List<ApmEvent> {
        val drained = mutableListOf<ApmEvent>()
        while (inMemoryBuffer.isNotEmpty()) {
            val item = inMemoryBuffer.poll()
            if (item != null) drained.add(item)
        }

        if (drained.isNotEmpty()) {
            // Persist to offline spool
            offlineStore.persist(drained)
            onBatchReady?.invoke(drained)
            if (enableLogging) {
                android.util.Log.d("CruiseApm", "Batch flushed ${drained.size} events to offline storage.")
            }
        }
        return drained
    }

    fun getPendingEvents(): List<ApmEvent> = inMemoryBuffer.toList()

    fun getPersistedCount(): Int = offlineStore.getStoredCount()

    fun clear() {
        inMemoryBuffer.clear()
        offlineStore.clear()
    }
}
