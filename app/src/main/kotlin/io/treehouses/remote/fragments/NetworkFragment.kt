package io.treehouses.remote.fragments

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.treehouses.remote.Constants
import io.treehouses.remote.fragments.dialogfragments.bottomsheetdialogs.BridgeBottomSheet
import io.treehouses.remote.fragments.dialogfragments.bottomsheetdialogs.EthernetBottomSheet
import io.treehouses.remote.fragments.dialogfragments.bottomsheetdialogs.HotspotBottomSheet
import io.treehouses.remote.fragments.dialogfragments.bottomsheetdialogs.WifiBottomSheet
import io.treehouses.remote.fragments.dialogfragments.WifiDialogFragment
import io.treehouses.remote.interfaces.FragmentDialogInterface
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityNetworkFragmentBinding
import io.treehouses.remote.ui.home.HomeFragment
import io.treehouses.remote.utils.*

class NetworkFragment : BaseFragment(), View.OnClickListener, FragmentDialogInterface {
    private lateinit var binding: ActivityNetworkFragmentBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val msg = getString(R.string.TREEHOUSES_NETWORKMODE)
        val toastMsg = "Network Mode retrieved"
        binding = ActivityNetworkFragmentBinding.inflate(inflater, container, false)
        mChatService = listener.getChatService()
        mChatService.updateHandler(mHandler)

        //update Network mode
        Utils.sendMessage(listener, Pair(msg, toastMsg), context, Toast.LENGTH_LONG)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Listeners
        binding.networkWifi.setOnClickListener(this)
        binding.networkHotspot.setOnClickListener(this)
        binding.networkBridge.setOnClickListener(this)
        binding.networkEthernet.setOnClickListener(this)
        binding.buttonNetworkMode.setOnClickListener(this)
        binding.rebootRaspberry.setOnClickListener(this)
        binding.resetNetwork.setOnClickListener(this)
        binding.discoverBtn.setOnClickListener(this)
        Tutorials.networkTutorials(binding, requireActivity())
    }

    private fun showBottomSheet(fragment: BottomSheetDialogFragment, tag: String) {
        fragment.setTargetFragment(this@NetworkFragment, Constants.NETWORK_BOTTOM_SHEET)
        fragment.show(requireActivity().supportFragmentManager, tag)
    }

    override fun onClick(v: View) {
        val msg = getString(R.string.TREEHOUSES_NETWORKMODE)
        val toastMsg = "Network Mode retrieved"
        when {
            binding.networkWifi == v -> showBottomSheet(WifiBottomSheet(), "wifi")
            binding.networkHotspot == v -> showBottomSheet(HotspotBottomSheet(), "hotspot")
            binding.networkBridge == v -> showBottomSheet(BridgeBottomSheet(), "bridge")
            binding.networkEthernet == v -> showBottomSheet(EthernetBottomSheet(), "ethernet")
            binding.buttonNetworkMode == v -> Utils.sendMessage(listener, Pair(msg, toastMsg), context, Toast.LENGTH_LONG)
            binding.rebootRaspberry == v -> reboot()
            binding.resetNetwork == v -> resetNetwork()
            binding.discoverBtn == v-> listener.openCallFragment(DiscoverFragment())
        }
    }

    private fun updateNetworkText(mode: String) {
        binding.currentNetworkMode.text = "Current Network Mode: $mode"
    }

    private fun performAction(output: String) {
        //Return from treehouses networkmode
        when (match(output)) {
            RESULTS.NETWORKMODE, RESULTS.DEFAULT_NETWORK -> updateNetworkText(output)

            RESULTS.DEFAULT_CONNECTED -> {

                val msg = getString(R.string.TREEHOUSES_NETWORKMODE)
                val toastMsg = "Network Mode retrieved"
                Toast.makeText(context, "Network Mode switched to default", Toast.LENGTH_LONG).show()
                //update network mode
                Utils.sendMessage(listener, Pair(msg, toastMsg), context, Toast.LENGTH_LONG)
            }
            RESULTS.ERROR -> {
                showDialog(context,"Error", output)
                binding.networkPbar.visibility = View.GONE
            }
            RESULTS.HOTSPOT_CONNECTED, RESULTS.WIFI_CONNECTED, RESULTS.BRIDGE_CONNECTED -> {
                val msg = getString(R.string.TREEHOUSES_NETWORKMODE)
                val toastMsg = "Network Mode retrieved"
                showDialog(context,"Network Switched", output)
                showDialog(context,"Network Switched", output)
                //update network mode
                Utils.sendMessage(listener, Pair(msg, toastMsg), context, Toast.LENGTH_LONG)
                binding.networkPbar.visibility = View.GONE
            }
            else -> logE("NewNetworkFragment: Result not Found")
        }
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

    private fun reboot() {
        val a = createAlertDialog(context, R.style.CustomAlertDialogStyle, "Reboot",
                "Are you sure you want to reboot your device?")
                .setPositiveButton("Yes") { dialog: DialogInterface, _: Int ->
                    rebootHelper()
                    dialog.dismiss()
                }.setNegativeButton("No") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
        a.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        a.show()
    }

    private fun resetNetwork() {
        val a = createAlertDialog(context, R.style.CustomAlertDialogStyle, "Reset Network",
                "Are you sure you want to reset the network to default?")
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    val msg = getString(R.string.TREEHOUSES_DEFAULT_NETWORK)
                    val toastMsg = "Switching to default network..."
                    Utils.sendMessage(listener, Pair(msg, toastMsg), context, Toast.LENGTH_LONG)
                }.setNegativeButton("No") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
        a.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        a.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == Constants.NETWORK_BOTTOM_SHEET && data!!.getBooleanExtra(CLICKED_START_CONFIG, false)) {
            binding.networkPbar.visibility = View.VISIBLE
        }
    }

    override fun getMessage(msg: Message) {
        if (msg.what == Constants.MESSAGE_READ) {
            val readMessage = msg.obj as String
            logD("readMessage = $readMessage")
            performAction(readMessage)
        }
    }

    companion object {
        @JvmField
        var CLICKED_START_CONFIG = "clicked_config"
        @JvmStatic
        fun openWifiDialog(bottomSheetDialogFragment: BottomSheetDialogFragment, context: Context?) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                Toast.makeText(context, "Wifi scan requires at least android API 23", Toast.LENGTH_LONG).show()
            } else {
                val dialogFrag = WifiDialogFragment.newInstance()
                dialogFrag.setTargetFragment(bottomSheetDialogFragment, Constants.REQUEST_DIALOG_WIFI)
                dialogFrag.show(bottomSheetDialogFragment.requireActivity().supportFragmentManager.beginTransaction(), "wifiDialog")
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