package io.treehouses.remote.ui.tor

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.adapter.TunnelPortAdapter
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityTorFragmentBinding
import io.treehouses.remote.utils.DialogUtils
import io.treehouses.remote.utils.TunnelUtils

open class TorFragment : BaseFragment() {
    private lateinit var bind: ActivityTorFragmentBinding
    protected val viewModel: TorViewModel by viewModels(ownerProducer = { this })
    private var portsName: ArrayList<String>? = null
    var adapter: TunnelPortAdapter? = null
    private var hostName: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        bind.btnHostName.setOnClickListener {
            val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle)).setTitle("Tor Hostname")
                .setMessage(hostName).setPositiveButton("Copy") { _, _ ->
                    viewModel.addHostName(hostName)
                }.setNegativeButton("Exit", null)
            val dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
        bind.notifyNow.setOnClickListener {
            viewModel.notifyNow(requireContext())
        }
        bind.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            viewModel.addNotification(isChecked)
        }
        bind.portList.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val deleteAllPortsButtonSelected = (portsName?.size ?: 0) > 1 && position == (portsName?.size ?: 0) - 1
            if (deleteAllPortsButtonSelected) DialogUtils.createAlertDialog(context, "Delete All Ports?") { viewModel.addPortList() }
            else DialogUtils.createAlertDialog(context, "Delete Port " + (portsName?.get(position)) + " ?") { viewModel.promptDeletePort(portsName, position) }
        }
        bind.btnTorStart.setOnClickListener {
            viewModel.addStart(bind.btnTorStart.text.toString())
        }
    }

    private fun loadObservers1(){
        viewModel.hostNameText.observe(viewLifecycleOwner) {
            hostName = it
        }
        viewModel.hostNameVisible.observe(viewLifecycleOwner) { visible ->
            if (!visible) bind.btnHostName.visibility = View.GONE
            else bind.btnHostName.visibility = View.VISIBLE
        }
        viewModel.switchNotificationEnabled.observe(viewLifecycleOwner) {
            bind.switchNotification.isEnabled = it
        }
        viewModel.switchNotificationCheck.observe(viewLifecycleOwner) {
            bind.switchNotification.isChecked = it
        }
        viewModel.torStartEnabled.observe(viewLifecycleOwner) {
            bind.btnTorStart.isEnabled = it
        }
        viewModel.torStartText.observe(viewLifecycleOwner) {
            bind.btnTorStart.text = it
        }
    }

    private fun loadObservers2() {
        viewModel.addPortText.observe(viewLifecycleOwner) {
            bind.btnAddPort.text = it
        }
        viewModel.addPortEnabled.observe(viewLifecycleOwner) {
            bind.btnAddPort.isEnabled = it
        }
        viewModel.portListEnabled.observe(viewLifecycleOwner) {
            bind.portList.isEnabled = it
        }
        viewModel.portsNameList.observe(viewLifecycleOwner) {
            portsName = it
            adapter = TunnelUtils.getPortAdapter(requireContext(), portsName)
            val portList = requireView().findViewById<ListView>(R.id.portList)
            portList.adapter = adapter
        }
        viewModel.notifyNowEnabled.observe(viewLifecycleOwner) {
            bind.notifyNow.isEnabled = it
        }
    }


    private fun addPortButtonListeners(dialog: Dialog) {
        val inputExternal: TextInputEditText = dialog.findViewById(R.id.ExternalTextInput)
        val inputInternal: TextInputEditText = dialog.findViewById(R.id.InternalTextInput)
        bind.btnAddPort.setOnClickListener {
            inputExternal.clearFocus()
            inputInternal.clearFocus()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
        val addingPortButton = dialog.findViewById<Button>(R.id.btn_adding_port)
        addingPortButton.setOnClickListener {
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            if (inputExternal.text.toString() !== "" && inputInternal.text.toString() !== "") {
                viewModel.addingPort(inputInternal.text.toString(), inputExternal.text.toString())
                dialog.dismiss()
                inputInternal.text?.clear(); inputExternal.text?.clear()
                dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            }
        }
        dialog.findViewById<ImageButton>(R.id.closeButton).setOnClickListener { dialog.dismiss() }
    }

    private fun setWindowProperties(dialog: Dialog) {
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        val window = dialog.window
        window?.setGravity(Gravity.CENTER)
        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}

