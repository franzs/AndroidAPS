package app.aaps.pump.medtronic

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.pump.medtronic.keys.MedtronicBooleanPreferenceKey

/**
 * Compose implementation of Medtronic preferences.
 * Note: RileyLink-specific preferences are handled separately.
 */
class MedtronicPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Medtronic pump settings category
        preferenceCategory(
            key = "medtronic_settings",
            titleResId = R.string.medtronic_name
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = MedtronicBooleanPreferenceKey.FixBolusSize,
            titleResId = R.string.medtronic_bolus_debugging
        )
    }
}
