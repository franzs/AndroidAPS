package app.aaps.plugins.automation

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.sharedPreferences.SP
import app.aaps.core.keys.StringKey
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.core.ui.compose.preference.stringListPreference

/**
 * Compose implementation of Automation preferences.
 */
class AutomationPreferencesCompose(
    private val sp: SP
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Automation category
        preferenceCategory(
            key = "automation_settings",
            titleResId = app.aaps.core.ui.R.string.automation
        )

        // Location service preference
        stringListPreference(
            sp = sp,
            key = StringKey.AutomationLocation.key,
            defaultValue = StringKey.AutomationLocation.defaultValue,
            entries = mapOf(
                "PASSIVE" to R.string.use_passive_location,
                "NETWORK" to R.string.use_network_location,
                "GPS" to R.string.use_gps_location
            ),
            titleResId = R.string.locationservice
        )
    }
}
