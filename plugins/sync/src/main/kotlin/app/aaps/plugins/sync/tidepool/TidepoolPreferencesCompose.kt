package app.aaps.plugins.sync.tidepool

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.BooleanKey
import app.aaps.core.keys.StringKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveStringPreference
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.plugins.sync.R
import app.aaps.plugins.sync.tidepool.keys.TidepoolBooleanKey

/**
 * Compose implementation of Tidepool preferences.
 */
class TidepoolPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Tidepool settings category
        preferenceCategory(
            key = "tidepool_settings",
            titleResId = R.string.tidepool
        )

        // Connection options category
        preferenceCategory(
            key = "tidepool_connection_options",
            titleResId = R.string.connection_settings_title
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientUseCellular,
            titleResId = R.string.ns_cellular
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientUseRoaming,
            titleResId = R.string.ns_allow_roaming
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientUseWifi,
            titleResId = R.string.ns_wifi
        )

        adaptiveStringPreference(
            preferences = preferences,
            config = config,
            stringKey = StringKey.NsClientWifiSsids,
            titleResId = R.string.ns_wifi_ssids
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientUseOnBattery,
            titleResId = R.string.ns_battery
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.NsClientUseOnCharging,
            titleResId = R.string.ns_charging
        )

        // Advanced settings category
        preferenceCategory(
            key = "tidepool_advanced",
            titleResId = app.aaps.core.ui.R.string.advanced_settings_title
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = TidepoolBooleanKey.UseTestServers,
            titleResId = R.string.title_tidepool_dev_servers,
            summaryResId = R.string.summary_tidepool_dev_servers
        )
    }
}
