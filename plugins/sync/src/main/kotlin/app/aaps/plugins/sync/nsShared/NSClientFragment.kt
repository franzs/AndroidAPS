package app.aaps.plugins.sync.nsShared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.text.toSpanned
import androidx.core.view.MenuCompat
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.aaps.core.data.ue.Action
import app.aaps.core.data.ue.Sources
import app.aaps.core.interfaces.db.PersistenceLayer
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.LTag
import app.aaps.core.interfaces.logging.UserEntryLogger
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.plugin.PluginBase
import app.aaps.core.interfaces.plugin.PluginFragment
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.utils.DateUtil
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.dialogs.OKDialog
import app.aaps.core.utils.HtmlHelper
import app.aaps.plugins.sync.R
import app.aaps.plugins.sync.databinding.NsClientFragmentBinding
import app.aaps.plugins.sync.databinding.NsClientLogItemBinding
import app.aaps.plugins.sync.nsShared.mvvm.NSClientLog
import app.aaps.plugins.sync.nsShared.mvvm.NSClientViewModel
import app.aaps.plugins.sync.nsclientV3.keys.NsclientBooleanKey
import dagger.android.support.DaggerFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NSClientFragment : DaggerFragment(), MenuProvider, PluginFragment {

    @Inject lateinit var rh: ResourceHelper
    @Inject lateinit var dateUtil: DateUtil
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var activePlugin: ActivePlugin
    @Inject lateinit var uel: UserEntryLogger
    @Inject lateinit var persistenceLayer: PersistenceLayer
    @Inject lateinit var aapsLogger: AAPSLogger
    @Inject lateinit var preferences: Preferences

    companion object {

        const val ID_MENU_CLEAR_LOG = 507
        const val ID_MENU_SEND_NOW = 509
        const val ID_MENU_FULL_SYNC = 510
    }

    override var plugin: PluginBase? = null
    private val nsClientPlugin get() = activePlugin.activeNsClient

    private lateinit var viewModel: NSClientViewModel
    private var _binding: NsClientFragmentBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        NsClientFragmentBinding.inflate(inflater, container, false).also {
            _binding = it
            requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, viewModelFactory)[NSClientViewModel::class.java]

        binding.paused.isChecked = preferences.get(NsclientBooleanKey.NsPaused)
        binding.paused.setOnCheckedChangeListener { _, isChecked ->
            uel.log(action = if (isChecked) Action.NS_PAUSED else Action.NS_RESUME, source = Sources.NSClient)
            nsClientPlugin?.pause(isChecked)
        }
        binding.recyclerview.layoutManager = LinearLayoutManager(context)
        binding.recyclerview.adapter = LogListAdapter()

        setupObservers()
        viewModel.loadInitialData()
    }

    private fun setupObservers() {
        // Collect UI state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.url.text = state.url
                    binding.status.text = state.status
                    binding.queue.text = state.queue

                    // Update log list with DiffUtil
                    (binding.recyclerview.adapter as? LogListAdapter)?.submitList(state.logList)
                    binding.recyclerview.scrollToPosition(0)
                }
            }
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menu.add(Menu.FIRST, ID_MENU_CLEAR_LOG, 0, rh.gs(R.string.clear_log)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.add(Menu.FIRST, ID_MENU_SEND_NOW, 0, rh.gs(R.string.deliver_now)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.add(Menu.FIRST, ID_MENU_FULL_SYNC, 0, rh.gs(R.string.full_sync)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        MenuCompat.setGroupDividerEnabled(menu, true)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            ID_MENU_CLEAR_LOG -> {
                viewModel.clearLog()
                true
            }

            ID_MENU_SEND_NOW  -> {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    nsClientPlugin?.resend("GUI")
                }
                true
            }

            ID_MENU_FULL_SYNC -> {
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
                true
            }

            else              -> false
        }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerview.adapter = null // avoid leaks
        _binding = null
    }

    private inner class LogListAdapter : ListAdapter<NSClientLog, LogListAdapter.NsClientLogViewHolder>(LogDiffCallback()) {

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): NsClientLogViewHolder =
            NsClientLogViewHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.ns_client_log_item, viewGroup, false))

        override fun onBindViewHolder(holder: NsClientLogViewHolder, position: Int) {
            val logItem = getItem(position)
            holder.binding.logText.text = HtmlHelper.fromHtml(logItem.toPreparedHtml(dateUtil))
        }

        inner class NsClientLogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val binding = NsClientLogItemBinding.bind(view)
        }
    }

    private class LogDiffCallback : DiffUtil.ItemCallback<NSClientLog>() {

        override fun areItemsTheSame(oldItem: NSClientLog, newItem: NSClientLog): Boolean = oldItem.date == newItem.date
        override fun areContentsTheSame(oldItem: NSClientLog, newItem: NSClientLog): Boolean = oldItem.date == newItem.date
    }
}