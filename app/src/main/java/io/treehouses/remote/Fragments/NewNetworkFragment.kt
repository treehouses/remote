package io.treehouses.remote.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.treehouses.remote.Constants
import io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs.BridgeBottomSheet
import io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs.EthernetBottomSheet
import io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs.HotspotBottomSheet
import io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs.WifiBottomSheet
import io.treehouses.remote.Fragments.DialogFragments.WifiDialogFragment
import io.treehouses.remote.Network.BluetoothChatService
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseFragment

class NewNetworkFragment : BaseFragment(), View.OnClickListener {
    private var wifiButton: Button? = null
    private var hotspotButton: Button? = null
    private var bridgeButton: Button? = null
    private var ethernetButton: Button? = null
    private var updateNetwork: Button? = null
    private var rebootPi: Button? = null
    private var resetNetwork: Button? = null
    private var currentNetworkMode: TextView? = null
    private override val mChatService: BluetoothChatService? = null
    private var progressBar: ProgressBar? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.new_network, container, false)
        mChatService = listener.chatService
        mChatService!!.updateHandler(mHandler)

        //Main Buttons
        wifiButton = view.findViewById(R.id.network_wifi)
        hotspotButton = view.findViewById(R.id.network_hotspot)
        bridgeButton = view.findViewById(R.id.network_bridge)
        ethernetButton = view.findViewById(R.id.network_ethernet)

        //Commands
        updateNetwork = view.findViewById(R.id.button_network_mode)
        rebootPi = view.findViewById(R.id.reboot_raspberry)
        resetNetwork = view.findViewById(R.id.reset_network)
        currentNetworkMode = view.findViewById(R.id.current_network_mode)
        progressBar = view.findViewById(R.id.network_pbar)

        //Listeners
        wifiButton?.setOnClickListener(this)
        hotspotButton?.setOnClickListener(this)
        bridgeButton?.setOnClickListener(this)
        ethernetButton?.setOnClickListener(this)
        updateNetwork?.setOnClickListener(this)
        rebootPi?.setOnClickListener(this)
        resetNetwork?.setOnClickListener(this)
        updateNetworkMode()
        return view
    }

    private fun showBottomSheet(fragment: BottomSheetDialogFragment, tag: String) {
        fragment.setTargetFragment(this@NewNetworkFragment, Constants.NETWORK_BOTTOM_SHEET)
        fragment.show(fragmentManager!!, tag)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.network_wifi -> showBottomSheet(WifiBottomSheet(), "wifi")
            R.id.network_hotspot -> showBottomSheet(HotspotBottomSheet(), "hotspot")
            R.id.network_bridge -> showBottomSheet(BridgeBottomSheet(), "bridge")
            R.id.network_ethernet -> showBottomSheet(EthernetBottomSheet(), "ethernet")
            R.id.button_network_mode -> updateNetworkMode()
            R.id.reboot_raspberry -> reboot()
            R.id.reset_network -> resetNetwork()
        }
    }

    private fun updateNetworkText(mode: String) {
        currentNetworkMode!!.text = "Current Network Mode: $mode"
    }

    private fun updateNetworkMode() {
        val s = "treehouses networkmode\n"
        mChatService!!.write(s.toByteArray())
        Toast.makeText(context, "Network Mode updated", Toast.LENGTH_LONG).show()
    }

    private fun isNetworkModeReturned(output: String): Boolean {
        return when (output.trim { it <= ' ' }) {
            "wifi", "bridge", "ap local", "ap internet", "static wifi", "static ethernet", "default" -> true
            else -> false
        }
    }

    private fun isConfigReturned(output: String): Boolean {
        return if (output.contains("pirateship has anchored successfully") || output.contains("the bridge has been built")) {
            true
        } else output.contains("open wifi network") || output.contains("password network")
    }

    private fun performAction(output: String) {
        //Return from treehouses networkmode
        if (isNetworkModeReturned(output)) {
            updateNetworkText(output)
        } else if (output.startsWith("Success: the network mode has")) {
            Toast.makeText(context, "Network Mode switched to default", Toast.LENGTH_LONG).show()
            updateNetworkMode()
        } else if (output.toLowerCase().contains("error")) {
            showDialog("Error", output)
            progressBar!!.visibility = View.GONE
        } else if (isConfigReturned(output)) {
            showDialog("Network Switched", output)
            updateNetworkMode()
            progressBar!!.visibility = View.GONE
        }
    }

    private fun showDialog(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(context).setTitle(title).setMessage(message)
                .setPositiveButton("OK") { dialog: DialogInterface, which: Int -> dialog.dismiss() }.create()
        alertDialog.show()
    }

    private fun rebootHelper() {
        try {
            listener.sendMessage("reboot")
            Thread.sleep(1000)
            if (mChatService!!.state != Constants.STATE_CONNECTED) {
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

    private fun reboot() {
        val a = AlertDialog.Builder(context)
                .setTitle("Reboot")
                .setMessage("Are you sure you want to reboot your device?")
                .setPositiveButton("Yes") { dialog: DialogInterface, which: Int ->
                    rebootHelper()
                    dialog.dismiss()
                }.setNegativeButton("No") { dialog: DialogInterface, which: Int -> dialog.dismiss() }.create()
        a.show()
    }

    private fun resetNetwork() {
        val a = AlertDialog.Builder(context)
                .setTitle("Reset Network")
                .setMessage("Are you sure you want to reset the network to default?")
                .setPositiveButton("Yes") { dialog: DialogInterface?, which: Int ->
                    listener.sendMessage("treehouses default network")
                    Toast.makeText(context, "Switching to default network...", Toast.LENGTH_LONG).show()
                }.setNegativeButton("No") { dialog: DialogInterface, which: Int -> dialog.dismiss() }.create()
        a.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == Constants.NETWORK_BOTTOM_SHEET && data!!.getBooleanExtra(CLICKED_START_CONFIG, false)) {
            progressBar!!.visibility = View.VISIBLE
        }
    }

    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == Constants.MESSAGE_READ) {
                val readMessage = msg.obj as String
                Log.d("TAG", "readMessage = $readMessage")
                performAction(readMessage)
            }
        }
    }

    companion object {
        var CLICKED_START_CONFIG = "clicked_config"
        fun openWifiDialog(bottomSheetDialogFragment: BottomSheetDialogFragment, context: Context?) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                Toast.makeText(context, "Wifi scan requires at least android API 23", Toast.LENGTH_LONG).show()
            } else {
                val dialogFrag: DialogFragment = WifiDialogFragment.newInstance()
                dialogFrag.setTargetFragment(bottomSheetDialogFragment, Constants.REQUEST_DIALOG_WIFI)
                dialogFrag.show(bottomSheetDialogFragment.activity!!.supportFragmentManager.beginTransaction(), "wifiDialog")
            }
        } //    Next Version:
        //    private void elementConditions(String element) {
        //        Log.e("TAG", "networkmode= " + element);
        //        if (element.contains("wlan0") && !element.contains("ap essid")) {                   // bridge essid
        //            setSSIDText(element.substring(14).trim());
        //        } else if (element.contains("ap essid")) {                                          // ap essid
        //            setSSIDText(element.substring(16).trim());
        //        } else if (element.contains("ap0")) {                                               // hotspot essid for bridge
        //            ButtonConfiguration.getEtHotspotEssid().setText(element.substring(11).trim());
        //        } else if (element.contains("essid")) {                                             // wifi ssid
        //            setSSIDText(element.substring(6).trim());
        //        }
        //    }
    }
}