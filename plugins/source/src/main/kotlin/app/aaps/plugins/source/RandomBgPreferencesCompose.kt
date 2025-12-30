package app.aaps.plugins.source

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.keys.BooleanKey
import app.aaps.core.keys.IntKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.adaptiveIntPreference
import app.aaps.core.ui.compose.preference.adaptiveSwitchPreference
import app.aaps.core.ui.compose.preference.preferenceCategory

/**
 * Compose implementation of Random BG source preferences.
 */
class RandomBgPreferencesCompose(
    private val preferences: Preferences,
    private val config: Config
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Random BG settings category
        preferenceCategory(
            key = "bg_source_upload_settings",
            titleResId = R.string.random_bg
        )

        adaptiveSwitchPreference(
            preferences = preferences,
            config = config,
            booleanKey = BooleanKey.BgSourceUploadToNs,
            titleResId = app.aaps.core.ui.R.string.do_ns_upload_title
        )

        adaptiveIntPreference(
            preferences = preferences,
            config = config,
            intKey = IntKey.BgSourceRandomInterval,
            titleResId = R.string.bg_generation_interval_minutes
        )
    }
}
