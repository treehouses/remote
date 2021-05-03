package io.treehouses.remote.ui.tunnelssh

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.text.Spanned
import android.view.View
import android.widget.*
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.logD
import io.treehouses.remote.utils.match
import org.json.JSONException
import org.json.JSONObject

open class BaseTunnelSSHViewModel(application: Application) : FragmentViewModel(application) {
    private val context = getApplication<MainApplication>().applicationContext
    var notifyNowEnabled: MutableLiveData<Boolean> = MutableLiveData() //notifyNow.isEnabled
    var switchChecked: MutableLiveData<Boolean> = MutableLiveData() //switchNotification.isChecked
    var switchEnabled: MutableLiveData<Boolean> = MutableLiveData() //switchNotification.isEnabled
    var addHostText: MutableLiveData<String> = MutableLiveData() //btnAddHosts.text
    var addHostEnabled: MutableLiveData<Boolean> = MutableLiveData() //btnAddHosts.isEnabled
    var addPortText: MutableLiveData<String> = MutableLiveData()  //btnAddPort.text
    var addPortEnabled: MutableLiveData<Boolean> = MutableLiveData()  //btnAddPort.isEnabled
    var sshPortEnabled: MutableLiveData<Boolean> = MutableLiveData()  //sshPorts.isEnabled
    var dialogKeysPublicText: MutableLiveData<Spanned> = MutableLiveData() //dialogKeys.public_key.text
    var dialogKeysPrivateText: MutableLiveData<Spanned> = MutableLiveData() //dialogKeys.private_key.text
    var portsNameArray: ArrayList<String>? = null
    var portsNameAdapter: MutableLiveData<ArrayList<String>?> = MutableLiveData() // portsName
    var hostsNameArray: ArrayList<String>? = null
    var hostsNameAdapter: MutableLiveData<ArrayList<String>?> = MutableLiveData() // hostsName
    var hostsPositionArray: ArrayList<Int>? = null
    var hostsPositionAdapter: MutableLiveData<ArrayList<Int>?> = MutableLiveData() // hostsPosition
    var progressBar: MutableLiveData<Int> = MutableLiveData() // hostsPosition
    protected var jsonReceiving = false
    protected var jsonSent = false
    protected var jsonString = ""


    fun handleMoreMessages(readMessage: String) {
        when {
            readMessage.contains("Error: only 'list'") -> {
                val message = "Please swipe slower in the future as you have a slow rpi, getting ports again..."
                sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE))
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            readMessage.contains("true") || readMessage.contains("false") -> {
                sendMessage("treehouses remote key send")
                Toast.makeText(context, "Please wait...", Toast.LENGTH_SHORT).show()
            }
            readMessage.contains("Saved") -> Toast.makeText(context, "Keys successfully saved to Pi", Toast.LENGTH_SHORT).show()
            readMessage.contains("unknown") -> jsonSend(false)
            else -> if (jsonSent) handleJson(readMessage)
        }
    }

    fun jsonSend(sent: Boolean) {
        jsonSent = sent
        if (sent) progressBar.value = View.VISIBLE
        else {
            progressBar.value = View.GONE
            jsonReceiving = false
        }
    }

    private fun handleJson(readMessage: String) {
        val s = match(readMessage)
        if (jsonReceiving) {
            jsonString += readMessage
            buildJSON()
            if (s == RESULTS.END_JSON || s == RESULTS.END_HELP) {
                buildJSON()
                jsonSend(false)
            }
        } else if (s == RESULTS.START_JSON) {
            jsonReceiving = true
            jsonString = readMessage.trim()
        }
    }

    private fun buildJSON() {
        try {
            val jsonObject = JSONObject(jsonString)
            val profile = jsonObject.getString("profile")
            val (piPublicKey, piPrivateKey) = getPublicKeys(jsonObject)
            val (storedPublicKey, storedPrivateKey) = getStoredKeys(profile)
            logKeys(piPublicKey, piPrivateKey, storedPublicKey, storedPrivateKey)

            val inPiAndPhone = piPublicKey == storedPublicKey && piPrivateKey == storedPrivateKey
            val inPiOnly = piPublicKey != "No public key found" && piPrivateKey != "No private key found " && storedPublicKey.isNullOrBlank() && storedPrivateKey.isNullOrBlank()
            val inPhoneOnly = piPublicKey == "No public key found" && piPrivateKey == "No private key found " && !storedPublicKey.isNullOrBlank() && !storedPrivateKey.isNullOrBlank()
            val inNeither = piPublicKey == "No public key found" && piPrivateKey == "No private key found " && storedPublicKey.isNullOrBlank() && storedPrivateKey.isNullOrBlank()
            // Pi and phone keys are the same
            if (inPiAndPhone) Toast.makeText(context, "The same keys for $profile are already saved in both Pi and phone", Toast.LENGTH_SHORT).show()
            // Key exists in Pi but not phone
            else if (inPiOnly) handlePhoneKeySave(profile, piPublicKey, piPrivateKey)
            // Key exists in phone but not Pi
            else if (inPhoneOnly) handlePiKeySave(profile, storedPublicKey, storedPrivateKey)
            // Keys don't exist in phone or Pi
            else if (inNeither) Toast.makeText(context, "No keys for $profile exist on either Pi or phone!", Toast.LENGTH_SHORT).show()
            // Keys are different, overwrite one or cancel
            else handleDifferentKeys(jsonObject)
        } catch (e: JSONException) { e.printStackTrace() }
    }

    private fun handleDifferentKeys(jsonObject: JSONObject) {
        val profile = jsonObject.getString("profile")
        val (piPublicKey, piPrivateKey) = getPublicKeys(jsonObject)
        val (storedPublicKey, storedPrivateKey) = getStoredKeys(profile)
        val builder = AlertDialog.Builder(context)
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
        builder.setNegativeButton("Save to Pi") { _: DialogInterface?, _: Int ->
            sendMessage("treehouses remote key receive \"$storedPublicKey\" \"$storedPrivateKey\" $profile")
            Toast.makeText(context, "The Pi's key has been overwritten with the phone's key successfully ", Toast.LENGTH_LONG).show()
        }
        setNeutralButton(builder, "Cancel")
        builder.show()
    }

    private fun getPublicKeys(jsonObject: JSONObject): Pair<String, String> {
        val piPublicKey = jsonObject.getString("public_key")
        val piPrivateKey = jsonObject.getString("private_key")
        return Pair(piPublicKey, piPrivateKey)
    }

    private fun getStoredKeys(profile: String): Pair<String?, String?> {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("SSHKeyPref", Context.MODE_PRIVATE)
        val storedPublicKey: String? = sharedPreferences.getString("${profile}_public_key", "")
        val storedPrivateKey: String? = sharedPreferences.getString("${profile}_private_key", "")
        return Pair(storedPublicKey, storedPrivateKey)
    }

    private fun logKeys(piPublicKey: String, piPrivateKey: String, storedPublicKey: String?, storedPrivateKey: String?) {
        logD(piPublicKey); logD(piPrivateKey); if (storedPublicKey != null) logD(storedPublicKey)
        if (storedPrivateKey != null) logD(storedPrivateKey)
    }

    private fun handlePiKeySave(profile: String, storedPublicKey: String?, storedPrivateKey: String?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Save Key To Pi")
        builder.setMessage(
                "Phone Public Key for ${profile}: \n$storedPublicKey\n\n" +
                        "Phone Private Key for ${profile}: \n$storedPrivateKey")
        builder.setPositiveButton("Save to Pi") { _: DialogInterface?, _: Int ->
            sendMessage("treehouses remote key receive \"${storedPublicKey}\" \"${storedPrivateKey}\" $profile")
            Toast.makeText(context, "Key saved to Pi successfully", Toast.LENGTH_LONG).show()
        }.setNegativeButton("Cancel") { dialog: DialogInterface?, _: Int -> dialog?.dismiss() }
        builder.show()
    }

    private fun handlePhoneKeySave(profile: String, piPublicKey: String, piPrivateKey: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Save Key To Phone")
        builder.setMessage("Pi Public Key for ${profile}: \n$piPublicKey\n" +
                "Pi Private Key for ${profile}: \n$piPrivateKey")

        saveKeyToPhone(builder, profile, piPublicKey, piPrivateKey)
        setNeutralButton(builder, "Cancel")
        builder.show()
    }

    private fun saveKeyToPhone(builder: AlertDialog.Builder, profile: String, piPublicKey: String, piPrivateKey: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("SSHKeyPref", Context.MODE_PRIVATE)
        val myEdit = sharedPreferences.edit()
        builder.setPositiveButton("Save to Phone") { _: DialogInterface?, _: Int ->
            myEdit.putString("${profile}_public_key", piPublicKey)
            myEdit.putString("${profile}_private_key", piPrivateKey)
            myEdit.apply()
            Toast.makeText(context, "Key saved to phone successfully", Toast.LENGTH_LONG).show()
        }
    }

    private fun setNeutralButton(builder: AlertDialog.Builder, text: String) {
        builder.setNeutralButton(text) { dialog: DialogInterface?, _: Int -> dialog?.dismiss() }
    }



}
