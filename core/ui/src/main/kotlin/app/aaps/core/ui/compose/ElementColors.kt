package app.aaps.core.ui.compose

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Color scheme for treatment tab icons.
 * Provides consistent color coding for different treatment types.
 *
 * **Usage:**
 * - TreatmentsActivity tab icons
 * - Treatment-related UI elements
 *
 * **Color Assignment:**
 * - bolusCarbs: Orange - Bolus and carbohydrate entries
 * - extendedBolus: Purple - Extended bolus deliveries
 * - tempBasal: Blue/Cyan - Temporary basal rate adjustments
 * - tempTarget: Green - Temporary blood glucose targets
 * - profileSwitch: Black/White - Profile changes
 * - careportal: Yellow/Orange - Careportal entries and notes
 * - runningMode: Yellow/Orange - Running mode changes
 * - userEntry: Green - User action log entries
 *
 * Colors match the existing theme attribute colors for consistency with the rest of the app.
 *
 * @property carbs Color for bolus and carbs icon
 * @property extendedBolus Color for extended bolus icon
 * @property tempBasal Color for temporary basal icon
 * @property tempTarget Color for temp target icon
 * @property profileSwitch Color for profile switch icon
 * @property careportal Color for careportal/note icon
 * @property runningMode Color for running mode icon
 * @property userEntry Color for user entry icon
 */
data class ElementColors(
    val insulin: Color,
    val carbs: Color,
    val extendedBolus: Color,
    val tempBasal: Color,
    val tempTarget: Color,
    val profileSwitch: Color,
    val careportal: Color,
    val runningMode: Color,
    val userEntry: Color,
    val loop: Color,  // Additional UserEntry for loop
    val pump: Color,  // Additional UserEntry for pump
    val aaps: Color   // Additional UserEntry for AAPS
)

/**
 * Light mode color scheme for basic elements.
 * Colors match the light theme values from colors.xml.
 */
internal val LightElementColors = ElementColors(
    insulin = Color(0xFF1E88E5),         // insulin - bolus
    carbs = Color(0xFFE19701),           // colorCarbsButton
    extendedBolus = Color(0xFFCF8BFE),   // extendedBolus
    tempBasal = Color(0xFF00FFFF),       // actionBasal
    tempTarget = Color(0xFF6BD16B),      // tempTargetConfirmation
    profileSwitch = Color(0xFF000000),   // profileSwitch (black for light mode)
    careportal = Color(0xFFFEAF05),      // note
    runningMode = Color(0xFFFFB400),     // hardcoded in drawable
    userEntry = Color(0xFF66BB6A),       // userOption
    loop = Color(0xFF00C03E),            // Additional UserEntry for loop
    pump = Color(0xFF939393),            // Additional UserEntry for loop
    aaps = Color(0xFF666666)             // Additional UserEntry for loop
)

/**
 * Light mode color scheme for basic elements.
 * Colors match the dark theme values from colors.xml (night folder).
 */
internal val DarkElementColors = ElementColors(
    insulin = Color(0xFF67DFE8),         // insulin - bolus (same in both modes)
    carbs = Color(0xFFFFAE01),           // colorCarbsButton (night)
    extendedBolus = Color(0xFFCF8BFE),   // extendedBolus (night)
    tempBasal = Color(0xFF00FFFF),       // actionBasal (same in both modes)
    tempTarget = Color(0xFF77DD77),      // tempTargetConfirmation (night)
    profileSwitch = Color(0xFFFFFFFF),   // profileSwitch (white for dark mode)
    careportal = Color(0xFFFEAF05),      // note (same in both modes)
    runningMode = Color(0xFFFFB400),     // hardcoded in drawable (same in both modes)
    userEntry = Color(0xFF6AE86D),       // userOption (night)
    loop = Color(0xFF00C03E),            // Additional UserEntry for loop
    pump = Color(0xFF939393),            // Additional UserEntry for loop
    aaps = Color(0xFFBBBBBB)             // Additional UserEntry for loop
)

/**
 * CompositionLocal providing treatment icon colors based on current theme (light/dark).
 * Accessed via AapsTheme.treatmentIconColors in composables.
 */
internal val LocalElementColors = compositionLocalOf { LightElementColors }
