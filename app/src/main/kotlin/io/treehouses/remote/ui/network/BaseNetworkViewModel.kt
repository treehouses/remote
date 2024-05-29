package io.treehouses.remote.ui.network

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.utils.SaveUtils

open class BaseNetworkViewModel(application: Application) : FragmentViewModel(application) {
    private val context = getApplication<MainApplication>().applicationContext
    var showNetworkProgress: MutableLiveData<Boolean> = MutableLiveData()
    var wifiUserError: MutableLiveData<Boolean> = MutableLiveData()
    var checkBoxChecked: MutableLiveData<Boolean> = MutableLiveData()

    fun bridgeStartConfigListener(stringMap: Map<String, String>) {
        showNetworkProgress.value = true
        sendMessage(getString(R.string.TREEHOUSES_BRIDGE, stringMap.getValue("etEssid"), stringMap.getValue("etHotspotEssid"),
                stringMap.getValue("etPassword"), stringMap.getValue("etHotspotPassword")))
        Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
    }

    fun bridgeSetAddProfileListener(stringMap: Map<String, String>) {
        val networkProfile = NetworkProfile(stringMap.getValue("etEssid"), stringMap.getValue("etHotspotEssid"),
                stringMap.getValue("etPassword"), stringMap.getValue("etHotspotPassword"))
        SaveUtils.addProfile(context, networkProfile)
        Toast.makeText(context, "Bridge Profile Added", Toast.LENGTH_LONG).show()
    }

    fun ethernetStartConfigListener(ip: String, mask: String, gateway: String, dns: String){
        sendMessage(getString(R.string.TREEHOUSES_ETHERNET, ip, mask, gateway, dns))
    }

    fun hotspotStartConfigListener(etHotspotSsid: String, etHotspotPassword: String,
                                   checkBoxHiddenHotspot: Boolean, spnHotspotType: String) {
        if (checkBoxHiddenHotspot) sendHotspotMessage(R.string.TREEHOUSES_AP_HIDDEN, spnHotspotType,
                etHotspotSsid, etHotspotPassword)
        else sendHotspotMessage(R.string.TREEHOUSES_AP, spnHotspotType, etHotspotSsid, etHotspotPassword)
        Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
    }

    private fun sendHotspotMessage(command : Int, spnHotspotType: String, etHotspotSsid: String, etHotspotPassword: String) {
        showNetworkProgress.value = true
        sendMessage(getString(command, spnHotspotType,
                etHotspotSsid, etHotspotPassword))

    }

    fun hotspotSetAddProfileListener(checkBoxHiddenHotspot: Boolean, spnHotspotType: String,
                                     etHotspotSsid: String, etHotspotPassword: String) {
        SaveUtils.addProfile(context,
                NetworkProfile(etHotspotSsid, etHotspotPassword,
                        spnHotspotType, checkBoxHiddenHotspot))
        Toast.makeText(context, "Hotspot Profile Saved", Toast.LENGTH_LONG).show()
    }

    fun sendWifiMessage(booleanMap: Map<String, Boolean>, ssid:String, password: String, username: String) {
        if (booleanMap.getValue("checkBoxEnterprise") && username.isEmpty()) {
            wifiUserError.value = true
            return
        }
        wifiUserError.value = false
        val hidden = booleanMap.getValue("checkBoxHiddenWifi")
        val enterprise = booleanMap.getValue("checkBoxEnterprise")
        when {
            !enterprise -> sendMessage(getString(if (hidden) R.string.TREEHOUSES_WIFI_HIDDEN else R.string.TREEHOUSES_WIFI, ssid, password))
            enterprise -> sendMessage(getString(if (hidden) R.string.TREEHOUSES_WIFI_HIDDEN_ENTERPRISE else R.string.TREEHOUSES_WIFI_ENTERPRISE, ssid, password, username))
        }
        showNetworkProgress.value = true
        Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
    }

    fun wifiSetAddProfileListener(editTextSSID: String, wifipassword: String, checkBoxHiddenWifi: Boolean) {
        SaveUtils.addProfile(context, NetworkProfile(editTextSSID,
                wifipassword, checkBoxHiddenWifi))
        Toast.makeText(context, "WiFi Profile Saved", Toast.LENGTH_LONG).show()
    }

    fun hiddenOrEnterprise(isChecked: Boolean) {
        checkBoxChecked.value = isChecked
    }
}