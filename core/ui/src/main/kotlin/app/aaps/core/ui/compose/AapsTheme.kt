package app.aaps.core.ui.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventPreferenceChange
import app.aaps.core.keys.StringKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.UiMode

/**
 * CompositionLocal providing access to user preferences for theme configuration.
 * Used to retrieve dark mode setting and react to preference changes.
 */
val LocalPreferences = compositionLocalOf<Preferences> { error("No Preferences provided") }

/**
 * CompositionLocal providing access to RxBus for listening to app-wide events.
 * Used to react to preference changes (e.g., dark mode toggle) in real-time.
 */
val LocalRxBus = compositionLocalOf<RxBus> { error("No RxBus provided") }

/**
 * AndroidAPS theme object providing access to custom theme colors and extensions.
 * Supplements Material 3 theme with AndroidAPS-specific color schemes.
 *
 * **Available Color Schemes:**
 * - profileHelperColors: Colors for profile viewer and comparison screens
 * - elementColors: Colors for treatment tab icons and elements
 * - generalColors: Colors for general UI elements (IOB, COB, etc.)
 *
 * **Usage:**
 * ```kotlin
 * @Composable
 * fun MyProfileGraph() {
 *     val colors = AapsTheme.profileHelperColors
 *     LineChart(color = colors.profile1)  // Use blue for primary profile
 * }
 *
 * @Composable
 * fun MyTreatmentTab() {
 *     val colors = AapsTheme.elementColors
 *     Icon(tint = colors.bolusCarbs)  // Use orange for bolus/carbs icon
 * }
 *
 * @Composable
 * fun MyOverviewScreen() {
 *     val colors = AapsTheme.generalColors
 *     Text(color = colors.activeInsulinText)  // Use IOB color for active insulin
 * }
 * ```
 */
object AapsTheme {

    /**
     * Color scheme for profile helper, profile viewer, and profile comparison screens.
     * Provides consistent blue/red color coding for distinguishing between two profiles.
     *
     * Automatically adapts to light/dark mode based on current theme.
     *
     * @see ProfileHelperColors for detailed color assignments
     */
    val profileHelperColors: ProfileHelperColors
        @Composable
        @ReadOnlyComposable
        get() = LocalProfileHelperColors.current

    /**
     * Color scheme for for basic elements.
     *
     * Automatically adapts to light/dark mode based on current theme.
     *
     */
    val elementColors: ElementColors
        @Composable
        @ReadOnlyComposable
        get() = LocalElementColors.current

    /**
     * Color scheme for general UI elements.
     * Provides colors for common elements like IOB, COB, etc.
     *
     * Automatically adapts to light/dark mode based on current theme.
     *
     * @see GeneralColors for detailed color assignments
     */
    val generalColors: GeneralColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGeneralColors.current
}

/**
 * Main AndroidAPS theme wrapper that applies Material 3 theming with custom extensions.
 * Wraps content with Material 3 ColorScheme and provides AndroidAPS-specific theme values.
 *
 * **Features:**
 * - Material 3 color scheme (light/dark mode)
 * - User preference-based theme selection (Light, Dark, System)
 * - Reactive theme switching (listens to preference changes via RxBus)
 * - Custom AndroidAPS color schemes (ProfileHelperColors)
 *
 * **Theme Modes:**
 * - LIGHT: Always use light theme
 * - DARK: Always use dark theme
 * - SYSTEM: Follow system dark mode setting
 *
 * The theme automatically updates when user changes dark mode preference in settings.
 *
 * **Usage:**
 * ```kotlin
 * setContent {
 *     AapsTheme {
 *         MyScreen()
 *     }
 * }
 * ```
 *
 * @param content The composable content to wrap with the theme
 */
@Composable
fun AapsTheme(
    content: @Composable () -> Unit
) {
    val preferences = LocalPreferences.current
    val rxBus = LocalRxBus.current
    var uiMode by remember { mutableStateOf(UiMode.fromString(preferences.get(StringKey.GeneralDarkMode))) }

    DisposableEffect(rxBus, preferences) {
        val disposable = rxBus.toObservable(EventPreferenceChange::class.java)
            .filter { it.changedKey == StringKey.GeneralDarkMode.key }
            .subscribe {
                uiMode = UiMode.fromString(preferences.get(StringKey.GeneralDarkMode))
            }

        onDispose {
            disposable.dispose()
        }
    }

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

    val isDark = when (uiMode) {
        UiMode.LIGHT  -> false
        UiMode.DARK   -> true
        UiMode.SYSTEM -> isSystemInDarkTheme()
    }

    val scheme = if (isDark) darkColors else lightColors
    val profileViewerColors = if (isDark) DarkProfileHelperColors else LightProfileHelperColors
    val treatmentIconColors = if (isDark) DarkElementColors else LightElementColors
    val generalColors = if (isDark) DarkGeneralColors else LightGeneralColors

    CompositionLocalProvider(
        LocalProfileHelperColors provides profileViewerColors,
        LocalElementColors provides treatmentIconColors,
        LocalGeneralColors provides generalColors
    ) {
        MaterialTheme(
            colorScheme = scheme,
            content = content
        )
    }
}
