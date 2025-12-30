package app.aaps.plugins.sensitivity

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.DoubleKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveDoublePreference
import app.aaps.core.ui.compose.preference.preferenceCategory

/**
 * Compose implementation of Sensitivity Oref1 preferences.
 */
class SensitivityOref1PreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Absorption settings category
        preferenceCategory(
            key = "sensitivity_oref1_settings",
            titleResId = R.string.absorption_settings_title
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.ApsSmbMin5MinCarbsImpact,
            titleResId = R.string.openapsama_min_5m_carb_impact
        )

        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.AbsorptionCutOff,
            titleResId = R.string.absorption_cutoff_title
        )

        // Advanced settings category
        preferenceCategory(
            key = "absorption_oref1_advanced",
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
