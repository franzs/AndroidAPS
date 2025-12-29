package app.aaps.compose.main

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

sealed class MainNavDestination(val id: String, val iconRes: Int, val labelRes: Int) {
    data object Overview : MainNavDestination(
        id = "overview",
        iconRes = app.aaps.core.ui.R.drawable.ic_home,
        labelRes = app.aaps.plugins.main.R.string.overview
    )

    data object Actions : MainNavDestination(
        id = "actions",
        iconRes = app.aaps.core.objects.R.drawable.ic_action,
        labelRes = app.aaps.plugins.main.R.string.actions
    )
}

@Composable
fun MainNavigationBar(
    currentDestination: MainNavDestination,
    onDestinationSelected: (MainNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val destinations = listOf(MainNavDestination.Overview, MainNavDestination.Actions)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    ) {
        destinations.forEach { destination ->
            val selected = currentDestination.id == destination.id
            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        painter = painterResource(id = destination.iconRes),
                        contentDescription = stringResource(destination.labelRes)
                    )
                },
                label = {
                    Text(text = stringResource(destination.labelRes))
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
