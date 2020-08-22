package io.treehouses.remote.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.callback.HomeInteractListener

class ViewHolderShutdownReboot internal constructor(v: View, context: Context?, listener: HomeInteractListener) {
    private var readMessage  = ""
    private val mChatService: BluetoothChatService = listener.getChatService()

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
    }


}