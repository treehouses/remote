package io.treehouses.remote.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import io.treehouses.remote.BuildConfig
import io.treehouses.remote.MainApplication
import io.treehouses.remote.Network.ParseDbService
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.pojo.enum.Resource
import io.treehouses.remote.pojo.enum.Status
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.match
import java.util.*

class HomeViewModel(application: Application) : FragmentViewModel(application) {
    var selectedLed = 0
    var checkVersionSent = false
    var internetSent = false
    var networkSsid = ""
    var networkProfile: NetworkProfile? = null

    var hashSent = MutableLiveData<Resource<String>>()
    var testConnectionResult = MutableLiveData<Resource<Boolean>>()
    val error : MutableLiveData<String> = MutableLiveData()
    val remoteUpdateRequired : MutableLiveData<Boolean> = MutableLiveData()
    val newCLIUpgradeAvailable : MutableLiveData<Boolean> = MutableLiveData()
    val internetStatus : MutableLiveData<Boolean> = MutableLiveData()
    val networkProfileResult : MutableLiveData<Resource<NetworkProfile>> = MutableLiveData()


    override fun onRead(output: String) {
        val s = match(output)
        when {
            hashSent.value?.status == Status.LOADING -> {
                hashSent.value = Resource.success(output)
                checkVersionSent = true
                sendMessage(getString(R.string.TREEHOUSES_REMOTE_VERSION, BuildConfig.VERSION_CODE))
            }
            s == RESULTS.ERROR && !output.toLowerCase(Locale.ROOT).contains("error") -> {
                error.value = output
                internetSent = false
            }
            s == RESULTS.VERSION && checkVersionSent -> checkVersion(output)
            s == RESULTS.REMOTE_CHECK -> {
                sendLog(mChatService.connectedDeviceName, output.trim().split(" "))
                sendMessage(getString(R.string.TREEHOUSES_INTERNET))
                internetSent = true
            }
            s == RESULTS.BOOLEAN && internetSent -> checkPackage(output)
            else -> moreActions(output, s)
        }
    }

    private fun moreActions(output: String, result: RESULTS) {
        when {
            result == RESULTS.UPGRADE_CHECK -> newCLIUpgradeAvailable.value = output.contains("true")
            result == RESULTS.HOTSPOT_CONNECTED || result == RESULTS.WIFI_CONNECTED -> networkProfileResult.value = Resource.success(networkProfile, "Switched to $networkSsid")
            result == RESULTS.BRIDGE_CONNECTED -> networkProfileResult.value = Resource.success(networkProfile, "Bridge Has Been Built")
            result == RESULTS.DEFAULT_NETWORK -> {
                networkProfileResult.value = Resource.loading(networkProfile)
                updateNetworkProfile(networkProfile)
            }
            result == RESULTS.ERROR -> networkProfileResult.value = Resource.error("Network Not Found", networkProfile)
            testConnectionResult.value?.status == Status.LOADING -> {
                testConnectionResult.value = Resource.success(true)
            }
        }
    }

    private fun updateNetworkProfile(profile: NetworkProfile?) {
        if (profile == null) return
        when {
            profile.isWifi -> {
                //WIFI
                sendMessage(
                        getString(if (profile.isHidden) R.string.TREEHOUSES_WIFI_HIDDEN else R.string.TREEHOUSES_WIFI,
                                profile.ssid, profile.password))
                networkSsid = profile.ssid
            }
            profile.isHotspot -> {
                //Hotspot
                sendMessage(
                        getString(if (profile.isHidden) R.string.TREEHOUSES_AP_HIDDEN else R.string.TREEHOUSES_AP,
                                profile.option, profile.ssid, profile.password))
                networkSsid = profile.ssid
            }
            profile.isBridge -> {
                //Bridge
                sendMessage(getString(R.string.TREEHOUSES_BRIDGE, profile.ssid, profile.hotspot_ssid,
                        profile.password, profile.hotspot_password))
            }
        }
    }

    private fun checkVersion(output: String) {
        checkVersionSent = false
        if (output.contains("Usage") || output.contains("command")) {
            error.value = output
        } else if (BuildConfig.VERSION_CODE == 2 || output.contains("true")) {
            sendMessage(getString(R.string.TREEHOUSES_REMOTE_CHECK))
        } else if (output.contains("false")) {
            remoteUpdateRequired.value = true
        }
    }

    private fun checkPackage(output: String) {
        internetSent = false
        internetStatus.value = output.contains("true")
        sendMessage(getString(R.string.TREEHOUSES_UPGRADE_CHECK))
    }

    override fun onWrite(input: String) {
        Log.e("ON WRITE", input)
    }

    private fun sendLog(deviceName: String, readMessage: List<String>) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val connectionCount = preferences!!.getInt("connection_count", 0)
        val sendLog = preferences!!.getBoolean("send_log", true)
        preferences.edit().putInt("connection_count", connectionCount + 1).apply()
        if (connectionCount >= 3 && sendLog) {
            val map = HashMap<String, String?>()
            map["bluetoothMacAddress"] = readMessage[0]
            map["imageVersion"] = readMessage[1]
            map["treehousesVersion"] = readMessage[2]
            map["rpiVersion"] = readMessage[3]
            ParseDbService.sendLog(getApplication(), deviceName, map, preferences)
            getApplication<MainApplication>().logSent = true
        }
    }
}