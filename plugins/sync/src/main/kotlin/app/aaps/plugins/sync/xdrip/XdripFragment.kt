package app.aaps.plugins.sync.xdrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import app.aaps.core.interfaces.plugin.PluginBase
import app.aaps.core.interfaces.plugin.PluginFragment
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.sync.DataSyncSelectorXdrip
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.LocalPreferences
import app.aaps.core.ui.compose.LocalRxBus
import app.aaps.plugins.sync.R
import app.aaps.plugins.sync.xdrip.mvvm.XdripMvvmRepository
import app.aaps.plugins.sync.xdrip.mvvm.XdripViewModel
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.launch
import javax.inject.Inject

class XdripFragment : DaggerFragment(), PluginFragment {

    @Inject lateinit var rh: ResourceHelper
    @Inject lateinit var dateUtil: DateUtil
    @Inject lateinit var dataSyncSelector: DataSyncSelectorXdrip
    @Inject lateinit var xdripPlugin: XdripPlugin
    @Inject lateinit var uiInteraction: UiInteraction
    @Inject lateinit var xdripMvvmRepository: XdripMvvmRepository
    @Inject lateinit var preferences: Preferences
    @Inject lateinit var rxBus: RxBus

    override var plugin: PluginBase? = null

    private var viewModel: XdripViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = XdripViewModel(
            rh = rh,
            xdripMvvmRepository = xdripMvvmRepository,
            dataSyncSelector = dataSyncSelector
        )

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CompositionLocalProvider(
                    LocalPreferences provides preferences,
                    LocalRxBus provides rxBus
                ) {
                    AapsTheme {
                        viewModel?.let { vm ->
                            XdripScreen(
                                viewModel = vm,
                                dateUtil = dateUtil,
                                rh = rh,
                                title = rh.gs(R.string.xdrip),
                                setToolbarConfig = { },
                                onNavigateBack = {
                                    activity?.onBackPressedDispatcher?.onBackPressed()
                                },
                                onClearLog = {
                                    xdripMvvmRepository.clearLog()
                                },
                                onFullSync = {
                                    handleFullSync()
                                },
                                onSettings = {
                                    uiInteraction.runPreferencesForPlugin(requireActivity(), xdripPlugin.javaClass.simpleName)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel?.loadInitialData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel = null
    }

    private fun handleFullSync() {
        uiInteraction.showOkCancelDialog(
            context = requireActivity(),
            title = R.string.xdrip,
            message = R.string.full_xdrip_sync_comment,
            ok = {
                viewLifecycleOwner.lifecycleScope.launch {
                    dataSyncSelector.resetToNextFullSync()
                }
            }
        )
    }
}
