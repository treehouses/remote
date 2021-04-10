package io.treehouses.remote.ui.network.bottomsheetdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import io.treehouses.remote.bases.BaseBottomSheetDialog
import io.treehouses.remote.databinding.DialogHotspotBinding
import io.treehouses.remote.pojo.NetworkProfile
import io.treehouses.remote.ui.network.NetworkViewModel

class HotspotBottomSheet : BaseBottomSheetDialog() {
    protected val viewModel: NetworkViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private lateinit var bind: DialogHotspotBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogHotspotBinding.inflate(inflater, container, false)
        bind.btnStartConfig.setOnClickListener {
            viewModel.hotspotStartConfigListener(bind.checkBoxHiddenHotspot.isChecked, getValuesMap())
//            viewModel.hotspotStartConfigListener(bind.etHotspotSsid.text.toString(), bind.etHotspotPassword.text.toString(),
//                    bind.checkBoxHiddenHotspot.isChecked, bind.spnHotspotType.selectedItem.toString())
            dismiss()
        }
        bind.setHotspotProfile.setOnClickListener {
            val networkProfile = NetworkProfile(bind.etHotspotSsid.text.toString(), bind.etHotspotPassword.text.toString(),
                    bind.spnHotspotType.selectedItem.toString(), bind.checkBoxHiddenHotspot.isChecked)
            viewModel.bridgeHotspotWifiSetAddProfileListener("Hotspot Profile Added", networkProfile)
//            viewModel.hotspotSetAddProfileListener(bind.checkBoxHiddenHotspot.isChecked, bind.spnHotspotType.selectedItem.toString(),
//                    bind.etHotspotSsid.text.toString(), bind.etHotspotPassword.text.toString())
        }
        return bind.root
    }

    private fun getValuesMap(): Map<String, String> {
        return mapOf("spnHotspotType" to bind.spnHotspotType.selectedItem.toString(), "etHotspotSsid" to bind.etHotspotSsid.text.toString(),
                "etHotspotPassword" to bind.etHotspotPassword.text.toString());
    }

}