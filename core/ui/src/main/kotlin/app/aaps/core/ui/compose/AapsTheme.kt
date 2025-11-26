package app.aaps.core.ui.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import app.aaps.core.keys.StringKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.UiMode

@Composable
fun AapsTheme(
    preferences: Preferences,
    content: @Composable () -> Unit
) {
    val lightColors = lightColorScheme(
        /*
                primary = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_primary),
                onPrimary = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_onPrimary),
                primaryContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_primaryContainer),
                onPrimaryContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_onPrimaryContainer),
                secondary = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_secondary),
                onSecondary = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_onSecondary),
                secondaryContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_secondaryContainer),
                onSecondaryContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_onSecondaryContainer),
                tertiary = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_tertiary),
                onTertiary = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_onTertiary),
                tertiaryContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_tertiaryContainer),
                onTertiaryContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_onTertiaryContainer),
                error = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_error),
                onError = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_onError),
                errorContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_errorContainer),
                onErrorContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_onErrorContainer),
                background = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_background),
                onBackground = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_onBackground),
                surface = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_surface),
                onSurface = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_onSurface),
                surfaceVariant = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_surfaceVariant),
                onSurfaceVariant = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_onSurfaceVariant),
                outline = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_outline),
                inverseSurface = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_inverseSurface),
                inverseOnSurface = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_inverseOnSurface),
                inversePrimary = colorResource(app.aaps.core.ui.R.color.aaps_theme_light_inversePrimary),
         */
    )

    val darkColors = darkColorScheme(
        /*
                primary = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_primary),
                onPrimary = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_onPrimary),
                primaryContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_primaryContainer),
                onPrimaryContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_onPrimaryContainer),
                secondary = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_secondary),
                onSecondary = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_onSecondary),
                secondaryContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_secondaryContainer),
                onSecondaryContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_onSecondaryContainer),
                tertiary = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_tertiary),
                onTertiary = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_onTertiary),
                tertiaryContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_tertiaryContainer),
                onTertiaryContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_onTertiaryContainer),
                error = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_error),
                onError = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_onError),
                errorContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_errorContainer),
                onErrorContainer = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_onErrorContainer),
                background = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_background),
                onBackground = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_onBackground),
                surface = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_surface),
                onSurface = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_onSurface),
                surfaceVariant = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_surfaceVariant),
                onSurfaceVariant = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_onSurfaceVariant),
                outline = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_outline),
                inverseSurface = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_inverseSurface),
                inverseOnSurface = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_inverseOnSurface),
                inversePrimary = colorResource(app.aaps.core.ui.R.color.aaps_theme_dark_inversePrimary),
         */
    )

    val mode = UiMode.fromString(preferences.get(StringKey.GeneralDarkMode))
    val scheme = when (mode) {
        UiMode.LIGHT  -> lightColors
        UiMode.DARK   -> darkColors
        UiMode.SYSTEM -> if (isSystemInDarkTheme()) darkColors else lightColors
    }

    MaterialTheme(
        colorScheme = scheme,
        content = content
    )
}
