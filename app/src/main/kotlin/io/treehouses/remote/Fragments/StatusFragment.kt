package io.treehouses.remote.Fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import io.treehouses.remote.BuildConfig
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.callback.NotificationCallback
import io.treehouses.remote.databinding.ActivityStatusFragmentBinding
import io.treehouses.remote.databinding.DialogRenameStatusBinding
import kotlinx.android.synthetic.main.activity_status_fragment.*

class StatusFragment : BaseFragment() {

    private var updateRightNow = false
    private var notificationListener: NotificationCallback? = null
    private var lastCommand = ""
    private var deviceName = ""
    private var rpiVersion = ""
    private var usedMemory = 0.0
    private var totalMemory = 0.0
    private var networkMode = ""
    private lateinit var bind: ActivityStatusFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityStatusFragmentBinding.inflate(inflater, container, false)
        mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)
        deviceName = mChatService.connectedDeviceName
        checkStatusNow()
        writeToRPI(requireActivity().getString(R.string.TREEHOUSES_REMOTE_STATUS))

        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.tvBluetooth.text = deviceName
        Log.e("STATUS", "device name: $deviceName")
        upgradeOnViewClickListener()
        rpiNameOnViewClickListener()
        Tutorials.statusTutorials(bind, requireActivity())
        bind.upgrade.visibility = View.GONE
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
        bind.editName.setOnClickListener { showRenameDialog() }
    }

    private fun updateStatus(readMessage: String) {
        Log.d(TAG, "updateStatus: $lastCommand response $readMessage")
        if (readMessage.trim().split(" ").size == 5 && lastCommand == requireActivity().getString(R.string.TREEHOUSES_REMOTE_STATUS)) {
            val res = readMessage.trim().split(" ")
            bind.imageText.text = String.format("Image Version: %s", res[2].substring(8))
            bind.deviceAddress.text = res[1]
            bind.tvRpiType.text = "Model: " + res[4]
            rpiVersion = res[3]
            //also set remote version
            bind.remoteVersionText.text = "Remote Version: " + BuildConfig.VERSION_NAME
            Log.e("REACHED", "YAYY")
            writeToRPI(requireActivity().getString(R.string.HOSTNAME))
        } else if (lastCommand == requireActivity().getString(R.string.HOSTNAME)){
            bind.tvRpiName.text = "Hostname: " + readMessage
            writeToRPI(requireActivity().getString(R.string.TREEHOUSES_MEMORY_USED_GB))
        } else if (lastCommand == requireActivity().getString(R.string.TREEHOUSES_MEMORY_USED_GB)) {
            usedMemory = readMessage.trim { it <= ' ' }.toDouble()
            writeToRPI(requireActivity().getString(R.string.TREEHOUSES_MEMORY_TOTAL_GB))
        } else if (lastCommand == requireActivity().getString(R.string.TREEHOUSES_MEMORY_TOTAL_GB)) {
            totalMemory = readMessage.trim { it <= ' ' }.toDouble()
            ObjectAnimator.ofInt(bind.memoryBar, "progress", (usedMemory/totalMemory*100).toInt()).setDuration(600).start()
            bind.memory.text = usedMemory.toString() + "/" + totalMemory.toString() + " GB"
            writeToRPI(requireActivity().getString(R.string.TREEHOUSES_TEMPERATURE_CELSIUS))
        } else if (lastCommand == requireActivity().getString(R.string.TREEHOUSES_TEMPERATURE_CELSIUS)) {
            bind.temperature.text = readMessage
            ObjectAnimator.ofInt(bind.temperatureBar, "progress", (readMessage.dropLast(3).toFloat() / 80 * 100).toInt()).setDuration(600).start()
            writeToRPI(requireActivity().getString(R.string.TREEHOUSES_DETECT_ARM))
        } else if (lastCommand == requireActivity().getString(R.string.TREEHOUSES_DETECT_ARM)) {
            bind.cpuModelText.text = "CPU: ARM " + readMessage
            writeToRPI(requireActivity().getString(R.string.TREEHOUSES_NETWORKMODE))
        } else if (lastCommand == requireActivity().getString(R.string.TREEHOUSES_NETWORKMODE)) {
            networkMode = readMessage.dropLast(1)
            writeToRPI(requireActivity().getString(R.string.TREEHOUSES_NETWORKMODE_INFO))
        } else if (lastCommand == requireActivity().getString(R.string.TREEHOUSES_NETWORKMODE_INFO)) {
            writeNetworkInfo(readMessage)
            writeToRPI(requireActivity().getString(R.string.TREEHOUSES_INTERNET))
        } else if (lastCommand == requireActivity().getString(R.string.TREEHOUSES_INTERNET)) {
            checkWifiStatus(readMessage)
        } else {
            checkUpgradeStatus(readMessage)
        }
    }

    private fun writeNetworkInfo(readMessage: String) {
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
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container, StatusFragment()).commit()
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
    val mHandler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
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
    }

    companion object {
        private const val TAG = "StatusFragment"
    }
}