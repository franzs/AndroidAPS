package app.aaps.pump.medtrum.ui

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.LTag
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.ui.UiInteraction
import app.aaps.core.ui.toast.ToastUtils
import app.aaps.pump.medtrum.R
import app.aaps.pump.medtrum.code.PatchStep
import app.aaps.pump.medtrum.databinding.FragmentMedtrumPreparePatchConnectBinding
import app.aaps.pump.medtrum.ui.viewmodel.MedtrumViewModel
import javax.inject.Inject

class MedtrumPreparePatchConnectFragment : MedtrumBaseFragment<FragmentMedtrumPreparePatchConnectBinding>() {

    @Inject lateinit var aapsLogger: AAPSLogger
    @Inject lateinit var rh: ResourceHelper
    @Inject lateinit var uiInteraction: UiInteraction

    companion object {

        fun newInstance(): MedtrumPreparePatchConnectFragment = MedtrumPreparePatchConnectFragment()
    }

    override fun getLayoutId(): Int = R.layout.fragment_medtrum_prepare_patch_connect

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        aapsLogger.debug(LTag.PUMP, "MedtrumPreparePatchFragment onViewCreated")
        binding.apply {
            viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[MedtrumViewModel::class.java]
            viewModel?.apply {
                setupStep.observe(viewLifecycleOwner) {
                    when (it) {
                        MedtrumViewModel.SetupStep.INITIAL -> btnPositive.visibility = View.GONE
                        MedtrumViewModel.SetupStep.FILLED  -> btnPositive.visibility = View.VISIBLE

                        MedtrumViewModel.SetupStep.ERROR   -> {
                            ToastUtils.errorToast(requireContext(), rh.gs(R.string.unexpected_state, it.toString()))
                            moveStep(PatchStep.CANCEL)
                        }

                        else                               -> Unit
                    }
                }
                preparePatchConnect()
            }
            btnNegative.setOnClickListener {
                uiInteraction.showOkCancelDialog(context = requireActivity(), message = rh.gs(R.string.cancel_sure), ok = {
                    viewModel?.apply {
                        moveStep(PatchStep.CANCEL)
                    }
                })
            }
        }
    }
}
