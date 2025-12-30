package app.aaps.pump.danar

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveIntPreference
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.pump.dana.R
import app.aaps.pump.dana.keys.DanaBooleanKey
import app.aaps.pump.dana.keys.DanaIntKey

/**
 * Compose implementation of DanaR preferences.
 * Note: Bluetooth device selection is handled separately as it requires runtime permissions.
 */
class DanaRPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // DanaR pump settings category
        preferenceCategory(
            key = "danar_settings",
            titleResId = R.string.danar_pump_settings
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = DanaIntKey.Password,
            titleResId = R.string.danar_password_title
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = DanaBooleanKey.UseExtended,
            titleResId = R.string.danar_useextended_title
        )
    }
}
