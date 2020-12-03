package io.treehouses.remote.ui.discover

import android.app.Application
import android.view.View
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.fragments.DiscoverFragment
import io.treehouses.remote.utils.logD
import io.treehouses.remote.utils.logE
import kotlinx.android.synthetic.main.activity_discover_fragment.view.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class DiscoverViewModel(application: Application) : FragmentViewModel(application) {
    val isLoading1: MutableLiveData<Boolean> = MutableLiveData()
    val isLoading2: MutableLiveData<Boolean> = MutableLiveData()
    val deviceContainer: MutableLiveData<Boolean> = MutableLiveData()
    val swiperefreshEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val isRefreshing: MutableLiveData<Boolean> = MutableLiveData()
    val gatewayIconVisible: MutableLiveData<Boolean> = MutableLiveData()
    val deviceContainerVisible: MutableLiveData<Boolean> = MutableLiveData()
    val deviceList = ArrayList<Device>()
    var gateway = Gateway()
    var pi = Device()
    var piIP = ""


    fun onLoad()
    {
        loadBT()
        isLoading1.value = true
        isLoading2.value = true
        gatewayIconVisible.value = false
        isRefreshing.value = false
        swiperefreshEnabled.value = false
        requestNetworkInfo()
    }

    private fun requestNetworkInfo() {
        logD("Requesting Network Information")
        try {
            sendMessage(getString(R.string.TREEHOUSES_DISCOVER_GATEWAY_LIST))
            sendMessage(getString(R.string.TREEHOUSES_DISCOVER_GATEWAY))
            sendMessage(getString(R.string.TREEHOUSES_DISCOVER_SELF))
        } catch (e: Exception) {
            logE("Error Requesting Network Information")
        }
    }

    override fun onRead(output: String) {
        super.onRead(output)
        logD("READ = $output")
        if(!addDevices(output))
            if(!updateGatewayInfo(output))
                updatePiInfo(output)
    }

    private fun extractText(pattern: String, separator: String, msg: String): String? {
        val regex = pattern.toRegex()
        val res = regex.find(msg)
        var text: String? = null

        if (res != null) {
            if (separator == "") text = res.value
            else text = res.value.split(separator.toRegex())[1]
        }

        return text
    }


    private fun updatePiInfo(readMessage: String): Boolean {
        val ip = extractText("([0-9]+\\.){3}[0-9]+", "", readMessage)

        if (ip != null) {
            pi.ip = ip
            piIP = ip
        }

        val mac1 = extractText("eth0:\\s+([0-9a-z]+:){5}[0-9a-z]+", "eth0:\\s+", readMessage)
        val mac2 = extractText("wlan0:\\s+([0-9a-z]+:){5}[0-9a-z]+", "wlan0:\\s+", readMessage)

        if (mac1 != null) pi.mac = "\n$mac1 (ethernet)\n"

        if (mac2 != null) pi.mac += "$mac2 (wlan)\n"

        if (pi.isComplete() && pi.mac.matches("\n(.)+\n(.)+\n".toRegex()))
            if (!deviceList.contains(pi)) deviceList.add(pi)

        return !ip.isNullOrEmpty() || mac1.isNullOrEmpty() || !mac2.isNullOrEmpty()
    }


    private fun updateGatewayInfo(readMessage: String): Boolean {
        val ip = extractText("ip address:\\s+([0-9]+\\.){3}[0-9]", "ip address:\\s+", readMessage)
        if (ip != null) gateway.device.ip = ip

        val ssid = extractText("ESSID:\"(.)+\"", "ESSID:", readMessage)
        if (ssid != null) gateway.ssid = ssid.substring(1, ssid.length - 1)

        val mac = extractText("MAC Address:\\s+([0-9A-Z]+:){5}[0-9A-Z]+", "MAC Address:\\s+", readMessage)
        if (mac != null) gateway.device.mac = mac

        return !ip.isNullOrEmpty() || !ssid.isNullOrEmpty() || !mac.isNullOrEmpty()
    }


    private fun addDevices(readMessage: String): Boolean {
        var regex = "([0-9]+\\.){3}[0-9]+\\s+([0-9A-Z]+:){5}[0-9A-Z]+".toRegex()
        val devices = regex.findAll(readMessage)

        devices.forEach {
            val device = Device()

            device.ip = it.value.split("\\s+".toRegex())[0]
            device.mac = it.value.split("\\s+".toRegex())[1]

            if (!deviceList.contains(device)) deviceList.add(device)
        }

        return !devices.none()
    }



    override fun onWrite(input: String) {
        super.onWrite(input)
        logD("WRITE $input")
    }

    fun onTransition()
    {
        swiperefreshEnabled.value = true
        isLoading1.value = false
        isLoading1.value = false
        deviceContainerVisible.value = true
    }

}