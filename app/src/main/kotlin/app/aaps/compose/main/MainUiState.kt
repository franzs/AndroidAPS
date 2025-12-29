package app.aaps.compose.main

data class MainUiState(
    val drawerCategories: List<DrawerCategory> = emptyList(),
    val selectedCategoryForSheet: DrawerCategory? = null,
    val pluginStateVersion: Int = 0,
    val isDrawerOpen: Boolean = false,
    val isSimpleMode: Boolean = true,
    val isProfileLoaded: Boolean = false,
    val currentNavDestination: MainNavDestination = MainNavDestination.Overview,
    val showAboutDialog: Boolean = false
)
