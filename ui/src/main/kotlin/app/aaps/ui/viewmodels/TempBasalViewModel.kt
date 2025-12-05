package app.aaps.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aaps.core.data.model.TB
import app.aaps.core.data.time.T
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.data.ue.ValueWithUnit
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.LTag
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.profile.ProfileFunction
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventTempBasalChange
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.DecimalFormatter
import app.aaps.core.objects.extensions.toStringFull
import app.aaps.core.objects.extensions.toTemporaryBasal
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
 * ViewModel for TempBasalScreen managing temporary basal state and business logic.
 */
class TempBasalViewModel @Inject constructor(
    private val persistenceLayer: PersistenceLayer,
    private val profileFunction: ProfileFunction,
    private val activePlugin: ActivePlugin,
    val rh: ResourceHelper,
    val dateUtil: DateUtil,
    val decimalFormatter: DecimalFormatter,
    private val rxBus: RxBus,
    private val aapsSchedulers: AapsSchedulers,
    private val aapsLogger: AAPSLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(TempBasalUiState())
    val uiState: StateFlow<TempBasalUiState> = _uiState.asStateFlow()

    private val disposable = CompositeDisposable()

    init {
        loadData()
        observeTempBasalChanges()
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    /**
     * Load temp basals and potentially extended boluses (if pump is faking temps)
     */
    fun loadData() {
        viewModelScope.launch {
            // Only show loading on initial load, not on refreshes
            val currentState = _uiState.value
            if (currentState.tempBasals.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }

            try {
                val tempBasals = withContext(Dispatchers.IO) {
                    val now = System.currentTimeMillis()
                    val millsToThePast = T.days(TREATMENT_HISTORY_DAYS).msecs()
                    val isFakingTempsByExtendedBoluses = activePlugin.activePump.isFakingTempsByExtendedBoluses

                    val tempBasalsList = if (currentState.showInvalidated) {
                        persistenceLayer.getTemporaryBasalsStartingFromTimeIncludingInvalid(now - millsToThePast, false).blockingGet()
                    } else {
                        persistenceLayer.getTemporaryBasalsStartingFromTime(now - millsToThePast, false).blockingGet()
                    }

                    if (isFakingTempsByExtendedBoluses) {
                        val extendedBolusList = if (currentState.showInvalidated) {
                            persistenceLayer.getExtendedBolusStartingFromTimeIncludingInvalid(now - millsToThePast, false).blockingGet()
                        } else {
                            persistenceLayer.getExtendedBolusesStartingFromTime(now - millsToThePast, false).blockingGet()
                        }

                        val convertedExtendedBoluses = extendedBolusList.mapNotNull { eb ->
                            profileFunction.getProfile(eb.timestamp)?.let { profile ->
                                eb.toTemporaryBasal(profile)
                            }
                        }

                        (tempBasalsList + convertedExtendedBoluses).sortedByDescending { it.timestamp }
                    } else {
                        tempBasalsList.sortedByDescending { it.timestamp }
                    }
                }

                _uiState.update {
                    it.copy(
                        tempBasals = tempBasals,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                aapsLogger.error(LTag.UI, "Failed to load temp basals", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error loading temp basals"
                    )
                }
            }
        }
    }

    /**
     * Subscribe to temp basal change events
     */
    private fun observeTempBasalChanges() {
        disposable += rxBus
            .toObservable(EventTempBasalChange::class.java)
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
    fun enterSelectionMode(item: TB) {
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
    fun toggleSelection(item: TB) {
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
            val tempBasal = selected.first()
            val isFakeExtended = tempBasal.type == TB.Type.FAKE_EXTENDED
            val profile = profileFunction.getProfile(dateUtil.now())
            if (profile != null) {
                "${if (isFakeExtended) rh.gs(app.aaps.core.ui.R.string.extended_bolus) else rh.gs(app.aaps.core.ui.R.string.tempbasal_label)}: ${
                    tempBasal.toStringFull(profile, dateUtil, rh)
                }\n${rh.gs(app.aaps.core.ui.R.string.date)}: ${dateUtil.dateAndTimeString(tempBasal.timestamp)}"
            } else {
                rh.gs(app.aaps.core.ui.R.string.confirm_remove_multiple_items, selected.size)
            }
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
                selected.forEach { tempBasal ->
                    val isFakeExtended = tempBasal.type == TB.Type.FAKE_EXTENDED
                    if (isFakeExtended) {
                        // For fake extended boluses, delete the underlying extended bolus
                        val extendedBolus = persistenceLayer.getExtendedBolusActiveAt(tempBasal.timestamp)
                        if (extendedBolus != null) {
                            persistenceLayer.invalidateExtendedBolus(
                                id = extendedBolus.id,
                                action = Action.EXTENDED_BOLUS_REMOVED,
                                source = Sources.Treatments,
                                listValues = listOf(
                                    ValueWithUnit.Timestamp(extendedBolus.timestamp),
                                    ValueWithUnit.Insulin(extendedBolus.amount),
                                    ValueWithUnit.UnitPerHour(extendedBolus.rate),
                                    ValueWithUnit.Minute(TimeUnit.MILLISECONDS.toMinutes(extendedBolus.duration).toInt())
                                )
                            ).blockingGet()
                        }
                    } else {
                        // Delete regular temp basal
                        persistenceLayer.invalidateTemporaryBasal(
                            id = tempBasal.id,
                            action = Action.TEMP_BASAL_REMOVED,
                            source = Sources.Treatments,
                            listValues = listOf(
                                ValueWithUnit.Timestamp(tempBasal.timestamp),
                                if (tempBasal.isAbsolute) ValueWithUnit.UnitPerHour(tempBasal.rate) else ValueWithUnit.Percent(tempBasal.rate.toInt()),
                                ValueWithUnit.Minute(T.msecs(tempBasal.duration).mins().toInt())
                            )
                        ).blockingGet()
                    }
                }
                exitSelectionMode()
                loadData()
            } catch (e: Exception) {
                aapsLogger.error(LTag.UI, "Failed to delete temp basals", e)
                _uiState.update { it.copy(error = e.message ?: "Unknown error deleting temp basals") }
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
 * UI state for TempBasalScreen
 */
data class TempBasalUiState(
    val tempBasals: List<TB> = emptyList(),
    val isLoading: Boolean = true,
    val showInvalidated: Boolean = false,
    val isRemovingMode: Boolean = false,
    val selectedItems: Set<TB> = emptySet(),
    val error: String? = null
)
