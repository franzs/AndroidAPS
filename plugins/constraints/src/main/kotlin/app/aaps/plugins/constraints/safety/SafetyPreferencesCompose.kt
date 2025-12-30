package app.aaps.plugins.constraints.safety

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.DoubleKey
import app.aaps.core.keys.IntKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveDoublePreference
import app.aaps.core.ui.compose.preference.adaptiveIntPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.plugins.constraints.R

/**
 * Compose implementation of Safety preferences using adaptive preferences.
 */
class SafetyPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Safety category
        preferenceCategory(
            key = "safety_settings",
            titleResId = R.string.treatmentssafety_title
        )

        // TODO: Patient age preference needs AdaptiveListPreference compose equivalent
        // Would use StringKey.SafetyAge

        // Max bolus - using adaptive preference with validation
        adaptiveDoublePreference(
            preferences = preferences,
            config = config,
            doubleKey = DoubleKey.SafetyMaxBolus,
            titleResId = app.aaps.core.ui.R.string.max_bolus_title,
            unit = " U"
        )

        // Max carbs - using adaptive preference with validation
        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = IntKey.SafetyMaxCarbs,
            titleResId = app.aaps.core.ui.R.string.max_carbs_title,
            unit = " g"
        )
    }
}
