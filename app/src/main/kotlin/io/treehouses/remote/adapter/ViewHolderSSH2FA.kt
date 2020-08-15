package io.treehouses.remote.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.utils.Utils
import io.treehouses.remote.utils.Utils.toast


class ViewHolderSSH2FA internal constructor(v: View, private val c: Context, listener: HomeInteractListener) {
    val v = v
    private val mChatService: BluetoothChatService = listener.getChatService()
    private var readMessage  = ""

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_READ -> {
                    readMessage = msg.obj as String
                    //Log.d(ViewHolderBlocker.TAG, "readMessage = $readMessage")
                    showResponse(readMessage)
                }
            }
        }
    }

    private val addUser: Button = v.findViewById(R.id.addBtn)
    private val removeUser: Button = v.findViewById(R.id.removeBtn)
    private val user:EditText = v.findViewById(R.id.user)
    private val twoFASwitch: Switch = v.findViewById(R.id.switch2FA)

    init {
        mChatService.updateHandler(mHandler)

        fun sendCommand1(id:Int){
            listener.sendMessage(c.resources.getString(id))
        }

        sendCommand1(R.string.TREEHOUSES_SSH_2FA)

        fun sendCommand2(id:Int){
            listener.sendMessage(c.resources.getString(id, user.text))
        }

        addUser.setOnClickListener {
            sendCommand2(R.string.TREEHOUSES_SSH_2FA_ADD)
        }
        removeUser.setOnClickListener {
            sendCommand2(R.string.TREEHOUSES_SSH_2FA_REMOVE)
        }
        twoFASwitch.isEnabled = false
        twoFASwitch.setOnClickListener {
            if (twoFASwitch.isChecked) {
                sendCommand1(R.string.TREEHOUSES_SSH_2FA_ENABLE)
                twoFASwitch.isEnabled = false
            } else {
                sendCommand1(R.string.TREEHOUSES_SSH_2FA_DISABLE)
                twoFASwitch.isEnabled = false
            }
        }
    }

    fun showResponse(readMessage:String){
        when {

            readMessage.contains("Authentication enabled") -> {
                c.toast(readMessage)
                twoFASwitch.isEnabled = true
            }
            readMessage.contains("Authentication disabled") -> {
                c.toast(readMessage)
                twoFASwitch.isEnabled = true
            }
            readMessage.contains("on") -> {
                twoFASwitch.isChecked = true
                twoFASwitch.isEnabled = true
            }
            readMessage.contains("off") -> {
                twoFASwitch.isChecked = false
                twoFASwitch.isEnabled = true
            }
            readMessage.contains("No") -> c.toast(readMessage)
            readMessage.contains("already exists") -> c.toast(readMessage)
            readMessage.contains("Your new secret key is:") -> openAuthenticator(readMessage.substringAfter(":"))
        }
    }

    private fun openAuthenticator(key:String){
        val uri:String = "otpauth://totp/" + "pi@treehouses" + "?secret=" + key + "&issuer=treehouses"
        val intent:Intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

        if (Utils.checkAppIsInstalled(c, v, intent, arrayOf("No Authenticator Client Installed on your Device", "Install", "https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2"))) return

        c.startActivity(intent)
    }
    
}

