package app.aaps.plugins.source

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.profile.ProfileUtil
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.compose.AapsTheme
import app.aaps.core.ui.compose.LocalPreferences
import app.aaps.core.ui.compose.LocalRxBus
import app.aaps.plugins.source.compose.BgSourceScreen
import app.aaps.plugins.source.viewmodels.BgSourceViewModel
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class BGSourceFragment : DaggerFragment() {

    @Inject lateinit var aapsLogger: AAPSLogger
    @Inject lateinit var rxBus: RxBus
    @Inject lateinit var rh: ResourceHelper
    @Inject lateinit var dateUtil: DateUtil
    @Inject lateinit var persistenceLayer: PersistenceLayer
    @Inject lateinit var profileUtil: ProfileUtil
    @Inject lateinit var uiInteraction: UiInteraction
    @Inject lateinit var preferences: Preferences

    private var viewModel: BgSourceViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = BgSourceViewModel(
            persistenceLayer = persistenceLayer,
            rh = rh,
            dateUtil = dateUtil,
            profileUtil = profileUtil,
            aapsLogger = aapsLogger
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
                            BgSourceScreen(
                                viewModel = vm,
                                uiInteraction = uiInteraction,
                                title = rh.gs(R.string.bgsource_settings),
                                setToolbarConfig = { },
                                onNavigateBack = {
                                    activity?.onBackPressedDispatcher?.onBackPressed()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel = null
    }
}
