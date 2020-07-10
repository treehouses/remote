package io.treehouses.remote.Fragments

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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.callback.NotificationCallback
import io.treehouses.remote.databinding.ActivityStatusFragmentBinding
import io.treehouses.remote.databinding.DialogRenameStatusBinding

class StatusFragment : BaseFragment() {

    private var updateRightNow = false
    private var notificationListener: NotificationCallback? = null
    private var lastCommand = "hostname"
    private var deviceName = ""
    private var rpiVersion = ""

    private lateinit var bind: ActivityStatusFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = ActivityStatusFragmentBinding.inflate(inflater, container, false)
        mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)
        deviceName = mChatService.connectedDeviceName
        checkStatusNow()
        writeToRPI("hostname")
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind.tvBluetooth.text = String.format("Bluetooth Connection: %s", deviceName)
        Log.e("STATUS", "device name: $deviceName")
        if (mChatService.state == Constants.STATE_CONNECTED) {
            bind.btStatus.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.tick))
        }
        upgradeOnViewClickListener()
        rpiNameOnViewClickListener()
        Tutorials.statusTutorials(bind, requireActivity())
    }

    private fun upgradeOnViewClickListener() {
        bind.upgrade.setOnClickListener {
            writeToRPI("treehouses upgrade")
            updateRightNow = true
            bind.progressBar.visibility = View.VISIBLE
        }
    }

    private fun rpiNameOnViewClickListener() {
        bind.rpiNameCard.setOnClickListener { showRenameDialog() }
    }

    private fun updateStatus(readMessage: String) {
        Log.d(TAG, "updateStatus: $lastCommand response $readMessage")
        if (lastCommand == "hostname") {
            setCard(bind.tvRpiName, bind.rpiName, "Connected RPI Name: $readMessage")
            writeToRPI("treehouses remote status")
        } else if (readMessage.trim().split(" ").size == 5 && lastCommand == "treehouses remote status") {
            val res = readMessage.trim().split(" ")
            setCard(bind.tvWifi, bind.wifiStatus, "RPI Wifi Connection : " + res[0])
            bind.imageText.text = String.format("Treehouses Image Version: %s", res[2])
            setCard(bind.tvRpiType, bind.rpiType, "RPI Type : " + res[4])
            rpiVersion = res[3]
            Log.e("REACHED", "YAYY")
            writeToRPI("treehouses memory free")
        } else if (lastCommand == "treehouses memory free") {
            setCard(bind.tvMemoryStatus, bind.memoryStatus, "Memory: " + readMessage + "bytes available")
            writeToRPI("treehouses temperature celsius")
        } else if(lastCommand == "treehouses temperature celsius"){
            setCard(bind.tvTemperature, bind.temperature, "Temperature: " + readMessage)
            writeToRPI("treehouses internet")
        } else if (lastCommand == "treehouses internet") {
            checkWifiStatus(readMessage)
        } else {
            checkUpgradeStatus(readMessage)
        }
    }

    private fun checkWifiStatus(readMessage: String) {
        bind.tvWifi.text = String.format("RPI Wifi Connection: %s", readMessage)
        if (readMessage.startsWith("true")) {
            bind.wifiStatus.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.tick))
            writeToRPI("treehouses upgrade --check")
        } else {
            bind.wifiStatus.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.tick_png))
            bind.tvUpgradeCheck.text = "Upgrade Status: NO INTERNET"
            bind.upgrade.visibility = View.GONE
        }
    }

    private fun writeToRPI(ping: String) {
        lastCommand = ping
        val pSend = ping.toByteArray()
        mChatService.write(pSend)
    }

    private fun setCard(textView: TextView, tick: ImageView?, text: String) {
        textView.text = text
        tick!!.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.tick))
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
            bind.tvUpgradeCheck.text = String.format("Upgrade Status: Latest Version: %s", rpiVersion)
            bind.upgrade.isEnabled = false
        } else if (readMessage.startsWith("true ") && readMessage.length < 14) {
            bind.upgradeCheck.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.tick_png))
            bind.tvUpgradeCheck.text = String.format("Upgrade available from %s to %s", rpiVersion, readMessage.substring(4))
            bind.upgrade.isEnabled = true
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
                        writeToRPI("treehouses rename " + mEditText.text.toString())
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
                    updateStatus(readMessage)
                }
            }
        }
    }

    companion object {
        private const val TAG = "StatusFragment"
    }
}