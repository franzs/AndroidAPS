package app.aaps.plugins.sync.tidepool.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aaps.plugins.sync.tidepool.auth.AuthFlowOut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TidepoolUiState(
    val connectionStatus: String = "",
    val logList: List<TidepoolLog> = emptyList()
)

class TidepoolViewModel @Inject constructor(
    private val tidepoolMvvmRepository: TidepoolMvvmRepository,
    private val authFlowOut: AuthFlowOut
) : ViewModel() {

    private val _uiState = MutableStateFlow(TidepoolUiState())
    val uiState: StateFlow<TidepoolUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tidepoolMvvmRepository.connectionStatus.collect { status ->
                _uiState.update { it.copy(connectionStatus = status.name) }
            }
        }
        viewModelScope.launch {
            tidepoolMvvmRepository.logList.collect { logList ->
                _uiState.update { it.copy(logList = logList) }
            }
        }
    }

    fun loadInitialData() {
        tidepoolMvvmRepository.updateConnectionStatus(authFlowOut.connectionStatus)
    }
}
