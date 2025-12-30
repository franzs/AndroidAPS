package app.aaps.plugins.sync.garmin

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveIntPreference
import app.aaps.core.ui.compose.preference.adaptiveStringPreference
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.plugins.sync.R
import app.aaps.plugins.sync.garmin.keys.GarminBooleanKey
import app.aaps.plugins.sync.garmin.keys.GarminIntKey
import app.aaps.plugins.sync.garmin.keys.GarminStringKey

/**
 * Compose implementation of Garmin preferences.
 */
class GarminPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Garmin settings category
        preferenceCategory(
            key = "garmin_settings",
            titleResId = R.string.garmin
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = GarminBooleanKey.LocalHttpServer,
            titleResId = R.string.garmin_local_http_server
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = GarminIntKey.LocalHttpPort,
            titleResId = R.string.garmin_local_http_server_port
        )

        adaptiveStringPreference(
            preferences = preferences,
            config = config,
            stringKey = GarminStringKey.RequestKey,
            titleResId = R.string.garmin_request_key,
            summaryResId = R.string.garmin_request_key_summary
        )
    }
}
