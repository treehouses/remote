package io.treehouses.remote.ui.tunnelssh

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.widget.Toast
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.utils.TunnelUtils
import io.treehouses.remote.utils.logD

open class TunnelSSHViewModel(application: Application): BaseTunnelSSHViewModel(application) {
    private val context = getApplication<MainApplication>().applicationContext

    override fun onRead(output: String) {
        super.onRead(output)
        logD("SSHTunnel reply $output")
//        if (lastCommand == getString(R.string.TREEHOUSES_REMOTE_KEY_SEND)) logD("Key send: $output")
        val modifyKeywords = arrayOf("Added", "Removed")
        if (output.contains("Host / port not found")) handleHostNotFound()
        else if (output.trim().contains("no tunnel has been set up")) addPortEnabled.value = false
        else if (output.trim().contains("added")) sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))
        else if (output.trim().contains("Removed") && lastCommand == getString(R.string.TREEHOUSES_SSHTUNNEL_REMOVE_ALL)) {
            portsNameArray!!.clear()
            portsNameAdapter.value = portsNameArray
            notifyNowEnabled.value = false
            sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE));
        } else if ((modifyKeywords.filter { it in output}).isNotEmpty()) handleModifiedList()
        else if (output.contains("@") && lastCommand == getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS)) handleNewList(output);
        else if (output.contains("the command 'treehouses sshtunnel ports' returns nothing")) handleNoPorts()
        else if (output.contains("Status: on")) handleOnStatus()
        else if (output.trim().contains("exists")) {
            addPortText.value = "Add Port"
            Toast.makeText(context, "Port already exists", Toast.LENGTH_SHORT).show()
        }
        else getOtherMessages(output)
    }

    protected fun getOtherMessages(readMessage: String) {
        when {
            readMessage.contains("Status: on") -> {
                switchChecked.value = true; switchEnabled.value = true
                notifyNowEnabled.value = true
            }
            readMessage.contains("Status: off") -> {
                switchChecked.value = false; switchEnabled.value = true
                notifyNowEnabled.value = true
                sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))
            }
            readMessage.contains("OK.") -> sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE))
            readMessage.contains("Thanks for the feedback!") -> {
                Toast.makeText(context, "Notified Gitter. Thank you!", Toast.LENGTH_SHORT).show()
                notifyNowEnabled.value = true
            }
            else -> handleMoreMessages(readMessage)
        }
    }

    fun onCreateView(){
        switchEnabled.value = false
        notifyNowEnabled.value = false
        hostsNameArray = ArrayList()
        hostsNameAdapter.value = hostsNameArray
    }

    fun setUserVisibleHint(){
        loadBT()
        portsNameArray = ArrayList()
        portsNameAdapter.value = portsNameArray
        sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE))
    }

    fun searchArray (array: java.util.ArrayList<String>?, portnum: String): Boolean {
        for(name in array!!){
            var check = name.substringAfter(":")
            if (check.equals(portnum)) return true
        }
        return false
    }

    fun initializeArrays(){
        portsNameArray = ArrayList()
        portsNameAdapter.value = portsNameArray
        hostsNameArray = ArrayList()
        hostsNameAdapter.value = hostsNameArray
        hostsPositionArray = ArrayList()
        hostsPositionAdapter.value = hostsPositionArray
    }

    fun deleteHostPorts(){
        sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_REMOVE_ALL))
    }

    fun switchButton(isChecked: Boolean) {
        switchEnabled.value = false
        if (isChecked) sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE_ON))
        else sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE_OFF))
    }

    fun addingHostButton(m1: String, m2: String) {
        sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_ADD_HOST, m1, m2))
        addHostText.value = "Adding......"
        addHostEnabled.value = false

    }

    fun addingPortButton(s1: String, s2: String, parts: String) {
            sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_ADD_PORT_ACTUAL, s2, s1, parts))
            addPortText.value = "Adding......"
            addPortEnabled.value = false
    }

    fun handleNewList(readMessage: String) {
        var position = 0
        addPortText.value = "Add Port"; addHostText.value = "Add Host"
        addPortEnabled.value = true; addHostEnabled.value = true
        notifyNowEnabled.value = true
        val hosts = readMessage.split('\n')
        for (host in hosts) {
            val ports = host.split(' ')
            for (port in ports) {
                if (port.length >= 3) portsNameArray!!.add(port)
                if (port.contains("@")) {
                    hostsPositionArray!!.add(position)
                    hostsNameArray!!.add(port)
                }
                position += 1
            }
        }

        if(portsNameArray!!.size > 1) portsNameArray!!.add("All")
        portsNameAdapter.value = portsNameArray
        hostsNameAdapter.value = hostsNameArray
        hostsPositionAdapter.value = hostsPositionArray
        sshPortEnabled.value = true
    }

    fun keyClickListener(profile: String) {
        sendMessage("treehouses remote key send $profile")
        jsonSend(true)
    }

    fun handleShowKeys(profileText: String) {
        var profile = profileText
        if (profile.isBlank()) profile = "default"
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("SSHKeyPref", Context.MODE_PRIVATE)
        var storedPublicKey: String? = sharedPreferences.getString("${profile}_public_key", "key")
        var storedPrivateKey: String? = sharedPreferences.getString("${profile}_private_key", "key")
        if (storedPublicKey != null && storedPrivateKey != null) {
            if (storedPublicKey.isBlank()) storedPublicKey = "No public key found"
            if (storedPrivateKey.isBlank()) storedPrivateKey = "No private key found"
        }

        val strPhonePublicKey : Spanned; val strPhonePrivateKey : Spanned
        if ((Build.VERSION.SDK_INT) >= 24) {
            strPhonePublicKey = Html.fromHtml("<b>Phone Public Key for ${profile}:</b> <br>$storedPublicKey\n", Html.FROM_HTML_MODE_LEGACY)
            strPhonePrivateKey = Html.fromHtml("<b>Phone Private Key for ${profile}:</b> <br>$storedPrivateKey", Html.FROM_HTML_MODE_LEGACY)
        } else {
            strPhonePublicKey = Html.fromHtml("<b>Phone Public Key for ${profile}:</b> <br>$storedPublicKey\n")
            strPhonePrivateKey = Html.fromHtml("<b>Phone Private Key for ${profile}:</b> <br>$storedPrivateKey")
        }
        dialogKeysPublicText.value = strPhonePublicKey; dialogKeysPrivateText.value = strPhonePrivateKey
    }

    protected fun handleOnStatus() {
        switchChecked.value = true; switchEnabled.value = true; notifyNowEnabled.value = true
        initializeArrays()
        sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))
    }

    protected fun handleNoPorts() {
        sshPortEnabled.value = true
        addPortText.value = "Add Port"; addHostText.value = "Add Host"
        addPortEnabled.value = false; addHostEnabled.value = true
        Toast.makeText(context, "Add a host", Toast.LENGTH_SHORT).show()
    }

    protected fun handleHostNotFound() {
        addHostText.value = "Add Host"; addHostEnabled.value = true
        addPortText.value = "Add Port"; addPortEnabled.value = true
        sshPortEnabled.value = true
        Toast.makeText(context, "Host not found. Failure to delete port.", Toast.LENGTH_SHORT).show()
    }

    protected fun handleModifiedList() {
        Toast.makeText(context, "Added/Removed. Retrieving port list.", Toast.LENGTH_SHORT).show()
        addPortText.value = "Retrieving"; addHostText.value = "Retrieving"
        initializeArrays()
        sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))
    }

    fun deleteHost(position: Int){
        val parts = portsNameArray!![position].split(":")[0]
        sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_REMOVE_HOST, parts)); addHostText.value = "deleting host ....."
        sshPortEnabled.value = false; addHostEnabled.value = false
    }

    fun deletePort(position: Int){
        var myPos: Int = 0
        for (pos in hostsPositionArray!!.indices) {
            if (hostsPositionArray!![pos] > position) {
                myPos = pos
                break
            }
        }
        if (portsNameArray!!.size > 1 && position == portsNameArray!!.size - 1) { sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_REMOVE_ALL)) }
        else {
            if (hostsPositionArray!!.last() < position) myPos = hostsPositionArray!!.lastIndex
            logD("dasda ${myPos.toString()}")
            val portName = TunnelUtils.getPortName(portsNameArray, position); val formatArgs = portName + " " + hostsNameArray!![myPos].split(":")[0]
            sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_REMOVE_PORT, formatArgs)); addPortText.value = "deleting port ....."
            sshPortEnabled.value = false; addPortEnabled.value = false
        }
    }


}