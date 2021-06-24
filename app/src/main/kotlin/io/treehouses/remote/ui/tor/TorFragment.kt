package io.treehouses.remote.ui.tor

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.adapter.TunnelPortAdapter
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityTorFragmentBinding
import io.treehouses.remote.utils.DialogUtils

import io.treehouses.remote.utils.TunnelUtils
import io.treehouses.remote.utils.Utils
import io.treehouses.remote.utils.logE
import java.util.*

class TorFragment : BaseFragment() {
    private lateinit var bind: ActivityTorFragmentBinding
    protected val viewModel: TorViewModel by viewModels(ownerProducer = { this })
    var portsName: ArrayList<String>? = null
    var adapter: TunnelPortAdapter? = null
    var hostName: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityTorFragmentBinding.inflate(inflater, container, false)
        viewModel.createView()
        loadObservers1()
        loadObservers2()
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_tor_ports)
        setWindowProperties(dialog)
        addPortButtonListeners(dialog)
        setListeners()
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.let { Tutorials.tunnelTorTutorials(it, requireActivity()) }
    }

    override fun setUserVisibleHint(visible: Boolean) {
        if(visible && isListenerInitialized()) viewModel.setUserVisibleHint()
    }

    private fun setListeners() {
        bind.btnHostName.setOnClickListener() {
            val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle)).setTitle("Tor Hostname")
                    .setMessage(hostName).setPositiveButton("Copy") { _, _ -> viewModel.addHostName(hostName) }
                    .setNegativeButton("Exit", null)
            val dialog = builder.create()
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
        bind.notifyNow.setOnClickListener {
            viewModel.notifyNow(requireContext())
        }
        bind.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            viewModel.addNotification(isChecked)
        }
        bind.portList.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val deleteAllPortsButtonSelected = portsName!!.size > 1 && position == portsName!!.size - 1
            if (deleteAllPortsButtonSelected) DialogUtils.createAlertDialog(context, "Delete All Ports?") { viewModel.addPortList() }
            else DialogUtils.createAlertDialog(context, "Delete Port " + portsName!![position] + " ?") { viewModel.promptDeletePort(portsName, position) }
        }
        bind.btnTorStart.setOnClickListener {
            viewModel.addStart(bind.btnTorStart.text.toString())
        }
    }

    private fun loadObservers1(){
        viewModel.hostNameText.observe(viewLifecycleOwner, Observer {
            hostName = it
        })
        viewModel.hostNameVisible.observe(viewLifecycleOwner, Observer {visible ->
            if (!visible) bind.btnHostName.visibility = View.GONE
            else bind.btnHostName.visibility = View.VISIBLE
        })
        viewModel.switchNotificationEnabled.observe(viewLifecycleOwner, Observer {
            bind.switchNotification.isEnabled = it
        })
        viewModel.switchNotificationCheck.observe(viewLifecycleOwner, Observer {
            bind.switchNotification.isChecked = it
        })
        viewModel.torStartEnabled.observe(viewLifecycleOwner, Observer {
            bind.btnTorStart.isEnabled = it
        })
        viewModel.torStartText.observe(viewLifecycleOwner, Observer {
            bind.btnTorStart.text = it
        })
    }

    private fun loadObservers2() {
        viewModel.addPortText.observe(viewLifecycleOwner, Observer {
            bind.btnAddPort.text = it
        })
        viewModel.addPortEnabled.observe(viewLifecycleOwner, Observer {
            bind.btnAddPort.isEnabled = it
        })
        viewModel.portListEnabled.observe(viewLifecycleOwner, Observer {
            bind.portList.isEnabled = it
        })
        viewModel.portsNameList.observe(viewLifecycleOwner, Observer {
            portsName = it
            adapter = TunnelUtils.getPortAdapter(requireContext(), portsName)
//            try {
//                adapter = TunnelPortAdapter(requireContext(), portsName!!)
//                logE("adapter successful")
//            } catch (e: Exception) {
//                logE(e.toString())
//            }
            val portList = requireView().findViewById<ListView>(R.id.portList)
            portList.adapter = adapter
        })
        viewModel.notifyNowEnabled.observe(viewLifecycleOwner, Observer {
            bind.notifyNow.isEnabled = it
        })
    }


    private fun addPortButtonListeners(dialog: Dialog) {
        val inputExternal: TextInputEditText = dialog.findViewById(R.id.ExternalTextInput)
        val inputInternal: TextInputEditText = dialog.findViewById(R.id.InternalTextInput)
        bind.btnAddPort.setOnClickListener {
            inputExternal.clearFocus()
            inputInternal.clearFocus()
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
        val addingPortButton = dialog.findViewById<Button>(R.id.btn_adding_port)
        addingPortButton.setOnClickListener {
            dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            if (inputExternal.text.toString() !== "" && inputInternal.text.toString() !== "") {
                viewModel.addingPort(inputInternal.text.toString(), inputExternal.text.toString())
                dialog.dismiss()
                inputInternal.text?.clear(); inputExternal.text?.clear()
                dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            }
        }
        dialog.findViewById<ImageButton>(R.id.closeButton).setOnClickListener { dialog.dismiss() }
    }

    private fun setWindowProperties(dialog: Dialog) {
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        val window = dialog.window
        window!!.setGravity(Gravity.CENTER)
        window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}

