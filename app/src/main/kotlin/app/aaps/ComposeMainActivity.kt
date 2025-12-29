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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.aaps.activities.HistoryBrowseActivity
import app.aaps.activities.PreferencesActivity
import app.aaps.compose.main.MainMenuItem
import app.aaps.compose.main.MainScreen
import app.aaps.compose.main.MainViewModel
import app.aaps.compose.navigation.AppRoute
import app.aaps.core.data.ue.Sources
import app.aaps.core.interfaces.configuration.ConfigBuilder
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.plugin.PluginBase
import app.aaps.core.interfaces.protection.ProtectionCheck
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.events.EventPreferenceChange
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
import app.aaps.ui.compose.ProfileHelperScreen
import app.aaps.ui.compose.StatsScreen
import app.aaps.ui.compose.TreatmentsScreen
import app.aaps.ui.viewmodels.ProfileHelperViewModel
import app.aaps.ui.viewmodels.StatsViewModel
import app.aaps.ui.viewmodels.TreatmentsViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlinx.coroutines.launch
import javax.inject.Inject

class ComposeMainActivity : DaggerAppCompatActivityWithResult() {

    @Inject lateinit var aapsSchedulers: AapsSchedulers
    @Inject lateinit var fabricPrivacy: FabricPrivacy
    @Inject lateinit var protectionCheck: ProtectionCheck
    @Inject lateinit var activePlugin: ActivePlugin
    @Inject lateinit var configBuilder: ConfigBuilder
    @Inject lateinit var uiInteraction: UiInteraction

    // ViewModels
    @Inject lateinit var mainViewModel: MainViewModel
    @Inject lateinit var treatmentsViewModel: TreatmentsViewModel
    @Inject lateinit var statsViewModel: StatsViewModel
    @Inject lateinit var profileHelperViewModel: ProfileHelperViewModel
    @Inject lateinit var profileViewModel: ProfileViewModel

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupEventListeners()
        setupWakeLock()

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
                val state by mainViewModel.uiState.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = AppRoute.Main.route
                ) {
                    composable(AppRoute.Main.route) {
                        MainScreen(
                            uiState = state,
                            versionName = mainViewModel.versionName,
                            appIcon = mainViewModel.appIcon,
                            aboutDialogData = if (state.showAboutDialog) {
                                mainViewModel.buildAboutDialogData(getString(R.string.app_name))
                            } else null,
                            onMenuClick = { mainViewModel.openDrawer() },
                            onPreferencesClick = {
                                protectionCheck.queryProtection(this@ComposeMainActivity, ProtectionCheck.Protection.PREFERENCES, {
                                    startActivity(
                                        Intent(this@ComposeMainActivity, PreferencesActivity::class.java)
                                            .setAction("app.aaps.ComposeMainActivity")
                                    )
                                })
                            },
                            onMenuItemClick = { menuItem ->
                                handleMenuItemClick(menuItem, navController)
                            },
                            onCategoryClick = { category ->
                                mainViewModel.handleCategoryClick(category) { plugin ->
                                    handlePluginClick(plugin)
                                }
                            },
                            onCategoryExpand = { category -> mainViewModel.showCategorySheet(category) },
                            onCategorySheetDismiss = { mainViewModel.dismissCategorySheet() },
                            onPluginClick = { plugin -> handlePluginClick(plugin) },
                            onPluginEnableToggle = { plugin, type, enabled ->
                                mainViewModel.togglePluginEnabled(plugin, type, enabled)
                            },
                            onPluginPreferencesClick = { plugin ->
                                handlePluginPreferencesClick(plugin)
                            },
                            onDrawerClosed = { mainViewModel.closeDrawer() },
                            onNavDestinationSelected = { destination ->
                                mainViewModel.setNavDestination(destination)
                            },
                            onSwitchToClassicUi = { switchToClassicUi() },
                            onAboutDialogDismiss = { mainViewModel.setShowAboutDialog(false) }
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
        mainViewModel.refreshProfileState()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    private fun setupEventListeners() {
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

    private fun switchToClassicUi() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun handleMenuItemClick(menuItem: MainMenuItem, navController: NavController) {
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
                // No-op: Compose UI doesn't have plugin tabs
            }

            is MainMenuItem.Profile -> {
                protectionCheck.queryProtection(this, ProtectionCheck.Protection.PREFERENCES, {
                    navController.navigate(AppRoute.Profile.route)
                })
            }

            is MainMenuItem.Treatments -> {
                navController.navigate(AppRoute.Treatments.route)
            }

            is MainMenuItem.HistoryBrowser -> {
                startActivity(
                    Intent(this, HistoryBrowseActivity::class.java)
                        .setAction("app.aaps.ComposeMainActivity")
                )
            }

            is MainMenuItem.SetupWizard -> {
                protectionCheck.queryProtection(this, ProtectionCheck.Protection.PREFERENCES, {
                    startActivity(
                        Intent(this, SetupWizardActivity::class.java)
                            .setAction("app.aaps.ComposeMainActivity")
                    )
                })
            }

            is MainMenuItem.Stats -> {
                navController.navigate(AppRoute.Stats.route)
            }

            is MainMenuItem.ProfileHelper -> {
                navController.navigate(AppRoute.ProfileHelper.route)
            }

            is MainMenuItem.About -> {
                mainViewModel.setShowAboutDialog(true)
            }

            is MainMenuItem.Exit -> {
                finish()
                configBuilder.exitApp("Menu", Sources.Aaps, false)
            }
        }
    }

    private fun handlePluginClick(plugin: PluginBase) {
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

    private fun handlePluginPreferencesClick(plugin: PluginBase) {
        protectionCheck.queryProtection(this, ProtectionCheck.Protection.PREFERENCES, {
            uiInteraction.runPreferencesForPlugin(this, plugin.javaClass.simpleName)
        })
    }
}
