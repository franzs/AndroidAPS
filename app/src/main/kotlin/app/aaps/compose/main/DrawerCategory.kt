package app.aaps.compose.main

import app.aaps.core.data.plugin.PluginType
import app.aaps.core.interfaces.plugin.PluginBase

/**
 * Represents a category in the drawer with its plugins.
 *
 * @param type The plugin type for this category
 * @param titleRes String resource for the category title
 * @param plugins List of plugins in this category
 * @param isMultiSelect Whether multiple plugins can be enabled (checkboxes) or only one (radio buttons)
 */
data class DrawerCategory(
    val type: PluginType,
    val titleRes: Int,
    val plugins: List<PluginBase>,
    val isMultiSelect: Boolean
) {

    /**
     * Get the currently enabled plugin(s) in this category
     */
    val enabledPlugins: List<PluginBase>
        get() = plugins.filter { it.isEnabled(type) }

    /**
     * Get the count of enabled plugins
     */
    val enabledCount: Int
        get() = enabledPlugins.size

    /**
     * Get the active plugin name for single-select categories
     */
    val activePluginName: String?
        get() = if (!isMultiSelect) enabledPlugins.firstOrNull()?.name else null

    /**
     * Get header text: "Category: PluginName" for single-select or 1 enabled, "Category: N enabled" for multi-select with more than 1
     */
    fun getHeaderText(categoryName: String): String {
        // If only 1 plugin is enabled, show its name (behave like exclusive selection)
        return if (enabledCount == 1) {
            enabledPlugins.firstOrNull()?.name?.let { "$categoryName: $it" } ?: categoryName
        } else if (isMultiSelect) {
            if (enabledCount > 0) "$categoryName: $enabledCount enabled" else categoryName
        } else {
            activePluginName?.let { "$categoryName: $it" } ?: categoryName
        }
    }

    companion object {

        /**
         * Check if a plugin type allows multiple selections
         */
        fun isMultiSelect(type: PluginType): Boolean {
            return type == PluginType.GENERAL ||
                type == PluginType.CONSTRAINTS ||
                type == PluginType.LOOP ||
                type == PluginType.SYNC
        }
    }
}

