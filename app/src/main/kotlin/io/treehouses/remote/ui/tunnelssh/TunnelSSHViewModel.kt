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
import io.treehouses.remote.pojo.enum.Resource
import io.treehouses.remote.utils.TUNNEL_SSH_RESULTS
import io.treehouses.remote.utils.TunnelUtils
import io.treehouses.remote.utils.matchSshOutput

open class TunnelSSHViewModel(application: Application) : BaseTunnelSSHViewModel(application) {
    private val context = getApplication<MainApplication>().applicationContext

    override fun onRead(output: String) {
        super.onRead(output)
        val s = matchSshOutput(output)
        when {
            s == TUNNEL_SSH_RESULTS.RESULT_HOST_NOT_FOUND -> {
                enableButtons()
                Toast.makeText(context, "Host not found. Failure to delete port.", Toast.LENGTH_SHORT).show()
            }
            s == TUNNEL_SSH_RESULTS.RESULT_NO_TUNNEL -> {
                tunnelSSHObject.enableAddPort = false
            }
            s == TUNNEL_SSH_RESULTS.RESULT_ADDED -> sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))
            s == TUNNEL_SSH_RESULTS.RESULT_REMOVED -> {
                tunnelSSHObject.portNames.clear()
                tunnelSSHObject.enabledNotifyNow = false
                sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE));
            }
            s == TUNNEL_SSH_RESULTS.RESULT_MODIFIED_LIST -> handleModifiedList()
            s == TUNNEL_SSH_RESULTS.RESULT_SSH_PORT && lastCommand == getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS) -> handleNewList(output)
            s == TUNNEL_SSH_RESULTS.RESULT_STATUS_ON -> handleOnStatus()
            s == TUNNEL_SSH_RESULTS.RESULT_STATUS_OFF -> handleOffStatus()
            s == TUNNEL_SSH_RESULTS.RESULT_NO_PORT -> handleNoPorts()
            s == TUNNEL_SSH_RESULTS.RESULT_ALREADY_EXIST -> {
                tunnelSSHObject.addPortText = "Add Port"
                Toast.makeText(context, "Port already exists", Toast.LENGTH_SHORT).show()
            }
            output.contains("Error: only 'list'") -> {
                val message = "Please swipe slower in the future as you have a slow rpi, getting ports again..."
                sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE))
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            output.contains("true") || output.contains("false") -> {
                sendMessage("treehouses remote key send")
                Toast.makeText(context, "Please wait...", Toast.LENGTH_SHORT).show()
            }
            output.contains("Saved") -> Toast.makeText(context, "Keys successfully saved to Pi", Toast.LENGTH_SHORT).show()
            output.contains("unknown") -> jsonSend(false)

            output.contains("OK.") -> sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE))
            output.contains("Thanks for the feedback!") -> {
                val notified = "Notified Gitter. Thank you!"
                Toast.makeText(context, notified, Toast.LENGTH_SHORT).show()
                tunnelSSHObject.enabledNotifyNow = true
            }
            else -> if (jsonSent) handleJson(output)


        }
        Resource.success(tunnelSSHObject)

    }


    fun onCreateView() {
        Resource.loading(tunnelSSHData)
        tunnelSSHObject.enableSwitchNotification = true
        tunnelSSHObject.enabledNotifyNow = true
        tunnelSSHObject.hostNames = ArrayList()
        Resource.success(tunnelSSHData)
    }

    fun setUserVisibleHint() {
        loadBT()
        initializeArrays()
        sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE))
    }

    fun searchArray(array: java.util.ArrayList<String>?, portnum: String): Boolean {
        for (name in array!!) {
            var check = name.substringAfter(":")
            if (check.equals(portnum)) return true
        }
        return false
    }

    fun initializeArrays() {
        tunnelSSHObject.portNames = ArrayList()
        tunnelSSHObject.hostNames = ArrayList()
    }

    fun deleteHostPorts() {
        sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_REMOVE_ALL))
    }

    fun switchButton(isChecked: Boolean) {
        tunnelSSHObject.enableSwitchNotification = false
        if (isChecked) sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE_ON))
        else sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_NOTICE_OFF))
    }

    fun addingHostButton(m1: String, m2: String) {
        sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_ADD_HOST, m1, m2))
        tunnelSSHObject.addHostText = "Adding......"
        tunnelSSHObject.enableAddHost = false

    }

    fun addingPortButton(s1: String, s2: String, parts: String) {
        sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_ADD_PORT_ACTUAL, s2, s1, parts))
        tunnelSSHObject.addPortText = "Adding......"
        tunnelSSHObject.enableAddPort = false
    }

    fun handleNewList(readMessage: String) {
        var position = 0
        tunnelSSHObject.addPortText = "Add Port"; tunnelSSHObject.addHostText = "Add Host"
        tunnelSSHObject.enableAddPort = true; tunnelSSHObject.enableAddHost = true
        tunnelSSHObject.enabledNotifyNow = true
        val hosts = readMessage.split('\n')
        for (host in hosts) {
            val ports = host.split(' ')
            for (port in ports) {
                if (port.length >= 3) tunnelSSHObject.portNames!!.add(port)
                if (port.contains("@")) {
//                    hostsPositionArray!!.add(position)
                    tunnelSSHObject.hostNames!!.add(port)
                }
                position += 1
            }
        }

        if (tunnelSSHObject.portNames!!.size > 1) tunnelSSHObject.portNames!!.add("All")
        tunnelSSHObject.enableSSHPort = true
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

        val strPhonePublicKey: Spanned;
        val strPhonePrivateKey: Spanned
        if ((Build.VERSION.SDK_INT) >= 24) {
            strPhonePublicKey = Html.fromHtml("<b>Phone Public Key for ${profile}:</b> <br>$storedPublicKey\n")
            strPhonePrivateKey = Html.fromHtml("<b>Phone Private Key for ${profile}:</b> <br>$storedPrivateKey")
        } else {
            strPhonePublicKey = Html.fromHtml("<b>Phone Public Key for ${profile}:</b> <br>$storedPublicKey\n")
            strPhonePrivateKey = Html.fromHtml("<b>Phone Private Key for ${profile}:</b> <br>$storedPrivateKey")
        }
        tunnelSSHObject.publicKey = strPhonePublicKey;
        tunnelSSHObject.privateKey = strPhonePrivateKey
    }

    protected fun handleOnStatus() {
        tunnelSSHObject.checkSwitchNotification = true;
        tunnelSSHObject.enableSwitchNotification = true;
        tunnelSSHObject.enabledNotifyNow = true
        initializeArrays()
        sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))
    }

    protected fun handleOffStatus() {
        tunnelSSHObject.checkSwitchNotification = false;
        tunnelSSHObject.enableSwitchNotification = true
        tunnelSSHObject.enabledNotifyNow = true
        sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))
    }

    protected fun handleNoPorts() {
        tunnelSSHObject.enableSSHPort = true
        tunnelSSHObject.addPortText = "Add Port"; tunnelSSHObject.addHostText = "Add Host"
        tunnelSSHObject.enableAddPort = false; tunnelSSHObject.enableAddHost = true
        Toast.makeText(context, "Add a host", Toast.LENGTH_SHORT).show()
    }

    private fun enableButtons() {
        tunnelSSHObject.addHostText = "Add Host"
        tunnelSSHObject.addPortText = "Add Port"
        tunnelSSHObject.enableAddHost = true
        tunnelSSHObject.enableAddPort = true
        tunnelSSHObject.enableSSHPort = true
        Resource.success(tunnelSSHData)

    }

    protected fun handleModifiedList() {
        Toast.makeText(context, "Added/Removed. Retrieving port list.", Toast.LENGTH_SHORT).show()
        tunnelSSHObject.addPortText = "Retrieving";
        tunnelSSHObject.addHostText = "Retrieving"
        initializeArrays()
        sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_PORTS))
    }

    fun deleteHost(position: Int) {
        val parts = tunnelSSHObject.portNames!![position].split(":")[0]
        sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_REMOVE_HOST, parts)); tunnelSSHObject.addHostText = "deleting host ....."
        tunnelSSHObject.enableSSHPort = false; tunnelSSHObject.enableAddHost = false
    }

    fun deletePort(position: Int) {
//        var myPos: Int = 0
//        for (pos in hostsPositionArray!!.indices) {
//            if (hostsPositionArray!![pos] > position) {
//                myPos = pos
//                break
//            }
//        }
        if (tunnelSSHObject.portNames!!.size > 1 && position == tunnelSSHObject.portNames!!.size - 1) {
            sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_REMOVE_ALL))
        } else {
//            if (hostsPositionArray!!.last() < position) myPos = hostsPositionArray!!.lastIndex
            val portName = TunnelUtils.getPortName(tunnelSSHObject.portNames, position);
            val formatArgs = portName + " " + tunnelSSHObject.hostNames[position].split(":")[0]
            sendMessage(getString(R.string.TREEHOUSES_SSHTUNNEL_REMOVE_PORT, formatArgs)); tunnelSSHObject.addPortText = "deleting port ....."
            tunnelSSHObject.enableSSHPort = false; tunnelSSHObject.enableAddPort = false
        }
    }


}