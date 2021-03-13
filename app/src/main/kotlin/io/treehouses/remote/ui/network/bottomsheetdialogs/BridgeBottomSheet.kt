package io.treehouses.remote.ui.network.bottomsheetdialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import io.treehouses.remote.Constants
import io.treehouses.remote.fragments.dialogfragments.WifiDialogFragment
import io.treehouses.remote.ui.network.NetworkFragment
import io.treehouses.remote.ui.network.NetworkFragment.Companion.openWifiDialog
import io.treehouses.remote.fragments.TextBoxValidation
import io.treehouses.remote.bases.BaseBottomSheetDialog
import io.treehouses.remote.databinding.DialogBridgeBinding
import io.treehouses.remote.ui.network.NetworkViewModel

class BridgeBottomSheet : BaseBottomSheetDialog() {
    protected val viewModel: NetworkViewModel by viewModels(ownerProducer = { this })
    private lateinit var bind: DialogBridgeBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogBridgeBinding.inflate(inflater, container, false)
        try {
            bind.etEssid.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            bind.etHotspotEssid.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bind.btnStartConfig.setOnClickListener {
            viewModel.bridgeStartConfigListener(bind)
            val intent = Intent()
            intent.putExtra(NetworkFragment.CLICKED_START_CONFIG, true)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            dismiss()
        }
        bind.addBridgeProfile.setOnClickListener {
            viewModel.bridgeSetAddProfileListener(bind)
        }
        bind.btnWifiSearch.setOnClickListener { openWifiDialog(this@BridgeBottomSheet, context) }
        val validation = TextBoxValidation(requireContext(), bind.etEssid, bind.etHotspotEssid, "bridge")
        validation.setStart(bind.btnStartConfig)
        validation.setAddprofile(bind.addBridgeProfile)
        return bind.root
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == Constants.REQUEST_DIALOG_WIFI) {
            val ssid = data!!.getStringExtra(WifiDialogFragment.WIFI_SSID_KEY)
            bind.etEssid.setText(ssid)
        }
    }
}