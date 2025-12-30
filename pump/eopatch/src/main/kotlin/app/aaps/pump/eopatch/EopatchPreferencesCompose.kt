package app.aaps.pump.eopatch

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory
import app.aaps.pump.eopatch.keys.EopatchBooleanKey

/**
 * Compose implementation of Eopatch preferences.
 */
class EopatchPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Eopatch pump settings category
        preferenceCategory(
            key = "eopatch_settings",
            titleResId = R.string.eopatch
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = EopatchBooleanKey.LowReservoirReminder,
            titleResId = R.string.key_low_reservoir_reminders
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = EopatchBooleanKey.ExpirationReminder,
            titleResId = R.string.key_expiration_reminders
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = EopatchBooleanKey.BuzzerReminder,
            titleResId = R.string.key_buzzer_reminders
        )
    }
}
