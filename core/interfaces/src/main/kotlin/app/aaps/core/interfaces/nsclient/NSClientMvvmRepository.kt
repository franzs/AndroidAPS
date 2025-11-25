package app.aaps.core.interfaces.nsclient

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

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
     * Log event flow - emits new log entries as they occur
     */
    val newLogItem: SharedFlow<Pair<String, String?>>

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
     * Send new log entry to NSClient fragment
     */
    fun addLog(action: String, logText: String?)
}