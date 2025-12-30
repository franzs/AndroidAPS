package app.aaps.plugins.insulin

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.IntKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveIntPreference
import app.aaps.core.ui.compose.preference.preferenceCategory

/**
 * Compose implementation of Insulin Free Peak preferences.
 */
class InsulinOrefFreePeakPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Insulin free peak settings category
        preferenceCategory(
            key = "insulin_free_peak_settings",
            titleResId = R.string.insulin_oref_peak
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = IntKey.InsulinOrefPeak,
            titleResId = R.string.insulin_peak_time
        )
    }
}
