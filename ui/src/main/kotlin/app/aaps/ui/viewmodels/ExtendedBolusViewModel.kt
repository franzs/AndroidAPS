package app.aaps.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aaps.core.data.model.EB
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
import app.aaps.core.interfaces.rx.events.EventExtendedBolusChange
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.ui.compose.ToolbarConfig
import app.aaps.ui.compose.TreatmentScreenToolbar
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
 * ViewModel for ExtendedBolusScreen managing extended bolus state and business logic.
 */
class ExtendedBolusViewModel @Inject constructor(
    private val persistenceLayer: PersistenceLayer,
    val rh: ResourceHelper,
    val dateUtil: DateUtil,
    private val rxBus: RxBus,
    private val aapsSchedulers: AapsSchedulers,
    private val aapsLogger: AAPSLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExtendedBolusUiState())
    val uiState: StateFlow<ExtendedBolusUiState> = _uiState.asStateFlow()

    private val disposable = CompositeDisposable()

    init {
        loadData()
        observeExtendedBolusChanges()
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    /**
     * Load extended boluses
     */
    fun loadData() {
        viewModelScope.launch {
            // Only show loading on initial load, not on refreshes
            val currentState = _uiState.value
            if (currentState.extendedBoluses.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }

            try {
                val extendedBoluses = withContext(Dispatchers.IO) {
                    val now = System.currentTimeMillis()
                    val millsToThePast = T.days(TREATMENT_HISTORY_DAYS).msecs()

                    if (currentState.showInvalidated) {
                        persistenceLayer.getExtendedBolusStartingFromTimeIncludingInvalid(now - millsToThePast, false).blockingGet()
                    } else {
                        persistenceLayer.getExtendedBolusesStartingFromTime(now - millsToThePast, false).blockingGet()
                    }
                }

                _uiState.update {
                    it.copy(
                        extendedBoluses = extendedBoluses,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                aapsLogger.error(LTag.UI, "Failed to load extended boluses", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error loading extended boluses"
                    )
                }
            }
        }
    }

    /**
     * Subscribe to extended bolus change events
     */
    private fun observeExtendedBolusChanges() {
        disposable += rxBus
            .toObservable(EventExtendedBolusChange::class.java)
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
    fun enterSelectionMode(item: EB) {
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
    fun toggleSelection(item: EB) {
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
     * Prepare delete confirmation message
     */
    fun getDeleteConfirmationMessage(): String {
        val selected = _uiState.value.selectedItems
        if (selected.isEmpty()) return ""

        return if (selected.size == 1) {
            val eb = selected.first()
            "${rh.gs(app.aaps.core.ui.R.string.extended_bolus)}\n${rh.gs(app.aaps.core.ui.R.string.date)}: ${dateUtil.dateAndTimeString(eb.timestamp)}"
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
                selected.forEach { eb ->
                    persistenceLayer.invalidateExtendedBolus(
                        id = eb.id,
                        action = Action.EXTENDED_BOLUS_REMOVED,
                        source = Sources.Treatments,
                        listValues = listOf(
                            ValueWithUnit.Timestamp(eb.timestamp),
                            ValueWithUnit.Insulin(eb.amount),
                            ValueWithUnit.UnitPerHour(eb.rate),
                            ValueWithUnit.Minute(TimeUnit.MILLISECONDS.toMinutes(eb.duration).toInt())
                        )
                    ).blockingGet()
                }
                exitSelectionMode()
                loadData()
            } catch (e: Exception) {
                aapsLogger.error(LTag.UI, "Failed to delete extended boluses", e)
                _uiState.update { it.copy(error = e.message ?: "Unknown error deleting extended boluses") }
            }
        }
    }

    /**
     * Get toolbar configuration for current state
     */
    fun getToolbarConfig(onNavigateBack: () -> Unit, onDeleteClick: () -> Unit): ToolbarConfig {
        val state = _uiState.value
        return TreatmentScreenToolbar(
            isRemovingMode = state.isRemovingMode,
            selectedCount = state.selectedItems.size,
            onExitRemovingMode = { exitSelectionMode() },
            onNavigateBack = onNavigateBack,
            onDelete = {
                if (state.selectedItems.isNotEmpty()) {
                    onDeleteClick()
                }
            },
            rh = rh,
            showInvalidated = state.showInvalidated,
            onToggleInvalidated = { toggleInvalidated() }
        )
    }
}

/**
 * UI state for ExtendedBolusScreen
 */
data class ExtendedBolusUiState(
    val extendedBoluses: List<EB> = emptyList(),
    val isLoading: Boolean = true,
    val showInvalidated: Boolean = false,
    val isRemovingMode: Boolean = false,
    val selectedItems: Set<EB> = emptySet(),
    val error: String? = null
)
