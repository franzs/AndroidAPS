package app.aaps.plugins.sensitivity

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.DoubleKey
import app.aaps.core.keys.IntKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveDoublePreference
import app.aaps.core.ui.compose.preference.adaptiveIntPreference
import app.aaps.core.ui.compose.preference.preferenceCategory

/**
 * Compose implementation of Sensitivity AAPS preferences.
 */
class SensitivityAAPSPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Absorption settings category
        preferenceCategory(
            key = "sensitivity_aaps_settings",
            titleResId = R.string.absorption_settings_title
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.AbsorptionMaxTime,
            titleResId = R.string.absorption_max_time_title
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = IntKey.AutosensPeriod,
            titleResId = R.string.openapsama_autosens_period
        )

        // Advanced settings category
        preferenceCategory(
            key = "absorption_aaps_advanced",
            titleResId = app.aaps.core.ui.R.string.advanced_settings_title
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.AutosensMax,
            titleResId = R.string.openapsama_autosens_max
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.AutosensMin,
            titleResId = R.string.openapsama_autosens_min
        )
    }
}
