package io.treehouses.remote.ui.socks

import android.app.AlertDialog
import android.app.Application
import android.view.ContextThemeWrapper
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.MainApplication
import io.treehouses.remote.MainApplication.Companion.context
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.utils.logD

class SocksViewModel(application: Application) : FragmentViewModel(application) {
    val addProfileButtonText: MutableLiveData<String> = MutableLiveData()
    val addProfileButtonEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val textStatusText: MutableLiveData<String> = MutableLiveData()
    val startButtonText: MutableLiveData<String> = MutableLiveData()
    val startButtonEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val passwordText: MutableLiveData<String> = MutableLiveData()
    val localPortText: MutableLiveData<String> = MutableLiveData()
    val localAddressText: MutableLiveData<String> = MutableLiveData()
    val serverHostText: MutableLiveData<String> = MutableLiveData()
    val refreshList: MutableLiveData<List<String>> = MutableLiveData()
    var profileNameText = mutableListOf<String>()
    val profileDialogDismiss: MutableLiveData<Boolean> = MutableLiveData()

    fun onLoad() {
        loadBT()
    }

    override fun onRead(output: String) {
        super.onRead(output)
        logD("SOCKS MESSAGE " + output)

        if (output.contains("inactive")) {
            textStatusText.value = "-"; startButtonText.value = "Start Tor"
            startButtonEnabled.value = true
            sendMessage(getString(R.string.TREEHOUSES_TOR_NOTICE))
        } else if (output.contains("Error when")) {
            profileNameText = ArrayList()
            sendMessage("treehouses shadowsocks list")
        } else if (output.contains("Use `treehouses shadowsock")) {
            addProfileButtonText.value = "Add Profile"
            addProfileButtonEnabled.value = true
            profileNameText = ArrayList()
            sendMessage("treehouses shadowsocks list")
        } else {
            getMessage2(output)
        }
        refreshList.value = profileNameText
    }

    fun portListListener() {

    }

    private fun getMessage2(readMessage: String) {
        if (readMessage.contains("removed")) {
            makeText(MainApplication.context, "Removed, retrieving list again", LENGTH_SHORT).show()
            profileNameText = ArrayList()
            sendMessage("treehouses shadowsocks list")
        } else if (readMessage.contains("tmptmp") && !readMessage.contains("disabled") && !readMessage.contains("stopped")) {
            if (readMessage.contains(' '))
                profileNameText.add(readMessage.split(' ')[0])
            else
                profileNameText.add(readMessage)

        }
        refreshList.value = profileNameText
    }

    fun addProfile(stringMap: Map<String, String>) {
        profileDialogDismiss.value = false

        val serverHost = stringMap.getValue("serverHost")
        val localAddress = stringMap.getValue("localAddress")
        val localPort = stringMap.getValue("localPort")
        val serverPort = stringMap.getValue("serverPort")
        val password = stringMap.getValue("password")
        if (serverHost.isNotEmpty() && localAddress.isNotEmpty() && localPort.isNotEmpty() && serverPort.isNotEmpty() && password.isNotEmpty()) {

            val message = "treehouses shadowsocks add { \\\"server\\\": \\\"$serverHost\\\", \\\"local_address\\\": \\\"$localAddress\\\", \\\"local_port\\\": $localPort, \\\"server_port\\\": $serverPort, \\\"password\\\": \\\"$password\\\", \\\"method\\\": \\\"rc4-md5\\\" }"
            sendMessage(message)
            addProfileButtonText.value = "Adding......"
            addProfileButtonEnabled.value = false

            profileDialogDismiss.value = true
        } else {
            makeText(context, "Missing Information", LENGTH_SHORT).show()
        }
    }

    fun listenerInitialized() {
        profileNameText = ArrayList()
        sendMessage("treehouses shadowsocks list")
    }
}