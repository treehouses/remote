package io.treehouses.remote.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.ui.home.HomeFragment
import io.treehouses.remote.utils.Utils.toast

class ViewHolderShutdownReboot internal constructor(v: View, context: Context?, listener: HomeInteractListener) {
    private var readMessage  = ""
    private val mChatService: BluetoothChatService = listener.getChatService()
    private val shutdownBtn: Button = v.findViewById(R.id.shutdownBtn)
    private val rebootBtn: Button = v.findViewById(R.id.rebootBtn)

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_READ -> {
                    readMessage = msg.obj as String
                }
            }
        }
    }

    init {
        mChatService.updateHandler(mHandler)
        rebootBtn.setOnClickListener { listener.sendMessage(context!!.getString(R.string.TREEHOUSES_REBOOTS_NOW))
            context.toast("Rebooting Device")
            listener.openCallFragment(HomeFragment()) }
        shutdownBtn.setOnClickListener { listener.sendMessage(context!!.getString(R.string.TREEHOUSES_SHUTDOWN_NOW))
            context.toast("Shutting Down Device")
            listener.openCallFragment(HomeFragment())}
    }


}