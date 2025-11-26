package app.aaps.plugins.sync.nsShared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.text.toSpanned
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.LTag
import app.aaps.core.interfaces.logging.UserEntryLogger
import app.aaps.core.interfaces.nsclient.NSClientMvvmRepository
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.plugin.PluginBase
import app.aaps.core.interfaces.plugin.PluginFragment
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.dialogs.OKDialog
import app.aaps.core.utils.HtmlHelper
import app.aaps.plugins.sync.R
import app.aaps.plugins.sync.nsShared.mvvm.NSClientViewModel
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NSClientFragment : DaggerFragment(), PluginFragment {

    @Inject lateinit var rh: ResourceHelper
    @Inject lateinit var dateUtil: DateUtil
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var activePlugin: ActivePlugin
    @Inject lateinit var uel: UserEntryLogger
    @Inject lateinit var persistenceLayer: PersistenceLayer
    @Inject lateinit var aapsLogger: AAPSLogger
    @Inject lateinit var preferences: Preferences
    @Inject lateinit var nsClientMvvmRepository: NSClientMvvmRepository
    @Inject lateinit var uiInteraction: UiInteraction


    override var plugin: PluginBase? = null
    private val nsClientPlugin get() = activePlugin.activeNsClient

    private lateinit var viewModel: NSClientViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(this, viewModelFactory)[NSClientViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AapsTheme(preferences) {
                    NSClientScreen(
                        viewModel = viewModel,
                        dateUtil = dateUtil,
                        onPauseChanged = { isChecked ->
                            uel.log(action = if (isChecked) Action.NS_PAUSED else Action.NS_RESUME, source = Sources.NSClient)
                            nsClientPlugin?.pause(isChecked)
                            viewModel.updatePaused(isChecked)
                        },
                        onClearLog = {
                            nsClientMvvmRepository.clearLog()
                        },
                        onSendNow = {
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                nsClientPlugin?.resend("GUI")
                            }
                        },
                        onFullSync = {
                            handleFullSync()
                        },
                        onSettings = {
                            uiInteraction.runPreferencesForPlugin(requireActivity(), nsClientPlugin?.javaClass?.simpleName)
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadInitialData()
    }

    private fun handleFullSync() {
        context?.let { context ->
            OKDialog.showConfirmation(
                context, rh.gs(R.string.ns_client), rh.gs(R.string.full_sync_comment),
                {
                    OKDialog.showConfirmation(requireContext(), rh.gs(R.string.ns_client), rh.gs(app.aaps.core.ui.R.string.cleanup_db_confirm_sync), {
                        viewLifecycleOwner.lifecycleScope.launch {
                            try {
                                val result = withContext(Dispatchers.IO) {
                                    persistenceLayer.cleanupDatabase(93, deleteTrackedChanges = true)
                                }
                                if (result.isNotEmpty())
                                    OKDialog.show(
                                        requireContext(),
                                        rh.gs(app.aaps.core.ui.R.string.result),
                                        HtmlHelper.fromHtml("<b>" + rh.gs(app.aaps.core.ui.R.string.cleared_entries) + "</b><br>" + result).toSpanned()
                                    )
                                aapsLogger.info(LTag.CORE, "Cleaned up databases with result: $result")
                                withContext(Dispatchers.IO) {
                                    nsClientPlugin?.resetToFullSync()
                                    nsClientPlugin?.resend("FULL_SYNC")
                                }
                            } catch (e: Exception) {
                                aapsLogger.error("Error cleaning up databases", e)
                            }
                        }
                        uel.log(action = Action.CLEANUP_DATABASES, source = Sources.NSClient)
                    }, {
                                                  viewLifecycleOwner.lifecycleScope.launch {
                                                      withContext(Dispatchers.IO) {
                                                          nsClientPlugin?.resetToFullSync()
                                                          nsClientPlugin?.resend("FULL_SYNC")
                                                      }
                                                  }
                                              })
                }
            )
        }
    }
}