package io.treehouses.remote.adapter

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.snackbar.Snackbar
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
    private val enable: Button = v.findViewById(R.id.enableBtn)
    private val disable: Button = v.findViewById(R.id.disableBtn)
    private val user:EditText = v.findViewById(R.id.user)

    init {
        mChatService.updateHandler(mHandler)

        addUser.setOnClickListener {
            listener.sendMessage("treehouses ssh 2fa add " + user.text)
        }
        removeUser.setOnClickListener {
            listener.sendMessage("treehouses ssh 2fa remove " + user.text)
        }
        enable.setOnClickListener {
            listener.sendMessage("treehouses ssh 2fa enable")
        }
        disable.setOnClickListener {
            listener.sendMessage("treehouses ssh 2fa disable")
        }
    }

    fun showResponse(readMessage:String){
        when {
            readMessage.contains("Authentication enabled") -> c.toast(readMessage)
            readMessage.contains("Authentication disabled") -> c.toast(readMessage)
            readMessage.contains("No") -> c.toast(readMessage)
            readMessage.contains("Your new secret key is:") -> openAuthenticator(readMessage.substringAfter(":"))
        }
    }

    private fun openAuthenticator(key:String){
        val uri:String = "otpauth://totp/" + "pi@treehouses" + "?secret=" + key + "&issuer=treehouses"
        val intent:Intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))

        if (Utils.checkAppIsInstalled(c, v, intent, "No Authenticator Client Installed on your Device", "Install", "https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2")) return

        c.startActivity(intent)
    }
    
}

