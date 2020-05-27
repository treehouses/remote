package io.treehouses.remote.Fragments.DialogFragments.BottomSheetDialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.treehouses.remote.Fragments.NewNetworkFragment
import io.treehouses.remote.R
import io.treehouses.remote.bases.BaseBottomSheetDialog
import io.treehouses.remote.databinding.DialogEthernetBinding

class EthernetBottomSheet : BaseBottomSheetDialog() {
    private lateinit var bind: DialogEthernetBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind = DialogEthernetBinding.inflate(inflater, container, false)
        bind.btnStartConfig.setOnClickListener {
            val ip = bind.ip.text.toString()
            val dns = bind.dns.text.toString()
            val gateway = bind.gateway.text.toString()
            val mask = bind.mask.text.toString()
            listener.sendMessage(getString(R.string.TREEHOUSES_ETHERNET, ip, mask, gateway, dns))
            val intent = Intent()
            intent.putExtra(NewNetworkFragment.CLICKED_START_CONFIG, true)
            targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
            dismiss()
        }
        return bind.root
    }
}