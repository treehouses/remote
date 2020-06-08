package io.treehouses.remote.adapter

import android.content.Context
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Switch
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener

class ViewHolderCamera internal constructor(v: View, private val c: Context, listener: HomeInteractListener) {
    private val mChatService: BluetoothChatService
    private val cameraSwitch: Switch
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == Constants.MESSAGE_READ) {
                readCameraReply(msg.obj.toString())
            }
        }
    }

    private fun readCameraReply(readMessage: String) {
        if (readMessage.contains("Camera settings which are currently enabled") || readMessage.contains("have been enabled")) {
            Toast.makeText(c, "Camera is enabled", Toast.LENGTH_LONG).show()
            cameraSwitch.isChecked = true
            cameraSwitch.isEnabled = true
        } else if (readMessage.contains("currently disabled") || readMessage.contains("has been disabled")) {
            Toast.makeText(c, "Camera is disabled", Toast.LENGTH_LONG).show()
            cameraSwitch.isEnabled = true
            cameraSwitch.isChecked = false
        }
    }

    companion object {
        private val editTextSSHKey: TextInputEditText? = null
    }

    init {
        mChatService = listener.chatService
        mChatService.updateHandler(mHandler)
        cameraSwitch = v.findViewById(R.id.CameraSwitch)
        listener.sendMessage("treehouses camera")
        cameraSwitch.isEnabled = false
        cameraSwitch.setOnClickListener { v2: View? ->
            if (cameraSwitch.isChecked) {
                listener.sendMessage("treehouses camera on")
                cameraSwitch.isEnabled = false
            } else {
                listener.sendMessage("treehouses camera off")
                cameraSwitch.isEnabled = false
            }
        }
    }
}