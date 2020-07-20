package io.treehouses.remote.Fragments

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
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
import kotlinx.android.synthetic.main.dialog_container.view.*
import kotlinx.android.synthetic.main.dialog_rename.*
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

        dropdown = dialog.findViewById(R.id.hosts)
        addPortButton = bind!!.btnAddPort
        addHostButton = bind!!.btnAddHosts
        val items = arrayOf("1", "2", "three")
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
        addPortButton!!.setOnClickListener { dialog.show() }
        addHostButton!!.setOnClickListener { dialogHosts.show() }
        addingPortButton.setOnClickListener {
            if (inputExternal.text.toString() !== "" && inputInternal.text.toString() !== "") {
                val s1 = inputInternal.text.toString()
                val s2 = inputExternal.text.toString()
                val parts = dropdown?.selectedItem.toString().split(":")
                Log.d("dasdas", parts[0])
//                listener.sendMessage("treehouses tor add $s2 $s1")
//                addPortButton!!.text = "Adding port, please wait for a while ............"
//                portList!!.isEnabled = false
//                addPortButton!!.isEnabled = false
//                dialog.dismiss()
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
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == Constants.MESSAGE_READ) {
                val readMessage: String = msg.obj as String
                Log.d("SSHTunnel reply", "" + readMessage)
                if (readMessage.contains("ole@")) {
                    val hosts = readMessage.split('\n')
                    for (host in hosts) {
                        val ports = host.split(' ')
                        for (port in ports) {
                            if (port.length >= 3)
                                portsName!!.add(port)
                            if (port.contains("ole@"))
                                hostsName!!.add(port)
                        }
                        val adapter: ArrayAdapter<String> = ArrayAdapter(context!!, R.layout.support_simple_spinner_dropdown_item, hostsName!!)
                        dropdown?.adapter = adapter
                    }
                    listener.sendMessage("treehouses sshtunnel notice")
                    adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, portsName!!)
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



            }
        }
    }
}
