package app.aaps.compose.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.aaps.core.data.plugin.PluginType
import app.aaps.core.interfaces.plugin.PluginBase
import app.aaps.core.interfaces.plugin.PluginDescription

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginSelectionSheet(
    category: DrawerCategory,
    isSimpleMode: Boolean,
    pluginStateVersion: Int,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onPluginClick: (PluginBase) -> Unit,
    onPluginEnableToggle: (PluginBase, PluginType, Boolean) -> Unit,
    onPluginPreferencesClick: (PluginBase) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = stringResource(category.titleRes),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(8.dp))

            // Plugin list - key forces recomposition when version changes
            key(pluginStateVersion) {
                category.plugins.forEach { plugin ->
                    val pluginEnabled = plugin.isEnabled(category.type)
                    val hasPreferences = plugin.preferencesId != PluginDescription.PREFERENCE_NONE
                    val showPrefs = hasPreferences && pluginEnabled &&
                        (!isSimpleMode || plugin.pluginDescription.preferencesVisibleInSimpleMode == true)

                    SheetPluginItem(
                        plugin = plugin,
                        isEnabled = pluginEnabled,
                        isMultiSelect = category.isMultiSelect,
                        isAlwaysEnabled = plugin.pluginDescription.alwaysEnabled,
                        showPreferences = showPrefs,
                        onPluginClick = { onPluginClick(plugin) },
                        onEnableToggle = { enabled ->
                            onPluginEnableToggle(plugin, category.type, enabled)
                        },
                        onPreferencesClick = { onPluginPreferencesClick(plugin) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SheetPluginItem(
    plugin: PluginBase,
    isEnabled: Boolean,
    isMultiSelect: Boolean,
    isAlwaysEnabled: Boolean,
    showPreferences: Boolean,
    onPluginClick: () -> Unit,
    onEnableToggle: (Boolean) -> Unit,
    onPreferencesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconRes = if (plugin.menuIcon != -1) {
        plugin.menuIcon
    } else {
        app.aaps.core.ui.R.drawable.ic_settings
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onPluginClick)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        // Radio button or Checkbox based on multi-select
        if (isMultiSelect) {
            Checkbox(
                checked = isEnabled,
                onCheckedChange = { onEnableToggle(it) },
                enabled = !isAlwaysEnabled
            )
        } else {
            RadioButton(
                selected = isEnabled,
                onClick = { if (!isEnabled) onEnableToggle(true) },
                enabled = !isAlwaysEnabled
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = if (isEnabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = plugin.name,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isEnabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Settings icon
        if (showPreferences) {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                painter = painterResource(id = app.aaps.core.ui.R.drawable.ic_settings),
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onPreferencesClick)
            )
        }
    }
}
