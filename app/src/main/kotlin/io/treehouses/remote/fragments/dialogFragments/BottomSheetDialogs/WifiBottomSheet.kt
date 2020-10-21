package io.treehouses.remote.fragments.dialogFragments.BottomSheetDialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.treehouses.remote.Constants
import io.treehouses.remote.fragments.dialogFragments.WifiDialogFragment
import io.treehouses.remote.fragments.NetworkFragment
import io.treehouses.remote.fragments.NetworkFragment.Companion.openWifiDialog
import io.treehouses.remote.fragments.TextBoxValidation
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
        hiddenOrEnterprise()
        bind.btnWifiSearch.setOnClickListener { openWifiDialog(this@WifiBottomSheet, context) }
        val validation = TextBoxValidation(requireContext(), bind.editTextSSID, bind.wifipassword, "wifi")
        validation.setStart(bind.btnStartConfig)
        validation.setAddprofile(bind.setWifiProfile)
        validation.setTextInputLayout(bind.textInputLayout)
        return bind.root
    }

    private fun setStartConfigListener() {
        bind.btnStartConfig.setOnClickListener {
            val ssid = bind.editTextSSID.text.toString()
            val password = bind.wifipassword.text.toString()
            val username = bind.wifiUsername.text.toString()
            if (bind.checkBoxEnterprise.isChecked && bind.wifiUsername.text.isNullOrEmpty()) {
                bind.wifiUsername.error = "Please enter a username"
                return@setOnClickListener
            }
            sendMessage(ssid, password, username)

            val intent = Intent()
            intent.putExtra(NetworkFragment.CLICKED_START_CONFIG, true)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            dismiss()
        }
    }

    private fun sendMessage(ssid:String, password: String, username: String) {
        val hidden = bind.checkBoxHiddenWifi.isChecked
        val enterprise = bind.checkBoxEnterprise.isChecked
        when {
            !enterprise -> listener.sendMessage(getString(if (hidden) R.string.TREEHOUSES_WIFI_HIDDEN else R.string.TREEHOUSES_WIFI, ssid, password))
            enterprise -> listener.sendMessage(getString(if (hidden) R.string.TREEHOUSES_WIFI_HIDDEN_ENTERPRISE else R.string.TREEHOUSES_WIFI_ENTERPRISE, ssid, password, username))
        }
        Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
    }

    private fun hiddenOrEnterprise() {
        bind.checkBoxEnterprise.setOnCheckedChangeListener {_, isChecked ->
            bind.enterpriseLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun setAddProfileListener() {
        bind.setWifiProfile.setOnClickListener {
            SaveUtils.addProfile(requireContext(), NetworkProfile(
                    bind.editTextSSID.text.toString(),
                    bind.wifipassword.text.toString(),
                    bind.checkBoxHiddenWifi.isChecked))
            Toast.makeText(context, "WiFi Profile Saved", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_DIALOG_WIFI) {
            bind.editTextSSID.setText(data?.getStringExtra(WifiDialogFragment.WIFI_SSID_KEY))
        }
    }
}