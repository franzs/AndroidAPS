package app.aaps.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.UserEntryLogger
import app.aaps.core.interfaces.maintenance.ImportExportPrefs
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.profile.ProfileFunction
import app.aaps.core.interfaces.profile.ProfileUtil
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.userEntry.UserEntryPresentationHelper
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.interfaces.utils.DecimalFormatter
import app.aaps.core.interfaces.utils.Translator
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.activities.TranslatedDaggerAppCompatActivity
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.LocalPreferences
import app.aaps.core.ui.compose.LocalRxBus
import app.aaps.ui.compose.TreatmentsScreen
import javax.inject.Inject

/**
 * Activity that displays diabetes treatments with tab navigation.
 *
 * This Compose-based activity provides a centralized view of various treatment types including:
 *
 * 1. **Bolus & Carbs**: Insulin boluses and carbohydrate entries
 * 2. **Extended Boluses**: Extended/dual-wave bolus deliveries (if pump supports)
 * 3. **Temporary Basals**: Temporary basal rate adjustments
 * 4. **Temp Targets**: Temporary blood glucose targets
 * 5. **Profile Switches**: Profile changes and adjustments
 * 6. **Careportal**: General careportal entries and notes
 * 7. **Running Mode**: Running mode changes (closed loop, open loop, etc.)
 * 8. **User Entry**: User action log entries
 *
 * @see app.aaps.ui.compose.TreatmentsScreen
 */
class TreatmentsActivity : TranslatedDaggerAppCompatActivity() {

    @Inject lateinit var activePlugin: ActivePlugin
    @Inject lateinit var preferences: Preferences
    @Inject lateinit var rxBus: RxBus
    @Inject lateinit var persistenceLayer: PersistenceLayer
    @Inject lateinit var profileUtil: ProfileUtil
    @Inject lateinit var profileFunction: ProfileFunction
    @Inject lateinit var rh: ResourceHelper
    @Inject lateinit var translator: Translator
    @Inject lateinit var dateUtil: DateUtil
    @Inject lateinit var decimalFormatter: DecimalFormatter
    @Inject lateinit var uiInteraction: UiInteraction
    @Inject lateinit var userEntryPresentationHelper: UserEntryPresentationHelper
    @Inject lateinit var importExportPrefs: ImportExportPrefs
    @Inject lateinit var uel: UserEntryLogger
    @Inject lateinit var aapsLogger: AAPSLogger


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Determine if Extended Bolus tab should be shown based on pump capabilities
        val showExtendedBolusTab = !activePlugin.activePump.isFakingTempsByExtendedBoluses &&
            activePlugin.activePump.pumpDescription.isExtendedBolusCapable

        setContent {
            CompositionLocalProvider(
                LocalPreferences provides preferences,
                LocalRxBus provides rxBus
            ) {
                AapsTheme {
                    TreatmentsScreen(
                        showExtendedBolusTab = showExtendedBolusTab,
                        persistenceLayer = persistenceLayer,
                        profileUtil = profileUtil,
                        profileFunction = profileFunction,
                        activePlugin = activePlugin,
                        rh = rh,
                        translator = translator,
                        dateUtil = dateUtil,
                        decimalFormatter = decimalFormatter,
                        uiInteraction = uiInteraction,
                        userEntryPresentationHelper = userEntryPresentationHelper,
                        importExportPrefs = importExportPrefs,
                        uel = uel,
                        aapsLogger = aapsLogger,
                        onNavigateBack = { finish() }
                    )
                }
            }
        }
    }
}