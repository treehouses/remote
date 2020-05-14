package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

class BridgeBottomSheet : BaseBottomSheetDialog() {
    private var essid: EditText? = null
    private var password: EditText? = null
    private var hotspotEssid: EditText? = null
    private var hotspotPassword: EditText? = null
    private var startConfig: Button? = null
    private var addProfile: Button? = null
    private var btnWifiSearch: Button? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_bridge, container, false)
        essid = v.findViewById(R.id.et_essid)
        password = v.findViewById(R.id.et_password)
        hotspotEssid = v.findViewById(R.id.et_hotspot_essid)
        hotspotPassword = v.findViewById(R.id.et_hotspot_password)
        startConfig = v.findViewById(R.id.btn_start_config)
        addProfile = v.findViewById(R.id.add_bridge_profile)
        btnWifiSearch = v.findViewById(R.id.btnWifiSearch)
        try {
            essid.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
            hotspotEssid.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        startConfigListener()
        setAddProfileListener()
        btnWifiSearch.setOnClickListener(View.OnClickListener { v1: View? -> openWifiDialog(this@BridgeBottomSheet, context) })
        val validation = TextBoxValidation(context, essid, hotspotEssid, "bridge")
        validation.setStart(startConfig)
        validation.setAddprofile(addProfile)
        return v
    }

    private fun startConfigListener() {
        startConfig!!.setOnClickListener { v: View? ->
            val temp = "treehouses bridge \"" + essid!!.text.toString() + "\" \"" + hotspotEssid!!.text.toString() + "\" "
            var overallMessage = if (TextUtils.isEmpty(password!!.text.toString())) temp + "\"\"" else temp + "\"" + password!!.text.toString() + "\""
            overallMessage += " "
            if (!TextUtils.isEmpty(hotspotPassword!!.text.toString())) {
                overallMessage += "\"" + hotspotPassword!!.text.toString() + "\""
            }
            listener.sendMessage(overallMessage)
            Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
            val intent = Intent()
            intent.putExtra(CLICKED_START_CONFIG, true)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            dismiss()
        }
    }

    private fun setAddProfileListener() {
        addProfile!!.setOnClickListener { v: View? ->
            val networkProfile = NetworkProfile(essid!!.text.toString(), password!!.text.toString(),
                    hotspotEssid!!.text.toString(), hotspotPassword!!.text.toString())
            SaveUtils.addProfile(context, networkProfile)
            Toast.makeText(context, "Bridge Profile Added", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == Constants.REQUEST_DIALOG_WIFI) {
            val ssid = data!!.getStringExtra(WifiDialogFragment.WIFI_SSID_KEY)
            essid!!.setText(ssid)
        }
    }
}