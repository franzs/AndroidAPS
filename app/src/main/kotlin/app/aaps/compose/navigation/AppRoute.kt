package app.aaps.compose.navigation

/**
 * Navigation routes for the Compose-based main activity.
 */
sealed class AppRoute(val route: String) {

    data object Main : AppRoute("main")
    data object Profile : AppRoute("profile")
    data object Treatments : AppRoute("treatments")
    data object Stats : AppRoute("stats")
    data object ProfileHelper : AppRoute("profile_helper")
    data object Preferences : AppRoute("preferences")
    data object PluginPreferences : AppRoute("plugin_preferences/{pluginKey}") {

        fun createRoute(pluginKey: String) = "plugin_preferences/$pluginKey"
    }
}
