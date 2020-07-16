package io.treehouses.remote.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.match
import org.json.JSONException
import org.json.JSONObject

class ViewHolderSSHTunnelKey internal constructor(v: View, private val c: Context, listener: HomeInteractListener) {
    private val mChatService: BluetoothChatService = listener.getChatService()

    private var jsonReceiving = false
    private var jsonSent = false
    private var jsonString = ""

    val keys: TextView
    val progressBar: ProgressBar

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_READ -> {
                    val readMessage = msg.obj as String
                    match(readMessage)

                    if(readMessage.contains("true") || readMessage.contains("false")){
                        listener.sendMessage("treehouses remote key send")
                        Toast.makeText(c, "Please wait...", Toast.LENGTH_SHORT).show()
                    }
                    if(readMessage.contains("Saved")){
                        Toast.makeText(c, "Keys successfully saved to Pi", Toast.LENGTH_SHORT).show()
                    }
                    if (readMessage.contains("unknown")) {
                        jsonSend(false)
                    }
                    else{
                        if (jsonSent) {
                            handleJson(readMessage)
                        }
                    }
                }
            }
        }
    }

    init {
        mChatService.updateHandler(mHandler)
        val btnGetKeys = v.findViewById<Button>(R.id.btn_save_keys)
        val btnShowKeys = v.findViewById<Button>(R.id.btn_show_keys)

        keys = v.findViewById(R.id.keys)
        progressBar = v.findViewById(R.id.progress_bar)
        btnGetKeys.setOnClickListener {
            listener.sendMessage("treehouses remote key send")
            jsonSend(true)
        }

        btnShowKeys.setOnClickListener {
            val sharedPreferences: SharedPreferences = c.getSharedPreferences("SSHKeyPref", Context.MODE_PRIVATE)
            val storedPublicKey: String? = sharedPreferences.getString("public_key", "")
            val storedPrivateKey: String? = sharedPreferences.getString("private_key", "")

            keys.text = "Public key: " + storedPublicKey + "\nPrivate key: " + storedPrivateKey
        }
    }

    val dialogListener = listener

    private fun buildJSON() {
        try {
            val jsonObject = JSONObject(jsonString)

            val sharedPreferences: SharedPreferences = c.getSharedPreferences("SSHKeyPref", Context.MODE_PRIVATE)
            val myEdit = sharedPreferences.edit()

            val storedPublicKey: String? = sharedPreferences.getString("public_key", "")
            val storedPrivateKey: String? = sharedPreferences.getString("private_key", "")

            val piPublicKey = jsonObject.getString("public_key")
            val piPrivateKey = jsonObject.getString("private_key")

            Log.d("storedPublicKey", storedPublicKey)
            Log.d("storedPrivateKey", storedPrivateKey)
            Log.d("pipPublicKey", piPublicKey)
            Log.d("piPrivateKey", piPrivateKey)

            // Pi and phone keys are the same
            if(piPublicKey == storedPublicKey && piPrivateKey == storedPrivateKey){
                Toast.makeText(c, "The same keys are already saved in both Pi and phone", Toast.LENGTH_SHORT).show()
            }
            // Key exists in Pi but not phone
            else if(!piPublicKey.isNullOrBlank() && !piPrivateKey.isNullOrBlank() && storedPublicKey.isNullOrBlank() && storedPrivateKey.isNullOrBlank()){
                val builder = AlertDialog.Builder(c)
                builder.setTitle("Save Key To Phone")
                builder.setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    myEdit.putString("public_key", jsonObject.getString("public_key"))
                    myEdit.putString("private_key", jsonObject.getString("private_key"))
                    myEdit.apply()
                    Toast.makeText(c, "Key saved to phone successfully", Toast.LENGTH_SHORT).show()
                }.setNegativeButton("No") { dialog: DialogInterface?, _: Int ->
                    dialog?.dismiss()
                }
                builder.show()
            }
            // Key exists in phone but not Pi
            else if(piPublicKey.isNullOrBlank() && piPrivateKey.isNullOrBlank() && !storedPublicKey.isNullOrBlank() && !storedPrivateKey.isNullOrBlank()){
                val builder = AlertDialog.Builder(c)
                builder.setTitle("Save Key To Pi")
                builder.setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    dialogListener.sendMessage("treehouses remote key receive $storedPublicKey $storedPrivateKey")
                    Toast.makeText(c, "Key saved to Pi successfully", Toast.LENGTH_SHORT).show()
                }.setNegativeButton("No") { dialog: DialogInterface?, _: Int ->
                    dialog?.dismiss()
                }
                builder.show()
            }
            // Keys don't exist in phone or Pi
            else if(piPublicKey.isNullOrBlank() && piPrivateKey.isNullOrBlank() && storedPublicKey.isNullOrBlank() && storedPrivateKey.isNullOrBlank()){
                Toast.makeText(c, "No keys exist on either Pi or phone!", Toast.LENGTH_SHORT).show()
            }
            // Keys are different, overwrite one or cancel
            else{
                val builder = AlertDialog.Builder(c)
                builder.setTitle("Overwrite On Pi or Phone")
                builder.setMessage("There are different keys on the Pi and the phone. Would you like to overwrite the Pi's key or the phone's key?")
                builder.setNegativeButton("Phone") { _: DialogInterface?, _: Int ->
                    myEdit.putString("public_key", piPublicKey)
                    myEdit.putString("private_key", piPrivateKey)
                    myEdit.apply()
                    Toast.makeText(c, "The phone's key has been overwritten with Pi's key successfully ", Toast.LENGTH_SHORT).show()
                }.setPositiveButton("Pi") { _: DialogInterface?, _: Int ->
                    dialogListener.sendMessage("treehouses remote key receive $storedPublicKey $storedPrivateKey")
                    Toast.makeText(c, "The Pi's key has been overwritten with the phone's key successfully ", Toast.LENGTH_SHORT).show()
                }.setNeutralButton("Cancel"){ dialog: DialogInterface?, _: Int ->
                    dialog?.dismiss()
                }
                builder.show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun handleJson(readMessage: String) {
        val s = match(readMessage)
        if (jsonReceiving) {
            jsonString += readMessage
            if (s == RESULTS.END_JSON || s == RESULTS.END_HELP) {
                buildJSON()
                jsonSend(false)
            }
        } else if (s == RESULTS.START_JSON) {
            jsonReceiving = true
            jsonString = readMessage.trim()
        }
    }
    private fun jsonSend(sent: Boolean) {
        jsonSent = sent
        if (sent) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
            jsonReceiving = false
        }
    }
}
