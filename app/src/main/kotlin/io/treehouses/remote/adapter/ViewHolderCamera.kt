package io.treehouses.remote.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import io.treehouses.remote.Constants
import io.treehouses.remote.network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener

class ViewHolderCamera internal constructor(v: View, private val c: Context, listener: HomeInteractListener) {
    private val mChatService: BluetoothChatService = listener.getChatService()
    private val cameraSwitch: Switch
    private val icon: ImageView
    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == Constants.MESSAGE_READ) {
                readCameraReply(msg.obj.toString())
            }
        }
    }

    private fun readCameraReply(readMessage: String) {
        if (readMessage.contains("Camera settings which are currently enabled") || readMessage.contains("have been enabled")) {
            Toast.makeText(c, "Camera is enabled", Toast.LENGTH_LONG).show()
            icon.setImageResource(android.R.drawable.presence_video_online)
            cameraSwitch.isChecked = true
            cameraSwitch.isEnabled = true
        } else if (readMessage.contains("currently disabled") || readMessage.contains("has been disabled")) {
            Toast.makeText(c, "Camera is disabled", Toast.LENGTH_LONG).show()
            icon.setImageResource(android.R.drawable.presence_video_busy)
            cameraSwitch.isEnabled = true
            cameraSwitch.isChecked = false
        }
    }

    init {
        mChatService.updateHandler(mHandler)
        cameraSwitch = v.findViewById(R.id.CameraSwitch)
        icon = v.findViewById(R.id.cameraIcon)
        listener.sendMessage(c.resources.getString(R.string.TREEHOUSES_CAMERA))
        cameraSwitch.isEnabled = false
        cameraSwitch.setOnClickListener {
            if (cameraSwitch.isChecked) listener.sendMessage(c.resources.getString(R.string.TREEHOUSES_CAMERA_ON))
            else listener.sendMessage(c.resources.getString(R.string.TREEHOUSES_CAMERA_OFF))
            cameraSwitch.isEnabled = false
        }
    }
}