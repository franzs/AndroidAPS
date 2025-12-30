package app.aaps.core.ui.compose.preference

import androidx.compose.foundation.lazy.LazyListScope

/**
 * Interface for plugins to provide compose preference content.
 * Plugins implement this to define their preference UI using the compose preference DSL.
 */
interface PreferenceScreenContent {

    /**
     * Add preference items to the LazyListScope.
     *
     * @param listScope The LazyListScope to add preferences to
     */
    fun LazyListScope.preferenceItems()
}

/**
 * Helper function to invoke PreferenceScreenContent within a LazyListScope
 */
fun LazyListScope.addPreferenceContent(content: PreferenceScreenContent) {
    with(content) {
        preferenceItems()
    }
}
