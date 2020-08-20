package io.treehouses.remote.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Message
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.callback.HomeInteractListener
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.match
import org.json.JSONException
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.N)
class ViewHolderSSHTunnelKey internal constructor(v: View, private val c: Context, listener: HomeInteractListener) {
    private val mChatService: BluetoothChatService = listener.getChatService()

    private var jsonReceiving = false
    private var jsonSent = false
    private var jsonString = ""

    private val publicKey: TextView
    private val privateKey: TextView
    val progressBar: ProgressBar

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                Constants.MESSAGE_READ -> {
                    val readMessage = msg.obj as String
                    match(readMessage)

                    if(readMessage.contains("true") || readMessage.contains("false")){
                        listener.sendMessage(c.getString(R.string.TREEHOUSES_REMOTE_KEY_SEND))
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

        val profileText = v.findViewById<EditText>(R.id.sshtunnel_profile).text

        val btnGetKeys = v.findViewById<Button>(R.id.btn_save_keys)
        val btnShowKeys = v.findViewById<Button>(R.id.btn_show_keys)

        publicKey = v.findViewById(R.id.public_key)
        privateKey = v.findViewById(R.id.private_key)
        progressBar = v.findViewById(R.id.progress_bar)

        Log.d("profile string", profileText.toString())
        btnGetKeys.setOnClickListener {
            var profile = profileText.toString()
            listener.sendMessage(c.getString(R.string.TREEHOUSES_REMOTE_KEY_SEND, profile))
            jsonSend(true)
        }

        btnShowKeys.setOnClickListener {
            var profile = profileText.toString()
            if(profile.isBlank())
                profile = "default"

            val sharedPreferences: SharedPreferences = c.getSharedPreferences("SSHKeyPref", Context.MODE_PRIVATE)
            var storedPublicKey: String? = sharedPreferences.getString("${profile}_public_key", "")
            var storedPrivateKey: String? = sharedPreferences.getString("${profile}_private_key", "")

            if (storedPublicKey != null && storedPrivateKey != null) {
                if(storedPublicKey.isBlank()){
                    storedPublicKey = "No public key found"
                }
                if(storedPrivateKey.isBlank()){
                    storedPrivateKey = "No private key found"
                }
            }

            val strPhonePublicKey = Html.fromHtml("<b>Phone Public Key for ${profile}:</b> <br>$storedPublicKey\n", Html.FROM_HTML_MODE_LEGACY)
            val strPhonePrivateKey = Html.fromHtml("<b>Phone Private Key for ${profile}:</b> <br>$storedPrivateKey", Html.FROM_HTML_MODE_LEGACY)
            publicKey.text = strPhonePublicKey
            privateKey.text = strPhonePrivateKey
        }
    }

    private val dialogListener = listener

    private fun getPublicKeys(jsonObject: JSONObject): Pair<String, String> {
        val piPublicKey = jsonObject.getString("public_key")
        val piPrivateKey = jsonObject.getString("private_key")
        return Pair(piPublicKey, piPrivateKey)
    }

    private fun getStoredKeys(profile: String): Pair<String?, String?> {
        val sharedPreferences: SharedPreferences = c.getSharedPreferences("SSHKeyPref", Context.MODE_PRIVATE)
        val storedPublicKey: String? = sharedPreferences.getString("${profile}_public_key", "")
        val storedPrivateKey: String? = sharedPreferences.getString("${profile}_private_key", "")
        return Pair(storedPublicKey, storedPrivateKey)
    }

    private fun buildJSON() {
        try {
            val jsonObject = JSONObject(jsonString)

            val profile = jsonObject.getString("profile")

            val (piPublicKey, piPrivateKey) = getPublicKeys(jsonObject)

            val (storedPublicKey, storedPrivateKey) = getStoredKeys(profile)

            Log.d("profile", profile)
            logKeys(piPublicKey, piPrivateKey, storedPublicKey, storedPrivateKey)

            val inPiAndPhone = piPublicKey == storedPublicKey && piPrivateKey == storedPrivateKey
            val inPiOnly = piPublicKey != "No public key found" && piPrivateKey != "No private key found " && storedPublicKey.isNullOrBlank() && storedPrivateKey.isNullOrBlank()
            val inPhoneOnly = piPublicKey == "No public key found" && piPrivateKey == "No private key found " && !storedPublicKey.isNullOrBlank() && !storedPrivateKey.isNullOrBlank()
            val inNeither = piPublicKey == "No public key found" && piPrivateKey == "No private key found " && storedPublicKey.isNullOrBlank() && storedPrivateKey.isNullOrBlank()

            // Pi and phone keys are the same
            if(inPiAndPhone) Toast.makeText(c, "The same keys for $profile are already saved in both Pi and phone", Toast.LENGTH_SHORT).show()
            // Key exists in Pi but not phone
            else if(inPiOnly) handlePhoneKeySave(profile, piPublicKey, piPrivateKey)
            // Key exists in phone but not Pi
            else if(inPhoneOnly) handlePiKeySave(profile, storedPublicKey, storedPrivateKey)
            // Keys don't exist in phone or Pi
            else if(inNeither) Toast.makeText(c, "No keys for $profile exist on either Pi or phone!", Toast.LENGTH_SHORT).show()
            // Keys are different, overwrite one or cancel
            else handleDifferentKeys(jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun handleDifferentKeys(jsonObject: JSONObject) {
        val profile = jsonObject.getString("profile")
        val (piPublicKey, piPrivateKey) = getPublicKeys(jsonObject)
        val (storedPublicKey, storedPrivateKey) = getStoredKeys(profile)

        var builder = AlertDialog.Builder(c)
        builder.setTitle("Overwrite On Pi or Phone")

        val strPiPublicKey = "Pi Public Key for ${profile}: \n$piPublicKey"
        val strPiPrivateKey = "Pi Private Key for ${profile}: \n$piPrivateKey"
        val strPhonePublicKey = "Phone Public Key for ${profile}: \n$storedPublicKey"
        val strPhonePrivateKey = "Phone Private Key for ${profile}: \n$storedPrivateKey"

        val message = ("There are different keys on the Pi and the phone. Would you like to overwrite the Pi's key or the phone's key?\n\n" +
                strPiPublicKey + "\n\n" +
                strPiPrivateKey + "\n\n" +
                strPhonePublicKey + "\n\n" +
                strPhonePrivateKey)

        builder.setMessage(message)

        saveKeyToPhone(builder, profile, piPublicKey, piPrivateKey)

        builder = receiveKey(builder, arrayOf(storedPublicKey, storedPrivateKey, profile, "The Pi's key has been overwritten with the phone's key successfully "), false)

        setNeutralButton(builder, "Cancel")

        builder.show()
    }

    private fun logKeys(piPublicKey: String, piPrivateKey: String, storedPublicKey: String?, storedPrivateKey: String?) {
        Log.d("piPublicKey", piPublicKey)
        Log.d("piPrivateKey", piPrivateKey)
        Log.d("storedPublicKey", storedPublicKey)
        Log.d("storedPrivateKey", storedPrivateKey)
    }

    private fun receiveKey(builder: AlertDialog.Builder, arr:Array<String?>, flag:Boolean):AlertDialog.Builder{
        if(flag){
            builder.setPositiveButton("Save to Pi") { _: DialogInterface?, _: Int ->
                dialogListener.sendMessage(c.getString(R.string.TREEHOUSES_REMOTE_KEY_RECEIVE, arr[0], arr[1], arr[2]))
                Toast.makeText(c, arr[3], Toast.LENGTH_LONG).show()
            }
        } else {
            builder.setNegativeButton("Save to Pi") { _: DialogInterface?, _: Int ->
                dialogListener.sendMessage(c.getString(R.string.TREEHOUSES_REMOTE_KEY_RECEIVE, arr[0], arr[1], arr[2]))
                Toast.makeText(c, arr[3], Toast.LENGTH_LONG).show()
            }
        }
        return builder
    }

    private fun handlePiKeySave(profile: String, storedPublicKey: String?, storedPrivateKey: String?) {
        var builder = AlertDialog.Builder(c)
        builder.setTitle("Save Key To Pi")
        builder.setMessage(
                "Phone Public Key for ${profile}: \n$storedPublicKey\n\n" +
                        "Phone Private Key for ${profile}: \n$storedPrivateKey")
        builder.setNegativeButton("Cancel") { dialog: DialogInterface?, _: Int ->
            dialog?.dismiss()
        }
        builder = receiveKey(builder, arrayOf(storedPublicKey, storedPrivateKey, profile, "Key saved to Pi successfully"), true)
        builder.show()
    }

    private fun handlePhoneKeySave(profile: String, piPublicKey: String, piPrivateKey: String) {
        val builder = AlertDialog.Builder(c)
        builder.setTitle("Save Key To Phone")
        builder.setMessage("Pi Public Key for ${profile}: \n$piPublicKey\n" +
                "Pi Private Key for ${profile}: \n$piPrivateKey")

        saveKeyToPhone(builder, profile, piPublicKey, piPrivateKey)
        setNeutralButton(builder, "Cancel")

        builder.show()
    }

    private fun saveKeyToPhone(builder: AlertDialog.Builder, profile: String, piPublicKey: String, piPrivateKey: String){
        val sharedPreferences: SharedPreferences = c.getSharedPreferences("SSHKeyPref", Context.MODE_PRIVATE)
        val myEdit = sharedPreferences.edit()
        builder.setPositiveButton("Save to Phone") { _: DialogInterface?, _: Int ->
            myEdit.putString("${profile}_public_key", piPublicKey)
            myEdit.putString("${profile}_private_key", piPrivateKey)
            myEdit.apply()
            Toast.makeText(c, "Key saved to phone successfully", Toast.LENGTH_LONG).show()
        }
    }



    private fun setNeutralButton(builder: AlertDialog.Builder, text: String){
        builder.setNeutralButton(text){ dialog: DialogInterface?, _: Int ->
            dialog?.dismiss()
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
