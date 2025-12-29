package app.aaps.plugins.main.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aaps.core.data.model.GlucoseUnit
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.LTag
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.profile.ProfileFunction
import app.aaps.core.interfaces.profile.ProfileSource
import app.aaps.core.interfaces.protection.ProtectionCheck
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventLocalProfileChanged
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.HardLimits
import app.aaps.core.objects.profile.ProfileSealed
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class TimeValue(
    val timeSeconds: Int,
    val value: Double
)

data class SingleProfileState(
    val name: String = "",
    val mgdl: Boolean = true,
    val dia: Double = 5.0,
    val ic: List<TimeValue> = listOf(TimeValue(0, 0.0)),
    val isf: List<TimeValue> = listOf(TimeValue(0, 0.0)),
    val basal: List<TimeValue> = listOf(TimeValue(0, 0.0)),
    val targetLow: List<TimeValue> = listOf(TimeValue(0, 0.0)),
    val targetHigh: List<TimeValue> = listOf(TimeValue(0, 0.0))
)

data class ProfileUiState(
    val profiles: List<String> = emptyList(),
    val currentProfileIndex: Int = 0,
    val currentProfile: SingleProfileState? = null,
    val isEdited: Boolean = false,
    val isValid: Boolean = true,
    val isLocked: Boolean = false,
    val selectedTab: Int = 0,
    val units: String = GlucoseUnit.MGDL.asText,
    val supportsDynamicIsf: Boolean = false,
    val supportsDynamicIc: Boolean = false,
    val basalMin: Double = 0.01,
    val basalMax: Double = 10.0,
    val diaMin: Double = 5.0,
    val diaMax: Double = 10.0,
    val icMin: Double = 0.5,
    val icMax: Double = 100.0,
    val isfMin: Double = 2.0,
    val isfMax: Double = 1000.0,
    val targetMin: Double = 72.0,
    val targetMax: Double = 180.0
)

class ProfileViewModel @Inject constructor(
    private val aapsLogger: AAPSLogger,
    private val rxBus: RxBus,
    private val rh: ResourceHelper,
    private val profilePlugin: ProfilePlugin,
    private val profileFunction: ProfileFunction,
    private val activePlugin: ActivePlugin,
    private val hardLimits: HardLimits,
    private val dateUtil: DateUtil,
    private val protectionCheck: ProtectionCheck,
    private val aapsSchedulers: AapsSchedulers,
    private val uiInteraction: UiInteraction
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val disposable = CompositeDisposable()

    init {
        loadState()
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        disposable += rxBus
            .toObservable(EventLocalProfileChanged::class.java)
            .observeOn(aapsSchedulers.main)
            .subscribe({
                           aapsLogger.debug(LTag.PROFILE, "EventLocalProfileChanged received")
                           loadState()
                       }, { aapsLogger.error("Error in EventLocalProfileChanged subscription", it) })
    }

    fun loadState() {
        val pumpDescription = activePlugin.activePump.pumpDescription
        val aps = activePlugin.activeAPS
        val profiles = profilePlugin.profile?.getProfileList()?.map { it.toString() } ?: emptyList()
        val currentProfile = profilePlugin.currentProfile()
        val isLocked = protectionCheck.isLocked(ProtectionCheck.Protection.PREFERENCES)

        val currentUnits = currentProfile?.mgdl?.let { if (it) GlucoseUnit.MGDL else GlucoseUnit.MMOL } ?: profileFunction.getUnits()
        val isMgdl = currentUnits == GlucoseUnit.MGDL

        _uiState.update { state ->
            state.copy(
                profiles = profiles,
                currentProfileIndex = profilePlugin.currentProfileIndex,
                currentProfile = currentProfile?.toState(),
                isEdited = profilePlugin.isEdited,
                isValid = profilePlugin.numOfProfiles > 0 && profilePlugin.isValidEditState(null),
                isLocked = isLocked,
                units = currentUnits.asText,
                supportsDynamicIsf = aps.supportsDynamicIsf(),
                supportsDynamicIc = aps.supportsDynamicIc(),
                basalMin = pumpDescription.basalMinimumRate,
                basalMax = pumpDescription.basalMaximumRate.coerceAtMost(10.0),
                diaMin = hardLimits.minDia(),
                diaMax = hardLimits.maxDia(),
                icMin = hardLimits.minIC(),
                icMax = hardLimits.maxIC(),
                isfMin = if (isMgdl) HardLimits.MIN_ISF else HardLimits.MIN_ISF / 18.0,
                isfMax = if (isMgdl) HardLimits.MAX_ISF else HardLimits.MAX_ISF / 18.0,
                targetMin = if (isMgdl) HardLimits.LIMIT_MIN_BG[0] else HardLimits.LIMIT_MIN_BG[0] / 18.0,
                targetMax = if (isMgdl) HardLimits.LIMIT_MAX_BG[1] else HardLimits.LIMIT_MAX_BG[1] / 18.0
            )
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun selectProfile(index: Int) {
        if (profilePlugin.isEdited) {
            // Show confirmation dialog - handled by UI
            return
        }
        profilePlugin.currentProfileIndex = index
        loadState()
    }

    fun forceSelectProfile(index: Int) {
        profilePlugin.currentProfileIndex = index
        profilePlugin.isEdited = false
        loadState()
    }

    fun updateProfileName(name: String) {
        profilePlugin.currentProfile()?.name = name
        markEdited()
    }

    fun updateDia(dia: Double) {
        profilePlugin.currentProfile()?.dia = dia
        markEdited()
    }

    fun updateIcEntry(index: Int, timeValue: TimeValue) {
        profilePlugin.currentProfile()?.let { profile ->
            updateJsonArrayEntry(profile.ic, index, timeValue)
            markEdited()
        }
    }

    fun updateIsfEntry(index: Int, timeValue: TimeValue) {
        profilePlugin.currentProfile()?.let { profile ->
            updateJsonArrayEntry(profile.isf, index, timeValue)
            markEdited()
        }
    }

    fun updateBasalEntry(index: Int, timeValue: TimeValue) {
        profilePlugin.currentProfile()?.let { profile ->
            updateJsonArrayEntry(profile.basal, index, timeValue)
            markEdited()
        }
    }

    fun updateTargetEntry(index: Int, low: TimeValue, high: TimeValue) {
        profilePlugin.currentProfile()?.let { profile ->
            updateJsonArrayEntry(profile.targetLow, index, low)
            updateJsonArrayEntry(profile.targetHigh, index, high)
            markEdited()
        }
    }

    fun addIcEntry(afterIndex: Int) {
        profilePlugin.currentProfile()?.let { profile ->
            addJsonArrayEntry(profile.ic, afterIndex)
            markEdited()
        }
    }

    fun addIsfEntry(afterIndex: Int) {
        profilePlugin.currentProfile()?.let { profile ->
            addJsonArrayEntry(profile.isf, afterIndex)
            markEdited()
        }
    }

    fun addBasalEntry(afterIndex: Int) {
        profilePlugin.currentProfile()?.let { profile ->
            addJsonArrayEntry(profile.basal, afterIndex)
            markEdited()
        }
    }

    fun addTargetEntry(afterIndex: Int) {
        profilePlugin.currentProfile()?.let { profile ->
            addJsonArrayEntry(profile.targetLow, afterIndex)
            addJsonArrayEntry(profile.targetHigh, afterIndex)
            markEdited()
        }
    }

    fun removeIcEntry(index: Int) {
        profilePlugin.currentProfile()?.let { profile ->
            if (profile.ic.length() > 1 && index > 0) {
                profile.ic.remove(index)
                markEdited()
            }
        }
    }

    fun removeIsfEntry(index: Int) {
        profilePlugin.currentProfile()?.let { profile ->
            if (profile.isf.length() > 1 && index > 0) {
                profile.isf.remove(index)
                markEdited()
            }
        }
    }

    fun removeBasalEntry(index: Int) {
        profilePlugin.currentProfile()?.let { profile ->
            if (profile.basal.length() > 1 && index > 0) {
                profile.basal.remove(index)
                markEdited()
            }
        }
    }

    fun removeTargetEntry(index: Int) {
        profilePlugin.currentProfile()?.let { profile ->
            if (profile.targetLow.length() > 1 && index > 0) {
                profile.targetLow.remove(index)
                profile.targetHigh.remove(index)
                markEdited()
            }
        }
    }

    private fun updateJsonArrayEntry(array: JSONArray, index: Int, timeValue: TimeValue) {
        if (index < array.length()) {
            val obj = array.getJSONObject(index)
            val hour = timeValue.timeSeconds / 3600
            obj.put("time", String.format("%02d:00", hour))
            obj.put("timeAsSeconds", timeValue.timeSeconds)
            obj.put("value", timeValue.value)
        }
    }

    private fun addJsonArrayEntry(array: JSONArray, afterIndex: Int) {
        if (array.length() >= 24) return

        val prevObj = if (afterIndex >= 0 && afterIndex < array.length()) {
            array.getJSONObject(afterIndex)
        } else {
            null
        }

        val newTime = if (prevObj != null) {
            prevObj.getInt("timeAsSeconds") + 3600 // Add 1 hour after current entry
        } else {
            0
        }

        if (newTime >= 24 * 3600) return

        // Copy value from previous entry (the one before it)
        val inheritedValue = prevObj?.optDouble("value", 0.0) ?: 0.0

        val newObj = JSONObject().apply {
            val hour = newTime / 3600
            put("time", String.format("%02d:00", hour))
            put("timeAsSeconds", newTime)
            put("value", inheritedValue)
        }

        // Insert at position afterIndex + 1
        val insertPos = afterIndex + 1
        val tempList = mutableListOf<JSONObject>()
        for (i in 0 until array.length()) {
            tempList.add(array.getJSONObject(i))
        }
        tempList.add(insertPos.coerceIn(0, tempList.size), newObj)

        // Clear and rebuild array
        while (array.length() > 0) array.remove(0)
        tempList.forEach { array.put(it) }
    }

    private fun markEdited() {
        profilePlugin.isEdited = true
        loadState()
    }

    fun addNewProfile() {
        if (profilePlugin.isEdited) {
            // Show dialog first
            return
        }
        profilePlugin.addNewProfile()
        loadState()
    }

    fun cloneProfile() {
        if (profilePlugin.isEdited) {
            // Show dialog first
            return
        }
        profilePlugin.cloneProfile()
        loadState()
    }

    fun removeCurrentProfile() {
        profilePlugin.removeCurrentProfile()
        loadState()
    }

    fun saveProfile() {
        viewModelScope.launch {
            profilePlugin.storeSettings(null, dateUtil.now())
            loadState()
        }
    }

    fun resetProfile() {
        profilePlugin.loadSettings()
        loadState()
    }

    fun unlockSettings(onUnlock: () -> Unit) {
        // This needs to be called from Activity context
        // The UI layer should handle this
    }

    fun getEditedProfile() = profilePlugin.getEditedProfile()

    fun getActiveInsulin() = activePlugin.activeInsulin

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    private fun ProfileSource.SingleProfile.toState(): SingleProfileState {
        return SingleProfileState(
            name = name,
            mgdl = mgdl,
            dia = dia,
            ic = ic.toTimeValueList(),
            isf = isf.toTimeValueList(),
            basal = basal.toTimeValueList(),
            targetLow = targetLow.toTimeValueList(),
            targetHigh = targetHigh.toTimeValueList()
        )
    }

    private fun JSONArray.toTimeValueList(): List<TimeValue> {
        val list = mutableListOf<TimeValue>()
        for (i in 0 until length()) {
            val obj = getJSONObject(i)
            list.add(
                TimeValue(
                    timeSeconds = obj.optInt("timeAsSeconds", 0),
                    value = obj.optDouble("value", 0.0)
                )
            )
        }
        return list
    }

    fun formatTime(seconds: Int): String {
        val hour = seconds / 3600
        return String.format("%02d:00", hour)
    }

    fun getBasalSum(): Double {
        return profilePlugin.getEditedProfile()?.let {
            ProfileSealed.Pure(it, null).baseBasalSum()
        } ?: 0.0
    }
}
