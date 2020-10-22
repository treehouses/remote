package io.treehouses.remote.bases

import android.animation.ObjectAnimator
import android.app.Dialog
import android.text.TextUtils
import android.view.View
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat
import io.treehouses.remote.BuildConfig
import io.treehouses.remote.R
import io.treehouses.remote.callback.NotificationCallback
import io.treehouses.remote.databinding.ActivityStatusFragmentBinding
import io.treehouses.remote.pojo.StatusData
import kotlinx.android.synthetic.main.activity_status_fragment.*
import kotlinx.android.synthetic.main.dialog_wificountry.*
import java.util.*

open class BaseStatusFragment : BaseFragment() {
    var countryList: ListView? = null
    lateinit var bind: ActivityStatusFragmentBinding
    var rpiVersion = ""
    var updateRightNow = false
    var notificationListener: NotificationCallback? = null

    fun getCountryName(country: String): String {
        val l = Locale("", country)
        val countryName = l.displayCountry
        return "$countryName ( $country )"
    }


    fun searchView(dialog: Dialog){
        val searchView = dialog.search_bar
        searchView.isIconifiedByDefault = false
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                if (TextUtils.isEmpty(newText)) {
                    countryList!!.clearTextFilter()
                } else {
                    countryList!!.setFilterText(newText)
                }
                return true
            }
        })
    }

    fun receiveMessage(readMessage: String, statInt : String){
        if (readMessage.startsWith("country=") || readMessage.contains("set to")) {
            val len = readMessage.length - 3
            val country = readMessage.substring(len).trim { it <= ' ' }
            bind.countryDisplay.setText(getCountryName(country))
            bind.countryDisplay.isEnabled = true
            checkWifiStatus(statInt)
        } else if (readMessage.contains("Error when")) {
            bind.countryDisplay.setText("Try again")
            bind.countryDisplay.isEnabled = true
            Toast.makeText(requireContext(), "Error when changing country", Toast.LENGTH_LONG).show()
        } else {
            try {
                updateStatus(readMessage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    open fun updateStatus(readMessage: String) {}

    fun updateStatusPage(statusData: StatusData) {
        val res = statusData.status.trim().split(" ")

        bind.imageText.text = String.format("Image Version: %s", res[2].substring(8))
        bind.deviceAddress.text = res[1]
        bind.tvRpiType.text = "Model: " + res[4]
        rpiVersion = res[3]

        bind.remoteVersionText.text = "Remote Version: " + BuildConfig.VERSION_NAME

   //     checkWifiStatus(statusData.internet)

        bind.refreshBtn.visibility = View.VISIBLE
        bind.swiperefresh.isRefreshing = false
        writeToRPI(requireActivity().getString(R.string.TREEHOUSES_WIFI_COUNTRY_CHECK))

    }

    open fun checkWifiStatus(readMessage: String) {}

    fun writeNetworkInfo(networkMode:String, readMessage: String) {
        val ssid = readMessage.substringAfter("essid: ").substringBefore(", ip:")
        var ip = readMessage.substringAfter("ip: ").substringBefore(", has")
        when(networkMode){
            "default" -> networkModeTitle.text = "Default"
            "wifi" -> {
                networkModeTitle.text = "WiFi"
                bind.countryDisplay.visibility = View.VISIBLE
            }
            "hotspot" -> networkModeTitle.text = "Hotspot"
            "bridge" -> networkModeTitle.text = "Bridge"
            "ethernet" -> networkModeTitle.text = "Ethernet"
        }
        if(ip == "") {
            ip = "N/A"
        }
        ipAdrText.text = "IP Address: " + ip
        ssidText.text = "SSID: " + ssid
    }

    open fun writeToRPI(ping: String) {}

    fun checkUpgradeStatus(readMessage: String) {
        checkUpgradeNow()
        if (readMessage.startsWith("false ") && readMessage.length < 14) {
            bind.upgradeCheck.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.tick))
            bind.tvUpgradeCheck.text = String.format("Latest Version: %s", rpiVersion)
            bind.upgrade.visibility = View.GONE
        } else if (readMessage.startsWith("true ") && readMessage.length < 14) {
            bind.upgradeCheck.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.tick_png))
            bind.tvUpgradeCheck.text = String.format("Upgrade available from %s to %s", rpiVersion, readMessage.substring(4))
            bind.upgrade.visibility = View.VISIBLE
        }
    }

    private fun checkUpgradeNow() {
        if (updateRightNow) {
            updateRightNow = false
            bind.progressBar.visibility = View.GONE
            Toast.makeText(context, "Treehouses Cli has been updated!!!", Toast.LENGTH_LONG).show()
            notificationListener!!.setNotification(false)
            refresh()
        }
    }

    fun refresh() {
        setChecking()
        writeToRPI(requireActivity().getString(R.string.TREEHOUSES_REMOTE_STATUSPAGE))
        bind.refreshBtn.visibility = View.GONE
        bind.countryDisplay.visibility = View.GONE
    }


    fun setChecking() {
        bind.upgrade.visibility = View.GONE
        bind.deviceAddress.text = "dc.."
        bind.networkModeTitle.text = "Checking Network Mode....."
        bind.ipAdrText.text = "IP Address: Checking....."
        bind.ssidText.text = "SSID: Checking....."
        bind.tvRpiName.text = "Hostname: ⏳"
        bind.tvRpiType.text = "Model: ⏳"
        bind.cpuModelText.text = "CPU: ⏳"
        bind.imageText.text = "Image Version: ⏳"
        bind.remoteVersionText.text = "Remote Version: ⏳"
        bind.tvUpgradeCheck.text = "Checking Version..."
        bind.temperature.text = "Checking......"
        bind.memory.text = "Checking......"
        bind.storage.text = "Checking......"
        bind.upgradeCheck.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.tick_png))
        ObjectAnimator.ofInt(bind.memoryBar, "progress", 0).setDuration(600).start()
        ObjectAnimator.ofInt(bind.storageBar, "progress", 0).setDuration(600).start()
        ObjectAnimator.ofInt(bind.temperatureBar, "progress", 0).setDuration(600).start()
    }
}