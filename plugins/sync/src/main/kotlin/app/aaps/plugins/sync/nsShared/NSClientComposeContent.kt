package app.aaps.plugins.sync.nsShared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.LTag
import app.aaps.core.interfaces.logging.UserEntryLogger
import app.aaps.core.interfaces.nsclient.NSClientMvvmRepository
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.plugin.PluginComposeContent
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.sync.NsClient
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.ComposablePluginContent
import app.aaps.core.ui.compose.ToolbarConfig
import app.aaps.plugins.sync.R
import app.aaps.plugins.sync.nsShared.mvvm.NSClientViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Compose content provider for NSClient plugins.
 * This class provides the UI for NSClientV1 and NSClientV3 plugins.
 */
class NSClientComposeContent(
    private val rh: ResourceHelper,
    private val dateUtil: DateUtil,
    private val aapsLogger: AAPSLogger,
    private val uiInteraction: UiInteraction,
    private val persistenceLayer: PersistenceLayer,
    private val uel: UserEntryLogger,
    private val nsClientMvvmRepository: NSClientMvvmRepository,
    private val activePlugin: ActivePlugin,
    private val preferences: Preferences,
    private val nsClient: () -> NsClient?,
    private val title: String
) : PluginComposeContent, ComposablePluginContent {

    override val content: Any = Unit

    @Composable
    override fun Render(
        setToolbarConfig: (ToolbarConfig) -> Unit,
        onNavigateBack: () -> Unit,
        onSettings: (() -> Unit)?
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val viewModel = remember {
            NSClientViewModel(
                rh = rh,
                activePlugin = activePlugin,
                nsClientMvvmRepository = nsClientMvvmRepository,
                preferences = preferences
            )
        }

        // Load initial data
        remember { viewModel.loadInitialData(); true }

        NSClientScreen(
            viewModel = viewModel,
            dateUtil = dateUtil,
            rh = rh,
            title = title,
            setToolbarConfig = setToolbarConfig,
            onNavigateBack = onNavigateBack,
            onPauseChanged = { isChecked ->
                uel.log(
                    action = if (isChecked) Action.NS_PAUSED else Action.NS_RESUME,
                    source = Sources.NSClient
                )
                nsClient()?.pause(isChecked)
                viewModel.updatePaused(isChecked)
            },
            onClearLog = {
                nsClientMvvmRepository.clearLog()
            },
            onSendNow = {
                scope.launch(Dispatchers.IO) {
                    nsClient()?.resend("GUI")
                }
            },
            onFullSync = {
                handleFullSync(context, scope)
            },
            onSettings = onSettings
        )
    }

    private fun handleFullSync(
        context: android.content.Context,
        scope: kotlinx.coroutines.CoroutineScope
    ) {
        uiInteraction.showOkCancelDialog(
            context = context,
            title = R.string.ns_client,
            message = R.string.full_sync_comment,
            ok = {
                uiInteraction.showOkCancelDialog(
                    context = context,
                    title = R.string.ns_client,
                    message = app.aaps.core.ui.R.string.cleanup_db_confirm_sync,
                    ok = {
                        scope.launch {
                            try {
                                val result = withContext(Dispatchers.IO) {
                                    persistenceLayer.cleanupDatabase(93, deleteTrackedChanges = true)
                                }
                                if (result.isNotEmpty()) {
                                    uiInteraction.showOkDialog(
                                        context = context,
                                        title = rh.gs(app.aaps.core.ui.R.string.result),
                                        message = "<b>${rh.gs(app.aaps.core.ui.R.string.cleared_entries)}</b><br>$result"
                                    )
                                }
                                aapsLogger.info(LTag.CORE, "Cleaned up databases with result: $result")
                                withContext(Dispatchers.IO) {
                                    nsClient()?.resetToFullSync()
                                    nsClient()?.resend("FULL_SYNC")
                                }
                            } catch (e: Exception) {
                                aapsLogger.error("Error cleaning up databases", e)
                            }
                        }
                        uel.log(action = Action.CLEANUP_DATABASES, source = Sources.NSClient)
                    },
                    cancel = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                nsClient()?.resetToFullSync()
                                nsClient()?.resend("FULL_SYNC")
                            }
                        }
                    }
                )
            }
        )
    }
}
