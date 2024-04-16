package io.treehouses.remote.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.Button
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.network.BluetoothChatService
import io.treehouses.remote.ui.home.HomeFragment
import io.treehouses.remote.utils.DialogUtils
import io.treehouses.remote.utils.Utils.toast

class ViewHolderShutdownReboot internal constructor(v: View, context: Context?, listener: HomeInteractListener) {
    private var readMessage  = ""
    private val mChatService: BluetoothChatService = listener.getChatService()
    private val shutdownBtn: Button = v.findViewById(R.id.shutdownBtn)
    private val rebootBtn: Button = v.findViewById(R.id.rebootBtn)

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_READ -> {
                    readMessage = msg.obj as String
                }
            }
        }
    }



    init {
        fun action(command:Int, toast:String){
            listener.sendMessage(context!!.getString(command))
            context.toast(toast)
            listener.openCallFragment(HomeFragment())
        }

        mChatService.updateHandler(mHandler)
        val rebootCallback = { action(R.string.TREEHOUSES_REBOOTS_NOW, "Rebooting Device") }
        val shutdownCallback = { action(R.string.TREEHOUSES_SHUTDOWN_NOW, "Shutting Down Device") }
        rebootBtn.setOnClickListener {
            DialogUtils.createAlertDialog(context, "Reboot?", "Are you sure you want to reboot?", rebootCallback)
        }
        shutdownBtn.setOnClickListener {
            DialogUtils.createAlertDialog(context, "Shutdown?", "Are you sure you want to shutdown?", shutdownCallback)
        }
    }


}