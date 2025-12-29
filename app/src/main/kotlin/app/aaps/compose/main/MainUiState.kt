package app.aaps.compose.main

import app.aaps.core.interfaces.plugin.PluginBase

data class MainUiState(
    val tabs: List<PluginBase> = emptyList(),
    val drawerItems: List<PluginBase> = emptyList(),
    val drawerCategories: List<DrawerCategory> = emptyList(),
    val selectedCategoryForSheet: DrawerCategory? = null,
    val pluginStateVersion: Int = 0,
    val selectedTabIndex: Int = 0,
    val isDrawerOpen: Boolean = false,
    val isMenuExpanded: Boolean = false,
    val isSimpleMode: Boolean = true,
    val shortTabTitles: Boolean = false,
    val isProfileLoaded: Boolean = false,
    val currentPluginHasPreferences: Boolean = false,
    val currentPluginName: String = "",
    val currentNavDestination: MainNavDestination = MainNavDestination.Overview
)
