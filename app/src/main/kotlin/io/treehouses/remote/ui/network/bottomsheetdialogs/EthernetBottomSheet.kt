package io.treehouses.remote.ui.network.bottomsheetdialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import io.treehouses.remote.bases.BaseBottomSheetDialog
import io.treehouses.remote.databinding.DialogEthernetBinding
import io.treehouses.remote.ui.network.NetworkViewModel

class EthernetBottomSheet : BaseBottomSheetDialog() {
    protected val viewModel: NetworkViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private lateinit var bind: DialogEthernetBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        bind = DialogEthernetBinding.inflate(inflater, container, false)
        bind.btnStartConfig.setOnClickListener {
            viewModel.ethernetStartConfigListener(bind.ip.text.toString(), bind.mask.text.toString(),
                    bind.gateway.text.toString(), bind.dns.text.toString())
            dismiss()
        }
        return bind.root
    }
}