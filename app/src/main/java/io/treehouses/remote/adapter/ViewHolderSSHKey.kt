package io.treehouses.remote.adapter

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.utils.Utils.toast

class ViewHolderSSHKey internal constructor(v: View, private val c: Context, listener: HomeInteractListener) {
    private val mChatService: BluetoothChatService = listener.getChatService()
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == Constants.MESSAGE_READ) {
                val readMessage = msg.obj as String
                if (readMessage.contains("Added to 'pi' and 'root' user's authorized_keys")) {
                    c.toast("Added to 'pi' and 'root' user's authorized_keys", Toast.LENGTH_LONG)
                }
            }
        }
    }

    companion object {
        private lateinit var editTextSSHKey: TextInputEditText
    }

    init {
        mChatService.updateHandler(mHandler)
        val btnStartConfig = v.findViewById<Button>(R.id.btn_save_key)
        editTextSSHKey = v.findViewById(R.id.editTextSSHKey)
        btnStartConfig.setOnClickListener {
            if (editTextSSHKey.text.toString() != "") {
                Log.d("1111111", editTextSSHKey.text.toString())
                listener.sendMessage(c.resources.getString(R.string.TREEHOUSES_SSHKEY_ADD) + " \"" + editTextSSHKey.text.toString() + "\"")
            } else {
                Toast.makeText(c, "Incorrect SSH Key Input", Toast.LENGTH_LONG).show()
            }
        }
    }
}