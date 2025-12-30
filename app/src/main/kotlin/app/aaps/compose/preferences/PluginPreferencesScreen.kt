package app.aaps.compose.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.aaps.R
import app.aaps.core.interfaces.plugin.PluginBase
import app.aaps.core.ui.compose.preference.PreferenceScreenContent
import app.aaps.core.ui.compose.preference.ProvidePreferenceTheme
import app.aaps.core.ui.compose.preference.addPreferenceContent
import app.aaps.core.ui.compose.preference.verticalScrollIndicators

/**
 * Screen for displaying plugin preferences using Compose.
 *
 * @param plugin The plugin whose preferences to display
 * @param onBackClick Callback when back button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginPreferencesScreen(
    plugin: PluginBase,
    onBackClick: () -> Unit
) {
    val preferenceContent = plugin.getPreferenceScreenContent() as? PreferenceScreenContent

    ProvidePreferenceTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.nav_preferences_plugin, plugin.name),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(app.aaps.core.ui.R.string.back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            if (preferenceContent != null) {
                val listState = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScrollIndicators(listState),
                    state = listState
                ) {
                    addPreferenceContent(preferenceContent)
                }
            } else {
                // Fallback for plugins without compose preferences
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Text(
                        text = "No compose preferences available for this plugin",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

/**
 * Screen for displaying all preferences from all plugins.
 *
 * @param plugins List of plugins to display preferences for
 * @param onBackClick Callback when back button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllPreferencesScreen(
    plugins: List<PluginBase>,
    onBackClick: () -> Unit
) {
    val preferenceContents = plugins
        .mapNotNull { it.getPreferenceScreenContent() as? PreferenceScreenContent }

    ProvidePreferenceTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(app.aaps.core.ui.R.string.nav_plugin_preferences),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(app.aaps.core.ui.R.string.back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            val listState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScrollIndicators(listState),
                state = listState
            ) {
                preferenceContents.forEach { content ->
                    addPreferenceContent(content)
                }
            }
        }
    }
}
