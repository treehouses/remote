package io.treehouses.remote.ui.network

import android.app.Application
import android.view.View
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.MutableLiveData
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
        showNetworkProgress.value = false
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
                logD("HOTSPOT CONNECTED")
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

    fun bridgeStartConfigListener(etEssid: TextInputEditText, etHotspotEssid: TextInputEditText,
                                  etPassword: TextInputEditText, etHotspotPassword: TextInputEditText) {
        showNetworkProgress.value = true
        sendMessage(getString(R.string.TREEHOUSES_BRIDGE, etEssid.text.toString(),
                etHotspotEssid.text.toString(), etPassword.text.toString(), etHotspotPassword.text.toString()))
        Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
    }

    fun bridgeSetAddProfileListener(etEssid: TextInputEditText, etHotspotEssid: TextInputEditText,
                                    etPassword: TextInputEditText, etHotspotPassword: TextInputEditText) {
        val networkProfile = NetworkProfile(etEssid.text.toString(), etPassword.text.toString(),
                etHotspotEssid.text.toString(), etHotspotPassword.text.toString())
        SaveUtils.addProfile(context, networkProfile)
        Toast.makeText(context, "Bridge Profile Added", Toast.LENGTH_LONG).show()
    }

    fun ethernetStartConfigListener(ip: TextInputEditText, mask: TextInputEditText,
                                    gateway: TextInputEditText, dns: TextInputEditText){
        sendMessage(getString(R.string.TREEHOUSES_ETHERNET, ip.text.toString(),
                mask.text.toString(), gateway.text.toString(), dns.text.toString()))
    }

    fun hotspotStartConfigListener(checkBoxHiddenHotspot: AppCompatCheckBox, spnHotspotType: Spinner,
                                   etHotspotSsid: TextInputEditText, etHotspotPassword: TextInputEditText) {
        if (checkBoxHiddenHotspot.isChecked) sendHotspotMessage(R.string.TREEHOUSES_AP_HIDDEN, spnHotspotType,
                etHotspotSsid, etHotspotPassword)
        else sendHotspotMessage(R.string.TREEHOUSES_AP, spnHotspotType, etHotspotSsid, etHotspotPassword)
        Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
    }

    private fun sendHotspotMessage(command : Int, spnHotspotType: Spinner, etHotspotSsid: TextInputEditText,
                                   etHotspotPassword: TextInputEditText) {
        showNetworkProgress.value = true
        sendMessage(getString(command, spnHotspotType.selectedItem.toString(),
                etHotspotSsid.text.toString(), etHotspotPassword.text.toString()))
    }

    fun hotspotSetAddProfileListener(checkBoxHiddenHotspot: AppCompatCheckBox, spnHotspotType: Spinner,
                                     etHotspotSsid: TextInputEditText, etHotspotPassword: TextInputEditText) {
        SaveUtils.addProfile(context,
                NetworkProfile(etHotspotSsid.text.toString(), etHotspotPassword.text.toString(),
                        spnHotspotType.selectedItem.toString(), checkBoxHiddenHotspot.isChecked))
        Toast.makeText(context, "Hotspot Profile Saved", Toast.LENGTH_LONG).show()
    }

    fun wifiStartConfigListener(checkBoxHiddenWifi: AppCompatCheckBox, checkBoxEnterprise: AppCompatCheckBox,
                                editTextSSID:TextInputEditText, wifipassword: TextInputEditText, wifiUsername: TextInputEditText) {
        val ssid = editTextSSID.text.toString()
        val password = wifipassword.text.toString()
        val username = wifiUsername.text.toString()
        if (checkBoxEnterprise.isChecked && wifiUsername.text.isNullOrEmpty()) {
            wifiUsername.error = "Please enter a username"
            return
        }
        sendWifiMessage(checkBoxHiddenWifi, checkBoxEnterprise, ssid, password, username)
    }

    private fun sendWifiMessage(checkBoxHiddenWifi: AppCompatCheckBox, checkBoxEnterprise: AppCompatCheckBox, ssid:String, password: String, username: String) {
        val hidden = checkBoxHiddenWifi.isChecked
        val enterprise = checkBoxEnterprise.isChecked
        when {
            !enterprise -> sendMessage(getString(if (hidden) R.string.TREEHOUSES_WIFI_HIDDEN else R.string.TREEHOUSES_WIFI, ssid, password))
            enterprise -> sendMessage(getString(if (hidden) R.string.TREEHOUSES_WIFI_HIDDEN_ENTERPRISE else R.string.TREEHOUSES_WIFI_ENTERPRISE, ssid, password, username))
        }
        showNetworkProgress.value = true
        Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
    }

    fun hiddenOrEnterprise(checkBoxEnterprise: AppCompatCheckBox, enterpriseLayout: TextInputLayout) {
        checkBoxEnterprise.setOnCheckedChangeListener {_, isChecked ->
            enterpriseLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    fun wifiSetAddProfileListener(editTextSSID: TextInputEditText, wifipassword: TextInputEditText, checkBoxHiddenWifi: AppCompatCheckBox) {
        SaveUtils.addProfile(context, NetworkProfile(editTextSSID.text.toString(),
                wifipassword.text.toString(), checkBoxHiddenWifi.isChecked))
        Toast.makeText(context, "WiFi Profile Saved", Toast.LENGTH_LONG).show()
    }

}