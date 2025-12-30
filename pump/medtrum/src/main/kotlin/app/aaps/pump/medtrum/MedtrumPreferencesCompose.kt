package app.aaps.pump.medtrum

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveIntPreference
import app.aaps.core.ui.compose.preference.adaptiveStringPreference
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.pump.medtrum.keys.MedtrumBooleanKey
import app.aaps.pump.medtrum.keys.MedtrumIntKey
import app.aaps.pump.medtrum.keys.MedtrumStringKey

/**
 * Compose implementation of Medtrum preferences.
 */
class MedtrumPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Medtrum pump settings category
        preferenceCategory(
            key = "medtrum_settings",
            titleResId = R.string.medtrum
        )

        adaptiveStringPreference(
            preferences = preferences,
            config = config,
            stringKey = MedtrumStringKey.PumpSN,
            titleResId = R.string.sn_input_title,
            summaryResId = R.string.sn_input_summary
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = MedtrumIntKey.PatchExpiration,
            titleResId = R.string.patch_expiration_title
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = MedtrumIntKey.AlarmHourlyMaxInsulin,
            titleResId = R.string.alarm_hourly_max_insulin_title
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = MedtrumIntKey.AlarmDailyMaxInsulin,
            titleResId = R.string.alarm_daily_max_insulin_title
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = MedtrumBooleanKey.AlarmLowReservoir,
            titleResId = R.string.alarm_low_reservoir_title
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = MedtrumBooleanKey.AlarmSuspend,
            titleResId = R.string.alarm_suspend_title
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = MedtrumBooleanKey.AlarmPatchExpiration,
            titleResId = R.string.alarm_patch_expiration_title
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = MedtrumBooleanKey.AlarmUrgentBattery,
            titleResId = R.string.alarm_urgent_battery_title
        )
    }
}
