package io.treehouses.remote.Fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import io.treehouses.remote.Constants
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.callback.NotificationCallback

class StatusFragment : BaseFragment() {
    private var mView: View? = null
    private var wifiStatus: ImageView? = null
    private var btRPIName: ImageView? = null
    private var rpiType: ImageView? = null
    private var memoryStatus: ImageView? = null
    private var btStatus: ImageView? = null
    private var ivUpgrade: ImageView? = null
    private var tvStatus: TextView? = null
    private var tvStatus1: TextView? = null
    private var tvStatus2: TextView? = null
    private var tvStatus3: TextView? = null
    private var tvUpgrade: TextView? = null
    private var tvMemory: TextView? = null
    private var tvImage: TextView? = null
    private var upgrade: Button? = null
    private var pd: ProgressBar? = null
    private var updateRightNow = false
    private var cardRPIName: CardView? = null
    private var notificationListener: NotificationCallback? = null
    private var lastCommand = "hostname"
    private var deviceName = ""
    private var rpiVersion = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.activity_status_fragment, container, false)
        initializeUIElements(mView)
        mChatService = listener.chatService
        mChatService.updateHandler(mHandler)
        deviceName = mChatService.connectedDeviceName
        tvStatus!!.text = String.format("Bluetooth Connection: %s", deviceName)
        Log.e("STATUS", "device name: $deviceName")
        if (mChatService.state == Constants.STATE_CONNECTED) {
            btStatus!!.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.tick))
        }
        checkStatusNow()
        writeToRPI("hostname")
        return mView
    }

    private fun initializeUIElements(view: View?) {
        btStatus = view?.findViewById(R.id.btStatus)
        wifiStatus = view?.findViewById(R.id.wifiStatus)
        btRPIName = view?.findViewById(R.id.rpiName)
        rpiType = view?.findViewById(R.id.rpiType)
        pd = view?.findViewById(R.id.progressBar)
        memoryStatus = view?.findViewById(R.id.memoryStatus)
        ivUpgrade = view?.findViewById(R.id.upgradeCheck)
        tvStatus = view?.findViewById(R.id.tvStatus)
        tvStatus1 = view?.findViewById(R.id.tvStatus1)
        tvStatus2 = view?.findViewById(R.id.tvStatus2)
        tvStatus3 = view?.findViewById(R.id.tvStatus3)
        tvUpgrade = view?.findViewById(R.id.tvUpgradeCheck)
        tvMemory = view?.findViewById(R.id.tvMemoryStatus)
        tvImage = view?.findViewById(R.id.image_text)
        upgrade = view?.findViewById(R.id.upgrade)
        upgrade?.visibility = View.GONE
        cardRPIName = view?.findViewById(R.id.cardView)
        upgradeOnViewClickListener()
        rpiNameOnViewClickListener()
    }

    private fun upgradeOnViewClickListener() {
        upgrade!!.setOnClickListener {
            writeToRPI("treehouses upgrade")
            updateRightNow = true
            pd?.visibility = View.VISIBLE
        }
    }

    private fun rpiNameOnViewClickListener() {
        cardRPIName!!.setOnClickListener { showRenameDialog() }
    }

    private fun updateStatus(readMessage: String) {
        Log.d(TAG, "updateStatus: $lastCommand response $readMessage")
        if (lastCommand == "hostname") {
            Log.e("ENtERED", "YAY")
            setCard(tvStatus2, btRPIName, "Connected RPI Name: $readMessage")
            writeToRPI("treehouses remote status")
        } else if (readMessage.trim().split(" ").size == 5 && lastCommand == "treehouses remote status") {
            val res = readMessage.trim().split(" ")
            setCard(tvStatus1, wifiStatus, "RPI Wifi Connection : " + res[0])
            tvImage!!.text = String.format("Treehouses Image Version: %s", res[2])
            setCard(tvStatus3, rpiType, "RPI Type : " + res[4])
            rpiVersion = res[3]
            Log.e("REACHED", "YAYY")
            writeToRPI("treehouses memory free")
        } else if (lastCommand == "treehouses memory free") {
            setCard(tvMemory, memoryStatus, "Memory: " + readMessage + "bytes available")
            writeToRPI("treehouses internet")
        } else if (lastCommand == "treehouses internet") {
            checkWifiStatus(readMessage)
        } else {
            checkUpgradeStatus(readMessage)
        }
    }

    private fun checkWifiStatus(readMessage: String) {
        tvStatus1!!.text = String.format("RPI Wifi Connection: %s", readMessage)
        if (readMessage.startsWith("true")) {
            wifiStatus!!.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.tick))
            writeToRPI("treehouses upgrade --check")
        } else {
            wifiStatus!!.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.tick_png))
            tvUpgrade!!.text = "Upgrade Status: NO INTERNET"
            upgrade!!.visibility = View.GONE
        }
    }

    private fun writeToRPI(ping: String) {
        lastCommand = ping
        val pSend = ping.toByteArray()
        mChatService.write(pSend)
    }

    private fun setCard(textView: TextView?, tick: ImageView?, text: String) {
        textView!!.text = text
        tick!!.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.tick))
    }

    private fun checkUpgradeNow() {
        if (updateRightNow) {
            updateRightNow = false
            pd?.visibility = View.GONE
            Toast.makeText(context, "Treehouses Cli has been updated!!!", Toast.LENGTH_LONG).show()
            notificationListener!!.setNotification(false)
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container, StatusFragment()).commit()
        }
    }

    private fun checkUpgradeStatus(readMessage: String) {
        checkUpgradeNow()
        if (readMessage.startsWith("false ") && readMessage.length < 14) {
            ivUpgrade!!.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.tick))
            tvUpgrade!!.text = String.format("Upgrade Status: Latest Version: %s", rpiVersion)
            upgrade!!.visibility = View.GONE
        } else if (readMessage.startsWith("true ") && readMessage.length < 14) {
            ivUpgrade!!.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.tick_png))
            tvUpgrade!!.text = String.format("Upgrade available from %s to %s", rpiVersion, readMessage.substring(4))
            upgrade!!.visibility = View.VISIBLE
        }
    }

    private fun showRenameDialog() {
        val inflater = requireActivity().layoutInflater
        val mView = inflater.inflate(R.layout.dialog_rename_status, null)
        val mHostNameEditText = mView.findViewById<EditText>(R.id.hostname)
        mHostNameEditText.hint = "New Name"
        val alertDialog = createRenameDialog(mView, mHostNameEditText)
        alertDialog.show()
    }

    private fun createRenameDialog(view: View, mEditText: EditText): AlertDialog {
        return AlertDialog.Builder(activity)
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