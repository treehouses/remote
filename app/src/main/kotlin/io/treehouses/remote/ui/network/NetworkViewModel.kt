package io.treehouses.remote.ui.network

import android.app.Application
import android.view.View
import android.widget.Toast
import io.treehouses.remote.Constants
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.ui.home.HomeFragment
import io.treehouses.remote.utils.RESULTS
import io.treehouses.remote.utils.Utils
import io.treehouses.remote.utils.logE
import io.treehouses.remote.utils.match
import kotlinx.android.synthetic.main.activity_network_fragment.*

class NetworkViewModel(application: Application) : FragmentViewModel(application)  {
    private val context = getApplication<MainApplication>().applicationContext

    private fun updateNetworkText(mode: String) {
        binding.currentNetworkMode.text = "Current Network Mode: $mode"
    }

    private fun showIpAddress(output: String) {
        var ip = output.substringAfter("ip: ").substringBefore(", has")
        if (ip == "") ip = "N/A"
        networkIP.text = "IP Address: " + ip
    }

    private fun rebootHelper() {
        try {
            listener.sendMessage(getString(R.string.REBOOT))
            Thread.sleep(1000)
            if (mChatService.state != Constants.STATE_CONNECTED) {
                Toast.makeText(context, "Bluetooth Disconnected: Reboot in progress", Toast.LENGTH_LONG).show()
                listener.openCallFragment(HomeFragment())
                requireActivity().title = "Home"
            } else {
                Toast.makeText(context, "Reboot Unsuccessful", Toast.LENGTH_LONG).show()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun performAction(output: String) {
        //Return from treehouses networkmode
        when (match(output)) {
            RESULTS.NETWORKMODE, RESULTS.DEFAULT_NETWORK -> updateNetworkText(output)
            RESULTS.NETWORKMODE_INFO ->  showIpAddress(output)
            RESULTS.DEFAULT_CONNECTED -> {

                val msg = getString(R.string.TREEHOUSES_NETWORKMODE)
                Toast.makeText(context, "Network Mode switched to default", Toast.LENGTH_LONG).show()
                //update network mode
                Utils.sendMessage(listener, Pair(msg, "Network Mode retrieved"), context, Toast.LENGTH_LONG)
            }
            RESULTS.ERROR -> {
                showDialog(context, "Error", output)
                binding.networkPbar.visibility = View.GONE
            }
            RESULTS.HOTSPOT_CONNECTED, RESULTS.WIFI_CONNECTED, RESULTS.BRIDGE_CONNECTED -> {
                val msg = getString(R.string.TREEHOUSES_NETWORKMODE)
                showDialog(context, "Network Switched", output)
                //update network mode
                Utils.sendMessage(listener, Pair(msg, "Network Mode retrieved"), context, Toast.LENGTH_LONG)
                binding.networkPbar.visibility = View.GONE
            }
            else -> logE("NewNetworkFragment: Result not Found")
        }
    }
}