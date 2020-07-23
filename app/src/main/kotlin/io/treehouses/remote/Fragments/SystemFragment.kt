package io.treehouses.remote.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.adapter.NetworkListAdapter
import io.treehouses.remote.adapter.ViewHolderTether
import io.treehouses.remote.adapter.ViewHolderVnc
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivitySystemFragmentBinding
import io.treehouses.remote.pojo.NetworkListItem
import java.util.*

class SystemFragment : BaseFragment() {
    private var network = true
    private var hostname = false
    private var tether = false
    private var retry = false
    private lateinit var bind: ActivitySystemFragmentBinding

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivitySystemFragmentBinding.inflate(inflater, container, false)
        val mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)
        val adapter = NetworkListAdapter(requireContext(), NetworkListItem.systemList, mChatService)
        adapter.setListener(listener)
        bind.listView.setOnGroupExpandListener { groupPosition: Int ->
            if (groupPosition == 1) {
                listener.sendMessage(getString(R.string.TREEHOUSES_NETWORKMODE_INFO))
                tether = true
                return@setOnGroupExpandListener
            } else if (groupPosition == 2) {
                Log.d("3", "onCreateView: ")
            }
            listener.sendMessage(getString(R.string.TREEHOUSES_NETWORKMODE_INFO))
        }
        bind.listView.setAdapter(adapter)
        Tutorials.systemTutorials(bind, requireActivity())
        return bind.root
    }

    override fun onResume() {
        super.onResume()
        listener.sendMessage("hostname -I\n")
        hostname = true
    }

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
            listener.sendMessage(getString(R.string.TREEHOUSES_NETWORKMODE_INFO))
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
                ViewHolderTether.editTextSSID.setText(element.substring(12).trim { it <= ' ' })
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
        }
    }

    override fun getMessage(msg: Message) {
        if (msg.what == Constants.MESSAGE_READ) {
            val readMessage = msg.obj.toString().trim { it <= ' ' }
            val diff = ArrayList<Long>()
            readMessageConditions(readMessage)
            Log.d("TAG", "readMessage = $readMessage")
            vncToast(readMessage)
            checkAndPrefilIp(readMessage, diff)
        }
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