package app.aaps.compose.main

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.aaps.R
import app.aaps.plugins.configuration.R as ConfigR

sealed class MainMenuItem(val id: String) {
    data object Preferences : MainMenuItem("preferences")
    data class PluginPreferences(val pluginName: String) : MainMenuItem("plugin_preferences")
    data object Profile : MainMenuItem("profile")
    data object Treatments : MainMenuItem("treatments")
    data object HistoryBrowser : MainMenuItem("history_browser")
    data object SetupWizard : MainMenuItem("setup_wizard")
    data object Stats : MainMenuItem("stats")
    data object ProfileHelper : MainMenuItem("profile_helper")
    data object About : MainMenuItem("about")
    data object Exit : MainMenuItem("exit")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    onMenuClick: () -> Unit,
    isMenuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onMenuItemClick: (MainMenuItem) -> Unit,
    currentPluginName: String,
    hasPluginPreferences: Boolean,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.open_navigation)
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { onMenuExpandedChange(true) }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(app.aaps.core.ui.R.string.more_options)
                    )
                }
                MainDropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { onMenuExpandedChange(false) },
                    onMenuItemClick = { item ->
                        onMenuExpandedChange(false)
                        onMenuItemClick(item)
                    },
                    currentPluginName = currentPluginName,
                    hasPluginPreferences = hasPluginPreferences
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}

@Composable
private fun MainDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onMenuItemClick: (MainMenuItem) -> Unit,
    currentPluginName: String,
    hasPluginPreferences: Boolean
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        // Preferences
        DropdownMenuItem(
            text = { Text(stringResource(ConfigR.string.nav_preferences)) },
            onClick = { onMenuItemClick(MainMenuItem.Preferences) }
        )

        // Plugin Preferences
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(R.string.nav_preferences_plugin, currentPluginName),
                    color = if (hasPluginPreferences) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            },
            onClick = {
                if (hasPluginPreferences) {
                    onMenuItemClick(MainMenuItem.PluginPreferences(currentPluginName))
                }
            },
            enabled = hasPluginPreferences
        )

        HorizontalDivider()

        // About
        DropdownMenuItem(
            text = { Text(stringResource(R.string.nav_about)) },
            onClick = { onMenuItemClick(MainMenuItem.About) }
        )

        // Exit
        DropdownMenuItem(
            text = { Text(stringResource(R.string.nav_exit)) },
            onClick = { onMenuItemClick(MainMenuItem.Exit) }
        )
    }
}
