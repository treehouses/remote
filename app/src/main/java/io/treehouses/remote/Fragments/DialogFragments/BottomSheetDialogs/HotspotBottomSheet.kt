package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.treehouses.remote.Fragments.NewNetworkFragment
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseBottomSheetDialog
import io.treehouses.remote.databinding.DialogHotspotBinding
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.utils.SaveUtils

class HotspotBottomSheet : BaseBottomSheetDialog() {
    private lateinit var bind: DialogHotspotBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogHotspotBinding.inflate(inflater, container, false)
        startConfigListener()
        setAddProfileListener()
        return bind.root
    }

    private fun startConfigListener() {
        bind.btnStartConfig.setOnClickListener {
            if (bind.checkBoxHiddenHotspot.isChecked) sendMessage(R.string.TREEHOUSES_AP_HIDDEN)
            else sendMessage(R.string.TREEHOUSES_AP)

            Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show()
            val intent = Intent()
            intent.putExtra(NewNetworkFragment.CLICKED_START_CONFIG, true)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            dismiss()
        }
    }

    private fun sendMessage(command : Int) {
        listener.sendMessage(getString(command, bind.spnHotspotType.selectedItem.toString(),
                bind.etHotspotSsid.text.toString(), bind.etHotspotPassword.text.toString()))
    }

    private fun setAddProfileListener() {
        bind.setHotspotProfile.setOnClickListener {
            SaveUtils.addProfile(context,
                    NetworkProfile(
                            bind.etHotspotSsid.text.toString(),
                            bind.etHotspotPassword.text.toString(),
                            bind.spnHotspotType.selectedItem.toString(),
                            bind.checkBoxHiddenHotspot.isChecked))
            Toast.makeText(context, "Hotspot Profile Saved", Toast.LENGTH_LONG).show()
        }
    }
}