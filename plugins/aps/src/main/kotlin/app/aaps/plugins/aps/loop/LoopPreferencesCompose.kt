package app.aaps.plugins.aps.loop

import androidx.compose.foundation.lazy.LazyListScope
import app.aaps.core.interfaces.sharedPreferences.SP
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.preferenceCategory

/**
 * Compose implementation of Loop preferences.
 */
class LoopPreferencesCompose(
    private val sp: SP
) : PreferenceScreenContent {

    override fun LazyListScope.preferenceItems() {
        // Loop category
        preferenceCategory(
            key = "loop_settings",
            titleResId = app.aaps.core.ui.R.string.loop
        )

        // Open Mode Min Change - needs AdaptiveIntPreference compose equivalent
        // IntKey.LoopOpenModeMinChange with dialog message
    }
}
