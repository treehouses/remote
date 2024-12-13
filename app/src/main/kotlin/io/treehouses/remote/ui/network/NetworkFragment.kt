package io.treehouses.remote.ui.network

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.treehouses.remote.Constants
import io.treehouses.remote.R
import io.treehouses.remote.Tutorials
import io.treehouses.remote.bases.BaseFragment
import io.treehouses.remote.databinding.ActivityNetworkFragmentBinding
import io.treehouses.remote.databinding.DialogSpeedtestBinding
import io.treehouses.remote.fragments.DiscoverFragment
import io.treehouses.remote.fragments.dialogfragments.WifiDialogFragment
import io.treehouses.remote.interfaces.FragmentDialogInterface
import io.treehouses.remote.ui.home.HomeFragment
import io.treehouses.remote.ui.network.bottomsheetdialogs.BridgeBottomSheet
import io.treehouses.remote.ui.network.bottomsheetdialogs.EthernetBottomSheet
import io.treehouses.remote.ui.network.bottomsheetdialogs.HotspotBottomSheet
import io.treehouses.remote.ui.network.bottomsheetdialogs.WifiBottomSheet
import io.treehouses.remote.utils.Utils

open class NetworkFragment : BaseFragment(), View.OnClickListener, FragmentDialogInterface {
    private lateinit var binding: ActivityNetworkFragmentBinding
    private lateinit var speedDialog: Dialog
    private lateinit var speedDialogDismiss: Button
    private lateinit var speedDialogTest: Button
    private var speedDialogCheck: Boolean = false
    protected val viewModel: NetworkViewModel by viewModels(ownerProducer = { this })
    private lateinit var dialogSpeedtestBinding: DialogSpeedtestBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ActivityNetworkFragmentBinding.inflate(inflater, container, false)
        loadObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeSpeedDialog()
        //Listeners
        binding.networkWifi.setOnClickListener(this)
        binding.networkHotspot.setOnClickListener(this)
        binding.networkBridge.setOnClickListener(this)
        binding.networkEthernet.setOnClickListener(this)
        binding.buttonNetworkMode.setOnClickListener(this)
        binding.rebootRaspberry.setOnClickListener(this)
        binding.resetNetwork.setOnClickListener(this)
        binding.reverseLookup.setOnClickListener(this)
        binding.discoverBtn.setOnClickListener(this)
        binding.speedTest.setOnClickListener(this)
        Tutorials.networkTutorials(binding, requireActivity())
        viewModel.onLoad()
    }

    fun loadObservers() {
        viewModel.networkMode.observe(viewLifecycleOwner) {
            binding.currentNetworkMode.text = it
        }
        viewModel.ipAddress.observe(viewLifecycleOwner) {
            binding.networkIP.text = it
        }
        viewModel.showNetworkProgress.observe(viewLifecycleOwner) {
            binding.networkPbar.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.showHome.observe(viewLifecycleOwner) {
            if (it) {
                listener.openCallFragment(HomeFragment())
            }
        }
    }

    private fun showBottomSheet(fragment: BottomSheetDialogFragment, tag: String) {
        fragment.show(childFragmentManager, tag)
    }

    override fun onClick(v: View) {

        when {
            binding.networkWifi == v -> showBottomSheet(WifiBottomSheet(), "wifi")
            binding.networkHotspot == v -> showBottomSheet(HotspotBottomSheet(), "hotspot")
            binding.networkBridge == v -> showBottomSheet(BridgeBottomSheet(), "bridge")
            binding.networkEthernet == v -> showBottomSheet(EthernetBottomSheet(), "ethernet")
            binding.buttonNetworkMode == v -> viewModel.getNetworkMode()
            binding.rebootRaspberry == v -> reboot()
            binding.resetNetwork == v -> resetNetwork()
            binding.speedTest == v -> speedTest()
            binding.reverseLookup == v -> reverseLookup()
            binding.discoverBtn == v -> listener.openCallFragment(DiscoverFragment())
            dialogSpeedtestBinding.speedBtnTest == v -> viewModel.treehousesInternet()
            dialogSpeedtestBinding.speedBtnDismiss == v -> speedDialog.dismiss()
        }
    }

    private fun reboot() {
        val a = createAlertDialog(context, R.style.CustomAlertDialogStyle, "Reboot",
                "Are you sure you want to reboot your device?")
                .setPositiveButton("Yes") { dialog: DialogInterface, _: Int ->
                    viewModel.rebootHelper()
                    dialog.dismiss()
                }.setNegativeButton("No") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
        a.window?.setBackgroundDrawableResource(android.R.color.transparent)
        a.show()
    }

    private fun resetNetwork() {
        val a = createAlertDialog(context, R.style.CustomAlertDialogStyle, "Reset Network",
                "Are you sure you want to reset the network to default?")
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    viewModel.resetNetwork()
                }.setNegativeButton("No") { dialog: DialogInterface, _: Int -> dialog.dismiss() }.create()
        a.window?.setBackgroundDrawableResource(android.R.color.transparent)
        a.show()
    }

    private fun speedTest(){
        speedDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        speedDialog.show()
        if(!speedDialogCheck){
            viewModel.treehousesInternet()
        }
    }

    private fun initializeSpeedDialog() {
        speedDialog = Dialog(requireContext())
        dialogSpeedtestBinding = DialogSpeedtestBinding.inflate(layoutInflater)
        speedDialog.setContentView(dialogSpeedtestBinding.root)
        speedDialogDismiss = speedDialog.findViewById(R.id.speedBtnDismiss); speedDialogTest = speedDialog.findViewById(R.id.speedBtnTest)
        speedDialogDismiss.setOnClickListener(this); speedDialogTest.setOnClickListener(this)
        viewModel.downloadUpload.observe(viewLifecycleOwner) {
            dialogSpeedtestBinding.speedText.text = it
        }
        viewModel.dialogCheck.observe(viewLifecycleOwner) {
            speedDialogCheck = it
        }
    }

    private fun reverseLookup(){
        val dialog  = createRemoteReverseDialog(context)
        dialog!!.show()
        viewModel.remoteNetworkText.observe(viewLifecycleOwner) {
            Utils.setObserverMessage(dialog, it)
        }
        viewModel.treehousesRemoteReverse()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == Constants.NETWORK_BOTTOM_SHEET && data!!.getBooleanExtra(CLICKED_START_CONFIG, false)) {
            binding.networkPbar.visibility = View.VISIBLE
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
                val fragmentManager = bottomSheetDialogFragment.parentFragmentManager
                val dialogFrag = WifiDialogFragment.newInstance()
                dialogFrag.setTargetFragment(bottomSheetDialogFragment, Constants.REQUEST_DIALOG_WIFI)
                dialogFrag.show(fragmentManager, "wifiDialog")
            }
        }
    }
}