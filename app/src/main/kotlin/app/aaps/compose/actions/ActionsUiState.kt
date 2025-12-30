package app.aaps.compose.actions

import androidx.compose.ui.graphics.Color
import app.aaps.core.interfaces.pump.actions.CustomAction

/**
 * UI state for the Actions screen
 */
data class ActionsUiState(
    // Visibility states
    val showProfileSwitch: Boolean = false,
    val showTempTarget: Boolean = false,
    val showTempBasal: Boolean = false,
    val showCancelTempBasal: Boolean = false,
    val showExtendedBolus: Boolean = false,
    val showCancelExtendedBolus: Boolean = false,
    val showFill: Boolean = false,
    val showHistoryBrowser: Boolean = false,
    val showTddStats: Boolean = false,
    val showPumpBatteryChange: Boolean = false,

    // Cancel button labels (with active values)
    val cancelTempBasalText: String = "",
    val cancelExtendedBolusText: String = "",

    // Status light data
    val sensorStatus: StatusItem? = null,
    val insulinStatus: StatusItem? = null,
    val cannulaStatus: StatusItem? = null,
    val batteryStatus: StatusItem? = null,
    val isPatchPump: Boolean = false,

    // Custom pump actions
    val customActions: List<CustomAction> = emptyList()
)

/**
 * Status item for sensor/insulin/cannula/battery
 */
data class StatusItem(
    val label: String,
    val age: String,
    val ageColor: Color = Color.Unspecified,
    val agePercent: Float = -1f, // 0-1 progress toward critical threshold
    val level: String? = null,
    val levelColor: Color = Color.Unspecified,
    val levelPercent: Float = -1f, // -1 means no level, 0-1 for progress (inverted: 100% = empty/critical)
    val iconRes: Int
)
