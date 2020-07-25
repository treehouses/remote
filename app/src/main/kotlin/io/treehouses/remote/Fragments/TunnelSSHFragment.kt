package io.treehouses.remote.Fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.*
import android.widget.*
import bolts.Task.delay
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityTunnelSshFragmentBinding
import kotlinx.android.synthetic.main.dialog_container.view.*
import kotlinx.android.synthetic.main.dialog_rename.*
import kotlinx.android.synthetic.main.hotspot_dialog.*
import kotlin.math.log

class TunnelSSHFragment : BaseFragment() {
    private var addPortButton: Button? = null
    private var addHostButton: Button? = null
    var bind: ActivityTunnelSshFragmentBinding? = null
    private var dropdown: Spinner? = null
    private var portList: ListView? = null
    private var adapter: ArrayAdapter<String>? = null
    private var portsName: java.util.ArrayList<String>? = null
    private var hostsName: java.util.ArrayList<String>? = null
    private var hostsPosition: java.util.ArrayList<Int>? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        bind = ActivityTunnelSshFragmentBinding.inflate(inflater, container, false)
        bind!!.switchNotification.isEnabled = false;
        bind!!.notifyNow.isEnabled = false
        bind!!.switchNotification.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                bind!!.switchNotification.isEnabled = false
                listener.sendMessage("treehouses sshtunnel notice on")
            } else {
                bind!!.switchNotification.isEnabled = false
                listener.sendMessage("treehouses sshtunnel notice off")
            }
        }
        bind!!.notifyNow.setOnClickListener {
            bind!!.notifyNow.isEnabled = false
            listener.sendMessage("treehouses sshtunnel notice now")
        }
        portList = bind!!.sshPorts
        val dialog = Dialog(requireContext())
        val dialogHosts = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_sshtunnel_ports)
        dialogHosts.setContentView(R.layout.dialog_sshtunnel_hosts)
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialogHosts.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        val window = dialog.window
        val windowHost = dialogHosts.window
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        windowHost!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val inputExternal: TextInputEditText = dialog.findViewById(R.id.ExternalTextInput)
        val inputInternal: TextInputEditText = dialog.findViewById(R.id.InternalTextInput)
        val inputExternalHost: TextInputEditText = dialogHosts.findViewById(R.id.ExternalTextInput)
        val inputInternalHost: TextInputEditText = dialogHosts.findViewById(R.id.InternalTextInput)
        val addingPortButton = dialog.findViewById<Button>(R.id.btn_adding_port)
        val addingHostButton = dialogHosts.findViewById<Button>(R.id.btn_adding_host)

        dropdown = dialog.findViewById(R.id.hosts)
        addPortButton = bind!!.btnAddPort
            addHostButton = bind!!.btnAddHosts
            hostsName = ArrayList()
            hostsPosition = ArrayList()
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
        addPortButton!!.setOnClickListener { dialog.show() }
        addHostButton!!.setOnClickListener { dialogHosts.show() }
        addingPortButton.setOnClickListener {
                if (inputExternal.text!!.isNotEmpty() && inputInternal.text!!.isNotEmpty()) {
                    val s1 = inputInternal.text.toString()
                    val s2 = inputExternal.text.toString()
                    val parts = dropdown?.selectedItem.toString().split(":")[0]

                    listener.sendMessage("treehouses sshtunnel add port actual $s2 $s1 $parts")
                    addPortButton!!.text = "Adding......"
                    addPortButton!!.isEnabled = false

//                addPortButton!!.text = "Adding port, please wait for a while ............"
//                portList!!.isEnabled = false
//                addPortButton!!.isEnabled = false
                    dialog.dismiss()
//                dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                }

        }

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
                builder.setTitle("Delete Port " + portsName!![position] + " ?")
                builder.setPositiveButton("Confirm") { dialog, _ ->
                    var myPos:Int = 0
                    for(pos in hostsPosition!!.indices){
                        if(hostsPosition!![pos] >= position){
                            myPos = pos
                            break
                        }

                    }
                    listener.sendMessage("treehouses sshtunnel remove port " + portsName!![position].split(":".toRegex(), 2).toTypedArray()[0] + " " + hostsName!![myPos].split(":")[0])
                    addPortButton!!.text = "deleting port ....."
                    portList!!.isEnabled = false
                    addPortButton!!.isEnabled = false
                    dialog.dismiss()
                }
            }
//            builder.setMessage("Would you like to delete?");

            // add the buttons

            builder.setNegativeButton("Cancel", null)

            // create and show the alert dialog
            val dialog = builder.create()
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }

        addingHostButton.setOnClickListener {
            if (inputExternalHost.text.toString().isNotEmpty() && inputInternalHost.text.toString().isNotEmpty() ) {
                val s1 = inputInternalHost.text.toString()
                val s2 = inputExternalHost.text.toString()


                listener.sendMessage("treehouses sshtunnel add host $s1 $s2")
                addHostButton!!.text = "Adding......"
                addHostButton!!.isEnabled = false

//                addPortButton!!.text = "Adding port, please wait for a while ............"
//                portList!!.isEnabled = false
//                addPortButton!!.isEnabled = false
                dialogHosts.dismiss()
//                dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            }
        }

        return bind!!.root
    }

    override fun setUserVisibleHint(visible: Boolean) {
        if(visible) {
            mChatService = listener.getChatService()
            mChatService!!.updateHandler(mHandler)



            listener.sendMessage("treehouses sshtunnel ports")
            var sshPorts = bind!!.sshPorts
            portsName = ArrayList()

            adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, portsName!!)
            Log.i("Tag", "Reload fragment")
        }
    }
    override fun getMessage(msg: Message) {
            if (msg.what == Constants.MESSAGE_READ) {
                var Position:Int = 0
                val readMessage: String = msg.obj as String
                Log.d("SSHTunnel reply", "" + readMessage)
                if(readMessage.contains("ssh-rsa") || readMessage.contains("Added") || readMessage.contains("Removed")){
                    Toast.makeText(requireContext(), "Added/Removed. Retrieving port list.", Toast.LENGTH_SHORT).show()
                    addPortButton?.text = "Retrieving"
                    addHostButton?.text = "Retrieving"
                    portsName = ArrayList()
                    hostsName = ArrayList()
                    hostsPosition = ArrayList()
                    listener.sendMessage("treehouses sshtunnel ports")
                }
                else if (readMessage.contains("ole@")) {
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
                            if (port.contains("ole@")) {
                                hostsPosition!!.add(Position)
                                hostsName!!.add(port)
                            }
                            Position += 1
                        }
                        val adapter: ArrayAdapter<String> = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, hostsName!!)
                        dropdown?.adapter = adapter
                    }
                    listener.sendMessage("treehouses sshtunnel notice")
                    adapter = ArrayAdapter(requireContext(), R.layout.select_dialog_item, portsName!!)
                    bind!!.sshPorts.adapter = adapter
                }
                else if(readMessage.contains("Status: on")){
                    bind!!.switchNotification.isChecked = true;
                    bind!!.switchNotification.isEnabled = true;
                    bind!!.notifyNow.isEnabled = true
                }
                else if(readMessage.contains("Status: off")){
                    bind!!.switchNotification.isChecked = false;
                    bind!!.switchNotification.isEnabled = true;
                    bind!!.notifyNow.isEnabled = true

                }
                else if (readMessage.contains("OK.")) {
                    listener.sendMessage("treehouses sshtunnel notice")
                }
                else if( readMessage.contains("Thanks for the feedback!")){
                    Toast.makeText(requireContext(), "Notified Gitter. Thank you!", Toast.LENGTH_SHORT).show()
                    bind!!.notifyNow.isEnabled = true
                }
                else if(readMessage.contains("Error: only 'list'")){
                    listener.sendMessage("treehouses sshtunnel ports")
                    Toast.makeText(requireContext(), "Please swipe slower in the future as you have a slow rpi, getting ports again...", Toast.LENGTH_SHORT).show()
                }
                else if(readMessage.contains("Error")){
                    Toast.makeText(requireContext(), "Please add a host if you have no host", Toast.LENGTH_SHORT).show()
                    addPortButton?.isEnabled = false

                }

            }

    }
}

