package app.aaps.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aaps.core.data.model.UE
import app.aaps.core.data.time.T
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.LTag
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventNewHistoryData
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.ui.compose.MenuItemData
import app.aaps.ui.compose.ToolbarConfig
import app.aaps.ui.compose.TreatmentScreenToolbar
import app.aaps.ui.viewmodels.TreatmentConstants.EVENT_DEBOUNCE_SECONDS
import app.aaps.ui.viewmodels.TreatmentConstants.USER_ENTRY_FILTERED_DAYS
import app.aaps.ui.viewmodels.TreatmentConstants.USER_ENTRY_UNFILTERED_DAYS
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * ViewModel for UserEntryScreen managing user entry log state and business logic.
 */
class UserEntryViewModel @Inject constructor(
    private val persistenceLayer: PersistenceLayer,
    val rh: ResourceHelper,
    val dateUtil: DateUtil,
    private val rxBus: RxBus,
    private val aapsSchedulers: AapsSchedulers,
    private val aapsLogger: AAPSLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserEntryUiState())
    val uiState: StateFlow<UserEntryUiState> = _uiState.asStateFlow()

    private val disposable = CompositeDisposable()

    private val millsToThePastFiltered = T.days(USER_ENTRY_FILTERED_DAYS).msecs()
    private val millsToThePastUnFiltered = T.days(USER_ENTRY_UNFILTERED_DAYS).msecs()

    init {
        loadData()
        observeHistoryDataChanges()
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    /**
     * Load user entries
     */
    fun loadData() {
        viewModelScope.launch {
            // Only show loading on initial load, not on refreshes
            val currentState = _uiState.value
            if (currentState.userEntries.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }

            try {
                val userEntries = withContext(Dispatchers.IO) {
                    val now = System.currentTimeMillis()
                    if (currentState.showLoop) {
                        persistenceLayer.getUserEntryDataFromTime(now - millsToThePastUnFiltered).blockingGet()
                    } else {
                        persistenceLayer.getUserEntryFilteredDataFromTime(now - millsToThePastFiltered).blockingGet()
                    }
                }

                _uiState.update {
                    it.copy(
                        userEntries = userEntries,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                aapsLogger.error(LTag.UI, "Failed to load user entries", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error loading user entries"
                    )
                }
            }
        }
    }

    /**
     * Subscribe to history data change events
     */
    private fun observeHistoryDataChanges() {
        disposable += rxBus
            .toObservable(EventNewHistoryData::class.java)
            .observeOn(aapsSchedulers.io)
            .debounce(EVENT_DEBOUNCE_SECONDS, TimeUnit.SECONDS)
            .subscribe {
                loadData()
            }
    }

    /**
     * Clear error state
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Toggle show/hide loop records
     */
    fun toggleLoop() {
        _uiState.update { it.copy(showLoop = !it.showLoop) }
        loadData()
    }

    /**
     * Get toolbar configuration for current state
     */
    fun getToolbarConfig(onNavigateBack: () -> Unit, menuItems: List<MenuItemData>): ToolbarConfig {
        val state = _uiState.value
        return TreatmentScreenToolbar(
            isRemovingMode = false,
            selectedCount = 0,
            onExitRemovingMode = { },
            onNavigateBack = onNavigateBack,
            onDelete = { },
            rh = rh,
            showLoop = state.showLoop,
            onToggleLoop = { toggleLoop() },
            menuItems = menuItems
        )
    }
}

/**
 * UI state for UserEntryScreen
 */
data class UserEntryUiState(
    val userEntries: List<UE> = emptyList(),
    val isLoading: Boolean = true,
    val showLoop: Boolean = false,
    val error: String? = null
)
