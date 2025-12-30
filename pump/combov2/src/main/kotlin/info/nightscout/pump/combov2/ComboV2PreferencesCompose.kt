package info.nightscout.pump.combov2

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveIntPreference
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import info.nightscout.pump.combov2.keys.ComboBooleanKey
import info.nightscout.pump.combov2.keys.ComboIntKey

/**
 * Compose implementation of ComboV2 preferences.
 */
class ComboV2PreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // ComboV2 pump settings category
        preferenceCategory(
            key = "combov2_settings",
            titleResId = R.string.combov2_plugin_name
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = ComboBooleanKey.AutomaticReservoirEntry,
            titleResId = R.string.combov2_automatic_reservoir_entry
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = ComboIntKey.BolusSpeed,
            titleResId = R.string.combov2_bolus_speed
        )
    }
}
