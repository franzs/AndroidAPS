package app.aaps.pump.insight

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveIntPreference
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.pump.insight.keys.InsightBooleanKey
import app.aaps.pump.insight.keys.InsightIntKey

/**
 * Compose implementation of Insight preferences.
 */
class InsightPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Insight pump settings category
        preferenceCategory(
            key = "insight_settings",
            titleResId = R.string.insight_local
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = InsightBooleanKey.LogReservoirChanges,
            titleResId = R.string.log_reservoir_changes
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = InsightBooleanKey.LogCannulaChanges,
            titleResId = R.string.log_site_changes
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = InsightBooleanKey.LogBatteryChanges,
            titleResId = R.string.log_battery_changes
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = InsightBooleanKey.EnableTbrEmulation,
            titleResId = R.string.enable_tbr_emulation
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = InsightIntKey.MinRecoveryDuration,
            titleResId = R.string.min_recovery_duration
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = InsightIntKey.MaxRecoveryDuration,
            titleResId = R.string.max_recovery_duration
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = InsightIntKey.DisconnectDelay,
            titleResId = R.string.disconnect_delay
        )
    }
}
