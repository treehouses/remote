package io.treehouses.remote.ui.sshtunnel

import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.databinding.ActivityTunnelSshFragmentBinding
import io.treehouses.remote.databinding.DialogSshtunnelHostsBinding
import io.treehouses.remote.databinding.DialogSshtunnelKeyBinding
import io.treehouses.remote.databinding.DialogSshtunnelPortsBinding
import io.treehouses.remote.pojo.enum.Status
import io.treehouses.remote.utils.*

class SSHTunnelFragment : BaseSSHTunnelFragment() {
    lateinit var dialogSshTunnelPortsBinding: DialogSshtunnelPortsBinding
    private lateinit var dialogSshTunnelKeyBinding: DialogSshtunnelKeyBinding
    lateinit var dialogSshTunnelHostsBinding: DialogSshtunnelHostsBinding

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = ActivityTunnelSshFragmentBinding.inflate(inflater, container, false)
        viewModel.onCreateView()
        loadObservers1()
        loadDialogObservers()
        initializeDialog()
        addListeners1()
        addListeners2()
        return bind.root
    }

    private fun loadDialogObservers() {
        viewModel.tunnelSSHKeyDialogData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    if (it.data?.showHandleDifferentKeysDialog == true) {
                        handleDifferentKeys(it.data)
                    }
                    if (it.data?.showHandlePhoneKeySaveDialog == true) {
                        handlePhoneKeySave(it.data)
                    }
                    if (it.data?.showHandlePiKeySaveDialog == true)
                        handlePiKeySave(it.data.profile, it.data.storedPublicKey, it.data.storedPrivateKey)
                } else -> {}
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Tutorials.tunnelSSHTutorials(bind, requireActivity())
        viewModel.setUserVisibleHint()
    }

    private fun showDialog(dialog: Dialog) {
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun addListeners1() {
        bind.switchNotification.setOnCheckedChangeListener { _, isChecked -> viewModel.switchButton(isChecked) }
        bind.btnAddPort.setOnClickListener { showDialog(dialogPort) }
        bind.btnAddHosts.setOnClickListener {
            showDialog(dialogHosts) }
        bind.btnKeys.setOnClickListener { showDialog(dialogKeys) }
        bind.notifyNow.setOnClickListener { viewModel.notifyNow(requireContext()) }
        bind.info.setOnClickListener {
            val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle)); builder.setTitle("SSH Help")
            builder.setMessage(R.string.ssh_info)
            val dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent); dialog.show()
        }
        dialogSshTunnelPortsBinding.btnAddingPort.setOnClickListener { handleAddPort() }
        dialogSshTunnelHostsBinding.btnAddingHost.setOnClickListener {
            val m1 = "${dialogSshTunnelHostsBinding.PortNumberInput.text}"
            val m2 = "${dialogSshTunnelHostsBinding.UserNameInput.text}@${dialogSshTunnelHostsBinding.DomainIPInput.text}"
            viewModel.addingHostButton(m1, m2)
            dialogHosts.dismiss()
        }
    }

    private fun handleAddPort() {
        if (dialogSshTunnelPortsBinding.ExternalTextInput.text?.isNotEmpty() == true && dialogSshTunnelPortsBinding.InternalTextInput.text?.isNotEmpty() == true) {
            val parts = "${dialogSshTunnelPortsBinding.hosts.selectedItem}".split(":")[0]
            viewModel.addingPortButton("${dialogSshTunnelPortsBinding.InternalTextInput.text}", "${dialogSshTunnelPortsBinding.ExternalTextInput.text}", parts)
            dialogPort.dismiss()
        }
    }

    private fun addListeners2() {
        val profile = "${dialogKeys.findViewById<EditText>(R.id.sshtunnel_profile).text}"
        dialogSshTunnelKeyBinding.btnSaveKeys.setOnClickListener { viewModel.keyClickListener(profile); }
        dialogSshTunnelKeyBinding.btnShowKeys.setOnClickListener {
            viewModel.keyClickListener(profile); viewModel.handleShowKeys(profile)
        }
        dialogSshTunnelPortsBinding.addPortCloseButton.setOnClickListener { dialogPort.dismiss() }
        dialogSshTunnelHostsBinding.addHostCloseButton.setOnClickListener { dialogHosts.dismiss() }
        dialogSshTunnelKeyBinding.addKeyCloseButton.setOnClickListener { dialogKeys.dismiss() }
        bind.sshPorts.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
           handleDeletePort(position)
        }
    }

    private fun handleDeletePort(position: Int) {
        if ((portsName?.size ?: 0) > 1 && position == (portsName?.size ?: (0 - 1))) {
            DialogUtils.createAlertDialog(context, "Delete All Hosts and Ports?") { viewModel.deleteHostPorts() }
        } else {
            val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
            if (portsName?.get(position)?.contains("@") == true) {
                setPortDialog(builder, position, "Delete Host  ")
            } else {
                setPortDialog(builder, position, "Delete Port ")
            }
            builder.setNegativeButton("Cancel", null)
            val dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent); dialog.show()
        }
    }


    private fun loadObservers1() {
        viewModel.tunnelSSHData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    bind.notifyNow.isEnabled = it.data?.enabledNotifyNow ?: false
                    bind.switchNotification.isEnabled = it.data?.enableSwitchNotification == true; bind.btnAddHosts.text = it.data?.addHostText
                    bind.btnAddPort.text = it.data?.addPortText; bind.btnAddPort.isEnabled = it.data?.enableAddPort == true
                    bind.btnAddHosts.isEnabled = it.data?.enableAddHost == true; bind.sshPorts.isEnabled = it.data?.enableSSHPort == true
                    dialogSshTunnelKeyBinding.publicKey.text = it.data?.publicKey; dialogSshTunnelKeyBinding.privateKey.text = it.data?.privateKey
                    dialogSshTunnelKeyBinding.progressBar.visibility = View.GONE
                    portsName = it.data?.portNames; hostsName = it.data?.hostNames
                    adapter = TunnelUtils.getPortAdapter(requireContext(), portsName)
                    bind.sshPorts.adapter = adapter
                    adapter2 = ArrayAdapter(requireContext(), R.layout.simple_list_item, hostsName!!)
                    dialogSshTunnelPortsBinding.hosts.adapter = adapter2
                }
                Status.LOADING -> {
                    if (it == null) return@Observer
                    dialogSshTunnelKeyBinding.progressBar.visibility = View.VISIBLE
                } else -> {}
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeDialog() {
        dialogPort = Dialog(requireContext())
        dialogSshTunnelPortsBinding = DialogSshtunnelPortsBinding.inflate(layoutInflater)
        dialogPort.setContentView(dialogSshTunnelPortsBinding.root)

        dialogHosts = Dialog(requireContext())
        dialogSshTunnelHostsBinding = DialogSshtunnelHostsBinding.inflate(layoutInflater)
        dialogHosts.setContentView(dialogSshTunnelHostsBinding.root)

        dialogKeys = Dialog(requireContext())
        dialogSshTunnelKeyBinding = DialogSshtunnelKeyBinding.inflate(layoutInflater)
        dialogKeys.setContentView(dialogSshTunnelKeyBinding.root)

        addHostSyntaxCheck(dialogSshTunnelHostsBinding.UserNameInput, dialogSshTunnelHostsBinding.TLusername, Constants.userRegex, Constants.hostError)
        addHostSyntaxCheck(dialogSshTunnelHostsBinding.DomainIPInput, dialogSshTunnelHostsBinding.TLdomain, "${Constants.domainRegex}|${Constants.ipRegex}", Constants.domainIPError)
        addHostSyntaxCheck(dialogSshTunnelHostsBinding.PortNumberInput, dialogSshTunnelHostsBinding.TLportname, Constants.portRegex, Constants.portError)
        addPortSyntaxCheck(dialogSshTunnelPortsBinding.ExternalTextInput, dialogSshTunnelPortsBinding.TLexternal)
        addPortSyntaxCheck(dialogSshTunnelPortsBinding.InternalTextInput, dialogSshTunnelPortsBinding.TLinternal)
        viewModel.initializeArrays()
        val window = dialogPort.window
        val windowHost = dialogHosts.window
        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        windowHost?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        windowHost?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    fun checkAddingHostButtonEnable() {
        if (dialogSshTunnelHostsBinding.UserNameInput.editableText.isNotEmpty() &&
            dialogSshTunnelHostsBinding.DomainIPInput.editableText.isNotEmpty()
            && dialogSshTunnelHostsBinding.PortNumberInput.editableText.isNotEmpty()) {
            if (!dialogSshTunnelHostsBinding.TLusername.isErrorEnabled && !dialogSshTunnelHostsBinding.TLdomain.isErrorEnabled && !dialogSshTunnelHostsBinding.TLportname.isErrorEnabled) {
                dialogSshTunnelHostsBinding.btnAddingHost.isEnabled = true
            }
        }
    }

    fun checkAddingPortButtonEnable() {
        if (dialogSshTunnelPortsBinding.ExternalTextInput.editableText.isNotEmpty() && dialogSshTunnelPortsBinding.InternalTextInput.editableText.isNotEmpty())
            if (!dialogSshTunnelPortsBinding.TLexternal.isErrorEnabled && !dialogSshTunnelPortsBinding.TLinternal.isErrorEnabled)
                dialogSshTunnelPortsBinding.btnAddingPort.isEnabled = true
    }

    private fun addHostSyntaxCheck(textInputEditText: TextInputEditText, textInputLayout: TextInputLayout, regex: String, error: String) {
        textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textInputLayout.isErrorEnabled = true
                if (s?.isEmpty() == true) {
                    dialogSshTunnelHostsBinding.btnAddingHost.isEnabled = false
                } else {
                    if (!"$s".matches(regex.toRegex())) {
                        dialogSshTunnelHostsBinding.btnAddingHost.isEnabled = false
                        textInputLayout.error = error
                    } else {
                        textInputLayout.isErrorEnabled = false
                        checkAddingHostButtonEnable()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun addPortSyntaxCheck(textInputEditText: TextInputEditText, textInputLayout: TextInputLayout) {
        textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textInputLayout.isErrorEnabled = true
                if (s?.isEmpty() == true) {
                    dialogSshTunnelPortsBinding.btnAddingPort.isEnabled = false
                } else {
                    if (!"$s".matches(Constants.portRegex.toRegex())) {
                        dialogSshTunnelPortsBinding.btnAddingPort.isEnabled = false
                        textInputLayout.error = Constants.portError
                    } else if (textInputEditText == dialogSshTunnelPortsBinding.ExternalTextInput && viewModel.searchArray(portsName, "$s")) {
                        dialogSshTunnelPortsBinding.btnAddingPort.isEnabled = false
                        dialogSshTunnelPortsBinding.TLexternal.error = "Port number already exists"
                    } else {
                        textInputLayout.isErrorEnabled = false
                        checkAddingPortButtonEnable()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}

