package app.aaps.compose.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.aaps.R

sealed class MainMenuItem(val id: String) {
    data object Preferences : MainMenuItem("preferences")
    data object PluginPreferences : MainMenuItem("plugin_preferences")
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
    onPreferencesClick: () -> Unit,
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
            IconButton(onClick = onPreferencesClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(app.aaps.core.ui.R.string.settings)
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
