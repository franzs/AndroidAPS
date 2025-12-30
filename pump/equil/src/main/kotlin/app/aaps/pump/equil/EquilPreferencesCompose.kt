package app.aaps.pump.equil

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.pump.equil.keys.EquilBooleanPreferenceKey

/**
 * Compose implementation of Equil preferences.
 */
class EquilPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Equil pump settings category
        preferenceCategory(
            key = "equil_settings",
            titleResId = R.string.equil_name
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = EquilBooleanPreferenceKey.LogInsulinChange,
            titleResId = R.string.equil_log_insulin_change
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = EquilBooleanPreferenceKey.LogPodChange,
            titleResId = R.string.equil_log_pod_change
        )
    }
}
