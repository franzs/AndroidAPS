package app.aaps.plugins.sync.openhumans

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.BooleanKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.plugins.sync.R

/**
 * Compose implementation of Open Humans preferences.
 */
class OpenHumansPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Open Humans settings category
        preferenceCategory(
            key = "open_humans_settings",
            titleResId = R.string.open_humans
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.OpenHumansWifiOnly,
            titleResId = R.string.only_upload_if_connected_to_wifi
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.OpenHumansChargingOnly,
            titleResId = R.string.only_upload_if_charging
        )
    }
}
