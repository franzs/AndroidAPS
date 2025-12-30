package app.aaps.pump.diaconn

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.pump.diaconn.keys.DiaconnBooleanKey

/**
 * Compose implementation of Diaconn G8 preferences.
 */
class DiaconnG8PreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Diaconn G8 pump settings category
        preferenceCategory(
            key = "diaconn_g8_settings",
            titleResId = R.string.diaconn_g8_pump
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = DiaconnBooleanKey.LogInjectorChange,
            titleResId = R.string.loginjectorchange
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = DiaconnBooleanKey.LogTubeChange,
            titleResId = R.string.logtubechange
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = DiaconnBooleanKey.LogReservoirChange,
            titleResId = R.string.logreservoirchange
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = DiaconnBooleanKey.LogInsulinChange,
            titleResId = R.string.loginsulinchange
        )
    }
}
