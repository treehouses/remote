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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.adapter.TunnelPortAdapter
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityTunnelSshFragmentBinding
import io.treehouses.remote.utils.*
import kotlinx.android.synthetic.main.dialog_sshtunnel_hosts.*
import kotlinx.android.synthetic.main.dialog_sshtunnel_key.*
import kotlinx.android.synthetic.main.dialog_sshtunnel_ports.*

class TunnelSSHFragment :  BaseFragment() {
    protected val viewModel: TunnelSSHViewModel by viewModels(ownerProducer = { this })
    private lateinit var bind: ActivityTunnelSshFragmentBinding
    var adapter: TunnelPortAdapter? = null
    lateinit var adapter2: ArrayAdapter<String>
    var portsName: java.util.ArrayList<String>? = null
    var hostsName: java.util.ArrayList<String>? = null
    var hostsPosition: java.util.ArrayList<Int>? = null
    lateinit var dialogPort: Dialog
    lateinit var dialogHosts: Dialog
    lateinit var dialogKeys: Dialog

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityTunnelSshFragmentBinding.inflate(inflater, container, false)
        viewModel.onCreateView()
        loadObservers()
        initializeDialog1()
        addListeners()
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.let { Tutorials.tunnelSSHTutorials(it, requireActivity()) }
    }

    private fun addListeners() {
        fun showDialog(dialog: Dialog) { dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent); dialog.show() }
        bind.switchNotification.setOnCheckedChangeListener { _, isChecked -> viewModel.switchButton(isChecked) }
        bind.btnAddPort.setOnClickListener{ showDialog(dialogPort) }; bind.btnAddHosts.setOnClickListener{ showDialog(dialogHosts) }
        bind.btnKeys.setOnClickListener { showDialog(dialogKeys) }; bind.notifyNow.setOnClickListener{ viewModel.notifyNow() }
        addDialogListeners()
    }

    fun addDialogListeners(){
        dialogPort.btn_adding_port.setOnClickListener {
            if (dialogPort.ExternalTextInput.text!!.isNotEmpty() && dialogPort.InternalTextInput.text!!.isNotEmpty()) {
                val s1 = dialogPort.InternalTextInput.text.toString(); val s2 = dialogPort.ExternalTextInput.text.toString()
                val parts = dialogPort.hosts?.selectedItem.toString().split(":")[0]
                viewModel.addingPortButton(s1, s2, parts)
                dialogPort.dismiss()
            }
        }
        dialogHosts.btn_adding_host.setOnClickListener {
            val m1 = dialogHosts.PortNumberInput.text.toString()
            val m2 = dialogHosts.UserNameInput.text.toString() + "@" + dialogHosts.DomainIPInput.text.toString()
            viewModel.addingHostButton(m1, m2)
            dialogHosts.dismiss()
        }
        dialogPort.addPortCloseButton.setOnClickListener { dialogPort.dismiss() }
        dialogHosts.addHostCloseButton.setOnClickListener { dialogHosts.dismiss() }
        dialogKeys.addKeyCloseButton.setOnClickListener { dialogKeys.dismiss() }
        addInfoAndPortListListener()
    }

    private fun addInfoAndPortListListener() {
        bind.info.setOnClickListener{
            val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle)); builder.setTitle("SSH Help")
            builder.setMessage(R.string.ssh_info); val dialog = builder.create();
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent); dialog.show();
        }
        bind.sshPorts.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            if (portsName!!.size > 1 && position == portsName!!.size - 1) {
                DialogUtils.createAlertDialog(context, "Delete All Hosts and Ports?") { viewModel.deleteHostPorts() }
            }
        }
    }

    fun loadObservers(){
        viewModel.notifyNowEnabled.observe(viewLifecycleOwner, Observer {
            bind.notifyNow.isEnabled = it
        })
        viewModel.switchChecked.observe(viewLifecycleOwner, Observer {
            bind.switchNotification.isChecked = it
        })
        viewModel.switchEnabled.observe(viewLifecycleOwner, Observer {
            bind.switchNotification.isEnabled = it
        })
        viewModel.addHostText.observe(viewLifecycleOwner, Observer {
            bind.btnAddHosts.text = it
        })
        viewModel.addHostEnabled.observe(viewLifecycleOwner, Observer {
            bind.btnAddHosts.isEnabled = it
        })
        viewModel.addPortText.observe(viewLifecycleOwner, Observer {
            bind.btnAddPort.text = it
        })
        viewModel.addPortEnabled.observe(viewLifecycleOwner, Observer {
            bind.btnAddPort.isEnabled = it
        })
        viewModel.sshPortEnabled.observe(viewLifecycleOwner, Observer {
            bind.sshPorts.isEnabled = it
        })
        viewModel.dialogKeysPublicText.observe(viewLifecycleOwner, Observer {
            dialogKeys.public_key.text = it
        })
        viewModel.dialogKeysPrivateText.observe(viewLifecycleOwner, Observer {
            dialogKeys.private_key.text = it
        })
        viewModel.progressBar.observe(viewLifecycleOwner, Observer {
            dialogKeys.progress_bar.visibility = it
        })
        viewModel.portsNameAdapter.observe(viewLifecycleOwner, Observer {
            portsName = it
            try {
                adapter = TunnelPortAdapter(requireContext(), portsName!!)
                logE("adapter successful")
            } catch (e: Exception) {
                logE(e.toString())
            }
            bind.sshPorts.adapter = adapter
        })
        viewModel.hostsNameAdapter.observe(viewLifecycleOwner, Observer {
            hostsName = it
            try {
                adapter2 = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, hostsName!!)
                logE("adapter successful")
            } catch (e: Exception) {
                logE(e.toString())
            }
            dialogPort.hosts.adapter = adapter2
        })
        viewModel.hostsPositionAdapter.observe(viewLifecycleOwner, Observer {
            hostsPosition = it
        })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeDialog1() {
        dialogPort = Dialog(requireContext()); dialogHosts = Dialog(requireContext()); dialogKeys = Dialog(requireContext())
        dialogPort.setContentView(R.layout.dialog_sshtunnel_ports); dialogHosts.setContentView(R.layout.dialog_sshtunnel_hosts)
        dialogKeys.setContentView(R.layout.dialog_sshtunnel_key)
        addHostSyntaxCheck(dialogHosts.UserNameInput, dialogHosts.TLusername, Constants.userRegex, Constants.hostError)
        addHostSyntaxCheck(dialogHosts.DomainIPInput, dialogHosts.TLdomain, Constants.domainRegex + "|" + Constants.ipRegex, Constants.domainIPError)
        addHostSyntaxCheck(dialogHosts.PortNumberInput, dialogHosts.TLportname, Constants.portRegex, Constants.portError)
        addPortSyntaxCheck(dialogPort.ExternalTextInput, dialogPort.TLexternal)
        addPortSyntaxCheck(dialogPort.InternalTextInput, dialogPort.TLinternal)
        viewModel.initializeArrays()
        val window = dialogPort.window; val windowHost = dialogHosts.window
        window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        windowHost!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        windowHost.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        try { initializeDialog2() }
        catch (exception: Exception) { }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initializeDialog2() {
        bind.sshPorts.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
            if (portsName!![position].contains("@")) {
                builder.setTitle("Delete Host  " + portsName!![position] + " ?")
                builder.setPositiveButton("Confirm") { dialog, _ ->
                    viewModel.deleteHost(position)
                    dialog.dismiss()
                }
            }
//            initializeDialog3(builder, position)
            builder.setTitle("Delete Port " + portsName!![position] + " ?")
            builder.setPositiveButton("Confirm") { dialog, _ ->
                viewModel.deletePort(position)
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel", null)
            val dialog = builder.create()
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
//        initializeDialog4()
        var profile = dialogKeys.findViewById<EditText>(R.id.sshtunnel_profile).text.toString()
        dialogKeys.btn_save_keys.setOnClickListener { viewModel.keyClickListener(profile); }
        dialogKeys.btn_show_keys.setOnClickListener {
            viewModel.keyClickListener(profile)
            viewModel.handleShowKeys(profile)
        }
    }

//    private fun initializeDialog3(builder: AlertDialog.Builder, position: Int) {
//        builder.setTitle("Delete Port " + portsName!![position] + " ?")
//        builder.setPositiveButton("Confirm") { dialog, _ ->
//            viewModel.deletePort(position)
//            dialog.dismiss()
//        }
//    }

//    @RequiresApi(Build.VERSION_CODES.N)
//    private fun initializeDialog4() {
//        var profile = dialogKeys.findViewById<EditText>(R.id.sshtunnel_profile).text.toString()
//        dialogKeys.btn_save_keys.setOnClickListener { viewModel.keyClickListener(profile); }
//        dialogKeys.btn_show_keys.setOnClickListener {
//            viewModel.keyClickListener(profile)
//            viewModel.handleShowKeys(profile)
//        }
//    }

    override fun setUserVisibleHint(visible: Boolean) {
        if (visible) {
            viewModel.setUserVisibleHint()
        }
    }

    /*
       checks if the inputs in the host dialog are not empty. If so, check if their corresponding textLayouts don't have errors.
       If so, then enable addingHostButton.
         */
    fun checkAddingHostButtonEnable(){
        if(dialogHosts.UserNameInput.editableText.isNotEmpty() && dialogHosts.DomainIPInput.editableText.isNotEmpty()
                && dialogHosts.PortNumberInput.editableText.isNotEmpty())
            if(!dialogHosts.TLusername.isErrorEnabled && !dialogHosts.TLdomain.isErrorEnabled && !dialogHosts.TLportname.isErrorEnabled )
                dialogHosts.btn_adding_host.isEnabled = true
    }

    fun checkAddingPortButtonEnable(){
        if(dialogPort.ExternalTextInput.editableText.isNotEmpty() && dialogPort.InternalTextInput.editableText.isNotEmpty())
            if(!dialogPort.TLexternal.isErrorEnabled && !dialogPort.TLinternal.isErrorEnabled)
                dialogPort.btn_adding_port.isEnabled = true
    }

    /*
       adds a syntax check to textInputEditText. If input in textInputEditText does not match regex, outputs error message in textInputLayout
       and disables addingHostButton
         */
    fun addHostSyntaxCheck(textInputEditText: TextInputEditText, textInputLayout: TextInputLayout, regex: String, error: String){
        textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textInputLayout.setErrorEnabled(true)
                if(s!!.isEmpty()){
                    dialogHosts.btn_adding_host.isEnabled = false
                } else {
                    if(!s!!.toString().matches(regex.toRegex()) ){
                        dialogHosts.btn_adding_host.isEnabled = false
                        textInputLayout.setError(error)
                    } else {
                        textInputLayout.setErrorEnabled(false)
                        checkAddingHostButtonEnable()
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    fun addPortSyntaxCheck(textInputEditText: TextInputEditText, textInputLayout: TextInputLayout){
        textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                textInputLayout.setErrorEnabled(true)
                if (s!!.isEmpty()) {
                    dialogPort.btn_adding_port.isEnabled = false
                } else {
                    if (!s!!.toString().matches(Constants.portRegex.toRegex())) {
                        dialogPort.btn_adding_port.isEnabled = false
                        textInputLayout.setError(Constants.portError)
                    } else if (textInputEditText.equals(dialogPort.ExternalTextInput) && viewModel.searchArray(portsName, s!!.toString())) {
                        dialogPort.btn_adding_port.isEnabled = false
                        dialogPort.TLexternal.setError("Port number already exists")
                    } else {
                        textInputLayout.setErrorEnabled(false)
                        checkAddingPortButtonEnable()
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}

