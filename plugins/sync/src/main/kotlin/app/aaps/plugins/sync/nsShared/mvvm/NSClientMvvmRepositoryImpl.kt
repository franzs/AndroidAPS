package app.aaps.plugins.sync.nsShared.mvvm

import app.aaps.core.interfaces.nsclient.NSClientMvvmRepository
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventSWSyncStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NSClientMvvmRepositoryImpl @Inject constructor(
    private val rxBus: RxBus
) : NSClientMvvmRepository {

    // Repository scope for suspending operations
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // State flows - always hold a current value
    private val _queueSize = MutableStateFlow(-1L)
    override val queueSize: StateFlow<Long> = _queueSize.asStateFlow()

    private val _statusUpdate = MutableStateFlow("")
    override val statusUpdate: StateFlow<String> = _statusUpdate.asStateFlow()

    private val _urlUpdate = MutableStateFlow("")
    override val urlUpdate: StateFlow<String> = _urlUpdate.asStateFlow()

    // Event flow - for one-time events (logs) with larger buffer to prevent drops
    private val _newLog = MutableSharedFlow<Pair<String, String?>>(
        extraBufferCapacity = 100,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    override val newLogItem: SharedFlow<Pair<String, String?>> = _newLog.asSharedFlow()

    override fun updateQueueSize(size: Long) {
        _queueSize.value = size
    }

    override fun updateStatus(status: String) {
        _statusUpdate.value = status
        // Pass new status to SetupWizard if open
        rxBus.send(EventSWSyncStatus(status))
    }

    override fun updateUrl(url: String) {
        _urlUpdate.value = url
    }

    override fun addLog(action: String, logText: String?) {
        // Use emit in coroutine to guarantee delivery
        scope.launch {
            _newLog.emit(Pair(action, logText))
        }
    }
}