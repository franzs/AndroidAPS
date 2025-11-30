package app.aaps.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.logging.UserEntryLogger
import app.aaps.core.interfaces.profile.ProfileUtil
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.stats.DexcomTirCalculator
import app.aaps.core.interfaces.stats.TddCalculator
import app.aaps.core.interfaces.stats.TirCalculator
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.activities.TranslatedDaggerAppCompatActivity
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.LocalPreferences
import app.aaps.core.ui.compose.LocalRxBus
import app.aaps.ui.activityMonitor.ActivityMonitor
import app.aaps.ui.compose.StatsScreen
import javax.inject.Inject

/**
 * Activity that displays comprehensive diabetes management statistics.
 *
 * This Compose-based activity provides a centralized view of various statistical analyses
 * for diabetes management, including:
 *
 * 1. **Total Daily Dose (TDD)**: Insulin usage statistics showing basal, bolus, and total
 *    insulin amounts over configurable time periods (7 or 30 days)
 *
 * 2. **Time In Range (TIR)**: Standard glucose range analysis with user-configurable
 *    low and high thresholds, showing percentages of time below, within, and above target
 *
 * 3. **Dexcom TIR**: Extended 14-day glucose analysis following Dexcom's methodology with
 *    5 ranges (Very Low, Low, In Range, High, Very High), including HbA1c estimation
 *
 * 4. **Activity Monitor**: Usage statistics showing how much time users spend in different
 *    activities within the app
 *
 * Each statistics section is presented in a collapsible Material3 Card with:
 * - Loading states with animated transitions
 * - Expandable/collapsible headers
 * - Floating action buttons for recalculation (TDD) and reset (Activity)
 * - Smooth Crossfade animations between loading and data states
 *
 * The activity uses Jetpack Compose with AapsTheme and provides LocalPreferences and
 * LocalRxBus through CompositionLocalProvider for child composables.
 *
 * @see app.aaps.ui.compose.StatsScreen
 * @see TddCalculator
 * @see TirCalculator
 * @see DexcomTirCalculator
 * @see ActivityMonitor
 */
class StatsActivity : TranslatedDaggerAppCompatActivity() {

    @Inject lateinit var tddCalculator: TddCalculator
    @Inject lateinit var tirCalculator: TirCalculator
    @Inject lateinit var dexcomTirCalculator: DexcomTirCalculator
    @Inject lateinit var activityMonitor: ActivityMonitor
    @Inject lateinit var uel: UserEntryLogger
    @Inject lateinit var rh: ResourceHelper
    @Inject lateinit var persistenceLayer: PersistenceLayer
    @Inject lateinit var uiInteraction: UiInteraction
    @Inject lateinit var preferences: Preferences
    @Inject lateinit var rxBus: RxBus
    @Inject lateinit var dateUtil: DateUtil
    @Inject lateinit var profileUtil: ProfileUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(
                LocalPreferences provides preferences,
                LocalRxBus provides rxBus
            ) {
                AapsTheme {
                    StatsScreen(
                        tddCalculator = tddCalculator,
                        tirCalculator = tirCalculator,
                        dexcomTirCalculator = dexcomTirCalculator,
                        activityMonitor = activityMonitor,
                        persistenceLayer = persistenceLayer,
                        rh = rh,
                        uiInteraction = uiInteraction,
                        uel = uel,
                        dateUtil = dateUtil,
                        profileUtil = profileUtil,
                        onNavigateBack = { finish() }
                    )
                }
            }
        }
    }
}
