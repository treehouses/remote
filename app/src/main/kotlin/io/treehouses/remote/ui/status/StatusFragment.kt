package io.treehouses.remote.ui.status

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.callback.NotificationCallback
import io.treehouses.remote.databinding.ActivityStatusFragmentBinding
import io.treehouses.remote.databinding.DialogRenameStatusBinding
import io.treehouses.remote.utils.DialogUtils
import io.treehouses.remote.utils.Utils
import kotlinx.android.synthetic.main.dialog_wificountry.*

class StatusFragment : BaseFragment() {

    protected val viewModel: StatusViewModel by viewModels(ownerProducer = { this })
    var countryList: ListView? = null
    private lateinit var bind: ActivityStatusFragmentBinding
    var notificationListener: NotificationCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityStatusFragmentBinding.inflate(inflater, container, false)

        viewModel.onLoad()
        bind.refreshBtn.setOnClickListener { viewModel.refresh() }
        viewModel.countryList.observe(viewLifecycleOwner, Observer {
            val adapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item_countries, it)
            bind.countryDisplay.setOnClickListener { wifiCountry(adapter) }
        })

        return bind.root
    }

    private fun wifiCountry(adapter: ArrayAdapter<String?>) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_wificountry)
        dialog.countries
        countryList = dialog.countries
        adapter.filter.filter("")
        countryList?.adapter = adapter
        countryList?.isTextFilterEnabled = true
        countryList?.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, p: Int, _: Long ->
            viewModel.onSelectCountry(countryList!!.getItemAtPosition(p).toString());
            dialog.dismiss()
        }

        searchView(dialog)
        dialog.show()
    }

    fun observers() {
        viewModel.temperature.observe(viewLifecycleOwner, Observer {
            bind.temperature.text = it
        })
        viewModel.deviceName.observe(viewLifecycleOwner, Observer {
            bind.tvBluetooth.text = it
        })
        viewModel.memory.observe(viewLifecycleOwner, Observer {
            bind.memory.text = it
        })
        viewModel.memoryBarValue.observe(viewLifecycleOwner, Observer {
            ObjectAnimator.ofInt(bind.memoryBar, "progress", it).setDuration(600).start()
        })
        viewModel.storageBarValue.observe(viewLifecycleOwner, Observer {
            ObjectAnimator.ofInt(bind.storageBar, "progress", it).setDuration(600).start()
        })
        viewModel.storage.observe(viewLifecycleOwner, Observer {
            bind.storage.text = it;
        })

        viewModel.temperature.observe(viewLifecycleOwner, Observer {
            if (it.toFloatOrNull() != null)
                ObjectAnimator.ofInt(bind.temperatureBar, "progress", (it.toFloat() / 80 * 100).toInt()).setDuration(600).start()
            bind.temperature.text = "$it°C"
        })

        viewModel.showNotification.observe(viewLifecycleOwner, Observer {
            notificationListener!!.setNotification(false)
        })
        viewModel.showUpgrade.observe(viewLifecycleOwner, Observer {
            bind.upgrade.visibility = if (it) View.VISIBLE else View.GONE
            bind.upgradeCheck.setImageDrawable(ContextCompat.getDrawable(requireContext(), if (it) R.drawable.tick_png else R.drawable.tick))
        })
        viewModel.upgradeCheckText.observe(viewLifecycleOwner, Observer {
            bind.upgrade.text = it
        })
        viewModel.rpiType.observe(viewLifecycleOwner, Observer {
            bind.tvRpiType.text = it
        })
        viewModel.hostName.observe(viewLifecycleOwner, Observer {
            bind.tvRpiName.text = it
        })
        viewModel.ssidText.observe(viewLifecycleOwner, Observer {
            bind.ssidText.text = it
        })
        viewModel.ipAddressText.observe(viewLifecycleOwner, Observer {
            bind.ipAdrText.text = it
        })
        viewModel.upgradeCheckText.observe(viewLifecycleOwner, Observer {
            bind.tvUpgradeCheck.text = it
        })
        viewModel.imageText.observe(viewLifecycleOwner, Observer {
            bind.imageText.text = it
        })
        viewModel.deviceAddress.observe(viewLifecycleOwner, Observer {
            bind.deviceAddress.text = it
        })
        viewModel.remoteVersion.observe(viewLifecycleOwner, Observer {
            bind.remoteVersionText.text = it
        })
        viewModel.countryDisplayText.observe(viewLifecycleOwner, Observer {
            bind.countryDisplay.setText(it)
        })
        viewModel.networkModeText.observe(viewLifecycleOwner, Observer {
            bind.networkModeTitle.text = it
        })
        viewModel.cpuModelText.observe(viewLifecycleOwner, Observer {
            bind.cpuModelText.text = it
        })
        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            bind.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        })
        viewModel.countryDisplayTextEnabled.observe(viewLifecycleOwner, Observer {
            bind.countryDisplay.isEnabled = it
        })
    }


    fun searchView(dialog: Dialog) {
        val searchView = dialog.search_bar
        searchView.isIconifiedByDefault = false
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (TextUtils.isEmpty(newText)) {
                    countryList!!.clearTextFilter()
                } else {
                    countryList!!.setFilterText(newText)
                }
                return true
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addRefreshListener()
        viewModel.hostName.observe(viewLifecycleOwner, Observer {
            bind.tvBluetooth.text = it
        })

        bind.upgrade.setOnClickListener {
            viewModel.upgrade()
        }
        bind.editName.setOnClickListener { showRenameDialog() }
        Tutorials.statusTutorials(bind, requireActivity())
        observers()
    }

    private fun addRefreshListener() {
        bind.swiperefresh.setOnRefreshListener {
            viewModel.refresh()
        }
        bind.swiperefresh.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_light),
                ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light),
                ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light),
                ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
        viewModel.showRefresh.observe(viewLifecycleOwner, Observer {
            bind.refreshBtn.isEnabled = it
        })
    }


    private fun showRenameDialog() {
        val inflater = requireActivity().layoutInflater
        val dialogBinding = DialogRenameStatusBinding.inflate(inflater)
        dialogBinding.hostname.hint = "New Name"
        createRenameDialog(dialogBinding.root, dialogBinding.hostname)
    }

    private fun createRenameDialog(view: View, mEditText: EditText) {
        val builder = DialogUtils.createAlertDialog(context, "Rename " + viewModel.hostName.value?.substring(0, viewModel.hostName.value!!.indexOf("-")), view, R.drawable.dialog_icon)
        DialogUtils.createAdvancedDialog(builder, Pair("Rename", "Cancel"), {
            if (mEditText.text.toString() != "") {
                viewModel.sendMessage(requireActivity().getString(R.string.TREEHOUSES_RENAME, mEditText.text.toString()))
                Toast.makeText(context, "Raspberry Pi Renamed", Toast.LENGTH_LONG).show()
                viewModel.refresh()
            } else {
                Toast.makeText(context, "Please enter a new name", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        notificationListener = Utils.attach(context)
    }

}
