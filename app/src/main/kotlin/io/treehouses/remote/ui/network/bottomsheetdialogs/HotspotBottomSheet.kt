package io.treehouses.remote.ui.network.bottomsheetdialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import io.treehouses.remote.ui.network.NetworkFragment
import io.treehouses.remote.bases.BaseBottomSheetDialog
import io.treehouses.remote.databinding.DialogHotspotBinding
import io.treehouses.remote.ui.network.NetworkViewModel

class HotspotBottomSheet : BaseBottomSheetDialog() {
    protected val viewModel: NetworkViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private lateinit var bind: DialogHotspotBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogHotspotBinding.inflate(inflater, container, false)
        bind.btnStartConfig.setOnClickListener {
         //   val intent = Intent()
          //  intent.putExtra(NetworkFragment.CLICKED_START_CONFIG, true)
            viewModel.hotspotStartConfigListener(bind.checkBoxHiddenHotspot, bind.spnHotspotType,
                    bind.etHotspotSsid, bind.etHotspotPassword)
            dismiss()
        }
        bind.setHotspotProfile.setOnClickListener {
            viewModel.hotspotSetAddProfileListener(bind.checkBoxHiddenHotspot, bind.spnHotspotType,
                    bind.etHotspotSsid, bind.etHotspotPassword)
        }
        return bind.root
    }

}