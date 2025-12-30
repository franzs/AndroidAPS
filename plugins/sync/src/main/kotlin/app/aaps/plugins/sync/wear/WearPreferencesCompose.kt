package app.aaps.plugins.sync.wear

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.BooleanKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.plugins.sync.R

/**
 * Compose implementation of Wear preferences using adaptive preferences.
 */
class WearPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config,
    private val showBroadcastOption: Boolean
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Wear settings category
        preferenceCategory(
            key = "wear_settings",
            titleResId = R.string.wear_settings
        )

        // Wear control - using adaptive preference with visibility logic
        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.WearControl,
            titleResId = R.string.wearcontrol_title,
            summaryResId = R.string.wearcontrol_summary
        )

        // Broadcast data (only for AAPSCLIENT)
        if (showBroadcastOption) {
            adaptiveSwitchPreference(
                preferences = preferences,
                config = config,
                booleanKey = BooleanKey.WearBroadcastData,
                titleResId = R.string.wear_broadcast_data,
                summaryResId = R.string.wear_broadcast_data_summary
            )
        }

        // Wizard settings category
        preferenceCategory(
            key = "wear_wizard_settings",
            titleResId = app.aaps.core.ui.R.string.wear_wizard_settings
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.WearWizardBg,
            titleResId = app.aaps.core.ui.R.string.bg_label
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.WearWizardTt,
            titleResId = app.aaps.core.ui.R.string.tt_label
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.WearWizardTrend,
            titleResId = app.aaps.core.ui.R.string.bg_trend_label
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.WearWizardCob,
            titleResId = app.aaps.core.ui.R.string.treatments_wizard_cob_label
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.WearWizardIob,
            titleResId = app.aaps.core.ui.R.string.iob_label
        )

        // Custom watchface settings category
        preferenceCategory(
            key = "wear_custom_watchface_settings",
            titleResId = R.string.wear_custom_watchface_settings
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.WearCustomWatchfaceAuthorization,
            titleResId = R.string.wear_custom_watchface_authorization_title,
            summaryResId = R.string.wear_custom_watchface_authorization_summary
        )

        // General settings category
        preferenceCategory(
            key = "wear_general_settings",
            titleResId = R.string.wear_general_settings
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.WearNotifyOnSmb,
            titleResId = R.string.wear_notifysmb_title,
            summaryResId = R.string.wear_notifysmb_summary
        )
    }
}
