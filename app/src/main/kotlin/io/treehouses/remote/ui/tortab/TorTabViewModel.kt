package io.treehouses.remote.ui.tortab

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.Switch
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.databinding.ActivityTorFragmentBinding
import io.treehouses.remote.utils.DialogUtils
import io.treehouses.remote.utils.TunnelUtils
import java.util.ArrayList

open class TorTabViewModel(application: Application) : FragmentViewModel(application) {

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
    var notifyNowEnabled: MutableLiveData<Boolean> = MutableLiveData()

    fun createView(){
        loadBT()
        sendMessage(getString(R.string.TREEHOUSES_TOR_PORTS))
    }

    fun setUserVisibleHint() {
        loadBT()
        sendMessage(getString(R.string.TREEHOUSES_TOR_PORTS))
        portsNameList.value = ArrayList()
    }

    fun addHostName(hostName:String){
        myClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        myClip = ClipData.newPlainText("text", hostName)
        myClipboard!!.setPrimaryClip(myClip!!)
        Toast.makeText(context, "$hostName copied!", Toast.LENGTH_SHORT).show()
    }

    fun addNow(){
        sendMessage(getString(R.string.TREEHOUSES_TOR_NOTICE_NOW))
        Toast.makeText(context, "The Gitter Channel has been notified.", Toast.LENGTH_SHORT).show()
    }

    fun addNotification(isChecked: Boolean){
        if (isChecked) sendMessage(getString(R.string.TREEHOUSES_TOR_NOTICE_ON))
        else sendMessage(getString(R.string.TREEHOUSES_TOR_NOTICE_OFF))
        switchNotificationEnabled.value = false
    }

    fun addPortList(){
        sendMessage(getString(R.string.TREEHOUSES_TOR_DELETE_ALL))
    }

    fun promptDeletePort(portName: ArrayList<String>?, position: Int) {
        val msg = getString(R.string.TREEHOUSES_TOR_DELETE, TunnelUtils.getPortName(portName, position))
        sendMessage(msg)
        addPortText.value = "Deleting port. Please wait..."
        portListEnabled.value = false
        addPortEnabled.value = false

    }

    fun addStart(textTorStart: String){
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

    fun addingPort(s1: String, s2: String){
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

    fun handleOtherMessages(output: String) {
        if (output.contains("OK.")) sendMessage(getString(R.string.TREEHOUSES_TOR_NOTICE))
        else if (output.contains("Status: on")) {
            switchNotificationCheck.value = true; switchNotificationEnabled.value = true
        } else if (output.contains("Status: off")) {
            switchNotificationCheck.value = false; switchNotificationEnabled.value= true
        } //regex to match ports text
        else if (output.matches("(([0-9]+:[0-9]+)\\s?)+".toRegex())) {
            addPortText.value = "Add Port"
            portListEnabled.value = true
            addPortEnabled.value = true
            val ports = output.split(" ".toRegex()).toTypedArray()
            for (i in ports.indices) {
                if (i == ports.size - 1) break
                portsNameList.value?.add(ports[i])
            }
            if (portsNameList.value!!.size > 1) portsNameList.value?.add("All")
            sendMessage(getString(R.string.TREEHOUSES_TOR_STATUS))
        } else handleMoreMessages(output)
    }

    fun handleMoreMessages(output: String) {
        if (output.contains("No ports found")) {
            addPortText.value = "Add Port"
            portListEnabled.value = true; addPortEnabled.value = true
            portsNameList.value = ArrayList()
            sendMessage(getString(R.string.TREEHOUSES_TOR_STATUS))
        } else if (output.contains("the port has been added") || output.contains("has been deleted")) {
            sendMessage(getString(R.string.TREEHOUSES_TOR_PORTS))
            portsNameList.value = ArrayList()
            addPortText.value = "Retrieving port. Please wait..."
            if (output.contains("the port has been added")) {
                Toast.makeText(context, "Port added. Retrieving ports list again", Toast.LENGTH_SHORT).show()
            } else if (output.contains("has been deleted")) {
                Toast.makeText(context, "Port deleted. Retrieving ports list again", Toast.LENGTH_SHORT).show()
            } else handleFurtherMessages(output)
        } else if (output.contains("ports have been deleted")) {
            sendMessage(getString(R.string.TREEHOUSES_TOR_PORTS))
            portsNameList.value = ArrayList()
            portsNameList.value = ArrayList()
        }
    }

    fun handleFurtherMessages(output: String) {
        if (output.contains("Thanks for the feedback!")) {
            Toast.makeText(context, "Notified Gitter. Thank you!", Toast.LENGTH_SHORT).show()
            notifyNowEnabled.value = true
        } else if (output.contains("the tor service has been stopped") || output.contains("the tor service has been started")) {
            sendMessage(getString(R.string.TREEHOUSES_TOR_STATUS))
        }
    }

}