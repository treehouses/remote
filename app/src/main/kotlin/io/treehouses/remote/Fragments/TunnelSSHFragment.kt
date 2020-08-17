package io.treehouses.remote.Fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.*
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityTunnelSshFragmentBinding

import kotlinx.android.synthetic.main.dialog_container.view.*
import kotlinx.android.synthetic.main.dialog_rename.*
import kotlinx.android.synthetic.main.hotspot_dialog.*
import java.lang.Exception
import kotlin.math.log


class TunnelSSHFragment : BaseFragment(), View.OnClickListener {
    private var addPortButton: Button? = null
    private var addHostButton: Button? = null
    var bind: ActivityTunnelSshFragmentBinding? = null
    private var dropdown: Spinner? = null
    private var portList: ListView? = null
    private var adapter: ArrayAdapter<String>? = null
    private var portsName: java.util.ArrayList<String>? = null
    private var hostsName: java.util.ArrayList<String>? = null
    private var hostsPosition: java.util.ArrayList<Int>? = null
    private lateinit var dialogHosts:Dialog
    private lateinit var inputExternalHost: TextInputEditText
    private lateinit var inputInternalHost: TextInputEditText
    private lateinit var inputExternal: TextInputEditText
    private lateinit var inputInternal: TextInputEditText
    private lateinit var dialog:Dialog
    private lateinit var addingPortButton: Button
    private lateinit var addingHostButton: Button
    private lateinit var adapter2: ArrayAdapter<String>
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityTunnelSshFragmentBinding.inflate(inflater, container, false)
        bind!!.switchNotification.isEnabled = false
        bind!!.notifyNow.isEnabled = false
        portList = bind!!.sshPorts
        initializeDialog1()
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
        addListeners()
        return bind!!.root
    }

    private fun addListeners() {
        bind!!.switchNotification.setOnCheckedChangeListener { _, isChecked -> switchButton(isChecked) }
        addPortButton!!.setOnClickListener(this)
        addHostButton!!.setOnClickListener(this)
        addingPortButton.setOnClickListener(this)
        addingHostButton.setOnClickListener(this)
        bind!!.notifyNow.setOnClickListener(this)
    }


    private fun initializeDialog1() {
        dialog = Dialog(requireContext())
        dialogHosts = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_sshtunnel_ports)
        dropdown = dialog.findViewById(R.id.hosts)
        dialogHosts.setContentView(R.layout.dialog_sshtunnel_hosts)
        inputExternal = dialog.findViewById(R.id.ExternalTextInput)
        inputInternal = dialog.findViewById(R.id.InternalTextInput)
        inputExternalHost = dialogHosts.findViewById(R.id.ExternalTextInput)
        inputInternalHost = dialogHosts.findViewById(R.id.InternalTextInput)
        addingPortButton = dialog.findViewById<Button>(R.id.btn_adding_port)
        addingHostButton = dialogHosts.findViewById<Button>(R.id.btn_adding_host)
        portsName = ArrayList()
        hostsName = ArrayList()
        hostsPosition = ArrayList()
        val window = dialog.window
        val windowHost = dialogHosts.window
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        windowHost!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        windowHost.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        try{ initializeDialog2()} catch (exception:Exception){Log.e("Error1", exception.toString())}
    }

    private fun initializeDialog2(){
        portList!!.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.CustomAlertDialogStyle))
            if(portsName!![position].contains("@")){
                builder.setTitle("Delete Host  " + portsName!![position] + " ?")
                builder.setPositiveButton("Confirm") { dialog, _ ->
                    val parts = portsName!![position].split(":")[0]
                    listener.sendMessage("treehouses sshtunnel remove host $parts")
                    addHostButton!!.text = "deleting host ....."
                    portList!!.isEnabled = false
                    addHostButton!!.isEnabled = false
                    dialog.dismiss()
                }
            }
            else{
                initializeDialog3(builder, position)
            }
            builder.setNegativeButton("Cancel", null)
            val dialog = builder.create()
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            Log.d("dialoging", "dialog")
            dialog.show()
        }
    }

    private fun initializeDialog3(builder:AlertDialog.Builder, position:Int){
        builder.setTitle("Delete Port " + portsName!![position] + " ?")
        builder.setPositiveButton("Confirm") { dialog, _ ->
            var myPos:Int = 0
            for(pos in hostsPosition!!.indices){
                if(hostsPosition!![pos] > position){
                    myPos = pos
                    break
                }
            }
            if(hostsPosition!!.last() < position){
                myPos = hostsPosition!!.lastIndex
            }
            Log.d("dasda", myPos.toString())
            listener.sendMessage("treehouses sshtunnel remove port " + portsName!![position].split(":".toRegex(), 2).toTypedArray()[0] + " " + hostsName!![myPos].split(":")[0])
            addPortButton!!.text = "deleting port ....."
            portList!!.isEnabled = false
            addPortButton!!.isEnabled = false
            dialog.dismiss()
        }
    }

    private fun switchButton(isChecked:Boolean) {
        bind!!.switchNotification.isEnabled = false
        if (isChecked)  listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE_ON))
        else listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE_OFF))
    }

    private fun addingHostButton(){
        if (inputExternalHost.text.toString().isNotEmpty() && inputInternalHost.text.toString().isNotEmpty() ) {
            if(inputExternalHost.text.toString().contains("@")) {
                val s1 = inputInternalHost.text.toString()
                val s2 = inputExternalHost.text.toString()
                listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_ADD_HOST, s1, s2))
                addHostButton!!.text = "Adding......"
                addHostButton!!.isEnabled = false
            }
            else{
                Toast.makeText(requireContext(), "Invalid host name", Toast.LENGTH_SHORT).show()
            }


            dialogHosts.dismiss()
        }





    }


    private fun addingPortButton(){
        if (inputExternal.text!!.isNotEmpty() && inputInternal.text!!.isNotEmpty()) {
            val s1 = inputInternal.text.toString()
            val s2 = inputExternal.text.toString()
            val parts = dropdown?.selectedItem.toString().split(":")[0]

            listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_ADD_PORT_ACTUAL, s2, s1, parts))
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
                listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE_NOW))
            }
            R.id.btn_add_port -> dialog.show()
            R.id.btn_add_hosts -> dialogHosts.show()
        }
    }

    override fun setUserVisibleHint(visible: Boolean) {
        if(visible) {
            mChatService = listener.getChatService()
            mChatService.updateHandler(mHandler)

            listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE))
            bind!!.sshPorts
            portsName = ArrayList()
//            listener.sendMessage("treehouses sshtunnel notice")
            adapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item, portsName!!)
            Log.i("Tag", "Reload fragment")
        }
    }
    override fun getMessage(msg: Message) {
            if (msg.what == Constants.MESSAGE_READ) {
                var Position:Int = 0
                val readMessage: String = msg.obj as String
                Log.d("SSHTunnel reply", "" + readMessage)

                if(readMessage.contains("Host / port not found")) {
                    addHostButton?.isEnabled = true
                    addHostButton?.text = "Add Host"

                    addPortButton?.text = "Add Port"
                    portList?.isEnabled = true
                    addHostButton?.isEnabled = true
                    Toast.makeText(requireContext(), "incorrect deleting host/port, try again", Toast.LENGTH_SHORT).show()
                }
                else if(readMessage.contains("ssh-rsa") || readMessage.contains("Added") || readMessage.contains("Removed")){
                    Toast.makeText(requireContext(), "Added/Removed. Retrieving port list.", Toast.LENGTH_SHORT).show()

                    addPortButton?.text = "Retrieving"
                    addHostButton?.text = "Retrieving"
                    portsName = ArrayList()
                    hostsName = ArrayList()

                    hostsPosition = ArrayList()
                    listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))
                }

                else if (readMessage.contains("@")) {
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
                            if (port.contains("@")) {
                                hostsPosition!!.add(Position)
                                hostsName!!.add(port)
                            }
                            Position += 1
                        }

                    }
                   adapter2 = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, hostsName!!)
                    dropdown?.adapter = adapter2
                    adapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item, portsName!!)
                    bind!!.sshPorts.adapter = adapter
                    portList!!.isEnabled = true
                }
                else if(readMessage.contains("the command 'treehouses sshtunnel ports' returns nothing")){
                    adapter2 = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, hostsName!!)
                    dropdown?.adapter = adapter2
                    adapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item, portsName!!)
                    bind!!.sshPorts.adapter = adapter
                    portList!!.isEnabled = true
                    addPortButton!!.text = "Add Port"
                    addHostButton!!.text = "Add Host"
                    addPortButton!!.isEnabled = false
                    addHostButton!!.isEnabled = true
                    Toast.makeText(requireContext(), "Add a host", Toast.LENGTH_SHORT).show()

                }
                else if(readMessage.contains("Status: on")){
                    bind!!.switchNotification.isChecked = true;
                    bind!!.switchNotification.isEnabled = true;
                    bind!!.notifyNow.isEnabled = true
                    portsName = ArrayList()
                    hostsName = ArrayList()
                    hostsPosition = ArrayList()
                    listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))

                }
                else  getMessage2(readMessage)

            }
    }

    private fun getMessage2(readMessage: String) {

         getMessage3(readMessage)
    }

    private fun getMessage3(readMessage: String) {
        when {
            readMessage.contains("Status: on") -> {
                bind?.apply {
                    switchNotification.isChecked = true
                    switchNotification.isEnabled = true
                    notifyNow.isEnabled = true
                }
            }
            readMessage.contains("Status: off") -> {
                bind?.apply {
                    switchNotification.isChecked = false
                    switchNotification.isEnabled = true
                    notifyNow.isEnabled = true
                }
            }
            readMessage.contains("OK.") -> { listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE)) }
            readMessage.contains("Thanks for the feedback!") -> {
                Toast.makeText(requireContext(), "Notified Gitter. Thank you!", Toast.LENGTH_SHORT).show()
                bind!!.notifyNow.isEnabled = true }
            readMessage.contains("Error: only 'list'") -> {
                listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE))
                Toast.makeText(requireContext(), "Please swipe slower in the future as you have a slow rpi, getting ports again...", Toast.LENGTH_SHORT).show() }
        }
    }
}

