package io.treehouses.remote.ui.network

import android.app.Application
import android.app.Dialog
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.Constants
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.logD
import io.treehouses.remote.utils.logE
import io.treehouses.remote.utils.match

class NetworkViewModel(application: Application) : FragmentViewModel(application) {
    private val context = getApplication<MainApplication>().applicationContext
    var networkMode: MutableLiveData<String> = MutableLiveData()
    var ipAddress: MutableLiveData<String> = MutableLiveData()
    val reverseText: MutableLiveData<String> = MutableLiveData()
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

    fun treehousesRemoteReverse(){
        sendMessage("treehouses remote reverse")
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
            RESULTS.REVERSE_LOOKUP ->{
                reverseText.value = output
            }
            else -> logE("NewNetworkFragment: Result not Found")
        }

    }

     fun getNetworkMode() {
        val msg = getString(R.string.TREEHOUSES_NETWORKMODE)
        sendMessage(msg)
    }

}