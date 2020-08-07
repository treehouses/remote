package io.treehouses.remote.Fragments

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import io.treehouses.remote.BuildConfig
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.callback.NotificationCallback
import io.treehouses.remote.databinding.ActivityStatusFragmentBinding
import io.treehouses.remote.databinding.DialogRenameStatusBinding
import io.treehouses.remote.pojo.StatusData
import kotlinx.android.synthetic.main.activity_status_fragment.*

class StatusFragment : BaseFragment() {

    private var updateRightNow = false
    private var notificationListener: NotificationCallback? = null
    private var lastCommand = ""
    private var deviceName = ""
    private var rpiVersion = ""
    private lateinit var bind: ActivityStatusFragmentBinding
    private lateinit var refreshLayout: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityStatusFragmentBinding.inflate(inflater, container, false)
        mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)
        deviceName = mChatService.connectedDeviceName
        checkStatusNow()
        refresh()
        bind.refreshBtn.setOnClickListener { refresh() }
        return bind.root
    }

    private fun refresh() {
        writeToRPI(requireActivity().getString(R.string.TREEHOUSES_REMOTE_STATUSPAGE))
        bind.refreshBtn.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addRefreshListener(view)
        bind.tvBluetooth.text = deviceName
        Log.e("STATUS", "device name: $deviceName")
        upgradeOnViewClickListener()
        rpiNameOnViewClickListener()
        Tutorials.statusTutorials(bind, requireActivity())
        bind.upgrade.visibility = View.GONE
    }

    private fun addRefreshListener(view: View) {
        refreshLayout = view.findViewById<SwipeRefreshLayout?>(R.id.swiperefresh)!!
        refreshLayout.setOnRefreshListener {
            refresh()
        }
        refreshLayout.setColorSchemeColors(
                ContextCompat.getColor(requireContext(), android.R.color.holo_red_light),
                ContextCompat.getColor(requireContext(), android.R.color.holo_orange_light),
                ContextCompat.getColor(requireContext(), android.R.color.holo_blue_light),
                ContextCompat.getColor(requireContext(), android.R.color.holo_green_light))
    }

    private fun upgradeOnViewClickListener() {
        bind.upgrade.setOnClickListener {
            writeToRPI(requireActivity().getString(R.string.TREEHOUSES_UPGRADE))
            updateRightNow = true
            bind.progressBar.visibility = View.VISIBLE
            bind.upgrade.visibility = View.GONE
        }
    }

    private fun rpiNameOnViewClickListener() {
        bind.editName.setOnClickListener {showRenameDialog()}
    }

    private fun updateStatus(readMessage: String) {
        Log.d(TAG, "updateStatus: $lastCommand response $readMessage")

        if(lastCommand == requireActivity().getString(R.string.TREEHOUSES_REMOTE_STATUSPAGE)){
            val statusData = Gson().fromJson(readMessage, StatusData::class.java)

            bind.temperature.text = statusData.temperature + "°C"
            ObjectAnimator.ofInt(bind.temperatureBar, "progress", (statusData.temperature.toFloat() / 80 * 100).toInt()).setDuration(600).start()

            val usedMemory = statusData.memory_used.trim { it <= ' ' }.toDouble()
            val totalMemory = statusData.memory_total.trim { it <= ' ' }.toDouble()

            ObjectAnimator.ofInt(bind.memoryBar, "progress", (usedMemory/totalMemory*100).toInt()).setDuration(600).start()
            bind.memory.text = usedMemory.toString() + "/" + totalMemory.toString() + " GB"

            bind.cpuModelText.text = "CPU: ARM " + statusData.arm

            writeNetworkInfo(statusData.networkmode, statusData.info)

            bind.tvRpiName.text = "Hostname: " + statusData.hostname

            val res = statusData.status.trim().split(" ")

            bind.imageText.text = String.format("Image Version: %s", res[2].substring(8))
            bind.deviceAddress.text = res[1]
            bind.tvRpiType.text = "Model: " + res[4]
            rpiVersion = res[3]

            bind.remoteVersionText.text = "Remote Version: " + BuildConfig.VERSION_NAME

            checkWifiStatus(statusData.internet)

            bind.refreshBtn.visibility = View.VISIBLE
            refreshLayout.isRefreshing = false
        } else {
            checkUpgradeStatus(readMessage)
        }
    }


    private fun writeNetworkInfo(networkMode:String, readMessage: String) {
        val ssid = readMessage.substringAfter("essid: ").substringBefore(", ip:")
        var ip = readMessage.substringAfter("ip: ").substringBefore(", has")
        when(networkMode){
            "default" -> networkModeTitle.text = "Default"
            "wifi" -> networkModeTitle.text = "WiFi"
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

    private fun checkWifiStatus(readMessage: String) {
        if (readMessage.startsWith("true")) {
            writeToRPI(requireActivity().getString(R.string.TREEHOUSES_UPGRADE_CHECK))
        } else {
            bind.tvUpgradeCheck.text = "      NO INTERNET"
            bind.upgrade.visibility = View.GONE
        }
    }

    private fun writeToRPI(ping: String) {
        lastCommand = ping
        val pSend = ping.toByteArray()
        mChatService.write(pSend)
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

    private fun checkUpgradeStatus(readMessage: String) {
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

    private fun showRenameDialog() {
        val inflater = requireActivity().layoutInflater
        val dialogBinding = DialogRenameStatusBinding.inflate(inflater)
        dialogBinding.hostname.hint = "New Name"
        val alertDialog = createRenameDialog(dialogBinding.root, dialogBinding.hostname)
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

    private fun createRenameDialog(view: View, mEditText: EditText): AlertDialog {
        return AlertDialog.Builder(ContextThemeWrapper(activity, R.style.CustomAlertDialogStyle))
                .setView(view).setTitle("Rename " + deviceName.substring(0, deviceName.indexOf("-"))).setIcon(R.drawable.dialog_icon)
                .setPositiveButton("Rename"
                ) { _: DialogInterface?, _: Int ->
                    if (mEditText.text.toString() != "") {
                        writeToRPI(requireActivity().getString(R.string.TREEHOUSES_RENAME, mEditText.text.toString()))
                        Toast.makeText(context, "Raspberry Pi Renamed", Toast.LENGTH_LONG).show()
                        refresh()
                    } else {
                        Toast.makeText(context, "Please enter a new name", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                .create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        notificationListener = try {
            getContext() as NotificationCallback?
        } catch (e: ClassCastException) {
            throw ClassCastException("Activity must implement NotificationListener")
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    override fun getMessage(msg: Message) {
        when (msg.what) {
            Constants.MESSAGE_STATE_CHANGE -> checkStatusNow()
            Constants.MESSAGE_WRITE -> {
                val writeBuf = msg.obj as ByteArray
                val writeMessage = String(writeBuf)
                Log.d(TAG, "writeMessage = $writeMessage")
            }
            Constants.MESSAGE_READ -> {
                val readMessage = msg.obj as String
                Log.d(TAG, "readMessage = $readMessage")
                try {
                    updateStatus(readMessage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        private const val TAG = "StatusFragment"
    }
}