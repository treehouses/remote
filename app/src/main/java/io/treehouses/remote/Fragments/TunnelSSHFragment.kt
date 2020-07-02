package io.treehouses.remote.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityTunnelSshFragmentBinding

class TunnelSSHFragment : BaseFragment() {

    var bind: ActivityTunnelSshFragmentBinding? = null
    private var adapter: ArrayAdapter<String>? = null
    private var portsName: java.util.ArrayList<String>? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mChatService = listener.getChatService()
        mChatService!!.updateHandler(mHandler)
        bind = ActivityTunnelSshFragmentBinding.inflate(inflater, container, false)
        Thread.sleep(1000)
        listener.sendMessage("treehouses sshtunnel ports")
        var sshPorts = bind!!.sshPorts
        portsName = ArrayList()
        adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, portsName!!)
        return bind!!.root
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
                    adapter = ArrayAdapter(requireContext(), android.R.layout.select_dialog_item, portsName!!)
                    bind!!.sshPorts.adapter = adapter
                }


            }
        }
    }
}
