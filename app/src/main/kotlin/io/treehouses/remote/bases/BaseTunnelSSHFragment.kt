package io.treehouses.remote.bases

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.view.View
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.R
import io.treehouses.remote.adapter.TunnelPortAdapter
import io.treehouses.remote.databinding.ActivityTunnelSshFragmentBinding
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.Utils
import io.treehouses.remote.utils.logD
import io.treehouses.remote.utils.match
import kotlinx.android.synthetic.main.activity_tunnel_ssh_fragment.*
import org.json.JSONException
import org.json.JSONObject

open class BaseTunnelSSHFragment : BaseFragment() {
    protected var addPortButton: Button? = null
    protected var deleteAllPortsButton: Button? = null
    protected var addHostButton: Button? = null
    var bind: ActivityTunnelSshFragmentBinding? = null
    protected var dropdown: Spinner? = null
    protected var portList: ListView? = null
    protected var adapter: TunnelPortAdapter? = null
    protected var portsName: java.util.ArrayList<String>? = null
    protected var hostsName: java.util.ArrayList<String>? = null
    protected var hostsPosition: java.util.ArrayList<Int>? = null
    protected lateinit var dialogHosts: Dialog
    protected lateinit var dialogKeys: Dialog
    protected lateinit var inputExternalHost: TextInputEditText
    protected lateinit var inputInternalHost: TextInputEditText
    protected lateinit var inputExternal: TextInputEditText
    protected lateinit var inputInternal: TextInputEditText
    protected lateinit var dialog: Dialog
    protected lateinit var addingPortButton: Button
    protected lateinit var addingHostButton: Button
    protected lateinit var saveKeys: Button
    protected lateinit var showKeys: Button
    protected lateinit var publicKey: TextView
    protected lateinit var privateKey: TextView
    protected lateinit var progressBar: ProgressBar
    protected var jsonReceiving = false
    protected var jsonSent = false
    protected var jsonString = ""
    protected lateinit var adapter2: ArrayAdapter<String>

    protected fun getOtherMessages(readMessage: String) {
        when {
            readMessage.contains("Status: on") -> {
                bind?.apply {
                    switchNotification.isChecked = true; switchNotification.isEnabled = true
                    notifyNow.isEnabled = true
                }
            }
            readMessage.contains("Status: off") -> {
                bind?.apply {
                    switchNotification.isChecked = false; switchNotification.isEnabled = true
                    notifyNow.isEnabled = true
                    listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))
                }
            }
            readMessage.contains("OK.") -> listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE))
            readMessage.contains("Thanks for the feedback!") -> {
                Toast.makeText(requireContext(), "Notified Gitter. Thank you!", Toast.LENGTH_SHORT).show()
                bind!!.notifyNow.isEnabled = true
            }
            else -> handleMoreMessages(readMessage)
        }
    }

    private fun handleMoreMessages(readMessage: String) {
        when {
            readMessage.contains("Error: only 'list'") -> {
                val messages = Pair(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE), "Please swipe slower in the future as you have a slow rpi, getting ports again...")
                Utils.sendMessage(listener, messages, requireContext(), Toast.LENGTH_SHORT)
            }
            readMessage.contains("true") || readMessage.contains("false") -> {
                listener.sendMessage("treehouses remote key send")
                Toast.makeText(context, "Please wait...", Toast.LENGTH_SHORT).show()
            }
            readMessage.contains("Saved") -> Toast.makeText(context, "Keys successfully saved to Pi", Toast.LENGTH_SHORT).show()
            readMessage.contains("unknown") -> jsonSend(false)
            else -> if (jsonSent) handleJson(readMessage)
        }
    }

    protected fun jsonSend(sent: Boolean) {
        jsonSent = sent
        if (sent) progressBar.visibility = View.VISIBLE
        else {
            progressBar.visibility = View.GONE
            jsonReceiving = false
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

    private fun buildJSON() {
        try {
            val jsonObject = JSONObject(jsonString)
            val profile = jsonObject.getString("profile")
            val (piPublicKey, piPrivateKey) = getPublicKeys(jsonObject)
            val (storedPublicKey, storedPrivateKey) = getStoredKeys(profile)
            logD(profile)
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
            listener.sendMessage("treehouses remote key receive \"$storedPublicKey\" \"$storedPrivateKey\" $profile")
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
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("SSHKeyPref", Context.MODE_PRIVATE)
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
            listener.sendMessage("treehouses remote key receive \"${storedPublicKey}\" \"${storedPrivateKey}\" $profile")
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
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("SSHKeyPref", Context.MODE_PRIVATE)
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

    protected fun handleOnStatus() {
        bind!!.switchNotification.isChecked = true; bind!!.switchNotification.isEnabled = true; bind!!.notifyNow.isEnabled = true
        portsName = ArrayList(); hostsName = ArrayList(); hostsPosition = ArrayList()
        listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))
    }

    protected fun handleNoPorts() {
        adapter2 = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, hostsName!!)
        dropdown?.adapter = adapter2
        adapter = TunnelPortAdapter(requireContext(), portsName!!)
        bind!!.sshPorts.adapter = adapter
        portList!!.isEnabled = true
        addPortButton!!.text = "Add Port"; addHostButton!!.text = "Add Host"
        addPortButton!!.isEnabled = false; addHostButton!!.isEnabled = true
        Toast.makeText(requireContext(), "Add a host", Toast.LENGTH_SHORT).show()
    }

    protected fun handleHostNotFound() {
        addHostButton?.isEnabled = true; portList?.isEnabled = true; addHostButton?.isEnabled = true
        addHostButton?.text = "Add Host"; addPortButton?.text = "Add Port"
        Toast.makeText(requireContext(), "incorrect deleting host/port, try again", Toast.LENGTH_SHORT).show()
    }

    protected fun handleModifiedList() {
        Toast.makeText(requireContext(), "Added/Removed. Retrieving port list.", Toast.LENGTH_SHORT).show()
        addPortButton?.text = "Retrieving"; addHostButton?.text = "Retrieving"
        portsName = ArrayList(); hostsName = ArrayList(); hostsPosition = ArrayList()
        listener.sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))
    }

    protected fun handleNewList(readMessage: String) {
        var position = 0
        addPortButton?.isEnabled = true
        addPortButton?.text = "Add Port"; addHostButton?.text = "Add Host"
        addPortButton!!.isEnabled = true; addHostButton?.isEnabled = true
        deleteAllPortsButton!!.isEnabled = true;
        val hosts = readMessage.split('\n')
        for (host in hosts) {
            val ports = host.split(' ')
            for (port in ports) {
                if (port.length >= 3)
                    portsName!!.add(port)
                if (port.contains("@")) {
                    hostsPosition!!.add(position)
                    hostsName!!.add(port)
                }
                position += 1
            }
        }
        adapter2 = ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, hostsName!!)
        dropdown?.adapter = adapter2
        adapter = TunnelPortAdapter(requireContext(), portsName!!)
          bind!!.sshPorts.adapter = adapter
        portList!!.isEnabled = true
    }

}