package app.aaps.pump.danars

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.pump.dana.R
import app.aaps.pump.dana.keys.DanaBooleanKey

/**
 * Compose implementation of DanaRS preferences.
 * Note: Bluetooth device selection is handled separately as it requires runtime permissions and intent.
 */
class DanaRSPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // DanaRS pump settings category
        preferenceCategory(
            key = "danars_settings",
            titleResId = R.string.danarspump
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = DanaBooleanKey.LogInsulinChange,
            titleResId = R.string.rs_loginsulinchange_title,
            summaryResId = R.string.rs_loginsulinchange_summary
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = DanaBooleanKey.LogCannulaChange,
            titleResId = R.string.rs_logcanulachange_title,
            summaryResId = R.string.rs_logcanulachange_summary
        )
    }
}
