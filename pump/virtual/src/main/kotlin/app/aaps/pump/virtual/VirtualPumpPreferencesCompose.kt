package app.aaps.pump.virtual

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.data.pump.defs.PumpType
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.BooleanKey
import app.aaps.core.keys.StringKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveStringListPreference
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory

/**
 * Compose implementation of Virtual Pump preferences.
 */
class VirtualPumpPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Virtual pump settings category
        preferenceCategory(
            key = "virtual_pump_settings",
            titleResId = R.string.virtualpump_settings
        )

        // Build pump type entries dynamically
        val pumpTypeEntries = PumpType.entries
            .filter { it.description != "USER" }
            .sortedBy { it.description }
            .associate { it.description to it.description }

        adaptiveStringListPreference(
            preferences = preferences,
            config = config,
            stringKey = StringKey.VirtualPumpType,
            titleResId = R.string.virtual_pump_type,
            entries = pumpTypeEntries
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.VirtualPumpStatusUpload,
            titleResId = app.aaps.core.ui.R.string.virtualpump_uploadstatus_title
        )
    }
}
