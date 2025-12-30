package app.aaps.compose.actions

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aaps.core.data.model.RM
import app.aaps.core.data.model.TE
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.interfaces.aps.Loop
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.db.ProcessedTbrEbData
import app.aaps.core.interfaces.logging.UserEntryLogger
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.profile.ProfileFunction
import app.aaps.core.interfaces.pump.WarnColors
import app.aaps.core.interfaces.queue.Callback
import app.aaps.core.interfaces.queue.CommandQueue
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventCustomActionsChanged
import app.aaps.core.interfaces.rx.events.EventExtendedBolusChange
import app.aaps.core.interfaces.rx.events.EventInitializationChanged
import app.aaps.core.interfaces.rx.events.EventTempBasalChange
import app.aaps.core.interfaces.rx.events.EventTherapyEventChange
import app.aaps.core.interfaces.stats.TddCalculator
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.DecimalFormatter
import app.aaps.core.interfaces.utils.fabric.FabricPrivacy
import app.aaps.core.keys.BooleanNonKey
import app.aaps.core.keys.IntKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.objects.extensions.toStringMedium
import app.aaps.core.objects.extensions.toStringShort
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

class ActionsViewModel @Inject constructor(
    private val rh: ResourceHelper,
    private val activePlugin: ActivePlugin,
    private val profileFunction: ProfileFunction,
    private val loop: Loop,
    private val config: Config,
    private val processedTbrEbData: ProcessedTbrEbData,
    private val persistenceLayer: PersistenceLayer,
    private val dateUtil: DateUtil,
    private val commandQueue: CommandQueue,
    private val uel: UserEntryLogger,
    private val uiInteraction: UiInteraction,
    private val rxBus: RxBus,
    private val aapsSchedulers: AapsSchedulers,
    private val fabricPrivacy: FabricPrivacy,
    private val preferences: Preferences,
    private val warnColors: WarnColors,
    private val tddCalculator: TddCalculator,
    private val decimalFormatter: DecimalFormatter
) : ViewModel() {

    private val disposable = CompositeDisposable()
    private val _uiState = MutableStateFlow(ActionsUiState())
    val uiState: StateFlow<ActionsUiState> = _uiState.asStateFlow()

    // Colors for status items
    private val colorNormal = Color(0xFF4CAF50) // Green
    private val colorWarning = Color(0xFFFFC107) // Amber
    private val colorCritical = Color(0xFFF44336) // Red
    private val colorDefault = Color.Unspecified

    init {
        setupEventListeners()
        refreshState()
        preferences.put(BooleanNonKey.ObjectivesActionsUsed, true)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

    private fun setupEventListeners() {
        disposable += rxBus
            .toObservable(EventInitializationChanged::class.java)
            .observeOn(aapsSchedulers.main)
            .subscribe({ refreshState() }, fabricPrivacy::logException)
        disposable += rxBus
            .toObservable(EventExtendedBolusChange::class.java)
            .observeOn(aapsSchedulers.main)
            .subscribe({ refreshState() }, fabricPrivacy::logException)
        disposable += rxBus
            .toObservable(EventTempBasalChange::class.java)
            .observeOn(aapsSchedulers.main)
            .subscribe({ refreshState() }, fabricPrivacy::logException)
        disposable += rxBus
            .toObservable(EventCustomActionsChanged::class.java)
            .observeOn(aapsSchedulers.main)
            .subscribe({ refreshState() }, fabricPrivacy::logException)
        disposable += rxBus
            .toObservable(EventTherapyEventChange::class.java)
            .observeOn(aapsSchedulers.main)
            .subscribe({ refreshState() }, fabricPrivacy::logException)
    }

    fun refreshState() {
        viewModelScope.launch {
            val profile = profileFunction.getProfile()
            val pump = activePlugin.activePump
            val pumpDescription = pump.pumpDescription
            val isInitialized = pump.isInitialized()
            val isSuspended = pump.isSuspended()
            val isDisconnected = loop.runningMode == RM.Mode.DISCONNECTED_PUMP
            val isLoopRunning = loop.runningMode.isLoopRunning()
            val isPatchPump = pumpDescription.isPatchPump

            // Profile switch visibility
            val showProfileSwitch = activePlugin.activeProfileSource.profile != null &&
                pumpDescription.isSetBasalProfileCapable &&
                isInitialized &&
                !isDisconnected &&
                !isSuspended

            // Extended bolus visibility
            val showExtendedBolus: Boolean
            val showCancelExtendedBolus: Boolean
            val cancelExtendedBolusText: String

            if (!pumpDescription.isExtendedBolusCapable || !isInitialized || isSuspended ||
                isDisconnected || pump.isFakingTempsByExtendedBoluses || config.AAPSCLIENT
            ) {
                showExtendedBolus = false
                showCancelExtendedBolus = false
                cancelExtendedBolusText = ""
            } else {
                val activeExtendedBolus = withContext(Dispatchers.IO) {
                    persistenceLayer.getExtendedBolusActiveAt(dateUtil.now())
                }
                if (activeExtendedBolus != null) {
                    showExtendedBolus = false
                    showCancelExtendedBolus = true
                    cancelExtendedBolusText = rh.gs(app.aaps.core.ui.R.string.cancel) + " " +
                        activeExtendedBolus.toStringMedium(dateUtil, rh)
                } else {
                    showExtendedBolus = true
                    showCancelExtendedBolus = false
                    cancelExtendedBolusText = ""
                }
            }

            // Temp basal visibility
            val showTempBasal: Boolean
            val showCancelTempBasal: Boolean
            val cancelTempBasalText: String

            if (!pumpDescription.isTempBasalCapable || !isInitialized || isSuspended ||
                isDisconnected || config.AAPSCLIENT
            ) {
                showTempBasal = false
                showCancelTempBasal = false
                cancelTempBasalText = ""
            } else {
                val activeTemp = processedTbrEbData.getTempBasalIncludingConvertedExtended(System.currentTimeMillis())
                if (activeTemp != null) {
                    showTempBasal = false
                    showCancelTempBasal = true
                    cancelTempBasalText = rh.gs(app.aaps.core.ui.R.string.cancel) + " " +
                        activeTemp.toStringShort(rh)
                } else {
                    showTempBasal = true
                    showCancelTempBasal = false
                    cancelTempBasalText = ""
                }
            }

            // Build status items
            val sensorStatus = buildSensorStatus()
            val insulinStatus = buildInsulinStatus(isPatchPump, pumpDescription.maxResorvoirReading.toDouble())
            val cannulaStatus = buildCannulaStatus(isPatchPump)
            val batteryStatus = if (!isPatchPump || pumpDescription.useHardwareLink) {
                buildBatteryStatus()
            } else null

            // Custom actions
            val customActions = pump.getCustomActions()?.filter { it.isEnabled } ?: emptyList()

            _uiState.update { state ->
                state.copy(
                    showProfileSwitch = showProfileSwitch,
                    showTempTarget = profile != null && isLoopRunning,
                    showTempBasal = showTempBasal,
                    showCancelTempBasal = showCancelTempBasal,
                    showExtendedBolus = showExtendedBolus,
                    showCancelExtendedBolus = showCancelExtendedBolus,
                    showFill = pumpDescription.isRefillingCapable && isInitialized,
                    showHistoryBrowser = profile != null,
                    showTddStats = pumpDescription.supportsTDDs,
                    showPumpBatteryChange = pumpDescription.isBatteryReplaceable || pump.isBatteryChangeLoggingEnabled(),
                    cancelTempBasalText = cancelTempBasalText,
                    cancelExtendedBolusText = cancelExtendedBolusText,
                    sensorStatus = sensorStatus,
                    insulinStatus = insulinStatus,
                    cannulaStatus = cannulaStatus,
                    batteryStatus = batteryStatus,
                    isPatchPump = isPatchPump,
                    customActions = customActions
                )
            }
        }
    }

    private suspend fun buildSensorStatus(): StatusItem {
        val event = withContext(Dispatchers.IO) {
            persistenceLayer.getLastTherapyRecordUpToNow(TE.Type.SENSOR_CHANGE)
        }
        val bgSource = activePlugin.activeBgSource
        val level = if (bgSource.sensorBatteryLevel != -1) "${bgSource.sensorBatteryLevel}%" else null
        val levelPercent = if (bgSource.sensorBatteryLevel != -1) bgSource.sensorBatteryLevel / 100f else -1f

        return StatusItem(
            label = rh.gs(app.aaps.plugins.main.R.string.sensor_label),
            age = event?.let { formatAge(it.timestamp) } ?: "-",
            ageColor = event?.let { getAgeColor(it.timestamp, IntKey.OverviewSageWarning, IntKey.OverviewSageCritical) } ?: colorDefault,
            agePercent = event?.let { getAgePercent(it.timestamp, IntKey.OverviewSageCritical) } ?: 0f,
            level = level,
            levelColor = if (levelPercent >= 0) getLevelColor((levelPercent * 100).toDouble(), IntKey.OverviewSbatWarning, IntKey.OverviewSbatCritical) else colorDefault,
            levelPercent = if (levelPercent >= 0) 1f - levelPercent else -1f, // Invert: 100% battery = 0% toward empty
            iconRes = app.aaps.core.objects.R.drawable.ic_cp_age_sensor
        )
    }

    private suspend fun buildInsulinStatus(isPatchPump: Boolean, maxReading: Double): StatusItem {
        val event = withContext(Dispatchers.IO) {
            persistenceLayer.getLastTherapyRecordUpToNow(TE.Type.INSULIN_CHANGE)
        }
        val pump = activePlugin.activePump
        val reservoirLevel = pump.reservoirLevel
        val insulinUnit = rh.gs(app.aaps.core.ui.R.string.insulin_unit_shortname)

        val level: String? = if (reservoirLevel > 0) {
            if (isPatchPump && reservoirLevel >= maxReading) {
                "${decimalFormatter.to0Decimal(maxReading)}+ $insulinUnit"
            } else {
                decimalFormatter.to0Decimal(reservoirLevel, insulinUnit)
            }
        } else null

        return StatusItem(
            label = rh.gs(app.aaps.plugins.main.R.string.insulin_label),
            age = event?.let { formatAge(it.timestamp) } ?: "-",
            ageColor = event?.let { getAgeColor(it.timestamp, IntKey.OverviewIageWarning, IntKey.OverviewIageCritical) } ?: colorDefault,
            agePercent = event?.let { getAgePercent(it.timestamp, IntKey.OverviewIageCritical) } ?: 0f,
            level = level,
            levelColor = if (reservoirLevel > 0) getLevelColor(reservoirLevel, IntKey.OverviewResWarning, IntKey.OverviewResCritical) else colorDefault,
            levelPercent = -1f, // No progress bar - reservoir sizes vary by pump
            iconRes = app.aaps.core.objects.R.drawable.ic_cp_age_insulin
        )
    }

    private suspend fun buildCannulaStatus(isPatchPump: Boolean): StatusItem {
        val event = withContext(Dispatchers.IO) {
            persistenceLayer.getLastTherapyRecordUpToNow(TE.Type.CANNULA_CHANGE)
        }
        val insulinUnit = rh.gs(app.aaps.core.ui.R.string.insulin_unit_shortname)

        // Calculate usage since last cannula change
        val usage = if (event != null) {
            withContext(Dispatchers.IO) {
                tddCalculator.calculateInterval(event.timestamp, dateUtil.now(), allowMissingData = false)?.totalAmount ?: 0.0
            }
        } else 0.0

        val label = if (isPatchPump) rh.gs(app.aaps.plugins.main.R.string.patch_pump) else rh.gs(app.aaps.plugins.main.R.string.cannula)
        val iconRes = if (isPatchPump) app.aaps.core.objects.R.drawable.ic_patch_pump_outline else app.aaps.core.objects.R.drawable.ic_cp_age_cannula

        return StatusItem(
            label = label,
            age = event?.let { formatAge(it.timestamp) } ?: "-",
            ageColor = event?.let { getAgeColor(it.timestamp, IntKey.OverviewCageWarning, IntKey.OverviewCageCritical) } ?: colorDefault,
            agePercent = event?.let { getAgePercent(it.timestamp, IntKey.OverviewCageCritical) } ?: 0f,
            level = if (usage > 0) decimalFormatter.to0Decimal(usage, insulinUnit) else null,
            levelColor = colorDefault, // Usage doesn't have warning thresholds
            levelPercent = -1f,
            iconRes = iconRes
        )
    }

    private suspend fun buildBatteryStatus(): StatusItem? {
        val pump = activePlugin.activePump
        if (!pump.pumpDescription.isBatteryReplaceable && !pump.isBatteryChangeLoggingEnabled()) {
            return null
        }

        val event = withContext(Dispatchers.IO) {
            persistenceLayer.getLastTherapyRecordUpToNow(TE.Type.PUMP_BATTERY_CHANGE)
        }
        val batteryLevel = pump.batteryLevel
        val level = if (batteryLevel != null && pump.model().supportBatteryLevel) {
            "${batteryLevel}%"
        } else {
            rh.gs(app.aaps.core.ui.R.string.value_unavailable_short)
        }

        return StatusItem(
            label = rh.gs(app.aaps.plugins.main.R.string.pb_label),
            age = event?.let { formatAge(it.timestamp) } ?: "-",
            ageColor = event?.let { getAgeColor(it.timestamp, IntKey.OverviewBageWarning, IntKey.OverviewBageCritical) } ?: colorDefault,
            agePercent = event?.let { getAgePercent(it.timestamp, IntKey.OverviewBageCritical) } ?: 0f,
            level = level,
            levelColor = if (batteryLevel != null) getLevelColor(batteryLevel.toDouble(), IntKey.OverviewBattWarning, IntKey.OverviewBattCritical) else colorDefault,
            levelPercent = batteryLevel?.let { 1f - (it / 100f) } ?: -1f, // Invert: 100% battery = 0% toward empty
            iconRes = app.aaps.core.objects.R.drawable.ic_cp_age_battery
        )
    }

    private fun formatAge(timestamp: Long): String {
        val diff = dateUtil.computeDiff(timestamp, System.currentTimeMillis())
        val days = diff[TimeUnit.DAYS] ?: 0
        val hours = diff[TimeUnit.HOURS] ?: 0
        return if (rh.shortTextMode()) {
            "${days}${rh.gs(app.aaps.core.interfaces.R.string.shortday)}${hours}${rh.gs(app.aaps.core.interfaces.R.string.shorthour)}"
        } else {
            "$days ${rh.gs(app.aaps.core.interfaces.R.string.days)} $hours ${rh.gs(app.aaps.core.interfaces.R.string.hours)}"
        }
    }

    private fun getAgeColor(timestamp: Long, warnKey: IntKey, urgentKey: IntKey): Color {
        val warnHours = preferences.get(warnKey)
        val urgentHours = preferences.get(urgentKey)
        val ageHours = (System.currentTimeMillis() - timestamp) / (1000 * 60 * 60)
        return when {
            ageHours >= urgentHours -> colorCritical
            ageHours >= warnHours   -> colorWarning
            else                    -> colorNormal
        }
    }

    private fun getAgePercent(timestamp: Long, urgentKey: IntKey): Float {
        val urgentHours = preferences.get(urgentKey)
        if (urgentHours <= 0) return 0f
        val ageHours = (System.currentTimeMillis() - timestamp) / (1000.0 * 60 * 60)
        return (ageHours / urgentHours).coerceIn(0.0, 1.0).toFloat()
    }

    private fun getLevelColor(level: Double, warnKey: IntKey, criticalKey: IntKey): Color {
        val warn = preferences.get(warnKey)
        val critical = preferences.get(criticalKey)
        return when {
            level <= critical -> colorCritical
            level <= warn     -> colorWarning
            else              -> colorNormal
        }
    }

    // Action handlers
    fun cancelTempBasal(onResult: (Boolean, String) -> Unit) {
        if (processedTbrEbData.getTempBasalIncludingConvertedExtended(System.currentTimeMillis()) != null) {
            uel.log(Action.CANCEL_TEMP_BASAL, Sources.Actions)
            commandQueue.cancelTempBasal(enforceNew = true, callback = object : Callback() {
                override fun run() {
                    onResult(result.success, result.comment)
                }
            })
        }
    }

    fun cancelExtendedBolus(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val activeExtended = withContext(Dispatchers.IO) {
                persistenceLayer.getExtendedBolusActiveAt(dateUtil.now())
            }
            if (activeExtended != null) {
                uel.log(Action.CANCEL_EXTENDED_BOLUS, Sources.Actions)
                commandQueue.cancelExtended(object : Callback() {
                    override fun run() {
                        onResult(result.success, result.comment)
                    }
                })
            }
        }
    }

    fun executeCustomAction(actionType: app.aaps.core.interfaces.pump.actions.CustomActionType) {
        activePlugin.activePump.executeCustomAction(actionType)
    }
}
