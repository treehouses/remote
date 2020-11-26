package io.treehouses.remote.ui.discover

import android.app.Application
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.fragments.DiscoverFragment
import io.treehouses.remote.utils.logD
import io.treehouses.remote.utils.logE
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class DiscoverViewModel(application: Application) : FragmentViewModel(application) {
    val isLoading1: MutableLiveData<Boolean> = MutableLiveData()
    val isLoading2: MutableLiveData<Boolean> = MutableLiveData()
    val deviceContainer: MutableLiveData<Boolean> = MutableLiveData()
    val gatewayIcon: MutableLiveData<Boolean> = MutableLiveData()
    val swiperefresh: MutableLiveData<Boolean> = MutableLiveData()
    val deviceList: MutableLiveData<DiscoverFragment.Device> = MutableLiveData()

    fun onLoad()
    {
        loadBT()
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

        if (output.startsWith("Ports:")) transition()
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

}