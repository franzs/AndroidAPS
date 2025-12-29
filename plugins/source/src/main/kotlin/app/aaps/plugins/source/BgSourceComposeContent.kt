package app.aaps.plugins.source

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.profile.ProfileUtil
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.ui.compose.ComposablePluginContent
import app.aaps.core.ui.compose.ToolbarConfig
import app.aaps.plugins.source.compose.BgSourceScreen
import app.aaps.plugins.source.viewmodels.BgSourceViewModel

/**
 * Compose content provider for BG Source plugins.
 * This class is shared by all BG source plugins (Dexcom, xDrip, etc.) since they all
 * use the same UI to display blood glucose readings.
 *
 * The content is instantiated only when the plugin UI is displayed.
 */
class BgSourceComposeContent(
    private val persistenceLayer: PersistenceLayer,
    private val rh: ResourceHelper,
    private val dateUtil: DateUtil,
    private val profileUtil: ProfileUtil,
    private val aapsLogger: AAPSLogger,
    private val title: String
) : ComposablePluginContent {

    @Composable
    override fun Render(
        setToolbarConfig: (ToolbarConfig) -> Unit,
        onNavigateBack: () -> Unit,
        onSettings: (() -> Unit)?
    ) {
        // Use remember to create ViewModel only once per composition
        val viewModel = remember {
            BgSourceViewModel(
                persistenceLayer = persistenceLayer,
                rh = rh,
                dateUtil = dateUtil,
                profileUtil = profileUtil,
                aapsLogger = aapsLogger
            )
        }

        BgSourceScreen(
            viewModel = viewModel,
            title = title,
            setToolbarConfig = setToolbarConfig,
            onNavigateBack = onNavigateBack,
            onSettings = onSettings
        )
    }
}
