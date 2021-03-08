package io.treehouses.remote.ui.system

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.widget.Toast
import io.treehouses.remote.MainApplication
import io.treehouses.remote.R
import io.treehouses.remote.adapter.ViewHolderTether
import io.treehouses.remote.adapter.ViewHolderVnc
import io.treehouses.remote.bases.FragmentViewModel
import io.treehouses.remote.utils.DialogUtils
import io.treehouses.remote.utils.logD
import java.util.*

class SystemViewModel (application: Application) : FragmentViewModel(application){

    private val context = getApplication<MainApplication>().applicationContext
    private var network = true
    private var hostname = false
    private var tether = false
    private var retry = false

    private fun checkAndPrefilIp(readMessage: String, diff: ArrayList<Long>) {
        if (readMessage.contains(".") && hostname && !readMessage.contains("essid")) {
            checkSubnet(readMessage, diff)
        }
        ipPrefil(readMessage)
    }

    private fun ipPrefil(readMessage: String) {
        if (network) {
            checkIfHotspot(readMessage)
        } else if (!retry) {
            Toast.makeText(context, "Warning: Your RPI may be in the wrong subnet", Toast.LENGTH_LONG).show()
            prefillIp(readMessage)
        }
    }

    private fun checkIfHotspot(readMessage: String) {
        if (readMessage.contains("ip") && !readMessage.contains("ap0")) {
            prefillIp(readMessage)
        } else if (readMessage.contains("ap0")) {
            prefillHotspot(readMessage)
        }
    }

    private fun checkSubnet(readMessage: String, diff: ArrayList<Long>) {
        hostname = false
        val wm = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val deviceIp = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
        val deviceIpAddress = ipToLong(deviceIp)
        if (!convertIp(readMessage, deviceIpAddress, diff)) {
            sendMessage(getString(R.string.TREEHOUSES_NETWORKMODE_INFO))
            retry = true
        } else {
            retry = false
            network = isInNetwork(diff)
        }
    }

    private fun checkElement(s: String): Boolean {
        return s.length <= 15 && !s.contains(":")
    }

    private fun convertIp(readMessage: String, deviceIpAddress: Long, diff: ArrayList<Long>): Boolean {
        val array = readMessage.split(" ".toRegex()).toTypedArray()
        for (element in array) {
            //TODO: Need to convert IPv6 addresses to long; currently it is being skipped
            var ip: Long = -1
            if (checkElement(element)) {
                ip = ipToLong(element)
                diff.add(deviceIpAddress - ip)
            }
            if (ip == -1L) {
                return false
            }
        }
        return true
    }

    private fun ipToLong(ipAddress: String): Long {
        val ipAddressInArray = ipAddress.split("[.]".toRegex()).toTypedArray()
        var result: Long = 0
        for (i in ipAddressInArray.indices) {
            val power = 3 - i
            result += try {
                val ip = ipAddressInArray[i].toInt()
                ip * Math.pow(256.0, power.toDouble()).toLong()
            } catch (e: NumberFormatException) {
                return -1
            }
        }
        return result
    }

    private fun isInNetwork(diff: ArrayList<Long>): Boolean {
        Collections.sort(diff)
        return diff[0] <= 256
    }

    private fun prefillIp(readMessage: String) {
        val array = readMessage.split(",".toRegex()).toTypedArray()
        for (element in array) {
            elementConditions(element)
        }
    }

    private fun prefillHotspot(readMessage: String) {
        val array = readMessage.split(",".toRegex()).toTypedArray()
        for (element in array) {
            elementConditions(element)
            if (element.contains("essid") && tether) {
                tether = false
                ViewHolderTether.editTextSSID?.setText(element.substring(12).trim { it <= ' ' })
            }
        }
    }

    private fun elementConditions(element: String) {
        if (element.contains("ip")) {
            try {
                ViewHolderVnc.editTextIp.setText(element.substring(4).trim { it <= ' ' })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun vncToast(readMessage: String) {
        if (readMessage.contains("Success: the vnc service has been started")) {
            Toast.makeText(context, "VNC enabled", Toast.LENGTH_LONG).show()
        } else if (readMessage.contains("Success: the vnc service has been stopped")) {
            Toast.makeText(context, "VNC disabled", Toast.LENGTH_LONG).show()
        } else if (readMessage.contains("system.")) {
            ViewHolderVnc.vnc.isChecked = false
        } else if (readMessage.contains("You can now")) {
            ViewHolderVnc.vnc.isChecked = true
        }
    }

    override fun onRead(output: String) {
        super.onRead(output)
            val readMessage = output.toString().trim { it <= ' ' }
            val diff = ArrayList<Long>()
            readMessageConditions(readMessage)
            logD("readMessage = $readMessage")
            notifyUser(readMessage)
            vncToast(readMessage)
            checkAndPrefilIp(readMessage, diff)

    }

    private fun notifyUser(msg: String) {
        if (msg.contains("Error: password must have at least 8 characters")) Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        else if (msg.contains("wifi is not connected")) DialogUtils.createAlertDialog(context, "Wifi is not connected", "Check SSID and password and try again.")
    }

    private fun readMessageConditions(readMessage: String) {
        if (readMessage.contains("true") || readMessage.contains("false")) {
            return
        }
        if (readMessage == "password network" || readMessage == "open wifi network") {
            Toast.makeText(context, "Connected", Toast.LENGTH_LONG).show()
        }
    }

}
