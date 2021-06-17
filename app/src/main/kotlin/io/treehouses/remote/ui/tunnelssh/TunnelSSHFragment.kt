package io.treehouses.remote.ui.tunnelssh

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
import io.treehouses.remote.pojo.enum.Status
import io.treehouses.remote.utils.*
import kotlinx.android.synthetic.main.dialog_sshtunnel_hosts.*
import kotlinx.android.synthetic.main.dialog_sshtunnel_key.*
import kotlinx.android.synthetic.main.dialog_sshtunnel_ports.*

class TunnelSSHFragment : BaseTunnelSSHFragment() {


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        viewModel.tunnelSSHKeyDialogData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    if (it.data!!.showHandleDifferentKeysDialog)
                        handleDifferentKeys(it.data)
                    if (it.data!!.showHandlePhoneKeySaveDialog)
                        handlePhoneKeySave(it.data.profile, it.data.piPublicKey, it.data.piPrivateKey)
                    if (it.data!!.showHandlePiKeySaveDialog) handlePiKeySave(it.data.profile, it.data.storedPublicKey, it.data.storedPrivateKey)
                }
            }
        })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Tutorials.tunnelSSHTutorials(bind, requireActivity())
        viewModel.setUserVisibleHint()
    }

    fun showDialog(dialog: Dialog) {
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show()
    }

    private fun addListeners1() {
        bind.switchNotification.setOnCheckedChangeListener { _, isChecked -> viewModel.switchButton(isChecked) }
        bind.btnAddPort.setOnClickListener { showDialog(dialogPort) };
        bind.btnAddHosts.setOnClickListener { showDialog(dialogHosts) }
        bind.btnKeys.setOnClickListener { showDialog(dialogKeys) };
        bind.notifyNow.setOnClickListener { viewModel.notifyNow(requireContext()) }
        bind.info.setOnClickListener {
            val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle)); builder.setTitle("SSH Help")
            builder.setMessage(R.string.ssh_info);
            val dialog = builder.create();
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent); dialog.show();
        }
        dialogPort.btn_adding_port.setOnClickListener { handleAddPort() }
        dialogHosts.btn_adding_host.setOnClickListener {
            val m1 = dialogHosts.PortNumberInput.text.toString()
            val m2 = dialogHosts.UserNameInput.text.toString() + "@" + dialogHosts.DomainIPInput.text.toString()
            viewModel.addingHostButton(m1, m2)
            dialogHosts.dismiss()
        }
    }

    private fun handleAddPort() {
        if (dialogPort.ExternalTextInput.text!!.isNotEmpty() && dialogPort.InternalTextInput.text!!.isNotEmpty()) {
            val parts = dialogPort.hosts?.selectedItem.toString().split(":")[0]
            viewModel.addingPortButton(dialogPort.InternalTextInput.text.toString(), dialogPort.ExternalTextInput.text.toString(), parts)
            dialogPort.dismiss()
        }
    }

    private fun addListeners2() {
        var profile = dialogKeys.findViewById<EditText>(R.id.sshtunnel_profile).text.toString()
        dialogKeys.btn_save_keys.setOnClickListener { viewModel.keyClickListener(profile); }
        dialogKeys.btn_show_keys.setOnClickListener {
            viewModel.keyClickListener(profile); viewModel.handleShowKeys(profile)
        }
        dialogPort.addPortCloseButton.setOnClickListener { dialogPort.dismiss() }
        dialogHosts.addHostCloseButton.setOnClickListener { dialogHosts.dismiss() }
        dialogKeys.addKeyCloseButton.setOnClickListener { dialogKeys.dismiss() }
        bind.sshPorts.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            if (portsName!!.size > 1 && position == portsName!!.size - 1) {
                DialogUtils.createAlertDialog(context, "Delete All Hosts and Ports?") { viewModel.deleteHostPorts() }
            } else {
                val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
                if (portsName!![position].contains("@")) {
                    setPortDialog(builder, position, "Delete Host  ")
                } else {
                    setPortDialog(builder, position, "Delete Port ")
                }
                builder.setNegativeButton("Cancel", null)
                val dialog = builder.create()
                dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent); dialog.show()
            }
        }
    }

    private fun setPortDialog(builder: AlertDialog.Builder, position: Int, title: String) {
        builder.setTitle(title + portsName!![position] + " ?")
        builder.setPositiveButton("Confirm") { dialog, _ ->
            if (title.contains("Host")) viewModel.deleteHost(position)
            else viewModel.deletePort(position)
            dialog.dismiss()
        }
    }

    private fun loadObservers1() {
        viewModel.tunnelSSHData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            when (it.status) {
                Status.SUCCESS -> {
                    bind.notifyNow.isEnabled = it.data!!.enabledNotifyNow
                    bind.switchNotification.isEnabled = it.data.enableSwitchNotification; bind.btnAddHosts.text = it.data.addHostText
                    bind.btnAddPort.text = it.data.addPortText; bind.btnAddPort.isEnabled = it.data.enableAddPort
                    bind.btnAddHosts.isEnabled = it.data.enableAddHost; bind.sshPorts.isEnabled = it.data.enableSSHPort
                    dialogKeys.public_key.text = it.data.publicKey; dialogKeys.private_key.text = it.data.privateKey
                    dialogKeys.progress_bar.visibility = View.GONE
                    portsName = it.data.portNames; hostsName = it.data.hostNames
                    adapter = TunnelUtils.getPortAdapter(requireContext(), portsName)
                    bind.sshPorts.adapter = adapter
                    adapter2 = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, hostsName!!)
                    dialogPort.hosts.adapter = adapter2
                }
                Status.LOADING -> {
                    if (it == null) return@Observer
                    dialogKeys.progress_bar.visibility = View.VISIBLE
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeDialog() {
        dialogPort = Dialog(requireContext()); dialogHosts = Dialog(requireContext()); dialogKeys = Dialog(requireContext())
        dialogPort.setContentView(R.layout.dialog_sshtunnel_ports); dialogHosts.setContentView(R.layout.dialog_sshtunnel_hosts)
        dialogKeys.setContentView(R.layout.dialog_sshtunnel_key)
        addHostSyntaxCheck(dialogHosts.UserNameInput, dialogHosts.TLusername, Constants.userRegex, Constants.hostError)
        addHostSyntaxCheck(dialogHosts.DomainIPInput, dialogHosts.TLdomain, Constants.domainRegex + "|" + Constants.ipRegex, Constants.domainIPError)
        addHostSyntaxCheck(dialogHosts.PortNumberInput, dialogHosts.TLportname, Constants.portRegex, Constants.portError)
        addPortSyntaxCheck(dialogPort.ExternalTextInput, dialogPort.TLexternal)
        addPortSyntaxCheck(dialogPort.InternalTextInput, dialogPort.TLinternal)
        viewModel.initializeArrays()
        val window = dialogPort.window;
        val windowHost = dialogHosts.window
        window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        windowHost!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        windowHost.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

//
//    override fun setUserVisibleHint(visible: Boolean) {
//        if (visible) {
//
//        }
//    }

    fun checkAddingHostButtonEnable() {
        if (dialogHosts.UserNameInput.editableText.isNotEmpty() && dialogHosts.DomainIPInput.editableText.isNotEmpty()
                && dialogHosts.PortNumberInput.editableText.isNotEmpty())
            if (!dialogHosts.TLusername.isErrorEnabled && !dialogHosts.TLdomain.isErrorEnabled && !dialogHosts.TLportname.isErrorEnabled)
                dialogHosts.btn_adding_host.isEnabled = true

    }

    fun checkAddingPortButtonEnable() {
        if (dialogPort.ExternalTextInput.editableText.isNotEmpty() && dialogPort.InternalTextInput.editableText.isNotEmpty())
            if (!dialogPort.TLexternal.isErrorEnabled && !dialogPort.TLinternal.isErrorEnabled)
                dialogPort.btn_adding_port.isEnabled = true
    }

    /*
       adds a syntax check to textInputEditText. If input in textInputEditText does not match regex, outputs error message in textInputLayout
       and disables addingHostButton
         */
    fun addHostSyntaxCheck(textInputEditText: TextInputEditText, textInputLayout: TextInputLayout, regex: String, error: String) {
        textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textInputLayout.isErrorEnabled = true
                if (s!!.isEmpty()) {
                    dialogHosts.btn_adding_host.isEnabled = false
                } else {
                    if (!s!!.toString().matches(regex.toRegex())) {
                        dialogHosts.btn_adding_host.isEnabled = false
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

    fun addPortSyntaxCheck(textInputEditText: TextInputEditText, textInputLayout: TextInputLayout) {
        textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textInputLayout.isErrorEnabled = true
                if (s!!.isEmpty()) {
                    dialogPort.btn_adding_port.isEnabled = false
                } else {
                    if (!s!!.toString().matches(Constants.portRegex.toRegex())) {
                        dialogPort.btn_adding_port.isEnabled = false
                        textInputLayout.error = Constants.portError
                    } else if (textInputEditText == dialogPort.ExternalTextInput && viewModel.searchArray(portsName, s!!.toString())) {
                        dialogPort.btn_adding_port.isEnabled = false
                        dialogPort.TLexternal.error = "Port number already exists"
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

