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
import io.treehouses.remote.databinding.DialogEthernetBinding
import io.treehouses.remote.ui.network.NetworkViewModel

class EthernetBottomSheet : BaseBottomSheetDialog() {
    protected val viewModel: NetworkViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private lateinit var bind: DialogEthernetBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogEthernetBinding.inflate(inflater, container, false)
        bind.btnStartConfig.setOnClickListener {
            viewModel.ethernetStartConfigListener(bind.ip, bind.mask, bind.gateway, bind.dns)
           // val intent = Intent()
          //  intent.putExtra(NetworkFragment.CLICKED_START_CONFIG, true)
          //  targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            dismiss()
        }
        return bind.root
    }
}