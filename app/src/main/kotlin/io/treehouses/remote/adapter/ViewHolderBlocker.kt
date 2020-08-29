package io.treehouses.remote.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.utils.LogUtils

class ViewHolderBlocker internal constructor(v: View, context: Context?, listener: HomeInteractListener) {
    private val radioGroup: RadioGroup = v.findViewById(R.id.radioGroup)
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
                    LogUtils.log("$TAG, readMessage = $readMessage")
                    updateSelection(readMessage)
                }
            }
        }
    }

    init {
        mChatService.updateHandler(mHandler)
        listener.sendMessage(context!!.resources.getString(R.string.TREEHOUSES_BLOCKER_CHECK))

        fun setBlocker(level:String, msg:String) {
            listener.sendMessage(context.resources.getString(R.string.TREEHOUSES_BLOCKER, level))
            context.toast(msg)
        }

        radioGroup.setOnCheckedChangeListener { _: RadioGroup?, i: Int ->
            when (i) {
                R.id.radioButton1 -> setBlocker("0","Blocker Disabled")
                R.id.radioButton2 -> setBlocker("1","Blocker set to level 1")
                R.id.radioButton3 -> setBlocker("2","Blocker set to level 2")
                R.id.radioButton4 -> setBlocker("3","Blocker set to level 3")
                R.id.radioButton5 -> setBlocker("4","Blocker set to level 4")
                R.id.radioButton6 -> setBlocker("max","Blocker set to maximum level")
            }
        }
    }

    fun updateSelection(readMessage:String){
        when {
            readMessage.contains("blocker 0") -> radioGroup.check(R.id.radioButton1)
            readMessage.contains("blocker 1") -> radioGroup.check(R.id.radioButton2)
            readMessage.contains("blocker 2") -> radioGroup.check(R.id.radioButton3)
            readMessage.contains("blocker 3") -> radioGroup.check(R.id.radioButton4)
            readMessage.contains("blocker 4") -> radioGroup.check(R.id.radioButton5)
            readMessage.contains("blocker X") -> radioGroup.check(R.id.radioButton6)
        }
    }

    companion object {
        private const val TAG = "ViewHolderBlocker"
    }

    fun Context?.toast(s: String): Toast {
        return Toast.makeText(this, s, Toast.LENGTH_SHORT).apply { show() }
    }


}