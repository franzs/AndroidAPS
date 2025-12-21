package app.aaps.plugins.sync.xdrip.mvvm

import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.LTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XdripMvvmRepositoryImpl @Inject constructor(
    private val aapsLogger: AAPSLogger
) : XdripMvvmRepository {

    companion object {

        private const val MAX_LOG_ENTRIES = 100
    }

    // State flows - always hold a current value
    private val _queueSize = MutableStateFlow(-1L)
    override val queueSize: StateFlow<Long> = _queueSize.asStateFlow()

    // Log list as state - maintains history
    private val _logList = MutableStateFlow<List<XdripLog>>(emptyList())
    override val logList: StateFlow<List<XdripLog>> = _logList.asStateFlow()

    override fun updateQueueSize(size: Long) {
        _queueSize.value = size
    }

    override fun addLog(action: String, logText: String?) {
        _logList.update { currentList ->
            aapsLogger.debug(LTag.XDRIP, "$action $logText")
            val newLog = XdripLog(
                action = action,
                logText = logText
            )
            // Add to beginning and keep only last MAX_LOG_ENTRIES
            listOf(newLog) + currentList.take(MAX_LOG_ENTRIES - 1)
        }
    }

    override fun clearLog() {
        _logList.value = emptyList()
    }
}
