package com.cruise.apm.internal.storage

import android.content.Context
import com.cruise.apm.model.ApmEvent
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Thread-safe offline file buffer for persistent event storage.
 *
 * Prevents telemetry data loss across application restarts or network outages.
 */
internal class OfflineEventStore(context: Context) {

    private val storageDir = File(context.filesDir, "cruise_apm").apply {
        if (!exists()) mkdirs()
    }
    private val bufferFile = File(storageDir, "events_spool.log")
    private val lock = ReentrantLock()

    fun persist(events: List<ApmEvent>) {
        if (events.isEmpty()) return
        lock.withLock {
            try {
                FileWriter(bufferFile, true).use { writer ->
                    for (event in events) {
                        writer.write(serializeEvent(event) + "\n")
                    }
                }
            } catch (e: Throwable) {
                android.util.Log.e("CruiseApm", "Failed to persist events to offline storage", e)
            }
        }
    }

    fun drain(limit: Int = 100): List<ApmEvent> {
        lock.withLock {
            if (!bufferFile.exists() || bufferFile.length() == 0L) return emptyList()
            val allEvents = mutableListOf<ApmEvent>()
            val remainingLines = mutableListOf<String>()

            try {
                bufferFile.bufferedReader().useLines { lines ->
                    var count = 0
                    for (line in lines) {
                        if (line.isBlank()) continue
                        if (count < limit) {
                            deserializeEvent(line)?.let { allEvents.add(it) }
                            count++
                        } else {
                            remainingLines.add(line)
                        }
                    }
                }

                // Rewrite unconsumed lines
                if (remainingLines.isEmpty()) {
                    bufferFile.delete()
                } else {
                    FileWriter(bufferFile, false).use { writer ->
                        for (line in remainingLines) {
                            writer.write(line + "\n")
                        }
                    }
                }
            } catch (e: Throwable) {
                android.util.Log.e("CruiseApm", "Failed to drain events from offline storage", e)
            }

            return allEvents
        }
    }

    fun getStoredCount(): Int {
        lock.withLock {
            if (!bufferFile.exists()) return 0
            return try {
                bufferFile.bufferedReader().useLines { lines ->
                    lines.count { it.isNotBlank() }
                }
            } catch (e: Throwable) {
                0
            }
        }
    }

    fun clear() {
        lock.withLock {
            if (bufferFile.exists()) bufferFile.delete()
        }
    }

    private fun serializeEvent(event: ApmEvent): String {
        val json = JSONObject()
        json.put("eventId", event.eventId)
        json.put("category", event.category)
        json.put("name", event.name)
        json.put("timestamp", event.timestamp)
        event.durationMs?.let { json.put("durationMs", it) }
        event.userId?.let { json.put("userId", it) }
        event.sessionId?.let { json.put("sessionId", it) }

        val attrJson = JSONObject()
        for ((k, v) in event.attributes) {
            attrJson.put(k, v)
        }
        json.put("attributes", attrJson)

        return json.toString()
    }

    private fun deserializeEvent(line: String): ApmEvent? {
        return try {
            val json = JSONObject(line)
            val attrJson = json.optJSONObject("attributes")
            val attributes = mutableMapOf<String, Any>()
            if (attrJson != null) {
                val keys = attrJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    attributes[key] = attrJson.get(key)
                }
            }

            ApmEvent(
                eventId = json.optString("eventId"),
                category = json.optString("category"),
                name = json.optString("name"),
                timestamp = json.optLong("timestamp"),
                durationMs = if (json.has("durationMs")) json.optLong("durationMs") else null,
                attributes = attributes,
                userId = if (json.has("userId")) json.optString("userId") else null,
                sessionId = if (json.has("sessionId")) json.optString("sessionId") else null
            )
        } catch (e: Throwable) {
            null
        }
    }
}
