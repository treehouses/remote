package io.treehouses.remote.Fragments

import android.app.Dialog
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityTunnelSshFragmentBinding

class TunnelSSHFragment : BaseFragment(), View.OnClickListener {
    private var addPortButton: Button? = null
    private var addHostButton: Button? = null
    var bind: ActivityTunnelSshFragmentBinding? = null
    private var dropdown: Spinner? = null
    private var portList: ListView? = null
    private var adapter: ArrayAdapter<String>? = null
    private var portsName: java.util.ArrayList<String>? = null
    private var hostsName: java.util.ArrayList<String>? = null

    private lateinit var dialogHosts:Dialog
    private lateinit var inputExternalHost: TextInputEditText
    private lateinit var inputInternalHost: TextInputEditText
    private lateinit var inputExternal: TextInputEditText
    private lateinit var inputInternal: TextInputEditText
    private lateinit var dialog:Dialog
    private lateinit var addingPortButton: Button
    private lateinit var addingHostButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityTunnelSshFragmentBinding.inflate(inflater, container, false)
        bind!!.switchNotification.isEnabled = false
        bind!!.notifyNow.isEnabled = false
        portList = bind!!.sshPorts
        initializeDialog()
        addPortButton = bind!!.btnAddPort
        addHostButton = bind!!.btnAddHosts
        arrayOf("1", "2", "three")
        hostsName = ArrayList()
        val adapter: ArrayAdapter<String> = ArrayAdapter(this.requireContext(), R.layout.support_simple_spinner_dropdown_item, hostsName!!)
        dropdown?.adapter = adapter
        dropdown?.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3:
            Long) {
                Log.d("winwin", "YYYYY ")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {Log.d("nothing", "YYYYY ")}
        }
        bind!!.switchNotification.setOnCheckedChangeListener { _, isChecked -> switchButton(isChecked) }
        addPortButton!!.setOnClickListener(this)
        addHostButton!!.setOnClickListener(this)
        addingPortButton.setOnClickListener(this)
        addingHostButton.setOnClickListener(this)
        bind!!.notifyNow.setOnClickListener(this)
        return bind!!.root
    }

    private fun initializeDialog(){
        dialog = Dialog(requireContext())
        dropdown = dialog.findViewById(R.id.hosts)
        dialogHosts = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_sshtunnel_ports)
        dialogHosts.setContentView(R.layout.dialog_sshtunnel_hosts)
        inputExternal = dialog.findViewById(R.id.ExternalTextInput)
        inputInternal = dialog.findViewById(R.id.InternalTextInput)
        inputExternalHost = dialogHosts.findViewById(R.id.ExternalTextInput)
        inputInternalHost = dialogHosts.findViewById(R.id.InternalTextInput)
        addingPortButton = dialog.findViewById<Button>(R.id.btn_adding_port)
        addingHostButton = dialogHosts.findViewById<Button>(R.id.btn_adding_host)
        val window = dialog.window
        val windowHost = dialogHosts.window
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        windowHost!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        windowHost.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    private fun switchButton(isChecked:Boolean) {
        if (isChecked) {
            bind!!.switchNotification.isEnabled = false
            listener.sendMessage("treehouses sshtunnel notice on")
        } else {
            bind!!.switchNotification.isEnabled = false
            listener.sendMessage("treehouses sshtunnel notice off")
        }
    }

    private fun addingHostButton(){
        if (inputExternalHost.text.toString().isNotEmpty() && inputInternalHost.text.toString().isNotEmpty() ) {
            val s1 = inputInternalHost.text.toString()
            val s2 = inputExternalHost.text.toString()

            listener.sendMessage("treehouses sshtunnel add host $s1 $s2")
            addHostButton!!.text = "Adding......"
            addHostButton!!.isEnabled = false
            dialogHosts.dismiss()
        }
    }

    private fun addingPortButton(){
        if (inputExternal.text!!.isNotEmpty() && inputInternal.text!!.isNotEmpty()) {
            val s1 = inputInternal.text.toString()
            val s2 = inputExternal.text.toString()
            val parts = dropdown?.selectedItem.toString().split(":")[0]

            listener.sendMessage("treehouses sshtunnel add port actual $s2 $s1 $parts")
            addPortButton!!.text = "Adding......"
            addPortButton!!.isEnabled = false
            dialog.dismiss()

        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.btn_adding_host -> { addingHostButton() }
            R.id.btn_adding_port -> { addingPortButton() }
            R.id.notify_now -> {
                bind!!.notifyNow.isEnabled = false
                listener.sendMessage("treehouses sshtunnel notice now")
            }
            R.id.btn_add_port -> dialog.show()
            R.id.btn_add_hosts -> dialogHosts.show()
        }
    }

    override fun setUserVisibleHint(visible: Boolean) {
        if(visible) {
            mChatService = listener.getChatService()
            mChatService.updateHandler(mHandler)

            listener.sendMessage("treehouses sshtunnel ports")
            bind!!.sshPorts
            portsName = ArrayList()

            adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, portsName!!)
            Log.i("Tag", "Reload fragment")
        }
    }
    override fun getMessage(msg: Message) {
            if (msg.what == Constants.MESSAGE_READ) {
                val readMessage: String = msg.obj as String
                Log.d("SSHTunnel reply", "" + readMessage)
                if(readMessage.contains("Error when")) {
                    addHostButton?.isEnabled = true
                    addHostButton?.text = "Add Host"
                    if(readMessage.contains("'treehouses sshtunnel ports'")){
                        Toast.makeText(requireContext(), "Please add a host if you have no host. Don't add duplicate host also.", Toast.LENGTH_SHORT).show()
                        addPortButton?.isEnabled = false
                    }
                    else{
                        Toast.makeText(requireContext(), "No duplicate hosts allowed", Toast.LENGTH_SHORT).show()
                    }
                }
                else if(readMessage.contains("ssh-rsa") || readMessage.contains("Added")){
                    Toast.makeText(requireContext(), "Added. Retrieving port list.", Toast.LENGTH_SHORT).show()
                    addPortButton?.text = "Retrieving"
                    addHostButton?.text = "Retrieving"
                    portsName = ArrayList()
                    hostsName = ArrayList()
                    listener.sendMessage("treehouses sshtunnel ports")
                }
                else { getMessage2(readMessage) }
            }
    }

    private fun getMessage2(readMessage: String) {
        if (readMessage.contains("ole@")) {
            addPortButton?.isEnabled = true
            addPortButton?.text = "Add Port"
            addHostButton?.text = "Add Host"
            addPortButton!!.isEnabled = true
            addHostButton?.isEnabled = true
            val hosts = readMessage.split('\n')
            for (host in hosts) {
                val ports = host.split(' ')
                for (port in ports) {
                    if (port.length >= 3)
                        portsName!!.add(port)
                    if (port.contains("ole@"))
                        hostsName!!.add(port)
                }
                val adapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, hostsName!!)
                dropdown?.adapter = adapter
            }
            listener.sendMessage("treehouses sshtunnel notice")
            adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, portsName!!)
            bind!!.sshPorts.adapter = adapter
        }
        else {
            getMessage3(readMessage)
        }
    }

    private fun getMessage3(readMessage: String) {
        when {
            readMessage.contains("Status: on") -> {
                bind!!.switchNotification.isChecked = true
                bind!!.switchNotification.isEnabled = true
                bind!!.notifyNow.isEnabled = true }
            readMessage.contains("Status: off") -> {
                bind!!.switchNotification.isChecked = false
                bind!!.switchNotification.isEnabled = true
                bind!!.notifyNow.isEnabled = true }
            readMessage.contains("OK.") -> { listener.sendMessage("treehouses sshtunnel notice") }
            readMessage.contains("Thanks for the feedback!") -> {
                Toast.makeText(requireContext(), "Notified Gitter. Thank you!", Toast.LENGTH_SHORT).show()
                bind!!.notifyNow.isEnabled = true }
            readMessage.contains("Error: only 'list'") -> {
                listener.sendMessage("treehouses sshtunnel ports")
                Toast.makeText(requireContext(), "Please swipe slower in the future as you have a slow rpi, getting ports again...", Toast.LENGTH_SHORT).show() }
        }
    }
}

