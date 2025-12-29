package app.aaps

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.aaps.activities.HistoryBrowseActivity
import app.aaps.activities.PreferencesActivity
import app.aaps.compose.main.DrawerCategory
import app.aaps.compose.main.MainMenuItem
import app.aaps.compose.main.MainNavDestination
import app.aaps.compose.main.MainScreen
import app.aaps.compose.main.MainUiState
import app.aaps.compose.navigation.AppRoute
import app.aaps.core.data.plugin.PluginType
import app.aaps.core.data.ue.Sources
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.interfaces.configuration.ConfigBuilder
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.plugin.PluginBase
import app.aaps.core.interfaces.plugin.PluginDescription
import app.aaps.core.interfaces.profile.ProfileFunction
import app.aaps.core.interfaces.protection.ProtectionCheck
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.events.EventPreferenceChange
import app.aaps.core.interfaces.rx.events.EventRebuildTabs
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.fabric.FabricPrivacy
import app.aaps.core.keys.BooleanKey
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.LocalPreferences
import app.aaps.core.ui.compose.LocalRxBus
import app.aaps.plugins.configuration.activities.DaggerAppCompatActivityWithResult
import app.aaps.plugins.configuration.activities.SingleFragmentActivity
import app.aaps.plugins.configuration.setupwizard.SetupWizardActivity
import app.aaps.plugins.main.profile.ProfileScreen
import app.aaps.plugins.main.profile.ProfileViewModel
import app.aaps.ui.alertDialogs.AboutDialog
import app.aaps.ui.compose.ProfileHelperScreen
import app.aaps.ui.compose.StatsScreen
import app.aaps.ui.compose.TreatmentsScreen
import app.aaps.ui.viewmodels.ProfileHelperViewModel
import app.aaps.ui.viewmodels.StatsViewModel
import app.aaps.ui.viewmodels.TreatmentsViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import app.aaps.plugins.configuration.R as ConfigR

class ComposeMainActivity : DaggerAppCompatActivityWithResult() {

    @Inject lateinit var aapsSchedulers: AapsSchedulers
    @Inject lateinit var fabricPrivacy: FabricPrivacy
    @Inject lateinit var protectionCheck: ProtectionCheck
    @Inject lateinit var activePlugin: ActivePlugin
    @Inject lateinit var configBuilder: ConfigBuilder
    @Inject lateinit var config: Config
    @Inject lateinit var uiInteraction: UiInteraction
    @Inject lateinit var aboutDialog: AboutDialog
    @Inject lateinit var profileFunction: ProfileFunction

    // ViewModels for screens
    @Inject lateinit var treatmentsViewModel: TreatmentsViewModel
    @Inject lateinit var statsViewModel: StatsViewModel
    @Inject lateinit var profileHelperViewModel: ProfileHelperViewModel
    @Inject lateinit var profileViewModel: ProfileViewModel

    private val disposable = CompositeDisposable()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupEventListeners()
        setupWakeLock()
        loadPlugins()

        setContent {
            MainContent()
        }
    }

    @Composable
    private fun MainContent() {
        val navController = rememberNavController()

        CompositionLocalProvider(
            LocalPreferences provides preferences,
            LocalRxBus provides rxBus
        ) {
            AapsTheme {
                val state by uiState.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = AppRoute.Main.route
                ) {
                    composable(AppRoute.Main.route) {
                        MainScreen(
                            uiState = state,
                            onMenuClick = { openDrawer() },
                            onMenuExpandedChange = { toggleMenu(it) },
                            onMenuItemClick = { menuItem ->
                                handleMenuItemClick(menuItem, navController)
                            },
                            onCategoryClick = { category -> handleCategoryClick(category) },
                            onCategoryExpand = { category -> showCategorySheet(category) },
                            onCategorySheetDismiss = { dismissCategorySheet() },
                            onPluginClick = { plugin -> handlePluginClick(plugin) },
                            onPluginEnableToggle = { plugin, type, enabled ->
                                handlePluginEnableToggle(plugin, type, enabled)
                            },
                            onPluginPreferencesClick = { plugin ->
                                handlePluginPreferencesClick(plugin)
                            },
                            onDrawerClosed = { closeDrawer() },
                            onNavDestinationSelected = { destination ->
                                handleNavDestinationSelected(destination)
                            },
                            onSwitchToClassicUi = { switchToClassicUi() }
                        )
                    }

                    composable(AppRoute.Profile.route) {
                        ProfileScreen(
                            viewModel = profileViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(AppRoute.Treatments.route) {
                        TreatmentsScreen(
                            viewModel = treatmentsViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(AppRoute.Stats.route) {
                        StatsScreen(
                            viewModel = statsViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable(AppRoute.ProfileHelper.route) {
                        ProfileHelperScreen(
                            viewModel = profileHelperViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshProfileState()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    private fun setupEventListeners() {
        disposable += rxBus
            .toObservable(EventRebuildTabs::class.java)
            .observeOn(aapsSchedulers.main)
            .subscribe({
                           if (it.recreate) {
                               recreate()
                           } else {
                               loadPlugins()
                           }
                           setupWakeLock()
                       }, fabricPrivacy::logException)

        disposable += rxBus
            .toObservable(EventPreferenceChange::class.java)
            .observeOn(aapsSchedulers.main)
            .subscribe({ event ->
                           if (event.isChanged(BooleanKey.OverviewKeepScreenOn.key)) {
                               setupWakeLock()
                           }
                       }, fabricPrivacy::logException)
    }

    private fun setupWakeLock() {
        val keepScreenOn = preferences.get(BooleanKey.OverviewKeepScreenOn)
        if (keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun loadPlugins() {
        lifecycleScope.launch {
            val isSimpleMode = preferences.simpleMode
            val shortTabTitles = preferences.get(BooleanKey.OverviewShortTabTitles)
            val tabs = mutableListOf<PluginBase>()
            val drawerItems = mutableListOf<PluginBase>()

            for (plugin in activePlugin.getPluginsList()) {
                if (plugin.isEnabled() && plugin.hasFragment() && plugin.showInList(plugin.getType())) {
                    if (isSimpleMode && plugin.pluginDescription.simpleModePosition == PluginDescription.Position.TAB ||
                        !isSimpleMode && plugin.isFragmentVisible()
                    ) {
                        tabs.add(plugin)
                    }
                    if (isSimpleMode && !plugin.pluginDescription.neverVisible && plugin.pluginDescription.simpleModePosition == PluginDescription.Position.MENU ||
                        !isSimpleMode && !plugin.pluginDescription.neverVisible && !plugin.isFragmentVisible()
                    ) {
                        drawerItems.add(plugin)
                    }
                }
            }

            // Build drawer categories
            val categories = buildDrawerCategories()

            val currentPlugin = tabs.getOrNull(0)
            _uiState.update { state ->
                state.copy(
                    tabs = tabs,
                    drawerItems = drawerItems,
                    drawerCategories = categories,
                    isSimpleMode = isSimpleMode,
                    shortTabTitles = shortTabTitles,
                    isProfileLoaded = profileFunction.getProfile() != null,
                    currentPluginHasPreferences = currentPlugin?.let { hasPluginPreferences(it) } ?: false,
                    currentPluginName = currentPlugin?.name ?: ""
                )
            }
        }
    }

    private fun buildDrawerCategories(): List<DrawerCategory> {
        val categories = mutableListOf<DrawerCategory>()

        // Insulin (if APS or PUMPCONTROL or engineering mode)
        if (config.APS || config.PUMPCONTROL || config.isEngineeringMode()) {
            activePlugin.getSpecificPluginsVisibleInList(PluginType.INSULIN).takeIf { it.isNotEmpty() }?.let { plugins ->
                categories.add(
                    DrawerCategory(
                        type = PluginType.INSULIN,
                        titleRes = app.aaps.core.ui.R.string.configbuilder_insulin,
                        plugins = plugins,
                        isMultiSelect = DrawerCategory.isMultiSelect(PluginType.INSULIN)
                    )
                )
            }
        }

        // BG Source, Smoothing, Pump (if not AAPSCLIENT)
        if (!config.AAPSCLIENT) {
            activePlugin.getSpecificPluginsVisibleInList(PluginType.BGSOURCE).takeIf { it.isNotEmpty() }?.let { plugins ->
                categories.add(
                    DrawerCategory(
                        type = PluginType.BGSOURCE,
                        titleRes = ConfigR.string.configbuilder_bgsource,
                        plugins = plugins,
                        isMultiSelect = DrawerCategory.isMultiSelect(PluginType.BGSOURCE)
                    )
                )
            }

            activePlugin.getSpecificPluginsVisibleInList(PluginType.SMOOTHING).takeIf { it.isNotEmpty() }?.let { plugins ->
                categories.add(
                    DrawerCategory(
                        type = PluginType.SMOOTHING,
                        titleRes = ConfigR.string.configbuilder_smoothing,
                        plugins = plugins,
                        isMultiSelect = DrawerCategory.isMultiSelect(PluginType.SMOOTHING)
                    )
                )
            }

            activePlugin.getSpecificPluginsVisibleInList(PluginType.PUMP).takeIf { it.isNotEmpty() }?.let { plugins ->
                categories.add(
                    DrawerCategory(
                        type = PluginType.PUMP,
                        titleRes = ConfigR.string.configbuilder_pump,
                        plugins = plugins,
                        isMultiSelect = DrawerCategory.isMultiSelect(PluginType.PUMP)
                    )
                )
            }
        }

        // Sensitivity (if APS or PUMPCONTROL or engineering mode)
        if (config.APS || config.PUMPCONTROL || config.isEngineeringMode()) {
            activePlugin.getSpecificPluginsVisibleInList(PluginType.SENSITIVITY).takeIf { it.isNotEmpty() }?.let { plugins ->
                categories.add(
                    DrawerCategory(
                        type = PluginType.SENSITIVITY,
                        titleRes = ConfigR.string.configbuilder_sensitivity,
                        plugins = plugins,
                        isMultiSelect = DrawerCategory.isMultiSelect(PluginType.SENSITIVITY)
                    )
                )
            }
        }

        // APS, Loop, Constraints (if APS mode)
        if (config.APS) {
            activePlugin.getSpecificPluginsVisibleInList(PluginType.APS).takeIf { it.isNotEmpty() }?.let { plugins ->
                categories.add(
                    DrawerCategory(
                        type = PluginType.APS,
                        titleRes = ConfigR.string.configbuilder_aps,
                        plugins = plugins,
                        isMultiSelect = DrawerCategory.isMultiSelect(PluginType.APS)
                    )
                )
            }

            activePlugin.getSpecificPluginsVisibleInList(PluginType.LOOP).takeIf { it.isNotEmpty() }?.let { plugins ->
                categories.add(
                    DrawerCategory(
                        type = PluginType.LOOP,
                        titleRes = ConfigR.string.configbuilder_loop,
                        plugins = plugins,
                        isMultiSelect = DrawerCategory.isMultiSelect(PluginType.LOOP)
                    )
                )
            }

            activePlugin.getSpecificPluginsVisibleInList(PluginType.CONSTRAINTS).takeIf { it.isNotEmpty() }?.let { plugins ->
                categories.add(
                    DrawerCategory(
                        type = PluginType.CONSTRAINTS,
                        titleRes = app.aaps.core.ui.R.string.constraints,
                        plugins = plugins,
                        isMultiSelect = DrawerCategory.isMultiSelect(PluginType.CONSTRAINTS)
                    )
                )
            }
        }

        // Sync
        activePlugin.getSpecificPluginsVisibleInList(PluginType.SYNC).takeIf { it.isNotEmpty() }?.let { plugins ->
            categories.add(
                DrawerCategory(
                    type = PluginType.SYNC,
                    titleRes = ConfigR.string.configbuilder_sync,
                    plugins = plugins,
                    isMultiSelect = DrawerCategory.isMultiSelect(PluginType.SYNC)
                )
            )
        }

        // General
        activePlugin.getSpecificPluginsVisibleInList(PluginType.GENERAL).takeIf { it.isNotEmpty() }?.let { plugins ->
            categories.add(
                DrawerCategory(
                    type = PluginType.GENERAL,
                    titleRes = ConfigR.string.configbuilder_general,
                    plugins = plugins,
                    isMultiSelect = DrawerCategory.isMultiSelect(PluginType.GENERAL)
                )
            )
        }

        return categories
    }

    private fun openDrawer() {
        _uiState.update { it.copy(isDrawerOpen = true) }
    }

    private fun closeDrawer() {
        _uiState.update { it.copy(isDrawerOpen = false) }
    }

    private fun toggleMenu(expanded: Boolean) {
        _uiState.update { it.copy(isMenuExpanded = expanded) }
    }

    private fun refreshProfileState() {
        _uiState.update { it.copy(isProfileLoaded = profileFunction.getProfile() != null) }
    }

    private fun hasPluginPreferences(plugin: PluginBase): Boolean {
        if (plugin.preferencesId == PluginDescription.PREFERENCE_NONE) return false
        if (preferences.simpleMode && !plugin.pluginDescription.preferencesVisibleInSimpleMode) return false
        return true
    }

    private fun switchToClassicUi() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun handleNavDestinationSelected(destination: MainNavDestination) {
        _uiState.update { it.copy(currentNavDestination = destination) }
        // TODO: Navigate to actual content based on destination
    }

    private fun handleMenuItemClick(menuItem: MainMenuItem, navController: androidx.navigation.NavController) {
        when (menuItem) {
            is MainMenuItem.Preferences -> {
                protectionCheck.queryProtection(this, ProtectionCheck.Protection.PREFERENCES, {
                    startActivity(
                        Intent(this, PreferencesActivity::class.java)
                            .setAction("app.aaps.ComposeMainActivity")
                    )
                })
            }

            is MainMenuItem.PluginPreferences -> {
                val currentTab = _uiState.value.selectedTabIndex
                val plugin = _uiState.value.tabs.getOrNull(currentTab)
                if (plugin != null) {
                    uiInteraction.runPreferencesForPlugin(this, plugin.javaClass.simpleName)
                }
            }

            is MainMenuItem.Profile -> {
                protectionCheck.queryProtection(this, ProtectionCheck.Protection.PREFERENCES, {
                    navController.navigate(AppRoute.Profile.route)
                })
            }

            is MainMenuItem.Treatments -> {
                // Navigate using Compose Navigation
                navController.navigate(AppRoute.Treatments.route)
            }

            is MainMenuItem.HistoryBrowser -> {
                // Still uses Activity - not yet migrated to Compose
                startActivity(
                    Intent(this, HistoryBrowseActivity::class.java)
                        .setAction("app.aaps.ComposeMainActivity")
                )
            }

            is MainMenuItem.SetupWizard -> {
                // Still uses Activity - not yet migrated to Compose
                protectionCheck.queryProtection(this, ProtectionCheck.Protection.PREFERENCES, {
                    startActivity(
                        Intent(this, SetupWizardActivity::class.java)
                            .setAction("app.aaps.ComposeMainActivity")
                    )
                })
            }

            is MainMenuItem.Stats -> {
                // Navigate using Compose Navigation
                navController.navigate(AppRoute.Stats.route)
            }

            is MainMenuItem.ProfileHelper -> {
                // Navigate using Compose Navigation
                navController.navigate(AppRoute.ProfileHelper.route)
            }

            is MainMenuItem.About -> {
                aboutDialog.showAboutDialog(this, R.string.app_name)
            }

            is MainMenuItem.Exit -> {
                finish()
                configBuilder.exitApp("Menu", Sources.Aaps, false)
            }
        }
    }

    private fun handlePluginClick(plugin: PluginBase) {
        // Only open if plugin has UI (fragment or compose content)
        if (!plugin.hasFragment() && !plugin.hasComposeContent()) {
            return
        }
        lifecycleScope.launch {
            val pluginIndex = activePlugin.getPluginsList().indexOf(plugin)
            startActivity(
                Intent(this@ComposeMainActivity, SingleFragmentActivity::class.java)
                    .setAction(this@ComposeMainActivity::class.simpleName)
                    .putExtra("plugin", pluginIndex)
            )
        }
    }

    private fun handleCategoryClick(category: DrawerCategory) {
        // If only one plugin is enabled, open it directly
        if (category.enabledCount == 1) {
            category.enabledPlugins.firstOrNull()?.let { plugin ->
                handlePluginClick(plugin)
            }
        } else {
            // Show bottom sheet for selection
            showCategorySheet(category)
        }
    }

    private fun showCategorySheet(category: DrawerCategory) {
        _uiState.update { it.copy(selectedCategoryForSheet = category) }
    }

    private fun dismissCategorySheet() {
        _uiState.update { it.copy(selectedCategoryForSheet = null) }
    }

    private fun handlePluginEnableToggle(plugin: PluginBase, type: PluginType, enabled: Boolean) {
        configBuilder.performPluginSwitch(plugin, enabled, type)
        // Rebuild categories to reflect the change
        val categories = buildDrawerCategories()
        val currentSheet = _uiState.value.selectedCategoryForSheet
        val updatedSheet = currentSheet?.let { sheet ->
            categories.find { it.type == sheet.type }
        }
        _uiState.update { state ->
            state.copy(
                drawerCategories = categories,
                selectedCategoryForSheet = updatedSheet,
                pluginStateVersion = state.pluginStateVersion + 1
            )
        }
    }

    private fun handlePluginPreferencesClick(plugin: PluginBase) {
        protectionCheck.queryProtection(this, ProtectionCheck.Protection.PREFERENCES, {
            uiInteraction.runPreferencesForPlugin(this, plugin.javaClass.simpleName)
        })
    }
}
