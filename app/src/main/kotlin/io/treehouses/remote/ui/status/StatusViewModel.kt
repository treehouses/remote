package io.treehouses.remote.ui.status

import android.app.Application
import android.os.Message
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import io.treehouses.remote.BuildConfig
import io.treehouses.remote.Constants
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.pojo.StatusData
import java.util.*

class StatusViewModel(application: Application) : FragmentViewModel(application) {

    var updateRightNow = false
    val deviceName: MutableLiveData<String> = MutableLiveData()
    val error: MutableLiveData<String> = MutableLiveData()
    val cpuModelText: MutableLiveData<String> = MutableLiveData()
    val temperature: MutableLiveData<String> = MutableLiveData()
    val storage: MutableLiveData<String> = MutableLiveData()
    val memory: MutableLiveData<String> = MutableLiveData()
    val memoryBarValue: MutableLiveData<Int> = MutableLiveData()
    val storageBarValue: MutableLiveData<Int> = MutableLiveData()
    val networkModeText: MutableLiveData<String> = MutableLiveData()
    val hostName: MutableLiveData<String> = MutableLiveData()
    val countryDisplayText: MutableLiveData<String> = MutableLiveData()
    val countryDisplayTextEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val showUpgrade: MutableLiveData<Boolean> = MutableLiveData()
    val showRefresh: MutableLiveData<Boolean> = MutableLiveData()
    val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val showNotification: MutableLiveData<Boolean> = MutableLiveData()
    val imageText: MutableLiveData<String> = MutableLiveData()
    val deviceAddress: MutableLiveData<String> = MutableLiveData()
    val rpiType: MutableLiveData<String> = MutableLiveData()
    var rpiVersion: String = ""
    val ipAddressText: MutableLiveData<String> = MutableLiveData()
    val ssidText: MutableLiveData<String> = MutableLiveData()
    val upgradeCheckText: MutableLiveData<String> = MutableLiveData()
    val remoteVersion: MutableLiveData<String> = MutableLiveData()
    val countryList: MutableLiveData<Array<String?>> = MutableLiveData()

    private fun getCountryName(country: String): String {
        val l = Locale("", country)
        val countryName = l.displayCountry
        return "$countryName ( $country )"
    }

    fun onLoad() {
        loadBT()
        fetchWifiCountry()
        deviceName.value = mChatService.connectedDeviceName
        countryDisplayTextEnabled.value = false
        showUpgrade.value = false
        refresh()
    }


    private fun fetchWifiCountry() {
        val countriesCode = Locale.getISOCountries()
        val countriesName = arrayOfNulls<String>(countriesCode.size)
        for (i in countriesCode.indices) {
            countriesName[i] = getCountryName(countriesCode[i])
        }
        countryList.value = countriesName;
    }

    override fun onRead(output: String) {
        if (output.startsWith("country=") || output.contains("set to")) {
            val len = output.length - 3
            val country = output.substring(len).trim { it <= ' ' }
            countryDisplayText.value = getCountryName(country)
            countryDisplayTextEnabled.value = true
            showRefresh.value = true
        } else if (output.contains("invalid country code")) {
            countryDisplayText.value = "Try again"
            countryDisplayTextEnabled.value = true
            Toast.makeText(MainApplication.context, "Error when changing country", Toast.LENGTH_LONG).show()
        } else {
            updateViews(output)
        }
    }

    private fun updateViews(output: String) {
        try {
            if (lastCommand == getString(R.string.TREEHOUSES_REMOTE_STATUSPAGE)) {
                val statusData = Gson().fromJson(output, StatusData::class.java)
                temperature.value = statusData.temperature
                var usedMemory = statusData.memory_used.trim { it <= ' ' }.toDouble()
                var totalMemory = statusData.memory_total.trim { it <= ' ' }.toDouble()
                storageBarValue.value = statusData.storage.split(" ")[3].dropLast(1).toInt()
                storage.value = statusData.storage.split(" ")[2].dropLast(1).replace("G", "GB")
                cpuModelText.value = "CPU: ARM " + statusData.arm
                writeNetworkInfo(statusData.networkmode, statusData.info)
                hostName.value = "Hostname: " + statusData.hostname
                memoryBarValue.value = (usedMemory / totalMemory * 100).toInt()
                memory.value = usedMemory.toString() + "GB" + "/" + totalMemory.toString() + "GB"
                val res = statusData.status.trim().split(" ")
                imageText.value = String.format("Image Version: %s", res[2].substring(8))
                deviceAddress.value = res[1]
                rpiType.value = "Model: " + res[4]
                rpiVersion = res[3]
                remoteVersion.value = "Remote Version: " + BuildConfig.VERSION_NAME
                checkWifiStatus(statusData.internet)
                isLoading.value = false
                sendMessage(getString(R.string.TREEHOUSES_WIFI_COUNTRY_CHECK))
            } else checkUpgradeStatus(output)
        } catch (e: Exception) {
        }
    }

    private fun checkUpgradeStatus(readMessage: String) {
        checkUpgradeNow()
        if (readMessage.startsWith("false ") && readMessage.length < 14) {
            upgradeCheckText.value = String.format("Latest Version: %s", rpiVersion)
            showUpgrade.value = false
        } else if (readMessage.startsWith("true ") && readMessage.length < 14) {
            upgradeCheckText.value = String.format("Upgrade available from %s to %s", rpiVersion, readMessage.substring(4))
            showUpgrade.value = true
        }
    }

    private fun checkUpgradeNow() {
        if (updateRightNow) {
            updateRightNow = false
            isLoading.value = false
            Toast.makeText(MainApplication.context, "Treehouses Cli has been updated!!!", Toast.LENGTH_LONG).show()
            showNotification.value = false
            refresh()
        }
    }

    private fun checkWifiStatus(readMessage: String) {
        if (readMessage.startsWith("true")) {
            sendMessage(getString(R.string.TREEHOUSES_UPGRADE_CHECK))
        } else {
            upgradeCheckText.value = "      NO INTERNET"
            showUpgrade.value = false
        }
    }

    private fun writeNetworkInfo(networkMode: String, readMessage: String) {
        val ssid = readMessage.substringAfter("essid: ").substringBefore(", ip:")
        var ip = readMessage.substringAfter("ip: ").substringBefore(", has")
        when (networkMode) {
            "default" -> networkModeText.value = "Default"
            "wifi" -> {
                networkModeText.value = "WiFi"
                countryDisplayTextEnabled.value = true
            }
            "hotspot" -> networkModeText.value = "Hotspot"
            "bridge" -> networkModeText.value = "Bridge"
            "ethernet" -> networkModeText.value = "Ethernet"
        }
        if (ip == "") {
            ip = "N/A"
        }
        ipAddressText.value = "IP Address: " + ip
        ssidText.value = "SSID: " + ssid
    }

    fun setChecking() {
        showUpgrade.value = false
        deviceAddress.value = "dc.."
        networkModeText.value = "Checking Network Mode....."
        ipAddressText.value = "IP Address: Checking....."
        ssidText.value = "SSID: Checking....."
        hostName.value = "Hostname: ⏳"
        rpiType.value = "Model: ⏳"
        cpuModelText.value = "CPU: ⏳"
        imageText.value = "Image Version: ⏳"
        remoteVersion.value = "Remote Version: ⏳"
        upgradeCheckText.value = "Checking Version..."
        temperature.value = "Checking......"
        memory.value = "Checking......"
        storage.value = "Checking......"
        storageBarValue.value = 0
        memoryBarValue.value = 0
    }

    fun refresh() {
        setChecking()
        sendMessage(getString(R.string.TREEHOUSES_REMOTE_STATUSPAGE))
        showRefresh.value = false
        countryDisplayTextEnabled.value = false
    }

    fun onSelectCountry(selectedString: String) {
        var selected = selectedString.substring(selectedString.length - 4, selectedString.length - 2)
        getString(R.string.TREEHOUSES_UPGRADE_CHECK)
        sendMessage(getString(R.string.TREEHOUSES_WIFI_COUNTRY, selected))
        countryDisplayTextEnabled.value = false
        countryDisplayText.value = "Changing country"
    }

    fun upgrade() {
        sendMessage(getString(R.string.TREEHOUSES_UPGRADE))
        updateRightNow = true
        showUpgrade.value = false
        isLoading.value = true
    }
}