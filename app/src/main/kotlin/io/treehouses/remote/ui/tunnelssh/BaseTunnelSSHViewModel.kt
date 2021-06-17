package io.treehouses.remote.ui.tunnelssh

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.text.Html
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.pojo.TunnelSSHData
import io.treehouses.remote.pojo.TunnelSSHKeyDialogData
import io.treehouses.remote.pojo.enum.Resource
import io.treehouses.remote.utils.logD
import org.json.JSONException
import org.json.JSONObject

open class BaseTunnelSSHViewModel(application: Application) : FragmentViewModel(application) {
    private val context = getApplication<MainApplication>().applicationContext

    // hostsPosition
    var tunnelSSHData: MutableLiveData<Resource<TunnelSSHData>> = MutableLiveData()
    var tunnelSSHKeyDialogData: MutableLiveData<Resource<TunnelSSHKeyDialogData>> = MutableLiveData()
    var show: MutableLiveData<Resource<TunnelSSHData>> = MutableLiveData()
    var tunnelSSHObject: TunnelSSHData = TunnelSSHData() // hostsPosition
    var tunnelSSHKeyDialogObj: TunnelSSHKeyDialogData = TunnelSSHKeyDialogData() // hostsPosition
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
            logD(jsonString)
            val profile = jsonObject.getString("profile")
            val (piPublicKey, piPrivateKey) = getPublicKeys(jsonObject)
            val (storedPublicKey, storedPrivateKey) = getStoredKeys(profile)
            logKeys(piPublicKey, piPrivateKey, storedPublicKey, storedPrivateKey)

            val inPiAndPhone = piPublicKey == storedPublicKey && piPrivateKey == storedPrivateKey
            val inPiOnly = piPublicKey != "No public key found" && piPrivateKey != "No private key found " && storedPublicKey.isNullOrBlank() && storedPrivateKey.isNullOrBlank()
            val inPhoneOnly = piPublicKey == "No public key found" && piPrivateKey == "No private key found " && !storedPublicKey.isNullOrBlank() && !storedPrivateKey.isNullOrBlank()
            val inNeither = piPublicKey == "No public key found" && piPrivateKey == "No private key found " && storedPublicKey.isNullOrBlank() && storedPrivateKey.isNullOrBlank()
            // Pi and phone keys are the same
            tunnelSSHKeyDialogObj = TunnelSSHKeyDialogData()
            tunnelSSHKeyDialogObj.profile = profile
            tunnelSSHKeyDialogObj.storedPrivateKey = storedPrivateKey!!
            tunnelSSHKeyDialogObj.storedPublicKey = storedPublicKey!!
            tunnelSSHKeyDialogObj.piPrivateKey = piPrivateKey!!
            tunnelSSHKeyDialogObj.piPublicKey = piPublicKey!!
            if (inPiAndPhone) Toast.makeText(context, "The same keys for $profile are already saved in both Pi and phone", Toast.LENGTH_SHORT).show()
            // Key exists in Pi but not phone
//            else if (inPiOnly) handlePhoneKeySave(profile, piPublicKey, piPrivateKey)

            else if (inPiOnly) {
                tunnelSSHKeyDialogObj.showHandlePhoneKeySaveDialog = true
                tunnelSSHKeyDialogData.value = Resource.success(tunnelSSHKeyDialogObj)
                //  handlePhoneKeySave(profile, piPublicKey, piPrivateKey)
            }
            // Key exists in phone but not Pi
//            else if (inPhoneOnly) handlePiKeySave(profile, storedPublicKey, storedPrivateKey)
            else if (inPhoneOnly) {
                tunnelSSHKeyDialogObj.showHandlePiKeySaveDialog = true
                tunnelSSHKeyDialogData.value = Resource.success(tunnelSSHKeyDialogObj)
            }
            // Keys don't exist in phone or Pi
            else if (inNeither) Toast.makeText(context, "No keys for $profile exist on either Pi or phone!", Toast.LENGTH_SHORT).show()
            // Keys are different, overwrite one or cancel
            else {
                tunnelSSHKeyDialogObj.showHandleDifferentKeysDialog = true
                tunnelSSHKeyDialogData.value = Resource.success(tunnelSSHKeyDialogObj)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
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


    fun saveKeyToPhone(profile: String, piPublicKey: String, piPrivateKey: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("SSHKeyPref", Context.MODE_PRIVATE)
        val myEdit = sharedPreferences.edit()
        myEdit.putString("${profile}_public_key", piPublicKey)
        myEdit.putString("${profile}_private_key", piPrivateKey)
        myEdit.apply()
        tunnelSSHObject.publicKey = Html.fromHtml("<b>Phone Public Key for ${profile}:</b> <br>$piPublicKey\n")
        tunnelSSHObject.privateKey = Html.fromHtml("<b>Phone Public Key for ${profile}:</b> <br>$piPrivateKey\n")
        tunnelSSHData.value = Resource.success(tunnelSSHObject)
        Toast.makeText(context, "Key saved to phone successfully", Toast.LENGTH_LONG).show()
    }

    fun setUserVisibleHint() {
        loadBT()
        initializeArrays()
        sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE))
    }

    fun searchArray(array: java.util.ArrayList<String>?, portnum: String): Boolean {
        for (name in array!!) {
            var check = name.substringAfter(":")
            if (check == portnum) return true
        }
        return false
    }

    fun initializeArrays() {
        tunnelSSHObject.portNames = ArrayList()
        tunnelSSHObject.hostNames = ArrayList()
        tunnelSSHObject.hostPosition = ArrayList()
    }
}
