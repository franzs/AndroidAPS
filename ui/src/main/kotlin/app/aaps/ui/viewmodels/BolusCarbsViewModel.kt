package app.aaps.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aaps.core.data.time.T
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.data.ue.ValueWithUnit
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.LTag
import app.aaps.core.interfaces.profile.ProfileFunction
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventTreatmentChange
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.DecimalFormatter
import app.aaps.ui.compose.MealLink
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
 * ViewModel for BolusCarbsScreen managing bolus, carbs, and calculator result state and business logic.
 */
class BolusCarbsViewModel @Inject constructor(
    private val persistenceLayer: PersistenceLayer,
    private val profileFunction: ProfileFunction,
    val rh: ResourceHelper,
    val dateUtil: DateUtil,
    val decimalFormatter: DecimalFormatter,
    private val rxBus: RxBus,
    private val aapsSchedulers: AapsSchedulers,
    private val aapsLogger: AAPSLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(BolusCarbsUiState())
    val uiState: StateFlow<BolusCarbsUiState> = _uiState.asStateFlow()

    private val disposable = CompositeDisposable()

    init {
        loadData()
        observeTreatmentChanges()
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    /**
     * Load meal links (boluses, carbs, and calculator results)
     */
    fun loadData() {
        viewModelScope.launch {
            // Only show loading on initial load, not on refreshes
            val currentState = _uiState.value
            if (currentState.mealLinks.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }

            try {
                val mealLinks = withContext(Dispatchers.IO) {
                    val now = System.currentTimeMillis()
                    val millsToThePast = T.days(TREATMENT_HISTORY_DAYS).msecs()

                    val boluses = if (currentState.showInvalidated) {
                        persistenceLayer.getBolusesFromTimeIncludingInvalid(now - millsToThePast, false).blockingGet()
                            .map { MealLink(bolus = it) }
                    } else {
                        persistenceLayer.getBolusesFromTime(now - millsToThePast, false).blockingGet()
                            .map { MealLink(bolus = it) }
                    }

                    val carbs = if (currentState.showInvalidated) {
                        persistenceLayer.getCarbsFromTimeIncludingInvalid(now - millsToThePast, false).blockingGet()
                            .map { MealLink(carbs = it) }
                    } else {
                        persistenceLayer.getCarbsFromTime(now - millsToThePast, false).blockingGet()
                            .map { MealLink(carbs = it) }
                    }

                    val calcs = if (currentState.showInvalidated) {
                        persistenceLayer.getBolusCalculatorResultsIncludingInvalidFromTime(now - millsToThePast, false).blockingGet()
                            .map { MealLink(bolusCalculatorResult = it) }
                    } else {
                        persistenceLayer.getBolusCalculatorResultsFromTime(now - millsToThePast, false).blockingGet()
                            .map { MealLink(bolusCalculatorResult = it) }
                    }

                    (boluses + carbs + calcs).sortedByDescending {
                        it.bolusCalculatorResult?.timestamp ?: it.bolus?.timestamp ?: it.carbs?.timestamp ?: 0L
                    }
                }

                _uiState.update {
                    it.copy(
                        mealLinks = mealLinks,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                aapsLogger.error(LTag.UI, "Failed to load bolus/carbs data", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error loading bolus/carbs data"
                    )
                }
            }
        }
    }

    /**
     * Subscribe to treatment change events
     */
    private fun observeTreatmentChanges() {
        disposable += rxBus
            .toObservable(EventTreatmentChange::class.java)
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
    fun enterSelectionMode(item: MealLink) {
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
    fun toggleSelection(item: MealLink) {
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
     * Get current profile
     */
    fun getProfile() = profileFunction.getProfile()

    /**
     * Prepare delete confirmation message
     */
    fun getDeleteConfirmationMessage(): String {
        val selected = _uiState.value.selectedItems
        if (selected.isEmpty()) return ""

        return if (selected.size == 1) {
            val ml = selected.first()
            val bolus = ml.bolus
            if (bolus != null) {
                "${rh.gs(app.aaps.core.ui.R.string.configbuilder_insulin)}: ${rh.gs(app.aaps.core.ui.R.string.format_insulin_units, bolus.amount)}\n${rh.gs(app.aaps.core.ui.R.string.date)}: ${dateUtil.dateAndTimeString(bolus.timestamp)}"
            } else {
                val carbs = ml.carbs
                if (carbs != null) {
                    "${rh.gs(app.aaps.core.ui.R.string.carbs)}: ${rh.gs(app.aaps.core.objects.R.string.format_carbs, carbs.amount.toInt())}\n${rh.gs(app.aaps.core.ui.R.string.date)}: ${dateUtil.dateAndTimeString(carbs.timestamp)}"
                } else {
                    rh.gs(app.aaps.core.ui.R.string.confirm_remove_multiple_items, selected.size)
                }
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
                selected.forEach { ml ->
                    ml.bolus?.let { bolus ->
                        persistenceLayer.invalidateBolus(
                            bolus.id,
                            action = Action.BOLUS_REMOVED,
                            source = Sources.Treatments,
                            listValues = listOf(
                                ValueWithUnit.Timestamp(bolus.timestamp),
                                ValueWithUnit.Insulin(bolus.amount)
                            )
                        ).blockingGet()
                    }
                    ml.carbs?.let { carb ->
                        persistenceLayer.invalidateCarbs(
                            carb.id,
                            action = Action.CARBS_REMOVED,
                            source = Sources.Treatments,
                            listValues = listOf(
                                ValueWithUnit.Timestamp(carb.timestamp),
                                ValueWithUnit.Gram(carb.amount.toInt())
                            )
                        ).blockingGet()
                    }
                    ml.bolusCalculatorResult?.let { bolusCalculatorResult ->
                        persistenceLayer.invalidateBolusCalculatorResult(
                            bolusCalculatorResult.id,
                            action = Action.BOLUS_CALCULATOR_RESULT_REMOVED,
                            source = Sources.Treatments,
                            listValues = listOf(ValueWithUnit.Timestamp(bolusCalculatorResult.timestamp))
                        ).blockingGet()
                    }
                }
                exitSelectionMode()
                loadData()
            } catch (e: Exception) {
                aapsLogger.error(LTag.UI, "Failed to delete treatments", e)
                _uiState.update { it.copy(error = e.message ?: "Unknown error deleting treatments") }
            }
        }
    }
}

/**
 * UI state for BolusCarbsScreen
 */
data class BolusCarbsUiState(
    val mealLinks: List<MealLink> = emptyList(),
    val isLoading: Boolean = true,
    val showInvalidated: Boolean = false,
    val isRemovingMode: Boolean = false,
    val selectedItems: Set<MealLink> = emptySet(),
    val error: String? = null
)
