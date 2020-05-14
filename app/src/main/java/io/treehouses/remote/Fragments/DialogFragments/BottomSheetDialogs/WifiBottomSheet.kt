package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import io.treehouses.remote.Constants
import io.treehouses.remote.Fragments.DialogFragments.WifiDialogFragment
import io.treehouses.remote.Fragments.NewNetworkFragment.CLICKED_START_CONFIG
import io.treehouses.remote.Fragments.NewNetworkFragment.openWifiDialog
import io.treehouses.remote.Fragments.TextBoxValidation
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseBottomSheetDialog
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.utils.SaveUtils

class WifiBottomSheet : BaseBottomSheetDialog() {
    private var ssidText: EditText? = null
    private var passwordText: EditText? = null
    private var startConfig: Button? = null
    private var addProfile: Button? = null
    private var searchWifi: Button? = null
    private var hiddenEnabled: CheckBox? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_wifi, container, false)
        ssidText = v.findViewById(R.id.editTextSSID)
        passwordText = v.findViewById(R.id.wifipassword)
        startConfig = v.findViewById(R.id.btn_start_config)
        addProfile = v.findViewById(R.id.set_wifi_profile)
        searchWifi = v.findViewById(R.id.btnWifiSearch)
        hiddenEnabled = v.findViewById(R.id.checkBoxHiddenWifi)
        setStartConfigListener()
        setAddProfileListener()
        searchWifi.setOnClickListener(View.OnClickListener { v1: View? -> openWifiDialog(this@WifiBottomSheet, context) })
        val validation = TextBoxValidation(context, ssidText, passwordText, "wifi")
        validation.setStart(startConfig)
        validation.setAddprofile(addProfile)
        validation.setTextInputLayout(v.findViewById(R.id.textInputLayout))
        return v
    }

    private fun setStartConfigListener() {
        startConfig!!.setOnClickListener { v: View? ->
            val ssid = ssidText!!.text.toString()
            val password = passwordText!!.text.toString()
            if (hiddenEnabled!!.isChecked) listener.sendMessage(String.format("treehouses wifihidden \"%s\" \"%s\"", ssid, password)) else listener.sendMessage(String.format("treehouses wifi \"%s\" \"%s\"", ssid, password))
            Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
            val intent = Intent()
            intent.putExtra(CLICKED_START_CONFIG, true)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            dismiss()
        }
    }

    private fun setAddProfileListener() {
        addProfile!!.setOnClickListener { v: View? ->
            SaveUtils.addProfile(context, NetworkProfile(ssidText!!.text.toString(), passwordText!!.text.toString()))
            Toast.makeText(context, "WiFi Profile Saved", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_DIALOG_WIFI) {
            ssidText!!.setText(data!!.getStringExtra(WifiDialogFragment.WIFI_SSID_KEY))
        }
    }
}