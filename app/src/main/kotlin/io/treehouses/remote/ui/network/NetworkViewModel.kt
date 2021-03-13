package io.treehouses.remote.ui.network

import android.app.Application
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.MutableLiveData
import com.google.android.material.textfield.TextInputEditText
import io.treehouses.remote.Constants
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.databinding.DialogBridgeBinding
import io.treehouses.remote.databinding.DialogEthernetBinding
import io.treehouses.remote.databinding.DialogHotspotBinding
import io.treehouses.remote.databinding.DialogWifiBinding
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.utils.*

class NetworkViewModel(application: Application) : FragmentViewModel(application) {
    private val context = getApplication<MainApplication>().applicationContext
    var networkMode: MutableLiveData<String> = MutableLiveData()
    var ipAddress: MutableLiveData<String> = MutableLiveData()
    var showHome: MutableLiveData<Boolean> = MutableLiveData()
    var showNetworkProgress: MutableLiveData<Boolean> = MutableLiveData()
    private fun updateNetworkText(mode: String) {
        logD( "Current Network Mode: $mode" )
        networkMode.value = "Current Network Mode: $mode"
    }

    fun onLoad() {
        getNetworkMode()
        sendMessage(getString(R.string.TREEHOUSES_NETWORKMODE_INFO))
    }

    private fun showIpAddress(output: String) {
        var ip = output.substringAfter("ip: ").substringBefore(", has")
        logD( "Current ip: $ip" )
        if (ip == "") ip = "N/A"
        ipAddress.value = "IP Address: " + ip
    }

    fun rebootHelper() {
        try {
            sendMessage(getString(R.string.REBOOT))
            Thread.sleep(1000)
            if (mChatService.state != Constants.STATE_CONNECTED) {
                Toast.makeText(context, "Bluetooth Disconnected: Reboot in progress", Toast.LENGTH_LONG).show()
                showHome.value = true
            } else {
                Toast.makeText(context, "Reboot Unsuccessful", Toast.LENGTH_LONG).show()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }


    fun resetNetwork() {
        val msg = getString(R.string.TREEHOUSES_DEFAULT_NETWORK)
        sendMessage(msg)
    }

    override fun onRead(output: String) {
        super.onRead(output)
        when (match(output)) {
            RESULTS.NETWORKMODE, RESULTS.DEFAULT_NETWORK -> updateNetworkText(output)
            RESULTS.NETWORKMODE_INFO -> showIpAddress(output)
            RESULTS.DEFAULT_CONNECTED -> {
                //update network mode
                getNetworkMode()
            }
            RESULTS.ERROR -> {
                // showDialog(context, "Error", output)
                showNetworkProgress.value = false
            }
            RESULTS.HOTSPOT_CONNECTED, RESULTS.WIFI_CONNECTED, RESULTS.BRIDGE_CONNECTED -> {

                // showDialog(context, "Network Switched", output)
                //update network mode
                getNetworkMode()
                // Utils.sendMessage(listener, Pair(msg, "Network Mode retrieved"), context, Toast.LENGTH_LONG)
                showNetworkProgress.value = false
            }
            else -> logE("NewNetworkFragment: Result not Found")
        }
    }

     fun getNetworkMode() {
        val msg = getString(R.string.TREEHOUSES_NETWORKMODE)
        sendMessage(msg)
    }

    fun bridgeStartConfigListener(bind: DialogBridgeBinding) {
        sendMessage(getString(R.string.TREEHOUSES_BRIDGE, bind.etEssid.text.toString(),
                bind.etHotspotEssid.text.toString(), bind.etPassword.text.toString(),
                bind.etHotspotPassword.text.toString()))
        Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
    }

    fun bridgeSetAddProfileListener(bind: DialogBridgeBinding) {
        val networkProfile = NetworkProfile(bind.etEssid.text.toString(), bind.etPassword.text.toString(),
                bind.etHotspotEssid.text.toString(), bind.etHotspotPassword.text.toString())
        SaveUtils.addProfile(context, networkProfile)
        Toast.makeText(context, "Bridge Profile Added", Toast.LENGTH_LONG).show()
    }

    fun ethernetStartConfigListener(ip: TextInputEditText, mask: TextInputEditText,
                                    gateway: TextInputEditText, dns: TextInputEditText){
        sendMessage(getString(R.string.TREEHOUSES_ETHERNET, ip.text.toString(),
                mask.text.toString(), gateway.text.toString(), dns.text.toString()))
    }

    fun hotspotStartConfigListener(bind: DialogHotspotBinding) {
        if (bind.checkBoxHiddenHotspot.isChecked) sendHotspotMessage(R.string.TREEHOUSES_AP_HIDDEN, bind)
        else sendHotspotMessage(R.string.TREEHOUSES_AP, bind)
        Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
    }

    fun sendHotspotMessage(command : Int, bind: DialogHotspotBinding) {
        sendMessage(getString(command, bind.spnHotspotType.selectedItem.toString(),
                bind.etHotspotSsid.text.toString(), bind.etHotspotPassword.text.toString()))
    }

    fun hotspotSetAddProfileListener(bind: DialogHotspotBinding) {
        SaveUtils.addProfile(context,
                NetworkProfile(bind.etHotspotSsid.text.toString(), bind.etHotspotPassword.text.toString(),
                        bind.spnHotspotType.selectedItem.toString(), bind.checkBoxHiddenHotspot.isChecked))
        Toast.makeText(context, "Hotspot Profile Saved", Toast.LENGTH_LONG).show()
    }

    fun wifiStartConfigListener(bind: DialogWifiBinding) {
        val ssid = bind.editTextSSID.text.toString()
        val password = bind.wifipassword.text.toString()
        val username = bind.wifiUsername.text.toString()
        if (bind.checkBoxEnterprise.isChecked && bind.wifiUsername.text.isNullOrEmpty()) {
            bind.wifiUsername.error = "Please enter a username"
            return
        }
        sendWifiMessage(bind, ssid, password, username)
    }

    fun sendWifiMessage(bind: DialogWifiBinding, ssid:String, password: String, username: String) {
        val hidden = bind.checkBoxHiddenWifi.isChecked
        val enterprise = bind.checkBoxEnterprise.isChecked
        when {
            !enterprise -> sendMessage(getString(if (hidden) R.string.TREEHOUSES_WIFI_HIDDEN else R.string.TREEHOUSES_WIFI, ssid, password))
            enterprise -> sendMessage(getString(if (hidden) R.string.TREEHOUSES_WIFI_HIDDEN_ENTERPRISE else R.string.TREEHOUSES_WIFI_ENTERPRISE, ssid, password, username))
        }
        Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
    }

    fun hiddenOrEnterprise(bind: DialogWifiBinding) {
        bind.checkBoxEnterprise.setOnCheckedChangeListener {_, isChecked ->
            bind.enterpriseLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    fun wifiSetAddProfileListener(bind: DialogWifiBinding) {
        SaveUtils.addProfile(context, NetworkProfile(bind.editTextSSID.text.toString(),
                bind.wifipassword.text.toString(), bind.checkBoxHiddenWifi.isChecked))
        Toast.makeText(context, "WiFi Profile Saved", Toast.LENGTH_LONG).show()
    }

}