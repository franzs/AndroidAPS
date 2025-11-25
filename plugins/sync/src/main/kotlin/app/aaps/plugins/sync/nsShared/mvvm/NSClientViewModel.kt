package app.aaps.plugins.sync.nsShared.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.LTag
import app.aaps.core.interfaces.nsclient.NSClientMvvmRepository
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.resources.ResourceHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NSClientUiState(
    val url: String = "",
    val status: String = "",
    val queue: String = "",
    val logList: List<NSClientLog> = emptyList()
)

class NSClientViewModel @Inject constructor(
    private val rh: ResourceHelper,
    private val aapsLogger: AAPSLogger,
    private val activePlugin: ActivePlugin,
    private val nsClientMvvmRepository: NSClientMvvmRepository
) : ViewModel() {

    private val logList = mutableListOf<NSClientLog>()
    private val maxLogLines = 100

    private val nsClientPlugin get() = activePlugin.activeNsClient

    // UI state
    private val _uiState = MutableStateFlow(NSClientUiState())
    val uiState: StateFlow<NSClientUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            nsClientMvvmRepository.queueSize.collect { size ->
                val queueText = if (size >= 0) size.toString() else rh.gs(app.aaps.core.ui.R.string.value_unavailable_short)
                _uiState.update { it.copy(queue = queueText) }
            }
        }
        viewModelScope.launch {
            nsClientMvvmRepository.statusUpdate.collect { status ->
                _uiState.update { it.copy(status = status) }
            }
        }
        viewModelScope.launch {
            nsClientMvvmRepository.newLogItem.collect { event ->
                addToLog(event)
            }
        }
    }

    private fun addToLog(newEntry: Pair<String, String?>) {
        synchronized(logList) {
            aapsLogger.debug(LTag.NSCLIENT, newEntry.first + " " + newEntry.second)
            logList.add(0, NSClientLog(newEntry))  // Add to beginning
            // Remove oldest if log is too large
            if (logList.size >= maxLogLines) logList.removeAt(logList.size - 1)
            _uiState.update { it.copy(logList = logList.toList()) }
        }
    }

    fun clearLog() {
        synchronized(logList) {
            logList.clear()
            _uiState.update { it.copy(logList = logList.toList()) }
        }
    }

    fun loadInitialData() {
        _uiState.update {
            it.copy(
                url = nsClientPlugin?.address ?: ""
            )
        }
    }
}
