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
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventEffectiveProfileSwitchChanged
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.objects.profile.ProfileSealed
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
 * ViewModel for ProfileSwitchScreen managing profile switch state and business logic.
 */
class ProfileSwitchViewModel @Inject constructor(
    private val persistenceLayer: PersistenceLayer,
    val rh: ResourceHelper,
    val dateUtil: DateUtil,
    private val rxBus: RxBus,
    private val aapsSchedulers: AapsSchedulers,
    private val aapsLogger: AAPSLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSwitchUiState())
    val uiState: StateFlow<ProfileSwitchUiState> = _uiState.asStateFlow()

    private val disposable = CompositeDisposable()

    init {
        loadData()
        observeProfileSwitchChanges()
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    /**
     * Load profile switches (both PS and EPS)
     */
    fun loadData() {
        viewModelScope.launch {
            // Only show loading on initial load, not on refreshes
            val currentState = _uiState.value
            if (currentState.profileSwitches.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }

            try {
                val profileSwitches = withContext(Dispatchers.IO) {
                    val now = System.currentTimeMillis()
                    val millsToThePast = T.days(TREATMENT_HISTORY_DAYS).msecs()

                    val ps = if (currentState.showInvalidated) {
                        persistenceLayer.getProfileSwitchesIncludingInvalidFromTime(now - millsToThePast, false).blockingGet()
                    } else {
                        persistenceLayer.getProfileSwitchesFromTime(now - millsToThePast, false).blockingGet()
                    }

                    val eps = if (currentState.showInvalidated) {
                        persistenceLayer.getEffectiveProfileSwitchesIncludingInvalidFromTime(now - millsToThePast, false).blockingGet()
                    } else {
                        persistenceLayer.getEffectiveProfileSwitchesFromTime(now - millsToThePast, false).blockingGet()
                    }

                    (ps.map { ProfileSealed.PS(value = it, activePlugin = null) } +
                        eps.map { ProfileSealed.EPS(value = it, activePlugin = null) })
                        .sortedByDescending { it.timestamp }
                }

                _uiState.update {
                    it.copy(
                        profileSwitches = profileSwitches,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                aapsLogger.error(LTag.UI, "Failed to load profile switches", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error loading profile switches"
                    )
                }
            }
        }
    }

    /**
     * Subscribe to profile switch change events
     */
    private fun observeProfileSwitchChanges() {
        disposable += rxBus
            .toObservable(EventEffectiveProfileSwitchChanged::class.java)
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
    fun enterSelectionMode(item: ProfileSealed) {
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
    fun toggleSelection(item: ProfileSealed) {
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
     * Get currently active effective profile switch
     */
    fun getActiveProfile(): ProfileSealed? {
        val eps = persistenceLayer.getEffectiveProfileSwitchActiveAt(dateUtil.now())
        return eps?.let { ProfileSealed.EPS(value = it, activePlugin = null) }
    }

    /**
     * Prepare delete confirmation message
     */
    fun getDeleteConfirmationMessage(): String {
        val selected = _uiState.value.selectedItems
        if (selected.isEmpty()) return ""

        return if (selected.size == 1) {
            val ps = selected.first()
            "${rh.gs(app.aaps.core.ui.R.string.careportal_profileswitch)}: ${ps.profileName}\n${dateUtil.dateAndTimeString(ps.timestamp)}"
        } else {
            rh.gs(app.aaps.core.ui.R.string.confirm_remove_multiple_items, selected.size)
        }
    }

    /**
     * Delete selected items (only PS can be deleted, not EPS)
     */
    fun deleteSelected() {
        val selected = _uiState.value.selectedItems
        if (selected.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                selected.forEach { profileSwitch ->
                    if (profileSwitch is ProfileSealed.PS) {
                        persistenceLayer.invalidateProfileSwitch(
                            id = profileSwitch.id,
                            action = Action.PROFILE_SWITCH_REMOVED,
                            source = Sources.Treatments,
                            note = profileSwitch.profileName,
                            listValues = listOf(
                                ValueWithUnit.Timestamp(profileSwitch.timestamp)
                            )
                        ).blockingGet()
                    }
                }
                exitSelectionMode()
                loadData()
            } catch (e: Exception) {
                aapsLogger.error(LTag.UI, "Failed to delete profile switches", e)
                _uiState.update { it.copy(error = e.message ?: "Unknown error deleting profile switches") }
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
 * UI state for ProfileSwitchScreen
 */
data class ProfileSwitchUiState(
    val profileSwitches: List<ProfileSealed> = emptyList(),
    val isLoading: Boolean = true,
    val showInvalidated: Boolean = false,
    val isRemovingMode: Boolean = false,
    val selectedItems: Set<ProfileSealed> = emptySet(),
    val error: String? = null
)
