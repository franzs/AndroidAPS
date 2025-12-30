package app.aaps.plugins.sync.xdrip

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.BooleanKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.plugins.sync.R

/**
 * Compose implementation of xDrip preferences.
 */
class XdripPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // xDrip settings category
        preferenceCategory(
            key = "xdrip_settings",
            titleResId = R.string.xdrip
        )

        // Note: IntentPreference not yet supported in Compose

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.XdripSendStatus,
            titleResId = R.string.xdrip_send_status_title
        )

        // xDrip status settings category
        preferenceCategory(
            key = "xdrip_advanced",
            titleResId = R.string.xdrip_status_settings
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.XdripSendDetailedIob,
            titleResId = R.string.xdrip_status_detailed_iob_title,
            summaryResId = R.string.xdrip_status_detailed_iob_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.XdripSendBgi,
            titleResId = R.string.xdrip_status_show_bgi_title,
            summaryResId = R.string.xdrip_status_show_bgi_summary
        )
    }
}
