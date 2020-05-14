package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.treehouses.remote.Fragments.NewNetworkFragment.CLICKED_START_CONFIG
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseBottomSheetDialog
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.utils.SaveUtils

class HotspotBottomSheet : BaseBottomSheetDialog() {
    private var essidText: EditText? = null
    private var passwordText: EditText? = null
    private var startConfig: Button? = null
    private var addProfile: Button? = null
    private var spinner: Spinner? = null
    private var hiddenEnabled: CheckBox? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_hotspot, container, false)
        essidText = v.findViewById(R.id.et_hotspot_ssid)
        passwordText = v.findViewById(R.id.et_hotspot_password)
        startConfig = v.findViewById(R.id.btn_start_config)
        addProfile = v.findViewById(R.id.set_hotspot_profile)
        spinner = v.findViewById(R.id.spn_hotspot_type)
        hiddenEnabled = v.findViewById(R.id.checkBoxHiddenWifi)
        startConfigListener()
        setAddProfileListener()
        return v
    }

    private fun startConfigListener() {
        startConfig!!.setOnClickListener { v: View? ->
            val command = if (hiddenEnabled!!.isChecked) "treehouses hiddenap " else "treehouses ap "
            if (passwordText!!.text.toString().isEmpty()) {
                listener.sendMessage(command + "\"" + spinner!!.selectedItem.toString() + "\" \"" + essidText!!.text.toString() + "\"")
                Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
            } else {
                listener.sendMessage(command + "\"" + spinner!!.selectedItem.toString() + "\" \"" + essidText!!.text.toString() + "\" \"" + passwordText!!.text.toString() + "\"")
                Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
            }
            val intent = Intent()
            intent.putExtra(CLICKED_START_CONFIG, true)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            dismiss()
        }
    }

    private fun setAddProfileListener() {
        addProfile!!.setOnClickListener { v: View? ->
            SaveUtils.addProfile(context, NetworkProfile(essidText!!.text.toString(), passwordText!!.text.toString(), spinner!!.selectedItem.toString()))
            Toast.makeText(context, "Hotspot Profile Saved", Toast.LENGTH_LONG).show()
        }
    }
}