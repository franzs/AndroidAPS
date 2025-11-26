package app.aaps.plugins.sync.nsShared.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aaps.core.interfaces.nsclient.NSClientLog
import app.aaps.core.interfaces.nsclient.NSClientMvvmRepository
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.plugins.sync.nsclientV3.keys.NsclientBooleanKey
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
    val paused: Boolean = false,
    val logList: List<NSClientLog> = emptyList()
)

class NSClientViewModel @Inject constructor(
    private val rh: ResourceHelper,
    private val activePlugin: ActivePlugin,
    private val nsClientMvvmRepository: NSClientMvvmRepository,
    private val preferences: Preferences
) : ViewModel() {

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
            nsClientMvvmRepository.logList.collect { logList ->
                _uiState.update { it.copy(logList = logList) }
            }
        }
    }

    fun loadInitialData() {
        _uiState.update {
            it.copy(
                url = nsClientPlugin?.address ?: "",
                paused = preferences.get(NsclientBooleanKey.NsPaused)
            )
        }
    }

    fun updatePaused(paused: Boolean) {
        _uiState.update { it.copy(paused = paused) }
    }
}
