package app.aaps.ui.viewmodels.base

import androidx.lifecycle.ViewModel
import app.aaps.core.data.time.T
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.utils.DateUtil
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Base ViewModel for treatment screens with common functionality.
 *
 * @param T The data model type (TB, TT, EB, etc.)
 * @param S The UI state type that must implement TreatmentUiState
 */
abstract class BaseTreatmentViewModel<T : Any, S : TreatmentUiState<T>>(
    protected val persistenceLayer: PersistenceLayer,
    val rh: ResourceHelper,
    val dateUtil: DateUtil,
    protected val rxBus: RxBus,
    protected val aapsSchedulers: AapsSchedulers
) : ViewModel() {

    protected abstract val _uiState: MutableStateFlow<S>
    abstract val uiState: StateFlow<S>

    protected val disposable = CompositeDisposable()

    companion object {

        /** Default time range for treatment history */
        const val TREATMENT_HISTORY_DAYS = 30L

        /** Debounce duration for RxJava events in seconds */
        const val EVENT_DEBOUNCE_SECONDS = 1L
    }

    init {
        loadData()
        observeDataChanges()
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    /**
     * Load treatment data from persistence layer
     */
    abstract fun loadData()

    /**
     * Subscribe to RxJava event changes
     */
    protected abstract fun observeDataChanges()

    /**
     * Get time range for data loading
     */
    protected fun getTimeRange(): Long = T.days(TREATMENT_HISTORY_DAYS).msecs()
}

/**
 * Base UI state interface for treatment screens
 */
interface TreatmentUiState<T : Any> {

    val items: List<T>
    val isLoading: Boolean
    val error: String?
}

/**
 * Interface for treatment ViewModels that support selection mode
 */
interface SelectableViewModel<T : Any> {

    val uiState: StateFlow<out SelectableTreatmentUiState<T>>

    fun toggleInvalidated()
    fun enterSelectionMode(item: T)
    fun exitSelectionMode()
    fun toggleSelection(item: T)
    fun deleteSelected()
    fun getDeleteConfirmationMessage(): String
}

/**
 * UI state for treatment screens with selection support
 */
interface SelectableTreatmentUiState<T : Any> : TreatmentUiState<T> {

    val showInvalidated: Boolean
    val isRemovingMode: Boolean
    val selectedItems: Set<T>
}

/**
 * Base implementation for selectable treatment ViewModels
 */
abstract class SelectableTreatmentViewModel<T : Any, S : SelectableTreatmentUiState<T>>(
    persistenceLayer: PersistenceLayer,
    rh: ResourceHelper,
    dateUtil: DateUtil,
    rxBus: RxBus,
    aapsSchedulers: AapsSchedulers
) : BaseTreatmentViewModel<T, S>(persistenceLayer, rh, dateUtil, rxBus, aapsSchedulers),
    SelectableViewModel<T> {

    /**
     * Toggle show/hide invalidated items
     */
    override fun toggleInvalidated() {
        updateState { toggleInvalidatedState(it) }
        loadData()
    }

    /**
     * Enter selection mode with initial item selected
     */
    override fun enterSelectionMode(item: T) {
        updateState { enterSelectionModeState(it, item) }
    }

    /**
     * Exit selection mode and clear selection
     */
    override fun exitSelectionMode() {
        updateState { exitSelectionModeState(it) }
    }

    /**
     * Toggle selection of an item
     */
    override fun toggleSelection(item: T) {
        updateState { state ->
            val newSelection = if (item in getSelectedItems(state)) {
                getSelectedItems(state) - item
            } else {
                getSelectedItems(state) + item
            }
            updateSelectedItems(state, newSelection)
        }
    }

    /**
     * Update state using the provided transform function
     */
    protected abstract fun updateState(transform: (S) -> S)

    /**
     * Toggle invalidated state
     */
    protected abstract fun toggleInvalidatedState(state: S): S

    /**
     * Enter selection mode state
     */
    protected abstract fun enterSelectionModeState(state: S, item: T): S

    /**
     * Exit selection mode state
     */
    protected abstract fun exitSelectionModeState(state: S): S

    /**
     * Get selected items from state
     */
    protected abstract fun getSelectedItems(state: S): Set<T>

    /**
     * Update selected items in state
     */
    protected abstract fun updateSelectedItems(state: S, items: Set<T>): S
}
