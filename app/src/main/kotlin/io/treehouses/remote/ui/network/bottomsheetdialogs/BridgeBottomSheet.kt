package io.treehouses.remote.ui.network.bottomsheetdialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.treehouses.remote.Constants
import io.treehouses.remote.fragments.dialogfragments.WifiDialogFragment
import io.treehouses.remote.ui.network.NetworkFragment
import io.treehouses.remote.ui.network.NetworkFragment.Companion.openWifiDialog
import io.treehouses.remote.fragments.TextBoxValidation
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseBottomSheetDialog
import io.treehouses.remote.databinding.DialogBridgeBinding
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.utils.SaveUtils

class BridgeBottomSheet : BaseBottomSheetDialog() {
    private lateinit var bind: DialogBridgeBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogBridgeBinding.inflate(inflater, container, false)
        try {
            bind.etEssid.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            bind.etHotspotEssid.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        } catch (e: Exception) {
            e.printStackTrace()
        }
        startConfigListener()
        setAddProfileListener()
        bind.btnWifiSearch.setOnClickListener { openWifiDialog(this@BridgeBottomSheet, context) }
        val validation = TextBoxValidation(requireContext(), bind.etEssid, bind.etHotspotEssid, "bridge")
        validation.setStart(bind.btnStartConfig)
        validation.setAddprofile(bind.addBridgeProfile)
        return bind.root
    }

    private fun startConfigListener() {
        bind.btnStartConfig.setOnClickListener {
            listener.sendMessage(getString(R.string.TREEHOUSES_BRIDGE, bind.etEssid.text.toString(), bind.etHotspotEssid.text.toString(),
                    bind.etPassword.text.toString(), bind.etHotspotPassword.text.toString()))
            Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
            val intent = Intent()
            intent.putExtra(NetworkFragment.CLICKED_START_CONFIG, true)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            dismiss()
        }
    }

    private fun setAddProfileListener() {
        bind.addBridgeProfile.setOnClickListener {
            val networkProfile = NetworkProfile(bind.etEssid.text.toString(), bind.etPassword.text.toString(),
                    bind.etHotspotEssid.text.toString(), bind.etHotspotPassword.text.toString())
            SaveUtils.addProfile(requireContext(), networkProfile)
            Toast.makeText(context, "Bridge Profile Added", Toast.LENGTH_LONG).show()
        }
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