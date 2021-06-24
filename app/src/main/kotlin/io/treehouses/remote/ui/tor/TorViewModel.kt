package io.treehouses.remote.ui.tor

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.databinding.ActivityTorFragmentBinding
import io.treehouses.remote.utils.TunnelUtils
import java.util.*

open class TorViewModel(application: Application) : FragmentViewModel(application) {

    private val context = getApplication<MainApplication>().applicationContext
    var myClipboard: ClipboardManager? = null
    var myClip: ClipData? = null
    var bind: ActivityTorFragmentBinding? = null
    var hostNameText: MutableLiveData<String> = MutableLiveData()
    var hostNameVisible: MutableLiveData<Boolean> = MutableLiveData()
    var switchNotificationEnabled: MutableLiveData<Boolean> = MutableLiveData()
    var switchNotificationCheck: MutableLiveData<Boolean> = MutableLiveData()
    var torStartText: MutableLiveData<String> = MutableLiveData()
    var torStartEnabled: MutableLiveData<Boolean> = MutableLiveData()
    var addPortText: MutableLiveData<String> = MutableLiveData()
    var addPortEnabled: MutableLiveData<Boolean> = MutableLiveData()
    var portListEnabled: MutableLiveData<Boolean> = MutableLiveData()
    var portsNameList: MutableLiveData<ArrayList<String>?> = MutableLiveData()
    var portNameArray: ArrayList<String>? = null
    var notifyNowEnabled: MutableLiveData<Boolean> = MutableLiveData()

    fun createView() {
        loadBT()
        sendMessage(getString(R.string.TREEHOUSES_TOR_PORTS))
        portNameArray = ArrayList()
        portsNameList.value = portNameArray
        hostNameVisible.value = false
        switchNotificationEnabled.value = false
        torStartEnabled.value = false
        torStartText.value = "Getting Tor Status from raspberry pi"
    }

    fun setUserVisibleHint() {
        loadBT()
        sendMessage(getString(R.string.TREEHOUSES_TOR_PORTS))
        portNameArray = ArrayList()
        portsNameList.value = portNameArray
    }

    fun addHostName(hostName: String) {
        myClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        myClip = ClipData.newPlainText("text", hostName)
        myClipboard!!.setPrimaryClip(myClip!!)
        Toast.makeText(context, "$hostName copied!", Toast.LENGTH_SHORT).show()
    }

    fun addNotification(isChecked: Boolean) {
        val noticeOn = R.string.TREEHOUSES_TOR_NOTICE_ON
        val noticeOff = R.string.TREEHOUSES_TOR_NOTICE_OFF
        if (isChecked) sendMessage(getString(noticeOn))
        else sendMessage(getString(noticeOff))
        switchNotificationEnabled.value = false
    }

    fun addPortList() {
        sendMessage(getString(R.string.TREEHOUSES_TOR_DELETE_ALL))
    }

    fun promptDeletePort(portName: ArrayList<String>?, position: Int) {
        val msg = getString(R.string.TREEHOUSES_TOR_DELETE, TunnelUtils.getPortName(portName, position))
        sendMessage(msg)
        addPortText.value = "Deleting port. Please wait..."
        portListEnabled.value = false
        addPortEnabled.value = false

    }

    fun addStart(textTorStart: String) {
        if (textTorStart === "Stop Tor") {
            torStartText.value = "Stopping Tor"
            torStartEnabled.value = false
            sendMessage(getString(R.string.TREEHOUSES_TOR_STOP))
        } else {
            sendMessage(getString(R.string.TREEHOUSES_TOR_START))
            torStartEnabled.value = false
            torStartText.value = "Starting Tor..."
        }
    }

    fun addingPort(s1: String, s2: String) {
        sendMessage(getString(R.string.TREEHOUSES_TOR_ADD, s2, s1))
        addPortText.value = "Adding port. Please wait..."
        portListEnabled.value = false; addPortEnabled.value = false
    }

    override fun onRead(output: String) {
        super.onRead(output)
        if (output.contains("inactive")) {
            hostNameVisible.value = false
            torStartText.value = "Start Tor"
            torStartEnabled.value = true
            sendMessage(getString(R.string.TREEHOUSES_TOR_NOTICE))
        } else if (output.contains(".onion")) {
            hostNameVisible.value = true
            hostNameText.value = output
            sendMessage(getString(R.string.TREEHOUSES_TOR_NOTICE))
        } else if (output.contains("Error")) {
            Toast.makeText(context, "Error, add a port if its your first time", Toast.LENGTH_SHORT).show()
            addPortText.value = "add ports"
            addPortEnabled.value = true
            portListEnabled.value = true
        } else if (output.contains("active")) {
            torStartText.value = "Stop Tor"
            sendMessage(getString(R.string.TREEHOUSES_TOR))
            torStartEnabled.value = true
        } else handleOtherMessages(output)
    }

    private fun handleOtherMessages(output: String) {
        if (output.contains("OK.")) sendMessage(getString(R.string.TREEHOUSES_TOR_NOTICE))
        else if (output.contains("Status: on")) {
            switchNotificationCheck.value = true; switchNotificationEnabled.value = true
        } else if (output.contains("Status: off")) {
            switchNotificationCheck.value = false; switchNotificationEnabled.value = true
        } //regex to match ports text
        else if (output.matches("(([0-9]+:[0-9]+)\\s?)+".toRegex())) {
            addPortText.value = "Add Port"
            portListEnabled.value = true
            addPortEnabled.value = true
            val ports = output.split(" ".toRegex()).toTypedArray()
            for (i in ports.indices) {
                if (i == ports.size - 1) break
                portNameArray!!.add(ports[i])
            }
            if (portNameArray!!.size > 1) portNameArray!!.add("All")
            portsNameList.value = portNameArray
            sendMessage(getString(R.string.TREEHOUSES_TOR_STATUS))
        } else handleMoreMessages(output)
    }

    private fun clearListAndNotice() {
        portNameArray = ArrayList()
        portsNameList.value = portNameArray
        sendMessage(getString(R.string.TREEHOUSES_TOR_STATUS))
    }

    private fun handleMoreMessages(output: String) {
        if (output.contains("No ports found")) {
            addPortText.value = "Add Port"
            portListEnabled.value = true; addPortEnabled.value = true
            clearListAndNotice()
        } else if (output.contains("the port has been added") || output.contains("has been deleted")) {
            clearListAndNotice()
            addPortText.value = "Retrieving port. Please wait..."
            if (output.contains("the port has been added")) {
                Toast.makeText(context, "Port added. Retrieving ports list again", Toast.LENGTH_SHORT).show()
            } else if (output.contains("has been deleted")) {
                Toast.makeText(context, "Port deleted. Retrieving ports list again", Toast.LENGTH_SHORT).show()
            }
        } else if (output.contains("ports have been deleted")) {
            clearListAndNotice()
        } else handleFurtherMessages(output)
    }

    private fun handleFurtherMessages(output: String) {
        if (output.contains("Thanks for the feedback!")) {
            notifyNowEnabled.value = true
            Toast.makeText(context, "Notified Gitter. Thank you!", Toast.LENGTH_SHORT).show()
        } else if (output.contains("the tor service has been stopped") || output.contains("the tor service has been started")) {
            sendMessage(getString(R.string.TREEHOUSES_TOR_STATUS))
        }
    }

}