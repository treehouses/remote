package io.treehouses.remote.ui.network

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import io.treehouses.remote.Constants
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.Utils
import io.treehouses.remote.utils.match

class NetworkViewModel(application: Application) : BaseNetworkViewModel(application) {
    private val context = getApplication<MainApplication>().applicationContext
    var networkMode: MutableLiveData<String> = MutableLiveData()
    var ipAddress: MutableLiveData<String> = MutableLiveData()
    val remoteNetworkText: MutableLiveData<String> = MutableLiveData()
    var showHome: MutableLiveData<Boolean> = MutableLiveData()
    val downloadUpload: MutableLiveData<String> = MutableLiveData()
    var dialogCheck: MutableLiveData<Boolean> = MutableLiveData()

    private fun updateNetworkText(mode: String) {
        networkMode.value = "Current Network Mode: $mode"
        showNetworkProgress.value = false
    }

    fun onLoad() {
        getNetworkMode()
        sendMessage(getString(R.string.TREEHOUSES_NETWORKMODE_INFO))
    }

    private fun showIpAddress(output: String) {
        var ip = output.substringAfter("ip: ").substringBefore(", has")
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
            RESULTS.DEFAULT_CONNECTED ->  getNetworkMode()
            RESULTS.ERROR -> {
                showNetworkProgress.value = false
            }
            RESULTS.HOTSPOT_CONNECTED, RESULTS.WIFI_CONNECTED, RESULTS.BRIDGE_CONNECTED -> {
                getNetworkMode()
                showNetworkProgress.value = false
            }
            RESULTS.REVERSE_LOOKUP -> {
                Utils.showRemoteReverse(output, remoteNetworkText)
            }
            RESULTS.BOOLEAN -> updateInternet(output)
            RESULTS.SPEED_TEST -> {
                updateSpeed(output)
            }
            else -> { }
        }

    }

     fun getNetworkMode() {
         val msg = getString(R.string.TREEHOUSES_NETWORKMODE)
         sendMessage(msg)
     }

//    fun showRemoteReverse(output: String){
//        val reverseData = Gson().fromJson(output, ReverseData::class.java)
//        val ip = "ip: " + reverseData.ip
//        val postal = "postal: " + reverseData.postal
//        val city = "city: " + reverseData.city
//        val country = "country: " + reverseData.country
//        val org = "org: " + reverseData.org
//        val timezone = "timezone: " + reverseData.timezone
//        reverseText.value = ip + "\n" + org  + "\n" + country + "\n" + city + "\n" + postal + "\n" + timezone
//
////        reverseText.value = output
//    }

    override fun onError(output: String) {
        super.onError(output)
        downloadUpload.value = "Speed Test Failed"
        Toast.makeText(context, "Python Error", Toast.LENGTH_LONG).show()
    }

    fun treehousesInternet(){
        dialogCheck.value = true
        sendMessage("treehouses internet")
    }

    fun updateInternet(output: String){
        if (output.contains("true")) {
            downloadUpload.value = "Internet check passed. Performing speed test......"
            sendMessage("treehouses speedtest")
        } else{
            downloadUpload.value = "Internet check failed. Connect to network"
        }
    }

    fun updateSpeed(output: String){
        if (output.contains("Download:") && output.contains("Upload:")){
            downloadUpload.value = getSubString("Download:", output)
            downloadUpload.value += "\n" + getSubString("Upload", output)
        } else if (output.contains("Download:")){
            downloadUpload.value = getSubString("Download:", output)
        } else {
            downloadUpload.value += "\n" + getSubString("Upload", output)
        }
    }

    fun getSubString(stringStart: String, output: String) : String {
        val startIndex = output.indexOf(stringStart)
        val endIndex = output.indexOf("/s", startIndex)
        return output.substring(startIndex, endIndex + 2)
    }

}