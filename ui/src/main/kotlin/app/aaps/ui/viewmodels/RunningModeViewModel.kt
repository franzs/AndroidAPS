package app.aaps.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aaps.core.data.model.RM
import app.aaps.core.data.time.T
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.data.ue.ValueWithUnit
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.LTag
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventRunningModeChange
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.ui.viewmodels.TreatmentConstants.EVENT_DEBOUNCE_SECONDS
import app.aaps.ui.viewmodels.TreatmentConstants.TREATMENT_HISTORY_DAYS
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
 * ViewModel for RunningModeScreen managing running mode state and business logic.
 */
class RunningModeViewModel @Inject constructor(
    private val persistenceLayer: PersistenceLayer,
    val rh: ResourceHelper,
    val dateUtil: DateUtil,
    private val rxBus: RxBus,
    private val aapsSchedulers: AapsSchedulers,
    private val aapsLogger: AAPSLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(RunningModeUiState())
    val uiState: StateFlow<RunningModeUiState> = _uiState.asStateFlow()

    private val disposable = CompositeDisposable()

    init {
        loadData()
        observeRunningModeChanges()
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    /**
     * Load running modes
     */
    fun loadData() {
        viewModelScope.launch {
            // Only show loading on initial load, not on refreshes
            val currentState = _uiState.value
            if (currentState.runningModes.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }

            try {
                val runningModes = withContext(Dispatchers.IO) {
                    val now = System.currentTimeMillis()
                    val millsToThePast = T.days(TREATMENT_HISTORY_DAYS).msecs()

                    if (currentState.showInvalidated) {
                        persistenceLayer.getRunningModesIncludingInvalidFromTime(now - millsToThePast, false).blockingGet()
                    } else {
                        persistenceLayer.getRunningModesFromTime(now - millsToThePast, false).blockingGet()
                    }
                }

                _uiState.update {
                    it.copy(
                        runningModes = runningModes,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                aapsLogger.error(LTag.UI, "Failed to load running modes", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error loading running modes"
                    )
                }
            }
        }
    }

    /**
     * Subscribe to running mode change events
     */
    private fun observeRunningModeChanges() {
        disposable += rxBus
            .toObservable(EventRunningModeChange::class.java)
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
     * Toggle show/hide invalidated items
     */
    fun toggleInvalidated() {
        _uiState.update { it.copy(showInvalidated = !it.showInvalidated) }
        loadData()
    }

    /**
     * Enter selection mode with initial item selected
     */
    fun enterSelectionMode(item: RM) {
        _uiState.update {
            it.copy(
                isRemovingMode = true,
                selectedItems = setOf(item)
            )
        }
    }

    /**
     * Exit selection mode and clear selection
     */
    fun exitSelectionMode() {
        _uiState.update {
            it.copy(
                isRemovingMode = false,
                selectedItems = emptySet()
            )
        }
    }

    /**
     * Toggle selection of an item
     */
    fun toggleSelection(item: RM) {
        _uiState.update { state ->
            val newSelection = if (item in state.selectedItems) {
                state.selectedItems - item
            } else {
                state.selectedItems + item
            }
            state.copy(selectedItems = newSelection)
        }
    }

    /**
     * Get currently active running mode
     */
    fun getActiveMode(): RM {
        return persistenceLayer.getRunningModeActiveAt(dateUtil.now())
    }

    /**
     * Prepare delete confirmation message
     */
    fun getDeleteConfirmationMessage(): String {
        val selected = _uiState.value.selectedItems
        if (selected.isEmpty()) return ""

        return if (selected.size == 1) {
            val rm = selected.first()
            "${rh.gs(app.aaps.core.ui.R.string.running_mode)}: ${rm.mode.name}\n${dateUtil.dateAndTimeString(rm.timestamp)}"
        } else {
            rh.gs(app.aaps.core.ui.R.string.confirm_remove_multiple_items, selected.size)
        }
    }

    /**
     * Delete selected items
     */
    fun deleteSelected() {
        val selected = _uiState.value.selectedItems
        if (selected.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                selected.forEach { rm ->
                    persistenceLayer.invalidateRunningMode(
                        id = rm.id,
                        action = Action.LOOP_REMOVED,
                        source = Sources.Treatments,
                        note = null,
                        listValues = listOfNotNull(
                            ValueWithUnit.Timestamp(rm.timestamp),
                            ValueWithUnit.RMMode(rm.mode),
                            ValueWithUnit.Minute(TimeUnit.MILLISECONDS.toMinutes(rm.duration).toInt())
                        )
                    ).blockingGet()
                }
                exitSelectionMode()
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

}

/**
 * UI state for RunningModeScreen
 */
data class RunningModeUiState(
    val runningModes: List<RM> = emptyList(),
    val isLoading: Boolean = true,
    val showInvalidated: Boolean = false,
    val isRemovingMode: Boolean = false,
    val selectedItems: Set<RM> = emptySet(),
    val error: String? = null
)
