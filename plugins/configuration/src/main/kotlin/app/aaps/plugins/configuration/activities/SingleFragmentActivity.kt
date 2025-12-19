package app.aaps.plugins.configuration.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.MenuProvider
import app.aaps.core.interfaces.overview.Overview
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.plugin.PluginBase
import app.aaps.core.interfaces.plugin.PluginDescription
import app.aaps.core.interfaces.protection.ProtectionCheck
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.ComposablePluginContent
import app.aaps.core.ui.compose.LocalPreferences
import app.aaps.core.ui.compose.LocalRxBus
import app.aaps.core.ui.compose.ToolbarConfig
import app.aaps.plugins.configuration.R
import javax.inject.Inject

class SingleFragmentActivity : DaggerAppCompatActivityWithResult() {

    @Inject lateinit var activePlugin: ActivePlugin
    @Inject lateinit var protectionCheck: ProtectionCheck
    @Inject lateinit var uiInteraction: UiInteraction
    @Inject lateinit var overview: Overview

    private var plugin: PluginBase? = null

    // Toolbar state for Compose content
    private var toolbarConfig by mutableStateOf(
        ToolbarConfig(
            title = "",
            navigationIcon = { },
            actions = { }
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        plugin = activePlugin.getPluginsList()[intent.getIntExtra("plugin", -1)]
        val currentPlugin = plugin ?: return

        if (currentPlugin.hasComposeContent()) {
            // Plugin has Compose content - use Compose UI
            setupComposeContent(currentPlugin)
        } else {
            // Plugin uses Fragment - use legacy layout with Fragment hosting
            setupFragmentContent(currentPlugin, savedInstanceState)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun setupComposeContent(plugin: PluginBase) {
        val composeContent = plugin.getComposeContent() ?: return

        // Hide the system ActionBar - we use Compose TopAppBar instead
        supportActionBar?.hide()

        // Initialize toolbar with plugin name
        toolbarConfig = ToolbarConfig(
            title = plugin.name,
            navigationIcon = {
                IconButton(onClick = { onBackPressedDispatcher.onBackPressed() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                // Settings button if plugin has preferences
                if (shouldShowPreferencesMenu(plugin)) {
                    IconButton(onClick = { openPluginPreferences(plugin) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            }
        )

        setContent {
            CompositionLocalProvider(
                LocalPreferences provides preferences,
                LocalRxBus provides rxBus
            ) {
                AapsTheme {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text(toolbarConfig.title) },
                                navigationIcon = { toolbarConfig.navigationIcon() },
                                actions = { toolbarConfig.actions(this) }
                            )
                        }
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            // Invoke the Compose content via ComposablePluginContent interface
                            if (composeContent is ComposablePluginContent) {
                                composeContent.Render(
                                    setToolbarConfig = { config -> toolbarConfig = config },
                                    onNavigateBack = { onBackPressedDispatcher.onBackPressed() },
                                    onSettings = if (shouldShowPreferencesMenu(plugin)) {
                                        { openPluginPreferences(plugin) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupFragmentContent(plugin: PluginBase, savedInstanceState: Bundle?) {
        setTheme(app.aaps.core.ui.R.style.AppTheme)
        setContentView(R.layout.activity_single_fragment)

        title = plugin.name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.frame_layout,
                supportFragmentManager.fragmentFactory.instantiate(
                    ClassLoader.getSystemClassLoader(),
                    plugin.pluginDescription.fragmentClass!!
                )
            ).commit()
        }

        overview.setVersionView(findViewById<TextView>(R.id.version))

        // Add menu items for Fragment-based plugins
        val singleFragmentMenuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                if (!shouldShowPreferencesMenu(plugin)) return
                menuInflater.inflate(R.menu.menu_single_fragment, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    android.R.id.home -> {
                        onBackPressedDispatcher.onBackPressed()
                        true
                    }

                    R.id.nav_plugin_preferences -> {
                        openPluginPreferences(plugin)
                        true
                    }

                    else -> false
                }
        }
        addMenuProvider(singleFragmentMenuProvider)
    }

    private fun shouldShowPreferencesMenu(plugin: PluginBase): Boolean {
        if ((plugin.preferencesId) == PluginDescription.PREFERENCE_NONE) return false
        if (preferences.simpleMode && plugin.pluginDescription.preferencesVisibleInSimpleMode != true) return false
        return true
    }

    private fun openPluginPreferences(plugin: PluginBase) {
        protectionCheck.queryProtection(this, ProtectionCheck.Protection.PREFERENCES, {
            val i = Intent(this, uiInteraction.preferencesActivity)
                .setAction("app.aaps.plugins.configuration.activities.SingleFragmentActivity")
                .putExtra(UiInteraction.PLUGIN_NAME, plugin.javaClass.simpleName)
            startActivity(i)
        }, null)
    }
}
