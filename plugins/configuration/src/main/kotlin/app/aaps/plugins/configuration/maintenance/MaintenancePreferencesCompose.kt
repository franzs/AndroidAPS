package app.aaps.plugins.configuration.maintenance

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.BooleanKey
import app.aaps.core.keys.IntKey
import app.aaps.core.keys.StringKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveIntPreference
import app.aaps.core.ui.compose.preference.adaptiveStringPreference
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.plugins.configuration.R

/**
 * Compose implementation of Maintenance preferences.
 */
class MaintenancePreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Maintenance settings category
        preferenceCategory(
            key = "maintenance_settings",
            titleResId = R.string.maintenance_settings
        )

        adaptiveStringPreference(
            preferences = preferences,
            config = config,
            stringKey = StringKey.MaintenanceEmail,
            titleResId = R.string.maintenance_email
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = IntKey.MaintenanceLogsAmount,
            titleResId = R.string.maintenance_amount
        )

        // Data choices category
        preferenceCategory(
            key = "data_choice_setting",
            titleResId = R.string.data_choices
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.MaintenanceEnableFabric,
            titleResId = R.string.fabric_upload
        )

        adaptiveStringPreference(
            preferences = preferences,
            config = config,
            stringKey = StringKey.MaintenanceIdentification,
            titleResId = R.string.identification
        )

        // Unattended export category
        preferenceCategory(
            key = "unattended_export_setting",
            titleResId = R.string.unattended_settings_export
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.MaintenanceEnableExportSettingsAutomation,
            titleResId = R.string.unattended_settings_export,
            summaryResId = R.string.unattended_settings_export_summary
        )
    }
}
