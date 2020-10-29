package io.treehouses.remote.ui.home

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.os.Message
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import io.treehouses.remote.BuildConfig
import io.treehouses.remote.Constants
import io.treehouses.remote.MainApplication
import io.treehouses.remote.network.ParseDbService
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.pojo.enum.Resource
import io.treehouses.remote.pojo.enum.Status
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.logE
import io.treehouses.remote.utils.match
import java.util.*

class HomeViewModel(application: Application) : FragmentViewModel(application) {
    /**
     * The selected LED that is currently showing in the Test Connection Dialog
     */
    var selectedLed = 0

    /**
     * Boolean value to see if the version check has been sent
     * @see R.string.TREEHOUSES_REMOTE_VERSION
     */
    var checkVersionSent = false

    /**
     * Boolean value to see if internet check has been sent
     */
    var internetSent = false

    /**
     * Stores the SSID of the Network Profile that was chosen. Could be removed since networkProfile
     * is also being passed now
     */
    var networkSsid = ""

    /**
     * The Network Profile that was selected by the user
     */
    var networkProfile: NetworkProfile? = null

    var hashSent = MutableLiveData<Resource<String>>()
    var testConnectionResult = MutableLiveData<Resource<Boolean>>()

    /**
     * Generic error has occurred; Let the user know.
     */
    val error : MutableLiveData<String> = MutableLiveData()
    val errorConnecting : MutableLiveData<String> = MutableLiveData()
    val remoteUpdateRequired : MutableLiveData<Boolean> = MutableLiveData()
    val newCLIUpgradeAvailable : MutableLiveData<Boolean> = MutableLiveData()
    val internetStatus : MutableLiveData<Boolean> = MutableLiveData()
    val networkProfileResult : MutableLiveData<Resource<NetworkProfile>> = MutableLiveData()

    /**
     * Bluetooth device to connect to. Selected from the RPIDialogFragment
     * @see io.treehouses.remote.fragments.dialogfragments.RPIDialogFragment
     */
    var device: BluetoothDevice? = null

    /**
     * Connects to a Bluetooth device
     * @param device : BluetoothDevice = Device to connect to
     */
    fun connect(device: BluetoothDevice) {
        this.device = device
        mChatService.connect(device, true)
    }

    /**
     * @see FragmentViewModel.onRead
     * Matches the output to the command that was sent. Since values can be processed asynchronously, the order
     * in which we receive the commands may not be the same as sent.
     *
     * When HomeFragment is loaded, this is the desired flow of input.
     *
     * INITIALIZATION FLOW:
     * CHECK_INTERNET -> SEND_HASH -> CHECK_TREEHOUSES_REMOTE_VERSION -> TREEHOUSES_REMOTE_CHECK -> CLI_UPGRADE_CHECK
     *
     * Also, needs to listen for Network Profile configuration changes, and Test Connection Dialog
     * @see moreActions
     */
    override fun onRead(output: String) {
        val s = match(output)
        when {
            s == RESULTS.BOOLEAN && internetSent -> checkPackage(output)
            hashSent.value?.status == Status.LOADING && output.length > 30 -> {
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
                sendMessage(getString(R.string.TREEHOUSES_UPGRADE_CHECK))
            }
            else -> moreActions(output, s)
        }
    }

    /**
     * Interactive user checks (Network profile switches, and test connection result)
     */
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

    /**
     * Once the response from treehouses default network is received, it is now safe to switch network configurations
     */
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
                sendMessage(getApplication<MainApplication>().getString(R.string.TREEHOUSES_BRIDGE, profile.ssid, profile.hotspot_ssid,
                        profile.password, profile.hotspot_password))
            }
        }
    }

    /**
     * Check if the version of the remote matches the CLI required version
     */
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

    /**
     * Verify RPI internet connection
     */
    private fun checkPackage(output: String) {
        internetSent = false
        internetStatus.value = output.contains("true")
        sendMessage("remotehash\n")
        hashSent.value = Resource.loading("")
    }

    /**
     * @see FragmentViewModel.onWrite
     */
    override fun onWrite(input: String) {
        logE("ON WRITE $input")
    }

    override fun onOtherMessage(msg: Message) {
        if (msg.what == Constants.MESSAGE_ERROR) {
            errorConnecting.value = msg.obj as String
        }
    }

    /**
     * Uses the TREEHOUSES_REMOTE_CHECK to send data to Parse DB
     */
    private fun sendLog(deviceName: String, readMessage: List<String>) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        val connectionCount = preferences!!.getInt("connection_count", 0)
        val sendLog = preferences.getBoolean("send_log", true)
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