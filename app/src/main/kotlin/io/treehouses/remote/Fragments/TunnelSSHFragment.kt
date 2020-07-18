package io.treehouses.remote.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityTunnelSshFragmentBinding
import kotlin.math.log

class TunnelSSHFragment : BaseFragment() {

    var bind: ActivityTunnelSshFragmentBinding? = null
    private var adapter: ArrayAdapter<String>? = null
    private var portsName: java.util.ArrayList<String>? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        bind = ActivityTunnelSshFragmentBinding.inflate(inflater, container, false)
        bind!!.switchNotification.isEnabled = false;
        bind!!.switchNotification.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                bind!!.switchNotification.isEnabled = false
                listener.sendMessage("treehouses sshtunnel notice on")
            } else {
                bind!!.switchNotification.isEnabled = false
                listener.sendMessage("treehouses sshtunnel notice off")
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
                        for (port in ports)
                            if(port.length >= 3)
                            portsName!!.add(port)
                    }
                    listener.sendMessage("treehouses sshtunnel notice")
                    adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, portsName!!)
                    bind!!.sshPorts.adapter = adapter
                }
                else if(readMessage.contains("Status: on")){
                    bind!!.switchNotification.isChecked = true;
                    bind!!.switchNotification.isEnabled = true;
                }
                else if(readMessage.contains("Status: off")){
                    bind!!.switchNotification.isChecked = false;
                    bind!!.switchNotification.isEnabled = true;

                }
                else if (readMessage.contains("OK.")) {
                    listener.sendMessage("treehouses sshtunnel notice")
                }



            }
        }
    }
}
