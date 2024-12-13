package io.treehouses.remote.ui.network.bottomsheetdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import io.treehouses.remote.bases.BaseBottomSheetDialog
import io.treehouses.remote.databinding.DialogHotspotBinding
import io.treehouses.remote.ui.network.NetworkViewModel

open class HotspotBottomSheet : BaseBottomSheetDialog() {
    protected val viewModel: NetworkViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private lateinit var bind: DialogHotspotBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = DialogHotspotBinding.inflate(inflater, container, false)
        bind.btnStartConfig.setOnClickListener {
            viewModel.hotspotStartConfigListener("${bind.etHotspotSsid.text}", "${bind.etHotspotPassword.text}",
                    bind.checkBoxHiddenHotspot.isChecked, "${bind.spnHotspotType.selectedItem}"
            )
            dismiss()
        }
        bind.setHotspotProfile.setOnClickListener {
            viewModel.hotspotSetAddProfileListener(bind.checkBoxHiddenHotspot.isChecked, "${bind.spnHotspotType.selectedItem}", "${bind.etHotspotSsid.text}", "${bind.etHotspotPassword.text}")
        }
        return bind.root
    }

}