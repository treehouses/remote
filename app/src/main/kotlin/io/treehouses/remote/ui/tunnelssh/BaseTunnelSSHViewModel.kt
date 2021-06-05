package io.treehouses.remote.ui.tunnelssh

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.pojo.TunnelSSHData
import io.treehouses.remote.pojo.enum.Resource
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.logD
import io.treehouses.remote.utils.match
import org.json.JSONException
import org.json.JSONObject

open class BaseTunnelSSHViewModel(application: Application) : FragmentViewModel(application) {
    private val context = getApplication<MainApplication>().applicationContext

    // hostsPosition
    var tunnelSSHData: MutableLiveData<Resource<TunnelSSHData>> = MutableLiveData()
    var tunnelSSHObject: TunnelSSHData = TunnelSSHData() // hostsPosition
    protected var jsonReceiving = false
    protected var jsonSent = false
    protected var jsonString = ""

    fun jsonSend(sent: Boolean) {
        jsonSent = sent
        if (sent) tunnelSSHData.value = Resource.loading()
        else {
            tunnelSSHData.value = Resource.loading(tunnelSSHObject)
            jsonReceiving = false
        }
    }


   public fun buildJSON() {
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
        } catch (e: JSONException) {
            e.printStackTrace()
        }
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
