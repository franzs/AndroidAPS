package app.aaps.plugins.sync.xdrip.mvvm

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for Xdrip MVVM repository
 */
interface XdripMvvmRepository {

    /**
     * Queue size state flow - always has a current value
     */
    val queueSize: StateFlow<Long>

    /**
     * Log list state flow - always has current log history
     */
    val logList: StateFlow<List<XdripLog>>

    /**
     * Update queue size
     */
    fun updateQueueSize(size: Long)

    /**
     * Add new log entry
     */
    fun addLog(action: String, logText: String?)

    /**
     * Clear all log entries
     */
    fun clearLog()
}
