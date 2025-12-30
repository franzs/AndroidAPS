package app.aaps.pump.omnipod.dash

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveIntPreference
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.pump.omnipod.common.keys.OmnipodBooleanPreferenceKey
import app.aaps.pump.omnipod.common.keys.OmnipodIntPreferenceKey

/**
 * Compose implementation of Omnipod DASH preferences.
 */
class OmnipodDashPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Omnipod DASH pump settings category
        preferenceCategory(
            key = "omnipod_dash_settings",
            titleResId = R.string.omnipod_common_dash
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = OmnipodBooleanPreferenceKey.NotificationUncertainTbrSoundEnabled,
            titleResId = app.aaps.pump.omnipod.common.R.string.omnipod_common_notification_uncertain_tbr_sound_enabled
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = OmnipodBooleanPreferenceKey.NotificationUncertainSmbSoundEnabled,
            titleResId = app.aaps.pump.omnipod.common.R.string.omnipod_common_notification_uncertain_smb_sound_enabled
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = OmnipodBooleanPreferenceKey.NotificationUncertainBolusSoundEnabled,
            titleResId = app.aaps.pump.omnipod.common.R.string.omnipod_common_notification_uncertain_bolus_sound_enabled
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = OmnipodBooleanPreferenceKey.AutomaticallyAcknowledgeAlerts,
            titleResId = app.aaps.pump.omnipod.common.R.string.omnipod_common_automatically_acknowledge_alerts_title
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = OmnipodIntPreferenceKey.ExpirationReminderHours,
            titleResId = app.aaps.pump.omnipod.common.R.string.omnipod_common_expiration_reminder_hours
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = OmnipodIntPreferenceKey.LowReservoirAlertUnits,
            titleResId = app.aaps.pump.omnipod.common.R.string.omnipod_common_low_reservoir_alert_units
        )
    }
}
