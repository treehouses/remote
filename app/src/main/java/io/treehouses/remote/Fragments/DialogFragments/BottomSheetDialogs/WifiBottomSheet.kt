package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.treehouses.remote.Constants
import io.treehouses.remote.Fragments.DialogFragments.WifiDialogFragment
import io.treehouses.remote.Fragments.NewNetworkFragment
import io.treehouses.remote.Fragments.NewNetworkFragment.Companion.openWifiDialog
import io.treehouses.remote.Fragments.TextBoxValidation
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseBottomSheetDialog
import io.treehouses.remote.databinding.DialogWifiBinding
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.utils.SaveUtils

class WifiBottomSheet : BaseBottomSheetDialog() {
    private lateinit var bind: DialogWifiBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogWifiBinding.inflate(inflater, container, false)
        setStartConfigListener()
        setAddProfileListener()
        bind.btnWifiSearch.setOnClickListener { openWifiDialog(this@WifiBottomSheet, context) }
        val validation = TextBoxValidation(getContext(), bind.editTextSSID, bind.wifipassword, "wifi")
        validation.setStart(bind.btnStartConfig)
        validation.setAddprofile(bind.setWifiProfile)
        validation.setTextInputLayout(bind.textInputLayout)
        return bind.root
    }

    private fun setStartConfigListener() {
        bind.btnStartConfig.setOnClickListener {
            val ssid = bind.editTextSSID.text.toString()
            val password = bind.wifipassword.text.toString()
            if (bind.checkBoxHiddenWifi.isChecked) listener.sendMessage(getString(R.string.TREEHOUSES_WIFI_HIDDEN, ssid, password))
            else listener.sendMessage(getString(R.string.TREEHOUSES_WIFI, ssid, password))
            Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
            val intent = Intent()
            intent.putExtra(NewNetworkFragment.CLICKED_START_CONFIG, true)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            dismiss()
        }
    }

    private fun setAddProfileListener() {
        bind.setWifiProfile.setOnClickListener {
            SaveUtils.addProfile(context, NetworkProfile(bind.editTextSSID.text.toString(), bind.wifipassword.text.toString()))
            Toast.makeText(context, "WiFi Profile Saved", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_DIALOG_WIFI) {
            bind.editTextSSID.setText(data!!.getStringExtra(WifiDialogFragment.WIFI_SSID_KEY))
        }
    }
}