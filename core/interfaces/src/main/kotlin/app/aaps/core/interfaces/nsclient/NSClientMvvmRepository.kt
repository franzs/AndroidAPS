package app.aaps.core.interfaces.nsclient

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.json.JSONObject

/**
 * Interface for NSClient MVVM repository
 */
interface NSClientMvvmRepository {

    /**
     * Queue size state flow - always has a current value
     */
    val queueSize: StateFlow<Long>

    /**
     * Status state flow - always has a current value
     */
    val statusUpdate: StateFlow<String>

    /**
     * URL state flow - always has a current value
     */
    val urlUpdate: StateFlow<String>

    /**
     * Log list state flow - always has current log history
     */
    val logList: StateFlow<List<NSClientLog>>

    /**
     * Update queue size in NSClient fragment
     */
    fun updateQueueSize(size: Long)

    /**
     * Update status in NSClient fragment
     */
    fun updateStatus(status: String)

    /**
     * Update url in NSClient fragment
     */
    fun updateUrl(url: String)

    /**
     * Add new log entry to NSClient fragment
     */
    fun addLog(action: String, logText: String?, json: JsonElement?)

    /**
     * Add new log entry to NSClient fragment
     */
    fun addLog(action: String, logText: String?) {
        addLog(action, logText, null as JsonElement?)
    }

    /**
     * Add new log entry to NSClient fragment
     */
    @Deprecated("Migrate to kotlin's JsonObject")
    fun addLog(action: String, logText: String?, json: JSONObject) {
        val jsonObject = json.let { Json.parseToJsonElement(it.toString()) as JsonObject }
        addLog(action, logText, jsonObject)
    }

    /**
     * Clear all log entries
     */
    fun clearLog()
}
